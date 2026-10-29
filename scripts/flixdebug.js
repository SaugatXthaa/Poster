const { chromium } = require('playwright');
const fs = require('fs');

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';

async function main() {
  const accessId = '1nrcm15wafa0';
  const browser = await chromium.launch({
    headless: true,
    args: ['--no-sandbox', '--disable-dev-shm-usage', '--autoplay-policy=no-user-gesture-required'],
  });
  const context = await browser.newContext({ userAgent: UA, viewport: { width: 1280, height: 720 } });
  const page = await context.newPage();

  // Log ALL console messages
  page.on('console', msg => console.log(`[console.${msg.type()}] ${msg.text()}`));
  page.on('pageerror', err => console.log(`[pageerror] ${err.message}`));
  page.on('requestfailed', req => console.log(`[reqfail] ${req.url()} - ${req.failure()?.errorText}`));

  // Capture network requests
  const requests = [];
  page.on('request', req => {
    const url = req.url();
    if (url.includes('atomic4cdn') || url.includes('flixcloud') || url.includes('fetch8')) {
      requests.push({ method: req.method(), url });
    }
  });

  page.on('response', resp => {
    const url = resp.url();
    if (url.includes('atomic4cdn') || url.includes('m3u8') || url.includes('seg-')) {
      console.log(`[response] ${resp.status()} ${url.slice(0, 100)}`);
    }
  });

  console.log('Loading page...');
  try {
    const resp = await page.goto(`https://flixcloud.cc/e/${accessId}?v=2&autoPlay=true`, {
      waitUntil: 'domcontentloaded',
      timeout: 30000,
    });
    console.log(`Page loaded, status: ${resp.status()}`);
    console.log(`Page URL: ${page.url()}`);
    console.log(`Page title: ${await page.title()}`);
  } catch (e) {
    console.log(`Page load error: ${e.message}`);
  }

  // Wait a bit for things to settle
  await page.waitForTimeout(5000);

  // Check what's on the page
  const html = await page.content();
  console.log(`Page HTML length: ${html.length}`);
  console.log(`First 500 chars: ${html.slice(0, 500)}`);

  // Check for video element
  const videoInfo = await page.evaluate(() => {
    const video = document.querySelector('video');
    if (!video) return { found: false };
    return {
      found: true,
      src: video.src,
      currentSrc: video.currentSrc,
      readyState: video.readyState,
      networkState: video.networkState,
      duration: video.duration,
      currentTime: video.currentTime,
      paused: video.paused,
    };
  });
  console.log(`Video element: ${JSON.stringify(videoInfo, null, 2)}`);

  // Check if Hls is loaded
  const hlsInfo = await page.evaluate(() => {
    return {
      hlsLoaded: typeof window.Hls !== 'undefined',
      artplayerLoaded: typeof window.Artplayer !== 'undefined',
      __pk: window.__pk || null,
    };
  });
  console.log(`Player state: ${JSON.stringify(hlsInfo, null, 2)}`);

  // Wait more for video to start
  await page.waitForTimeout(10000);

  // Try clicking play
  try {
    await page.click('#bigPlay, .jw-bigplay, button[aria-label="Play"], .art-video-player', { timeout: 5000 });
    console.log('Clicked play');
  } catch (e) {
    console.log(`Click failed: ${e.message}`);
  }

  await page.waitForTimeout(10000);

  // Final stats
  console.log(`\nTotal requests captured: ${requests.length}`);
  console.log('Sample requests:');
  for (const r of requests.slice(0, 10)) {
    console.log(`  ${r.method} ${r.url.slice(0, 120)}`);
  }

  await browser.close();
}

main().catch(e => { console.error(e); process.exit(1); });
