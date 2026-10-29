// KMMovies Scraper for Nuvio Local Scrapers
// Written 2026-08-17 — returns direct Google Drive download URLs.
//
// REVERSE-ENGINEERED FLOW:
//   1. Search kmmovies.online/?s=<title> (with full browser headers to bypass CF)
//   2. Fetch post → parse download buttons (quality + size + magiclinks.lol URLs)
//   3. For each magiclinks.lol link:
//      a. Fetch with redirect follow → get insurance page with download host links
//      b. Find hubcloud.* link on the page
//   4. Resolve hubcloud → gamerxyt.com → pixel.hubcloud.cx → workers.dev →
//      dl.php?link=<googleusercontent_url>
//   5. Extract the googleusercontent URL → direct Google Drive download (video/mkv)
//
// Supports: movies, TV series, up to 4K/2160p when available.
// No Playwright, no FlareSolverr — uses plain fetch() + curl.

"use strict";

var cheerio = require("cheerio-without-node-native");
var { execFile } = require("child_process");
var path = require("path");
var he = require("./hub_extractor.js");

// Try to load got-scraping for CF bypass (works on Render where curl gets 403)
var _gsHelper = null;
function getGsHelper() {
  if (_gsHelper !== null) return _gsHelper;
  try {
    _gsHelper = require("./got_scraping_helper");
  } catch (e) {
    try {
      _gsHelper = require(path.join(__dirname, "got_scraping_helper"));
    } catch (e2) {
      _gsHelper = false;
    }
  }
  return _gsHelper;
}

var PROVIDER_NAME = "KMMovies";
var BASE_URL = "https://kmmovies.online";
var TMDB_API_KEY = "1c29a5198ee1854bd5eb45dbe8d17d92";

var USER_AGENT =
  "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
  "(KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36";

// Full browser headers to bypass Cloudflare
var FULL_HEADERS = {
  "User-Agent": USER_AGENT,
  "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8",
  "Accept-Language": "en-US,en;q=0.9",
  "Sec-Ch-Ua": '"Chromium";v="147", "Not?A_Brand";v="24"',
  "Sec-Ch-Ua-Mobile": "?0",
  "Sec-Ch-Ua-Platform": '"Windows"',
  "Sec-Fetch-Dest": "document",
  "Sec-Fetch-Mode": "navigate",
  "Sec-Fetch-Site": "none",
  "Sec-Fetch-User": "?1",
  "Upgrade-Insecure-Requests": "1"
};

// ===== HTTP =====

// Multi-strategy fetch:
// 1. For kmmovies.online: try got-scraping (Chrome TLS bypass) → curl → fetch
// 2. For magiclinks.lol: use curl with cookie jar (redirect chain needs cookies)
// 3. For hubcloud/gamerxyt: try curl → got-scraping → fetch
// 4. For everything else: plain fetch
function fetchText(url, extraHeaders) {
  var headers = Object.assign({}, FULL_HEADERS, extraHeaders || {});

  // kmmovies.online needs CF bypass (got-scraping first, then curl)
  if (url.indexOf("kmmovies") !== -1) {
    var gs = getGsHelper();
    if (gs) {
      return gs.httpGet(url, { headers: headers, timeout: 25000 })
        .then(function (body) {
          if (body && body.length > 50 && body.indexOf("Just a moment") === -1) {
            return body;
          }
          return fetchViaCurl(url, headers);
        })
        .catch(function () { return fetchViaCurl(url, headers); });
    }
    return fetchViaCurl(url, headers).catch(function () { return fetchViaPlainFetch(url, headers); });
  }

  // magiclinks.lol needs cookie jar for redirect chain (curl only)
  if (url.indexOf("magiclinks") !== -1) {
    return fetchViaCurl(url, headers).catch(function () { return fetchViaPlainFetch(url, headers); });
  }

  // hubcloud/gamerxyt: try curl first (handles redirects + cookies), then fetch
  if (url.indexOf("hubcloud") !== -1 || url.indexOf("gamerxyt") !== -1) {
    return fetchViaCurl(url, headers).catch(function () { return fetchViaPlainFetch(url, headers); });
  }

  // Everything else: plain fetch
  return fetchViaPlainFetch(url, headers);
}

