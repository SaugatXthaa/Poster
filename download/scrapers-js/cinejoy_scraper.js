/**
 * Cinejoy Scraper (cinejoy.to) — Node.js
 * =======================================
 * SvelteKit SPA backed by shegu.st services.
 *
 * Architecture (verified live 2026-08-10):
 *   1. App uses TMDB key 8476a7ab80ad76f0936744df0430e67c for metadata.
 *   2. GET https://api.shegu.st/servers              -> public server list
 *   3. Client generates `rid` (164-char base64url token).
 *   4. GET https://api.shegu.st/challenge?rid={rid}  -> PoW challenge {v,b,s,e,n,r,p,d,k,g}
 *   5. Client solves the PoW and builds X-At token.
 *   6. GET https://api.shegu.st/{rid}  HEADER X-At: {token}
 *      -> encrypted base64 blob (decrypt with key `k`) containing the content ID.
 *   7. GET https://help.earthcleaner.cc/playlist/{contentId}.m3u8
 *      HEADER Referer: https://cinejoy.to/
 *      -> master playlist with 1080p/720p/360p variants + audio.
 *
 * The CDN gate is simple — JUST `Referer: https://cinejoy.to/`. The hard part is
 * solving the PoW + decrypting the blob to get the content ID.
 *
 * This scraper ships with a Playwright browser strategy that intercepts the
 * m3u8 request — no PoW solving required.
 *
 * Install:
 *   npm install axios playwright
 *   npx playwright install chromium
 *
 * Usage:
 *   node cinejoy_scraper.js
 */

const axios = require('axios');
const crypto = require('crypto');

const TMDB_KEY = '8476a7ab80ad76f0936744df0430e67c'; // baked into cinejoy bundle
const CINEJOY_ORIGIN = 'https://cinejoy.to';
const SHEGU_API = 'https://api.shegu.st';
const SHEGU_SUBS = 'https://subtitles.shegu.st';
const EARTHCDN = 'https://help.earthcleaner.cc';

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 ' +
           '(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36';

class CinejoyScraper {
  constructor(timeout = 30000) {
    this.timeout = timeout;
    this.client = axios.create({
      timeout,
      headers: {
        'User-Agent': UA,
        'Accept': 'application/json, text/plain, */*',
        'Accept-Language': 'en-US,en;q=0.9',
        'Referer': CINEJOY_ORIGIN + '/',
        'Origin': CINEJOY_ORIGIN,
      },
    });
  }

  /**
   * Get all HLS variants for a movie (TMDB ID).
   * Uses Playwright to bypass the PoW challenge.
   */
  async getMovieStreams(tmdbId, title = '') {
    const watchUrl = `${CINEJOY_ORIGIN}/watch/movie/${tmdbId}`;
    return this._scrapeWatchPage(watchUrl, title || `tmdb-${tmdbId}`);
  }

  /**
   * Get all HLS variants for a series episode.
   */
  async getSeriesStreams(tmdbId, season, episode, title = '') {
    const watchUrl = `${CINEJOY_ORIGIN}/watch/tv/${tmdbId}/${season}/${episode}`;
    return this._scrapeWatchPage(
      watchUrl,
      title || `tmdb-${tmdbId}-s${season}e${episode}`
    );
  }

  /**
   * Public subtitles endpoint.
   * Returns array of: {id, language, url, type, display, source}
   */
  async getSubtitles(tmdbId, isMovie = true, season = null, episode = null) {
    const params = {
      type: isMovie ? 'movie' : 'tv',
      tmdb: tmdbId,
    };
    if (!isMovie && season !== null && episode !== null) {
      params.season = season;
      params.episode = episode;
    }
    const r = await this.client.get(`${SHEGU_SUBS}/subtitles`, { params });
    return (r.data || {}).subtitles || [];
  }

  /** List of streaming servers (public). */
  async listServers() {
    const r = await this.client.get(`${SHEGU_API}/servers`);
    return (r.data || {}).servers || [];
  }

  async _scrapeWatchPage(watchUrl, title) {
    let playwright;
    try {
      playwright = require('playwright');
    } catch (e) {
      throw new Error(
        'Playwright is required. Install: npm install playwright && npx playwright install chromium'
      );
    }

    let capturedUrl = null;

    const browser = await playwright.chromium.launch({ headless: true });
    try {
      const ctx = await browser.newContext({
        userAgent: UA,
        extraHTTPHeaders: { 'Referer': CINEJOY_ORIGIN + '/' },
      });
      const page = await ctx.newPage();

      page.on('request', (req) => {
        if (
          !capturedUrl &&
          req.url().includes('help.earthcleaner.cc') &&
          req.url().endsWith('.m3u8') &&
          req.url().includes('/playlist/')
        ) {
          capturedUrl = req.url();
        }
      });

      try {
        await page.goto(watchUrl, {
          waitUntil: 'networkidle',
          timeout: this.timeout * 1000,
        });
        // Wait up to ~20s for the playlist request to fire
        for (let i = 0; i < 40 && !capturedUrl; i++) {
          await page.waitForTimeout(500);
        }
      } catch (e) {
        // ignore navigation errors — the request interceptor may still have fired
      }
    } finally {
      await browser.close();
    }

    if (!capturedUrl) return [];
    return this._parseMasterPlaylist(capturedUrl, title);
  }

