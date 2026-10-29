/**
 * GramCinema (Hashhackers) Provider — Movies, TV, Anime, Dramas up to 4K
 * ======================================================================
 * Uses the Hashhackers API (tga-hd.api.hashhackers.com) to fetch direct
 * stream links for movies, TV shows, anime (Japanese), and Asian dramas
 * (Korean, Chinese, Thai, etc.). Returns up to 2160p (4K) quality.
 *
 * Sources: https://github.com/D3adlyRocket/ShowboxTV/blob/main/providers/gramcinema.js
 * Adapted for standalone Node.js (no Playwright, pure fetch).
 *
 * TOKEN RESOLUTION (in priority order):
 *   1. process.env.HASHHACKERS_TOKEN     (recommended for server/PC)
 *   2. process.env.GRAMCINEMA_TOKEN       (alias)
 *   3. global.SCRAPER_SETTINGS.cinemaTvToken
 *   4. LOCAL_TOKEN_URL                    (Android local HTTP server)
 *
 * USAGE:
 *   const { getStreams } = require('./gramcinema');
 *   const streams = await getStreams(693134, 'movie');         // Dune Part Two
 *   const streams = await getStreams(82856, 'tv', 1, 1);       // Mandalorian S01E01
 *   const streams = await getStreams(95479, 'tv', 1, 1);       // Jujutsu Kaisen (anime)
 *   const streams = await getStreams(84958, 'tv', 1, 1);       // Crash Landing on You (K-drama)
 *
 * ENV:
 *   export HASHHACKERS_TOKEN=your_bearer_token_here
 */
"use strict";

const PROVIDER_NAME = "GramCinema";

// ─── TOKEN SOURCES ─────────────────────────────────────────────────────────────
const FALLBACK_TOKEN =
  process.env.HASHHACKERS_TOKEN ||
  process.env.GRAMCINEMA_TOKEN ||
  process.env.CINEMATV_TOKEN ||
  "";

const LOCAL_TOKEN_URL =
  process.env.GRAMCINEMA_LOCAL_URL ||
  "http://192.168.1.244:8080/cookie_gram.txt";

// ─── CONFIG ────────────────────────────────────────────────────────────────────
const TMDB_API_KEY = "d131017ccc6e5462a81c9304d21476de";
const TMDB_BASE = "https://api.themoviedb.org/3";
const HASH_API = "https://tga-hd.api.hashhackers.com";

const HASH_HEADERS_TEMPLATE = {
  "User-Agent":
    "Mozilla/5.0 (iPhone; CPU iPhone OS 18_7 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/26.0.1 Mobile/15E148 Safari/604.1",
  Accept: "*/*",
  Origin: "https://bollywood.eu.org",
  Referer: "https://bollywood.eu.org/",
};

// ─── TOKEN FETCHER ─────────────────────────────────────────────────────────────
function getToken() {
  // 1. Env var (highest priority)
  if (FALLBACK_TOKEN) {
    console.log(`[${PROVIDER_NAME}] Using token from environment variable`);
    return Promise.resolve(FALLBACK_TOKEN.trim());
  }

  // 2. globalThis.SCRAPER_SETTINGS (Nuvio)
  try {
    if (typeof global !== "undefined" && global.SCRAPER_SETTINGS && global.SCRAPER_SETTINGS.cinemaTvToken) {
      console.log(`[${PROVIDER_NAME}] Using token from global.SCRAPER_SETTINGS`);
      return Promise.resolve(String(global.SCRAPER_SETTINGS.cinemaTvToken));
    }
    if (typeof window !== "undefined" && window.SCRAPER_SETTINGS && window.SCRAPER_SETTINGS.cinemaTvToken) {
      console.log(`[${PROVIDER_NAME}] Using token from window.SCRAPER_SETTINGS`);
      return Promise.resolve(String(window.SCRAPER_SETTINGS.cinemaTvToken));
    }
  } catch (ex) { /* ignore */ }

  // 3. Local HTTP server (Android)
  return fetch(LOCAL_TOKEN_URL)
    .then(resp => {
      if (!resp.ok) throw new Error("Local server error: " + resp.status);
      return resp.text();
    })
    .then(text => {
      const token = text.trim();
      if (token) {
        console.log(`[${PROVIDER_NAME}] Token successfully fetched from local server`);
        return token;
      }
      throw new Error("Empty token from local server");
    })
    .catch(e => {
      console.error(`[${PROVIDER_NAME}] No token found: ${e.message}`);
      return "";
    });
}

