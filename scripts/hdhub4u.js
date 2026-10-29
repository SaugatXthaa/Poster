// HDHub4u Scraper for Nuvio Local Scrapers
// Updated 2026-08-18 — uses shared hub_extractor module.
//
// FLOW:
//   1. Search new1.hdhub4u.af/search/<title> → find movie/TV post URL
//   2. Fetch post → parse quality headings (h3/h4) with download links
//   3. For each link: use hub_extractor.resolveDownloadUrl() → direct GDrive URL
//      - hubcdn.sbs links → base64 decode → GDrive URL ✅
//      - hubcloud.* links → gamerxyt chain → GDrive URL ✅
//      - pixeldrain links → direct download API ✅
//      - greenmountmotors.com links → needs JS execution ❌ (skipped)
//      - hubdrive.tips links → needs auth ❌ (skipped)
//   4. Return streams with quality + direct GDrive URL
//
// No Playwright, no FlareSolverr — uses hub_extractor (got-scraping + curl + fetch).

"use strict";

var cheerio = require("cheerio-without-node-native");
var he = require("./hub_extractor.js");

var PROVIDER_NAME = "HDHub4u";
var BASE_URL = "https://new1.hdhub4u.af";
var DOMAINS_URL = "https://raw.githubusercontent.com/phisher98/TVVVV/refs/heads/main/domains.json";
var TMDB_API_KEY = "1c29a5198ee1854bd5eb45dbe8d17d92";

var USER_AGENT = he.USER_AGENT;
var DEFAULT_HEADERS = he.FULL_HEADERS;

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
      var hit = data && (data["HDHUB4u"] || data["hdhub4u"]);
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

function searchHDHub4u(title) {
  return getBaseUrl().then(function (base) {
    var searchUrl = base + "/search/" + encodeURIComponent(title);
    console.log("[HDHub4u] Searching: " + searchUrl);
    return fetchText(searchUrl, { Referer: base + "/" }).then(function (html) {
      var $ = cheerio.load(html);
      var results = [];

      $("a[href]").each(function (_, el) {
        var href = $(el).attr("href") || "";
        var text = $(el).text().trim();
        if (href.indexOf(base) !== -1 && text.length > 5 && text.length < 300) {
          if (!href.match(/\/(category|tag|page|about|contact|privacy|terms|dmca|wp-|feed|comments|how-to|request|join|disclaimer)/i)) {
            if (text.match(/(19|20)\d{2}|4k|1080p|720p|480p|bluray|web-?dl|webrip/i)) {
              results.push({ url: href, title: text });
            }
          }
        }
      });

      var seen = {};
      results = results.filter(function (r) {
        if (seen[r.url]) return false;
        seen[r.url] = true;
        return true;
      });

      console.log("[HDHub4u] Found " + results.length + " search results");
      return results;
    });
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
    var isSeriesPost = /series|s\d{2}|season/i.test(raw);
    if (isMovie && !isSeriesPost) score += 10;
    if (!isMovie && isSeriesPost) score += 10;
    return { result: r, score: score, bare: bare };
  });
  scored.sort(function (a, b) { return b.score - a.score; });
  var best = scored[0];
  if (best && best.score >= 30) {
    console.log("[HDHub4u] Matched: " + best.bare + " (score=" + best.score + ")");
    return best.result;
  }
  return null;
}

// ===== DOWNLOAD LINK EXTRACTION =====

function parseQuality(heading) {
  var t = String(heading || "").toLowerCase();
  if (t.indexOf("2160p") !== -1 || t.indexOf("4k") !== -1 || t.indexOf("uhd") !== -1) return "2160p";
  if (t.indexOf("1080p") !== -1) return "1080p";
  if (t.indexOf("720p") !== -1) return "720p";
  if (t.indexOf("480p") !== -1) return "480p";
  return "HD";
}

function parseCodec(heading) {
  var t = String(heading || "").toLowerCase();
  if (/10bit|10-bit|hevc|x265|h265/i.test(t)) return "H265 10bit";
  if (/x264|h264/i.test(t)) return "H264";
  return "HEVC";
}

