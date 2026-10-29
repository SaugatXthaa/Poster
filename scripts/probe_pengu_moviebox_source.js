// Probe PenguPlay's MovieBox source with the user's auth_token.
// MovieBox reportedly supports sub/dub audios for movies/series/animes.
'use strict';

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';
const AUTH_TOKEN = 'wuW_pplyXmYBInI8p3VwacEOCrlEspsWp8SZMi7E3_o';

async function fetchJson(url) {
  console.log('  GET ' + url.slice(0, 220) + (url.length > 220 ? '...' : ''));
  try {
    const res = await fetch(url, {
      headers: { 'User-Agent': UA, 'Accept': 'application/json' },
      signal: AbortSignal.timeout(30000),
    });
    const text = await res.text();
    return { status: res.status, body: text };
  } catch (e) {
    return { status: 0, error: e.message };
  }
}

(async () => {
  // Build config with ONLY source_moviebox enabled, all resolutions
  const cfg = encodeURIComponent(JSON.stringify({
    auth_token: AUTH_TOKEN,
    source_moviebox: 'on',
    source_arctic: 'off',
    source_4khdhub: 'off',
    source_moviesdrives: 'off',
    source_vaplayer: 'off',
    source_2peckle: 'off',
    source_hdhub4u: 'off',
    res_2160: 'on',
    res_1080: 'on',
    res_720: 'on',
    res_480: 'on',
    res_360: 'on',
  }));
  
  // Test movies, series, and anime
  const tests = [
    { id: 'tt1375666', type: 'movie', label: 'Inception (movie)' },
    { id: 'tt0111161', type: 'movie', label: 'Shawshank Redemption' },
    { id: 'tt0903747', type: 'series', season: 1, episode: 1, label: 'Breaking Bad S01E01' },
    { id: 'tt0944947', type: 'series', season: 1, episode: 1, label: 'GoT S01E01' },
    { id: 'tt0245429', type: 'movie', label: 'Spirited Away (anime)' },
    { id: 'tt2948372', type: 'movie', label: 'Your Name (anime)' },
    { id: 'tt10872600', type: 'series', season: 1, episode: 1, label: 'Squid Game (kdrama)' },
  ];
  
  for (const t of tests) {
    console.log('\n===== ' + t.label + ' =====');
    const path = t.type === 'movie'
      ? '/stream/movie/' + t.id + '.json'
      : '/stream/series/' + t.id + ':' + t.season + ':' + t.episode + '.json';
    const url = 'https://pengu.uk/' + cfg + path;
    const r = await fetchJson(url);
    console.log('  status: ' + r.status);
    if (r.error) { console.log('  error: ' + r.error); continue; }
    try {
      const j = JSON.parse(r.body);
      const streams = j.streams || [];
      console.log('  streams: ' + streams.length);
      // Show first 5 streams with their names + URLs
      streams.slice(0, 5).forEach((s, i) => {
        console.log('  [' + i + '] ' + (s.name || '(no name)'));
        console.log('      title: ' + (s.title || '').slice(0, 150));
        console.log('      url:   ' + (s.url || '').slice(0, 250));
      });
      // Also count by quality
      const byQ = {};
      streams.forEach(s => {
        const m = (s.name || '').match(/(\d{3,4}[pP]|4K|2160|1080|720|480|360)/);
        const q = m ? m[0].toLowerCase() : '?';
        byQ[q] = (byQ[q] || 0) + 1;
      });
      console.log('  by quality: ' + JSON.stringify(byQ));
      // Count by language
      const byLang = {};
      streams.forEach(s => {
        const name = (s.name || '') + ' ' + (s.title || '');
        const langs = ['hindi','tamil','telugu','malayalam','korean','japanese','english','chinese','spanish','french','german','italian'];
        langs.forEach(l => {
          if (name.toLowerCase().includes(l)) {
            byLang[l] = (byLang[l] || 0) + 1;
          }
        });
      });
      console.log('  by language: ' + JSON.stringify(byLang));
    } catch (e) {
      console.log('  parse err: ' + e.message);
      console.log('  body (300): ' + (r.body || '').slice(0, 300));
    }
    // Small delay to avoid rate limit
    await new Promise(r => setTimeout(r, 1500));
  }
})();
