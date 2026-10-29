// 1DesireMovies Scraper for Nuvio Local Scrapers
// Written 2026-08-17 — returns direct Google Drive download URLs.
//
// REVERSE-ENGINEERED FLOW:
//   1. Search 1desiremovies.wales/search/<title> → find movie/TV post URL
//      (Uses /search/ path because /?s= is CF-blocked)
//   2. Fetch post → parse for gyanigurus.online/view/<hash> links
//      (Each quality variant has a "G-DRIVE" and "DOWNLOAD" link)
//   3. For each gyanigurus link: fetch page → find hubcloud.cx/drive/<id>
//   4. For each hubcloud.cx link: resolve through the chain:
//      hubcloud.cx → gamerxyt.com → pixel.hubcloud.cx → workers.dev → dl.php?link=<gdrive>
//   5. Extract the googleusercontent URL → direct Google Drive download (video/mkv)
//
// The final googleusercontent URL is a DIRECT Google Drive download link
// that returns content-type: video/mkv and is playable in Stremio/Nuvio.
//
// Supports: movies, TV series (up to 4K/2160p when available).
// No Playwright, no FlareSolverr — uses plain fetch() throughout.

"use strict";

var cheerio = require("cheerio-without-node-native");
var he = require("./hub_extractor.js");

var PROVIDER_NAME = "1DesireMovies";
var BASE_URL = "https://1desiremovies.wales";
var TMDB_API_KEY = "1c29a5198ee1854bd5eb45dbe8d17d92";

var USER_AGENT =
  "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
  "(KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36";

var DEFAULT_HEADERS = {
  "User-Agent": USER_AGENT,
  Accept: "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
  "Accept-Language": "en-US,en;q=0.9"
};

// ===== HTTP =====

function fetchText(url, extraHeaders) {
  var headers = Object.assign({}, DEFAULT_HEADERS, extraHeaders || {});
  return fetch(url, { headers: headers, redirect: "follow" })
    .then(function (res) {
      if (!res.ok) throw new Error("HTTP " + res.status + " for " + url);
      return res.text();
    });
}

// ===== TMDB =====

function getTMDBInfo(tmdbId, mediaType) {
  var type = mediaType === "tv" ? "tv" : "movie";
  var url = "https://api.themoviedb.org/3/" + type + "/" + tmdbId + "?api_key=" + TMDB_API_KEY;
  return fetchText(url)
    .then(function (body) {
      var d = JSON.parse(body);
      if (!d || d.success === false) return null;
      return {
        title: type === "tv" ? d.name : d.title,
        year: ((d.first_air_date || d.release_date || "") + "").split("-")[0],
        type: type
      };
    })
    .catch(function () { return null; });
}

// ===== SEARCH =====

function normalizeTitle(s) {
  return String(s || "").toLowerCase().replace(/[^a-z0-9\s]/g, " ").replace(/\s+/g, " ").trim();
}

function searchDesireMovies(title) {
  // Use /search/<query> — the /?s= pattern is CF-blocked
  var searchUrl = BASE_URL + "/search/" + encodeURIComponent(title);
  console.log("[1DesireMovies] Searching: " + searchUrl);
  return fetchText(searchUrl, { Referer: BASE_URL + "/" }).then(function (html) {
    var $ = cheerio.load(html);
    var results = [];

    // Find post links (URLs with /<slug>/ that contain the search term)
    $("a[href]").each(function (_, el) {
      var href = $(el).attr("href") || "";
      var text = $(el).text().trim();
      if (href.indexOf(BASE_URL) !== -1 && text.length > 5 && text.length < 300) {
        if (!href.match(/\/(category|tag|page|about|contact|privacy|terms|dmca|wp-|feed|comments|how-to|request|join|disclaimer|\/\?s=|\/search\/|\/[0-9]{4}\/[0-9]{2}\/|\/author\/)/i)) {
          if (href.match(/\/[a-z0-9].*-\d*\/?$/) || text.match(/(19|20)\d{2}|4k|1080p|720p|480p|bluray|web-?dl|webrip/i)) {
            results.push({ url: href, title: text });
          }
        }
      }
    });

    // Dedupe
    var seen = {};
    results = results.filter(function (r) {
      if (seen[r.url]) return false;
      seen[r.url] = true;
      return true;
    });

    console.log("[1DesireMovies] Found " + results.length + " search results");
    return results;
  });
}

