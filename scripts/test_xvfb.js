const { spawn, execSync } = require('child_process');
const http = require('http');
const WebSocket = require('ws');
const fs = require('fs');

const CHROME = '/home/z/.cache/ms-playwright/chromium-1200/chrome-linux64/chrome';
const USER_DATA_DIR = '/tmp/chrome_cf_xvfb';
const TARGET_URL = 'https://mkvbase.site/';

// Start Xvfb (virtual display)
const Xvfb = spawn('Xvfb', [':99', '-screen', '0', '1920x1080x24'], { stdio: 'pipe' });
process.env.DISPLAY = ':99';

// Wait for Xvfb to start
setTimeout(() => {
  // Start Chrome in non-headless mode (using the virtual display)
  const chrome = spawn(CHROME, [
    '--no-sandbox',
    '--disable-gpu',
    '--disable-dev-shm-usage',
    '--disable-blink-features=AutomationControlled',
    '--user-data-dir=' + USER_DATA_DIR,
    '--remote-debugging-port=9225',
    '--window-size=1920,1080',
    TARGET_URL,
  ], { stdio: 'pipe', env: { ...process.env } });
  
  setTimeout(async () => {
    try {
      const tabs = await new Promise((resolve, reject) => {
        http.get('http://127.0.0.1:9225/json', (res) => {
          let body = '';
          res.on('data', c => body += c);
          res.on('end', () => resolve(JSON.parse(body)));
        }).on('error', reject);
      });
      
      const page = tabs.find(t => t.type === 'page');
      if (!page) { console.log('No page'); process.exit(1); }
      
      console.log('Connected to:', page.url);
      const ws = new WebSocket(page.webSocketDebuggerUrl);
      
      ws.on('open', () => {
        console.log('WS connected, waiting for CF...');
        ws.send(JSON.stringify({ id: 1, method: 'Network.enable' }));
        ws.send(JSON.stringify({ id: 2, method: 'Page.enable' }));
        
        let checkCount = 0;
        const interval = setInterval(() => {
          checkCount++;
          ws.send(JSON.stringify({ id: 100 + checkCount, method: 'Network.getCookies', params: { urls: [TARGET_URL] } }));
          ws.send(JSON.stringify({ id: 200 + checkCount, method: 'Runtime.evaluate', params: { expression: 'document.title' } }));
          
          if (checkCount >= 15) {
            clearInterval(interval);
            console.log('Timeout - CF challenge not solved');
            ws.close();
            chrome.kill();
            Xvfb.kill();
            process.exit(0);
          }
        }, 2000);
      });
      
      ws.on('message', (data) => {
        const msg = JSON.parse(data);
        
        if (msg.id && msg.id >= 100 && msg.id < 200) {
          const cookies = msg.result && msg.result.cookies || [];
          const cfClearance = cookies.find(c => c.name === 'cf_clearance');
          if (cfClearance) {
            console.log('\n=== GOT cf_clearance! ===');
            fs.writeFileSync('/tmp/mkv_cf_cookie.txt', cfClearance.value);
            console.log('Cookie saved!');
            
            // Get the page content
            ws.send(JSON.stringify({ id: 999, method: 'Runtime.evaluate', params: { expression: 'document.title + " | " + document.URL' } }));
            
            clearInterval(interval);
            setTimeout(() => {
              ws.close();
              chrome.kill();
              Xvfb.kill();
              process.exit(0);
            }, 2000);
          }
        }
        
        if (msg.id && msg.id >= 200 && msg.id < 300) {
          const title = msg.result && msg.result.result && msg.result.result.value;
          if (title) console.log('Title:', title);
        }
        
        if (msg.id === 999) {
          console.log('Page:', msg.result && msg.result.result && msg.result.result.value);
        }
      });
      
      ws.on('error', (e) => console.log('WS error:', e.message));
    } catch(e) {
      console.log('Error:', e.message);
      chrome.kill();
      Xvfb.kill();
      process.exit(1);
    }
  }, 3000);
  
  setTimeout(() => { chrome.kill(); Xvfb.kill(); process.exit(1); }, 35000);
}, 1000);
