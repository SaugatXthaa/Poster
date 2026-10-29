// Look at all stellar.rip movie page RSC chunks and try watch/play routes.
'use strict';

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';

async function fetchText(url) {
  console.log('  GET ' + url);
  const res = await fetch(url, {
    redirect: 'follow',
    headers: { 'User-Agent': UA, 'Accept': 'text/html,application/json,*/*' },
    signal: AbortSignal.timeout(30000),
  });
  const text = await res.text();
  return { status: res.status, body: text };
}

(async () => {
  console.log('===== 1. Look for ALL URLs in movie page =====');
  const r = await fetchText('https://stellar.rip/en/movie/tt1375666');
  // Find ALL https URLs
  const urls = [...new Set([...r.body.matchAll(/https?:\/\/[^"'\s\\]+/g)].map(m => m[0]))];
  console.log('  total unique URLs: ' + urls.length);
  // Show ones that look like API/stream endpoints
  const interesting = urls.filter(u => /api|stream|play|video|source|embed|watch|m3u8|mp4|mkv|hls/i.test(u));
  console.log('  interesting URLs (api/stream/play/video/embed/watch/m3u8/mp4/mkv/hls):');
  interesting.slice(0, 30).forEach(u => console.log('    ' + u.slice(0, 250)));

  console.log('\n===== 2. Try /watch, /play, /embed, /stream variants =====');
  for (const p of [
    '/en/movie/tt1375666/watch',
    '/en/movie/tt1375666/play',
    '/en/movie/tt1375666/stream',
    '/en/movie/tt1375666/embed',
    '/en/movie/tt1375666/sources',
    '/en/movie/tt1375666/video',
    '/en/movie/tt1375666/playlist',
    '/en/movie/tt1375666.m3u8',
    '/en/movie/tt1375666.json',
    '/en/movie/tt1375666/playlist.m3u8',
  ]) {
    const r2 = await fetchText('https://stellar.rip' + p);
    console.log('  ' + p + ' → ' + r2.status + ' | body (200 chars): ' + r2.body.slice(0, 200).replace(/\n/g, ' '));
  }

  console.log('\n===== 3. Look at Next.js JS chunks for API patterns =====');
  // Find all JS chunk URLs in the page
  const chunks = [...new Set([...r.body.matchAll(/\/_next\/static\/chunks\/[^"'\s\\]+\.js/g)].map(m => m[0]))];
  console.log('  total JS chunks referenced: ' + chunks.length);
  // Fetch first 3 chunks and look for stream URL patterns
  for (const chunk of chunks.slice(0, 5)) {
    const url = 'https://stellar.rip' + chunk;
    console.log('\n  --- chunk ' + chunk + ' ---');
    const r3 = await fetchText(url);
    if (r3.status === 200) {
      // Find fetch/axios patterns
      const apiPatterns = [
        /fetch\(["'`]([^"'`]+)["'`]/g,
        /["'`]\/api\/[^"'`]+["'`]/g,
        /["'`]\/en\/[^"'`]+["'`]/g,
        /https?:\/\/[^"'\s\\]+\.m3u8[^"'\s\\]*/g,
        /https?:\/\/[^"'\s\\]+\.mp4[^"'\s\\]*/g,
        /source|stream|playbackUrl|videoUrl|hls|playlistUrl/gi,
      ];
      for (const re of apiPatterns) {
        const matches = [...new Set([...r3.body.matchAll(re)].map(m => m[0]))].slice(0, 5);
        if (matches.length > 0) {
          console.log('    pattern ' + re.source.slice(0, 60) + ':');
          matches.forEach(m => console.log('      ' + m.slice(0, 200)));
        }
      }
    }
  }
})();
