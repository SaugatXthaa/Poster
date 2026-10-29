// Capture ALL decoded fMP4 segments from FlixCloud player via Playwright.
const { chromium } = require('playwright');
const fs = require('fs');
const path = require('path');

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';

async function main() {
  const accessId = process.argv[2] || '1nrcm15wafa0';
  const outDir = process.argv[3] || '/tmp/flix_decoded';
  const maxSegs = parseInt(process.argv[4] || '60');
  fs.mkdirSync(outDir, { recursive: true });

  console.log(`[flixcapture] accessId=${accessId}, outDir=${outDir}, maxSegs=${maxSegs}`);

  const browser = await chromium.launch({
    headless: true,
    args: ['--no-sandbox', '--disable-dev-shm-usage', '--autoplay-policy=no-user-gesture-required', '--mute-audio'],
  });
  const context = await browser.newContext({ userAgent: UA, viewport: { width: 1280, height: 720 } });
  const page = await context.newPage();

  // Inject hooks BEFORE the page loads
  await page.addInitScript(() => {
    window.__capturedSegs = [];
    const origAddSourceBuffer = MediaSource.prototype.addSourceBuffer;
    MediaSource.prototype.addSourceBuffer = function(mimeType) {
      console.log('[inject] addSourceBuffer: ' + mimeType);
      const sb = origAddSourceBuffer.call(this, mimeType);
      const origAppend = sb.appendBuffer.bind(sb);
      sb.appendBuffer = function(data) {
        try {
          let buf;
          if (data instanceof ArrayBuffer) buf = new Uint8Array(data);
          else if (ArrayBuffer.isView(data)) buf = new Uint8Array(data.buffer, data.byteOffset, data.byteLength);
          else buf = new Uint8Array(data);

          // Determine segment type
          const isVideo = mimeType.includes('video');
          const isAudio = mimeType.includes('audio');
          // Detect init segment: starts with ftyp box (size + 'ftyp')
          const isInit = buf.length >= 8 && buf[4] === 0x66 && buf[5] === 0x74 && buf[6] === 0x79 && buf[7] === 0x70;
          // Detect moof: starts with size + 'moof'
          const isMoof = buf.length >= 8 && buf[4] === 0x6D && buf[5] === 0x6F && buf[6] === 0x6F && buf[7] === 0x66;

          const segType = isInit ? 'init' : 'media';
          const streamType = isVideo ? 'video' : (isAudio ? 'audio' : 'unknown');

          if (window.__capturedSegs.length < 200) {
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
              segType,
              streamType,
              b64: chunks.join(''),
            });
            if (window.__capturedSegs.length <= 5 || window.__capturedSegs.length % 10 === 0) {
              console.log('[inject] #' + window.__capturedSegs.length + ': ' + buf.length + 'B ' + streamType + '/' + segType);
            }
          }
        } catch (e) { console.log('[inject] err: ' + e.message); }
        return origAppend(data);
      };
      return sb;
    };
  });

  page.on('console', msg => {
    const text = msg.text();
    if (text.includes('[inject]')) console.log(text);
  });

  console.log('[flixcapture] Loading FlixCloud player...');
  await page.goto(`https://flixcloud.cc/e/${accessId}?v=2&autoPlay=true`, {
    waitUntil: 'domcontentloaded',
    timeout: 30000,
  });

  // Wait for video to start
  console.log('[flixcapture] Waiting for video playback...');
  await page.waitForTimeout(5000);

  // Force play (muted)
  await page.evaluate(() => {
    const video = document.querySelector('video');
    if (video) {
      video.muted = true;
      video.play().catch(e => console.log('[inject] play err: ' + e.message));
    }
  });

  // Wait for segments to be captured (target count or timeout)
  const startTime = Date.now();
  while (Date.now() - startTime < 60000) {
    await page.waitForTimeout(2000);
    const count = await page.evaluate(() => window.__capturedSegs.length);
    console.log(`[flixcapture] ${count} segments captured...`);
    if (count >= maxSegs) break;
  }

  // Extract and save all segments
  console.log('[flixcapture] Extracting captured segments...');
  const segs = await page.evaluate(() => window.__capturedSegs || []);

  // Save segments — organize by stream type
  const videoInit = segs.find(s => s.streamType === 'video' && s.segType === 'init');
  const audioInit = segs.find(s => s.streamType === 'audio' && s.segType === 'init');

  // Save init segments
  if (videoInit) {
    const buf = Buffer.from(videoInit.b64, 'base64');
    fs.writeFileSync(path.join(outDir, 'video_init.mp4'), buf);
    console.log(`[flixcapture] Saved video_init.mp4 (${buf.length} bytes)`);
  }
  if (audioInit) {
    const buf = Buffer.from(audioInit.b64, 'base64');
    fs.writeFileSync(path.join(outDir, 'audio_init.mp4'), buf);
    console.log(`[flixcapture] Saved audio_init.mp4 (${buf.length} bytes)`);
  }

  // Save media segments
  let videoCount = 0, audioCount = 0;
  for (const seg of segs) {
    if (seg.segType !== 'media') continue;
    const buf = Buffer.from(seg.b64, 'base64');
    const filename = path.join(outDir, `${seg.streamType}_${String(seg.streamType === 'video' ? videoCount : audioCount).padStart(4, '0')}.m4s`);
    fs.writeFileSync(filename, buf);
    if (seg.streamType === 'video') videoCount++;
    else audioCount++;
  }
  console.log(`[flixcapture] Saved ${videoCount} video + ${audioCount} audio media segments`);

  // Also: build a single playable MP4 by concatenating init + media fragments
  // (works for fMP4 — ffprobe/ffmpeg can read this as a single file)
  if (videoInit) {
    const combined = [];
    combined.push(Buffer.from(videoInit.b64, 'base64'));
    for (const seg of segs) {
      if (seg.streamType === 'video' && seg.segType === 'media') {
        combined.push(Buffer.from(seg.b64, 'base64'));
      }
    }
    const combinedBuf = Buffer.concat(combined);
    fs.writeFileSync(path.join(outDir, 'video_combined.mp4'), combinedBuf);
    console.log(`[flixcapture] Saved video_combined.mp4 (${combinedBuf.length} bytes)`);
  }
  if (audioInit) {
    const combined = [];
    combined.push(Buffer.from(audioInit.b64, 'base64'));
    for (const seg of segs) {
      if (seg.streamType === 'audio' && seg.segType === 'media') {
        combined.push(Buffer.from(seg.b64, 'base64'));
      }
    }
    const combinedBuf = Buffer.concat(combined);
    fs.writeFileSync(path.join(outDir, 'audio_combined.mp4'), combinedBuf);
    console.log(`[flixcapture] Saved audio_combined.mp4 (${combinedBuf.length} bytes)`);
  }

  await browser.close();
  console.log(`\n[flixcapture] Done. Files saved in ${outDir}/`);
}

main().catch(e => { console.error(e); process.exit(1); });
