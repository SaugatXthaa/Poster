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
  
  // Try each function export to see if it's a constructor
  for (const k of Object.keys(mod)) {
    if (typeof mod[k] !== 'function') continue;
    try {
      const obj = new mod[k]('https://api.shegu.st');
      const proto = Object.getPrototypeOf(obj);
      const methods = Object.getOwnPropertyNames(proto).filter(m => m !== 'constructor');
      if (methods.length > 0) {
        console.log(k + ' (' + mod[k].name + '): constructor! methods:', methods.slice(0, 10).join(', '));
      }
    } catch(e) {
      // Not a constructor or wrong args
    }
  }
})();
