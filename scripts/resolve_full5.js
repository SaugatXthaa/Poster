const { spawn } = require('child_process');
const http = require('http');
const WebSocket = require('ws');

const CHROME = '/home/z/.cache/ms-playwright/chromium-1200/chrome-linux64/chrome';
const Xvfb = spawn('Xvfb', [':107', '-screen', '0', '1920x1080x24'], { stdio: 'pipe' });
process.env.DISPLAY = ':107';

const TEKNO_URL = process.argv[2];

const chrome = spawn(CHROME, [
  '--no-sandbox', '--disable-dev-shm-usage',
  '--disable-blink-features=AutomationControlled',
  '--user-data-dir=/tmp/chrome_resolve5',
  '--remote-debugging-port=9245',
  '--window-size=1920,1080',
], { stdio: 'pipe', env: { ...process.env } });

setTimeout(async () => {
  try {
    const tabs = await new Promise((res, rej) => {
      http.get('http://127.0.0.1:9245/json', (r) => {
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
      ws.send(JSON.stringify({ id: 3, method: 'Target.setAutoAttach', params: { autoAttach: true, waitForDebuggerOnStart: false, flatten: true } }));
      
      ws.send(JSON.stringify({ id: 5, method: 'Page.navigate', params: { url: TEKNO_URL } }));
      
      // After 5s, execute the redirect via window.open() instead of window.location
      setTimeout(() => {
        console.log('Opening s0-greate.net in popup...');
        ws.send(JSON.stringify({
          id: 50,
          method: 'Runtime.evaluate',
          params: {
            expression: `
              // Find the redirect URL
              var scripts = document.querySelectorAll('script');
              var redirectUrl = null;
              for (var s of scripts) {
                var match = (s.textContent||'').match(/window\\.location\\.href\\s*=\\s*['"]([^'"]+)['"]/);
                if (match) { redirectUrl = match[1]; break; }
              }
              if (redirectUrl) {
                // Open in a new window (popup) so s0-greate.net doesn't call close()
                window.open(redirectUrl, '_blank');
                'Opened popup: ' + redirectUrl;
              } else {
                'No redirect found. URL: ' + document.URL;
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
            expression: 'JSON.stringify({url: document.URL, title: document.title, bodyLen: document.body?.innerHTML?.length || 0})',
            returnByValue: true
          }
        }));
        if (checks >= 15) {
          clearInterval(interval);
          console.log('\\nFinal URL:', finalUrl || 'not found');
          ws.close(); chrome.kill(); Xvfb.kill(); process.exit(0);
        }
      }, 2000);
    });
    
    // Listen for new targets (popups)
    ws.on('message', (data) => {
      const msg = JSON.parse(data);
      
      // New tab/popup opened
      if (msg.method === 'Target.attachedToTarget') {
        const targetInfo = msg.params?.targetInfo;
        if (targetInfo?.type === 'page') {
          console.log('NEW TARGET:', targetInfo.url?.substring(0, 100));
          const sessionId = msg.params?.sessionId;
          
          // Enable Page on the new target
          ws.send(JSON.stringify({ id: 200, method: 'Page.enable', sessionId }));
          ws.send(JSON.stringify({ id: 201, method: 'Network.enable', sessionId }));
          
          // Monitor the popup for redirects
          let popupChecks = 0;
          const popupInterval = setInterval(() => {
            popupChecks++;
            ws.send(JSON.stringify({
              id: 300 + popupChecks,
              method: 'Runtime.evaluate',
              params: {
                expression: 'JSON.stringify({url: document.URL, title: document.title, bodyLen: document.body?.innerHTML?.length || 0})',
                returnByValue: true
              },
              sessionId
            }));
            if (popupChecks >= 10) {
              clearInterval(popupInterval);
              console.log('Popup final URL:', finalUrl || 'not found');
              ws.close(); chrome.kill(); Xvfb.kill(); process.exit(0);
            }
          }, 2000);
        }
      }
      
      // Navigation events
      if (msg.method === 'Page.frameNavigated') {
        const frameUrl = msg.params?.frame?.url || '';
        if (frameUrl !== 'about:blank' && !frameUrl.includes('chrome://') && !frameUrl.includes('about:srcdoc')) {
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
      
      // Network events
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
      
      if (msg.id === 50) console.log('Popup:', msg.result?.result?.value);
      
      if (msg.id && msg.id >= 300) {
        const result = msg.result?.result?.value;
        if (result) {
          try {
            const info = JSON.parse(result);
            console.log('Popup check', msg.id - 300, '- URL:', info.url?.substring(0, 80), '| Body:', info.bodyLen);
            if (info.url && (info.url.includes('drive.google') || info.url.includes('mega.nz') ||
                info.url.includes('1fichier') || info.url.includes('pixeldrain'))) {
              finalUrl = info.url;
              console.log('FOUND FILE URL:', info.url);
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

setTimeout(() => { chrome.kill(); Xvfb.kill(); process.exit(1); }, 40000);
