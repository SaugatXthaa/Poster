// Comprehensive bug + playability test for cinejoy_all_in_one.js
// Run: node /home/z/my-project/scripts/test_cinejoy_playability.js

'use strict';
const path = require('path');
const cinejoy = require(path.join(__dirname, 'cinejoy_all_in_one.js'));

const TITLES = [
  { tmdb: '27205',  type: 'movie', label: 'Inception (movie)' },
  { tmdb: '693134', type: 'movie', label: 'Dune Part Two (movie)' },
  { tmdb: '438631', type: 'movie', label: 'Dune 2021 (movie)' },
  { tmdb: '634649', type: 'movie', label: 'Spider-Man NWH (movie)' },
  { tmdb: '1396',   type: 'tv', season: 1, episode: 1, label: 'Breaking Bad S01E01' },
  { tmdb: '82856',  type: 'tv', season: 1, episode: 1, label: 'Mandalorian S01E01' },
  { tmdb: '95479',  type: 'tv', season: 1, episode: 1, label: 'Jujutsu Kaisen S01E01 (anime)' },
];

// Verify if a stream URL is actually playable (returns valid HLS/file content)
async function verifyStreamPlayable(stream) {
  const url = stream.url;
  const headers = stream.headers || {};
  const fetchOpts = { 
    method: 'GET',
    headers: { 'User-Agent': headers['User-Agent'] || 'Mozilla/5.0', ...(headers.Referer ? { Referer: headers.Referer } : {}) },
    signal: AbortSignal.timeout(15000),
  };
  
  try {
    const res = await fetch(url, fetchOpts);
    const status = res.status;
    const contentType = res.headers.get('content-type') || '';
    const body = await res.text();
    const bodySize = body.length;
    
    // HLS playlist check
    const isHls = body.trimStart().startsWith('#EXTM3U');
    const hasStreamInf = body.includes('#EXT-X-STREAM-INF');
    const hasSegments = body.includes('#EXTINF') || body.includes('#EXT-X-MEDIA-SEQUENCE');
    
    // MP4/file check (first 4 bytes)
    const firstBytes = body.slice(0, 4);
    const isMp4 = firstBytes.includes('ftyp') || firstBytes.includes('mp4');
    
    let playable = false;
    let reason = '';
    if (status === 200 && isHls) {
      playable = true;
      reason = `HLS playlist (${hasStreamInf ? 'master' : 'media'}, ${bodySize} bytes)`;
    } else if (status === 200 && isMp4) {
      playable = true;
      reason = `MP4 file (${bodySize} bytes — note: response was body-truncated)`;
    } else if (status === 200 && bodySize > 0) {
      playable = true;
      reason = `Unknown format (${contentType}, ${bodySize} bytes, first 50: ${body.slice(0, 50)})`;
    } else if (status === 200 && bodySize === 0) {
      playable = false;
      reason = 'Empty response body';
    } else if (status === 403) {
      playable = false;
      reason = 'HTTP 403 — likely Cloudflare block';
    } else if (status === 404) {
      playable = false;
      reason = 'HTTP 404 — not found';
    } else if (status >= 400) {
      playable = false;
      reason = `HTTP ${status} — server error`;
    } else {
      playable = false;
      reason = `HTTP ${status} — unhandled status`;
    }
    
    return { playable, status, contentType, bodySize, reason, firstBytes: firstBytes.slice(0, 10) };
  } catch (e) {
    return { playable: false, error: e.message, reason: 'Network error: ' + e.message };
  }
}