function findBestMatch(results, tmdbTitle, tmdbYear, isMovie) {
  if (!results || !results.length) return null;
  var normQuery = normalizeTitle(tmdbTitle);
  var y = parseInt(tmdbYear, 10) || 0;

  var scored = results.map(function (r) {
    var raw = r.title || "";
    var yearMatch = raw.match(/\b(19|20)\d{2}\b/);
    var resultYear = yearMatch ? parseInt(yearMatch[0], 10) : 0;
    var bare = raw.replace(/\s*[\(\[]?(19|20)\d{2}[\)\]]?.*$/i, "").trim();
    var normBare = normalizeTitle(bare);

    var score = 0;
    if (normBare === normQuery) score += 100;
    else if (normBare.indexOf(normQuery) === 0 || normQuery.indexOf(normBare) === 0) score += 70;
    else if (normBare.split(" ").slice(0, 3).join(" ") === normQuery.split(" ").slice(0, 3).join(" ")) score += 40;

    if (y && resultYear && y === resultYear) score += 30;

    return { result: r, score: score, year: resultYear, bare: bare };
  });

  scored.sort(function (a, b) { return b.score - a.score; });
  var best = scored[0];
  if (best && best.score >= 30) {
    console.log("[1DesireMovies] Matched: " + best.bare + " (score=" + best.score + ")");
    return best.result;
  }
  return null;
}

// ===== DOWNLOAD LINK EXTRACTION =====

function parseQuality(text) {
  var t = String(text || "").toLowerCase();
  if (t.indexOf("2160p") !== -1 || t.indexOf("4k") !== -1 || t.indexOf("uhd") !== -1) return "2160p";
  if (t.indexOf("1080p") !== -1) return "1080p";
  if (t.indexOf("720p") !== -1) return "720p";
  if (t.indexOf("480p") !== -1) return "480p";
  return "HD";
}

function parseCodec(text) {
  var t = String(text || "").toLowerCase();
  if (/10bit|10-bit|hevc|x265|h265/i.test(t)) return "H265 10bit";
  if (/x264|h264/i.test(t)) return "H264";
  return "HEVC";
}

function parseSize(text) {
  var m = String(text || "").match(/([0-9.]+\s*(?:GB|MB))/i);
  return m ? m[1].replace(/\s+/g, "") : null;
}

function parseLanguage(text) {
  var t = String(text || "");
  var langs = [];
  if (/hindi/i.test(t)) langs.push("Hindi");
  if (/english/i.test(t)) langs.push("English");
  if (/tamil/i.test(t)) langs.push("Tamil");
  if (/telugu/i.test(t)) langs.push("Telugu");
  if (/chinese/i.test(t)) langs.push("Chinese");
  if (/bengali/i.test(t)) langs.push("Bengali");
  if (/gujarati/i.test(t)) langs.push("Gujarati");
  return langs.length ? langs.join("+") : "Multi";
}

// Extract download links from the post. Supports both gyanigurus.online
// and hindifire.store link protectors.
function extractDownloadLinks(html, targetSeason, targetEpisode) {
  var $ = cheerio.load(html);
  var links = [];

  // Find all gyanigurus.online AND hindifire.store links
  $("a[href*='gyanigurus.online'], a[href*='hindifire.store']").each(function (_, el) {
    var href = $(el).attr("href") || "";
    var linkText = $(el).text().trim();

    // Get quality context from parent/grandparent text
    var $parent = $(el).parent();
    var $gparent = $parent.parent();
    var context = $gparent.text().trim().slice(0, 300);

    // For TV: check if this matches the requested SxxExx
    if (targetSeason && targetEpisode) {
      var sxxexx = context.match(/S(\d{2})E(\d{2})/i);
      if (sxxexx) {
        if (parseInt(sxxexx[1], 10) !== targetSeason || parseInt(sxxexx[2], 10) !== targetEpisode) return;
      }
    }

    var quality = parseQuality(context);
    var codec = parseCodec(context);
    var size = parseSize(context);
    var lang = parseLanguage(context);

    // Skip duplicate URLs
    var existing = links.find(function (l) { return l.url === href; });
    if (existing) return;

    links.push({
      url: href,
      linkText: linkText,
      quality: quality,
      codec: codec,
      size: size,
      language: lang,
      context: context
    });
  });

  // Dedupe by quality (keep first link per quality)
  var byQuality = {};
  var unique = [];
  links.forEach(function (l) {
    var key = l.quality + "|" + l.codec;
    if (!byQuality[key]) {
      byQuality[key] = true;
      unique.push(l);
    }
  });

  console.log("[1DesireMovies] Extracted " + unique.length + " unique quality variants from " + links.length + " links");
  return unique;
}

