// Test harness: load every nuvio provider from both repos,
// call getStreams for a popular movie + TV show, count streams.
//
// Usage:
//   node /home/z/my-project/scripts/test_providers.js
//
// Output: writes JSON + Markdown summary to /home/z/my-project/download/

const fs = require("fs");
const path = require("path");

// --- Test cases (popular, well-indexed titles) ---------------------------
const TEST_CASES = {
  movie: {
    // "Inception" (2010) - TMDB 27205, IMDB tt1375666
    tmdbId: "27205",
    imdbId: "tt1375666",
    title: "Inception (2010)",
    type: "movie",
    season: null,
    episode: null,
  },
  tv: {
    // "Breaking Bad" S01E01 - TMDB 1396
    tmdbId: "1396",
    imdbId: "tt0903747",
    title: "Breaking Bad S01E01",
    type: "tv",
    season: 1,
    episode: 1,
  },
};

const REPOS = [
  { name: "GitHub: nuvio-plugins", dir: "/tmp/nuvio-plugins/providers" },
  { name: "Codeberg: nuvio-plugin", dir: "/tmp/nuvio-plugin/providers" },
];

const TIMEOUT_MS = 20000; // 20s per provider per test case
const CONCURRENCY = 8; // run this many providers in parallel

function withTimeout(promise, ms, label) {
  return Promise.race([
    promise,
    new Promise((resolve) =>
      setTimeout(() => {
        console.log(`[timeout] ${label} exceeded ${ms}ms`);
        resolve(null);
      }, ms)
    ),
  ]);
}

async function loadProvider(file) {
  // Some providers reference `global.getStreams = getStreams` or use
  // UMD-style exports. Loading via require() handles both.
  try {
    // Clear cache so re-requiring in different runs doesn't bleed state.
    delete require.cache[require.resolve(file)];
    const mod = require(file);
    if (mod && typeof mod.getStreams === "function") return mod.getStreams;
    // Some UMD modules set module.exports = te($e) (a transpiled default)
    // try to find any async function on the module
    for (const k of Object.keys(mod || {})) {
      if (typeof mod[k] === "function") return mod[k];
    }
    return null;
  } catch (e) {
    console.log(`[load-error] ${path.basename(file)}: ${e.message}`);
    return null;
  }
}

async function runProvider(getStreams, file, testCase) {
  const label = `${path.basename(file)} :: ${testCase.title}`;
  if (!getStreams) return { ok: false, error: "no getStreams export", count: 0 };

  // Capture console.log to surface provider messages without spamming.
  const logs = [];
  const origLog = console.log;
  console.log = (...args) => logs.push(args.join(" "));
  try {
    const result = await withTimeout(
      Promise.resolve(
        getStreams(testCase.tmdbId, testCase.type, testCase.season, testCase.episode)
      ),
      TIMEOUT_MS,
      label
    );
    const count = Array.isArray(result) ? result.length : 0;
    return { ok: true, count, sample: (result || []).slice(0, 2), logs };
  } catch (e) {
    return { ok: false, error: e.message, count: 0, logs };
  } finally {
    console.log = origLog;
  }
}

// Make sure an unhandled rejection inside a provider doesn't tear down the
// whole test run -- log and continue.
process.on("unhandledRejection", (err) => {
  console.log(`[unhandledRejection] ${err && err.message ? err.message : err}`);
});
process.on("uncaughtException", (err) => {
  console.log(`[uncaughtException] ${err && err.message ? err.message : err}`);
});

// Run an async fn over an array with limited concurrency.
async function mapLimit(items, limit, fn) {
  const results = new Array(items.length);
  let cursor = 0;
  let active = 0;
  await new Promise((resolve) => {
    const launch = () => {
      while (active < limit && cursor < items.length) {
        const i = cursor++;
        active++;
        Promise.resolve()
          .then(() => fn(items[i], i))
          .then((r) => {
            results[i] = r;
            active--;
            if (cursor >= items.length && active === 0) resolve();
            else launch();
          })
          .catch((e) => {
            results[i] = { error: e.message };
            active--;
            if (cursor >= items.length && active === 0) resolve();
            else launch();
          });
      }
    };
    launch();
  });
  return results;
}

