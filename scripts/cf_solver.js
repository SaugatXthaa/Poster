const { chromium } = require('playwright');
const fs = require('fs');

async function solveCF(url, downloadUrl) {
  const browser = await chromium.launch({
    headless: false,  // NON-headless — needed for CF challenge
    args: ['--no-sandbox', '--disable-dev-shm-usage', '--disable-blink-features=AutomationControlled']
  });
  const context = await browser.newContext({
    userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36',
    viewport: { width: 1920, height: 1080 },
    locale: 'en-US',
  });
  const page = await context.newPage();

  console.log('Loading: ' + url);
  try {
    await page.goto(url, { waitUntil: 'domcontentloaded', timeout: 20000 });
  } catch (e) {
    console.log('Nav: ' + e.message.slice(0, 60));
  }

  // Wait for CF challenge to auto-solve (non-headless bypasses detection)
  console.log('Waiting for CF challenge...');
  for (let i = 0; i < 30; i++) {
    await page.waitForTimeout(1000);
    const title = await page.title();
    if (!title.includes('Just a moment') && !title.includes('challenge')) {
      console.log('Challenge solved! Title: ' + title);
      break;
    }
    if (i % 5 === 0) console.log('  Waiting... (' + (i+1) + 's)');
  }

  // Get cookies
  const cookies = await context.cookies();
  const clearance = cookies.find(c => c.name === 'cf_clearance');
  console.log('cf_clearance: ' + (clearance ? 'FOUND' : 'NOT FOUND'));

  if (clearance) {
    console.log('Cookie: ' + clearance.value.slice(0, 40) + '...');
    console.log('Expires: ' + new Date(clearance.expires * 1000).toISOString());

    // Test file download
    if (downloadUrl) {
      console.log('\nTesting file download...');
      const response = await page.evaluate(async (url) => {
        const r = await fetch(url, { headers: { 'Range': 'bytes=0-1023' } });
        const buf = await r.arrayBuffer();
        const bytes = new Uint8Array(buf);
        return {
          status: r.status,
          contentType: r.headers.get('content-type'),
          contentLength: r.headers.get('content-length'),
          acceptRanges: r.headers.get('accept-ranges'),
          firstByte: '0x' + bytes[0].toString(16),
          first16: Array.from(bytes.slice(0, 16)).map(b => b.toString(16).padStart(2, '0')).join(' '),
        };
      }, downloadUrl);
      console.log('Response: ' + JSON.stringify(response, null, 2));
    }

    // Output cookies as JSON
    console.log('\n=== ALL COOKIES ===');
    const cookieData = cookies.map(c => ({
      name: c.name,
      value: c.value,
      domain: c.domain,
      path: c.path,
      expires: c.expires,
    }));
    fs.writeFileSync('/tmp/cf_cookies.json', JSON.stringify(cookieData, null, 2));
    console.log(JSON.stringify(cookieData));
  }

  await browser.close();
  return clearance;
}

const url = process.argv[2] || 'https://p.111477.xyz/';
const downloadUrl = process.argv[3] || 'https://p.111477.xyz/bulk?u=https://a.111477.xyz/movies/10+%281979%29/10+1979+1080p+BluRay+x264-OFT.mkv';
solveCF(url, downloadUrl).then(c => {
  console.log(c ? '\n✅ CF challenge solved!' : '\n❌ CF challenge not solved');
  process.exit(c ? 0 : 1);
}).catch(e => { console.error(e); process.exit(1); });
