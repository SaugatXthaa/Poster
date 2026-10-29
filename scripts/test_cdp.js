const { spawn } = require('child_process');
const http = require('http');
const WebSocket = require('ws');

const CHROME = '/home/z/.cache/ms-playwright/chromium-1200/chrome-linux64/chrome';
const USER_DATA_DIR = '/tmp/chrome_cf_profile2';
const TARGET_URL = 'https://mkvbase.site/';

// Start Chrome with remote debugging
const chrome = spawn(CHROME, [
  '--headless=new',
  '--no-sandbox',
  '--disable-gpu',
  '--disable-dev-shm-usage',
  '--user-data-dir=' + USER_DATA_DIR,
  '--remote-debugging-port=9222',
], { stdio: 'pipe' });

let ws = null;
let cookies = [];

// Wait for Chrome to start, then connect via CDP
setTimeout(async () => {
  try {
    // Get the debug URL
    const data = await new Promise((resolve, reject) => {
      http.get('http://127.0.0.1:9222/json/version', (res) => {
        let body = '';
        res.on('data', c => body += c);
        res.on('end', () => resolve(JSON.parse(body)));
      }).on('error', reject);
    });
    
    console.log('Chrome version:', data.Browser);
    
    // Connect to the browser via WebSocket
    ws = new WebSocket(data.webSocketDebuggerUrl);
    
    ws.on('open', () => {
      console.log('Connected to Chrome');
      
      let msgId = 1;
      function send(method, params = {}) {
        const id = msgId++;
        ws.send(JSON.stringify({ id, method, params }));
        return id;
      }
      
      // Create a new page
      send('Target.createTarget', { url: TARGET_URL });
    });
    
    const pending = new Map();
    ws.on('message', (data) => {
      const msg = JSON.parse(data);
      
      if (msg.id) {
        pending.set(msg.id, msg.result);
        
        if (msg.method === 'Target.targetCreated') {
          const targetId = msg.result.targetId || (msg.params && msg.params.targetInfo && msg.params.targetInfo.targetId);
          if (targetId) {
            console.log('Target created:', targetId);
            
            // Attach to the target
            ws.send(JSON.stringify({
              id: msg.id + 100,
              method: 'Target.attachToTarget',
              params: { targetId, flatten: true },
            }));
          }
        }
      }
      
      if (msg.method === 'Target.attachedToTarget') {
        const sessionId = msg.params.sessionId;
        const targetId = msg.params.targetInfo.targetId;
        console.log('Attached to target, session:', sessionId);
        
        // Enable Page domain
        ws.send(JSON.stringify({
          id: 200,
          method: 'Page.enable',
          params: {},
          sessionId,
        }));
        
        // Navigate to the page
        ws.send(JSON.stringify({
          id: 201,
          method: 'Page.navigate',
          params: { url: TARGET_URL },
          sessionId,
        }));
        
        // Wait for CF to solve, then get cookies
        setTimeout(() => {
          // Get all cookies
          ws.send(JSON.stringify({
            id: 300,
            method: 'Network.getAllCookies',
            params: {},
            sessionId,
          }));
          
          // Get the page content
          ws.send(JSON.stringify({
            id: 301,
            method: 'Runtime.evaluate',
            params: { expression: 'document.documentElement.outerHTML' },
            sessionId,
          }));
        }, 15000);
      }
      
      if (msg.id === 300) {
        cookies = msg.result && msg.result.cookies || [];
        console.log('\nCookies:', cookies.map(c => c.name + '=' + c.value.substring(0, 30)));
        const cfClearance = cookies.find(c => c.name === 'cf_clearance');
        if (cfClearance) {
          console.log('GOT cf_clearance!');
        }
      }
      
      if (msg.id === 301) {
        const html = msg.result && msg.result.result && msg.result.result.value;
        if (html) {
          console.log('\nPage title:', html.match(/<title>([^<]*)<\/title>/)?.[1]);
          console.log('Is CF:', html.includes('Just a moment'));
          console.log('Body preview:', html.substring(0, 200));
        }
        
        // Clean up
        setTimeout(() => {
          ws.close();
          chrome.kill();
          process.exit(0);
        }, 1000);
      }
    });
    
    ws.on('error', (e) => console.log('WS error:', e.message));
  } catch(e) {
    console.log('Error:', e.message);
    chrome.kill();
    process.exit(1);
  }
}, 2000);

// Kill Chrome after 30s
setTimeout(() => {
  console.log('Timeout - killing Chrome');
  chrome.kill();
  process.exit(1);
}, 30000);
