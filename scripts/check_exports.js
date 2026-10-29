// Check what each export is
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
  for (const k of Object.keys(mod)) {
    const v = mod[k];
    const t = typeof v;
    let s = '';
    if (t === 'string') s = '"' + v.substring(0, 60) + '"';
    else if (t === 'function') s = v.name || 'anonymous';
    else if (t === 'object' && v) s = JSON.stringify(v).substring(0, 60);
    else s = String(v);
    console.log(k + ': ' + t + ' = ' + s);
  }
})();
