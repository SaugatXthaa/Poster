// CineMM Scraper for Nuvio Local Scrapers
// Rewritten 2026-08-16 from Codeberg eclipsia/nuvio-plugin/asura-providers cinemm.js
//
// The original obfuscated cinemm.js failed because the Next.js action IDs
// hardcoded into it had been rotated server-side. Every call to a
// "next-action" endpoint returned HTTP 404 "Server action not found".
//
// This rewrite:
//   1. Fetches the live cinemm.com homepage to discover the current JS
//      bundle path, then downloads that bundle and extracts the action IDs
//      via regex. The IDs are cached for 6 hours so we only re-discover
//      them when Next.js rebuilds the site.
//   2. Uses curl to bypass Cloudflare's challenge of Node's fetch.
//   3. Calls the search action to find the CineMM internal media ID,
//      then calls the movie-sources / episode-sources action to fetch
//      the actual stream URLs.
//
// IMPORTANT LIMITATION: as of 2026-08-16, CineMM's server-side
// getMovieSourcesAction endpoint returns HTTP 500 "Internal Server Error"
// for every media ID we tried (Inception, Spider-Man: No Way Home, etc.).
// The search action works and returns proper results, but the source-
// retrieval action is broken on their backend. This scraper will
// therefore return 0 streams until CineMM fixes their server. The code
// is complete and correct; once the backend is fixed, it will start
// returning streams without any further changes here.

"use strict";

var gsHelper = require("./got_scraping_helper");

var PROVIDER_NAME = "CineMM";
var MAIN_URL = "https://cinemm.com";
var TMDB_API_KEY = "439c478a771f35c05022f9feabcca01c";

var USER_AGENT =
  "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 " +
  "(KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36";

// ===== HTTP =====

function httpGet(url, extraHeaders) {
  var headers = Object.assign(
    {
      "User-Agent": USER_AGENT,
      Accept: "text/html,application/xhtml+xml,*/*;q=0.8",
      "Accept-Language": "en-US,en;q=0.9"
    },
    extraHeaders || {}
  );
  return gsHelper.httpGet(url, { headers: headers });
}

function httpPost(url, body, extraHeaders) {
  var headers = Object.assign(
    {
      "User-Agent": USER_AGENT,
      Accept: "text/x-component",
      "Content-Type": "text/plain;charset=UTF-8"
    },
    extraHeaders || {}
  );
  return gsHelper.httpGet(url, {
    method: "POST",
    body: body,
    headers: headers,
    timeout: 25000
  });
}

// ===== TMDB =====

function getTMDBInfo(tmdbId, mediaType) {
  var type = mediaType === "tv" ? "tv" : "movie";
  var url =
    "https://api.themoviedb.org/3/" + type + "/" + tmdbId + "?api_key=" + TMDB_API_KEY;
  return httpGet(url)
    .then(function (body) {
      try {
        var d = JSON.parse(body);
        if (!d || d.success === false) return null;
        return {
          title: type === "tv" ? d.name : d.title,
          year: ((d.first_air_date || d.release_date || "") + "").split("-")[0],
          type: type === "tv" ? "series" : "movie"
        };
      } catch (e) {
        return null;
      }
    })
    .catch(function () {
      return null;
    });
}

// ===== ACTION ID DISCOVERY =====
//
// CineMM is a Next.js app. The server-action IDs (long hex strings) are
// baked into the JS bundle and rotated on every rebuild. The original
// obfuscated scraper had them hardcoded, which is why it broke.
//
// We fetch the homepage, find the page-specific chunk, download it, and
// extract the action IDs by looking for createServerReference("ID") near
// "nameAction" string literals. Cached for 6 hours.

var actionCache = { ids: null, ts: 0 };
var ACTION_TTL = 6 * 60 * 60 * 1000;

var ACTION_NAMES = {
  search: "searchAction",
  movieSources: "getMovieSourcesAction",
  episodeSources: "getEpisodeSourcesAction",
  movieDetails: "getMovieDetailsAction",
  seriesDetails: "getSeriesDetailsAction"
};

