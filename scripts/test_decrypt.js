// Test decrypting stream_urls using the WASM decryptor from vidsrcme.ru
const fs = require('fs');
const https = require('https');

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';
const REFERER = 'https://cloudorchestranova.com/';

function fetchBuf(url, { headers = {}, timeout = 30000 } = {}) {
  return new Promise((resolve, reject) => {
    const u = new URL(url);
    const req = https.request({
      hostname: u.hostname,
      path: u.pathname + u.search,
      method: 'GET',
      headers: { 'User-Agent': UA, 'Referer': REFERER, 'Accept': '*/*', ...headers },
      timeout,
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

function b64decode(s) {
  return Buffer.from(s, 'base64');
}

async function main() {
  const imdbId = process.argv[2] || 'tt15239678';
  const type = process.argv[3] || 'movie';   // 'movie' | 'tv'
  const season = process.argv[4] || null;
  const episode = process.argv[5] || null;

  let apiUrl = `https://data.vidsrcme.ru/api.php?type=${type}&imdb=${imdbId}`;
  if (type === 'tv' && season && episode) apiUrl += `&season=${season}&episode=${episode}`;
  apiUrl += '&stream_urls';

  console.log(`[1] Fetching encrypted stream data: ${apiUrl}`);
  const r1 = await fetchBuf(apiUrl, { headers: { 'X-Requested-With': 'XMLHttpRequest' } });
  if (r1.status !== 200) { console.error(`HTTP ${r1.status}`); process.exit(1); }

  const json = JSON.parse(r1.body.toString('utf8'));
  console.log(`    title: ${json.data.title}`);
  console.log(`    file_name: ${json.data.file_name}`);
  console.log(`    encrypted stream_urls length: ${json.data.stream_urls.length}`);
  console.log(`    vs.w: ${json.vs && json.vs.w}`);
  console.log(`    vs.wasm_url: ${json.vs && json.vs.wasm_url}`);

  console.log(`\n[2] Fetching WASM decryptor...`);
  const wasmUrl = json.vs.wasm_url;
  const r2 = await fetchBuf(wasmUrl, { headers: { 'Accept': 'application/wasm' } });
  if (r2.status !== 200) { console.error(`HTTP ${r2.status} for wasm`); process.exit(1); }
  console.log(`    wasm bytes: ${r2.body.length}`);
  console.log(`    wasm magic: ${r2.body.slice(0, 4).toString('hex')} (0061736d = wasm)`);

  fs.writeFileSync('/home/z/my-project/scripts/decrypt.wasm', r2.body);

  console.log(`\n[3] Instantiating WASM + decrypting stream_urls...`);
  const mod = await WebAssembly.compile(r2.body);
  const inst = await WebAssembly.instantiate(mod, {});
  const ex = inst.exports;
  console.log(`    exports: ${Object.keys(ex).join(', ')}`);

  const enc = b64decode(json.data.stream_urls);
  console.log(`    encrypted bytes: ${enc.length}`);
  if (typeof ex.alloc !== 'function' || typeof ex.decrypt !== 'function') {
    console.error('Missing required exports'); process.exit(1);
  }
  const ptr = ex.alloc(enc.length);
  new Uint8Array(ex.memory.buffer, ptr, enc.length).set(enc);
  const outLen = ex.decrypt(ptr, enc.length);
  console.log(`    decrypted length: ${outLen}`);
  const dec = Buffer.from(new Uint8Array(ex.memory.buffer, ptr + 12, outLen)).toString('utf8');
  console.log(`\n[4] Decrypted stream URLs:`);
  const urls = dec.split('\n').filter(Boolean);
  for (const u of urls) console.log(`    - ${u}`);
  return urls;
}

main().catch(e => { console.error(e); process.exit(1); });
