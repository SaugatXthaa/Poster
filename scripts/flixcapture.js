// Capture decoded segment data from FlixCloud player via Playwright.
// The FlixCloud player uses a custom hls.js fork that decodes XOR-encrypted
// image-disguised segments. By intercepting the SourceBuffer.appendBuffer()
// calls, we capture the decoded MPEG-TS / fMP4 data and save it to disk.
//
// This produces standard HLS-playable MPEG-TS segments that any client
// (Stremio, hls.js, ExoPlayer, AVPlayer) can play without the FlixCloud fork.

const { chromium } = require('playwright');
const fs = require('fs');
const path = require('path');

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';

async function main() {
  const accessId = process.argv[2] || '1nrcm15wafa0';
  const outDir = process.argv[3] || '/tmp/flix_decoded';
  fs.mkdirSync(outDir, { recursive: true });

  console.log(`[flixcapture] accessId=${accessId}, outDir=${outDir}`);

  const browser = await chromium.launch({
    headless: true,
    args: ['--no-sandbox', '--disable-dev-shm-usage', '--autoplay-policy=no-user-gesture-required'],
  });

  const context = await browser.newContext({
    userAgent: UA,
    viewport: { width: 1280, height: 720 },
    locale: 'en-US',
  });

  const page = await context.newPage();

  // Intercept segment requests and responses
  const segmentMap = new Map();  // url -> { data, mime }
  const segments = [];
  let segmentCount = 0;

  page.on('request', req => {
    const url = req.url();
    if (url.includes('vault-') && url.includes('atomic4cdn') && (url.endsWith('.webp') || url.endsWith('.png'))) {
      // Segment request
    }
  });

  page.on('response', async response => {
    const url = response.url();
    try {
      if (url.includes('atomic4cdn') && (url.endsWith('.webp') || url.endsWith('.png'))) {
        const buf = await response.body();
        const segIdx = segmentCount++;
        const ext = url.endsWith('.webp') ? 'webp' : 'png';
        const filename = path.join(outDir, `seg-${String(segIdx).padStart(4, '0')}.${ext}`);
        fs.writeFileSync(filename, buf);
        segments.push({ idx: segIdx, url, filename, size: buf.length });
        if (segIdx < 5 || segIdx % 10 === 0) {
          console.log(`[flixcapture] Captured segment ${segIdx}: ${buf.length} bytes (${ext})`);
        }
      }
    } catch (e) {
      // ignore
    }
  });

  // Inject a MediaSource hook BEFORE the page loads to capture decoded TS data
  await page.addInitScript(() => {
    window.__decodedSegments = [];
    window.__segIdx = 0;

    // Hook SourceBuffer.appendBuffer to capture decoded data
    const origAppendBuffer = SourceBuffer.prototype.appendBuffer;
    SourceBuffer.prototype.appendBuffer = function(data) {
      try {
        // data is ArrayBuffer or ArrayBufferView
        let buf;
        if (data instanceof ArrayBuffer) buf = new Uint8Array(data);
        else if (data.buffer) buf = new Uint8Array(data.buffer, data.byteOffset, data.byteLength);
        else buf = new Uint8Array(data);

        // Save decoded segment
        const idx = window.__segIdx++;
        const filename = `decoded-${String(idx).padStart(4, '0')}.bin`;
        // Convert to base64 for transfer
        let b64 = '';
        const chunkSize = 32768;
        for (let i = 0; i < buf.length; i += chunkSize) {
          const chunk = buf.subarray(i, Math.min(i + chunkSize, buf.length));
          b64 += btoa(String.fromCharCode.apply(null, chunk));
        }
        window.__decodedSegments.push({ idx, filename, size: buf.length, b64 });
        if (idx < 3 || idx % 10 === 0) {
          console.log(`[inject] Decoded segment ${idx}: ${buf.length} bytes`);
        }
      } catch (e) {
        console.log('[inject] Error capturing segment:', e.message);
      }
      return origAppendBuffer.call(this, data);
    };

    // Also hook MediaSource.addSourceBuffer to log codecs
    const origAddSourceBuffer = MediaSource.prototype.addSourceBuffer;
    MediaSource.prototype.addSourceBuffer = function(mimeType) {
      console.log('[inject] addSourceBuffer:', mimeType);
      return origAddSourceBuffer.call(this, mimeType);
    };
  });

  // Listen for console messages
  page.on('console', msg => {
    const text = msg.text();
    if (text.includes('[inject]') || text.includes('[flixcapture]')) {
      console.log(text);
    }
  });

  console.log('[flixcapture] Loading FlixCloud player...');
  await page.goto(`https://flixcloud.cc/e/${accessId}?v=2&autoPlay=true`, {
    waitUntil: 'networkidle',
    timeout: 30000,
  });

  // Wait for the video element to start playing
  console.log('[flixcapture] Waiting for video playback...');
  await page.waitForTimeout(5000);

  // Try to click the play button if needed
  try {
    const playBtn = await page.$('#bigPlay, .jw-bigplay, button[aria-label="Play"]');
    if (playBtn) {
      await playBtn.click();
      console.log('[flixcapture] Clicked play button');
    }
  } catch (e) {
    // ignore
  }

  // Wait for segments to be captured
  console.log('[flixcapture] Capturing segments for 30 seconds...');
  await page.waitForTimeout(30000);

  // Extract decoded segments from the page
  console.log('[flixcapture] Extracting decoded segments...');
  const decodedSegments = await page.evaluate(() => {
    return window.__decodedSegments || [];
  });

  console.log(`[flixcapture] Captured ${decodedSegments.length} decoded segments`);
  console.log(`[flixcapture] Also captured ${segments.length} raw segment files`);

  // Save decoded segments
  for (const seg of decodedSegments) {
    const filename = path.join(outDir, seg.filename);
    const buf = Buffer.from(seg.b64, 'base64');
    fs.writeFileSync(filename, buf);
    if (seg.idx < 5 || seg.idx % 10 === 0) {
      console.log(`[flixcapture] Saved decoded segment ${seg.idx}: ${buf.length} bytes`);
    }
  }

  await browser.close();

  console.log(`\n[flixcapture] Done. Captured:`);
  console.log(`  - ${segments.length} raw segment files (in ${outDir}/seg-*.webp|png)`);
  console.log(`  - ${decodedSegments.length} decoded segment files (in ${outDir}/decoded-*.bin)`);
  console.log(`\nNext: try ffprobe on the decoded files to see if they're valid MPEG-TS/fMP4`);
}

main().catch(e => { console.error(e); process.exit(1); });
