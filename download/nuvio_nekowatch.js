/**
 * NekoWatch Scraper — nekowatch.xyz
 * ==================================
 * Flow:
 *   1. TMDB → title → AniList search → get AniList ID + MAL ID
 *   2. For each working provider, call:
 *      /api/anime/watch/{provider}/{anilistId}/{sub|dub}/{episode}
 *   3. For embed streams (megaplay.buzz), resolve to HLS via getSources API
 *   4. For HLS streams, return directly
 *   5. For MP4 streams, return directly
 *
 * Providers (8 total, 5-6 working):
 *   2dhive, anidbapp, anikoto, animegg, animenosub, anineko, anizone, reanime
 *
 * Supports: SUB + DUB, multiple servers per episode
 * Stream types: HLS (m3u8), MP4, embed (resolved to HLS)
 *
 * Pure Node.js fetch() — no Playwright, no browser, no curl
 */
'use strict';

const TMDB_API_KEY = '439c478a771f35c05022f9feabcca01c';
const TMDB_BASE = 'https://api.themoviedb.org/3';
const NW_API = 'https://nekowatch.xyz/api/anime';
const ANILIST_GQL = 'https://graphql.anilist.co';
const MEGAPLAY = 'https://megaplay.buzz';
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';

const NW_HEADERS = {
  'User-Agent': UA,
  'X-Requested-With': 'NekoWatch',
  'Accept': 'application/json',
};

const PROVIDERS = ['2dhive', 'anikoto', 'animegg', 'animenosub', 'anineko', 'anidbapp', 'anizone', 'reanime'];

// ─── HTTP Helpers ────────────────────────────────────────────────────────────
async function fetchJson(url, headers = {}) {
  try {
    const res = await fetch(url, { headers: { ...NW_HEADERS, ...headers } });
    if (!res.ok) return null;
    return await res.json();
  } catch (e) { return null; }
}

async function fetchText(url, headers = {}) {
  try {
    const res = await fetch(url, { headers: { 'User-Agent': UA, ...headers } });
    if (!res.ok) return '';
    return await res.text();
  } catch (e) { return ''; }
}

// ─── TMDB ────────────────────────────────────────────────────────────────────
async function getTmdbInfo(tmdbId, mediaType) {
  const type = mediaType === 'tv' ? 'tv' : 'movie';
  return await fetchJson(`${TMDB_BASE}/${type}/${tmdbId}?api_key=${TMDB_API_KEY}&language=en-US`, {});
}

// ─── AniList Search ──────────────────────────────────────────────────────────
async function getAniListId(title) {
  const query = `query ($search: String) { Media(search: $search, type: ANIME) { id idMal title { romaji english } } }`;
  try {
    const res = await fetch(ANILIST_GQL, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
      body: JSON.stringify({ query, variables: { search: title } }),
    });
    const data = await res.json();
    return data?.data?.Media || null;
  } catch (e) { return null; }
}

// ─── Get Episodes ────────────────────────────────────────────────────────────
async function getEpisodes(anilistId) {
  const data = await fetchJson(`${NW_API}/episodes/${anilistId}`);
  if (!data) return [];
  // The response contains episode mappings
  return data.mappings || data.episodes || [];
}

// ─── Get Streams from Provider ───────────────────────────────────────────────
async function getProviderStreams(provider, anilistId, audio, episode) {
  const url = `${NW_API}/watch/${provider}/${anilistId}/${audio}/${episode}`;
  console.log(`[NekoWatch] ${provider} ${audio} ep${episode}: ${url}`);
  const data = await fetchJson(url);
  if (!data || !data.streams) return [];
  return data.streams;
}

// ─── Resolve megaplay.buzz Embed to HLS ──────────────────────────────────────
async function resolveMegaPlay(embedUrl) {
  const html = await fetchText(embedUrl, { 'Referer': 'https://nekowatch.xyz/' });
  if (!html) return null;

  const idMatch = html.match(/data-id="(\d+)"/);
  if (!idMatch) return null;

  const apiUrl = `${MEGAPLAY}/stream/getSources?id=${idMatch[1]}`;
  const data = await fetchJson(apiUrl, { 'Referer': embedUrl, 'X-Requested-With': 'XMLHttpRequest' });
  if (!data || !data.sources) return null;

  return {
    url: data.sources.file,
    subtitles: data.tracks || [],
  };
}

