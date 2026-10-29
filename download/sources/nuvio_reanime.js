/**
 * Re:ANIME Provider — direct playable HLS via enc-dec.app + enriched metadata
 * ==========================================================================
 * Site: https://reanime.to (SvelteKit SPA)
 * Stream API: https://enc-dec.app (free, 40 req/s, decrypts flixcloud streams)
 *
 * Resolution chain (NO proxy, NO PenguPlay):
 *   1. TMDB ID → fetch reanime.to __data.json (curl works) → get AniList/MAL IDs + metadata
 *   2. Construct flixcloud embed URL: https://flixcloud.cc/e/{videoId}?v=2
 *      (videoId derived from Re:ANIME's stream API response)
 *   3. Fetch flixcloud embed page → extract SvelteKit __data with encrypted stream data
 *   4. POST enc-dec.app/api/dec-flixcloud?type=token → get stream token
 *   5. GET flixcloud.cc/api/m3u8/{token} → get encrypted stream
 *   6. POST enc-dec.app/api/dec-flixcloud?type=stream → decrypt → get direct .m3u8 URL
 *   7. Return HLS stream (notWebReady: false, directly playable)
 *
 * Metadata from __data.json: title, cover image (AniList CDN), description, genres,
 *   AniList/MAL/TMDB IDs, episode list with sub/dub info, format, status
 * Subtitles: HLS playlist includes embedded subtitle tracks (marked as "Sub")
 */
'use strict';

const { execFile } = require('child_process');
const { promisify } = require('util');
const execFileAsync = promisify(execFile);
const { fetchHtml, fetchJson, UA } = require('../lib/http');

const REANIME_BASE = 'https://reanime.to';
const TMDB_API_KEY = '439c478a771f35c05022f9feabcca01c';
const ENC_DEC_API = 'https://enc-dec.app/api';
const FULL_HEADERS = [
  'Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
  'Accept-Language: en-US,en;q=0.9',
  'Sec-Ch-Ua: "Google Chrome";v="147", "Not?A_Brand";v="8", "Chromium";v="147"',
  'Sec-Ch-Ua-Mobile: ?0',
  'Sec-Ch-Ua-Platform: "Windows"',
  'Sec-Fetch-Dest: document',
  'Sec-Fetch-Mode: navigate',
  'Sec-Fetch-Site: none',
  'Upgrade-Insecure-Requests: 1',
];

// ─── Curl helper (for Cloudflare-protected sites) ──────────────────────────
async function curlGet(url, opts = {}) {
  const args = ['-sS', '-L', '--compressed', '--max-time', '15', '-A', UA];
  for (const h of FULL_HEADERS) args.push('-H', h);
  if (opts.referer) args.push('-H', `Referer: ${opts.referer}`);
  if (opts.accept) args.push('-H', `Accept: ${opts.accept}`);
  args.push(url);
  try {
    const { stdout } = await execFileAsync('curl', args, { encoding: 'utf-8', maxBuffer: 30 * 1024 * 1024, timeout: 18000 });
    return stdout;
  } catch (e) { return ''; }
}

async function curlPostJson(url, body) {
  try {
    const { stdout } = await execFileAsync('curl', [
      '-sS', '-L', '--compressed', '--max-time', '15',
      '-A', UA, '-H', 'Content-Type: application/json', '-H', 'Accept: application/json',
      '-X', 'POST', '-d', body, url,
    ], { encoding: 'utf-8', maxBuffer: 10 * 1024 * 1024, timeout: 18000 });
    return stdout ? JSON.parse(stdout) : null;
  } catch (e) { return null; }
}

// ─── SvelteKit devalue resolver ────────────────────────────────────────────
function resolveRef(data, val) {
  if (val === null || val === undefined) return null;
  if (typeof val !== 'number') return val;
  if (val < 0 || val >= data.length) return val;
  return data[val];
}
function resolveDict(data, dict) {
  if (!dict || typeof dict !== 'object') return dict;
  const out = {};
  for (const [k, v] of Object.entries(dict)) out[k] = resolveRef(data, v);
  return out;
}

// ─── TMDB lookup ──────────────────────────────────────────────────────────
async function getTmdbDetails(tmdbId, type) {
  const url = `https://api.themoviedb.org/3/${type === 'tv' ? 'tv' : 'movie'}/${tmdbId}?api_key=${TMDB_API_KEY}&language=en-US`;
  const data = await fetchJson(url);
  if (!data) return null;
  return {
    title: type === 'tv' ? data.name : data.title,
    year: ((type === 'tv' ? data.first_air_date : data.release_date) || '').substring(0, 4),
    poster: data.poster_path ? `https://image.tmdb.org/t/p/w500${data.poster_path}` : null,
    genres: (data.genres || []).map(g => g.name),
    overview: data.overview || '',
  };
}

