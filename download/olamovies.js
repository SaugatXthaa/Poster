/**
 * OlaMovies Scraper — 100% Pure Node.js (NO Playwright, NO Browser)
 * ====================================================================
 * Bypasses Cloudflare on v4.olamovies.mov using the cors.sh proxy.
 *
 * ARCHITECTURE:
 *   1. TMDB API for metadata (get movie title from TMDB ID)
 *   2. Search v4.olamovies.mov via cors.sh proxy: ?s={title}
 *   3. Find the best matching movie page
 *   4. Fetch movie page via cors.sh proxy
 *   5. Extract download links (links.ol-am.top URLs) with quality labels
 *   6. Return stream objects with {name, title, url, quality, source}
 *
 * CF BYPASS:
 *   v4.olamovies.mov has CF managed challenge that blocks direct access.
 *   This scraper uses https://proxy.cors.sh/ as a CORS proxy to bypass CF.
 *
 * DOWNLOAD LINKS:
 *   Movie pages have download links like:
 *     <a href="https://links.ol-am.top/XXX">720p [1.88GB]</a>
 *   These short URLs redirect to links.olamovies.mov/XXX (CF protected).
 *   The final file is on tutorials.ol-am.top (R2 bucket, no CF).
 *   
 *   The short URLs work in browsers (CF solves automatically).
 *   For direct HTTP, the user needs to solve CF or use a browser.
 *
 * USAGE:
 *   node olamovies.js movie 155              # By TMDB ID
 *   node olamovies.js movie "avengers 2012"  # By title
 *   node olamovies.js search "dark knight"   # Search only
 */

'use strict';

const TMDB_API_KEY = '8476a7ab80ad76f0936744df0430e67c';
const TMDB_API = 'https://api.themoviedb.org/3';
const OLA_BASE = 'https://v4.olamovies.mov';
const CORS_PROXY = 'https://proxy.cors.sh/';

const UA =
  'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 ' +
  '(KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';

const sleep = (ms) => new Promise(r => setTimeout(r, ms));

/** Fetch via cors.sh proxy to bypass CF. */
async function fetchViaProxy(url, options = {}) {
  const proxyUrl = CORS_PROXY + url;
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), 30000);
  try {
    const r = await fetch(proxyUrl, {
      ...options,
      signal: controller.signal,
      headers: {
        'User-Agent': UA,
        'Accept': 'text/html,*/*',
        ...options.headers,
      },
    });
    return {
      status: r.status,
      headers: Object.fromEntries(r.headers.entries()),
      body: await r.text(),
    };
  } finally {
    clearTimeout(timer);
  }
}

/** Fetch TMDB API directly. */
async function fetchTMDB(path) {
  const r = await fetch(`${TMDB_API}${path}`, {
    headers: { 'User-Agent': UA },
  });
  return JSON.parse(await r.text());
}

class OlaMoviesScraper {
  constructor(timeout = 30000) {
    this.timeout = timeout;
  }

  async getInfo(tmdbId, type = 'movie') {
    return fetchTMDB(`/${type}/${tmdbId}?api_key=${TMDB_API_KEY}&append_to_response=external_ids`);
  }

  /**
   * Search for movies on olamovies.
   * Returns array of {url, title} objects.
   */
  async search(query) {
    const searchUrl = `${OLA_BASE}/?s=${encodeURIComponent(query)}`;
    const r = await fetchViaProxy(searchUrl);

    // Extract movie page URLs from search results
    const linkRegex = /href="(https:\/\/v4\.olamovies\.mov\/[a-z0-9][a-z0-9-]+\/)"/g;
    const links = new Set();
    let match;
    while ((match = linkRegex.exec(r.body)) !== null) {
      const url = match[1];
      // Filter out non-movie URLs
      if (!url.includes('/page/') &&
          !url.includes('/wp-') &&
          !url.includes('/sso-') &&
          !url.includes('/feed') &&
          !url.includes('/category/') &&
          !url.includes('/tag/') &&
          !url.includes('/author/') &&
          !url.includes('/collection/') &&
          url !== OLA_BASE + '/') {
        links.add(url);
      }
    }

    // Extract titles from the page for each link
    const results = [];
    for (const url of links) {
      // Extract the slug from URL and convert to readable title
      const slug = url.replace(OLA_BASE + '/', '').replace(/\/$/, '');
      const title = slug.replace(/-/g, ' ').replace(/\b\w/g, c => c.toUpperCase());
      results.push({ url, title, slug });
    }

    return results;
  }

  /**
   * Find the best matching movie page for a given title.
   */
  _findBestMatch(results, query) {
    if (results.length === 0) return null;

    const queryLower = query.toLowerCase().trim();
    const queryWords = queryLower.split(/\s+/).filter(w => w.length > 2);

    // Score each result
    let bestMatch = null;
    let bestScore = 0;

    for (const r of results) {
      const titleLower = r.title.toLowerCase();
      let score = 0;

      // Exact match
      if (titleLower === queryLower) score += 100;

      // Contains all query words
      const allWordsMatch = queryWords.every(w => titleLower.includes(w));
      if (allWordsMatch) score += 50;

      // Contains query as substring
      if (titleLower.includes(queryLower)) score += 30;

      // Count matching words
      for (const w of queryWords) {
        if (titleLower.includes(w)) score += 5;
      }

      // Penalize collection pages (they contain multiple movies)
      if (titleLower.includes('collection') || titleLower.includes('trilogy')) {
        score -= 10; // Slight penalty but don't exclude
      }

      if (score > bestScore) {
        bestScore = score;
        bestMatch = r;
      }
    }

    // If no good match, return the first result
    if (!bestMatch && results.length > 0) bestMatch = results[0];

    return bestMatch;
  }

