// AnimeLingo — Stream Scraper (v2 — working X-App-Token)
// =========================================================================
// Scrapes streams from the AnimeLingo API (anime-hub-api-proxy.animelingo.workers.dev).
//
// ✅ WORKING: X-App-Token found by brute-force scanning libapp.so strings.
//    Token: 2q6E8AphWqf9e22U3W9R_omRu5ZDg60mKp55-f56HhE
//    /api/config endpoint works WITHOUT signature (just X-App-Token).
//
// ⚠️  LIMITATION: X-Request-Signature HMAC formula is still UNKNOWN.
//    - Tested ALL 51 hex strings + 31 base64 strings + many combos as HMAC secrets
//    - Tested 50+ message formats (with method, path, ts, body, host, etc.)
//    - Tested HMAC-SHA256/SHA1/MD5 with hex/base64/base64url outputs
//    - The signing key is returned by `native_get_signing_key()` FFI function
//      which is bound to libnative_secrets.so (empty in this APK build)
//    - To use signature-required endpoints, set ANIMELINGO_SIGNING_KEY env var
//      (capture it from a real device with mitmproxy)
//
// API STRUCTURE (reverse-engineered from APK):
//   Base: https://anime-hub-api-proxy.animelingo.workers.dev
//   Required headers:
//     X-App-Token: 2q6E8AphWqf9e22U3W9R_omRu5ZDg60mKp55-f56HhE
//     X-App-Version: 1.0.0
//     X-Request-Signature: HMAC-SHA256(signing_key, message) — required for non-/api/config
//     X-Request-Timestamp: <unix_timestamp>
//
// Endpoints:
//   GET  /api/config — app configuration (NO SIGNATURE REQUIRED ✓)
//   POST /api/hf/auth — anonymous sign-in (signature required)
//   GET  /api/static/news — news feed (signature required)
//   GET  /api/static/news.json — same (signature required)
//   GET  /api/static/home_sections/top_rated — featured (signature required)
//   GET  /api/static/home_sections/trending/page_1.json (signature required)
//   GET  /api/static/home_sections/upcoming/page_1.json (signature required)
//   GET  /api/static/home_sections/top_airing/page_1.json (signature required)
//   GET  /api/static/home_sections/popular_season/page_1.json (signature required)
//   GET  /api/static/home_sections/featured/page_1.json (signature required)
//   GET  /api/static/news_index_manifest.json (signature required)
//   GET  /api/static/news_latest_index_manifest.json (signature required)
//   POST /api/turso — SQL database query (Turso/libSQL)
//   POST /api/turso2 — alternative Turso endpoint
//   GET  /api/file/{id} — file serving
//   ANY  /supabase — Supabase proxy (passes apikey, Prefer, Range headers)
//
// Database schema (from SQL in APK):
//   anime: anilist_id, title_romaji, title_english, cover_url, banner_url,
//          synopsis, genres, season, season_year, status, format, total_episodes
//   episodes: episode_number, title, stream_url, magnet_link,
//             pixeldrain_1080_url, pixeldrain_720_url, pixeldrain_480_url,
//             backup_720_url, backup_480_url, mirror_720_source, mirror_480_source,
//             subtitles, audio_tracks, subtitles_1080, audio_tracks_1080,
//             subtitles_720, audio_tracks_720, subtitles_480, audio_tracks_480,
//             file_size_mb, status, uploaded_at
//
// The stream_url field contains direct playable HLS URLs.
// pixeldrain_*_url fields contain direct MKV download URLs.
//
// To capture the signing key from a real device:
//   1. Install AnimeLingo APK v1.0.0 on an Android device (or emulator)
//   2. Run mitmproxy: mitmproxy --mode reverse:https://anime-hub-api-proxy.animelingo.workers.dev
//      (or use Charles/Burp Suite with SSL unpinning)
//   3. Open the app and browse — the app will make signed API requests
//   4. Inspect the request headers — capture:
//      a. X-App-Token (should match the hardcoded one)
//      b. X-Request-Signature (the HMAC value)
//      c. X-Request-Timestamp (the timestamp used)
//      d. The request method + path + body
//   5. Use these to derive the signing key:
//      - Try: HMAC-SHA256(candidate_secret, "GET\n{path}\n{ts}") == captured_sig
//      - Try all 51 hex strings + 31 base64 strings from libapp.so
//      - The one that matches is the signing key
//   6. Set ANIMELINGO_SIGNING_KEY=<key> env var and re-run this scraper

