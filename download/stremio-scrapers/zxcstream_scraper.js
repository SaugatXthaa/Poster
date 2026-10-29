/**
 * ZXCStream Scraper — Node.js
 * ===========================
 * ZXCStream is a Stremio-style embed player at https://player.zxcstream.xyz
 * (NOT zxcstream.icu which is just a Blogger marketing landing page).
 *
 * Self-described as: "merely pulls streams from 3rd party services" (meta-aggregator).
 * PenguPlay tags: Fast, 1080p, Multi-Audio, Subtitles, Mainstream, Series.
 *
 * ⚠️ STATUS AS OF 2026-08-10: PARTIALLY BROKEN
 *   - /backend/tmdb/details/{type}/{tmdb}: ✅ WORKS (GET, returns TMDB metadata)
 *   - /backend/token: ❌ BROKEN — JS bundle sends POST, server returns 405.
 *     The deploy was updated to disable POST but the JS bundle was not updated.
 *     Once ZXC fixes this, the scraper will work as-is.
 *   - /backend_/embed/sentinel: ⛔ NOT REACHABLE (depends on /backend/token)
 *
 * Architecture (reverse-engineered from JS bundle, 2026-08-10):
 *   - Frontend: Next.js 16 App Router SPA at player.zxcstream.xyz
 *   - Embed URL: /embed/movie/{tmdbId} or /embed/tv/{tmdbId}/{s}/{e}
 *   - Backend: /backend/tmdb/details/{movie|tv}/{tmdbId} (public)
 *   - Backend: /backend/token (auth-token issuer, currently broken)
 *   - Backend: /backend_/embed/sentinel (returns embed URL to 3rd-party player)
 *
 * Token generation (from deobfuscated bundle):
 *   1. ts = Date.now()
 *   2. xt = sha512(`${ts}:24356351231432574635345245245252324:${tmdbId}`).slice(0,64)
 *   3. POST /backend/token {id, fToken, ts} -> {token, ts}
 *   4. GET /backend_/embed/sentinel?id={tmdbId}&b={movie|tv}&ts={ts}&token={token}&fToken={xt}
 *      (&season=&episode= for TV)
 *      -> {embed: "https://..."} (iframe URL to a 3rd-party player)
 *
 * ⚠️ Cloudflare Bot Management:
 *   /backend/* endpoints are protected by Cloudflare's bot challenge. We use
 *   playwright-extra + stealth plugin to bypass this.
 *
 * ⚠️ Final stream extraction:
 *   The `embed` URL from sentinel is an iframe to ANOTHER player (e.g. smashystream,
 *   vidsrc, vidlink, 2embed, multiembed). To get the actual m3u8, you need to
 *   follow the iframe and parse its content. This scraper handles this via
 *   Playwright by intercepting m3u8/mp4 requests.
 *
 * Install:
 *   npm install axios playwright-extra puppeteer-extra-plugin-stealth playwright
 *   npx playwright install chromium
 *
 * Usage:
 *   node zxcstream_scraper.js [tmdb_id]
 */

const axios = require('axios');
const crypto = require('crypto');

const PLAYER_BASE = 'https://player.zxcstream.xyz';
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 ' +
           '(KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';

// Reverse-engineered constants from the ZXC bundle
const SECRET = '24356351231432574635345245245252324';
const FIELD_MAP = {
  id:      'c81f7a42d9e253b16f408',
  fToken:  '9e3c7bd314af65281d0e49b73',
  ts:      '54d8b21fc9a374e60b1fd',
  token:   'b7f18e4c25d963a50ef81c4a9',
  title:   '2af9c71de384b5630c91e',
  year:    'f0b34e8d61c6a9275a14f',
  season:  'd41e8c6b259af73510fc48a7e',
  episode: '8b7d13fa8e620c9541d8e7bc2',
  imdbId:  '6e2af5c97d19840b3f81a6d54',
};

class ZXCStreamScraper {
  constructor(timeout = 30000) {
    this.timeout = timeout;
    this.client = axios.create({
      baseURL: PLAYER_BASE,
      timeout,
      headers: {
        'User-Agent': UA,
        'Accept': 'application/json, text/plain, */*',
        'Accept-Language': 'en-US,en;q=0.9',
        'Referer': `${PLAYER_BASE}/embed/movie/693134`,
      },
    });
  }

