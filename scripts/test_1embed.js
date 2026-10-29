// Test the rewritten 1embed.js for both a movie and a TV show.
// Run: cd /tmp && node /home/z/my-project/scripts/test_1embed.js

process.on("unhandledRejection", function (err) {
  console.log("[unhandledRejection] " + (err && err.message ? err.message : err));
});

var embed = require("/home/z/my-project/scripts/1embed.js");

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
    label: "Money Heist S01E01 tv",
    tmdbId: "71446",
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
      var streams = await embed.getStreams(
        t.tmdbId,
        t.type,
        t.season,
        t.episode
      );
      console.log("\n>>> RESULT: " + streams.length + " streams");
      streams.forEach(function (s, i) {
        console.log(
          "  [" +
            (i + 1) +
            "] " +
            s.name +
            " | " +
            (s.quality || "?") +
            " | subs=" +
            (s.subtitles ? s.subtitles.length : 0) +
            " | " +
            s.url.slice(0, 100) +
            "..."
        );
      });
    } catch (e) {
      console.log(">>> EXCEPTION: " + (e && e.message ? e.message : e));
    }
  }
  process.exit(0);
})();
