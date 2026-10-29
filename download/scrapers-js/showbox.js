/**
 * ShowBox (SuperStream) Provider — Febbox Token + showbox.media aggregator
 * =======================================================================
 * Fetches streams up to 4K for movies, TV shows, anime, and dramas via
 * showbox.media → Febbox file_share_list → video_quality_list flow.
 *
 * Sources: https://github.com/D3adlyRocket/ShowboxTV/blob/main/providers/showbox.js
 * Adapted for standalone Node.js (no Playwright, pure fetch).
 *
 * TOKEN RESOLUTION (in priority order):
 *   1. process.env.FEBBOX_TOKEN_1                (recommended for server/PC)
 *   2. process.env.FEBBOX_COOKIE                  (alias)
 *   3. global.SCRAPER_SETTINGS.febboxToken        (Nuvio integration)
 *   4. FALLBACK_FEBBOX_TOKEN                      (hardcoded emergency)
 *   5. LOCAL_TOKEN_URL                            (Android local HTTP server)
 *
 * USAGE:
 *   const { getStreams } = require('./showbox');
 *   const streams = await getStreams(693134, 'movie');
 *   const streams = await getStreams(82856, 'tv', 1, 1);
 *
 * ENV:
 *   export FEBBOX_TOKEN_1=eyJhbGciOiJIUzI1NiJ9...
 */
"use strict";

const PROVIDER_NAME = "ShowBox";

// ─── CONFIGURATION ─────────────────────────────────────────────────────────────
const TMDB_API_KEY = "439c478a771f35c05022f9feabcca01c";

// Hardcoded emergency token (populate via env var)
const FALLBACK_FEBBOX_TOKEN =
  process.env.FEBBOX_TOKEN_1 ||
  process.env.FEBBOX_COOKIE ||
  process.env.FEBBOX_TOKEN ||
  "";

// Local HTTP server URL (Android Simple HTTP Server)
const LOCAL_TOKEN_URL =
  process.env.FEBBOX_LOCAL_URL_1 ||
  process.env.FEBBOX_LOCAL_URL ||
  "http://192.168.1.244:8080/cookie_show.txt";

const FOURTH_API = "https://showbox.media";
const THIRD_API = "https://www.febbox.com";
const TMDB_BASE = "https://api.themoviedb.org/3";

const BASE_HEADERS = {
  Accept: "application/json, text/html, */*",
  "Accept-Language": "en",
  "User-Agent":
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36",
};

const VIDEO_HEADERS = {
  Accept: "*/*",
  "Accept-Language": "en-US,en;q=0.8",
  Connection: "keep-alive",
  Range: "bytes=0-",
  Referer: "https://www.febbox.com",
  "User-Agent":
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36",
};

// ─── UTILS ────────────────────────────────────────────────────────────────────
function copyObj(src) {
  const out = {};
  const keys = Object.keys(src);
  for (let i = 0; i < keys.length; i++) out[keys[i]] = src[keys[i]];
  return out;
}

function normalizeToken(token) {
  if (!token) return "";
  if (token.indexOf("eyJ") === 0) return "ui=" + token;
  if (token.indexOf("ui=") === 0) return token;
  return "ui=" + token;
}

function episodeSlug(n) {
  return n < 10 ? "0" + n : "" + n;
}

function extractQuality(str) {
  if (!str) return "Unknown";
  const m = str.match(/(\d{3,4})[pP]/);
  if (m) {
    const q = parseInt(m[1], 10);
    if (q >= 2160) return "2160p";
    if (q >= 1080) return "1080p";
    if (q >= 720) return "720p";
    if (q >= 480) return "480p";
    return q + "p";
  }
  if (/4k|uhd/i.test(str)) return "2160p";
  return str.toUpperCase();
}

function parseQualityDivs(html) {
  const results = [];
  const re = /<div[^>]*class="[^"]*file_quality[^"]*"[^>]*>/gi;
  let m;
  while ((m = re.exec(html)) !== null) {
    const tag = m[0];
    const uM = tag.match(/data-url="([^"]+)"/);
    const qM = tag.match(/data-quality="([^"]+)"/);
    if (uM && qM) {
      results.push({
        url: uM[1].replace(new RegExp("\\/", "g"), "/"),
        quality: qM[1],
      });
    }
  }
  return results;
}

function parseSearchHref(html, baseUrl) {
  const m =
    html.match(/class="film-name[^"]*"[^>]*>\s*<a[^>]+href="([^"]+)"/i) ||
    html.match(/<a[^>]+href="([^"]+)"[^>]*class="[^"]*film-name[^"]*"/i);
  return m ? baseUrl + m[1] : null;
}

