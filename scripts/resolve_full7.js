const { spawn } = require('child_process');
const http = require('http');
const WebSocket = require('ws');

const CHROME = '/home/z/.cache/ms-playwright/chromium-1200/chrome-linux64/chrome';
const Xvfb = spawn('Xvfb', [':109', '-screen', '0', '1920x1080x24'], { stdio: 'pipe' });
process.env.DISPLAY = ':109';

const TEKNO_URL = process.argv[2];

const chrome = spawn(CHROME, [
  '--no-sandbox', '--disable-dev-shm-usage',
  '--disable-blink-features=AutomationControlled',
  '--user-data-dir=/tmp/chrome_resolve7',
  '--remote-debugging-port=9247',
  '--window-size=1920,1080',
  '--disable-popup-blocking',
], { stdio: 'pipe', env: { ...process.env } });

setTimeout(async () => {
  try {
    const tabs = await new Promise((res, rej) => {
      http.get('http://127.0.0.1:9247/json', (r) => {
        let b = ''; r.on('data', c => b += c);
        r.on('end', () => res(JSON.parse(b)));
      }).on('error', rej);
    });
    
    // Connect to the browser-level WebSocket (not page-level)
    const browserWsUrl = tabs.find(t => t.type === 'browser')?.webSocketDebuggerUrl;
    const pageWsUrl = tabs.find(t => t.type === 'page')?.webSocketDebuggerUrl;
    
    const ws = new WebSocket(pageWsUrl);
    let finalUrl = null;
    
    ws.on('open', () => {
      ws.send(JSON.stringify({ id: 1, method: 'Network.enable' }));
      ws.send(JSON.stringify({ id: 2, method: 'Page.enable' }));
      ws.send(JSON.stringify({ id: 5, method: 'Page.navigate', params: { url: TEKNO_URL } }));
      
      setTimeout(() => {
        // Get redirect URL
        ws.send(JSON.stringify({
          id: 50,
          method: 'Runtime.evaluate',
          params: {
            expression: `(function(){var s=document.querySelectorAll('script');for(var i=0;i<s.length;i++){var m=(s[i].textContent||'').match(/window\\.location\\.href\\s*=\\s*['"]([^'"]+)['"]/);if(m)return m[1]}return null})()`,
            returnByValue: true
          }
        }));
      }, 5000);
    });
    
    ws.on('message', async (data) => {
      const msg = JSON.parse(data);
      
      if (msg.id === 50) {
        const redirectUrl = msg.result?.result?.value;
        console.log('Redirect URL:', redirectUrl);
        
        if (redirectUrl) {
          // Use CDP to create a new target (tab)
          // Connect to browser-level endpoint
          const versionResp = await new Promise((res) => {
            http.get('http://127.0.0.1:9247/json/version', (r) => {
              let b = ''; r.on('data', c => b += c);
              r.on('end', () => res(JSON.parse(b)));
            }).on('error', () => res(null));
          });
          
          // Use PUT to create new tab
          const newTab = await new Promise((resolve) => {
            const req = http.request({
              hostname: '127.0.0.1',
              port: 9247,
              path: '/json/new?' + encodeURIComponent(redirectUrl),
              method: 'PUT',
            }, (r) => {
              let b = ''; r.on('data', c => b += c);
              r.on('end', () => { try { resolve(JSON.parse(b)); } catch { resolve(null); } });
            });
            req.on('error', () => resolve(null));
            req.end();
          });
          
          if (!newTab) {
            // Fallback: use Target.createTarget via WebSocket
            console.log('Trying Target.createTarget...');
            // Connect to browser WS
            const browserWs = new WebSocket(versionResp.webSocketDebuggerUrl);
            browserWs.on('open', () => {
              browserWs.send(JSON.stringify({
                id: 100,
                method: 'Target.createTarget',
                params: { url: redirectUrl }
              }));
            });
            browserWs.on('message', (bdata) => {
              const bmsg = JSON.parse(bdata);
              if (bmsg.id === 100 && bmsg.result?.targetId) {
                console.log('Created target:', bmsg.result.targetId);
                // Get the target info
                setTimeout(async () => {
                  const allTabs = await new Promise((res) => {
                    http.get('http://127.0.0.1:9247/json', (r) => {
                      let b = ''; r.on('data', c => b += c);
                      r.on('end', () => res(JSON.parse(b)));
                    }).on('error', () => res([]));
                  });
                  const newPage = allTabs.find(t => t.url?.includes('s0-greate'));
                  if (newPage) {
                    console.log('Found new tab:', newPage.url?.substring(0, 80));
                    monitorTab(newPage.webSocketDebuggerUrl);
                  }
                }, 2000);
              }
            });
          } else {
            console.log('New tab:', newTab.url?.substring(0, 80));
            monitorTab(newTab.webSocketDebuggerUrl);
          }
        }
      }
    });
    
    function monitorTab(wsUrl) {
      const ws2 = new WebSocket(wsUrl);
      ws2.on('open', () => {
        console.log('Monitoring new tab');
        ws2.send(JSON.stringify({ id: 1, method: 'Network.enable' }));
        ws2.send(JSON.stringify({ id: 2, method: 'Page.enable' }));
        
        let checks = 0;
        const interval = setInterval(() => {
          checks++;
          ws2.send(JSON.stringify({
            id: 100 + checks,
            method: 'Runtime.evaluate',
            params: {
              expression: 'JSON.stringify({url:document.URL,title:document.title,bodyLen:document.body?.innerHTML?.length||0,body:document.body?.innerHTML?.substring(0,300)})',
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
          const url = msg.params?.frame?.url || '';
          if (url !== 'about:blank' && !url.includes('chrome://')) {
            console.log('TAB NAV:', url.substring(0, 100));
            if (url.includes('drive.google') || url.includes('mega.nz') || url.includes('1fichier') || url.includes('pixeldrain') || url.includes('userupload') || url.includes('gdtot') || url.includes('gdflix') || url.includes('workers.dev')) {
              finalUrl = url;
              console.log('FOUND:', url);
            }
          }
        }
        if (msg.method === 'Network.responseReceived') {
          const url = msg.params?.response?.url || '';
          if (url.includes('drive.google') || url.includes('mega.nz') || url.includes('1fichier') || url.includes('pixeldrain') || url.includes('userupload') || url.includes('gdtot') || url.includes('gdflix') || url.includes('workers.dev')) {
            finalUrl = url;
            console.log('TAB NET:', url.substring(0, 100));
          }
        }
        if (msg.id && msg.id >= 100) {
          const result = msg.result?.result?.value;
          if (result) {
            try {
              const info = JSON.parse(result);
              console.log('Check', msg.id - 100, '- URL:', info.url?.substring(0, 80), '| Body:', info.bodyLen);
              if (info.body && info.bodyLen > 0) console.log('  Body:', info.body.substring(0, 150));
              if (info.url && (info.url.includes('drive.google') || info.url.includes('mega.nz') || info.url.includes('1fichier') || info.url.includes('pixeldrain'))) {
                finalUrl = info.url;
                console.log('FOUND:', info.url);
              }
            } catch {}
          }
        }
      });
      ws2.on('error', () => {});
    }
    
    ws.on('error', () => {});
  } catch(e) {
    console.log('Error:', e.message);
    chrome.kill(); Xvfb.kill(); process.exit(1);
  }
}, 3000);

setTimeout(() => { chrome.kill(); Xvfb.kill(); process.exit(1); }, 45000);
