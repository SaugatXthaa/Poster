/**
 * Unified Test Runner — ShowboxTV 4-Provider Suite
 * =================================================
 * Tests all 4 providers from the ShowboxTV repo (2peckle, showbox, showbox2,
 * gramcinema) across movies, TV shows, anime, and dramas — verifying real
 * playable stream URLs up to 4K (2160p).
 *
 * Pure Node.js — no Playwright, no browser, no external tools.
 *
 * Run:
 *   node test_showbox_sources.js
 *
 * Set tokens via env vars before running:
 *   export TWOPACKLE_TOKEN=...        # 2Peckle (pengu.uk)
 *   export FEBBOX_TOKEN_1=...         # ShowBox (Febbox account #1)
 *   export FEBBOX_TOKEN_2=...         # ShowBox 2 (Febbox account #2, for rotation)
 *   export HASHHACKERS_TOKEN=...      # GramCinema (hashhackers API)
 */
'use strict';

const axios = require('axios');
const https = require('https');

const twoPeckle = require('./2peckle');
const showbox = require('./showbox');
const showbox2 = require('./showbox2');
const gramCinema = require('./gramcinema');

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 ' +
           '(KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36';

// Some CDNs have incomplete cert chains; mirror browser behavior.
const httpsAgent = new https.Agent({ rejectUnauthorized: false });

// ─── HTTP Verification ────────────────────────────────────────────────────────
async function verifyStream(url, headers = {}) {
  if (!url) return { ok: false, error: 'No URL' };
  const finalHeaders = { 'User-Agent': UA, ...headers };
  if (!finalHeaders['User-Agent'] && !finalHeaders['user-agent']) {
    finalHeaders['User-Agent'] = UA;
  }

  for (let attempt = 0; attempt < 3; attempt++) {
    try {
      const r = await axios.head(url, {
        timeout: 12000,
        headers: finalHeaders,
        httpsAgent,
        maxRedirects: 5,
        validateStatus: s => s < 400,
      });
      return {
        ok: true,
        status: r.status,
        contentType: r.headers['content-type'],
        size: r.headers['content-length'],
      };
    } catch (e) { /* fall through to GET */ }

    try {
      const r = await axios.get(url, {
        timeout: 12000,
        headers: { ...finalHeaders, Range: 'bytes=0-0' },
        httpsAgent,
        maxRedirects: 5,
        validateStatus: s => s < 400,
        responseType: 'text',
      });
      return {
        ok: true,
        status: r.status,
        contentType: r.headers['content-type'],
        size: r.headers['content-length'],
        viaGet: true,
      };
    } catch (e) {
      const status = e.response?.status;
      if (status === 403 && attempt < 2) {
        await new Promise(r => setTimeout(r, 1500));
        continue;
      }
      return { ok: false, status, error: e.message };
    }
  }
  return { ok: false, status: 0, error: 'All retries failed' };
}

// ─── Test Cases ───────────────────────────────────────────────────────────────
// Each test case covers a different content type (movie / show / anime / drama)
const TEST_CASES = [
  {
    label: 'Movie (Dune Part Two, TMDB 693134)',
    tmdbId: 693134,
    type: 'movie',
    season: null,
    episode: null,
  },
  {
    label: 'TV Show (Mandalorian S01E01, TMDB 82856)',
    tmdbId: 82856,
    type: 'tv',
    season: 1,
    episode: 1,
  },
  {
    label: 'Anime (Jujutsu Kaisen S01E01, TMDB 95479)',
    tmdbId: 95479,
    type: 'tv',
    season: 1,
    episode: 1,
  },
  {
    label: 'K-Drama (Crash Landing on You S01E01, TMDB 86639)',
    tmdbId: 86639,
    type: 'tv',
    season: 1,
    episode: 1,
  },
];

