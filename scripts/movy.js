// Movy.bz / Vidy.st Scraper — Pure Node.js Streamcrypto Implementation
// =================================================================
// Implements the "streamcrypto" protocol used by movy.bz and vidy.st
// to encrypt stream source responses from api.wecollege.net.
//
// Architecture:
//   movy.bz       — frontend (Next.js SSG, multilingual)
//   vidy.st       — embed player (Next.js, exposes /movie/{tmdbId} etc.)
//   api.wecollege.net — Fastify backend with 12 city-named server endpoints:
//                       Miami, Boston, Seattle, Denver, Austin, Chicago,
//                       Dallas, Munich, Berlin, Paris, Cancun, Delhi
//
// Protocol:
//   1. GET https://api.wecollege.net/seed?mediaId={tmdbId}
//      → returns { seed: "...", ttlMs: 30000 }
//   2. GET https://api.wecollege.net/{server}/sources?...params&enc=2&seed={seed}
//      → returns base64url-encoded encrypted payload
//   3. Decrypt using the seed + mediaId length:
//      a. base64url-decode the encrypted payload → bytes
//      b. Generate a keystream using:
//         - RC4-like 256-byte permutation table (keyed by seed)
//         - 61-element state array (mixed from FNV-1a hash, MurmurHash3,
//           MD5 initial value, and SHA-256 round constants)
//         - Custom accumulator mixing with constants like 2654435769
//           (the golden ratio for 32-bit)
//      c. XOR the encrypted bytes with the keystream
//      d. Verify the 4-byte magic header "mvm1" (bytes [109, 118, 109, 49])
//      e. Return the rest as UTF-8 JSON: { sources: [...], subtitles: [...] }
//
// Stream object format (from decrypted JSON):
//   sources: [{
//     url: "https://...",
//     quality: "1080p" | "720p" | ...,
//     type: "mp4" | "hls" | ...,
//     label: "...",
//     ...
//   }]
//
// No Playwright, no FlareSolverr — pure Node.js.

'use strict';

const crypto = require('crypto');

const PROVIDER_NAME = 'Movy';
const TMDB_API_KEY = '8476a7ab80ad76f0936744df0430e67c';
const MOVY_API = 'https://api.wecollege.net';
const VIDY_ORIGIN = 'https://www.vidy.st';

const UA =
  'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 ' +
  '(KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';

// All 12 city-named servers (per chunk 8351)
const ALL_SERVERS = [
  'miami', 'boston', 'seattle', 'denver',
  'austin', 'chicago', 'dallas', 'munich',
  'berlin', 'paris', 'cancun', 'delhi',
];

// =================== STREAMCRYPTO CONSTANTS ===================
// SHA-256 round constants (used to seed the state array)
const SHA256_K = [
  1116352408, 1899447441, 3049323471, 3921009573, 961987163, 1508970993,
  2453635748, 2870763221, 3624381080, 310598601, 607225278, 1426881987,
  1925078388, 2162078206, 2614888103, 3248222580,
];

// Magic header "mvm1" — first 4 bytes of every decrypted payload
const MAGIC = [109, 118, 109, 49];  // 'm', 'v', 'm', '1'

// Golden ratio constant for 32-bit (2^32 / φ ≈ 2654435769)
const PHI = 2654435769;

// MD5 initial value (used in accumulator seeding)
const MD5_INIT = 1732584193;

// FNV-1a constants
const FNV_OFFSET = 2166136261;
const FNV_PRIME = 16777619;

// MurmurHash3 finalizer constant
const MH3_C1 = 2246822507;
const MH3_C2 = 3266489909;

// Final state-XOR constant (from chunk 8351)
const FINAL_XOR = 2779096485;

// =================== STREAMCRYPTO HELPERS ===================

// MurmurHash3 32-bit finalizer
function murmurFinalize(e) {
  e >>>= 0;
  e ^= e >>> 16;
  e = Math.imul(e, MH3_C1) >>> 0;
  e ^= e >>> 13;
  e = Math.imul(e, MH3_C2) >>> 0;
  return (e ^= e >>> 16) >>> 0;
}

// Rotate-left 32-bit
function rotl(e, t) {
  e >>>= 0;
  if ((t &= 31) === 0) return e >>> 0;
  return ((e << t) | (e >>> (32 - t))) >>> 0;
}

