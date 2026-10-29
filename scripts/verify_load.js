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
  const mod = await import('file:///home/z/my-project/download/cinejoy_lib/BOqDcafn.mjs');
  console.log('Loaded OK. C0 available:', typeof mod._c0);
  
  const client = mod._c0('https://api.shegu.st');
  await client.handshake();
  
  // Test movie
  const result = await client.callOnce('/Lisbon/movie?tmdb=155');
  console.log('Movie 155:', result?.data?.stream?.[0]?.playlist || 'FAILED');
  
  // Test TV
  const result2 = await client.callOnce('/Lisbon/series?tmdb=82856&season=1&episode=1');
  console.log('TV 82856 S1E1:', result2?.data?.stream?.[0]?.playlist || 'FAILED');
})();
