/**
 * OlaMovies Scraper for Nuvio — 100% Pure Node.js
 * =================================================
 * NO proxies, NO Playwright, NO browser.
 *
 * Accesses v4.olamovies.mov directly. Cloudflare may block datacenter IPs
 * but typically allows residential IPs. No proxy fallback.
 *
 * Flow:
 *   1. TMDB → movie title
 *   2. Search v4.olamovies.mov/?s={title} → movie page URLs
 *   3. Fetch movie page → extract links.ol-am.top download links
 *   4. Follow links.ol-am.top → links.olamovies.mov → actual file URL
 *   5. Return stream objects with {name, title, quality, url, source}
 *
 * The download links are MKV/MP4 files on tutorials.ol-am.top (no CF).
 * These are direct video files that can be played by most media players.
 *
 * If Cloudflare blocks access, the scraper returns empty results.
 */

'use strict';

const TMDB_API_KEY = '8476a7ab80ad76f0936744df0430e67c';
const OLA_BASE = 'https://v4.olamovies.mov';

const UA = 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';
const sleep = (ms) => new Promise(r => setTimeout(r, ms));

// Cache
const _searchCache = new Map();
const _pageCache = new Map();
const _resolveCache = new Map();
const CACHE_TTL = 10 * 60 * 1000;

async function fetchText(url, options = {}, timeout = 20000) {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), timeout);
  try {
    const r = await fetch(url, {
      ...options, signal: controller.signal,
      headers: { 'User-Agent': UA, 'Accept': 'text/html,application/xhtml+xml,*/*', ...options.headers },
    });
    const body = await r.text();

    // Check for CF challenge/block
    if (body.includes('Just a moment') || body.includes('you have been blocked')) {
      throw new Error('CF_BLOCKED');
    }

    return { status: r.status, body, ok: r.ok };
  } finally {
    clearTimeout(timer);
  }
}

async function getTmdbInfo(tmdbId, type) {
  try {
    const r = await fetchText(
      `https://api.themoviedb.org/3/${type}/${tmdbId}?api_key=${TMDB_API_KEY}&append_to_response=external_ids`
    );
    const d = JSON.parse(r.body);
    return {
      title: type === 'tv' ? d.name : d.title,
      year: (d.release_date || d.first_air_date || '').slice(0, 4),
    };
  } catch { return { title: '', year: '' }; }
}

/**
 * Search for movies on olamovies.
 */
async function searchMovie(query) {
  const cached = _searchCache.get(query);
  if (cached && Date.now() - cached.time < CACHE_TTL) return cached.results;

  const searchUrl = `${OLA_BASE}/?s=${encodeURIComponent(query)}`;
  const r = await fetchText(searchUrl);

  // Extract movie page URLs
  const linkRegex = /href="(https:\/\/v4\.olamovies\.mov\/[a-z0-9][a-z0-9-]+\/)"/g;
  const links = new Set();
  let match;
  while ((match = linkRegex.exec(r.body)) !== null) {
    const url = match[1];
    if (!url.includes('/page/') && !url.includes('/wp-') && !url.includes('/sso-') &&
        !url.includes('/feed') && !url.includes('/category/') && !url.includes('/tag/') &&
        !url.includes('/author/') && !url.includes('/collection/') && url !== OLA_BASE + '/') {
      links.add(url);
    }
  }

  const results = [];
  for (const url of links) {
    const slug = url.replace(OLA_BASE + '/', '').replace(/\/$/, '');
    const title = slug.replace(/-/g, ' ').replace(/\b\w/g, c => c.toUpperCase());
    results.push({ url, title, slug });
  }

  _searchCache.set(query, { results, time: Date.now() });
  return results;
}

function findBestMatch(results, query) {
  if (results.length === 0) return null;
  const queryLower = query.toLowerCase().trim();
  const queryWords = queryLower.split(/\s+/).filter(w => w.length > 2);

  let bestMatch = null;
  let bestScore = 0;

  for (const r of results) {
    const titleLower = r.title.toLowerCase();
    let score = 0;

    if (titleLower === queryLower) score += 100;
    if (queryWords.every(w => titleLower.includes(w))) score += 50;
    if (titleLower.includes(queryLower)) score += 30;
    for (const w of queryWords) {
      if (titleLower.includes(w)) score += 5;
    }

    if (score > bestScore) { bestScore = score; bestMatch = r; }
  }

  return bestMatch || results[0];
}

/**
 * Extract download links from movie page HTML.
 */
function extractDownloadLinks(html) {
  const links = [];
  const linkRegex = /<a[^>]*href="(https:\/\/links\.ol-am\.top\/[^"]+)"[^>]*>(.*?)<\/a>/gi;
  let match;

  while ((match = linkRegex.exec(html)) !== null) {
    const url = match[1];
    const text = match[2].replace(/<[^>]+>/g, '').trim();
    if (!text || text.length < 3) continue;

    const qualityMatch = text.match(/(\d{3,4}p|4K|UHD)/i);
    const sizeMatch = text.match(/\[([0-9.]+GB|[0-9.]+MB|[0-9.]+gb|[0-9.]+mb)\]/i);
    const hdrMatch = text.match(/(HDR10\+?|DV|Dolby|HDR|BluRay|HEVC|x265|x264|REMUX|DS4K|SDR)/i);

    let quality = 'unknown';
    if (qualityMatch) quality = qualityMatch[1].toLowerCase();
    else if (/4k|uhd/i.test(text)) quality = '4k';

    const formatParts = [];
    if (hdrMatch) {
      const fmt = hdrMatch[1].toUpperCase();
      if (fmt !== quality.toUpperCase()) formatParts.push(fmt);
    }
    if (formatParts.length > 0) quality += ' ' + formatParts.join(' ');

    links.push({
      quality: quality.trim(),
      size: sizeMatch ? sizeMatch[1].toUpperCase() : '',
      shortUrl: url,
      text,
    });
  }

  // Sort by quality
  const qOrder = { '4k': 5, '2160p': 5, '1080p': 4, '720p': 3, '480p': 2, '360p': 1 };
  links.sort((a, b) =>
    (qOrder[b.quality.toLowerCase().split(' ')[0]] || 0) -
    (qOrder[a.quality.toLowerCase().split(' ')[0]] || 0)
  );

  return links;
}

