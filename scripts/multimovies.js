// MultiMovies Scraper for Nuvio Local Scrapers
// Written 2026-08-17 — returns embed URLs from multimovies.makeup (Dooplay theme).
//
// FLOW:
//   1. Search multimovies.makeup/?s=<title> → find post URL
//   2. Fetch post → extract post ID from data-id attribute
//   3. Call admin-ajax.php with action=doo_player_ajax → get embed URLs for each server
//   4. For the iqsmartgames embed: extract sub-provider URLs using IMDB ID
//   5. Return all embed URLs as playable streams
//
// The embed URLs are iframe-type streams. Some Stremio/Nuvio players support
// iframe embeds (they open the URL in a webview). For direct video URLs (mp4/mkv/m3u8),
// the embed pages need JavaScript rendering which requires Playwright.
//
// No Playwright, no FlareSolverr — uses plain fetch() + cheerio.

"use strict";

var cheerio = require("cheerio-without-node-native");

var PROVIDER_NAME = "MultiMovies";
var BASE_URL = "https://multimovies.makeup";
var AJAX_URL = "https://multimovies.motorcycles/wp-admin/admin-ajax.php";
var TMDB_API_KEY = "1c29a5198ee1854bd5eb45dbe8d17d92";

var USER_AGENT =
  "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
  "(KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36";

