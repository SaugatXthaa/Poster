// Use Playwright to capture the stream URL with proper devtools-blocking
const { chromium } = require('playwright');

(async () => {
  const tmdbId = process.argv[2] || '693134';
  const type = process.argv[3] || 'movie';
  const season = process.argv[4] || null;
  const episode = process.argv[5] || null;

  console.log(`Testing ${type} ${tmdbId}${season ? ` S${season}E${episode}` : ''}...`);

  const browser = await chromium.launch({
    headless: true,
    args: ['--disable-blink-features=AutomationControlled'],
  });
  const context = await browser.newContext({
    userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36',
    viewport: { width: 1280, height: 720 },
    locale: 'en-US',
  });

  // Block devtools-detection scripts and ads
  await context.route('**/disable-devtool.js**', route => route.abort());
  await context.route('**/disable-devtool**', route => route.abort());
  await context.route('**/dtscout.com**', route => route.abort());
  await context.route('**/histats.com**', route => route.abort());
  await context.route('**/cloudflareinsights.com**', route => route.abort());
  await context.route('**/cdn-cgi/rum**', route => route.abort());

  const page = await context.newPage();

  const capturedStreamUrls = [];

  // Capture ALL requests (especially to welkinwanderer.space)
  page.on('request', (req) => {
    const url = req.url();
    if (url.includes('welkinwanderer') || url.includes('.m3u8') ||
        url.includes('master.m3u8') || url.includes('/pl/')) {
      const headers = req.headers();
      console.log(`\n=== STREAM REQUEST ===`);
      console.log(`URL: ${url}`);
      console.log(`Method: ${req.method()}`);
      console.log('Headers:');
      for (const h of Object.keys(headers)) {
        console.log(`  ${h}: ${headers[h]}`);
      }
      capturedStreamUrls.push({ url, headers });
    }
  });

  page.on('response', (res) => {
    const url = res.url();
    if (url.includes('welkinwanderer') || url.includes('.m3u8')) {
      console.log(`\n[STREAM RESPONSE ${res.status()}] ${url.slice(0, 150)}`);
      // If 200, fetch the body
      if (res.status() === 200) {
        res.text().then(body => {
          console.log(`Body (first 500 chars): ${body.slice(0, 500)}`);
        }).catch(() => {});
      }
    }
  });

  // Capture console messages (for debugging)
  page.on('console', (msg) => {
    const text = msg.text();
    if (text.includes('token') || text.includes('vs') || text.includes('stream') ||
        text.includes('master') || text.includes('m3u8') || text.includes('welkin')) {
      console.log(`[CONSOLE] ${text.slice(0, 300)}`);
    }
  });

  try {
    const embedUrl = season
      ? `https://vidsrc.io/embed/tv/${tmdbId}/${season}/${episode}`
      : `https://vidsrc.io/embed/movie/${tmdbId}`;
    console.log(`Navigating to: ${embedUrl}`);

    await page.goto(embedUrl, { waitUntil: 'domcontentloaded', timeout: 30000 });
    console.log('Page loaded.');

    // Wait for the iframe to be added to the DOM
    await page.waitForTimeout(2000);

    // Look for iframes in nested frames too
    const frames = page.frames();
    console.log(`Found ${frames.length} frames`);
    for (const f of frames) {
      if (f !== page.mainFrame()) {
        console.log(`  Frame URL: ${f.url()}`);
      }
    }

    // Try to find the play button in the main page or any iframe
    for (const frame of frames) {
      try {
        const playBtn = await frame.$('#bigPlay, .jw-bigplay, button[aria-label="Play"]');
        if (playBtn) {
          console.log(`Found play button in frame ${frame.url().slice(0, 80)}`);
          await playBtn.click();
          console.log('Clicked play button');
          break;
        }
      } catch (e) { /* ignore */ }
    }

    // Wait for the stream URL to be requested
    console.log('Waiting for stream URL...');
    for (let i = 0; i < 30; i++) {
      await page.waitForTimeout(1000);
      if (capturedStreamUrls.length > 0) {
        console.log(`Captured stream URL after ${i+1}s!`);
        break;
      }
    }

    // If we captured URLs, try to fetch them with the same headers
    if (capturedStreamUrls.length > 0) {
      const streamReq = capturedStreamUrls[0];
      console.log(`\n=== SUCCESS! Captured stream URL ===`);
      console.log(`URL: ${streamReq.url}`);
      console.log(`\n=== Headers needed for stream ===`);
      for (const [k, v] of Object.entries(streamReq.headers)) {
        if (k !== 'user-agent' && k !== 'accept' && k !== 'accept-language' && k !== 'accept-encoding' && k !== 'sec-ch-ua' && k !== 'sec-ch-ua-mobile' && k !== 'sec-ch-ua-platform' && k !== 'sec-fetch-dest' && k !== 'sec-fetch-mode' && k !== 'sec-fetch-site' && k !== 'sec-fetch-user' && k !== 'upgrade-insecure-requests' && k !== 'referer') {
          console.log(`  ${k}: ${v}`);
        }
      }
    } else {
      console.log('\n❌ No stream URL captured in 30 seconds.');
      console.log('Trying to inspect the page for clues...');
      const content = await page.content();
      console.log(`Page content length: ${content.length}`);
      // Look for any URLs in the page
      const urls = content.match(/https?:\/\/[^\s"'<>]+/g) || [];
      console.log(`URLs in page: ${urls.length}`);
      for (const u of urls.slice(0, 10)) {
        console.log(`  ${u.slice(0, 150)}`);
      }
    }
  } catch (e) {
    console.error('Error:', e.message);
  } finally {
    await browser.close();
  }
})();
