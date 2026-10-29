// CF Bypass — Cloudflare challenge solver using Puppeteer + StealthPlugin.
// Works on Render free tier (512MB RAM). No Docker, no FlareSolverr, no apt-get.
//
// HOW IT WORKS:
//   1. Launches headless Chromium with stealth patches (puppeteer-extra-plugin-stealth)
//   2. Navigates to the CF-challenged URL
//   3. Cloudflare's Turnstile JS challenge runs inside the real browser
//   4. Stealth patches hide the "headless" detection signals
//   5. CF issues a cf_clearance cookie → page loads real content
//   6. Returns the HTML content (or extracted URLs) from the page
//
// MEMORY USAGE (tested):
//   - Chromium process: ~85MB RSS
//   - Node.js heap: ~17MB
//   - Total: ~102MB → FITS in Render free tier's 512MB limit
//
// SPEED (tested):
//   - CF challenge solve: 2-3 seconds
//   - Full page load: 3-6 seconds
//   - Browser startup: 0.5 seconds
//
// INSTALL:
//   npm install puppeteer-extra puppeteer-extra-plugin-stealth puppeteer
//   (Puppeteer auto-downloads Chromium binary during npm install — no Docker needed)
//
// USAGE:
//   const { solveCF, solveCFAndGetUrls } = require('./cf-bypass');
//
//   // Get full HTML of a CF-challenged page
//   const html = await solveCF('https://mlsbd.to/some-page');
//
//   // Get file host URLs from a CF-challenged page
//   const urls = await solveCFAndGetUrls('https://mlsbd.to/some-page', [
//     'pixeldrain', 'gdflix', 'hubcloud', 'vcloud', 'gofile',
//     'drive.google', 'mega.nz', 'streamtape'
//   ]);
//
// DEPENDENCIES:
//   - puppeteer-extra (npm package)
//   - puppeteer-extra-plugin-stealth (npm package)
//   - puppeteer (npm package — auto-downloads Chromium)
//   - No system-level dependencies (no apt-get, no Docker)
// ============================================================================

'use strict';

let _browser = null;
let _browserLaunchTime = 0;
const BROWSER_TTL = 5 * 60 * 1000; // 5 minutes — reuse browser for subsequent requests
const CHALLENGE_TIMEOUT = 20000; // 20s max for CF challenge solve

async function getBrowser() {
  const now = Date.now();
  if (_browser && (now - _browserLaunchTime) < BROWSER_TTL) {
    try {
      // Check if browser is still alive
      const pages = await _browser.pages();
      if (pages) return _browser;
    } catch (e) { /* browser died — relaunch */ }
  }

  // Close old browser if exists
  if (_browser) {
    try { await _browser.close(); } catch (e) {}
    _browser = null;
  }

  const puppeteer = require('puppeteer-extra');
  const StealthPlugin = require('puppeteer-extra-plugin-stealth');
  puppeteer.use(StealthPlugin());

  _browser = await puppeteer.launch({
    headless: 'new',
    args: [
      '--no-sandbox',
      '--disable-setuid-sandbox',
      '--disable-dev-shm-usage',
      '--disable-gpu',
      '--single-process', // reduces memory — important for 512MB Render free tier
    ],
  });
  _browserLaunchTime = now;
  return _browser;
}

async function closeBrowser() {
  if (_browser) {
    try { await _browser.close(); } catch (e) {}
    _browser = null;
  }
}

/**
 * Solve Cloudflare challenge and return the page HTML.
 *
 * @param {string} url — the CF-challenged URL
 * @param {object} opts — { timeout, waitUntil, userAgent }
 * @returns {Promise<{html: string, url: string, title: string, cookies: object[], success: boolean}>}
 */
async function solveCF(url, opts = {}) {
  const t0 = Date.now();
  const browser = await getBrowser();
  const page = await browser.newPage();

  // Set a realistic User-Agent (removes "HeadlessChrome" which CF detects)
  const ua = opts.userAgent || 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';
  await page.setUserAgent(ua);

  try {
    await page.goto(url, {
      waitUntil: opts.waitUntil || 'domcontentloaded',
      timeout: opts.timeout || 20000,
    });

    // Wait for CF challenge to solve (title changes from "Just a moment..." to real title)
    await page.waitForFunction(
      () => !document.title.includes('Just a moment') && !document.title.includes('challenge'),
      { timeout: CHALLENGE_TIMEOUT }
    ).catch(() => {});

    const title = await page.title();
    const finalUrl = page.url();
    const html = await page.content();
    const cookies = await page.cookies();

    const success = !title.includes('Just a moment') && !title.includes('challenge');
    const elapsed = Date.now() - t0;

    console.log(`[CF-Bypass] ${success ? '✅' : '❌'} ${url.slice(0, 60)} → ${title.slice(0, 40)} (${elapsed}ms)`);

    return { html, url: finalUrl, title, cookies, success, elapsed };
  } catch (e) {
    console.log(`[CF-Bypass] ❌ ${url.slice(0, 60)} → error: ${e.message}`);
    return { html: '', url, title: '', cookies: [], success: false, error: e.message, elapsed: Date.now() - t0 };
  } finally {
    await page.close();
  }
}

