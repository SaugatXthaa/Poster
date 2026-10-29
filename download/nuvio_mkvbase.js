/**
 * MKVBase Scraper for Nuvio — Pure Node.js with Chrome CDP
 * ==========================================================
 * Uses Chrome (via Chrome DevTools Protocol) to bypass Cloudflare.
 * 
 * This scraper requires Chrome/Chromium to be installed.
 * It does NOT use playwright or puppeteer - just child_process + WebSocket.
 * 
 * When run on a residential IP, Chrome will solve the CF challenge
 * automatically and the scraper will get real content.
 * 
 * Nuvio format: module.exports = { getStreams }
 */

'use strict';

const { spawn } = require('child_process');
const http = require('http');
const WebSocket = require('ws');
const fs = require('fs');
const path = require('path');

const TMDB_API_KEY = '8476a7ab80ad76f0936744df0430e67c';
const TARGET_BASE = 'https://mkvbase.site';
const UA = 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';

// Find Chrome binary
function findChrome() {
  const paths = [
    '/home/z/.cache/ms-playwright/chromium-1200/chrome-linux64/chrome',
    '/usr/bin/google-chrome',
    '/usr/bin/chromium',
    '/usr/bin/chromium-browser',
    '/usr/local/bin/chrome',
  ];
  for (const p of paths) {
    if (fs.existsSync(p)) return p;
  }
  return null;
}

// Cache for cf_clearance cookie
let _cfCookie = null;
let _cfCookieTime = 0;
const CF_COOKIE_TTL = 30 * 60 * 1000; // 30 minutes

/**
 * Get cf_clearance cookie by launching Chrome and solving the challenge.
 */
async function getCFCookie() {
  // Check cache
  if (_cfCookie && Date.now() - _cfCookieTime < CF_COOKIE_TTL) {
    return _cfCookie;
  }

  const chromePath = findChrome();
  if (!chromePath) {
    throw new Error('Chrome/Chromium not found. Install Chrome or Playwright.');
  }

  return new Promise((resolve, reject) => {
    const port = 9250 + Math.floor(Math.random() * 100);
    const profileDir = '/tmp/chrome_mkvbase_profile';

    const chrome = spawn(chromePath, [
      '--no-sandbox',
      '--disable-dev-shm-usage',
      '--disable-blink-features=AutomationControlled',
      '--window-size=1920,1080',
      '--lang=en-US,en',
      `--user-data-dir=${profileDir}`,
      `--remote-debugging-port=${port}`,
    ], { stdio: 'pipe' });

    let resolved = false;
    let ws = null;

    const timeout = setTimeout(() => {
      if (!resolved) {
        resolved = true;
        if (ws) ws.close();
        chrome.kill();
        reject(new Error('Chrome timeout'));
      }
    }, 30000);

    setTimeout(async () => {
      try {
        // Get the page
        const tabs = await new Promise((resolve, reject) => {
          http.get(`http://127.0.0.1:${port}/json`, (res) => {
            let body = '';
            res.on('data', c => body += c);
            res.on('end', () => resolve(JSON.parse(body)));
          }).on('error', reject);
        });

        const page = tabs.find(t => t.type === 'page');
        if (!page) { reject(new Error('No Chrome page')); return; }

        ws = new WebSocket(page.webSocketDebuggerUrl);

        ws.on('open', () => {
          ws.send(JSON.stringify({ id: 1, method: 'Network.enable' }));
          ws.send(JSON.stringify({ id: 2, method: 'Page.enable' }));
          
          // Anti-detection
          ws.send(JSON.stringify({
            id: 3,
            method: 'Page.addScriptToEvaluateOnNewDocument',
            params: {
              source: `Object.defineProperty(navigator, 'webdriver', { get: () => false }); window.chrome = { runtime: {} };`
            }
          }));

          // Navigate to the site
          ws.send(JSON.stringify({ id: 4, method: 'Page.navigate', params: { url: TARGET_BASE + '/' } }));

          // Check for cf_clearance cookie every 2 seconds
          let checkCount = 0;
          const interval = setInterval(() => {
            checkCount++;
            ws.send(JSON.stringify({
              id: 100 + checkCount,
              method: 'Network.getCookies',
              params: { urls: [TARGET_BASE] }
            }));

            if (checkCount >= 12) {
              clearInterval(interval);
              if (!resolved) {
                resolved = true;
                clearTimeout(timeout);
                ws.close();
                chrome.kill();
                resolve(null); // No cookie obtained
              }
            }
          }, 2000);
        });

        ws.on('message', (data) => {
          const msg = JSON.parse(data);
          if (msg.id && msg.id >= 100 && msg.id < 200) {
            const cookies = msg.result?.cookies || [];
            const cfClearance = cookies.find(c => c.name === 'cf_clearance');
            if (cfClearance && !resolved) {
              resolved = true;
              clearTimeout(timeout);
              clearInterval(global._cfInterval);
              _cfCookie = cfClearance.value;
              _cfCookieTime = Date.now();
              ws.close();
              chrome.kill();
              resolve(_cfCookie);
            }
          }
        });

        ws.on('error', () => {});
      } catch(e) {
        if (!resolved) {
          resolved = true;
          clearTimeout(timeout);
          chrome.kill();
          reject(e);
        }
      }
    }, 3000);
  });
}

