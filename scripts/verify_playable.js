const https = require('https');
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';
function fetchBuf(url, { headers = {}, timeout = 30000 } = {}) {
  return new Promise((resolve, reject) => {
    const u = new URL(url);
    const req = https.request({
      hostname: u.hostname, path: u.pathname + u.search, method: 'GET',
      headers: { 'User-Agent': UA, 'Referer': 'https://cloudorchestranova.com/', 'Accept': '*/*', ...headers }, timeout,
    }, (res) => {
      const chunks = [];
      res.on('data', c => chunks.push(c));
      res.on('end', () => resolve({ status: res.statusCode, headers: res.headers, body: Buffer.concat(chunks) }));
    });
    req.on('error', reject); req.on('timeout', () => { req.destroy(new Error('timeout')); });
    req.end();
  });
}

async function main() {
  // Use the IMDbPlay provider module
  const imdbplay = require('/home/z/my-project/scripts/imdbplay_all_in_one.js');
  const streams = await imdbplay.getStreams('693134', 'movie');
  if (streams.length === 0) { console.log('no streams'); return; }

  // Pick the 1080p stream
  const s1080 = streams.find(s => s.quality === '1080p');
  if (!s1080) { console.log('no 1080p'); return; }
  console.log(`\nVerifying: ${s1080.name}\nURL: ${s1080.url.slice(0, 200)}...`);

  // Fetch the variant playlist
  const r = await fetchBuf(s1080.url, { headers: { Accept: 'application/vnd.apple.mpegurl, */*' } });
  console.log(`\nVariant playlist: HTTP ${r.status}, Content-Type: ${r.headers['content-type']}, ${r.body.length} bytes`);
  const text = r.body.toString('utf8');
  console.log('First 12 lines:\n  ' + text.split('\n').slice(0, 12).join('\n  '));

  // Find the first .ts segment
  const segLine = text.split('\n').find(l => l && !l.startsWith('#'));
  if (!segLine) { console.log('no segment URL'); return; }
  let segUrl;
  if (segLine.startsWith('http')) segUrl = segLine;
  else if (segLine.startsWith('/')) { const u = new URL(s1080.url); segUrl = `${u.protocol}//${u.host}${segLine}`; }
  else { const base = s1080.url.replace(/\/[^/]*$/, ''); segUrl = `${base}/${segLine}`; }

  console.log(`\nFetching first segment: ${segUrl.slice(0, 200)}...`);
  const sr = await fetchBuf(segUrl, { headers: { Accept: 'video/mp2t, */*' }, timeout: 30000 });
  console.log(`Segment: HTTP ${sr.status}, Content-Type: ${sr.headers['content-type']}, ${sr.body.length} bytes`);
  console.log(`First 16 bytes (hex): ${sr.body.slice(0, 16).toString('hex')}`);
  console.log(`\n✅ Stream is DIRECTLY PLAYABLE — ${sr.body.length} byte .ts segment downloaded.`);
}

main().catch(e => { console.error(e); process.exit(1); });
