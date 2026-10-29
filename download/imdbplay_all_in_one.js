// imdbplay.tech — Direct Stream Extractor (Movies + TV, up to 4K pass-through)
// ============================================================================
// Extracts DIRECT PLAYABLE HLS streams from the IMDbPlay backend chain:
//
//   imdbplay.tech  →  proxy.garageband.rocks/embed/<type>/<imdb>
//                  →  data.vidsrcme.ru/api.php?type=<type>&imdb=<imdb>[&season=S&episode=E]&stream_urls
//                  →  (encrypted stream_urls via WASM decryptor)
//                  →  per-host JWT token from <host>/generate.php
//                  →  master.m3u8 with all quality variants (240p/720p/1080p/2160p)
//
// KEY MECHANICS
//   1. TMDB → IMDB ID via TMDB API (so Stremio/Nuvio TMDB queries work).
//   2. Fetch encrypted JSON envelope from data.vidsrcme.ru/api.php — `data.stream_urls`
//      is a base64 ChaCha20 nonce||ciphertext; `vs.wasm_url` carries the per-5-min
//      window decryptor (compiled once per process).
//   3. Instantiate the WASM module, call alloc(len)+decrypt(ptr,len), read plaintext
//      from ptr+12. Split by \n to get raw stream URLs.
//   4. For each unique host in those URLs, GET <origin>/generate.php → JWT token
//      (IP-bound, ~short-lived; reuse across all streams of same host).
//   5. Apply token (replace __TOKEN__ placeholder OR append ?token=...).
//   6. Fetch master.m3u8, parse #EXT-X-STREAM-INF variants, return one Nuvio
//      stream per (host × variant).
//
// USAGE
//   node imdbplay_all_in_one.js <tmdbId> <movie|tv> [season] [episode]
//   node imdbplay_all_in_one.js 693134 movie                # Dune Part Two
//   node imdbplay_all_in_one.js 1396 tv 1 1                 # Breaking Bad S01E01
//   node imdbplay_all_in_one.js 1399 tv                     # list seasons/episodes

'use strict';

const https = require('https');
const http = require('http');

const PROVIDER_NAME = 'IMDbPlay';
const TMDB_API_KEY = '8476a7ab80ad76f0936744df0430e67c';
const VIDSRC_API = 'https://data.vidsrcme.ru';
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';
const REFERER = 'https://cloudorchestranova.com/';