// c(n) returns true if n*(n+1) is even (n = 0 or 3 mod 4)
// m(n) returns true if n*(n+1) is odd  (n = 1 or 2 mod 4)
function parityEven(e) { return (e * (e + 1) & 1) === 0; }
function parityOdd(e) { return (e * (e + 1) & 1) === 1; }

// FNV-1a 32-bit hash of a string
function fnv1a(str) {
  let t = FNV_OFFSET;
  for (let a = 0; a < str.length; a++) {
    t = Math.imul(t ^ str.charCodeAt(a), FNV_PRIME) >>> 0;
  }
  return murmurFinalize(t);
}

// MD5-init-style hash mixing (uses SHA-256 K table)
function md5StyleHash(str) {
  let t = MD5_INIT;
  for (let a = 0; a < str.length; a++) {
    t = rotl((t ^ Math.imul(str.charCodeAt(a), SHA256_K[15 & a])) >>> 0, 5);
  }
  return murmurFinalize(t);
}

// RC4 Key Scheduling Algorithm — returns 256-element permutation table
function rc4ksa(key) {
  const t = new Array(256);
  for (let i = 0; i < 256; i++) t[i] = i;
  let j = 0;
  for (let i = 0; i < 256; i++) {
    j = (j + t[i] + key.charCodeAt(i % key.length)) & 255;
    const tmp = t[i];
    t[i] = t[j];
    t[j] = tmp;
  }
  return t;
}

// Build cipher state from seed and mediaId length.
// IMPORTANT: There are TWO branches depending on SEED length parity (not mediaId length)!
//   - If m(seed.length) is TRUE (seed length is 1 or 2 mod 4):
//       S = RC4 KSA permutation table (256 elements)
//       acc = MD5-style hash of seed
//   - If c(seed.length) is TRUE (seed length is 0 or 3 mod 4):
//       S = 61-element state array (mixed from FNV-1a hash, MurmurHash3,
//           and SHA-256 round constants)
//       acc = mixed hash accumulator
function buildState(seed, mediaIdLength) {
  if (parityOdd(seed.length)) {
    // Use RC4 KSA path
    const S_table = rc4ksa(seed);
    const acc = md5StyleHash(seed);
    return { S: S_table, acc: acc };
  }
  // Use 61-element state array path
  const a = new Array(61);
  
  // Compute r_init = p(fnv1a(seed) ^ p(mediaIdLength ^ PHI))
  let r = murmurFinalize(
    fnv1a(seed) ^ murmurFinalize((mediaIdLength >>> 0) ^ PHI)
  ) >>> 0;
  
  // Fill the 61-element state array
  for (let e = 0; e < 8; e++) {
    if (parityEven(e)) {
      // e = 0, 3, 4, 7 (e % 4 == 0 or 3)
      const t = r % 61;
      r = rotl((r + PHI) >>> 0, 7 + (7 & e));
      a[t] = (r ^ murmurFinalize(r)) >>> 0;
      r = murmurFinalize((r + t) >>> 0);
    } else {
      // e = 1, 2, 5, 6
      a[e] = SHA256_K[15 & e];
    }
  }
  
  return { S: a, acc: murmurFinalize(FINAL_XOR ^ r) >>> 0 };
}

// Generate next 32-bit output byte from cipher state
function nextByte(state, counter) {
  const S = state.S;          // 61-element state array
  let s = state.acc;          // accumulator
  const o = s % 61;           // index into S
  const i = -1;                // (o in S) is always true → i = 0 - 1 = -1 (all 1s in 32-bit)
  const d = S[o] >>> 0;       // S[o]
  
  // Compute r = d XOR (PHI * (counter+1))
  const r = (d ^ (Math.imul(PHI, (counter + 1)) >>> 0)) >>> 0;
  
  // l = (s ^ r) | (s & r & i) = (s ^ r) | (s & r) = s | r
  // (since x | y = (x ^ y) | (x & y))
  let l = (((s ^ r) >>> 0) | ((s & r & i) >>> 0)) >>> 0;
  
  // Mix: l = rotl(l + s, o % 32) XOR rotl(s, (o*7) % 32)
  //   (uses `o` - the index into S - NOT `s` the accumulator)
  l = (rotl((l + s) >>> 0, 31 & o) ^ rotl(s, 31 & Math.imul(o, 7))) >>> 0;
  
  // Add PHI, finalize
  const new_s = murmurFinalize((l + PHI) >>> 0) >>> 0;
  S[o] = new_s;
  state.acc = new_s;
  return new_s;
}

