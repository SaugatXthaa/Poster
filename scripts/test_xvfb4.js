const { spawn } = require('child_process');
const http = require('http');
const WebSocket = require('ws');
const fs = require('fs');

const CHROME = '/home/z/.cache/ms-playwright/chromium-1200/chrome-linux64/chrome';
const USER_DATA_DIR = '/tmp/chrome_cf_xvfb4';
const TARGET_URL = 'https://mkvbase.site/';

const Xvfb = spawn('Xvfb', [':99', '-screen', '0', '1920x1080x24'], { stdio: 'pipe' });
process.env.DISPLAY = ':99';

setTimeout(() => {
  const chrome = spawn(CHROME, [
    '--no-sandbox', '--disable-gpu', '--disable-dev-shm-usage',
    '--disable-blink-features=AutomationControlled',
    '--user-data-dir=' + USER_DATA_DIR,
    '--remote-debugging-port=9228',
    '--window-size=1920,1080',
  ], { stdio: 'pipe', env: { ...process.env } });
  
  setTimeout(async () => {
    try {
      const tabs = await new Promise((resolve, reject) => {
        http.get('http://127.0.0.1:9228/json', (res) => {
          let body = '';
          res.on('data', c => body += c);
          res.on('end', () => resolve(JSON.parse(body)));
        }).on('error', reject);
      });
      
      const page = tabs.find(t => t.type === 'page');
      const ws = new WebSocket(page.webSocketDebuggerUrl);
      
      ws.on('open', () => {
        console.log('Setting up...');
        ws.send(JSON.stringify({ id: 1, method: 'Network.enable' }));
        ws.send(JSON.stringify({ id: 2, method: 'Page.enable' }));
        
        // Anti-detection
        ws.send(JSON.stringify({
          id: 3,
          method: 'Page.addScriptToEvaluateOnNewDocument',
          params: {
            source: `Object.defineProperty(navigator, 'webdriver', { get: () => undefined });`
          }
        }));
        
        // Navigate
        console.log('Navigating...');
        ws.send(JSON.stringify({ id: 4, method: 'Page.navigate', params: { url: TARGET_URL } }));
        
        // After 5 seconds, try to click the Turnstile checkbox
        setTimeout(() => {
          console.log('Looking for Turnstile checkbox...');
          
          // Find the Turnstile iframe and click its checkbox
          ws.send(JSON.stringify({
            id: 50,
            method: 'Runtime.evaluate',
            params: {
              expression: `
                // Find the Turnstile iframe
                var iframes = document.querySelectorAll('iframe');
                var tsFrame = null;
                for (var f of iframes) {
                  if (f.src && f.src.includes('challenges.cloudflare.com')) {
                    tsFrame = f;
                    break;
                  }
                }
                if (tsFrame) {
                  // Get the iframe position
                  var rect = tsFrame.getBoundingClientRect();
                  JSON.stringify({found: true, x: rect.x + rect.width/2, y: rect.y + rect.height/2, w: rect.width, h: rect.height});
                } else {
                  JSON.stringify({found: false, iframes: iframes.length});
                }
              `,
              returnByValue: true,
            }
          }));
        }, 5000);
        
        // Check cookies periodically
        let checkCount = 0;
        const interval = setInterval(() => {
          checkCount++;
          ws.send(JSON.stringify({ id: 100 + checkCount, method: 'Network.getCookies', params: { urls: [TARGET_URL] } }));
          
          if (checkCount >= 15) {
            clearInterval(interval);
            console.log('Timeout');
            ws.close(); chrome.kill(); Xvfb.kill(); process.exit(0);
          }
        }, 2000);
      });
      
      ws.on('message', (data) => {
        const msg = JSON.parse(data);
        
        if (msg.id === 50) {
          const result = msg.result?.result?.value;
          console.log('Turnstile search result:', result);
          
          if (result && result.found) {
            // Click on the Turnstile checkbox
            console.log('Clicking at', result.x, result.y);
            ws.send(JSON.stringify({
              id: 60,
              method: 'Input.dispatchMouseEvent',
              params: {
                type: 'mouseMoved',
                x: result.x,
                y: result.y,
              }
            }));
            ws.send(JSON.stringify({
              id: 61,
              method: 'Input.dispatchMouseEvent',
              params: {
                type: 'mousePressed',
                x: result.x,
                y: result.y,
                button: 'left',
                clickCount: 1,
              }
            }));
            ws.send(JSON.stringify({
              id: 62,
              method: 'Input.dispatchMouseEvent',
              params: {
                type: 'mouseReleased',
                x: result.x,
                y: result.y,
                button: 'left',
                clickCount: 1,
              }
            }));
            console.log('Click sent!');
          }
        }
        
        if (msg.id && msg.id >= 100 && msg.id < 200) {
          const cookies = msg.result?.cookies || [];
          const cfClearance = cookies.find(c => c.name === 'cf_clearance');
          if (cfClearance) {
            console.log('\n=== GOT cf_clearance! ===');
            fs.writeFileSync('/tmp/mkv_cf_cookie.txt', cfClearance.value);
            
            ws.send(JSON.stringify({
              id: 999,
              method: 'Runtime.evaluate',
              params: { expression: 'document.title + " | " + document.URL', returnByValue: true }
            }));
            
            clearInterval(interval);
            setTimeout(() => { ws.close(); chrome.kill(); Xvfb.kill(); process.exit(0); }, 2000);
          }
        }
        
        if (msg.id === 999) {
          console.log('Page:', msg.result?.result?.value);
        }
      });
    } catch(e) {
      console.log('Error:', e.message);
      chrome.kill(); Xvfb.kill(); process.exit(1);
    }
  }, 3000);
  
  setTimeout(() => { chrome.kill(); Xvfb.kill(); process.exit(1); }, 35000);
}, 1000);
