// Use Playwright to run cloudorchestranova.com's player and capture all network requests
// This will reveal what 'token' value the player.js generates for welkinwanderer.space

const { chromium } = require('playwright');

(async () => {
  const tmdbId = process.argv[2] || '693134';
  const type = process.argv[3] || 'movie';
  const season = process.argv[4] || null;
  const episode = process.argv[5] || null;

  console.log(`Testing ${type} ${tmdbId}${season ? ` S${season}E${episode}` : ''}...`);

  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext({
    userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36',
    viewport: { width: 1280, height: 720 },
    locale: 'en-US',
  });

  const page = await context.newPage();

  // Capture all requests
  const requests = [];
  page.on('request', (req) => {
    const url = req.url();
    if (url.includes('welkinwanderer') || url.includes('cloudorchestranova') ||
        url.includes('vidsrcme') || url.includes('vidsrc.io') ||
        url.includes('.m3u8') || url.includes('master') || url.includes('playlist')) {
      const headers = req.headers();
      requests.push({
        url,
        method: req.method(),
        headers,
        resourceType: req.resourceType(),
      });
      console.log(`[${req.method()}] ${url.slice(0, 150)}`);
      // Show interesting headers
      const interesting = ['token', 'authorization', 'x-token', 'x-vs-token', 'vs', 'origin', 'referer', 'cookie'];
      for (const h of Object.keys(headers)) {
        if (interesting.some(i => h.toLowerCase().includes(i))) {
          console.log(`   ${h}: ${headers[h].slice(0, 100)}`);
        }
      }
    }
  });

  page.on('response', (res) => {
    const url = res.url();
    if (url.includes('welkinwanderer') || url.includes('.m3u8')) {
      console.log(`[RESP ${res.status()}] ${url.slice(0, 150)}`);
    }
  });

  page.on('console', (msg) => {
    if (msg.type() === 'error' || msg.type() === 'warning') {
      console.log(`[CONSOLE ${msg.type()}] ${msg.text().slice(0, 200)}`);
    }
  });

  try {
    // Build the embed URL
    const embedUrl = season
      ? `https://vidsrc.io/embed/tv/${tmdbId}/${season}/${episode}`
      : `https://vidsrc.io/embed/movie/${tmdbId}`;
    console.log(`\nNavigating to: ${embedUrl}`);

    await page.goto(embedUrl, { waitUntil: 'networkidle', timeout: 30000 });
    console.log('Page loaded.');

    // Wait for any iframe to be created
    await page.waitForTimeout(3000);

    // Click the play button if there is one
    try {
      const playButton = await page.$('button[id*="play"], button[class*="play"], .jw-bigplay, #bigPlay');
      if (playButton) {
        console.log('Clicking play button...');
        await playButton.click();
        await page.waitForTimeout(5000);
      }
    } catch (e) {
      console.log('No play button to click');
    }

    // Look for iframes
    const iframes = await page.$$('iframe');
    console.log(`Found ${iframes.length} iframes`);
    for (let i = 0; i < iframes.length; i++) {
      const src = await iframes[i].getAttribute('src');
      console.log(`  iframe ${i}: ${src}`);
    }

    // Wait for the stream to load
    await page.waitForTimeout(5000);
  } catch (e) {
    console.error('Error:', e.message);
  } finally {
    await browser.close();
  }

  console.log('\n=== Summary of all captured requests ===');
  for (const r of requests) {
    console.log(`${r.method} ${r.url}`);
    if (r.headers.token) console.log(`  TOKEN: ${r.headers.token}`);
    if (r.headers.authorization) console.log(`  AUTH: ${r.headers.authorization}`);
    if (r.headers.cookie) console.log(`  COOKIE: ${r.headers.cookie.slice(0, 150)}`);
  }
})();
