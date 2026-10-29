// Try various PenguPlay config combinations to find a working one for moviesbox.
// The existing moviebox.js auth_token may be stale — let's also try different
// config flags to see what works.
'use strict';

const UA = 'Mozilla/5.0 (Leadtin Far/1.5; Stremio; +https://stremio.com)';

async function fetchJson(url) {
  console.log('  GET ' + url.slice(0, 200) + (url.length > 200 ? '...' : ''));
  try {
    const res = await fetch(url, {
      redirect: 'follow',
      headers: {
        'User-Agent': UA,
        'Accept': 'application/json',
        'Accept-Language': 'en-US,en;q=0.9',
        'Sec-Fetch-Dest': 'empty',
        'Sec-Fetch-Mode': 'cors',
        'Sec-Fetch-Site': 'cross-site',
      },
      signal: AbortSignal.timeout(30000),
    });
    const text = await res.text();
    return { status: res.status, body: text };
  } catch (e) {
    return { status: 0, error: e.message };
  }
}

(async () => {
  // Config variations to try
  const configs = [
    // Original config from existing moviebox.js
    '{"source_moviebox":"on","res_1080":"on","disable_direct":"on","auth_token":"XwZg2rLkLlbjXBeDVCyxgfHXjxN1ijLMkUuToW8KaKc"}',
    // With direct streams enabled
    '{"source_moviebox":"on","res_1080":"on","res_2160":"on","disable_direct":"off","auth_token":"XwZg2rLkLlbjXBeDVCyxgfHXjxN1ijLMkUuToW8KaKc"}',
    // All sources
    '{"source_moviebox":"on","res_1080":"on","res_2160":"on","res_720":"on","disable_direct":"off","auth_token":"XwZg2rLkLlbjXBeDVCyxgfHXjxN1ijLMkUuToW8KaKc"}',
    // No auth_token (will it 401?)
    '{"source_moviebox":"on","res_1080":"on"}',
  ];

  const imdbId = 'tt1375666'; // Inception
  for (let i = 0; i < configs.length; i++) {
    const cfg = configs[i];
    const cfgUrl = encodeURIComponent(cfg).replace(/%2C/g, ',').replace(/%22/g, '%22').replace(/%3A/g, ':');
    // Actually just URL-encode the JSON properly:
    const cfgUrlEnc = cfg.replace(/"/g, '%22').replace(/,/g, '%2C').replace(/:/g, '%3A').replace(/\{/g, '%7B').replace(/\}/g, '%7D');
    const url = 'https://pengu.uk/' + cfgUrlEnc + '/stream/movie/' + imdbId + '.json';
    console.log('\n--- Config #' + i + ': ' + cfg.slice(0, 80) + '... ---');
    const r = await fetchJson(url);
    console.log('  status: ' + r.status);
    if (r.body) {
      try {
        const j = JSON.parse(r.body);
        const streams = j.streams || [];
        console.log('  streams: ' + streams.length);
        streams.slice(0, 3).forEach((s, idx) => {
          console.log('    [' + idx + '] ' + (s.name || '(no name)'));
          console.log('        title: ' + (s.title || '').slice(0, 150));
          console.log('        url:   ' + (s.url || '').slice(0, 250));
          if (s.behaviorHints) console.log('        hints: ' + JSON.stringify(s.behaviorHints).slice(0, 250));
        });
        if (streams.length > 0) {
          console.log('  ✓✓✓ Working config found!');
          break;
        }
      } catch (e) {
        console.log('  parse error: ' + e.message);
        console.log('  raw body: ' + r.body.slice(0, 600));
      }
    } else {
      console.log('  error: ' + r.error);
    }
    // Wait between requests to avoid 429
    if (i < configs.length - 1) {
      console.log('  (waiting 5s to avoid 429...)');
      await new Promise(r => setTimeout(r, 5000));
    }
  }
})();