// Generate keystream bytes from seed + mediaId length
function generateKeystream(seed, mediaIdLength, outputLen) {
  const state = buildState(seed, mediaIdLength);
  const n = new Uint8Array(outputLen);
  let s = 0;
  for (let e = 0; e < outputLen; ) {
    const next = nextByte(state, s++);
    n[e++] = 255 & next;
    if (e < outputLen) n[e++] = (next >>> 8) & 255;
    if (e < outputLen) n[e++] = (next >>> 16) & 255;
    if (e < outputLen) n[e++] = (next >>> 24) & 255;
  }
  return n;
}

// Base64url decode → Uint8Array
function base64urlDecode(str) {
  // Convert base64url to standard base64
  let b64 = str.replace(/-/g, '+').replace(/_/g, '/');
  // Pad to multiple of 4
  b64 = b64.padEnd(4 * Math.ceil(b64.length / 4), '=');
  const binStr = atob(b64);
  const out = new Uint8Array(binStr.length);
  for (let i = 0; i < binStr.length; i++) out[i] = binStr.charCodeAt(i);
  return out;
}

// Decrypt the encrypted payload using seed + mediaId length
function decryptPayload(encryptedB64, seed, mediaIdLength) {
  // 1. Decode base64
  const encrypted = base64urlDecode(encryptedB64);
  
  // 2. Generate keystream of same length
  const keystream = generateKeystream(seed, mediaIdLength, encrypted.length);
  
  // 3. XOR
  for (let i = 0; i < encrypted.length; i++) {
    encrypted[i] ^= keystream[i];
  }
  
  // 4. Verify magic header "mvm1"
  for (let i = 0; i < MAGIC.length; i++) {
    if (encrypted[i] !== MAGIC[i]) {
      throw new Error('Decryption failed: bad seed or tampered payload (magic header mismatch)');
    }
  }
  
  // 5. Return UTF-8 string of bytes after magic header
  const jsonBytes = encrypted.subarray(MAGIC.length);
  return new TextDecoder('utf-8').decode(jsonBytes);
}

// =================== API CLIENT ===================

// Seed cache: { mediaId → { seed, expiresAt } }
const _seedCache = new Map();

// Get a seed for the given mediaId. If `forceFresh` is true, skip cache.
async function getSeed(mediaId, forceFresh) {
  if (!forceFresh) {
    const cached = _seedCache.get(String(mediaId));
    if (cached && cached.expiresAt - 5000 > Date.now()) {
      return cached.seed;
    }
  }
  
  const url = MOVY_API + '/seed?mediaId=' + encodeURIComponent(mediaId);
  const res = await fetch(url, {
    headers: {
      'User-Agent': UA,
      'Origin': VIDY_ORIGIN,
      'Referer': VIDY_ORIGIN + '/',
    },
    signal: AbortSignal.timeout(10000),
  });
  if (!res.ok) {
    throw new Error('Seed fetch failed: HTTP ' + res.status);
  }
  const data = await res.json();
  if (!data.seed) {
    throw new Error('No seed in response: ' + JSON.stringify(data).slice(0, 100));
  }
  const ttlMs = data.ttlMs || 30000;
  _seedCache.set(String(mediaId), {
    seed: data.seed,
    expiresAt: Date.now() + ttlMs,
  });
  return data.seed;
}

