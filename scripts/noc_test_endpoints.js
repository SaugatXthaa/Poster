// Test noctra endpoints with valid session token
const crypto = require('crypto').webcrypto;
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';
const Mi = 'https://noctratv.com';

function b64url(buf) {
  return Buffer.from(buf).toString('base64').replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
}

async function getSession() {
  const cRes = await fetch(Mi + '/api/player/v2/challenge', {
    headers: { 'User-Agent': UA, 'Accept': 'application/json', 'Referer': 'https://noctratv.com/', 'Origin': 'https://noctratv.com' }
  });
  const c = await cRes.json();
  const keys = await crypto.subtle.generateKey({ name: 'ECDSA', namedCurve: 'P-256' }, false, ['sign', 'verify']);
  const pubJwk = await crypto.subtle.exportKey('jwk', keys.publicKey);
  const msg = new TextEncoder().encode(c.challengeId + '.' + c.nonce + '.' + c.issuedAt);
  const sig = await crypto.subtle.sign({ name: 'ECDSA', hash: 'SHA-256' }, keys.privateKey, msg);
  const sigB64 = b64url(new Uint8Array(sig));
  const fp = { webdriver: false, languages: ['en-US','en'], plugins: 5, hardwareConcurrency: 8, deviceMemory: 8, maxTouchPoints: 0, platform: 'Linux x86_64', screen: { width: 1920, height: 1080, colorDepth: 24, pixelRatio: 1 }, visibilityState: 'visible' };
  const caps = ['hls','dash','codec-avc','codec-hevc','codec-vp9','codec-av1'];
  const sRes = await fetch(Mi + '/api/player/v2/session', {
    method: 'POST',
    headers: { 'User-Agent': UA, 'Content-Type': 'application/json', 'Accept': 'application/json', 'Origin': 'https://noctratv.com', 'Referer': 'https://noctratv.com/' },
    body: JSON.stringify({
      challengeId: c.challengeId, nonce: c.nonce, publicKey: pubJwk, signature: sigB64,
      fingerprint: fp, embedded: false, embedOrigin: '', sandboxed: false, turnstileToken: '',
      supportsEncryptedTransport: false, supportsEncryptedCapabilities: false, transportPublicKey: null,
      playerVersion: 'mplayer-web-v2', playbackSessionVersion: 2, capabilities: caps
    })
  });
  const s = await sRes.json();
  if (!s.token) throw new Error('Session failed: ' + JSON.stringify(s));
  const cookie = (sRes.headers.get('set-cookie') || '').split(';')[0];
  return { token: s.token, sessionId: s.sessionId, cookie, keys, full: s };
}

async function test(session, label, method, path, body) {
  const headers = {
    'User-Agent': UA,
    'Accept': 'application/json',
    'Origin': 'https://noctratv.com',
    'Referer': 'https://noctratv.com/watch/movie/27205',
    'X-MZone-Playback-Lease': session.token,
    'Cookie': session.cookie
  };
  if (body) headers['Content-Type'] = 'application/json';
  const opts = { method: method || 'GET', headers };
  if (body) opts.body = JSON.stringify(body);
  const r = await fetch(Mi + path, opts);
  const txt = await r.text();
  console.log(`  [${label}] ${method} ${path}`);
  console.log(`    HTTP ${r.status} → ${txt.slice(0, 250)}`);
  try { return JSON.parse(txt); } catch { return null; }
}

(async () => {
  console.log('=== Get session ===');
  const s = await getSession();
  console.log('token:', s.token.slice(0, 60) + '...');
  console.log('tier:', s.full.tier, 'capabilities:', JSON.stringify(s.full.capabilities));
  
  console.log('\n=== Subtitles endpoint (with session) ===');
  await test(s, 'subs-movie-vidapi', 'GET', '/api/subtitles/source?provider=vidapi&type=movie&tmdbId=27205');
  await test(s, 'subs-movie-cinejoy', 'GET', '/api/subtitles/source?provider=cinejoy&type=movie&tmdbId=27205');
  await test(s, 'subs-movie-movienight', 'GET', '/api/subtitles/source?provider=movienight&type=movie&tmdbId=27205');
  await test(s, 'subs-tv-vidapi', 'GET', '/api/subtitles/source?provider=vidapi&type=tv&tmdbId=1396&season=1&episode=1');
  
  console.log('\n=== Try other endpoints ===');
  await test(s, 'providers', 'GET', '/api/player/v4/providers?mediaType=movie&tmdbId=27205');
  await test(s, 'providers-list', 'GET', '/api/player/v4/sources?mediaType=movie&tmdbId=27205');
  await test(s, 'site-location', 'GET', '/api/site/location');
  await test(s, 'runtime', 'GET', '/api/runtime/v3');
  await test(s, 'admin-runtime', 'GET', '/api/admin/ad-manager/runtime');
  
  console.log('\n=== Try POST /api/player/v4/native with various URL formats ===');
  // Try with m-zone.org /p/v4/ URLs
  const urls = [
    'https://m-zone.org/p/v4/movie/27205',
    'https://m-zone.org/p/v4/movies/27205',
    'https://m-zone.org/p/v4/27205',
    'https://m-zone.org/p/v4/movie/27205/play',
    'https://m-zone.org/p/v4/tt1375666',
    'https://api.m-zone.org/p/v4/movie/27205',
  ];
  for (const url of urls) {
    await test(s, 'native', 'POST', '/api/player/v4/native', { url });
  }
})().catch(e => console.log('ERR:', e.message));
