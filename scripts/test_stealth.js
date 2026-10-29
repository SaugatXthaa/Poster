const { spawn } = require('child_process');
const http = require('http');
const WebSocket = require('ws');
const fs = require('fs');

const CHROME = '/home/z/.cache/ms-playwright/chromium-1200/chrome-linux64/chrome';
const TARGET_URL = 'https://mkvbase.site/';

const Xvfb = spawn('Xvfb', [':100', '-screen', '0', '1920x1080x24'], { stdio: 'pipe' });
process.env.DISPLAY = ':100';

setTimeout(() => {
  const chrome = spawn(CHROME, [
    '--no-sandbox',
    '--disable-dev-shm-usage',
    '--disable-blink-features=AutomationControlled',
    '--disable-features=IsolateOrigins,site-per-process',
    '--disable-infobars',
    '--window-size=1920,1080',
    '--lang=en-US,en',
    '--user-data-dir=/tmp/chrome_cf_stealth',
    '--remote-debugging-port=9229',
    '--password-store=basic',
    '--use-mock-keychain',
    // Don't use --disable-gpu as it's a detection signal
    '--enable-features=NetworkService,NetworkServiceInProcess',
    TARGET_URL,
  ], { stdio: 'pipe', env: { ...process.env } });
  
  setTimeout(async () => {
    try {
      const tabs = await new Promise((resolve, reject) => {
        http.get('http://127.0.0.1:9229/json', (res) => {
          let body = '';
          res.on('data', c => body += c);
          res.on('end', () => resolve(JSON.parse(body)));
        }).on('error', reject);
      });
      
      const page = tabs.find(t => t.type === 'page');
      if (!page) { console.log('No page'); process.exit(1); }
      
      console.log('Page URL:', page.url);
      const ws = new WebSocket(page.webSocketDebuggerUrl);
      
      ws.on('open', () => {
        console.log('Connected, adding stealth scripts...');
        ws.send(JSON.stringify({ id: 1, method: 'Network.enable' }));
        ws.send(JSON.stringify({ id: 2, method: 'Page.enable' }));
        
        // Comprehensive stealth script
        ws.send(JSON.stringify({
          id: 3,
          method: 'Page.addScriptToEvaluateOnNewDocument',
          params: {
            source: `
              // 1. Remove webdriver
              Object.defineProperty(navigator, 'webdriver', { get: () => false });
              
              // 2. Fake plugins
              Object.defineProperty(navigator, 'plugins', {
                get: () => {
                  const plugins = [
                    { name: 'Chrome PDF Plugin', filename: 'internal-pdf-viewer', description: 'Portable Document Format' },
                    { name: 'Chrome PDF Viewer', filename: 'mhjfbmdgcfjbbpaeojofohoefgiehjai', description: '' },
                    { name: 'Native Client', filename: 'internal-nacl-plugin', description: '' }
                  ];
                  plugins.length = 3;
                  return plugins;
                }
              });
              
              // 3. Fake languages
              Object.defineProperty(navigator, 'languages', { get: () => ['en-US', 'en'] });
              
              // 4. Add window.chrome
              window.chrome = {
                runtime: {},
                loadTimes: function() { return {}; },
                csi: function() { return {}; },
                app: {},
              };
              
              // 5. Override permissions
              const originalQuery = window.navigator.permissions.query;
              window.navigator.permissions.query = (parameters) =>
                parameters.name === 'notifications'
                  ? Promise.resolve({ state: Notification.permission })
                  : originalQuery(parameters);
              
              // 6. Override WebGL vendor
              const getParameter = WebGLRenderingContext.prototype.getParameter;
              WebGLRenderingContext.prototype.getParameter = function(parameter) {
                if (parameter === 37445) return 'Intel Inc.';
                if (parameter === 37446) return 'Intel Iris OpenGL Engine';
                return getParameter.call(this, parameter);
              };
              
              // 7. Override hardwareConcurrency
              Object.defineProperty(navigator, 'hardwareConcurrency', { get: () => 4 });
              
              // 8. Override deviceMemory
              Object.defineProperty(navigator, 'deviceMemory', { get: () => 8 });
              
              // 9. Remove CDP artifacts
              const cdcProps = Object.getOwnPropertyNames(window).filter(p => p.startsWith('cdc_'));
              for (const p of cdcProps) { delete window[p]; }
              
              // 10. Override Notification
              if (!window.Notification) {
                window.Notification = { permission: 'default', requestPermission: () => Promise.resolve('default') };
              }
            `
          }
        }));
        
        // Reload the page to apply stealth scripts
        console.log('Reloading page with stealth...');
        ws.send(JSON.stringify({ id: 4, method: 'Page.reload', params: { ignoreCache: true } }));
        
        // Check periodically
        let checkCount = 0;
        const interval = setInterval(() => {
          checkCount++;
          ws.send(JSON.stringify({ id: 100 + checkCount, method: 'Network.getCookies', params: { urls: [TARGET_URL] } }));
          ws.send(JSON.stringify({
            id: 200 + checkCount,
            method: 'Runtime.evaluate',
            params: { expression: 'document.title', returnByValue: true }
          }));
          
          if (checkCount >= 15) {
            clearInterval(interval);
            console.log('Timeout after 30s');
            ws.close(); chrome.kill(); Xvfb.kill(); process.exit(0);
          }
        }, 2000);
      });
      
      ws.on('message', (data) => {
        const msg = JSON.parse(data);
        
        if (msg.id && msg.id >= 100 && msg.id < 200) {
          const cookies = msg.result?.cookies || [];
          const cfClearance = cookies.find(c => c.name === 'cf_clearance');
          if (cfClearance) {
            console.log('\n=== GOT cf_clearance! ===');
            fs.writeFileSync('/tmp/mkv_cf_cookie.txt', cfClearance.value);
            console.log('Cookie value:', cfClearance.value.substring(0, 60));
            
            ws.send(JSON.stringify({
              id: 999,
              method: 'Runtime.evaluate',
              params: { expression: 'document.title + " | " + document.URL', returnByValue: true }
            }));
            
            clearInterval(interval);
            setTimeout(() => { ws.close(); chrome.kill(); Xvfb.kill(); process.exit(0); }, 2000);
          }
        }
        
        if (msg.id && msg.id >= 200 && msg.id < 300) {
          const title = msg.result?.result?.value;
          if (title && !title.includes('Just a moment')) {
            console.log('Title changed:', title);
          }
        }
        
        if (msg.id === 999) {
          console.log('Final page:', msg.result?.result?.value);
        }
      });
    } catch(e) {
      console.log('Error:', e.message);
      chrome.kill(); Xvfb.kill(); process.exit(1);
    }
  }, 3000);
  
  setTimeout(() => { chrome.kill(); Xvfb.kill(); process.exit(1); }, 35000);
}, 1000);
