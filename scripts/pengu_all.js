// PenguAll — Maximum-coverage HTTPS stream provider aggregating ALL upstream
// addon sources. This provider enables every source the upstream addon supports
// (Arctic, MovieBox, 4KHDHub, MoviesDrives, VAPlayer, 2Peckle, HDHub4U,
// VegaMovies, Miruro, VidLink, VidFast, CineFreak, Cinejoy, KissKH, Anikoto)
// to return the maximum number of streams per title.
//
// USE CASE
//   Use this provider when you want the WIDEST selection of streams.
//   For specialized providers, use antarctica.js (Arctic-only) or
//   moviebox.js (MovieBox-only multi-audio).
//
// ARCHITECTURE (same as antarctica_v2 / moviebox_v2)
//   Nuvio →  pengu.uk/<config-with-auth_token+ALL-sources-on>/stream/<imdb>.json
//            → 200 with streams[] array (typically 80-200 streams per title)
//            → each stream.url is HTTPS on pengu.uk/direct/... or pengu.uk/hls/...
//            → player follows 307 redirect → real CDN (googleusercontent, R2, etc.)
//
// PURE NODE.JS — no Playwright, no Cloudflare solver, no browser.
// ============================================================================

'use strict';

const https = require('https');
const http = require('http');

const PROVIDER_NAME = 'PenguAll';
const TMDB_API_KEY = '8476a7ab80ad76f0936744df0430e67c';

const DEFAULT_AUTH_TOKEN = 'wuW_pplyXmYBInI8p3VwacEOCrlEspsWp8SZMi7E3_o';
const ACTIVE_AUTH_TOKEN = (process.env.PENGU_AUTH_TOKEN && process.env.PENGU_AUTH_TOKEN.trim()) || DEFAULT_AUTH_TOKEN;

// Build upstream config with ALL sources enabled
function buildUpstreamConfig() {
  return {
    auth_token: ACTIVE_AUTH_TOKEN,
    // Enable ALL sources for maximum coverage
    source_arctic: 'on',
    source_moviebox: 'on',
    source_4khdhub: 'on',
    source_moviesdrives: 'on',
    source_vaplayer: 'on',
    source_2peckle: 'on',
    source_hdhub4u: 'on',
    source_vegamovies: 'on',
    source_miruro: 'on',
    source_vidlink: 'on',
    source_vidfast: 'on',
    source_cinefreak: 'on',
    source_cinejoy: 'on',
    source_kisskh: 'on',
    source_anikoto: 'on',
    // All resolutions
    res_2160: 'on',
    res_1080: 'on',
    res_720: 'on',
    res_480: 'on',
    res_360: 'on',
  };
}

function buildUpstreamUrl(imdbId, isMovie, season, episode) {
  const cfg = buildUpstreamConfig();
  const cfgEnc = encodeURIComponent(JSON.stringify(cfg));
  const path = isMovie
    ? '/stream/movie/' + imdbId + '.json'
    : '/stream/series/' + imdbId + ':' + (season || 1) + ':' + (episode || 1) + '.json';
  return 'https://pengu.uk/' + cfgEnc + path;
}

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';

// Cap — higher than antarctica/moviebox since this is the "kitchen sink" provider
const MAX_STREAMS = process.env.PENGUALL_MAX_STREAMS !== undefined
  ? parseInt(process.env.PENGUALL_MAX_STREAMS, 10) || Infinity
  : 75;

