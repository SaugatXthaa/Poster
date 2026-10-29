/**
 * 2Peckle Provider — Pengu.uk proxy + Febbox auth token
 * =====================================================
 * Fetches streams up to 4K (2160p / 1080p / 720p) for movies, TV shows,
 * anime, and dramas via the Pengu.uk proxy aggregator.
 *
 * Sources: https://github.com/D3adlyRocket/ShowboxTV/blob/main/providers/2peckle.js
 * Adapted for standalone Node.js (no Playwright, no browser, pure fetch).
 *
 * TOKEN RESOLUTION (in priority order):
 *   1. process.env.TWOPACKLE_TOKEN         (recommended for server/PC)
 *   2. process.env.FEBBOX_COOKIE            (alias)
 *   3. globalThis.SCRAPER_SETTINGS.authToken (Nuvio integration)
 *   4. FALLBACK_AUTH_TOKEN                  (hardcoded emergency)
 *   5. LOCAL_TOKEN_URL                      (Android local HTTP server)
 *
 * USAGE:
 *   const { getStreams } = require('./2peckle');
 *   const streams = await getStreams(693134, 'movie');         // Dune Part Two
 *   const streams = await getStreams(82856, 'tv', 1, 1);       // Mandalorian S01E01
 *   const streams = await getStreams(95479, 'tv', 1, 1);       // Jujutsu Kaisen S01E01 (anime)
 *
 * ENV:
 *   export TWOPACKLE_TOKEN=eyJhbGciOiJIUzI1NiJ9...   # your Febbox JWT
 *
 * OUTPUT (Stremio-compatible + enriched):
 *   [{
 *     name: "2Peckle | 2160p | Hindi 🇮🇳",
 *     title: "🎬 Movie Name - (2024)\n💎 2160p | 🔊 Hindi 🇮🇳 | 💾 14.2 GB\n🎞️ MKV | ⛓️‍💥 2Peckle",
 *     url: "https://...",
 *     behaviorHints: {
 *       notWebReady: true,
 *       proxyHeaders: { request: { "Referer": "https://stremio-moviebox-1.onrender.com/" } }
 *     },
 *     quality: "2160p",
 *     meta: { provider: "2Peckle", quality, language, size, format, audio }
 *   }]
 */
"use strict";

const PROVIDER_NAME = "2Peckle";
const TMDB_API_KEY = "439c478a771f35c05022f9feabcca01c";

// ─── TOKEN SOURCES ─────────────────────────────────────────────────────────────
// Override via env var; default points to Android local HTTP server.
const LOCAL_TOKEN_URL =
  process.env.TWOPACKLE_LOCAL_URL ||
  process.env.PENGU_LOCAL_URL ||
  "http://192.168.1.244:8080/token.txt";

// Emergency fallback (empty by default; populate via env var TWOPACKLE_TOKEN).
const FALLBACK_AUTH_TOKEN = process.env.TWOPACKLE_TOKEN ||
                            process.env.FEBBOX_COOKIE ||
                            process.env.PENGU_TOKEN ||
                            "";

// ─── TOKEN FETCHER ─────────────────────────────────────────────────────────────
function getAuthToken() {
  // 1. Environment variable (highest priority for server/PC use)
  if (FALLBACK_AUTH_TOKEN) {
    console.log(`[${PROVIDER_NAME}] Using token from environment variable`);
    return Promise.resolve(FALLBACK_AUTH_TOKEN.trim());
  }

  // 2. globalThis.SCRAPER_SETTINGS (Nuvio integration)
  try {
    if (typeof globalThis !== "undefined" && globalThis.SCRAPER_SETTINGS && globalThis.SCRAPER_SETTINGS.authToken) {
      console.log(`[${PROVIDER_NAME}] Using token from globalThis.SCRAPER_SETTINGS`);
      return Promise.resolve(String(globalThis.SCRAPER_SETTINGS.authToken).trim());
    }
  } catch (ex) { /* ignore */ }

  // 3. Local HTTP server (Android Simple HTTP Server)
  return fetch(LOCAL_TOKEN_URL)
    .then(resp => {
      if (!resp.ok) throw new Error(`Local server HTTP error: ${resp.status}`);
      return resp.text();
    })
    .then(text => {
      const token = text ? text.trim() : "";
      if (token) {
        console.log(`[${PROVIDER_NAME}] Token successfully fetched from local server`);
        return token;
      }
      throw new Error("Empty token received from local server");
    })
    .catch(e => {
      console.error(`[${PROVIDER_NAME}] No auth token found from any source: ${e.message}`);
      return "";
    });
}

