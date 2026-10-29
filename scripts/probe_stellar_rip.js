// Probe stellar.rip to understand what kind of site it is and how it serves streams.
'use strict';

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';

async function fetchOnce(url, opts = {}) {
  console.log('  ' + (opts.method || 'GET') + ' ' + url);
  try {
    const res = await fetch(url, {
      redirect: 'follow',
      method: opts.method || 'GET',
      headers: { 'User-Agent': UA, 'Accept': '*/*', ...(opts.headers || {}) },
      signal: AbortSignal.timeout(opts.timeout || 20000),
    });
    const headers = {};
    res.headers.forEach((v, k) => headers[k] = v);
    const text = await res.text();
    return { status: res.status, headers, body: text };
  } catch (e) {
    return { status: 0, error: e.message };
  }
}

(async () => {
  console.log('===== 1. Fetch stellar.rip/en (homepage) =====');
  const r1 = await fetchOnce('https://stellar.rip/en');
  console.log('  status: ' + r1.status);
  console.log('  body length: ' + (r1.body || '').length);
  console.log('  body (first 3000): ' + (r1.body || r1.error || '').slice(0, 3000));

  console.log('\n===== 2. Probe common Stremio addon endpoints =====');
  for (const p of ['/manifest.json', '/stream/movie/tt1375666.json', '/stream/series/tt0903747:1:1.json', '/configure', '/configure.json', '/stremio/manifest.json', '/api/manifest', '/api/stream/tt1375666', '/api/movie/tt1375666']) {
    const r = await fetchOnce('https://stellar.rip' + p);
    console.log('  ' + p + ' → ' + r.status + ' | body: ' + (r.body || '').slice(0, 250));
  }

  console.log('\n===== 3. Check for /en/ patterns (looks like a streaming site) =====');
  for (const p of ['/en/movie/tt1375666', '/en/movie/inception-2010', '/en/watch/tt1375666', '/en/embed/tt1375666', '/en/stream/tt1375666', '/en/play/tt1375666', '/api/movie/tt1375666', '/api/stream/tt1375666']) {
    const r = await fetchOnce('https://stellar.rip' + p);
    console.log('  ' + p + ' → ' + r.status + ' | body: ' + (r.body || '').slice(0, 250));
  }
})();
