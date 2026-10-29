// 4KHDHub Scraper for Nuvio Local Scrapers
// Rewritten 2026-08-17 to directly resolve Google Drive download URLs.
// Updated 2026-08-18 to use shared hub_extractor module.
//
// FLOW:
//   1. Search 4khdhub.one/?s=<title> → find movie/TV post URL
//   2. Fetch post → parse download sections (badges + hubcloud.ist links)
//   3. For each link: use hub_extractor.resolveHubcloudUrl() → direct GDrive URL
//   4. Return streams with quality (2160p/1080p), format, codec, and direct GDrive URL
//
// No Playwright, no FlareSolverr — uses hub_extractor (got-scraping + curl + fetch).

"use strict";

var cheerio = require("cheerio-without-node-native");
var he = require("./hub_extractor.js");

var PROVIDER_NAME = "4KHDHub";
var BASE_URL = "https://4khdhub.one";
var TMDB_API_KEY = "1c29a5198ee1854bd5eb45dbe8d17d92";
var DOMAINS_URL = "https://raw.githubusercontent.com/phisher98/TVVVV/refs/heads/main/domains.json";

var USER_AGENT =
  "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
  "(KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36";

var DEFAULT_HEADERS = {
  "User-Agent": USER_AGENT,
  Accept: "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
  "Accept-Language": "en-US,en;q=0.9"
};

// ===== DOMAIN RESOLUTION =====

var domainCache = { url: BASE_URL, ts: 0 };

function getBaseUrl() {
  var now = Date.now();
  if (now - domainCache.ts < 3600000) {
    return Promise.resolve(domainCache.url);
  }
  return fetch(DOMAINS_URL, { headers: { "User-Agent": "Mozilla/5.0" } })
    .then(function (r) { return r.json(); })
    .then(function (data) {
      var hit = data && (data["4khdhub"] || data["4KHDHub"]);
      if (hit && /^https?:\/\//.test(hit)) {
        domainCache.url = hit.replace(/\/+$/, "");
      }
      domainCache.ts = now;
      return domainCache.url;
    })
    .catch(function () {
      domainCache.ts = now;
      return domainCache.url;
    });
}

// ===== HTTP =====

function fetchText(url, extraHeaders) {
  var headers = Object.assign({}, DEFAULT_HEADERS, extraHeaders || {});
  return fetch(url, { headers: headers, redirect: "follow" })
    .then(function (res) {
      if (!res.ok) throw new Error("HTTP " + res.status + " for " + url);
      return res.text();
    });
}

