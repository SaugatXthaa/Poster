// TokyoInsider Scraper for Nuvio Local Scrapers
// Rewritten 2026-08-16 from nuvio-plugins/providers/tokyoinsider.js
//
// The original tokyoinsider.js failed because every endpoint of
// www.tokyoinsider.com (except the bare homepage) is protected by a
// Cloudflare "I'm Under Attack" / Turnstile challenge. Node's fetch
// and even cloudscraper all receive a "Just a moment..." 403 page.
//
// This rewrite works around it by:
//
//   1. Using the static /upload/autocomplete.js index (a 421KB file
//      listing every anime + URL on the site) to resolve the anime
//      URL from a title search. This file is served as a static asset
//      and is NOT behind the Cloudflare challenge.
//   2. Using got-scraping (a pure-JS HTTP client that mimics Chrome's
//      TLS fingerprint) to fetch the anime page. This bypasses most
//      Cloudflare bot challenges without any external tools.
//   3. Falls back to plain fetch() if got-scraping is not installed.
//
// NOTE: TokyoInsider's Cloudflare config uses Turnstile (interactive
// challenge) on some pages. got-scraping can bypass the JS challenge
// but NOT the interactive Turnstile. If the page returns a Turnstile
// challenge, this scraper returns 0 streams gracefully.
//
// This scraper supports 4K (2160p) streams when TokyoInsider has them.
// Most anime on TI max out at 1080p, but some movies and recent
// releases do have 2160p encodes.

"use strict";

var gsHelper = require("./got_scraping_helper");

var PROVIDER_NAME = "TokyoInsider";
var BASE_URL = "https://www.tokyoinsider.com";
var TMDB_API_KEY = "1c29a5198ee1854bd5eb45dbe8d17d92";
var TMDB_BASE_URL = "https://api.themoviedb.org/3";
var ANILIST_API_URL = "https://graphql.anilist.co";

var USER_AGENT =
  "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
  "(KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36";

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

// ===== TMDB + AniList =====

function getTMDBDetails(tmdbId, mediaType) {
  var type = mediaType === "tv" ? "tv" : "movie";
  var url = TMDB_BASE_URL + "/" + type + "/" + tmdbId + "?api_key=" + TMDB_API_KEY;
  return httpGet(url)
    .then(function (body) {
      var d = JSON.parse(body);
      if (!d || d.success === false) return null;
      return {
        title: type === "tv" ? d.name : d.title,
        originalTitle: type === "tv" ? d.original_name : d.original_title,
        year: ((d.first_air_date || d.release_date || "") + "").substring(0, 4),
        mediaType: type
      };
    })
    .catch(function () { return null; });
}

function getAniListDetails(title, year) {
  var query =
    "query ($search: String, $year: Int, $type: MediaType) {" +
    "  Media(search: $search, seasonYear: $year, type: $type) {" +
    "    id title { romaji english native }" +
    "  }" +
    "}";
  var variables = { search: title, year: year ? parseInt(year, 10) : null, type: "ANIME" };
  return fetch(ANILIST_API_URL, {
    method: "POST",
    headers: { "Content-Type": "application/json", Accept: "application/json" },
    body: JSON.stringify({ query: query, variables: variables })
  })
    .then(function (r) { return r.json(); })
    .then(function (data) {
      if (data && data.data && data.data.Media) {
        var t = data.data.Media.title;
        return { romaji: t.romaji, english: t.english, native: t.native };
      }
      return null;
    })
    .catch(function () { return null; });
}

// ===== AUTOCOMPLETE INDEX =====
//
// /upload/autocomplete.js is a static file containing a JS array of
// [title, url] pairs for every anime on the site. It's served without
// the Cloudflare challenge, so plain fetch can get it.

var autocompleteCache = { data: null, ts: 0 };
var AUTOCOMPLETE_TTL = 24 * 60 * 60 * 1000;

