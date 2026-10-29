/**
 * Per-Provider Diagnostic — find EXACTLY why each provider returns empty
 * Uses 60s timeout (matching Nuvio's actual behavior)
 */
'use strict';

const fs = require('fs');
const path = require('path');

const DIR = '/home/z/my-project/download/scrapers-js';

const PROVIDERS = [
  'vidfast', 'anikototv', 'animesalt', 'animeworld', 'animesdigital',
  'uhdmovies', 'movieblast', 'movies4u', 'animezey',
];

const TESTS = [
  { label: 'Movie (Dune Part Two, TMDB 693134)', tmdbId: 693134, type: 'movie', season: null, episode: null },
  { label: 'TV (Mandalorian S01E01, TMDB 82856)', tmdbId: 82856, type: 'tv', season: 1, episode: 1 },
  { label: 'Anime (Jujutsu Kaisen S01E01, TMDB 95479)', tmdbId: 95479, type: 'tv', season: 1, episode: 1 },
];

async function testProvider(name, test) {
  // Try with nuvio_ prefix first, then plain name
  let file = path.join(DIR, `nuvio_${name}.js`);
  if (!fs.existsSync(file)) file = path.join(DIR, `${name}.js`);
  if (!fs.existsSync(file)) return { name, error: 'file not found' };

  try {
    delete require.cache[require.resolve(file)];
    const mod = require(file);
    if (!mod || typeof mod.getStreams !== 'function') {
      return { name, error: 'no getStreams export', hasExports: Object.keys(mod || {}) };
    }

    const start = Date.now();
    const result = await Promise.race([
      mod.getStreams(test.tmdbId, test.type, test.season, test.episode),
      new Promise(r => setTimeout(() => r('__TIMEOUT__'), 60000)),
    ]);
    const elapsed = ((Date.now() - start) / 1000).toFixed(1);

    if (result === '__TIMEOUT__') return { name, elapsed, result: 'TIMEOUT (60s)' };
    if (!Array.isArray(result)) return { name, elapsed, result: `non-array: ${typeof result}` };
    return {
      name,
      elapsed,
      count: result.length,
      sample: result[0] ? {
        url: String(result[0].url || '').slice(0, 80),
        quality: result[0].quality || '?',
      } : null,
    };
  } catch (e) {
    return { name, error: e.message.slice(0, 120) };
  }
}

(async () => {
  for (const test of TESTS) {
    console.log(`\n${'='.repeat(70)}`);
    console.log(`  ${test.label}`);
    console.log('='.repeat(70));
    for (const p of PROVIDERS) {
      const r = await testProvider(p, test);
      const tag = r.count > 0 ? '✓' : (r.error ? '✗' : '·');
      const info = r.count !== undefined
        ? `${r.count} streams in ${r.elapsed}s${r.sample ? ` [${r.sample.quality}] ${r.sample.url.slice(0, 50)}` : ''}`
        : `${r.error || r.result}`;
      console.log(`  ${tag} ${p.padEnd(20)} ${info}`);
    }
  }
})();
