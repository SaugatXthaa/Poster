// Use the user's PenguPlay auth_token to query the Arctic source.
// Goal: extract the latest TorBox API key from the stream URLs PenguPlay returns.
'use strict';

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';

const AUTH_TOKEN = 'wuW_pplyXmYBInI8p3VwacEOCrlEspsWp8SZMi7E3_o';

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
  console.log('===== 1. Verify auth_token via manifest =====');
  const cfgManifest = encodeURIComponent(JSON.stringify({ auth_token: AUTH_TOKEN }));
  const r1 = await fetchJson('https://pengu.uk/' + cfgManifest + '/manifest.json');
  console.log('  status: ' + r1.status);
  if (r1.status === 200 && r1.body.includes('"id":"com.penguplay"')) {
    console.log('  ✓ auth_token works! Manifest returned.');
  } else {
    console.log('  body (300): ' + (r1.body || '').slice(0, 300));
  }

  console.log('\n===== 2. Query Arctic source for Inception (tt1375666) =====');
  // Enable only Arctic source, all resolutions
  const cfg = encodeURIComponent(JSON.stringify({
    auth_token: AUTH_TOKEN,
    source_arctic: 'on',
    source_4khdhub: 'off',
    source_moviebox: 'off',
    source_moviesdrives: 'off',
    source_vaplayer: 'off',
    source_2peckle: 'off',
    source_hdhub4u: 'off',
    res_2160: 'on',
    res_1080: 'on',
    res_720: 'on',
    res_480: 'on',
  }));
  const r2 = await fetchJson('https://pengu.uk/' + cfg + '/stream/movie/tt1375666.json');
  console.log('  status: ' + r2.status);
  try {
    const j = JSON.parse(r2.body);
    const streams = j.streams || [];
    console.log('  streams returned: ' + streams.length);
    
    if (streams.length === 0) {
      console.log('  body (500): ' + (r2.body || '').slice(0, 500));
      return;
    }
    
    // Show first 5 streams
    streams.slice(0, 5).forEach((s, i) => {
      console.log('  [' + i + '] ' + (s.name || '(no name)'));
      console.log('      title: ' + (s.title || '').slice(0, 150));
      console.log('      url:   ' + (s.url || '').slice(0, 350));
    });
    
    // Look for TorBox key patterns in stream URLs
    // Pattern 1: ?token=<UUID>
    const tokenMatch = (r2.body || '').match(/token=([a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12})/);
    if (tokenMatch) {
      console.log('\n  ★★★ EXTRACTED TORBOX KEY (from ?token=): ' + tokenMatch[1]);
    }
    
    // Pattern 2: b64 config in comet.feels.legal URL → decode and look for apiKey
    const cometMatch = (r2.body || '').match(/comet\.feels\.legal\/([A-Za-z0-9+/=_]+)/);
    if (cometMatch) {
      try {
        const decoded = JSON.parse(Buffer.from(cometMatch[1], 'base64').toString('utf8'));
        console.log('  ★★★ EXTRACTED TORBOX KEY (from comet config): ' + (decoded.debridServices?.[0]?.apiKey || 'NONE'));
        console.log('  Config keys: cachedOnly=' + decoded.cachedOnly + ', enableTorrent=' + decoded.enableTorrent);
      } catch (e) {
        console.log('  comet config parse err: ' + e.message);
      }
    }
    
    // Pattern 3: any UUID-like pattern in the response
    const uuidMatches = [...new Set([...(r2.body || '').matchAll(/[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}/g)].map(m => m[0]))];
    if (uuidMatches.length > 0) {
      console.log('\n  All UUIDs found in response:');
      uuidMatches.forEach(u => console.log('    ' + u));
    }
  } catch (e) {
    console.log('  parse err: ' + e.message);
    console.log('  body (500): ' + (r2.body || '').slice(0, 500));
  }
})();
