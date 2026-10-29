// Verify AI-upscale penalty is split correctly:
//   • Animation (anime / cartoon) → -1500 (upscales still allowed near top)
//   • Live-action (movies / series / kdramas) → -5000 (upscales buried below top 50)
'use strict';

const path = require('path');

(async () => {
  const provider = require(path.resolve(__dirname, '../allinone-nuvio-repo/providers/comet.js'));

  const tests = [
    // --- Animation (should KEEP AI upscales, penalty = 1500) ---
    { id: 'tt0245429', type: 'movie', label: 'Spirited Away (anime)' },
    { id: 'tt0266543', type: 'movie', label: 'Finding Nemo (cartoon)' },
    { id: 'tt0317705', type: 'movie', label: 'The Incredibles (Pixar)' },
    { id: 'tt2948372', type: 'movie', label: 'Your Name (Kimi no Na wa) — anime' },
    // --- Live-action movies/series (should BURY AI upscales, penalty = 5000) ---
    { id: 'tt1375666', type: 'movie', label: 'Inception (live-action movie)' },
    { id: 'tt0903747', type: 'series', season: 1, episode: 1, label: 'Breaking Bad S01E01 (live-action series)' },
    { id: 'tt0944947', type: 'series', season: 1, episode: 1, label: 'GoT S01E01 (live-action series)' },
    // --- Kdrama (should BURY AI upscales, penalty = 5000) ---
    { id: 'tt7285546', type: 'series', season: 1, episode: 1, label: 'Crash Landing on You S01E01 (kdrama)' },
    { id: 'tt10872600', type: 'series', season: 1, episode: 1, label: 'Squid Game S01E01 (kdrama)' },
  ];

  console.log('================================================================================');
  console.log('  ANIMATION vs LIVE-ACTION: AI-UPSCALE PENALTY SPLIT TEST');
  console.log('================================================================================');
  console.log('');

  for (const t of tests) {
    console.log('\n--- ' + t.label + ' ---');
    console.log('    query: ' + t.id + ' ' + t.type +
      (t.season ? ' S' + t.season + 'E' + t.episode : ''));
    const streams = await provider.getStreams(t.id, t.type, t.season, t.episode);
    if (streams.length === 0) {
      console.log('    (no streams — skipping)');
      continue;
    }
    // Find the highest-ranked AI-upscale stream and the highest-ranked non-upscale stream
    let topUpscale = null, topNonUpscale = null;
    for (const s of streams) {
      const fn = (s.behaviorHints && s.behaviorHints.filename) || s.title || s.name || '';
      const lf = fn.toLowerCase();
      const isUpscale = /\bupscale\b|\bupscaled\b|ai[-_]upscale/.test(lf) ||
        lf.includes('upscale') || lf.includes('upscaled');
      if (isUpscale && !topUpscale) topUpscale = { idx: streams.indexOf(s), name: s.name, filename: fn };
      if (!isUpscale && !topNonUpscale) topNonUpscale = { idx: streams.indexOf(s), name: s.name, filename: fn };
    }
    console.log('    total streams: ' + streams.length);
    console.log('    top stream:    ' + streams[0].name);
    console.log('    top filename:  ' + ((streams[0].behaviorHints || {}).filename || streams[0].title || '').slice(0, 100));
    if (topUpscale) {
      console.log('    first upscale in list: position #' + topUpscale.idx + ' / ' + streams.length);
      console.log('      name: ' + topUpscale.name);
      console.log('      file: ' + topUpscale.filename.slice(0, 100));
    } else {
      console.log('    no AI upscales in returned streams');
    }
    if (topNonUpscale) {
      console.log('    first non-upscale in list: position #' + topNonUpscale.idx + ' / ' + streams.length);
    }
  }
})();
