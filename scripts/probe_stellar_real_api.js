// The /api/4505f21c was a red herring (Adcash ads). The real flow is:
//   /api/request-token → sets window.__REQUEST_TOKEN__
//   Some playback API uses the token + IMDB ID
// Find the playback API in the chunks.
'use strict';

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';
const fs = require('fs');

async function fetchText(url, headers = {}) {
  const res = await fetch(url, {
    redirect: 'follow',
    headers: { 'User-Agent': UA, 'Accept': '*/*', ...headers },
    signal: AbortSignal.timeout(30000),
  });
  return { status: res.status, body: await res.text(), headers: res.headers };
}

(async () => {
  // First, try /api/request-token with full browser headers
  console.log('===== 1. /api/request-token with browser headers =====');
  const r1 = await fetchText('https://stellar.rip/api/request-token', {
    'Accept': 'application/json',
    'Accept-Language': 'en-US,en;q=0.9',
    'Referer': 'https://stellar.rip/en/movie/tt1375666/watch',
    'Sec-Fetch-Dest': 'empty',
    'Sec-Fetch-Mode': 'cors',
    'Sec-Fetch-Site': 'same-origin',
  });
  console.log('  status: ' + r1.status + ' ct: ' + (r1.headers.get('content-type') || ''));
  console.log('  body (first 1000): ' + r1.body.slice(0, 1000));

  console.log('\n===== 2. Search ALL JS chunks for __REQUEST_TOKEN__ usage =====');
  const rpage = await fetchText('https://stellar.rip/en/movie/tt1375666/watch');
  const jsUrls = [...new Set([...rpage.body.matchAll(/\/_next\/static\/chunks\/[^"'\\]+\.js/g)].map(m => m[0]))];
  console.log('  total chunks: ' + jsUrls.length);

  const tokenChunks = [];
  for (const chunk of jsUrls) {
    const rc = await fetchText('https://stellar.rip' + chunk);
    if (rc.status !== 200) continue;
    // Look for __REQUEST_TOKEN__ usage
    if (rc.body.includes('__REQUEST_TOKEN__') || rc.body.includes('request-token') || rc.body.includes('requestToken')) {
      tokenChunks.push(chunk);
      console.log('  ✓ chunk with __REQUEST_TOKEN__: ' + chunk);
      // Save it
      fs.writeFileSync('/home/z/my-project/scripts/stellar_tok_chunk_' + chunk.split('/').pop(), rc.body);
      // Find all fetch/axios patterns in this chunk
      const fetches = [...rc.body.matchAll(/(?:fetch|axios|\.\s*(?:get|post))\s*\(\s*["'`]([^"'`]+)["'`]/g)];
      console.log('    fetch patterns in this chunk: ' + fetches.length);
      fetches.slice(0, 30).forEach(m => console.log('      ' + m[1]));
      // Look for template strings with /api/
      const apiPatterns = [...rc.body.matchAll(/["'`]\/api\/[^"'`]+["'`]/g)];
      console.log('    /api/ paths in this chunk: ' + apiPatterns.length);
      [...new Set(apiPatterns.map(m => m[0]))].slice(0, 30).forEach(p => console.log('      ' + p));
      // Look for stream/video URL patterns
      const videoPatterns = [...rc.body.matchAll(/["'`](https?:\/\/[^"'`]+\.(?:m3u8|mp4|mkv|hls)[^"'`]*)["'`]/g)];
      if (videoPatterns.length > 0) {
        console.log('    VIDEO URLs found:');
        videoPatterns.slice(0, 10).forEach(m => console.log('      ' + m[1].slice(0, 250)));
      }
      // Look for /watch/ paths
      const watchPatterns = [...rc.body.matchAll(/["'`]\/watch\/[^"'`]+["'`]/g)];
      if (watchPatterns.length > 0) {
        console.log('    /watch/ paths found:');
        [...new Set(watchPatterns.map(m => m[0]))].slice(0, 10).forEach(p => console.log('      ' + p));
      }
    }
  }
})();