// Fetch and decrypt sources from a single city server.
// Each server gets its OWN fresh seed (the server invalidates seeds after one use).
// On HTTP 401 (STREAMCRYPTO_SEED_INVALID), retry once with a new seed.
async function fetchFromServer(server, tmdbId, type, season, episode, tmdbInfo) {
  try {
    const mediaId = String(tmdbId);
    
    // Build query params (matching the JS bundle's order)
    const params = new URLSearchParams();
    if (tmdbInfo && tmdbInfo.title) {
      params.set('title', encodeURIComponent(tmdbInfo.title));
    }
    params.set('mediaType', type === 'tv' ? 'tv' : 'movie');
    if (tmdbInfo && tmdbInfo.year) params.set('year', String(tmdbInfo.year));
    if (type === 'tv') {
      if (season != null) params.set('season', String(season));
      if (episode != null) params.set('episode', String(episode));
    }
    params.set('tmdbId', mediaId);
    if (tmdbInfo && tmdbInfo.imdbId) params.set('imdbId', tmdbInfo.imdbId);
    
    const url = MOVY_API + '/' + server + '/sources';
    
    // Try up to 3 times (in case of 401 seed invalidation)
    let lastError = null;
    for (let attempt = 0; attempt < 3; attempt++) {
      // Fetch a FRESH seed for each attempt
      const seed = await getSeed(mediaId, true /* force fresh */);
      params.set('enc', '2');
      params.set('seed', seed);
      
      const res = await fetch(url + '?' + params.toString(), {
        headers: {
          'User-Agent': UA,
          'Origin': VIDY_ORIGIN,
          'Referer': VIDY_ORIGIN + '/',
        },
        signal: AbortSignal.timeout(15000),
      });
      
      if (res.status === 401) {
        // STREAMCRYPTO_SEED_INVALID — retry with fresh seed
        lastError = 'STREAMCRYPTO_SEED_INVALID (attempt ' + (attempt + 1) + ')';
        // Invalidate cache for this mediaId
        _seedCache.delete(mediaId);
        // Small delay before retry
        await new Promise(r => setTimeout(r, 200));
        continue;
      }
      
      if (!res.ok) {
        // Try to read the error
        const errText = await res.text().catch(() => '');
        let errMsg = errText;
        try {
          const errJson = JSON.parse(errText);
          errMsg = errJson.message || errJson.error || errText;
        } catch (_) {}
        return { server, error: 'HTTP ' + res.status + ': ' + errMsg.slice(0, 200) };
      }
      
      // Got 200 OK with encrypted payload
      const encrypted = await res.text();
      
      // Decrypt it
      try {
        const decryptedJson = decryptPayload(encrypted, seed, mediaId.length);
        const parsed = JSON.parse(decryptedJson);
        return { server, data: parsed, status: 200 };
      } catch (e) {
        // Decryption failed — might be stale seed, retry
        lastError = 'Decryption failed: ' + e.message;
        _seedCache.delete(mediaId);
        await new Promise(r => setTimeout(r, 200));
        continue;
      }
    }
    
    // All retries exhausted
    return { server, error: lastError || 'All retries exhausted' };
  } catch (e) {
    return { server, error: e.message };
  }
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

// Parse a source entry from the decrypted JSON
function parseSourceEntry(source, server, info) {
  const streams = [];
  if (!source || !source.url) return streams;
  
  // Determine quality
  let quality = source.quality || source.label || '';
  const q = String(quality).toLowerCase();
  if (q.includes('2160') || q.includes('4k') || q.includes('uhd')) quality = '2160p';
  else if (q.includes('1440')) quality = '1440p';
  else if (q.includes('1080')) quality = '1080p';
  else if (q.includes('720')) quality = '720p';
  else if (q.includes('480')) quality = '480p';
  else if (q.includes('360')) quality = '360p';
  else if (!quality) quality = 'HD';
  
  // Determine type
  let type = source.type || '';
  let streamType;
  if (type === 'mp4' || type === 'mkv') streamType = 'video/mp4';
  else if (type === 'hls' || type === 'm3u8') streamType = 'application/vnd.apple.mpegurl';
  else if (source.url.endsWith('.m3u8') || source.url.includes('.m3u8')) {
    streamType = 'application/vnd.apple.mpegurl';
  } else if (source.url.endsWith('.mp4') || source.url.endsWith('.mkv')) {
    streamType = 'video/mp4';
  } else {
    streamType = 'application/vnd.apple.mpegurl';  // default
  }
  
  // Build label
  const label = source.label || source.quality || server + ' ' + quality;
  
  streams.push({
    name: PROVIDER_NAME + ' - ' + server + ' (' + label + ')',
    title: (info.title || 'unknown') + ' [' + server + ']',
    url: source.url,
    quality: quality,
    type: streamType,
    headers: { 'User-Agent': UA, 'Referer': VIDY_ORIGIN + '/' },
    behaviorHints: {
      bingeGroup: 'movy-' + server + '-' + quality,
      notWebReady: false,
    },
  });
  
  return streams;
}

// Main entry: get streams from all 12 servers in parallel
async function getStreams(tmdbId, type, season, episode) {
  const isMovie = type !== 'tv';
  
  // Input validation
  if (!isMovie && (season == null || episode == null)) {
    console.log('[Movy] TV request without season/episode — returning empty');
    return [];
  }
  if (!tmdbId) {
    console.log('[Movy] Empty TMDB ID — returning empty');
    return [];
  }
  
  console.log('[Movy] Request: tmdb=' + tmdbId + ' type=' + type +
    (isMovie ? '' : ' S' + season + 'E' + episode));
  
  // Get TMDB info (with 10s timeout)
  let info;
  try {
    info = await Promise.race([
      getTMDBInfo(tmdbId, type),
      new Promise((_, reject) => setTimeout(() => reject(new Error('TMDB timeout')), 10000)),
    ]);
  } catch (e) {
    console.log('[Movy] TMDB fetch error: ' + e.message);
    return [];
  }
  if (!info || !info.title) {
    console.log('[Movy] Could not resolve TMDB info');
    return [];
  }
  console.log('[Movy] TMDB: ' + info.title + ' (' + info.year + ')');
  
  // Fetch from all 12 servers SEQUENTIALLY (rate limits prevent parallel requests).
  // Each server gets its own fresh seed.
  console.log('[Movy] Fetching from ' + ALL_SERVERS.length + ' servers sequentially...');
  const results = [];
  for (let i = 0; i < ALL_SERVERS.length; i++) {
    const server = ALL_SERVERS[i];
    const r = await fetchFromServer(server, tmdbId, type, season, episode, info);
    results.push(r);
    // Small delay to avoid rate limiting (skip after last)
    if (i < ALL_SERVERS.length - 1) {
      await new Promise(resolve => setTimeout(resolve, 300));
    }
  }
  
  // Parse results
  const allStreams = [];
  const serverStatus = [];
  for (const r of results) {
    if (r.error) {
      serverStatus.push(r.server + ': ERROR (' + r.error + ')');
      continue;
    }
    if (r.data && Array.isArray(r.data.sources)) {
      const parsedStreams = [];
      for (const source of r.data.sources) {
        const s = parseSourceEntry(source, r.server, info);
        parsedStreams.push(...s);
      }
      if (parsedStreams.length > 0) {
        allStreams.push(...parsedStreams);
        serverStatus.push(r.server + ': ' + parsedStreams.length + ' streams');
      } else {
        serverStatus.push(r.server + ': 0 streams (sources empty)');
      }
    } else {
      serverStatus.push(r.server + ': no sources array');
    }
  }
  
  console.log('[Movy] ' + allStreams.length + ' streams from ' + ALL_SERVERS.length + ' servers');
  serverStatus.forEach((s) => console.log('  ' + s));
  
  return allStreams;
}

// List servers
function listServers() {
  return ALL_SERVERS.slice();
}

module.exports = {
  getStreams,
  listServers,
  getTMDBInfo,
  fetchFromServer,
  _decryptPayload: decryptPayload,
  _getSeed: getSeed,
  _generateKeystream: generateKeystream,
};

// CLI test
if (require.main === module) {
  (async () => {
    const args = process.argv.slice(2);
    const tmdbId = args[0] || '27205';
    const type = args[1] || 'movie';
    const season = args[2] ? parseInt(args[2], 10) : null;
    const episode = args[3] ? parseInt(args[3], 10) : null;
    
    console.log('=== Movy CLI Test ===');
    console.log('TMDB: ' + tmdbId + ', Type: ' + type +
      (season ? ' S' + season + 'E' + episode : ''));
    
    const streams = await getStreams(tmdbId, type, season, episode);
    console.log('\n=== RESULT ===');
    console.log('Total streams: ' + streams.length);
    streams.forEach((s, i) => {
      console.log('\n[' + i + '] ' + s.name);
      console.log('    Quality: ' + s.quality);
      console.log('    Type: ' + s.type);
      console.log('    URL: ' + s.url.slice(0, 100));
    });
  })();
}
