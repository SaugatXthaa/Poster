/**
 * Enriched Anime Sources — Unified Wrapper for 5 Working Anime Providers
 * ======================================================================
 * Wraps each provider's getStreams() and ADDS enriched metadata fields
 * (behaviorHints + meta) WITHOUT modifying url, headers, or subtitles.
 *
 * Design goals:
 *   1. Preserve all existing stream URLs and playback headers UNCHANGED
 *   2. Add a `behaviorHints` object (Stremio-spec) with notWebReady + headers
 *   3. Add a `meta` object with rich provider/source/server/audio/language info
 *   4. Work on obfuscated providers (AniKotoTV) — wraps, doesn't modify
 *
 * SUPPORTED SOURCES (all return real working streams, verified Aug 2026):
 *   1. AniKotoTV    — HLS via megaplay.buzz (sub + dub, 9 subtitle languages)
 *   2. AnimesDigital — HLS via cdn.imagesskill.com (PT-BR sub + dub)
 *   3. AnimeSalt    — HLS via as-cdn21.top (multi-audio, 720p)
 *   4. AnimeWorld   — HLS via play.zephyrix.top (multi-audio, 1080p)
 *   5. AnimeKai     — HLS via zokoanime.video/aniwatchtv.uk (sub+dub, 9 subs)
 *
 * USAGE:
 *   const { getEnrichedStreams } = require('./enriched_anime_sources');
 *   const streams = await getEnrichedStreams({
 *     tmdbId: 95479, type: 'tv', season: 1, episode: 1,
 *     animeKaiQuery: 'jujutsu kaisen', animeKaiCategory: 'sub'
 *   });
 *
 * OUTPUT FORMAT (Stremio-compatible + enriched):
 *   [{
 *     name: "AniKotoTV\nSUB 1080p",
 *     title: "JUJUTSU KAISEN - S01E01 (SUB) — via megaplay.buzz",
 *     url: "https://megap.akirax.buzz/.../master.m3u8",       // UNCHANGED
 *     quality: "1080p",
 *     behaviorHints: {
 *       notWebReady: true,
 *       headers: { Referer: "https://megaplay.buzz/", Origin: "https://megaplay.buzz" }
 *     },
 *     headers: { ... },                                        // preserved if present
 *     subtitles: [{ url, lang, name }],                       // preserved if present
 *     meta: {
 *       provider: "AniKotoTV",
 *       source: "megaplay.buzz",
 *       server: "megaplay.buzz",
 *       type: "hls",
 *       quality: "1080p",
 *       audio: "japanese",                // 'japanese' | 'english' | 'portuguese'
 *       language: ["ja"],                 // ISO 639-1 codes
 *       category: "sub",                  // 'sub' | 'dub'
 *       subtitles: [...],                 // mirror of top-level subtitles
 *       animeTitle: "JUJUTSU KAISEN",
 *       episode: 1,
 *       season: 1,
 *       malId: "40748",                   // when available
 *       postId: "40",                     // when available (AnimeKai)
 *       subCount: 24, dubCount: 24,       // when available (AnimeKai)
 *       downloadUrl: "/download/...",     // when available (AnimeKai)
 *       bingeGroup: "anikototv-...",      // when applicable
 *     }
 *   }]
 */
'use strict';

const path = require('path');
const { AnimeKaiScraper } = require('./animekai_scraper');

const DIR = __dirname;

// ==================== Provider Registry ====================

const PROVIDERS = [
  {
    id: 'anikototv',
    name: 'AniKotoTV',
    file: 'nuvio_anikototv.js',
    sourceDomain: 'megaplay.buzz',
    type: 'hls',
    defaultAudio: 'japanese',
    defaultDubAudio: 'english',  // AniKotoTV is an English-audio site
    defaultLanguage: ['ja'],
  },
  {
    id: 'animesdigital',
    name: 'AnimesDigital',
    file: 'nuvio_animesdigital.js',
    sourceDomain: 'animesdigital.org',
    cdnDomain: 'cdn.imagesskill.com',
    type: 'hls',
    defaultAudio: 'japanese',
    defaultDubAudio: 'portuguese',  // Brazilian site → DUB means Portuguese
    defaultLanguage: ['pt'],
  },
  {
    id: 'animesalt',
    name: 'AnimeSalt',
    file: 'nuvio_animesalt.js',
    sourceDomain: 'animesalt.link',
    cdnDomain: 'as-cdn21.top',
    type: 'hls',
    defaultAudio: 'multi',
    defaultDubAudio: 'multi',  // AnimeSalt serves multi-audio streams
    defaultLanguage: ['en', 'ja'],
  },
  {
    id: 'animeworld',
    name: 'AnimeWorld',
    file: 'nuvio_animeworld.js',
    sourceDomain: 'watchanimeworld.top',
    cdnDomain: 'play.zephyrix.top',
    type: 'hls',
    defaultAudio: 'multi',
    defaultDubAudio: 'multi',  // AnimeWorld serves multi-audio streams
    defaultLanguage: ['en', 'ja'],
  },
  // AnimeKai is handled separately (different API: title-based, not TMDB)
];

