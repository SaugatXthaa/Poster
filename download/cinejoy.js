// CineJoy Enhanced Scraper — All 7 Servers via Pure-Node.js Noise Protocol
// =================================================================
// Implements the "lumen-gate-v2" Noise protocol directly in pure Node.js,
// using the official crush.wasm from api.shegu.st.
//
// This bypasses the HdHub addon wrapper and calls api.shegu.st directly,
// giving us access to ALL 7 cinejoy.to servers:
//
//   Lisbon   (US) — has 4K HDR + 1080p/720p/480p (HLS playlist)
//   Solara   (US) — multi-quality direct file CDN (lol.movieboxnoob.cc)
//   Athens   (US) — synthetic HLS with multi-language captions
//   Joy      (US) — sparse content (404 for most titles)
//   Castle   (US) — synthetic HLS with multi-language captions
//   Sakura   (JP) — anime-only (rejects movies with 404)
//   Canaias  (BR) — multi-quality direct MP4 files (RedeFlix, HJA)
//
// Flow:
//   1. Download crush.wasm (cached locally on first run)
//   2. For each server (in parallel):
//      a. Build payload: { path: "/<server>/movie?tmdb=...", payload: null }
//      b. JSON-encode → f (Uint8Array)
//      c. Generate 44-byte random nonce → r
//      d. WASM seal_request(f, r) → encrypted output
//      e. Output structure: [32B responseKey, 1B keyId, 65B ephemeralPublic, encrypted_body]
//      f. POST encrypted_body to https://api.shegu.st/g (Content-Type: text/plain)
//   3. Decrypt response using AES-GCM:
//      - Key: responseKey (32 bytes)
//      - IV: response[0:12]
//      - AAD: "lumen-gate-v2" + [0x00, 0x02, keyId] + ephemeralPublic (81 bytes total)
//      - TagLength: 128 bits
//   4. Decrypted JSON: { data: { stream: [{ type, id, playlist, ... }] }, status: 200 }
//   5. Parse the stream entry — could be HLS playlist, direct file, or synthetic HLS
//
// No Playwright, no FlareSolverr — pure Node.js + WebAssembly.

'use strict';

const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

const PROVIDER_NAME = 'CineJoy';
const TMDB_API_KEY = '8476a7ab80ad76f0936744df0430e67c';
const SHEGU_API = 'https://api.shegu.st';
const CRUSH_WASM_URL = 'https://api.shegu.st/crush.wasm';
const CRUSH_WASM_CACHE = path.join(__dirname, 'crush.wasm');
const CINEJOY_ORIGIN = 'https://cinejoy.to';

const UA =
  'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 ' +
  '(KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';

// Protocol constants (from cinejoy.to JS bundle)
const K = 32;          // responseKey length
const $ = 1;           // keyId length
const S0 = 65;         // ephemeralPublic length (includes 0x04 prefix)
const T = K + $ + S0;  // total header length = 98
const B = 12;          // AES-GCM IV length
const W0 = 16;         // AES-GCM tag length

const ALL_SERVERS = ['Lisbon', 'Solara', 'Athens', 'Joy', 'Castle', 'Sakura', 'Canaias'];

let _wasmInstance = null;

async function loadWasm() {
  if (_wasmInstance) return _wasmInstance;

  if (!fs.existsSync(CRUSH_WASM_CACHE)) {
    console.log('[CineJoy] Downloading crush.wasm...');
    const res = await fetch(CRUSH_WASM_URL, {
      headers: { 'User-Agent': UA, 'Referer': CINEJOY_ORIGIN + '/' },
    });
    if (!res.ok) throw new Error(`Failed to download crush.wasm: HTTP ${res.status}`);
    const buf = Buffer.from(await res.arrayBuffer());
    fs.writeFileSync(CRUSH_WASM_CACHE, buf);
    console.log('[CineJoy] crush.wasm cached (' + buf.length + ' bytes)');
  }

  const wasmBuf = fs.readFileSync(CRUSH_WASM_CACHE);
  const mod = await WebAssembly.compile(wasmBuf);
  const inst = await WebAssembly.instantiate(mod, {});
  _wasmInstance = inst;
  return inst;
}