// ─── PENGU.UK URL BUILDER ──────────────────────────────────────────────────────
function get2PeckleBaseUrl(token) {
  const config = {
    source_2peckle: "on",
    res_2160: "on",   // 4K
    res_1080: "on",
    res_720: "on",
    disable_direct: "on",
    auth_token: token,
  };
  return `https://pengu.uk/${encodeURIComponent(JSON.stringify(config))}`;
}

// ─── HELPERS ───────────────────────────────────────────────────────────────────
function detectLanguage(s) {
  const matchText = `${s.url || ""} ${s.title || ""} ${s.description || ""} ${s.name || ""}`.toLowerCase();
  if (/hindi|hin|dual/.test(matchText)) return "Hindi 🇮🇳";
  if (/multi|🌐/.test(matchText)) return "Multi 🌐";
  if (/tamil|tam/.test(matchText)) return "Tamil 🇮🇳";
  if (/telugu|tel/.test(matchText)) return "Telugu 🇮🇳";
  if (/bengali|ben/.test(matchText)) return "Bengali 🇮🇳";
  if (/korean|kor|kdrama/.test(matchText)) return "Korean 🇰🇷";
  if (/japanese|jpn|anime/.test(matchText)) return "Japanese 🇯🇵";
  if (/chinese|mandarin|cantonese|chi/.test(matchText)) return "Chinese 🇨🇳";
  return "English 🇺🇸";
}

// Map display language string to ISO 639-1 codes (array)
function languageToCodes(langDisplay) {
  const l = (langDisplay || "").toLowerCase();
  if (/hindi/.test(l)) return ["hi"];
  if (/multi/.test(l)) return ["en", "hi", "ja"];
  if (/tamil/.test(l)) return ["ta"];
  if (/telugu/.test(l)) return ["te"];
  if (/bengali/.test(l)) return ["bn"];
  if (/korean/.test(l)) return ["ko"];
  if (/japanese/.test(l)) return ["ja"];
  if (/chinese/.test(l)) return ["zh"];
  return ["en"];
}

function detectQuality(s) {
  const matchText = `${s.url || ""} ${s.title || ""} ${s.description || ""} ${s.name || ""}`.toLowerCase();
  if (/2160|4k|uhd/.test(matchText)) return "2160p";
  if (/1080|fhd/.test(matchText)) return "1080p";
  if (/720|hd/.test(matchText)) return "720p";
  if (/480|sd/.test(matchText)) return "480p";
  if (/360/.test(matchText)) return "360p";
  return "1080p"; // assume 1080p default for unlabeled streams
}

function detectFormat(s) {
  const matchText = `${s.url || ""} ${s.title || ""} ${s.description || ""} ${s.name || ""}`.toLowerCase();
  if (/\.(mp4|avi|m4v)\b/.test(matchText) || /\b(mp4|avi|m4v)\b/.test(matchText)) return "MP4";
  if (/\.mkv\b/.test(matchText) || /\bmkv\b/.test(matchText)) return "MKV";
  if (/\.m3u8\b/.test(matchText) || /\bhls\b/.test(matchText)) return "HLS";
  return "MKV";
}

function detectSize(s) {
  const matchText = `${s.url || ""} ${s.title || ""} ${s.description || ""} ${s.name || ""}`.toLowerCase();
  const sizeMatch = matchText.match(/(\d+(?:\.\d+)?\s*(?:GB|MB|gb|mb))/i);
  if (sizeMatch) return sizeMatch[1].toUpperCase();
  if (s.size) {
    const bytes = parseInt(s.size, 10);
    if (!isNaN(bytes) && bytes > 0) {
      return bytes > 1024 * 1024 * 1024
        ? `${(bytes / (1024 * 1024 * 1024)).toFixed(2)} GB`
        : `${(bytes / (1024 * 1024)).toFixed(0)} MB`;
    }
  }
  return "Unknown Size";
}

