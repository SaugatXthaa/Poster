'use strict';
const p = require('./../allinone-nuvio-repo/providers/antarctica.js');
const https = require('https');
const http = require('http');
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';

function probe(url) {
  return new Promise(resolve => {
    let cur = url, h = 0; const visited = [];
    const next = () => {
      if (h++ >= 5) return resolve({ visited });
      const u = new URL(cur);
      const lib = u.protocol === 'http:' ? http : https;
      const req = lib.request({
        hostname: u.hostname, path: u.pathname + u.search, method: 'GET',
        headers: { 'User-Agent': UA, 'Range': 'bytes=0-1', 'Accept': '*/*' }
      }, (res) => {
        res.destroy();
        visited.push({
          url: cur, status: res.statusCode,
          ct: res.headers['content-type'], cl: res.headers['content-length'],
          cr: res.headers['content-range'], loc: res.headers['location']
        });
        if (res.statusCode >= 300 && res.statusCode < 400 && res.headers.location) {
          cur = new URL(res.headers.location, cur).href;
          next();
        } else {
          resolve({ visited });
        }
      });
      req.on('error', e => resolve({ error: e.message, visited }));
      req.on('timeout', () => { req.destroy(new Error('timeout')); resolve({ error: 'timeout', visited }); });
      req.setTimeout(15000);
      req.end();
    };
    next();
  });
}

const tests = [
  // Anime
  { id: 'tt0245429', type: 'movie', label: 'Spirited Away (anime)' },
  { id: 'tt2948372', type: 'movie', label: 'Your Name (anime)' },
  { id: 'tt0317705', type: 'movie', label: 'The Incredibles (Pixar)' },
  // Kdramas
  { id: 'tt7285546', type: 'series', season: 1, episode: 1, label: 'Crash Landing on You (kdrama)' },
  { id: 'tt10872600', type: 'series', season: 1, episode: 1, label: 'Squid Game (kdrama)' },
  { id: 'tt11203428', type: 'series', season: 1, episode: 1, label: 'Hellbound (kdrama)' },
  // Action / classics
  { id: 'tt0111161', type: 'movie', label: 'The Shawshank Redemption' },
  { id: 'tt0468569', type: 'movie', label: 'The Dark Knight' },
  { id: 'tt0848228', type: 'movie', label: 'The Avengers' },
  // Indian / regional
  { id: 'tt0165362', type: 'movie', label: '3 Idiots (Hindi)' },
  { id: 'tt11309582', type: 'movie', label: 'RRR (Telugu)' },
  // TV series
  { id: 'tt0903747', type: 'series', season: 1, episode: 1, label: 'Breaking Bad S01E01' },
  { id: 'tt0944947', type: 'series', season: 1, episode: 1, label: 'GoT S01E01' },
  { id: 'tt4574334', type: 'series', season: 1, episode: 1, label: 'Stranger Things S01E01' },
];

(async () => {
  console.log('================================================================================');
  console.log('  MULTI-TITLE PLAYABILITY TEST (anime + kdrama + movies + series)');
  console.log('================================================================================');
  let totalProbed = 0, totalPlayable = 0;
  for (const t of tests) {
    console.log('\n--- ' + t.label + ' ---');
    const s = await p.getStreams(t.id, t.type, t.season, t.episode);
    console.log('  streams returned: ' + s.length);
    if (s.length === 0) continue;
    // Probe top 3
    for (const idx of [0, 1, 2]) {
      if (idx >= s.length) continue;
      totalProbed++;
      const r = await probe(s[idx].url);
      if (r.error) { console.log('  pos #' + idx + ' ✗ ' + r.error); continue; }
      const last = r.visited[r.visited.length - 1];
      const cdn = (last.url.match(/(googleusercontent|gdflix|hubcloud|tb-cdn\.[a-z]+|amazonaws|cloudfront|r2\.cloudflarestorage|shegu)/) || [])[0] || 'pengu.uk';
      const sizeMatch = (last.cr || '').match(/\/(\d+)$/) || (last.cl && last.cl.match(/^(\d+)$/));
      const sizeStr = sizeMatch ? (parseInt(sizeMatch[1]) / 1e9).toFixed(2) + 'GB' : '?';
      const isPlayable = (last.status === 200 || last.status === 206) && /video|matroska|octet-stream/i.test(last.ct || '');
      if (isPlayable) {
        totalPlayable++;
        console.log('  pos #' + idx + ' ✓ ' + last.status + ' ' + cdn + ' ' + sizeStr + ' ' + (last.ct || '').slice(0, 30));
      } else {
        console.log('  pos #' + idx + ' ✗ ' + last.status + ' ' + cdn + ' ' + (last.ct || '').slice(0, 30));
      }
    }
  }
  console.log('\n================================================================================');
  console.log('  SUMMARY: ' + totalPlayable + '/' + totalProbed + ' streams playable (' + (totalProbed > 0 ? ((totalPlayable / totalProbed) * 100).toFixed(0) : '0') + '%)');
  console.log('================================================================================');
})();
