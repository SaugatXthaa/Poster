const https = require('https');
const http = require('http');

function fetch(url, opts = {}, timeout = 10000) {
  return new Promise((resolve, reject) => {
    const u = new URL(url);
    const lib = u.protocol === 'https:' ? https : http;
    const req = lib.request({
      hostname: u.hostname,
      port: u.port || (u.protocol === 'https:' ? 443 : 80),
      path: u.pathname + u.search,
      method: opts.method || 'GET',
      headers: {
        'User-Agent': 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36',
        'Accept': '*/*',
        'Accept-Language': 'en-US,en;q=0.9',
        ...opts.headers,
      },
    }, (res) => {
      const chunks = [];
      res.on('data', c => chunks.push(c));
      res.on('end', () => resolve({
        status: res.statusCode,
        headers: res.headers,
        body: Buffer.concat(chunks).toString('utf-8'),
      }));
    });
    req.on('error', reject);
    req.setTimeout(timeout, () => { req.destroy(); reject(new Error('timeout')); });
    if (opts.body) req.write(opts.body);
    req.end();
  });
}

async function getGemmaStreams(imdbId) {
  // Step 1: Get the player config
  const r1 = await fetch('https://gemma416okl.com/play/' + imdbId);
  const m = r1.body.match(/(?:let p3 = |new HDVBPlayer\()(\{[\s\S]*?\})\s*[;)]/);
  if (!m) throw new Error('No player config found for ' + imdbId);
  
  const config = JSON.parse(m[1].replace(/\\\//g, '/'));
  let fileUrl = config.file;
  if (!fileUrl.startsWith('http')) fileUrl = 'https://rasta428jem.com' + fileUrl;
  
  // Step 2: POST to get sources (IMMEDIATELY - token expires in ~2 seconds)
  const r2 = await fetch(fileUrl, {
    method: 'POST',
    headers: {
      'X-CSRF-TOKEN': config.key,
      'Content-Type': 'application/x-www-form-urlencoded',
      'Origin': 'https://gemma416okl.com',
      'Referer': 'https://gemma416okl.com/play/' + imdbId,
    },
  });
  
  if (r2.body === '7' || !r2.body.startsWith('[')) {
    throw new Error('Token expired or invalid response: ' + r2.body.substring(0, 50));
  }
  
  const sources = JSON.parse(r2.body);
  const baseUrl = fileUrl.substring(0, fileUrl.lastIndexOf('/') + 1);
  
  // Step 3: Get stream URLs for ALL sources in parallel (racing against token expiry)
  const streamPromises = sources
    .filter(s => s.file && s.file.startsWith('~'))
    .map(src => {
      const streamUrl = baseUrl + src.file.substring(1) + '.txt';
      return fetch(streamUrl, {
        method: 'POST',
        headers: {
          'X-CSRF-TOKEN': config.key,
          'Content-Type': 'application/x-www-form-urlencoded',
          'Origin': 'https://gemma416okl.com',
          'Referer': 'https://gemma416okl.com/play/' + imdbId,
        },
      }).then(r => ({ title: src.title, url: r.body.trim() }))
        .catch(e => ({ title: src.title, url: null, error: e.message }));
    });
  
  const results = await Promise.all(streamPromises);
  return results.filter(r => r.url && r.url.startsWith('http'));
}

(async () => {
  const imdbId = process.argv[2] || 'tt0468569';
  console.log('Fetching streams for', imdbId);
  
  // Retry up to 3 times (token expiry is the main issue)
  for (let attempt = 1; attempt <= 3; attempt++) {
    try {
      console.log('Attempt', attempt + '...');
      const streams = await getGemmaStreams(imdbId);
      console.log('Got', streams.length, 'streams:');
      for (const s of streams) {
        console.log('  ' + s.title + ':', s.url.substring(0, 100) + '...');
      }
      
      if (streams.length > 0) {
        // Test the first stream URL
        console.log('\nTesting first stream URL...');
        const r = await fetch(streams[0].url, {
          headers: {
            'Referer': 'https://gemma416okl.com/',
            'Origin': 'https://i-arch-400.keymi417exx.com',
          },
        });
        console.log('Status:', r.status);
        console.log('Content-Type:', r.headers['content-type']);
        console.log('Body preview:', r.body.substring(0, 200));
        break;
      }
    } catch(e) {
      console.log('Error:', e.message);
      if (attempt < 3) await new Promise(r => setTimeout(r, 500));
    }
  }
})();