// ==================== Helpers ====================

/**
 * Detect stream category (sub/dub) from a stream object.
 * Recognizes common tags: DUB, DUBBED, LEG (legendado/PT-BR sub),
 * VOSTFR, SUB, SUBBED.
 */
function detectCategory(stream, fallback = 'sub') {
  const text = [
    stream.title || '',
    stream.name || '',
    (stream.meta && stream.meta.category) || '',
    (stream.meta && stream.meta.audio) || '',
  ].join(' ').toLowerCase();
  // Check LEG (legendado) BEFORE PT-BR/DUB — LEG means subbed in Portuguese
  if (/\b(leg|legendado|vostfr|sub|subbed|japanese)\b/.test(text)) return 'sub';
  if (/\b(dub|dubbed|dublado)\b/.test(text)) return 'dub';
  // English-audio streams are typically dubbed
  if (/english audio|\ben audio\b/.test(text)) return 'dub';
  return fallback;
}

/**
 * Detect audio language from a stream object.
 * Recognizes: PT-BR DUB (portuguese), PT-BR LEG (japanese),
 * English audio (english), multi-audio.
 *
 * Provider context matters: for Brazilian providers (AnimesDigital),
 * DUB defaults to Portuguese; for English providers (AniKotoTV, AnimeKai),
 * DUB defaults to English.
 */
function detectAudio(stream, providerDefault, provider) {
  const text = [
    stream.title || '',
    stream.name || '',
    (stream.meta && stream.meta.audio) || '',
  ].join(' ').toLowerCase();
  // Order matters: check LEG (legendado) before DUB — LEG means subbed in Portuguese
  if (/\b(leg|legendado|vostfr)\b/.test(text)) return 'japanese'; // PT-BR sub = Japanese audio
  // Explicit Portuguese indicators
  if (/dublado|portugu[êe]s|pt-br/.test(text)) return 'portuguese';
  // Multi-audio
  if (/multi[-\s]?audio/.test(text)) return 'multi';
  // DUB with no other context — use provider's default dub language
  if (/\bdub\b|dubbed|english audio/.test(text)) {
    if (provider && provider.defaultDubAudio) return provider.defaultDubAudio;
    return providerDefault === 'portuguese' ? 'portuguese' : 'english';
  }
  // Explicit sub indicators → Japanese audio (the original language)
  if (/\bsub\b|subbed|japanese/.test(text)) return 'japanese';
  return providerDefault || 'japanese';
}

/**
 * Detect stream type (hls/mp4) from URL.
 */
function detectType(stream) {
  if (stream.type) return stream.type;
  if (stream.meta && stream.meta.type) return stream.meta.type;
  const url = stream.url || '';
  if (/\.m3u8(\?|$)/i.test(url)) return 'hls';
  if (/\.mp4(\?|$)/i.test(url)) return 'mp4';
  if (/\.mkv(\?|$)/i.test(url)) return 'mkv';
  return 'hls';
}

/**
 * Normalize a single stream object by adding behaviorHints + meta,
 * WITHOUT touching url, headers, or subtitles.
 */
