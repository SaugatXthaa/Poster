// Test runner for TENIES.SITE source bundle
// Run: node test_sources.js

'use strict';

const path = require('path');
const { getStreams, CORE_SCRAPERS, ALL_SCRAPERS } = require('./addon');

const TESTS = [
  { tmdb: '27205',  type: 'movie', label: 'Inception (movie)' },
  { tmdb: '693134', type: 'movie', label: 'Dune Part Two (movie, 4K)' },
  { tmdb: '634649', type: 'movie', label: 'Spider-Man NWH (movie)' },
  { tmdb: '76600',  type: 'movie', label: 'Avatar 2022 (movie)' },
  { tmdb: '1396',   type: 'tv', season: 1, episode: 1, label: 'Breaking Bad S01E01' },
  { tmdb: '82856',  type: 'tv', season: 1, episode: 1, label: 'Mandalorian S01E01 (4K)' },
  { tmdb: '95479',  type: 'tv', season: 1, episode: 1, label: 'Jujutsu Kaisen S01E01 (anime)' },
];

async function main() {
  console.log('='.repeat(70));
  console.log('  TENIES.SITE Source Bundle — Test Runner');
  console.log('='.repeat(70));
  console.log(`\nBundle: ${ALL_SCRAPERS.length} scrapers (${CORE_SCRAPERS.length} core + ${ALL_SCRAPERS.length - CORE_SCRAPERS.length} extended)\n`);

  let totalStreams = 0;
  const byScraper = {};

  for (const test of TESTS) {
    console.log(`\n--- ${test.label} (TMDB ${test.tmdb}) ---`);
    const streams = await getStreams(test.tmdb, test.type, test.season, test.episode, {
      timeoutMs: 25000,
      concurrency: 8,
    });

    console.log(`  Total: ${streams.length} streams`);
    totalStreams += streams.length;

    for (const s of streams) {
      const src = s.source || 'unknown';
      byScraper[src] = (byScraper[src] || 0) + 1;
    }

    // Show quality breakdown
    const qual = {};
    for (const s of streams) {
      const q = s.quality || '?';
      qual[q] = (qual[q] || 0) + 1;
    }
    console.log('  By quality:', Object.entries(qual).sort((a, b) => b[1] - a[1]).map(([q, c]) => `${q}=${c}`).join(', '));
  }

  console.log('\n' + '='.repeat(70));
  console.log('  SUMMARY');
  console.log('='.repeat(70));
  console.log(`Total streams across ${TESTS.length} titles: ${totalStreams}`);
  console.log('\nStreams per scraper:');
  Object.entries(byScraper)
    .sort((a, b) => b[1] - a[1])
    .forEach(([name, count]) => {
      const bar = '█'.repeat(Math.min(40, Math.ceil(count / 5)));
      console.log(`  ${name.padEnd(28)} ${String(count).padStart(4)}  ${bar}`);
    });
}

main().catch((e) => {
  console.error('FATAL:', e);
  process.exit(1);
});
