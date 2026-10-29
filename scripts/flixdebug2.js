const { chromium } = require('playwright');

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';

async function main() {
  const browser = await chromium.launch({
    headless: true,
    args: ['--no-sandbox', '--disable-dev-shm-usage', '--autoplay-policy=no-user-gesture-required', '--mute-audio'],
  });
  const context = await browser.newContext({ userAgent: UA, viewport: { width: 1280, height: 720 } });
  const page = await context.newPage();

  page.on('console', msg => {
    const text = msg.text();
    if (text.includes('[inject]') || text.includes('[MediaSource') || text.includes('[hls')) {
      console.log(`[console] ${text}`);
    }
  });

  // Inject hooks BEFORE the page loads
  await page.addInitScript(() => {
    console.log('[inject] Init script loaded');

    // Hook MediaSource to track when SourceBuffers are created
    const origAddSourceBuffer = MediaSource.prototype.addSourceBuffer;
    window.__sourceBuffers = [];
    MediaSource.prototype.addSourceBuffer = function(mimeType) {
      console.log('[inject] addSourceBuffer: ' + mimeType);
      const sb = origAddSourceBuffer.call(this, mimeType);
      
      // Hook appendBuffer
      const origAppend = sb.appendBuffer.bind(sb);
      sb.appendBuffer = function(data) {
        try {
          let buf;
          if (data instanceof ArrayBuffer) buf = new Uint8Array(data);
          else if (ArrayBuffer.isView(data)) buf = new Uint8Array(data.buffer, data.byteOffset, data.byteLength);
          else buf = new Uint8Array(data);

          const mimeTypeParts = mimeType.split(';')[0].split('/');
          const ext = mimeTypeParts[1] === 'mp2t' ? 'ts' : (mimeTypeParts[1] === 'mp4' ? 'm4s' : 'bin');
          console.log('[inject] appendBuffer: ' + buf.length + ' bytes, mime=' + mimeType);
          
          if (!window.__capturedSegs) window.__capturedSegs = [];
          // Save a copy (limit to first 50 segments to avoid OOM)
          if (window.__capturedSegs.length < 50) {
            // Convert to base64 in chunks (large data needs to be chunked)
            const chunks = [];
            const chunkSize = 32768;
            for (let i = 0; i < buf.length; i += chunkSize) {
              const chunk = buf.subarray(i, Math.min(i + chunkSize, buf.length));
              chunks.push(btoa(String.fromCharCode.apply(null, chunk)));
            }
            window.__capturedSegs.push({
              idx: window.__capturedSegs.length,
              size: buf.length,
              mime: mimeType,
              ext,
              b64: chunks.join(''),
              first16hex: Array.from(buf.subarray(0, 16)).map(b => b.toString(16).padStart(2, '0')).join(''),
            });
          }
        } catch (e) {
          console.log('[inject] appendBuffer error: ' + e.message);
        }
        return origAppend(data);
      };
      
      window.__sourceBuffers.push(sb);
      return sb;
    };

    // Also hook URL.createObjectURL to find the MediaSource URL
    const origCreateObjectURL = URL.createObjectURL;
    URL.createObjectURL = function(obj) {
      if (obj instanceof MediaSource) {
        console.log('[inject] createObjectURL for MediaSource');
      }
      return origCreateObjectURL.call(this, obj);
    };
  });

  console.log('Loading page...');
  await page.goto('https://flixcloud.cc/e/1nrcm15wafa0?v=2&autoPlay=true', {
    waitUntil: 'domcontentloaded',
    timeout: 30000,
  });

  // Wait for things to load
  await page.waitForTimeout(8000);

  // Check video state
  const videoInfo = await page.evaluate(() => {
    const video = document.querySelector('video');
    if (!video) return { found: false };
    return {
      found: true,
      src: video.src,
      readyState: video.readyState,
      networkState: video.networkState,
      duration: video.duration,
      currentTime: video.currentTime,
      paused: video.paused,
      error: video.error ? video.error.code : null,
    };
  });
  console.log('Video state: ' + JSON.stringify(videoInfo, null, 2));

  // Check captured segments count
  const segCount = await page.evaluate(() => window.__capturedSegs ? window.__capturedSegs.length : 0);
  console.log('Captured segments so far: ' + segCount);

  // Try to force play
  await page.evaluate(() => {
    const video = document.querySelector('video');
    if (video) {
      video.muted = true;
      video.play().catch(e => console.log('Play failed: ' + e.message));
    }
  });

  // Wait for playback
  await page.waitForTimeout(10000);

  // Get final state
  const finalSegs = await page.evaluate(() => window.__capturedSegs || []);
  console.log('\n=== Captured segments ===');
  console.log('Count: ' + finalSegs.length);
  for (const s of finalSegs.slice(0, 5)) {
    console.log(`  [${s.idx}] ${s.size} bytes, mime=${s.mime}, first16hex=${s.first16hex}`);
  }

  // Save the first decoded segment
  if (finalSegs.length > 0) {
    const fs = require('fs');
    const seg = finalSegs[0];
    const buf = Buffer.from(seg.b64, 'base64');
    fs.writeFileSync('/tmp/first_decoded_seg.' + seg.ext, buf);
    console.log(`\nSaved first decoded segment to /tmp/first_decoded_seg.${seg.ext} (${buf.length} bytes)`);
  }

  await browser.close();
}

main().catch(e => { console.error(e); process.exit(1); });
