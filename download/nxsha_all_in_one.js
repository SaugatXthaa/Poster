// Nxsha — 33 streaming servers + OpenSubtitles subtitles via nxsha.space API.
// Returns direct playable HLS streams (multi-audio) + .srt subtitle downloads.
//
// ARCHITECTURE (reverse-engineered from nxsha.app's JS bundle):
//   1. Encode query params with AES (passphrase: S8x!Jk4ZP1uG8$my)
//   2. Call nxsha.space/api/servers?q=<encoded> → get encrypted server list
//   3. For each server: POST nxsha.space/api/sources → get encrypted stream URLs
//   4. Call nxsha.space/api/subtitles?q=<encoded> → get encrypted subtitle list
//   5. Decrypt all _hash responses → get servers/sources/subtitles
//   6. Stream URLs are HLS playlists (.m3u8) with multi-audio tracks
//   7. Subtitle URLs are direct .srt downloads from dl.opensubtitles.org
//
// 33 servers including: Nitro, MbPly, MhPly, MbBlast, Citadel, Multi-blue,
// Multi-bill, Lolly, VidStpM, VidE, VidBo, + 2 embedded (screenscape.me, vidzee.wtf)
//
// PURE NODE.JS — no PenguPlay, no auth. Uses crypto-js for AES.
// ============================================================================

'use strict';

const CryptoJS = require('crypto-js');
const TMDB_API_KEY = '8476a7ab80ad76f0936744df0430e67c';
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';
const NXSHA_API = 'https://nxsha.space';
// AES passphrase (decoded from String.fromCharCode(83,56,120,33,74,107,52,90,80,49,117,71,56,36,109,121))
const PASSPHRASE = String.fromCharCode(83, 56, 120, 33, 74, 107, 52, 90, 80, 49, 117, 71, 56, 36, 109, 121);
const MAX_STREAMS = 50;

// ─── AES encode/decode (from nxsha's JS bundle) ───────────────────────────
function decodeData(e) {
  if (!e) return null;
  try {
    let x = e.replace(/-/g, '+').replace(/_/g, '/');
    for (; x.length % 4 != 0;) x += '=';
    let t = CryptoJS.AES.decrypt(x, PASSPHRASE).toString(CryptoJS.enc.Utf8);
    if (!t) return null;
    let c = JSON.parse(t);
    delete c._req_ts; delete c._req_salt;
    return c;
  } catch (e) { return null; }
}

function encodeData(e) {
  if (!e) return '';
  try {
    let r = { ...e, _req_ts: Date.now(), _req_salt: Math.random().toString(36).substring(2, 12) };
    return CryptoJS.AES.encrypt(JSON.stringify(r), PASSPHRASE).toString()
      .replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '');
  } catch (e) { return ''; }
}

// ─── HTTP helpers ─────────────────────────────────────────────────────────
async function fetchJson(url, opts = {}) {
  const res = await fetch(url, {
    headers: { 'User-Agent': UA, 'Accept': 'application/json', ...opts.headers },
    signal: AbortSignal.timeout(opts.timeout || 15000),
    ...opts,
  });
  return res.json();
}

async function postJson(url, body, opts = {}) {
  const res = await fetch(url, {
    method: 'POST',
    headers: { 'User-Agent': UA, 'Content-Type': 'application/json', 'Accept': 'application/json', ...opts.headers },
    body: JSON.stringify(body),
    signal: AbortSignal.timeout(opts.timeout || 15000),
  });
  return res.json();
}

