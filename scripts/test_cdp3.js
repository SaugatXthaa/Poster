const { spawn } = require('child_process');
const http = require('http');
const WebSocket = require('ws');

const CHROME = '/home/z/.cache/ms-playwright/chromium-1200/chrome-linux64/chrome';
const USER_DATA_DIR = '/tmp/chrome_cf_profile4';
const TARGET_URL = 'https://mkvbase.site/';

const chrome = spawn(CHROME, [
  '--headless=new',
  '--no-sandbox',
  '--disable-gpu',
  '--disable-dev-shm-usage',
  '--disable-blink-features=AutomationControlled',
  '--user-data-dir=' + USER_DATA_DIR,
  '--remote-debugging-port=9224',
  '--window-size=1920,1080',
  TARGET_URL,
], { stdio: 'pipe' });

setTimeout(async () => {
  try {
    const tabs = await new Promise((resolve, reject) => {
      http.get('http://127.0.0.1:9224/json', (res) => {
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
      console.log('WS connected');
      ws.send(JSON.stringify({ id: 1, method: 'Network.enable' }));
      ws.send(JSON.stringify({ id: 2, method: 'Page.enable' }));
      
      // Check periodically
      let checkCount = 0;
      const interval = setInterval(() => {
        checkCount++;
        console.log('Check', checkCount, '- getting cookies...');
        ws.send(JSON.stringify({ id: 100 + checkCount, method: 'Network.getCookies', params: { urls: [TARGET_URL] } }));
        ws.send(JSON.stringify({ id: 200 + checkCount, method: 'Runtime.evaluate', params: { expression: 'document.title' } }));
        
        if (checkCount >= 20) {
          clearInterval(interval);
          ws.close();
          chrome.kill();
          process.exit(0);
        }
      }, 2000);
    });
    
    ws.on('message', (data) => {
      const msg = JSON.parse(data);
      
      if (msg.id && msg.id >= 100) {
        const cookies = msg.result && msg.result.cookies || [];
        const cfClearance = cookies.find(c => c.name === 'cf_clearance');
        if (cfClearance) {
          console.log('\n=== GOT cf_clearance! ===');
          console.log('Value:', cfClearance.value.substring(0, 60) + '...');
          
          // Now get the page content
          ws.send(JSON.stringify({ id: 999, method: 'Runtime.evaluate', params: { expression: 'document.documentElement.outerHTML.substring(0, 500)' } }));
          
          // Save the cookie and exit
          require('fs').writeFileSync('/tmp/mkv_cf_cookie.txt', cfClearance.value);
          console.log('Cookie saved to /tmp/mkv_cf_cookie.txt');
          
          clearInterval(global._interval);
          setTimeout(() => { ws.close(); chrome.kill(); process.exit(0); }, 2000);
        }
      }
      
      if (msg.id && msg.id >= 200 && msg.id < 300) {
        const title = msg.result && msg.result.result && msg.result.result.value;
        if (title && !title.includes('Just a moment')) {
          console.log('Page title changed to:', title);
        }
      }
      
      if (msg.id === 999) {
        const html = msg.result && msg.result.result && msg.result.result.value;
        console.log('\nPage content:', html);
      }
    });
    
    ws.on('error', (e) => console.log('WS error:', e.message));
  } catch(e) {
    console.log('Error:', e.message);
    chrome.kill();
    process.exit(1);
  }
}, 3000);

setTimeout(() => { chrome.kill(); process.exit(1); }, 45000);
