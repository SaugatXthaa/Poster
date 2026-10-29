const vm = require('vm');
const crypto = require('crypto');
const https = require('https');
const { URL } = require('url');

const TARGET = 'https://mkvbase.site/';
const UA = 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';
const cookies = {};

function httpsGet(url, options = {}) {
  return new Promise((resolve, reject) => {
    const u = new URL(url);
    const cookieStr = Object.entries(cookies).map(([k,v]) => `${k}=${v}`).join('; ');
    const req = https.request({
      hostname: u.hostname, port: 443, path: u.pathname + u.search,
      method: options.method || 'GET',
      headers: { 'User-Agent': UA, 'Accept': '*/*', 'Cookie': cookieStr, ...options.headers },
    }, (res) => {
      if (res.headers['set-cookie']) {
        for (const sc of res.headers['set-cookie']) {
          const m = sc.match(/^([^=]+)=([^;]*)/);
          if (m) cookies[m[1]] = m[2];
        }
      }
      const chunks = [];
      res.on('data', c => chunks.push(c));
      res.on('end', () => resolve({ status: res.statusCode, headers: res.headers, body: Buffer.concat(chunks).toString('utf-8') }));
    });
    req.on('error', reject);
    if (options.body) req.write(options.body);
    req.end();
  });
}

// Generic event target
function makeEventTarget() {
  const listeners = {};
  return {
    addEventListener: function(type, fn) {
      if (!listeners[type]) listeners[type] = [];
      listeners[type].push(fn);
    },
    removeEventListener: function(type, fn) {
      if (listeners[type]) {
        listeners[type] = listeners[type].filter(f => f !== fn);
      }
    },
    dispatchEvent: function(event) {
      const ls = listeners[event.type] || [];
      for (const fn of ls) {
        try { fn(event); } catch {}
      }
      return true;
    },
  };
}

