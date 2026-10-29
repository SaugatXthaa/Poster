// MovieBox — PenguPlay-INDEPENDENT multi-source aggregator.
// ============================================================================
// This provider combines 3 direct scrapers (desiflix, vegamovies, hdhub4u)
// into a single "MovieBox" provider. NO PenguPlay dependency, NO auth_token.
//
// WHY THIS EXISTS
//   The previous moviebox.js used PenguPlay's `source_moviebox` with an
//   auth_token. The user requested a PenguPlay-independent version. Since
//   the original MovieBox backend (stremio-moviebox-1.onrender.com) is
//   suspended, we now aggregate the 3 working PenguPlay-independent
//   providers (desiflix, vegamovies, hdhub4u) under the "MovieBox" name.
//
// ARCHITECTURE
//   Nuvio →  moviebox.js (this file)
//            → calls desiflix.js + vegamovies.js + hdhub4u.js IN PARALLEL
//            → combines results, dedupes by URL, ranks by quality
//            → returns up to 50 streams
//   Each sub-provider scrapes its own site directly (no PenguPlay, no auth).
//
// PURE NODE.JS — works on Render free tier with cheerio + crypto-js.
// ============================================================================

'use strict';

const https = require('https');
const http = require('http');
const path = require('path');

const PROVIDER_NAME = 'MovieBox';
const TMDB_API_KEY = '8476a7ab80ad76f0936744df0430e67c';

// Sub-providers to aggregate (all PenguPlay-independent, all no-auth)
const PROVIDERS_DIR = path.resolve(__dirname, '..', 'allinone-nuvio-repo', 'providers');
const SUB_PROVIDERS = ['desiflix', 'vegamovies', 'hdhub4u'];

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';

const MAX_STREAMS = process.env.MOVIEBOX_MAX_STREAMS !== undefined
  ? parseInt(process.env.MOVIEBOX_MAX_STREAMS, 10) || Infinity
  : 50;

// ─── HTTP helper ───────────────────────────────────────────────────────────
function fetchJson(url, { headers = {}, timeout = 30000 } = {}) {
  return new Promise((resolve, reject) => {
    const u = new URL(url);
    const lib = u.protocol === 'http:' ? http : https;
    const req = lib.request({
      hostname: u.hostname,
      path: u.pathname + u.search,
      method: 'GET',
      headers: { 'User-Agent': UA, 'Accept': 'application/json', ...headers },
      timeout,
    }, (res) => {
      if (res.statusCode >= 300 && res.statusCode < 400 && res.headers.location) {
        return resolve(fetchJson(new URL(res.headers.location, url).href, { headers, timeout }));
      }
      const chunks = [];
      res.on('data', c => chunks.push(c));
      res.on('end', () => {
        const body = Buffer.concat(chunks).toString('utf8');
        if (res.statusCode < 200 || res.statusCode >= 300) {
          return reject(new Error('HTTP ' + res.statusCode));
        }
        try { resolve(JSON.parse(body)); }
        catch (e) { reject(new Error('JSON parse failed: ' + e.message)); }
      });
    });
    req.on('error', reject);
    req.on('timeout', () => { req.destroy(new Error('timeout')); });
    req.end();
  });
}

// ─── TMDB → IMDB resolver ──────────────────────────────────────────────────
async function tmdbToImdb(tmdbId, type) {
  if (!tmdbId) return null;
  if (/^tt\d+$/.test(String(tmdbId))) return String(tmdbId);
  const endpoint = type === 'tv' ? 'tv' : 'movie';
  try {
    const ids = await fetchJson(
      'https://api.themoviedb.org/3/' + endpoint + '/' + tmdbId +
      '/external_ids?api_key=' + TMDB_API_KEY
    );
    if (ids.imdb_id) return ids.imdb_id;
    const d = await fetchJson(
      'https://api.themoviedb.org/3/' + endpoint + '/' + tmdbId + '?api_key=' + TMDB_API_KEY
    );
    return d.imdb_id || null;
  } catch (e) { return null; }
}

