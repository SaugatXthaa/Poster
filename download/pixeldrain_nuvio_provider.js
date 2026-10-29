// PixelDrain — Direct stream provider using PenguPlay for discovery + PixelDrain for playback.
//
// ARCHITECTURE:
//   1. Use PenguPlay's Arctic source to DISCOVER streams (encrypted URLs)
//   2. Probe each encrypted URL → follow redirect → find actual backend
//   3. If backend is PixelDrain → return PixelDrain direct download URL (INDEPENDENT of PenguPlay)
//   4. If backend is R2 CloudFlare Storage → return R2 URL directly (INDEPENDENT)
//   5. Skip pengu.uk HLS URLs (those depend on PenguPlay)
//   6. For each PixelDrain file, fetch file info for enriched metadata
//
// KEY BENEFIT: The returned stream URLs are on pixeldrain.dev and
// r2.cloudflarestorage.com — NOT on pengu.uk. Even if PenguPlay dies,
// the streams still play (discovery stops, but playback continues).
//
// PenguPlay is used ONLY for discovery (finding which PixelDrain files exist
// for a given movie). The actual playback is 100% PenguPlay-independent.
// ============================================================================

'use strict';

const https = require('https');
const http = require('http');
const TMDB_API_KEY = '8476a7ab80ad76f0936744df0430e67c';
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';
const AUTH_TOKEN = (process.env.PENGU_AUTH_TOKEN && process.env.PENGU_AUTH_TOKEN.trim()) || 'wuW_pplyXmYBInI8p3VwacEOCrlEspsWp8SZMi7E3_o';
const MAX_STREAMS = 50;

function fetchJson(url) {
  return fetch(url, { headers: { 'User-Agent': UA, 'Accept': 'application/json' }, signal: AbortSignal.timeout(20000) }).then(r => r.json());
}

async function getTMDBInfo(id, type) {
  const mediaType = type === 'tv' || type === 'series' ? 'tv' : 'movie';
  if (/^tt\d+$/.test(String(id))) {
    try {
      const d = await fetchJson(`https://api.themoviedb.org/3/find/${id}?api_key=${TMDB_API_KEY}&external_source=imdb_id`);
      const r = mediaType === 'tv' ? d.tv_results : d.movie_results;
      if (r && r[0]) return { title: mediaType==='tv'?r[0].name:r[0].title, year: ((r[0].first_air_date||r[0].release_date||'')+'').split('-')[0] };
    } catch(e){}
  }
  try {
    const d = await fetchJson(`https://api.themoviedb.org/3/${mediaType}/${id}?api_key=${TMDB_API_KEY}`);
    return { title: mediaType==='tv'?d.name:d.title, year: ((d.first_air_date||d.release_date||'')+'').split('-')[0] };
  } catch(e) { return null; }
}

// Probe a PenguPlay encrypted URL to discover the actual backend
function probeUrl(url) {
  return new Promise(resolve => {
    let cur = url, h = 0;
    const next = () => {
      if (h++ >= 3) return resolve(null); // max 3 hops
      const u = new URL(cur); const lib = u.protocol === 'http:' ? http : https;
      const req = lib.request({hostname:u.hostname, path:u.pathname+u.search, method:'GET',
        headers:{'User-Agent':UA,'Range':'bytes=0-1'}}, (res) => {
        res.destroy();
        if (res.statusCode >= 300 && res.statusCode < 400 && res.headers.location) {
          cur = new URL(res.headers.location, cur).href;
          next();
        } else {
          resolve({ finalUrl: cur, status: res.statusCode, ct: res.headers['content-type'], cl: res.headers['content-length'] });
        }
      });
      req.on('error', () => resolve(null));
      req.on('timeout', () => { req.destroy(); resolve(null); });
      req.setTimeout(8000); req.end();
    };
    next();
  });
}

// Get PixelDrain file info
async function getPixelDrainInfo(fileId) {
  try {
    const r = await fetch(`https://pixeldrain.dev/api/file/${fileId}/info`, { headers: {'User-Agent':UA}, signal: AbortSignal.timeout(5000) });
    return await r.json();
  } catch(e) { return null; }
}

// Parse quality from filename
function parseQuality(filename) {
  const f = (filename || '').toLowerCase();
  if (f.includes('2160p') || f.includes('4k')) return '2160p';
  if (f.includes('1080p')) return '1080p';
  if (f.includes('720p')) return '720p';
  if (f.includes('480p')) return '480p';
  return '1080p';
}

function parseAudio(filename) {
  const f = (filename || '').toLowerCase();
  const t = [];
  if (f.includes('ddp') || f.includes('dd5') || f.includes('dd2')) t.push('DD');
  if (f.includes('hindi')) t.push('Hindi');
  if (f.includes('english') || f.includes('eng')) t.push('English');
  return t.join('+');
}

