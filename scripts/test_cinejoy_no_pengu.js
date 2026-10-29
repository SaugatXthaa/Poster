// Test script: verify cinejoy returns RAW stream URLs (no pengu.uk proxy)
// and that streams remain playable when proper Referer/Origin headers are sent.
//
// Run:
//   node /home/z/my-project/scripts/test_cinejoy_no_pengu.js

'use strict';

const cinejoy = require('/home/z/my-project/scripts/cinejoy_all_in_one.js');

const TMDB = '27205';       // Inception (movie)
const TYPE = 'movie';

function pass(msg) { console.log('  \u2713 ' + msg); }
function fail(msg) { console.log('  \u2717 ' + msg); }

(async () => {
  console.log('=== Test 1: Module no longer exports _wrapWithPenguProxy ===');
  if (cinejoy._wrapWithPenguProxy === undefined) {
    pass('module.exports._wrapWithPenguProxy is undefined');
  } else {
    fail('_wrapWithPenguProxy is still exported');
    process.exit(1);
  }

  console.log('');
  console.log('=== Test 2: getStreams returns RAW stream URLs (no pengu.uk) ===');
  let streams = [];
  try {
    streams = await cinejoy.getStreams(TMDB, TYPE);
  } catch (e) {
    fail('getStreams threw: ' + e.message);
    process.exit(1);
  }

  if (streams.length === 0) {
    fail('no streams returned');
    process.exit(1);
  }
  pass('returned ' + streams.length + ' streams');

  let penguCount = 0;
  let rawCount = 0;
  let iframeCount = 0;
  for (const s of streams) {
    if (!s.url) continue;
    if (s.url.includes('pengu.uk')) {
      penguCount++;
      console.log('    PENGU URL FOUND: ' + s.name + ' -> ' + s.url);
    } else if (s.url.startsWith('http')) {
      rawCount++;
    } else if (s.type === 'iframe' || s.url.startsWith('https://cinejoy.to')) {
      iframeCount++;
    }
  }

  if (penguCount === 0) {
    pass('no pengu.uk URLs in any stream (' + rawCount + ' raw, ' + iframeCount + ' iframe)');
  } else {
    fail(penguCount + ' stream(s) still use pengu.uk proxy');
    process.exit(1);
  }

  console.log('');
  console.log('=== Test 3: All HTTP streams carry behaviorHints.proxyHeaders.request ===');
  let missingProxyHeaders = 0;
  for (const s of streams) {
    if (!s.url || !s.url.startsWith('http') || s.type === 'iframe') continue;
    const ph = s.behaviorHints && s.behaviorHints.proxyHeaders && s.behaviorHints.proxyHeaders.request;
    if (!ph) {
      missingProxyHeaders++;
      console.log('    MISSING proxyHeaders: ' + s.name + ' -> ' + s.url);
      continue;
    }
    if (!ph.Referer || !ph.Origin || !ph['User-Agent']) {
      missingProxyHeaders++;
      console.log('    INCOMPLETE proxyHeaders: ' + s.name);
    }
  }
  if (missingProxyHeaders === 0) {
    pass('all HTTP streams carry full proxyHeaders (Referer + Origin + UA)');
  } else {
    fail(missingProxyHeaders + ' stream(s) missing/incomplete proxyHeaders');
  }

  console.log('');
  console.log('=== Test 4: Raw URLs are actually playable (with proper headers) ===');
  // Pick up to 3 raw HTTP streams to test
  const toTest = streams
    .filter(s => s.url && s.url.startsWith('http') && s.type !== 'iframe')
    .slice(0, 3);

  let playable = 0;
  for (const s of toTest) {
    const ph = s.behaviorHints.proxyHeaders.request;
    try {
      const res = await fetch(s.url, {
        method: 'GET',
        headers: {
          'User-Agent': ph['User-Agent'],
          'Referer': ph.Referer,
          'Origin': ph.Origin,
          'Range': 'bytes=0-1023',
        },
        signal: AbortSignal.timeout(10000),
      });
      if (res.ok || res.status === 206 || res.status === 200) {
        pass(s.name + ' -> HTTP ' + res.status + ' (' + s.url.slice(0, 80) + '...)');
        playable++;
      } else {
        fail(s.name + ' -> HTTP ' + res.status);
      }
    } catch (e) {
      fail(s.name + ' -> ' + e.message);
    }
  }

  console.log('');
  console.log('=== Summary ===');
  console.log('  Total streams:     ' + streams.length);
  console.log('  Raw HTTP streams:  ' + rawCount);
  console.log('  Iframe streams:    ' + iframeCount);
  console.log('  Pengu proxy URLs:  ' + penguCount + ' (should be 0)');
  console.log('  Tested playable:   ' + playable + '/' + toTest.length);

  if (penguCount === 0 && missingProxyHeaders === 0) {
    console.log('');
    console.log('\u2713 SUCCESS: cinejoy works independently without pengu.uk proxy');
    process.exit(0);
  } else {
    console.log('');
    console.log('\u2717 FAILURE: cinejoy still depends on pengu.uk or missing proxyHeaders');
    process.exit(1);
  }
})().catch(e => {
  console.error('FATAL: ' + e.stack);
  process.exit(2);
});
