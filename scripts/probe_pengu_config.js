// PenguPlay has a /stream/movie/<imdb>.json endpoint. The config can be
// passed as query params or path to enable specific sources. Let's find
// how PenguPlay accepts config and get the moviesbox streams.
'use strict';

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';

async function fetchOnce(url, opts = {}) {
  console.log('  GET ' + url.slice(0, 200) + (url.length > 200 ? '...' : ''));
  try {
    const res = await fetch(url, {
      redirect: 'manual',
      headers: { 'User-Agent': UA, 'Accept': '*/*', ...(opts.headers || {}) },
      signal: AbortSignal.timeout(opts.timeout || 30000),
    });
    const headers = {};
    res.headers.forEach((v, k) => headers[k] = v);
    const body = await res.text();
    return { status: res.statusCode || res.status, statusText: res.statusText, headers, body };
  } catch (e) {
    return { status: 0, error: e.message };
  }
}

(async () => {
  // First, fetch the default stream endpoint (no config) to see what shape it returns
  console.log('===== 1. Default /stream/movie/tt1375666.json (no config) =====');
  const r1 = await fetchOnce('https://pengu.uk/stream/movie/tt1375666.json');
  console.log('  status: ' + r1.status);
  console.log('  body length: ' + (r1.body || '').length);
  console.log('  body (first 2500): ' + (r1.body || r1.error || '').slice(0, 2500));

  // Try with a query param that enables only moviebox
  console.log('\n===== 2. With config query param source_moviebox=1 =====');
  const r2 = await fetchOnce('https://pengu.uk/stream/movie/tt1375666.json?source_moviebox=1&source_4khdhub=0');
  console.log('  status: ' + r2.status);
  console.log('  body (first 2500): ' + (r2.body || r2.error || '').slice(0, 2500));

  // Try fetching the source code to understand the config shape
  console.log('\n===== 3. Look for /configure endpoint =====');
  for (const p of ['/configure', '/configure.json', '/config', '/config.json', '/sources']) {
    const r = await fetchOnce('https://pengu.uk' + p);
    console.log('  ' + p + ' → ' + r.status + ' body: ' + (r.body || '').slice(0, 200));
  }

  // Check if PenguPlay follows Stremio config pattern — base64 path
  console.log('\n===== 4. Try Stremio-style config in path =====');
  // Common Stremio addon pattern: /<base64-config>/stream/movie/<imdb>.json
  const cfg = {
    source_moviebox: true,
    source_4khdhub: false,
    source_vegamovies: false,
  };
  const cfgB64 = Buffer.from(JSON.stringify(cfg)).toString('base64');
  const r4 = await fetchOnce('https://pengu.uk/' + cfgB64 + '/stream/movie/tt1375666.json');
  console.log('  status: ' + r4.status);
  console.log('  body (first 2500): ' + (r4.body || r4.error || '').slice(0, 2500));

  // Also try base64url
  const cfgB64Url = Buffer.from(JSON.stringify(cfg)).toString('base64url');
  const r4b = await fetchOnce('https://pengu.uk/' + cfgB64Url + '/stream/movie/tt1375666.json');
  console.log('\n  base64url variant:');
  console.log('  status: ' + r4b.status);
  console.log('  body (first 2500): ' + (r4b.body || r4b.error || '').slice(0, 2500));
})();
