const { spawn } = require('child_process');
const http = require('http');
const WebSocket = require('ws');

const CHROME = '/home/z/.cache/ms-playwright/chromium-1200/chrome-linux64/chrome';
const Xvfb = spawn('Xvfb', [':103', '-screen', '0', '1920x1080x24'], { stdio: 'pipe' });
process.env.DISPLAY = ':103';

// Fresh tekno URL from pahe
const TEKNO_URL = process.argv[2] || 'https://teknoasian.com/?ht=hweOXM6IhP88IsH5zIMOQSV4q6jODtJPk6h6A5FcD%2BDAggVsge%2BaMcoruCvUsu35cEpjcFJTZHhaTXhxQS8xNExiKzJhMFRiYktGL0tnS1VEYXpwcnh2bmxpQ3EyWWVscFdFTkpYQ01iY2prbHhrSA%3D%3D';

const chrome = spawn(CHROME, [
  '--no-sandbox', '--disable-dev-shm-usage',
  '--disable-blink-features=AutomationControlled',
  '--user-data-dir=/tmp/chrome_full_resolve',
  '--remote-debugging-port=9241',
  '--window-size=1920,1080',
], { stdio: 'pipe', env: { ...process.env } });

let finalUrl = null;

setTimeout(async () => {
  try {
    const tabs = await new Promise((res, rej) => {
      http.get('http://127.0.0.1:9241/json', (r) => {
        let b = ''; r.on('data', c => b += c);
        r.on('end', () => res(JSON.parse(b)));
      }).on('error', rej);
    });
    
    const page = tabs.find(t => t.type === 'page');
    const ws = new WebSocket(page.webSocketDebuggerUrl);
    
    ws.on('open', () => {
      console.log('Navigating to teknoasian.com...');
      ws.send(JSON.stringify({ id: 1, method: 'Network.enable' }));
      ws.send(JSON.stringify({ id: 2, method: 'Page.enable' }));
      
      // Navigate to the teknoasian URL
      ws.send(JSON.stringify({ id: 3, method: 'Page.navigate', params: { url: TEKNO_URL } }));
      
      // Monitor all network requests to find the final redirect
      let checks = 0;
      const interval = setInterval(() => {
        checks++;
        ws.send(JSON.stringify({
          id: 100 + checks,
          method: 'Runtime.evaluate',
          params: {
            expression: 'JSON.stringify({url: document.URL, title: document.title, body: document.body?.innerHTML?.substring(0, 500)})',
            returnByValue: true
          }
        }));
        
        if (checks >= 15) {
          clearInterval(interval);
          console.log('Final URL:', finalUrl || 'not found');
          ws.close(); chrome.kill(); Xvfb.kill();
          process.exit(0);
        }
      }, 2000);
    });
    
    // Capture all network responses
    ws.on('message', (data) => {
      const msg = JSON.parse(data);
      
      // Capture network responses
      if (msg.method === 'Network.responseReceived') {
        const respUrl = msg.params?.response?.url || '';
        if (respUrl.includes('drive.google') || respUrl.includes('mega.nz') ||
            respUrl.includes('1fichier') || respUrl.includes('pixeldrain') ||
            respUrl.includes('userupload') || respUrl.includes('gdtot') ||
            respUrl.includes('gdflix') || respUrl.includes('workers.dev') ||
            respUrl.includes('hubcloud') || respUrl.includes('fsl-buckets')) {
          finalUrl = respUrl;
          console.log('FOUND FILE URL:', respUrl);
        }
      }
      
      // Capture page navigation
      if (msg.method === 'Page.frameNavigated') {
        const frameUrl = msg.params?.frame?.url || '';
        if (frameUrl.includes('drive.google') || frameUrl.includes('mega.nz') ||
            frameUrl.includes('1fichier') || frameUrl.includes('pixeldrain') ||
            frameUrl.includes('userupload') || frameUrl.includes('gdtot') ||
            frameUrl.includes('gdflix') || frameUrl.includes('workers.dev')) {
          finalUrl = frameUrl;
          console.log('NAVIGATED TO FILE URL:', frameUrl);
        }
      }
      
      // Check page content
      if (msg.id && msg.id >= 100) {
        const result = msg.result?.result?.value;
        if (result) {
          try {
            const info = JSON.parse(result);
            console.log('Check', msg.id - 100, '- URL:', info.url?.substring(0, 80));
            if (info.title) console.log('  Title:', info.title);
            
            // Check if the page redirected to a file host
            if (info.url && (info.url.includes('drive.google') || info.url.includes('mega.nz') ||
                info.url.includes('1fichier') || info.url.includes('pixeldrain'))) {
              finalUrl = info.url;
              console.log('FOUND FILE URL:', info.url);
            }
          } catch {
            console.log('Check', msg.id - 100, ':', result.substring(0, 100));
          }
        }
      }
    });
  } catch(e) {
    console.log('Error:', e.message);
    chrome.kill(); Xvfb.kill();
    process.exit(1);
  }
}, 3000);

setTimeout(() => { chrome.kill(); Xvfb.kill(); process.exit(1); }, 35000);
