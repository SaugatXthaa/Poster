const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext({
    userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36',
    viewport: { width: 1280, height: 720 },
    locale: 'en-US',
  });
  const page = await context.newPage();
  
  // Block ads + tracking
  await context.route('**/dtscout.com**', route => route.abort());
  await context.route('**/histats.com**', route => route.abort());
  await context.route('**/cloudflareinsights.com**', route => route.abort());
  await context.route('**/cdn-cgi/rum**', route => route.abort());
  await context.route('**/googletagmanager.com**', route => route.abort());
  await context.route('**/adsboosters.xyz**', route => route.abort());
  await context.route('**/al5sm.com**', route => route.abort());

  const allRequests = [];
  page.on('request', (req) => {
    const url = req.url();
    // Log all requests to external domains
    if (!url.includes('hshare.ink') || url.includes('z0f76')) {
      // Also log hshare.ink requests that aren't the initial page
    }
    allRequests.push(url);
    if (url.includes('gdshine') || url.includes('hcloud') || url.includes('gdirect') ||
        url.includes('pro.iqsmart') || url.includes('evid') || url.includes('svid') ||
        url.includes('hanerix') || url.includes('smoothpre') || url.includes('.m3u8') ||
        url.includes('acek-cdn') || url.includes('premilkyway') ||
        url.includes('workers.dev') || url.includes('r2.dev') ||
        url.includes('googleusercontent') || url.includes('video-downloads')) {
      console.log(`[REQ ${req.method()}] ${url.slice(0, 250)}`);
    }
  });
  
  page.on('framenavigated', (frame) => {
    console.log(`[NAV] ${frame.url()}`);
  });
  
  page.on('response', (res) => {
    const url = res.url();
    if (url.includes('gdshine') || url.includes('hcloud') || url.includes('gdirect') ||
        url.includes('pro.iqsmart') || url.includes('evid') || url.includes('svid')) {
      console.log(`[RESP ${res.status()}] ${url.slice(0, 200)}`);
      const loc = res.headers()['location'];
      if (loc) console.log(`  → Location: ${loc}`);
    }
  });

  try {
    // Test with BOTH a movie and TV episode
    const tests = [
      { name: 'Dune Part Two (movie)', url: 'https://hshare.ink/?id=Dune.Part.Two.2024.1080p.10Bit.WEB-DL.Hindi.English.Msubs..mkv' },
      { name: 'Breaking Bad S01E01 (TV)', url: 'https://hshare.ink/?id=Breaking.Bad.S01E01.1080p.10Bit.Bluray.Hindi.English.Esubs.mkv' },
      { name: 'Star Wars Mandalorian 4K', url: 'https://hshare.ink/?id=Star.Wars.The.Mandalorian.and.Grogu.2026.IMAX.2160p.10bit.hevc.Web-DL.Hindi.English.Esub.mkv' },
    ];
    
    for (const test of tests) {
      console.log(`\n=== Testing: ${test.name} ===`);
      console.log(`URL: ${test.url}`);
      
      allRequests.length = 0;
      await page.goto(test.url, { waitUntil: 'domcontentloaded', timeout: 30000 });
      console.log(`DOM loaded. Waiting for redirect...`);
      
      // Wait up to 20s for redirect
      for (let i = 0; i < 20; i++) {
        await page.waitForTimeout(1000);
        const currentUrl = page.url();
        if (currentUrl !== test.url && !currentUrl.includes('hshare.ink')) {
          console.log(`✅ Redirected to: ${currentUrl}`);
          break;
        }
        if (i % 5 === 4) console.log(`  Still waiting... (${i+1}s)`);
      }
      
      const finalUrl = page.url();
      const title = await page.title();
      console.log(`Final URL: ${finalUrl}`);
      console.log(`Title: ${title}`);
      
      // Check if we're on GDShine/hcloud/etc
      if (finalUrl.includes('gdshine') || finalUrl.includes('hcloud') || 
          finalUrl.includes('gdirect') || finalUrl.includes('pro.iqsmart')) {
        console.log('✅ Successfully redirected to file host!');
        
        // Look for "Watch Online" or stream button
        const watchBtn = await page.$('a:has-text("Watch Online"), button:has-text("Watch Online"), a:has-text("Watch"), [class*="watch"]');
        if (watchBtn) {
          console.log('Found Watch Online button — clicking...');
          await watchBtn.click();
          await page.waitForTimeout(5000);
          console.log(`After click: ${page.url()}`);
        }
        
        // Look for video element or iframe
        const video = await page.$('video');
        if (video) {
          const src = await video.getAttribute('src');
          console.log(`Video src: ${src}`);
        }
        const iframe = await page.$('iframe');
        if (iframe) {
          const src = await iframe.getAttribute('src');
          console.log(`Iframe src: ${src}`);
        }
        
        // Get all links on the page
        const links = await page.$$eval('a', els => els.map(e => ({ href: e.href, text: e.textContent.trim() })).filter(l => l.href && l.text));
        console.log('Links on page:');
        links.forEach(l => console.log('  ' + l.text.substring(0,50).padEnd(50) + ' -> ' + l.href.substring(0, 100)));
      }
      
      // Show all captured requests
      console.log(`\nCaptured requests (${allRequests.length}):`);
      allRequests.filter(u => !u.includes('hshare.ink') || allRequests.indexOf(u) === 0).slice(0, 15).forEach((u, i) => {
        console.log(`  ${i+1}. ${u.slice(0, 200)}`);
      });
    }
  } catch (e) {
    console.error('Error:', e.message);
  } finally {
    await browser.close();
  }
})();
