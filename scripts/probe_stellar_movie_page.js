// Look at the stellar.rip movie page structure to find the stream API.
'use strict';

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';

async function fetchText(url, opts = {}) {
  console.log('  GET ' + url);
  const res = await fetch(url, {
    redirect: 'follow',
    headers: { 'User-Agent': UA, 'Accept': 'text/html,application/json,*/*', ...(opts.headers || {}) },
    signal: AbortSignal.timeout(opts.timeout || 30000),
  });
  const text = await res.text();
  return { status: res.status, body: text, headers: res.headers };
}

(async () => {
  console.log('===== stellar.rip/en/movie/tt1375666 — find stream data =====');
  const r = await fetchText('https://stellar.rip/en/movie/tt1375666');
  console.log('  status: ' + r.status + ', body length: ' + r.body.length);

  // Next.js typically embeds data in <script id="__NEXT_DATA__"> or as RSC payload
  // Let's look for stream URLs, video tags, m3u8, mp4, api endpoints
  const patterns = [
    /https?:\/\/[^"'\s]+\.m3u8[^"'\s]*/gi,
    /https?:\/\/[^"'\s]+\.mp4[^"'\s]*/gi,
    /https?:\/\/[^"'\s]+\.mkv[^"'\s]*/gi,
    /https?:\/\/[^"'\s]*api[^"'\s]*/gi,
    /https?:\/\/[^"'\s]*stream[^"'\s]*/gi,
    /https?:\/\/[^"'\s]*play[^"'\s]*/gi,
    /https?:\/\/[^"'\s]*source[^"'\s]*/gi,
  ];
  for (const re of patterns) {
    const matches = [...new Set(r.body.match(re) || [])].slice(0, 10);
    if (matches.length > 0) {
      console.log('  pattern ' + re.source + ':');
      matches.forEach(m => console.log('    ' + m.slice(0, 200)));
    }
  }

  // Look for __NEXT_DATA__ script tag (Next.js Pages Router data)
  const ndMatch = r.body.match(/<script[^>]*id=["']__NEXT_DATA__["'][^>]*>([^<]+)<\/script>/);
  if (ndMatch) {
    console.log('\n  __NEXT_DATA__ found (length ' + ndMatch[1].length + '):');
    console.log('    ' + ndMatch[1].slice(0, 1500));
  } else {
    console.log('\n  __NEXT_DATA__ not found (likely App Router/RSC)');
  }

  // Look for RSC (React Server Components) stream chunks
  const rscMatch = r.body.match(/<script[^>]*>self\.__next_f\.push\(\[1,([^<]+)\]\)<\/script>/);
  if (rscMatch) {
    console.log('\n  RSC __next_f found, first chunk (length 1500):');
    console.log('    ' + rscMatch[1].slice(0, 1500));
  }

  // Look for ALL self.__next_f.push chunks (collect all of them)
  const chunks = r.body.match(/self\.__next_f\.push\(\[1,[^<]+\]\)/g) || [];
  console.log('\n  Total __next_f chunks: ' + chunks.length);
  if (chunks.length > 0) {
    // Concatenate and search for stream/video/api patterns
    const all = chunks.join('\n');
    console.log('  Combined chunks length: ' + all.length);
    // Search for video URL patterns in the combined chunks
    const videoPatterns = [
      /https?:\/\/[^"'\s\\]+\.m3u8[^"'\s\\]*/gi,
      /https?:\/\/[^"'\s\\]+\.mp4[^"'\s\\]*/gi,
      /https?:\/\/[^"'\s\\]+\.mkv[^"'\s\\]*/gi,
      /["']stream["']\s*:\s*["']([^"']+)["']/gi,
      /["']source["']\s*:\s*["']([^"']+)["']/gi,
      /["']url["']\s*:\s*["']([^"']+)["']/gi,
      /["']videoUrl["']\s*:\s*["']([^"']+)["']/gi,
      /["']playbackUrl["']\s*:\s*["']([^"']+)["']/gi,
      /["']hls["']\s*:\s*["']([^"']+)["']/gi,
    ];
    for (const re of videoPatterns) {
      const matches = [...new Set(all.match(re) || [])].slice(0, 5);
      if (matches.length > 0) {
        console.log('  pattern ' + re.source.slice(0, 50) + ':');
        matches.forEach(m => console.log('    ' + m.slice(0, 250)));
      }
    }
  }
})();
