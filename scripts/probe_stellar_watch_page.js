// Examine the stellar.rip /watch page + look at all JS chunks for fetch() patterns.
'use strict';

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';

async function fetchText(url, headers = {}) {
  const res = await fetch(url, {
    redirect: 'follow',
    headers: { 'User-Agent': UA, 'Accept': 'text/html,application/json,*/*', ...headers },
    signal: AbortSignal.timeout(30000),
  });
  return { status: res.status, body: await res.text(), headers: res.headers };
}

(async () => {
  console.log('===== 1. /en/movie/tt1375666/watch page =====');
  const r = await fetchText('https://stellar.rip/en/movie/tt1375666/watch');
  console.log('  status: ' + r.status + ', length: ' + r.body.length);

  // All __next_f chunks
  const chunks = r.body.match(/self\.__next_f\.push\(\[1,[^<]+\]\)/g) || [];
  console.log('  RSC chunks: ' + chunks.length);
  const all = chunks.join('\n');
  console.log('  combined chunks length: ' + all.length);

  // Search for URLs in the combined chunks
  const urlRe = /https?:\/\/[^"'\s\\,)]+/g;
  const urls = [...new Set([...all.matchAll(urlRe)].map(m => m[0]))].slice(0, 30);
  console.log('  URLs found in chunks:');
  urls.forEach(u => console.log('    ' + u.slice(0, 250)));

  // Also look for /api/ patterns
  const apiPatterns = [
    /["'`]\/api\/[^"'`]+["'`]/g,
    /["'`]\/en\/[^"'`]+["'`]/g,
    /["'`]\/play\/[^"'`]+["'`]/g,
    /["'`]\/stream\/[^"'`]+["'`]/g,
    /["'`]\/sources\/[^"'`]+["'`]/g,
  ];
  for (const re of apiPatterns) {
    const matches = [...new Set([...all.matchAll(re)].map(m => m[0]))].slice(0, 10);
    if (matches.length > 0) {
      console.log('  pattern ' + re.source.slice(0, 50) + ':');
      matches.forEach(m => console.log('    ' + m.slice(0, 200)));
    }
  }

  // Look at __NEXT_DATA__ if any
  const ndMatch = r.body.match(/<script[^>]*id=["']__NEXT_DATA__["'][^>]*>([^<]+)<\/script>/);
  if (ndMatch) {
    console.log('\n  __NEXT_DATA__ found (length ' + ndMatch[1].length + '):');
    console.log('    ' + ndMatch[1].slice(0, 1500));
  }

  // Look for meta tags with stream info
  const metaMatches = [...r.body.matchAll(/<meta[^>]+>/g)].map(m => m[0]).filter(m => /video|stream|source|content/i.test(m));
  console.log('\n  video-related meta tags:');
  metaMatches.forEach(m => console.log('    ' + m.slice(0, 250)));

  console.log('\n===== 2. Probe all JS chunks for fetch/axios patterns =====');
  const jsUrls = [...new Set([...r.body.matchAll(/\/_next\/static\/chunks\/[^"'\\]+\.js/g)].map(m => m[0]))];
  console.log('  total JS chunks: ' + jsUrls.length);
  // Fetch all chunks and look for API patterns
  for (const chunk of jsUrls) {
    const url = 'https://stellar.rip' + chunk;
    const rc = await fetchText(url);
    if (rc.status !== 200) continue;
    // Look for fetch() and axios.get() patterns
    const fetches = [...new Set([
      ...rc.body.matchAll(/fetch\(["'`]([^"'`]+)["'`]/g),
      ...rc.body.matchAll(/\.get\(["'`]([^"'`]+)["'`]/g),
      ...rc.body.matchAll(/\.post\(["'`]([^"'`]+)["'`]/g),
    ].flatMap(arr => arr.map(m => m[1])))];
    const apiFetches = fetches.filter(u => /api|stream|play|source|video|watch|hls|m3u8|mp4|torrent|movie|tv|series/i.test(u));
    if (apiFetches.length > 0) {
      console.log('  chunk ' + chunk + ' — API fetch patterns:');
      apiFetches.slice(0, 15).forEach(u => console.log('    ' + u.slice(0, 200)));
    }
  }
})();