  /**
   * Parse an HLS master playlist into stream variants.
   */
  async _parseMasterPlaylist(masterUrl, title) {
    const r = await this.client.get(masterUrl, {
      headers: { 'Referer': CINEJOY_ORIGIN + '/' },
    });
    const text = r.data;
    const lines = text.split('\n').map(l => l.trim()).filter(Boolean);

    const streams = [];
    let currentInf = {};
    let audioUrl = null;

    for (const line of lines) {
      if (line.startsWith('#EXT-X-MEDIA:')) {
        if (line.includes('TYPE=AUDIO')) {
          const m = line.match(/URI="([^"]+)"/);
          if (m) audioUrl = m[1];
        }
        continue;
      }
      if (line.startsWith('#EXT-X-STREAM-INF:')) {
        const attrs = line.slice('#EXT-X-STREAM-INF:'.length);
        const bandwidthMatch = attrs.match(/BANDWIDTH=(\d+)/);
        const resolutionMatch = attrs.match(/RESOLUTION=([^,]+)/);
        const codecsMatch = attrs.match(/CODECS="([^"]+)"/);
        currentInf = {
          bandwidth: bandwidthMatch ? parseInt(bandwidthMatch[1]) : 0,
          resolution: resolutionMatch ? resolutionMatch[1] : '',
          codecs: codecsMatch ? codecsMatch[1] : '',
        };
        continue;
      }
      if (line.startsWith('http') && currentInf.bandwidth) {
        streams.push({
          title,
          quality: this._resolutionToQuality(currentInf.resolution),
          url: line,
          bandwidth: currentInf.bandwidth,
          resolution: currentInf.resolution,
          codecs: currentInf.codecs,
          audioUrl,
          source: 'cinejoy',
        });
        currentInf = {};
      }
    }
    return streams;
  }

  _resolutionToQuality(res) {
    if (!res) return 'unknown';
    const parts = res.split('x');
    if (parts.length !== 2) return 'unknown';
    const h = parseInt(parts[1]);
    if (isNaN(h)) return 'unknown';
    if (h >= 1080) return '1080p';
    if (h >= 720) return '720p';
    if (h >= 360) return '360p';
    return `${h}p`;
  }
}

// --------------------------------------------------------------------- //
// Demo
// --------------------------------------------------------------------- //
if (require.main === module) (async () => {
  const s = new CinejoyScraper();

  console.log('=== Servers (public) ===');
  try {
    const servers = await s.listServers();
    for (const srv of servers) {
      console.log(`  - ${srv.name}  (${srv.status})`);
    }
  } catch (e) {
    console.error('Servers error:', e.message);
  }

  console.log('\n=== Subtitles for TMDB 693134 (movie) ===');
  try {
    const subs = await s.getSubtitles(693134, true);
    console.log(`  total: ${subs.length}`);
    for (const sub of subs.slice(0, 5)) {
      console.log(`  - ${sub.language}  type=${sub.type}  src=${sub.source}`);
    }
  } catch (e) {
    console.error('Subs error:', e.message);
  }

  console.log('\n=== Master playlist parse test (content ID 9937486, Dune Part Two) ===');
  try {
    const streams = await s._parseMasterPlaylist(
      `${EARTHCDN}/playlist/9937486.m3u8`,
      'Dune: Part Two (test)'
    );
    for (const st of streams) {
      console.log(`  [${st.quality}] ${st.resolution} ${st.bandwidth}bps codecs=${st.codecs}`);
      console.log(`    URL: ${st.url}`);
      console.log(`    Audio: ${st.audioUrl}`);
    }
  } catch (e) {
    console.error('Playlist parse error:', e.message);
  }

  // Full browser flow (requires Playwright)
  if (process.env.RUN_BROWSER === '1') {
    console.log('\n=== Movie streams: TMDB 693134 (Dune Part Two) via browser ===');
    try {
      const streams = await s.getMovieStreams(693134, 'Dune: Part Two');
      console.log(`Got ${streams.length} streams`);
      for (const st of streams) {
        console.log(`  [${st.quality}] ${st.resolution}  ${st.url}`);
      }
    } catch (e) {
      console.error('Browser flow error:', e.message);
    }
  } else {
    console.log('\n(skipping browser flow — set RUN_BROWSER=1 to enable)');
  }
})();

module.exports = { CinejoyScraper };