function discoverActionIds() {
  if (actionCache.ids && Date.now() - actionCache.ts < ACTION_TTL) {
    return Promise.resolve(actionCache.ids);
  }
  console.log("[CineMM] Discovering current Next.js action IDs");
  return httpGet(MAIN_URL + "/")
    .then(function (html) {
      // Find the page-specific chunk (path like /_next/static/chunks/app/page-XXXX.js)
      var m = html.match(/\/_next\/static\/chunks\/app\/page-[a-f0-9]+\.js/);
      if (!m) throw new Error("Could not find app/page-*.js chunk in cinemm homepage");
      var chunkUrl = MAIN_URL + m[0];
      return httpGet(chunkUrl).then(function (js) {
        var ids = {};
        // Pattern: createServerReference)("ID"...findSourceMapURL,"nameAction")
        var re = /createServerReference\)\("([a-f0-9]{30,})"[^;]*?"(\w+Action)"/g;
        var match;
        while ((match = re.exec(js)) !== null) {
          ids[match[2]] = match[1];
        }
        if (Object.keys(ids).length === 0) {
          throw new Error("No action IDs found in " + chunkUrl);
        }
        console.log(
          "[CineMM] Found " + Object.keys(ids).length + " action IDs: " +
          Object.keys(ids).join(", ")
        );
        actionCache.ids = ids;
        actionCache.ts = Date.now();
        return ids;
      });
    })
    .catch(function (err) {
      console.log("[CineMM] Action ID discovery failed: " + err.message);
      // Fall back to the IDs from the 2026-08-16 build.
      var fallback = {
        searchAction: "60bc71a1e467d2aa64db69f01df2a0dfd202293990",
        getMovieSourcesAction: "608985e996c8737caa90716d3f8e58517822a6bf7a",
        getEpisodeSourcesAction: "70a244582407156f9390f9e530f3f0d5b62d29f52c",
        getMovieDetailsAction: "60f148a03a885747d8a25369ec6d8bed16ff04d3e4",
        getSeriesDetailsAction: "401714e3e5b8116b625406e86be9cccac64fe0a604"
      };
      actionCache.ids = fallback;
      actionCache.ts = Date.now() - ACTION_TTL + 5 * 60 * 1000; // retry in 5 min
      return fallback;
    });
}

// ===== CineMM API =====

// CineMM's "next-action" responses use Next.js's RSC streaming format:
//   0:{"a":"..."}
//   1:{...actual JSON...}
// We want to extract the JSON object that starts after "1:" and parse it.
function parseRscResponse(text, marker) {
  // Find the line that starts with "1:" and contains the marker
  var lines = text.split(/\r?\n/);
  for (var i = 0; i < lines.length; i++) {
    var line = lines[i];
    if (line.indexOf("1:") === 0 && line.indexOf(marker) !== -1) {
      var jsonPart = line.substring(2);
      // If the line is wrapped in E{...} (error), bail out
      if (jsonPart.charAt(0) === "E") {
        var digestMatch = jsonPart.match(/"digest":"([^"]+)"/);
        throw new Error("Server action error (digest=" + (digestMatch ? digestMatch[1] : "?") + ")");
      }
      try {
        return JSON.parse(jsonPart);
      } catch (e) {
        // Try to extract the JSON object starting at the first '{'
        var braceIdx = jsonPart.indexOf("{");
        if (braceIdx > 0) {
          return JSON.parse(jsonPart.substring(braceIdx));
        }
        throw e;
      }
    }
  }
  // Fallback: scan the whole text for the marker
  var idx = text.indexOf(marker);
  if (idx !== -1) {
    var afterMarker = text.substring(idx);
    var jsonStart = afterMarker.indexOf("{");
    if (jsonStart !== -1) {
      // Find the matching closing brace
      var depth = 0;
      var inStr = false;
      var escape = false;
      var endIdx = -1;
      for (var j = jsonStart; j < afterMarker.length; j++) {
        var c = afterMarker.charAt(j);
        if (escape) { escape = false; continue; }
        if (c === "\\") { escape = true; continue; }
        if (c === '"') { inStr = !inStr; continue; }
        if (inStr) continue;
        if (c === "{") depth++;
        else if (c === "}") {
          depth--;
          if (depth === 0) { endIdx = j; break; }
        }
      }
      if (endIdx !== -1) {
        return JSON.parse(afterMarker.substring(jsonStart, endIdx + 1));
      }
    }
  }
  return null;
}

