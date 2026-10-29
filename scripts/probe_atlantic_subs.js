const UA = 'Mozilla/5.0';
(async () => {
  console.log('=== Fetch atlantic.st JS bundle ===');
  const r = await fetch('https://atlantic.st/assets/index-D85ysXGG.js', { headers: { 'User-Agent': UA }, signal: AbortSignal.timeout(15000) });
  const t = await r.text();
  console.log('status: ' + r.status + ' length: ' + t.length);
  
  // Search for subtitle-related URLs
  const subRegex = /["'`](https?:\/\/[^"'`]*(?:subtitle|opensubtitle|yifysub|podnapisi|subscene|addic7ed|caption|\.srt|\.vtt)[^"'`]*)["'`]/gi;
  const subMatches = [...new Set([...t.matchAll(subRegex)].map(m => m[1]))];
  console.log('\n=== Subtitle API URLs (' + subMatches.length + ') ===');
  subMatches.slice(0, 15).forEach(u => console.log('  ' + u.slice(0, 250)));

  // Search for ALL fetch() calls
  const fetchRegex = /(?:fetch|axios|\.get|\.post)\s*\(\s*["'`]([^"'`]+)["'`]/g;
  const fetches = [...new Set([...t.matchAll(fetchRegex)].map(m => m[1]))];
  console.log('\n=== ALL fetch/axios URLs (' + fetches.length + ') ===');
  fetches.slice(0, 20).forEach(f => console.log('  ' + f.slice(0, 200)));

  // Search for /api/ paths
  const apiRegex = /["'`](\/api\/[^"'`]+)["'`]/g;
  const apis = [...new Set([...t.matchAll(apiRegex)].map(m => m[1]))];
  console.log('\n=== /api/ paths (' + apis.length + ') ===');
  apis.slice(0, 20).forEach(a => console.log('  ' + a));

  // Search for ALL unique external domains
  const domainRegex = /https?:\/\/([a-z0-9-]+\.[a-z.]+)/gi;
  const domains = [...new Set([...t.matchAll(domainRegex)].map(m => m[1]))];
  console.log('\n=== ALL external domains (' + domains.length + ') ===');
  domains.forEach(d => console.log('  ' + d));
})();