// ─── TMDB ────────────────────────────────────────────────────────────────────
async function getTMDBInfo(id, type) {
  const mediaType = type === 'tv' || type === 'series' ? 'tv' : 'movie';
  if (/^tt\d+$/.test(String(id))) {
    try {
      const d = await fetchJson(`https://api.themoviedb.org/3/find/${id}?api_key=${TMDB_API_KEY}&external_source=imdb_id`);
      const r = mediaType === 'tv' ? d.tv_results : d.movie_results;
      if (r && r[0]) return { title: mediaType === 'tv' ? r[0].name : r[0].title, year: ((r[0].first_air_date || r[0].release_date || '') + '').split('-')[0], tmdbId: String(r[0].id), imdbId: String(id) };
    } catch (e) {}
  }
  try {
    const d = await fetchJson(`https://api.themoviedb.org/3/${mediaType}/${id}?api_key=${TMDB_API_KEY}`);
    const ext = await fetchJson(`https://api.themoviedb.org/3/${mediaType}/${id}/external_ids?api_key=${TMDB_API_KEY}`);
    return { title: mediaType === 'tv' ? d.name : d.title, year: ((d.first_air_date || d.release_date || '') + '').split('-')[0], tmdbId: String(id), imdbId: ext.imdb_id || '' };
  } catch (e) { return null; }
}

// ─── Nxsha API calls ──────────────────────────────────────────────────────
async function getServers(tmdbId, imdbId, type, season, episode) {
  const enc = encodeData({ tmdbId, imdb_id: imdbId, type, season, episode });
  const url = `${NXSHA_API}/api/servers?q=${encodeURIComponent(enc)}`;
  console.log('[Nxsha] GET servers: ' + url.slice(0, 80) + '...');
  try {
    const data = await fetchJson(url);
    const decoded = decodeData(data._hash);
    return decoded?.servers || [];
  } catch (e) { console.log('[Nxsha] servers err: ' + e.message); return []; }
}

async function getSources(provider, tmdbId, imdbId, type, season, episode) {
  const enc = encodeData({ provider, tmdbId, imdb_id: imdbId, type, season, episode });
  try {
    const data = await postJson(`${NXSHA_API}/api/sources`, { q: enc });
    const decoded = decodeData(data._hash);
    return decoded?.sources || [];
  } catch (e) { return []; }
}

async function getSubtitles(tmdbId, type, season, episode) {
  const enc = encodeData({ tmdbId, type, season, episode });
  const url = `${NXSHA_API}/api/subtitles?q=${encodeURIComponent(enc)}`;
  console.log('[Nxsha] GET subtitles: ' + url.slice(0, 80) + '...');
  try {
    const data = await fetchJson(url);
    const decoded = decodeData(data._hash);
    return decoded?.subtitles || [];
  } catch (e) { console.log('[Nxsha] subtitles err: ' + e.message); return []; }
}

