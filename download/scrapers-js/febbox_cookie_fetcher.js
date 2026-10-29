/**
 * FebBox Cookie Auto-Fetcher
 * ==========================
 * FebBox (febbox.com) requires a `ui` JWT cookie for stream URL resolution.
 * The login is Google OAuth only (no email/password signup).
 *
 * This script:
 *   1. Opens FebBox in a browser
 *   2. Waits for the user to log in (via Google OAuth)
 *   3. Automatically captures the `ui` cookie when it appears
 *   4. Saves it to ~/.febbox_cookie or prints it for export
 *
 * The cookie is valid for ~1 year and gives 100 GB/month download quota.
 *
 * Usage:
 *   node febbox_cookie_fetcher.js           # interactive - opens browser
 *   node febbox_cookie_fetcher.js --print   # print cookie and exit
 *   node febbox_cookie_fetcher.js --check   # check if existing cookie is valid
 *
 * Install:
 *   npm install playwright
 *   npx playwright install chromium
 */

'use strict';

const fs = require('fs');
const path = require('path');
const os = require('os');

const COOKIE_FILE = path.join(os.homedir(), '.febbox_cookie');
const FEBBOX_URL = 'https://www.febbox.com';
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 ' +
           '(KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';

// ==================== Cookie Storage ====================

function saveCookie(cookie) {
  fs.writeFileSync(COOKIE_FILE, cookie, '0600');
  console.log(`✓ Cookie saved to ${COOKIE_FILE}`);
}

function loadCookie() {
  try {
    return fs.readFileSync(COOKIE_FILE, 'utf8').trim();
  } catch (e) {
    return null;
  }
}

// ==================== Cookie Validation ====================

async function checkCookieQuota(cookie) {
  const axios = require('axios');
  try {
    const r = await axios.get(`${FEBBOX_URL}/console/user_cards`, {
      headers: {
        Cookie: cookie.startsWith('ui=') ? cookie : `ui=${cookie}`,
        'User-Agent': UA,
        'Accept': 'application/json',
      },
      timeout: 8000,
      validateStatus: () => true,
    });
    if (r.status === 200 && r.data?.data?.flow) {
      const f = r.data.data.flow;
      const remaining = (Number(f.traffic_limit_mb) || 0) - (Number(f.traffic_usage_mb) || 0);
      return {
        valid: true,
        remainingMB: remaining,
        limitMB: Number(f.traffic_limit_mb) || 0,
        usageMB: Number(f.traffic_usage_mb) || 0,
        plan: f.plan_name || 'free',
      };
    }
    return { valid: false, status: r.status };
  } catch (e) {
    return { valid: false, error: e.message };
  }
}

// ==================== Browser-Based Cookie Fetcher ====================

async function fetchCookieViaBrowser() {
  let chromium;
  try {
    chromium = require('playwright-extra');
    const stealth = require('puppeteer-extra-plugin-stealth');
    chromium.use(stealth());
  } catch (e) {
    chromium = require('playwright');
  }

  const browser = await chromium.chromium.launch({
    headless: false,  // MUST be headed so user can interact with Google OAuth
    args: ['--no-sandbox', '--disable-blink-features=AutomationControlled'],
  });

  const context = await browser.newContext({
    userAgent: UA,
    viewport: { width: 1280, height: 800 },
    locale: 'en-US',
  });

  const page = await context.newPage();

  console.log('Opening FebBox login page...');
  console.log('Please log in using your Google account.');
  console.log('The script will automatically detect when login is complete.\n');

  await page.goto(`${FEBBOX_URL}/account`);

  // Poll for the `ui` cookie
  let cookie = null;
  const maxWait = 5 * 60 * 1000;  // 5 minutes
  const startTime = Date.now();

  while (Date.now() - startTime < maxWait) {
    const cookies = await context.cookies();
    const uiCookie = cookies.find(c => c.name === 'ui' && c.value && c.value !== 'deleted');

    if (uiCookie) {
      cookie = `ui=${uiCookie.value}`;
      break;
    }

    // Show a waiting message every 10 seconds
    const elapsed = Math.floor((Date.now() - startTime) / 1000);
    if (elapsed % 10 === 0) {
      process.stdout.write(`\r  Waiting for login... (${elapsed}s elapsed)`);
    }

    await page.waitForTimeout(1000);
  }

  console.log();  // newline after the waiting message

  if (!cookie) {
    console.error('✗ Timed out waiting for login. Please try again.');
    await browser.close();
    return null;
  }

  console.log('\n✓ Login detected! Captured `ui` cookie.');

  // Validate the cookie
  console.log('Validating cookie...');
  const quota = await checkCookieQuota(cookie);
  if (quota.valid) {
    console.log(`✓ Cookie is valid!`);
    console.log(`  Plan: ${quota.plan}`);
    console.log(`  Quota: ${quota.usageMB} MB used / ${quota.limitMB} MB total`);
    console.log(`  Remaining: ${quota.remainingMB} MB`);
  } else {
    console.log('⚠ Cookie validation failed. The cookie may still work for /file/share_download.');
  }

  await browser.close();
  return cookie;
}

// ==================== CLI ====================

(async () => {
  const args = process.argv.slice(2);

  // --check: validate existing cookie
  if (args.includes('--check')) {
    const cookie = loadCookie();
    if (!cookie) {
      console.log('No cookie found. Run: node febbox_cookie_fetcher.js');
      process.exit(1);
    }
    console.log('Checking cookie...');
    const quota = await checkCookieQuota(cookie);
    if (quota.valid) {
      console.log(`✓ Cookie is valid!`);
      console.log(`  Plan: ${quota.plan}`);
      console.log(`  Remaining: ${quota.remainingMB} MB of ${quota.limitMB} MB`);
    } else {
      console.log(`✗ Cookie is invalid or expired. (status: ${quota.status})`);
      console.log('  Run: node febbox_cookie_fetcher.js');
      process.exit(1);
    }
    return;
  }

  // Fetch cookie via browser
  const cookie = await fetchCookieViaBrowser();
  if (!cookie) {
    process.exit(1);
  }

  // Save cookie
  saveCookie(cookie);

  if (args.includes('--print')) {
    console.log('\n=== Cookie value ===');
    console.log(cookie);
    console.log('\nTo use with scrapers:');
    console.log(`  export FEBBOX_COOKIE="${cookie}"`);
  } else {
    console.log(`\nTo use with scrapers:`);
    console.log(`  export FEBBOX_COOKIE="${cookie.slice(0, 30)}..."`);
    console.log(`  (full cookie saved to ${COOKIE_FILE})`);
  }
})();
