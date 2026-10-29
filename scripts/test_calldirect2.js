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
  const client = mod._c0('https://api.shegu.st');
  
  console.log('Handshake...');
  await client.handshake();
  console.log('Session:', client.session?.id || 'none');
  
  // Try without embed
  console.log('\n1. Call without embed...');
  let result = await client.callOnce('/Lisbon/movie?tmdb=155');
  console.log('Result:', JSON.stringify(result).substring(0, 500));
  
  // Now try with embed
  console.log('\n2. Call with embed...');
  result = await client.callOnce('/Lisbon/movie?tmdb=155&embed=true');
  console.log('Result:', JSON.stringify(result).substring(0, 800));
  
  // Try TV show
  console.log('\n3. TV show 82856 S01E01...');
  result = await client.callOnce('/Lisbon/tv?tmdb=82856&season=1&episode=1');
  console.log('Result:', JSON.stringify(result).substring(0, 500));
  
  result = await client.callOnce('/Lisbon/tv?tmdb=82856&season=1&episode=1&embed=true');
  console.log('Result:', JSON.stringify(result).substring(0, 800));
})();
