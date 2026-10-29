// ShowBox Scraper for Nuvio Local Scrapers
// Rewritten 2026-08-16 from nuvio-plugins/providers/showbox.js
//
// IMPORTANT — ShowBox is fundamentally a "bring your own FebBox account"
// provider. It does NOT host any content itself. Instead it:
//
//   1. Maps a TMDB ID to an internal ShowBox media ID via the
//      id-mapping-api-showbox-proxy.hf.space HuggingFace Space proxy.
//   2. Uses that ID to look up a FebBox share link.
//   3. Walks the FebBox share to find the file for the requested
//      movie or (SxxExx) episode.
//   4. Calls FebBox's /console/video_quality_list endpoint to enumerate
//      the available quality variants. THIS STEP REQUIRES A VALID
//      "ui" COOKIE FROM AN AUTHENTICATED FEBBOX SESSION.
//
// Without the ui cookie, every quality-list call returns empty HTML
// and no streams are produced.
//
// This rewrite is functionally identical to the original but adds:
//   - curl-based HTTP to bypass Cloudflare (FebBox sits behind CF)
//   - A clearer settings contract: pass the ui token via the
//     SHOWBOX_FEBBOX_UI_TOKEN environment variable, or via the
//     onSettings() UI prompt at runtime.
//   - 4K support: FebBox sometimes has 2160p variants. We no longer
//     filter them out.

"use strict";

var cheerio = require("cheerio-without-node-native");
var gsHelper = require("./got_scraping_helper");

var PROVIDER_NAME = "ShowBox";
var TMDB_API_KEY = "1c29a5198ee1854bd5eb45dbe8d17d92";
var TMDB_BASE_URL = "https://api.themoviedb.org/3";
var DEFAULT_API_BASE = "https://id-mapping-api-showbox-proxy.hf.space/api/media";
var FEBBOX_BASE = "https://www.febbox.com";

var USER_AGENT =
  "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
  "(KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36";

// Read the FebBox UI token from env (so it can be configured without UI)
var _uiToken = process.env.SHOWBOX_FEBBOX_UI_TOKEN || "";

function setUiToken(token) {
  _uiToken = String(token || "").trim();
  if (_uiToken && _uiToken.indexOf("ui=") === 0) _uiToken = _uiToken.substring(3);
}

function getUiToken() {
  return _uiToken;
}

// ===== HTTP =====

function httpGet(url, extraHeaders) {
  var headers = Object.assign(
    {
      "User-Agent": USER_AGENT,
      Accept: "application/json, text/html, */*;q=0.8",
      "Accept-Language": "en-US,en;q=0.9"
    },
    extraHeaders || {}
  );
  return gsHelper.httpGet(url, { headers: headers });
}

function httpGetJson(url, extraHeaders) {
  return httpGet(url, extraHeaders).then(function (body) {
    try { return JSON.parse(body); } catch (e) {
      throw new Error("Bad JSON from " + url + ": " + body.slice(0, 120));
    }
  });
}

// ===== TMDB =====

function getTMDBDetails(tmdbId, mediaType) {
  var type = mediaType === "tv" ? "tv" : "movie";
  var url = TMDB_BASE_URL + "/" + type + "/" + tmdbId + "?api_key=" + TMDB_API_KEY;
  return httpGetJson(url)
    .then(function (d) {
      if (!d || d.success === false) return null;
      return {
        title: type === "tv" ? d.name : d.title,
        year: ((d.first_air_date || d.release_date || "") + "").split("-")[0]
      };
    })
    .catch(function () { return null; });
}

// ===== ShowBox ID Mapping =====
//
// The HuggingFace Space proxy maps TMDB IDs to ShowBox internal IDs.
// Path: /api/media/<type>/<tmdbId>
// Response: {"success":true,"id":12345,"mid":12345,"versions":[]}
//
// Note: as of 2026-08-16, "versions" is always empty in our tests —
// the proxy database is up but doesn't have share URLs for most titles.

function getShowBoxId(tmdbId, mediaType) {
  var type = mediaType === "tv" ? "tv" : "movie";
  var url = DEFAULT_API_BASE + "/" + type + "/" + tmdbId;
  return httpGetJson(url)
    .then(function (data) {
      if (!data || !data.success) return null;
      return data.id || data.mid || null;
    })
    .catch(function () { return null; });
}

