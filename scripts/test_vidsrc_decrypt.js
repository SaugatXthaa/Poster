// Test: decrypt vidsrc.io stream_urls using the WASM module
// Algorithm (from vsdec.js):
//   1. base64-decode stream_urls → enc (bytes)
//   2. ptr = wasm.alloc(enc.length)
//   3. memory[ptr:ptr+len] = enc
//   4. outLen = wasm.decrypt(ptr, enc.length)
//   5. Result: memory[ptr + 12 : ptr + 12 + outLen] as UTF-8 string
//   6. Split by '\n' to get URL array

'use strict';
const fs = require('fs');
const https = require('https');

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';

function fetchBuf(url, referer) {
  return new Promise((resolve, reject) => {
    const headers = { 'User-Agent': UA, 'Accept': '*/*' };
    if (referer) headers['Referer'] = referer;
    https.get(url, { headers, rejectUnauthorized: false }, (res) => {
      if (res.statusCode !== 200) {
        res.resume();
        return reject(new Error(`HTTP ${res.statusCode} for ${url}`));
      }
      const chunks = [];
      res.on('data', c => chunks.push(c));
      res.on('end', () => resolve(Buffer.concat(chunks)));
    }).on('error', reject);
  });
}

async function fetchJson(url, referer) {
  const buf = await fetchBuf(url, referer);
  return JSON.parse(buf.toString('utf8'));
}

(async () => {
  try {
    const tmdbId = process.argv[2] || '693134';
    const type = process.argv[3] || 'movie';
    const season = process.argv[4] || null;
    const episode = process.argv[5] || null;

    // Step 1: Get /vs_src.php → cloudorchestranova.com URL with vs token
    const vsSrcUrl = `https://vidsrc.io/vs_src.php?type=${type}&id=${tmdbId}` +
                     (season ? `&season=${season}&episode=${episode}` : '');
    console.log('Step 1: Fetching /vs_src.php:', vsSrcUrl);
    const vsSrc = await fetchJson(vsSrcUrl);
    console.log('  → cloudorchestranova URL:', vsSrc.src.slice(0, 80) + '...');

    // Step 2: Fetch cloudorchestranova.com embed → player URL (extract from window.CFG)
    console.log('Step 2: Fetching cloudorchestranova embed...');
    const embedBuf = await fetchBuf(vsSrc.src, 'https://vidsrc.io/');
    const embedHtml = embedBuf.toString('utf8');

    // First check: window.CONFIG (player HTML) — direct config with stream API
    let config = null;
    let playerUrl = null;
    const configMatch = embedHtml.match(/window\.CONFIG\s*=\s*({[^<]+})/);
    if (configMatch) {
      config = JSON.parse(configMatch[1]);
      console.log('  → Found window.CONFIG (player HTML)');
    } else {
      // Look for window.CFG (embed HTML) — contains playerUrl
      const cfgMatch = embedHtml.match(/window\.CFG\s*=\s*({[^<]+})/);
      if (cfgMatch) {
        const cfg = JSON.parse(cfgMatch[1]);
        playerUrl = cfg.playerUrl;
        console.log('  → Found window.CFG (embed HTML), playerUrl:', playerUrl);
      }
    }

    // If we got a playerUrl instead of a direct api URL, fetch the player HTML
    if (!config && playerUrl) {
      const playerFullUrl = playerUrl.startsWith('http') ? playerUrl : `https://cloudorchestranova.com${playerUrl}`;
      console.log('Step 2b: Fetching player HTML:', playerFullUrl);
      const playerBuf = await fetchBuf(playerFullUrl, 'https://cloudorchestranova.com/');
      const playerHtml = playerBuf.toString('utf8');
      const m = playerHtml.match(/window\.CONFIG\s*=\s*({[^<]+})/);
      if (m) {
        config = JSON.parse(m[1]);
        console.log('  → Found window.CONFIG in player HTML');
      }
    }

    if (!config || !config.api) throw new Error('Cannot find window.CONFIG.api in player HTML');
    console.log('  → Stream API:', config.api);

    // Step 3: Fetch the stream API (returns stream_urls + vs.wasm_url)
    console.log('Step 3: Fetching stream API:', config.api);
    const streamData = await fetchJson(config.api, 'https://cloudorchestranova.com/');
    console.log('  → stream_urls length:', streamData.data.stream_urls.length, 'chars');
    console.log('  → wasm_url:', streamData.vs.wasm_url);

    // Step 4: Fetch the WASM module
    console.log('Step 4: Fetching WASM:', streamData.vs.wasm_url);
    const wasmBuf = await fetchBuf(streamData.vs.wasm_url, 'https://cloudorchestranova.com/');
    console.log('  → WASM size:', wasmBuf.length, 'bytes');

    // Step 5: Instantiate the WASM module
    console.log('Step 5: Instantiating WASM...');
    const wasmModule = await WebAssembly.compile(wasmBuf);
    const instance = await WebAssembly.instantiate(wasmModule, {});
    const ex = instance.exports;
    console.log('  → exports:', Object.keys(ex));

    // Step 6: Decrypt the stream_urls
    console.log('Step 6: Decrypting stream_urls...');
    const encB64 = streamData.data.stream_urls;
    const enc = Buffer.from(encB64, 'base64');
    console.log('  → Encrypted bytes:', enc.length);

    const ptr = ex.alloc(enc.length);
    console.log('  → Allocated at ptr:', ptr);
    new Uint8Array(ex.memory.buffer, ptr, enc.length).set(enc);

    const outLen = ex.decrypt(ptr, enc.length);
    console.log('  → Output length:', outLen);

    const result = Buffer.from(ex.memory.buffer, ptr + 12, outLen).toString('utf8');
    console.log('  → Decrypted result:');
    console.log(result);

    const urls = result.split('\n').filter(s => s.trim());
    console.log('\n=== Final stream URLs ===');
    urls.forEach((u, i) => console.log(`  ${i+1}. ${u}`));
    console.log(`\nTotal: ${urls.length} streams`);
  } catch (e) {
    console.error('ERROR:', e.message);
    console.error(e.stack);
    process.exit(1);
  }
})();
