// Test script: verify Stellar scraper returns playable streams for all servers.
// Tests embed URL reachability across multiple movies and TV shows.
//
// Run:
//   node /home/z/my-project/scripts/test_stellar_playability.js

'use strict';

const stellar = require('/home/z/my-project/scripts/stellar_all_in_one.js');

function pass(msg) { console.log('  \u2713 ' + msg); }
function fail(msg) { console.log('  \u2717 ' + msg); }

async function testItem(label, tmdbId, type, season, episode) {
  console.log('');
  console.log('=== ' + label + ' (TMDB ' + tmdbId + ') ===');
  let streams = [];
  try {
    streams = await stellar.getStreams(tmdbId, type, season, episode);
  } catch (e) {
    fail('getStreams threw: ' + e.message);
    return { ok: false, total: 0, reachable: 0 };
  }

  if (streams.length === 0) {
    fail('no streams returned');
    return { ok: false, total: 0, reachable: 0 };
  }
  pass('returned ' + streams.length + ' stream(s)');

  // Verify NO pengu.uk proxy URLs
  let penguCount = 0;
  for (const s of streams) {
    if (s.url && s.url.includes('pengu.uk')) penguCount++;
  }
  if (penguCount === 0) pass('no pengu.uk proxy URLs');
  else fail(penguCount + ' stream(s) still use pengu.uk proxy');

  // Validate each iframe URL is reachable
  let reachable = 0;
  for (const s of streams) {
    if (!s.url || !s.url.startsWith('http')) continue;
    try {
      const res = await fetch(s.url, {
        headers: { 'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36' },
        signal: AbortSignal.timeout(10000),
      });
      if (res.ok) {
        pass(s.name + ' -> HTTP ' + res.status);
        reachable++;
      } else {
        fail(s.name + ' -> HTTP ' + res.status);
      }
    } catch (e) {
      fail(s.name + ' -> ' + e.message);
    }
  }

  return { ok: penguCount === 0 && reachable > 0, total: streams.length, reachable };
}

(async () => {
  const results = [];
  results.push(await testItem('Inception', '27205', 'movie'));
  results.push(await testItem('Dune Part Two', '693134', 'movie'));
  results.push(await testItem('Oppenheimer', '872585', 'movie'));
  results.push(await testItem('Breaking Bad S01E01', '1396', 'tv', 1, 1));

  console.log('');
  console.log('=== Final summary ===');
  let totalStreams = 0, totalReachable = 0;
  for (let i = 0; i < results.length; i++) {
    const r = results[i];
    console.log('  Test ' + (i + 1) + ': ' + r.total + ' streams, ' + r.reachable + '/' + r.total + ' reachable');
    totalStreams += r.total;
    totalReachable += r.reachable;
  }
  console.log('');
  console.log('  Total streams:    ' + totalStreams);
  console.log('  Reachable:        ' + totalReachable + '/' + totalStreams);

  if (results.every(r => r.ok)) {
    console.log('');
    console.log('\u2713 SUCCESS: All Stellar streams are reachable');
    process.exit(0);
  } else {
    console.log('');
    console.log('\u2717 FAILURE: some streams not reachable');
    process.exit(1);
  }
})().catch(e => {
  console.error('FATAL: ' + e.stack);
  process.exit(2);
});
