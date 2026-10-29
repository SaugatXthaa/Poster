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
  const mod = await import('file:///tmp/cinejoy_mod/BOqDcafn.js');
  // The module loaded successfully. Let me try to access the internal functions
  // by reading the module source and extracting what I need
  
  // Actually, the simplest approach: Ax expects 'c' to have a method that returns a string
  // (probably a language/region filter). Let me pass an object with that method
  console.log('Testing Ax with proper c argument...');
  
  const servers = await mod.d();
  
  // c needs a method at offset 387/'j6i7' - probably 'language' or 'region'
  // Let me just try passing an object with common methods
  const c = {
    language: () => 'en',
    region: () => 'US',
    embed: true,
    // Try as a function
    toLowerCase: () => 'en',
    toString: () => 'en',
  };
  
  try {
    const result = await mod.s(servers, { type: 'movie', id: 155, tmdb: 155 }, null, c);
    console.log('Result:', JSON.stringify(result).substring(0, 500));
  } catch(e) {
    console.log('Error:', e.message.substring(0, 200));
  }
})();
