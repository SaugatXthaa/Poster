const { chromium } = require('playwright');
const fs = require('fs');
const path = require('path');

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';

async function main() {
  const accessId = process.argv[2] || '1nrcm15wafa0';
  const outDir = process.argv[3] || '/tmp/flix_decoded';
  const maxSegs = parseInt(process.argv[4] || '20');
  fs.mkdirSync(outDir, { recursive: true });

  const browser = await chromium.launch({
    headless: true,
    args: ['--no-sandbox', '--disable-dev-shm-usage', '--autoplay-policy=no-user-gesture-required', '--mute-audio'],
  });
  const context = await browser.newContext({ userAgent: UA, viewport: { width: 1280, height: 720 } });
  const page = await context.newPage();

  // Hook MediaSource BEFORE page loads
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

          const isVideo = mimeType.includes('video');
          const isInit = buf.length >= 8 && buf[4] === 0x66 && buf[5] === 0x74 && buf[6] === 0x79 && buf[7] === 0x70;
          const isMoof = buf.length >= 8 && buf[4] === 0x6D && buf[5] === 0x6F && buf[6] === 0x6F && buf[7] === 0x66;

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
              segType: isInit ? 'init' : 'media',
              streamType: isVideo ? 'video' : 'audio',
              b64: chunks.join(''),
            });
          }
        } catch (e) { console.log('[inject] err: ' + e.message); }
        return origAppend(data);
      };
      return sb;
    };
    console.log('[inject] Hooks installed');
  });

  page.on('console', msg => {
    const text = msg.text();
    if (text.includes('[inject]')) console.log(text);
  });

  console.log('[flixcapture] Loading FlixCloud player...');
  try {
    await page.goto(`https://flixcloud.cc/e/${accessId}?v=2&autoPlay=true`, {
      waitUntil: 'networkidle',
      timeout: 60000,
    });
  } catch (e) {
    console.log(`[flixcapture] Page load: ${e.message.slice(0, 80)}`);
  }

  console.log('[flixcapture] Waiting 15s for video to load + play...');
  await page.waitForTimeout(15000);

  // Force play
  await page.evaluate(() => {
    const video = document.querySelector('video');
    if (video) {
      video.muted = true;
      video.play().catch(e => console.log('[inject] play err: ' + e.message));
    } else {
      console.log('[inject] No video element found!');
    }
  });

  // Capture segments for 30 seconds
  console.log('[flixcapture] Capturing for 30s...');
  for (let i = 0; i < 15; i++) {
    await page.waitForTimeout(2000);
    const count = await page.evaluate(() => window.__capturedSegs.length);
    console.log(`[flixcapture] ${count} segments captured...`);
    if (count >= maxSegs) break;
  }

  // Save segments
  const segs = await page.evaluate(() => window.__capturedSegs || []);
  console.log(`\n[flixcapture] Total: ${segs.length} segments`);

  // Save init + media segments
  for (const seg of segs) {
    const buf = Buffer.from(seg.b64, 'base64');
    let filename;
    if (seg.segType === 'init') {
      filename = path.join(outDir, `${seg.streamType}_init.mp4`);
    } else {
      // Number media segments by stream
      const mediaSegs = segs.filter(s => s.segType === 'media' && s.streamType === seg.streamType);
      const idx = mediaSegs.indexOf(seg);
      filename = path.join(outDir, `${seg.streamType}_${String(idx).padStart(4, '0')}.m4s`);
    }
    fs.writeFileSync(filename, buf);
  }

  // Build combined video + audio MP4 files
  const videoInit = segs.find(s => s.streamType === 'video' && s.segType === 'init');
  const audioInit = segs.find(s => s.streamType === 'audio' && s.segType === 'init');

  if (videoInit) {
    const parts = [Buffer.from(videoInit.b64, 'base64')];
    for (const seg of segs) {
      if (seg.streamType === 'video' && seg.segType === 'media') {
        parts.push(Buffer.from(seg.b64, 'base64'));
      }
    }
    fs.writeFileSync(path.join(outDir, 'video_combined.mp4'), Buffer.concat(parts));
    console.log(`[flixcapture] Saved video_combined.mp4`);
  }
  if (audioInit) {
    const parts = [Buffer.from(audioInit.b64, 'base64')];
    for (const seg of segs) {
      if (seg.streamType === 'audio' && seg.segType === 'media') {
        parts.push(Buffer.from(seg.b64, 'base64'));
      }
    }
    fs.writeFileSync(path.join(outDir, 'audio_combined.mp4'), Buffer.concat(parts));
    console.log(`[flixcapture] Saved audio_combined.mp4`);
  }

  // Also build a combined A/V MP4 by interleaving
  if (videoInit && audioInit) {
    const parts = [Buffer.from(videoInit.b64, 'base64'), Buffer.from(audioInit.b64, 'base64')];
    const videoMedia = segs.filter(s => s.streamType === 'video' && s.segType === 'media');
    const audioMedia = segs.filter(s => s.streamType === 'audio' && s.segType === 'media');
    const maxLen = Math.max(videoMedia.length, audioMedia.length);
    for (let i = 0; i < maxLen; i++) {
      if (i < videoMedia.length) parts.push(Buffer.from(videoMedia[i].b64, 'base64'));
      if (i < audioMedia.length) parts.push(Buffer.from(audioMedia[i].b64, 'base64'));
    }
    fs.writeFileSync(path.join(outDir, 'av_combined.mp4'), Buffer.concat(parts));
    console.log(`[flixcapture] Saved av_combined.mp4`);
  }

  await browser.close();
  console.log(`\n[flixcapture] Done. Files in ${outDir}/`);
}

main().catch(e => { console.error(e); process.exit(1); });
