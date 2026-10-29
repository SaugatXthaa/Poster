const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext({
    userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36',
    viewport: { width: 1280, height: 720 },
  });
  const page = await context.newPage();
  
  const capturedUrls = [];
  page.on('request', (req) => {
    const url = req.url();
    if (url.includes('google') || url.includes('drive') || url.includes('googleusercontent') ||
        url.includes('worker') || url.includes('.m3u8') || url.includes('.mp4') ||
        url.includes('gdshine.org/api') || url.includes('stream') || url.includes('download')) {
      console.log('[REQ ' + req.method() + '] ' + url.substring(0, 250));
      capturedUrls.push(url);
    }
  });

  try {
    console.log('Opening GDShine file page...');
    await page.goto('https://gdshine.org/f/SSio4Pj5', { waitUntil: 'networkidle', timeout: 30000 });
    console.log('Page loaded. Title: ' + await page.title());
    
    // Wait for React to render
    await page.waitForTimeout(5000);
    
    // Look for "Watch Online" button
    const watchBtn = await page.$('text=Watch Online');
    if (watchBtn) {
      console.log('Found "Watch Online" button — clicking...');
      await watchBtn.click();
      await page.waitForTimeout(5000);
      console.log('After click URL: ' + page.url());
      
      // Check for new tabs/windows
      const pages = context.pages();
      console.log('Pages: ' + pages.length);
      for (const p of pages) {
        console.log('  Page URL: ' + p.url());
      }
    } else {
      console.log('No "Watch Online" button found');
      // Try looking for any button with "watch" or "download"
      const allBtns = await page.$$('button, a');
      console.log('All buttons/links:');
      for (const btn of allBtns) {
        const text = await btn.textContent();
        const href = await btn.getAttribute('href');
        if (text && text.trim() && !text.includes('GDShine') && !text.includes('Search')) {
          console.log('  "' + text.trim().substring(0, 50) + '"' + (href ? ' → ' + href.substring(0, 100) : ''));
        }
      }
    }
    
    console.log('\n=== Captured URLs ===');
    capturedUrls.forEach((u, i) => console.log((i+1) + '. ' + u.substring(0, 200)));
  } catch (e) {
    console.error('Error: ' + e.message);
  } finally {
    await browser.close();
  }
})();