  /**
   * Public endpoint — returns TMDB metadata for the title.
   * ✅ WORKS (no auth, no CF challenge for this endpoint via axios).
   */
  async getTmdbDetails(mediaType, tmdbId, language = 'en-US') {
    const r = await this.client.get(
      `/backend/tmdb/details/${mediaType}/${tmdbId}`,
      { params: { language } }
    );
    return r.data;
  }

  /**
   * Generate the frontend token (xt) for a given TMDB ID.
   * Algorithm extracted from the deobfuscated bundle:
   *   xt = sha512(`${ts}:${SECRET}:${tmdbId}`).slice(0, 64)
   */
  generateFrontendToken(tmdbId) {
    const ts = Date.now();
    const raw = `${ts}:${SECRET}:${tmdbId}`;
    const xt = crypto.createHash('sha512').update(raw).digest('hex').slice(0, 64);
    return { xt, ts };
  }

  /**
   * Launch a stealth Playwright browser (used to bypass Cloudflare).
   */
  async _launchBrowser() {
    let chromium;
    try {
      chromium = require('playwright-extra');
      const stealth = require('puppeteer-extra-plugin-stealth');
      chromium.use(stealth());
    } catch (e) {
      // Fall back to plain playwright (CF may still block, but try)
      chromium = require('playwright');
    }
    return chromium.chromium.launch({
      headless: true,
      args: ['--no-sandbox', '--disable-blink-features=AutomationControlled'],
    });
  }

  /**
   * Get the embed URL for a movie or TV episode.
   * Uses Playwright to bypass Cloudflare's bot challenge on /backend/token.
   *
   * Returns { embed, ... } from the sentinel endpoint — the embed URL points to
   * an iframe of a 3rd-party player (smashystream, vidsrc, etc.).
   *
   * ⚠️ Currently broken on the server side (ZXC returns 405 for POST /backend/token).
   */
  async getEmbedUrl(tmdbId, mediaType = 'movie', season = null, episode = null) {
    const { xt, ts } = this.generateFrontendToken(String(tmdbId));
    const embedUrl = mediaType === 'movie'
      ? `${PLAYER_BASE}/embed/movie/${tmdbId}`
      : `${PLAYER_BASE}/embed/tv/${tmdbId}/${season}/${episode}`;

    let tokenResponse = null;
    let sentinelResponse = null;

    const browser = await this._launchBrowser();
    try {
      const ctx = await browser.newContext({
        userAgent: UA,
        viewport: { width: 1920, height: 1080 },
        locale: 'en-US',
        extraHTTPHeaders: { 'Accept-Language': 'en-US,en;q=0.9' },
      });
      const page = await ctx.newPage();

      // Intercept the /backend/token and /backend_/embed/sentinel responses
      page.on('response', async (response) => {
        const url = response.url();
        if (url.includes('/backend/token') && !tokenResponse) {
          try {
            if (response.status() === 200) {
              tokenResponse = await response.json();
            } else {
              tokenResponse = { _status: response.status(), _error: 'non-200' };
            }
          } catch (_) {}
        }
        if (url.includes('/backend_/embed/sentinel') && !sentinelResponse) {
          try {
            if (response.status() === 200) {
              sentinelResponse = await response.json();
            } else {
              sentinelResponse = { _status: response.status(), _error: 'non-200' };
            }
          } catch (_) {}
        }
      });

      try {
        await page.goto(embedUrl, {
          waitUntil: 'networkidle',
          timeout: this.timeout * 1000,
        });
        // Wait for sentinel response
        for (let i = 0; i < 60 && !sentinelResponse; i++) {
          await page.waitForTimeout(500);
        }
      } catch (e) {
        // navigation may time out — but sentinel may still have fired
      }
    } finally {
      await browser.close();
    }

    if (!sentinelResponse) {
      throw new Error(
        'Failed to capture embed URL from sentinel endpoint. ' +
        'Token response was: ' + JSON.stringify(tokenResponse) + '. ' +
        'If tokenResponse._status is 405, the ZXC server is currently broken ' +
        '(POST /backend/token is disabled but the JS bundle still uses POST). ' +
        'Try again later or contact the ZXC dev.'
      );
    }
    return sentinelResponse;
  }

