const { spawn } = require('child_process');
const http = require('http');
const WebSocket = require('ws');

const CHROME = '/home/z/.cache/ms-playwright/chromium-1200/chrome-linux64/chrome';
const Xvfb = spawn('Xvfb', [':104', '-screen', '0', '1920x1080x24'], { stdio: 'pipe' });
process.env.DISPLAY = ':104';

const TEKNO_URL = process.argv[2];

const chrome = spawn(CHROME, [
  '--no-sandbox', '--disable-dev-shm-usage',
  '--disable-blink-features=AutomationControlled',
  '--user-data-dir=/tmp/chrome_resolve2',
  '--remote-debugging-port=9242',
  '--window-size=1920,1080',
], { stdio: 'pipe', env: { ...process.env } });

let finalUrl = null;

setTimeout(async () => {
  try {
    const tabs = await new Promise((res, rej) => {
      http.get('http://127.0.0.1:9242/json', (r) => {
        let b = ''; r.on('data', c => b += c);
        r.on('end', () => res(JSON.parse(b)));
      }).on('error', rej);
    });
    
    const page = tabs.find(t => t.type === 'page');
    const ws = new WebSocket(page.webSocketDebuggerUrl);
    
    ws.on('open', () => {
      console.log('Connected to Chrome');
      ws.send(JSON.stringify({ id: 1, method: 'Network.enable' }));
      ws.send(JSON.stringify({ id: 2, method: 'Page.enable' }));
      
      // Navigate to teknoasian URL
      console.log('Navigating to:', TEKNO_URL?.substring(0, 60) + '...');
      ws.send(JSON.stringify({ id: 3, method: 'Page.navigate', params: { url: TEKNO_URL } }));
      
      // After 3 seconds, check the page and manually submit the form if needed
      setTimeout(() => {
        console.log('Checking page state...');
        ws.send(JSON.stringify({
          id: 50,
          method: 'Runtime.evaluate',
          params: {
            expression: `
              // Check if the form exists
              var form = document.getElementById('xq');
              if (form) {
                // Submit the form manually
                form.submit();
                'Form submitted!';
              } else {
                // Check current URL and page content
                'URL: ' + document.URL + ' | Title: ' + document.title + ' | Has form: ' + (form !== null);
              }
            `,
            returnByValue: true
          }
        }));
      }, 3000);
      
      // Monitor navigation
      let checks = 0;
      const interval = setInterval(() => {
        checks++;
        ws.send(JSON.stringify({
          id: 100 + checks,
          method: 'Runtime.evaluate',
          params: {
            expression: 'JSON.stringify({url: document.URL, title: document.title, bodyLen: document.body?.innerHTML?.length || 0})',
            returnByValue: true
          }
        }));
        
        if (checks >= 12) {
          clearInterval(interval);
          console.log('\\nFinal URL:', finalUrl || 'not found');
          ws.close(); chrome.kill(); Xvfb.kill();
          process.exit(0);
        }
      }, 2000);
    });
    
    ws.on('message', (data) => {
      const msg = JSON.parse(data);
      
      // Capture network responses
      if (msg.method === 'Network.responseReceived') {
        const respUrl = msg.params?.response?.url || '';
        if (respUrl.includes('drive.google') || respUrl.includes('mega.nz') ||
            respUrl.includes('1fichier') || respUrl.includes('pixeldrain') ||
            respUrl.includes('userupload') || respUrl.includes('gdtot') ||
            respUrl.includes('gdflix') || respUrl.includes('workers.dev') ||
            respUrl.includes('hubcloud') || respUrl.includes('fsl-buckets') ||
            respUrl.includes('s0-greate')) {
          console.log('NETWORK:', respUrl.substring(0, 100));
          if (!respUrl.includes('s0-greate')) {
            finalUrl = respUrl;
          }
        }
      }
      
      // Capture page navigation
      if (msg.method === 'Page.frameNavigated') {
        const frameUrl = msg.params?.frame?.url || '';
        if (frameUrl !== 'about:blank' && frameUrl !== 'chrome://intro/') {
          console.log('NAV:', frameUrl.substring(0, 100));
        }
        if (frameUrl.includes('drive.google') || frameUrl.includes('mega.nz') ||
            frameUrl.includes('1fichier') || frameUrl.includes('pixeldrain')) {
          finalUrl = frameUrl;
          console.log('FOUND FILE URL:', frameUrl);
        }
      }
      
      if (msg.id === 50) {
        console.log('Form check:', msg.result?.result?.value);
      }
      
      if (msg.id && msg.id >= 100) {
        const result = msg.result?.result?.value;
        if (result) {
          try {
            const info = JSON.parse(result);
            console.log('Check', msg.id - 100, '- URL:', info.url?.substring(0, 80), '| Body:', info.bodyLen, 'bytes');
            if (info.url && (info.url.includes('drive.google') || info.url.includes('mega.nz') ||
                info.url.includes('1fichier') || info.url.includes('pixeldrain'))) {
              finalUrl = info.url;
            }
          } catch {}
        }
      }
    });
  } catch(e) {
    console.log('Error:', e.message);
    chrome.kill(); Xvfb.kill(); process.exit(1);
  }
}, 3000);

setTimeout(() => { chrome.kill(); Xvfb.kill(); process.exit(1); }, 35000);
