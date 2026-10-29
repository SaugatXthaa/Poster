/**
 * VidFast Scraper — Node.js
 * =========================
 * VidFast is a Stremio-style embed player at https://vidfast.vc
 * (vidfast.pro is the marketing site, vidfast.domains is the mirror checker).
 *
 * Architecture (reverse-engineered 2026-08-10):
 *   - Frontend: Next.js 16 App Router SPA at vidfast.vc
 *   - Embed URL: /movie/{tmdbId} or /tv/{tmdbId}/{s}/{e}
 *   - The page returns a React Server Component (RSC) payload containing:
 *       {en: "<encrypted-stream-token>", host, id, title, year, ...}
 *   - The "en" parameter is decrypted client-side by heavily-obfuscated JS
 *     (string-array + decoder + control-flow flattening). The decryption
 *     key is NOT statically extractable.
 *   - The stream URL is a direct MP4/MKV or HLS m3u8 on FebBox-style CDN
 *
 * ⚠ PURE HTTP INFEASIBLE:
 *   The "en" parameter decryption requires running the obfuscated bundle.
 *   The bundle uses:
 *     - Web Workers for PoW/decryption
 *     - crypto.subtle for AES + HKDF
 *     - String array obfuscation (iF, i5, i1, i2 decoder functions)
 *   Extracting the AES key + IV from the bundle would take days.
 *
 * STRATEGY:
 *   Use Playwright to load the embed page, click play, and intercept the
 *   first m3u8/mp4 request. This is the same approach as Cinejoy.
 *
 * Install:
 *   npm install axios playwright-extra puppeteer-extra-plugin-stealth playwright
 *   npx playwright install chromium
 *
 * Usage:
 *   node vidfast_scraper.js movie 693134
 *   node vidfast_scraper.js tv 1399 1 1
 */

'use strict';

const axios = require('axios');

const VIDFAST_BASE = 'https://vidfast.vc';
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 ' +
           '(KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';

class VidFastScraper {
  constructor(timeout = 45000) {
    this.timeout = timeout;
    this.client = axios.create({
      timeout,
      headers: {
        'User-Agent': UA,
        'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
        'Accept-Language': 'en-US,en;q=0.9',
      },
    });
  }

