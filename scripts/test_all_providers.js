const fs = require('fs');
const path = require('path');

const repos = [
  { name: 'GitHub', dir: '/tmp/nuvio-plugins/providers' },
  { name: 'Codeberg', dir: '/tmp/nuvio-plugin/providers' },
];

// Test movies
const testMovies = [
  { tmdbId: 155, type: 'movie', title: 'The Dark Knight' },
  { tmdbId: 634649, type: 'movie', title: 'Spider-Man NWH' },
];

async function testProvider(name, file) {
  try {
    delete require.cache[require.resolve(file)];
    const mod = require(file);
    if (!mod.getStreams) return { name, status: 'NO_GETSTREAMS', count: 0 };
    
    const streams = await Promise.race([
      mod.getStreams(155, 'movie', null, null),
      new Promise((_, reject) => setTimeout(() => reject(new Error('timeout')), 15000)),
    ]);
    
    const count = Array.isArray(streams) ? streams.length : 0;
    const hasUrl = count > 0 && streams[0].url;
    
    return { name, status: count > 0 ? 'OK' : 'NO_STREAMS', count, hasUrl };
  } catch (e) {
    return { name, status: 'ERROR', error: e.message.substring(0, 60), count: 0 };
  }
}

(async () => {
  const results = [];
  
  for (const repo of repos) {
    const files = fs.readdirSync(repo.dir).filter(f => f.endsWith('.js'));
    console.log(`\n=== ${repo.name} (${files.length} providers) ===`);
    
    for (const file of files) {
      const name = file.replace('.js', '');
      const filepath = path.join(repo.dir, file);
      process.stdout.write(`  Testing ${name}...`);
      const result = await testProvider(name, filepath);
      results.push({ repo: repo.name, ...result });
      
      if (result.status === 'OK') {
        console.log(` \x1b[32mOK\x1b[0m (${result.count} streams)`);
      } else if (result.status === 'NO_STREAMS') {
        console.log(` \x1b[33mNO STREAMS\x1b[0m`);
      } else if (result.status === 'ERROR') {
        console.log(` \x1b[31mERROR\x1b[0m (${result.error})`);
      } else {
        console.log(` \x1b[31mNO getStreams\x1b[0m`);
      }
    }
  }
  
  // Summary
  console.log('\n=== SUMMARY ===');
  const working = results.filter(r => r.status === 'OK');
  const noStreams = results.filter(r => r.status === 'NO_STREAMS');
  const errors = results.filter(r => r.status === 'ERROR');
  
  console.log(`Working: ${working.length} / ${results.length}`);
  console.log(`No streams: ${noStreams.length}`);
  console.log(`Errors: ${errors.length}`);
  
  console.log('\n--- WORKING ---');
  for (const r of working) {
    console.log(`  [${r.repo}] ${r.name}: ${r.count} streams`);
  }
  
  console.log('\n--- NOT WORKING ---');
  for (const r of [...noStreams, ...errors]) {
    console.log(`  [${r.repo}] ${r.name}: ${r.status}${r.error ? ' (' + r.error + ')' : ''}`);
  }
})();
