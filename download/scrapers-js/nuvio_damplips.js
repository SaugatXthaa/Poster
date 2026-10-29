/**
 * Damplips Provider — www.damplips.com
 * =====================================
 * Reverse-engineered flow:
 *   1. Search via web.archive.org proxy (main site is CF-protected from datacenter IPs)
 *      → Archive serves the WP search results page with all post data inline
 *   2. Parse search results: each <article> has data-id, title, category, date,
 *      poster image, and <source> tag with video.damplips.com/video7/{folder}/{file}.mp4
 *   3. Return video CDN URL directly — video.damplips.com is NOT CF-protected
 *      (HTTP 200, video/mp4, range-supported, direct stream)
 *
 * Stream source: https://video.damplips.com/video7/{folder}/{file}.mp4
 * Stream format: Direct MP4 (range-seekable, no HLS)
 * Content type: Adult short films / clips
 *
 * The video CDN is fully accessible from any IP (no CF, no auth, no referer needed).
 * The main site (www.damplips.com) is CF-protected, so we use web.archive.org
 * as a search proxy. The archived pages contain the full article HTML with
 * inline video sources, so we can extract streams without touching the live site.
 *
 * Pure Node.js fetch() — no Playwright, no browser.
 *
 * USAGE:
 *   const { getStreams, searchCatalog, getCatalog } = require('./nuvio_damplips');
 *   const streams = await getStreams(null, 'movie', null, null, { search: 'blonde' });
 *   const catalog = await getCatalog();
 */
'use strict';

const XPRIME = false; // This is not xprime — marker for enriched_anime_sources compatibility
const DAMPLIPS_BASE = 'https://www.damplips.com';
const VIDEO_CDN_BASE = 'https://video.damplips.com';
const ARCHIVE_BASE = 'https://web.archive.org';

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 ' +
           '(KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';

// ─── HTTP Helpers ─────────────────────────────────────────────────────────────
async function httpGet(url, referer) {
  const headers = {
    'User-Agent': UA,
    'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
    'Accept-Language': 'en-US,en;q=0.9',
    'Accept-Encoding': 'gzip, deflate, br',
  };
  if (referer) headers['Referer'] = referer;
  try {
    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), 15000);
    const res = await fetch(url, { headers, signal: controller.signal });
    clearTimeout(timer);
    return res;
  } catch (e) {
    return null;
  }
}

async function httpGetText(url, referer) {
  const res = await httpGet(url, referer);
  if (!res || !res.ok) return '';
  try {
    return await res.text();
  } catch (e) {
    return '';
  }
}

// ─── Fetch via Web Archive (bypasses CF on main site) ────────────────────────
async function fetchViaArchive(path) {
  // Use a recent archive timestamp
  const timestamp = '20260804170626';
  const archiveUrl = `${ARCHIVE_BASE}/web/${timestamp}id_/${DAMPLIPS_BASE}${path}`;
  console.log(`[Damplips] Archive: ${archiveUrl}`);
  return await httpGetText(archiveUrl);
}

