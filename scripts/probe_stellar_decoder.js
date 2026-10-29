// Get the FULL /api/4505f21c response, and search JS chunks for the decoder.
'use strict';

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';
const REFERER = 'https://stellar.rip/en/movie/tt1375666/watch';

async function fetchText(url, headers = {}) {
  const res = await fetch(url, {
    redirect: 'follow',
    headers: { 'User-Agent': UA, 'Referer': REFERER, 'Accept': '*/*', ...headers },
    signal: AbortSignal.timeout(30000),
  });
  return { status: res.status, body: await res.text(), headers: res.headers };
}

(async () => {
  console.log('===== 1. Get FULL /api/4505f21c response =====');
  const r = await fetchText('https://stellar.rip/api/4505f21c');
  console.log('  status: ' + r.status + ', body length: ' + r.body.length);
  console.log('  content-type: ' + (r.headers.get('content-type') || ''));
  console.log('  body (first 4000): ' + r.body.slice(0, 4000));
  console.log('  body (last 1000): ' + r.body.slice(-1000));
  
  // Save full response for analysis
  const fs = require('fs');
  fs.writeFileSync('/home/z/my-project/scripts/stellar_4505f21c_response.js', r.body);
  console.log('  saved full response to scripts/stellar_4505f21c_response.js');
  
  console.log('\n===== 2. Find JS chunk that fetches /api/4505f21c =====');
  // First fetch the watch page to get the list of JS chunks
  const rpage = await fetchText('https://stellar.rip/en/movie/tt1375666/watch');
  const jsUrls = [...new Set([...rpage.body.matchAll(/\/_next\/static\/chunks\/[^"'\\]+\.js/g)].map(m => m[0]))];
  console.log('  total chunks: ' + jsUrls.length);
  
  // Search each chunk for the API endpoint hash
  let decoderChunk = null;
  for (const chunk of jsUrls) {
    const rc = await fetchText('https://stellar.rip' + chunk);
    if (rc.status !== 200) continue;
    // Look for /api/4505f21c or the var name ZpQw9XkLmN8c3vR3 or fetch of /api/
    if (rc.body.includes('4505f21c') || rc.body.includes('ZpQw9XkLmN8c3vR3') || /\/api\/[a-f0-9]{8}/.test(rc.body)) {
      console.log('  ✓ Found decoder-relevant chunk: ' + chunk);
      // Save it
      fs.writeFileSync('/home/z/my-project/scripts/stellar_chunk_' + chunk.split('/').pop(), rc.body);
      console.log('    saved to scripts/stellar_chunk_' + chunk.split('/').pop());
      // Print interesting context around the match
      const idx = rc.body.search(/4505f21c|ZpQw9XkLmN8c3vR3|\/api\/[a-f0-9]{8}/);
      if (idx > 0) {
        console.log('    context around match (chars ' + Math.max(0, idx - 200) + ' to ' + (idx + 800) + '):');
        console.log('    ' + rc.body.slice(Math.max(0, idx - 200), idx + 800));
      }
      decoderChunk = chunk;
    }
  }
  
  if (decoderChunk) {
    console.log('\n===== 3. Search for window[] assignments and eval/new Function =====');
    // Get the decoder chunk again
    const decoder = await fetchText('https://stellar.rip' + decoderChunk);
    // Look for eval, new Function, window[ assignments
    const patterns = [/eval\([^)]+\)/g, /new Function\([^)]+\)/g, /window\[['"][^'"]+['"]\]\s*=/g, /atob\(/g];
    for (const p of patterns) {
      const matches = [...decoder.body.matchAll(p)].map(m => m[0]);
      if (matches.length > 0) {
        console.log('  pattern ' + p.source.slice(0, 50) + ': ' + matches.length + ' matches');
        matches.slice(0, 5).forEach(m => console.log('    ' + m.slice(0, 250)));
      }
    }
  }
})();
