// Comet (PenguPlay /direct/experimental/ backend) — Pure HTTPS Stream Extractor
// ============================================================================
// WHAT THIS PROVIDER DOES
//   Re-implements PenguPlay's `/direct/experimental/` endpoint as a standalone
//   Stremio-style provider. Every stream returned is a direct HTTPS CDN URL —
//   no magnet links, no infohashes, no torrent client needed on the user side.
//
// HOW PENGUPLAY'S /direct/experimental/ ACTUALLY WORKS
//   1. Stremio calls `pengu.uk/direct/experimental/<base64(json+filename)>`
//      where json = {t:"movie", i:"tt37287335", s:null, e:null,
//                    h:"<40-char infohash>", f:<fileIdx>}
//   2. pengu.uk 307-redirects to comet.feels.legal with a HARDCODED base64
//      config payload in the path:
//        { cachedOnly: true, enableTorrent: false,
//          deduplicateStreams: true,
//          debridServices: [{service:"torbox",
//                             apiKey:"70f3c689-821f-4a9c-8277-198518d99bc8"}] }
//      → That is a TorBox PUBLIC API key (works for any cached file).
//   3. comet.feels.legal (the Comet addon) asks TorBox:
//        "Is infohash <h> file <f> already in your cache?"
//      → Because cachedOnly:true, Comet only returns HTTPS streams for
//        torrents TorBox has ALREADY downloaded. Torrents not yet cached
//        are silently dropped (because enableTorrent:false).
//   4. For each cached torrent, Comet returns a playback URL shaped like:
//        https://comet.feels.legal/<config>/playback/<hash>/<idx>/n/n/n
//      which itself 302-redirects to:
//        https://store-XXX.wnam.tb-cdn.io/dld/<uuid>?token=<TorBox-key>
//      which is the final, seekable (accept-ranges: bytes) MKV over HTTPS.
//
// THIS SCRAPER
//   Bypasses the PenguPlay middle layer entirely. Calls Comet's normal
//   Stremio-addon endpoint `/stream/{movie|series}/<imdb>[:s:e].json`
//   with the SAME config — gets back the same stream list PenguPlay would
//   assemble. Then sorts, deduplicates, parses quality from filename, and
//   returns a tidy Stremio stream list.
//
// USAGE
//   node comet_all_in_one.js tt37287335 movie             # Obsession (2025)
//   node comet_all_in_one.js tt1375666 movie              # Inception
//   node comet_all_in_one.js tt0903747 series 1 1         # Breaking Bad S01E01
//   node comet_all_in_one.js tt0944947 series 1 1         # Game of Thrones S01E01
//
// RETURN FORMAT
//   Array of Stremio stream objects:
//     {
//       name:    "[TB] Comet 4K HDR | x265 | Hindi",
//       title:   "Obsession.2025.2160p.UHD.BluRay.REMUX.DV.HDR...",
//       url:     "https://comet.feels.legal/.../playback/<hash>/<idx>/n/n/n",
//       quality: "2160p",
//       behaviorHints: { bingeGroup, filename, notWebReady:false }
//     }
//
//   The `url` field is HTTPS (no magnet: URLs ever). When the player opens
//   it, Comet returns 302 → tb-cdn.io (TorBox CDN) → plays as direct MKV.
//
// PURE NODE.JS
//   Zero Playwright, zero browser, zero cloudscraper. Comet itself runs on
//   Cloudflare and never challenges addon-to-addon JSON requests.
// ============================================================================

'use strict';

const https = require('https');
const http = require('http');

const PROVIDER_NAME = 'Comet';
const TMDB_API_KEY = '8476a7ab80ad76f0936744df0430e67c';