// ─── Build Stream Object ─────────────────────────────────────────────────────
function buildStream(url, type, provider, audio, serverName, animeTitle, episode, subtitles) {
  const isHls = url.includes('.m3u8');
  const isMp4 = url.includes('.mp4');
  const quality = isHls ? '1080p' : '720p';
  const streamType = isHls ? 'hls' : (isMp4 ? 'mp4' : 'embed');
  const subs = (subtitles || []).map(s => ({
    url: s.file,
    lang: s.label || 'en',
    name: s.label || 'English',
  }));

  return {
    name: `NekoWatch\n${audio.toUpperCase()} ${quality}`,
    title: `${animeTitle} - Episode ${episode} (${audio.toUpperCase()}) — ${serverName}`,
    url,
    quality,
    behaviorHints: {
      notWebReady: false,
      ...(isHls && { headers: { 'Referer': 'https://megaplay.buzz/' } }),
      ...(isMp4 && { headers: { 'Referer': 'https://www.animegg.org/' } }),
    },
    subtitles: subs,
    meta: {
      provider: 'NekoWatch',
      source: provider,
      server: serverName,
      type: streamType,
      quality,
      audio: audio === 'dub' ? 'english' : 'japanese',
      language: [audio === 'dub' ? 'en' : 'ja'],
      category: audio,
      title: animeTitle,
      episode,
      directStream: isHls || isMp4,
      mediaType: 'movie',
    },
  };
}

// ─── Main: getStreams ────────────────────────────────────────────────────────
async function getStreams(tmdbId, mediaType, season, episode) {
  console.log(`[NekoWatch] getStreams: ${tmdbId} ${mediaType} S${season || '?'}E${episode || '?'}`);
  try {
    // 1. Get title from TMDB
    const meta = await getTmdbInfo(tmdbId, mediaType);
    if (!meta) return [];
    const title = mediaType === 'tv' ? meta.name : meta.title;
    console.log(`[NekoWatch] Title: ${title}`);

    // 2. Get AniList ID
    const aniData = await getAniListId(title);
    if (!aniData || !aniData.id) {
      console.log('[NekoWatch] No AniList match');
      return [];
    }
    console.log(`[NekoWatch] AniList ID: ${aniData.id}`);

    // 3. Get streams from all providers (sub + dub)
    const ep = parseInt(episode) || 1;
    const allStreams = [];
    const seenUrls = new Set();

    for (const audio of ['sub', 'dub']) {
      for (const provider of PROVIDERS) {
        try {
          const streams = await getProviderStreams(provider, aniData.id, audio, ep);
          for (const s of streams) {
            if (!s.url || seenUrls.has(s.url)) continue;

            let streamUrl = s.url;
            let subtitles = [];

            // Resolve megaplay.buzz embeds to HLS
            if (s.url.includes('megaplay.buzz') && s.type === 'embed') {
              const resolved = await resolveMegaPlay(s.url);
              if (resolved && resolved.url) {
                streamUrl = resolved.url;
                subtitles = resolved.subtitles;
              } else {
                continue; // skip if can't resolve
              }
            }

            // Skip proxy URLs that return 403
            if (s.url.includes('workers.dev/m3u8-proxy')) continue;

            // Skip non-http URLs
            if (!streamUrl.startsWith('http')) continue;

            seenUrls.add(streamUrl);
            allStreams.push(buildStream(streamUrl, s.type, provider, audio, s.server, title, ep, subtitles));
          }
        } catch (e) {
          // Continue to next provider on error
        }
      }
    }

    // Sort: sub first, then dub; HLS first, then MP4
    allStreams.sort((a, b) => {
      if (a.meta.category === 'sub' && b.meta.category === 'dub') return -1;
      if (a.meta.category === 'dub' && b.meta.category === 'sub') return 1;
      if (a.meta.type === 'hls' && b.meta.type !== 'hls') return -1;
      if (a.meta.type !== 'hls' && b.meta.type === 'hls') return 1;
      return 0;
    });

    console.log(`[NekoWatch] Returning ${allStreams.length} stream(s)`);
    return allStreams;
  } catch (e) {
    console.error(`[NekoWatch] Error: ${e.message}`);
    return [];
  }
}

// ─── Export ───────────────────────────────────────────────────────────────────
module.exports = { getStreams, getAniListId, getEpisodes, getProviderStreams, resolveMegaPlay, PROVIDER_NAME: 'NekoWatch' };
