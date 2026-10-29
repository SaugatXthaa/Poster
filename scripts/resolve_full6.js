const { spawn } = require('child_process');
const http = require('http');
const WebSocket = require('ws');

const CHROME = '/home/z/.cache/ms-playwright/chromium-1200/chrome-linux64/chrome';
const Xvfb = spawn('Xvfb', [':108', '-screen', '0', '1920x1080x24'], { stdio: 'pipe' });
process.env.DISPLAY = ':108';

const TEKNO_URL = process.argv[2];

const chrome = spawn(CHROME, [
  '--no-sandbox', '--disable-dev-shm-usage',
  '--disable-blink-features=AutomationControlled',
  '--user-data-dir=/tmp/chrome_resolve6',
  '--remote-debugging-port=9246',
  '--window-size=1920,1080',
  '--disable-popup-blocking',  // Allow popups!
], { stdio: 'pipe', env: { ...process.env } });

setTimeout(async () => {
  try {
    const getTabs = () => new Promise((res, rej) => {
      http.get('http://127.0.0.1:9246/json', (r) => {
        let b = ''; r.on('data', c => b += c);
        r.on('end', () => res(JSON.parse(b)));
      }).on('error', rej);
    });
    
    const tabs1 = await getTabs();
    const page = tabs1.find(t => t.type === 'page');
    const ws = new WebSocket(page.webSocketDebuggerUrl);
    let finalUrl = null;
    
    ws.on('open', () => {
      ws.send(JSON.stringify({ id: 1, method: 'Network.enable' }));
      ws.send(JSON.stringify({ id: 2, method: 'Page.enable' }));
      ws.send(JSON.stringify({ id: 5, method: 'Page.navigate', params: { url: TEKNO_URL } }));
      
      // After 5s, find the redirect URL and open it as a new tab
      setTimeout(async () => {
        // Get the redirect URL from the page
        const evalResult = await new Promise((resolve) => {
          ws.send(JSON.stringify({
            id: 50,
            method: 'Runtime.evaluate',
            params: {
              expression: `(function() {
                var scripts = document.querySelectorAll('script');
                for (var s of scripts) {
                  var match = (s.textContent||'').match(/window\\.location\\.href\\s*=\\s*['"]([^'"]+)['"]/);
                  if (match) return match[1];
                }
                return null;
              })()`,
              returnByValue: true
            }
          }));
          const handler = (data) => {
            const msg = JSON.parse(data);
            if (msg.id === 50) {
              ws.off('message', handler);
              resolve(msg.result?.result?.value);
            }
          };
          ws.on('message', handler);
        });
        
        console.log('Redirect URL:', evalResult);
        
        if (evalResult) {
          // Create a new tab with the s0-greate.net URL
          console.log('Creating new tab for s0-greate.net...');
          const newTab = await new Promise((resolve) => {
            http.get('http://127.0.0.1:9246/json/new?' + encodeURIComponent(evalResult), (r) => {
              let b = ''; r.on('data', c => b += c);
              r.on('end', () => resolve(JSON.parse(b)));
            }).on('error', () => resolve(null));
          });
          
          if (newTab) {
            console.log('New tab created:', newTab.url?.substring(0, 80));
            // Connect to the new tab
            const ws2 = new WebSocket(newTab.webSocketDebuggerUrl);
            
            ws2.on('open', () => {
              console.log('Connected to new tab');
              ws2.send(JSON.stringify({ id: 1, method: 'Network.enable' }));
              ws2.send(JSON.stringify({ id: 2, method: 'Page.enable' }));
              
              // Monitor the new tab
              let checks = 0;
              const interval = setInterval(() => {
                checks++;
                ws2.send(JSON.stringify({
                  id: 100 + checks,
                  method: 'Runtime.evaluate',
                  params: {
                    expression: 'JSON.stringify({url: document.URL, title: document.title, bodyLen: document.body?.innerHTML?.length || 0, body: document.body?.innerHTML?.substring(0, 200)})',
                    returnByValue: true
                  }
                }));
                if (checks >= 12) {
                  clearInterval(interval);
                  console.log('\\nFinal URL:', finalUrl || 'not found');
                  ws2.close(); ws.close(); chrome.kill(); Xvfb.kill();
                  process.exit(0);
                }
              }, 2000);
            });
            
            ws2.on('message', (data) => {
              const msg = JSON.parse(data);
              
              if (msg.method === 'Page.frameNavigated') {
                const frameUrl = msg.params?.frame?.url || '';
                if (frameUrl !== 'about:blank' && !frameUrl.includes('chrome://')) {
                  console.log('TAB NAV:', frameUrl.substring(0, 100));
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
                  console.log('TAB NETWORK:', respUrl.substring(0, 100));
                }
              }
              
              if (msg.id && msg.id >= 100) {
                const result = msg.result?.result?.value;
                if (result) {
                  try {
                    const info = JSON.parse(result);
                    console.log('Tab check', msg.id - 100, '- URL:', info.url?.substring(0, 80), '| Body:', info.bodyLen);
                    if (info.body && info.body.length > 0) {
                      console.log('  Body:', info.body.substring(0, 100));
                    }
                    if (info.url && (info.url.includes('drive.google') || info.url.includes('mega.nz') ||
                        info.url.includes('1fichier') || info.url.includes('pixeldrain'))) {
                      finalUrl = info.url;
                      console.log('FOUND FILE URL:', info.url);
                    }
                  } catch {}
                }
              }
            });
            
            ws2.on('error', () => {});
          }
        }
      }, 5000);
    });
    
    ws.on('error', () => {});
  } catch(e) {
    console.log('Error:', e.message);
    chrome.kill(); Xvfb.kill(); process.exit(1);
  }
}, 3000);

setTimeout(() => { chrome.kill(); Xvfb.kill(); process.exit(1); }, 45000);
