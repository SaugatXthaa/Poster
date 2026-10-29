// Re-test with WASM _c() call to get the per-session AES key for m3u8 content decryption
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
function match1(re, str) {
  const m = str.match(re);
  return m ? m[1] : null;
}

async function main() {
  const accessId = '68050p0lh9wh';
  console.log('[1] Fetching flixcloud.cc/e/' + accessId + '?v=2');
  const r = await fetchBuf(`https://flixcloud.cc/e/${accessId}?v=2`);
  if (r.status !== 200) throw new Error(`HTTP ${r.status}`);
  const html = r.body.toString('utf8');

  const seed = match1(/obfuscation_seed:"([^"]+)"/, html);
  const wPayloadB64 = match1(/w_payload:"([^"]+)"/, html);
  console.log('seed:', seed);

  // Derive field names
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

  // Extract obfuscated_crypto_data
  const esc = (s) => s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  const ocRe = new RegExp(
    `${esc(fields.containerName)}"?:\\s*\\{` +
    `${esc(fields.arrayName)}"?:\\s*\\[\\{` +
    `${esc(fields.objectName)}"?:\\s*\\{` +
    `[^}]*${esc(fields.keyField)}"?:\\s*"([^"]+)"` +
    `[^}]*${esc(fields.ivField)}"?:\\s*"([^"]+)"`
  );
  const ocMatch = html.match(ocRe);
  const frag1_b64 = ocMatch[1];
  const iv_b64 = ocMatch[2];

  // Find tokenField and keyFrag2Field values
  const tokenValue = html.match(new RegExp(`"?${esc(fields.tokenField)}"?\\s*:\\s*"([^"]+)"`))[1];
  const keyFrag2 = html.match(new RegExp(`"?${esc(fields.keyFrag2Field)}"?\\s*:\\s*"([^"]+)"`))[1];

  console.log('tokenValue:', tokenValue);

  // Fetch /api/m3u8/<tokenValue>
  const m3u8Resp = await fetchBuf(`https://flixcloud.cc/api/m3u8/${tokenValue}`, {
    headers: { 'Accept': 'application/json' },
  });
  if (m3u8Resp.status !== 200) throw new Error(`m3u8 token HTTP ${m3u8Resp.status}`);
  const m3u8Json = JSON.parse(m3u8Resp.body.toString('utf8'));

  const vidKey = sha256Hex(tokenValue + 'vid').substring(0, 10);
  const keyKey = sha256Hex(tokenValue + 'key').substring(0, 10);
  const vidB64 = m3u8Json[vidKey];
  const keyB64 = m3u8Json[keyKey];

  // WASM-derive the 32-byte key fragment O
  const v = parseInt(seed.substring(0, 8), 16);
  const wasmBytes = b64decode(wPayloadB64);
  const mod = await WebAssembly.compile(wasmBytes);
  const inst = await WebAssembly.instantiate(mod, {});
  const ex = inst.exports;
  console.log('WASM exports:', Object.keys(ex));

  const t = b64decode(frag1_b64);
  const eBuf = b64decode(keyFrag2);
  const aBuf = b64decode(keyB64);
  const k = t.length;
  const I = 1000, P = I + k, U = P + k, nt = U + k;
  if (ex.memory.buffer.byteLength < nt + k) {
    const pagesNeeded = Math.ceil((nt + k - ex.memory.buffer.byteLength) / 65536);
    ex.memory.grow(pagesNeeded);
  }
  const S = new Uint8Array(ex.memory.buffer);
  S.set(t, I);
  S.set(eBuf, P);
  S.set(aBuf, U);
  ex._s(v);
  ex._r(I, P, U, nt, k);
  const O = Buffer.from(S.subarray(nt, nt + k));

  // PBKDF2 + XOR + SHA-256
  const W = crypto.pbkdf2Sync(O, Buffer.from(seed, 'utf8'), 1000, 32, 'sha256');
  const tt = Buffer.from(W);
  const seedBytes = Buffer.from(seed, 'utf8');
  for (let i = 0; i < 32; i++) tt[i] ^= seedBytes[i % seedBytes.length];
  const Nt = crypto.createHash('sha256').update(tt).digest();

  // AES-CBC decrypt the m3u8 URL
  const iv = b64decode(iv_b64);
  const ct = b64decode(vidB64);
  const decipher = crypto.createDecipheriv('aes-256-cbc', Nt, iv);
  let decrypted = decipher.update(ct);
  decrypted = Buffer.concat([decrypted, decipher.final()]);
  const m3u8Url = decrypted.toString('utf8');
  console.log('\nDecrypted m3u8 URL:', m3u8Url);

  // Call WASM _c() to get the per-session 32-byte key (window.__pk in browser)
  // The JS does: const s=Zt.exports,d=s._c(),i=new Uint8Array(s.memory.buffer).slice(d,d+32);window.__pk=btoa(...)
  // Zt is the same WASM instance we already have. _c() returns a pointer (or offset) to 32 bytes in WASM memory.
  // But _c() likely returns a number (offset) — let me try.
  if (typeof ex._c === 'function') {
    const cPtr = ex._c();
    console.log('\n_c() returned:', cPtr, typeof cPtr);
    if (typeof cPtr === 'number') {
      const pk = Buffer.from(new Uint8Array(ex.memory.buffer).slice(cPtr, cPtr + 32));
      console.log('window.__pk (32 bytes hex):', pk.toString('hex'));
      console.log('window.__pk (base64):', pk.toString('base64'));

      // Now fetch the m3u8 URL contents (encrypted)
      const m3u8ContentResp = await fetchBuf(m3u8Url, {
        headers: { 'Referer': 'https://flixcloud.cc/' },
      });
      const m3u8Content = m3u8ContentResp.body;
      console.log('\n[m3u8 content] HTTP', m3u8ContentResp.status, 'length:', m3u8Content.length);
      const m3u8Text = m3u8Content.toString('utf8');
      console.log('First 200 chars:', m3u8Text.slice(0, 200));

      // Try base64-decoding the m3u8 content and decrypting with pk as key
      let ciphertext;
      try {
        ciphertext = Buffer.from(m3u8Text, 'base64');
        console.log('\nBase64-decoded m3u8:', ciphertext.length, 'bytes');
      } catch (e) {
        console.log('Not base64');
        ciphertext = m3u8Content;
      }

      // Try AES-256-CBC with key=pk, iv=first 16 bytes of ciphertext
      if (ciphertext.length > 16 && ciphertext.length % 16 === 0) {
        try {
          const iv2 = ciphertext.slice(0, 16);
          const ct2 = ciphertext.slice(16);
          const dec2 = crypto.createDecipheriv('aes-256-cbc', pk, iv2);
          let d2 = dec2.update(ct2);
          d2 = Buffer.concat([d2, dec2.final()]);
          console.log('\nAES-256-CBC decrypt (key=pk, iv=ct[:16]):', d2.toString('utf8').slice(0, 500));
        } catch (e) { console.log('AES-256-CBC failed:', e.message); }
      }

      // Try AES-256-CTR with key=pk, iv=first 16 bytes
      if (ciphertext.length > 16) {
        try {
          const iv3 = ciphertext.slice(0, 16);
          const ct3 = ciphertext.slice(16);
          const dec3 = crypto.createDecipheriv('aes-256-ctr', pk, iv3);
          let d3 = dec3.update(ct3);
          d3 = Buffer.concat([d3, dec3.final()]);
          console.log('\nAES-256-CTR decrypt (key=pk, iv=ct[:16]):', d3.toString('utf8').slice(0, 500));
        } catch (e) { console.log('AES-256-CTR failed:', e.message); }
      }

      // Try AES-128-CBC with key=pk[:16], iv=pk[16:32]
      try {
        const dec4 = crypto.createDecipheriv('aes-128-cbc', pk.slice(0, 16), pk.slice(16, 32));
        let d4 = dec4.update(ciphertext);
        d4 = Buffer.concat([d4, dec4.final()]);
        console.log('\nAES-128-CBC decrypt (key=pk[:16], iv=pk[16:32]):', d4.toString('utf8').slice(0, 500));
      } catch (e) { console.log('AES-128-CBC failed:', e.message); }

      // Try XOR with pk (repeating)
      const xored = Buffer.alloc(ciphertext.length);
      for (let i = 0; i < ciphertext.length; i++) {
        xored[i] = ciphertext[i] ^ pk[i % pk.length];
      }
      console.log('\nXOR with pk:', xored.toString('utf8').slice(0, 500));
    }
  } else {
    console.log('WASM has no _c() export');
  }
}

main().catch(e => { console.error(e); process.exit(1); });
