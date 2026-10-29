// CineJoy Direct API Client — Pure Node.js using crush.wasm
// =================================================================
// This implements the new cinejoy.to Noise protocol using the official
// crush.wasm module from api.shegu.st/crush.wasm.
//
// Flow:
//   1. Download crush.wasm from https://api.shegu.st/crush.wasm (cached)
//   2. JSON-encode {path: "/<server>/<movie|tv>?tmdb=...", payload: null}
//   3. Generate 44-byte random nonce
//   4. Call WASM seal_request(input, nonce) → encrypted output
//   5. POST the encrypted body to https://api.shegu.st/g
//   6. Decrypt the response using AES-GCM with:
//      - Key: responseKey (first 32 bytes of seal_request output)
//      - IV: first 12 bytes of response
//      - AAD: ephemeralPublic (65 bytes) + keyId (1 byte)
//      - TagLength: 16 bytes
//   7. Response is JSON: {stream: [{type: 'hls', playlist: 'https://...'}]}
//
// No Playwright, no FlareSolverr — pure Node.js + WebAssembly.

'use strict';

const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

const SHEGU_API = 'https://api.shegu.st';
const CRUSH_WASM_URL = 'https://api.shegu.st/crush.wasm';
const CRUSH_WASM_CACHE = path.join(__dirname, 'crush.wasm');
const TMDB_API_KEY = '8476a7ab80ad76f0936744df0430e67c';
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';

// Constants from cinejoy.to JS bundle
const K = 32;          // responseKey length
const $ = 1;           // keyId length
const S0 = 65;         // ephemeralPublic length (includes 0x04 prefix)
const T = K + $ + S0;  // total header length = 98
const B = 12;          // AES-GCM IV length
const W0 = 16;         // AES-GCM tag length

let _wasmInstance = null;

async function loadWasm() {
  if (_wasmInstance) return _wasmInstance;

  // Ensure crush.wasm is downloaded
  if (!fs.existsSync(CRUSH_WASM_CACHE)) {
    console.log('[CineJoy-Direct] Downloading crush.wasm...');
    const res = await fetch(CRUSH_WASM_URL, {
      headers: { 'User-Agent': UA, 'Referer': 'https://cinejoy.to/' },
    });
    if (!res.ok) throw new Error(`Failed to download crush.wasm: HTTP ${res.status}`);
    const buf = Buffer.from(await res.arrayBuffer());
    fs.writeFileSync(CRUSH_WASM_CACHE, buf);
    console.log('[CineJoy-Direct] crush.wasm cached (' + buf.length + ' bytes)');
  }

  const wasmBuf = fs.readFileSync(CRUSH_WASM_CACHE);
  const mod = await WebAssembly.compile(wasmBuf);
  const inst = await WebAssembly.instantiate(mod, {});
  _wasmInstance = inst;
  return inst;
}

// Call crush.wasm's seal_request to encrypt the request payload.
// Returns: { responseKey, keyId, ephemeralPublic, body }
async function sealRequest(payloadJson) {
  const inst = await loadWasm();
  const exports = inst.exports;
  const memory = exports.memory;

  const f = Buffer.from(payloadJson, 'utf8');
  const r = crypto.randomBytes(44);  // 44-byte nonce (u0=44)
  const outputSize = f.length + 512;

  // Allocate memory in WASM
  const a = exports.alloc(f.length, 1);
  const k = exports.alloc(r.length, 1);
  const b = exports.alloc(outputSize, 1);

  // Copy f and r into WASM memory
  new Uint8Array(memory.buffer, a, f.length).set(f);
  new Uint8Array(memory.buffer, k, r.length).set(r);

  // Call seal_request(input_ptr, input_len, nonce_ptr, nonce_len, output_ptr, output_size)
  const n = exports.seal_request(a, f.length, k, r.length, b, outputSize);

  exports.dealloc(a);
  exports.dealloc(k);

  if (n <= 0) {
    exports.dealloc(b);
    throw new Error(`seal_request failed (returned ${n})`);
  }

  // Read the output
  const output = new Uint8Array(memory.buffer, b, n);
  const result = {
    responseKey: Buffer.from(output.slice(0, K)),
    keyId: output[K],
    ephemeralPublic: Buffer.from(output.slice(K + $, T)),
    body: Buffer.from(output.slice(T)),
  };

  exports.dealloc(b);
  return result;
}