// ===== FebBox Share Resolution =====

function extractFebBoxShare(showboxId, mediaType, seasonNum, episodeNum) {
  var boxType = mediaType === "tv" ? 2 : 1;
  var shareUrl = FEBBOX_BASE + "/mbp/to_share_page?box_type=" + boxType + "&mid=" + showboxId + "&json=1";
  console.log("[ShowBox] FebBox share URL: " + shareUrl);

  return httpGetJson(shareUrl)
    .then(function (shareRes) {
      if (!shareRes || shareRes.code !== 1 || !shareRes.data) {
        console.log("[ShowBox] FebBox share not found for ShowBox ID " + showboxId);
        return [];
      }
      var shareLink = shareRes.data.share_link || shareRes.data.shareLink;
      if (!shareLink) return [];
      var shareKey = shareLink.split("/").pop();
      console.log("[ShowBox] FebBox share key: " + shareKey);

      var listUrl = FEBBOX_BASE + "/file/file_share_list?share_key=" + shareKey;
      return httpGetJson(listUrl, { "Accept-Language": "en" })
        .then(function (listRes) {
          if (!listRes || listRes.code !== 1 || !listRes.data || !listRes.data.file_list) {
            console.log("[ShowBox] FebBox file list empty");
            return [];
          }
          var fids = [];
          if (mediaType !== "tv") {
            fids = listRes.data.file_list;
          } else {
            // Find the season folder, then list its episodes
            var seasonName = "season " + seasonNum;
            var seasonFolder = null;
            for (var i = 0; i < listRes.data.file_list.length; i++) {
              if (listRes.data.file_list[i].file_name &&
                  listRes.data.file_list[i].file_name.toLowerCase() === seasonName) {
                seasonFolder = listRes.data.file_list[i];
                break;
              }
            }
            if (!seasonFolder) {
              console.log("[ShowBox] Season " + seasonNum + " folder not found");
              return [];
            }
            var seasonListUrl = FEBBOX_BASE + "/file/file_share_list?share_key=" + shareKey +
                                "&parent_id=" + seasonFolder.fid + "&page=1";
            return httpGetJson(seasonListUrl, { "Accept-Language": "en" })
              .then(function (seasonRes) {
                if (!seasonRes || seasonRes.code !== 1 || !seasonRes.data || !seasonRes.data.file_list) {
                  return [];
                }
                var sSlug = String(seasonNum).padStart(2, "0");
                var eSlug = String(episodeNum).padStart(2, "0");
                return seasonRes.data.file_list.filter(function (f) {
                  if (!f.file_name) return false;
                  var n = f.file_name.toLowerCase();
                  return n.indexOf("s" + sSlug + "e" + eSlug) !== -1 ||
                         n.indexOf("s" + seasonNum + "e" + episodeNum) !== -1;
                });
              });
          }
          return fids;
        });
    })
    .then(function (fids) {
      console.log("[ShowBox] Found " + fids.length + " matching FebBox file(s)");
      return fids;
    });
}

function fetchQualities(file, shareKey) {
  var qualityUrl = FEBBOX_BASE + "/console/video_quality_list?fid=" + file.fid + "&share_key=" + shareKey;
  var cookie = "ui=" + getUiToken();
  return httpGet(qualityUrl, { Cookie: cookie })
    .then(function (body) {
      try {
        var data = JSON.parse(body);
        if (!data || !data.html) return [];
        var $ = cheerio.load(data.html);
        var streams = [];
        $("div.file_quality").each(function (_, el) {
          var $q = $(el);
          var streamUrl = $q.attr("data-url");
          var qualityLabel = $q.attr("data-quality") || "";
          var sizeText = $q.find(".size").text().trim();
          if (!streamUrl) return;
          streams.push({
            url: streamUrl,
            quality: qualityLabel,
            size: sizeText,
            fileName: file.file_name
          });
        });
        return streams;
      } catch (e) {
        return [];
      }
    })
    .catch(function () { return []; });
}

