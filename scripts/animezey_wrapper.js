// AnimeZeY Wrapper — parses quality from stream name (obfuscated scraper doesn't set it)
var _azModule = null;
function loadAZ() {
  if (_azModule) return _azModule;
  try { _azModule = require("./animezey_orig.js"); }
  catch(e) { try { _azModule = require("./animezey.js"); } catch(e2) { _azModule = {getStreams:()=>Promise.resolve([])}; } }
  return _azModule;
}
function parseQuality(text) {
  var t = String(text||"").toLowerCase();
  if (t.indexOf("2160p")!==-1||t.indexOf("4k")!==-1||t.indexOf("uhd")!==-1) return "2160p";
  if (t.indexOf("1440p")!==-1) return "1440p";
  if (t.indexOf("1080p")!==-1) return "1080p";
  if (t.indexOf("720p")!==-1) return "720p";
  if (t.indexOf("480p")!==-1) return "480p";
  if (t.indexOf("360p")!==-1) return "360p";
  return "HD";
}
function getStreams(tmdbId, type, season, episode) {
  var mod = loadAZ();
  return Promise.resolve(mod.getStreams(tmdbId, type, season, episode))
    .then(function(streams) {
      if (!Array.isArray(streams)) return [];
      return streams.map(function(s) {
        if (!s) return s;
        var q = s.quality;
        if (!q || q === "" || q === undefined) {
          q = parseQuality(s.name) || parseQuality(s.title) || "HD";
        }
        return Object.assign({}, s, { quality: q, type: s.type || "video/mp4" });
      });
    })
    .catch(function() { return []; });
}
module.exports = { getStreams: getStreams };