// ===== LINK PROTECTOR → HUBCLOUD RESOLUTION =====
// Uses the shared hub_extractor module.

function resolveLinkProtectorUrl(protectorUrl) {
  return he.resolveLinkProtectorUrl(protectorUrl);
}

// Resolve hubcloud → direct Google Drive URL (uses shared hub_extractor)
function resolveHubcloudUrl(hubcloudUrl) {
  return he.resolveHubcloudUrl(hubcloudUrl);
}

// Full resolution: link protector → hubcloud → googleusercontent
function resolveStreamUrl(protectorUrl) {
  return he.resolveFromLinkProtector(protectorUrl)
    .then(function (gdriveUrl) {
      console.log("[1DesireMovies] Resolved link protector → GDrive URL");
      return gdriveUrl;
    });
}

// ===== MAIN ENTRY =====

function getStreams(tmdbId, type, season, episode) {
  var isMovie = type !== "tv";
  var targetSeason = season ? parseInt(season, 10) : null;
  var targetEpisode = episode ? parseInt(episode, 10) : null;

  console.log(
    "[1DesireMovies] Request: tmdb=" + tmdbId + " type=" + type +
    (isMovie ? "" : " S" + season + "E" + episode)
  );

  return getTMDBInfo(tmdbId, type)
    .then(function (info) {
      if (!info || !info.title) {
        console.log("[1DesireMovies] Could not resolve TMDB info");
        return [];
      }
      console.log("[1DesireMovies] TMDB: " + info.title + " (" + info.year + ")");

      return searchDesireMovies(info.title).then(function (results) {
        if (!results.length) return [];
        var match = findBestMatch(results, info.title, info.year, isMovie);
        if (!match) {
          console.log("[1DesireMovies] No matching post found");
          return [];
        }

        return fetchText(match.url, { Referer: BASE_URL + "/" }).then(function (postHtml) {
          var links = extractDownloadLinks(postHtml, targetSeason, targetEpisode);
          if (!links.length) return [];

          // Resolve each gyanigurus link to a direct Google Drive URL
          return Promise.all(links.map(function (l) {
            return resolveStreamUrl(l.url)
              .then(function (gdriveUrl) {
                return Object.assign({}, l, { gdriveUrl: gdriveUrl });
              })
              .catch(function (err) {
                console.log("[1DesireMovies] Failed to resolve " + l.url + ": " + err.message);
                return null;
              });
          })).then(function (resolved) {
            return resolved.filter(function (r) { return r !== null && r.gdriveUrl; });
          });
        }).then(function (resolvedLinks) {
          return resolvedLinks.map(function (l) {
            var titleLine = info.title;
            if (!isMovie) {
              titleLine += " S" + String(targetSeason || 1).padStart(2, "0") +
                           "E" + String(targetEpisode || 1).padStart(2, "0");
            }
            titleLine += " (" + info.year + ")";
            titleLine += " " + l.quality + " " + l.codec;
            if (l.size) titleLine += " [" + l.size + "]";
            titleLine += " [" + l.language + "]";

            return {
              name: PROVIDER_NAME + " - " + l.quality + " " + l.codec + " [" + l.language + "]",
              title: titleLine,
              url: l.gdriveUrl,
              quality: l.quality,
              type: "video/mkv",
              headers: {
                "User-Agent": USER_AGENT
              },
              behaviorHints: {
                bingeGroup: "1desiremovies-" + l.quality
              }
            };
          });
        });
      });
    })
    .catch(function (err) {
      console.log("[1DesireMovies] Error: " + (err && err.message ? err.message : err));
      return [];
    });
}

module.exports = { getStreams: getStreams };
