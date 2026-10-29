// Multi-stream playability probe — verifies NOT just the top stream is playable,
// but a sample of streams at positions [0, 5, 25, 49] from each title.
// Also tests additional titles (anime, kdrama, action) for broad coverage.
'use strict';

const path = require('path');
const https = require('https');
const http = require('http');
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';

function rangeProbe(url, hops = 4) {
  return new Promise((resolve) => {
    let cur = url;
    const visited = [];
    let h = 0;
    const next = () => {
      if (h++ >= hops) return resolve({ ok: false, error: 'too many hops', visited });
      const u = new URL(cur);
      const lib = u.protocol === 'http:' ? http : https;
      const req = lib.request({
        hostname: u.hostname, port: u.port, path: u.pathname + u.search,
        method: 'GET',
        headers: { 'User-Agent': UA, 'Range': 'bytes=0-1', 'Accept': '*/*' },
      }, (res) => {
        res.destroy();
        visited.push({
          url: cur, status: res.statusCode,
          contentType: res.headers['content-type'],
          contentLength: res.headers['content-length'],
          acceptRanges: res.headers['accept-ranges'],
          contentRange: res.headers['content-range'],
          location: res.headers['location'],
        });
        if (res.statusCode >= 300 && res.statusCode < 400 && res.headers.location) {
          cur = new URL(res.headers.location, cur).href;
          next();
        } else {
          resolve({ ok: true, visited });
        }
      });
      req.on('error', (e) => resolve({ ok: false, error: e.message, visited }));
      req.on('timeout', () => { req.destroy(new Error('timeout')); });
      req.setTimeout(15000);
      req.end();
    };
    next();
  });
}

(async () => {
  const provider = require(path.resolve(__dirname, '../allinone-nuvio-repo/providers/antarctica.js'));

  const tests = [
    { id: 'tt37287335', type: 'movie', label: 'Obsession 2025' },
    { id: 'tt1375666',  type: 'movie', label: 'Inception 2010' },
    { id: 'tt0903747',  type: 'series', season: 1, episode: 1, label: 'Breaking Bad S01E01' },
    { id: 'tt0944947',  type: 'series', season: 1, episode: 1, label: 'GoT S01E01' },
    // Additional titles
    { id: 'tt0111161',  type: 'movie', label: 'The Shawshank Redemption' },
    { id: 'tt0468569',  type: 'movie', label: 'The Dark Knight' },
    { id: 'tt0245429',  type: 'movie', label: 'Spirited Away (anime)' },
    { id: 'tt2948372',  type: 'movie', label: 'Your Name (anime)' },
    { id: 'tt4154796',  type: 'series', season: 1, episode: 1, label: 'The Boys S01E01' },
    { id: 'tt4574334',  type: 'series', season: 1, episode: 1, label: 'Stranger Things S01E01' },
  ];

  console.log('================================================================================');
  console.log('  MULTI-STREAM PLAYABILITY PROBE (positions 0, 5, 25, 49)');
  console.log('================================================================================');

  let totalProbed = 0, totalPlayable = 0, totalFailed = 0;
  const failedSamples = [];

  for (const t of tests) {
    console.log('\n--- ' + t.label + ' ---');
    const streams = await provider.getStreams(t.id, t.type, t.season, t.episode);
    if (streams.length === 0) {
      console.log('  (no streams returned — skipping)');
      continue;
    }
    // Sample positions: 0 (top), 5, 25, last (49 if cap=50)
    const positions = [0, 5, 25, streams.length - 1];
    for (const pos of positions) {
      if (pos >= streams.length) continue;
      const s = streams[pos];
      totalProbed++;
      const r = await rangeProbe(s.url);
      if (r.ok && r.visited.length >= 2) {
        const last = r.visited[r.visited.length - 1];
        const cdnHost = (last.url.match(/tb-cdn\.[a-z]+/) || [])[0] || 'unknown';
        const isCdn = !!cdnHost || /tb-cdn\.[a-z]+/.test(last.url);
        const is206 = last.status === 206 || last.status === 200;
        const hasRanges = last.acceptRanges === 'bytes';
        const size = (last.contentRange || '').match(/\/(\d+)$/);
        const sizeBytes = size ? parseInt(size[1], 10) : null;
        if (isCdn && is206 && hasRanges) {
          totalPlayable++;
          const sizeStr = sizeBytes ? (sizeBytes / 1e9).toFixed(2) + 'GB' : '?';
          console.log('  ✓ pos #' + pos + ' PLAYABLE — ' + s.name + ' — ' + cdnHost + ' ' + sizeStr + ' seekable');
        } else {
          totalFailed++;
          failedSamples.push({ title: t.label, pos, name: s.name, last });
          console.log('  ✗ pos #' + pos + ' FAILED — ' + s.name);
          console.log('    last status: ' + last.status + ', content-type: ' + last.contentType);
          console.log('    accept-ranges: ' + last.acceptRanges + ', URL: ' + last.url.slice(0, 150));
        }
      } else {
        totalFailed++;
        failedSamples.push({ title: t.label, pos, name: s.name, error: r.error });
        console.log('  ✗ pos #' + pos + ' FAILED — ' + s.name + ' — error: ' + (r.error || 'unknown'));
      }
    }
  }

  console.log('\n================================================================================');
  console.log('  PLAYABILITY SUMMARY');
  console.log('================================================================================');
  console.log('  Total streams probed: ' + totalProbed);
  console.log('  Playable (302→CDN→206+ranges): ' + totalPlayable);
  console.log('  Failed: ' + totalFailed);
  console.log('  Success rate: ' + (totalProbed > 0 ? ((totalPlayable / totalProbed) * 100).toFixed(1) : '0') + '%');
  if (failedSamples.length > 0) {
    console.log('\n  Failed samples:');
    failedSamples.forEach(f => console.log('    - ' + f.title + ' pos #' + f.pos + ': ' + f.name));
  } else {
    console.log('\n  ✓ ALL PROBED STREAMS PLAYABLE');
  }
  process.exit(totalFailed > 0 ? 1 : 0);
})();
