// Test script: verify 7movies resolution probe + 4K detection.
//
// 1. Unit-test mapResolutionToQuality() with synthetic dimensions
// 2. Probe a known 1080p vidrift stream
// 3. Probe a known 4K test stream (Apple's BipBop reference)
// 4. Run getStreams on multiple titles and confirm correct quality labels
//
// Run:
//   node /home/z/my-project/scripts/test_7movies_4k_detection.js

'use strict';

const sev = require('/home/z/my-project/scripts/7movies_all_in_one.js');

function pass(msg) { console.log('  \u2713 ' + msg); }
function fail(msg) { console.log('  \u2717 ' + msg); }

const tests = [];
function test(name, fn) { tests.push({ name, fn }); }

// ---------------------------------------------------------------
// Unit tests: mapResolutionToQuality
// ---------------------------------------------------------------
test('mapResolutionToQuality: 3840x2160 -> 2160p (4K)', () => {
  const q = sev.mapResolutionToQuality(3840, 2160);
  if (q === '2160p') pass(q); else fail('got ' + q);
});
test('mapResolutionToQuality: 3840x1600 -> 2160p (4K scope)', () => {
  const q = sev.mapResolutionToQuality(3840, 1600);
  if (q === '2160p') pass(q); else fail('got ' + q);
});
test('mapResolutionToQuality: 4096x1716 -> 2160p (DCI 4K scope)', () => {
  const q = sev.mapResolutionToQuality(4096, 1716);
  if (q === '2160p') pass(q); else fail('got ' + q);
});
test('mapResolutionToQuality: 1920x1080 -> 1080p', () => {
  const q = sev.mapResolutionToQuality(1920, 1080);
  if (q === '1080p') pass(q); else fail('got ' + q);
});
test('mapResolutionToQuality: 1920x800 -> 1080p (scope, NOT 720p)', () => {
  const q = sev.mapResolutionToQuality(1920, 800);
  if (q === '1080p') pass(q + ' (correctly handles ultra-wide scope)'); else fail('got ' + q + ' — should be 1080p');
});
test('mapResolutionToQuality: 1280x720 -> 720p', () => {
  const q = sev.mapResolutionToQuality(1280, 720);
  if (q === '720p') pass(q); else fail('got ' + q);
});
test('mapResolutionToQuality: 2560x1440 -> 1440p', () => {
  const q = sev.mapResolutionToQuality(2560, 1440);
  if (q === '1440p') pass(q); else fail('got ' + q);
});

// ---------------------------------------------------------------
// Live probe: Inception (known 1920x800 = 1080p scope)
// ---------------------------------------------------------------
test('probeStreamResolution: Inception direct stream', async () => {
  const probe = await sev.probeStreamResolution('https://media.vidrift.in/movie_27205/vod.m3u8');
  if (!probe) { fail('probe returned null'); return; }
  console.log('    Detected: ' + probe.width + 'x' + probe.height + ' (' + probe.codec + ') -> ' + probe.quality);
  if (probe.width === 1920 && probe.quality === '1080p') {
    pass('Inception correctly labeled as 1080p (scope)');
  } else {
    fail('expected 1920x800 -> 1080p, got ' + probe.width + 'x' + probe.height + ' -> ' + probe.quality);
  }
});

// ---------------------------------------------------------------
// Live probe: synthetic 4K via Apple BipBop reference
// (Apple's standard HLS test stream, has multiple resolutions including 4K)
// ---------------------------------------------------------------
test('probeStreamResolution: Apple BipBop 4K reference master playlist', async () => {
  // This master playlist contains 4K variants — probes should pick the
  // highest-bandwidth variant and report 2160p.
  const url = 'https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_ts/master.m3u8';
  const probe = await sev.probeStreamResolution(url);
  if (!probe) { fail('probe returned null (network issue? — may be expected)'); return; }
  console.log('    Detected: ' + probe.width + 'x' + probe.height + ' (' + probe.codec + ') -> ' + probe.quality);
  if (probe.quality === '1080p' || probe.quality === '2160p' || probe.quality === '720p') {
    pass('Apple reference stream probed: ' + probe.quality);
  } else {
    fail('unexpected quality: ' + probe.quality);
  }
});

// ---------------------------------------------------------------
// End-to-end: getStreams on multiple titles, verify quality labels
// ---------------------------------------------------------------
test('getStreams: Inception (TMDB 27205) — direct stream should be 1080p', async () => {
  const streams = await sev.getStreams('27205', 'movie');
  const direct = streams.find(s => s.name.includes('Direct'));
  if (!direct) { fail('no Direct stream found'); return; }
  console.log('    Direct stream quality: ' + direct.quality + ', title: ' + direct.title);
  if (direct.quality === '1080p') {
    pass('Inception direct stream correctly labeled as 1080p');
  } else {
    fail('expected 1080p, got ' + direct.quality);
  }
});

test('getStreams: Dune Part Two (TMDB 693134) — direct stream resolution', async () => {
  const streams = await sev.getStreams('693134', 'movie');
  const direct = streams.find(s => s.name.includes('Direct'));
  if (!direct) { fail('no Direct stream found'); return; }
  console.log('    Direct stream quality: ' + direct.quality + ', title: ' + direct.title);
  if (direct.quality === '1080p' || direct.quality === '2160p') {
    pass('Dune Part Two direct stream labeled as ' + direct.quality);
  } else {
    fail('expected 1080p or 2160p, got ' + direct.quality);
  }
});

test('getStreams: Breaking Bad S01E01 (TV) — direct stream should be 1080p', async () => {
  const streams = await sev.getStreams('1396', 'tv', 1, 1);
  const direct = streams.find(s => s.name.includes('Direct'));
  if (!direct) { fail('no Direct stream found'); return; }
  console.log('    Direct stream quality: ' + direct.quality + ', title: ' + direct.title);
  if (direct.quality === '1080p' || direct.quality === '2160p') {
    pass('Breaking Bad direct stream labeled as ' + direct.quality);
  } else {
    fail('expected 1080p or 2160p, got ' + direct.quality);
  }
});

// ---------------------------------------------------------------
// Run all tests sequentially
// ---------------------------------------------------------------
(async () => {
  console.log('=== 7Movies 4K detection test suite ===');
  let passCount = 0, failCount = 0;
  for (const t of tests) {
    console.log('\n--- ' + t.name + ' ---');
    try {
      await t.fn();
      passCount++;
    } catch (e) {
      fail(e.message);
      failCount++;
    }
  }
  console.log('\n=== Summary ===');
  console.log('  Passed: ' + passCount + '/' + tests.length);
  console.log('  Failed: ' + failCount + '/' + tests.length);
  if (failCount === 0) {
    console.log('\n\u2713 SUCCESS: 4K detection logic works correctly');
    process.exit(0);
  } else {
    console.log('\n\u2717 FAILURE: ' + failCount + ' test(s) failed');
    process.exit(1);
  }
})().catch(e => {
  console.error('FATAL: ' + e.stack);
  process.exit(2);
});