// Call crush.wasm's seal_request to encrypt the request payload.
async function sealRequest(payloadJson) {
  const inst = await loadWasm();
  const exports = inst.exports;
  const memory = exports.memory;

  const f = Buffer.from(payloadJson, 'utf8');
  const r = crypto.randomBytes(44);
  const outputSize = f.length + 512;

  const a = exports.alloc(f.length, 1);
  const k = exports.alloc(r.length, 1);
  const b = exports.alloc(outputSize, 1);

  new Uint8Array(memory.buffer, a, f.length).set(f);
  new Uint8Array(memory.buffer, k, r.length).set(r);

  const n = exports.seal_request(a, f.length, k, r.length, b, outputSize);

  exports.dealloc(a);
  exports.dealloc(k);

  if (n <= 0) {
    exports.dealloc(b);
    throw new Error(`seal_request failed (returned ${n})`);
  }

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
function buildAad(ephemeralPublic, keyId) {
  const prefix = Buffer.from('lumen-gate-v2', 'utf8');
  return Buffer.concat([
    prefix,
    Buffer.from([0x00, 0x02]),
    Buffer.from([keyId]),
    ephemeralPublic,
  ]);
}

async function decryptResponse(responseBuf, sealed) {
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

  const decrypted = await crypto.webcrypto.subtle.decrypt(
    {
      name: 'AES-GCM',
      iv: iv,
      additionalData: aad,
      tagLength: W0 * 8,
    },
    key,
    ciphertextWithTag
  );
  return JSON.parse(Buffer.from(decrypted).toString('utf8'));
}

// Get TMDB info for the request payload
async function getTMDBInfo(tmdbId, type) {
  const endpoint = type === 'tv' ? 'tv' : 'movie';
  const url = 'https://api.themoviedb.org/3/' + endpoint + '/' + tmdbId +
    '?api_key=' + TMDB_API_KEY + '&append_to_response=external_ids';
  try {
    const res = await fetch(url, { headers: { 'User-Agent': UA } });
    if (!res.ok) return null;
    const d = await res.json();
    return {
      title: type === 'tv' ? d.name : d.title,
      year: parseInt((d.first_air_date || d.release_date || '').slice(0, 4)) || null,
      imdbId: (d.external_ids && d.external_ids.imdb_id) || d.imdb_id || null,
    };
  } catch (e) {
    return null;
  }
}

// Build the path string for the request payload
// Movies: /<server>/movie?imdb=...&title=...&tmdb=...&year=...
// TV:     /<server>/series?imdb=...&title=...&tmdb=...&year=...&season=...&episode=...
function buildPath(server, tmdbId, type, season, episode, tmdbInfo) {
  var params = [];
  if (tmdbInfo && tmdbInfo.imdbId) params.push(['imdb', tmdbInfo.imdbId]);
  if (tmdbInfo && tmdbInfo.title) params.push(['title', tmdbInfo.title]);
  params.push(['tmdb', String(tmdbId)]);
  if (tmdbInfo && tmdbInfo.year) params.push(['year', String(tmdbInfo.year)]);
  if (type === 'tv' && season != null && episode != null) {
    params.push(['season', String(season)]);
    params.push(['episode', String(episode)]);
  }
  // Sort alphabetically
  params.sort(function (a, b) { return a[0].localeCompare(b[0]); });
  var queryString = params.map(function (p) {
    return encodeURIComponent(p[0]) + '=' + encodeURIComponent(p[1]);
  }).join('&');
  // For TV shows, use "series" path; for movies, use "movie"
  var mediaType = type === 'tv' ? 'series' : 'movie';
  return '/' + server + '/' + mediaType + '?' + queryString;
}

// Fetch stream URL from a single server
async function fetchFromServer(server, tmdbId, type, season, episode, tmdbInfo) {
  try {
    const reqPath = buildPath(server, tmdbId, type, season, episode, tmdbInfo);
    const payloadJson = JSON.stringify({ path: reqPath, payload: null });
    const sealed = await sealRequest(payloadJson);

    const res = await fetch(SHEGU_API + '/g', {
      method: 'POST',
      headers: {
        'Content-Type': 'text/plain;charset=UTF-8',
        'User-Agent': UA,
        'Origin': CINEJOY_ORIGIN,
        'Referer': CINEJOY_ORIGIN + '/',
      },
      body: sealed.body,
    });

    if (!res.ok) {
      return { server, error: `HTTP ${res.status}`, status: res.status };
    }

    const responseBuf = Buffer.from(await res.arrayBuffer());
    const decrypted = await decryptResponse(responseBuf, sealed);
    return { server, data: decrypted.data, status: decrypted.status };
  } catch (e) {
    return { server, error: e.message };
  }
}

// Parse a stream entry from the decrypted response.
// Stream entry can be:
//   { type: "hls", id, playlist, captions: [] }
//   { type: "file", id, qualities: {<q>: {type, url}}, captions: [] }
function parseStreamEntry(entry, server, info) {
  var streams = [];
  if (!entry) return streams;

  if (entry.type === 'hls' && entry.playlist) {
    streams.push({
      name: PROVIDER_NAME + ' - ' + server + (entry.id ? ' (' + entry.id + ')' : ''),
      title: info.title + ' [' + server + ' server]',
      url: entry.playlist,
      quality: server === 'Lisbon' ? '2160p' : '1080p',
      type: 'application/vnd.apple.mpegurl',
      headers: { 'User-Agent': UA, 'Referer': CINEJOY_ORIGIN + '/' },
      behaviorHints: { bingeGroup: 'cinejoy-' + server.toLowerCase() },
    });
  } else if (entry.type === 'file' && entry.qualities) {
    for (var q of Object.keys(entry.qualities)) {
      var qInfo = entry.qualities[q];
      if (qInfo && qInfo.url) {
        // Resolve relative URLs like "redeflix-720p"
        var url = qInfo.url;
        if (!url.startsWith('http')) {
          // These are internal IDs that need further resolution - skip for now
          continue;
        }
        streams.push({
          name: PROVIDER_NAME + ' - ' + server + ' ' + q + ' (' + entry.id + ')',
          title: info.title + ' [' + server + ' ' + q + ']',
          url: url,
          quality: q + 'p',
          type: qInfo.type === 'mp4' ? 'video/mp4' : 'application/vnd.apple.mpegurl',
          headers: { 'User-Agent': UA, 'Referer': CINEJOY_ORIGIN + '/' },
          behaviorHints: { bingeGroup: 'cinejoy-' + server.toLowerCase() + '-' + q },
        });
      }
    }
  }

  return streams;
}

// Main entry: get streams from all 7 servers in parallel
async function getStreams(tmdbId, type, season, episode) {
  var isMovie = type !== 'tv';
  console.log('[CineJoy] Request: tmdb=' + tmdbId + ' type=' + type +
    (isMovie ? '' : ' S' + season + 'E' + episode));

  // Pre-flight: ensure WASM is loaded
  try {
    await loadWasm();
  } catch (e) {
    console.log('[CineJoy] WASM load failed: ' + e.message + ' — falling back to no streams');
    return [];
  }

  // Get TMDB info
  var info = await getTMDBInfo(tmdbId, type);
  if (!info || !info.title) {
    console.log('[CineJoy] Could not resolve TMDB info');
    return [];
  }
  console.log('[CineJoy] TMDB: ' + info.title + ' (' + info.year + ')');

  // For movies, skip Sakura (anime-only). For TV, include all servers.
  var servers = isMovie
    ? ALL_SERVERS.filter(function (s) { return s !== 'Sakura'; })
    : ALL_SERVERS;

  // Fetch from all servers in parallel
  console.log('[CineJoy] Fetching from ' + servers.length + ' servers in parallel...');
  var results = await Promise.all(
    servers.map(function (server) {
      return fetchFromServer(server, tmdbId, type, season, episode, info);
    })
  );

  // Parse results into streams
  var allStreams = [];
  var serverStatus = [];
  for (var r of results) {
    if (r.error) {
      serverStatus.push(r.server + ': ERROR (' + r.error + ')');
      continue;
    }
    if (r.status !== 200) {
      var errMsg = r.data && r.data.error ? r.data.error : 'status ' + r.status;
      serverStatus.push(r.server + ': ' + errMsg);
      continue;
    }
    if (r.data && r.data.stream) {
      var parsedStreams = [];
      for (var entry of r.data.stream) {
        var s = parseStreamEntry(entry, r.server, info);
        parsedStreams = parsedStreams.concat(s);
      }
      if (parsedStreams.length > 0) {
        allStreams = allStreams.concat(parsedStreams);
        serverStatus.push(r.server + ': ' + parsedStreams.length + ' streams');
      } else {
        serverStatus.push(r.server + ': 0 streams (parse fail)');
      }
    } else {
      serverStatus.push(r.server + ': no stream data');
    }
  }

  console.log('[CineJoy] ' + allStreams.length + ' streams from ' + servers.length + ' servers');
  serverStatus.forEach(function (s) { console.log('  ' + s); });

  return allStreams;
}

module.exports = {
  getStreams: getStreams,
  listServers: function () { return ALL_SERVERS.slice(); },
  getTMDBInfo: getTMDBInfo,
  fetchFromServer: fetchFromServer,
  _loadWasm: loadWasm,
  _sealRequest: sealRequest,
  _decryptResponse: decryptResponse,
};