// The exact same Comet config PenguPlay uses (extracted from its 307 redirect
// from pengu.uk/direct/experimental/). Re-using it means we get the EXACT same
// stream list — same TorBox API key, same cachedOnly:true, same enableTorrent:
// false (so no magnet URLs ever leak to the client).
const PENGUPLAY_COMET_CONFIG_B64 = 'eyJtYXhSZXN1bHRzUGVyUmVzb2x1dGlvbiI6MCwibWF4U2l6ZSI6MCwiY2FjaGVkT25seSI6dHJ1ZSwic29ydENhY2hlZFVuY2FjaGVkVG9nZXRoZXIiOmZhbHNlLCJyZW1vdmVUcmFzaCI6dHJ1ZSwicmVzdWx0Rm9ybWF0IjpbImFsbCJdLCJlbmFibGVUb3JyZW50IjpmYWxzZSwiZGVkdXBsaWNhdGVTdHJlYW1zIjp0cnVlLCJzY3JhcGVEZWJyaWRBY2NvdW50VG9ycmVudHMiOmZhbHNlLCJkZWJyaWRTdHJlYW1Qcm94eVBhc3N3b3JkIjoiIiwibGFuZ3VhZ2VzIjp7InJlcXVpcmVkIjpbXSwiYWxsb3dlZCI6W10sImV4Y2x1ZGUiOltdLCJwcmVmZXJyZWQiOltdfSwicmVzb2x1dGlvbnMiOnt9LCJvcHRpb25zIjp7InJlbW92ZV9yYW5rc191bmRlciI6LTEwMDAwMDAwMDAwLCJhbGxvd19lbmdsaXNoX2luX2xhbmd1YWdlcyI6ZmFsc2UsInJlbW92ZV91bmtub3duX2xhbmd1YWdlcyI6ZmFsc2V9LCJkZWJyaWRTZXJ2aWNlcyI6W3sic2VydmljZSI6InRvcmJveCIsImFwaUtleSI6IjcwZjNjNjg5LTgyMWYtNGE5Yy04Mjc3LTE5ODUxOGQ5OWJjOCJ9XX0=';

// Allow overriding the TorBox key via env var. If TORBOX_API_KEY is set and
// differs from DEFAULT_TORBOX_KEY, the script uses the env-var key instead.
// Otherwise, the default key (below) is used to regenerate the Comet config.
// Get your own free key at https://torbox.app/settings.
// USER'S OWN FREE-TIER TORBOX KEY (signup at https://torbox.app)
const DEFAULT_TORBOX_KEY = 'ef869d91-98b8-4d1c-81a5-0d53e259b179';
const USER_TORBOX_KEY = process.env.TORBOX_API_KEY && process.env.TORBOX_API_KEY.trim() || '';
const ACTIVE_TORBOX_KEY = USER_TORBOX_KEY || DEFAULT_TORBOX_KEY;

// Always regenerate the Comet config with the active key (so the key in the
// config always matches what we want — no hardcoded fallback surprises).
const cometConfig = {
  maxResultsPerResolution: 0,
  maxSize: 0,
  cachedOnly: true,
  sortCachedUncachedTogether: false,
  removeTrash: true,
  resultFormat: ['all'],
  enableTorrent: false,
  deduplicateStreams: true,
  scrapeDebridAccountTorrents: false,
  debridStreamProxyPassword: '',
  languages: { required: [], allowed: [], exclude: [], preferred: [] },
  resolutions: {},
  options: { remove_ranks_under: -10000000000, allow_english_in_languages: false, remove_unknown_languages: false },
  debridServices: [{ service: 'torbox', apiKey: ACTIVE_TORBOX_KEY }],
};
const COMET_CONFIG_B64 = Buffer.from(JSON.stringify(cometConfig), 'utf8').toString('base64');
if (USER_TORBOX_KEY && USER_TORBOX_KEY !== DEFAULT_TORBOX_KEY) {
  console.log('[Comet] Using user-supplied TorBox API key from TORBOX_API_KEY env var');
} else {
  console.log('[Comet] Using default TorBox key: ' + DEFAULT_TORBOX_KEY.slice(0, 8) + '...');
}

const COMET_BASE = 'https://comet.feels.legal/' + COMET_CONFIG_B64;

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';

