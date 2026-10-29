/**
 * Antova (AniLibria / AniLiberty) Scraper — Updated
 * =================================================
 * AniLibria anime dub group. API at anilibria.top/api/v1
 *
 * Flow:
 *   1. TMDB → title → search AniLibria API
 *   2. Get release details → extract HLS stream URLs
 *   3. Return HLS streams (1080p, 720p, 480p) with enriched metadata
 *
 * API: https://anilibria.top/api/v1
 *   - Search: /app/search/releases?query={query}&limit={n}
 *   - Release: /anime/releases/{alias}
 *   - Latest: /anime/releases/latest?limit={n}
 *
 * Stream URL: https://cache.libria.fun/videos/media/ts/{releaseId}/{episode}/{quality}/{md5hash}.m3u8
 *
 * Supports: SUB (Russian dub with subtitles), movies, TV, OVA
 * Pure Node.js fetch() — no Playwright, no axios, no curl
 */
'use strict';

const TMDB_API_KEY = '439c478a771f35c05022f9feabcca01c';
const TMDB_BASE = 'https://api.themoviedb.org/3';
const API_BASE = 'https://anilibria.top/api/v1';
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';

// ─── HTTP Helpers ────────────────────────────────────────────────────────────
async function fetchJson(url, headers = {}) {
  try {
    const res = await fetch(url, { headers: { 'User-Agent': UA, 'Accept': 'application/json', ...headers } });
    if (!res.ok) return null;
    return await res.json();
  } catch (e) { return null; }
}

// ─── TMDB ────────────────────────────────────────────────────────────────────
async function getTmdbInfo(tmdbId, mediaType) {
  const type = mediaType === 'tv' ? 'tv' : 'movie';
  return await fetchJson(`${TMDB_BASE}/${type}/${tmdbId}?api_key=${TMDB_API_KEY}&language=en-US`);
}

// ─── Search AniLibria ────────────────────────────────────────────────────────
async function searchReleases(query, limit = 20) {
  const url = `${API_BASE}/app/search/releases?query=${encodeURIComponent(query)}&limit=${limit}`;
  console.log(`[Antova] Search: ${url}`);
  const data = await fetchJson(url);
  if (!data || !Array.isArray(data)) return [];
  console.log(`[Antova] Found ${data.length} results`);
  return data;
}

// ─── Get Release Details ─────────────────────────────────────────────────────
async function getRelease(alias) {
  const url = `${API_BASE}/anime/releases/${alias}`;
  console.log(`[Antova] Release: ${url}`);
  const data = await fetchJson(url);
  if (!data) return null;
  return data;
}

// ─── Get Latest Releases ─────────────────────────────────────────────────────
async function getLatestReleases(limit = 20) {
  const url = `${API_BASE}/anime/releases/latest?limit=${limit}`;
  const data = await fetchJson(url);
  if (!data || !Array.isArray(data)) return [];
  return data;
}

// ─── Find by Title (fuzzy match) ─────────────────────────────────────────────
async function findByTitle(title, year) {
  // Normalize: remove special chars, replace ū→u, etc.
  const normalize = (s) => s.toLowerCase()
    .replace(/[\u016B\u016A]/g, 'u').replace(/[\u014D\u014C]/g, 'o').replace(/[\u0101\u0100]/g, 'a')
    .replace(/[\u0113\u0112]/g, 'e').replace(/[\u012B\u012A]/g, 'i')
    .replace(/[^a-z0-9\s]/g, '').replace(/\s+/g, ' ').trim();
  
  const cleanTitle = normalize(title);
  // Search with normalized title (handles ū→u, ō→o, etc.)
  let results = await searchReleases(cleanTitle, 10);
  
  // If no results, try with original title
  if (!results.length) {
    results = await searchReleases(title, 10);
  }
  if (!results.length) return null;

  let best = null;
  let bestScore = 0;

  for (const r of results) {
    const engName = normalize(r.name?.english || r.name?.main || '');
    const mainName = normalize(r.name?.main || '');
    const score = engName === cleanTitle ? 100 :
                  engName.includes(cleanTitle) || cleanTitle.includes(engName) ? 80 :
                  engName.startsWith(cleanTitle.slice(0, Math.min(cleanTitle.length, 10))) ? 70 :
                  mainName.includes(cleanTitle) || cleanTitle.includes(mainName) ? 50 : 0;
    const yearBonus = year && r.year === year ? 10 : 0;
    const totalScore = score + yearBonus;
    if (totalScore > bestScore) {
      bestScore = totalScore;
      best = r;
    }
  }

  if (bestScore >= 50) {
    console.log(`[Antova] Best match: ${best.name?.english} (score: ${bestScore})`);
    return best;
  }
  return null;
}