function parseHeadingId(html) {
  const m = html.match(/class="heading-name[^"]*"[^>]*>[\s\S]*?<a[^>]+href="([^"]+)"/i);
  if (!m) return null;
  const parts = m[1].split("/");
  const id = parseInt(parts[parts.length - 1], 10);
  return isNaN(id) ? null : id;
}

// ─── TOKEN FETCHER ─────────────────────────────────────────────────────────────
function getFebboxToken() {
  // 1. Environment variable (highest priority for server/PC)
  if (FALLBACK_FEBBOX_TOKEN) {
    console.log(`[${PROVIDER_NAME}] Using token from environment variable`);
    return Promise.resolve(FALLBACK_FEBBOX_TOKEN.trim());
  }

  // 2. globalThis.SCRAPER_SETTINGS (Nuvio)
  try {
    if (typeof global !== "undefined" && global.SCRAPER_SETTINGS && global.SCRAPER_SETTINGS.febboxToken) {
      console.log(`[${PROVIDER_NAME}] Using token from global.SCRAPER_SETTINGS`);
      return Promise.resolve(String(global.SCRAPER_SETTINGS.febboxToken).trim());
    }
    if (typeof window !== "undefined" && window.SCRAPER_SETTINGS && window.SCRAPER_SETTINGS.febboxToken) {
      console.log(`[${PROVIDER_NAME}] Using token from window.SCRAPER_SETTINGS`);
      return Promise.resolve(String(window.SCRAPER_SETTINGS.febboxToken).trim());
    }
  } catch (ex) { /* ignore */ }

  // 3. Local HTTP server
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
      console.error(`[${PROVIDER_NAME}] No token found from any source: ${e.message}`);
      return "";
    });
}

// ─── HTTP CLIENT (pure fetch) ──────────────────────────────────────────────────
function httpGet(url, headers, timeoutMs) {
  timeoutMs = timeoutMs || 15000;
  headers = headers || BASE_HEADERS;
  // Use AbortController for timeout
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), timeoutMs);
  return fetch(url, { headers, signal: controller.signal })
    .then(res => {
      clearTimeout(timer);
      return res;
    })
    .catch(e => {
      clearTimeout(timer);
      throw e;
    });
}

// ─── IMDB LOOKUP ──────────────────────────────────────────────────────────────
function getImdbId(tmdbId, mediaType) {
  const url1 = `${TMDB_BASE}/${mediaType}/${tmdbId}/external_ids?api_key=${TMDB_API_KEY}`;
  console.log(`[${PROVIDER_NAME}] TMDB: ${url1}`);
  return httpGet(url1, BASE_HEADERS, 10000)
    .then(res => res.json())
    .then(data => {
      if (data && data.imdb_id) {
        console.log(`[${PROVIDER_NAME}] IMDB ID: ${data.imdb_id}`);
        return data.imdb_id;
      }
      const url2 = `${TMDB_BASE}/${mediaType}/${tmdbId}?api_key=${TMDB_API_KEY}&language=en-US`;
      return httpGet(url2, BASE_HEADERS, 10000)
        .then(r2 => r2.json())
        .then(d2 => {
          if (d2 && d2.imdb_id) {
            console.log(`[${PROVIDER_NAME}] IMDB ID fallback: ${d2.imdb_id}`);
            return d2.imdb_id;
          }
          return null;
        });
    })
    .catch(e => {
      console.log(`[${PROVIDER_NAME}] getImdbId error: ${e.message}`);
      return null;
    });
}

// ─── SUPERSTREAM API ──────────────────────────────────────────────────────────
function searchSuperstream(imdbId) {
  const url = `${FOURTH_API}/search?keyword=${encodeURIComponent(imdbId)}`;
  console.log(`[${PROVIDER_NAME}] Search: ${url}`);
  return httpGet(url, BASE_HEADERS, 10000)
    .then(res => (res.ok ? res.text() : null))
    .then(html => {
      if (!html) return null;
      const href = parseSearchHref(html, FOURTH_API);
      if (!href) {
        console.log(`[${PROVIDER_NAME}] film-name href not found`);
        return null;
      }
      return httpGet(href, BASE_HEADERS, 10000)
        .then(r2 => (r2.ok ? r2.text() : null))
        .then(h2 => (h2 ? parseHeadingId(h2) : null));
    })
    .catch(e => {
      console.log(`[${PROVIDER_NAME}] searchSuperstream error: ${e.message}`);
      return null;
    });
}

function getShareKey(mediaId, type) {
  const url = `${FOURTH_API}/index/share_link?id=${mediaId}&type=${type}`;
  console.log(`[${PROVIDER_NAME}] share_link: ${url}`);
  return httpGet(url, BASE_HEADERS, 10000)
    .then(res => (res.ok ? res.json() : null))
    .then(data => {
      const link = data && data.data && data.data.link ? data.data.link : null;
      if (!link) return null;
      const parts = link.split("/");
      return parts[parts.length - 1] || null;
    })
    .catch(e => {
      console.log(`[${PROVIDER_NAME}] getShareKey error: ${e.message}`);
      return null;
    });
}

