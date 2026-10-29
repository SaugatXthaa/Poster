const { spawn } = require('child_process');
const http = require('http');
const WebSocket = require('ws');

const CHROME = '/home/z/.cache/ms-playwright/chromium-1200/chrome-linux64/chrome';
const USER_DATA_DIR = '/tmp/chrome_cf_profile3';
const TARGET_URL = 'https://mkvbase.site/';

const chrome = spawn(CHROME, [
  '--headless=new',
  '--no-sandbox',
  '--disable-gpu',
  '--disable-dev-shm-usage',
  '--user-data-dir=' + USER_DATA_DIR,
  '--remote-debugging-port=9223',
  TARGET_URL,
], { stdio: 'pipe' });

setTimeout(async () => {
  try {
    // Get the list of open tabs
    const tabs = await new Promise((resolve, reject) => {
      http.get('http://127.0.0.1:9223/json', (res) => {
        let body = '';
        res.on('data', c => body += c);
        res.on('end', () => resolve(JSON.parse(body)));
      }).on('error', reject);
    });
    
    const page = tabs.find(t => t.type === 'page');
    if (!page) { console.log('No page found'); process.exit(1); }
    
    console.log('Found page:', page.url);
    
    const ws = new WebSocket(page.webSocketDebuggerUrl);
    
    ws.on('open', () => {
      console.log('Connected to page');
      
      // Enable Network to capture cookies
      ws.send(JSON.stringify({ id: 1, method: 'Network.enable' }));
      ws.send(JSON.stringify({ id: 2, method: 'Page.enable' }));
      
      // Wait for CF to solve
      console.log('Waiting 15s for CF challenge to solve...');
      setTimeout(() => {
        // Get cookies
        ws.send(JSON.stringify({ id: 10, method: 'Network.getCookies', params: { urls: [TARGET_URL] } }));
        
        // Get page content
        ws.send(JSON.stringify({ id: 11, method: 'Runtime.evaluate', params: { expression: 'document.title + " | " + document.URL' } }));
      }, 15000);
    });
    
    ws.on('message', (data) => {
      const msg = JSON.parse(data);
      
      if (msg.id === 10) {
        const cookies = msg.result && msg.result.cookies || [];
        console.log('\n=== Cookies ===');
        for (const c of cookies) {
          console.log(`  ${c.name} = ${c.value.substring(0, 50)}...`);
        }
        
        const cfClearance = cookies.find(c => c.name === 'cf_clearance');
        if (cfClearance) {
          console.log('\nGOT cf_clearance!');
          // Export cookie for use with curl/node fetch
          console.log('Cookie value:', cfClearance.value);
        } else {
          console.log('\nNo cf_clearance found');
        }
      }
      
      if (msg.id === 11) {
        const result = msg.result && msg.result.result;
        console.log('\n=== Page Info ===');
        console.log('Title/URL:', result && result.value);
      }
    });
    
    ws.on('error', (e) => console.log('WS error:', e.message));
    
    // Clean up after 20s
    setTimeout(() => {
      ws.close();
      chrome.kill();
      process.exit(0);
    }, 20000);
  } catch(e) {
    console.log('Error:', e.message);
    chrome.kill();
    process.exit(1);
  }
}, 3000);

setTimeout(() => { chrome.kill(); process.exit(1); }, 25000);
