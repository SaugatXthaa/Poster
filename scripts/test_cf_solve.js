const { JSDOM } = require('jsdom');
const https = require('https');
const { URL } = require('url');

const TARGET = 'https://new2.zinkmovies.mobi/';
const UA = 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';

function fetch(url, options = {}) {
  return new Promise((resolve, reject) => {
    const u = new URL(url);
    const req = https.request({
      hostname: u.hostname,
      port: 443,
      path: u.pathname + u.search,
      method: options.method || 'GET',
      headers: {
        'User-Agent': UA,
        'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
        'Accept-Language': 'en-US,en;q=0.9',
        'Accept-Encoding': 'identity',  // No compression for easier parsing
        ...options.headers,
      },
    }, (res) => {
      const chunks = [];
      res.on('data', c => chunks.push(c));
      res.on('end', () => {
        resolve({
          status: res.statusCode,
          headers: res.headers,
          body: Buffer.concat(chunks).toString('utf-8'),
        });
      });
    });
    req.on('error', reject);
    if (options.body) req.write(options.body);
    req.end();
  });
}

(async () => {
  console.log('1. Fetching initial page...');
  const resp1 = await fetch(TARGET);
  console.log('Status:', resp1.status);
  console.log('Set-Cookie:', resp1.headers['set-cookie']);
  
  // Extract the challenge details from the page
  const html = resp1.body;
  const chlOptMatch = html.match(/window\._cf_chl_opt\s*=\s*(\{[^}]+\})/);
  if (!chlOptMatch) {
    console.log('No challenge found');
    return;
  }
  
  // Extract the ray ID and challenge script URL
  const rayMatch = html.match(/ray=([a-f0-9]+)/);
  const ray = rayMatch ? rayMatch[1] : '';
  console.log('Ray:', ray);
  
  // Extract cookies from Set-Cookie
  const cookies = (resp1.headers['set-cookie'] || []).map(c => c.split(';')[0]).join('; ');
  console.log('Cookies:', cookies);
  
  // Fetch the challenge orchestrate script
  console.log('\n2. Fetching challenge script...');
  const scriptUrl = `https://new2.zinkmovies.mobi/cdn-cgi/challenge-platform/h/g/orchestrate/chl_page/v1?ray=${ray}`;
  const resp2 = await fetch(scriptUrl, { headers: { Cookie: cookies } });
  console.log('Script status:', resp2.status);
  console.log('Script size:', resp2.body.length);
  
  // Now set up JSDOM with the challenge page and execute the script
  console.log('\n3. Setting up JSDOM...');
  
  // Create a JSDOM instance with the challenge HTML
  const dom = new JSDOM(html, {
    url: TARGET,
    runScripts: 'dangerously',
    resources: 'usable',
    pretendToBeVisual: true,
  });
  
  // Polyfill navigator properties
  const nav = dom.window.navigator;
  Object.defineProperty(nav, 'userAgent', { value: UA, configurable: true });
  Object.defineProperty(nav, 'platform', { value: 'Linux x86_64', configurable: true });
  Object.defineProperty(nav, 'language', { value: 'en-US', configurable: true });
  Object.defineProperty(nav, 'languages', { value: ['en-US', 'en'], configurable: true });
  Object.defineProperty(nav, 'hardwareConcurrency', { value: 4, configurable: true });
  Object.defineProperty(nav, 'deviceMemory', { value: 8, configurable: true });
  Object.defineProperty(nav, 'webdriver', { value: false, configurable: true });
  
  // Polyfill screen
  Object.defineProperty(dom.window.screen, 'width', { value: 1920, configurable: true });
  Object.defineProperty(dom.window.screen, 'height', { value: 1080, configurable: true });
  Object.defineProperty(dom.window.screen, 'colorDepth', { value: 24, configurable: true });
  Object.defineProperty(dom.window.screen, 'pixelDepth', { value: 24, configurable: true });
  
  // Override XMLHttpRequest to use our fetch
  dom.window.XMLHttpRequest = function() {
    this.open = function(method, url, async) {
      this._method = method;
      this._url = url;
      this._async = async;
    };
    this.setRequestHeader = function(name, value) {
      if (!this._headers) this._headers = {};
      this._headers[name] = value;
    };
    this.send = async function(body) {
      try {
        const fullUrl = this._url.startsWith('http') ? this._url : TARGET + this._url.replace(/^\//, '');
        const resp = await fetch(fullUrl, {
          method: this._method,
          headers: { ...this._headers, Cookie: cookies },
          body: body,
        });
        this.status = resp.status;
        this.responseText = resp.body;
        this.response = resp.body;
        if (this.onload) this.onload();
      } catch(e) {
        console.log('XHR error:', e.message);
        if (this.onerror) this.onerror(e);
      }
    };
  };
  
  // Wait for the challenge to complete
  console.log('Waiting for challenge to solve...');
  await new Promise(r => setTimeout(r, 10000));
  
  // Check if cf_clearance cookie was set
  const docCookies = dom.window.document.cookie;
  console.log('Document cookies:', docCookies);
  
  dom.window.close();
})();
