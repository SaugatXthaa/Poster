// Test direct access to the Rx client via patched module
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
  console.log('Exports:', Object.keys(mod).join(', '));
  
  if (mod._c0) {
    console.log('C0 found!');
    // First init the client via G0 (which calls lx -> C0 -> handshake)
    console.log('Initializing client...');
    await mod.d(); // G0
    
    // Now get the client singleton
    const client = mod._c0('https://api.shegu.st');
    console.log('Client:', typeof client);
    
    // List methods
    const proto = Object.getPrototypeOf(client);
    const methods = Object.getOwnPropertyNames(proto).filter(m => m !== 'constructor');
    console.log('Client methods:', methods);
    
    // The methods are computed (async[S(692,'tbGp')] etc.)
    // They have symbol-like names. Let me call them by index
    // Actually, the methods are stored as string keys (decoded at class definition time)
    // Let me check what keys exist on the client object
    const allKeys = [...Object.getOwnPropertyNames(client), ...Object.getOwnPropertyNames(Object.getPrototypeOf(client))];
    console.log('All keys:', allKeys);
    
    // Try calling each method to see which one is 'call' or 'callOnce'
    for (const key of allKeys) {
      if (key === 'constructor') continue;
      const val = client[key];
      if (typeof val === 'function') {
        console.log('  ' + key + ': function (' + val.length + ' args)');
      } else {
        console.log('  ' + key + ': ' + typeof val + ' = ' + String(val).substring(0, 50));
      }
    }
  }
})();
