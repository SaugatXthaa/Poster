const { chromium } = require('playwright');

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

  const capturedUrls = [];
  page.on('request', (req) => {
    const url = req.url();
    if (url.includes('pro.iqsmartgames.com') || url.includes('evid') || 
        url.includes('svid') || url.includes('hanerix') || 
        url.includes('smoothpre') || url.includes('.m3u8')) {
      console.log(`[${req.method()}] ${url.slice(0, 250)}`);
      capturedUrls.push(url);
    }
  });

  page.on('response', (res) => {
    const url = res.url();
    if (url.includes('pro.iqsmartgames.com') || url.includes('evid') || 
        url.includes('svid') || url.includes('hanerix')) {
      console.log(`[RESP ${res.status()}] ${url.slice(0, 200)}`);
    }
  });

  try {
    // Visit hshare.ink with the TV episode filename
    const filename = 'Breaking.Bad.S01E01.1080p.10Bit.Bluray.Hindi.English.Esubs.mkv';
    const url = `https://hshare.ink/?id=${filename}`;
    console.log(`Visiting: ${url}`);
    
    await page.goto(url, { waitUntil: 'networkidle', timeout: 30000 });
    console.log('Page loaded.');
    console.log(`Final URL: ${page.url()}`);
    console.log(`Title: ${await page.title()}`);
    
    // Wait for redirect
    for (let i = 0; i < 15; i++) {
      await page.waitForTimeout(1000);
      if (page.url().includes('pro.iqsmartgames.com') || page.url().includes('evid')) {
        console.log(`Redirected to: ${page.url()}`);
        break;
      }
    }
    
    console.log(`\n=== Captured URLs ===`);
    capturedUrls.forEach((u, i) => console.log(`${i+1}. ${u.slice(0, 200)}`));
  } catch (e) {
    console.error('Error:', e.message);
  } finally {
    await browser.close();
  }
})();