function fetchViaCurl(url, headers) {
  return new Promise(function (resolve, reject) {
    var cookieFile = "/tmp/kmmovies_cookies.txt";
    var args = [
      "-sSk", "--max-time", "25", "-L", "--compressed",
      "-c", cookieFile, "-b", cookieFile,
      "-A", headers["User-Agent"],
      "-H", "Accept: " + headers["Accept"],
      "-H", "Accept-Language: " + headers["Accept-Language"],
      "-H", 'Sec-Ch-Ua: "Chromium";v="147", "Not?A_Brand";v="24"',
      "-H", "Sec-Ch-Ua-Mobile: ?0",
      "-H", 'Sec-Ch-Ua-Platform: "Windows"',
      "-H", "Sec-Fetch-Dest: document",
      "-H", "Sec-Fetch-Mode: navigate",
      "-H", "Sec-Fetch-Site: none",
      "-H", "Upgrade-Insecure-Requests: 1"
    ];
    if (headers["Referer"]) args.push("-H", "Referer: " + headers["Referer"]);
    args.push(url);
    execFile("curl", args, { encoding: "utf8", maxBuffer: 30 * 1024 * 1024, timeout: 30000, windowsHide: true },
      function (err, stdout) {
        if (err) { reject(new Error("curl failed: " + err.message)); return; }
        resolve(stdout || "");
      });
  });
}

function fetchViaPlainFetch(url, headers) {
  return fetch(url, { headers: headers, redirect: "follow" }).then(function (res) {
    if (!res.ok) throw new Error("HTTP " + res.status + " for " + url);
    return res.text();
  });
}

// ===== TMDB =====

function getTMDBInfo(tmdbId, mediaType) {
  var type = mediaType === "tv" ? "tv" : "movie";
  var url = "https://api.themoviedb.org/3/" + type + "/" + tmdbId + "?api_key=" + TMDB_API_KEY;
  return fetch(url, { headers: { "User-Agent": "Mozilla/5.0" } })
    .then(function (r) { return r.text(); })
    .then(function (body) {
      var d = JSON.parse(body);
      if (!d || d.success === false) return null;
      return {
        title: type === "tv" ? d.name : d.title,
        year: ((d.first_air_date || d.release_date || "") + "").split("-")[0]
      };
    })
    .catch(function () { return null; });
}

// ===== SEARCH =====

function normalizeTitle(s) {
  return String(s || "").toLowerCase().replace(/[^a-z0-9\s]/g, " ").replace(/\s+/g, " ").trim();
}

function searchKMMovies(title) {
  var searchUrl = BASE_URL + "/?s=" + encodeURIComponent(title);
  console.log("[KMMovies] Searching: " + searchUrl);
  return fetchText(searchUrl, { Referer: BASE_URL + "/" }).then(function (html) {
    var $ = cheerio.load(html);
    var results = [];
    var firstWord = normalizeTitle(title).split(" ")[0];

    // Find ALL links that contain the search term
    $("a[href]").each(function (_, el) {
      var href = $(el).attr("href") || "";
      var text = $(el).text().trim();
      var imgAlt = $(el).find("img").attr("alt") || "";
      var ariaLabel = $(el).closest("[aria-label]").attr("aria-label") || "";
      var combined = (text + " " + imgAlt + " " + ariaLabel + " " + href).toLowerCase();

      // Match any kmmovies.* domain (not just BASE_URL — results may link to kmmovies.pics etc.)
      if (combined.indexOf("kmmovies") === -1) return;
      if (combined.indexOf(firstWord) === -1) return;

      // Skip navigation/category/etc links
      if (href.match(/\/(category|tag|page|about|contact|privacy|terms|dmca|wp-|feed|comments|how-to|request|join|disclaimer|genre|trending|browse|\/\?s=|\/search\/)/i)) return;

      // Use the most descriptive title: prefer img alt, then aria-label, then text
      var linkTitle = imgAlt || ariaLabel || text;
      if (linkTitle.length > 2) {
        var existing = results.find(function (r) { return r.url === href; });
        if (existing) {
          if (linkTitle.length > existing.title.length) existing.title = linkTitle;
        } else {
          results.push({ url: href, title: linkTitle });
        }
      }
    });

    console.log("[KMMovies] Found " + results.length + " search results");
    return results;
  });
}

