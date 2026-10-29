// Try fetching stellar.rip with RSC headers to get stream data.
// Also fetch ALL JS chunks and grep for fetch/video/hls patterns more aggressively.
'use strict';

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';

async function fetchText(url, headers = {}) {
  const res = await fetch(url, {
    redirect: 'follow',
    headers: { 'User-Agent': UA, ...headers },
    signal: AbortSignal.timeout(30000),
  });
  return { status: res.status, body: await res.text(), headers: res.headers };
}

(async () => {
  // 1. Try RSC streaming format with Next-Router-State-Tree header
  console.log('===== 1. Fetch /en/movie/tt1375666/watch with RSC: 1 =====');
  const r1 = await fetchText('https://stellar.rip/en/movie/tt1375666/watch', {
    'Accept': 'text/x-component',
    'RSC': '1',
    'Next-Router-State-Tree': '%5B%22%22%2C%7B%22children%22%3A%5B%5B%22locale%22%2C%22en%22%2C%22d%22%3A%5B%22actions%22%2C%22api%22%2C%22news%22%5D%5D%2C%7B%7D%7D%5D%7D%2Cnull%2Cnull%2Ctrue%5D',
  });
  console.log('  status: ' + r1.status + ', body length: ' + r1.body.length);
  console.log('  body (first 3000): ' + r1.body.slice(0, 3000));

  console.log('\n===== 2. Try /en/api/* variations =====');
  for (const p of [
    '/en/api/stream/tt1375666',
    '/en/api/sources/tt1375666',
    '/en/api/movie/tt1375666',
    '/api/v1/stream/tt1375666',
    '/api/v1/movie/tt1375666',
    '/api/v1/sources/tt1375666',
    '/api/sources?imdb=tt1375666',
    '/api/stream?imdb=tt1375666',
    '/api/movie?imdb=tt1375666',
    '/api/movies/tt1375666',
    '/api/streams/tt1375666',
    '/api/play/tt1375666',
    '/api/watch/tt1375666',
  ]) {
    const r = await fetchText('https://stellar.rip' + p, { 'Accept': 'application/json' });
    const ct = r.headers.get('content-type') || '';
    const preview = r.body.slice(0, 250).replace(/\n/g, ' ');
    console.log('  ' + p + ' → ' + r.status + ' ct=' + ct + ' body: ' + preview);
  }

  console.log('\n===== 3. Fetch ALL 25 JS chunks, search for ANY URL pattern =====');
  const r2 = await fetchText('https://stellar.rip/en/movie/tt1375666/watch');
  const jsUrls = [...new Set([...r2.body.matchAll(/\/_next\/static\/chunks\/[^"'\\]+\.js/g)].map(m => m[0]))];
  console.log('  total chunks: ' + jsUrls.length);
  // Track all unique URL-like strings found across all chunks
  const allUrls = new Set();
  const allApiPatterns = new Set();
  for (const chunk of jsUrls) {
    const rc = await fetchText('https://stellar.rip' + chunk);
    if (rc.status !== 200) continue;
    // Match https URLs
    for (const m of rc.body.matchAll(/https?:\/\/[A-Za-z0-9.\-_/]+/g)) allUrls.add(m[0]);
    // Match /api/ paths
    for (const m of rc.body.matchAll(/["'`]\/api\/[^"'`]+["'`]/g)) allApiPatterns.add(m[0]);
    // Match /en/ paths
    for (const m of rc.body.matchAll(/["'`]\/en\/[^"'`]+["'`]/g)) allApiPatterns.add(m[0]);
    // Match /play/ /stream/ /sources/ etc
    for (const m of rc.body.matchAll(/["'`]\/(?:play|stream|sources?|videos?|watch|embed|proxy)\/[^"'`]+["'`]/g)) allApiPatterns.add(m[0]);
  }
  console.log('  unique URLs found: ' + allUrls.size);
  console.log('  unique API paths found: ' + allApiPatterns.size);
  // Print interesting ones
  console.log('\n  --- ALL URLs containing stellar.rip ---');
  [...allUrls].filter(u => u.includes('stellar')).slice(0, 20).forEach(u => console.log('    ' + u));
  console.log('\n  --- ALL URLs containing stream/play/video/hls/m3u8 ---');
  [...allUrls].filter(u => /stream|play|video|hls|m3u8|mp4|mkv/i.test(u)).slice(0, 20).forEach(u => console.log('    ' + u));
  console.log('\n  --- ALL /api/ /en/ paths found in chunks ---');
  [...allApiPatterns].slice(0, 40).forEach(p => console.log('    ' + p));
})();
