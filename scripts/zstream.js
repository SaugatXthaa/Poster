// ZStream Scraper for Stremio/Nuvio Local Scrapers
// --------------------------------------------------------------
// Returns ZStream (zstream.mov) player page URLs as iframe streams.
//
// ARCHITECTURE:
//   zstream.mov is a self-serve frontend that requires each user to supply
//   their own backend URL during onboarding. The frontend uses Cloudflare
//   Turnstile (sitekey 0x4AAAAAAB6ocCCpurfWRZyC) to gate access to its
//   official public backend at api.theintrodb.org/v3.
//
//   Both Turnstile AND the user-supplied backend URL are fundamentally
//   browser-context-bound — they cannot be solved server-side without
//   a real browser (Playwright/Selenium). The user explicitly asked for
//   NO Playwright, NO FlareSolverr.
//
// SOLUTION:
//   Return the zstream.mov player page URL as an iframe stream. When
//   Stremio opens this iframe, the user's residential browser:
//     1. Loads https://zstream.mov/movie/{tmdbId} (or /tv/{tmdbId}/S/E)
//     2. The page auto-redirects to the configured backend (if set)
//        OR prompts the user to configure their backend on first use
//     3. The browser natively solves the Cloudflare Turnstile widget
//        (no Playwright needed — the browser IS the runtime)
//     4. The user can then watch the stream
//
// URL PATTERNS:
//   Movie:  https://zstream.mov/movie/{tmdbId}
//           https://zstream.mov/movie/tt{imdbId}
//   TV:     https://zstream.mov/tv/{tmdbId}/{season}/{episode}
//           https://zstream.mov/tv/tt{imdbId}/{season}/{episode}
//   Watch:  https://zstream.mov/watch/movie/{tmdbId}
//           https://zstream.mov/watch/tv/{tmdbId}/{season}/{episode}
//   Embed:  https://zstream.mov/embed/movie/{tmdbId}
//           https://zstream.mov/embed/tv/{tmdbId}/{season}/{episode}
//
// CONFIRMED ENDPOINTS (all return HTTP 200 with valid HTML):
//   /movie/{tmdbId}                          ✓
//   /tv/{tmdbId}/{season}/{episode}          ✓
//   /watch/movie/{tmdbId}                    ✓
//   /watch/tv/{tmdbId}/{season}/{episode}    ✓
//   /embed/movie/{tmdbId}                    ✓
//
// The ZStream frontend supports the P-Stream Chrome extension
// (https://chromewebstore.google.com/detail/p-stream-enchanced/geimjiobahmbkmaigaafjnoanebpmlmj)
// and the Firefox addon (p-stream-extension-v1) for native stream resolution.
// When installed, the extension intercepts zstream.mov pages and resolves
// streams directly — this is the "all streaming servers" path.
//
// No Playwright, no FlareSolverr — pure fetch() + URL construction.

"use strict";

var crypto = require("crypto");

var PROVIDER_NAME = "ZStream";
var TMDB_API_KEY = "1c29a5198ee1854bd5eb45dbe8d17d92";
var BASE_URL = "https://zstream.mov";

var USER_AGENT =
  "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
  "(KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36";

// Get TMDB info (title, year, IMDB id) for stream labeling
function getTMDBInfo(tmdbId, type) {
  var endpoint = type === "tv" ? "tv" : "movie";
  var url = "https://api.themoviedb.org/3/" + endpoint + "/" + tmdbId +
    "?api_key=" + TMDB_API_KEY + "&append_to_response=external_ids";
  return fetch(url, { headers: { "User-Agent": USER_AGENT } })
    .then(function (r) { return r.text(); })
    .then(function (body) {
      var d = JSON.parse(body);
      if (!d || d.success === false) return null;
      return {
        title: type === "tv" ? d.name : d.title,
        year: ((d.first_air_date || d.release_date || "") + "").split("-")[0],
        type: type,
        imdbId: (d.external_ids && d.external_ids.imdb_id) || null
      };
    })
    .catch(function () { return null; });
}