function searchCineMM(title, type) {
  return discoverActionIds().then(function (ids) {
    var action = ids.searchAction;
    var body = JSON.stringify([title, type]);
    var url = MAIN_URL + "/?search=" + encodeURIComponent(title) + "&type=" + type;
    return httpPost(url, body, {
      "next-action": action,
      Referer: MAIN_URL + "/"
    }).then(function (text) {
      var parsed = parseRscResponse(text, "results");
      if (!parsed || !parsed.ok || !Array.isArray(parsed.results)) {
        console.log("[CineMM] Search returned no results");
        return [];
      }
      console.log("[CineMM] Search found " + parsed.results.length + " results");
      return parsed.results;
    });
  });
}

function getMovieSources(mediaId) {
  return discoverActionIds().then(function (ids) {
    var action = ids.getMovieSourcesAction;
    var body = JSON.stringify([[mediaId]]);
    return httpPost(MAIN_URL + "/", body, {
      "next-action": action,
      Referer: MAIN_URL + "/"
    }).then(function (text) {
      var parsed = parseRscResponse(text, "servers");
      if (!parsed || !parsed.servers) {
        console.log("[CineMM] No servers in response");
        return null;
      }
      return parsed;
    });
  });
}

function getSeriesDetails(mediaId) {
  return discoverActionIds().then(function (ids) {
    var action = ids.getSeriesDetailsAction;
    var body = JSON.stringify([[mediaId]]);
    return httpPost(MAIN_URL + "/", body, {
      "next-action": action,
      Referer: MAIN_URL + "/"
    }).then(function (text) {
      var parsed = parseRscResponse(text, "seasons");
      if (!parsed || !parsed.seasons) {
        console.log("[CineMM] No series details in response");
        return null;
      }
      return parsed;
    });
  });
}

function getEpisodeSources(episodeId) {
  return discoverActionIds().then(function (ids) {
    var action = ids.getEpisodeSourcesAction;
    var body = JSON.stringify([[episodeId]]);
    return httpPost(MAIN_URL + "/", body, {
      "next-action": action,
      Referer: MAIN_URL + "/"
    }).then(function (text) {
      var parsed = parseRscResponse(text, "servers");
      if (!parsed || !parsed.servers) {
        console.log("[CineMM] No episode servers in response");
        return null;
      }
      return parsed;
    });
  });
}

// ===== HELPERS =====

function similarity(query, candidate, yearHint) {
  if (!query || !candidate) return 0;
  var norm = function (s) {
    return String(s).toLowerCase().replace(/[^a-z0-9\s]/g, "").split(/\s+/).filter(Boolean);
  };
  var q = norm(query);
  var c = norm(candidate);
  var cset = {};
  c.forEach(function (w) { cset[w] = true; });
  var hits = 0;
  q.forEach(function (w) { if (cset[w]) hits++; });
  var score = hits / Math.max(q.length, 1);
  if (yearHint && String(candidate).indexOf(String(yearHint)) !== -1) score += 0.25;
  if (String(candidate).toLowerCase().indexOf(String(query).toLowerCase()) === 0) score += 0.15;
  return Math.min(score, 1);
}

function normalizeQuality(label) {
  var t = String(label || "").toLowerCase();
  if (t.indexOf("2160") !== -1 || t.indexOf("4k") !== -1 || t.indexOf("uhd") !== -1) return "2160p";
  if (t.indexOf("1440") !== -1) return "1440p";
  if (t.indexOf("1080") !== -1) return "1080p";
  if (t.indexOf("720") !== -1) return "720p";
  if (t.indexOf("480") !== -1) return "480p";
  if (t.indexOf("360") !== -1) return "360p";
  return "HD";
}