var DEFAULT_HEADERS = {
  "User-Agent": USER_AGENT,
  "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
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

function postJSON(url, body, extraHeaders) {
  var headers = Object.assign({}, DEFAULT_HEADERS, {
    "Content-Type": "application/x-www-form-urlencoded",
    "X-Requested-With": "XMLHttpRequest"
  }, extraHeaders || {});
  return fetch(url, {
    method: "POST",
    headers: headers,
    body: body,
    redirect: "follow"
  }).then(function (res) { return res.text(); });
}

// ===== TMDB =====

function getTMDBInfo(tmdbId, mediaType) {
  var type = mediaType === "tv" ? "tv" : "movie";
  var url = "https://api.themoviedb.org/3/" + type + "/" + tmdbId + "?api_key=" + TMDB_API_KEY + "&append_to_response=external_ids";
  return fetchText(url)
    .then(function (body) {
      var d = JSON.parse(body);
      if (!d || d.success === false) return null;
      return {
        title: type === "tv" ? d.name : d.title,
        year: ((d.first_air_date || d.release_date || "") + "").split("-")[0],
        type: type,
        imdbId: d.external_ids && d.external_ids.imdb_id ? d.external_ids.imdb_id : null
      };
    })
    .catch(function () { return null; });
}

// ===== SEARCH =====

function normalizeTitle(s) {
  return String(s || "").toLowerCase().replace(/[^a-z0-9\s]/g, " ").replace(/\s+/g, " ").trim();
}

function searchMultiMovies(title) {
  var searchUrl = BASE_URL + "/?s=" + encodeURIComponent(title);
  console.log("[MultiMovies] Searching: " + searchUrl);
  return fetchText(searchUrl, { Referer: BASE_URL + "/" }).then(function (html) {
    var $ = cheerio.load(html);
    var results = [];
    var normTitle = normalizeTitle(title);
    var firstWord = normTitle.split(" ")[0];

    $("a[href]").each(function (_, el) {
      var href = $(el).attr("href") || "";
      var text = $(el).text().trim();
      var ariaLabel = $(el).closest("[aria-label]").attr("aria-label") || "";
      var imgAlt = $(el).find("img").attr("alt") || "";
      var combined = (text + " " + ariaLabel + " " + imgAlt + " " + href).toLowerCase();

      if (href.indexOf("multimovies") !== -1 && combined.indexOf(firstWord) !== -1) {
        if (!href.match(/\/(category|tag|page|about|contact|privacy|terms|dmca|wp-|feed|comments|genre|trending|browse|\/\?s=|\/search\/)/i)) {
          // Use the most descriptive title: prefer img alt, then aria-label, then text
          var title = imgAlt || ariaLabel || text;
          if (title.length > 2) {
            // For duplicates, keep the one with the longest title
            var existing = results.find(function (r) { return r.url === href; });
            if (existing) {
              if (title.length > existing.title.length) {
                existing.title = title;
              }
            } else {
              results.push({ url: href, title: title });
            }
          }
        }
      }
    });

    console.log("[MultiMovies] Found " + results.length + " search results");
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
  if (best && best.score >= 20) {
    console.log("[MultiMovies] Matched: " + best.bare + " (score=" + best.score + ")");
    return best.result;
  }
  return null;
}

// ===== POST PARSING =====

// Extract the post ID from the post page HTML
function extractPostId(html) {
  var $ = cheerio.load(html);
  var postId = $("body").attr("class");
  if (postId) {
    var match = postId.match(/postid-(\d+)/);
    if (match) return match[1];
  }
  // Also try data-id attribute
  var dataId = $("[data-id]").attr("data-id");
  if (dataId) return dataId;
  // Try data-post on player options
  var dataPost = $(".dooplay_player_option").first().attr("data-post");
  if (dataPost) return dataPost;
  return null;
}

// Extract all server options from the post page
function extractServerOptions(html) {
  var $ = cheerio.load(html);
  var servers = [];
  $(".dooplay_player_option").each(function (_, el) {
    var post = $(el).attr("data-post");
    var type = $(el).attr("data-type");
    var nume = $(el).attr("data-nume");
    var title = $(el).find(".title").text().trim();
    if (post && nume) {
      servers.push({ post: post, type: type, nume: nume, title: title });
    }
  });
  return servers;
}

// Call admin-ajax to get the embed URL for a server
function getEmbedUrl(postId, type, nume, postUrl) {
  var body = "action=doo_player_ajax&post=" + postId + "&nume=" + nume + "&type=" + type;
  return postJSON(AJAX_URL, body, { Referer: postUrl })
    .then(function (response) {
      try {
        var data = JSON.parse(response);
        if (data && data.embed_url) {
          return { embedUrl: data.embed_url, type: data.type, serverTitle: "Server " + nume };
        }
      } catch (e) {}
      return null;
    })
    .catch(function () { return null; });
}

// Extract sub-provider URLs from the iqsmartgames embed page
// The page lists multiple providers that use IMDB IDs
function extractSubProviders(embedHtml, imdbId) {
  var providers = [];
  
  // The iqsmartgames page has URLs with ${FinalID} that we replace with the IMDB ID
  var templates = [
    "https://vidsrc.cc/v2/embed/movie/" + imdbId,
    "https://multiembed.mov/?video_id=" + imdbId,
    "https://player.videasy.net/movie/" + imdbId,
    "https://embed.su/embed/movie/" + imdbId,
    "https://moviesapi.club/movie/" + imdbId,
    "https://vidrock.net/movie/" + imdbId,
  ];
  
  // Check which templates are present in the embed page
  templates.forEach(function (url) {
    // Check if the base domain appears in the embed page
    var domain = url.match(/https?:\/\/([^/]+)/)[1];
    if (embedHtml.indexOf(domain) !== -1) {
      providers.push(url);
    }
  });
  
  return providers;
}

// ===== MAIN ENTRY =====

function getStreams(tmdbId, type, season, episode) {
  var isMovie = type !== "tv";
  var postId = null;
  var postUrl = null;
  var imdbId = null;
  var info = null;

  console.log("[MultiMovies] Request: tmdb=" + tmdbId + " type=" + type);

  return getTMDBInfo(tmdbId, type)
    .then(function (i) {
      info = i;
      if (!info || !info.title) return [];
      imdbId = info.imdbId;
      console.log("[MultiMovies] TMDB: " + info.title + " (" + info.year + ") IMDB: " + (imdbId || "N/A"));

      return searchMultiMovies(info.title);
    })
    .then(function (results) {
      if (!results.length) return null;
      var match = findBestMatch(results, info.title, info.year);
      if (!match) return null;
      postUrl = match.url;

      return fetchText(postUrl, { Referer: BASE_URL + "/" });
    })
    .then(function (postHtml) {
      if (!postHtml || typeof postHtml !== "string") return [];
      postId = extractPostId(postHtml);
      if (!postId) {
        console.log("[MultiMovies] Could not find post ID");
        return [];
      }
      console.log("[MultiMovies] Post ID: " + postId);

      var servers = extractServerOptions(postHtml);
      console.log("[MultiMovies] Found " + servers.length + " server options");

      // Get embed URLs for all servers
      return Promise.all(servers.map(function (s) {
        return getEmbedUrl(postId, s.type, s.nume, postUrl);
      }));
    })
    .then(function (embedResults) {
      if (!embedResults || !embedResults.length) return [];
      var valid = embedResults.filter(function (r) { return r !== null; });
      console.log("[MultiMovies] Got " + valid.length + " embed URLs");

      var streams = [];

      // For each embed URL, try to extract sub-providers
      return Promise.all(valid.map(function (r) {
        return fetchText(r.embedUrl, { Referer: postUrl })
          .then(function (embedHtml) {
            // Add the embed URL itself as a stream
            streams.push({
              name: PROVIDER_NAME + " - " + r.serverTitle,
              title: info.title + " (" + info.year + ") " + r.serverTitle,
              url: r.embedUrl,
              quality: "HD",
              type: "iframe",
              headers: { "User-Agent": USER_AGENT }
            });

            // If this is the iqsmartgames embed and we have an IMDB ID,
            // extract sub-provider URLs
            if (imdbId && (r.embedUrl.indexOf("iqsmartgames") !== -1 || r.embedUrl.indexOf("gdmirrorbot") !== -1)) {
              var subProviders = extractSubProviders(embedHtml, imdbId);
              subProviders.forEach(function (sp) {
                streams.push({
                  name: PROVIDER_NAME + " - " + sp.match(/\/\/([^/]+)/)[1],
                  title: info.title + " (" + info.year + ") " + sp.match(/\/\/([^/]+)/)[1],
                  url: sp,
                  quality: "HD",
                  type: "iframe",
                  headers: { "User-Agent": USER_AGENT }
                });
              });
            }

            // For p2pplay/rpmhub/uns.bio embeds, extract the hash and add the API URL
            var hashMatch = r.embedUrl.match(/#([a-z0-9]+)/);
            if (hashMatch) {
              // Try the p2pplay/rpmhub/uns.bio API
              var baseUrl = r.embedUrl.replace(/#.*$/, "");
              streams.push({
                name: PROVIDER_NAME + " - " + baseUrl.match(/\/\/([^/]+)/)[1],
                title: info.title + " (" + info.year + ") Direct Stream",
                url: baseUrl + "/api/v1/video?id=" + hashMatch[1],
                quality: "HD",
                type: "video/mp4",
                headers: {
                  "User-Agent": USER_AGENT,
                  "Referer": baseUrl + "/"
                }
              });
            }

            return null; // already pushed to streams
          })
          .catch(function () { return null; });
      })).then(function () {
        // Dedupe by URL
        var seen = {};
        return streams.filter(function (s) {
          if (seen[s.url]) return false;
          seen[s.url] = true;
          return true;
        });
      });
    })
    .then(function (streams) {
      console.log("[MultiMovies] Returning " + streams.length + " streams");
      return streams;
    })
    .catch(function (err) {
      console.log("[MultiMovies] Error: " + (err && err.message ? err.message : err));
      return [];
    });
}

module.exports = { getStreams: getStreams };
