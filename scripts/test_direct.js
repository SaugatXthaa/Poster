if (!globalThis.crypto) {
  const { webcrypto } = require('crypto');
  globalThis.crypto = webcrypto;
}
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
  const mod = await import('file:///tmp/cinejoy_mod/BOqDcafn.js');
  
  // Init client
  console.log('Initializing...');
  await mod.d(); // G0 - fetches servers, inits Rx client
  
  // Now try calling Ax (export 's') which is the main entry point
  // Ax iterates over all providers and calls y0 for each
  // Ax(a, W, x, c) where:
  //   a = list of providers (from G0)
  //   W = media object
  //   x = optional filter
  //   c = options
  //
  // Let me look at what W (media object) should contain
  // From y0: it accesses W for tmdb, season, episode, embed
  // y0(a, W, x, c) where W = "movie" or "tv", x = media info, c = embed flag
  
  // Try calling Ax with proper args
  console.log('Calling Ax (main scraper)...');
  try {
    const servers = await mod.d();
    // Ax expects: (providers, mediaInfo, filterFn, options)
    // mediaInfo should have: type, id, season, episode
    const mediaInfo = {
      type: 'movie',
      id: 155,
      tmdb: 155,
    };
    const result = await mod.s(servers, mediaInfo, undefined, { embed: true });
    console.log('Result:', JSON.stringify(result).substring(0, 1000));
  } catch(e) {
    console.log('Ax error:', e.message.substring(0, 300));
    if (e.stack) console.log(e.stack.split('\n').slice(0, 8).join('\n'));
  }
})();
