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
      hostname: u.hostname,
      port: 443,
      path: u.pathname + u.search,
      method: options.method || 'GET',
      headers: {
        'User-Agent': UA,
        'Accept': '*/*',
        'Cookie': cookieStr,
        ...options.headers,
      },
    }, (res) => {
      if (res.headers['set-cookie']) {
        for (const sc of res.headers['set-cookie']) {
          const m = sc.match(/^([^=]+)=([^;]*)/);
          if (m) cookies[m[1]] = m[2];
        }
      }
      const chunks = [];
      res.on('data', c => chunks.push(c));
      res.on('end', () => resolve({
        status: res.statusCode,
        headers: res.headers,
        body: Buffer.concat(chunks).toString('utf-8'),
      }));
    });
    req.on('error', reject);
    if (options.body) req.write(options.body);
    req.end();
  });
}

(async () => {
  console.log('1. Fetching challenge page...');
  const resp = await httpsGet(TARGET);
  console.log('Status:', resp.status);
  console.log('Cookies:', Object.keys(cookies));
  
  // Extract the challenge script URL
  const scriptMatch = resp.body.match(/a\.src\s*=\s*['"]([^'"]+)['"]/);
  if (!scriptMatch) {
    console.log('No challenge script found');
    console.log('Body preview:', resp.body.substring(0, 500));
    return;
  }
  
  const scriptPath = scriptMatch[1];
  const scriptUrl = scriptPath.startsWith('http') ? scriptPath : 'https://mkvbase.site' + scriptPath;
  console.log('Script URL:', scriptUrl);
  
  // Fetch the challenge script
  console.log('\n2. Fetching challenge script...');
  const scriptResp = await httpsGet(scriptUrl);
  console.log('Script size:', scriptResp.body.length);
  
  // Create a sandbox environment
  console.log('\n3. Setting up VM sandbox...');
  
  const sandbox = {
    // Window/global
    window: {},
    document: {
      createElement: function(tag) {
        return {
          tagName: tag.toUpperCase(),
          style: {},
          setAttribute: function() {},
          appendChild: function() {},
          addEventListener: function() {},
          removeChild: function() {},
          parentNode: null,
          innerHTML: '',
          textContent: '',
          src: '',
          href: '',
          nonce: '',
          type: '',
        };
      },
      getElementsByTagName: function() { return [{ appendChild: function() {} }]; },
      getElementById: function() { return null; },
      querySelector: function() { return null; },
      querySelectorAll: function() { return []; },
      addEventListener: function() {},
      removeEventListener: function() {},
      cookie: '',
      title: '',
      referrer: '',
      URL: TARGET,
      location: { href: TARGET, hostname: 'mkvbase.site', pathname: '/', search: '', hash: '' },
      readyState: 'complete',
      hidden: false,
      visibilityState: 'visible',
    },
    navigator: {
      userAgent: UA,
      platform: 'Linux x86_64',
      language: 'en-US',
      languages: ['en-US', 'en'],
      hardwareConcurrency: 4,
      deviceMemory: 8,
      maxTouchPoints: 0,
      webdriver: false,
      plugins: [],
      cookieEnabled: true,
      doNotTrack: null,
      vendor: 'Google Inc.',
      vendorSub: '',
      product: 'Gecko',
      productSub: '20030107',
      appName: 'Netscape',
      appVersion: '5.0',
      appCodeName: 'Mozilla',
      onLine: true,
      javaEnabled: function() { return false; },
      getBattery: function() { return Promise.resolve({ level: 0.99, charging: true }); },
    },
    screen: {
      width: 1920, height: 1080,
      availWidth: 1920, availHeight: 1080,
      colorDepth: 24, pixelDepth: 24,
      orientation: { type: 'landscape-primary', angle: 0 },
    },
    location: {
      href: TARGET, hostname: 'mkvbase.site',
      pathname: '/', search: '', hash: '',
      origin: 'https://mkvbase.site', protocol: 'https:',
      port: '', host: 'mkvbase.site',
    },
    history: {
      replaceState: function() {},
      pushState: function() {},
      back: function() {},
      forward: function() {},
      go: function() {},
    },
    crypto: crypto.webcrypto,
    performance: {
      now: function() { return Date.now(); },
      timing: { navigationStart: Date.now() - 1000 },
      getEntriesByType: function() { return []; },
      mark: function() {},
      measure: function() {},
    },
    XMLHttpRequest: function() {
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
        vm.createContext(sandbox);
  try {
          const fullUrl = this._url.startsWith('http') ? this._url : TARGET + this._url.replace(/^\//, '');
          const r = await httpsGet(fullUrl, { method: this._method, headers: this._headers, body });
          this.status = r.status;
          this.responseText = r.body;
          this.response = r.body;
          this.readyState = 4;
          if (this.onload) this.onload({ type: 'load', target: this });
        } catch(e) {
          if (this.onerror) this.onerror(e);
        }
      };
      this.getResponseHeader = function(name) {
        return null;
      };
      this.getAllResponseHeaders = function() {
        return '';
      };
      this.abort = function() {};
    },
    fetch: async function(url, opts = {}) {
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
    },
    setTimeout: setTimeout,
    setInterval: setInterval,
    clearTimeout: clearTimeout,
    clearInterval: clearInterval,
    console: console,
    Buffer: Buffer,
    TextEncoder: TextEncoder,
    TextDecoder: TextDecoder,
    URL: URL,
    URLSearchParams: URLSearchParams,
    atob: function(str) { return Buffer.from(str, 'base64').toString('binary'); },
    btoa: function(str) { return Buffer.from(str, 'binary').toString('base64'); },
    Date: Date,
    Math: Math,
    JSON: JSON,
    Object: Object,
    Array: Array,
    String: String,
    Number: Number,
    Boolean: Boolean,
    RegExp: RegExp,
    Error: Error,
    Promise: Promise,
    Symbol: Symbol,
    Map: Map,
    Set: Set,
    WeakMap: WeakMap,
    WeakSet: WeakSet,
    Proxy: Proxy,
    Reflect: Reflect,
    isNaN: isNaN,
    isFinite: isFinite,
    parseInt: parseInt,
    parseFloat: parseFloat,
    encodeURIComponent: encodeURIComponent,
    decodeURIComponent: decodeURIComponent,
    encodeURI: encodeURI,
    decodeURI: decodeURI,
  };
  
  // Make window point to itself
  sandbox.window = sandbox;
  sandbox.self = sandbox;
  sandbox.globalThis = sandbox;
  sandbox.top = sandbox;
  sandbox.parent = sandbox;
  
  // Set document.cookie getter/setter
  let _cookie = '';
  Object.defineProperty(sandbox.document, 'cookie', {
    get: function() {
      return _cookie + ' ' + Object.entries(cookies).map(([k,v]) => `${k}=${v}`).join('; ');
    },
    set: function(val) {
      const m = val.match(/^([^=]+)=([^;]*)/);
      if (m) cookies[m[1]] = m[2];
      _cookie = val;
    },
  });
  
  vm.createContext(sandbox);
  try {
    console.log('4. Executing challenge script in VM...');
    
    // First execute the inline challenge init script from the page
    const inlineMatch = resp.body.match(/<script\s+nonce="[^"]*">\s*\(function\(\)\s*\{window\._cf_chl_opt[\s\S]*?\}\)\(\);\s*<\/script>/);
    if (inlineMatch) {
      const inlineCode = inlineMatch[0].match(/>\s*([\s\S]*?)\s*<\/script>/)[1];
      console.log('Running inline init script...');
      
      vm.runInContext(inlineCode, sandbox, { timeout: 5000 });
    }
    
    // Execute the orchestrate script
    vm.runInContext(scriptResp.body, sandbox, { timeout: 30000 });
    
    console.log('Script executed. Waiting for challenge to complete...');
    
    // Wait for cf_clearance cookie
    for (let i = 0; i < 30; i++) {
      await new Promise(r => setTimeout(r, 1000));
      if (cookies['cf_clearance']) {
        console.log('Got cf_clearance after', i+1, 'seconds!');
        break;
      }
    }
    
    console.log('Final cookies:', Object.keys(cookies));
    
    if (cookies['cf_clearance']) {
      // Try fetching the actual page with the cookie
      const r2 = await httpsGet(TARGET);
      console.log('\n5. Page after solve:');
      console.log('Status:', r2.status);
      console.log('Is CF:', r2.body.includes('Just a moment'));
      console.log('Title:', r2.body.match(/<title>([^<]*)<\/title>/)?.[1]);
      console.log('Body preview:', r2.body.substring(0, 300));
    } else {
      console.log('Failed to get cf_clearance');
    }
  } catch(e) {
    console.log('VM error:', e.message);
    if (e.stack) console.log(e.stack.split('\n').slice(0, 5).join('\n'));
  }
})();
