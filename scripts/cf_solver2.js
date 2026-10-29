const { chromium } = require('playwright');
const fs = require('fs');

(async () => {
  const browser = await chromium.launch({
    headless: false,
    args: ['--no-sandbox', '--disable-dev-shm-usage', '--disable-blink-features=AutomationControlled']
  });
  const context = await browser.newContext({
    userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36',
    viewport: { width: 1920, height: 1080 },
    locale: 'en-US',
  });

  // Stealth: remove webdriver flag
  await context.addInitScript(() => {
    Object.defineProperty(navigator, 'webdriver', { get: () => undefined });
    window.chrome = { runtime: {} };
  });

  const page = await context.newPage();
  console.log('Loading p.111477.xyz...');
  try {
    await page.goto('https://p.111477.xyz/', { waitUntil: 'domcontentloaded', timeout: 20000 });
  } catch (e) { console.log('Nav: ' + e.message.slice(0, 60)); }

  // Wait for CF challenge — check every 2s for up to 60s
  console.log('Waiting for CF challenge (up to 60s)...');
  let solved = false;
  for (let i = 0; i < 30; i++) {
    await page.waitForTimeout(2000);
    const title = await page.title();
    if (!title.includes('Just a moment') && !title.includes('challenge') && title !== '') {
      console.log('✅ Challenge solved! Title: ' + title);
      solved = true;
      break;
    }
    if (i % 5 === 0) console.log('  Waiting... (' + ((i+1)*2) + 's) title=' + title);
  }

  if (!solved) {
    // Check if there's a Turnstile widget to click
    console.log('Looking for Turnstile widget...');
    const turnstile = await page.$('.cf-turnstile, iframe[src*="challenges.cloudflare.com"]');
    if (turnstile) {
      console.log('Found Turnstile, clicking...');
      await turnstile.click();
      await page.waitForTimeout(10000);
    }

    // Also try clicking the challenge checkbox
    const checkbox = await page.$('#challenge-form input[type="checkbox"], .ctp-checkbox-label');
    if (checkbox) {
      console.log('Found checkbox, clicking...');
      await checkbox.click();
      await page.waitForTimeout(10000);
    }

    // Check again
    const title2 = await page.title();
    console.log('After interaction, title: ' + title2);
    if (!title2.includes('Just a moment')) {
      solved = true;
    }
  }

  // Get cookies
  const cookies = await context.cookies();
  const clearance = cookies.find(c => c.name === 'cf_clearance');
  console.log('\ncf_clearance: ' + (clearance ? 'FOUND' : 'NOT FOUND'));

  if (clearance) {
    console.log('Value: ' + clearance.value.slice(0, 50) + '...');
    console.log('Expires: ' + new Date(clearance.expires * 1000).toISOString());

    // Test file download
    console.log('\nTesting file download...');
    const result = await page.evaluate(async () => {
      try {
        const r = await fetch('https://p.111477.xyz/bulk?u=https://a.111477.xyz/movies/10+%281979%29/10+1979+1080p+BluRay+x264-OFT.mkv', {
          headers: { 'Range': 'bytes=0-1023' }
        });
        const buf = await r.arrayBuffer();
        const bytes = new Uint8Array(buf);
        return {
          status: r.status,
          contentType: r.headers.get('content-type'),
          contentLength: r.headers.get('content-length'),
          acceptRanges: r.headers.get('accept-ranges'),
          firstByte: '0x' + bytes[0].toString(16),
        };
      } catch (e) { return { error: e.message }; }
    });
    console.log('Download result: ' + JSON.stringify(result, null, 2));

    // Save cookies
    const cookieData = cookies.map(c => ({ name: c.name, value: c.value, domain: c.domain, path: c.path, expires: c.expires }));
    fs.writeFileSync('/tmp/cf_cookies.json', JSON.stringify(cookieData, null, 2));
    console.log('\nCookies saved to /tmp/cf_cookies.json');
  }

  await browser.close();
  process.exit(solved ? 0 : 1);
})();