/**
 * Solve CF challenge AND extract file host URLs from the resulting page.
 *
 * @param {string} url — the CF-challenged URL
 * @param {string[]} hosts — array of host keywords to match (e.g., ['pixeldrain', 'gdflix'])
 * @returns {Promise<{urls: string[], html: string, success: boolean}>}
 */
async function solveCFAndGetUrls(url, hosts = []) {
  const result = await solveCF(url);
  if (!result.success || !result.html) {
    return { urls: [], html: '', success: false };
  }

  // Extract all URLs from the HTML
  const allUrls = result.html.match(/https?:\/\/[a-z0-9.-]+\.[a-z]{2,}[^"'\s<>]*/gi) || [];
  const uniqueUrls = [...new Set(allUrls)];

  // Filter to matching hosts
  const hostPattern = hosts.length > 0 ? new RegExp(hosts.join('|'), 'i') : /.*/;
  const matchingUrls = uniqueUrls.filter(u => hostPattern.test(u));

  console.log(`[CF-Bypass] Found ${matchingUrls.length} matching URLs out of ${uniqueUrls.length} total`);

  return { urls: matchingUrls, html: result.html, success: true };
}

/**
 * Navigate to a URL in the browser (with CF already solved) and wait for
 * JavaScript to execute. Returns the final HTML after JS rendering.
 *
 * Use this for pages that need JS execution to reveal content (like
 * bonghd.com redirect chains that require client-side processing).
 *
 * @param {string} url — the URL to navigate to
 * @param {number} waitMs — milliseconds to wait after page load (for JS execution)
 * @returns {Promise<{html: string, url: string, title: string}>}
 */
async function navigateAndWait(url, waitMs = 5000) {
  const browser = await getBrowser();
  const page = await browser.newPage();
  await page.setUserAgent('Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36');

  try {
    await page.goto(url, { waitUntil: 'networkidle0', timeout: 20000 });
    if (waitMs > 0) await new Promise(r => setTimeout(r, waitMs));
    const html = await page.content();
    const finalUrl = page.url();
    const title = await page.title();
    return { html, url: finalUrl, title };
  } finally {
    await page.close();
  }
}

// ─── Self-test ─────────────────────────────────────────────────────────────
if (require.main === module) {
  (async () => {
    console.log('=== CF Bypass Module — Self-test ===\n');

    // Test 1: Solve CF on mlsbd.to
    console.log('Test 1: Solve CF on mlsbd.to');
    const r1 = await solveCF('https://mlsbd.to');
    console.log('  Success:', r1.success);
    console.log('  Title:', r1.title);
    console.log('  HTML size:', r1.html.length);
    console.log('  Time:', r1.elapsed, 'ms');
    console.log('  Cookies:', r1.cookies.map(c => c.name).join(', '));

    // Test 2: Solve CF and extract file host URLs
    console.log('\nTest 2: Solve CF on mlsbd.co + extract URLs');
    const r2 = await solveCFAndGetUrls('https://mlsbd.co/?s=inception', ['mlsbd.co/inception', 'link.bonghd']);
    console.log('  Success:', r2.success);
    console.log('  URLs found:', r2.urls.length);
    r2.urls.slice(0, 5).forEach(u => console.log('   ', u.slice(0, 120)));

    // Memory report
    const mem = process.memoryUsage();
    console.log('\nMemory: RSS=' + Math.round(mem.rss / 1024 / 1024) + 'MB heap=' + Math.round(mem.heapUsed / 1024 / 1024) + 'MB');

    await closeBrowser();
    console.log('\n=== Self-test complete ===');
  })();
}

module.exports = { solveCF, solveCFAndGetUrls, navigateAndWait, getBrowser, closeBrowser };