function getFileList(shareKey, parentId, page) {
  page = page || 1;
  let url = `${THIRD_API}/file/file_share_list?share_key=${shareKey}`;
  if (parentId) url += `&parent_id=${parentId}&page=${page}`;
  return httpGet(url, BASE_HEADERS, 10000)
    .then(res => (res.ok ? res.json() : null))
    .then(data => (data && data.data ? data.data : null))
    .catch(e => {
      console.log(`[${PROVIDER_NAME}] getFileList error: ${e.message}`);
      return null;
    });
}

function getVideoQualities(fid, shareKey, token) {
  const cookieHeader = normalizeToken(token);
  const url = `${THIRD_API}/console/video_quality_list?fid=${fid}&share_key=${shareKey}`;
  const hdrs = copyObj(BASE_HEADERS);
  if (cookieHeader) hdrs.Cookie = cookieHeader;
  return httpGet(url, hdrs, 12000)
    .then(res => (res.ok ? res.json() : null))
    .then(data => {
      const h = data && data.html ? data.html : "";
      return h ? parseQualityDivs(h) : [];
    })
    .catch(e => {
      console.log(`[${PROVIDER_NAME}] getVideoQualities error: ${e.message}`);
      return [];
    });
}

// ─── MAIN getStreams ───────────────────────────────────────────────────────────
function getStreams(tmdbId, mediaType, season, episode) {
  let _shareKey = null;
  let _token = null;
  let _meta = null;

  return getFebboxToken()
    .then(fetchedToken => {
      _token = fetchedToken;
      if (!_token) {
        console.error(`[${PROVIDER_NAME}] No token available, aborting getStreams`);
        return [];
      }

      return getImdbId(String(tmdbId), mediaType)
        .then(imdbId => {
          if (!imdbId) return Promise.reject(new Error("IMDB ID not found"));
          return searchSuperstream(imdbId);
        })
        .then(mediaId => {
          if (!mediaId) return Promise.reject(new Error("Media ID not found"));
          const type = mediaType === "tv" ? 2 : 1;
          return getShareKey(mediaId, type);
        })
        .then(shareKey => {
          if (!shareKey) return Promise.reject(new Error("Share key not found"));
          _shareKey = shareKey;
          return getFileList(shareKey, null, 1);
        })
        .then(data => {
          if (!data) return [];
          const fileList = data.file_list || [];
          if (!fileList.length) return [];

          if (mediaType === "tv" && season && episode) {
            const seasonStr = episodeSlug(season);
            const episodeStr = episodeSlug(episode);
            let seasonFolders = fileList.filter(f => {
              return (
                f.is_dir &&
                f.file_name &&
                (f.file_name.toLowerCase().indexOf("season " + parseInt(seasonStr, 10)) !== -1 ||
                  f.file_name.toLowerCase().indexOf("s" + seasonStr) !== -1)
              );
            });
            if (!seasonFolders.length) {
              seasonFolders = fileList.filter(f => f.is_dir);
            }
            if (!seasonFolders.length) return [];

            return getFileList(_shareKey, seasonFolders[0].fid, 1).then(epData => {
              if (!epData) return [];
              const epList = epData.file_list || [];
              let epFile = epList.filter(f => {
                if (f.is_dir) return false;
                const name = (f.file_name || "").toLowerCase();
                return (
                  name.indexOf("e" + episodeStr) !== -1 ||
                  name.indexOf("ep" + episodeStr) !== -1 ||
                  name.indexOf("episode " + parseInt(episodeStr, 10)) !== -1
                );
              });
              if (!epFile.length) epFile = epList.filter(f => !f.is_dir);
              if (!epFile.length) return [];
              return getVideoQualities(epFile[0].fid, _shareKey, _token);
            });
          } else {
            const videoFiles = fileList.filter(f => !f.is_dir);
            if (!videoFiles.length) return [];
            return getVideoQualities(videoFiles[0].fid, _shareKey, _token);
          }
        })
        .then(qualities => {
          if (!qualities || !qualities.length) return [];
          return qualities.map(q => {
            const quality = extractQuality(q.quality);
            return {
              name: `${PROVIDER_NAME} ${quality}`,
              title: `${PROVIDER_NAME} • ${quality}`,
              url: q.url,
              quality,
              provider: "superstream",
              headers: VIDEO_HEADERS,
              behaviorHints: {
                notWebReady: true,
                headers: VIDEO_HEADERS,
              },
              meta: {
                provider: PROVIDER_NAME,
                source: "febbox.com",
                server: "febbox.com",
                type: "mp4",
                quality,
                audio: "english",
                language: ["en"],
              },
            };
          });
        })
        .catch(e => {
          console.log(`[${PROVIDER_NAME}] getStreams error: ${e.message}`);
          return [];
        });
    });
}

if (typeof module !== "undefined" && module.exports) {
  module.exports = { getStreams, getFebboxToken, PROVIDER_NAME };
} else {
  global.getStreams = getStreams;
}