// ─── HTTP helper ───────────────────────────────────────────────────────────
function fetchJson(url, { headers = {}, timeout = 30000 } = {}) {
  return new Promise((resolve, reject) => {
    const u = new URL(url);
    const lib = u.protocol === 'http:' ? http : https;
    const req = lib.request({
      hostname: u.hostname,
      path: u.pathname + u.search,
      method: 'GET',
      headers: { 'User-Agent': UA, 'Accept': 'application/json, text/plain, */*', ...headers },
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
          return reject(new Error('HTTP ' + res.statusCode + ': ' + body.slice(0, 200)));
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
    const genreNames = genres.map(g => (g.name || '').toLowerCase()).join('|');
    if (genreIds.includes(16)) return true;
    if (genreNames.includes('animation')) return true;
    const isJapanese = (meta.origin_country || []).includes('JP') ||
      (meta.original_language === 'ja');
    if (isJapanese && (genreIds.includes(10765) || genreIds.includes(10759) ||
        genreIds.includes(18) || genreIds.includes(28))) {
      return true;
    }
    return false;
  } catch (e) { return false; }
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
  if (hasToken(f, '360p')) return '360p';
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
  if (f.includes('blu-ray') || f.includes('bluray') || f.includes('bdremux')) return 'BluRay';
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
  if (f.includes('2.0')) t.push('2.0');
  return t.slice(0, 4).join(' ');
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
  if (f.includes('japanese') || /\bjpn\b/.test(f) || /\bsub\b/.test(f)) l.push('Japanese');
  if (f.includes('chinese') || f.includes('mandarin')) l.push('Chinese');
  if (f.includes('spanish') || /\bespa/.test(f)) l.push('Spanish');
  if (f.includes('french') || /\bfra\b/.test(f)) l.push('French');
  if (f.includes('german') || /\bger\b/.test(f) || /\bdeu\b/.test(f)) l.push('German');
  if (f.includes('italian') || /\bita\b/.test(f)) l.push('Italian');
  if (f.includes('english') || /\beng\b/.test(f)) l.push('English');
  return l.slice(0, 4).join('+');
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
  else if (q === '360p') score += 600;
  else score += 600;

  if (s === 'REMUX') score += 500;
  else if (s === 'BluRay') score += 400;
  else if (s === 'WEB-DL') score += 300;
  else if (s === 'WEBRip') score += 200;
  else if (s === 'HDTV') score += 100;

  if (hdr.includes('DV') && (hdr.includes('HDR10+') || hdr.includes('HDR10'))) score += 80;
  else if (hdr.includes('DV')) score += 70;
  else if (hdr.includes('HDR10+')) score += 60;
  else if (hdr.includes('HDR10')) score += 50;
  else if (hdr.includes('HDR')) score += 40;
  if (hdr.includes('10bit')) score += 10;

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

  if (c === 'HEVC') score += 50;
  else if (c === 'AV1') score += 45;
  else if (c === 'H264') score += 30;

  const upscalePenalty = ctx.isAnimation ? 1500 : 5000;
  if (hasToken(f, 'upscale') || hasToken(f, 'upscaled') || hasToken(f, 'ai-upscale')) {
    score -= upscalePenalty;
  }
  if (hasToken(f, 'rife') || hasToken(f, 'interpol') || f.includes('60fps')) score -= 200;
  return score;
}

// ─── Main entry: getStreams(id, type, season, episode) ─────────────────────
async function getStreams(id, type, season, episode) {
  if (!id) return [];
  const isMovie = (type || 'movie') !== 'tv' && type !== 'series';

  let imdbId;
  if (/^tt\d+$/.test(String(id))) {
    imdbId = String(id);
  } else {
    imdbId = await tmdbToImdb(id, isMovie ? 'movie' : 'tv');
    if (!imdbId) {
      console.error('[PenguAll] could not resolve TMDB→IMDB for ' + id);
      return [];
    }
  }

  const url = buildUpstreamUrl(imdbId, isMovie, season, episode);
  console.log('[PenguAll] GET ' + url.slice(0, 200) + (url.length > 200 ? '...' : ''));

  let data;
  try {
    data = await fetchJson(url, { timeout: 30000 });
  } catch (e) {
    console.error('[PenguAll] fetch failed: ' + e.message);
    return [];
  }
  const raw = Array.isArray(data.streams) ? data.streams : [];
  if (raw.length === 0) { console.log('[PenguAll] no streams returned'); return []; }
  console.log('[PenguAll] ' + raw.length + ' raw streams from upstream');

  const httpsOnly = raw.filter(s => (s.url || '').startsWith('https://'));
  const realStreams = httpsOnly.filter(s => s.url && s.url.length > 30 && !/support the project/i.test(s.name || ''));
  console.log('[PenguAll] ' + realStreams.length + ' after HTTPS+donation filters');

  const isAnimation = await detectIsAnimation(imdbId, isMovie ? 'movie' : 'tv');
  console.log('[PenguAll] isAnimation=' + isAnimation);

  const ranked = realStreams
    .map(s => ({ s, score: rankStream(s, { isAnimation }) }))
    .sort((a, b) => b.score - a.score)
    .map(x => x.s);

  const out = [];
  for (const s of ranked) {
    if (out.length >= MAX_STREAMS) break;
    let filename = '';
    try {
      const urlObj = new URL(s.url);
      const pathSegments = urlObj.pathname.split('/').filter(Boolean);
      for (let i = pathSegments.length - 1; i >= 0; i--) {
        const seg = decodeURIComponent(pathSegments[i]);
        if (seg.length > 5 && /\.[a-z0-9]{2,4}$/i.test(seg)) {
          filename = seg;
          break;
        }
      }
    } catch (e) {}
    if (!filename) filename = (s.title || s.name || '').replace(/^.*❯\s*/, '');

    const isHls = s.url.includes('/hls/') || s.url.endsWith('.m3u8') || s.url.includes('index.m3u8');

    const quality = parseQuality(filename) ||
      (s.name || '').match(/(\d{3,4}[pP])/)?.[1]?.toLowerCase() ||
      (s.name || '').match(/4K/i)?.[0]?.toLowerCase() ||
      '1080p';
    const codec = parseCodec(filename);
    const source = parseSource(filename);
    const hdr = parseHdrFlags(filename);
    const audio = parseAudio(filename);
    const lang = parseLanguage(filename);

    const sourceMatch = (s.name || '').match(/[•·]\s*([^•·]+?)\s*$/);
    const upstreamSource = sourceMatch ? sourceMatch[1].trim() : '';

    const parts = [];
    if (quality === '4320p') parts.push('8K');
    else if (quality === '2160p') parts.push('4K');
    else parts.push(quality.toUpperCase());
    if (hdr) parts.push(hdr.split(' ').slice(0, 2).join('+'));
    if (codec) parts.push(codec);
    if (lang) parts.push(lang);
    if (audio) parts.push(audio);
    if (source === 'REMUX') parts.push('REMUX');
    if (upstreamSource) parts.push('[' + upstreamSource + ']');
    if (isHls) parts.push('HLS');
    const nameLine = '[PA] ' + PROVIDER_NAME + ' ' + parts.join(' | ');

    out.push({
      name: nameLine,
      title: filename || s.title || s.name || (quality + ' ' + codec),
      url: s.url,
      quality: quality,
      type: isHls ? 'video/mp4' : 'video/mkv',
      headers: { 'User-Agent': UA },
      provider: PROVIDER_NAME,
      behaviorHints: {
        bingeGroup: (s.behaviorHints && s.behaviorHints.bingeGroup) ||
          ('penguall|' + imdbId + '|' + (out.length)),
        filename: filename,
        notWebReady: false,
      },
    });
  }
  console.log('[PenguAll] returning ' + out.length + ' streams' + (MAX_STREAMS === Infinity ? '' : ' (cap=' + MAX_STREAMS + ')'));
  return out;
}

// ─── CLI test ───────────────────────────────────────────────────────────────
if (require.main === module) {
  (async () => {
    const args = process.argv.slice(2);
    if (args.length === 0) {
      console.log('Usage: node pengu_all.js <imdbId|tmdbId> <movie|tv|series> [season] [episode]');
      process.exit(0);
    }
    const streams = await getStreams(args[0], args[1] || 'movie', args[2], args[3]);
    console.log('\n' + streams.length + ' streams:');
    streams.forEach((s, i) => {
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