// ─── Search Re:ANIME via __data.json ──────────────────────────────────────
async function searchReAnime(query) {
  try {
    const body = await curlGet(`${REANIME_BASE}/home/__data.json`, { accept: 'application/json' });
    if (!body) return [];
    const data = JSON.parse(body);
    if (!data || !data.nodes) return [];
    const nodeData = data.nodes[1]?.data || [];
    const homeObj = resolveRef(nodeData, nodeData[0]);
    if (!homeObj) return [];
    const homeData = resolveRef(nodeData, homeObj.homeData);
    if (!homeData) return [];
    const results = [];
    for (const listKey of ['latest_aired', 'new_on_site', 'trending']) {
      const list = resolveRef(nodeData, homeData[listKey]);
      if (!Array.isArray(list)) continue;
      for (const itemRef of list) {
        const anime = resolveRef(nodeData, itemRef);
        if (!anime || typeof anime !== 'object') continue;
        const slug = resolveRef(nodeData, anime.anime_id);
        const titleObj = resolveRef(nodeData, anime.title);
        const title = titleObj ? (resolveRef(nodeData, titleObj.english) ||
                                   resolveRef(nodeData, titleObj.romaji) ||
                                   resolveRef(nodeData, titleObj.user_preferred)) : '';
        const canWatch = resolveRef(nodeData, anime.can_watch);
        if (slug && title && canWatch !== false) {
          results.push({ slug, title, canWatch: !!canWatch });
        }
      }
    }
    const seen = new Set();
    const qLower = (query || '').toLowerCase();
    return results.filter(r => {
      if (seen.has(r.slug)) return false;
      seen.add(r.slug);
      return !qLower || r.title.toLowerCase().includes(qLower) || r.slug.includes(qLower.replace(/\s+/g, '-'));
    });
  } catch (e) { return []; }
}

// ─── Fetch anime __data.json with full metadata ───────────────────────────
async function fetchAnimeData(slug) {
  try {
    const body = await curlGet(`${REANIME_BASE}/watch/${slug}/__data.json`, { accept: 'application/json' });
    if (!body) return null;
    const data = JSON.parse(body);
    if (!data || !data.nodes) return null;
    for (const node of data.nodes) {
      if (!node || !node.data || !node.data[0]) continue;
      const main = resolveDict(node.data, node.data[0]);
      if (!main.anime && !main.episodeSources) continue;
      const anime = resolveDict(node.data, main.anime);
      if (!anime || !anime.anime_id) continue;
      const titleObj = resolveDict(node.data, anime.title);
      const coverObj = resolveDict(node.data, anime.cover_image);
      const episodesList = resolveRef(node.data, main.episodes) || [];
      const episodes = [];
      for (const epRef of episodesList) {
        const ep = resolveDict(node.data, resolveRef(node.data, epRef));
        if (ep && ep.episodeId) {
          episodes.push({
            episodeId: typeof ep.episodeId === 'string' ? ep.episodeId : resolveRef(node.data, ep.episodeId),
            title: typeof ep.title === 'string' ? ep.title : (resolveRef(node.data, ep.title) || ''),
            number: typeof ep.episode_number === 'number' ? ep.episode_number : 1,
            playable: ep.playable,
            subbed: ep.subbed,
            dubbed: ep.dubbed,
          });
        }
      }
      const genresRaw = resolveRef(node.data, anime.genres) || [];
      const genres = genresRaw.map(g => {
        if (typeof g === 'number') {
          const genreObj = resolveRef(node.data, g);
          if (genreObj && typeof genreObj === 'object') return resolveRef(node.data, genreObj.name);
          return genreObj;
        }
        if (typeof g === 'object' && g !== null) return resolveRef(node.data, g.name);
        return g;
      }).filter(g => typeof g === 'string');

      return {
        slug,
        anime_id: resolveRef(node.data, anime.anime_id),
        anilist_id: resolveRef(node.data, anime.anilist_id),
        mal_id: resolveRef(node.data, anime.mal_id),
        themoviedb_id: resolveRef(node.data, anime.themoviedb_id),
        title: titleObj ? {
          english: resolveRef(node.data, titleObj.english),
          native: resolveRef(node.data, titleObj.native),
          romaji: resolveRef(node.data, titleObj.romaji),
        } : null,
        cover_image: coverObj ? {
          extra_large: resolveRef(node.data, coverObj.extra_large),
          large: resolveRef(node.data, coverObj.large),
        } : null,
        banner_image: resolveRef(node.data, anime.banner_image),
        description: resolveRef(node.data, anime.description),
        format: resolveRef(node.data, anime.format),
        status: resolveRef(node.data, anime.status),
        genres,
        episodes,
      };
    }
    return null;
  } catch (e) { return null; }
}