  /**
   * Fetch the RSC payload for a movie/TV page.
   * Returns {en, host, id, title, year, ...} — the player config.
   */
  async getPlayerConfig(tmdbId, mediaType, season = null, episode = null) {
    const url = mediaType === 'movie'
      ? `${VIDFAST_BASE}/movie/${tmdbId}`
      : `${VIDFAST_BASE}/tv/${tmdbId}/${season}/${episode}`;

    const r = await this.client.get(url, {
      headers: {
        'RSC': '1',
        'Next-Router-State-Tree': '%5B%22%22%2C%7B%22children%22%3A%5B%22' +
          (mediaType === 'movie' ? 'movie' : 'tv') +
          '%22%2C%7B%22children%22%3A%5B%5B%22id%22%2C%22' + tmdbId +
          '%22%2C%22d%22%5D%2C%7B%22children%22%3A%5B%22__PAGE__%22%2C%7B%7D%5D%7D%5D%7D%5D%7D%2Cnull%2Cnull%2Ctrue%5D',
      },
    });

    // Parse the RSC payload to extract the player config
    // The payload is a series of self.__next_f.push([1, "..."]) calls
    const text = r.data;
    const configMatch = text.match(/"en":"([^"]+)"/);
    const titleMatch = text.match(/"title":"([^"]+)"/);
    const yearMatch = text.match(/"year":"([^"]+)"/);

    return {
      en: configMatch ? configMatch[1] : null,
      title: titleMatch ? titleMatch[1] : null,
      year: yearMatch ? yearMatch[1] : null,
      host: 'vidfast.vc',
      id: String(tmdbId),
    };
  }

  /**
   * Launch a stealth Playwright browser (bypasses Cloudflare bot detection).
   */
  async _launchBrowser() {
    let chromium;
    try {
      chromium = require('playwright-extra');
      const stealth = require('puppeteer-extra-plugin-stealth');
      chromium.use(stealth());
    } catch (e) {
      chromium = require('playwright');
    }
    return chromium.chromium.launch({
      headless: true,
      args: ['--no-sandbox', '--disable-blink-features=AutomationControlled'],
    });
  }

  /**
   * Get playable streams for a movie or TV episode.
   * Uses Playwright to bypass the obfuscated "en" decryption.
   *
   * Returns an array of stream objects: {url, quality, source, headers}
   */
  async getStreams(tmdbId, mediaType = 'movie', season = null, episode = null) {
    const watchUrl = mediaType === 'movie'
      ? `${VIDFAST_BASE}/movie/${tmdbId}?autoPlay=true`
      : `${VIDFAST_BASE}/tv/${tmdbId}/${season}/${episode}?autoPlay=true`;

    const browser = await this._launchBrowser();
    const streams = [];
    try {
      const ctx = await browser.newContext({
        userAgent: UA,
        viewport: { width: 1920, height: 1080 },
        locale: 'en-US',
        extraHTTPHeaders: { 'Accept-Language': 'en-US,en;q=0.9' },
      });
      const page = await ctx.newPage();

      // Intercept all video/stream requests
      page.on('request', (req) => {
        const url = req.url();
        if (
          (url.endsWith('.m3u8') || url.includes('.m3u8?') ||
           url.endsWith('.mp4') || url.includes('.mp4?')) &&
          !streams.find(s => s.url === url) &&
          !url.includes('vidfast.vc')
        ) {
          streams.push({
            url,
            quality: this._inferQuality(url),
            source: 'vidfast',
            headers: req.headers(),
          });
        }
      });

      try {
        await page.goto(watchUrl, {
          waitUntil: 'domcontentloaded',
          timeout: this.timeout * 1000,
        });
      } catch (e) {
        // navigation may time out — but the request interceptor may still have fired
      }

      // Wait for the player to initialize and click play
      await page.waitForTimeout(5000);

      // Force-click the play button via JS (bypasses overlay intercepts)
      await page.evaluate(() => {
        const btns = document.querySelectorAll('button');
        for (const b of btns) {
          const rect = b.getBoundingClientRect();
          if (rect.width > 50 && rect.height > 50) {
            b.click();
            break;
          }
        }
        // Also try to play the video directly
        const video = document.querySelector('video');
        if (video) {
          video.muted = true;
          video.play().catch(() => {});
        }
      });

      // Wait for stream URL to be captured
      for (let i = 0; i < 50 && streams.length === 0; i++) {
        await page.waitForTimeout(500);
      }
    } finally {
      await browser.close();
    }

    return streams;
  }

  async getMovieStreams(tmdbId) {
    return this.getStreams(tmdbId, 'movie');
  }

  async getSeriesStreams(tmdbId, season, episode) {
    return this.getStreams(tmdbId, 'tv', season, episode);
  }

  _inferQuality(url) {
    const m = url.match(/(\d{3,4})p/i);
    if (m) return `${m[1]}p`;
    if (url.match(/2160|4k|uhd/i)) return '2160p';
    if (url.match(/1080/i)) return '1080p';
    if (url.match(/720/i)) return '720p';
    if (url.match(/480/i)) return '480p';
    if (url.match(/360/i)) return '360p';
    return 'unknown';
  }
}

// --------------------------------------------------------------------- //
// Demo
// --------------------------------------------------------------------- //
if (require.main === module) (async () => {
  const s = new VidFastScraper();
  const args = process.argv.slice(2);
  const mediaType = args[0] || 'movie';
  const tmdbId = args[1] || '693134';

  console.log(`=== VidFast player config for ${mediaType} ${tmdbId} ===`);
  try {
    const config = await s.getPlayerConfig(
      tmdbId,
      mediaType,
      mediaType === 'tv' ? parseInt(args[2]) : null,
      mediaType === 'tv' ? parseInt(args[3]) : null
    );
    console.log('  title:', config.title);
    console.log('  year:', config.year);
    console.log('  en (encrypted token, first 50):', config.en?.slice(0, 50) + '...');
    console.log('  (decryption requires running the obfuscated bundle in a browser)');
  } catch (e) {
    console.error('  config error:', e.message);
  }

  if (process.env.GET_STREAMS === '1') {
    console.log(`\n=== Full stream extraction for ${mediaType} ${tmdbId} (via Playwright) ===`);
    try {
      const streams = mediaType === 'movie'
        ? await s.getMovieStreams(tmdbId)
        : await s.getSeriesStreams(tmdbId, parseInt(args[2]), parseInt(args[3]));
      console.log(`  Captured ${streams.length} stream(s):`);
      for (const st of streams) {
        console.log(`    [${st.quality}]  ${st.url.slice(0, 100)}`);
      }
      if (streams.length === 0) {
        console.log('  (No streams captured — VidFast player may require user interaction');
        console.log('   or has aggressive bot detection. Try running in non-headless mode.)');
      }
    } catch (e) {
      console.error('  error:', e.message);
    }
  } else {
    console.log('\n(set GET_STREAMS=1 to extract stream URLs via Playwright)');
  }
})();

module.exports = { VidFastScraper };
