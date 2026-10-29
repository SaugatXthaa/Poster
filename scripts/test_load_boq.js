// Polyfill browser APIs for loading cinejoy chunks in Node.js
const { webcrypto } = require('crypto');

// WebCrypto
globalThis.crypto = globalThis.crypto || webcrypto;

// localStorage stub
const store = {};
globalThis.localStorage = {
  getItem: (k) => store[k] ?? null,
  setItem: (k, v) => { store[k] = String(v); },
  removeItem: (k) => { delete store[k]; },
  clear: () => { for (const k in store) delete store[k]; },
  get length() { return Object.keys(store).length; },
  key: (i) => Object.keys(store)[i] ?? null,
};

// document stub (minimal)
globalThis.document = {
  createElement: () => ({ style: {}, getContext: () => null, setAttribute: () => {}, appendChild: () => {} }),
  getElementById: () => null,
  querySelector: () => null,
  querySelectorAll: () => [],
  addEventListener: () => {},
  removeEventListener: () => {},
  hidden: false,
  visibilityState: 'visible',
  hasFocus: () => true,
};

// window = globalThis
globalThis.window = globalThis;
globalThis.window.__svelte = { v: new Set() };
globalThis.window.addEventListener = () => {};
globalThis.window.removeEventListener = () => {};

// navigator stub
globalThis.navigator = globalThis.navigator || {
  userAgent: 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36',
  language: 'en-US',
  languages: ['en-US'],
  platform: 'Linux x86_64',
  hardwareConcurrency: 2,
  deviceMemory: 4,
  maxTouchPoints: 0,
};

// performance stub
globalThis.performance = globalThis.performance || { now: () => Date.now() };

// location stub
globalThis.location = { href: 'https://cinejoy.to/', hostname: 'cinejoy.to', origin: 'https://cinejoy.to' };

(async () => {
  try {
    const mod = await import('file:///tmp/cinejoy_mod/BOqDcafn.js');
    console.log('Loaded! Exports:', Object.keys(mod).join(', '));
    
    if (mod.P) {
      console.log('P (Rx class) type:', typeof mod.P);
      try {
        const client = new mod.P('https://api.shegu.st');
        console.log('Client created!');
        const proto = Object.getPrototypeOf(client);
        const methods = Object.getOwnPropertyNames(proto).filter(m => m !== 'constructor');
        console.log('Methods:', methods.join(', '));
      } catch(e) {
        console.log('Create error:', e.message.substring(0, 300));
      }
    }
  } catch(e) {
    console.log('Load error:', e.message.substring(0, 500));
    if (e.stack) console.log(e.stack.split('\n').slice(0, 5).join('\n'));
  }
})();
