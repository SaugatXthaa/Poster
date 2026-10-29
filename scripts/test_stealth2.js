const { spawn } = require('child_process');
const http = require('http');
const WebSocket = require('ws');
const fs = require('fs');

const CHROME = '/home/z/.cache/ms-playwright/chromium-1200/chrome-linux64/chrome';
const TARGET_URL = 'https://mkvbase.site/';

const Xvfb = spawn('Xvfb', [':101', '-screen', '0', '1920x1080x24'], { stdio: 'pipe' });
process.env.DISPLAY = ':101';

setTimeout(() => {
  const chrome = spawn(CHROME, [
    '--no-sandbox', '--disable-dev-shm-usage',
    '--disable-blink-features=AutomationControlled',
    '--window-size=1920,1080', '--lang=en-US,en',
    '--user-data-dir=/tmp/chrome_stealth2',
    '--remote-debugging-port=9230',
  ], { stdio: 'pipe', env: { ...process.env } });
  
  setTimeout(async () => {
    try {
      const tabs = await new Promise((resolve, reject) => {
        http.get('http://127.0.0.1:9230/json', (res) => {
          let body = '';
          res.on('data', c => body += c);
          res.on('end', () => resolve(JSON.parse(body)));
        }).on('error', reject);
      });
      
      const page = tabs.find(t => t.type === 'page');
      const ws = new WebSocket(page.webSocketDebuggerUrl);
      
      ws.on('open', () => {
        ws.send(JSON.stringify({ id: 1, method: 'Network.enable' }));
        ws.send(JSON.stringify({ id: 2, method: 'Page.enable' }));
        
        // Add stealth
        ws.send(JSON.stringify({
          id: 3,
          method: 'Page.addScriptToEvaluateOnNewDocument',
          params: {
            source: `Object.defineProperty(navigator, 'webdriver', { get: () => false }); window.chrome = { runtime: {} };`
          }
        }));
        
        // Navigate
        console.log('Navigating to mkvbase.site...');
        ws.send(JSON.stringify({ id: 4, method: 'Page.navigate', params: { url: TARGET_URL } }));
        
        // Check every 2s
        let checkCount = 0;
        const interval = setInterval(() => {
          checkCount++;
          ws.send(JSON.stringify({ id: 100 + checkCount, method: 'Network.getCookies', params: { urls: [TARGET_URL] } }));
          ws.send(JSON.stringify({ id: 200 + checkCount, method: 'Runtime.evaluate', params: { expression: 'document.title', returnByValue: true } }));
          if (checkCount >= 15) {
            clearInterval(interval);
            console.log('Timeout');
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
            clearInterval(interval);
            setTimeout(() => { ws.close(); chrome.kill(); Xvfb.kill(); process.exit(0); }, 1000);
          }
        }
        if (msg.id && msg.id >= 200 && msg.id < 300) {
          const title = msg.result?.result?.value;
          if (title) console.log('Check', msg.id - 200, ':', title);
        }
      });
    } catch(e) {
      console.log('Error:', e.message);
      chrome.kill(); Xvfb.kill(); process.exit(1);
    }
  }, 3000);
  setTimeout(() => { chrome.kill(); Xvfb.kill(); process.exit(1); }, 35000);
}, 1000);