// ─── Main entry: getStreams(id, type, season, episode) ────────────────────
async function getStreams(id, type, season, episode) {
  const isMovie = type !== 'tv' && type !== 'series';
  console.log('[PixelDrain] Request: id=' + id + ' type=' + type + (season ? ' S'+season+'E'+episode : ''));

  const info = await getTMDBInfo(id, type);
  if (!info) return [];

  // 1. Call PenguPlay's Arctic source to DISCOVER streams
  const cfg = encodeURIComponent(JSON.stringify({
    auth_token: AUTH_TOKEN,
    source_arctic: 'on',
    source_moviebox: 'off', source_4khdhub: 'off', source_2peckle: 'off',
    source_hdhub4u: 'off', source_moviesdrives: 'off', source_vaplayer: 'off',
    source_vegamovies: 'off', source_vidlink: 'off', source_vidfast: 'off',
    source_cinefreak: 'off', source_cinejoy: 'off', source_kisskh: 'off',
    source_anikoto: 'off', source_miruro: 'off',
    res_2160: 'on', res_1080: 'on', res_720: 'on', res_480: 'on',
  }));
  const path = isMovie ? `/stream/movie/${id}.json` : `/stream/series/${id}:${season||1}:${episode||1}.json`;
  const penguUrl = 'https://pengu.uk/' + cfg + path;
  console.log('[PixelDrain] Discovering via PenguPlay Arctic...');

  let streams;
  try {
    const r = await fetch(penguUrl, { headers: { 'User-Agent': UA, 'Accept': 'application/json' }, signal: AbortSignal.timeout(20000) });
    const j = await r.json();
    streams = (j.streams || []).filter(s => s.url && s.url.length > 30);
  } catch(e) {
    console.log('[PixelDrain] PenguPlay discovery failed: ' + e.message);
    return [];
  }
  console.log('[PixelDrain] Discovered ' + streams.length + ' streams');

  // 2. Probe each stream to find the actual backend (PixelDrain, R2, etc.)
  console.log('[PixelDrain] Probing backends...');
  const probeResults = await Promise.all(streams.map(s => probeUrl(s.url)));

  // 3. Build output — only include INDEPENDENT URLs (PixelDrain + R2)
  const out = [];
  for (let i = 0; i < streams.length; i++) {
    if (out.length >= MAX_STREAMS) break;
    const probe = probeResults[i];
    if (!probe) continue;

    const finalUrl = probe.finalUrl;
    let streamUrl = null;
    let backend = 'unknown';
    let filename = '';
    let fileSize = 0;

    // Check if backend is PixelDrain
    const pdMatch = finalUrl.match(/pixeldrain\.[a-z]+\/api\/file\/([A-Za-z0-9]+)/);
    if (pdMatch) {
      const fileId = pdMatch[1];
      backend = 'PixelDrain';
      streamUrl = `https://pixeldrain.dev/api/file/${fileId}?download`;
      // Get file info for metadata
      const fileInfo = await getPixelDrainInfo(fileId);
      if (fileInfo) {
        filename = fileInfo.name || '';
        fileSize = fileInfo.size || 0;
      }
    }
    // Check if backend is R2 CloudFlare Storage
    else if (finalUrl.includes('r2.cloudflarestorage.com') || finalUrl.includes('r2.dev')) {
      backend = 'R2-CDN';
      streamUrl = finalUrl;
      const cl = probe.cl;
      if (cl) fileSize = parseInt(cl, 10);
    }
    // Skip pengu.uk HLS and dead domains (shegu.net, etc.)
    else if (finalUrl.includes('pengu.uk')) {
      continue; // HLS from pengu.uk — depends on PenguPlay, skip
    }
    else if (probe.status === 200 && /video|octet-stream|mpegurl/i.test(probe.ct || '')) {
      // Unknown but playable backend
      backend = 'Direct';
      streamUrl = finalUrl;
    }
    else {
      continue; // Skip non-playable
    }

    if (!streamUrl) continue;

    // Parse metadata from filename or stream name
    const quality = parseQuality(filename || streams[i].name || '');
    const audio = parseAudio(filename || streams[i].name || '');
    const sizeStr = fileSize > 0 ? (fileSize/1e9).toFixed(1) + 'GB' : '';

    const parts = [quality.toUpperCase()];
    if (audio) parts.push(audio);
    if (sizeStr) parts.push(sizeStr);
    parts.push('[' + backend + ']');

    out.push({
      name: '[PD] PixelDrain ' + parts.join(' | '),
      title: (filename || info.title + ' (' + info.year + ') ' + quality).slice(0, 200),
      url: streamUrl,
      quality: quality,
      type: 'video/mkv',
      headers: { 'User-Agent': UA },
      provider: 'PixelDrain',
      behaviorHints: {
        bingeGroup: 'pixeldrain|' + backend + '|' + quality,
        filename: filename,
        notWebReady: false,
      },
    });
  }

  console.log('[PixelDrain] Returning ' + out.length + ' INDEPENDENT streams (PixelDrain + R2, no PenguPlay proxy)');
  return out;
}

if (require.main === module) {
  (async () => {
    const args = process.argv.slice(2);
    if (!args[0]) { console.log('Usage: node pixeldrain_scraper.js <imdbId> <movie|tv> [season] [episode]'); process.exit(0); }
    const streams = await getStreams(args[0], args[1]||'movie', args[2], args[3]);
    console.log('\n' + streams.length + ' streams:');
    streams.forEach((s, i) => {
      console.log('[' + i + '] ' + s.name + ' (' + s.quality + ')');
      console.log('    ' + s.url.slice(0, 200));
    });
  })();
}

module.exports = { getStreams };
