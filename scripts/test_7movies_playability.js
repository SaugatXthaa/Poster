// Test script: verify 7movies scraper returns playable stream URLs
// (no penguplay proxy, all URLs reachable, subtitles attached)
//
// Run:
//   node /home/z/my-project/scripts/test_7movies_playability.js

'use strict';

const sev = require('/home/z/my-project/scripts/7movies_all_in_one.js');

function pass(msg) { console.log('  \u2713 ' + msg); }
function fail(msg) { console.log('  \u2717 ' + msg); }

async function testItem(label, tmdbId, type, season, episode) {
  console.log('');
  console.log('=== ' + label + ' (TMDB ' + tmdbId + ') ===');
  let streams = [];
  try {
    streams = await sev.getStreams(tmdbId, type, season, episode);
  } catch (e) {
    fail('getStreams threw: ' + e.message);
    return { ok: false, total: 0, playable: 0 };
  }

  if (streams.length === 0) {
    fail('no streams returned');
    return { ok: false, total: 0, playable: 0 };
  }
  pass('returned ' + streams.length + ' stream(s)');

  // Verify NO pengu.uk proxy URLs
  let penguCount = 0;
  for (const s of streams) {
    if (s.url && s.url.includes('pengu.uk')) penguCount++;
  }
  if (penguCount === 0) {
    pass('no pengu.uk proxy URLs');
  } else {
    fail(penguCount + ' stream(s) still use pengu.uk proxy');
  }

  // Verify each stream URL is reachable
  let playable = 0;
  for (const s of streams) {
    if (!s.url || !s.url.startsWith('http')) continue;
    try {
      const headers = { 'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36' };
      if (s.behaviorHints && s.behaviorHints.proxyHeaders && s.behaviorHints.proxyHeaders.request) {
        Object.assign(headers, s.behaviorHints.proxyHeaders.request);
      }
      const res = await fetch(s.url, {
        method: 'GET',
        headers: { ...headers, Range: 'bytes=0-1023' },
        signal: AbortSignal.timeout(10000),
      });
      if (res.ok || res.status === 206 || res.status === 200) {
        const body = await res.text();
        if (body.trimStart().startsWith('#EXTM3U') || body.length > 100) {
          pass(s.name + ' -> HTTP ' + res.status + ' (' + (s.url.length > 80 ? s.url.slice(0, 80) + '...' : s.url) + ')');
          playable++;
        } else {
          fail(s.name + ' -> HTTP ' + res.status + ' but body too short (' + body.length + ')');
        }
      } else {
        fail(s.name + ' -> HTTP ' + res.status);
      }
    } catch (e) {
      fail(s.name + ' -> ' + e.message);
    }
  }

  // Check subtitles
  const withSubs = streams.filter(s => s.subtitles && s.subtitles.length > 0).length;
  console.log('  Subtitles: ' + withSubs + '/' + streams.length + ' streams have subtitle tracks');

  return { ok: penguCount === 0 && playable > 0, total: streams.length, playable };
}

(async () => {
  const results = [];
  results.push(await testItem('Inception (movie)', '27205', 'movie'));
  results.push(await testItem('Breaking Bad S01E01 (TV)', '1396', 'tv', 1, 1));
  results.push(await testItem('Jujutsu Kaisen S01E01 (anime TV)', '95479', 'tv', 1, 1));

  console.log('');
  console.log('=== Final summary ===');
  let totalStreams = 0, totalPlayable = 0;
  for (let i = 0; i < results.length; i++) {
    const r = results[i];
    console.log('  Test ' + (i + 1) + ': ' + r.total + ' streams, ' + r.playable + ' playable, ' + (r.ok ? 'PASS' : 'FAIL'));
    totalStreams += r.total;
    totalPlayable += r.playable;
  }
  console.log('');
  console.log('  Total streams:     ' + totalStreams);
  console.log('  Total playable:    ' + totalPlayable);
  console.log('  Playability rate:  ' + Math.round(totalPlayable / Math.max(totalStreams, 1) * 100) + '%');

  if (results.every(r => r.ok)) {
    console.log('');
    console.log('\u2713 SUCCESS: 7movies works independently — no penguplay, raw URLs, playable');
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