// ─── MAIN getStreams ───────────────────────────────────────────────────────────
function getStreams(tmdbId, mediaType, season, episode) {
  return getAuthToken().then(userAuthToken => {
    if (!userAuthToken) {
      console.warn(`[${PROVIDER_NAME}] Missing Auth Token. Aborting request.`);
      return [];
    }

    const isSeries = mediaType === "tv" || mediaType === "series" || mediaType === "anime";
    const tmdbUrl = `https://api.themoviedb.org/3/${isSeries ? "tv" : "movie"}/${tmdbId}?api_key=${TMDB_API_KEY}&append_to_response=external_ids`;

    const baseUrl = get2PeckleBaseUrl(userAuthToken);

    return fetch(tmdbUrl)
      .then(r => r.json())
      .catch(() => null)
      .then(meta => {
        const imdbId = meta?.external_ids?.imdb_id || meta?.imdb_id;
        const rawTmdbId = meta?.id || tmdbId;
        const titleName = meta?.title || meta?.name || "Movie/Show";
        const releaseYear = meta?.release_date
          ? meta.release_date.split("-")[0]
          : meta?.first_air_date
          ? meta.first_air_date.split("-")[0]
          : "2026";

        const endpointsToFetch = [];
        if (imdbId) {
          endpointsToFetch.push(
            isSeries
              ? `${baseUrl}/stream/series/${imdbId}:${season || 1}:${episode || 1}.json`
              : `${baseUrl}/stream/movie/${imdbId}.json`
          );
        }
        if (rawTmdbId) {
          endpointsToFetch.push(
            isSeries
              ? `${baseUrl}/stream/series/tmdb:${rawTmdbId}:${season || 1}:${episode || 1}.json`
              : `${baseUrl}/stream/movie/tmdb:${rawTmdbId}.json`
          );
        }
        if (endpointsToFetch.length === 0) return [];

        return Promise.allSettled(
          endpointsToFetch.map(url => fetch(url).then(r => r.json()))
        ).then(responses => {
          // Merge and deduplicate by URL
          const streamMap = new Map();
          responses.forEach(res => {
            if (res.status === "fulfilled" && res.value?.streams) {
              res.value.streams.forEach(s => {
                if (s && s.url && !streamMap.has(s.url)) {
                  streamMap.set(s.url, s);
                }
              });
            }
          });

          const rawStreams = Array.from(streamMap.values());
          if (rawStreams.length === 0) return [];

          // Enrich each stream with detected quality/language/size/format
          const result = rawStreams.map(s => {
            const quality = detectQuality(s);
            const language = detectLanguage(s);
            const format = detectFormat(s);
            const sizeStr = detectSize(s);
            const cleanLangText = language.replace(/[\uE000-\uF8FF]|\uD83C[\uDC00-\uDFFF]|\uD83D[\uDC00-\uDFFF]|[\u2011-\u26FF]|\uD83E[\uDD00-\uDFFF]/g, "").trim();

            const fullLayout =
              `🎬 ${titleName} - (${releaseYear})\n` +
              `💎 ${quality} | 🔊 ${cleanLangText} | 💾 ${sizeStr}\n` +
              `🎞️ ${format} | ⛓️‍💥 ${PROVIDER_NAME}`;

            return {
              name: `${PROVIDER_NAME} | ${quality} | ${language}`,
              title: fullLayout,
              size: fullLayout,
              description: fullLayout,
              url: s.url,
              quality,
              behaviorHints: {
                notWebReady: true,
                proxyHeaders: {
                  request: {
                    Referer: "https://stremio-moviebox-1.onrender.com/",
                  },
                },
              },
              meta: {
                provider: PROVIDER_NAME,
                quality,
                language: languageToCodes(language),  // ISO 639-1 codes array
                languageDisplay: language,             // Human-readable string
                format,
                size: sizeStr,
                audio: cleanLangText,
                source: "pengu.uk",
                tmdbId: rawTmdbId,
                imdbId: imdbId || null,
                title: titleName,
                year: releaseYear,
                mediaType: isSeries ? "tv" : "movie",
                season: isSeries ? (season || 1) : null,
                episode: isSeries ? (episode || 1) : null,
              },
            };
          });

          // Sort: 4K first, then 1080p, then 720p, etc.
          const order = { "2160p": 0, "1080p": 1, "720p": 2, "480p": 3, "360p": 4 };
          result.sort((a, b) => (order[a.quality] ?? 5) - (order[b.quality] ?? 5));

          return result;
        });
      });
  }).catch(err => {
    console.error(`[${PROVIDER_NAME}] Global processing failure:`, err.message);
    return [];
  });
}

if (typeof module !== "undefined" && module.exports) {
  module.exports = { getStreams, getAuthToken, PROVIDER_NAME };
} else {
  global.getStreams = getStreams;
}