// Max streams to return per request. Default = 50 (capped to keep Stremio
// picker usable on older devices). Override with COMET_MAX_STREAMS=200 or
// COMET_MAX_STREAMS=0 for unlimited.
const MAX_STREAMS = process.env.COMET_MAX_STREAMS !== undefined
  ? parseInt(process.env.COMET_MAX_STREAMS, 10) || Infinity
  : 50;

// ─── HTTP helper (no external deps) ───────────────────────────────────────
function fetchJson(url, { headers = {}, timeout = 30000 } = {}) {
  return new Promise((resolve, reject) => {
    const u = new URL(url);
    const lib = u.protocol === 'http:' ? http : https;
    const req = lib.request({
      hostname: u.hostname,
      path: u.pathname + u.search,
      method: 'GET',
      headers: {
        'User-Agent': UA,
        'Accept': 'application/json, text/plain, */*',
        ...headers,
      },
      timeout,
    }, (res) => {
      // Comet follows redirects internally for JSON responses — but if we
      // ever get a 3xx, follow once.
      if (res.statusCode >= 300 && res.statusCode < 400 && res.headers.location) {
        const next = new URL(res.headers.location, url).href;
        return resolve(fetchJson(next, { headers, timeout }));
      }
      const chunks = [];
      res.on('data', c => chunks.push(c));
      res.on('end', () => {
        const body = Buffer.concat(chunks).toString('utf8');
        if (res.statusCode < 200 || res.statusCode >= 300) {
          return reject(new Error('HTTP ' + res.statusCode + ' from ' + url + ': ' + body.slice(0, 200)));
        }
        try {
          resolve(JSON.parse(body));
        } catch (e) {
          reject(new Error('JSON parse failed from ' + url + ': ' + e.message));
        }
      });
    });
    req.on('error', reject);
    req.on('timeout', () => { req.destroy(new Error('timeout')); });
    req.end();
  });
}

// ─── TMDB → IMDB helper ──────────────────────────────────────────────────
// Many Stremio addons (Nuvio family) pass a TMDB id rather than an IMDB id.
// Comet strictly requires IMDB ids. If the caller passes a TMDB numeric id,
// we resolve it to IMDB via TMDB's API.
async function tmdbToImdb(tmdbId, type) {
  if (!tmdbId) return null;
  // Already an IMDB id?
  if (/^tt\d+$/.test(String(tmdbId))) return String(tmdbId);
  const endpoint = type === 'tv' ? 'tv' : 'movie';
  const url = 'https://api.themoviedb.org/3/' + endpoint + '/' + tmdbId +
    '?api_key=' + TMDB_API_KEY + '&external_source=imdb_id';
  try {
    const d = await fetchJson(url);
    // TMDB has separate endpoints for external_ids; fetch them:
    const idsUrl = 'https://api.themoviedb.org/3/' + endpoint + '/' + tmdbId +
      '/external_ids?api_key=' + TMDB_API_KEY;
    const ids = await fetchJson(idsUrl);
    if (ids.imdb_id) return ids.imdb_id;
    // For movies, d.imdb_id is sometimes returned directly on the main endpoint.
    if (d.imdb_id) return d.imdb_id;
    return null;
  } catch (e) {
    return null;
  }
}

