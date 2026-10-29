// Re-probe stellar.rip — check if anything has changed since last time.
// Especially look for new API endpoints, removed Cloudflare protection, or
// changes to the playback flow.
'use strict';

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';

async function fetchOnce(url, opts = {}) {
  try {
    const res = await fetch(url, {
      redirect: opts.redirect || 'follow',
      method: opts.method || 'GET',
      headers: { 'User-Agent': UA, 'Accept': opts.accept || '*/*', ...(opts.headers || {}) },
      signal: AbortSignal.timeout(opts.timeout || 15000),
    });
    const headers = {};
    res.headers.forEach((v, k) => headers[k] = v);
    const body = await res.text();
    return { status: res.status, headers, body };
  } catch (e) {
    return { status: 0, error: e.message };
  }
}

(async () => {
  console.log('===== 1. /api/request-token (was 403 CF challenge before) =====');
  const r1 = await fetchOnce('https://stellar.rip/api/request-token', {
    accept: 'application/json',
    headers: { 'Referer': 'https://stellar.rip/en/movie/tt1375666/watch' }
  });
  console.log('  status: ' + r1.status + ' ct: ' + (r1.headers['content-type'] || ''));
  console.log('  body (300): ' + (r1.body || r1.error || '').slice(0, 300));
  // If it's still CF challenge, the body starts with <!DOCTYPE html>...
  if ((r1.body || '').startsWith('{')) {
    try {
      const j = JSON.parse(r1.body);
      console.log('  ★ JSON response! ' + JSON.stringify(j).slice(0, 600));
    } catch (e) {}
  }

  console.log('\n===== 2. /api/stream (was 410 retired before) =====');
  const r2 = await fetchOnce('https://stellar.rip/api/stream?imdb=tt1375666', {
    accept: 'application/json',
    headers: { 'Referer': 'https://stellar.rip/en/movie/tt1375666/watch' }
  });
  console.log('  status: ' + r2.status + ' ct: ' + (r2.headers['content-type'] || ''));
  console.log('  body: ' + (r2.body || '').slice(0, 500));

  console.log('\n===== 3. Re-fetch watch page — check for new API endpoints =====');
  const r3 = await fetchOnce('https://stellar.rip/en/movie/tt1375666/watch');
  console.log('  status: ' + r3.status + ' body length: ' + (r3.body || '').length);
  
  // Extract ALL JS chunk URLs
  const jsUrls = [...new Set([...r3.body.matchAll(/\/_next\/static\/chunks\/[^"'\\]+\.js/g)].map(m => m[0]))];
  console.log('  total JS chunks: ' + jsUrls.length);
  
  // Look for new chunks vs the ones I saved before
  const oldChunks = ['1ih9z_l56apxx.js', '2-urkx3cuhmdn.js', '0l4vuujt-7ukn.js', '2qg7vsfprrxhs.js', '26g4tn5b226u5.js', '32chfymtokyu8.js', '0x6n6k9woe0nw.js'];
  const newChunks = jsUrls.map(u => u.split('/').pop()).filter(name => !oldChunks.includes(name));
  console.log('  new chunks (since last check): ' + newChunks.length);
  if (newChunks.length > 0) {
    console.log('  --- new chunk names ---');
    newChunks.slice(0, 20).forEach(n => console.log('    ' + n));
  }
  
  // Fetch all chunks and look for NEW api endpoints we haven't seen
  const allApiPaths = new Set();
  for (const chunk of jsUrls) {
    const rc = await fetchOnce('https://stellar.rip' + chunk);
    if (rc.status !== 200) continue;
    for (const m of rc.body.matchAll(/["'`]\/api\/[^"'`]+["'`]/g)) allApiPaths.add(m[0]);
    // Also look for stream URL patterns
    for (const m of rc.body.matchAll(/["'`](https?:\/\/[^"'`]*(?:m3u8|mp4|mkv|hls|stream)[^"'`]*)["'`]/gi)) {
      const url = m[1];
      if (!/wsrv|tmdb|schema|postimg|google|cloudflare|stats\.stellar/i.test(url)) {
        console.log('  potential stream URL pattern in ' + chunk.split('/').pop() + ': ' + url.slice(0, 200));
      }
    }
  }
  console.log('\n  --- ALL /api/ paths found across all chunks ---');
  [...allApiPaths].sort().forEach(p => console.log('    ' + p));

  console.log('\n===== 4. Try /api/stream variations with different params =====');
  for (const qs of [
    '?imdb=tt1375666&type=movie',
    '?imdbId=tt1375666&type=movie',
    '?id=tt1375666',
    '?q=tt1375666',
    '?imdb=tt1375666&season=1&episode=1',
  ]) {
    const r = await fetchOnce('https://stellar.rip/api/stream' + qs, { accept: 'application/json' });
    console.log('  ' + qs + ' → ' + r.status + ' body: ' + (r.body || '').slice(0, 200));
  }
})();
