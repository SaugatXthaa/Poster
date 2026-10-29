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

  const wr = await fetchBuf(json.vs.wasm_url, { headers: { 'Accept': 'application/wasm' } });
  const mod = await WebAssembly.compile(wr.body);
  const inst = await WebAssembly.instantiate(mod, {});
  const enc = Buffer.from(json.data.stream_urls, 'base64');
  const ptr = inst.exports.alloc(enc.length);
  new Uint8Array(inst.exports.memory.buffer, ptr, enc.length).set(enc);
  const outLen = inst.exports.decrypt(ptr, enc.length);
  const dec = Buffer.from(new Uint8Array(inst.exports.memory.buffer, ptr + 12, outLen)).toString('utf8');
  const urls = dec.split('\n').filter(Boolean);

  console.log(`Title: ${json.data.title}\nfile_name: ${json.data.file_name}\n`);

  for (let i = 0; i < urls.length; i++) {
    const u = urls[i];
    const origin = new URL(u).origin;
    console.log(`\n=== Stream ${i+1} — host: ${new URL(u).host} ===`);

    // Fetch token
    const tr = await fetchBuf(origin + '/generate.php', { headers: { Accept: 'text/plain, application/json, */*' } });
    const token = (() => {
      const t = tr.body.toString('utf8').trim();
      if (!t) return '';
      if (t[0] === '{' || t[0] === '[') {
        try { const j = JSON.parse(t); if (typeof j === 'string') return j; return (j.token || j.data || j.string || j.result || '').toString(); } catch (e) {}
      }
      return t;
    })();

    if (!token) { console.log(`  token: (empty, HTTP ${tr.status})`); continue; }
    const playable = u + (u.indexOf('?') > -1 ? '&' : '?') + 'token=' + token;

    const mr = await fetchBuf(playable, { headers: { Accept: 'application/vnd.apple.mpegurl, */*' } });
    if (mr.status !== 200) { console.log(`  master: HTTP ${mr.status}`); continue; }

    const body = mr.body.toString('utf8');
    const variants = body.split('\n').filter(l => l.startsWith('#EXT-X-STREAM-INF'));
    console.log(`  token: ${token.slice(0,40)}...`);
    console.log(`  master.m3u8 HTTP ${mr.status}, ${variants.length} variants:`);
    for (const v of variants) {
      const res = v.match(/RESOLUTION=(\d+x\d+)/);
      const br = v.match(/BANDWIDTH=(\d+)/);
      const codecs = v.match(/CODECS="([^"]+)"/);
      const fr = v.match(/FRAME-RATE=([\d.]+)/);
      console.log(`    - ${res ? res[1] : '?'} @ ${br ? Math.round(parseInt(br[1])/1000) : '?'}Mbps, codecs=${codecs ? codecs[1] : '?'}, fps=${fr ? fr[1] : '?'}`);
    }
  }
}

main().catch(e => { console.error(e); process.exit(1); });