// ─── Animation detection (TMDB genre id 16 = Animation) ───────────────────
// Anime / cartoon / animated movies keep AI-upscales near the top (fans
// often want those releases). Live-action movies / series / kdramas bury
// AI-upscales below the top 50 (use a much larger penalty).
async function detectIsAnimation(imdbId, type) {
  if (!imdbId) return false;
  const endpoint = type === 'tv' ? 'tv' : 'movie';
  try {
    // Get the TMDB id from the imdb id via the /find endpoint.
    const findUrl = 'https://api.themoviedb.org/3/find/' + imdbId +
      '?api_key=' + TMDB_API_KEY + '&external_source=imdb_id';
    const findData = await fetchJson(findUrl);
    const results = type === 'tv' ? findData.tv_results : findData.movie_results;
    if (!results || results.length === 0) return false;
    const tmdbId = results[0].id;
    if (!tmdbId) return false;
    // Fetch full metadata (genres + origin country for anime detection).
    const metaUrl = 'https://api.themoviedb.org/3/' + endpoint + '/' + tmdbId +
      '?api_key=' + TMDB_API_KEY;
    const meta = await fetchJson(metaUrl);
    const genres = meta.genres || [];
    const genreIds = genres.map(g => g.id);
    const genreNames = genres.map(g => (g.name || '').toLowerCase()).join('|');
    // TMDB genre id 16 = Animation (covers both anime and Western cartoons)
    if (genreIds.includes(16)) return true;
    // Defensive: also check by name in case genre id changes
    if (genreNames.includes('animation')) return true;
    // Japanese origin + Sci-Fi & Fantasy genre is also commonly anime
    const isJapanese = (meta.origin_country || []).includes('JP') ||
      (meta.original_language === 'ja');
    if (isJapanese && (genreIds.includes(10765) || genreIds.includes(10759) ||
        genreIds.includes(18) || genreIds.includes(28))) {
      // Japanese TV with Anime-typical genres — also treat as animation
      // (covers some anime that TMDB forgets to tag as Animation)
      return true;
    }
    return false;
  } catch (e) {
    return false;
  }
}

// ─── Quality / codec / language parsing from filename ─────────────────────
// Word-boundary matching so "448Kbps" doesn't false-match "8K", "il68k.mkv"
// doesn't false-match "8K", and "1440p" doesn't false-match "4K" etc.
function hasToken(haystack, token) {
  // token must be surrounded by start-of-string OR a non-alphanumeric char,
  // and end-of-string OR a non-alphanumeric char. (We treat _ as a boundary
  // here, unlike regex \b which treats _ as a word char — this is what we
  // want for filenames like "Movie_8K.mkv" where _ is a real separator.)
  const re = new RegExp('(?:^|[^a-z0-9])' + token.replace(/[.*+?^${}()|[\]\\]/g, '\\$&') + '(?:[^a-z0-9]|$)', 'i');
  return re.test(haystack);
}
function parseQuality(filename) {
  const f = (filename || '').toLowerCase();
  if (hasToken(f, '4320p') || hasToken(f, '8k') || hasToken(f, 'uhd-8k')) return '4320p';
  if (hasToken(f, '2160p') || hasToken(f, '4k') || hasToken(f, 'uhd')) return '2160p';
  if (hasToken(f, '1440p')) return '1440p';
  if (hasToken(f, '1080p')) return '1080p';
  if (hasToken(f, '720p')) return '720p';
  if (hasToken(f, '576p')) return '576p';
  if (hasToken(f, '480p')) return '480p';
  return null;
}

function parseCodec(filename) {
  const f = (filename || '').toLowerCase();
  if (f.includes('x265') || f.includes('h265') || f.includes('hevc')) return 'HEVC';
  if (f.includes('av1')) return 'AV1';
  if (f.includes('x264') || f.includes('h264') || f.includes('avc')) return 'H264';
  return '';
}

function parseSource(filename) {
  const f = (filename || '').toLowerCase();
  if (f.includes('remux')) return 'REMUX';
  if (f.includes('blu-ray') || f.includes('bluray')) return 'BluRay';
  if (f.includes('web-dl') || f.includes('webdl')) return 'WEB-DL';
  if (f.includes('webrip')) return 'WEBRip';
  if (f.includes('hdtv')) return 'HDTV';
  return '';
}

function parseHdrFlags(filename) {
  const f = (filename || '').toLowerCase();
  const tags = [];
  if (f.includes('dolby vision') || f.includes('dolbyvision') || /\bdovi\b/.test(f) || /\bdv\b/.test(f)) tags.push('DV');
  if (f.includes('hdr10plus') || f.includes('hdr10+')) tags.push('HDR10+');
  else if (f.includes('hdr10')) tags.push('HDR10');
  else if (f.includes('hdr') && !tags.includes('DV')) tags.push('HDR');
  if (f.includes('10bit') || f.includes('10-bit')) tags.push('10bit');
  return tags.join(' ');
}