// Build the AAD for AES-GCM response decryption.
// AAD structure (captured from live cinejoy.to browser):
//   [0:13]   "lumen-gate-v2" (13 bytes ASCII - the protocol name)
//   [13]     0x00
//   [14]     0x02
//   [15]     keyId (1 byte)
//   [16:81]  ephemeralPublic (65 bytes, includes 0x04 prefix)
// Total: 81 bytes
function buildAad(ephemeralPublic, keyId) {
  const prefix = Buffer.from('lumen-gate-v2', 'utf8');  // 13 bytes
  return Buffer.concat([
    prefix,                           // 13 bytes
    Buffer.from([0x00, 0x02]),       // 2 bytes
    Buffer.from([keyId]),            // 1 byte
    ephemeralPublic,                 // 65 bytes
  ]);                                // Total: 81 bytes
}

// Decrypt the response from POST /g using AES-GCM.
async function decryptResponse(responseBuf, sealed) {
  // From JS R0 function:
  //   d = await j().importKey("raw", N(_.responseKey), "AES-GCM", false, ["decrypt"])
  //   t = await j().decrypt({name:"AES-GCM", iv:N(o.slice(0,B=12)),
  //                          additionalData: N(O0(_.ephemeralPublic, _.keyId)),
  //                          tagLength: W0=16*8=128},
  //                         d, N(o.slice(B=12)))
  //   f = JSON.parse(C0.decode(t))

  const key = await crypto.webcrypto.subtle.importKey(
    'raw',
    sealed.responseKey,
    { name: 'AES-GCM' },
    false,
    ['decrypt']
  );

  const iv = responseBuf.slice(0, B);
  const ciphertextWithTag = responseBuf.slice(B);
  const aad = buildAad(sealed.ephemeralPublic, sealed.keyId);

  try {
    const decrypted = await crypto.webcrypto.subtle.decrypt(
      {
        name: 'AES-GCM',
        iv: iv,
        additionalData: aad,
        tagLength: W0 * 8,  // 128 bits
      },
      key,
      ciphertextWithTag
    );
    return JSON.parse(Buffer.from(decrypted).toString('utf8'));
  } catch (e) {
    // Try without AAD (in case our AAD construction is wrong)
    console.log('[CineJoy-Direct] AES-GCM with AAD failed, trying without AAD...');
    const decrypted2 = await crypto.webcrypto.subtle.decrypt(
      { name: 'AES-GCM', iv: iv, tagLength: W0 * 8 },
      key,
      ciphertextWithTag
    );
    return JSON.parse(Buffer.from(decrypted2).toString('utf8'));
  }
}

// Get TMDB info for the request payload
async function getTmdbInfo(tmdbId, type) {
  const endpoint = type === 'tv' ? 'tv' : 'movie';
  const url = `https://api.themoviedb.org/3/${endpoint}/${tmdbId}?api_key=${TMDB_API_KEY}&append_to_response=external_ids`;
  const res = await fetch(url, { headers: { 'User-Agent': UA } });
  if (!res.ok) return null;
  const d = await res.json();
  return {
    title: type === 'tv' ? d.name : d.title,
    year: parseInt((d.first_air_date || d.release_date || '').slice(0, 4)) || null,
    imdbId: d.external_ids?.imdb_id || d.imdb_id || null,
  };
}