// ─── Main entry: getStreams(id, type, season, episode) ────────────────────
async function getStreams(id, type, season, episode) {
  const isMovie = type !== 'tv' && type !== 'series';
  const actualType = isMovie ? 'movie' : 'tv';
  console.log('[Nxsha] Request: id=' + id + ' type=' + type + (season ? ' S' + season + 'E' + episode : ''));

  const info = await getTMDBInfo(id, type);
  if (!info || !info.tmdbId) { console.log('[Nxsha] No TMDB info'); return []; }
  console.log('[Nxsha] TMDB: ' + info.title + ' (' + info.year + ') tmdbId=' + info.tmdbId);

  // 1. Get server list
  const servers = await getServers(info.tmdbId, info.imdbId, actualType, season, episode);
  console.log('[Nxsha] Servers: ' + servers.length);
  if (!servers.length) return [];

  // Filter to web-support servers that are online and not disabled
  const webServers = servers.filter(s => s.web_support !== false && !s.isDisable && s.status === 'online');
  console.log('[Nxsha] Web-support servers: ' + webServers.length);

  // 2. Fetch sources for each web-support server IN PARALLEL
  const sourceResults = await Promise.allSettled(
    webServers.slice(0, 16).map(s => getSources(s.scraper, info.tmdbId, info.imdbId, actualType, season, episode))
  );

  // 3. Get subtitles
  const subtitles = await getSubtitles(info.tmdbId, actualType, season, episode);
  console.log('[Nxsha] Subtitles: ' + subtitles.length);

  // 4. Build stream objects
  const streams = [];
  for (let i = 0; i < sourceResults.length; i++) {
    if (sourceResults[i].status !== 'fulfilled') continue;
    const sources = sourceResults[i].value;
    const server = webServers[i];
    for (const src of sources) {
      if (!src.url) continue;
      // Parse quality from HLS playlist or source metadata
      const quality = src.quality?.includes('4k') || src.quality?.includes('2160') ? '2160p'
        : src.quality?.includes('1080') ? '1080p'
        : src.quality?.includes('720') ? '720p'
        : src.quality?.includes('480') ? '480p'
        : '1080p';

      streams.push({
        name: '[NX] Nxsha ' + quality.toUpperCase() + ' | ' + server.name + (src.quality ? ' [' + src.quality + ']' : ''),
        title: info.title + (isMovie ? '' : ' S' + (season || 1) + 'E' + (episode || 1)) + ' (' + info.year + ') [' + server.name + ']',
        url: src.url,
        quality: quality,
        type: 'video/mp4', // HLS is playable as mp4 type in Stremio
        headers: { 'User-Agent': UA, 'Referer': 'https://web.nxsha.app/' },
        provider: 'Nxsha',
        behaviorHints: {
          bingeGroup: 'nxsha|' + server.scraper + '|' + quality,
          notWebReady: false,
        },
      });
    }
  }

  // 5. Add embedded servers (hardcoded in player JS)
  const H = info.tmdbId;
  if (isMovie) {
    streams.push({
      name: '[NX] Nxsha HD | VidStpM [Embed]',
      title: info.title + ' (' + info.year + ') [VidStpM]',
      url: `https://nxsha.screenscape.me/embed?tmdb=${H}&type=movie&autoplay=true`,
      quality: '1080p', type: 'video/mp4',
      headers: { 'User-Agent': UA }, provider: 'Nxsha',
      behaviorHints: { bingeGroup: 'nxsha|em-sc|embed', notWebReady: false },
    });
    streams.push({
      name: '[NX] Nxsha HD | VidBo [Embed]',
      title: info.title + ' (' + info.year + ') [VidBo]',
      url: `https://player.vidzee.wtf/embed/movie/${H}?autoplay=true`,
      quality: '1080p', type: 'video/mp4',
      headers: { 'User-Agent': UA }, provider: 'Nxsha',
      behaviorHints: { bingeGroup: 'nxsha|em-7|embed', notWebReady: false },
    });
  }

  console.log('[Nxsha] Total streams: ' + streams.length);

  // 6. Attach subtitles to each stream
  if (subtitles.length > 0) {
    const subUrls = subtitles.map(s => s.uri || s.url).filter(Boolean);
    for (const stream of streams) {
      stream.behaviorHints.subtitles = subUrls;
    }
    // Also return subtitles separately
    streams.subtitles = subtitles.map(s => ({
      url: s.uri || s.url,
      lang: s.language || s.lang || 'en',
      name: (s.label || s.language || 'Unknown') + ' [OpenSubs]',
    }));
  }

  // Cap at MAX_STREAMS
  const capped = streams.slice(0, MAX_STREAMS);
  if (streams.subtitles) capped.subtitles = streams.subtitles;
  console.log('[Nxsha] Returning ' + capped.length + ' streams + ' + (capped.subtitles?.length || 0) + ' subtitles');
  return capped;
}

// ─── CLI ───────────────────────────────────────────────────────────────────────
if (require.main === module) {
  (async () => {
    const args = process.argv.slice(2);
    if (!args[0]) { console.log('Usage: node nxsha.js <imdbId|tmdbId> <movie|tv> [season] [episode]'); process.exit(0); }
    const streams = await getStreams(args[0], args[1] || 'movie', args[2], args[3]);
    console.log('\n' + streams.length + ' streams:');
    streams.slice(0, 10).forEach((s, i) => {
      console.log('[' + i + '] ' + s.name + ' (' + s.quality + ')');
      console.log('    ' + s.url.slice(0, 200));
    });
    if (streams.subtitles) {
      console.log('\n' + streams.subtitles.length + ' subtitles:');
      streams.subtitles.slice(0, 10).forEach((s, i) => console.log('[' + i + '] ' + s.lang + ' ' + s.name + ' → ' + s.url.slice(0, 100)));
    }
  })();
}

module.exports = { getStreams, getSubtitles, encodeData, decodeData };