  /**
   * HIGH-LEVEL: Get playable streams for a movie.
   * Returns { embed, streams } where streams is an array of:
   *   { url, quality, source, headers }
   *
   * This does TWO levels of iframe unwrapping:
   *   1. ZXC's sentinel returns an embed URL (iframe to a 3rd-party player)
   *   2. We visit that iframe and intercept any m3u8/mp4 request
   */
  async getMovieStreams(tmdbId) {
    return this._getStreams(tmdbId, 'movie');
  }

  async getSeriesStreams(tmdbId, season, episode) {
    return this._getStreams(tmdbId, 'tv', season, episode);
  }

  async _getStreams(tmdbId, mediaType, season = null, episode = null) {
    // First, get the embed URL via sentinel (uses browser to bypass CF)
    const sentinel = await this.getEmbedUrl(tmdbId, mediaType, season, episode);
    if (!sentinel || !sentinel.embed) {
      return { embed: null, streams: [], sentinel };
    }

    // Now visit the embed URL and capture m3u8/mp4 requests
    const streams = [];
    const browser = await this._launchBrowser();
    try {
      const ctx = await browser.newContext({
        userAgent: UA,
        viewport: { width: 1920, height: 1080 },
        locale: 'en-US',
        extraHTTPHeaders: { 'Accept-Language': 'en-US,en;q=0.9' },
      });
      const page = await ctx.newPage();

      page.on('request', (req) => {
        const url = req.url();
        if (
          (url.endsWith('.m3u8') || url.includes('.m3u8?') ||
           url.endsWith('.mp4') || url.includes('.mp4?')) &&
          !streams.find(s => s.url === url) &&
          !url.includes('zxcstream.')
        ) {
          streams.push({
            url,
            quality: this._inferQuality(url),
            source: 'zxcstream',
            headers: req.headers(),
          });
        }
      });

      try {
        await page.goto(sentinel.embed, {
          waitUntil: 'networkidle',
          timeout: this.timeout * 1000,
        });
        // Wait for streams to be discovered
        for (let i = 0; i < 40 && streams.length === 0; i++) {
          await page.waitForTimeout(500);
        }
      } catch (e) {
        // ignore — streams may have been captured already
      }
    } finally {
      await browser.close();
    }

    return { embed: sentinel.embed, streams, sentinel };
  }

  _inferQuality(url) {
    const m = url.match(/(\d{3,4})p/i);
    if (m) return `${m[1]}p`;
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
  const s = new ZXCStreamScraper();
  const TMDB = process.argv[2] ? parseInt(process.argv[2]) : 693134; // Dune Part Two

  console.log(`=== TMDB details for ${TMDB} (public endpoint) ===`);
  try {
    const details = await s.getTmdbDetails('movie', TMDB);
    console.log(`  title: ${details.title}`);
    console.log(`  imdb_id: ${details.imdb_id}`);
    console.log(`  backdrop: ${details.backdrop_paths?.[0]}`);
  } catch (e) {
    console.error(`  error: ${e.message}`);
  }

  console.log(`\n=== Frontend token test ===`);
  const { xt, ts } = s.generateFrontendToken(String(TMDB));
  console.log(`  xt: ${xt.slice(0, 32)}...`);
  console.log(`  ts: ${ts}`);

  console.log(`\n=== Sentinel embed URL for TMDB ${TMDB} (via Playwright) ===`);
  try {
    const result = await s.getEmbedUrl(String(TMDB), 'movie');
    console.log('  sentinel response:', JSON.stringify(result, null, 2));
  } catch (e) {
    console.error(`  error: ${e.message}`);
  }

  if (process.env.GET_STREAMS === '1') {
    console.log(`\n=== Full stream extraction for TMDB ${TMDB} ===`);
    try {
      const { embed, streams } = await s.getMovieStreams(String(TMDB));
      console.log(`  embed URL: ${embed}`);
      console.log(`  captured streams: ${streams.length}`);
      for (const st of streams) {
        console.log(`    [${st.quality}]  ${st.url.slice(0, 100)}`);
      }
    } catch (e) {
      console.error(`  error: ${e.message}`);
    }
  }
})();

module.exports = { ZXCStreamScraper, FIELD_MAP, SECRET };
