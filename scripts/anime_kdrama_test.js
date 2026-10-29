/**
 * Anime & K-Drama Test — Verify working providers cover anime + dramas
 */
'use strict';

const fs = require('fs');
const path = require('path');

const PROVIDERS_DIR = '/home/z/my-project/allinone-nuvio-repo/providers';

// Test anime + K-drama
const TESTS = [
  { label: 'anime', tmdbId: 95479, type: 'tv', season: 1, episode: 1 },   // Jujutsu Kaisen
  { label: 'kdrama', tmdbId: 86639, type: 'tv', season: 1, episode: 1 },  // Crash Landing on You
];

// Only test the providers that worked in movie/tv test
const WORKING_PROVIDERS = [
  '4khdhub', 'animezey', 'castle', 'cineby', 'ctgmovies',
  'dahmermovies', 'dahmermovies-4k', 'desiflix', 'goated', 'hdghartv',
  'hdhub4u', 'hindmoviez', 'movieblast', 'movies4u', 'movieshunt',
  'movix', 'netmirror', 'persianstremio', 'playimdb', 'purstream',
  'torrentio', 'uhdmovies', 'videasy', 'vidfast', 'vidlove', 'vixsrc', 'zinkmovies',
];

async function testProvider(file, test) {
  const name = path.basename(file, '.js');
  try {
    delete require.cache[require.resolve(file)];
    const mod = require(file);
    if (!mod || typeof mod.getStreams !== 'function') {
      return { name, ok: false, count: 0, error: 'no getStreams' };
    }
    const result = await Promise.race([
      mod.getStreams(test.tmdbId, test.type, test.season, test.episode),
      new Promise(resolve => setTimeout(() => resolve(null), 25000)),
    ]);
    if (!result) return { name, ok: false, count: 0, error: 'timeout' };
    if (!Array.isArray(result)) return { name, ok: false, count: 0, error: 'non-array' };
    return {
      name,
      ok: result.length > 0,
      count: result.length,
      sample: result[0] ? {
        url: result[0].url ? String(result[0].url).slice(0, 80) : '(no url)',
        quality: result[0].quality || '?',
      } : null,
    };
  } catch (e) {
    return { name, ok: false, count: 0, error: e.message.slice(0, 80) };
  }
}

(async () => {
  const allResults = [];
  for (const test of TESTS) {
    console.log(`\n=== ${test.label.toUpperCase()}: TMDB ${test.tmdbId} ===`);
    for (const name of WORKING_PROVIDERS) {
      const file = path.join(PROVIDERS_DIR, name + '.js');
      const r = await testProvider(file, test);
      const tag = r.ok ? 'OK' : '..';
      const sample = r.sample ? ` [${r.sample.quality}] ${r.sample.url.slice(0, 60)}` : '';
      const err = r.error ? ` (${r.error})` : '';
      console.log(`  [${tag}] ${r.name.padEnd(22)} ${r.count}${sample}${err}`);
      allResults.push({ ...r, test: test.label });
    }
  }

  const working = allResults.filter(r => r.ok);
  const workingUnique = new Set(working.map(r => r.name));
  console.log(`\n${'='.repeat(70)}`);
  console.log(`  ANIME/KDRAMA WORKING: ${workingUnique.size}/${WORKING_PROVIDERS.length}`);
  console.log(`${'='.repeat(70)}`);
  for (const name of [...workingUnique].sort()) {
    const animeOk = working.some(r => r.name === name && r.test === 'anime');
    const kdramaOk = working.some(r => r.name === name && r.test === 'kdrama');
    const tags = [];
    if (animeOk) tags.push('anime');
    if (kdramaOk) tags.push('kdrama');
    console.log(`  + ${name.padEnd(22)} [${tags.join('+')}]`);
  }
})();
