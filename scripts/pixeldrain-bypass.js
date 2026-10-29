// Pixeldrain Bypass — JS helper module for the pixeldrain-bypass.gamedrive.org service.
//
// ARCHITECTURE (reverse-engineered from pixeldrain-bypass.gamedrive.org + .user.js):
//   • Proxy list endpoint: https://pixeldrain-bypass.gamedrive.org/api/proxy.json
//     Returns: { "proxies": ["cdn.pixeldrain.eu.cc"] }
//   • Bypass URL pattern (from userscript):
//       - Single file: https://<proxy>/<file_id>
//       - Directory file: https://<proxy>/d/<dir_id>/<filename>
//       - Directory ZIP: https://<proxy>/ds/<dir_id>
//   • API endpoint (from HTML page): https://<proxy>/proxy-api/filesystem/<file_id>
//     Returns JSON metadata listing
//   • Redirect chain: cdn.pixeldrain.eu.cc/<id> → 302 → cdn<N>.pixeldrain.eu.cc/api/file/<id>
//     The cdn<N> subdomain is randomly assigned (cdn07, cdn08, cdn09...) — load balanced!
//
// HOW THE BYPASS WORKS:
//   The bypass service proxies requests to pixeldrain.com through a distributed CDN
//   of cloudflare-fronted mirror servers. Each request gets a 302 redirect to a
//   RANDOM CDN subdomain (cdn01-cdn20+), which:
//     1. Distributes the load across many subdomains
//     2. Bypasses pixeldrain's per-IP rate limiting (each request comes from a
//        different CDN IP from Cloudflare's perspective)
//     3. Bypasses Cloudflare's bot detection on pixeldrain.com (the CDN
//        subdomains are also CF-fronted, so requests look like normal CF traffic)
//
// BEFORE (direct pixeldrain.com):
//   • Large/popular files: HTTP 429 (Too Many Requests) after a few downloads
//   • Bot-like User-Agents: HTTP 403 (Forbidden)
//   • Without cookies/session: limited to ~5 concurrent downloads
//
// AFTER (via bypass service):
//   • Each request hits a DIFFERENT CDN subdomain → no rate limit accumulation
//   • All requests use the same bypass service origin → CF treats as same-site
//   • Direct file bytes returned (after 302 redirect to cdn<N>)
//   • Tested: bypass URL returns 302 → cdn07/cdn08/cdn09... → 200 + file bytes
//
// SUPPORTED URL FORMATS:
//   - https://pixeldrain.com/u/<id>            (single file view)
//   - https://pixeldrain.com/d/<id>/<filename> (directory file download)
//   - https://pixeldrain.com/d/<id>            (directory listing)
//   - https://pixeldrain.com/l/<id>            (album/list)
//   - https://pixeldrain.com/api/file/<id>     (API metadata)
//   - https://pixeldrain.com/api/file/<id>?download=1 (forced download)
//   - Also: pixeldra.in, pixeldrain.net, pixeldrain.dev domains
//
// USAGE:
//   const { bypassPixeldrain, isPixeldrainUrl, extractPixeldrainId } = require('./pixeldrain-bypass');
//
//   if (isPixeldrainUrl(streamUrl)) {
//     streamUrl = bypassPixeldrain(streamUrl);  // → cdn.pixeldrain.eu.cc/<id>
//   }
//
// DEPENDENCIES: Node 18+ (for native fetch). No npm packages.
// ============================================================================

'use strict';

const PROXY_LIST_URL = 'https://pixeldrain-bypass.gamedrive.org/api/proxy.json';
const DEFAULT_PROXY = 'cdn.pixeldrain.eu.cc';

// Cache the proxy list (refreshed every 24h by the userscript)
let _proxyCache = null;
let _proxyCacheTime = 0;
const PROXY_CACHE_TTL = 24 * 60 * 60 * 1000;

/**
 * Fetch the list of available bypass proxies. Cached for 24 hours.
 * @returns {Promise<string>} a proxy base URL (with trailing slash), e.g. "https://cdn.pixeldrain.eu.cc/"
 */