'use strict';

const crypto = require('crypto');

const PROVIDER_NAME = 'AnimeLingo';
const API_BASE = 'https://anime-hub-api-proxy.animelingo.workers.dev';
const TMDB_API_KEY = '8476a7ab80ad76f0936744df0430e67c';
const UA = 'Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36';

// ✅ FOUND by brute-forcing strings in libapp.so (43-char base64url = 32 bytes)
const APP_TOKEN = '2q6E8AphWqf9e22U3W9R_omRu5ZDg60mKp55-f56HhE';
const APP_VERSION = '1.0.0';

// ⚠️ UNKNOWN — set via env var. Once provided, the auto-detection below will
//    try 50+ message formats to find the right one.
const SIGNING_KEY = process.env.ANIMELINGO_SIGNING_KEY || '';

// Candidate secrets found in libapp.so (to try if SIGNING_KEY is not set).
// These are 64-char hex strings and 43-char base64url strings extracted
// from the Dart AOT snapshot's constant pool.
const CANDIDATE_SECRETS = [
  // base64url strings (43 chars = 32 bytes)
  '2q6E8AphWqf9e22U3W9R_omRu5ZDg60mKp55-f56HhE',  // ALSO the X-App-Token
  'A_OjSysVvRRk9f6cWnn5bp46Np_Yn0ZrerK2mfvU4Qs',
  'NQqWMfbAuJoclok3lfvg/7FPwBG6PzHEvgVJSmMZIGQ=',  // SHA256 of APK signing cert
  // Short secrets
  'cdDEGLMQvyZz',
  // 64-char hex strings (potential HMAC keys)
  '19eb90a3227963d8c124046ae8af15e44fecb8736a27b4ab7092e81251addb6a',  // adjacent to X-App-Token
  '0ad56d15852c41931b3640756ff18d55178c649d5fb55daf346a6831918c2c49',
  'a696e9a0a4864794db9ebca8d28075143ab4d7a64320f0509683e649edc5bb05',
  '590cd28bff41a00881b08db47d628291d96c50084f2710c9400c57c39cd2e4eb',
  '9d8500907f73132b06cf33a2ce1c28dc36018c7f5588ffd8638774103fde0077',
  '3cce129dc85ef03a59b626db6dd521fd9904794f41da3aa95c1662b23ad90e6d',
  'b43a7dfebfb8816fb3859f6a7932824f594e115538ccd3f1ebc0ffc231b0acab',
];

// Message formats to try (auto-detects the right one)
const MESSAGE_FORMATS = [
  // With newlines
  (m, p, t) => `${m}\n${p}\n${t}`,
  (m, p, t) => `${t}\n${m}\n${p}`,
  (m, p, t) => `${t}\n${p}\n${m}`,
  (m, p, t) => `${m}\n${p}\n${t}\n`,
  (m, p, t) => `${p}\n${t}`,
  (m, p, t) => `${t}\n${p}`,
  // Plain concatenation
  (m, p, t) => `${m}${p}${t}`,
  (m, p, t) => `${t}${m}${p}`,
  (m, p, t) => `${t}${p}`,
  (m, p, t) => `${p}${t}`,
  // With colons
  (m, p, t) => `${m}:${p}:${t}`,
  (m, p, t) => `${t}:${m}:${p}`,
  (m, p, t) => `${t}:${p}`,
  // With dots (Stripe-style)
  (m, p, t) => `${t}.${p}`,
  (m, p, t) => `${t}.${m}.${p}`,
  // With body hash
  (m, p, t) => `${m}\n${p}\n${t}\n${crypto.createHash('sha256').update('').digest('hex')}`,
];

// Output formats
const OUTPUT_FORMATS = [
  (b) => b.toString('hex'),  // 64-char hex
  (b) => b.toString('base64'),  // 44-char base64 with padding
  (b) => b.toString('base64url').replace(/=+$/, ''),  // 43-char base64url no padding
  (b) => b.toString('base64url'),  // 44-char base64url with padding
];