// ─── Build Stream Object ─────────────────────────────────────────────────────
function buildStream(url, quality, animeTitle, episode, releaseId) {
  return {
    name: `Antova\n${quality}`,
    title: `${animeTitle} - Episode ${episode} (${quality})`,
    url,
    quality,
    behaviorHints: {
      notWebReady: false,
    },
    meta: {
      provider: 'Antova',
      source: 'cache.libria.fun',
      server: 'cache.libria.fun',
      type: 'hls',
      quality,
      audio: 'russian', // AniLibria provides Russian dub
      language: ['ru'],
      category: 'sub',
      title: animeTitle,
      episode,
      releaseId,
      directStream: true,
      mediaType: 'movie',
    },
  };
}

// ─── Main: getStreams ────────────────────────────────────────────────────────
async function getStreams(tmdbId, mediaType, season, episode) {
  console.log(`[Antova] getStreams: ${tmdbId} ${mediaType} S${season || '?'}E${episode || '?'}`);
  try {
    // 1. Get title from TMDB
    const meta = await getTmdbInfo(tmdbId, mediaType);
    if (!meta) return [];
    const title = mediaType === 'tv' ? meta.name : meta.title;
    const year = parseInt((mediaType === 'tv' ? meta.first_air_date : meta.release_date || '').substring(0, 4)) || null;
    console.log(`[Antova] Title: ${title} (${year})`);

    // 2. Search AniLibria
    const match = await findByTitle(title, year);
    if (!match) {
      console.log('[Antova] No match found');
      return [];
    }

    // 3. Get release details
    const release = await getRelease(match.alias || match.code);
    if (!release) return [];

    const animeTitle = release.name?.english || release.name?.main || title;
    const episodes = release.episodes || {};
    const ep = parseInt(episode) || 1;
    const epKey = String(ep - 1); // AniLibria uses 0-indexed episodes

    // Find the episode (try 0-indexed, then 1-indexed)
    let epData = episodes[epKey] || episodes[String(ep)] || Object.values(episodes)[0];
    if (!epData) {
      console.log(`[Antova] Episode ${ep} not found`);
      return [];
    }

    // 4. Build streams from HLS URLs
    const streams = [];
    const qualityMap = {
      hls_1080: '1080p',
      hls_720: '720p',
      hls_480: '480p',
    };

    for (const [key, quality] of Object.entries(qualityMap)) {
      if (epData[key]) {
        streams.push(buildStream(epData[key], quality, animeTitle, ep, release.id));
      }
    }

    // Sort by quality (1080p first)
    const qualityOrder = { '1080p': 0, '720p': 1, '480p': 2 };
    streams.sort((a, b) => (qualityOrder[a.quality] ?? 3) - (qualityOrder[b.quality] ?? 3));

    console.log(`[Antova] Returning ${streams.length} stream(s)`);
    return streams;
  } catch (e) {
    console.error(`[Antova] Error: ${e.message}`);
    return [];
  }
}

// ─── Export ───────────────────────────────────────────────────────────────────
module.exports = {
  getStreams,
  searchReleases,
  getRelease,
  getLatestReleases,
  findByTitle,
  PROVIDER_NAME: 'Antova',
};
