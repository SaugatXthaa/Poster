/**
 * Quick Smoke Test — Find which Nuvio providers actually work
 * ============================================================
 * Tests ALL 61 providers from All-in-One-Nuvio against:
 *   - Movie: Dune Part Two (TMDB 693134)
 *   - TV: Mandalorian S01E01 (TMDB 82856, season 1, episode 1)
 *
 * Outputs a list of WORKING providers (return ≥ 1 stream).
 */
'use strict';

const fs = require('fs');
const path = require('path');

const PROVIDERS_DIR = '/home/z/my-project/allinone-nuvio-repo/providers';

const TESTS = [
  { label: 'movie', tmdbId: 693134, type: 'movie', season: null, episode: null },
  { label: 'tv',    tmdbId: 82856,  type: 'tv',    season: 1,     episode: 1     },
];

async function testProvider(file, test) {
  const name = path.basename(file, '.js');
  try {
    delete require.cache[require.resolve(file)];
    const mod = require(file);
    if (!mod || typeof mod.getStreams !== 'function') {
      return { name, ok: false, count: 0, error: 'no getStreams export' };
    }
    const result = await Promise.race([
      mod.getStreams(test.tmdbId, test.type, test.season, test.episode),
      new Promise(resolve => setTimeout(() => resolve(null), 20000)),
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
    return { name, ok: false, count: 0, error: e.message.slice(0, 100) };
  }
}

(async () => {
  const files = fs.readdirSync(PROVIDERS_DIR)
    .filter(f => f.endsWith('.js'))
    .map(f => path.join(PROVIDERS_DIR, f));

  console.log(`Testing ${files.length} providers × ${TESTS.length} test cases...\n`);

  const allResults = [];
  for (const test of TESTS) {
    console.log(`\n=== ${test.label.toUpperCase()}: TMDB ${test.tmdbId} ===`);
    const batchSize = 8;
    for (let i = 0; i < files.length; i += batchSize) {
      const batch = files.slice(i, i + batchSize);
      const results = await Promise.all(batch.map(f => testProvider(f, test)));
      for (const r of results) {
        const tag = r.ok ? '✓' : '✗';
        const sample = r.sample ? ` [${r.sample.quality}] ${r.sample.url}` : '';
        const err = r.error ? ` (${r.error})` : '';
        console.log(`  ${tag} ${r.name.padEnd(22)} ${r.count} stream(s)${sample}${err}`);
        allResults.push({ ...r, test: test.label });
      }
    }
  }

  const working = allResults.filter(r => r.ok);
  const workingUnique = new Set(working.map(r => r.name));
  console.log(`\n${'='.repeat(70)}`);
  console.log(`  SUMMARY: ${workingUnique.size}/${files.length} providers returned >=1 stream`);
  console.log(`${'='.repeat(70)}`);
  console.log(`\nWorking providers (use these):`);
  for (const name of [...workingUnique].sort()) {
    const movieOk = working.some(r => r.name === name && r.test === 'movie');
    const tvOk = working.some(r => r.name === name && r.test === 'tv');
    const tags = [];
    if (movieOk) tags.push('movie');
    if (tvOk) tags.push('tv');
    console.log(`  + ${name.padEnd(22)} [${tags.join('+')}]`);
  }
})();