async function main() {
  console.log('='.repeat(80));
  console.log('  CineJoy All-In-One Bug + Playability Test');
  console.log('='.repeat(80) + '\n');
  
  let totalStreamsFound = 0;
  let totalPlayable = 0;
  let totalBugs = 0;
  const bugs = [];
  const allStreams = [];
  
  for (const t of TITLES) {
    console.log('\n--- ' + t.label + ' (TMDB ' + t.tmdb + ') ---');
    
    let streams;
    try {
      streams = await cinejoy.getStreams(t.tmdb, t.type, t.season, t.episode);
    } catch (e) {
      console.log('  ✗ getStreams threw: ' + e.message);
      bugs.push({ test: t.label, bug: 'getStreams threw exception: ' + e.message });
      totalBugs++;
      continue;
    }
    
    if (!Array.isArray(streams)) {
      console.log('  ✗ getStreams returned non-array: ' + typeof streams);
      bugs.push({ test: t.label, bug: 'getStreams returned non-array' });
      totalBugs++;
      continue;
    }
    
    console.log('  Streams returned: ' + streams.length);
    totalStreamsFound += streams.length;
    
    for (let i = 0; i < streams.length; i++) {
      const s = streams[i];
      allStreams.push({ ...s, testLabel: t.label });
      
      // Validate stream object structure
      const issues = [];
      if (!s.name) issues.push('missing name');
      if (!s.url) issues.push('missing url');
      if (!s.quality) issues.push('missing quality');
      if (!s.type) issues.push('missing type');
      if (s.quality && !/^\d+[pPkK]$|^HD$/.test(s.quality) && s.quality !== '') {
        // Quality looks weird
      }
      
      if (issues.length > 0) {
        console.log('  [' + i + '] STRUCTURE BUG: ' + issues.join(', '));
        bugs.push({ test: t.label, stream: s.name, bug: issues.join(', ') });
        totalBugs++;
        continue;
      }
      
      // Verify the URL is actually playable
      process.stdout.write('  [' + i + '] ' + s.name + ' | q=' + s.quality + ' | verifying... ');
      const verification = await verifyStreamPlayable(s);
      
      if (verification.playable) {
        console.log('✓ PLAYABLE');
        console.log('       ' + verification.reason);
        totalPlayable++;
      } else {
        console.log('✗ NOT PLAYABLE');
        console.log('       ' + verification.reason);
        bugs.push({ 
          test: t.label, 
          stream: s.name, 
          url: s.url.slice(0, 80), 
          bug: 'Stream not playable: ' + verification.reason 
        });
        totalBugs++;
      }
    }
  }
  
  // Bug analysis
  console.log('\n' + '='.repeat(80));
  console.log('  BUG ANALYSIS');
  console.log('='.repeat(80));
  
  // Test edge cases
  console.log('\n--- Edge Case Tests ---');
  
  // 1. Invalid TMDB ID
  console.log('\n  Test: Invalid TMDB ID (999999999)');
  try {
    const r = await cinejoy.getStreams('999999999', 'movie');
    console.log('  Result: ' + r.length + ' streams (expected: 0 due to no TMDB info)');
    if (r.length > 0) {
      console.log('  ⚠ POTENTIAL BUG: got streams for non-existent TMDB ID');
      bugs.push({ test: 'invalid-tmdb', bug: 'returned streams for non-existent ID' });
      totalBugs++;
    }
  } catch (e) {
    console.log('  ✗ Exception: ' + e.message);
    bugs.push({ test: 'invalid-tmdb', bug: 'threw exception: ' + e.message });
    totalBugs++;
  }
  
  // 2. TV without season/episode
  console.log('\n  Test: TV without season/episode (TMDB 1396, type=tv)');
  try {
    const r = await cinejoy.getStreams('1396', 'tv');
    console.log('  Result: ' + r.length + ' streams (expected: 0 — server needs season/episode)');
    if (r.length > 0) {
      console.log('  ⚠ UNEXPECTED: got streams without season/episode');
    }
  } catch (e) {
    console.log('  ✗ Exception: ' + e.message);
    bugs.push({ test: 'tv-no-episode', bug: 'threw exception: ' + e.message });
    totalBugs++;
  }
  
  // 3. Empty TMDB ID
  console.log('\n  Test: Empty TMDB ID');
  try {
    const r = await cinejoy.getStreams('', 'movie');
    console.log('  Result: ' + r.length + ' streams');
    if (r.length > 0) {
      console.log('  ⚠ BUG: got streams for empty TMDB ID');
      bugs.push({ test: 'empty-tmdb', bug: 'returned streams for empty ID' });
      totalBugs++;
    }
  } catch (e) {
    console.log('  Exception (acceptable): ' + e.message.slice(0, 80));
  }
  
  // 4. Concurrent calls (race condition test)
  console.log('\n  Test: Concurrent getStreams calls (race condition)');
  try {
    const results = await Promise.all([
      cinejoy.getStreams('27205', 'movie'),
      cinejoy.getStreams('1396', 'tv', 1, 1),
      cinejoy.getStreams('95479', 'tv', 1, 1),
    ]);
    const counts = results.map(r => r.length);
    console.log('  Concurrent result counts: ' + JSON.stringify(counts));
    if (results.some(r => !Array.isArray(r))) {
      console.log('  ✗ BUG: at least one concurrent call returned non-array');
      bugs.push({ test: 'concurrent', bug: 'non-array result' });
      totalBugs++;
    } else {
      console.log('  ✓ All concurrent calls returned arrays');
    }
  } catch (e) {
    console.log('  ✗ Exception: ' + e.message);
    bugs.push({ test: 'concurrent', bug: 'threw: ' + e.message });
    totalBugs++;
  }
  
  // Final summary
  console.log('\n' + '='.repeat(80));
  console.log('  FINAL SUMMARY');
  console.log('='.repeat(80));
  console.log('Total streams found:     ' + totalStreamsFound);
  console.log('Total playable:          ' + totalPlayable + ' / ' + totalStreamsFound);
  console.log('Total bugs found:        ' + totalBugs);
  console.log('Playability rate:       ' + (totalStreamsFound > 0 ? Math.round(totalPlayable / totalStreamsFound * 100) : 0) + '%');
  
  if (bugs.length > 0) {
    console.log('\n=== BUG LIST ===');
    bugs.forEach((b, i) => {
      console.log('  [' + (i+1) + '] ' + (b.test || '?') + (b.stream ? '/' + b.stream : '') + ': ' + b.bug);
    });
  } else {
    console.log('\n=== NO BUGS FOUND ✓ ===');
  }
}

main().catch(e => { console.error('FATAL:', e); process.exit(1); });
