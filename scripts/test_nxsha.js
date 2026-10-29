const { chromium } = require('playwright');
const https = require('https');

function fetchText(url) {
  return new Promise((resolve, reject) => {
    https.get(url, {
      headers: { 'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36' }
    }, (res) => {
      if (res.statusCode !== 200) { res.resume(); return reject(new Error(`HTTP ${res.statusCode}`)); }
      const chunks = [];
      res.on('data', c => chunks.push(c));
      res.on('end', () => resolve(Buffer.concat(chunks).toString()));
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
  
  await context.route('**/dtscout.com**', route => route.abort());
  await context.route('**/histats.com**', route => route.abort());
  await context.route('**/cloudflareinsights.com**', route => route.abort());
  await context.route('**/cdn-cgi/rum**', route => route.abort());
  await context.route('**/googletagmanager.com**', route => route.abort());
  await context.route('**/adsboosters.xyz**', route => route.abort());
  await context.route('**/cosedcost.com**', route => route.abort());

  const streamUrls = [];
  const apiCalls = [];
  
  page.on('request', (req) => {
    const url = req.url();
    if (url.includes('.m3u8') || url.includes('.mp4') || url.includes('/stream/') ||
        url.includes('master.m3u8') || url.includes('playlist') || url.includes('/api/')) {
      console.log(`[${req.method()}] ${url.slice(0, 250)}`);
      if (url.includes('.m3u8')) streamUrls.push(url);
      if (url.includes('/api/')) apiCalls.push({ url, method: req.method() });
    }
  });

  page.on('response', (res) => {
    const url = res.url();
    if (url.includes('.m3u8') || url.includes('master') || url.includes('playlist')) {
      console.log(`[RESP ${res.status()}] ${url.slice(0, 200)}`);
    }
  });

  try {
    // Try multiple titles
    const titles = [
      { id: 'tt15239678', name: 'Dune Part Two', url: 'https://nxsha.space/embed/movie/tt15239678' },
      { id: 'tt10872600', name: 'Spider-Man NWH', url: 'https://nxsha.space/embed/movie/tt10872600' },
      { id: 'tt1630029', name: 'Avatar Way of Water', url: 'https://nxsha.space/embed/movie/tt1630029' },
    ];
    
    for (const t of titles) {
      console.log(`\n=== Testing ${t.name} (${t.id}) ===`);
      streamUrls.length = 0;
      apiCalls.length = 0;
      
      await page.goto(t.url, { waitUntil: 'networkidle', timeout: 30000 });
      console.log('Page loaded.');
      
      // Wait for stream activity
      for (let i = 0; i < 20; i++) {
        await page.waitForTimeout(1000);
        if (streamUrls.length > 0) {
          console.log(`Got HLS URL after ${i+1}s`);
          break;
        }
      }
      
      // Look for video element
      const video = await page.$('video');
      if (video) {
        const src = await video.getAttribute('src');
        if (src) {
          console.log(`Video src: ${src.slice(0, 200)}`);
          streamUrls.push(src);
        }
      }
      
      console.log(`\nStream URLs: ${streamUrls.length}`);
      streamUrls.forEach((u, i) => console.log(`  ${i+1}. ${u.slice(0, 200)}`));
      console.log(`API calls: ${apiCalls.length}`);
      apiCalls.forEach((a, i) => console.log(`  ${i+1}. [${a.method}] ${a.url.slice(0, 200)}`));
    }
  } catch (e) {
    console.error('Error:', e.message);
  } finally {
    await browser.close();
  }
})();