// ─── UTILS ────────────────────────────────────────────────────────────────────
function formatBytes(bytes) {
  if (!bytes || bytes == 0) return "Unknown";
  const k = 1024;
  const sizes = ["Bytes", "KB", "MB", "GB", "TB"];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + " " + sizes[i];
}

function fetchJson(url, options) {
  console.log(`[${PROVIDER_NAME}] Fetching: ${url}`);
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), 15000);
  const opts = Object.assign({}, options || {}, { signal: controller.signal });
  return fetch(url, opts)
    .then(res => {
      clearTimeout(timer);
      if (!res.ok) throw new Error("HTTP " + res.status);
      return res.json();
    })
    .catch(err => {
      clearTimeout(timer);
      console.error(`[${PROVIDER_NAME}] Fetch Failed: ${err.message}`);
      throw err;
    });
}

function detectLanguageFromName(name) {
  const n = (name || "").toLowerCase();
  if (/hindi|hin|dual/.test(n)) return "Hindi";
  if (/tamil|tam/.test(n)) return "Tamil";
  if (/telugu|tel/.test(n)) return "Telugu";
  if (/bengali|ben/.test(n)) return "Bengali";
  if (/korean|kor|kdrama/.test(n)) return "Korean";
  if (/japanese|jpn|anime/.test(n)) return "Japanese";
  if (/chinese|mandarin|cantonese|chi/.test(n)) return "Chinese";
  if (/thai/.test(n)) return "Thai";
  if (/spanish|spa/.test(n)) return "Spanish";
  if (/french|fre|fra/.test(n)) return "French";
  return "English";
}

function detectFormatFromName(name) {
  const n = (name || "").toLowerCase();
  if (/\.mp4\b/.test(n)) return "MP4";
  if (/\.mkv\b/.test(n)) return "MKV";
  if (/\.avi\b/.test(n)) return "AVI";
  if (/\.m4v\b/.test(n)) return "M4V";
  return "MKV";
}

