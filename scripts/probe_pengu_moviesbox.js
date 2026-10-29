// Probe PenguPlay's manifest + /direct/moviesbox/ endpoint.
// Goal: identify the real backend, then build an Antarctica-style scraper
// that hits it directly (no pengu.uk dependency).
'use strict';

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';

async function fetchOnce(url, opts = {}) {
  console.log('  GET ' + url.slice(0, 180) + (url.length > 180 ? '...' : ''));
  try {
    const res = await fetch(url, {
      redirect: 'manual',
      headers: { 'User-Agent': UA, 'Accept': '*/*', ...(opts.headers || {}) },
      signal: AbortSignal.timeout(opts.timeout || 15000),
    });
    const headers = {};
    res.headers.forEach((v, k) => headers[k] = v);
    let body = '';
    if (opts.method !== 'HEAD') {
      body = await res.text();
      if (body.length > 2000) body = body.slice(0, 2000) + '...[truncated]';
    }
    return { status: res.statusCode || res.status, statusText: res.statusText, headers, body };
  } catch (e) {
    return { status: 0, error: e.message };
  }
}

(async () => {
  console.log('===== PENGUPLAY MANIFEST =====');
  const m = await fetchOnce('https://pengu.uk/manifest.json');
  console.log('  status: ' + m.status);
  console.log('  body: ' + (m.body || m.error || '').slice(0, 1000));

  console.log('\n===== PROBE /direct/moviesbox/ WITHOUT PAYLOAD =====');
  const p1 = await fetchOnce('https://pengu.uk/direct/moviesbox/');
  console.log('  status: ' + p1.status + ' ' + (p1.statusText || ''));
  console.log('  location: ' + (p1.headers.location || '(none)'));
  console.log('  body: ' + (p1.body || p1.error || '').slice(0, 800));

  console.log('\n===== PROBE /direct/moviesbox/ WITH DUMMY PAYLOAD =====');
  // Try a dummy base64 payload (just to see how the endpoint responds)
  const dummyPayload = Buffer.from(JSON.stringify({
    t: 'movie', i: 'tt1375666', s: null, e: null,
    h: '0000000000000000000000000000000000000000', f: 0
  })).toString('base64url');
  const p2 = await fetchOnce('https://pengu.uk/direct/moviesbox/' + dummyPayload);
  console.log('  status: ' + p2.status + ' ' + (p2.statusText || ''));
  console.log('  location: ' + (p2.headers.location || '(none)'));
  console.log('  body: ' + (p2.body || p2.error || '').slice(0, 800));

  console.log('\n===== PROBE /moviesbox/ (no /direct/ prefix) =====');
  const p3 = await fetchOnce('https://pengu.uk/moviesbox/');
  console.log('  status: ' + p3.status + ' ' + (p3.statusText || ''));
  console.log('  location: ' + (p3.headers.location || '(none)'));
  console.log('  body: ' + (p3.body || p3.error || '').slice(0, 800));

  console.log('\n===== PROBE root + variants =====');
  for (const p of ['/direct/', '/direct', '/moviesbox', '/stream/', '/stream/movie/tt1375666.json']) {
    const r = await fetchOnce('https://pengu.uk' + p);
    console.log('  ' + p + ' → ' + r.status + ' ' + (r.statusText || '') + ' location=' + (r.headers.location || '(none)'));
  }
})();
