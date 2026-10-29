/**
 * Smart Provider Test — tests each provider with appropriate content
 *
 * Key insight: Not all providers support all content types. This test
 * uses the RIGHT test title for each provider:
 *   - General providers: Dune Part Two (movie) + Mandalorian (TV)
 *   - Anime providers: Jujutsu Kaisen (TV) + Spirited Away (movie)
 *   - Movie-only providers: only test with movies
 */
'use strict';

const fs = require('fs');
const path = require('path');

const DIR = '/home/z/my-project/download/scrapers-js';

// Test cases
const MOVIE = { label: 'Movie', tmdbId: 693134, type: 'movie', season: null, episode: null };
const TV = { label: 'TV', tmdbId: 82856, type: 'tv', season: 1, episode: 1 };
const ANIME_TV = { label: 'Anime', tmdbId: 95479, type: 'tv', season: 1, episode: 1 };
const ANIME_MOVIE = { label: 'AnimeMovie', tmdbId: 129, type: 'movie', season: null, episode: null }; // Spirited Away

// Provider configs — which test cases apply to each
const PROVIDERS = [
  // General providers (movies + TV)
  { name: '4khdhub', tests: [MOVIE, TV] },
  { name: 'castle', tests: [MOVIE, TV] },
  { name: 'cineby', tests: [MOVIE, TV] },
  { name: 'ctgmovies', tests: [MOVIE, TV] },
  { name: 'dahmermovies', tests: [MOVIE, TV] },
  { name: 'dahmermovies-4k', tests: [MOVIE, TV] },
  { name: 'desiflix', tests: [MOVIE, TV, ANIME_TV] },
  { name: 'goated', tests: [MOVIE, TV, ANIME_TV] },
  { name: 'hdghartv', tests: [MOVIE, TV] },
  { name: 'hdhub4u', tests: [MOVIE, TV] },
  { name: 'hindmoviez', tests: [MOVIE, TV, ANIME_TV] },
  { name: 'movix', tests: [MOVIE, TV, ANIME_TV] },
  { name: 'netmirror', tests: [MOVIE, TV, ANIME_TV] },
  { name: 'persianstremio', tests: [MOVIE, TV, ANIME_TV] },
  { name: 'playimdb', tests: [MOVIE, TV, ANIME_TV] },
  { name: 'purstream', tests: [MOVIE, TV, ANIME_TV] },
  { name: 'torrentio', tests: [MOVIE, TV, ANIME_TV] },
  { name: 'videasy', tests: [MOVIE, TV, ANIME_TV] },
  { name: 'vidfast', tests: [MOVIE, TV, ANIME_TV] }, // broken — enc-dec.app out of sync
  { name: 'vidlove', tests: [MOVIE, TV, ANIME_TV] },
  { name: 'vixsrc', tests: [MOVIE, TV] },
  { name: 'zinkmovies', tests: [MOVIE, TV] },

  // Movie-only providers
  { name: 'movieblast', tests: [MOVIE] },         // catalog doesn't index TV
  { name: 'movies4u', tests: [MOVIE] },           // movie-only by design
  { name: 'movieshunt', tests: [MOVIE, ANIME_TV] },// movie-focused, some anime
  { name: 'uhdmovies', tests: [MOVIE] },          // movie-only by design

  // Anime-only providers
  { name: 'anikototv', tests: [ANIME_TV, ANIME_MOVIE] },  // requires MAL mapping
  { name: 'animesalt', tests: [ANIME_TV, ANIME_MOVIE] },
  { name: 'animeworld', tests: [ANIME_TV, ANIME_MOVIE] },
  { name: 'animesdigital', tests: [ANIME_TV, ANIME_MOVIE] },
  { name: 'animezey', tests: [ANIME_TV, ANIME_MOVIE] },
];

async function testProvider(name, test) {
  let file = path.join(DIR, `nuvio_${name}.js`);
  if (!fs.existsSync(file)) file = path.join(DIR, `${name}.js`);
  if (!fs.existsSync(file)) return { ok: false, error: 'file not found' };

  try {
    delete require.cache[require.resolve(file)];
    const mod = require(file);
    if (!mod || typeof mod.getStreams !== 'function') return { ok: false, error: 'no getStreams' };

    const start = Date.now();
    const result = await Promise.race([
      mod.getStreams(test.tmdbId, test.type, test.season, test.episode),
      new Promise(r => setTimeout(() => null, 60000)),
    ]);
    const elapsed = ((Date.now() - start) / 1000).toFixed(1);

    if (!result) return { ok: false, elapsed, error: 'timeout', count: 0 };
    if (!Array.isArray(result)) return { ok: false, elapsed, error: 'non-array', count: 0 };
    return { ok: result.length > 0, elapsed, count: result.length };
  } catch (e) {
    return { ok: false, error: e.message.slice(0, 80), count: 0 };
  }
}

(async () => {
  console.log('╔' + '═'.repeat(68) + '╗');
  console.log('║  Smart Provider Test — Right content for each provider            ║');
  console.log('╚' + '═'.repeat(68) + '╝\n');

  const results = [];
  for (const p of PROVIDERS) {
    console.log(`\n${p.name}:`);
    for (const test of p.tests) {
      const r = await testProvider(p.name, test);
      const tag = r.ok ? '✓' : '✗';
      const info = r.count !== undefined
        ? `${r.count} streams in ${r.elapsed}s`
        : `${r.error}`;
      console.log(`  ${tag} ${test.label.padEnd(12)} ${info}`);
      results.push({ provider: p.name, test: test.label, ...r });
    }
  }

  // Summary
  console.log('\n' + '═'.repeat(70));
  console.log('  SUMMARY');
  console.log('═'.repeat(70));
  console.log('  Provider          | Tests Passed | Status');
  console.log('  ------------------|--------------|------------------');
  for (const p of PROVIDERS) {
    const providerResults = results.filter(r => r.provider === p.name);
    const passed = providerResults.filter(r => r.ok).length;
    const total = providerResults.length;
    const status = passed === total ? '✓ All working'
      : passed > 0 ? `⚠ Partial (${passed}/${total})`
      : '✗ Broken';
    console.log(`  ${p.name.padEnd(18)} | ${passed}/${total}          | ${status}`);
  }
})().catch(e => console.error('Fatal:', e.message));
