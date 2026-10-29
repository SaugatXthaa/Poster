// Probe stellar.rip's actual playback API.
// Found these in the JS chunks:
//   /api/register-rate-limit
//   /api/verify-turnstile
//   /api/tmdb/<id>
//   /api/request-token
//   /watch/embed/
//   /watch/movie/
//   /watch/tv/
//   /api/4505f21c   ← looks like the actual playback API (hex-obfuscated name)
'use strict';

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';
const REFERER = 'https://stellar.rip/en/movie/tt1375666/watch';

async function fetchText(url, opts = {}) {
  console.log('  ' + (opts.method || 'GET') + ' ' + url);
  if (opts.body) console.log('    body: ' + opts.body);
  const res = await fetch(url, {
    redirect: 'follow',
    method: opts.method || 'GET',
    headers: {
      'User-Agent': UA,
      'Accept': 'application/json, text/plain, */*',
      'Accept-Language': 'en-US,en;q=0.9',
      'Referer': opts.referer || REFERER,
      'Origin': 'https://stellar.rip',
      ...(opts.headers || {}),
    },
    signal: AbortSignal.timeout(opts.timeout || 15000),
  });
  const text = await res.text();
  return { status: res.status, body: text, headers: res.headers };
}

(async () => {
  console.log('===== 1. /api/request-token =====');
  const r1 = await fetchText('https://stellar.rip/api/request-token');
  console.log('  status: ' + r1.status + ' ct: ' + (r1.headers.get('content-type') || ''));
  console.log('  body: ' + r1.body.slice(0, 1000));

  console.log('\n===== 2. /api/4505f21c with no params =====');
  const r2 = await fetchText('https://stellar.rip/api/4505f21c');
  console.log('  status: ' + r2.status + ' ct: ' + (r2.headers.get('content-type') || ''));
  console.log('  body: ' + r2.body.slice(0, 1000));

  console.log('\n===== 3. /api/4505f21c with various query params =====');
  for (const qs of [
    '?imdb=tt1375666',
    '?imdbId=tt1375666',
    '?id=tt1375666',
    '?tmdb=1375666',
    '?tmdbId=1375666',
    '?type=movie&imdb=tt1375666',
    '?type=movie&id=tt1375666',
    '?kind=movie&id=tt1375666',
    '?mediaType=movie&imdbId=tt1375666',
    '?action=play&imdb=tt1375666',
    '?play=tt1375666',
  ]) {
    const r = await fetchText('https://stellar.rip/api/4505f21c' + qs);
    console.log('  ' + qs + ' → ' + r.status + ' body: ' + r.body.slice(0, 300));
  }

  console.log('\n===== 4. /api/4505f21c with POST =====');
  for (const body of [
    '{"imdb":"tt1375666"}',
    '{"imdbId":"tt1375666"}',
    '{"id":"tt1375666","type":"movie"}',
    '{"kind":"movie","id":"tt1375666"}',
    '{"mediaType":"movie","imdbId":"tt1375666"}',
  ]) {
    const r = await fetchText('https://stellar.rip/api/4505f21c', { method: 'POST', body, headers: { 'Content-Type': 'application/json' } });
    console.log('  POST body=' + body + ' → ' + r.status + ' body: ' + r.body.slice(0, 300));
  }

  console.log('\n===== 5. /watch/movie/tt1375666 and /watch/embed/tt1375666 =====');
  for (const p of ['/watch/movie/tt1375666', '/watch/embed/tt1375666', '/watch/embed/movie/tt1375666', '/watch/embed/tt1375666.mp4']) {
    const r = await fetchText('https://stellar.rip' + p);
    console.log('  ' + p + ' → ' + r.status + ' ct=' + (r.headers.get('content-type') || '') + ' body: ' + r.body.slice(0, 250));
  }

  console.log('\n===== 6. /api/tmdb/tt1375666 =====');
  const r6 = await fetchText('https://stellar.rip/api/tmdb/tt1375666');
  console.log('  status: ' + r6.status);
  console.log('  body: ' + r6.body.slice(0, 500));
})();