// ─── MAIN getStreams ───────────────────────────────────────────────────────────
function getStreams(tmdbId, mediaType, season, episode) {
  console.log(`[${PROVIDER_NAME}] getStreams: ${tmdbId} | Type: ${mediaType}`);

  if (mediaType !== "movie" && mediaType !== "tv" && mediaType !== "series" && mediaType !== "anime") {
    return Promise.resolve([]);
  }

  return getToken()
    .then(token => {
      if (!token) {
        console.error(`[${PROVIDER_NAME}] No token available, aborting getStreams`);
        return [];
      }

      const isTv = mediaType !== "movie";
      const isImdb = String(tmdbId).indexOf("tt") === 0;

      const tmdbUrl = isImdb
        ? `${TMDB_BASE}/find/${tmdbId}?api_key=${TMDB_API_KEY}&external_source=imdb_id&language=en-US`
        : `${TMDB_BASE}/${isTv ? "tv" : "movie"}/${tmdbId}?api_key=${TMDB_API_KEY}&language=en-US`;

      const HASH_HEADERS = Object.assign({}, HASH_HEADERS_TEMPLATE, {
        Authorization: "Bearer " + token,
      });

      return fetchJson(tmdbUrl)
        .then(tmdbData => {
          let mediaData;
          if (isImdb) {
            mediaData = isTv
              ? tmdbData.tv_results && tmdbData.tv_results[0]
              : tmdbData.movie_results && tmdbData.movie_results[0];
          } else {
            mediaData = tmdbData;
          }
          if (!mediaData) return [];

          const title = isTv ? mediaData.name : mediaData.title;
          const releaseDate = isTv ? mediaData.first_air_date : mediaData.release_date;
          const year = releaseDate ? releaseDate.split("-")[0] : "";

          let queryStr = `${title} ${year}`;
          if (isTv && season !== undefined && episode !== undefined) {
            const s = season < 10 ? "0" + season : "" + season;
            const e = episode < 10 ? "0" + episode : "" + episode;
            queryStr += ` S${s}E${e}`;
          }
          const query = encodeURIComponent(queryStr.trim());

          const searchUrl = `${HASH_API}/mix_media_files/search?q=${query}&page=1`;

          return fetchJson(searchUrl, { headers: HASH_HEADERS })
            .then(searchData => {
              const files = searchData.files || [];
              const validFiles = files.filter(f => /\.(mkv|mp4)$/.test((f.file_name || "").toLowerCase().trim()));
              if (validFiles.length === 0) return [];

              const topFiles = validFiles.slice(0, 6);
              const streamPromises = topFiles.map(file => {
                return fetchJson(`${HASH_API}/genLink?type=mix_media&id=${file.id}`, { headers: HASH_HEADERS })
                  .then(linkData => {
                    if (linkData.success && linkData.url) {
                      const fn = (file.file_name || "").toLowerCase();
                      let quality = "Auto";
                      if (fn.indexOf("2160p") !== -1 || fn.indexOf("4k") !== -1) quality = "2160p";
                      else if (fn.indexOf("1080p") !== -1) quality = "1080p";
                      else if (fn.indexOf("720p") !== -1) quality = "720p";
                      else if (fn.indexOf("480p") !== -1) quality = "480p";

                      const language = detectLanguageFromName(file.file_name);
                      const format = detectFormatFromName(file.file_name);
                      const sizeStr = formatBytes(parseInt(file.file_size, 10));

                      return {
                        name: `${PROVIDER_NAME} | ${quality} | ${language}`,
                        title: `🎬 ${title} (${year})\n💎 ${quality} | 🔊 ${language} | 💾 ${sizeStr}\n🎞️ ${format} | ⛓️‍💥 ${PROVIDER_NAME}`,
                        size: sizeStr,
                        description: `🎬 ${title} (${year})\n💎 ${quality} | 🔊 ${language} | 💾 ${sizeStr}\n🎞️ ${format} | ⛓️‍💥 ${PROVIDER_NAME}`,
                        url: linkData.url,
                        quality,
                        behaviorHints: {
                          notWebReady: true,
                          headers: {
                            "User-Agent": HASH_HEADERS_TEMPLATE["User-Agent"],
                            Origin: HASH_HEADERS_TEMPLATE.Origin,
                            Referer: HASH_HEADERS_TEMPLATE.Referer,
                          },
                        },
                        meta: {
                          provider: PROVIDER_NAME,
                          source: "hashhackers.com",
                          server: "tga-hd.api.hashhackers.com",
                          type: format.toLowerCase(),
                          quality,
                          audio: language.toLowerCase(),
                          language: [language.toLowerCase().slice(0, 2)],
                          size: sizeStr,
                          format,
                          fileName: file.file_name,
                          title: title,
                          year: year,
                          mediaType: isTv ? "tv" : "movie",
                          season: isTv ? season : null,
                          episode: isTv ? episode : null,
                        },
                      };
                    }
                    return null;
                  })
                  .catch(() => null);
              });

              return Promise.all(streamPromises).then(results => {
                const valid = results.filter(r => r !== null);
                // Sort 4K first
                const order = { "2160p": 0, "1080p": 1, "720p": 2, "480p": 3, "Auto": 4 };
                valid.sort((a, b) => (order[a.quality] ?? 5) - (order[b.quality] ?? 5));
                return valid;
              });
            })
            .catch(error => {
              console.error(`[${PROVIDER_NAME}] Search error: ${error.message}`);
              return [];
            });
        })
        .catch(error => {
          console.error(`[${PROVIDER_NAME}] TMDB error: ${error.message}`);
          return [];
        });
    });
}

if (typeof module !== "undefined" && module.exports) {
  module.exports = { getStreams, getToken, PROVIDER_NAME };
} else {
  global.getStreams = getStreams;
}
