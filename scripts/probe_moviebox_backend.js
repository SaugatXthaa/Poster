// Probe the actual MovieBox backend directly (bypassing pengu.uk).
// The existing moviebox.js shows proxyHeaders.referer = 'https://stremio-moviebox-1.onrender.com/'
// — that's the real Stremio addon. Let's see if it's accessible without auth.
'use strict';

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';

async function fetchOnce(url, opts = {}) {
  console.log('  ' + (opts.method || 'GET') + ' ' + url.slice(0, 200) + (url.length > 200 ? '...' : ''));
  try {
    const res = await fetch(url, {
      redirect: 'follow',
      method: opts.method || 'GET',
      headers: { 'User-Agent': UA, 'Accept': '*/*', ...(opts.headers || {}) },
      signal: AbortSignal.timeout(opts.timeout || 15000),
    });
    const headers = {};
    res.headers.forEach((v, k) => headers[k] = v);
    let body = '';
    if (opts.method !== 'HEAD') {
      body = await res.text();
      if (body.length > 3000) body = body.slice(0, 3000) + '...[truncated]';
    }
    return { status: res.statusCode || res.status, statusText: res.statusText, headers, body };
  } catch (e) {
    return { status: 0, error: e.message };
  }
}

(async () => {
  const HOSTS = [
    'https://stremio-moviebox-1.onrender.com',
    'https://stremio-moviebox-2.onrender.com',
    'https://stremio-moviebox.onrender.com',
    'https://moviebox.strem.fun',
    'https://mb.knifeyou.win',
  ];

  for (const host of HOSTS) {
    console.log('\n===== ' + host + ' =====');
    // 1. Manifest
    const m = await fetchOnce(host + '/manifest.json');
    if (m.status === 0) {
      console.log('  ✗ unreachable: ' + m.error);
      continue;
    }
    console.log('  manifest status: ' + m.status);
    console.log('  manifest body: ' + (m.body || '').slice(0, 800));
    if (m.status === 200 && m.body && m.body.includes('"id"')) {
      // Try stream endpoint
      console.log('  --- stream/movie/tt1375666.json ---');
      const s = await fetchOnce(host + '/stream/movie/tt1375666.json');
      console.log('  status: ' + s.status);
      console.log('  body (first 2000): ' + (s.body || '').slice(0, 2000));

      // Try TV episode
      console.log('  --- stream/series/tt0903747:1:1.json ---');
      const s2 = await fetchOnce(host + '/stream/series/tt0903747:1:1.json');
      console.log('  status: ' + s2.status);
      console.log('  body (first 2000): ' + (s2.body || '').slice(0, 2000));
    }
  }
})();
