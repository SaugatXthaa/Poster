const { chromium } = require('playwright');
const axios = require('axios');

const CINEJOY_ORIGIN = 'https://cinejoy.to';
const TMDB_KEY = '8476a7ab80ad76f0936744df0430e67c';
const UA = 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Safari/537.36';

(async () => {
  const browser = await chromium.launch({ headless: true });
  try {
    const ctx = await browser.newContext({
      userAgent: UA,
      extraHTTPHeaders: { 'Referer': CINEJOY_ORIGIN + '/' },
    });
    const page = await ctx.newPage();
    
    let playlistUrl = null;
    page.on('request', (req) => {
      const url = req.url();
      if (!playlistUrl && url.includes('info.movieboxnoob.cc/playlist/') && url.endsWith('.m3u8')) {
        playlistUrl = url;
      }
    });
    
    // Test TV show
    const watchUrl = `${CINEJOY_ORIGIN}/watch/tv/82856/1/1`;
    console.log('Loading:', watchUrl);
    
    try {
      await page.goto(watchUrl, { waitUntil: 'networkidle', timeout: 30000 });
    } catch (e) {
      console.log('Nav error (continuing):', e.message);
    }
    
    // Wait for the playlist URL to appear
    for (let i = 0; i < 30 && !playlistUrl; i++) {
      await page.waitForTimeout(500);
    }
    
    console.log('Playlist URL:', playlistUrl);
    
    if (playlistUrl) {
      // Fetch the master playlist
      const r = await axios.get(playlistUrl, { 
        headers: { 'User-Agent': UA, 'Referer': CINEJOY_ORIGIN + '/' },
        timeout: 10000,
      });
      console.log('\nMaster playlist (first 1500 chars):');
      console.log(r.data.substring(0, 1500));
      
      // Parse variants
      const lines = r.data.split('\n').map(l => l.trim()).filter(Boolean);
      const variants = [];
      let currentInf = {};
      let audioUrl = null;
      for (const line of lines) {
        if (line.startsWith('#EXT-X-MEDIA:') && line.includes('TYPE=AUDIO')) {
          const m = line.match(/URI="([^"]+)"/);
          if (m) audioUrl = m[1];
        } else if (line.startsWith('#EXT-X-STREAM-INF:')) {
          const attrs = line.slice('#EXT-X-STREAM-INF:'.length);
          const bandwidthMatch = attrs.match(/BANDWIDTH=(\d+)/);
          const resolutionMatch = attrs.match(/RESOLUTION=([^,]+)/);
          const codecsMatch = attrs.match(/CODECS="([^"]+)"/);
          currentInf = {
            bandwidth: bandwidthMatch ? parseInt(bandwidthMatch[1]) : 0,
            resolution: resolutionMatch ? resolutionMatch[1] : '',
            codecs: codecsMatch ? codecsMatch[1] : '',
          };
        } else if (line.startsWith('http') && currentInf.bandwidth) {
          variants.push({ ...currentInf, url: line, audioUrl });
          currentInf = {};
        }
      }
      console.log('\nVariants found:');
      for (const v of variants) {
        const h = v.resolution ? parseInt(v.resolution.split('x')[1]) : 0;
        const quality = h >= 2160 ? '4K' : h >= 1080 ? '1080p' : h >= 720 ? '720p' : h >= 360 ? '360p' : 'unknown';
        console.log('  [' + quality + '] ' + v.resolution + ' ' + v.codecs + ' ' + v.bandwidth + 'bps');
        console.log('    URL: ' + v.url);
      }
    }
  } finally {
    await browser.close();
  }
})();
