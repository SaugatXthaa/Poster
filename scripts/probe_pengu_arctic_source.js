// PenguPlay now has source_arctic as a built-in source.
// Try querying pengu.uk/stream/movie/<imdb>.json with source_arctic=on
// to see what streams it returns and extract the latest TorBox key.
'use strict';

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';

async function fetchJson(url, opts = {}) {
  console.log('  GET ' + url.slice(0, 220) + (url.length > 220 ? '...' : ''));
  try {
    const res = await fetch(url, {
      redirect: 'follow',
      headers: { 'User-Agent': UA, 'Accept': 'application/json', ...(opts.headers || {}) },
      signal: AbortSignal.timeout(opts.timeout || 30000),
    });
    const text = await res.text();
    return { status: res.status, body: text };
  } catch (e) {
    return { status: 0, error: e.message };
  }
}

(async () => {
  console.log('===== 1. PenguPlay with source_arctic=on (no auth) =====');
  const cfg1 = encodeURIComponent(JSON.stringify({
    source_arctic: 'on',
    res_2160: 'on',
    res_1080: 'on',
  }));
  // PenguPlay config-in-path style
  const url1 = 'https://pengu.uk/' + cfg1 + '/stream/movie/tt1375666.json';
  const r1 = await fetchJson(url1);
  console.log('  status: ' + r1.status);
  try {
    const j = JSON.parse(r1.body);
    console.log('  streams: ' + (j.streams || []).length);
    (j.streams || []).slice(0, 3).forEach((s, i) => {
      console.log('    [' + i + '] ' + (s.name || '(no name)'));
      console.log('        title: ' + (s.title || '').slice(0, 150));
      console.log('        url:   ' + (s.url || '').slice(0, 300));
      if (s.behaviorHints) console.log('        hints: ' + JSON.stringify(s.behaviorHints).slice(0, 250));
    });
  } catch (e) {
    console.log('  parse err: ' + e.message);
    console.log('  body (500): ' + (r1.body || '').slice(0, 500));
  }

  console.log('\n===== 2. With all sources disabled except arctic =====');
  const cfg2 = encodeURIComponent(JSON.stringify({
    source_arctic: 'on',
    source_4khdhub: 'off',
    source_moviebox: 'off',
    source_moviesdrives: 'off',
    source_vaplayer: 'off',
    source_2peckle: 'off',
    source_hdhub4u: 'off',
    res_2160: 'on',
    res_1080: 'on',
  }));
  const r2 = await fetchJson('https://pengu.uk/' + cfg2 + '/stream/movie/tt1375666.json');
  console.log('  status: ' + r2.status);
  try {
    const j = JSON.parse(r2.body);
    console.log('  streams: ' + (j.streams || []).length);
    (j.streams || []).slice(0, 5).forEach((s, i) => {
      console.log('    [' + i + '] ' + (s.name || ''));
      console.log('        url: ' + (s.url || '').slice(0, 300));
    });
    // Extract TorBox key from any URL that has ?token=
    const tokenMatch = (r2.body || '').match(/token=([a-f0-9-]{36})/);
    if (tokenMatch) {
      console.log('  ★ Extracted TorBox key from stream URL: ' + tokenMatch[1]);
    }
  } catch (e) {
    console.log('  parse err: ' + e.message);
    console.log('  body (500): ' + (r2.body || '').slice(0, 500));
  }

  console.log('\n===== 3. Probe with auth_token from existing moviebox.js =====');
  // Try the existing auth_token to see if Arctic works with auth
  const cfg3 = encodeURIComponent(JSON.stringify({
    source_arctic: 'on',
    source_4khdhub: 'off',
    source_moviebox: 'off',
    source_moviesdrives: 'off',
    source_vaplayer: 'off',
    source_2peckle: 'off',
    source_hdhub4u: 'off',
    res_2160: 'on',
    res_1080: 'on',
    auth_token: 'XwZg2rLkLlbjXBeDVCyxgfHXjxN1ijLMkUuToW8KaKc',
  }));
  const r3 = await fetchJson('https://pengu.uk/' + cfg3 + '/stream/movie/tt1375666.json');
  console.log('  status: ' + r3.status);
  try {
    const j = JSON.parse(r3.body);
    console.log('  streams: ' + (j.streams || []).length);
    (j.streams || []).slice(0, 5).forEach((s, i) => {
      console.log('    [' + i + '] ' + (s.name || ''));
      console.log('        url: ' + (s.url || '').slice(0, 300));
    });
    // Extract token from any URL
    const tokenMatch = (r3.body || '').match(/token=([a-f0-9-]{36})/);
    if (tokenMatch) {
      console.log('  ★ Extracted TorBox key from stream URL: ' + tokenMatch[1]);
    } else {
      console.log('  (no ?token= in any stream URL)');
    }
  } catch (e) {
    console.log('  parse err: ' + e.message);
    console.log('  body (500): ' + (r3.body || '').slice(0, 500));
  }
})();