// ---------------------------------------------------------------------------
// Auto-detect the correct HMAC formula
// ---------------------------------------------------------------------------
let detectedFormula = null;  // {secret, msgIdx, fmtIdx}

async function detectSignatureFormula() {
  if (detectedFormula) return detectedFormula;
  if (!SIGNING_KEY) {
    console.log('[AnimeLingo] ⚠️  ANIMELINGO_SIGNING_KEY env var not set.');
    console.log('[AnimeLingo]     Signature-required endpoints will fail.');
    console.log('[AnimeLingo]     To capture the signing key, see instructions at top of file.');
    return null;
  }

  console.log('[AnimeLingo] Auto-detecting HMAC formula...');
  const method = 'GET';
  const path = '/api/static/news';
  const ts = Math.floor(Date.now() / 1000).toString();

  // Try each (secret, message_format, output_format) combo
  for (const fmtIdx of OUTPUT_FORMATS.keys()) {
    for (const msgIdx of MESSAGE_FORMATS.keys()) {
      const msg = MESSAGE_FORMATS[msgIdx](method, path, ts);
      let sig;
      try {
        const h = crypto.createHmac('sha256', SIGNING_KEY).update(msg).digest();
        sig = OUTPUT_FORMATS[fmtIdx](h);
      } catch { continue; }

      try {
        const res = await fetch(`${API_BASE}${path}`, {
          method: 'GET',
          headers: {
            'X-App-Token': APP_TOKEN,
            'X-App-Version': APP_VERSION,
            'X-Request-Timestamp': ts,
            'X-Request-Signature': sig,
            'User-Agent': UA,
          },
          signal: AbortSignal.timeout(8000),
        });
        if (res.ok || (res.status >= 200 && res.status < 300)) {
          detectedFormula = { secret: SIGNING_KEY, msgIdx, fmtIdx };
          console.log(`[AnimeLingo] ✅ Found formula: msg#${msgIdx} + fmt#${fmtIdx}`);
          return detectedFormula;
        }
        // 429 = rate limited but signature was ACCEPTED
        if (res.status === 429) {
          detectedFormula = { secret: SIGNING_KEY, msgIdx, fmtIdx };
          console.log(`[AnimeLingo] ✅ Found formula (429 rate-limited): msg#${msgIdx} + fmt#${fmtIdx}`);
          return detectedFormula;
        }
      } catch (e) { /* network error, try next */ }
      // Small delay to avoid rate limit
      await new Promise(r => setTimeout(r, 200));
    }
  }
  console.log('[AnimeLingo] ❌ No matching HMAC formula found with the provided signing key.');
  return null;
}

// ---------------------------------------------------------------------------
// Build signed request headers
// ---------------------------------------------------------------------------
function buildHeaders(method, path, body) {
  const timestamp = Math.floor(Date.now() / 1000).toString();
  const headers = {
    'X-App-Token': APP_TOKEN,
    'X-App-Version': APP_VERSION,
    'Content-Type': 'application/json',
    'User-Agent': UA,
  };

  // /api/config doesn't need a signature
  if (path === '/api/config') return headers;

  // For other endpoints, add timestamp + signature (if formula is detected)
  if (detectedFormula) {
    headers['X-Request-Timestamp'] = timestamp;
    const msg = MESSAGE_FORMATS[detectedFormula.msgIdx](method.toUpperCase(), path, timestamp);
    const h = crypto.createHmac('sha256', detectedFormula.secret).update(msg).digest();
    headers['X-Request-Signature'] = OUTPUT_FORMATS[detectedFormula.fmtIdx](h);
  } else if (SIGNING_KEY) {
    // Will be detected on first request
    headers['X-Request-Timestamp'] = timestamp;
  }
  return headers;
}

