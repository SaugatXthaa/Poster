// Comprehensive regression test for cinejoy, zxcstream, animezey
// Run: node /home/z/my-project/scripts/test_fixed_scrapers.js

"use strict";

var TESTS = [
  { tmdb: "27205",   type: "movie", label: "Inception (movie, 4K)" },
  { tmdb: "693134",  type: "movie", label: "Dune Part Two (movie, 4K)" },
  { tmdb: "438631",  type: "movie", label: "Dune 2021 (movie)" },
  { tmdb: "634649",  type: "movie", label: "Spider-Man NWH (movie)" },
  { tmdb: "76600",   type: "movie", label: "Avatar 2022 (movie)" },
  { tmdb: "1396",    type: "tv", season: 1, episode: 1, label: "Breaking Bad S01E01" },
  { tmdb: "82856",   type: "tv", season: 1, episode: 1, label: "Mandalorian S01E01 (4K)" },
  { tmdb: "95479",   type: "tv", season: 1, episode: 1, label: "Jujutsu Kaisen S01E01 (anime)" },
  { tmdb: "93405",   type: "tv", season: 1, episode: 1, label: "Squid Game S01E01" }
];

var SCRAPERS = ["cinejoy", "zxcstream", "animezey"];

function runOne(scraperName, test) {
  return new Promise(function (resolve) {
    var mod = require("./" + scraperName + ".js");
    var timeout = setTimeout(function () {
      console.log("  [" + scraperName + "] TIMEOUT");
      resolve({ scraper: scraperName, test: test.label, count: 0, error: "timeout" });
    }, 45000);

    Promise.resolve()
      .then(function () {
        return mod.getStreams(test.tmdb, test.type, test.season, test.episode);
      })
      .then(function (streams) {
        clearTimeout(timeout);
        streams = Array.isArray(streams) ? streams : [];
        // Verify the stream objects have required fields
        var validStreams = streams.filter(function (s) {
          return s && s.url && s.name && s.quality;
        });
        // Detect invisible/BOM chars in names
        var dirty = streams.filter(function (s) {
          return /[\uFEFF\u200B\u200C\u200D\u2060\u2061\u2062\u2063\u180E\u200E\u200F]/.test(s.name || "") ||
                 /[\uFEFF\u200B\u200C\u200D\u2060\u2061\u2062\u2063\u180E\u200E\u200F]/.test(s.title || "");
        });
        var maxQuality = streams.reduce(function (m, s) {
          return (s.quality > m ? s.quality : m);
        }, "");
        var result = {
          scraper: scraperName,
          test: test.label,
          count: streams.length,
          valid: validStreams.length,
          dirty: dirty.length,
          maxQuality: maxQuality,
          sample: streams.slice(0, 2).map(function (s) {
            return { name: s.name, q: s.quality, type: s.type };
          })
        };
        resolve(result);
      })
      .catch(function (err) {
        clearTimeout(timeout);
        console.log("  [" + scraperName + "] ERROR: " + (err && err.message ? err.message : err));
        resolve({ scraper: scraperName, test: test.label, count: 0, error: err.message });
      });
  });
}

async function main() {
  console.log("=============================================================");
  console.log("  CINEJOY / ZXCSTREAM / ANIMEZEY — Regression Test Suite");
  console.log("=============================================================\n");

  var totalStreams = 0;
  var totalDirtyNames = 0;
  var failures = [];

  for (var i = 0; i < TESTS.length; i++) {
    var test = TESTS[i];
    console.log("\n--- " + test.label + " (TMDB " + test.tmdb + ") ---");
    for (var j = 0; j < SCRAPERS.length; j++) {
      var result = await runOne(SCRAPERS[j], test);
      var status = "✓";
      if (result.error) status = "✗";
      else if (result.count === 0) status = "⚠";
      if (result.dirty > 0) {
        status += " DIRTY:" + result.dirty;
        totalDirtyNames += result.dirty;
      }
      console.log("  " + status + " " + result.scraper + ": " +
        (result.error ? "ERR: " + result.error :
         result.count + " streams | max=" + result.maxQuality));
      if (result.sample && result.samples !== 0 && result.count > 0) {
        result.samples = null;
      }
      if (result.count > 0) totalStreams += result.count;
      if (result.error || result.count === 0) {
        failures.push(result.scraper + " / " + test.label);
      }
    }
  }

  console.log("\n=============================================================");
  console.log("  SUMMARY");
  console.log("=============================================================");
  console.log("Total streams across all tests: " + totalStreams);
  console.log("Dirty names found:               " + totalDirtyNames);
  console.log("Failures (0-stream or error):    " + failures.length);
  if (failures.length > 0) {
    failures.forEach(function (f) { console.log("  - " + f); });
  }
}

main().catch(function (e) { console.error("FATAL:", e); process.exit(1); });
