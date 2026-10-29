// Use the hardcoded auth_token from the existing moviebox.js to call
// pengu.uk directly. We'll see if it returns streams AND what backend
// URLs are inside the streams (which reveals the real MovieBox backend).
'use strict';

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';

// From the existing moviebox.js — the URL-encoded JSON config that includes auth_token
const PENGU_CONFIG_URL = 'https://pengu.uk/%7B%22source_moviebox%22%3A%22on%22%2C%22res_1080%22%3A%22on%22%2C%22disable_direct%22%3A%22on%22%2C%22auth_token%22%3A%22XwZg2rLkLlbjXBeDVCyxgfHXjxN1ijLMkUuToW8KaKc%22%7D';

async function fetchJson(url, opts = {}) {
  console.log('  GET ' + url.slice(0, 200) + (url.length > 200 ? '...' : ''));
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
  // Movie: Inception (tt1375666)
  console.log('\n===== Inception (movie) =====');
  const r1 = await fetchJson(PENGU_CONFIG_URL + '/stream/movie/tt1375666.json');
  console.log('  status: ' + r1.status);
  if (r1.body) {
    try {
      const j = JSON.parse(r1.body);
      console.log('  streams count: ' + (j.streams || []).length);
      // Show first 5 streams to see the URLs
      (j.streams || []).slice(0, 5).forEach((s, i) => {
        console.log('  [' + i + '] ' + (s.name || '(no name)'));
        console.log('      title: ' + (s.title || '').slice(0, 150));
        console.log('      url:   ' + (s.url || '').slice(0, 200));
        if (s.behaviorHints) console.log('      hints: ' + JSON.stringify(s.behaviorHints).slice(0, 250));
      });
    } catch (e) {
      console.log('  parse error: ' + e.message);
      console.log('  raw body: ' + r1.body.slice(0, 1000));
    }
  } else {
    console.log('  error: ' + r1.error);
  }

  // TV: Breaking Bad S01E01 (tt0903747)
  console.log('\n===== Breaking Bad S01E01 (series) =====');
  const r2 = await fetchJson(PENGU_CONFIG_URL + '/stream/series/tt0903747:1:1.json');
  console.log('  status: ' + r2.status);
  if (r2.body) {
    try {
      const j = JSON.parse(r2.body);
      console.log('  streams count: ' + (j.streams || []).length);
      (j.streams || []).slice(0, 5).forEach((s, i) => {
        console.log('  [' + i + '] ' + (s.name || '(no name)'));
        console.log('      title: ' + (s.title || '').slice(0, 150));
        console.log('      url:   ' + (s.url || '').slice(0, 200));
        if (s.behaviorHints) console.log('      hints: ' + JSON.stringify(s.behaviorHints).slice(0, 250));
      });
    } catch (e) {
      console.log('  parse error: ' + e.message);
      console.log('  raw body: ' + r2.body.slice(0, 1000));
    }
  }
})();
