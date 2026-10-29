// UHDMovies Wrapper — parses quality from stream name (obfuscated scraper doesn't set it)
var _uhdModule = null;
function loadUHD() {
  if (_uhdModule) return _uhdModule;
  try { _uhdModule = require("./uhdmovies_orig.js"); }
  catch(e) { try { _uhdModule = require("./uhdmovies.js"); } catch(e2) { _uhdModule = {getStreams:()=>Promise.resolve([])}; } }
  return _uhdModule;
}
function parseQuality(text) {
  var t = String(text||"").toLowerCase();
  if (t.indexOf("2160p")!==-1||t.indexOf("4k")!==-1||t.indexOf("uhd")!==-1) return "2160p";
  if (t.indexOf("1440p")!==-1) return "1440p";
  if (t.indexOf("1080p")!==-1) return "1080p";
  if (t.indexOf("720p")!==-1) return "720p";
  if (t.indexOf("480p")!==-1) return "480p";
  return "HD";
}
function getStreams(tmdbId, type, season, episode) {
  var mod = loadUHD();
  return Promise.resolve(mod.getStreams(tmdbId, type, season, episode))
    .then(function(streams) {
      if (!Array.isArray(streams)) return [];
      return streams.map(function(s) {
        if (!s) return s;
        var q = s.quality;
        if (!q || q === "" || q === undefined) {
          q = parseQuality(s.name) || parseQuality(s.title);
        }
        return Object.assign({}, s, { quality: q, type: s.type || "video/mkv" });
      });
    })
    .catch(function() { return []; });
}
module.exports = { getStreams: getStreams };