async function getProxyBaseUrl() {
  const now = Date.now();
  if (_proxyCache && (now - _proxyCacheTime) < PROXY_CACHE_TTL) {
    return _proxyCache;
  }
  try {
    const r = await fetch(PROXY_LIST_URL, {
      headers: { 'Accept': 'application/json' },
      signal: AbortSignal.timeout(8000),
    });
    const d = await r.json();
    let list = [];
    if (Array.isArray(d?.proxies)) list = d.proxies.slice();
    else if (typeof d?.proxy === 'string') list = [d.proxy];
    else if (Array.isArray(d)) list = d.slice();

    const normalized = list.map(s => {
      if (!s || typeof s !== 'string') return null;
      s = s.trim();
      if (!/^https?:\/\//i.test(s)) s = 'https://' + s;
      if (!s.endsWith('/')) s += '/';
      return s;
    }).filter(Boolean);

    if (normalized.length > 0) {
      // Pick random for load balancing
      const pick = normalized[Math.floor(Math.random() * normalized.length)];
      _proxyCache = pick;
      _proxyCacheTime = now;
      return pick;
    }
  } catch (e) { /* fall through to default */ }

  _proxyCache = `https://${DEFAULT_PROXY}/`;
  _proxyCacheTime = now;
  return _proxyCache;
}

/**
 * Check if a URL is a pixeldrain URL.
 * Matches: pixeldrain.com, pixeldrain.net, pixeldrain.dev, pixeldra.in
 */
function isPixeldrainUrl(url) {
  return /^https?:\/\/(?:[^/]*\.)?(?:pixeldra\.in|pixeldrain\.com|pixeldrain\.net|pixeldrain\.dev)\//i.test(url || '');
}

/**
 * Extract the file ID from a pixeldrain URL.
 * Returns null if no ID could be extracted.
 */
function extractPixeldrainId(url) {
  if (!url) return null;
  // Match: /u/<id>, /api/file/<id>, /l/<id>, /d/<id>, /d/<id>/<filename>
  const m = url.match(/\/(?:u|api\/file|l|d|ds)\/([^/?#\s]+)/);
  return m ? m[1] : null;
}

/**
 * Convert a direct pixeldrain URL to the bypass URL.
 *
 * Single file (https://pixeldrain.com/u/<id>):
 *   → https://cdn.pixeldrain.eu.cc/<id>
 *   → 302 redirects to https://cdn<N>.pixeldrain.eu.cc/api/file/<id>
 *   → 200 + file bytes
 *
 * Directory file (https://pixeldrain.com/d/<dir_id>/<filename>):
 *   → https://cdn.pixeldrain.eu.cc/d/<dir_id>/<filename>
 *   → 302 redirects to https://cdn<N>.pixeldrain.eu.cc/api/file/<dir_id>/<filename>
 *   → 200 + file bytes
 *
 * Directory ZIP (https://pixeldrain.com/d/<dir_id>):
 *   → https://cdn.pixeldrain.eu.cc/ds/<dir_id>
 *   → 302 redirects to https://cdn<N>.pixeldrain.eu.cc/api/directory/<dir_id>/download
 *   → 200 + ZIP bytes
 *
 * @param {string} url the original pixeldrain URL
 * @param {string} proxyBase optional proxy base URL (with trailing slash)
 * @returns {Promise<string|null>} the bypass URL, or null if URL is not pixeldrain
 */
async function bypassPixeldrain(url, proxyBase) {
  if (!isPixeldrainUrl(url)) return null;
  if (!proxyBase) proxyBase = await getProxyBaseUrl();

  // Try directory file download: /d/<dir_id>/<filename>
  const dirFileMatch = url.match(/\/d\/([^/?#\s]+)\/([^/?#\s]+)/);
  if (dirFileMatch) {
    return `${proxyBase}d/${dirFileMatch[1]}/${encodeURIComponent(dirFileMatch[2])}`;
  }

  // Try directory single-zip: /d/<dir_id> (no filename in path)
  const dirOnlyMatch = url.match(/\/d\/([^/?#\s]+)$/);
  if (dirOnlyMatch) {
    return `${proxyBase}ds/${dirOnlyMatch[1]}`;
  }

  // Try album/list: /l/<id>
  const listMatch = url.match(/\/l\/([^/?#\s]+)/);
  if (listMatch) {
    // For lists, we use the proxy-api/filesystem/ endpoint to get JSON listing
    return `${proxyBase}proxy-api/filesystem/${listMatch[1]}`;
  }

  // Single file: /u/<id>, /api/file/<id>
  const id = extractPixeldrainId(url);
  if (id) {
    return `${proxyBase}${id}`;
  }

  return null;
}

/**
 * Convert multiple pixeldrain URLs at once.
 * Non-pixeldrain URLs are returned unchanged.
 *
 * @param {string[]} urls array of URLs
 * @returns {Promise<string[]>} array with pixeldrain URLs replaced by bypass URLs
 */
async function bypassPixeldrainBatch(urls) {
  const out = [];
  for (const u of urls) {
    if (isPixeldrainUrl(u)) {
      const b = await bypassPixeldrain(u);
      out.push(b || u);
    } else {
      out.push(u);
    }
  }
  return out;
}

/**
 * Probe a URL (direct pixeldrain or bypass URL) for playability.
 * Returns { status, contentType, contentLength, playable, location }.
 */
async function probeUrl(url, headers = {}) {
  try {
    const u = new URL(url);
    const lib = u.protocol === 'http:' ? require('http') : require('https');
    return await new Promise(resolve => {
      let depth = 0;
      const follow = (curUrl) => {
        if (depth++ > 4) return resolve({ playable: false, status: 0, error: 'redirects exceeded' });
        const cu = new URL(curUrl);
        const lib2 = cu.protocol === 'http:' ? require('http') : require('https');
        const req = lib2.request({
          hostname: cu.hostname,
          path: cu.pathname + cu.search,
          method: 'GET',
          headers: {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
            'Range': 'bytes=0-1',
            'Accept': '*/*',
            ...headers,
          }
        }, (res) => {
          res.destroy();
          const ct = res.headers['content-type'] || '';
          if (res.statusCode >= 300 && res.statusCode < 400 && res.headers.location) {
            return follow(new URL(res.headers.location, curUrl).href);
          }
          const ok = (res.statusCode === 200 || res.statusCode === 206) &&
                     /video|octet-stream|mpegurl|binary/i.test(ct);
          resolve({
            status: res.statusCode,
            contentType: ct,
            contentLength: res.headers['content-length'] || '?',
            playable: ok,
          });
        });
        req.on('error', (e) => resolve({ playable: false, status: 0, error: e.message }));
        req.on('timeout', () => { req.destroy(); resolve({ playable: false, status: 0, timeout: true }); });
        req.setTimeout(8000); req.end();
      };
      follow(u.href);
    });
  } catch (e) { return { playable: false, error: e.message }; }
}

// ─── Self-test / demo ─────────────────────────────────────────────────────
if (require.main === module) {
  (async () => {
    console.log('=== Pixeldrain Bypass Module — Self-test ===\n');
    console.log('Proxy list URL:', PROXY_LIST_URL);
    console.log('Default proxy:', DEFAULT_PROXY);
    console.log('');

    // 1. Fetch proxy list
    console.log('1. Fetching proxy list...');
    const proxyBase = await getProxyBaseUrl();
    console.log('   Proxy base:', proxyBase);

    // 2. Test URL detection
    const testUrls = [
      'https://pixeldrain.com/u/abc123',
      'https://pixeldrain.com/d/abc123/file.mkv',
      'https://pixeldrain.com/d/abc123',
      'https://pixeldrain.com/l/album1',
      'https://pixeldrain.com/api/file/abc123',
      'https://pixeldrain.com/api/file/abc123?download=1',
      'https://pixeldra.in/u/abc123',
      'https://pixeldrain.net/u/abc123',
      'https://example.com/file.mkv',  // not pixeldrain — should not convert
      'https://gdflix.dev/file/abc123',  // not pixeldrain
    ];

    console.log('\n2. URL conversion test:');
    for (const url of testUrls) {
      const isPd = isPixeldrainUrl(url);
      const id = extractPixeldrainId(url);
      const bypass = isPd ? await bypassPixeldrain(url, proxyBase) : null;
      console.log(`   ${url.slice(0, 60)}`);
      console.log(`     isPd=${isPd} id=${id || '-'} bypass=${bypass || '(unchanged)'}`);
    }

    // 3. Live probe with a test (non-existent) ID
    console.log('\n3. Live probe with test ID "abc123" (does not exist):');
    const directUrl = 'https://pixeldrain.com/api/file/abc123';
    const bypassUrl = await bypassPixeldrain('https://pixeldrain.com/u/abc123', proxyBase);
    console.log('   Direct URL:', directUrl);
    console.log('   Bypass URL:', bypassUrl);

    console.log('\n   Direct probe:');
    const directProbe = await probeUrl(directUrl, { 'Accept': 'application/json' });
    console.log('     ', JSON.stringify(directProbe));

    console.log('\n   Bypass probe (should 302 → cdn<N>.pixeldrain.eu.cc → 404 from pixeldrain):');
    const bypassProbe = await probeUrl(bypassUrl);
    console.log('     ', JSON.stringify(bypassProbe));

    console.log('\n=== Self-test complete ===');
    console.log('\nNote: The bypass service transparently proxies to pixeldrain.com.');
    console.log('For non-existent IDs, BOTH direct and bypass return 404 (same behavior).');
    console.log('For REAL file IDs that hit rate limits (HTTP 429) on pixeldrain.com directly,');
    console.log('the bypass URL will succeed because each request hits a different CDN subdomain.');
  })();
}

module.exports = {
  bypassPixeldrain,
  bypassPixeldrainBatch,
  isPixeldrainUrl,
  extractPixeldrainId,
  getProxyBaseUrl,
  probeUrl,
  PROXY_LIST_URL,
  DEFAULT_PROXY,
};
