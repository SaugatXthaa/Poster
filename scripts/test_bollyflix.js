// Test the rewritten bollyflix.js for both a movie and a TV show.
// Run: cd /tmp && node /home/z/my-project/scripts/test_bollyflix.js

process.on("unhandledRejection", function (err) {
  console.log("[unhandledRejection] " + (err && err.message ? err.message : err));
});

var bollyflix = require("/home/z/my-project/scripts/bollyflix.js");

var TESTS = [
  {
    label: "Inception (2010) movie",
    tmdbId: "27205",
    type: "movie",
    season: null,
    episode: null
  },
  {
    label: "Breaking Bad S01E01 tv",
    tmdbId: "1396",
    type: "tv",
    season: 1,
    episode: 1
  },
  {
    label: "Money Heist S01 tv",
    tmdbId: "71446", // La Casa de Papel
    type: "tv",
    season: 1,
    episode: 1
  }
];

(async function () {
  for (const t of TESTS) {
    console.log("\n========================================");
    console.log("TEST: " + t.label);
    console.log("========================================");
    try {
      var streams = await bollyflix.getStreams(
        t.tmdbId,
        t.type,
        t.season,
        t.episode
      );
      console.log("\n>>> RESULT: " + streams.length + " streams");
      streams.slice(0, 8).forEach(function (s, i) {
        console.log(
          "  [" +
            (i + 1) +
            "] " +
            s.name +
            " | " +
            (s.quality || "?") +
            " | " +
            (s.size || "?") +
            " | " +
            s.url.slice(0, 100)
        );
      });
      if (streams.length > 8) {
        console.log("  ... and " + (streams.length - 8) + " more");
      }
    } catch (e) {
      console.log(">>> EXCEPTION: " + (e && e.message ? e.message : e));
    }
  }
  process.exit(0);
})();
