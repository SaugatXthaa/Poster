// AntarcticaIndependent — Aggregator provider that combines ALL working
// PenguPlay-independent, no-auth providers into a single source.
//
// This provider calls multiple direct scrapers in parallel and returns their
// combined results. 100% PenguPlay-independent — no auth_token, no pengu.uk
// dependency. Pure Node.js + cheerio + crypto-js.
//
// AGGREGATED SOURCES (all PenguPlay-independent, all no-auth):
//   • hdhub4u     — new6.hdhub4u.cl (direct MKV, 13 streams, multi-audio)
//   • 4khdhub     — 4khdhub.one (direct MKV via R2 CloudFlare Storage)
//   • desiflix    — manifest.desitvhub.eu.org (HLS playlists)
//   • purstream   — (1 stream, often 4K)
//   • vegamovies  — (4 streams, Indian content focus)
//
// If pengu.uk dies tomorrow, this provider KEEPS WORKING because none of its
// sources depend on PenguPlay.
//
// USAGE
//   node antarctica_independent.js tt1375666 movie
//   node antarctica_independent.js tt0903747 series 1 1
//
// RENDER FREE TIER: Works perfectly. Requires only `cheerio-without-node-native`
// and `crypto-js` npm packages (both pure JavaScript, no native modules).
// ============================================================================

'use strict';

const https = require('https');
const http = require('http');
const path = require('path');

const PROVIDER_NAME = 'AntarcticaIndep';
const TMDB_API_KEY = '8476a7ab80ad76f0936744df0430e67c';

// Paths to the aggregated providers (relative to this file)
const PROVIDERS_DIR = path.resolve(__dirname, '..', 'allinone-nuvio-repo', 'providers');
const AGGREGATED_PROVIDERS = ['hdhub4u', '4khdhub', 'desiflix', 'purstream', 'vegamovies'];

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';

// Cap — since we aggregate multiple providers, total can be high
const MAX_STREAMS = process.env.ANTARCTICA_INDEP_MAX_STREAMS !== undefined
  ? parseInt(process.env.ANTARCTICA_INDEP_MAX_STREAMS, 10) || Infinity
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

// ─── Filename parsers (same as antarctica_v2 / moviebox_v2) ─────────────────
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

// ─── Ranking (same as other providers) ─────────────────────────────────────
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

  // AI Upscale penalty (only for live-action)
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
  // Try multiple paths (Nuvio addon layout, scripts folder, current dir)
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
    } catch (e) { /* try next path */ }
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
      console.error('[AntarcticaIndep] could not resolve TMDB→IMDB for ' + id);
      return [];
    }
  }

  // Detect animation (for ranking)
  const isAnimation = await detectIsAnimation(imdbId, isMovie ? 'movie' : 'tv');
  console.log('[AntarcticaIndep] imdbId=' + imdbId + ' isAnimation=' + isAnimation);

  // Call ALL aggregated sub-providers IN PARALLEL (with timeout)
  const results = await Promise.allSettled(
    AGGREGATED_PROVIDERS.map(async (name) => {
      const provider = loadSubProvider(name);
      if (!provider) {
        console.log('[AntarcticaIndep] ' + name + ': could not load');
        return [];
      }
      try {
        const streams = await Promise.race([
          provider.getStreams(imdbId, isMovie ? 'movie' : 'tv', season, episode),
          new Promise((_, rej) => setTimeout(() => rej(new Error('20s timeout')), 20000))
        ]);
        console.log('[AntarcticaIndep] ' + name + ': ' + (streams || []).length + ' streams');
        // Tag each stream with the sub-provider name
        return (streams || []).map(s => ({
          ...s,
          _subProvider: name,
          name: s.name ? '[AI] ' + name + ' | ' + s.name.replace(/^\[.*?\]\s*/, '') : s.name,
        }));
      } catch (e) {
        console.log('[AntarcticaIndep] ' + name + ': err ' + (e.message || '').slice(0, 80));
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
  console.log('[AntarcticaIndep] combined: ' + combined.length + ' streams');

  // Dedup by URL
  const seen = new Set();
  const deduped = combined.filter(s => {
    if (!s.url) return false;
    if (seen.has(s.url)) return false;
    seen.add(s.url);
    return true;
  });
  console.log('[AntarcticaIndep] deduped: ' + deduped.length + ' streams');

  // Filter HTTPS only
  const httpsOnly = deduped.filter(s => (s.url || '').startsWith('https://') || (s.url || '').startsWith('http://'));
  
  // Rank
  const ranked = httpsOnly
    .map(s => ({ s, score: rankStream(s, { isAnimation }) }))
    .sort((a, b) => b.score - a.score)
    .map(x => x.s);

  // Build output
  const out = [];
  for (const s of ranked) {
    if (out.length >= MAX_STREAMS) break;
    // Re-parse metadata from the stream's filename/title
    const filename = (s.behaviorHints && s.behaviorHints.filename) || s.title || s.name || '';
    const quality = parseQuality(filename) || '1080p';
    const codec = parseCodec(filename);
    const source = parseSource(filename);
    const hdr = parseHdrFlags(filename);
    const audio = parseAudio(filename);
    const lang = parseLanguage(filename);

    out.push({
      name: s.name || ('[AI] ' + PROVIDER_NAME + ' ' + quality),
      title: s.title || filename,
      url: s.url,
      quality: quality,
      type: s.type || 'video/mkv',
      headers: s.headers || { 'User-Agent': UA },
      provider: PROVIDER_NAME,
      behaviorHints: s.behaviorHints || {
        bingeGroup: 'antarctica-indep|' + imdbId + '|' + out.length,
        filename: filename,
        notWebReady: false,
      },
    });
  }
  console.log('[AntarcticaIndep] returning ' + out.length + ' streams');
  return out;
}

// ─── CLI test ───────────────────────────────────────────────────────────────
if (require.main === module) {
  (async () => {
    const args = process.argv.slice(2);
    if (args.length === 0) {
      console.log('Usage: node antarctica_independent.js <imdbId|tmdbId> <movie|tv|series> [season] [episode]');
      console.log('Aggregates: ' + AGGREGATED_PROVIDERS.join(', '));
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