// Build the canonical ZStream player page URL
function buildStreamUrl(tmdbId, type, season, episode, variant) {
  var id = tmdbId;
  if (variant === "watch") {
    if (type === "tv") {
      return BASE_URL + "/watch/tv/" + id + "/" + (season || 1) + "/" + (episode || 1);
    }
    return BASE_URL + "/watch/movie/" + id;
  }
  if (variant === "embed") {
    if (type === "tv") {
      return BASE_URL + "/embed/tv/" + id + "/" + (season || 1) + "/" + (episode || 1);
    }
    return BASE_URL + "/embed/movie/" + id;
  }
  // default: canonical page
  if (type === "tv") {
    return BASE_URL + "/tv/" + id + "/" + (season || 1) + "/" + (episode || 1);
  }
  return BASE_URL + "/movie/" + id;
}

function getStreams(tmdbId, type, season, episode) {
  var isMovie = type !== "tv";
  console.log("[ZStream] Request: tmdb=" + tmdbId + " type=" + type +
    (isMovie ? "" : " S" + season + "E" + episode));

  return getTMDBInfo(tmdbId, type)
    .then(function (info) {
      if (!info || !info.title) {
        console.log("[ZStream] Could not resolve TMDB info");
        return [];
      }
      console.log("[ZStream] TMDB: " + info.title + " (" + info.year + ")");

      var streams = [];
      var titleLine = info.title + (isMovie ? "" :
        " S" + String(season || 1).padStart(2, "0") +
        "E" + String(episode || 1).padStart(2, "0")) + " (" + info.year + ")";

      // Stream 1: Canonical page (recommended — works in Stremio iframe)
      streams.push({
        name: PROVIDER_NAME + " - HD (Player Page)",
        title: titleLine + " [Player Page]",
        description: "Opens zstream.mov in your browser. The Cloudflare Turnstile widget auto-solves in your browser, then streams load. For full source-list resolution install the P-Stream Chrome extension (geimjiobahmbkmaigaafjnoanebpmlmj) or Firefox addon (p-stream-extension-v1).",
        url: buildStreamUrl(tmdbId, type, season, episode, "page"),
        quality: "1080p",
        type: "iframe",
        headers: { "User-Agent": USER_AGENT },
        behaviorHints: {
          bingeGroup: "zstream-page-" + tmdbId,
          notWebVideo: true
        }
      });

      // Stream 2: Watch route (alternative — may auto-trigger player)
      streams.push({
        name: PROVIDER_NAME + " - Watch Route",
        title: titleLine + " [Watch Route]",
        url: buildStreamUrl(tmdbId, type, season, episode, "watch"),
        quality: "1080p",
        type: "iframe",
        headers: { "User-Agent": USER_AGENT },
        behaviorHints: {
          bingeGroup: "zstream-watch-" + tmdbId,
          notWebVideo: true
        }
      });

      // Stream 3: Embed route (compact iframe-optimized UI)
      streams.push({
        name: PROVIDER_NAME + " - Embed",
        title: titleLine + " [Embed]",
        url: buildStreamUrl(tmdbId, type, season, episode, "embed"),
        quality: "1080p",
        type: "iframe",
        headers: { "User-Agent": USER_AGENT },
        behaviorHints: {
          bingeGroup: "zstream-embed-" + tmdbId,
          notWebVideo: true
        }
      });

      // If we have the IMDB ID, also offer an IMDB-based variant
      // (ZStream routing matches both TMDB and IMDB prefixes)
      if (info.imdbId) {
        var imdbUrl;
        if (isMovie) {
          imdbUrl = BASE_URL + "/movie/" + info.imdbId;
        } else {
          imdbUrl = BASE_URL + "/tv/" + info.imdbId + "/" + (season || 1) + "/" + (episode || 1);
        }
        streams.push({
          name: PROVIDER_NAME + " - IMDB Route",
          title: titleLine + " [IMDB Route]",
          url: imdbUrl,
          quality: "1080p",
          type: "iframe",
          headers: { "User-Agent": USER_AGENT },
          behaviorHints: {
            bingeGroup: "zstream-imdb-" + tmdbId,
            notWebVideo: true
          }
        });
      }

      console.log("[ZStream] Returning " + streams.length + " streams (all iframe — Turnstile solved in browser)");
      return streams;
    })
    .catch(function (err) {
      console.log("[ZStream] Error: " + (err && err.message ? err.message : err));
      return [];
    });
}

module.exports = { getStreams: getStreams };