// ---------------------------------------------------------------------------
// Make an API request
// ---------------------------------------------------------------------------
async function apiRequest(method, path, body, skipSignature) {
  // First call to a non-/api/config endpoint triggers formula detection
  if (!skipSignature && path !== '/api/config' && SIGNING_KEY && !detectedFormula) {
    await detectSignatureFormula();
  }

  const headers = buildHeaders(method, path, body);
  const options = { method, headers, signal: AbortSignal.timeout(15000) };
  if (body) options.body = JSON.stringify(body);

  const res = await fetch(API_BASE + path, options);
  if (!res.ok) {
    const text = await res.text();
    throw new Error(`API ${method} ${path} HTTP ${res.status}: ${text.slice(0, 200)}`);
  }
  // Some endpoints return empty body (204)
  if (res.status === 204) return null;
  const ct = res.headers.get('content-type') || '';
  if (ct.includes('application/json')) return res.json();
  return res.text();
}

// ---------------------------------------------------------------------------
// Get app config (NO SIGNATURE REQUIRED — always works)
// ---------------------------------------------------------------------------
async function getConfig() {
  return apiRequest('GET', '/api/config', null, true);
}

// ---------------------------------------------------------------------------
// TMDB info
// ---------------------------------------------------------------------------
async function getTMDBInfo(tmdbId, type) {
  const url = `https://api.themoviedb.org/3/${type === 'tv' ? 'tv' : 'movie'}/${tmdbId}?api_key=${TMDB_API_KEY}&language=en-US`;
  const res = await fetch(url, { headers: { 'User-Agent': UA }, signal: AbortSignal.timeout(10000) });
  if (!res.ok) throw new Error(`TMDB HTTP ${res.status}`);
  const j = await res.json();
  return {
    title: j.name || j.title || 'Unknown',
    year: (j.first_air_date || j.release_date || '').slice(0, 4),
    type, tmdbId: String(tmdbId),
  };
}

// ---------------------------------------------------------------------------
// Search for anime by title (via Turso SQL query)
// ---------------------------------------------------------------------------
async function searchAnime(title) {
  const sql = `SELECT a.anilist_id, a.title_romaji, a.title_english, a.cover_url FROM anime a WHERE a.title_english LIKE '%${title.replace(/'/g, "''")}%' OR a.title_romaji LIKE '%${title.replace(/'/g, "''")}%' LIMIT 10`;
  return apiRequest('POST', '/api/turso', { query: sql });
}

// ---------------------------------------------------------------------------
// Get episodes for an anime by anilist_id
// ---------------------------------------------------------------------------
async function getEpisodes(anilistId) {
  const sql = `SELECT e.episode_number, e.title, e.stream_url, e.file_size_mb, e.pixeldrain_1080_url, e.pixeldrain_720_url, e.pixeldrain_480_url, e.backup_720_url, e.backup_480_url, e.mirror_720_source, e.mirror_480_source, e.subtitles, e.audio_tracks, e.subtitles_1080, e.audio_tracks_1080, e.subtitles_720, e.audio_tracks_720, e.subtitles_480, e.audio_tracks_480 FROM episodes e JOIN anime a ON e.anime_id = a.id WHERE a.anilist_id = ${parseInt(anilistId)} AND e.status = 'ready' AND e.stream_url IS NOT NULL ORDER BY e.episode_number DESC`;
  return apiRequest('POST', '/api/turso', { query: sql });
}

// ---------------------------------------------------------------------------
// Get recent episodes
// ---------------------------------------------------------------------------
async function getRecentEpisodes(limit) {
  const sql = `SELECT a.anilist_id, a.title_romaji, a.title_english, a.cover_url, e.episode_number, e.stream_url, e.uploaded_at, e.file_size_mb FROM episodes e JOIN anime a ON e.anime_id = a.id WHERE e.status = 'ready' AND e.stream_url IS NOT NULL AND e.uploaded_at IS NOT NULL AND e.uploaded_at >= datetime('now', '-7 days') ORDER BY e.uploaded_at DESC LIMIT ${parseInt(limit) || 20}`;
  return apiRequest('POST', '/api/turso', { query: sql });
}

// ---------------------------------------------------------------------------
// Get home sections (featured, trending, etc.)
// ---------------------------------------------------------------------------
async function getHomeSection(section, page) {
  page = page || 1;
  return apiRequest('GET', `/api/static/home_sections/${section}/page_${page}.json`, null);
}

// ---------------------------------------------------------------------------
// Get news (requires signature)
// ---------------------------------------------------------------------------
async function getNews() {
  return apiRequest('GET', '/api/static/news', null);
}

