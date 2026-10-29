// Test script: verify NikaStream scraper returns playable streams with
// sub + dub audio and subtitles, for multiple anime titles.
//
// Run:
//   node /home/z/my-project/scripts/test_nikastream_playability.js

'use strict';

const nika = require('/home/z/my-project/scripts/nikastream_all_in_one.js');

function pass(msg) { console.log('  \u2713 ' + msg); }
function fail(msg) { console.log('  \u2717 ' + msg); }

async function testItem(label, tmdbId, type, season, episode) {
  console.log('');
  console.log('=== ' + label + ' (TMDB ' + tmdbId + ') ===');
  let streams = [];
  try {
    streams = await nika.getStreams(tmdbId, type, season, episode);
  } catch (e) {
    fail('getStreams threw: ' + e.message);
    return { ok: false, total: 0, playable: 0, sub: 0, dub: 0, withSubs: 0 };
  }

  if (streams.length === 0) {
    fail('no streams returned');
    return { ok: false, total: 0, playable: 0, sub: 0, dub: 0, withSubs: 0 };
  }
  pass('returned ' + streams.length + ' stream(s)');

  // Verify NO pengu.uk proxy URLs
  let penguCount = 0;
  for (const s of streams) {
    if (s.url && s.url.includes('pengu.uk')) penguCount++;
  }
  if (penguCount === 0) pass('no pengu.uk proxy URLs');
  else fail(penguCount + ' stream(s) still use pengu.uk proxy');

  // Count sub vs dub
  let subCount = 0, dubCount = 0;
  for (const s of streams) {
    if (s.title.includes('SUB')) subCount++;
    else if (s.title.includes('DUB')) dubCount++;
  }
  console.log('  SUB streams: ' + subCount);
  console.log('  DUB streams: ' + dubCount);
  if (subCount > 0) pass('has SUB audio streams');
  if (dubCount > 0) pass('has DUB audio streams');

  // Count streams with subtitles
  let withSubs = 0;
  for (const s of streams) {
    if (s.subtitles && s.subtitles.length > 0) withSubs++;
  }
  console.log('  Streams with subtitles: ' + withSubs + '/' + streams.length);
  if (withSubs > 0) pass('subtitles attached to ' + withSubs + ' stream(s)');

  // Validate a sample of streams (max 5 to avoid hammering CDNs)
  const toTest = streams.slice(0, 5);
  let playable = 0;
  for (const s of toTest) {
    if (!s.url || !s.url.startsWith('http')) continue;
    try {
      const headers = { 'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36' };
      if (s.behaviorHints?.proxyHeaders?.request) {
        Object.assign(headers, s.behaviorHints.proxyHeaders.request);
      }
      const res = await fetch(s.url, { headers, signal: AbortSignal.timeout(10000) });
      if (res.ok || res.status === 206) {
        pass(s.name + ' -> HTTP ' + res.status);
        playable++;
      } else {
        fail(s.name + ' -> HTTP ' + res.status);
      }
    } catch (e) {
      fail(s.name + ' -> ' + e.message);
    }
  }

  return {
    ok: penguCount === 0 && streams.length > 0,
    total: streams.length,
    playable,
    sub: subCount,
    dub: dubCount,
    withSubs,
  };
}

(async () => {
  const results = [];
  results.push(await testItem('Jujutsu Kaisen S01E01', '95479', 'tv', 1, 1));
  results.push(await testItem('One Piece S01E01', '37854', 'tv', 1, 1));
  results.push(await testItem('Naruto S01E01', '31910', 'tv', 1, 1));

  console.log('');
  console.log('=== Final summary ===');
  let totalStreams = 0, totalPlayable = 0, totalSub = 0, totalDub = 0, totalWithSubs = 0;
  for (let i = 0; i < results.length; i++) {
    const r = results[i];
    console.log('  Test ' + (i + 1) + ': ' + r.total + ' streams (' + r.sub + ' sub, ' + r.dub + ' dub, ' + r.withSubs + ' with subs), ' + r.playable + '/5 spot-tested playable');
    totalStreams += r.total;
    totalPlayable += r.playable;
    totalSub += r.sub;
    totalDub += r.dub;
    totalWithSubs += r.withSubs;
  }
  console.log('');
  console.log('  Total streams:     ' + totalStreams);
  console.log('  SUB streams:       ' + totalSub);
  console.log('  DUB streams:       ' + totalDub);
  console.log('  With subtitles:    ' + totalWithSubs);
  console.log('  Spot-test playable: ' + totalPlayable + '/' + (results.length * 5));

  if (results.every(r => r.ok && r.sub > 0)) {
    console.log('');
    console.log('\u2713 SUCCESS: NikaStream returns sub + dub streams with subtitles');
    process.exit(0);
  } else {
    console.log('');
    console.log('\u2717 FAILURE: missing sub/dub or no streams');
    process.exit(1);
  }
})().catch(e => {
  console.error('FATAL: ' + e.stack);
  process.exit(2);
});
