import { got } from 'got-scraping';
try {
  const res = await got('https://new5.hdhub4u.cl/?s=inception', {
    headers: { 'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36' },
    timeout: { request: 15000 },
    retry: { limit: 1 },
  });
  console.log('HTTP:', res.statusCode, 'Body length:', res.body.length);
  // Check if redirected
  console.log('URL:', res.url);
  // Find movie links
  const links = [...res.body.matchAll(/href="(https:\/\/new5\.hdhub4u\.cl\/([a-z0-9-]+)\/?)"/g)];
  const movies = links.filter(m => !m[2].match(/^(category|tag|page|wp-|feed|comment|disclaimer|dmca|privacy|contact|about|how-to|join|request|xmlrpc)/));
  console.log('Movie links:', movies.length);
  movies.slice(0, 5).forEach(m => console.log('  ', m[2]));
} catch(e) {
  console.log('ERR:', e.message);
}
