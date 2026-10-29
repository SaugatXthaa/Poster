/**
 * DahmerMovies CF-Bypass Wrapper
 * ===============================
 * Wraps the obfuscated nuvio_dahmermovies.js to add Cloudflare `cf_clearance`
 * cookie support. When CF challenges block the /bulk endpoint, the user can
 * solve the challenge once in their browser and provide the cookie via env var.
 *
 * HOW IT WORKS:
 *   1. User visits https://p.111477.xyz/ in their browser
 *   2. Cloudflare JS challenge runs and solves automatically
 *   3. Browser receives cf_clearance cookie (valid ~30 minutes)
 *   4. User opens DevTools → Application → Cookies → https://p.111477.xyz
 *   5. Copies the cf_clearance value
 *   6. Sets DAHMER_CF_CLEARANCE env var (or passes via SCRAPER_SETTINGS)
 *   7. This wrapper injects the cookie into all stream requests
 *
 * USAGE:
 *   export DAHMER_CF_CLEARANCE=abc123_xyz789...
 *   node -e "require('./dahmermovies_cf'); const d = require('./nuvio_dahmermovies'); d.getStreams(693134, 'movie').then(console.log)"
 *
 * OR set via SCRAPER_SETTINGS (Nuvio integration):
 *   global.SCRAPER_SETTINGS = { dahmerCfClearance: 'abc123_xyz789...' };
 *
 * TOKEN RESOLUTION:
 *   1. process.env.DAHMER_CF_CLEARANCE
 *   2. global.SCRAPER_SETTINGS.dahmerCfClearance
 *   3. (none — wrapper passes through to original)
 *
 * WITHOUT A COOKIE: the wrapper still works for the directory listing
 * (a.111477.xyz has no CF), but stream URLs (p.111477.xyz/bulk) will return
 * 403 Cloudflare challenge. The URLs will still play in Stremio's player
 * (which handles CF internally).
 */
'use strict';

const ORIGINAL = require('./nuvio_dahmermovies');

const CF_CLEARANCE = process.env.DAHMER_CF_CLEARANCE || '';
const USER_AGENT = process.env.DAHMER_USER_AGENT ||
  'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';

// Try global.SCRAPER_SETTINGS
function resolveClearance() {
  if (CF_CLEARANCE) return CF_CLEARANCE;
  try {
    if (typeof global !== 'undefined' && global.SCRAPER_SETTINGS && global.SCRAPER_SETTINGS.dahmerCfClearance) {
      return String(global.SCRAPER_SETTINGS.dahmerCfClearance);
    }
  } catch (e) {}
  return '';
}

// Patch global fetch to inject cf_clearance cookie for p.111477.xyz requests
const originalFetch = global.fetch;
let isPatched = false;

function patchFetch() {
  if (isPatched) return;
  isPatched = true;

  const clearance = resolveClearance();
  if (!clearance) {
    console.log('[DahmerCF] No cf_clearance cookie set — stream URLs may be CF-blocked. Set DAHMER_CF_CLEARANCE env var to enable bypass.');
    console.log('[DahmerCF] To get the cookie: visit https://p.111477.xyz/ in your browser, solve the CF challenge, then copy cf_clearance from DevTools.');
    return;
  }

  console.log('[DahmerCF] cf_clearance cookie detected — injecting into p.111477.xyz requests');

  global.fetch = async function(input, init) {
    const url = typeof input === 'string' ? input : (input && input.url) || String(input);

    // Inject cookie for p.111477.xyz (the CF-protected bulk proxy)
    if (url.includes('p.111477.xyz') || url.includes('111477.xyz/bulk')) {
      init = init || {};
      init.headers = init.headers || {};

      // Normalize headers to a plain object
      let headers = init.headers;
      if (headers instanceof Headers) {
        const newHeaders = {};
        for (const [k, v] of headers.entries()) {
          newHeaders[k] = v;
        }
        headers = newHeaders;
        init.headers = headers;
      }

      // Inject Cookie and User-Agent if not already set
      const hasCookie = headers['Cookie'] || headers['cookie'];
      if (!hasCookie) {
        headers['Cookie'] = `cf_clearance=${clearance}`;
      }

      const hasUA = headers['User-Agent'] || headers['user-agent'];
      if (!hasUA) {
        headers['User-Agent'] = USER_AGENT;
      }
    }

    return originalFetch.call(this, input, init);
  };
}

// Patch immediately on require
patchFetch();

// Re-export the original module's getStreams
module.exports = {
  getStreams: ORIGINAL.getStreams,
  patchFetch,
  resolveClearance,
};
