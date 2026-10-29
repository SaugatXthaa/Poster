/**
 * CF Cookie Helper — Obtain cf_clearance for p.111477.xyz
 * =======================================================
 *
 * Since p.111477.xyz uses Cloudflare's interactive JS challenge, the only
 * way to get a valid cf_clearance cookie without a browser is to:
 *
 *   1. Open https://p.111477.xyz/ in Chrome/Firefox
 *   2. Wait for the "Just a moment..." page to solve (5-10 seconds)
 *   3. Open DevTools (F12) → Application → Cookies → https://p.111477.xyz
 *   4. Find the "cf_clearance" row and copy its Value
 *   5. Export it as an environment variable:
 *
 *        export DAHMER_CF_CLEARANCE=<paste_value_here>
 *
 *   6. Run your scraper / test as usual. The dahmermovies_cf.js wrapper
 *      will automatically inject this cookie into all p.111477.xyz requests.
 *
 * COOKIE LIFETIME: ~30 minutes (Cloudflare rotates it). When it expires,
 * you'll see 403 errors again — just repeat the steps above to get a new one.
 *
 * VERIFICATION:
 *   Run this script to test if your cookie works:
 *
 *     node get_cf_cookie.js
 *
 *   Expected output with a valid cookie:
 *     [+] cf_clearance cookie found (length: 130)
 *     [+] Testing against p.111477.xyz...
 *     [+] HTTP 200 — cookie is valid! Streams will work.
 *
 *   Expected output with an expired/invalid cookie:
 *     [+] cf_clearance cookie found (length: 130)
 *     [+] Testing against p.111477.xyz...
 *     [-] HTTP 403 — cookie is expired or invalid. Get a new one.
 */
'use strict';

const https = require('https');

const CF_CLEARANCE = process.env.DAHMER_CF_CLEARANCE || '';
const USER_AGENT = process.env.DAHMER_USER_AGENT ||
  'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';

async function testCookie() {
  if (!CF_CLEARANCE) {
    console.log('[-] No cf_clearance cookie set.');
    console.log('    Get one from: https://p.111477.xyz/ (solve CF challenge in browser,');
    console.log('    then copy cf_clearance from DevTools → Application → Cookies)');
    console.log('');
    console.log('    Then run:');
    console.log('      export DAHMER_CF_CLEARANCE=<value>');
    console.log('      node get_cf_cookie.js');
    return false;
  }

  console.log(`[+] cf_clearance cookie found (length: ${CF_CLEARANCE.length})`);

  // Test against p.111477.xyz
  const testUrl = 'https://p.111477.xyz/bulk?u=https%3A%2F%2Fa.111477.xyz%2Fmovies%2FDune%20-%20Part%20Two%20%282024%29%2FDune.Part.Two.2024.1080p.BluRay.x264-OFT.mkv';

  console.log('[+] Testing against p.111477.xyz...');

  return new Promise((resolve) => {
    const req = https.request(testUrl, {
      method: 'HEAD',
      headers: {
        'User-Agent': USER_AGENT,
        'Cookie': `cf_clearance=${CF_CLEARANCE}`,
        'Accept': '*/*',
        'Referer': 'https://a.111477.xyz/',
      },
      timeout: 10000,
    }, (res) => {
      console.log(`    Status: ${res.statusCode}`);
      console.log(`    Server: ${res.headers.server || '(none)'}`);
      console.log(`    CF-Mitigated: ${res.headers['cf-mitigated'] || '(none)'}`);
      console.log(`    Content-Type: ${res.headers['content-type'] || '(none)'}`);
      console.log(`    Content-Length: ${res.headers['content-length'] || '(none)'}`);

      if (res.statusCode === 200 || res.statusCode === 206) {
        console.log('[+] Cookie is VALID — streams will work!');
        resolve(true);
      } else if (res.statusCode === 403) {
        console.log('[-] HTTP 403 — cookie is expired or invalid.');
        console.log('    Get a new one from https://p.111477.xyz/ in your browser.');
        resolve(false);
      } else if (res.statusCode === 302 || res.statusCode === 307) {
        console.log(`[~] HTTP ${res.statusCode} — redirect to: ${res.headers.location}`);
        console.log('    Cookie may be valid — follow the redirect to confirm.');
        resolve(true);
      } else {
        console.log(`[?] HTTP ${res.statusCode} — unknown response.`);
        resolve(false);
      }
    });

    req.on('timeout', () => { req.destroy(); console.log('[-] Request timed out'); resolve(false); });
    req.on('error', (e) => { console.log('[-] Error:', e.message); resolve(false); });
    req.end();
  });
}

testCookie();
