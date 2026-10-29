const { JSDOM } = require('jsdom');
const { createCanvas } = require('canvas');
const https = require('https');
const { URL } = require('url');

const TARGET = 'https://links.olamovies.mov/17dh40zBbl8SS8sKUB';
const UA = 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';

let cookies = {};

function fetch(url, options = {}) {
  return new Promise((resolve, reject) => {
    const u = new URL(url);
    const cookieStr = Object.entries(cookies).map(([k,v]) => k+'='+v).join('; ');
    const req = https.request({
      hostname: u.hostname, port: 443, path: u.pathname + u.search,
      method: options.method || 'GET',
      headers: { 'User-Agent': UA, 'Accept': 'text/html,*/*', 'Cookie': cookieStr, ...options.headers },
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
  console.log('Fetching link page...');
  const resp = await fetch(TARGET);
  console.log('Status:', resp.status);
  console.log('Is CF challenge:', resp.body.includes('Just a moment'));
  console.log('Is blocked:', resp.body.includes('blocked'));
  console.log('Body preview:', resp.body.substring(0, 200));
  
  if (resp.body.includes('Just a moment') || resp.status === 403) {
    console.log('\nAttempting to solve CF challenge...');
    
    const dom = new JSDOM(resp.body, {
      url: TARGET, runScripts: 'dangerously', resources: 'usable', pretendToBeVisual: true,
      beforeParse: (window) => {
        window.HTMLCanvasElement.prototype.getContext = function(type) {
          const c = createCanvas(200, 40);
          const ctx = c.getContext(type || '2d');
          if (ctx) { ctx.canvas = c; c.toDataURL = () => 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAAC0lEQVQI12NgAAIABQABNjN9GQAAAABJRU5ErkJggg=='; }
          return ctx;
        };
        const nav = window.navigator;
        Object.defineProperty(nav, 'userAgent', { value: UA, configurable: true });
        Object.defineProperty(nav, 'platform', { value: 'Linux x86_64', configurable: true });
        Object.defineProperty(nav, 'language', { value: 'en-US', configurable: true });
        Object.defineProperty(nav, 'languages', { value: ['en-US', 'en'], configurable: true });
        Object.defineProperty(nav, 'hardwareConcurrency', { value: 4, configurable: true });
        Object.defineProperty(nav, 'deviceMemory', { value: 8, configurable: true });
        Object.defineProperty(nav, 'webdriver', { value: false, configurable: true });
        Object.defineProperty(window.screen, 'width', { value: 1920, configurable: true });
        Object.defineProperty(window.screen, 'height', { value: 1080, configurable: true });
        Object.defineProperty(window.screen, 'colorDepth', { value: 24, configurable: true });
        window.WebGLRenderingContext = function(){};
        window.WebGL2RenderingContext = function(){};
        window.XMLHttpRequest = function() {
          this.readyState = 0; this.status = 0; this.responseText = ''; this.response = '';
          this._headers = {};
          this.open = function(m, u, a) { this._method = m; this._url = u; this._async = a; this.readyState = 1; };
          this.setRequestHeader = function(n, v) { this._headers[n] = v; };
          this.send = async function(body) {
            try {
              const fullUrl = this._url.startsWith('http') ? this._url : TARGET + this._url.replace(/^\//, '');
              const r = await fetch(fullUrl, { method: this._method, headers: this._headers, body });
              this.status = r.status; this.responseText = r.body; this.response = r.body; this.readyState = 4;
              if (this.onload) this.onload({ type: 'load', target: this });
            } catch(e) { if (this.onerror) this.onerror(e); }
          };
          this.getResponseHeader = function() { return null; };
          this.getAllResponseHeaders = function() { return ''; };
        };
        window.fetch = async (url, opts = {}) => {
          const fullUrl = url.startsWith('http') ? url : TARGET + url.replace(/^\//, '');
          return fetch(fullUrl, opts);
        };
        if (!window.crypto) window.crypto = globalThis.crypto;
      },
    });
    
    for (let i = 0; i < 30; i++) {
      await new Promise(r => setTimeout(r, 1000));
      if (cookies['cf_clearance']) { console.log('Got cf_clearance after', i+1, 's!'); break; }
    }
    console.log('Cookies:', Object.keys(cookies));
    
    if (cookies['cf_clearance']) {
      const r2 = await fetch(TARGET);
      console.log('\nAfter solve:', r2.status);
      console.log('Body:', r2.body.substring(0, 500));
      // Check for redirect or download URL
      const mkvUrls = r2.body.match(/https:\/\/[^"]*\.mkv|https:\/\/tutorials[^"]*/g);
      if (mkvUrls) console.log('MKV URLs:', mkvUrls);
    }
    dom.window.close();
  }
})();
