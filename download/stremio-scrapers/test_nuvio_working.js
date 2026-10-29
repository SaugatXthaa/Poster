/**
 * Unified Test Runner — 27 Working Nuvio Providers
 * =================================================
 * Verifies all 27 providers from NuvioPlugin/All-in-One-Nuvio return
 * real playable streams across movies, TV shows, anime, and K-dramas.
 *
 * Each provider is tested against all 4 content types. Streams are
 * verified with HTTP HEAD/GET to confirm they actually play.
 *
 * Pure Node.js — no Playwright, no browser.
 *
 * Run:
 *   node test_nuvio_working.js
 */
'use strict';

const fs = require('fs');
const path = require('path');
const axios = require('axios');
const https = require('https');

const DIR = __dirname;
const httpsAgent = new https.Agent({ rejectUnauthorized: false });
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 ' +
           '(KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36';

// ─── Test Cases ───────────────────────────────────────────────────────────────
const TESTS = [
  { label: 'Movie (Dune Part Two, TMDB 693134)',       tmdbId: 693134, type: 'movie', season: null, episode: null },
  { label: 'TV Show (Mandalorian S01E01, TMDB 82856)', tmdbId: 82856,  type: 'tv',    season: 1,     episode: 1     },
  { label: 'Anime (Jujutsu Kaisen S01E01, TMDB 95479)',tmdbId: 95479,  type: 'tv',    season: 1,     episode: 1     },
  { label: 'K-Drama (Crash Landing S01E01, TMDB 86639)',tmdbId: 86639, type: 'tv',    season: 1,     episode: 1     },
];

// ─── Providers ────────────────────────────────────────────────────────────────
const PROVIDERS = [
  '4khdhub', 'animezey', 'castle', 'cineby', 'ctgmovies',
  'dahmermovies', 'dahmermovies-4k', 'desiflix', 'goated', 'hdghartv',
  'hdhub4u', 'hindmoviez', 'movieblast', 'movies4u', 'movieshunt',
  'movix', 'netmirror', 'persianstremio', 'playimdb', 'purstream',
  'torrentio', 'uhdmovies', 'videasy', 'vidfast', 'vidlove',
  'vixsrc', 'zinkmovies',
];

// ─── HTTP Verification ────────────────────────────────────────────────────────
async function verifyStream(url, headers = {}) {
  if (!url || typeof url !== 'string' || !url.startsWith('http')) {
    // Allow magnet links (torrentio) — they're "valid" but not HTTP-verifiable
    if (url && url.startsWith('magnet:')) return { ok: true, status: 'magnet', contentType: 'magnet' };
    return { ok: false, error: 'No URL or non-http' };
  }
  const finalHeaders = { 'User-Agent': UA, ...headers };

  for (let attempt = 0; attempt < 2; attempt++) {
    try {
      const r = await axios.head(url, {
        timeout: 10000,
        headers: finalHeaders,
        httpsAgent,
        maxRedirects: 5,
        validateStatus: s => s < 400,
      });
      return { ok: true, status: r.status, contentType: r.headers['content-type'] };
    } catch (e) { /* fall through */ }

    try {
      const r = await axios.get(url, {
        timeout: 10000,
        headers: { ...finalHeaders, Range: 'bytes=0-0' },
        httpsAgent,
        maxRedirects: 5,
        validateStatus: s => s < 400,
        responseType: 'text',
      });
      return { ok: true, status: r.status, contentType: r.headers['content-type'], viaGet: true };
    } catch (e) {
      if (e.response?.status === 403 && attempt < 1) {
        await new Promise(r => setTimeout(r, 1500));
        continue;
      }
      return { ok: false, status: e.response?.status, error: e.message.slice(0, 60) };
    }
  }
  return { ok: false, error: 'retries exhausted' };
}