function getAutocompleteIndex() {
  if (autocompleteCache.data && Date.now() - autocompleteCache.ts < AUTOCOMPLETE_TTL) {
    return Promise.resolve(autocompleteCache.data);
  }
  return httpGet(BASE_URL + "/upload/autocomplete.js")
    .then(function (text) {
      var m = text.match(/var\s+allseries\s*=\s*(\[.+?\]);/s);
      if (!m) throw new Error("Could not parse autocomplete.js");
      var arr = JSON.parse(m[1]);
      var index = {};
      arr.forEach(function (entry) {
        if (Array.isArray(entry) && entry.length >= 2) {
          var title = entry[0];
          var url = entry[1];
          if (!index[title.toLowerCase()]) {
            index[title.toLowerCase()] = { title: title, url: url };
          }
        }
      });
      console.log("[TokyoInsider] Autocomplete index: " + Object.keys(index).length + " titles");
      autocompleteCache.data = index;
      autocompleteCache.ts = Date.now();
      return index;
    })
    .catch(function (err) {
      console.log("[TokyoInsider] Autocomplete fetch failed: " + err.message);
      return {};
    });
}

function findAnimeUrl(index, title) {
  if (!index || !title) return null;
  var lower = title.toLowerCase();
  if (index[lower]) return BASE_URL + index[lower].url;
  var suffixes = [" (TV)", " (Movie)", " (OVA)", " (Special)", " (ONA)"];
  for (var i = 0; i < suffixes.length; i++) {
    if (index[lower + suffixes[i].toLowerCase()]) {
      return BASE_URL + index[lower + suffixes[i].toLowerCase()].url;
    }
  }
  var prefix = lower.substring(0, 20);
  var keys = Object.keys(index);
  for (var j = 0; j < keys.length; j++) {
    if (keys[j].indexOf(prefix) === 0) {
      return BASE_URL + index[keys[j]].url;
    }
  }
  return null;
}

// ===== DOWNLOAD LINK EXTRACTION =====

function parseQuality(text) {
  var t = String(text).toLowerCase();
  if (t.indexOf("2160p") !== -1 || t.indexOf("4k") !== -1 || t.indexOf("uhd") !== -1) return "2160p";
  if (t.indexOf("1440p") !== -1) return "1440p";
  if (t.indexOf("1080p") !== -1) return "1080p";
  if (t.indexOf("720p") !== -1) return "720p";
  if (t.indexOf("480p") !== -1) return "480p";
  if (t.indexOf("360p") !== -1) return "360p";
  return "Unknown";
}

function parseFileSize(text) {
  var m = String(text).match(/([0-9.]+\s*[KMGT]B)/i);
  return m ? m[1].replace(/\s+/g, "").toUpperCase() : null;
}

function extractDownloadLinks(html) {
  var links = [];
  var re = /<a[^>]+href=["']([^"']*(?:episode_download|\/download\/)[^"']+)["'][^>]*>([^<]+<\/a>)/gi;
  var match;
  while ((match = re.exec(html)) !== null) {
    var href = match[1];
    var label = match[2].replace(/<[^>]+>/g, "").trim();
    if (!label) continue;
    var quality = parseQuality(label);
    var size = parseFileSize(label);
    var fullUrl = href.indexOf("http") === 0 ? href : BASE_URL + (href.charAt(0) === "/" ? "" : "/") + href;
    links.push({
      url: fullUrl,
      filename: label,
      quality: quality,
      size: size
    });
  }

  if (links.length === 0) {
    var broadRe = /<a[^>]+href=["']([^"']+)["'][^>]*>([^<]*?\.(?:mkv|mp4)[^<]*?)<\/a>/gi;
    while ((match = broadRe.exec(html)) !== null) {
      var href2 = match[1];
      var label2 = match[2].trim();
      if (!label2) continue;
      var q2 = parseQuality(label2);
      var s2 = parseFileSize(label2);
      var fullUrl2 = href2.indexOf("http") === 0 ? href2 : BASE_URL + (href2.charAt(0) === "/" ? "" : "/") + href2;
      links.push({
        url: fullUrl2,
        filename: label2,
        quality: q2,
        size: s2
      });
    }
  }

  var seen = {};
  return links.filter(function (l) {
    if (seen[l.url]) return false;
    seen[l.url] = true;
    return true;
  });
}

