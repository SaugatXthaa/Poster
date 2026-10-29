// Fix: use Node's native fetch (don't polyfill performance incorrectly)
// Node 18+ has globalThis.fetch, globalThis.crypto

// Only polyfill what's missing
if (!globalThis.crypto) {
  const { webcrypto } = require('crypto');
  globalThis.crypto = webcrypto;
}

// Polyfill browser APIs minimally
globalThis.localStorage = { 
  getItem: () => null, 
  setItem: () => {}, 
  removeItem: () => {},
  clear: () => {},
  key: () => null,
  length: 0
};
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
globalThis.window = globalThis;
globalThis.window.__svelte = { v: new Set() };
globalThis.window.addEventListener = () => {};
globalThis.window.removeEventListener = () => {};
globalThis.navigator = { 
  userAgent: 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36',
  language: 'en-US',
  languages: ['en-US'],
  platform: 'Linux x86_64',
  hardwareConcurrency: 4,
  deviceMemory: 8,
  maxTouchPoints: 0,
  webdriver: false,
};
// Don't override performance - Node has it natively
globalThis.location = { href: 'https://cinejoy.to/', hostname: 'cinejoy.to', origin: 'https://cinejoy.to' };

(async () => {
  try {
    const mod = await import('file:///tmp/cinejoy_mod/BOqDcafn.js');
    console.log('Module loaded. Exports:', Object.keys(mod).join(', '));
    
    // The Rx class is NOT exported directly. C0(a) creates a singleton.
    // But C0 is also not exported. Let me find how to access it.
    // 
    // Looking at the exports: a=Px, b=P0, c=a0, d=G0, e=Xx, f=qx, g=Zx, h=Fx, i=D0, j=r0, k=q0, l=t0, m=y0, n=O0, o=p0, p=b0, r=wx, s=Ax
    // 
    // G0 (export 'd') calls lx(j) which calls C0(j).handshake()
    // So calling G0() will initialize the client!
    
    console.log('\nCalling G0() to fetch servers and init client...');
    const servers = await mod.d();
    console.log('Servers:', JSON.stringify(servers).substring(0, 300));
    
    // Now the Rx client is initialized (singleton _0)
    // I need to find the function that calls client.call(path, payload)
    // Let me try calling Ax (export 's') which is the main scraper
    // Ax(a, W, x, c) - need to figure out the args
    
  } catch(e) {
    console.log('Error:', e.message.substring(0, 500));
    if (e.stack) console.log(e.stack.split('\n').slice(0, 5).join('\n'));
  }
})();