// Build the path string for the request payload
function buildPath(server, tmdbId, type, season, episode, tmdbInfo) {
  const params = new URLSearchParams();
  if (tmdbInfo?.imdbId) params.set('imdb', tmdbInfo.imdbId);
  if (tmdbInfo?.title) params.set('title', tmdbInfo.title);
  params.set('tmdb', String(tmdbId));
  if (tmdbInfo?.year) params.set('year', String(tmdbInfo.year));
  if (type === 'tv' && season != null && episode != null) {
    params.set('season', String(season));
    params.set('episode', String(episode));
  }
  // Sort params alphabetically (matching browser behavior)
  const sortedParams = [...params.entries()].sort(([a], [b]) => a.localeCompare(b));
  const queryString = sortedParams
    .map(([k, v]) => encodeURIComponent(k) + '=' + encodeURIComponent(v))
    .join('&');
  return `/${server}/${type === 'tv' ? 'tv' : 'movie'}?${queryString}`;
}

// Get the list of available servers
async function listServers() {
  const res = await fetch(`${SHEGU_API}/servers`, {
    headers: { 'User-Agent': UA, 'Referer': 'https://cinejoy.to/' },
  });
  if (!res.ok) throw new Error(`Failed to list servers: HTTP ${res.status}`);
  const data = await res.json();
  return data.servers || [];
}

// Main: get stream playlist URL for a movie/TV episode on a specific server
async function getStreamUrl(server, tmdbId, type, season, episode, tmdbInfo) {
  // Build the path
  const path = buildPath(server, tmdbId, type, season, episode, tmdbInfo);

  // Build the request payload
  const payloadJson = JSON.stringify({ path, payload: null });

  // Encrypt the request using crush.wasm
  console.log(`[CineJoy-Direct] Sealing request for ${server} (path: ${path.slice(0, 80)}...)`);
  const sealed = await sealRequest(payloadJson);
  console.log(`[CineJoy-Direct] Sealed ${sealed.body.length} bytes (keyId=${sealed.keyId})`);

  // POST the encrypted body to /g
  console.log(`[CineJoy-Direct] POST ${SHEGU_API}/g`);
  const res = await fetch(`${SHEGU_API}/g`, {
    method: 'POST',
    headers: {
      'Content-Type': 'text/plain;charset=UTF-8',
      'User-Agent': UA,
      'Origin': 'https://cinejoy.to',
      'Referer': 'https://cinejoy.to/',
    },
    body: sealed.body,
  });

  if (!res.ok) {
    throw new Error(`POST /g failed: HTTP ${res.status}`);
  }

  const responseBuf = Buffer.from(await res.arrayBuffer());
  console.log(`[CineJoy-Direct] Got ${responseBuf.length} bytes encrypted response`);

  // Decrypt the response using AES-GCM
  const decrypted = await decryptResponse(responseBuf, sealed);
  console.log(`[CineJoy-Direct] Decrypted:`, JSON.stringify(decrypted).slice(0, 200));
  return decrypted;
}

module.exports = {
  loadWasm,
  sealRequest,
  decryptResponse,
  buildPath,
  listServers,
  getStreamUrl,
  getTmdbInfo,
};

// CLI test
if (require.main === module) {
  (async () => {
    const args = process.argv.slice(2);
    const server = args[0] || 'Lisbon';
    const tmdbId = args[1] || '27205';
    const type = args[2] || 'movie';
    const season = args[3] ? parseInt(args[3], 10) : null;
    const episode = args[4] ? parseInt(args[4], 10) : null;

    console.log(`=== CineJoy Direct API Client ===`);
    console.log(`Server: ${server}, TMDB: ${tmdbId}, Type: ${type}${season ? ` S${season}E${episode}` : ''}`);

    const tmdbInfo = await getTmdbInfo(tmdbId, type);
    console.log('TMDB info:', tmdbInfo);

    try {
      const result = await getStreamUrl(server, tmdbId, type, season, episode, tmdbInfo);
      console.log('\n=== RESULT ===');
      console.log(JSON.stringify(result, null, 2));
    } catch (e) {
      console.error('Error:', e.message);
      if (process.env.DEBUG) console.error(e.stack);
    }
  })();
}
