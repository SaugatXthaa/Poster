// Test runner that loads cinejoy.to in jsdom to capture fetch/WSS calls

// Pre-define a global window so the jsbn module doesn't crash
if (typeof globalThis.window === 'undefined') {
  globalThis.window = { crypto: require('crypto').webcrypto || {} };
}

const { JSDOM } = require('jsdom');
const fs = require('fs');

const fetches = [];
const wsConnects = [];
const localStorageData = {};
const consoleMessages = [];

const html = fs.readFileSync('/tmp/cinejoy_home.html', 'utf8');

const dom = new JSDOM(html, {
  url: 'https://cinejoy.to/movie/27205',
  referrer: 'https://cinejoy.to/',
  contentType: 'text/html',
  runScripts: 'dangerously',
  pretendToBeVisual: true,
  resources: 'usable',
});

const window = dom.window;

// Override fetch
window.fetch = function(url, opts) {
  const urlStr = typeof url === 'string' ? url : (url.url || url.href || String(url));
  console.log('FETCH:', urlStr.slice(0, 250));
  fetches.push({ url: urlStr, opts });
  
  if (urlStr.includes('/servers')) {
    const serversList = {"servers":[{"name":"Lisbon","status":"ok","language":"US","description":"","4k":true}]};
    return Promise.resolve(new window.Response(JSON.stringify(serversList), { status: 200, headers: { 'content-type': 'application/json' } }));
  }
  return Promise.reject(new Error('blocked: ' + urlStr));
};

// Override WebSocket
window.WebSocket = function(url, protocols) {
  console.log('WSS:', String(url).slice(0, 250));
  wsConnects.push(String(url));
  return {
    send: () => {},
    close: () => {},
    addEventListener: () => {},
    removeEventListener: () => {},
    onopen: null, onmessage: null, onerror: null, onclose: null,
    readyState: 0
  };
};

// Override localStorage
Object.defineProperty(window, 'localStorage', {
  value: {
    getItem: (k) => localStorageData[k] || null,
    setItem: (k, v) => {
      localStorageData[k] = String(v);
      console.log('localStorage set:', k, '=', String(v).slice(0, 100));
    },
    removeItem: (k) => { delete localStorageData[k]; },
  },
  writable: true, configurable: true
});

// Capture console messages from the page
const origLog = window.console.log;
window.console.log = function(...args) {
  consoleMessages.push(args.map(String).join(' '));
  // console.log('PAGE LOG:', ...args);  // uncomment for verbose
};
window.console.error = function(...args) {
  consoleMessages.push('ERROR: ' + args.map(String).join(' '));
  console.log('PAGE ERR:', ...args);
};

// Wait for the page to load and try to capture fetches
setTimeout(() => {
  console.log('');
  console.log('=== Total HTTP fetches:', fetches.length, '===');
  fetches.slice(0, 30).forEach(f => console.log('  HTTP:', f.url.slice(0, 200)));
  console.log('');
  console.log('=== Total WSS connects:', wsConnects.length, '===');
  wsConnects.slice(0, 10).forEach(u => console.log('  WSS:', u.slice(0, 200)));
  
  console.log('');
  console.log('=== localStorage captured ===');
  Object.keys(localStorageData).forEach(k => console.log('  ' + k + ' = ' + localStorageData[k]));
  
  console.log('');
  console.log('=== Total console messages:', consoleMessages.length, '===');
  consoleMessages.slice(0, 10).forEach(m => console.log('  MSG:', m.slice(0, 200)));
  
  process.exit(0);
}, 15000);
