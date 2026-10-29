const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch({ 
    headless: true,
    args: ['--disable-blink-features=AutomationControlled'],
  });
  const context = await browser.newContext({
    // Use a REAL Chrome UA (not HeadlessChrome)
    userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36',
    viewport: { width: 1280, height: 720 },
    locale: 'en-US',
    extraHTTPHeaders: {
      'Accept-Language': 'en-US,en;q=0.9',
    },
  });
  
  // Remove webdriver flag
  await context.addInitScript(() => {
    Object.defineProperty(navigator, 'webdriver', { get: () => undefined });
    // Override plugins
    Object.defineProperty(navigator, 'plugins', {
      get: () => [1, 2, 3, 4, 5],
    });
    // Override languages
    Object.defineProperty(navigator, 'languages', {
      get: () => ['en-US', 'en'],
    });
  });
  
  const page = await context.newPage();
  
  // Block ads
  await context.route('**/dtscout.com**', route => route.abort());
  await context.route('**/histats.com**', route => route.abort());
  await context.route('**/cloudflareinsights.com**', route => route.abort());
  await context.route('**/cdn-cgi/rum**', route => route.abort());
  await context.route('**/googletagmanager.com**', route => route.abort());
  await context.route('**/adsboosters.xyz**', route => route.abort());
  await context.route('**/al5sm.com**', route => route.abort());
  await context.route('**/tailwindcss.com**', route => route.abort());

  const capturedUrls = [];
  page.on('request', (req) => {
    const url = req.url();
    if (url.includes('gdshine') || url.includes('hcloud') || url.includes('gdirect') ||
        url.includes('pro.iqsmart') || url.includes('evid') || url.includes('svid') ||
        url.includes('hanerix') || url.includes('smoothpre') || url.includes('.m3u8') ||
        url.includes('workers.dev') || url.includes('r2.dev') ||
        url.includes('googleusercontent') || url.includes('video-downloads') ||
        url.includes('z0f76')) {
      console.log('[REQ ' + req.method() + '] ' + url.substring(0, 250));
      capturedUrls.push(url);
    }
  });
  
  page.on('framenavigated', (frame) => {
    console.log('[NAV] ' + frame.url());
  });

  try {
    const filename = 'Dune.Part.Two.2024.1080p.10Bit.WEB-DL.Hindi.English.Msubs..mkv';
    const url = 'https://hshare.ink/?id=' + filename;
    console.log('Visiting: ' + url);
    
    await page.goto(url, { waitUntil: 'domcontentloaded', timeout: 30000 });
    console.log('DOM loaded. Waiting for challenge to resolve...');
    
    for (let i = 0; i < 30; i++) {
      await page.waitForTimeout(2000);
      const currentUrl = page.url();
      const title = await page.title();
      console.log('  ' + (i*2) + 's: URL=' + currentUrl.substring(0, 80) + ' | Title=' + title);
      
      if (!currentUrl.includes('hshare.ink')) {
        console.log('REDIRECTED to: ' + currentUrl);
        break;
      }
      if (title !== 'One moment, please...' && !title.includes('moment') && !title.includes('Admin')) {
        console.log('Title changed: ' + title);
      }
    }
    
    console.log('\nFinal URL: ' + page.url());
    console.log('Title: ' + await page.title());
    console.log('\nCaptured URLs:');
    capturedUrls.forEach((u, i) => console.log('  ' + (i+1) + '. ' + u.substring(0, 200)));
    
    // Get page content
    const content = await page.content();
    console.log('\nPage content (first 2000):');
    console.log(content.substring(0, 2000));
  } catch (e) {
    console.error('Error: ' + e.message);
  } finally {
    await browser.close();
  }
})();