function parseAudio(filename) {
  const f = (filename || '').toLowerCase();
  const tags = [];
  if (f.includes('truehd')) tags.push('TrueHD');
  if (f.includes('atmos')) tags.push('Atmos');
  if (f.includes('ddp') || f.includes('e-ac3') || f.includes('eac3')) tags.push('DDP');
  if (f.includes('dts-hd') || f.includes('dts-hdma') || f.includes('dts hd')) tags.push('DTS-HD');
  if (f.includes('dts')) tags.push('DTS');
  if (f.includes('aac')) tags.push('AAC');
  if (f.includes('5.1')) tags.push('5.1');
  if (f.includes('7.1')) tags.push('7.1');
  return tags.slice(0, 3).join(' ');
}

function parseLanguage(filename) {
  const f = (filename || '').toLowerCase();
  const langs = [];
  if (f.includes('hindi')) langs.push('Hindi');
  if (f.includes('tamil')) langs.push('Tamil');
  if (f.includes('telugu')) langs.push('Telugu');
  if (f.includes('malayalam')) langs.push('Malayalam');
  if (f.includes('korean')) langs.push('Korean');
  if (f.includes('japanese') || f.includes('jpn')) langs.push('Japanese');
  if (f.includes('chinese') || f.includes('mandarin')) langs.push('Chinese');
  if (f.includes('spanish') || /\bespa/.test(f)) langs.push('Spanish');
  if (f.includes('french') || /\bfra\b/.test(f) || /\bfr\b/.test(f)) langs.push('French');
  if (f.includes('german') || /\bger\b/.test(f) || /\bdeu\b/.test(f)) langs.push('German');
  if (f.includes('italian') || /\bita\b/.test(f)) langs.push('Italian');
  return langs.slice(0, 3).join('+');
}

