const fs = require('fs');
const https = require('https');
const js = fs.readFileSync('/tmp/cinejoy_app.js', 'utf-8');
const chunks = [...js.matchAll(/"(\.\.\/(?:chunks|nodes|assets)\/[A-Za-z0-9_.\-\/]+\.js)"/g)];
const urls = [...new Set(chunks.map(m => m[1]))];
console.log('Found', urls.length, 'unique chunks');

function download(fullUrl) {
  return new Promise((resolve) => {
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
  for (const relPath of urls) {
    const urlPath = relPath.replace(/^\.\.\//, '');
    const fullUrl = 'https://cinejoy.to/_app/immutable/' + urlPath;
    const fname = urlPath.split('/').join('_');
    const data = await download(fullUrl);
    if (data && data.length > 100 && !data.toString().startsWith('<!doctype')) {
      fs.writeFileSync('/tmp/cinejoy_chunks/' + fname, data);
      downloaded++;
      const text = data.toString('utf8');
      if (text.includes('charCodeAt') && text.includes('subtle')) {
        console.log('CRYPTO+OBFUSCATED:', fname, '(' + data.length + ' bytes)');
      } else if (text.includes('subtle') || text.includes('importKey') || text.includes('deriveBits')) {
        console.log('CRYPTO:', fname, '(' + data.length + ' bytes)');
      } else if (text.includes('charCodeAt') && data.length > 30000) {
        console.log('OBFUSCATED:', fname, '(' + data.length + ' bytes)');
      }
    }
  }
  console.log('Downloaded:', downloaded, 'valid JS files');
})();