// ─── Filename parsers ──────────────────────────────────────────────────────
function hasToken(haystack, token) {
  const re = new RegExp('(?:^|[^a-z0-9])' + token.replace(/[.*+?^${}()|[\]\\]/g, '\\$&') + '(?:[^a-z0-9]|$)', 'i');
  return re.test(haystack);
}
function parseQuality(filename) {
  const f = (filename || '').toLowerCase();
  if (hasToken(f, '4320p') || hasToken(f, '8k')) return '4320p';
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
  if (f.includes('x265') || f.includes('h265') || f.includes('hevc') || f.includes('h.265')) return 'HEVC';
  if (f.includes('av1')) return 'AV1';
  if (f.includes('x264') || f.includes('h264') || f.includes('avc') || f.includes('h.264')) return 'H264';
  return '';
}
function parseSource(filename) {
  const f = (filename || '').toLowerCase();
  if (f.includes('remux')) return 'REMUX';
  if (f.includes('blu-ray') || f.includes('bluray')) return 'BluRay';
  if (f.includes('web-dl') || f.includes('webdl') || f.includes('web.dl')) return 'WEB-DL';
  if (f.includes('webrip')) return 'WEBRip';
  if (f.includes('hdtv')) return 'HDTV';
  return '';
}
function parseHdrFlags(filename) {
  const f = (filename || '').toLowerCase();
  const t = [];
  if (f.includes('dolby vision') || f.includes('dolbyvision') || /\bdovi\b/.test(f) || /\bdv\b/.test(f)) t.push('DV');
  if (f.includes('hdr10plus') || f.includes('hdr10+')) t.push('HDR10+');
  else if (f.includes('hdr10')) t.push('HDR10');
  else if (f.includes('hdr') && !t.includes('DV')) t.push('HDR');
  if (f.includes('10bit') || f.includes('10-bit')) t.push('10bit');
  return t.join(' ');
}
function parseAudio(filename) {
  const f = (filename || '').toLowerCase();
  const t = [];
  if (f.includes('truehd')) t.push('TrueHD');
  if (f.includes('atmos')) t.push('Atmos');
  if (f.includes('ddp') || f.includes('e-ac3') || f.includes('eac3')) t.push('DDP');
  if (f.includes('dts-hd') || f.includes('dts hd') || f.includes('dts-hdma')) t.push('DTS-HD');
  else if (f.includes('dts')) t.push('DTS');
  if (f.includes('aac')) t.push('AAC');
  if (f.includes('5.1')) t.push('5.1');
  if (f.includes('7.1')) t.push('7.1');
  return t.slice(0, 3).join(' ');
}
function parseLanguage(filename) {
  const f = (filename || '').toLowerCase();
  const l = [];
  if (f.includes('hindi')) l.push('Hindi');
  if (f.includes('tamil')) l.push('Tamil');
  if (f.includes('telugu')) l.push('Telugu');
  if (f.includes('malayalam')) l.push('Malayalam');
  if (f.includes('kannada')) l.push('Kannada');
  if (f.includes('korean')) l.push('Korean');
  if (f.includes('japanese') || /\bjpn\b/.test(f)) l.push('Japanese');
  if (f.includes('english') || /\beng\b/.test(f)) l.push('English');
  return l.slice(0, 3).join('+');
}

