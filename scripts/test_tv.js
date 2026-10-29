if (!globalThis.crypto) { const { webcrypto } = require('crypto'); globalThis.crypto = webcrypto; }
globalThis.localStorage = { getItem: () => null, setItem: () => {}, removeItem: () => {}, clear: () => {}, key: () => null, length: 0 };
globalThis.document = { createElement: () => ({style:{},getContext:()=>null}), addEventListener:()=>{}, hidden:false, visibilityState:'visible', hasFocus:()=>true, getElementById:()=>null, querySelector:()=>null, querySelectorAll:()=>[] };
globalThis.window = globalThis;
globalThis.window.__svelte = { v: new Set() };
globalThis.window.addEventListener = () => {};
globalThis.window.removeEventListener = () => {};
globalThis.navigator = { userAgent: 'Mozilla/5.0', language: 'en-US', languages: ['en-US'], platform: 'Linux x86_64', hardwareConcurrency: 4, deviceMemory: 8, maxTouchPoints: 0, webdriver: false };
globalThis.location = { href: 'https://cinejoy.to/' };

(async () => {
  const mod = await import('file:///tmp/cinejoy_mod/BOqDcafn_patched.js');
  const client = mod._c0('https://api.shegu.st');
  await client.handshake();
  
  // Try different TV path formats
  const paths = [
    '/Lisbon/tv?tmdb=82856&season=1&episode=1',
    '/Lisbon/tv?tmdb=82856&s=1&e=1',
    '/Lisbon/tv?tmdb=82856&season=1&episode=1&embed=true',
    '/Lisbon/show?tmdb=82856&season=1&episode=1',
    '/Lisbon/series?tmdb=82856&season=1&episode=1',
  ];
  
  for (const path of paths) {
    try {
      const result = await client.callOnce(path);
      console.log(path, '=>', JSON.stringify(result).substring(0, 200));
    } catch(e) {
      console.log(path, '=> ERROR:', e.message.substring(0, 100));
    }
  }
  
  // Also try movie with different IDs
  console.log('\n--- Movie tests ---');
  for (const tmdb of [155, 634649, 27205]) {
    try {
      const result = await client.callOnce('/Lisbon/movie?tmdb=' + tmdb);
      const playlist = result?.data?.stream?.[0]?.playlist;
      console.log('Movie', tmdb, '=>', playlist ? playlist.substring(0, 80) : JSON.stringify(result).substring(0, 100));
    } catch(e) {
      console.log('Movie', tmdb, '=> ERROR:', e.message.substring(0, 100));
    }
  }
})();