/**
 * Fetch URL with CF cookie (using native fetch).
 */
async function fetchWithCF(url) {
  // Get CF cookie
  const cfCookie = await getCFCookie();
  if (!cfCookie) {
    throw new Error('Could not obtain CF clearance cookie');
  }

  const r = await fetch(url, {
    headers: {
      'User-Agent': UA,
      'Cookie': `cf_clearance=${cfCookie}`,
    },
  });
  const body = await r.text();

  if (body.includes('Just a moment')) {
    // Cookie expired, clear cache and retry
    _cfCookie = null;
    throw new Error('CF cookie expired');
  }

  return body;
}

async function getTmdbInfo(tmdbId, type) {
  try {
    const r = await fetch(`https://api.themoviedb.org/3/${type}/${tmdbId}?api_key=${TMDB_API_KEY}`);
    const d = await r.json();
    return { title: type === 'tv' ? d.name : d.title, year: (d.release_date || d.first_air_date || '').slice(0, 4) };
  } catch { return { title: '', year: '' }; }
}

/**
 * Search for movies on mkvbase.site.
 */
async function searchMovie(query) {
  const searchUrl = `${TARGET_BASE}/?s=${encodeURIComponent(query)}`;
  const html = await fetchWithCF(searchUrl);

  const linkRegex = /href="(https:\/\/mkvbase\.site\/[a-z0-9][a-z0-9-]+\/)"/g;
  const links = new Set();
  let match;
  while ((match = linkRegex.exec(html)) !== null) {
    const url = match[1];
    if (!url.includes('/page/') && !url.includes('/wp-') && !url.includes('/feed') &&
        !url.includes('/category/') && !url.includes('/tag/') && url !== TARGET_BASE + '/') {
      links.add(url);
    }
  }

  const results = [];
  for (const url of links) {
    const slug = url.replace(TARGET_BASE + '/', '').replace(/\/$/, '');
    results.push({ url, title: slug.replace(/-/g, ' '), slug });
  }
  return results;
}

/**
 * Extract download links from movie page.
 */
function extractDownloadLinks(html) {
  const links = [];
  // Look for download links (various patterns)
  const patterns = [
    /<a[^>]*href="(https?:\/\/[^"]*(?:download|file|stream|play)[^"]*)"[^>]*>(.*?)<\/a>/gi,
    /<a[^>]*href="(https?:\/\/[^"]*\.mkv[^"]*)"[^>]*>(.*?)<\/a>/gi,
    /<a[^>]*href="(https?:\/\/[^"]*\.mp4[^"]*)"[^>]*>(.*?)<\/a>/gi,
    /data-(?:href|url|link)="(https?:\/\/[^"]*)"[^>]*>(.*?)<\/a>/gi,
  ];

  for (const pattern of patterns) {
    let match;
    while ((match = pattern.exec(html)) !== null) {
      const url = match[1];
      const text = match[2]?.replace(/<[^>]+>/g, '').trim() || '';
      const qualityMatch = text.match(/(\d{3,4}p|4K|UHD)/i) || url.match(/(\d{3,4}p|4K|UHD)/i);
      const sizeMatch = text.match(/\[?([0-9.]+(?:GB|MB|gb|mb))\]?/i);

      links.push({
        quality: qualityMatch ? qualityMatch[1].toLowerCase() : 'unknown',
        size: sizeMatch ? sizeMatch[1].toUpperCase() : '',
        url,
        text,
      });
    }
  }

  return links;
}

/**
 * Nuvio-compatible getStreams function.
 */
async function getStreams(tmdbId, type, season, episode) {
  const isTV = type === 'tv' || type === 'series';
  const info = await getTmdbInfo(tmdbId, isTV ? 'tv' : 'movie');
  if (!info.title) return [];

  let query = info.title;
  if (info.year) query += ' ' + info.year;

  console.error(`[MKVBase] Searching for: ${query}`);

  let results;
  try {
    results = await searchMovie(query);
  } catch (e) {
    console.error('[MKVBase] Search failed:', e.message);
    return [];
  }

  if (results.length === 0) {
    console.error('[MKVBase] No results');
    return [];
  }

  // Fetch the first result's page
  const movieUrl = results[0].url;
  console.error(`[MKVBase] Fetching: ${movieUrl}`);

  let html;
  try {
    html = await fetchWithCF(movieUrl);
  } catch (e) {
    console.error('[MKVBase] Page fetch failed:', e.message);
    return [];
  }

  const links = extractDownloadLinks(html);
  console.error(`[MKVBase] Found ${links.length} download links`);

  const displayTitle = info.title + (isTV && season ? ` S${season}E${episode || 1}` : '');

  return links.map(l => ({
    name: `MKVBase | ${l.quality} ${l.size ? '[' + l.size + ']' : ''}`.trim(),
    title: displayTitle,
    size: l.size || '',
    description: `${l.quality} ${l.size} | ${l.text}`,
    url: l.url,
    quality: l.quality,
    language: '',
    source: 'mkvbase',
    headers: {},
  }));
}

module.exports = { getStreams };