// ─── Ranking ────────────────────────────────────────────────────────────────
function rankStream(stream, ctx = {}) {
  const fn = (stream.behaviorHints && stream.behaviorHints.filename) ||
             stream.title || stream.name || '';
  const f = (fn || '').toLowerCase();
  const q = parseQuality(fn) || '';
  const c = parseCodec(fn);
  const s = parseSource(fn);
  const hdr = parseHdrFlags(fn);
  const audio = parseAudio(fn);
  let score = 0;

  if (q === '4320p') score += 4000;
  else if (q === '2160p') score += 3000;
  else if (q === '1440p') score += 2500;
  else if (q === '1080p') score += 2000;
  else if (q === '720p') score += 1500;
  else if (q === '576p') score += 1000;
  else if (q === '480p') score += 800;
  else score += 600;

  if (s === 'REMUX') score += 500;
  else if (s === 'BluRay') score += 400;
  else if (s === 'WEB-DL') score += 300;
  else if (s === 'WEBRip') score += 200;
  else if (s === 'HDTV') score += 100;

  if (hdr.includes('DV')) score += 70;
  if (hdr.includes('HDR10+')) score += 60;
  if (hdr.includes('HDR10')) score += 50;
  if (hdr.includes('HDR')) score += 40;
  if (hdr.includes('10bit')) score += 10;

  if (audio.includes('TrueHD') && audio.includes('Atmos')) score += 28;
  else if (audio.includes('TrueHD')) score += 22;
  else if (audio.includes('DTS-HD')) score += 18;
  else if (audio.includes('Atmos')) score += 15;
  else if (audio.includes('DDP') && audio.includes('5.1')) score += 12;
  else if (audio.includes('AAC') && audio.includes('5.1')) score += 8;

  if (c === 'HEVC') score += 50;
  else if (c === 'AV1') score += 45;
  else if (c === 'H264') score += 30;

  if (!ctx.isAnimation) {
    if (hasToken(f, 'upscale') || hasToken(f, 'upscaled')) score -= 5000;
  }
  return score;
}

// ─── Animation detection ───────────────────────────────────────────────────
async function detectIsAnimation(imdbId, type) {
  if (!imdbId) return false;
  const endpoint = type === 'tv' ? 'tv' : 'movie';
  try {
    const findUrl = 'https://api.themoviedb.org/3/find/' + imdbId +
      '?api_key=' + TMDB_API_KEY + '&external_source=imdb_id';
    const findData = await fetchJson(findUrl);
    const results = type === 'tv' ? findData.tv_results : findData.movie_results;
    if (!results || results.length === 0) return false;
    const tmdbId = results[0].id;
    if (!tmdbId) return false;
    const metaUrl = 'https://api.themoviedb.org/3/' + endpoint + '/' + tmdbId +
      '?api_key=' + TMDB_API_KEY;
    const meta = await fetchJson(metaUrl);
    const genres = meta.genres || [];
    const genreIds = genres.map(g => g.id);
    return genreIds.includes(16);
  } catch (e) { return false; }
}

// ─── Load a sub-provider (try multiple paths) ───────────────────────────────
function loadSubProvider(name) {
  const paths = [
    path.join(PROVIDERS_DIR, name + '.js'),
    path.join(__dirname, name + '.js'),
    path.join(__dirname, '..', 'allinone-nuvio-repo', 'providers', name + '.js'),
    path.join(__dirname, '..', 'scripts', name + '_all_in_one.js'),
  ];
  for (const p of paths) {
    try {
      const mod = require(p);
      if (mod && typeof mod.getStreams === 'function') return mod;
    } catch (e) { /* try next */ }
  }
  return null;
}

