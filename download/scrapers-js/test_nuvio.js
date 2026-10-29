/**
 * Nuvio Provider Test Runner
 * Tests all anime providers from the Anime-Nuvio repo.
 * Usage: node test_nuvio.js
 */
'use strict';

const providers = [
  { name: 'AniKotoTV', file: './nuvio_anikototv.js' },
  { name: 'AnimesDigital', file: './nuvio_animesdigital.js' },
  { name: 'AllAnime', file: './nuvio_allanime.js' },
  { name: 'AnimeSalt', file: './nuvio_animesalt.js' },
  { name: 'AnimeWorld', file: './nuvio_animeworld.js' },
  { name: 'AllWish', file: './nuvio_allwish.js' },
  { name: 'Anime-Sama', file: './nuvio_anime-sama.js' },
  { name: 'AnimeKai', file: './nuvio_animekai.js' },
  { name: 'AnimePahe', file: './nuvio_animepahe.js' },
];

// Test with Jujutsu Kaisen (TMDB 95479) S01E01
const TMDB_ID = 95479;
const TYPE = 'tv';
const SEASON = 1;
const EPISODE = 1;

(async () => {
  console.log(`Testing all Nuvio providers with TMDB ${TMDB_ID} (Jujutsu Kaisen) S${SEASON}E${EPISODE}\n`);
  
  const results = [];
  
  for (const p of providers) {
    try {
      const mod = require(p.file);
      const streams = await mod.getStreams(TMDB_ID, TYPE, SEASON, EPISODE);
      const count = streams.length;
      console.log(`  ${count > 0 ? '✓' : '✗'} ${p.name}: ${count} stream(s)`);
      if (count > 0) {
        for (const s of streams.slice(0, 2)) {
          console.log(`    [${s.quality || '?'}] ${s.url?.slice(0, 80) || 'no url'}`);
        }
      }
      results.push({ name: p.name, count, streams });
    } catch (e) {
      console.log(`  ✗ ${p.name}: ${e.message.slice(0, 80)}`);
      results.push({ name: p.name, count: 0, error: e.message });
    }
  }
  
  // Summary
  console.log('\n=== SUMMARY ===');
  const working = results.filter(r => r.count > 0);
  const broken = results.filter(r => r.count === 0);
  console.log(`Working: ${working.length}/${results.length}`);
  for (const w of working) console.log(`  ✓ ${w.name}: ${w.count} streams`);
  console.log(`Broken: ${broken.length}/${results.length}`);
  for (const b of broken) console.log(`  ✗ ${b.name}: ${b.error || '0 streams'}`);
})();