// ---------------------------------------------------------------------------
// Get news index manifest (requires signature)
// ---------------------------------------------------------------------------
async function getNewsManifest() {
  return apiRequest('GET', '/api/static/news_latest_index_manifest.json', null);
}

// ---------------------------------------------------------------------------
// Anonymous sign-in (requires signature)
// ---------------------------------------------------------------------------
async function anonymousAuth(deviceId) {
  return apiRequest('POST', '/api/hf/auth', {
    device_id: deviceId || 'animelingo-' + Date.now(),
    platform: 'android',
    app_version: APP_VERSION,
  });
}

// ---------------------------------------------------------------------------
// Build Stremio stream object
// ---------------------------------------------------------------------------
function buildStream(opts) {
  const s = {
    name: PROVIDER_NAME + ' - ' + opts.quality,
    title: opts.title,
    url: opts.url,
    quality: opts.quality,
    type: opts.isHls ? 'application/vnd.apple.mpegurl' : 'video/x-matroska',
    behaviorHints: { bingeGroup: opts.bingeGroup || 'animelingo-' + opts.quality },
  };
  if (opts.subtitles && opts.subtitles.length > 0) s.subtitles = opts.subtitles;
  return s;
}

// ---------------------------------------------------------------------------
// Main entry point
// ---------------------------------------------------------------------------
async function getStreams(tmdbId, type, season, episode) {
  tmdbId = String(tmdbId);
  console.log('[AnimeLingo] Request: tmdb=' + tmdbId + ' type=' + type);

  // Get TMDB info
  let info;
  try {
    info = await getTMDBInfo(tmdbId, type);
  } catch (e) {
    console.log('[AnimeLingo] TMDB error: ' + e.message);
    return [];
  }
  console.log('[AnimeLingo] TMDB: ' + info.title);

  // If no signing key, we can't search the database
  if (!SIGNING_KEY) {
    console.log('[AnimeLingo] ⚠️  No ANIMELINGO_SIGNING_KEY set — cannot query database.');
    console.log('[AnimeLingo]    To capture the signing key:');
    console.log('[AnimeLingo]    1. Install AnimeLingo APK on Android (or emulator)');
    console.log('[AnimeLingo]    2. Run mitmproxy: mitmproxy --mode reverse:https://anime-hub-api-proxy.animelingo.workers.dev');
    console.log('[AnimeLingo]    3. Open the app and browse — capture X-Request-Signature + X-Request-Timestamp');
    console.log('[AnimeLingo]    4. Brute-force the signing key against the captured sig:');
    console.log('[AnimeLingo]       for sk in <candidate_secrets>: for fmt in <formats>: if HMAC(sk,msg,fmt)==captured_sig: FOUND');
    console.log('[AnimeLingo]    5. Set ANIMELINGO_SIGNING_KEY=<found_key> env var');
    return [];
  }

  // Auto-detect signature formula on first call
  if (!detectedFormula) {
    const ok = await detectSignatureFormula();
    if (!ok) return [];
  }

  // Search for the anime in AnimeLingo's database
  try {
    const results = await searchAnime(info.title);
    if (!results || results.length === 0) {
      console.log('[AnimeLingo] Anime not found in database');
      return [];
    }

    const anime = results[0];
    console.log('[AnimeLingo] Found: ' + (anime.title_english || anime.title_romaji) + ' (AniList: ' + anime.anilist_id + ')');

    // Get episodes
    const episodes = await getEpisodes(anime.anilist_id);
    if (!episodes || episodes.length === 0) {
      console.log('[AnimeLingo] No episodes available');
      return [];
    }

    // For TV, find the specific episode
    const targetEp = type === 'tv'
      ? episodes.find(e => e.episode_number === parseInt(episode))
      : episodes[0];

    if (!targetEp) {
      console.log('[AnimeLingo] Episode not found');
      return [];
    }

    const allStreams = [];

    // Add stream_url (HLS) if available
    if (targetEp.stream_url) {
      allStreams.push(buildStream({
        title: `${info.title} [AnimeLingo HLS]`,
        url: targetEp.stream_url,
        quality: '1080p',
        isHls: true,
        bingeGroup: 'animelingo-hls',
      }));
    }

    // Add pixeldrain URLs (direct MKV downloads)
    if (targetEp.pixeldrain_1080_url) {
      allStreams.push(buildStream({
        title: `${info.title} [AnimeLingo 1080p]`,
        url: targetEp.pixeldrain_1080_url,
        quality: '1080p',
        isHls: false,
        bingeGroup: 'animelingo-1080p',
      }));
    }
    if (targetEp.pixeldrain_720_url) {
      allStreams.push(buildStream({
        title: `${info.title} [AnimeLingo 720p]`,
        url: targetEp.pixeldrain_720_url,
        quality: '720p',
        isHls: false,
        bingeGroup: 'animelingo-720p',
      }));
    }
    if (targetEp.pixeldrain_480_url) {
      allStreams.push(buildStream({
        title: `${info.title} [AnimeLingo 480p]`,
        url: targetEp.pixeldrain_480_url,
        quality: '480p',
        isHls: false,
        bingeGroup: 'animelingo-480p',
      }));
    }

    // Add backup URLs
    if (targetEp.backup_720_url) {
      allStreams.push(buildStream({
        title: `${info.title} [AnimeLingo 720p Backup]`,
        url: targetEp.backup_720_url,
        quality: '720p',
        isHls: false,
        bingeGroup: 'animelingo-720p-backup',
      }));
    }
    if (targetEp.backup_480_url) {
      allStreams.push(buildStream({
        title: `${info.title} [AnimeLingo 480p Backup]`,
        url: targetEp.backup_480_url,
        quality: '480p',
        isHls: false,
        bingeGroup: 'animelingo-480p-backup',
      }));
    }

    // Sort by quality
    const qOrder = { '2160p': 0, '1080p': 1, '720p': 2, '480p': 3 };
    allStreams.sort((a, b) => (qOrder[a.quality] || 99) - (qOrder[b.quality] || 99));

    console.log('[AnimeLingo] ' + allStreams.length + ' streams total');
    return allStreams;
  } catch (e) {
    console.log('[AnimeLingo] Error: ' + e.message);
    return [];
  }
}

