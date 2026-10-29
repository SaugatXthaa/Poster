const fs = require('fs');
const https = require('https');
const js = fs.readFileSync('/tmp/cinejoy_app.js','utf-8');
const chunks = [...js.matchAll(/"(\.\.\/(?:chunks|nodes|assets)\/[A-Za-z0-9_.\-\/]+\.js)"/g)];
const urls = [...new Set(chunks.map(m => m[1]))];
console.log('Found', urls.length, 'unique chunks');

function download(url) {
  return new Promise((resolve) => {
    const fullUrl = 'https://cinejoy.to/_app/immutable/' + url.replace(/^\.\.\//, '');
    https.get(fullUrl, { headers: { 'User-Agent': 'Mozilla/5.0' } }, (res) => {
      if (res.statusCode !== 200) { resolve(null); return; }
      const data = [];
      res.on('data', c => data.push(c));
      res.on('end', () => resolve(Buffer.concat(data)));
    }).on('error', () => resolve(null));
  });
}

(async () => {
  let downloaded = 0;
  let withCrypto = 0;
  for (const url of urls) {
    const fname = url.replace(/[\/\\]/g, '_');
    const data = await download(url);
    if (data && data.length > 100) {
      fs.writeFileSync('/tmp/cinejoy_chunks/' + fname, data);
      downloaded++;
      const text = data.toString('utf8');
      if (text.includes('subtle') || text.includes('importKey') || text.includes('deriveKey') || 
          text.includes('deriveBits') || text.includes('generateKey') || text.includes('getRandomValues') ||
          text.includes('crypto.')) {
        withCrypto++;
        console.log('CRYPTO in', fname, '(' + data.length + ' bytes)');
      }
    }
  }
  console.log('Downloaded:', downloaded, '| With crypto:', withCrypto);
})();