// ─── Ranking — pick the genuinely highest-quality streams ────────────────
// Score weights:
//   • Resolution  : 1000s     (4320p=4000, 2160p=3000, 1440p=2500, 1080p=2000…)
//   • Source      : 100s      (REMUX=500, BluRay Encode=400, WEB-DL=300, …)
//   • HDR tier    : 10s       (DV+HDR10 hybrid=80, DV=70, HDR10+=60, …)
//   • Audio tier  : 1s        (TrueHD Atmos 7.1=30, TrueHD=22, DDP 5.1=12, …)
//   • Codec       : fraction   (HEVC=50, AV1=45, H264=30)
//   • AI Upscale  : -1500 penalty — fake 8K AI upscales lose to real 4K REMUX.
//
// Result: 4K REMUX BluRay DV+HDR10 HEVC TrueHD Atmos 7.1 ranks above any
// "8K AI Upscale" WEB-DL — which is what people actually want for "highest
// quality". Real 8K REMUX (e.g. genuine 4320p BluRay REMUX) still wins.
function rankStream(stream, ctx = {}) {
  // ctx.isAnimation: true if title is anime/cartoon/animated.
  //   For animation: AI-upscales get a small penalty (-1500) so they can
  //     still appear near the top (anime fans often want them).
  //   For live-action: AI-upscales get a huge penalty (-5000) so they're
  //     buried below the top 50 — real 4K REMUX always wins for movies/series.
  const fn = (stream.behaviorHints && stream.behaviorHints.filename) || stream.title || stream.name || '';
  const f = (fn || '').toLowerCase();
  const q = parseQuality(fn) || '';
  const c = parseCodec(fn);
  const s = parseSource(fn);
  const hdr = parseHdrFlags(fn);
  const audio = parseAudio(fn);
  let score = 0;

  // Resolution tier (1000s)
  if (q === '4320p') score += 4000;
  else if (q === '2160p') score += 3000;
  else if (q === '1440p') score += 2500;
  else if (q === '1080p') score += 2000;
  else if (q === '720p') score += 1500;
  else if (q === '576p') score += 1000;
  else if (q === '480p') score += 800;
  else score += 600;

  // Source tier (100s). REMUX = untouched Blu-ray (best possible encode).
  if (s === 'REMUX') score += 500;
  else if (s === 'BluRay') score += 400;
  else if (s === 'WEB-DL') score += 300;
  else if (s === 'WEBRip') score += 200;
  else if (s === 'HDTV') score += 100;

  // HDR tier (10s). Hybrid DV+HDR10 is best (plays on all HDR displays).
  if (hdr.includes('DV') && (hdr.includes('HDR10+') || hdr.includes('HDR10'))) score += 80;
  else if (hdr.includes('DV')) score += 70;
  else if (hdr.includes('HDR10+')) score += 60;
  else if (hdr.includes('HDR10')) score += 50;
  else if (hdr.includes('HDR')) score += 40;
  if (hdr.includes('10bit')) score += 10;

  // Audio tier (1s). Lossless > lossy. Atmos object-audio is best.
  if (audio.includes('TrueHD') && audio.includes('Atmos') && audio.includes('7.1')) score += 30;
  else if (audio.includes('TrueHD') && audio.includes('Atmos')) score += 28;
  else if (audio.includes('TrueHD') && audio.includes('7.1')) score += 25;
  else if (audio.includes('TrueHD')) score += 22;
  else if (audio.includes('DTS-HD') && audio.includes('7.1')) score += 20;
  else if (audio.includes('DTS-HD')) score += 18;
  else if (audio.includes('Atmos')) score += 15;
  else if (audio.includes('DDP') && audio.includes('5.1')) score += 12;
  else if (audio.includes('DTS') && audio.includes('5.1')) score += 10;
  else if (audio.includes('AAC') && audio.includes('5.1')) score += 8;
  else if (audio.includes('AAC')) score += 5;

  // Codec (fraction). HEVC is best for HDR; AV1 is more efficient; H264 baseline.
  if (c === 'HEVC') score += 50;
  else if (c === 'AV1') score += 45;
  else if (c === 'H264') score += 30;

  // AI Upscale / RIFE penalty.
  // For animation (anime/cartoon): -1500 (still allow AI upscales near top —
  //   anime fans often want them).
  // For live-action (movies/series/kdramas): -5000 (bury below top 50 —
  //   real 4K REMUX should always win for live-action content).
  const upscalePenalty = ctx.isAnimation ? 1500 : 5000;
  if (hasToken(f, 'upscale') || hasToken(f, 'upscaled') || hasToken(f, 'ai-upscale')) {
    score -= upscalePenalty;
  }
  // RIFE / interpolated frame-rate manipulation = not real source quality.
  // Moderate penalty only — it's a quality enhancement, not a fake encode.
  if (hasToken(f, 'rife') || hasToken(f, 'interpol') || f.includes('60fps')) score -= 200;

  return score;
}

