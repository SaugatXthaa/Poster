// Test /api/player/v4/native with minimal headers (no v4 proof)
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
  console.log('Full session response keys:', Object.keys(s));
  console.log('session.token (first 80):', s.token.slice(0, 80));
  console.log('session.sessionId:', s.sessionId);
  console.log('session.expiresAt:', s.expiresAt);
  console.log('session.mode:', s.mode);
  console.log('session.risk:', s.risk);
  console.log('session.transport:', s.transport);
  console.log('session.capabilities:', s.capabilities);
  // Decode the JWT-like token
  try {
    const parts = s.token.split('.');
    if (parts.length >= 2) {
      const payload = JSON.parse(Buffer.from(parts[0], 'base64').toString());
      console.log('token payload:', JSON.stringify(payload));
    }
  } catch(e) {}
  const cookie = (sRes.headers.get('set-cookie') || '').split(';')[0];
  return { token: s.token, sessionId: s.sessionId, cookie, keys, full: s };
}

async function tryNative(session, urlPath, useProof=false) {
  const fullUrl = Mi + urlPath;
  const bodyStr = JSON.stringify({ url: fullUrl });
  const headers = {
    'User-Agent': UA,
    'Content-Type': 'application/json',
    'Accept': 'application/json',
    'Origin': 'https://noctratv.com',
    'Referer': 'https://noctratv.com/watch/movie/27205',
    'X-MZone-Playback-Lease': session.token,
    'Cookie': session.cookie
  };
  
  if (useProof) {
    const bodyBytes = new TextEncoder().encode(bodyStr);
    const bodyHash = await crypto.subtle.digest('SHA-256', bodyBytes);
    const bodyHashB64 = b64url(new Uint8Array(bodyHash));
    const ts = Date.now();
    const nonceArr = new Uint8Array(18);
    crypto.getRandomValues(nonceArr);
    const nonceB64 = b64url(nonceArr.buffer);
    const u = new URL(fullUrl);
    const pathQuery = u.pathname + u.search;
    const proofMsg = ['mzone-proof-v4', session.sessionId, 'POST', pathQuery, bodyHashB64, String(ts), nonceB64].join('\n');
    const proofSig = await crypto.subtle.sign({ name: 'ECDSA', hash: 'SHA-256' }, session.keys.privateKey, new TextEncoder().encode(proofMsg));
    const proofB64 = b64url(new Uint8Array(proofSig));
    headers['X-MZone-Proof-Version'] = '4';
    headers['X-MZone-Proof-Timestamp'] = String(ts);
    headers['X-MZone-Proof-Nonce'] = nonceB64;
    headers['X-MZone-Proof'] = proofB64;
  }
  
  const nRes = await fetch(Mi + '/api/player/v4/native', {
    method: 'POST', headers, body: bodyStr
  });
  const txt = await nRes.text();
  console.log(`  [${useProof ? 'PROOF' : 'BASIC'}] ${urlPath} → HTTP ${nRes.status}: ${txt.slice(0, 300)}`);
  try { return JSON.parse(txt); } catch { return null; }
}

(async () => {
  const session = await getSession();
  console.log('\n=== Try with BASIC auth (no proof) ===');
  for (const p of ['/p/v4/movie/27205', '/p/v4/tv/1396/1/1', '/p/v4/movie/tt1375666', '/p/v4/movie/129']) {
    const r = await tryNative(session, p, false);
    if (r && r.success && r.url) { console.log('  *** SUCCESS:', r.url); break; }
  }
})().catch(e => console.log('ERR:', e.message));
