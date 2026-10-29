// Test full reanime.to → flixcloud.cc decryption chain in ONE go.
const https = require('https');
const crypto = require('crypto');

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36';

function fetchBuf(url, { headers = {}, timeout = 30000 } = {}) {
  return new Promise((resolve, reject) => {
    const u = new URL(url);
    const req = https.request({
      hostname: u.hostname, path: u.pathname + u.search, method: 'GET',
      headers: { 'User-Agent': UA, ...headers }, timeout,
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

function sha256Hex(s) {
  return crypto.createHash('sha256').update(s, 'utf8').digest('hex');
}

function b64decode(s) { return Buffer.from(s, 'base64'); }

// JS-style regex match — Node.js supports named groups via match()
// We use plain capture groups and array index instead.
function match1(re, str) {
  const m = str.match(re);
  return m ? m[1] : null;
}

// Walk braces from a starting position to find the matching closing brace
function extractBlock(html, startPos) {
  let depth = 0;
  for (let i = startPos; i < html.length; i++) {
    if (html[i] === '{') depth++;
    else if (html[i] === '}') {
      depth--;
      if (depth === 0) return html.slice(startPos, i + 1);
    }
  }
  return null;
}

async function resolveFlixcloudStream(accessId) {
  // 1. Fetch flixcloud player HTML
  console.log(`[1] Fetching flixcloud.cc/e/${accessId}?v=2`);
  const r = await fetchBuf(`https://flixcloud.cc/e/${accessId}?v=2`);
  if (r.status !== 200) throw new Error(`flixcloud HTTP ${r.status}`);
  const html = r.body.toString('utf8');
  console.log(`   HTML: ${html.length} bytes`);

  // 2. Find obfuscation_seed, w_payload, obfuscated_crypto_data
  const seed = match1(/obfuscation_seed:"([^"]+)"/, html);
  if (!seed) throw new Error('obfuscation_seed not found');
  console.log(`   obfuscation_seed: ${seed}`);

  const wPayloadB64 = match1(/w_payload:"([^"]+)"/, html);
  if (!wPayloadB64) throw new Error('w_payload not found');
  console.log(`   w_payload: ${wPayloadB64.length} chars (base64 WASM)`);

  // 3. Compute field names via SHA-256 chain (xn function)
  let e = seed;
  for (let o = 0; o < 3; o++) e = sha256Hex(e + o.toString());
  let a = e;
  for (let o = 0; o < 3; o++) a = sha256Hex(a + o.toString());
  const fields = {
    containerName: `cd_${e.substring(24, 32)}`,
    arrayName: `ad_${e.substring(32, 40)}`,
    objectName: `od_${e.substring(40, 48)}`,
    keyField: `kf_${e.substring(8, 16)}`,
    ivField: `ivf_${e.substring(16, 24)}`,
    tokenField: `${e.substring(48, 64)}_${e.substring(56, 64)}`,
    keyFrag2Field: `${a.substring(0, 16)}_${a.substring(16, 24)}`,
  };
  console.log(`\n[2] Derived field names:`);
  for (const [k, v] of Object.entries(fields)) console.log(`   ${k}: ${v}`);

  // 4. Extract frag1_b64 + iv_b64 from obfuscated_crypto_data
  const esc = (s) => s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  const ocRe = new RegExp(
    `${esc(fields.containerName)}"?:\\s*\\{` +
    `${esc(fields.arrayName)}"?:\\s*\\[\\{` +
    `${esc(fields.objectName)}"?:\\s*\\{` +
    `[^}]*${esc(fields.keyField)}"?:\\s*"([^"]+)"` +
    `[^}]*${esc(fields.ivField)}"?:\\s*"([^"]+)"`
  );
  const ocMatch = html.match(ocRe);
  if (!ocMatch) throw new Error('Failed to extract obfuscated_crypto_data');
  const frag1_b64 = ocMatch[1];
  const iv_b64 = ocMatch[2];
  console.log(`\n[3] Extracted:`);
  console.log(`   frag1_b64: ${frag1_b64}`);
  console.log(`   iv_b64: ${iv_b64}`);

  // 5. Find tokenField + keyFrag2Field values in the payload
  const tokenRe = new RegExp(`"?${esc(fields.tokenField)}"?\\s*:\\s*"([^"]+)"`);
  const tokenMatch = html.match(tokenRe);
  if (!tokenMatch) throw new Error(`tokenField ${fields.tokenField} not found`);
  const tokenValue = tokenMatch[1];
  console.log(`\n[4] tokenField value: ${tokenValue}`);

  const kf2Re = new RegExp(`"?${esc(fields.keyFrag2Field)}"?\\s*:\\s*"([^"]+)"`);
  const kf2Match = html.match(kf2Re);
  if (!kf2Match) throw new Error(`keyFrag2Field ${fields.keyFrag2Field} not found`);
  const keyFrag2 = kf2Match[1];
  console.log(`[5] keyFrag2Field value: ${keyFrag2}`);

  // 6. Fetch /api/m3u8/<tokenValue>
  console.log(`\n[6] Fetching /api/m3u8/${tokenValue}`);
  const m3u8Resp = await fetchBuf(`https://flixcloud.cc/api/m3u8/${tokenValue}`, {
    headers: { 'Accept': 'application/json, text/plain, */*' },
  });
  if (m3u8Resp.status !== 200) {
    console.log(`   HTTP ${m3u8Resp.status}: ${m3u8Resp.body.toString('utf8').slice(0, 200)}`);
    throw new Error(`m3u8 token fetch failed: ${m3u8Resp.status}`);
  }
  const m3u8Json = JSON.parse(m3u8Resp.body.toString('utf8'));
  console.log(`   Token response keys: ${Object.keys(m3u8Json).join(', ')}`);

  // 7. Find vid and key in the response
  const vidKey = sha256Hex(tokenValue + 'vid').substring(0, 10);
  const keyKey = sha256Hex(tokenValue + 'key').substring(0, 10);
  console.log(`   Looking for vidKey=${vidKey}, keyKey=${keyKey}`);
  const vidB64 = m3u8Json[vidKey];
  const keyB64 = m3u8Json[keyKey];
  if (!vidB64 || !keyB64) throw new Error('Missing vid or key in m3u8 response');
  console.log(`   vid_b64: ${vidB64}`);
  console.log(`   key_b64: ${keyB64}`);

  // 8. Compute the 32-byte AES key via WASM + PBKDF2 + XOR + SHA-256
  const v = parseInt(seed.substring(0, 8), 16);
  console.log(`\n[7] v (seed[0:8] as int): ${v}`);

  const wasmBytes = b64decode(wPayloadB64);
  const mod = await WebAssembly.compile(wasmBytes);
  const inst = await WebAssembly.instantiate(mod, {});
  const ex = inst.exports;
  console.log(`[8] WASM exports: ${Object.keys(ex).join(', ')}`);

  // kn(t, e, a, o):
  //   k = t.length; I = 1000; P = I+k; U = P+k; nt = U+k;
  //   S.set(t, I); S.set(e, P); S.set(a, U);
  //   L._s(o); L._r(I, P, U, nt, k);
  //   return S.subarray(nt, nt+k);
  const t = b64decode(frag1_b64);
  const eBuf = b64decode(keyFrag2);
  const aBuf = b64decode(keyB64);
  const k = t.length;
  const I = 1000, P = I + k, U = P + k, nt = U + k;
  const needed = nt + k;
  if (ex.memory.buffer.byteLength < needed) {
    const pagesNeeded = Math.ceil((needed - ex.memory.buffer.byteLength) / 65536);
    ex.memory.grow(pagesNeeded);
  }
  const S = new Uint8Array(ex.memory.buffer);
  S.set(t, I);
  S.set(eBuf, P);
  S.set(aBuf, U);
  ex._s(v);
  ex._r(I, P, U, nt, k);
  const O = Buffer.from(S.subarray(nt, nt + k));
  console.log(`   O (WASM-derived): ${O.toString('hex')}`);

  // PBKDF2: deriveBits with PBKDF2, salt=seed, iterations=1000, hash=SHA-256, length=256
  const W = crypto.pbkdf2Sync(O, Buffer.from(seed, 'utf8'), 1000, 32, 'sha256');
  const tt = Buffer.from(W);
  // XOR with seed bytes
  const seedBytes = Buffer.from(seed, 'utf8');
  for (let i = 0; i < 32; i++) tt[i] ^= seedBytes[i % seedBytes.length];
  // SHA-256 → final AES key
  const Nt = crypto.createHash('sha256').update(tt).digest();
  console.log(`   Final AES key: ${Nt.toString('hex')}`);

  // 9. AES-CBC decrypt: key=Nt, iv=iv_b64, ciphertext=vid_b64
  const iv = b64decode(iv_b64);
  const ct = b64decode(vidB64);
  const decipher = crypto.createDecipheriv('aes-256-cbc', Nt, iv);
  let decrypted = decipher.update(ct);
  decrypted = Buffer.concat([decrypted, decipher.final()]);
  const m3u8Url = decrypted.toString('utf8');
  console.log(`\n[9] ✅ DECRYPTED M3U8 URL:`);
  console.log(`   ${m3u8Url}`);

  // 10. Test the URL
  console.log(`\n[10] Testing m3u8 URL...`);
  try {
    const m3u8Resp = await fetchBuf(m3u8Url, { 
      headers: { 'Accept': 'application/vnd.apple.mpegurl, */*' },
      timeout: 15000
    });
    const body = m3u8Resp.body.toString('utf8');
    console.log(`   HTTP ${m3u8Resp.status}, Content-Type: ${m3u8Resp.headers['content-type']}`);
    if (body.startsWith('#EXTM3U')) {
      const variants = body.split('\n').filter(l => l.startsWith('#EXT-X-STREAM-INF'));
      console.log(`   ✅ Valid HLS — ${variants.length} variants:`);
      for (const v of variants) {
        const res = v.match(/RESOLUTION=(\d+x\d+)/);
        const br = v.match(/BANDWIDTH=(\d+)/);
        const codecs = v.match(/CODECS="([^"]+)"/);
        console.log(`      - ${res ? res[1] : '?'} @ ${br ? Math.round(parseInt(br[1])/1000) : '?'}kbps, codecs=${codecs ? codecs[1] : '?'}`);
      }
    }
  } catch (e) { console.log(`   ❌ ${e.message}`); }
}

async function main() {
  const accessId = process.argv[2] || '68050p0lh9wh';
  await resolveFlixcloudStream(accessId);
}

main().catch(e => { console.error('FATAL:', e.message); process.exit(1); });
