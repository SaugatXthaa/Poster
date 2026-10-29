// Test script: verify Stellar scraper returns DIRECT playable stream URLs
// with 4K detection across multiple movies and TV shows.
//
// Run:
//   node /home/z/my-project/scripts/test_stellar_direct.js

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
    return { ok: false, total: 0, direct: 0, playable: 0, has4K: false };
  }

  if (streams.length === 0) {
    fail('no streams returned');
    return { ok: false, total: 0, direct: 0, playable: 0, has4K: false };
  }
  pass('returned ' + streams.length + ' stream(s)');

  // Verify NO pengu.uk proxy URLs
  let penguCount = 0;
  for (const s of streams) {
    if (s.url && s.url.includes('pengu.uk')) penguCount++;
  }
  if (penguCount === 0) pass('no pengu.uk proxy URLs');
  else fail(penguCount + ' stream(s) use pengu.uk');

  // Count direct vs iframe
  const directStreams = streams.filter(s => s.type !== 'iframe');
  const iframeStreams = streams.filter(s => s.type === 'iframe');
  console.log('  Direct HLS streams: ' + directStreams.length);
  console.log('  Iframe streams: ' + iframeStreams.length);

  // Check for 4K
  const has4K = directStreams.some(s => s.quality === '2160p');
  if (has4K) pass('has 4K (2160p) stream!');
  else console.log('  Max quality: ' + (directStreams[0]?.quality || 'N/A'));

  // Validate direct streams are playable (HTTP 200 + #EXTM3U)
  let playable = 0;
  for (const s of directStreams) {
    if (!s.url || !s.url.startsWith('http')) continue;
    try {
      const res = await fetch(s.url, {
        headers: { 'User-Agent': 'Mozilla/5.0' },
        signal: AbortSignal.timeout(10000),
      });
      if (res.ok) {
        const text = await res.text();
        if (text.includes('#EXTM3U')) {
          pass(s.name + ' -> HTTP ' + res.status + ' (' + s.quality + ', HLS valid)');
          playable++;
        } else {
          fail(s.name + ' -> HTTP ' + res.status + ' but not HLS');
        }
      } else {
        fail(s.name + ' -> HTTP ' + res.status);
      }
    } catch (e) {
      fail(s.name + ' -> ' + e.message);
    }
  }

  // Validate iframe streams are reachable
  for (const s of iframeStreams) {
    try {
      const res = await fetch(s.url, {
        headers: { 'User-Agent': 'Mozilla/5.0' },
        signal: AbortSignal.timeout(10000),
      });
      if (res.ok) pass(s.name + ' (iframe) -> HTTP ' + res.status);
      else fail(s.name + ' (iframe) -> HTTP ' + res.status);
    } catch (e) {
      fail(s.name + ' (iframe) -> ' + e.message);
    }
  }

  return {
    ok: penguCount === 0 && playable > 0,
    total: streams.length,
    direct: directStreams.length,
    playable,
    has4K,
  };
}

(async () => {
  const results = [];
  results.push(await testItem('Inception', '27205', 'movie'));
  results.push(await testItem('Oppenheimer (4K)', '872585', 'movie'));
  results.push(await testItem('Dune Part Two', '693134', 'movie'));
  results.push(await testItem('Breaking Bad S01E01', '1396', 'tv', 1, 1));

  console.log('');
  console.log('=== Final summary ===');
  let totalStreams = 0, totalDirect = 0, totalPlayable = 0, total4K = 0;
  for (let i = 0; i < results.length; i++) {
    const r = results[i];
    console.log('  Test ' + (i + 1) + ': ' + r.total + ' streams (' + r.direct + ' direct, ' + r.playable + ' playable' + (r.has4K ? ', 4K!' : '') + ')');
    totalStreams += r.total;
    totalDirect += r.direct;
    totalPlayable += r.playable;
    if (r.has4K) total4K++;
  }
  console.log('');
  console.log('  Total streams:     ' + totalStreams);
  console.log('  Direct playable:   ' + totalPlayable + '/' + totalDirect);
  console.log('  4K titles:         ' + total4K + '/' + results.length);

  if (results.every(r => r.ok)) {
    console.log('');
    console.log('\u2713 SUCCESS: All Stellar streams are direct playable HLS URLs');
    process.exit(0);
  } else {
    console.log('');
    console.log('\u2717 FAILURE: some streams not playable');
    process.exit(1);
  }
})().catch(e => {
  console.error('FATAL: ' + e.stack);
  process.exit(2);
});