function parseSize(heading) {
  var m = String(heading || "").match(/\[?([0-9.]+\s*(?:GB|MB))\]?/i);
  return m ? m[1].replace(/\s+/g, "") : null;
}

function parseLanguage(heading) {
  var t = String(heading || "");
  var langs = [];
  if (/hindi/i.test(t)) langs.push("Hindi");
  if (/english/i.test(t)) langs.push("English");
  if (/tamil/i.test(t)) langs.push("Tamil");
  if (/telugu/i.test(t)) langs.push("Telugu");
  return langs.length ? langs.join("+") : "Multi";
}

// Extract all download links from the post HTML.
// Supports: hubcdn.sbs, hubcloud.*, hubdrive.tips, pixeldrain, greenmountmotors.com
function extractDownloadLinks(html, targetSeason, targetEpisode) {
  var $ = cheerio.load(html);
  var links = [];

  // Find quality headings with download links
  $("h3, h4, h5").each(function (_, el) {
    var $h = $(el);
    var heading = $h.text().trim();
    if (!heading.match(/480p|720p|1080p|2160p|4k|uhd/i)) return;

    // For TV: check if this matches the requested SxxExx
    if (targetSeason && targetEpisode) {
      var sxxexx = heading.match(/S(\d{2})E(\d{2})/i);
      if (sxxexx) {
        if (parseInt(sxxexx[1], 10) !== targetSeason || parseInt(sxxexx[2], 10) !== targetEpisode) return;
      }
    }

    // Find the download link — it's INSIDE the heading or in the next element
    var $links = $h.find("a[href]");
    if ($links.length === 0) {
      $links = $h.next().find("a[href]");
    }

    $links.each(function (_, a) {
      var href = $(a).attr("href") || "";
      var quality = parseQuality(heading);
      var codec = parseCodec(heading);
      var size = parseSize(heading);
      var lang = parseLanguage(heading);

      // Determine link type and whether it's resolvable
      var linkType = "unknown";
      var resolvable = false;

      if (href.indexOf("hubcdn.sbs") !== -1) {
        linkType = "hubcdn";
        resolvable = true;
      } else if (href.match(/hubcloud\.[a-z]+\/drive\//)) {
        linkType = "hubcloud";
        resolvable = true;
      } else if (href.match(/pixeldrain\.(?:com|dev)\/[ul]\//)) {
        linkType = "pixeldrain";
        resolvable = true;
      } else if (href.indexOf("hubdrive.tips") !== -1 || href.indexOf("hubdrive.cc") !== -1) {
        linkType = "hubdrive";
        resolvable = false; // requires auth
      } else if (href.indexOf("greenmountmotors") !== -1) {
        linkType = "greenmountmotors";
        resolvable = false; // requires JS execution
      } else if (href.indexOf("hdstream4u") !== -1) {
        linkType = "hdstream4u";
        resolvable = false;
      } else if (href.indexOf("hubstream") !== -1) {
        linkType = "hubstream";
        resolvable = false;
      } else if (href.indexOf("video-downloads.googleusercontent.com") !== -1) {
        linkType = "gdrive";
        resolvable = true; // already direct
      }

      links.push({
        url: href,
        heading: heading,
        quality: quality,
        codec: codec,
        size: size,
        language: lang,
        linkType: linkType,
        resolvable: resolvable
      });
    });
  });

  // De-duplicate by URL
  var seen = {};
  links = links.filter(function (l) {
    if (seen[l.url]) return false;
    seen[l.url] = true;
    return true;
  });

  // De-duplicate by quality+codec (keep first per quality)
  var byKey = {};
  var unique = [];
  links.forEach(function (l) {
    var key = l.quality + "|" + l.codec;
    if (!byKey[key]) {
      byKey[key] = true;
      unique.push(l);
    }
  });

  console.log("[HDHub4u] Extracted " + unique.length + " download links (" +
    unique.filter(function(l) { return l.resolvable; }).length + " resolvable, " +
    unique.filter(function(l) { return !l.resolvable; }).length + " not resolvable)");

  return unique;
}

// ===== HDHUB ADDON FALLBACK =====
// When greenmountmotors links can't be resolved (no JS), use the HdHub addon API
// to get pre-resolved PixelDrain and HubCloud URLs.

var HDHUB_ADDON_BASE = "https://hdhub.thevolecitor.qzz.io";
var HDHUB_CONFIG = "eyJ0b3Jib3giOiJ1bnNldCIsInF1YWxpdGllcyI6IjIxNjBwLDEwODBwLDcyMHAsNDgwcCIsInNvcnQiOiJkZXNjIiwiY2F0YWxvZ3MiOiIifQ";

function getHdHubAddonStreams(tmdbId, type, season, episode, info) {
  // Build the IMDB ID from TMDB
  var streamUrl = HDHUB_ADDON_BASE + "/" + HDHUB_CONFIG + "/stream/" + (type === "tv" ? "series" : "movie") + "/tmdb:" + tmdbId + ".json";

  console.log("[HDHub4u] Trying HdHub addon fallback: " + streamUrl.slice(0, 80));

  return fetch(streamUrl, { headers: { "User-Agent": USER_AGENT }, redirect: "follow" })
    .then(function (r) { return r.text(); })
    .then(function (body) {
      try {
        var data = JSON.parse(body);
        var streams = data.streams || [];
        var result = [];

        streams.forEach(function (s) {
          var url = s.url || "";
          var name = (s.name || "").trim();
          var desc = (s.description || "").trim();

          // Skip donation/discord links
          if (!url || url.indexOf("http") === -1) return;
          if (name.indexOf("Donation") !== -1 || name.indexOf("Discord") !== -1) return;

          // Parse quality from the stream name/description
          var quality = "HD";
          var qText = (name + " " + desc).toLowerCase();
          if (qText.indexOf("2160p") !== -1 || qText.indexOf("4k") !== -1) quality = "2160p";
          else if (qText.indexOf("1080p") !== -1) quality = "1080p";
          else if (qText.indexOf("720p") !== -1) quality = "720p";
          else if (qText.indexOf("480p") !== -1) quality = "480p";

          // Parse size from description
          var sizeMatch = desc.match(/\[.*?([0-9.]+\s*(?:GB|MB)).*?\]/i);
          var size = sizeMatch ? sizeMatch[1].replace(/\s+/g, "") : null;

          // Detect source type
          var sourceType = "Other";
          if (url.indexOf("pixeldrain") !== -1) sourceType = "PixelDrain";
          else if (url.indexOf("pixel.hubcloud") !== -1) sourceType = "HubCloud";
          else if (url.indexOf("gpdl2.hubcloud") !== -1) sourceType = "HubCloud";
          else if (url.indexOf("video-downloads.googleusercontent") !== -1) sourceType = "GDrive";
          else if (url.indexOf("r2.cloudflarestorage") !== -1 || url.indexOf("r2.dev") !== -1) sourceType = "R2";
          else if (url.indexOf("/resolve/cj/") !== -1) sourceType = "CineJoy";
          else if (url.indexOf("hubdrive.tips") !== -1) sourceType = "HubDrive";

          // Include all resolvable/direct stream types
          if (sourceType === "Other") return;

          // For CineJoy HLS streams, use the URL directly (it's an m3u8)
          if (sourceType === "CineJoy") {
            result.push({
              name: PROVIDER_NAME + " - " + quality + " [" + sourceType + "]",
              title: (info.title || "") + " " + quality + " [" + sourceType + "]",
              url: url,
              quality: quality,
              type: "application/vnd.apple.mpegurl",
              headers: { "User-Agent": USER_AGENT },
              behaviorHints: { bingeGroup: "hdhub4u-" + quality + "-cj" }
            });
          } else if (sourceType === "HubCloud") {
            // HubCloud URLs need resolution — add them as-is (the player will follow redirects)
            result.push({
              name: PROVIDER_NAME + " - " + quality + " [" + sourceType + "]",
              title: (info.title || "") + " " + quality + (size ? " " + size : "") + " [" + sourceType + "]",
              url: url,
              quality: quality,
              type: "video/mkv",
              headers: { "User-Agent": USER_AGENT },
              behaviorHints: { bingeGroup: "hdhub4u-" + quality }
            });
          } else {
            // PixelDrain, GDrive, R2 — already direct URLs
            result.push({
              name: PROVIDER_NAME + " - " + quality + " [" + sourceType + "]",
              title: (info.title || "") + " " + quality + (size ? " " + size : "") + " [" + sourceType + "]",
              url: url,
              quality: quality,
              type: sourceType === "PixelDrain" ? "video/x-matroska" : "video/mkv",
              headers: { "User-Agent": USER_AGENT },
              behaviorHints: { bingeGroup: "hdhub4u-" + quality }
            });
          }
        });

        // Dedupe by URL
        var seen = {};
        result = result.filter(function (s) {
          if (seen[s.url]) return false;
          seen[s.url] = true;
          return true;
        });

        console.log("[HDHub4u] HdHub addon fallback: " + result.length + " streams");
        return result;
      } catch (e) {
        console.log("[HDHub4u] HdHub addon fallback failed: " + e.message);
        return [];
      }
    })
    .catch(function () { return []; });
}

// ===== MAIN ENTRY =====

function getStreams(tmdbId, type, season, episode) {
  var isMovie = type !== "tv";
  var targetSeason = season ? parseInt(season, 10) : null;
  var targetEpisode = episode ? parseInt(episode, 10) : null;
  var info = null;

  console.log(
    "[HDHub4u] Request: tmdb=" + tmdbId + " type=" + type +
    (isMovie ? "" : " S" + season + "E" + episode)
  );

  return getTMDBInfo(tmdbId, type)
    .then(function (i) {
      info = i;
      if (!info || !info.title) {
        console.log("[HDHub4u] Could not resolve TMDB info");
        return [];
      }
      console.log("[HDHub4u] TMDB: " + info.title + " (" + info.year + ")");

      return searchHDHub4u(info.title).then(function (results) {
        if (!results.length) return [];
        var match = findBestMatch(results, info.title, info.year, isMovie);
        if (!match) {
          console.log("[HDHub4u] No matching post found");
          return [];
        }

        return getBaseUrl().then(function (base) {
          return fetchText(match.url, { Referer: base + "/" }).then(function (postHtml) {
            var links = extractDownloadLinks(postHtml, targetSeason, targetEpisode);
            if (!links.length) return [];

            // Log all link types
            links.forEach(function (l) {
              console.log("[HDHub4u] " + l.quality + " " + l.codec + " [" + l.linkType + "] " +
                (l.resolvable ? "✓ resolvable" : "✗ not resolvable") +
                (l.size ? " " + l.size : ""));
            });

            var resolvable = links.filter(function (l) { return l.resolvable; });

            if (!resolvable.length) {
              console.log("[HDHub4u] No directly resolvable links — using HdHub addon fallback");
              return getHdHubAddonStreams(tmdbId, type, season, episode, info);
            }

            // Resolve each link using hub_extractor
            return Promise.all(resolvable.map(function (l) {
              return he.resolveDownloadUrl(l.url)
                .then(function (gdriveUrl) {
                  if (!gdriveUrl) return null;
                  return Object.assign({}, l, { gdriveUrl: gdriveUrl });
                })
                .catch(function (err) {
                  console.log("[HDHub4u] Failed to resolve " + l.url.slice(0, 60) + ": " + err.message);
                  return null;
                });
            })).then(function (resolved) {
              var resolvedLinks = resolved.filter(function (r) { return r !== null && r.gdriveUrl; });

              // If direct resolution failed, try HdHub addon fallback
              if (!resolvedLinks.length) {
                console.log("[HDHub4u] Direct resolution failed — using HdHub addon fallback");
                return getHdHubAddonStreams(tmdbId, type, season, episode, info);
              }

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
                  headers: { "User-Agent": USER_AGENT },
                  behaviorHints: { bingeGroup: "hdhub4u-" + l.quality }
                };
              });
            });
          });
        });
      });
    })
    .catch(function (err) {
      console.log("[HDHub4u] Error: " + (err && err.message ? err.message : err));
      return [];
    });
}

module.exports = { getStreams: getStreams };
