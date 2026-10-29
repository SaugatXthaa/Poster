// Build script: generates cinejoy_all_in_one.js with crush.wasm embedded as base64
// Run: node /home/z/my-project/scripts/build_cinejoy_all_in_one.js

'use strict';
const fs = require('fs');
const path = require('path');

const SRC_DIR = '/home/z/my-project/scripts';
const WASM_PATH = path.join(SRC_DIR, 'crush.wasm');
const TEMPLATE_PATH = path.join(SRC_DIR, 'cinejoy.js');
const OUTPUT_PATH = path.join(SRC_DIR, 'cinejoy_all_in_one.js');

// Read the WASM binary and base64-encode it
const wasmBuf = fs.readFileSync(WASM_PATH);
const wasmB64 = wasmBuf.toString('base64');
console.log('Embedded crush.wasm: ' + wasmBuf.length + ' bytes (' + wasmB64.length + ' base64 chars)');

// Read the template (cinejoy.js)
let template = fs.readFileSync(TEMPLATE_PATH, 'utf8');

// Strip the existing loadWasm function and replace with embedded version
// The new loadWasm decodes the embedded base64 instead of reading from disk
const newLoadWasm = `
// crush.wasm embedded as base64 (downloaded from https://api.shegu.st/crush.wasm)
// Source: api.shegu.st/crush.wasm — 67,222 bytes (the official cinejoy.to encryption engine)
// Updated: 2026-08-18 — protocol version "lumen-gate-v2"
const CRUSH_WASM_B64 = "${wasmB64}";

let _wasmInstance = null;

async function loadWasm() {
  if (_wasmInstance) return _wasmInstance;

  // Try embedded base64 first
  try {
    const wasmBuf = Buffer.from(CRUSH_WASM_B64, 'base64');
    const mod = await WebAssembly.compile(wasmBuf);
    _wasmInstance = await WebAssembly.instantiate(mod, {});
    return _wasmInstance;
  } catch (e) {
    console.log('[CineJoy] Embedded WASM failed (' + e.message + '), falling back to download...');
  }

  // Fallback: download from api.shegu.st (in case the embedded version is outdated)
  try {
    const res = await fetch(CRUSH_WASM_URL, {
      headers: { 'User-Agent': UA, 'Referer': CINEJOY_ORIGIN + '/' },
    });
    if (!res.ok) throw new Error('HTTP ' + res.status);
    const buf = Buffer.from(await res.arrayBuffer());
    const mod = await WebAssembly.compile(buf);
    _wasmInstance = await WebAssembly.instantiate(mod, {});
    console.log('[CineJoy] Downloaded crush.wasm (' + buf.length + ' bytes)');
    return _wasmInstance;
  } catch (e) {
    throw new Error('Failed to load crush.wasm: ' + e.message);
  }
}
`;

// Replace the existing loadWasm function (lines 63-84 in the template)
// Match from "let _wasmInstance = null;" through the closing "}" of loadWasm
const oldLoadWasmRegex = /let _wasmInstance = null;\s*\n\s*async function loadWasm\(\)[\s\S]*?return inst;\s*\n}/;
if (!oldLoadWasmRegex.test(template)) {
  console.error('ERROR: Could not find loadWasm function in template');
  process.exit(1);
}
template = template.replace(oldLoadWasmRegex, newLoadWasm.trim());

// Update the file header to reflect that this is the all-in-one version
const newHeader = `// CineJoy All-In-One — Single-File Pure-Node.js Noise Protocol Implementation
// =========================================================================
// This file contains EVERYTHING needed to talk to api.shegu.st directly:
//   - crush.wasm embedded as base64 (67KB binary → 90KB base64 string)
//   - seal_request() encryption function (uses WebAssembly)
//   - AES-GCM response decryption (uses crypto.webcrypto.subtle)
//   - TMDB metadata fetcher
//   - All 7 server fetcher (Lisbon, Solara, Athens, Joy, Castle, Sakura, Canaias)
//   - Stream parser (HLS playlists + direct file URLs)
//
// NO EXTERNAL FILE DEPENDENCIES. Just require this one file:
//
//   const cinejoy = require('./cinejoy_all_in_one.js');
//   const streams = await cinejoy.getStreams('27205', 'movie');
//
// If the embedded crush.wasm becomes outdated, the code automatically falls
// back to downloading the latest version from https://api.shegu.st/crush.wasm.
//
// Protocol: "lumen-gate-v2"
// Architecture: Pure Node.js + WebAssembly + WebCrypto
// No Playwright, no FlareSolverr, no browser dependency at runtime.
//
// Test:
//   node cinejoy_all_in_one.js Lisbon 27205 movie
//   node cinejoy_all_in_one.js Lisbon 1396 tv 1 1   (TV: Breaking Bad S01E01)
//
// Servers:
//   Lisbon   (US) — 4K HDR + 1080p/720p/480p (HLS playlist on info.movieboxnoob.cc)
//   Solara   (US) — multi-quality direct file CDN (lol.movieboxnoob.cc)
//   Athens   (US) — synthetic HLS with multi-language captions
//   Joy      (US) — sparse content (often returns 404)
//   Castle   (US) — synthetic HLS with multi-language captions
//   Sakura   (JP) — anime-only (rejects movies with 404)
//   Canaias  (BR) — multi-quality direct MP4 files (RedeFlix, HJA)
`;

// Replace existing header (everything before "'use strict';")
const headerEnd = template.indexOf("'use strict';");
if (headerEnd < 0) {
  console.error("ERROR: Could not find 'use strict' marker");
  process.exit(1);
}
template = newHeader + '\n' + template.slice(headerEnd);

// Also remove the fs/path requires since we don't need them anymore
// (we use Buffer.from(base64) instead of fs.readFileSync)
template = template.replace(/^const fs = require\('fs'\);\s*\n/m, '');
// Keep `path` since we don't use CRUSH_WASM_CACHE anymore — remove it
template = template.replace(/const CRUSH_WASM_CACHE = path\.join\(__dirname, 'crush\.wasm'\);\s*\n/m, '');
// Remove path require if it's no longer used
if (!template.includes('path.')) {
  template = template.replace(/^const path = require\('path'\);\s*\n/m, '');
}

// Write the all-in-one file
fs.writeFileSync(OUTPUT_PATH, template);

const outSize = fs.statSync(OUTPUT_PATH).size;
console.log('Generated: ' + OUTPUT_PATH);
console.log('Size: ' + outSize + ' bytes (' + (outSize / 1024).toFixed(1) + ' KB)');
