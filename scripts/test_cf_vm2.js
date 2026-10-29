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

(async () => {
  console.log('1. Fetching challenge page...');
  const resp = await httpsGet(TARGET);
  console.log('Status:', resp.status, '| Cookies:', Object.keys(cookies));
  
  // Extract ALL script tags from the page
  const scripts = resp.body.match(/<script[^>]*>([\s\S]*?)<\/script>/gi) || [];
  console.log('Found', scripts.length, 'script tags');
  
  // Extract the challenge script URL
  const scriptMatch = resp.body.match(/a\.src\s*=\s*['"]([^'"]+)['"]/);
  if (!scriptMatch) {
    console.log('No challenge script URL found');
    return;
  }
  const scriptUrl = scriptMatch[1].startsWith('http') ? scriptMatch[1] : 'https://mkvbase.site' + scriptMatch[1];
  console.log('Challenge script URL:', scriptUrl);
  
  // Fetch the challenge orchestrate script
  console.log('\n2. Fetching challenge script...');
  const scriptResp = await httpsGet(scriptUrl);
  console.log('Script size:', scriptResp.body.length);
  
  // Create the sandbox
  console.log('\n3. Setting up VM sandbox...');
  
  const sandbox = {};
  
  // Build a complete window/document mock
  const documentElement = {
    style: {}, setAttribute: function(){}, appendChild: function(){},
    removeChild: function(){}, addEventListener: function(){}, removeEventListener: function(){},
    children: [], childNodes: [], firstChild: null, lastChild: null,
    parentNode: null, nextSibling: null, previousSibling: null,
    innerHTML: '', textContent: '', className: '', id: '',
  };
  
  const head = { ...documentElement, tagName: 'HEAD', appendChild: function(child) { this.children.push(child); } };
  const body = { ...documentElement, tagName: 'BODY', appendChild: function(child) { this.children.push(child); } };
  
  sandbox.document = {
    createElement: function(tag) {
      const el = { ...documentElement, tagName: tag.toUpperCase() };
      el.style = {};
      el.setAttribute = function(name, val) { this[name] = val; };
      el.appendChild = function(child) { this.children = this.children || []; this.children.push(child); };
      el.addEventListener = function(){};
      el.removeChild = function(){};
      el.getContext = function() { return null; };
      el.getElementsByTagName = function() { return []; };
      return el;
    },
    createTextNode: function(text) { return { textContent: text, nodeType: 3 }; },
    getElementsByTagName: function(tag) {
      if (tag === 'head') return [head];
      if (tag === 'body') return [body];
      return [];
    },
    getElementById: function() { return null; },
    querySelector: function() { return null; },
    querySelectorAll: function() { return []; },
    addEventListener: function(){},
    removeEventListener: function(){},
    head: head,
    body: body,
    documentElement: documentElement,
    readyState: 'complete',
    hidden: false,
    visibilityState: 'visible',
    title: 'Just a moment...',
    referrer: '',
    URL: TARGET,
    location: null, // set later
  };
  
  // Cookie handling
  let _docCookie = '';
  Object.defineProperty(sandbox.document, 'cookie', {
    get: function() {
      return Object.entries(cookies).map(([k,v]) => `${k}=${v}`).join('; ');
    },
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
    plugins: { length: 0 },
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
  };
  
  sandbox.crypto = crypto.webcrypto;
  
  sandbox.performance = {
    now: function() { return Date.now() - 1786813715000; },
    timing: {
      navigationStart: Date.now() - 2000,
      loadEventEnd: Date.now() - 1000,
      domContentLoadedEventEnd: Date.now() - 1500,
    },
    getEntriesByType: function() { return []; },
    getEntries: function() { return []; },
    mark: function(){}, measure: function(){}, clearMarks: function(){}, clearMeasures: function(){},
  };
  
  // XMLHttpRequest with real HTTP requests
  sandbox.XMLHttpRequest = function() {
    this.readyState = 0;
    this.status = 0;
    this.responseText = '';
    this.response = '';
    this._headers = {};
    this.open = function(method, url, async) {
      this._method = method;
      this._url = url;
      this._async = async;
      this.readyState = 1;
    };
    this.setRequestHeader = function(name, value) {
      this._headers[name] = value;
    };
    this.send = async function(body) {
      try {
        const fullUrl = this._url.startsWith('http') ? this._url : TARGET + this._url.replace(/^\//, '');
        const r = await httpsGet(fullUrl, { method: this._method, headers: this._headers, body });
        this.status = r.status;
        this.responseText = r.body;
        this.response = r.body;
        this.readyState = 4;
        if (this.onload) this.onload({ type: 'load', target: this });
        if (this.onreadystatechange) this.onreadystatechange();
      } catch(e) {
        if (this.onerror) this.onerror(e);
      }
    };
    this.getResponseHeader = function(name) { return null; };
    this.getAllResponseHeaders = function() { return ''; };
    this.abort = function() {};
  };
  
  // fetch polyfill
  sandbox.fetch = async function(url, opts = {}) {
    const fullUrl = url.startsWith('http') ? url : TARGET + url.replace(/^\//, '');
    const r = await httpsGet(fullUrl, opts);
    return {
      ok: r.status >= 200 && r.status < 300,
      status: r.status,
      statusText: '',
      headers: { get: function(name) { return r.headers[name.toLowerCase()] || null; } },
      text: async () => r.body,
      json: async () => JSON.parse(r.body),
      arrayBuffer: async () => Buffer.from(r.body),
    };
  };
  
  // Standard globals
  sandbox.setTimeout = setTimeout;
  sandbox.setInterval = setInterval;
  sandbox.clearTimeout = clearTimeout;
  sandbox.clearInterval = clearInterval;
  sandbox.queueMicrotask = queueMicrotask;
  sandbox.console = console;
  sandbox.Buffer = Buffer;
  sandbox.TextEncoder = TextEncoder;
  sandbox.TextDecoder = TextDecoder;
  sandbox.URL = URL;
  sandbox.URLSearchParams = URLSearchParams;
  sandbox.atob = function(str) { return Buffer.from(str, 'base64').toString('binary'); };
  sandbox.btoa = function(str) { return Buffer.from(str, 'binary').toString('base64'); };
  sandbox.Date = Date;
  sandbox.Math = Math;
  sandbox.JSON = JSON;
  sandbox.Object = Object;
  sandbox.Array = Array;
  sandbox.String = String;
  sandbox.Number = Number;
  sandbox.Boolean = Boolean;
  sandbox.RegExp = RegExp;
  sandbox.Error = Error;
  sandbox.TypeError = TypeError;
  sandbox.RangeError = RangeError;
  sandbox.SyntaxError = SyntaxError;
  sandbox.ReferenceError = ReferenceError;
  sandbox.Promise = Promise;
  sandbox.Symbol = Symbol;
  sandbox.Map = Map;
  sandbox.Set = Set;
  sandbox.WeakMap = WeakMap;
  sandbox.WeakSet = WeakSet;
  sandbox.Proxy = Proxy;
  sandbox.Reflect = Reflect;
  sandbox.isNaN = isNaN;
  sandbox.isFinite = isFinite;
  sandbox.parseInt = parseInt;
  sandbox.parseFloat = parseFloat;
  sandbox.encodeURIComponent = encodeURIComponent;
  sandbox.decodeURIComponent = decodeURIComponent;
  sandbox.encodeURI = encodeURI;
  sandbox.decodeURI = decodeURI;
  sandbox.Uint8Array = Uint8Array;
  sandbox.Uint16Array = Uint16Array;
  sandbox.Uint32Array = Uint32Array;
  sandbox.Int8Array = Int8Array;
  sandbox.Int16Array = Int16Array;
  sandbox.Int32Array = Int32Array;
  sandbox.Float32Array = Float32Array;
  sandbox.Float64Array = Float64Array;
  sandbox.ArrayBuffer = ArrayBuffer;
  sandbox.DataView = DataView;
  sandbox.TextEncoder = TextEncoder;
  sandbox.TextDecoder = TextDecoder;
  
  // window = sandbox itself (circular reference)
  sandbox.window = sandbox;
  sandbox.self = sandbox;
  sandbox.top = sandbox;
  sandbox.parent = sandbox;
  sandbox.frames = sandbox;
  sandbox.globalThis = sandbox;
  
  // Create the context
  vm.createContext(sandbox);
  
  // Run the inline scripts first (to set up _cf_chl_opt)
  console.log('\n4. Running inline scripts...');
  for (const scriptTag of scripts) {
    const code = scriptTag.match(/<script[^>]*>([\s\S]*?)<\/script>/i);
    if (!code || !code[1].trim()) continue;
    
    // Skip scripts with src attribute
    if (scriptTag.includes('src=')) continue;
    
    try {
      vm.runInContext(code[1], sandbox, { timeout: 5000, filename: 'inline.js' });
      console.log('  Inline script OK');
    } catch(e) {
      console.log('  Inline script error:', e.message.substring(0, 100));
    }
  }
  
  // Check if _cf_chl_opt was set
  console.log('_cf_chl_opt set:', typeof sandbox._cf_chl_opt);
  if (sandbox._cf_chl_opt) {
    console.log('_cf_chl_opt keys:', Object.keys(sandbox._cf_chl_opt).join(', '));
  }
  
  // Run the orchestrate script
  console.log('\n5. Running orchestrate script...');
  try {
    vm.runInContext(scriptResp.body, sandbox, { timeout: 30000, filename: 'orchestrate.js' });
    console.log('Orchestrate script executed successfully');
  } catch(e) {
    console.log('Orchestrate error:', e.message.substring(0, 200));
  }
  
  // Wait for cf_clearance
  console.log('\n6. Waiting for cf_clearance...');
  for (let i = 0; i < 30; i++) {
    await new Promise(r => setTimeout(r, 1000));
    if (cookies['cf_clearance']) {
      console.log('Got cf_clearance after', i+1, 'seconds!');
      break;
    }
    if (i % 5 === 4) console.log('  Still waiting...', i+1, 's');
  }
  
  console.log('Final cookies:', Object.keys(cookies));
  
  if (cookies['cf_clearance']) {
    console.log('\n7. Fetching real page with cf_clearance...');
    const r2 = await httpsGet(TARGET);
    console.log('Status:', r2.status);
    console.log('Is CF:', r2.body.includes('Just a moment'));
    const title = r2.body.match(/<title>([^<]*)<\/title>/);
    console.log('Title:', title ? title[1] : 'unknown');
    console.log('Body preview:', r2.body.substring(0, 300));
  } else {
    console.log('\nFailed to get cf_clearance');
  }
})();