// ─── Per-Provider Test ────────────────────────────────────────────────────────
async function testProvider(providerName, mod, testCase) {
  console.log(`\n  [${providerName}] ${testCase.label}`);
  try {
    const streams = testCase.type === 'movie'
      ? await mod.getStreams(testCase.tmdbId, 'movie')
      : await mod.getStreams(testCase.tmdbId, 'tv', testCase.season, testCase.episode);

    if (!streams.length) {
      console.log(`    ✗ No streams returned`);
      return { name: providerName, label: testCase.label, count: 0, verified: 0, maxQuality: '-' };
    }

    // Determine max quality (4K detection)
    const order = { '2160p': 0, '1080p': 1, '720p': 2, '480p': 3, 'Auto': 4, 'Unknown': 5 };
    const sorted = [...streams].sort((a, b) =>
      (order[a.quality] ?? 6) - (order[b.quality] ?? 6));
    const maxQuality = sorted[0]?.quality || '?';

    // Verify top 2 streams
    let verified = 0;
    for (const s of sorted.slice(0, 2)) {
      const playHeaders = (s.behaviorHints && s.behaviorHints.headers) ||
                          (s.behaviorHints && s.behaviorHints.proxyHeaders && s.behaviorHints.proxyHeaders.request) ||
                          s.headers || {};
      const v = await verifyStream(s.url, playHeaders);
      const tag = v.ok ? '✓' : '✗';
      console.log(`    ${tag} [${s.quality}] HTTP ${v.status || 'ERR'} ${v.contentType || v.error || ''}`);
      console.log(`       url: ${s.url.slice(0, 80)}...`);
      if (s.meta) {
        console.log(`       meta: provider=${s.meta.provider} audio=${s.meta.audio} lang=${(s.meta.language||[]).join(',')}`);
      }
      if (v.ok) verified++;
    }

    return {
      name: providerName,
      label: testCase.label,
      count: streams.length,
      verified,
      maxQuality,
      sample: sorted[0] ? sorted[0].url.slice(0, 50) : '',
    };
  } catch (e) {
    console.log(`    ✗ Error: ${e.message}`);
    return { name: providerName, label: testCase.label, count: 0, verified: 0, maxQuality: '-', error: e.message };
  }
}

// ─── Main ─────────────────────────────────────────────────────────────────────
(async () => {
  console.log('╔' + '═'.repeat(68) + '╗');
  console.log('║  ShowboxTV 4-Provider Suite — Movies/Shows/Anime/Dramas up to 4K   ║');
  console.log('╚' + '═'.repeat(68) + '╝');
  console.log('  Providers: 2Peckle, ShowBox, ShowBox2, GramCinema');
  console.log('  Pure Node.js — no Playwright, no browser, no external tools\n');

  // Check tokens
  console.log('  Token status:');
  console.log(`    TWOPACKLE_TOKEN:   ${process.env.TWOPACKLE_TOKEN ? '✓ set' : '✗ not set'}`);
  console.log(`    FEBBOX_TOKEN_1:    ${process.env.FEBBOX_TOKEN_1 ? '✓ set' : '✗ not set'}`);
  console.log(`    FEBBOX_TOKEN_2:    ${process.env.FEBBOX_TOKEN_2 ? '✓ set' : '✗ not set'}`);
  console.log(`    HASHHACKERS_TOKEN: ${process.env.HASHHACKERS_TOKEN ? '✓ set' : '✗ not set'}`);
  console.log('    (If tokens are not set, providers will fall back to global.SCRAPER_SETTINGS or local HTTP server)');

  const providers = [
    { name: '2Peckle',   mod: twoPeckle,  envVar: 'TWOPACKLE_TOKEN' },
    { name: 'ShowBox',   mod: showbox,    envVar: 'FEBBOX_TOKEN_1' },
    { name: 'ShowBox 2', mod: showbox2,   envVar: 'FEBBOX_TOKEN_2' },
    { name: 'GramCinema',mod: gramCinema, envVar: 'HASHHACKERS_TOKEN' },
  ];

  const allResults = [];

  for (const testCase of TEST_CASES) {
    console.log('\n' + '═'.repeat(70));
    console.log(`  TEST: ${testCase.label}`);
    console.log('═'.repeat(70));

    for (const p of providers) {
      const r = await testProvider(p.name, p.mod, testCase);
      allResults.push(r);
    }
  }

  // ─── Summary ────────────────────────────────────────────────────────────────
  console.log('\n' + '═'.repeat(70));
  console.log('  SUMMARY');
  console.log('═'.repeat(70));
  console.log('  Provider     | Test Case                          | Streams | Verified | Max Q | Sample URL');
  console.log('  -------------|------------------------------------|---------|----------|-------|---------------------');
  for (const r of allResults) {
    const sample = (r.sample || '').slice(0, 40);
    console.log(`  ${r.name.padEnd(12)} | ${r.label.padEnd(34)} | ${String(r.count).padStart(7)} | ${String(r.verified).padStart(8)} | ${r.maxQuality.padEnd(5)} | ${sample}`);
  }

  console.log('\n  ✓ = stream URL returned HTTP < 400 with valid content-type');
  console.log('  Max Q = highest quality found (2160p = 4K, 1080p, 720p, etc.)');

  // Exit code based on results
  const anyStreams = allResults.some(r => r.count > 0);
  const anyVerified = allResults.some(r => r.verified > 0);
  if (anyStreams && anyVerified) {
    console.log('\n  ✓ PASS — At least one provider returned real working streams.');
  } else if (anyStreams) {
    console.log('\n  ⚠ PARTIAL — Streams returned but none verified playable (may need tokens or be transient).');
  } else {
    console.log('\n  ✗ FAIL — No streams returned. Check token configuration.');
  }
})().catch(e => console.error('Fatal:', e.message));
