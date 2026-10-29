// 1Shows wrapper - loads the obfuscated 1shows.js from All-in-One-Nuvio
// and post-processes its output to:
//   1. Add the missing `quality` field (parsed from the stream name)
//   2. Use curl (via child_process) for all HTTP requests, bypassing
//      any Cloudflare/anti-bot blocks that Node's fetch would hit.
//
// The obfuscated 1shows.js calls the global `fetch()` function
// internally. We temporarily replace global.fetch with a curl-based
// implementation before calling the scraper, then restore it
// afterwards. This gives us the reliability of curl without
// modifying the obfuscated source.

"use strict";

var path = require("path");
var curlFetch = require("./curl_fetch");

var _showsModule = null;

function loadShows() {
  if (_showsModule) return _showsModule;
  try {
    _showsModule = require("./1shows.js");
  } catch (e) {
    try {
      _showsModule = require(path.join(__dirname, "1shows.js"));
    } catch (e2) {
      _showsModule = { getStreams: function () { return Promise.resolve([]); } };
    }
  }
  return _showsModule;
}

function parseQuality(text) {
  var t = String(text || "").toLowerCase();
  if (t.indexOf("2160p") !== -1 || t.indexOf("4k") !== -1 || t.indexOf("uhd") !== -1) return "2160p";
  if (t.indexOf("1440p") !== -1) return "1440p";
  if (t.indexOf("1080p") !== -1) return "1080p";
  if (t.indexOf("720p") !== -1) return "720p";
  if (t.indexOf("480p") !== -1) return "480p";
  if (t.indexOf("360p") !== -1) return "360p";
  return "HD";
}

function getStreams(tmdbId, mediaType, season, episode) {
  var mod = loadShows();
  // Run the obfuscated scraper with curl as the global fetch.
  return curlFetch.withCurlFetch(function () {
    return Promise.resolve(mod.getStreams(tmdbId, mediaType, season, episode));
  })
    .then(function (streams) {
      if (!Array.isArray(streams)) return [];
      return streams.map(function (s) {
        if (!s) return s;
        var q = s.quality;
        if (!q || q === "undefined" || q === undefined) {
          q = parseQuality(s.name) || parseQuality(s.title);
        }
        return Object.assign({}, s, { quality: q });
      });
    })
    .catch(function () { return []; });
}

module.exports = { getStreams: getStreams };
