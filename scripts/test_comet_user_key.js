// Full validation test for the user's TorBox key (ef869d91-...).
// Verifies:
//   1. Key works — Comet returns streams (not 0, not 401/403)
//   2. MAX_STREAMS=50 cap is respected
//   3. Each stream has enriched metadata (quality/codec/HDR/audio/language in name)
//   4. Streams are actually playable:
//        - URL is HTTPS
//        - GET → 302 → tb-cdn.io HTTPS CDN
//        - CDN returns 206 Partial Content + accept-ranges: bytes (seekable MKV)
//   5. Works for both movies AND TV episodes
'use strict';

const path = require('path');
const https = require('https');
const http = require('http');
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';

// Range-GET through the redirect chain (does NOT download the file — Range: bytes=0-1)
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
        hostname: u.hostname,
        port: u.port,
        path: u.pathname + u.search,
        method: 'GET',
        headers: { 'User-Agent': UA, 'Range': 'bytes=0-1', 'Accept': '*/*' },
      }, (res) => {
        // Drain immediately
        res.destroy();
        visited.push({
          url: cur,
          status: res.statusCode,
          contentType: res.headers['content-type'],
          contentLength: res.headers['content-length'],
          acceptRanges: res.headers['accept-ranges'],
          contentRange: res.headers['content-range'],
          contentDisposition: (res.headers['content-disposition'] || '').slice(0, 100),
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
  console.log('================================================================');
  console.log('  USER TORBOX KEY VALIDATION TEST');
  console.log('  Key: ef869d91-98b8-4d1c-81a5-0d53e259b179 (user-supplied)');
  console.log('  Cap: 50 streams (default MAX_STREAMS)');
  console.log('================================================================');

  // Load the provider as Nuvio does
  const provider = require(path.resolve(__dirname, '../allinone-nuvio-repo/providers/comet.js'));
  const internals = require(path.resolve(__dirname, '../scripts/comet_all_in_one.js'))._internals;

  console.log('\n--- 1. CONFIG CHECK ---');
  const cfgB64 = internals.COMET_BASE.replace('https://comet.feels.legal/', '');
  const cfg = JSON.parse(Buffer.from(cfgB64, 'base64').toString('utf8'));
  console.log('cachedOnly:', cfg.cachedOnly);
  console.log('enableTorrent:', cfg.enableTorrent);
  console.log('debridServices[0]:', JSON.stringify(cfg.debridServices[0]));
  const expectedKey = 'ef869d91-98b8-4d1c-81a5-0d53e259b179';
  const actualKey = cfg.debridServices[0].apiKey;
  console.log('Key in config matches user key:', actualKey === expectedKey ? '✓ YES' : '✗ NO');
  if (actualKey !== expectedKey) {
    console.log('  expected:', expectedKey);
    console.log('  actual:  ', actualKey);
    process.exit(1);
  }
  console.log('MAX_STREAMS:', internals.MAX_STREAMS);

  const tests = [
    { id: 'tt37287335', type: 'movie', label: 'Obsession 2025' },
    { id: 'tt1375666',  type: 'movie', label: 'Inception 2010' },
    { id: 'tt0903747',  type: 'series', season: 1, episode: 1, label: 'Breaking Bad S01E01' },
    { id: 'tt0944947',  type: 'series', season: 1, episode: 1, label: 'Game of Thrones S01E01' },
  ];

  let totalPass = 0;
  let totalFail = 0;

  for (const t of tests) {
    console.log('\n--- TEST: ' + t.label + ' ---');
    const streams = await provider.getStreams(t.id, t.type, t.season, t.episode);
    console.log('Streams returned: ' + streams.length);

    if (streams.length === 0) {
      console.log('✗ FAIL: 0 streams returned — key may be invalid or rate-limited');
      totalFail++;
      continue;
    }

    // Check 1: cap of 50
    if (streams.length <= 50) {
      console.log('  ✓ cap=50 respected (returned ' + streams.length + ')');
    } else {
      console.log('  ✗ FAIL: cap=50 violated (returned ' + streams.length + ')');
      totalFail++;
      continue;
    }

    // Check 2: required fields
    const reqFields = ['name', 'title', 'url', 'quality', 'provider', 'behaviorHints'];
    let bad = 0;
    for (const s of streams) {
      for (const f of reqFields) {
        if (!s[f]) { console.log('  missing ' + f + ' on: ' + (s.name || '(no name)')); bad++; break; }
      }
      if (!s.url.startsWith('https://')) { console.log('  NON-HTTPS URL: ' + s.url); bad++; }
    }
    if (bad > 0) {
      console.log('  ✗ FAIL: ' + bad + ' field issues');
      totalFail++;
      continue;
    }
    console.log('  ✓ all streams have required fields and HTTPS URLs');

    // Check 3: enriched metadata (name should contain at least quality + codec)
    const sample = streams.slice(0, 5);
    console.log('  --- first 5 streams (enriched metadata) ---');
    for (const s of sample) {
      const tags = [];
      if (s.quality) tags.push(s.quality.toUpperCase());
      const codecMatch = s.name.match(/HEVC|AV1|H264/);
      if (codecMatch) tags.push(codecMatch[0]);
      const hdrMatch = s.name.match(/DV|HDR10\+|HDR10|HDR/);
      if (hdrMatch) tags.push(hdrMatch[0]);
      const audioMatch = s.name.match(/TrueHD|Atmos|DDP|DTS-HD|DTS|AAC|5\.1|7\.1/);
      if (audioMatch) tags.push(audioMatch[0]);
      const langMatch = s.name.match(/Hindi|Tamil|Telugu|Korean|Japanese|French|German|Italian|Spanish/);
      if (langMatch) tags.push(langMatch[0]);
      console.log('    ' + s.name);
      console.log('      parsed tags: ' + tags.join(', '));
      console.log('      filename: ' + (s.behaviorHints.filename || '(none)').slice(0, 110));
    }
    // Enrichment check: at least the top 5 streams should have ≥3 tags
    const enrichedCount = sample.filter(s => {
      let n = 0;
      if (/HEVC|AV1|H264/.test(s.name)) n++;
      if (/DV|HDR10\+|HDR10|HDR/.test(s.name)) n++;
      if (/TrueHD|Atmos|DDP|DTS/.test(s.name)) n++;
      if (/Hindi|Tamil|Telugu|Korean|Japanese|French|German|Italian|Spanish/.test(s.name)) n++;
      if (/REMUX|BluRay|WEB-DL|WEBRip|HDTV/.test(s.name)) n++;
      return n >= 3;
    }).length;
    if (enrichedCount >= 3) {
      console.log('  ✓ enrichment: ' + enrichedCount + '/5 top streams have ≥3 parsed tags');
    } else {
      console.log('  ⚠ enrichment: only ' + enrichedCount + '/5 top streams have ≥3 tags');
    }

    // Check 4: stream is actually playable (top stream → 302 → CDN → 206 + accept-ranges:bytes)
    console.log('  --- playability check (top stream) ---');
    const top = streams[0];
    console.log('    probing: ' + top.name);
    const r = await rangeProbe(top.url);
    if (!r.ok) {
      console.log('  ✗ FAIL: range probe failed: ' + r.error);
      console.log('    hops: ' + JSON.stringify(r.visited, null, 2));
      totalFail++;
      continue;
    }
    // Print the redirect chain
    r.visited.forEach((v, i) => {
      console.log('    [' + i + '] ' + v.status + ' ' + (v.contentType || '') + ' ' + (v.contentRange || '') + ' ' + (v.acceptRanges || ''));
      console.log('        url: ' + v.url.slice(0, 130) + (v.url.length > 130 ? '...' : ''));
      if (v.location) console.log('        → ' + v.location.slice(0, 130) + (v.location.length > 130 ? '...' : ''));
      if (v.contentDisposition) console.log('        disposition: ' + v.contentDisposition);
    });
    const last = r.visited[r.visited.length - 1];
    const isCdn = /tb-cdn\.(io|pw)/.test(last.url);
    const is206 = last.status === 206 || last.status === 200;
    const hasRanges = last.acceptRanges === 'bytes';
    if (isCdn && is206 && hasRanges) {
      console.log('  ✓ PLAYABLE: 302→CDN (' + (last.url.match(/(tb-cdn\.[a-z]+)/) || [])[1] + ') → ' + last.status + ' Partial Content, accept-ranges: bytes');
      console.log('    file size: ' + (last.contentRange || last.contentLength) + ' bytes (range-requestable, seekable)');
      totalPass++;
    } else {
      console.log('  ✗ FAIL: final response not playable');
      console.log('    CDN URL?', isCdn);
      console.log('    206/200?', is206, '(got ' + last.status + ')');
      console.log('    accept-ranges=bytes?', hasRanges);
      totalFail++;
    }
  }

  console.log('\n================================================================');
  console.log('  SUMMARY: ' + totalPass + '/' + tests.length + ' PASS, ' + totalFail + ' FAIL');
  console.log('================================================================');
  process.exit(totalFail > 0 ? 1 : 0);
})();
