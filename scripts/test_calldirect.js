if (!globalThis.crypto) { const { webcrypto } = require('crypto'); globalThis.crypto = webcrypto; }
globalThis.localStorage = { getItem: () => null, setItem: () => {}, removeItem: () => {}, clear: () => {}, key: () => null, length: 0 };
globalThis.document = { 
  createElement: () => ({ style: {}, getContext: () => null, setAttribute: () => {}, appendChild: () => {}, toDataURL: () => '' }),
  getElementById: () => null, querySelector: () => null, querySelectorAll: () => [],
  addEventListener: () => {}, removeEventListener: () => {},
  hidden: false, visibilityState: 'visible', hasFocus: () => true,
};
globalThis.window = globalThis;
globalThis.window.__svelte = { v: new Set() };
globalThis.window.addEventListener = () => {};
globalThis.window.removeEventListener = () => {};
globalThis.navigator = { 
  userAgent: 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36',
  language: 'en-US', languages: ['en-US'], platform: 'Linux x86_64',
  hardwareConcurrency: 4, deviceMemory: 8, maxTouchPoints: 0, webdriver: false,
};
globalThis.location = { href: 'https://cinejoy.to/', hostname: 'cinejoy.to', origin: 'https://cinejoy.to' };

(async () => {
  const mod = await import('file:///tmp/cinejoy_mod/BOqDcafn_patched.js');
  
  // Get the client
  const client = mod._c0('https://api.shegu.st');
  console.log('Client origin:', client.origin);
  
  // Do the handshake
  console.log('Performing handshake...');
  try {
    await client.handshake();
    console.log('Handshake done! Session:', client.session ? 'set' : 'null');
    if (client.session) {
      console.log('Session ID:', client.session.id);
      console.log('Session keys:', Object.keys(client.session));
    }
  } catch(e) {
    console.log('Handshake error:', e.message.substring(0, 300));
  }
  
  // Now call the API to get stream URL
  // From the code: hx builds path = "/" + provider + "/" + endpoint + "?" + query
  // For movie: "/Lisbon/movie?tmdb=155&embed=true"
  // callOnce(path, payload) - but looking at hx, it's either:
  //   e.callOnce(d ? W+"?"+d : W)  -- GET-style (path includes query)
  //   e.callOnce(W, Object.fromEntries(x.entries()))  -- POST-style (path + payload)
  //
  // The g0 flag determines which mode. Let me try both
  
  console.log('\nCalling callOnce for movie 155...');
  try {
    // Try GET-style first (path includes query string)
    const path = '/Lisbon/movie?tmdb=155&embed=true';
    const result = await client.callOnce(path);
    console.log('callOnce result:', JSON.stringify(result).substring(0, 500));
  } catch(e) {
    console.log('callOnce GET-style error:', e.message.substring(0, 300));
    // Try POST-style
    try {
      const result = await client.callOnce('/Lisbon/movie', { tmdb: '155', embed: 'true' });
      console.log('callOnce POST-style result:', JSON.stringify(result).substring(0, 500));
    } catch(e2) {
      console.log('callOnce POST-style error:', e2.message.substring(0, 300));
    }
  }
})();
