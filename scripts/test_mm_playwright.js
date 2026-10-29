const { chromium } = require('playwright');
const https = require('https');

function fetchJson(url, referer) {
  return new Promise((resolve, reject) => {
    const headers = { 'User-Agent': 'Stremio/4.4.137 (Windows; x64)', 'Accept': 'application/json' };
    if (referer) headers['Referer'] = referer;
    https.get(url, { headers }, (res) => {
      if (res.statusCode !== 200) { res.resume(); return reject(new Error(`HTTP ${res.statusCode}`)); }
      const chunks = [];
      res.on('data', c => chunks.push(c));
      res.on('end', () => { try { resolve(JSON.parse(Buffer.concat(chunks).toString())); } catch(e) { reject(e); } });
    }).on('error', reject);
  });
}

(async () => {
  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext({
    userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36',
    viewport: { width: 1280, height: 720 },
  });
  const page = await context.newPage();
  
  // Block ads
  await context.route('**/dtscout.com**', route => route.abort());
  await context.route('**/histats.com**', route => route.abort());
  await context.route('**/cloudflareinsights.com**', route => route.abort());
  await context.route('**/cdn-cgi/rum**', route => route.abort());
  await context.route('**/googletagmanager.com**', route => route.abort());
  await context.route('**/adsboosters.xyz**', route => route.abort());

  const streamUrls = [];
  page.on('request', (req) => {
    const url = req.url();
    if (url.includes('.m3u8') || url.includes('.mp4') || url.includes('/stream/') ||
        url.includes('master.m3u8') || url.includes('index.m3u8') ||
        url.includes('ddn.iqsmartgames.com') || url.includes('iqsmartgames.com')) {
      console.log(`[${req.method()}] ${url.slice(0, 250)}`);
      if (url.endsWith('.m3u8') || url.includes('.m3u8?')) streamUrls.push(url);
    }
  });
  
  page.on('response', (res) => {
    const url = res.url();
    if (url.includes('.m3u8') || url.includes('master') || url.includes('playlist')) {
      console.log(`[RESP ${res.status()}] ${url.slice(0, 200)}`);
    }
  });

  try {
    // Get the API response first
    const data = await fetchJson('https://streams.iqsmartgames.com/mymovieapi?imdbid=tt15239678&key=e11a7debaaa4f5d25b671706ffe4d2acb56efbd4', 'https://streams.iqsmartgames.com/');
    console.log('API response:', JSON.stringify(data));
    const fileslug = data.data[0].fileslug;
    console.log('\nFileslug:', fileslug);
    
    // Visit the player page
    const playerUrl = `https://pro.iqsmartgames.com/evid/${fileslug}`;
    console.log('\nVisiting player:', playerUrl);
    await page.goto(playerUrl, { waitUntil: 'networkidle', timeout: 30000 });
    console.log('Page loaded.');
    
    // Click the play button if any
    try {
      const playBtn = await page.$('#bigPlay, .play-btn, button[aria-label="Play"], .jw-bigplay');
      if (playBtn) {
        console.log('Clicking play button...');
        await playBtn.click();
      }
    } catch (e) { console.log('No play button:', e.message.slice(0, 80)); }
    
    // Wait for stream activity
    for (let i = 0; i < 30; i++) {
      await page.waitForTimeout(1000);
      if (streamUrls.length > 0) {
        console.log(`Got HLS URL after ${i+1}s`);
        break;
      }
    }
    
    // Check video element
    const video = await page.$('video');
    if (video) {
      const src = await video.getAttribute('src');
      console.log('\nVideo src:', src);
    }
    
    console.log('\n=== Stream URLs captured ===');
    streamUrls.forEach((u, i) => console.log(`${i+1}. ${u.slice(0, 200)}`));
  } catch (e) {
    console.error('Error:', e.message);
  } finally {
    await browser.close();
  }
})();
