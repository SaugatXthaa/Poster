// Test ALL 4K-capable providers from both repos
// Tests each provider with Dune Part Two (TMDB 693134) and Breaking Bad S01E01 (TMDB 1396)
// Reports which ones return actual 4K (2160p) playable streams

'use strict';
const fs = require('fs');
const path = require('path');
const { execFileSync } = require('child_process');

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';
const DIR = '/home/z/my-project/download/scrapers-js';

// All 4K-capable providers to test (excluding torrentio which is torrent-based)
const PROVIDERS_TO_TEST = [
  // Already verified 4K (re-test for confirmation)
  '4khdhub', 'cineby', 'dahmermovies', 'dahmermovies-4k', 'hdhub4u', 'hindmoviez', 'uhdmovies', 'videasy',
  // Unverified from Hindi-Nuvio
  'cinemacity', 'movieblast', 'movies4u', 'moviesdrive', 'vegamovies',
  // Unverified from All-in-One-Nuvio
  '1shows', 'allwish', 'anime-sama', 'animekai', 'animesalt', 'animezey', 'castle',
  'cinefreak', 'ctgmovies', 'desiflix', 'einthusan', 'fibwatch', 'goated',
  'gramcinema', 'hdghartv', 'moonflix', 'moviebox', 'movieshunt', 'netmirror',
  'onlykdrama', 'peachify', 'persianstremio', 'playimdb', 'purstream',
  'showbox', 'topcartoons', 'vidfast', 'vidlink', 'vidlove', 'vidrock',
  'vixsrc', 'zinkmovies',
];

const TESTS = [
  { label: 'Movie (Dune Part Two, TMDB 693134)', tmdbId: 693134, type: 'movie', season: null, episode: null },
];

// Copy provider from source repo if not already in scrapers-js
function ensureProvider(name) {
  const dest = path.join(DIR, `nuvio_${name}.js`);
  if (fs.existsSync(dest)) return true;
  
  // Try All-in-One-Nuvio first
  const src1 = `/home/z/my-project/allinone-nuvio-repo/providers/${name}.js`;
  if (fs.existsSync(src1)) {
    fs.copyFileSync(src1, dest);
    return true;
  }
  // Try Hindi-Nuvio
  const src2 = `/home/z/my-project/hindi-nuvio-repo/providers/${name}.js`;
  if (fs.existsSync(src2)) {
    fs.copyFileSync(src2, dest);
    return true;
  }
  return false;
}

// Verify a stream URL actually returns playable content
function verifyStream(url) {
  if (!url || typeof url !== 'string') return { ok: false, error: 'no url' };
  if (url.startsWith('magnet:')) return { ok: false, error: 'magnet (torrent)' };
  if (!url.startsWith('http')) return { ok: false, error: 'non-http' };
  
  try {
    const result = execFileSync('curl', [
      '-sI', '--max-time', '8',
      '-H', `User-Agent: ${UA}`,
      '-H', 'Referer: https://example.com/',
      '-H', 'Range: bytes=0-0',
      url,
    ], { encoding: 'utf8', timeout: 12000 });
    
    const statusLine = result.split('\n')[0];
    const code = parseInt(statusLine.match(/\d{3}/)?.[0] || '0');
    const ct = (result.match(/content-type:\s*(.+)/i)?.[1] || '').trim();
    
    if (code >= 200 && code < 400) {
      return { ok: true, status: code, contentType: ct };
    }
    return { ok: false, status: code, contentType: ct };
  } catch (e) {
    return { ok: false, error: e.message.slice(0, 60) };
  }
}

// Test a single provider
async function testProvider(name, test) {
  const file = path.join(DIR, `nuvio_${name}.js`);
  try {
    delete require.cache[require.resolve(file)];
    const mod = require(file);
    if (!mod || typeof mod.getStreams !== 'function') {
      return { name, status: 'no getStreams', streams4K: 0, total: 0 };
    }
    
    const result = await Promise.race([
      mod.getStreams(test.tmdbId, test.type, test.season, test.episode),
      new Promise(r => setTimeout(() => null, 20000)),
    ]);
    
    if (!result || !Array.isArray(result) || result.length === 0) {
      return { name, status: 'no streams', streams4K: 0, total: 0 };
    }
    
    // Count 4K streams
    const streams4K = result.filter(s => {
      const q = (s.quality || '').toLowerCase();
      return q.includes('2160') || q.includes('4k');
    });
    
    // Verify first 4K stream plays
    let verified4K = false;
    if (streams4K.length > 0) {
      const v = verifyStream(streams4K[0].url);
      verified4K = v.ok;
    }
    
    // Also count non-torrent streams
    const playable = result.filter(s => {
      const u = s.url || '';
      return u.startsWith('http') && !u.startsWith('magnet:');
    });
    
    return {
      name,
      status: 'ok',
      total: result.length,
      playable: playable.length,
      streams4K: streams4K.length,
      verified4K,
      qualities: [...new Set(result.map(s => s.quality))].sort(),
    };
  } catch (e) {
    return { name, status: 'error: ' + e.message.slice(0, 80), streams4K: 0, total: 0 };
  }
}

// Main
(async () => {
  console.log('=== 4K Provider Test ===');
  console.log(`Testing ${PROVIDERS_TO_TEST.length} providers with Dune Part Two (TMDB 693134)\n`);
  
  const results = [];
  
  for (const name of PROVIDERS_TO_TEST) {
    // Ensure provider file exists
    if (!ensureProvider(name)) {
      console.log(`❓ ${name}: file not found in any repo`);
      results.push({ name, status: 'file not found', streams4K: 0, total: 0 });
      continue;
    }
    
    process.stdout.write(`Testing ${name}... `);
    const result = await testProvider(name, TESTS[0]);
    results.push(result);
    
    if (result.status === 'ok' && result.streams4K > 0) {
      const verified = result.verified4K ? '✅ VERIFIED' : '⚠️ 4K found but unverified';
      console.log(`✅ ${result.streams4K} 4K stream(s) [${verified}] | ${result.playable} total playable | qualities: ${result.qualities.join(', ')}`);
    } else if (result.status === 'ok') {
      console.log(`⚠️ ${result.playable} stream(s), no 4K | qualities: ${result.qualities.join(', ')}`);
    } else {
      console.log(`❌ ${result.status}`);
    }
  }
  
  // Summary
  console.log('\n' + '='.repeat(80));
  console.log('=== SUMMARY ===');
  console.log('='.repeat(80));
  
  const verified4K = results.filter(r => r.verified4K);
  const has4K = results.filter(r => r.streams4K > 0 && !r.verified4K);
  const no4K = results.filter(r => r.status === 'ok' && r.streams4K === 0);
  const failed = results.filter(r => r.status !== 'ok');
  
  console.log(`\n✅ VERIFIED 4K providers (${verified4K.length}):`);
  verified4K.forEach(r => console.log(`   ${r.name}: ${r.streams4K} 4K stream(s), ${r.playable} total`));
  
  console.log(`\n⚠️  4K found but unverified (${has4K.length}):`);
  has4K.forEach(r => console.log(`   ${r.name}: ${r.streams4K} 4K stream(s)`));
  
  console.log(`\n📋 Working but no 4K (${no4K.length}):`);
  no4K.forEach(r => console.log(`   ${r.name}: ${r.playable} stream(s) | ${r.qualities.join(', ')}`));
  
  console.log(`\n❌ Failed/No streams (${failed.length}):`);
  failed.forEach(r => console.log(`   ${r.name}: ${r.status}`));
})();
