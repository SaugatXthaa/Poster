// Test various /p/v4/ URL formats against noctra's /api/player/v4/native endpoint
// Uses the session flow we already proved works.

const crypto = require('crypto').webcrypto;
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';
const Mi = 'https://noctratv.com';

function b64url(buf) {
  return Buffer.from(buf).toString('base64').replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
}

async function getSession() {
  // 1. Get challenge
  const cRes = await fetch(Mi + '/api/player/v2/challenge', {
    headers: { 'User-Agent': UA, 'Accept': 'application/json', 'Referer': 'https://noctratv.com/', 'Origin': 'https://noctratv.com' }
  });
  const c = await cRes.json();
  console.log('challenge:', c.challengeId?.slice(0, 12), '...');
  
  // 2. Generate ECDSA P-256 keypair (cached across calls in real provider)
  const keys = await crypto.subtle.generateKey({ name: 'ECDSA', namedCurve: 'P-256' }, false, ['sign', 'verify']);
  const pubJwk = await crypto.subtle.exportKey('jwk', keys.publicKey);
  
  // 3. Sign challengeId.nonce.issuedAt
  const msg = new TextEncoder().encode(c.challengeId + '.' + c.nonce + '.' + c.issuedAt);
  const sig = await crypto.subtle.sign({ name: 'ECDSA', hash: 'SHA-256' }, keys.privateKey, msg);
  const sigB64 = b64url(new Uint8Array(sig));
  
  // 4. POST /api/player/v2/session
  const fp = {
    webdriver: false,
    languages: ['en-US', 'en'],
    plugins: 5,
    hardwareConcurrency: 8,
    deviceMemory: 8,
    maxTouchPoints: 0,
    platform: 'Linux x86_64',
    screen: { width: 1920, height: 1080, colorDepth: 24, pixelRatio: 1 },
    visibilityState: 'visible'
  };
  const caps = ['hls', 'dash', 'codec-avc', 'codec-hevc', 'codec-vp9', 'codec-av1'];
  
  const sRes = await fetch(Mi + '/api/player/v2/session', {
    method: 'POST',
    headers: {
      'User-Agent': UA, 'Content-Type': 'application/json', 'Accept': 'application/json',
      'Origin': 'https://noctratv.com', 'Referer': 'https://noctratv.com/'
    },
    body: JSON.stringify({
      challengeId: c.challengeId, nonce: c.nonce, publicKey: pubJwk, signature: sigB64,
      fingerprint: fp, embedded: false, embedOrigin: '', sandboxed: false, turnstileToken: '',
      supportsEncryptedTransport: false, supportsEncryptedCapabilities: false, transportPublicKey: null,
      playerVersion: 'mplayer-web-v2', playbackSessionVersion: 2, capabilities: caps
    })
  });
  const s = await sRes.json();
  if (!s.token) throw new Error('Session failed: ' + JSON.stringify(s));
  
  const setCookie = sRes.headers.get('set-cookie') || '';
  const cookie = setCookie.split(';')[0];
  console.log('session token:', s.token.slice(0, 30) + '...', 'cookie:', cookie.slice(0, 50) + '...');
  
  return { token: s.token, sessionId: s.sessionId, cookie, keys, pubJwk };
}

async function tryNative(session, urlPath) {
  const fullUrl = Mi + urlPath;
  console.log('  POST /api/player/v4/native  url=' + urlPath);
  
  // Build proof headers (capabilityVersion 4)
  // proof format: "mzone-proof-v4\n<sessionId>\n<POST>\n<pathname+search>\n<sha256_of_body>\n<timestamp>\n<nonce>"
  const bodyStr = JSON.stringify({ url: fullUrl });
  const bodyBytes = new TextEncoder().encode(bodyStr);
  const bodyHash = await crypto.subtle.digest('SHA-256', bodyBytes);
  const bodyHashB64 = b64url(new Uint8Array(bodyHash));
  const ts = Date.now();
  const nonce = new Uint8Array(18);
  crypto.getRandomValues(nonce);
  const nonceB64 = b64url(nonce.buffer);
  const u = new URL(fullUrl);
  const pathQuery = u.pathname + u.search;
  const proofMsg = ['mzone-proof-v4', session.sessionId, 'POST', pathQuery, bodyHashB64, String(ts), nonceB64].join('\n');
  const proofSig = await crypto.subtle.sign({ name: 'ECDSA', hash: 'SHA-256' }, session.keys.privateKey, new TextEncoder().encode(proofMsg));
  const proofB64 = b64url(new Uint8Array(proofSig));
  
  const nRes = await fetch(Mi + '/api/player/v4/native', {
    method: 'POST',
    headers: {
      'User-Agent': UA, 'Content-Type': 'application/json', 'Accept': 'application/json',
      'Origin': 'https://noctratv.com', 'Referer': 'https://noctratv.com/watch/movie/27205',
      'X-MZone-Playback-Lease': session.token,
      'X-MZone-Proof-Version': '4',
      'X-MZone-Proof-Timestamp': String(ts),
      'X-MZone-Proof-Nonce': nonceB64,
      'X-MZone-Proof': proofB64,
      'Cookie': session.cookie
    },
    body: bodyStr
  });
  const n = await nRes.json();
  console.log('  HTTP', nRes.status, '→', JSON.stringify(n).slice(0, 300));
  return n;
}

(async () => {
  const session = await getSession();
  console.log('\n=== Try various /p/v4/ URL formats for Inception (TMDB 27205) ===');
  const candidates = [
    '/p/v4/movie/27205',
    '/p/v4/movie/tt1375666',
    '/p/v4/movies/27205',
    '/p/v4/m/27205',
    '/p/v4/27205',
    '/p/v4/movie/27205/0/0',
    '/p/v4/movie/27205?season=0&episode=0',
    '/p/v4/movie/27205/play',
    '/p/v4/tv/1396/1/1',
    '/p/v4/tv/1396',
    '/p/v4/series/1396/1/1',
    '/p/v4/episode/1396/1/1',
    '/p/v4/play/movie/27205',
    '/p/v4/watch/movie/27205',
    '/p/v4/source/movie/27205',
    '/p/v4/stream/movie/27205',
  ];
  for (const path of candidates) {
    const r = await tryNative(session, path);
    if (r && r.success && r.url) {
      console.log('  *** SUCCESS: ' + r.url.slice(0, 200));
      break;
    }
  }
})().catch(e => console.log('ERR:', e.message));