// ─── HTTP helpers ──────────────────────────────────────────────────────────
function fetchBuf(url, { headers = {}, timeout = 30000 } = {}) {
  return new Promise((resolve, reject) => {
    const u = new URL(url);
    const lib = u.protocol === 'http:' ? http : https;
    const req = lib.request({
      hostname: u.hostname, path: u.pathname + u.search, method: 'GET',
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

async function fetchJson(url, opts = {}) {
  const r = await fetchBuf(url, { ...opts, headers: { Accept: 'application/json, */*', ...(opts.headers || {}) } });
  if (r.status !== 200) throw new Error(`HTTP ${r.status} from ${url}`);
  return JSON.parse(r.body.toString('utf8'));
}

async function fetchText(url, opts = {}) {
  const r = await fetchBuf(url, opts);
  if (r.status !== 200) throw new Error(`HTTP ${r.status} from ${url}`);
  return r.body.toString('utf8');
}

// ─── TMDB → IMDb ─────────────────────────────────────────────────────────────
async function getTMDBInfo(tmdbId, type) {
  const isTV = type === 'tv' || type === 'series';
  const url = `https://api.themoviedb.org/3/${isTV ? 'tv' : 'movie'}/${tmdbId}?api_key=${TMDB_API_KEY}&append_to_response=external_ids`;
  try {
    const j = await fetchJson(url, { headers: { 'User-Agent': UA } });
    return {
      title: (isTV ? j.name : j.title) || 'Unknown',
      year: ((isTV ? j.first_air_date : j.release_date) || '').slice(0, 4),
      imdbId: j.imdb_id || (j.external_ids && j.external_ids.imdb_id) || null,
      type, tmdbId: String(tmdbId),
    };
  } catch (e) { console.log(`[IMDbPlay] TMDB fetch failed: ${e.message}`); return null; }
}

// ─── Encrypted API + WASM decryptor ─────────────────────────────────────────
// Module-level cache: keep the compiled WASM module for the lifetime of the
// process — same window key `vs.w` means same decryptor.
let _wasmCache = {};   // w -> Promise<WebAssembly.Module>

async function getWasmModule(vs) {
  const key = vs.w;
  if (_wasmCache[key]) return _wasmCache[key];
  const url = vs.wasm_url;
  if (!url) throw new Error('no wasm_url in API response');
  const r = await fetchBuf(url, { headers: { Accept: 'application/wasm' } });
  if (r.status !== 200) throw new Error(`wasm fetch HTTP ${r.status}`);
  _wasmCache[key] = WebAssembly.compile(r.body);
  return _wasmCache[key];
}

function b64decode(s) { return Buffer.from(s, 'base64'); }

async function decryptStreamUrls(json) {
  if (!json || !json.vs || !json.data || typeof json.data.stream_urls !== 'string') {
    // Already plaintext array?
    if (json && json.data && Array.isArray(json.data.stream_urls)) return json.data.stream_urls.filter(Boolean);
    throw new Error('no encrypted stream_urls / decryptor available');
  }
  const mod = await getWasmModule(json.vs);
  const inst = await WebAssembly.instantiate(mod, {});
  const ex = inst.exports;
  if (typeof ex.alloc !== 'function' || typeof ex.decrypt !== 'function') {
    throw new Error('WASM exports missing alloc/decrypt');
  }
  const enc = b64decode(json.data.stream_urls);
  // Fresh memory each call (instantiate fresh so the bump allocator doesn't grow).
  const ptr = ex.alloc(enc.length);
  new Uint8Array(ex.memory.buffer, ptr, enc.length).set(enc);
  const outLen = ex.decrypt(ptr, enc.length);
  const txt = Buffer.from(new Uint8Array(ex.memory.buffer, ptr + 12, outLen)).toString('utf8');
  return txt.split('\n').filter(Boolean);
}

// ─── Token: per-host JWT (IP-bound) ──────────────────────────────────────────
function parseToken(text) {
  const t = String(text || '').trim();
  if (!t) return '';
  if (t.charAt(0) === '{' || t.charAt(0) === '[') {
    try {
      const j = JSON.parse(t);
      if (typeof j === 'string') return j;
      if (j && typeof j === 'object') return j.token || j.data || j.string || j.result || '';
    } catch (e) {}
  }
  return t;
}

async function fetchToken(streamUrl) {
  const u = new URL(streamUrl);
  const tokenUrl = `${u.protocol}//${u.host}/generate.php`;
  try {
    const r = await fetchBuf(tokenUrl, { headers: { Accept: 'text/plain, application/json, */*' }, timeout: 10000 });
    if (r.status !== 200) return '';
    return parseToken(r.body.toString('utf8'));
  } catch (e) { return ''; }
}

function applyToken(url, token) {
  if (!token) return url;
  if (url.indexOf('__TOKEN__') > -1) return url.split('__TOKEN__').join(token);
  return url + (url.indexOf('?') > -1 ? '&' : '?') + 'token=' + token;
}

// ─── Parse master.m3u8 ──────────────────────────────────────────────────────
function parseMasterM3u8(text, streamUrl) {
  if (!text || !text.startsWith('#EXTM3U')) return [];
  const variants = [];
  let currentRes = null, currentBw = null, currentCodecs = null, currentFps = null, currentHdr = null, currentAudio = [];
  const lines = text.split('\n');
  for (const line of lines) {
    const l = line.trim();
    if (l.startsWith('#EXT-X-MEDIA:TYPE=AUDIO')) {
      const langMatch = l.match(/LANGUAGE="([^"]+)"/);
      if (langMatch) currentAudio.push(langMatch[1]);
    } else if (l.startsWith('#EXT-X-STREAM-INF:')) {
      const resMatch = l.match(/RESOLUTION=(\d+)x(\d+)/);
      currentRes = resMatch ? { w: parseInt(resMatch[1]), h: parseInt(resMatch[2]) } : null;
      const bwMatch = l.match(/BANDWIDTH=(\d+)/);
      currentBw = bwMatch ? parseInt(bwMatch[1]) : null;
      const codecsMatch = l.match(/CODECS="([^"]+)"/);
      currentCodecs = codecsMatch ? codecsMatch[1] : null;
      const fpsMatch = l.match(/FRAME-RATE=([\d.]+)/);
      currentFps = fpsMatch ? parseFloat(fpsMatch[1]) : null;
      currentHdr = /VIDEO-RANGE=PQ/.test(l) ? 'HDR/DV' : null;
    } else if (l && !l.startsWith('#') && currentRes) {
      let variantUrl;
      if (l.startsWith('http')) variantUrl = l;
      else if (l.startsWith('/')) { const u = new URL(streamUrl); variantUrl = `${u.protocol}//${u.host}${l}`; }
      else { const base = streamUrl.replace(/\/[^/]*$/, ''); variantUrl = `${base}/${l}`; }
      // The variant playlist URL needs the same token as the master.
      if (streamUrl.includes('token=')) {
        const m = streamUrl.match(/[?&]token=([^&]+)/);
        if (m && !variantUrl.includes('token=')) {
          variantUrl += (variantUrl.includes('?') ? '&' : '?') + 'token=' + m[1];
        }
      }
      const h = currentRes.h;
      const w = currentRes.w;
      // For cinematic 2.39:1 content (height ≈ 0.42 × width), use WIDTH as the
      // primary signal so 1920x802 still classifies as 1080p rather than 720p.
      const primaryH = h >= 2160 || w >= 3840 ? 2160
                    : h >= 1440 || w >= 2560 ? 1440
                    : h >= 1080 || w >= 1920 ? 1080
                    : h >= 720  || w >= 1280 ? 720
                    : h >= 480  || w >= 852  ? 480
                    : 360;
      const quality = primaryH >= 2160 ? '4K' : primaryH >= 1440 ? '1440p' : primaryH >= 1080 ? '1080p' : primaryH >= 720 ? '720p' : primaryH >= 480 ? '480p' : '360p';
      variants.push({
        resolution: currentRes,
        bandwidth: currentBw,
        codecs: currentCodecs,
        fps: currentFps,
        hdr: currentHdr,
        quality,
        audioLanguages: [...currentAudio],
        url: variantUrl,
      });
      currentRes = null; currentBw = null; currentCodecs = null; currentFps = null; currentHdr = null; currentAudio = [];
    }
  }
  return variants;
}

// ─── Build Nuvio stream object ──────────────────────────────────────────────
function buildStream(variant, info, hostLabel, sourceTag) {
  const langs = variant.audioLanguages && variant.audioLanguages.length > 0
    ? variant.audioLanguages.join('+')
    : 'multi';
  const sizeHint = variant.bandwidth ? `~${Math.round(variant.bandwidth / 1000)}kbps` : '';
  const hdrTag = variant.hdr ? ` ${variant.hdr}` : '';
  const fpsTag = variant.fps && variant.fps !== 24 ? ` ${variant.fps}fps` : '';
  const res = variant.resolution ? `${variant.resolution.w}x${variant.resolution.h}` : variant.quality;
  return {
    name: `${PROVIDER_NAME} [${sourceTag}] | ${variant.quality}${hdrTag}${fpsTag} | ${langs}`,
    title: `${info.title}${info.year ? ` (${info.year})` : ''}${info.epLabel ? ` ${info.epLabel}` : ''} [IMDbPlay ${res} ${variant.quality}${hdrTag}${fpsTag} ${sizeHint} ${langs}]`,
    url: variant.url,
    quality: variant.quality,
    type: 'application/vnd.apple.mpegurl',
    behaviorHints: {
      bingeGroup: `imdbplay-${variant.quality}-${langs}-${sourceTag}`,
      notWebReady: false,
    },
  };
}

// ─── Fetch + decrypt + tokenize ──────────────────────────────────────────────
async function resolveStreams(imdbId, isTV, season, episode, info) {
  let apiUrl = `${VIDSRC_API}/api.php?type=${isTV ? 'tv' : 'movie'}&imdb=${imdbId}`;
  if (isTV && season != null && episode != null) apiUrl += `&season=${season}&episode=${episode}`;
  apiUrl += '&stream_urls';

  console.log(`[IMDbPlay] Fetching encrypted envelope: ${apiUrl}`);
  const json = await fetchJson(apiUrl, { headers: { 'X-Requested-With': 'XMLHttpRequest' } });
  if (!json || !json.data) {
    console.log(`[IMDbPlay] API returned no data`);
    return { streams: [], eps: null, meta: null };
  }
  console.log(`[IMDbPlay] Title: ${json.data.title}`);
  console.log(`[IMDbPlay] Source file: ${(json.data.file_name || '').slice(0, 100)}`);
  if (json.thumbnails_url) console.log(`[IMDbPlay] Thumbnails: ${json.thumbnails_url}`);

  let rawUrls;
  try { rawUrls = await decryptStreamUrls(json); }
  catch (e) { console.log(`[IMDbPlay] Decrypt failed: ${e.message}`); return { streams: [], eps: json.data.eps || null, meta: json.data }; }
  console.log(`[IMDbPlay] Decrypted ${rawUrls.length} stream URL(s)`);

  // Group by host — same host => same token
  const byHost = {};
  for (const u of rawUrls) {
    let h;
    try { h = new URL(u).host; } catch (e) { continue; }
    if (!byHost[h]) byHost[h] = [];
    byHost[h].push(u);
  }

  const allStreams = [];
  const seenUrls = new Set();
  for (const [host, urls] of Object.entries(byHost)) {
    console.log(`[IMDbPlay] Host ${host}: ${urls.length} stream(s) — fetching token...`);
    const token = await fetchToken(urls[0]);
    if (!token) { console.log(`[IMDbPlay]   token: (empty) — skipping host`); continue; }
    console.log(`[IMDbPlay]   token: ${token.slice(0, 32)}...`);

    for (let i = 0; i < urls.length; i++) {
      const playable = applyToken(urls[i], token);
      let masterText;
      try { masterText = await fetchText(playable, { headers: { Accept: 'application/vnd.apple.mpegurl, */*' }, timeout: 15000 }); }
      catch (e) { console.log(`[IMDbPlay]   [${i + 1}] master HTTP error: ${e.message}`); continue; }
      const variants = parseMasterM3u8(masterText, playable);
      if (variants.length === 0) { console.log(`[IMDbPlay]   [${i + 1}] no variants`); continue; }
      console.log(`[IMDbPlay]   [${i + 1}] ${variants.length} variants: ${variants.map(v => v.quality + (v.hdr ? `/${v.hdr}` : '')).join(', ')}`);

      const hostLabel = host.split('.')[0];
      for (const v of variants) {
        if (seenUrls.has(v.url)) continue;
        seenUrls.add(v.url);
        allStreams.push(buildStream(v, info, hostLabel, hostLabel));
      }
    }
  }

  return { streams: allStreams, eps: json.data.eps || null, meta: json.data };
}

// ─── Public entry ────────────────────────────────────────────────────────────
async function getStreams(tmdbId, type, season, episode) {
  tmdbId = String(tmdbId);
  const isTV = type === 'tv' || type === 'series';
  console.log(`[IMDbPlay] Request: tmdb=${tmdbId} type=${type}` + (isTV ? ` S${season || '?'}E${episode || '?'}` : ''));

  const info0 = await getTMDBInfo(tmdbId, type);
  if (!info0) return [];
  if (!info0.imdbId) { console.log('[IMDbPlay] No IMDB id in TMDB — cannot resolve'); return []; }
  const info = { ...info0, epLabel: isTV && season && episode ? `S${String(season).padStart(2, '0')}E${String(episode).padStart(2, '0')}` : '' };
  console.log(`[IMDbPlay] TMDB → ${info.title} (${info.year}) IMDB=${info.imdbId}`);

  if (isTV && (season == null || episode == null)) {
    console.log('[IMDbPlay] TV request without S/E — listing episodes instead of resolving streams');
    const list = await listEpisodes(info.imdbId);
    if (list) {
      console.log(`[IMDbPlay] Available seasons: ${Object.keys(list).join(', ')}`);
      for (const s of Object.keys(list).sort((a, b) => +a - +b)) {
        console.log(`[IMDbPlay]   Season ${s}: episodes ${list[s].join(', ')}`);
      }
    }
    return [];
  }

  const { streams } = await resolveStreams(info.imdbId, isTV, season, episode, info);
  const qOrder = { '4K': 0, '2160p': 0, '1440p': 1, '1080p': 2, '720p': 3, '480p': 4, '360p': 5 };
  streams.sort((a, b) => (qOrder[a.quality] || 99) - (qOrder[b.quality] || 99));
  console.log(`[IMDbPlay] ${streams.length} stream(s) total`);
  return streams;
}

async function listEpisodes(imdbId) {
  try {
    const json = await fetchJson(`${VIDSRC_API}/api.php?type=tv&imdb=${imdbId}`);
    return (json.data && json.data.eps) || null;
  } catch (e) { return null; }
}

// ─── Module exports ─────────────────────────────────────────────────────────
module.exports = {
  getStreams, getTMDBInfo, listEpisodes, resolveStreams,
  decryptStreamUrls, parseMasterM3u8, applyToken, fetchToken,
  VIDSRC_API, PROVIDER_NAME,
};

// ─── CLI ────────────────────────────────────────────────────────────────────
if (require.main === module) {
  const args = process.argv.slice(2);
  if (args.length < 2) {
    console.log('IMDbPlay.tech Direct Stream Extractor');
    console.log('  Movies + TV — direct playable HLS, up to 4K pass-through');
    console.log('');
    console.log('Usage: node imdbplay_all_in_one.js <tmdbId> <movie|tv> [season] [episode]');
    console.log('Examples:');
    console.log('  node imdbplay_all_in_one.js 693134 movie          # Dune Part Two');
    console.log('  node imdbplay_all_in_one.js 1396 tv 1 1           # Breaking Bad S01E01');
    console.log('  node imdbplay_all_in_one.js 1399 tv               # list seasons/episodes');
    process.exit(1);
  }
  getStreams(args[0], args[1], args[2], args[3])
    .then(s => {
      console.log('\n=== Final playable streams ===');
      if (s.length === 0) { console.log('No streams found.'); return; }
      s.forEach((x, i) => {
        console.log(`${i + 1}. ${x.name}`);
        console.log(`   ${x.url.slice(0, 180)}${x.url.length > 180 ? '...' : ''}`);
      });
      console.log(`\nTotal: ${s.length}`);
    })
    .catch(e => { console.error('FATAL: ' + e.stack); process.exit(1); });
}