  /**
   * Get all download links for a movie.
   * @param {number|string} tmdbOrImdbId - TMDB ID or movie title
   * @param {string} [title] - Movie title
   * @returns {Promise<Array>} Stream objects
   */
  async getMovieStreams(tmdbOrImdbId, title = '') {
    // Get title from TMDB if not provided
    if (!title && !String(tmdbOrImdbId).startsWith('tt')) {
      try {
        const info = await this.getInfo(tmdbOrImdbId, 'movie');
        title = info.title || String(tmdbOrImdbId);
        // Add year for better matching
        if (info.release_date) {
          title += ' ' + info.release_date.substring(0, 4);
        }
      } catch {
        title = String(tmdbOrImdbId);
      }
    } else if (!title) {
      title = String(tmdbOrImdbId);
    }

    console.error(`[OlaMovies] Searching for: ${title}`);

    // Search for the movie
    const searchResults = await this.search(title);
    if (searchResults.length === 0) {
      throw new Error(`No results found for "${title}"`);
    }

    console.error(`[OlaMovies] Found ${searchResults.length} results`);

    // Find the best matching movie page
    const bestMatch = this._findBestMatch(searchResults, title);
    if (!bestMatch) {
      throw new Error(`No matching movie found for "${title}"`);
    }

    console.error(`[OlaMovies] Best match: ${bestMatch.title}`);

    // Fetch the movie page
    const r = await fetchViaProxy(bestMatch.url);

    // Extract download links
    const streams = this._extractDownloadLinks(r.body, title);

    if (streams.length === 0) {
      throw new Error(`No download links found on ${bestMatch.url}`);
    }

    return streams;
  }

  /**
   * Extract download links from movie page HTML.
   */
  _extractDownloadLinks(html, title) {
    const streams = [];

    // Find all links.ol-am.top links with their text
    const linkRegex = /<a[^>]*href="(https:\/\/links\.ol-am\.top\/[^"]+)"[^>]*>(.*?)<\/a>/gi;
    let match;

    while ((match = linkRegex.exec(html)) !== null) {
      const url = match[1];
      const text = match[2].replace(/<[^>]+>/g, '').trim();

      if (!text || text.length < 3) continue;

      // Parse quality and size
      const qualityMatch = text.match(/(\d{3,4}p|4K|UHD)/i);
      const sizeMatch = text.match(/\[([0-9.]+GB|[0-9.]+MB|[0-9.]+gb|[0-9.]+mb)\]/i);
      const hdrMatch = text.match(/(HDR10\+?|DV|Dolby|HDR|BluRay|HEVC|x265|x264|REMUX|DS4K|SDR)/i);

      let quality = 'unknown';
      if (qualityMatch) {
        quality = qualityMatch[1].toLowerCase();
      } else if (/4k|uhd/i.test(text)) {
        quality = '4k';
      }

      // Add format info
      const formatParts = [];
      if (hdrMatch) {
        const fmt = hdrMatch[1].toUpperCase();
        if (fmt !== quality.toUpperCase()) {
          formatParts.push(fmt);
        }
      }
      if (formatParts.length > 0) {
        quality += ' ' + formatParts.join(' ');
      }

      const size = sizeMatch ? sizeMatch[1].toUpperCase() : '';

      streams.push({
        name: `OlaMovies | ${quality} ${size ? '[' + size + ']' : ''}`.trim(),
        title: title,
        quality: quality.trim(),
        size: size,
        url: url,
        source: 'olamovies',
        headers: {},
      });
    }

    // Sort by quality (highest first)
    const qualityOrder = {
      '4k': 5, '2160p': 5,
      '1080p': 4, '720p': 3, '480p': 2, '360p': 1,
    };
    streams.sort((a, b) => {
      const aQ = qualityOrder[a.quality.toLowerCase().split(' ')[0]] || 0;
      const bQ = qualityOrder[b.quality.toLowerCase().split(' ')[0]] || 0;
      return bQ - aQ;
    });

    return streams;
  }
}

// CLI
if (require.main === module) (async () => {
  const s = new OlaMoviesScraper();
  const args = process.argv.slice(2);
  const command = args[0] || 'movie';
  const id = args[1] || '155';

  console.log('=== OlaMovies Scraper (Pure Node.js) ===');
  console.log(`Command: ${command} ${id}\n`);

  try {
    if (command === 'search') {
      const results = await s.search(args.slice(1).join(' '));
      console.log(`Found ${results.length} results:`);
      for (const r of results.slice(0, 15)) {
        console.log(`  ${r.title}`);
        console.log(`    ${r.url}`);
      }
    } else if (command === 'movie') {
      // If id is a number, treat as TMDB ID; otherwise treat as title
      const isTmdbId = /^\d+$/.test(id);
      const streams = await s.getMovieStreams(isTmdbId ? parseInt(id) : id);
      console.log(`Got ${streams.length} streams:\n`);
      for (const st of streams) {
        console.log(`  [${st.quality}] ${st.name}`);
        console.log(`    URL: ${st.url}`);
        console.log();
      }
    }
  } catch (e) {
    console.error('Error:', e.message);
    if (process.env.DEBUG) console.error(e.stack);
  }
})();

module.exports = { OlaMoviesScraper };
