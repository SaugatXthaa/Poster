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

function b64decode(s) { return Buffer.from(s, 'base64'); }

async function fetchEncrypted(imdbId, type, season, episode) {
  let url = `https://data.vidsrcme.ru/api.php?type=${type}&imdb=${imdbId}`;
  if (type === 'tv' && season && episode) url += `&season=${season}&episode=${episode}`;
  url += '&stream_urls';
  const r = await fetchBuf(url, { headers: { 'X-Requested-With': 'XMLHttpRequest' } });
  if (r.status !== 200) throw new Error(`api.php ${r.status}`);
  return JSON.parse(r.body.toString('utf8'));
}

async function decryptStreamUrls(json) {
  const wasmUrl = json.vs.wasm_url;
  if (!wasmUrl) throw new Error('no wasm_url');
  const wr = await fetchBuf(wasmUrl, { headers: { 'Accept': 'application/wasm' } });
  if (wr.status !== 200) throw new Error(`wasm ${wr.status}`);
  const mod = await WebAssembly.compile(wr.body);
  const inst = await WebAssembly.instantiate(mod, {});
  const ex = inst.exports;
  const enc = b64decode(json.data.stream_urls);
  const ptr = ex.alloc(enc.length);
  new Uint8Array(ex.memory.buffer, ptr, enc.length).set(enc);
  const outLen = ex.decrypt(ptr, enc.length);
  const txt = Buffer.from(new Uint8Array(ex.memory.buffer, ptr + 12, outLen)).toString('utf8');
  return txt.split('\n').filter(Boolean);
}

function parseToken(text) {
  const t = String(text || '').trim();
  if (!t) return '';
  if (t.charAt(0) === '{' || t.charAt(0) === '[') {
    try { const j = JSON.parse(t); if (typeof j === 'string') return j; if (j && typeof j === 'object') return j.token || j.data || j.string || j.result || ''; } catch (e) {}
  }
  return t;
}

async function fetchToken(streamUrl) {
  const u = new URL(streamUrl);
  const tokenUrl = `${u.protocol}//${u.host}/generate.php`;
  const r = await fetchBuf(tokenUrl, { headers: { 'Accept': 'text/plain, application/json, */*' } });
  if (r.status !== 200) return '';
  return parseToken(r.body.toString('utf8'));
}

function applyToken(url, token) {
  if (!token) return url;
  if (url.indexOf('__TOKEN__') > -1) return url.split('__TOKEN__').join(token);
  return url + (url.indexOf('?') > -1 ? '&' : '?') + 'token=' + token;
}

async function main() {
  const imdbId = process.argv[2] || 'tt15239678';
  const type = process.argv[3] || 'movie';
  const season = process.argv[4] || null;
  const episode = process.argv[5] || null;

  const json = await fetchEncrypted(imdbId, type, season, episode);
  console.log(`Title: ${json.data.title}`);
  console.log(`file_name: ${json.data.file_name}`);
  console.log(`thumbnails: ${json.thumbnails_url}`);

  const urls = await decryptStreamUrls(json);
  console.log(`\nFound ${urls.length} stream(s):`);
  for (const u of urls) console.log(`  - ${u.slice(0, 80)}...`);

  console.log(`\nFetching token + verifying each stream:`);
  for (const u of urls) {
    const origin = new URL(u).host;
    const token = await fetchToken(u);
    const playable = applyToken(u, token);
    const r = await fetchBuf(playable, { headers: { 'Accept': 'application/vnd.apple.mpegurl, */*' } });
    const preview = r.body.toString('utf8').slice(0, 400);
    console.log(`\n  [${origin}] token=${token.slice(0,30)}${token.length > 30 ? '...' : ''}`);
    console.log(`  HTTP ${r.status}, Content-Type: ${r.headers['content-type']}`);
    console.log(`  Body preview:\n    ${preview.split('\n').slice(0, 8).join('\n    ')}`);
  }
}

main().catch(e => { console.error(e); process.exit(1); });
