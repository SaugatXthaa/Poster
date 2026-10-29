globalThis.crypto = require('crypto').webcrypto;
globalThis.localStorage = { getItem: () => null, setItem: () => {}, removeItem: () => {} };
globalThis.document = { createElement: () => ({style:{},getContext:()=>null}), addEventListener:()=>{} };
globalThis.window = globalThis;
globalThis.window.__svelte = { v: new Set() };
globalThis.window.addEventListener = () => {};
globalThis.navigator = { userAgent: 'Mozilla/5.0', language: 'en-US', languages: ['en-US'], platform: 'Linux x86_64', hardwareConcurrency: 2, deviceMemory: 4, maxTouchPoints: 0 };
globalThis.performance = { now: () => Date.now() };
globalThis.location = { href: 'https://cinejoy.to/' };

(async () => {
  const mod = await import('file:///tmp/cinejoy_mod/BOqDcafn.js');
  // The exports are functions. Let me try calling each to see what they return
  // G0 is exported as 'd' - it fetches servers
  // But I need to find the function that calls the Rx client's .call() method
  
  // Let me look at the 's' export (Ax) - the main scraper
  // It takes (a, W, x, c) - probably (providers, tmdb, type, options)
  
  // First, let me just try calling Ax and see what happens
  console.log('Testing Ax (s export)...');
  try {
    const result = await mod.s(undefined, { id: 155, type: 'movie' }, undefined, undefined);
    console.log('Ax result:', JSON.stringify(result).substring(0, 500));
  } catch(e) {
    console.log('Ax error:', e.message.substring(0, 300));
  }
})();
