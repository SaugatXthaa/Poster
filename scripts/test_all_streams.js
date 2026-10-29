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
  const imdbId = process.argv[2] || 'tt15239678';
  const type = process.argv[3] || 'movie';
  const season = process.argv[4] || null;
  const episode = process.argv[5] || null;

  let apiUrl = `https://data.vidsrcme.ru/api.php?type=${type}&imdb=${imdbId}`;
  if (type === 'tv' && season && episode) apiUrl += `&season=${season}&episode=${episode}`;
  apiUrl += '&stream_urls';

  const r = await fetchBuf(apiUrl, { headers: { 'X-Requested-With': 'XMLHttpRequest' } });
  const json = JSON.parse(r.body.toString('utf8'));
  console.log(`Title: ${json.data.title}\nfile_name: ${json.data.file_name}\n`);

  const wr = await fetchBuf(json.vs.wasm_url, { headers: { Accept: 'application/wasm' } });
  const mod = await WebAssembly.compile(wr.body);
  const inst = await WebAssembly.instantiate(mod, {});
  const enc = Buffer.from(json.data.stream_urls, 'base64');
  const ptr = inst.exports.alloc(enc.length);
  new Uint8Array(inst.exports.memory.buffer, ptr, enc.length).set(enc);
  const outLen = inst.exports.decrypt(ptr, enc.length);
  const dec = Buffer.from(new Uint8Array(inst.exports.memory.buffer, ptr + 12, outLen)).toString('utf8');
  const urls = dec.split('\n').filter(Boolean);

  // Group by host
  const byHost = {};
  for (const u of urls) {
    const h = new URL(u).host;
    if (!byHost[h]) byHost[h] = [];
    byHost[h].push(u);
  }

  for (const [host, streamUrls] of Object.entries(byHost)) {
    console.log(`\n=== Host: ${host} (${streamUrls.length} streams) ===`);
    const origin = 'https://' + host;
    const tr = await fetchBuf(origin + '/generate.php', { headers: { Accept: 'text/plain, application/json, */*' } });
    let token = tr.body.toString('utf8').trim();
    if (token && (token[0] === '{' || token[0] === '[')) {
      try { const j = JSON.parse(token); if (typeof j === 'string') token = j; else token = j.token || j.data || j.string || j.result || ''; } catch (e) {}
    }
    console.log(`  Token: ${token.slice(0,40)}${token.length > 40 ? '...' : ''}`);
    for (let i = 0; i < streamUrls.length; i++) {
      const playable = streamUrls[i] + '?token=' + token;
      const mr = await fetchBuf(playable, { headers: { Accept: 'application/vnd.apple.mpegurl, */*' } });
      if (mr.status !== 200) { console.log(`  [${i+1}] HTTP ${mr.status}`); continue; }
      const body = mr.body.toString('utf8');
      const variants = body.split('\n').filter(l => l.startsWith('#EXT-X-STREAM-INF'));
      console.log(`  [${i+1}] HTTP ${mr.status} — ${variants.length} variants:`);
      for (const v of variants) {
        const res = v.match(/RESOLUTION=(\d+x\d+)/);
        const br = v.match(/BANDWIDTH=(\d+)/);
        const codecs = v.match(/CODECS="([^"]+)"/);
        console.log(`      - ${res ? res[1] : '?'} @ ${br ? Math.round(parseInt(br[1])/1000) : '?'}Mbps, codecs=${codecs ? codecs[1] : '?'}`);
      }
    }
  }
}

main().catch(e => { console.error(e); process.exit(1); });
