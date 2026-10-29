const { spawn } = require('child_process');
const http = require('http');
const WebSocket = require('ws');

const CHROME = '/home/z/.cache/ms-playwright/chromium-1200/chrome-linux64/chrome';
const Xvfb = spawn('Xvfb', [':106', '-screen', '0', '1920x1080x24'], { stdio: 'pipe' });
process.env.DISPLAY = ':106';

const TEKNO_URL = process.argv[2];

const chrome = spawn(CHROME, [
  '--no-sandbox', '--disable-dev-shm-usage',
  '--disable-blink-features=AutomationControlled',
  '--user-data-dir=/tmp/chrome_resolve4',
  '--remote-debugging-port=9244',
  '--window-size=1920,1080',
], { stdio: 'pipe', env: { ...process.env } });

setTimeout(async () => {
  try {
    const tabs = await new Promise((res, rej) => {
      http.get('http://127.0.0.1:9244/json', (r) => {
        let b = ''; r.on('data', c => b += c);
        r.on('end', () => res(JSON.parse(b)));
      }).on('error', rej);
    });
    
    const page = tabs.find(t => t.type === 'page');
    const ws = new WebSocket(page.webSocketDebuggerUrl);
    let finalUrl = null;
    
    // Enable console and runtime to capture errors
    ws.on('open', () => {
      ws.send(JSON.stringify({ id: 1, method: 'Network.enable' }));
      ws.send(JSON.stringify({ id: 2, method: 'Page.enable' }));
      ws.send(JSON.stringify({ id: 3, method: 'Runtime.enable' }));
      ws.send(JSON.stringify({ id: 4, method: 'Log.enable' }));
      
      ws.send(JSON.stringify({ id: 5, method: 'Page.navigate', params: { url: TEKNO_URL } }));
      
      // After 5s, execute the redirect script
      setTimeout(() => {
        ws.send(JSON.stringify({
          id: 50,
          method: 'Runtime.evaluate',
          params: {
            expression: `
              var scripts = document.querySelectorAll('script');
              var redirectUrl = null;
              for (var s of scripts) {
                var match = (s.textContent||'').match(/window\\.location\\.href\\s*=\\s*['"]([^'"]+)['"]/);
                if (match) { redirectUrl = match[1]; break; }
              }
              if (redirectUrl) { window.location.href = redirectUrl; 'Redirecting to ' + redirectUrl; }
              else { 'No redirect found in ' + scripts.length + ' scripts'; }
            `,
            returnByValue: true
          }
        }));
      }, 5000);
      
      // After 10s (5s after redirect), check s0-greate.net page and manually submit form
      setTimeout(() => {
        console.log('Checking s0-greate.net page...');
        ws.send(JSON.stringify({
          id: 60,
          method: 'Runtime.evaluate',
          params: {
            expression: `
              // Check if form exists on s0-greate.net
              var form = document.getElementById('rf');
              var q = document.getElementById('q');
              var rnd = document.getElementById('rnd');
              if (form && q && rnd) {
                // Set values and submit
                q.value = 'test';
                rnd.value = Math.random().toString(36).substring(7);
                form.submit();
                'Form submitted with q=' + q.value + ' rnd=' + rnd.value;
              } else {
                'Form: ' + (form?'yes':'no') + ' q: ' + (q?q.value:'null') + ' rnd: ' + (rnd?rnd.value:'null') + ' URL: ' + document.URL + ' Body: ' + (document.body?.innerHTML?.substring(0,200)||'empty');
              }
            `,
            returnByValue: true
          }
        }));
      }, 10000);
      
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
    
    ws.on('message', (data) => {
      const msg = JSON.parse(data);
      
      // Capture console errors
      if (msg.method === 'Runtime.consoleAPICalled' || msg.method === 'Log.entryAdded') {
        const text = msg.params?.args?.[0]?.value || msg.params?.entry?.text || '';
        if (text) console.log('CONSOLE:', text.substring(0, 150));
      }
      
      // Capture JS exceptions
      if (msg.method === 'Runtime.exceptionThrown') {
        console.log('EXCEPTION:', msg.params?.exceptionDetails?.text?.substring(0, 150));
      }
      
      if (msg.method === 'Page.frameNavigated') {
        const frameUrl = msg.params?.frame?.url || '';
        if (frameUrl !== 'about:blank' && !frameUrl.includes('chrome://') && !frameUrl.includes('about:srcdoc')) {
          console.log('NAV:', frameUrl.substring(0, 100));
          if (frameUrl.includes('drive.google') || frameUrl.includes('mega.nz') ||
              frameUrl.includes('1fichier') || frameUrl.includes('pixeldrain') ||
              frameUrl.includes('userupload') || frameUrl.includes('gdtot') ||
              frameUrl.includes('gdflix') || frameUrl.includes('workers.dev')) {
            finalUrl = frameUrl;
            console.log('FOUND:', frameUrl);
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
      
      if (msg.id === 50) console.log('Redirect:', msg.result?.result?.value);
      if (msg.id === 60) console.log('Form check:', msg.result?.result?.value);
      
      if (msg.id && msg.id >= 100) {
        const result = msg.result?.result?.value;
        if (result) {
          try {
            const info = JSON.parse(result);
            console.log('Check', msg.id - 100, '- URL:', info.url?.substring(0, 80), '| Body:', info.bodyLen);
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

setTimeout(() => { chrome.kill(); Xvfb.kill(); process.exit(1); }, 40000);
