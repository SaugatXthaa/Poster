const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext({
    userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36',
    viewport: { width: 1280, height: 720 },
    locale: 'en-US',
  });
  const page = await context.newPage();
  
  const capturedUrls = [];
  const capturedResponses = [];
  page.on('request', (req) => {
    const url = req.url();
    if (url.includes('pro.iqsmartgames') || url.includes('evid') || url.includes('svid') || 
        url.includes('hanerix') || url.includes('smoothpre') || url.includes('.m3u8') ||
        url.includes('embedhelper') || url.includes('z0f76')) {
      console.log(`[REQ ${req.method()}] ${url.slice(0, 250)}`);
      capturedUrls.push(url);
    }
  });
  page.on('response', (res) => {
    const url = res.url();
    if (url.includes('pro.iqsmartgames') || url.includes('evid') || url.includes('svid') ||
        url.includes('z0f76') || url.includes('embedhelper')) {
      console.log(`[RESP ${res.status()}] ${url.slice(0, 200)}`);
      capturedResponses.push({ url, status: res.status() });
      // Capture redirect locations
      const loc = res.headers()['location'];
      if (loc) console.log(`  → Location: ${loc}`);
    }
  });
  page.on('framenavigated', (frame) => {
    console.log(`[FRAME] ${frame.url()}`);
  });

  try {
    const filename = 'Breaking.Bad.S01E01.1080p.10Bit.Bluray.Hindi.English.Esubs.mkv';
    const url = `https://hshare.ink/?id=${filename}`;
    console.log(`Visiting: ${url}`);
    
    // Use 'domcontentloaded' instead of 'networkidle' (the challenge page reloads)
    await page.goto(url, { waitUntil: 'domcontentloaded', timeout: 30000 });
    console.log('DOM loaded. Waiting for challenge to resolve...');
    
    // Wait for the page to navigate (the challenge JS reloads the page after ~5s)
    for (let i = 0; i < 30; i++) {
      await page.waitForTimeout(2000);
      const currentUrl = page.url();
      const title = await page.title();
      if (currentUrl.includes('pro.iqsmartgames') || currentUrl.includes('evid') || 
          currentUrl.includes('svid') || currentUrl.includes('hanerix') ||
          currentUrl.includes('smoothpre') || currentUrl.includes('acek-cdn') ||
          currentUrl.includes('premilkyway')) {
        console.log(`✅ Redirected to: ${currentUrl}`);
        break;
      }
      if (title !== 'One moment, please...' && !title.includes('moment')) {
        console.log(`Title changed: ${title}`);
      }
    }
    
    console.log(`\nFinal URL: ${page.url()}`);
    console.log(`Title: ${await page.title()}`);
    
    console.log(`\n=== All captured URLs ===`);
    capturedUrls.forEach((u, i) => console.log(`${i+1}. ${u.slice(0, 200)}`));
    
    console.log(`\n=== All captured responses ===`);
    capturedResponses.forEach((r, i) => console.log(`${i+1}. [${r.status}] ${r.url.slice(0, 200)}`));
  } catch (e) {
    console.error('Error:', e.message);
  } finally {
    await browser.close();
  }
})();