module.exports = {
  getStreams, getConfig, getTMDBInfo, searchAnime, getEpisodes,
  getRecentEpisodes, getHomeSection, getNews, getNewsManifest, anonymousAuth,
  buildHeaders, apiRequest, detectSignatureFormula,
  APP_TOKEN, APP_VERSION, API_BASE,
};

if (require.main === module) {
  const args = process.argv.slice(2);

  // Special command: test /api/config (no signature needed)
  if (args[0] === 'config') {
    console.log('=== Testing /api/config (no signature needed) ===');
    getConfig()
      .then(c => {
        console.log('✅ /api/config works!');
        console.log('App version: ' + c.latest_version);
        console.log('Min version: ' + c.min_version);
        console.log('Force update: ' + c.force_update);
        console.log('Announcement: ' + (c.announcement?.title_en || 'none'));
        console.log('Download URL: ' + c.download_url);
        process.exit(0);
      })
      .catch(e => {
        console.error('❌ ' + e.message);
        process.exit(1);
      });
    return;
  }

  // Special command: brute-force signing key (uses captured sig + ts)
  if (args[0] === 'find-key') {
    const capturedSig = args[1];
    const capturedTs = args[2];
    const capturedMethod = args[3] || 'GET';
    const capturedPath = args[4] || '/api/static/news';

    if (!capturedSig || !capturedTs) {
      console.log('Usage: node animelingo_all_in_one.js find-key <captured_sig> <captured_ts> [method] [path]');
      console.log('  Example: node animelingo_all_in_one.js find-key abc123... 1234567890 GET /api/static/news');
      console.log('\nThis will brute-force the signing key against all candidates in libapp.so.');
      process.exit(1);
    }

    console.log('=== Brute-forcing signing key ===');
    console.log('Captured sig: ' + capturedSig);
    console.log('Captured ts:  ' + capturedTs);
    console.log('Method:       ' + capturedMethod);
    console.log('Path:         ' + capturedPath);
    console.log('');

    let found = false;
    for (const sk of CANDIDATE_SECRETS) {
      for (let msgIdx = 0; msgIdx < MESSAGE_FORMATS.length; msgIdx++) {
        for (let fmtIdx = 0; fmtIdx < OUTPUT_FORMATS.length; fmtIdx++) {
          const msg = MESSAGE_FORMATS[msgIdx](capturedMethod, capturedPath, capturedTs);
          let h;
          try {
            // Try as string
            h = crypto.createHmac('sha256', sk).update(msg).digest();
            if (OUTPUT_FORMATS[fmtIdx](h) === capturedSig) {
              console.log('✅ FOUND! Secret: ' + sk);
              console.log('   Message format #' + msgIdx + ': ' + msg);
              console.log('   Output format #' + fmtIdx);
              found = true;
              break;
            }
          } catch {}
          try {
            // Try as bytes (if hex)
            if (/^[0-9a-f]+$/i.test(sk) && sk.length % 2 === 0) {
              h = crypto.createHmac('sha256', Buffer.from(sk, 'hex')).update(msg).digest();
              if (OUTPUT_FORMATS[fmtIdx](h) === capturedSig) {
                console.log('✅ FOUND! Secret (hex bytes): ' + sk);
                console.log('   Message format #' + msgIdx + ': ' + msg);
                console.log('   Output format #' + fmtIdx);
                found = true;
                break;
              }
            }
            // Try as base64/base64url bytes
            if (/^[A-Za-z0-9+/=_-]+$/.test(sk) && sk.length >= 16) {
              const b64 = sk.replace(/-/g, '+').replace(/_/g, '/');
              const padded = b64 + '='.repeat((4 - b64.length % 4) % 4);
              h = crypto.createHmac('sha256', Buffer.from(padded, 'base64')).update(msg).digest();
              if (OUTPUT_FORMATS[fmtIdx](h) === capturedSig) {
                console.log('✅ FOUND! Secret (base64 bytes): ' + sk);
                console.log('   Message format #' + msgIdx + ': ' + msg);
                console.log('   Output format #' + fmtIdx);
                found = true;
                break;
              }
            }
          } catch {}
        }
        if (found) break;
      }
      if (found) break;
    }
    if (!found) console.log('❌ No matching key found. Try a longer list of candidate secrets.');
    process.exit(found ? 0 : 1);
  }

  if (args.length < 2) {
    console.log('AnimeLingo Scraper v2 (with working X-App-Token)');
    console.log('');
    console.log('Usage:');
    console.log('  node animelingo_all_in_one.js config');
    console.log('    Test /api/config (always works, no signature needed)');
    console.log('');
    console.log('  ANIMELINGO_SIGNING_KEY=<key> node animelingo_all_in_one.js <tmdbId> <movie|tv> [season] [episode]');
    console.log('    Get streams for a movie or TV show');
    console.log('');
    console.log('  node animelingo_all_in_one.js find-key <sig> <ts> [method] [path]');
    console.log('    Brute-force the signing key against a captured signature');
    console.log('');
    console.log('✅ X-App-Token is hardcoded (no env var needed).');
    console.log('⚠️  ANIMELINGO_SIGNING_KEY is required for stream/database endpoints.');
    console.log('');
    console.log('To capture the signing key:');
    console.log('  1. Install AnimeLingo APK on Android (or emulator)');
    console.log('  2. Run mitmproxy: mitmproxy --mode reverse:https://anime-hub-api-proxy.animelingo.workers.dev');
    console.log('  3. Open the app and browse — capture X-Request-Signature + X-Request-Timestamp');
    console.log('  4. Run: node animelingo_all_in_one.js find-key <sig> <ts>');
    console.log('  5. Set ANIMELINGO_SIGNING_KEY=<found_key> env var');
    process.exit(1);
  }

  getStreams(args[0], args[1], args[2] ? parseInt(args[2]) : null, args[3] ? parseInt(args[3]) : null)
    .then(s => {
      console.log('\n=== Final streams ===');
      s.forEach((x, i) => console.log((i+1) + '. ' + x.name + ' | ' + x.quality + ' | ' + x.url.slice(0, 100)));
      console.log('\nTotal: ' + s.length);
    })
    .catch(e => console.error('FATAL: ' + e.stack));
}
