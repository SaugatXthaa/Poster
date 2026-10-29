// Probe various PenguPlay sources to find the best complement to MovieBox.
// Specifically look at KissKH (asian dramas), Anikoto (anime subs),
// Miruro (anime), VidFast, Cinejoy.
'use strict';

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';
const AUTH_TOKEN = 'wuW_pplyXmYBInI8p3VwacEOCrlEspsWp8SZMi7E3_o';

async function fetchJson(url) {
  try {
    const res = await fetch(url, {
      headers: { 'User-Agent': UA, 'Accept': 'application/json' },
      signal: AbortSignal.timeout(30000),
    });
    return { status: res.status, body: await res.text() };
  } catch (e) { return { status: 0, error: e.message }; }
}

async function probeSource(sourceKey, sourceName, tests) {
  console.log('\n===== ' + sourceName + ' (' + sourceKey + ') =====');
  for (const t of tests) {
    const cfg = encodeURIComponent(JSON.stringify({
      auth_token: AUTH_TOKEN,
      [sourceKey]: 'on',
      // Disable all other sources
      source_arctic: 'off', source_moviebox: 'off', source_4khdhub: 'off',
      source_moviesdrives: 'off', source_vaplayer: 'off', source_2peckle: 'off',
      source_hdhub4u: 'off', source_vegamovies: 'off', source_miruro: 'off',
      source_vidlink: 'off', source_vidfast: 'off', source_cinefreak: 'off',
      source_cinejoy: 'off', source_kisskh: 'off', source_anikoto: 'off',
      res_2160: 'on', res_1080: 'on', res_720: 'on', res_480: 'on',
    }));
    const path = t.type === 'movie'
      ? '/stream/movie/' + t.id + '.json'
      : '/stream/series/' + t.id + ':' + t.season + ':' + t.episode + '.json';
    const url = 'https://pengu.uk/' + cfg + path;
    const r = await fetchJson(url);
    let count = 0;
    try {
      const j = JSON.parse(r.body);
      count = (j.streams || []).filter(s => s.url && s.url.length > 30).length;
    } catch (e) {}
    console.log('  ' + t.label + ': ' + count + ' streams (status ' + r.status + ')');
    await new Promise(r => setTimeout(r, 1200));
  }
}

(async () => {
  const tests = [
    { id: 'tt1375666', type: 'movie', label: 'Inception' },
    { id: 'tt0245429', type: 'movie', label: 'Spirited Away' },
    { id: 'tt10872600', type: 'series', season: 1, episode: 1, label: 'Squid Game (kdrama)' },
    { id: 'tt7285546', type: 'series', season: 1, episode: 1, label: 'Crash Landing (kdrama)' },
    { id: 'tt0903747', type: 'series', season: 1, episode: 1, label: 'Breaking Bad' },
  ];
  
  // Test each source individually
  await probeSource('source_kisskh', 'KissKH', tests);
  await probeSource('source_anikoto', 'Anikoto', tests);
  await probeSource('source_miruro', 'Miruro', tests);
  await probeSource('source_cinejoy', 'Cinejoy', tests);
  await probeSource('source_vidfast', 'VidFast', tests);
  await probeSource('source_2peckle', '2Peckle', tests);
})();
