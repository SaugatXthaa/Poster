const https = require('https');
const http = require('http');
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';

function fetchBuf(url, { headers = {}, timeout = 20000 } = {}) {
  return new Promise((resolve, reject) => {
    const u = new URL(url);
    const lib = u.protocol === 'http:' ? http : https;
    const req = lib.request({
      hostname: u.hostname, path: u.pathname + u.search, method: 'GET',
      headers: { 'User-Agent': UA, 'Accept': '*/*', ...headers }, timeout,
    }, (res) => {
      const chunks = [];
      res.on('data', c => chunks.push(c));
      res.on('end', () => resolve({ status: res.statusCode, headers: res.headers, body: Buffer.concat(chunks) }));
    });
    req.on('error', reject);
    req.on('timeout', () => { req.destroy(new Error('timeout')); });
    req.end();
  });
}

async function testStream(name, url) {
  console.log(`\n=== ${name} ===`);
  console.log(`URL: ${url.slice(0, 200)}${url.length > 200 ? '...' : ''}`);
  try {
    const r = await fetchBuf(url, { headers: { Accept: 'application/vnd.apple.mpegurl, */*' }, timeout: 15000 });
    const body = r.body.toString('utf8');
    console.log(`HTTP ${r.status}, Content-Type: ${r.headers['content-type']}, ${r.body.length} bytes`);
    if (r.status === 200 && body.startsWith('#EXTM3U')) {
      const variants = body.split('\n').filter(l => l.startsWith('#EXT-X-STREAM-INF'));
      console.log(`✅ Valid HLS — ${variants.length} variants:`);
      for (const v of variants) {
        const res = v.match(/RESOLUTION=(\d+x\d+)/);
        const br = v.match(/BANDWIDTH=(\d+)/);
        const codecs = v.match(/CODECS="([^"]+)"/);
        console.log(`   - ${res ? res[1] : '?'} @ ${br ? Math.round(parseInt(br[1])/1000) : '?'}kbps, codecs=${codecs ? codecs[1] : '?'}`);
      }
    } else {
      console.log(`Body preview: ${body.slice(0, 200)}`);
    }
  } catch (e) { console.log(`❌ Error: ${e.message}`); }
}

async function main() {
  const tmdbId = process.argv[2] || '693134';
  const type = process.argv[3] || 'movie';   // movie | tv
  const season = process.argv[4] || null;
  const episode = process.argv[5] || null;
  const providers = ['apex','pulse','solstice','quasar','horizon','primevids','flowcast','asiacloud','citadel','hindicast','guru'];
  
  for (const p of providers) {
    let url = `https://scrapper.rivestream.app/api/provider?provider=${p}&id=${tmdbId}`;
    if (type === 'tv' && season && episode) url += `&season=${season}&episode=${episode}`;
    if (p === 'primevids' || p === 'citadel') url += `&cb=${Math.floor(Date.now()/3e6)}`;
    
    console.log(`\n>>> ${p.toUpperCase()} <<<`);
    try {
      const r = await fetchBuf(url, { headers: { Accept: 'application/json', Referer: 'https://rivestream.ru/' }, timeout: 15000 });
      if (r.status !== 200) { console.log(`HTTP ${r.status}`); continue; }
      const json = JSON.parse(r.body.toString('utf8'));
      if (!json.data || !json.data.sources || json.data.sources.length === 0) {
        console.log('No sources');
        continue;
      }
      console.log(`Got ${json.data.sources.length} source(s):`);
      for (const src of json.data.sources) {
        await testStream(`${p}/${src.source} [${src.quality}]`, src.url);
      }
    } catch (e) { console.log(`Error: ${e.message}`); }
  }
}
main().catch(e => { console.error(e); process.exit(1); });