// Follow a redirect chain manually and return the final URL.
// Used to extract the googleusercontent URL from the Location header
// without downloading the page body.
function getRedirectLocation(url, extraHeaders) {
  var headers = Object.assign({}, DEFAULT_HEADERS, extraHeaders || {});
  return fetch(url, { headers: headers, redirect: "manual" })
    .then(function (res) {
      return res.headers.get("location") || null;
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

function search4KHDHub(title) {
  return getBaseUrl().then(function (base) {
    var searchUrl = base + "/?s=" + encodeURIComponent(title);
    console.log("[4KHDHub] Searching: " + searchUrl);
    return fetchText(searchUrl, { Referer: base + "/" }).then(function (html) {
      var $ = cheerio.load(html);
      var results = [];

      // Find movie-card links
      $("a.movie-card").each(function (_, el) {
        var href = $(el).attr("href") || "";
        var label = $(el).attr("aria-label") || "";
        if (!href || !label) return;
        if (href.indexOf("http") !== 0) href = base + (href.charAt(0) === "/" ? "" : "/") + href;
        results.push({ url: href, title: label.replace(/\s+details$/, "").trim() });
      });

      // Fallback: any link that looks like a post slug
      if (results.length === 0) {
        $("a[href]").each(function (_, el) {
          var href = $(el).attr("href") || "";
          var label = $(el).attr("aria-label") || $(el).find("h3, h2, .title").first().text().trim();
          if (!href || !label) return;
          if (href.match(/\/[a-z0-9-]+-\d+\/?$/) && href.indexOf("/category/") === -1) {
            if (href.indexOf("http") !== 0) href = base + (href.charAt(0) === "/" ? "" : "/") + href;
            results.push({ url: href, title: label.replace(/\s+details$/, "").trim() });
          }
        });
      }

      console.log("[4KHDHub] Found " + results.length + " search results");
      return results;
    });
  });
}

function normalizeTitle(s) {
  return String(s || "").toLowerCase().replace(/[^a-z0-9\s]/g, " ").replace(/\s+/g, " ").trim();
}

function findBestMatch(results, tmdbTitle, tmdbYear, isMovie) {
  if (!results || !results.length) return null;
  var normQuery = normalizeTitle(tmdbTitle);
  var y = parseInt(tmdbYear, 10) || 0;

  var scored = results.map(function (r) {
    var raw = r.title || "";
    var yearMatch = raw.match(/\b(19|20)\d{2}\b/);
    var resultYear = yearMatch ? parseInt(yearMatch[0], 10) : 0;
    var bare = raw.replace(/^Download\s+/i, "").replace(/\s*[\(\[]?(19|20)\d{2}[\)\]]?.*$/i, "").trim();
    var normBare = normalizeTitle(bare);

    var score = 0;
    if (normBare === normQuery) score += 100;
    else if (normBare.indexOf(normQuery) === 0 || normQuery.indexOf(normBare) === 0) score += 70;
    else if (normBare.split(" ").slice(0, 3).join(" ") === normQuery.split(" ").slice(0, 3).join(" ")) score += 40;

    if (y && resultYear && y === resultYear) score += 30;

    // Check if it's a movie or series post
    var isSeriesPost = /series|s\d{2}/i.test(raw);
    if (isMovie && !isSeriesPost) score += 10;
    if (!isMovie && isSeriesPost) score += 10;

    return { result: r, score: score, year: resultYear, bare: bare };
  });

  scored.sort(function (a, b) { return b.score - a.score; });
  var best = scored[0];
  if (best && best.score >= 30) {
    console.log("[4KHDHub] Matched: " + best.bare + " (score=" + best.score + ")");
    return best.result;
  }
  return null;
}

// ===== DOWNLOAD LINK EXTRACTION =====

function parseQualityFromBadges(badges) {
  var text = badges.join(" ").toLowerCase();
  if (text.indexOf("2160p") !== -1 || text.indexOf("4k") !== -1) return "2160p";
  if (text.indexOf("1440p") !== -1) return "1440p";
  if (text.indexOf("1080p") !== -1) return "1080p";
  if (text.indexOf("720p") !== -1) return "720p";
  if (text.indexOf("480p") !== -1) return "480p";
  return "HD";
}

function parseFormatFromBadges(badges) {
  var text = badges.join(" ");
  if (/blu-?ray|bd|remux/i.test(text)) return "BluRay REMUX";
  if (/web-?dl|webrip/i.test(text)) return "WEB-DL";
  if (/nf|netflix/i.test(text)) return "NF WEB-DL";
  if (/amzn|amazon/i.test(text)) return "AMZN WEB-DL";
  return "BluRay";
}

function parseCodecFromBadges(badges) {
  var text = badges.join(" ");
  if (/dv\s*hdr|dolby\s*vision/i.test(text)) return "DV HDR";
  if (/hdr/i.test(text)) return "HDR";
  if (/10bit|10-bit|h265|hevc/i.test(text)) return "H265 10bit";
  if (/vc-1/i.test(text)) return "VC-1";
  if (/h264|x264/i.test(text)) return "H264";
  return "H265";
}

function parseLanguagesFromBadges(badges) {
  var langs = [];
  var text = badges.join(" ");
  if (/hindi/i.test(text)) langs.push("Hindi");
  if (/english/i.test(text)) langs.push("English");
  if (/tamil/i.test(text)) langs.push("Tamil");
  if (/telugu/i.test(text)) langs.push("Telugu");
  if (/bengali/i.test(text)) langs.push("Bengali");
  if (/korean/i.test(text)) langs.push("Korean");
  if (/japanese/i.test(text)) langs.push("Japanese");
  if (/spanish/i.test(text)) langs.push("Spanish");
  return langs.length ? langs.join("+") : "Multi";
}

function extractDownloadLinks(html, targetSeason, targetEpisode) {
  var $ = cheerio.load(html);
  var links = [];

  $("a").each(function (_, el) {
    var href = $(el).attr("href") || "";
    // Match all hubcloud.* domains (cx, foo, ist, etc.)
    if (!href.match(/https:\/\/hubcloud\.[a-z]+\/drive\//)) return;

    var $container = $(el).closest("div").parent();
    var badges = [];
    $container.find(".badge").each(function (_, badge) {
      var t = $(badge).text().trim();
      if (t && t.length > 0 && t.length < 50) badges.push(t);
    });

    // For TV: check if this link matches the requested SxxExx
    var nearbyText = $container.text().trim();
    var seasonMatch = nearbyText.match(/S(\d{2})E(\d{2})/i);
    if (targetSeason && targetEpisode) {
      if (seasonMatch) {
        var s = parseInt(seasonMatch[1], 10);
        var e = parseInt(seasonMatch[2], 10);
        if (s !== targetSeason || e !== targetEpisode) return;
      }
      // If no SxxExx pattern, check if it's a season-level link
      if (!seasonMatch) {
        var seasonOnly = nearbyText.match(/Season\s*(\d+)/i);
        if (seasonOnly && parseInt(seasonOnly[1], 10) !== targetSeason) return;
        // If neither SxxExx nor Season N, skip for TV
        if (!seasonOnly) return;
      }
    }

    var quality = parseQualityFromBadges(badges);
    var format = parseFormatFromBadges(badges);
    var codec = parseCodecFromBadges(badges);
    var lang = parseLanguagesFromBadges(badges);

    links.push({
      url: href,
      quality: quality,
      format: format,
      codec: codec,
      language: lang,
      badges: badges,
      season: seasonMatch ? parseInt(seasonMatch[1], 10) : null,
      episode: seasonMatch ? parseInt(seasonMatch[2], 10) : null
    });
  });

  // De-duplicate by URL
  var seen = {};
  return links.filter(function (l) {
    if (seen[l.url]) return false;
    seen[l.url] = true;
    return true;
  });
}

// ===== HUBCLOUD → GOOGLEUSERCONTENT RESOLUTION =====
// Uses the shared hub_extractor module (supports all hubcloud.* domains,
// multi-strategy fetch with got-scraping/curl/fetch fallbacks).

function resolveHubcloudUrl(hubcloudUrl) {
  return he.resolveHubcloudUrl(hubcloudUrl);
}

// ===== MAIN ENTRY =====

function getStreams(tmdbId, type, season, episode) {
  var isMovie = type !== "tv";
  var targetSeason = season ? parseInt(season, 10) : null;
  var targetEpisode = episode ? parseInt(episode, 10) : null;

  console.log(
    "[4KHDHub] Request: tmdb=" + tmdbId + " type=" + type +
    (isMovie ? "" : " S" + season + "E" + episode)
  );

  return getTMDBInfo(tmdbId, type)
    .then(function (info) {
      if (!info || !info.title) {
        console.log("[4KHDHub] Could not resolve TMDB info");
        return [];
      }
      console.log("[4KHDHub] TMDB: " + info.title + " (" + info.year + ")");

      return search4KHDHub(info.title).then(function (results) {
        if (!results.length) return [];
        var match = findBestMatch(results, info.title, info.year, isMovie);
        if (!match) {
          console.log("[4KHDHub] No matching post found");
          return [];
        }

        return getBaseUrl().then(function (base) {
          return fetchText(match.url, { Referer: base + "/" }).then(function (postHtml) {
            var links = extractDownloadLinks(postHtml, targetSeason, targetEpisode);
            console.log("[4KHDHub] Extracted " + links.length + " download links");

            if (!links.length) return [];

            // De-duplicate by quality+format+codec (keep first occurrence)
            var byKey = {};
            var unique = [];
            links.forEach(function (l) {
              var key = l.quality + "|" + l.format + "|" + l.codec;
              if (byKey[key]) return;
              byKey[key] = true;
              unique.push(l);
            });

            console.log("[4KHDHub] " + unique.length + " unique quality variants");

            // Resolve each hubcloud URL to a direct Google Drive URL
            return Promise.all(unique.map(function (l) {
              return resolveHubcloudUrl(l.url)
                .then(function (gdriveUrl) {
                  return Object.assign({}, l, { gdriveUrl: gdriveUrl });
                })
                .catch(function (err) {
                  console.log("[4KHDHub] Failed to resolve " + l.url + ": " + err.message);
                  return null;
                });
            })).then(function (resolved) {
              return resolved.filter(function (r) { return r !== null && r.gdriveUrl; });
            });
          }).then(function (resolvedLinks) {
            return resolvedLinks.map(function (l) {
              var titleLine = info.title;
              if (!isMovie) {
                titleLine += " S" + String(targetSeason || l.season || 1).padStart(2, "0") +
                             "E" + String(targetEpisode || l.episode || 1).padStart(2, "0");
              }
              titleLine += " (" + info.year + ")";
              titleLine += " " + l.quality + " " + l.format + " " + l.codec;
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
                  bingeGroup: "4khdhub-" + l.quality + "-" + l.codec
                }
              };
            });
          });
        });
      });
    })
    .catch(function (err) {
      console.log("[4KHDHub] Error: " + (err && err.message ? err.message : err));
      return [];
    });
}

module.exports = { getStreams: getStreams };