async function main() {
  const summary = [];

  // Build the full task list across all repos.
  const tasks = [];
  for (const repo of REPOS) {
    const files = fs
      .readdirSync(repo.dir)
      .filter((f) => f.endsWith(".js"))
      .sort()
      .map((f) => path.join(repo.dir, f));
    for (const file of files) {
      for (const key of Object.keys(TEST_CASES)) {
        tasks.push({ repo, file, key });
      }
    }
  }

  console.log(`Running ${tasks.length} test cases (concurrency=${CONCURRENCY})...`);

  await mapLimit(tasks, CONCURRENCY, async (task) => {
    const { repo, file, key } = task;
    const baseName = path.basename(file, ".js");
    const tc = TEST_CASES[key];
    let r;
    try {
      const getStreams = await loadProvider(file);
      r = await runProvider(getStreams, file, tc);
    } catch (e) {
      r = { ok: false, count: 0, error: `harness: ${e.message}` };
    }
    summary.push({
      source: repo.name,
      provider: baseName,
      test_case: key,
      title: tc.title,
      ok: r.ok,
      count: r.count,
      error: r.error || null,
      last_log: (r.logs || []).slice(-1)[0] || null,
    });
    process.stdout.write(
      `[${repo.name.split(":")[0]}] ${baseName.padEnd(14)} ${key.padEnd(5)} -> ${
        r.ok ? `OK streams=${r.count}` : `ERR ${r.error}`
      }\n`
    );
  });

  // Sort for stable output
  summary.sort((a, b) =>
    a.source === b.source
      ? a.provider.localeCompare(b.provider) || a.test_case.localeCompare(b.test_case)
      : a.source.localeCompare(b.source)
  );

  // Write JSON
  const outDir = "/home/z/my-project/download";
  fs.mkdirSync(outDir, { recursive: true });
  fs.writeFileSync(
    path.join(outDir, "provider_stream_counts.json"),
    JSON.stringify(summary, null, 2)
  );

  // Write Markdown summary
  const md = renderMarkdown(summary);
  fs.writeFileSync(path.join(outDir, "provider_stream_counts.md"), md);

  console.log("\n=== DONE ===");
  console.log(`JSON  : ${outDir}/provider_stream_counts.json`);
  console.log(`Markdown: ${outDir}/provider_stream_counts.md`);

  // Force exit -- some providers leave orphaned fetches/timers in the event
  // loop that would otherwise keep Node alive for minutes.
  process.exit(0);
}

function renderMarkdown(rows) {
  const bySource = {};
  for (const r of rows) {
    if (!bySource[r.source]) bySource[r.source] = [];
    bySource[r.source].push(r);
  }

  let md = "# Nuvio Plugin Provider Stream Count Report\n\n";
  md += `_Generated: ${new Date().toISOString()}_\n\n`;
  md +=
    "Each provider was invoked with two test cases: **Inception (2010)** (movie, TMDB 27205) and **Breaking Bad S01E01** (TV, TMDB 1396). Timeout per call: 30s.\n\n";

  for (const src of Object.keys(bySource)) {
    const list = bySource[src];
    md += `## ${src}\n\n`;
    md += "| Provider | Movie streams | TV streams | Status |\n";
    md += "|----------|--------------:|-----------:|--------|\n";

    // Group by provider
    const byProv = {};
    for (const r of list) {
      if (!byProv[r.provider]) byProv[r.provider] = {};
      byProv[r.provider][r.test_case] = r;
    }
    for (const p of Object.keys(byProv).sort()) {
      const m = byProv[p].movie;
      const t = byProv[p].tv;
      const mCount = m && m.ok ? m.count : 0;
      const tCount = t && t.ok ? t.count : 0;
      const status =
        m && m.ok && t && t.ok
          ? "ok"
          : m && !m.ok && t && !t.ok
          ? `fail: ${m.error || ""}`
          : m && !m.ok
          ? `movie fail: ${m.error}`
          : t && !t.ok
          ? `tv fail: ${t.error}`
          : "ok";
      md += `| ${p} | ${mCount} | ${tCount} | ${status} |\n`;
    }
    md += "\n";
  }

  // Totals
  md += "## Totals\n\n";
  md += "| Source | Providers tested | Providers with >=1 stream (movie) | Providers with >=1 stream (TV) | Total streams (movie) | Total streams (TV) |\n";
  md += "|--------|----------------:|----------------------------------:|------------------------------:|---------------------:|------------------:|\n";
  for (const src of Object.keys(bySource)) {
    const list = bySource[src];
    const providers = new Set(list.map((r) => r.provider)).size;
    const movieRows = list.filter((r) => r.test_case === "movie");
    const tvRows = list.filter((r) => r.test_case === "tv");
    const movieOk = movieRows.filter((r) => r.ok && r.count > 0).length;
    const tvOk = tvRows.filter((r) => r.ok && r.count > 0).length;
    const movieTotal = movieRows.reduce((s, r) => s + (r.ok ? r.count : 0), 0);
    const tvTotal = tvRows.reduce((s, r) => s + (r.ok ? r.count : 0), 0);
    md += `| ${src} | ${providers} | ${movieOk} | ${tvOk} | ${movieTotal} | ${tvTotal} |\n`;
  }

  return md;
}

main().catch((e) => {
  console.error("Fatal:", e);
  process.exit(1);
});