// ─── Main entry: getStreams ───────────────────────────────────────────────
// Args:
//   id      — IMDB id (ttXXXXX) or TMDB numeric id (will be resolved)
//   type    — 'movie' | 'tv' | 'series'
//   season  — episode season (only for tv)
//   episode — episode number (only for tv)
// Returns: Promise<StremioStream[]>
async function getStreams(id, type, season, episode) {
  if (!id) return [];
  const isMovie = (type || 'movie') !== 'tv' && type !== 'series';

  // Resolve IMDB id (Comet requires IMDB ids).
  let imdbId;
  if (/^tt\d+$/.test(String(id))) {
    imdbId = String(id);
  } else {
    imdbId = await tmdbToImdb(id, isMovie ? 'movie' : 'tv');
    if (!imdbId) {
      console.error('[Comet] could not resolve TMDB→IMDB for ' + id);
      return [];
    }
  }

  const streamPath = isMovie
    ? '/stream/movie/' + imdbId + '.json'
    : '/stream/series/' + imdbId + ':' + (season || 1) + ':' + (episode || 1) + '.json';
  const url = COMET_BASE + streamPath;

  console.log('[Comet] GET ' + url.slice(0, 200) + (url.length > 200 ? '...' : ''));

  let data;
  try {
    data = await fetchJson(url, { timeout: 30000 });
  } catch (e) {
    console.error('[Comet] fetch failed: ' + e.message);
    return [];
  }
  const raw = Array.isArray(data.streams) ? data.streams : [];
  if (raw.length === 0) {
    console.log('[Comet] no streams returned');
    return [];
  }
  console.log('[Comet] ' + raw.length + ' raw streams from backend');

  // Detect if this title is animated (anime/cartoon) so we know whether
  // to bury AI upscales below the top 50 or let them stay near the top.
  // Animation fans often want AI upscales; live-action fans never do.
  const isAnimation = await detectIsAnimation(imdbId, isMovie ? 'movie' : 'tv');
  console.log('[Comet] isAnimation=' + isAnimation +
    ' (TMDB genre id 16 — anime/cartoon/animated). AI-upscale penalty = ' +
    (isAnimation ? 1500 : 5000));

  // Deduplicate by infohash (Comet sometimes returns the same torrent under
  // multiple "names" — we keep the first instance per infohash).
  const byInfohash = new Map();
  for (const s of raw) {
    const bg = (s.behaviorHints && s.behaviorHints.bingeGroup) || '';
    const hashMatch = bg.match(/([0-9a-f]{40})$/i);
    const key = hashMatch ? hashMatch[1].toLowerCase() : (s.url || JSON.stringify(s));
    if (!byInfohash.has(key)) byInfohash.set(key, s);
  }
  const unique = Array.from(byInfohash.values());
  console.log('[Comet] ' + unique.length + ' after dedup by infohash');

  // Filter: drop any stream whose URL is NOT https (would only happen if
  // Comet somehow returned a magnet — which the config forbids, but check
  // defensively).
  const httpsOnly = unique.filter(s => (s.url || '').startsWith('https://'));
  console.log('[Comet] ' + httpsOnly.length + ' after https:// filter');

  // Rank by quality descending.
  const ranked = httpsOnly
    .map(s => ({ s, score: rankStream(s, { isAnimation }) }))
    .sort((a, b) => b.score - a.score)
    .map(x => x.s);

  // Build final Stremio stream objects.
  const out = [];
  const seenQualities = new Set();
  for (const s of ranked) {
    if (out.length >= MAX_STREAMS) break;
    const filename = (s.behaviorHints && s.behaviorHints.filename) || '';
    const quality = parseQuality(filename) || (s.name || '').match(/(\d{3,4}p)/)?.[1] || '1080p';
    const codec = parseCodec(filename);
    const source = parseSource(filename);
    const hdr = parseHdrFlags(filename);
    const audio = parseAudio(filename);
    const lang = parseLanguage(filename);
    const bg = (s.behaviorHints && s.behaviorHints.bingeGroup) || '';

    // Compose name like: "[TB] Comet 4K HDR | HEVC | Hindi"
    const parts = [];
    if (quality === '4320p') parts.push('8K');
    else if (quality === '2160p') parts.push('4K');
    else parts.push(quality.toUpperCase());
    if (hdr) parts.push(hdr.split(' ').slice(0, 2).join('+'));
    if (codec) parts.push(codec);
    if (lang) parts.push(lang);
    if (audio) parts.push(audio);
    if (source === 'REMUX') parts.push('REMUX');
    const nameLine = '[TB] ' + PROVIDER_NAME + ' ' + parts.join(' | ');

    // Title: full release filename so the user can pick the best encode.
    const titleLine = filename || s.title || s.name || (quality + ' ' + codec);

    out.push({
      name: nameLine,
      title: titleLine,
      url: s.url, // HTTPS — Comet playback URL 302s to tb-cdn.io CDN
      quality: quality,
      type: 'video/mkv',
      behaviorHints: {
        bingeGroup: bg || ('comet|torbox|' + imdbId + '|' + (out.length)),
        filename: filename,
        notWebReady: false, // HTTPS, web-playable directly
      },
    });
    seenQualities.add(quality);
  }
  console.log('[Comet] returning ' + out.length + ' streams' + (MAX_STREAMS === Infinity ? '' : ' (cap=' + MAX_STREAMS + ')'));
  return out;
}

