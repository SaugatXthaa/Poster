const { spawn } = require('child_process');
const http = require('http');
const WebSocket = require('ws');

const CHROME = '/home/z/.cache/ms-playwright/chromium-1200/chrome-linux64/chrome';
const Xvfb = spawn('Xvfb', [':102', '-screen', '0', '1920x1080x24'], { stdio: 'pipe' });
process.env.DISPLAY = ':102';

async function resolveWithChrome(url) {
  return new Promise((resolve) => {
    const chrome = spawn(CHROME, [
      '--no-sandbox', '--disable-dev-shm-usage',
      '--disable-blink-features=AutomationControlled',
      '--user-data-dir=/tmp/chrome_s0_resolve',
      '--remote-debugging-port=9240',
      '--window-size=1920,1080',
    ], { stdio: 'pipe', env: { ...process.env } });
    
    let resolved = false;
    let ws = null;
    
    setTimeout(async () => {
      try {
        const tabs = await new Promise((res, rej) => {
          http.get('http://127.0.0.1:9240/json', (r) => {
            let b = ''; r.on('data', c => b += c);
            r.on('end', () => res(JSON.parse(b)));
          }).on('error', rej);
        });
        
        const page = tabs.find(t => t.type === 'page');
        ws = new WebSocket(page.webSocketDebuggerUrl);
        
        ws.on('open', () => {
          ws.send(JSON.stringify({ id: 1, method: 'Network.enable' }));
          ws.send(JSON.stringify({ id: 2, method: 'Page.enable' }));
          
          // Navigate to the URL
          ws.send(JSON.stringify({ id: 3, method: 'Page.navigate', params: { url } }));
          
          // Check periodically for navigation/redirect
          let checks = 0;
          const interval = setInterval(() => {
            checks++;
            // Get current URL and page content
            ws.send(JSON.stringify({
              id: 100 + checks,
              method: 'Runtime.evaluate',
              params: { expression: 'document.URL + " | " + document.title + " | " + document.body?.innerHTML?.substring(0, 200)', returnByValue: true }
            }));
            
            if (checks >= 10) {
              clearInterval(interval);
              if (!resolved) {
                resolved = true;
                resolve(null);
                ws.close();
                chrome.kill();
                Xvfb.kill();
              }
            }
          }, 2000);
        });
        
        ws.on('message', (data) => {
          const msg = JSON.parse(data);
          if (msg.id && msg.id >= 100) {
            const result = msg.result?.result?.value || '';
            console.log('Check', msg.id - 100, ':', result.substring(0, 150));
            
            // Check if we got redirected to a file host
            if (result.includes('drive.google.com') || result.includes('mega.nz') ||
                result.includes('1fichier') || result.includes('pixeldrain') ||
                result.includes('userupload') || result.includes('gdtot') ||
                result.includes('gdflix') || result.includes('workers.dev')) {
              if (!resolved) {
                resolved = true;
                // Extract the URL
                const urlMatch = result.match(/https:\/\/[^"'\s<>]+/);
                resolve(urlMatch ? urlMatch[0] : result);
                clearInterval(global._interval);
                ws.close();
                chrome.kill();
                Xvfb.kill();
              }
            }
          }
        });
      } catch(e) {
        if (!resolved) { resolved = true; resolve(null); chrome.kill(); Xvfb.kill(); }
      }
    }, 3000);
    
    setTimeout(() => {
      if (!resolved) { resolved = true; resolve(null); }
      if (ws) ws.close();
      chrome.kill();
      Xvfb.kill();
    }, 25000);
  });
}

// Test
(async () => {
  console.log('Resolving s0-greate.net URL via Chrome...');
  const result = await resolveWithChrome('https://s0-greate.net/go/1475978');
  console.log('Result:', result);
  process.exit(0);
})();