/**
 * Resolve a links.ol-am.top short URL to the actual file URL.
 * Chain: links.ol-am.top → links.olamovies.mov → tutorials.ol-am.top/file.mkv
 */
async function resolveShortUrl(shortUrl) {
  // Check cache
  const cached = _resolveCache.get(shortUrl);
  if (cached && Date.now() - cached.time < CACHE_TTL) return cached.url;

  try {
    // Follow redirects manually to track the chain
    let currentUrl = shortUrl;
    for (let i = 0; i < 5; i++) {
      const r = await fetch(currentUrl, {
        method: 'GET',
        redirect: 'manual',
        headers: { 'User-Agent': UA },
      });

      const location = r.headers.get('location');
      if (location) {
        currentUrl = location.startsWith('http') ? location : new URL(location, currentUrl).href;
        // If we reached tutorials.ol-am.top, that's the final file URL
        if (currentUrl.includes('tutorials.ol-am.top') || currentUrl.includes('.mkv') || currentUrl.includes('.mp4')) {
          _resolveCache.set(shortUrl, { url: currentUrl, time: Date.now() });
          return currentUrl;
        }
      } else {
        // No redirect — check if the response body has the actual URL
        const body = await r.text();
        // Look for a redirect URL in the HTML/JS
        const urlMatch = body.match(/https:\/\/tutorials\.ol-am\.top[^"'\s]+/) ||
                         body.match(/https:\/\/[^"'\s]+\.mkv/) ||
                         body.match(/https:\/\/[^"'\s]+\.mp4/);
        if (urlMatch) {
          _resolveCache.set(shortUrl, { url: urlMatch[0], time: Date.now() });
          return urlMatch[0];
        }
        break;
      }
    }
  } catch {}

  // If we can't resolve, return the short URL itself
  // (it may work in the user's browser/player which can handle CF)
  return shortUrl;
}

/**
 * Nuvio-compatible getStreams function.
 *
 * @param {number} tmdbId - TMDB ID
 * @param {string} type - 'movie' or 'tv'
 * @param {number} season - Season number (for TV)
 * @param {number} episode - Episode number (for TV)
 * @returns {Promise<Array>} Stream objects
 */
async function getStreams(tmdbId, type, season, episode) {
  const isTV = type === 'tv' || type === 'series';
  const tmdbType = isTV ? 'tv' : 'movie';

  const info = await getTmdbInfo(tmdbId, tmdbType);
  if (!info.title) {
    console.error('[OlaMovies] No title found for TMDB ' + tmdbId);
    return [];
  }

  // Build search query
  let query = info.title;
  if (info.year) query += ' ' + info.year;

  console.error(`[OlaMovies] Searching for: ${query}`);

  // Search
  let results;
  try {
    results = await searchMovie(query);
  } catch (e) {
    if (e.message === 'CF_BLOCKED') {
      console.error('[OlaMovies] Cloudflare blocked access. Try from a different IP.');
      return [];
    }
    throw e;
  }

  if (results.length === 0) {
    console.error('[OlaMovies] No search results');
    return [];
  }

  // Find best match
  const bestMatch = findBestMatch(results, query);
  if (!bestMatch) return [];

  console.error(`[OlaMovies] Best match: ${bestMatch.title}`);

  // Fetch movie page
  let pageHtml;
  try {
    const r = await fetchText(bestMatch.url);
    pageHtml = r.body;
  } catch (e) {
    if (e.message === 'CF_BLOCKED') {
      console.error('[OlaMovies] Cloudflare blocked movie page access.');
      return [];
    }
    throw e;
  }

  // Check page cache
  const cachedPage = _pageCache.get(bestMatch.url);
  if (cachedPage && Date.now() - cachedPage.time < CACHE_TTL) {
    return formatStreams(cachedPage.links, info, isTV, season, episode);
  }

  // Extract download links
  const links = extractDownloadLinks(pageHtml);
  _pageCache.set(bestMatch.url, { links, time: Date.now() });

  if (links.length === 0) {
    console.error('[OlaMovies] No download links found');
    return [];
  }

  console.error(`[OlaMovies] Found ${links.length} download links`);

  // Resolve short URLs to actual file URLs (limit to top 10 to avoid too many requests)
  const topLinks = links.slice(0, 10);
  const resolved = await Promise.all(
    topLinks.map(async (l) => {
      const fileUrl = await resolveShortUrl(l.shortUrl);
      return { ...l, fileUrl };
    })
  );

  return formatStreams(resolved, info, isTV, season, episode);
}

function formatStreams(links, info, isTV, season, episode) {
  const displayTitle = info.title + (isTV && season ? ` S${season}E${episode || 1}` : '');

  return links.map(l => ({
    name: `OlaMovies | ${l.quality} ${l.size ? '[' + l.size + ']' : ''}`.trim(),
    title: displayTitle,
    size: l.size || '',
    description: `${l.quality} ${l.size} | ${l.text}`,
    url: l.fileUrl,
    quality: l.quality,
    language: '',
    source: 'olamovies',
    headers: {},
  }));
}

module.exports = { getStreams };