// ─── CLI test mode ────────────────────────────────────────────────────────
if (require.main === module) {
  (async () => {
    const args = process.argv.slice(2);
    if (args.length === 0) {
      console.log('Usage: node comet_all_in_one.js <imdbId|tmdbId> <movie|tv|series> [season] [episode]');
      console.log('Examples:');
      console.log('  node comet_all_in_one.js tt37287335 movie');
      console.log('  node comet_all_in_one.js tt1375666 movie');
      console.log('  node comet_all_in_one.js tt0903747 series 1 1');
      process.exit(0);
    }
    const id = args[0];
    const type = args[1] || 'movie';
    const season = args[2] ? parseInt(args[2], 10) : null;
    const episode = args[3] ? parseInt(args[3], 10) : null;

    console.log('=== Comet (PenguPlay /direct/experimental/) ===');
    console.log('Query: id=' + id + ' type=' + type +
      (season ? ' S' + season + 'E' + episode : ''));
    console.log('');

    const streams = await getStreams(id, type, season, episode);
    console.log('\n===== STREAMS (' + streams.length + ') =====');
    streams.forEach((s, i) => {
      console.log('[' + i + '] ' + s.name + '  (' + s.quality + ')');
      console.log('    title: ' + (s.title || '').slice(0, 150));
      console.log('    url:   ' + (s.url || '').slice(0, 200) + (s.url && s.url.length > 200 ? '...' : ''));
    });

    // Verify the top stream actually 302s to a real tb-cdn.io HTTPS URL.
    if (streams.length > 0) {
      console.log('\n===== HTTPS REDIRECT VERIFICATION (top stream) =====');
      const top = streams[0];
      console.log('Top stream name: ' + top.name);
      console.log('Following: ' + top.url.slice(0, 200) + (top.url.length > 200 ? '...' : ''));
      let cur = top.url;
      for (let i = 0; i < 4; i++) {
        try {
          const u = new URL(cur);
          const lib = u.protocol === 'http:' ? http : https;
          const r = await new Promise((resolve, reject) => {
            const req = lib.request({
              hostname: u.hostname, path: u.pathname + u.search, method: 'GET',
              headers: { 'User-Agent': UA, 'Range': 'bytes=0-1' },
            }, (res) => {
              // We do NOT consume the body — 1 byte range request, immediately destroy.
              res.destroy();
              resolve(res);
            });
            req.on('error', reject);
            req.end();
          });
          console.log('  [' + i + '] ' + r.statusCode + ' ' + r.statusMessage);
          console.log('      content-type:    ' + r.headers['content-type']);
          console.log('      accept-ranges:    ' + r.headers['accept-ranges']);
          console.log('      content-range:    ' + r.headers['content-range']);
          console.log('      content-length:   ' + r.headers['content-length']);
          console.log('      content-disp:     ' + (r.headers['content-disposition'] || '').slice(0, 100));
          const loc = r.headers['location'];
          if (loc) {
            console.log('      → Location:       ' + loc.slice(0, 200) + (loc.length > 200 ? '...' : ''));
            cur = new URL(loc, cur).href;
            continue;
          }
          console.log('  ✓ FINAL — direct HTTPS stream (no more redirects)');
          break;
        } catch (e) {
          console.log('  error: ' + e.message);
          break;
        }
      }
    }
  })();
}

module.exports = {
  getStreams,
  tmdbToImdb,
  detectIsAnimation,
  parseQuality,
  parseCodec,
  parseSource,
  parseHdrFlags,
  parseAudio,
  parseLanguage,
  rankStream,
  // expose internals for tests
  _internals: {
    COMET_BASE,
    PENGUPLAY_COMET_CONFIG_B64,
    fetchJson,
    MAX_STREAMS,
  },
};