function findBestMatch(results, tmdbTitle, tmdbYear) {
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
    return { result: r, score: score, bare: bare };
  });
  scored.sort(function (a, b) { return b.score - a.score; });
  var best = scored[0];
  if (best && best.score >= 30) {
    console.log("[KMMovies] Matched: " + best.bare + " (score=" + best.score + ")");
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

function parseSize(text) {
  var m = String(text || "").match(/([0-9.]+\s*(?:GB|MB))/i);
  return m ? m[1].replace(/\s+/g, "") : null;
}

// Extract magiclinks.lol URLs with quality/size from the post.
function extractDownloadLinks(html) {
  var $ = cheerio.load(html);
  var links = [];

  // Find download buttons with magiclinks.lol URLs
  $("a[href*='magiclinks.lol']").each(function (_, el) {
    var href = $(el).attr("href") || "";
    var text = $(el).text().trim().replace(/\s+/g, " ");

    // Parse quality and size from the button text
    var quality = parseQuality(text);
    var size = parseSize(text);

    links.push({
      url: href,
      quality: quality,
      size: size,
      text: text
    });
  });

  // Dedupe by URL
  var seen = {};
  links = links.filter(function (l) {
    if (seen[l.url]) return false;
    seen[l.url] = true;
    return true;
  });

  console.log("[KMMovies] Extracted " + links.length + " download links");
  return links;
}

// ===== MAGICLINKS → HUBCLOUD RESOLUTION =====
// Uses the shared hub_extractor module for the full resolution chain.

// Fetch magiclinks.lol page and find hubcloud link
// Uses the kmmovies-specific fetchText (curl with cookie jar for redirect chain)
function resolveMagiclinksUrl(magiclinksUrl) {
  return fetchText(magiclinksUrl)
    .then(function (html) {
      var match = html.match(/https:\/\/hubcloud\.[a-z]+\/drive\/[a-zA-Z0-9_]+/);
      if (!match) throw new Error("No hubcloud link found on magiclinks page");
      return match[0];
    });
}

// Resolve hubcloud → direct GDrive URL (uses shared hub_extractor)
function resolveHubcloudUrl(hubcloudUrl) {
  return he.resolveHubcloudUrl(hubcloudUrl);
}

// Full resolution: magiclinks → hubcloud → googleusercontent
function resolveStreamUrl(magiclinksUrl) {
  return resolveMagiclinksUrl(magiclinksUrl)
    .then(function (hubcloudUrl) {
      console.log("[KMMovies] Resolved magiclinks → " + hubcloudUrl.slice(0, 50));
      return he.resolveHubcloudUrl(hubcloudUrl);
    });
}

// ===== MAIN ENTRY =====

function getStreams(tmdbId, type, season, episode) {
  var isMovie = type !== "tv";

  console.log("[KMMovies] Request: tmdb=" + tmdbId + " type=" + type);

  return getTMDBInfo(tmdbId, type)
    .then(function (info) {
      if (!info || !info.title) return [];
      console.log("[KMMovies] TMDB: " + info.title + " (" + info.year + ")");

      return searchKMMovies(info.title).then(function (results) {
        if (!results.length) return [];
        var match = findBestMatch(results, info.title, info.year);
        if (!match) return [];

        return fetchText(match.url, { Referer: BASE_URL + "/" }).then(function (postHtml) {
          var links = extractDownloadLinks(postHtml);
          if (!links.length) return [];

          // Resolve each magiclinks URL to a direct GDrive URL
          return Promise.all(links.map(function (l) {
            return resolveStreamUrl(l.url)
              .then(function (gdriveUrl) {
                return Object.assign({}, l, { gdriveUrl: gdriveUrl });
              })
              .catch(function (err) {
                console.log("[KMMovies] Failed to resolve " + l.url + ": " + err.message);
                return null;
              });
          })).then(function (resolved) {
            return resolved.filter(function (r) { return r !== null && r.gdriveUrl; });
          });
        }).then(function (resolvedLinks) {
          return resolvedLinks.map(function (l) {
            return {
              name: PROVIDER_NAME + " - " + l.quality + (l.size ? " [" + l.size + "]" : ""),
              title: info.title + " (" + info.year + ") " + l.quality + (l.size ? " " + l.size : ""),
              url: l.gdriveUrl,
              quality: l.quality,
              type: "video/mkv",
              headers: { "User-Agent": USER_AGENT },
              behaviorHints: { bingeGroup: "kmmovies-" + l.quality }
            };
          });
        });
      });
    })
    .catch(function (err) {
      console.log("[KMMovies] Error: " + (err && err.message ? err.message : err));
      return [];
    });
}

module.exports = { getStreams: getStreams };