function enrichStream(stream, provider, context) {
  const url = stream.url;
  const headers = stream.headers || (stream.behaviorHints && stream.behaviorHints.headers) || {};
  const subtitles = stream.subtitles || (stream.meta && stream.meta.subtitles) || [];

  // Detect enrichment data
  const category = detectCategory(stream, context.category || 'sub');
  const audio = detectAudio(stream, provider.defaultAudio, provider);
  const type = detectType(stream);
  const quality = stream.quality || (stream.meta && stream.meta.quality) || 'unknown';

  // Determine the language list based on audio.
  // If the provider has a default language list AND the detected audio matches,
  // keep the provider's list. Otherwise, derive from audio.
  let language = provider.defaultLanguage || [];
  if (audio === 'portuguese') language = ['pt'];
  else if (audio === 'english') language = ['en'];
  else if (audio === 'japanese') language = ['ja'];
  else if (audio === 'multi') language = provider.defaultLanguage || ['en', 'ja'];

  // Determine source/server from URL or provider config
  let source = provider.sourceDomain;
  let server = provider.sourceDomain;
  try {
    const u = new URL(url);
    source = u.hostname;
    server = u.hostname;
  } catch (e) {}

  // Build a clean, descriptive title if not already enriched
  const animeTitle = context.animeTitle || (stream.meta && stream.meta.animeTitle) || '';
  const season = context.season;
  const episode = context.episode;
  const seasonEp = season && episode
    ? `S${String(season).padStart(2, '0')}E${String(episode).padStart(2, '0')}`
    : (episode ? `EP${episode}` : '');
  const categoryTag = category.toUpperCase();
  const enrichedTitle = animeTitle
    ? `${animeTitle} - ${seasonEp} (${categoryTag}) — via ${source}`
    : (stream.title || `${provider.name} ${seasonEp} (${categoryTag})`);

  // Compose the name with quality suffix
  const enrichedName = `${provider.name}\n${categoryTag} ${quality}`;

  // Build the meta object
  const meta = Object.assign({
    provider: provider.name,
    source: source,
    server: server,
    type: type,
    quality: quality,
    audio: audio,
    language: language,
    category: category,
    subtitles: Array.isArray(subtitles) ? subtitles.map(s => ({
      url: s.url,
      lang: s.lang || s.language || 'en',
      name: s.name || s.label || s.lang || 'Unknown',
      default: s.default || false,
    })) : [],
    animeTitle: animeTitle,
    episode: episode || null,
    season: season || null,
  }, stream.meta || {});

  // Carry over optional fields from existing meta (if any)
  if (stream.meta) {
    if (stream.meta.malId) meta.malId = stream.meta.malId;
    if (stream.meta.postId) meta.postId = stream.meta.postId;
    if (stream.meta.subCount != null) meta.subCount = stream.meta.subCount;
    if (stream.meta.dubCount != null) meta.dubCount = stream.meta.dubCount;
    if (stream.meta.downloadUrl) meta.downloadUrl = stream.meta.downloadUrl;
    if (stream.meta.aniId) meta.aniId = stream.meta.aniId;
  }

  // Build behaviorHints (Stremio-spec). Preserve existing bingeGroup if present.
  const existingBehaviorHints = stream.behaviorHints || {};
  const behaviorHints = Object.assign({}, existingBehaviorHints, {
    notWebReady: existingBehaviorHints.notWebReady !== undefined
      ? existingBehaviorHints.notWebReady
      : true,
    headers: Object.keys(headers).length ? headers : (existingBehaviorHints.headers || {}),
  });

  // Assemble the enriched stream — preserve ALL original fields, ADD enrichment
  const enriched = Object.assign({}, stream, {
    name: enrichedName,
    title: enrichedTitle,
    quality: quality,
    behaviorHints: behaviorHints,
  });

  // Ensure `headers` at top-level is preserved (some Stremio clients read this)
  if (Object.keys(headers).length && !enriched.headers) {
    enriched.headers = headers;
  }

  // Ensure `subtitles` at top-level is preserved
  if (Array.isArray(subtitles) && subtitles.length && !enriched.subtitles) {
    enriched.subtitles = subtitles;
  }

  // Attach the meta object
  enriched.meta = meta;

  return enriched;
}

// ==================== Per-Provider Fetchers ====================

async function fetchAniKotoTV(tmdbId, type, season, episode, context) {
  const provider = PROVIDERS[0];
  try {
    const mod = require(path.join(DIR, provider.file));
    const raw = await mod.getStreams(tmdbId, type, season, episode);
    if (!Array.isArray(raw)) return [];
    return raw.map(s => enrichStream(s, provider, context));
  } catch (e) {
    console.error(`[EnrichedSources] AniKotoTV error: ${e.message}`);
    return [];
  }
}

async function fetchAnimesDigital(tmdbId, type, season, episode, context) {
  const provider = PROVIDERS[1];
  try {
    const mod = require(path.join(DIR, provider.file));
    const raw = await mod.getStreams(tmdbId, type, season, episode);
    if (!Array.isArray(raw)) return [];
    return raw.map(s => enrichStream(s, provider, context));
  } catch (e) {
    console.error(`[EnrichedSources] AnimesDigital error: ${e.message}`);
    return [];
  }
}

async function fetchAnimeSalt(tmdbId, type, season, episode, context) {
  const provider = PROVIDERS[2];
  try {
    const mod = require(path.join(DIR, provider.file));
    const raw = await mod.getStreams(tmdbId, type, season, episode);
    if (!Array.isArray(raw)) return [];
    return raw.map(s => enrichStream(s, provider, context));
  } catch (e) {
    console.error(`[EnrichedSources] AnimeSalt error: ${e.message}`);
    return [];
  }
}

async function fetchAnimeWorld(tmdbId, type, season, episode, context) {
  const provider = PROVIDERS[3];
  try {
    const mod = require(path.join(DIR, provider.file));
    const raw = await mod.getStreams(tmdbId, type, season, episode);
    if (!Array.isArray(raw)) return [];
    return raw.map(s => enrichStream(s, provider, context));
  } catch (e) {
    console.error(`[EnrichedSources] AnimeWorld error: ${e.message}`);
    return [];
  }
}