(async () => {
  console.log('1. Fetching challenge page...');
  const resp = await httpsGet(TARGET);
  console.log('Status:', resp.status);
  
  const scripts = resp.body.match(/<script[^>]*>([\s\S]*?)<\/script>/gi) || [];
  const scriptMatch = resp.body.match(/a\.src\s*=\s*['"]([^'"]+)['"]/);
  const scriptUrl = scriptMatch[1].startsWith('http') ? scriptMatch[1] : 'https://mkvbase.site' + scriptMatch[1];
  
  console.log('2. Fetching orchestrate script...');
  const scriptResp = await httpsGet(scriptUrl);
  console.log('Script size:', scriptResp.body.length);
  
  console.log('3. Setting up VM sandbox...');
  
  const sandbox = {};
  
  // Event targets for window and document
  const windowEvents = makeEventTarget();
  const docEvents = makeEventTarget();
  
  const head = { tagName: 'HEAD', children: [], appendChild: function(c) { this.children.push(c); }, ...makeEventTarget() };
  const body = { tagName: 'BODY', children: [], appendChild: function(c) { this.children.push(c); }, ...makeEventTarget() };
  
  sandbox.document = {
    createElement: function(tag) {
      const el = {
        tagName: tag.toUpperCase(),
        style: {},
        children: [],
        attributes: {},
        setAttribute: function(name, val) { this.attributes[name] = val; },
        getAttribute: function(name) { return this.attributes[name] || null; },
        appendChild: function(child) { this.children.push(child); child.parentNode = this; return child; },
        removeChild: function(child) { this.children = this.children.filter(c => c !== child); },
        insertBefore: function(newNode, refNode) { this.children.push(newNode); return newNode; },
        replaceChild: function() {},
        addEventListener: function(){},
        removeEventListener: function(){},
        dispatchEvent: function() { return true; },
        getContext: function() { return null; },
        getElementsByTagName: function() { return []; },
        getBoundingClientRect: function() { return { top: 0, left: 0, right: 0, bottom: 0, width: 0, height: 0 }; },
        click: function() {},
        focus: function() {},
        blur: function() {},
        parentNode: null,
        innerHTML: '',
        textContent: '',
        className: '',
        id: '',
        src: '',
        href: '',
        nonce: '',
        type: '',
        nodeType: 1,
      };
      return el;
    },
    createTextNode: function(text) { return { textContent: text, nodeType: 3 }; },
    createDocumentFragment: function() { return sandbox.document.createElement('FRAGMENT'); },
    getElementsByTagName: function(tag) {
      if (tag === 'head') return [head];
      if (tag === 'body') return [body];
      if (tag === 'script') return [];
      return [];
    },
    getElementsByClassName: function() { return []; },
    getElementById: function() { return null; },
    querySelector: function() { return null; },
    querySelectorAll: function() { return []; },
    addEventListener: docEvents.addEventListener,
    removeEventListener: docEvents.removeEventListener,
    dispatchEvent: docEvents.dispatchEvent,
    head: head,
    body: body,
    documentElement: { ...makeEventTarget(), tagName: 'HTML', children: [head, body] },
    readyState: 'complete',
    hidden: false,
    visibilityState: 'visible',
    title: 'Just a moment...',
    referrer: '',
    URL: TARGET,
    location: null,
    nodeType: 9,
  };
  
  Object.defineProperty(sandbox.document, 'cookie', {
    get: function() { return Object.entries(cookies).map(([k,v]) => `${k}=${v}`).join('; '); },
    set: function(val) {
      const m = val.match(/^([^=;]+)=([^;]*)/);
      if (m) cookies[m[1].trim()] = m[2].trim();
    },
    configurable: true,
  });
  
  sandbox.location = {
    href: TARGET, hostname: 'mkvbase.site', pathname: '/',
    search: '', hash: '', origin: 'https://mkvbase.site',
    protocol: 'https:', port: '', host: 'mkvbase.site',
    replace: function(){}, assign: function(){}, reload: function(){},
    toString: function() { return TARGET; },
  };
  sandbox.document.location = sandbox.location;
  
  sandbox.navigator = {
    userAgent: UA,
    platform: 'Linux x86_64',
    language: 'en-US',
    languages: ['en-US', 'en'],
    hardwareConcurrency: 4,
    deviceMemory: 8,
    maxTouchPoints: 0,
    webdriver: false,
    plugins: { length: 0, item: function() { return null; }, namedItem: function() { return null; }, refresh: function() {} },
    cookieEnabled: true,
    doNotTrack: null,
    vendor: 'Google Inc.',
    vendorSub: '',
    product: 'Gecko',
    productSub: '20030107',
    appName: 'Netscape',
    appVersion: '5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36',
    appCodeName: 'Mozilla',
    onLine: true,
    javaEnabled: function() { return false; },
    getBattery: function() { return Promise.resolve({ level: 0.99, charging: true, addEventListener: function(){} }); },
    permissions: { query: function() { return Promise.resolve({ state: 'granted' }); } },
  };
  
  sandbox.screen = {
    width: 1920, height: 1080,
    availWidth: 1920, availHeight: 1040,
    colorDepth: 24, pixelDepth: 24,
    orientation: { type: 'landscape-primary', angle: 0 },
  };
  
  sandbox.history = {
    replaceState: function(){}, pushState: function(){},
    back: function(){}, forward: function(){}, go: function(){},
    length: 1, state: null,
  };
  
  sandbox.crypto = crypto.webcrypto;
  
  sandbox.performance = {
    now: function() { return Date.now() % 1000000; },
    timing: {
      navigationStart: Date.now() - 2000,
      loadEventEnd: Date.now() - 1000,
      domContentLoadedEventEnd: Date.now() - 1500,
      fetchStart: Date.now() - 1900,
      domainLookupStart: Date.now() - 1800,
      domainLookupEnd: Date.now() - 1700,
      connectStart: Date.now() - 1600,
      connectEnd: Date.now() - 1500,
      requestStart: Date.now() - 1400,
      responseStart: Date.now() - 1300,
      responseEnd: Date.now() - 1200,
    },
    getEntriesByType: function() { return []; },
    getEntries: function() { return []; },
    mark: function(){}, measure: function(){},
    clearMarks: function(){}, clearMeasures: function(){},
  };
  
  sandbox.XMLHttpRequest = function() {
    this.readyState = 0; this.status = 0; this.responseText = ''; this.response = '';
    this._headers = {};
    this.open = function(method, url, async) { this._method = method; this._url = url; this._async = async; this.readyState = 1; };
    this.setRequestHeader = function(name, value) { this._headers[name] = value; };
    this.send = async function(body) {
      try {
        const fullUrl = this._url.startsWith('http') ? this._url : TARGET + this._url.replace(/^\//, '');
        const r = await httpsGet(fullUrl, { method: this._method, headers: this._headers, body });
        this.status = r.status; this.responseText = r.body; this.response = r.body; this.readyState = 4;
        if (this.onreadystatechange) this.onreadystatechange();
        if (this.onload) this.onload({ type: 'load', target: this });
      } catch(e) { if (this.onerror) this.onerror(e); }
    };
    this.getResponseHeader = function(name) { return null; };
    this.getAllResponseHeaders = function() { return ''; };
    this.abort = function() {};
  };
  sandbox.XMLHttpRequest.DONE = 4;
  sandbox.XMLHttpRequest.HEADERS_RECEIVED = 2;
  sandbox.XMLHttpRequest.LOADING = 3;
  sandbox.XMLHttpRequest.OPENED = 1;
  sandbox.XMLHttpRequest.UNSENT = 0;
  
  sandbox.fetch = async function(url, opts = {}) {
    const fullUrl = url.startsWith('http') ? url : TARGET + url.replace(/^\//, '');
    const r = await httpsGet(fullUrl, opts);
    return {
      ok: r.status >= 200 && r.status < 300, status: r.status, statusText: '',
      headers: { get: function(name) { return r.headers[name.toLowerCase()] || null; } },
      text: async () => r.body, json: async () => JSON.parse(r.body), arrayBuffer: async () => Buffer.from(r.body),
    };
  };
  
  // Copy all globals from the previous script
  const globals = ['setTimeout','setInterval','clearTimeout','clearInterval','queueMicrotask','console','Buffer','TextEncoder','TextDecoder','URL','URLSearchParams','Date','Math','JSON','Object','Array','String','Number','Boolean','RegExp','Error','TypeError','RangeError','SyntaxError','ReferenceError','Promise','Symbol','Map','Set','WeakMap','WeakSet','Proxy','Reflect','isNaN','isFinite','parseInt','parseFloat','encodeURIComponent','decodeURIComponent','encodeURI','decodeURI','Uint8Array','Uint16Array','Uint32Array','Int8Array','Int16Array','Int32Array','Float32Array','Float64Array','ArrayBuffer','DataView','BigInt','BigInt64Array','BigUint64Array'];
  for (const g of globals) {
    if (typeof globalThis[g] !== 'undefined') sandbox[g] = globalThis[g];
  }
  
  sandbox.atob = function(str) { return Buffer.from(str, 'base64').toString('binary'); };
  sandbox.btoa = function(str) { return Buffer.from(str, 'binary').toString('base64'); };
  
  // Window is sandbox itself with event target
  sandbox.window = sandbox;
  sandbox.self = sandbox;
  sandbox.top = sandbox;
  sandbox.parent = sandbox;
  sandbox.frames = sandbox;
  sandbox.globalThis = sandbox;
  sandbox.addEventListener = windowEvents.addEventListener;
  sandbox.removeEventListener = windowEvents.removeEventListener;
  sandbox.dispatchEvent = windowEvents.dispatchEvent;
  
  // MutationObserver
  sandbox.MutationObserver = function() {
    return { observe: function(){}, disconnect: function(){}, takeRecords: function() { return []; } };
  };
  
  // IntersectionObserver
  sandbox.IntersectionObserver = function() {
    return { observe: function(){}, unobserve: function(){}, disconnect: function(){}, takeRecords: function() { return []; } };
  };
  
  // ResizeObserver
  sandbox.ResizeObserver = function() {
    return { observe: function(){}, disconnect: function(){}, unobserve: function(){} };
  };
  
  vm.createContext(sandbox);
  
  // Run inline scripts
  console.log('4. Running inline scripts...');
  for (const scriptTag of scripts) {
    const code = scriptTag.match(/<script[^>]*>([\s\S]*?)<\/script>/i);
    if (!code || !code[1].trim()) continue;
    if (scriptTag.includes('src=')) continue;
    try {
      vm.runInContext(code[1], sandbox, { timeout: 5000, filename: 'inline.js' });
      console.log('  Inline script OK');
    } catch(e) {
      console.log('  Inline script error:', e.message.substring(0, 100));
    }
  }
  
  // Run orchestrate script
  console.log('5. Running orchestrate script...');
  try {
    vm.runInContext(scriptResp.body, sandbox, { timeout: 30000, filename: 'orchestrate.js' });
    console.log('Orchestrate script executed OK');
  } catch(e) {
    console.log('Orchestrate error:', e.message.substring(0, 200));
  }
  
  // Wait for cf_clearance
  console.log('6. Waiting for cf_clearance...');
  for (let i = 0; i < 30; i++) {
    await new Promise(r => setTimeout(r, 1000));
    if (cookies['cf_clearance']) {
      console.log('GOT cf_clearance after', i+1, 'seconds!');
      break;
    }
    if (i % 5 === 4) console.log('  Still waiting...', i+1, 's');
  }
  
  console.log('Final cookies:', Object.keys(cookies));
  
  if (cookies['cf_clearance']) {
    const r2 = await httpsGet(TARGET);
    console.log('\n7. Real page:');
    console.log('Status:', r2.status);
    console.log('Is CF:', r2.body.includes('Just a moment'));
    console.log('Title:', r2.body.match(/<title>([^<]*)<\/title>/)?.[1]);
    console.log('Body preview:', r2.body.substring(0, 500));
  } else {
    console.log('Failed to get cf_clearance');
  }
})();
