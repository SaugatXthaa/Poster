// Search the NEW stellar.rip chunks for any NEW stream/playback patterns.
// Maybe they replaced /api/stream (retired) with a new endpoint.
'use strict';

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';

async function fetchText(url) {
  const res = await fetch(url, {
    headers: { 'User-Agent': UA },
    signal: AbortSignal.timeout(20000),
  });
  return { status: res.status, body: await res.text() };
}

(async () => {
  console.log('===== Fetch all chunks and search for NEW patterns =====');
  const page = await fetchText('https://stellar.rip/en/movie/tt1375666/watch');
  const jsUrls = [...new Set([...page.body.matchAll(/\/_next\/static\/chunks\/[^"'\\]+\.js/g)].map(m => m[0]))];
  
  // Patterns to search for
  const searchPatterns = [
    /["'`](\/api\/[a-z0-9_\-\/]+)["'`]/gi,        // all /api/ paths
    /["'`](\/(?:play|player|stream|sources?|videos?)\/[^"'`]+)["'`]/gi, // play/player/stream/sources paths
    /["'`](\/(?:playback|video|watch|embed)\/[^"'`]+)["'`]/gi,
    /["'`](\/(?:v\d+|api\/v\d+)\/[^"'`]+)["'`]/gi, // /v1/ /v2/ API versions
    /["'`](https?:\/\/[^"'`]*(?:stream|playback|video|hls|m3u8|mp4|mkv)[^"'`]*)["'`]/gi, // external stream URLs
    /\b(?:playbackUrl|videoUrl|streamUrl|hlsUrl|sourceUrl|playlistUrl|playUrl|fileUrl)\b/gi, // JS variable names
    /\b(?:getStream|fetchStream|resolveStream|loadStream|playStream)\b/gi, // JS function names
  ];
  
  const found = new Map();
  for (const chunk of jsUrls) {
    const rc = await fetchText('https://stellar.rip' + chunk);
    if (rc.status !== 200) continue;
    for (const re of searchPatterns) {
      for (const m of rc.body.matchAll(re)) {
        const key = re.source.slice(0, 30);
        if (!found.has(key)) found.set(key, new Set());
        found.get(key).add(m[1] || m[0]);
      }
    }
  }
  
  console.log('\n--- ALL pattern matches across all chunks ---');
  for (const [key, matches] of found.entries()) {
    console.log('\n  pattern ' + key + ' (' + matches.size + ' unique):');
    [...matches].slice(0, 25).forEach(m => console.log('    ' + m.slice(0, 250)));
  }
  
  // Also try fetching the /en/movie/tt1375666 page (info page) — maybe the info page has stream URLs
  console.log('\n===== /en/movie/tt1375666 (info page) =====');
  const r2 = await fetchText('https://stellar.rip/en/movie/tt1375666');
  const urls2 = [...new Set([...r2.body.matchAll(/https?:\/\/[A-Za-z0-9.\-_/]+/g)].map(m => m[0]))];
  console.log('  unique URLs in info page: ' + urls2.length);
  urls2.filter(u => /stream|video|play|m3u8|mp4|mkv|cdn/i.test(u)).slice(0, 20).forEach(u => console.log('    ' + u.slice(0, 200)));
})();