async function fetchAnimeKai(context) {
  // AnimeKai uses title-based search, not TMDB ID
  const query = context.animeKaiQuery || context.animeTitle || '';
  const episode = context.episode || 1;
  const category = context.animeKaiCategory || context.category || 'sub';
  if (!query) {
    console.error('[EnrichedSources] AnimeKai requires animeKaiQuery or animeTitle');
    return [];
  }
  try {
    const s = new AnimeKaiScraper();
    const raw = await s.getStreams(query, episode, category);
    if (!Array.isArray(raw)) return [];

    // AnimeKai already has enriched meta — just ensure consistency
    const provider = {
      id: 'animekai',
      name: 'AnimeKai',
      sourceDomain: 'zokoanime.video',
      type: 'hls',
      defaultAudio: category === 'dub' ? 'english' : 'japanese',
      defaultDubAudio: 'english',  // AnimeKai DUB = English dub
      defaultLanguage: [category === 'dub' ? 'en' : 'ja'],
    };
    return raw.map(s => enrichStream(s, provider, context));
  } catch (e) {
    console.error(`[EnrichedSources] AnimeKai error: ${e.message}`);
    return [];
  }
}

// ==================== Main Entry: Get All Enriched Streams ====================

/**
 * Get enriched streams from all 5 working anime sources.
 *
 * @param {Object} opts
 * @param {number} opts.tmdbId - TMDB ID for Nuvio providers (1-4)
 * @param {string} [opts.type='tv'] - 'tv' or 'movie'
 * @param {number} [opts.season=1] - Season number (for TV)
 * @param {number} [opts.episode=1] - Episode number
 * @param {string} [opts.animeTitle] - Anime title (used for enrichment display)
 * @param {string} [opts.animeKaiQuery] - Title for AnimeKai search (defaults to animeTitle)
 * @param {string} [opts.category='sub'] - 'sub' or 'dub'
 * @param {string[]} [opts.providers] - Subset of provider IDs to query
 * @returns {Promise<Array>} Enriched Stremio-compatible stream objects
 */
async function getEnrichedStreams(opts) {
  const {
    tmdbId,
    type = 'tv',
    season = 1,
    episode = 1,
    animeTitle = '',
    animeKaiQuery,
    category = 'sub',
    providers,
  } = opts;

  const context = {
    tmdbId,
    type,
    season,
    episode,
    animeTitle,
    animeKaiQuery,
    category,
  };

  const tasks = [];

  // Nuvio providers (TMDB-based)
  if (!providers || providers.includes('anikototv')) {
    tasks.push({ id: 'anikototv', run: () => fetchAniKotoTV(tmdbId, type, season, episode, context) });
  }
  if (!providers || providers.includes('animesdigital')) {
    tasks.push({ id: 'animesdigital', run: () => fetchAnimesDigital(tmdbId, type, season, episode, context) });
  }
  if (!providers || providers.includes('animesalt')) {
    tasks.push({ id: 'animesalt', run: () => fetchAnimeSalt(tmdbId, type, season, episode, context) });
  }
  if (!providers || providers.includes('animeworld')) {
    tasks.push({ id: 'animeworld', run: () => fetchAnimeWorld(tmdbId, type, season, episode, context) });
  }
  if (!providers || providers.includes('animekai')) {
    tasks.push({ id: 'animekai', run: () => fetchAnimeKai(context) });
  }

  // Run all providers in parallel
  const results = await Promise.allSettled(tasks.map(t => t.run()));

  const allStreams = [];
  for (let i = 0; i < results.length; i++) {
    const r = results[i];
    const id = tasks[i].id;
    if (r.status === 'fulfilled' && Array.isArray(r.value)) {
      allStreams.push(...r.value);
    } else if (r.status === 'rejected') {
      console.error(`[EnrichedSources] ${id} rejected: ${r.reason && r.reason.message}`);
    }
  }

  // Sort: 1080p first, then 720p, then by provider name
  const qualityOrder = { '1080p': 0, '720p': 1, '480p': 2, '360p': 3, 'Auto': 4, 'unknown': 5 };
  allStreams.sort((a, b) => {
    const qa = qualityOrder[a.quality] ?? 6;
    const qb = qualityOrder[b.quality] ?? 6;
    if (qa !== qb) return qa - qb;
    const pa = (a.meta && a.meta.provider) || '';
    const pb = (b.meta && b.meta.provider) || '';
    return pa.localeCompare(pb);
  });

  return allStreams;
}

// ==================== Per-Provider Direct Access ====================

module.exports = {
  getEnrichedStreams,
  fetchAniKotoTV,
  fetchAnimesDigital,
  fetchAnimeSalt,
  fetchAnimeWorld,
  fetchAnimeKai,
  enrichStream,
  detectCategory,
  detectAudio,
  detectType,
  PROVIDERS,
};