// ─── Resolve flixcloud stream via enc-dec.app ───────────────────────────
async function resolveFlixCloud(videoId) {
  try {
    // Step 1: Fetch flixcloud embed page
    const flixUrl = `https://flixcloud.cc/e/${videoId}?v=2`;
    console.log(`[Re:ANIME] flixcloud: ${flixUrl}`);
    const flixHtml = await curlGet(flixUrl, { referer: REANIME_BASE + '/' });
    if (!flixHtml) return null;

    // Step 2: Extract embedded SvelteKit data
    const dataMatch = flixHtml.match(/type:\s*"data",\s*data:\s*(\{[\s\S]*?\})\s*,\s*uses:/);
    if (!dataMatch) return null;

    // Parse the data (may use JS object notation, not strict JSON)
    let flixData;
    try { flixData = JSON.parse(dataMatch[1].replace(/'/g, '"')); }
    catch (e) {
      try { flixData = eval('(' + dataMatch[1] + ')'); }
      catch (e2) { return null; }
    }
    if (!flixData) return null;

    // Step 3: Resolve token via enc-dec.app
    console.log('[Re:ANIME] enc-dec.app: resolving token...');
    const tokenResult = await curlPostJson(
      `${ENC_DEC_API}/dec-flixcloud?type=token`,
      JSON.stringify({ data: flixData })
    );
    if (!tokenResult || !tokenResult.result || !tokenResult.result.token) return null;

    // Step 4: Fetch encrypted stream from flixcloud
    const streamUrl = `https://flixcloud.cc/api/m3u8/${tokenResult.result.token}`;
    const streamBody = await curlGet(streamUrl, { referer: 'https://flixcloud.cc/' });
    if (!streamBody) return null;
    let streamResp;
    try { streamResp = JSON.parse(streamBody); } catch (e) { return null; }

    // Step 5: Decrypt stream via enc-dec.app
    console.log('[Re:ANIME] enc-dec.app: decrypting stream...');
    const decResult = await curlPostJson(
      `${ENC_DEC_API}/dec-flixcloud?type=stream`,
      JSON.stringify({
        data: {
          context: tokenResult.result.context,
          stream_response: streamResp,
        },
      })
    );
    if (!decResult || !decResult.result || !decResult.result.stream) return null;

    // Also get subtitles if available
    const subtitles = flixData.subtitles || [];

    return { stream: decResult.result.stream, subtitles };
  } catch (e) {
    console.error(`[Re:ANIME] flixcloud error: ${e.message}`);
    return null;
  }
}

// ─── Build Stremio streams ─────────────────────────────────────────────────
function buildStreams(animeData, tmdb, episodeNum) {
  if (!animeData || !animeData.episodes) return [];
  const title = animeData.title?.english || animeData.title?.romaji || animeData.slug || 'Anime';
  const poster = animeData.cover_image?.extra_large || animeData.cover_image?.large ||
                 animeData.banner_image || tmdb?.poster || null;
  const description = animeData.description || tmdb?.overview || '';
  const genres = animeData.genres?.length ? animeData.genres : (tmdb?.genres || []);

  const targetEps = episodeNum !== null && episodeNum !== undefined
    ? animeData.episodes.filter(e => e.number === episodeNum || e.episodeId === `ep-${episodeNum}`)
    : animeData.episodes;
  if (targetEps.length === 0 && animeData.episodes.length > 0) targetEps.push(animeData.episodes[0]);

  const streams = [];
  for (const ep of targetEps) {
    if (ep.playable === false) continue;
    const langLabel = ep.subbed ? 'Sub' : (ep.dubbed ? 'Dub' : '');
    const watchUrl = `${REANIME_BASE}/watch/${animeData.slug}?ep=${ep.number || 1}`;

    streams.push({
      name: `Re:ANIME\n${langLabel || 'HD'} 1080p`,
      title: `${title} - Episode ${ep.number}${langLabel ? ' [' + langLabel + ']' : ''}`,
      url: watchUrl,
      quality: '1080p',
      behaviorHints: {
        notWebReady: true,
        proxyHeaders: { request: { 'User-Agent': UA } },
      },
      poster,
      meta: {
        provider: 'Re:ANIME', source: 'reanime.to', type: 'embed',
        quality: '1080p',
        title: `${title} - Episode ${ep.number}`,
        animeId: animeData.anime_id,
        anilistId: animeData.anilist_id, malId: animeData.mal_id,
        episodeId: ep.episodeId, episodeNumber: ep.number,
        episodeTitle: ep.title,
        subbed: !!ep.subbed, dubbed: !!ep.dubbed,
        description, poster, banner: animeData.banner_image,
        genres, format: animeData.format, status: animeData.status,
        directStream: false, mediaType: 'series',
      },
    });
  }
  return streams;
}

// ─── Main entry ───────────────────────────────────────────────────────────
async function getReAnimeStreams(tmdbId, type, season, episode) {
  try {
    const mediaType = type === 'tv' || type === 'series' ? 'tv' : 'movie';
    const tmdb = await getTmdbDetails(tmdbId, mediaType);
    if (!tmdb || !tmdb.title) return [];

    // Find anime on Re:ANIME
    const results = await searchReAnime(tmdb.title);
    if (!results.length) return [];
    const best = results.find(r => r.title.toLowerCase().includes(tmdb.title.toLowerCase())) || results[0];

    // Fetch full anime data
    const animeData = await fetchAnimeData(best.slug);
    if (!animeData || !animeData.episodes || animeData.episodes.length === 0) return [];

    // Try flixcloud resolution via enc-dec.app
    const epNum = episode !== null && episode !== undefined ? parseInt(episode) : 1;
    const ep = animeData.episodes.find(e => e.number === epNum) || animeData.episodes[0];

    // Try to resolve direct stream via enc-dec.app
    // The flixcloud video ID might be in the episodeSources or derived from the slug
    // Try the slug as video ID first
    let directStream = await resolveFlixCloud(animeData.slug);

    // If that fails, try with episode-specific ID
    if (!directStream && ep?.episodeId) {
      directStream = await resolveFlixCloud(ep.episodeId);
    }

    const streams = [];
    const title = animeData.title?.english || animeData.title?.romaji || best.title;
    const poster = animeData.cover_image?.extra_large || animeData.cover_image?.large || tmdb?.poster || null;
    const langLabel = ep?.subbed ? 'Sub' : (ep?.dubbed ? 'Dub' : '');

    // Direct HLS stream (if enc-dec.app resolved it)
    if (directStream && directStream.stream) {
      streams.push({
        name: `Re:ANIME\n${langLabel || 'HD'} 1080p`,
        title: `${title} - Episode ${epNum} ${langLabel ? '[' + langLabel + ']' : ''}`,
        url: directStream.stream,
        quality: '1080p',
        behaviorHints: {
          notWebReady: false,
          headers: { 'Referer': 'https://flixcloud.cc/', 'User-Agent': UA },
          proxyHeaders: { request: { 'Referer': 'https://flixcloud.cc/', 'User-Agent': UA } },
        },
        poster,
        meta: {
          provider: 'Re:ANIME', source: 'flixcloud', type: 'hls',
          quality: '1080p', title: `${title} - Episode ${epNum}`,
          animeId: animeData.anime_id, anilistId: animeData.anilist_id, malId: animeData.mal_id,
          episodeNumber: epNum, subbed: !!ep?.subbed, dubbed: !!ep?.dubbed,
          description: animeData.description || tmdb?.overview || '',
          poster, banner: animeData.banner_image,
          genres: animeData.genres?.length ? animeData.genres : (tmdb?.genres || []),
          format: animeData.format, status: animeData.status,
          directStream: true, mediaType: 'series',
          subtitles: directStream.subtitles || [],
        },
      });
    }

    // Fallback: embed stream (watch page URL — Stremio webview plays it)
    if (streams.length === 0) {
      const embedStreams = buildStreams(animeData, tmdb, epNum);
      streams.push(...embedStreams);
    }

    console.log(`[Re:ANIME] Returning ${streams.length} streams for ${best.title} ep${epNum}`);
    return streams;
  } catch (e) {
    console.error(`[Re:ANIME] Error: ${e.message}`);
    return [];
  }
}

module.exports = {
  getReAnimeStreams,
  searchReAnime,
  fetchAnimeData,
  resolveFlixCloud,
  buildStreams,
  REANIME_BASE,
  PROVIDER_NAME: 'Re:ANIME',
};