// ─── Parse Article Entries from HTML ─────────────────────────────────────────
function parseArticles(html) {
  const articles = [];
  
  // Pattern: <article class="...post-{ID}..."> ... </article>
  const articleRegex = /<article[^>]*class="[^"]*post-(\d+)[^"]*"[^>]*>([\s\S]*?)<\/article>/gi;
  let match;
  while ((match = articleRegex.exec(html)) !== null) {
    const postId = match[1];
    const content = match[2];
    
    // Extract title
    const titleMatch = content.match(/<a[^>]*href="[^"]*\/p\/[^"]*"[^>]*>([^<]+)<\/a>/i);
    const title = titleMatch ? titleMatch[1].trim() : `Post ${postId}`;
    
    // Extract video source
    const videoMatch = content.match(/video\.damplips\.com\/(video\d+\/\d+\/\d+\.mp4)/i);
    const videoPath = videoMatch ? videoMatch[1] : null;
    
    // Extract poster
    const posterMatch = content.match(/poster="[^"]*(video\.damplips\.com\/[^"]+\.jpg)"/i);
    const poster = posterMatch ? posterMatch[1] : null;
    
    // Extract category
    const catMatch = content.match(/category-([a-z0-9-]+)/i);
    const category = catMatch ? catMatch[1] : null;
    
    // Extract date
    const dateMatch = content.match(/<span class="updated[^"]*">([^<]+)<\/span>/i);
    const date = dateMatch ? dateMatch[1].trim() : null;
    
    // Extract category link
    const catLinkMatch = content.match(/\/c\/([a-z0-9-]+)/i);
    const categorySlug = catLinkMatch ? catLinkMatch[1] : category;
    
    if (videoPath) {
      articles.push({
        id: postId,
        title: decodeHtmlEntities(title),
        url: `${DAMPLIPS_BASE}/p/${postId}/`,
        videoUrl: `${VIDEO_CDN_BASE}/${videoPath}`,
        poster: poster ? `https://${poster}` : null,
        category: categorySlug,
        date,
      });
    }
  }
  
  return articles;
}

function decodeHtmlEntities(text) {
  return text
    .replace(/&amp;/g, '&')
    .replace(/&#038;/g, '&')
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/&quot;/g, '"')
    .replace(/&#039;/g, "'")
    .replace(/&apos;/g, "'")
    .replace(/&#8217;/g, "'")
    .replace(/&#8216;/g, "'")
    .replace(/&#8220;/g, '"')
    .replace(/&#8221;/g, '"')
    .replace(/&#8230;/g, '...')
    .replace(/&nbsp;/g, ' ');
}

// ─── Search ──────────────────────────────────────────────────────────────────
async function searchDamplips(query, page) {
  page = page || 1;
  const searchPath = page > 1 ? `/page/${page}/?s=${encodeURIComponent(query)}` : `/?s=${encodeURIComponent(query)}`;
  const html = await fetchViaArchive(searchPath);
  if (!html) {
    console.log('[Damplips] Search returned empty');
    return [];
  }
  
  const articles = parseArticles(html);
  console.log(`[Damplips] Search "${query}": ${articles.length} results`);
  return articles;
}

// ─── Fetch Catalog (latest posts) ────────────────────────────────────────────
async function fetchCatalog(page) {
  page = page || 1;
  const path = page > 1 ? `/page/${page}/` : '/';
  const html = await fetchViaArchive(path);
  if (!html) return [];
  
  return parseArticles(html);
}

// ─── Fetch by Category ───────────────────────────────────────────────────────
async function fetchByCategory(category, page) {
  page = page || 1;
  const path = `/c/${category}/${page > 1 ? `page/${page}/` : ''}`;
  const html = await fetchViaArchive(path);
  if (!html) return [];
  
  return parseArticles(html);
}

// ─── Get Available Categories ────────────────────────────────────────────────
async function getCategories() {
  const html = await fetchViaArchive('/');
  if (!html) return [];
  
  const cats = new Set();
  const catRegex = /\/c\/([a-z0-9-]+)/gi;
  let match;
  while ((match = catRegex.exec(html)) !== null) {
    cats.add(match[1]);
  }
  return [...cats].sort();
}

// ─── Build Enriched Stream Object ────────────────────────────────────────────
function buildStream(article) {
  return {
    name: `Damplips\n${article.category || 'clip'}`,
    title: article.title,
    url: article.videoUrl,
    quality: '720p', // default quality (videos are typically 720p webrips)
    behaviorHints: {
      notWebReady: true,
      headers: {}, // No headers needed — CDN is fully open
    },
    poster: article.poster,
    meta: {
      provider: 'Damplips',
      source: 'video.damplips.com',
      server: 'video.damplips.com',
      type: 'mp4',
      quality: '720p',
      audio: 'english',
      language: ['en'],
      title: article.title,
      postId: article.id,
      postUrl: article.url,
      category: article.category,
      date: article.date,
      poster: article.poster,
      directStream: true,
      mediaType: 'movie',
    },
  };
}

// ─── Main: getStreams ────────────────────────────────────────────────────────
/**
 * Get streams from Damplips.
 * Since Damplips is an adult clip site (not TMDB-indexed), this function
 * uses a search query instead of TMDB IDs.
 *
 * @param {number} tmdbId - Ignored (not TMDB-indexed)
 * @param {string} mediaType - Ignored (always 'movie')
 * @param {number} season - Ignored
 * @param {number} episode - Ignored
 * @param {Object} opts - { search: 'query' } or { category: 'category-slug' }
 */
async function getStreams(tmdbId, mediaType, season, episode, opts) {
  const { search, category, postId } = opts || {};
  
  console.log(`[Damplips] getStreams: search="${search || ''}" category="${category || ''}" postId="${postId || ''}"`);
  
  try {
    let articles = [];
    
    if (postId) {
      // Fetch specific post by ID
      const html = await fetchViaArchive(`/p/${postId}/`);
      if (html) {
        articles = parseArticles(html);
      }
    } else if (search) {
      articles = await searchDamplips(search);
    } else if (category) {
      articles = await fetchByCategory(category);
    } else {
      // Default: latest posts
      articles = await fetchCatalog();
    }
    
    if (!articles.length) {
      console.log('[Damplips] No articles found');
      return [];
    }
    
    // Convert to enriched stream objects
    const streams = articles.map(buildStream);
    
    console.log(`[Damplips] Returning ${streams.length} stream(s)`);
    return streams;
  } catch (e) {
    console.error(`[Damplips] Error: ${e.message}`);
    return [];
  }
}

// ─── Catalog Functions ───────────────────────────────────────────────────────
let catalogCache = null;
let catalogFetchTime = 0;
const CATALOG_CACHE_TTL = 6 * 60 * 60 * 1000; // 6 hours

/**
 * Get the full catalog (latest posts across multiple pages).
 * Fetches 5 pages of ~16 posts each = ~80 posts.
 */
async function getCatalog(forceRefresh) {
  const now = Date.now();
  if (!forceRefresh && catalogCache && (now - catalogFetchTime) < CATALOG_CACHE_TTL) {
    console.log(`[Damplips] Using cached catalog (${catalogCache.length} items)`);
    return catalogCache;
  }
  
  const allArticles = [];
  // Fetch first 5 pages
  for (let page = 1; page <= 5; page++) {
    const articles = await fetchCatalog(page);
    allArticles.push(...articles);
    if (articles.length === 0) break; // no more pages
  }
  
  catalogCache = allArticles;
  catalogFetchTime = now;
  console.log(`[Damplips] Catalog: ${allArticles.length} items`);
  return allArticles;
}

/**
 * Search the catalog by title.
 */
async function searchCatalog(query, limit) {
  // Use the site's search directly (via archive)
  const articles = await searchDamplips(query);
  return articles.slice(0, limit || 20);
}

/**
 * Get catalog as Stremio meta objects.
 */
async function getCatalogMetas(opts) {
  const { page, pageSize, search, category } = opts || {};
  const pageNum = page || 1;
  const size = pageSize || 50;
  
  let entries;
  if (search) {
    entries = await searchCatalog(search, 100);
  } else if (category) {
    entries = await fetchByCategory(category, pageNum);
  } else {
    entries = await getCatalog();
  }
  
  // Paginate
  const start = (pageNum - 1) * size;
  const pageEntries = entries.slice(start, start + size);
  
  const metas = pageEntries.map(article => ({
    id: `damplips:${article.id}`,
    type: 'movie',
    name: article.title,
    poster: article.poster,
    description: `${article.title}${article.date ? ' — ' + article.date : ''}${article.category ? ' [' + article.category + ']' : ''}`,
    releaseInfo: article.date ? article.date : '',
    genres: article.category ? [article.category] : [],
    links: [{ name: 'Damplips', category: 'stream', url: article.url }],
    meta: {
      provider: 'Damplips',
      postId: article.id,
      videoUrl: article.videoUrl,
      category: article.category,
      date: article.date,
      poster: article.poster,
    },
  }));
  
  return {
    metas,
    total: entries.length,
    page: pageNum,
    pageSize: size,
    hasMore: (start + size) < entries.length,
  };
}

/**
 * Get streams for a specific post by ID.
 */
async function getStreamsByPostId(postId) {
  const html = await fetchViaArchive(`/p/${postId}/`);
  if (!html) return [];
  
  const articles = parseArticles(html);
  if (!articles.length) return [];
  
  return [buildStream(articles[0])];
}

// ─── Export ───────────────────────────────────────────────────────────────────
if (typeof module !== 'undefined' && module.exports) {
  module.exports = {
    getStreams,
    getCatalog,
    getCatalogMetas,
    searchCatalog,
    getStreamsByPostId,
    getCategories,
    fetchByCategory,
    parseArticles,
    PROVIDER_NAME: 'Damplips',
  };
} else {
  global.getStreams = getStreams;
  global.getCatalog = getCatalog;
  global.getCatalogMetas = getCatalogMetas;
  global.searchCatalog = searchCatalog;
}
