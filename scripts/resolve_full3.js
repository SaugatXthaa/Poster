const { spawn } = require('child_process');
const http = require('http');
const WebSocket = require('ws');

const CHROME = '/home/z/.cache/ms-playwright/chromium-1200/chrome-linux64/chrome';
const Xvfb = spawn('Xvfb', [':105', '-screen', '0', '1920x1080x24'], { stdio: 'pipe' });
process.env.DISPLAY = ':105';

const TEKNO_URL = process.argv[2];

const chrome = spawn(CHROME, [
  '--no-sandbox', '--disable-dev-shm-usage',
  '--disable-blink-features=AutomationControlled',
  '--user-data-dir=/tmp/chrome_resolve3',
  '--remote-debugging-port=9243',
  '--window-size=1920,1080',
], { stdio: 'pipe', env: { ...process.env } });

setTimeout(async () => {
  try {
    const tabs = await new Promise((res, rej) => {
      http.get('http://127.0.0.1:9243/json', (r) => {
        let b = ''; r.on('data', c => b += c);
        r.on('end', () => res(JSON.parse(b)));
      }).on('error', rej);
    });
    
    const page = tabs.find(t => t.type === 'page');
    const ws = new WebSocket(page.webSocketDebuggerUrl);
    let finalUrl = null;
    
    ws.on('open', () => {
      ws.send(JSON.stringify({ id: 1, method: 'Network.enable' }));
      ws.send(JSON.stringify({ id: 2, method: 'Page.enable' }));
      ws.send(JSON.stringify({ id: 3, method: 'Page.navigate', params: { url: TEKNO_URL } }));
      
      // After 5 seconds, check the page and manually execute the redirect
      setTimeout(() => {
        console.log('Checking for redirect script...');
        ws.send(JSON.stringify({
          id: 50,
          method: 'Runtime.evaluate',
          params: {
            expression: `
              // Find the redirect script in the page
              var scripts = document.querySelectorAll('script');
              var redirectUrl = null;
              for (var s of scripts) {
                var text = s.textContent || s.innerText;
                var match = text.match(/window\\.location\\.href\\s*=\\s*['"]([^'"]+)['"]/);
                if (match) {
                  redirectUrl = match[1];
                  break;
                }
              }
              if (redirectUrl) {
                'Found redirect: ' + redirectUrl + ' | Executing...';
                window.location.href = redirectUrl;
              } else {
                'No redirect found. Scripts: ' + scripts.length + ' | URL: ' + document.URL;
              }
            `,
            returnByValue: true
          }
        }));
      }, 5000);
      
      // Monitor
      let checks = 0;
      const interval = setInterval(() => {
        checks++;
        ws.send(JSON.stringify({
          id: 100 + checks,
          method: 'Runtime.evaluate',
          params: {
            expression: 'JSON.stringify({url: document.URL, title: document.title})',
            returnByValue: true
          }
        }));
        if (checks >= 10) {
          clearInterval(interval);
          console.log('\\nFinal URL:', finalUrl || 'not found');
          ws.close(); chrome.kill(); Xvfb.kill(); process.exit(0);
        }
      }, 2000);
    });
    
    ws.on('message', (data) => {
      const msg = JSON.parse(data);
      
      if (msg.method === 'Page.frameNavigated') {
        const frameUrl = msg.params?.frame?.url || '';
        if (frameUrl !== 'about:blank' && !frameUrl.includes('chrome://')) {
          console.log('NAV:', frameUrl.substring(0, 100));
          if (frameUrl.includes('drive.google') || frameUrl.includes('mega.nz') ||
              frameUrl.includes('1fichier') || frameUrl.includes('pixeldrain') ||
              frameUrl.includes('userupload') || frameUrl.includes('gdtot') ||
              frameUrl.includes('gdflix') || frameUrl.includes('workers.dev')) {
            finalUrl = frameUrl;
            console.log('FOUND FILE URL:', frameUrl);
          }
        }
      }
      
      if (msg.method === 'Network.responseReceived') {
        const respUrl = msg.params?.response?.url || '';
        if (respUrl.includes('drive.google') || respUrl.includes('mega.nz') ||
            respUrl.includes('1fichier') || respUrl.includes('pixeldrain') ||
            respUrl.includes('userupload') || respUrl.includes('gdtot') ||
            respUrl.includes('gdflix') || respUrl.includes('workers.dev')) {
          finalUrl = respUrl;
          console.log('NETWORK:', respUrl.substring(0, 100));
        }
      }
      
      if (msg.id === 50) {
        console.log('Redirect check:', msg.result?.result?.value);
      }
      
      if (msg.id && msg.id >= 100) {
        const result = msg.result?.result?.value;
        if (result) {
          try {
            const info = JSON.parse(result);
            console.log('Check', msg.id - 100, '- URL:', info.url?.substring(0, 80));
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

setTimeout(() => { chrome.kill(); Xvfb.kill(); process.exit(1); }, 30000);