// ─── Main entry: getStreams(id, type, season, episode) ─────────────────────
async function getStreams(id, type, season, episode) {
  if (!id) return [];
  const isMovie = (type || 'movie') !== 'tv' && type !== 'series';

  // Resolve IMDB id
  let imdbId;
  if (/^tt\d+$/.test(String(id))) {
    imdbId = String(id);
  } else {
    imdbId = await tmdbToImdb(id, isMovie ? 'movie' : 'tv');
    if (!imdbId) {
      console.error('[MovieBox] could not resolve TMDB→IMDB for ' + id);
      return [];
    }
  }

  // Detect animation (for ranking)
  const isAnimation = await detectIsAnimation(imdbId, isMovie ? 'movie' : 'tv');
  console.log('[MovieBox] imdbId=' + imdbId + ' isAnimation=' + isAnimation + ' (aggregating: ' + SUB_PROVIDERS.join(', ') + ')');

  // Call ALL sub-providers IN PARALLEL (with 20s timeout each)
  const results = await Promise.allSettled(
    SUB_PROVIDERS.map(async (name) => {
      const provider = loadSubProvider(name);
      if (!provider) {
        console.log('[MovieBox] ' + name + ': could not load');
        return [];
      }
      try {
        const streams = await Promise.race([
          provider.getStreams(imdbId, isMovie ? 'movie' : 'tv', season, episode),
          new Promise((_, rej) => setTimeout(() => rej(new Error('20s timeout')), 20000))
        ]);
        console.log('[MovieBox] ' + name + ': ' + (streams || []).length + ' streams');
        return streams || [];
      } catch (e) {
        console.log('[MovieBox] ' + name + ': err ' + (e.message || '').slice(0, 80));
        return [];
      }
    })
  );

  // Combine all results
  let combined = [];
  for (const r of results) {
    if (r.status === 'fulfilled' && Array.isArray(r.value)) {
      combined = combined.concat(r.value);
    }
  }
  console.log('[MovieBox] combined: ' + combined.length + ' streams');

  // Dedup by URL
  const seen = new Set();
  const deduped = combined.filter(s => {
    if (!s.url) return false;
    if (seen.has(s.url)) return false;
    seen.add(s.url);
    return true;
  });
  console.log('[MovieBox] deduped: ' + deduped.length + ' streams');

  // Rank
  const ranked = deduped
    .map(s => ({ s, score: rankStream(s, { isAnimation }) }))
    .sort((a, b) => b.score - a.score)
    .map(x => x.s);

  // Build output — keep the [MB] MovieBox prefix for consistency with old version
  const out = [];
  for (const s of ranked) {
    if (out.length >= MAX_STREAMS) break;
    const filename = (s.behaviorHints && s.behaviorHints.filename) || s.title || s.name || '';
    const quality = parseQuality(filename) || '1080p';
    const codec = parseCodec(filename);
    const source = parseSource(filename);
    const hdr = parseHdrFlags(filename);
    const audio = parseAudio(filename);
    const lang = parseLanguage(filename);

    // Compose name — keep "[MB] MovieBox" prefix for UI consistency
    const parts = [];
    if (quality === '4320p') parts.push('8K');
    else if (quality === '2160p') parts.push('4K');
    else parts.push(quality.toUpperCase());
    if (hdr) parts.push(hdr.split(' ').slice(0, 2).join('+'));
    if (codec) parts.push(codec);
    if (lang) parts.push(lang);
    if (audio) parts.push(audio);
    if (source === 'REMUX') parts.push('REMUX');
    const nameLine = '[MB] ' + PROVIDER_NAME + ' ' + parts.join(' | ');

    out.push({
      name: nameLine,
      title: s.title || filename,
      url: s.url,
      quality: quality,
      type: s.type || 'video/mkv',
      headers: s.headers || { 'User-Agent': UA },
      provider: PROVIDER_NAME,
      behaviorHints: s.behaviorHints || {
        bingeGroup: 'moviebox-indep|' + imdbId + '|' + out.length,
        filename: filename,
        notWebReady: false,
      },
    });
  }
  console.log('[MovieBox] returning ' + out.length + ' streams');
  return out;
}

// ─── CLI test ───────────────────────────────────────────────────────────────
if (require.main === module) {
  (async () => {
    const args = process.argv.slice(2);
    if (args.length === 0) {
      console.log('Usage: node moviebox.js <imdbId|tmdbId> <movie|tv|series> [season] [episode]');
      console.log('Aggregates: ' + SUB_PROVIDERS.join(', ') + ' (all PenguPlay-independent, no auth)');
      process.exit(0);
    }
    const streams = await getStreams(args[0], args[1] || 'movie', args[2], args[3]);
    console.log('\n' + streams.length + ' streams:');
    streams.slice(0, 10).forEach((s, i) => {
      console.log('[' + i + '] ' + s.name + ' (' + s.quality + ')');
      console.log('    ' + (s.url || '').slice(0, 200));
    });
  })();
}

if (typeof module !== 'undefined' && module.exports) {
  module.exports = { getStreams };
} else {
  global.getStreams = getStreams;
}
