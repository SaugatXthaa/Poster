// Check pengu.uk status with multiple endpoints + see if any return a working key.
'use strict';

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';

async function fetchOnce(url, opts = {}) {
  try {
    const res = await fetch(url, {
      redirect: 'manual',
      method: opts.method || 'GET',
      headers: { 'User-Agent': UA, 'Accept': '*/*', ...(opts.headers || {}) },
      signal: AbortSignal.timeout(opts.timeout || 15000),
    });
    const headers = {};
    res.headers.forEach((v, k) => headers[k] = v);
    return { status: res.status, headers, body: opts.includeBody ? await res.text() : '' };
  } catch (e) {
    return { status: 0, error: e.message };
  }
}

(async () => {
  console.log('===== 1. Check pengu.uk general health =====');
  for (const p of ['/', '/manifest.json', '/direct/experimental/test', '/direct/external/test', '/health']) {
    const r = await fetchOnce('https://pengu.uk' + p);
    console.log('  ' + p + ' → ' + r.status + ' ' + (r.error || ''));
  }

  console.log('\n===== 2. Try /direct/experimental/ with different payloads =====');
  // Different movie IDs
  const ids = ['tt37287335', 'tt1375666', 'tt0111161', 'tt0468569'];
  for (const id of ids) {
    const payload = Buffer.from(JSON.stringify({
      t: 'movie', i: id, s: null, e: null,
      h: '18d8295b3a394a51e6e7bed6b3fb421d15d0c5ff', f: 0
    })).toString('base64url');
    const url = 'https://pengu.uk/direct/experimental/' + payload + '?test.mkv';
    const r = await fetchOnce(url);
    console.log('  ' + id + ' → ' + r.status + ' loc: ' + (r.headers.location || '(none)').slice(0, 150));
    if (r.headers.location && r.headers.location.includes('comet.feels.legal')) {
      // Extract the key
      const m = r.headers.location.match(/apiKey%22%3A%22([a-f0-9-]+)%22/);
      if (m) {
        const key = m[1].replace(/%22/g, '');
        console.log('    ★ Found key in redirect: ' + key);
        // Decode the b64 config to be sure
        const cfg = r.headers.location.match(/comet\.feels\.legal\/([A-Za-z0-9+/=_]+)/);
        if (cfg) {
          try {
            const decoded = JSON.parse(Buffer.from(cfg[1], 'base64').toString('utf8'));
            console.log('    Decoded apiKey: ' + (decoded.debridServices?.[0]?.apiKey || 'NOT FOUND'));
          } catch (e) {}
        }
        break;
      }
    }
    await new Promise(r => setTimeout(r, 2000));
  }

  console.log('\n===== 3. Check Comet itself with the current key (does Comet still respond?) =====');
  // Test with current key b9ffa8fd-...
  const testKey = 'b9ffa8fd-5ece-40b4-8348-f36272056f1f';
  const config = {
    maxResultsPerResolution: 0, maxSize: 0, cachedOnly: true,
    sortCachedUncachedTogether: false, removeTrash: true, resultFormat: ['all'],
    enableTorrent: false, deduplicateStreams: true, scrapeDebridAccountTorrents: false,
    debridStreamProxyPassword: '',
    languages: { required: [], allowed: [], exclude: [], preferred: [] },
    resolutions: {},
    options: { remove_ranks_under: -10000000000, allow_english_in_languages: false, remove_unknown_languages: false },
    debridServices: [{ service: 'torbox', apiKey: testKey }],
  };
  const cfgB64 = Buffer.from(JSON.stringify(config)).toString('base64');
  const r3 = await fetchOnce('https://comet.feels.legal/' + cfgB64 + '/stream/movie/tt1375666.json', { includeBody: true });
  console.log('  status: ' + r3.status + ' body length: ' + (r3.body || '').length);
  try {
    const j = JSON.parse(r3.body);
    console.log('  streams count: ' + (j.streams || []).length);
    if (j.streams && j.streams.length > 0) {
      console.log('  first stream: ' + (j.streams[0].name || ''));
      console.log('  first url:    ' + (j.streams[0].url || '').slice(0, 200));
    }
  } catch (e) {
    console.log('  parse error: ' + e.message);
  }
})();
