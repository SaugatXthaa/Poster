// Option I: Run playability tests across Nuvio providers.
// Tests a sample of providers (the ones most likely to work) to find which
// are reliable for PenguPlay-independent streaming.
'use strict';

const fs = require('fs');
const path = require('path');
const https = require('https');
const http = require('http');
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';

// Check if a provider file exists and has getStreams export
function loadProvider(name) {
  const providersDir = '/home/z/my-project/allinone-nuvio-repo/providers';
  const filePath = path.join(providersDir, name + '.js');
  if (!fs.existsSync(filePath)) return null;
  try {
    // Need to install cheerio-without-node-native for some providers
    // We'll try to load and catch any errors
    const mod = require(filePath);
    if (typeof mod.getStreams === 'function') return mod;
    return null;
  } catch (e) {
    return { error: e.message };
  }
}

function probe(url, hops = 4) {
  return new Promise(resolve => {
    let cur = url, h = 0; const visited = [];
    const next = () => {
      if (h++ >= hops) return resolve({ ok: false, error: 'too many hops', visited });
      const u = new URL(cur);
      const lib = u.protocol === 'http:' ? http : https;
      const req = lib.request({
        hostname: u.hostname, port: u.port, path: u.pathname + u.search,
        method: 'GET',
        headers: { 'User-Agent': UA, 'Range': 'bytes=0-1', 'Accept': '*/*' }
      }, (res) => {
        res.destroy();
        visited.push({
          url: cur, status: res.statusCode,
          ct: res.headers['content-type'],
          cl: res.headers['content-length'],
          cr: res.headers['content-range']
        });
        if (res.statusCode >= 300 && res.statusCode < 400 && res.headers.location) {
          cur = new URL(res.headers.location, cur).href;
          next();
        } else {
          resolve({ ok: true, visited });
        }
      });
      req.on('error', e => resolve({ ok: false, error: e.message, visited }));
      req.on('timeout', () => { req.destroy(new Error('timeout')); resolve({ ok: false, error: 'timeout', visited }); });
      req.setTimeout(10000);
      req.end();
    };
    next();
  });
}

(async () => {
  console.log('================================================================================');
  console.log('  OPTION I: PLAYABILITY TEST ACROSS NUVIO PROVIDERS');
  console.log('================================================================================');
  
  // List of providers to test (from manifest, focusing on the well-known ones)
  const providersToTest = [
    'antarctica', 'moviebox', 'pengu_all',
    '4khdhub', 'hdhub4u', 'moviesdrive', 'hindmoviez', 'movieshunt',
    'cineby', 'castle', 'cinefreak', 'cinemacity', 'moonflix',
    'dahmermovies', 'dahmermovies-4k', 'dooflix', 'einthusan',
    'goated', 'gramcinema', 'hdghartv', 'movieblast', 'movies4u',
    'movix', 'movy', 'moviesources', 'multimovies', 'nikastream',
    'reanime', 'rivestream', 'streamxtv', 'uhdmovies', 'vidlink',
    'vidfast', '1shows', '4khdhub', 'allmovieland', 'desiflix'
  ];
  
  const testId = 'tt1375666'; // Inception
  const testType = 'movie';
  
  const results = [];
  for (const name of providersToTest) {
    const provider = loadProvider(name);
    if (provider === null) {
      results.push({ name, status: 'not_found', streams: 0, playable: 0 });
      continue;
    }
    if (provider.error) {
      results.push({ name, status: 'load_error', error: provider.error.slice(0, 80), streams: 0, playable: 0 });
      continue;
    }
    
    // Call getStreams
    try {
      const streams = await Promise.race([
        provider.getStreams(testId, testType),
        new Promise((_, reject) => setTimeout(() => reject(new Error('30s timeout')), 30000))
      ]);
      if (!Array.isArray(streams) || streams.length === 0) {
        results.push({ name, status: 'no_streams', streams: 0, playable: 0 });
        continue;
      }
      
      // Probe top 2 streams
      let playable = 0;
      for (let i = 0; i < Math.min(2, streams.length); i++) {
        const s = streams[i];
        if (!s.url || !s.url.startsWith('http')) continue;
        const r = await probe(s.url);
        if (r.ok && r.visited.length >= 1) {
          const last = r.visited[r.visited.length - 1];
          const isPlayable = (last.status === 200 || last.status === 206) &&
            /video|matroska|octet-stream|mpegurl|mp4|mkv/i.test(last.ct || '');
          if (isPlayable) playable++;
        }
      }
      results.push({ name, status: 'ok', streams: streams.length, playable });
    } catch (e) {
      results.push({ name, status: 'error', error: (e.message || '').slice(0, 80), streams: 0, playable: 0 });
    }
    process.stdout.write('.');
  }
  
  console.log('');
  console.log('================================================================================');
  console.log('  RESULTS (probing top-2 streams for Inception tt1375666)');
  console.log('================================================================================');
  console.log('Provider               | Status       | Streams | Playable');
  console.log('-----------------------+--------------+---------+----------');
  
  const working = [];
  const broken = [];
  for (const r of results) {
    const statusStr = r.status === 'ok' ? '✓ ok' :
                       r.status === 'no_streams' ? '⚠ no streams' :
                       r.status === 'load_error' ? '✗ load err' :
                       r.status === 'not_found' ? '✗ not found' :
                       '✗ err: ' + (r.error || '').slice(0, 25);
    const str = (r.name + '                       ').slice(0, 23);
    const stat = (statusStr + '              ').slice(0, 14);
    const streams = String(r.streams).padStart(7);
    const playable = String(r.playable).padStart(8);
    console.log(str + ' | ' + stat + ' | ' + streams + ' | ' + playable);
    if (r.status === 'ok' && r.playable > 0) working.push(r);
    else broken.push(r);
  }
  
  console.log('');
  console.log('================================================================================');
  console.log('  SUMMARY');
  console.log('================================================================================');
  console.log('  Working providers (returned playable streams): ' + working.length);
  console.log('  Broken/empty providers: ' + broken.length);
  console.log('');
  console.log('  Working providers (ranked by streams returned):');
  working.sort((a, b) => b.streams - a.streams).forEach(r => {
    console.log('    ✓ ' + r.name + ' — ' + r.streams + ' streams, ' + r.playable + '/2 playable');
  });
})();