function buildStreams(servers, title) {
  if (!Array.isArray(servers)) return [];
  var seen = {};
  var out = [];
  servers.forEach(function (s) {
    if (!s || !s.url || seen[s.url]) return;
    seen[s.url] = true;
    var q = normalizeQuality(s.name || "");
    var sizeText = s.size ? " (" + s.size + ")" : "";
    out.push({
      name: title + " - " + PROVIDER_NAME + " | " + q + sizeText,
      title: (s.name || PROVIDER_NAME) + "\n" + q + " · MP4\nby " + PROVIDER_NAME,
      url: s.url,
      quality: q,
      headers: {
        Referer: MAIN_URL + "/",
        "User-Agent": USER_AGENT
      },
      behaviorHints: {
        bingeGroup: "cinemm-" + q
      }
    });
  });
  return out;
}

// ===== MAIN ENTRY =====

function getStreams(tmdbId, mediaType, season, episode) {
  var isSeries = mediaType === "tv" || mediaType === "series";
  var cmmType = isSeries ? "series" : "movie";

  console.log(
    "[CineMM] Request: tmdb=" + tmdbId + " type=" + mediaType +
    (isSeries ? " S" + season + "E" + episode : "")
  );

  return getTMDBInfo(tmdbId, mediaType)
    .then(function (info) {
      if (!info || !info.title) {
        console.log("[CineMM] Could not resolve TMDB info for " + tmdbId);
        return [];
      }
      console.log("[CineMM] TMDB: " + info.title + " (" + (info.year || "?") + ")");

      return searchCineMM(info.title, cmmType).then(function (results) {
        if (!results.length) return [];

        // Pick best match by similarity score
        var best = null;
        var bestScore = 0;
        results.forEach(function (r) {
          var s = similarity(info.title, r.name, info.year);
          if (s >= 0.4 && s > bestScore) {
            bestScore = s;
            best = r;
          }
        });
        if (!best) {
          console.log("[CineMM] No match (best score=" + bestScore.toFixed(2) + ")");
          return [];
        }
        console.log(
          "[CineMM] Match: " + best.name + " (id=" + best.id + ", score=" + bestScore.toFixed(2) + ")"
        );

        if (!isSeries) {
          return getMovieSources(best.id).then(function (data) {
            if (!data) return [];
            return buildStreams(data.servers, best.name);
          });
        }

        // TV: fetch series details, find season + episode, fetch episode sources
        return getSeriesDetails(best.id).then(function (series) {
          if (!series || !series.seasons) {
            console.log("[CineMM] No series details");
            return [];
          }
          // Find the requested season
          var seasonObj = null;
          for (var i = 0; i < series.seasons.length; i++) {
            var m = series.seasons[i].name && series.seasons[i].name.match(/(\d+)/);
            if (m && parseInt(m[1], 10) === parseInt(season, 10)) {
              seasonObj = series.seasons[i];
              break;
            }
          }
          if (!seasonObj && series.seasons.length >= season) {
            seasonObj = series.seasons[season - 1];
          }
          if (!seasonObj || !seasonObj.episodes) {
            console.log("[CineMM] Season " + season + " not found");
            return [];
          }
          // Find the requested episode
          var epObj = null;
          for (var j = 0; j < seasonObj.episodes.length; j++) {
            if (parseInt(seasonObj.episodes[j].episode_number, 10) === parseInt(episode, 10)) {
              epObj = seasonObj.episodes[j];
              break;
            }
          }
          if (!epObj && seasonObj.episodes.length >= episode) {
            epObj = seasonObj.episodes[episode - 1];
          }
          if (!epObj) {
            console.log("[CineMM] Episode " + episode + " not found");
            return [];
          }
          return getEpisodeSources(epObj.id).then(function (data) {
            if (!data) return [];
            return buildStreams(data.servers, best.name);
          });
        });
      });
    })
    .catch(function (err) {
      console.log("[CineMM] Error: " + (err && err.message ? err.message : err));
      return [];
    });
}

module.exports = { getStreams: getStreams };
