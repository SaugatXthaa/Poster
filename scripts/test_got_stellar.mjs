import { got } from 'got-scraping';
try {
  const res = await got('https://stellar.gdn/', {
    timeout: { request: 30000 },
    headers: {
      'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36',
      'Accept': 'text/html,application/xhtml+xml',
      'Accept-Language': 'en-US,en;q=0.9',
    },
  });
  console.log('HTTP:', res.statusCode);
  console.log('Body length:', res.body.length);
  console.log('Title:', res.body.match(/<title>[^<]+<\/title>/)?.[0]);
  console.log('First 800:', res.body.slice(0, 800));
} catch(e) {
  console.log('ERR:', e.message);
  if (e.response) {
    console.log('HTTP:', e.response.statusCode);
  }
}