function parseQuality(label) {
  var t = String(label || "").toLowerCase();
  if (t.indexOf("2160") !== -1 || t.indexOf("4k") !== -1 || t.indexOf("uhd") !== -1) return "2160p";
  if (t.indexOf("1440") !== -1) return "1440p";
  if (t.indexOf("1080") !== -1) return "1080p";
  if (t.indexOf("720") !== -1) return "720p";
  if (t.indexOf("480") !== -1) return "480p";
  if (t.indexOf("360") !== -1) return "360p";
  return "HD";
}

// ===== MAIN ENTRY =====

function getStreams(tmdbId, mediaType, seasonNum, episodeNum) {
  console.log(
    "[ShowBox] Request: tmdb=" + tmdbId + " type=" + mediaType +
    (mediaType === "tv" ? " S" + seasonNum + "E" + episodeNum : "")
  );

  if (!getUiToken()) {
    console.log("[ShowBox] No FebBox UI token configured.");
    console.log("[ShowBox] Set SHOWBOX_FEBBOX_UI_TOKEN env var or call setUiToken(token).");
    console.log("[ShowBox] To get the token: log into febbox.com, open browser devtools,");
    console.log("[ShowBox] find the 'ui' cookie, copy its value.");
    return Promise.resolve([]);
  }

  return getTMDBDetails(tmdbId, mediaType)
    .then(function (info) {
      if (!info) return [];
      console.log("[ShowBox] TMDB: " + info.title + " (" + (info.year || "?") + ")");

      return getShowBoxId(tmdbId, mediaType).then(function (showboxId) {
        if (!showboxId) {
          console.log("[ShowBox] No ShowBox ID mapping for TMDB " + tmdbId);
          return [];
        }
        console.log("[ShowBox] ShowBox ID: " + showboxId);

        return extractFebBoxShare(showboxId, mediaType, seasonNum, episodeNum)
          .then(function (fids) {
            if (!fids.length) return [];
            return Promise.all(fids.map(function (f) {
              // We need the share key for the quality URL. Re-derive it from the share_link call.
              var boxType = mediaType === "tv" ? 2 : 1;
              var shareUrl = FEBBOX_BASE + "/mbp/to_share_page?box_type=" + boxType + "&mid=" + showboxId + "&json=1";
              return httpGetJson(shareUrl).then(function (shareRes) {
                var shareLink = shareRes && shareRes.data && (shareRes.data.share_link || shareRes.data.shareLink);
                var shareKey = shareLink ? shareLink.split("/").pop() : "";
                return fetchQualities(f, shareKey);
              });
            })).then(function (qualityGroups) {
              var allStreams = [];
              qualityGroups.forEach(function (group) {
                group.forEach(function (s) { allStreams.push(s); });
              });
              console.log("[ShowBox] Total streams: " + allStreams.length);

              // De-duplicate by URL
              var seen = {};
              return allStreams.filter(function (s) {
                if (seen[s.url]) return false;
                seen[s.url] = true;
                return true;
              });
            });
          })
          .then(function (streams) {
            return streams.map(function (s) {
              var q = parseQuality(s.quality);
              var titleLine = info.title;
              if (mediaType === "tv") {
                titleLine += " S" + String(seasonNum).padStart(2, "0") +
                             "E" + String(episodeNum).padStart(2, "0");
              }
              titleLine += " (" + (info.year || "") + ")";
              if (s.size) titleLine += " " + s.size;

              return {
                name: PROVIDER_NAME + " - " + q + (s.size ? " (" + s.size + ")" : ""),
                title: (s.fileName || titleLine),
                url: s.url,
                quality: q,
                size: s.size || undefined,
                headers: {
                  "User-Agent": USER_AGENT,
                  Referer: FEBBOX_BASE + "/",
                  Range: "bytes=0-"
                },
                behaviorHints: {
                  bingeGroup: "showbox-" + q
                }
              };
            });
          });
      });
    })
    .catch(function (err) {
      console.log("[ShowBox] Error: " + (err && err.message ? err.message : err));
      return [];
    });
}

function onSettings() {
  return [
    {
      id: "showbox_febbox_ui_token",
      description: "FebBox UI Token cookie value. Log into febbox.com, open browser devtools, find the 'ui' cookie, copy its value here.",
      type: "string",
      default: ""
    }
  ];
}

module.exports = { getStreams: getStreams, onSettings: onSettings, setUiToken: setUiToken };
