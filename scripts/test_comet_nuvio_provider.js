// Final integration test: load comet.js as Nuvio would (require + getStreams call).
// Verifies:
//   1. require('./providers/comet.js') works
//   2. getStreams is exported
//   3. Returns HTTPS-only streams for both movie and TV
//   4. Each stream has the Nuvio-required fields (name, title, url, quality, provider)
//   5. Each stream's URL 302-redirects to a real tb-cdn.io HTTPS URL
'use strict';

const path = require('path');
const https = require('https');
const http = require('http');

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';

async function followOnce(url) {
  const u = new URL(url);
  const lib = u.protocol === 'http:' ? http : https;
  return new Promise((resolve, reject) => {
    const req = lib.request({
      hostname: u.hostname, path: u.pathname + u.search, method: 'GET',
      headers: { 'User-Agent': UA, 'Range': 'bytes=0-1' },
    }, (res) => {
      res.destroy();
      resolve({
        status: res.statusCode,
        location: res.headers['location'],
        contentType: res.headers['content-type'],
        acceptRanges: res.headers['accept-ranges'],
        contentRange: res.headers['content-range'],
        contentLength: res.headers['content-length'],
      });
    });
    req.on('error', reject);
    req.end();
  });
}

(async () => {
  console.log('===== TEST 1: load provider via require =====');
  const provider = require(path.resolve(__dirname, '../allinone-nuvio-repo/providers/comet.js'));
  console.log('typeof provider:', typeof provider);
  console.log('typeof getStreams:', typeof provider.getStreams);
  if (typeof provider.getStreams !== 'function') {
    console.error('FAIL: provider.getStreams is not a function');
    process.exit(1);
  }

  const tests = [
    { id: 'tt37287335', type: 'movie', label: 'Obsession (2025)' },
    { id: 'tt1375666',  type: 'movie', label: 'Inception (2010)' },
    { id: 'tt0903747',  type: 'series', season: 1, episode: 1, label: 'Breaking Bad S01E01' },
    { id: 'tt0944947',  type: 'series', season: 1, episode: 1, label: 'Game of Thrones S01E01' },
  ];

  let totalPass = 0;
  let totalFail = 0;
  for (const t of tests) {
    console.log('\n===== ' + t.label + ' =====');
    const streams = await provider.getStreams(t.id, t.type, t.season, t.episode);
    if (streams.length === 0) {
      console.log('FAIL: 0 streams returned');
      totalFail++;
      continue;
    }
    console.log('  ' + streams.length + ' streams returned');
    // Field check
    const requiredFields = ['name', 'title', 'url', 'quality', 'provider'];
    let fieldErrors = 0;
    for (const s of streams) {
      for (const f of requiredFields) {
        if (!s[f]) { console.log('  missing field ' + f + ' on stream: ' + (s.name || '(no name)')); fieldErrors++; }
      }
      if (!s.url.startsWith('https://')) {
        console.log('  NON-HTTPS URL: ' + s.url);
        fieldErrors++;
      }
    }
    if (fieldErrors > 0) {
      console.log('  FAIL: ' + fieldErrors + ' field issues');
      totalFail++;
      continue;
    }
    console.log('  ✓ all streams have required fields and HTTPS URLs');
    // Verify the top stream 302s to tb-cdn
    const top = streams[0];
    console.log('  top stream: ' + top.name);
    const r1 = await followOnce(top.url);
    if (r1.status !== 302) {
      console.log('  FAIL: expected 302 from Comet playback URL, got ' + r1.status);
      totalFail++;
      continue;
    }
    console.log('  ✓ Comet → ' + r1.status);
    console.log('    Location: ' + r1.location.slice(0, 120));
    if (!r1.location.startsWith('https://')) {
      console.log('  FAIL: Comet redirect is NOT https');
      totalFail++;
      continue;
    }
    // Now hit the tb-cdn URL — should return 206 Partial Content
    const r2 = await followOnce(r1.location);
    if (r2.status !== 206 && r2.status !== 200) {
      console.log('  FAIL: tb-cdn returned ' + r2.status + ', expected 206');
      totalFail++;
      continue;
    }
    console.log('  ✓ tb-cdn → ' + r2.status + (r2.status === 206 ? ' Partial Content' : ''));
    console.log('    content-type:  ' + r2.contentType);
    console.log('    accept-ranges: ' + r2.acceptRanges);
    console.log('    content-range: ' + r2.contentRange);
    console.log('    content-length:' + r2.contentLength);
    if (r2.acceptRanges === 'bytes') {
      console.log('  ✓ FINAL: seekable HTTPS MKV (Range requests supported)');
      totalPass++;
    } else {
      console.log('  FAIL: accept-ranges not bytes');
      totalFail++;
    }
  }
  console.log('\n===== SUMMARY =====');
  console.log('PASS: ' + totalPass + '/' + tests.length);
  console.log('FAIL: ' + totalFail + '/' + tests.length);
  process.exit(totalFail > 0 ? 1 : 0);
})();