// ===== MAIN ENTRY =====

function getStreams(tmdbId, mediaType, seasonNum, episodeNum) {
  var isMovie = mediaType !== "tv";
  console.log(
    "[TokyoInsider] Request: tmdb=" + tmdbId + " type=" + mediaType +
    (isMovie ? "" : " S" + seasonNum + "E" + episodeNum)
  );

  if (!isMovie && !episodeNum) {
    console.log("[TokyoInsider] Episode number required for TV");
    return Promise.resolve([]);
  }

  return getTMDBDetails(tmdbId, mediaType)
    .then(function (info) {
      if (!info) return [];
      console.log("[TokyoInsider] TMDB: " + info.title + " (" + info.year + ")");

      return getAniListDetails(info.title, info.year).then(function (anilist) {
        var candidates = [];
        if (anilist && anilist.romaji) candidates.push(anilist.romaji);
        if (info.title && (!anilist || info.title !== anilist.romaji)) candidates.push(info.title);
        if (anilist && anilist.english && anilist.english !== info.title && anilist.english !== anilist.romaji) {
          candidates.push(anilist.english);
        }
        if (info.originalTitle && info.originalTitle !== info.title && (!anilist || info.originalTitle !== anilist.romaji)) {
          candidates.push(info.originalTitle);
        }
        console.log("[TokyoInsider] Candidates: " + candidates.join(" | "));

        return getAutocompleteIndex().then(function (index) {
          var animeUrl = null;
          var usedTitle = null;
          for (var i = 0; i < candidates.length; i++) {
            animeUrl = findAnimeUrl(index, candidates[i]);
            if (animeUrl) {
              usedTitle = candidates[i];
              break;
            }
          }
          if (!animeUrl) {
            console.log("[TokyoInsider] No URL found in autocomplete index");
            return [];
          }
          console.log("[TokyoInsider] Found URL: " + animeUrl + " (using title: " + usedTitle + ")");

          var pageUrl = animeUrl;
          if (isMovie) {
            pageUrl = animeUrl.replace(/\/?$/, "/movie/1");
          } else {
            pageUrl = animeUrl.replace(/\/?$/, "/episode/" + episodeNum);
          }
          console.log("[TokyoInsider] Fetching page: " + pageUrl);

          return httpGet(pageUrl, { Referer: BASE_URL + "/" })
            .then(function (html) {
              if (!html || html.length < 100) {
                console.log("[TokyoInsider] Empty page response");
                return [];
              }
              if (html.indexOf("Just a moment...") !== -1) {
                console.log("[TokyoInsider] Got Cloudflare challenge page - cannot extract streams");
                return [];
              }
              var links = extractDownloadLinks(html);
              console.log("[TokyoInsider] Extracted " + links.length + " download links");
              if (!links.length) return [];

              return links.map(function (l) {
                var titleLine = info.title;
                if (!isMovie) {
                  titleLine += " S" + String(seasonNum || 1).padStart(2, "0") +
                               "E" + String(episodeNum || 1).padStart(2, "0");
                }
                titleLine += " (" + info.year + ")";
                if (l.size) titleLine += " " + l.size;

                return {
                  name: PROVIDER_NAME + (l.quality !== "Unknown" ? " - " + l.quality : ""),
                  title: l.filename || titleLine,
                  url: l.url,
                  quality: l.quality,
                  size: l.size || undefined,
                  headers: {
                    "User-Agent": USER_AGENT,
                    Referer: BASE_URL + "/"
                  },
                  behaviorHints: {
                    bingeGroup: "tokyoinsider-" + l.quality
                  }
                };
              });
            });
        });
      });
    })
    .catch(function (err) {
      console.log("[TokyoInsider] Error: " + (err && err.message ? err.message : err));
      return [];
    });
}

module.exports = { getStreams: getStreams };
