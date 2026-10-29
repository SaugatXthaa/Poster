// ZinkMovies wrapper - loads the obfuscated zinkmovies.js from
// All-in-One-Nuvio and post-processes its output to:
//   1. Add the `quality` field (parsed from the stream name).
//   2. Set the `type` field to "application/x-mpegurl" for HLS streams.
//   3. Add retry logic because the gemma416okl.com API intermittently
//      returns "7" (an error code) instead of the playlist JSON.
//
// IMPORTANT: The gemma416okl.com API returns time-limited HLS stream
// URLs that expire within seconds. We therefore do NOT fetch the master
// playlist to determine quality (that would cause the URL to expire
// before the user can play it). Instead, we use the quality from the
// stream name. The actual HLS playlist typically contains variants
// from 360p to 1080p; the player auto-selects the best one.
//
// 4K (2160p) content is available on the main website
// (new2.zinkmovies.mobi) as direct download links, but that site is
// behind Cloudflare Turnstile which cannot be bypassed without a real
// browser. The gemma416okl.com API only returns embed streams up to
// 1080p.
//
// The gemma416okl.com API only accepts Node's native fetch() — it
// blocks got-scraping and curl. We therefore do NOT replace global.fetch.

"use strict";

var path = require("path");

var _zmModule = null;

function loadZM() {
  if (_zmModule) return _zmModule;
  try {
    _zmModule = require("./zinkmovies.js");
  } catch (e) {
    try {
      _zmModule = require(path.join(__dirname, "zinkmovies.js"));
    } catch (e2) {
      _zmModule = { getStreams: function () { return Promise.resolve([]); } };
    }
  }
  return _zmModule;
}

// Parse quality from the stream name. The gemma API returns names like:
//   "ZinkMovies | hd | Gemma | Hindi"
// The quality is usually "hd" but we check for higher values too.
function parseQualityFromName(text) {
  var t = String(text || "").toLowerCase();
  if (t.indexOf("2160p") !== -1 || t.indexOf("4k") !== -1 || t.indexOf("uhd") !== -1) return "2160p";
  if (t.indexOf("1440p") !== -1) return "1440p";
  if (t.indexOf("1080p") !== -1) return "1080p";
  if (t.indexOf("720p") !== -1) return "720p";
  if (t.indexOf("480p") !== -1) return "480p";
  if (t.indexOf("360p") !== -1) return "360p";
  // The gemma API typically returns "hd" which means the HLS playlist
  // has multiple quality variants (usually 360p-1080p). We report it
  // as "1080p" since that's typically the max variant in the playlist.
  if (t.indexOf("hd") !== -1) return "1080p";
  return "HD";
}

// Extract language from the stream name.
function parseLanguage(text) {
  var m = String(text || "").match(/(?:Hindi|English|Tamil|Telugu|Bengali|Multi|Spanish|Korean|Japanese|Malayalam|Kannada|Marathi|Punjabi|Gujarati)/i);
  return m ? m[0] : "";
}

// Run the obfuscated scraper with retries. The gemma416okl.com API
// intermittently returns "7" (an error code) instead of the playlist
// JSON, causing 0 streams. Retrying usually fixes it.
function getStreamsWithRetry(tmdbId, mediaType, season, episode, maxRetries) {
  var mod = loadZM();
  var attempt = 0;
  
  function attemptFetch() {
    attempt++;
    return Promise.resolve(mod.getStreams(tmdbId, mediaType, season, episode))
      .then(function (streams) {
        if (Array.isArray(streams) && streams.length > 0) {
          return streams;
        }
        if (attempt < maxRetries) {
          console.log("[ZinkMovies] Got 0 streams on attempt " + attempt + "/" + maxRetries + ", retrying...");
          return new Promise(function (resolve) {
            setTimeout(resolve, 1500);
          }).then(attemptFetch);
        }
        return [];
      });
  }
  
  return attemptFetch();
}

function getStreams(tmdbId, mediaType, season, episode) {
  return getStreamsWithRetry(tmdbId, mediaType, season, episode, 5)
    .then(function (streams) {
      if (!Array.isArray(streams) || streams.length === 0) return [];

      // Post-process streams immediately (no master playlist fetch —
      // the URLs expire too fast for that to work).
      return streams.map(function (s) {
        if (!s) return s;
        var quality = parseQualityFromName(s.name) || parseQualityFromName(s.title) || "HD";
        var lang = parseLanguage(s.name);
        var langSuffix = lang ? " " + lang : "";

        return Object.assign({}, s, {
          quality: quality,
          type: "application/x-mpegurl",
          name: "ZinkMovies - " + quality + langSuffix
        });
      });
    })
    .catch(function () { return []; });
}

module.exports = { getStreams: getStreams };
