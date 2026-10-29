// Test the full reanime.to → flixcloud.cc decryption chain.
// Steps:
//   1. Fetch flixcloud.cc/e/<accessId>?v=2 to get HTML with embedded payload.
//   2. Extract `obfuscation_seed`, `w_payload`, `obfuscated_crypto_data` from payload.
//   3. Derive the field-name map via the SHA-256 chain (xn() in flixcloud JS).
//   4. Extract frag1_b64 (keyField), iv_b64 (ivField) from obfuscated_crypto_data.
//   5. Find tokenField value (access_id reference) in the payload.
//   6. Find keyFrag2Field value in the payload.
//   7. GET /api/m3u8/<tokenFieldValue>  → returns {<sha(tokenFieldValue+"vid")[0:10]>: vid_b64, <sha(tokenFieldValue+"key")[0:10]>: key_b64}
//   8. Combine: frag1 ⊕ keyFrag2 ⊕ tokenKey → 32-byte AES key (PBKDF2 with seed as salt, 1000 iters, SHA-256, then XOR with seed bytes, then SHA-256 of result).
//   9. AES-CBC decrypt the tokenVid value with iv_b64 → plaintext m3u8 URL.
const https = require('https');
const crypto = require('crypto');

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';

function fetchBuf(url, { headers = {}, timeout = 30000 } = {}) {
  return new Promise((resolve, reject) => {
    const u = new URL(url);
    const req = https.request({
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

function sha256Hex(s) {
  return crypto.createHash('sha256').update(s, 'utf8').digest('hex');
}

async function xn(seed) {
  // JS: let e = t; for o in 0..3 (3 iters): e = Qt(e + o.toString())
  let e = seed;
  for (let o = 0; o < 3; o++) e = sha256Hex(e + o.toString());
  // Then a = e; for o in 0..3: a = Qt(a + o.toString())
  let a = e;
  for (let o = 0; o < 3; o++) a = sha256Hex(a + o.toString());
  return {
    videoField: `vf_${e.substring(0, 8)}`,
    keyField: `kf_${e.substring(8, 16)}`,
    ivField: `ivf_${e.substring(16, 24)}`,
    containerName: `cd_${e.substring(24, 32)}`,
    arrayName: `ad_${e.substring(32, 40)}`,
    objectName: `od_${e.substring(40, 48)}`,
    tokenField: `${e.substring(48, 64)}_${e.substring(56, 64)}`,
    keyFrag2Field: `${a.substring(0, 16)}_${a.substring(16, 24)}`,
  };
}

function b64decode(s) { return Buffer.from(s, 'base64'); }

async function main() {
  const accessId = process.argv[2] || '68050p0lh9wh';
  console.log(`[1] Fetching flixcloud.cc/e/${accessId}?v=2`);
  const r = await fetchBuf(`https://flixcloud.cc/e/${accessId}?v=2`, {
    headers: {
      'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
      'Accept-Language': 'en-US,en;q=0.9',
      'Referer': 'https://reanime.to/',
      'Origin': 'https://reanime.to',
    },
  });
  if (r.status !== 200) { console.error(`HTTP ${r.status}`); process.exit(1); }
  const html = r.body.toString('utf8');
  console.log(`   HTML length: ${html.length}`);

  // Extract the data: [null,null,{type:"data",data:{...}}] block
  const m = html.match(/\{type:"data",data:(\{.+?\})\}\}/s);
  if (!m) { console.error('data block not found'); process.exit(1); }
  const dataStr = m.group(1);
  console.log(`   data block: ${dataStr.length} chars`);

  // Find obfuscation_seed
  const seedMatch = dataStr.match(/obfuscation_seed"\s*:\s*"([^"]+)"/);
  if (!seedMatch) { console.error('obfuscation_seed not found'); process.exit(1); }
  const seed = seedMatch.group(1);
  console.log(`   obfuscation_seed: ${seed}`);

  // Find w_payload (WASM decryptor, base64)
  const wMatch = dataStr.match(/w_payload"\s*:\s*"([^"]+)"/);
  if (wMatch) console.log(`   w_payload: ${wMatch.group(1).length} chars (base64 WASM)`);
  const wPayloadB64 = wMatch.group(1);

  // Compute field map
  const fields = await xn(seed);
  console.log(`\n[2] Field names:`);
  for (const [k, v] of Object.entries(fields)) console.log(`   ${k}: ${v}`);

  // Extract obfuscated_crypto_data[containerName][arrayName][0][objectName] → {keyField, ivField}
  // Pattern: <containerName>:{<arrayName>:[{<objectName>:{<keyField>:"...",<ivField>:"..."}}]}
  const ocMatch = dataStr.match(new RegExp(
    `${fields.containerName.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}"\\s*:\\s*\\{` +
    `${fields.arrayName.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}"\\s*:\\s*\\[\\{` +
    `${fields.objectName.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}"\\s*:\\s*\\{` +
    `[^}]*${fields.keyField.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}"\\s*:\\s*"([^"]+)"` +
    `[^}]*${fields.ivField.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}"\\s*:\\s*"([^"]+)"`
  ));
  if (!ocMatch) { console.error('Failed to extract obfuscated_crypto_data'); process.exit(1); }
  const frag1_b64 = ocMatch.group(1);
  const iv_b64 = ocMatch.group(2);
  console.log(`\n[3] Extracted from obfuscated_crypto_data:`);
  console.log(`   frag1_b64 (${fields.keyField}): ${frag1_b64}`);
  console.log(`   iv_b64 (${fields.ivField}): ${iv_b64}`);

  // Find tokenField value in payload (the access_id reference)
  const tokenRe = new RegExp(`"${fields.tokenField.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}"\\s*:\\s*"([^"]+)"`);
  const tokenMatch = dataStr.match(tokenRe);
  if (!tokenMatch) { console.error(`tokenField ${fields.tokenField} not found`); process.exit(1); }
  const tokenValue = tokenMatch.group(1);
  console.log(`\n[4] tokenField value: ${tokenValue}`);

  // Find keyFrag2Field value
  const kf2Re = new RegExp(`"?${fields.keyFrag2Field.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}"?\\s*:\\s*"([^"]+)"`);
  const kf2Match = dataStr.match(kf2Re);
  if (!kf2Match) { console.error(`keyFrag2Field ${fields.keyFrag2Field} not found`); process.exit(1); }
  const keyFrag2 = kf2Match.group(1);
  console.log(`[5] keyFrag2Field value: ${keyFrag2}`);

  // Fetch /api/m3u8/<tokenValue>
  console.log(`\n[6] Fetching /api/m3u8/${tokenValue}`);
  const m3u8Resp = await fetchBuf(`https://flixcloud.cc/api/m3u8/${tokenValue}`, {
    headers: {
      'Accept': 'application/json, text/plain, */*',
      'Referer': `https://flixcloud.cc/e/${accessId}?v=2`,
      'Origin': 'https://flixcloud.cc',
    },
  });
  if (m3u8Resp.status !== 200) { console.error(`HTTP ${m3u8Resp.status}`); process.exit(1); }
  const m3u8Json = JSON.parse(m3u8Resp.body.toString('utf8'));
  console.log(`   Token response keys: ${Object.keys(m3u8Json).join(', ')}`);

  // Find vidKey and keyKey (substrings 0:10 of sha256(tokenValue+"vid") / sha256(tokenValue+"key"))
  const vidKey = sha256Hex(tokenValue + 'vid').substring(0, 10);
  const keyKey = sha256Hex(tokenValue + 'key').substring(0, 10);
  console.log(`   vidKey (sha[0:10] of token+"vid"): ${vidKey}`);
  console.log(`   keyKey (sha[0:10] of token+"key"): ${keyKey}`);

  const vidB64 = m3u8Json[vidKey];
  const keyB64 = m3u8Json[keyKey];
  console.log(`   vid_b64: ${vidB64}`);
  console.log(`   key_b64: ${keyB64}`);

  if (!vidB64 || !keyB64) { console.error('Missing vid or key in response'); process.exit(1); }

  // Compute the 32-byte AES key via PBKDF2 + XOR + SHA-256
  // JS: 
  //   v = parseInt(e.substring(0,8), 16)   // first 8 hex chars of seed → integer
  //   O = await kn(V, l, u, v)            // WASM-derives 32 bytes from (frag1, keyFrag2, keyB64, v)
  //   q = await crypto.subtle.importKey("raw", O, {name:"PBKDF2"}, false, ["deriveBits"])
  //   W = await crypto.subtle.deriveBits({name:"PBKDF2", salt: TextEncoder.encode(seed), iterations: 1000, hash:"SHA-256"}, q, 256)
  //   tt = new Uint8Array(W)
  //   for i in 0..32: tt[i] ^= seed.charCodeAt(i % seed.length)
  //   Nt = await crypto.subtle.digest("SHA-256", tt)  // final 32-byte AES key
  //   M = await crypto.subtle.importKey("raw", Nt, {name:"AES-CBC"}, false, ["decrypt"])
  //   i = await crypto.subtle.decrypt({name:"AES-CBC", iv: It(iv_b64)}, M, It(vid_b64))
  //   R = TextDecoder().decode(i)  → final m3u8 URL

  const v = parseInt(seed.substring(0, 8), 16);
  console.log(`\n[7] v (first 8 hex of seed as int): ${v}`);

  // WASM-derive the 32-byte key fragment O
  console.log(`[8] Instantiating WASM...`);
  const wasmBytes = b64decode(wPayloadB64);
  const mod = await WebAssembly.compile(wasmBytes);
  const inst = await WebAssembly.instantiate(mod, {});
  const ex = inst.exports;
  console.log(`   WASM exports: ${Object.keys(ex).join(', ')}`);
  // kn(t, e, a, o):
  //   const m = data.w_payload; const g = It(m); Zt = (await WebAssembly.instantiate(g, {})).instance;
  //   const L = Zt.exports, b = L.memory; b.buffer.byteLength===0 && b.grow(1);
  //   const S = new Uint8Array(b.buffer);
  //   const k = t.length; const I = 1000; const P = I+k; const U = P+k; const nt = U+k;
  //   S.set(t, I); S.set(e, P); S.set(a, U);
  //   L._s(o);  // set v as the int parameter
  //   L._r(I, P, U, nt, k);  // run with positions
  //   const V = new Uint8Array(k); V.set(S.subarray(nt, nt+k));
  //   return V;
  const t = b64decode(frag1_b64);
  const e = b64decode(keyFrag2);
  const a = b64decode(keyB64);
  const k = t.length;
  const I = 1000, P = I + k, U = P + k, nt = U + k;
  // grow memory if needed
  const needed = nt + k;
  const current = ex.memory.buffer.byteLength;
  if (current < needed) {
    const pagesNeeded = Math.ceil((needed - current) / 65536);
    ex.memory.grow(pagesNeeded);
  }
  const S = new Uint8Array(ex.memory.buffer);
  S.set(t, I);
  S.set(e, P);
  S.set(a, U);
  ex._s(v);
  ex._r(I, P, U, nt, k);
  const O = new Uint8Array(k);
  O.set(S.subarray(nt, nt + k));
  console.log(`   O (32-byte derived): ${O.length} bytes`);

  // PBKDF2: deriveBits with PBKDF2, salt=seed, iterations=1000, hash=SHA-256, length=256
  const W = crypto.pbkdf2Sync(O, Buffer.from(seed, 'utf8'), 1000, 32, 'sha256');
  const tt = Buffer.from(W);
  // XOR with seed bytes
  const seedBytes = Buffer.from(seed, 'utf8');
  for (let i = 0; i < 32; i++) tt[i] ^= seedBytes[i % seedBytes.length];
  // SHA-256 → final AES key
  const Nt = crypto.createHash('sha256').update(tt).digest();
  console.log(`   Final AES key: ${Nt.toString('hex')}`);

  // AES-CBC decrypt: key=Nt, iv=iv_b64, ciphertext=vid_b64
  const iv = b64decode(iv_b64);
  const ct = b64decode(vidB64);
  const decipher = crypto.createDecipheriv('aes-256-cbc', Nt, iv);
  let decrypted = decipher.update(ct);
  decrypted = Buffer.concat([decrypted, decipher.final()]);
  const m3u8Url = decrypted.toString('utf8');
  console.log(`\n[9] ✅ DECRYPTED M3U8 URL:`);
  console.log(`   ${m3u8Url}`);

  // Test the URL
  console.log(`\n[10] Testing m3u8 URL...`);
  try {
    const m3u8Resp = await fetchBuf(m3u8Url, { headers: { 'Accept': 'application/vnd.apple.mpegurl, */*' }, timeout: 15000 });
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

main().catch(e => { console.error(e); process.exit(1); });