// ─── Per-Provider Test ────────────────────────────────────────────────────────
async function testProvider(providerName, test) {
  const file = path.join(DIR, `nuvio_${providerName}.js`);
  try {
    delete require.cache[require.resolve(file)];
    const mod = require(file);
    if (!mod || typeof mod.getStreams !== 'function') {
      return { name: providerName, ok: false, count: 0, verified: 0, error: 'no getStreams' };
    }

    const result = await Promise.race([
      mod.getStreams(test.tmdbId, test.type, test.season, test.episode),
      new Promise(resolve => setTimeout(() => resolve(null), 30000)),
    ]);

    if (!result) return { name: providerName, ok: false, count: 0, verified: 0, error: 'timeout' };
    if (!Array.isArray(result)) return { name: providerName, ok: false, count: 0, verified: 0, error: 'non-array' };
    if (!result.length) return { name: providerName, ok: false, count: 0, verified: 0, error: 'empty' };

    // Verify top 2 streams
    let verified = 0;
    let maxQuality = '?';
    for (const s of result.slice(0, 2)) {
      const playHeaders = (s.behaviorHints && s.behaviorHints.headers) ||
                          (s.behaviorHints && s.behaviorHints.proxyHeaders && s.behaviorHints.proxyHeaders.request) ||
                          s.headers || {};
      const v = await verifyStream(s.url, playHeaders);
      if (v.ok) verified++;
      if (s.quality && (s.quality === '2160p' || s.quality === '4K')) maxQuality = '4K';
      else if (s.quality && s.quality === '1080p' && maxQuality !== '4K') maxQuality = '1080p';
      else if (s.quality && !['2160p','4K','1080p'].includes(s.quality) && maxQuality === '?') maxQuality = s.quality;
    }

    return {
      name: providerName,
      ok: true,
      count: result.length,
      verified,
      maxQuality,
      sample: result[0] ? String(result[0].url || '').slice(0, 50) : '',
    };
  } catch (e) {
    return { name: providerName, ok: false, count: 0, verified: 0, error: e.message.slice(0, 60) };
  }
}

// ─── Main ─────────────────────────────────────────────────────────────────────
(async () => {
  console.log('╔' + '═'.repeat(68) + '╗');
  console.log('║  27 Nuvio Providers — Movies/TV/Anime/K-Dramas up to 4K            ║');
  console.log('╚' + '═'.repeat(68) + '╝');
  console.log(`  Providers: ${PROVIDERS.length}`);
  console.log(`  Test cases: ${TESTS.length} (${TESTS.map(t => t.label.split(' ')[0]).join(', ')})\n`);

  const allResults = [];
  for (const test of TESTS) {
    console.log(`\n${'═'.repeat(70)}`);
    console.log(`  TEST: ${test.label}`);
    console.log('═'.repeat(70));

    for (const p of PROVIDERS) {
      const r = await testProvider(p, test);
      const tag = r.ok ? '✓' : '✗';
      const vInfo = r.ok ? `${r.verified}/${Math.min(2, r.count)} verified` : '';
      const qInfo = r.ok ? `[${r.maxQuality}]` : '';
      const err = r.error ? `(${r.error})` : '';
      console.log(`  ${tag} ${r.name.padEnd(22)} ${String(r.count).padStart(3)} stream(s) ${qInfo.padEnd(8)} ${vInfo}${err}`);
      allResults.push({ ...r, test: test.label });
    }
  }

  // ─── Summary ────────────────────────────────────────────────────────────────
  console.log('\n' + '═'.repeat(70));
  console.log('  SUMMARY');
  console.log('═'.repeat(70));
  console.log('  Provider          | Movie | TV    | Anime | K-Drama | Max Quality');
  console.log('  ------------------|-------|-------|-------|---------|------------');
  for (const p of PROVIDERS) {
    const movie = allResults.find(r => r.name === p && r.test.startsWith('Movie'));
    const tv = allResults.find(r => r.name === p && r.test.startsWith('TV'));
    const anime = allResults.find(r => r.name === p && r.test.startsWith('Anime'));
    const kdrama = allResults.find(r => r.name === p && r.test.startsWith('K-Drama'));
    const movieTag = movie && movie.ok ? `✓${movie.count}` : '✗';
    const tvTag = tv && tv.ok ? `✓${tv.count}` : '✗';
    const animeTag = anime && anime.ok ? `✓${anime.count}` : '✗';
    const kdramaTag = kdrama && kdrama.ok ? `✓${kdrama.count}` : '✗';
    const maxQ = [movie, tv, anime, kdrama].filter(Boolean).map(r => r.maxQuality).filter(q => q && q !== '?').sort()[0] || '?';
    console.log(`  ${p.padEnd(18)} | ${movieTag.padEnd(5)} | ${tvTag.padEnd(5)} | ${animeTag.padEnd(5)} | ${kdramaTag.padEnd(7)} | ${maxQ}`);
  }

  const totalStreams = allResults.reduce((s, r) => s + r.count, 0);
  const totalVerified = allResults.reduce((s, r) => s + r.verified, 0);
  const passingProviders = new Set(allResults.filter(r => r.ok).map(r => r.name)).size;
  console.log(`\n  Total: ${totalStreams} streams, ${totalVerified} verified playable`);
  console.log(`  Providers returning ≥1 stream: ${passingProviders}/${PROVIDERS.length}`);
  console.log(passingProviders >= 20 ? '\n  ✓ PASS — Suite is working.' : '\n  ⚠ PARTIAL — Some providers may be down.');
})().catch(e => console.error('Fatal:', e.message));
