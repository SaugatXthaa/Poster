/**
 * XPrimeHub Provider — xprimehub.bond
 * ====================================
 * Reverse-engineered flow:
 *   1. TMDB → title + year (via TMDB API)
 *   2. Search xprimehub.bond/?s={title} → parse HTML for post links
 *   3. Fetch post page → extract nexdrive.fit download link
 *   4. Fetch nexdrive.fit page → extract fastdl.zip embed link
 *   5. Fetch fastdl.zip embed page → extract `reurl` variable → Google CDN URL
 *   6. Return Google CDN URL as playable stream (video/mkv, range-supported)
 *
 * Supports: Movies, TV Shows, Web Series, Short Films
 * Quality: 720p, 1080p (detected from post title)
 * Stream format: Direct MKV/MP4 from Google CDN (video-downloads.googleusercontent.com)
 *
 * Pure Node.js fetch() — no Playwright, no browser.
 *
 * NOTE: The search endpoint (/?s=) is CF-protected from datacenter IPs.
 * It will work from residential IPs (like the Nuvio app on a phone).
 * The post pages, nexdrive pages, and fastdl pages are NOT CF-protected.
 *
 * USAGE:
 *   const { getStreams } = require('./nuvio_xprimehub');
 *   const streams = await getStreams(693134, 'movie');
 *   const streams = await getStreams(82856, 'tv', 1, 1);
 */
'use strict';

const TMDB_API_KEY = '439c478a771f35c05022f9feabcca01c';
const TMDB_BASE = 'https://api.themoviedb.org/3';
const XPRIME_BASE = 'https://xprimehub.bond';
const NEXDRIVE_BASE = 'https://nexdrive.fit';
const FASTDL_BASE = 'https://fastdl.zip';

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 ' +
           '(KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';

const HEADERS = {
  'User-Agent': UA,
  'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
  'Accept-Language': 'en-US,en;q=0.9',
};

// ─── HTTP Helpers ─────────────────────────────────────────────────────────────
async function httpGet(url, referer) {
  const headers = { ...HEADERS };
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
  return await res.text();
}

// ─── TMDB Metadata ────────────────────────────────────────────────────────────
async function getTmdbInfo(tmdbId, mediaType) {
  const type = mediaType === 'tv' ? 'tv' : 'movie';
  const url = `${TMDB_BASE}/${type}/${tmdbId}?api_key=${TMDB_API_KEY}&language=en-US`;
  try {
    const res = await fetch(url);
    if (!res.ok) return null;
    return await res.json();
  } catch (e) {
    return null;
  }
}

// ─── Search XPrimeHub ────────────────────────────────────────────────────────
async function searchXprime(query) {
  // Try the WP search first (works from residential IPs)
  const searchUrl = `${XPRIME_BASE}/?s=${encodeURIComponent(query)}`;
  console.log(`[XPrimeHub] Search: ${searchUrl}`);
  const html = await httpGetText(searchUrl, XPRIME_BASE + '/');
  
  if (html && !html.includes('Just a moment...') && !html.includes('challenge-platform')) {
    const results = parseSearchResults(html);
    if (results.length > 0) return results;
  } else {
    console.log('[XPrimeHub] Search is CF-protected, trying sitemap fallback...');
  }

  // Fallback: use sitemap to find matching posts
  return await searchViaSitemap(query);
}

// ─── Parse Search Results HTML ────────────────────────────────────────────────
function parseSearchResults(html) {
  const results = [];
  const seen = new Set();
  
  // Pattern 1: article with link
  const articleMatches = html.matchAll(/<article[^>]*>[\s\S]*?<a[^>]+href="(https:\/\/xprimehub\.bond\/([^"'?]+)\/?)"[^>]*>([^<]*)<\/a>/gi);
  for (const m of articleMatches) {
    const url = m[1];
    const slug = m[2];
    const title = m[3].trim();
    if (!seen.has(slug) && slug && !slug.includes('page/') && !slug.includes('category/') && !slug.includes('by-') && !slug.includes('tag/')) {
      seen.add(slug);
      results.push({ url, slug, title });
    }
  }

  // Pattern 2: any post link (broader)
  if (results.length === 0) {
    const linkMatches = html.matchAll(/href="(https:\/\/xprimehub\.bond\/([^"'?\/]+)\/?)"/gi);
    for (const m of linkMatches) {
      const url = m[1];
      const slug = m[2];
      if (!seen.has(slug) && slug && !slug.includes('page') && !slug.includes('category') && 
          !slug.includes('by-') && !slug.includes('tag') && !slug.includes('feed') &&
          !slug.includes('wp-') && !slug.includes('author') && slug.length > 5) {
        seen.add(slug);
        results.push({ url, slug, title: slug.replace(/-/g, ' ') });
      }
    }
  }

  console.log(`[XPrimeHub] Found ${results.length} search results`);
  return results;
}

// ─── Sitemap-based Search (CF-free fallback) ─────────────────────────────────
async function searchViaSitemap(query) {
  // Normalize query for slug matching
  const queryLower = query.toLowerCase().replace(/[^a-z0-9\s]/g, '').trim();
  const queryWords = queryLower.split(/\s+/).filter(w => w.length > 2);
  
  // Fetch sitemaps (try first 3, each has ~200 URLs)
  const allUrls = [];
  for (let i = 1; i <= 3; i++) {
    const sitemapUrl = `${XPRIME_BASE}/post-sitemap${i}.xml`;
    console.log(`[XPrimeHub] Fetching sitemap: ${sitemapUrl}`);
    const xml = await httpGetText(sitemapUrl);
    if (!xml) continue;
    
    // Extract URLs from sitemap
    const urlMatches = xml.match(/<loc>(https:\/\/xprimehub\.bond\/[^<]+)<\/loc>/g) || [];
    for (const m of urlMatches) {
      const url = m.match(/<loc>([^<]+)<\/loc>/)[1];
      const slug = url.split('/').filter(s => s).pop();
      if (slug && slug.length > 5) {
        allUrls.push({ url, slug, title: slug.replace(/-/g, ' ') });
      }
    }
    if (allUrls.length > 500) break; // enough data
  }
  
  console.log(`[XPrimeHub] Sitemap: ${allUrls.length} URLs indexed`);
  
  // Match query words against slugs
  const matches = [];
  for (const entry of allUrls) {
    const slugLower = entry.slug.toLowerCase().replace(/[^a-z0-9\s]/g, '');
    let score = 0;
    for (const word of queryWords) {
      if (slugLower.includes(word)) score++;
    }
    if (score >= Math.min(2, queryWords.length)) {
      matches.push({ ...entry, score });
    }
  }
  
  // Sort by score (best matches first)
  matches.sort((a, b) => b.score - a.score);
  
  console.log(`[XPrimeHub] Sitemap matches: ${matches.length}`);
  return matches.slice(0, 10); // top 10 matches
}

// ─── Find Best Match ──────────────────────────────────────────────────────────
function findBestMatch(results, title, year) {
  if (!results.length) return null;
  
  const cleanTitle = title.toLowerCase().replace(/[^a-z0-9\s]/g, '').trim();
  const titleWords = cleanTitle.split(/\s+/).filter(w => w.length > 2);
  
  let best = null;
  let bestScore = 0;
  
  for (const r of results) {
    const resultText = (r.slug + ' ' + r.title).toLowerCase().replace(/[^a-z0-9\s]/g, '');
    
    // Score based on how many title words appear in the result
    let score = 0;
    for (const word of titleWords) {
      if (resultText.includes(word)) score += 1;
    }
    
    // Bonus for year match
    if (year && resultText.includes(String(year))) score += 2;
    
    // Bonus for exact title match
    if (resultText.includes(cleanTitle)) score += 3;
    
    // Penalize for too many extra words (likely different content)
    const resultWords = resultText.split(/\s+/);
    if (resultWords.length > titleWords.length * 4) score -= 1;
    
    if (score > bestScore) {
      bestScore = score;
      best = r;
    }
  }
  
  // Only return if we have a reasonable match
  if (bestScore >= Math.max(2, titleWords.length * 0.5)) {
    console.log(`[XPrimeHub] Best match: ${best.title} (score: ${bestScore})`);
    return best;
  }
  
  console.log(`[XPrimeHub] No confident match (best score: ${bestScore})`);
  return null;
}

// ─── Extract Nexdrive Link from Post Page ────────────────────────────────────
async function getNexdriveLink(postUrl) {
  console.log(`[XPrimeHub] Fetching post: ${postUrl}`);
  const html = await httpGetText(postUrl, XPRIME_BASE + '/');
  if (!html) return null;

  // Find nexdrive.fit links
  const matches = html.match(/href="(https:\/\/nexdrive\.fit\/[^"]+)"/gi) || [];
  if (!matches.length) return null;

  // Extract unique URLs
  const links = [...new Set(matches.map(m => m.match(/href="([^"]+)"/)[1]))];
  console.log(`[XPrimeHub] Found ${links.length} nexdrive link(s)`);
  return links;
}

// ─── Extract Stream URL from Nexdrive Page ───────────────────────────────────
async function getStreamFromNexdrive(nexdriveUrl, referer) {
  console.log(`[XPrimeHub] Fetching nexdrive: ${nexdriveUrl}`);
  const html = await httpGetText(nexdriveUrl, referer || NEXDRIVE_BASE + '/');
  if (!html) return null;

  // Find all external download links (fastdl.zip, vcloud.fit, filebee.xyz)
  const fastdlLinks = [];
  const fastdlMatches = html.match(/href="(https:\/\/fastdl\.zip\/[^"]+)"/gi) || [];
  for (const m of fastdlMatches) {
    fastdlLinks.push(m.match(/href="([^"]+)"/)[1]);
  }

  // Also collect vcloud and filebee as fallbacks
  const vcloudLinks = [];
  const vcloudMatches = html.match(/href="(https:\/\/vcloud\.fit\/[^"]+)"/gi) || [];
  for (const m of vcloudMatches) {
    vcloudLinks.push(m.match(/href="([^"]+)"/)[1]);
  }

  const filebeeLinks = [];
  const filebeeMatches = html.match(/href="(https:\/\/filebee\.xyz\/[^"]+)"/gi) || [];
  for (const m of filebeeMatches) {
    filebeeLinks.push(m.match(/href="([^"]+)"/)[1]);
  }

  console.log(`[XPrimeHub] Found ${fastdlLinks.length} fastdl, ${vcloudLinks.length} vcloud, ${filebeeLinks.length} filebee links`);

  // Try fastdl.zip links first (these resolve to Google CDN)
  for (const fastdlUrl of fastdlLinks) {
    const streamUrl = await resolveFastdl(fastdlUrl, nexdriveUrl);
    if (streamUrl) return { url: streamUrl, source: 'fastdl' };
  }

  // vcloud and filebee are CF-protected — return them as embed URLs
  // (will work from residential IPs / Stremio player)
  if (vcloudLinks.length) {
    return { url: vcloudLinks[0], source: 'vcloud' };
  }
  if (filebeeLinks.length) {
    return { url: filebeeLinks[0], source: 'filebee' };
  }

  return null;
}

// ─── Resolve FastDL to Google CDN URL ────────────────────────────────────────
async function resolveFastdl(fastdlUrl, referer) {
  console.log(`[XPrimeHub] Resolving fastdl: ${fastdlUrl}`);
  const html = await httpGetText(fastdlUrl, referer);
  if (!html) return null;

  // Extract the reurl variable which contains the Google CDN URL
  const m = html.match(/var\s+reurl\s*=\s*"([^"]+)"/);
  if (!m) {
    console.log('[XPrimeHub] No reurl found in fastdl page');
    return null;
  }

  const reurl = m[1];
  // The reurl is: https://fastdl.zip/dl.php?link=https://video-downloads.googleusercontent.com/...
  // Extract the Google CDN URL
  const googleMatch = reurl.match(/link=(https:\/\/video-downloads\.googleusercontent\.com\/.+)/);
  if (googleMatch) {
    console.log('[XPrimeHub] Found Google CDN URL');
    return googleMatch[1];
  }

  // If not Google CDN, return the dl.php URL itself (may redirect)
  if (reurl.includes('dl.php')) {
    console.log('[XPrimeHub] Found dl.php URL (no Google CDN)');
    return reurl;
  }

  return null;
}

// ─── Detect Quality from Text ────────────────────────────────────────────────
function detectQuality(text) {
  const t = (text || '').toLowerCase();
  if (/2160p|4k|uhd/.test(t)) return '2160p';
  if (/1080p|fhd|1080/.test(t)) return '1080p';
  if (/720p|hd|720/.test(t)) return '720p';
  if (/480p|sd|480/.test(t)) return '480p';
  return '720p'; // default
}

function detectLanguage(text) {
  const t = (text || '').toLowerCase();
  if (/hindi|hin|dual/.test(t)) return 'hindi';
  if (/tamil|tam/.test(t)) return 'tamil';
  if (/telugu|tel/.test(t)) return 'telugu';
  if (/bengali|ben/.test(t)) return 'bengali';
  if (/korean|kor|kdrama/.test(t)) return 'korean';
  if (/japanese|jpn|anime/.test(t)) return 'japanese';
  if (/chinese|mandarin|chi/.test(t)) return 'chinese';
  return 'english';
}

function detectFormat(url) {
  const u = (url || '').toLowerCase();
  if (/\.mkv/.test(u)) return 'mkv';
  if (/\.mp4/.test(u)) return 'mp4';
  if (/\.m3u8/.test(u)) return 'hls';
  return 'mkv'; // default for Google CDN
}

// ─── Main: getStreams ────────────────────────────────────────────────────────
async function getStreams(tmdbId, mediaType, season, episode) {
  console.log(`[XPrimeHub] getStreams: ${tmdbId} ${mediaType} S${season || '?'}E${episode || '?'}`);
  
  try {
    // 1. Get metadata from TMDB
    const meta = await getTmdbInfo(tmdbId, mediaType);
    if (!meta) {
      console.log('[XPrimeHub] No TMDB metadata');
      return [];
    }
    
    const title = mediaType === 'tv' ? meta.name : meta.title;
    const year = (mediaType === 'tv' ? meta.first_air_date : meta.release_date || '').substring(0, 4);
    console.log(`[XPrimeHub] Title: ${title} (${year})`);

    // 2. Search XPrimeHub
    const searchQuery = season && episode ? `${title} s${season}e${episode}` : title;
    const results = await searchXprime(searchQuery);
    if (!results.length) return [];

    // 3. Find best match
    const best = findBestMatch(results, title, year);
    if (!best) return [];

    // 4. Get nexdrive links from post page
    const nexdriveLinks = await getNexdriveLink(best.url);
    if (!nexdriveLinks || !nexdriveLinks.length) return [];

    // 5. For TV: try to find episode-specific nexdrive link
    // XPrimeHub typically has one nexdrive link per quality, not per episode
    // The nexdrive page itself contains episode-specific download links
    // For movies: just use the first nexdrive link
    
    // 6. Resolve streams from each nexdrive link
    const streams = [];
    const seenUrls = new Set();
    
    for (const nexdriveUrl of nexdriveLinks.slice(0, 5)) { // limit to 5 links
      const streamData = await getStreamFromNexdrive(nexdriveUrl, best.url);
      if (streamData && streamData.url && !seenUrls.has(streamData.url)) {
        seenUrls.add(streamData.url);
        
        // Detect quality from the nexdrive URL / post title
        const qualityText = `${best.slug} ${best.title} ${nexdriveUrl}`;
        const quality = detectQuality(qualityText);
        const language = detectLanguage(qualityText);
        const format = detectFormat(streamData.url);
        
        const isGoogleCdn = streamData.url.includes('googleusercontent.com');
        
        streams.push({
          name: `XPrimeHub\n${quality} ${language}`,
          title: `${title} - ${quality} (${language})`,
          url: streamData.url,
          quality,
          behaviorHints: {
            notWebReady: true,
            headers: isGoogleCdn ? {} : { 'Referer': 'https://nexdrive.fit/' },
          },
          meta: {
            provider: 'XPrimeHub',
            source: streamData.source,
            server: isGoogleCdn ? 'googleusercontent.com' : streamData.source,
            type: format,
            quality,
            audio: language,
            language: [language.slice(0, 2)],
            title: title,
            year: year,
            mediaType: mediaType === 'tv' ? 'tv' : 'movie',
            season: mediaType === 'tv' ? (season || 1) : null,
            episode: mediaType === 'tv' ? (episode || 1) : null,
            directStream: isGoogleCdn,
          },
        });
      }
    }

    // Sort by quality (1080p first, then 720p, etc.)
    const qualityOrder = { '2160p': 0, '1080p': 1, '720p': 2, '480p': 3 };
    streams.sort((a, b) => (qualityOrder[a.quality] ?? 4) - (qualityOrder[b.quality] ?? 4));

    console.log(`[XPrimeHub] Returning ${streams.length} stream(s)`);
    return streams;
  } catch (e) {
    console.error(`[XPrimeHub] Error: ${e.message}`);
    return [];
  }
}

// ─── Catalog: Fetch all posts from sitemaps ──────────────────────────────────
// XPrimeHub has 32 sitemaps × 200 posts each = ~6400 posts total.
// We parse each post URL slug to extract: title, year, quality, season, language, type.

const TOTAL_SITEMAPS = 32;
const POSTS_PER_SITEMAP = 200;

// In-memory catalog cache
let catalogCache = null;
let catalogFetchTime = 0;
const CATALOG_CACHE_TTL = 6 * 60 * 60 * 1000; // 6 hours

/**
 * Parse a post URL slug into structured metadata.
 * Slug format: "{title}-{year}-{language}-{type}-{quality}-{extra}"
 * Examples:
 *   "chausar-2026-s01-hindi-complete-web-series-720p-1080p"
 *     → title: "Chausar", year: 2026, season: 1, language: hindi, type: series, qualities: [720p, 1080p]
 *   "only-yours-2026-english-adult-short-film-720p-download"
 *     → title: "Only Yours", year: 2026, language: english, type: movie, quality: 720p
 */
function parsePostSlug(slug) {
  // Remove trailing dashes and split
  const parts = slug.replace(/-+/g, '-').replace(/^-|-$/g, '').split('-');
  
  // Find where the title ends (usually before the year)
  let titleEndIdx = parts.length;
  let year = null;
  for (let i = 0; i < parts.length; i++) {
    if (/^\d{4}$/.test(parts[i])) {
      const y = parseInt(parts[i], 10);
      if (y >= 1970 && y <= 2030) {
        year = y;
        titleEndIdx = i;
        break;
      }
    }
  }
  
  // Title is everything before the year
  const titleParts = parts.slice(0, titleEndIdx);
  const title = titleParts.join(' ')
    .replace(/\b\d+\b/g, m => m) // keep numbers in title
    .replace(/\s+/g, ' ')
    .trim()
    .replace(/\b\w/g, c => c.toUpperCase()); // Title Case
  
  // Parse metadata from the remaining parts (after the year)
  const metaParts = parts.slice(titleEndIdx + 1).join(' ');
  const metaLower = metaParts.toLowerCase();
  
  // Detect season
  let season = null;
  const sMatch = metaLower.match(/s(\d{1,2})(?:e(\d{1,2}))?/);
  if (sMatch) {
    season = parseInt(sMatch[1], 10);
  }
  
  // Detect quality
  const qualities = [];
  if (/2160p|4k|uhd/.test(metaLower)) qualities.push('2160p');
  if (/1080p/.test(metaLower)) qualities.push('1080p');
  if (/720p/.test(metaLower)) qualities.push('720p');
  if (/480p/.test(metaLower)) qualities.push('480p');
  if (!qualities.length) qualities.push('720p'); // default
  
  // Detect language
  let language = 'english';
  if (/hindi/.test(metaLower)) language = 'hindi';
  else if (/tamil/.test(metaLower)) language = 'tamil';
  else if (/telugu/.test(metaLower)) language = 'telugu';
  else if (/bengali/.test(metaLower)) language = 'bengali';
  else if (/korean|kdrama/.test(metaLower)) language = 'korean';
  else if (/japanese|anime/.test(metaLower)) language = 'japanese';
  else if (/chinese|mandarin/.test(metaLower)) language = 'chinese';
  
  // Detect type (movie vs series)
  let type = 'movie';
  if (/web-series|webseries|complete-series|s\d+e\d+|s\d{1,2}\b|season/.test(metaLower)) {
    type = 'tv';
  } else if (/short-film|movie|film/.test(metaLower)) {
    type = 'movie';
  }
  
  return {
    title,
    year,
    season,
    qualities,
    language,
    type,
    slug,
  };
}

/**
 * Fetch all post URLs from all sitemaps.
 * Returns array of { url, slug, parsed } objects.
 */
async function fetchAllPosts(maxSitemaps) {
  const max = maxSitemaps || TOTAL_SITEMAPS;
  const allPosts = [];
  
  console.log(`[XPrimeHub] Fetching up to ${max} sitemaps...`);
  
  // Fetch sitemaps in parallel (batches of 5)
  for (let batch = 0; batch < max; batch += 5) {
    const batchEnd = Math.min(batch + 5, max);
    const promises = [];
    for (let i = batch + 1; i <= batchEnd; i++) {
      const sitemapUrl = `${XPRIME_BASE}/post-sitemap${i}.xml`;
      promises.push(
        httpGetText(sitemapUrl).then(xml => {
          if (!xml) return [];
          const urlMatches = xml.match(/<loc>(https:\/\/xprimehub\.bond\/[^<]+)<\/loc>/g) || [];
          const posts = [];
          for (const m of urlMatches) {
            const url = m.match(/<loc>([^<]+)<\/loc>/)[1];
            const slug = url.split('/').filter(s => s).pop();
            if (slug && slug.length > 5 && !slug.includes('sitemap')) {
              posts.push({ url, slug });
            }
          }
          return posts;
        })
      );
    }
    
    const results = await Promise.all(promises);
    for (const posts of results) {
      allPosts.push(...posts);
    }
    
    if (allPosts.length > 0 && batch === 0) {
      console.log(`[XPrimeHub] First batch: ${allPosts.length} posts so far`);
    }
  }
  
  // Parse all slugs
  const parsed = allPosts.map(p => ({
    ...p,
    parsed: parsePostSlug(p.slug),
  }));
  
  console.log(`[XPrimeHub] Total posts indexed: ${parsed.length}`);
  return parsed;
}

/**
 * Get the full catalog (with caching).
 * Returns array of catalog entries.
 */
async function getCatalog(forceRefresh) {
  const now = Date.now();
  if (!forceRefresh && catalogCache && (now - catalogFetchTime) < CATALOG_CACHE_TTL) {
    console.log(`[XPrimeHub] Using cached catalog (${catalogCache.length} items)`);
    return catalogCache;
  }
  
  const posts = await fetchAllPosts();
  catalogCache = posts;
  catalogFetchTime = now;
  return posts;
}

/**
 * Search the catalog by title.
 * Returns matching catalog entries sorted by relevance.
 */
async function searchCatalog(query, limit) {
  const catalog = await getCatalog();
  const queryLower = query.toLowerCase().replace(/[^a-z0-9\s]/g, '').trim();
  const queryWords = queryLower.split(/\s+/).filter(w => w.length > 2);
  
  if (!queryWords.length) return [];
  
  const matches = [];
  for (const entry of catalog) {
    const titleLower = (entry.parsed.title || '').toLowerCase().replace(/[^a-z0-9\s]/g, '');
    const slugLower = entry.slug.toLowerCase().replace(/[^a-z0-9\s]/g, '');
    const searchText = titleLower + ' ' + slugLower;
    
    let score = 0;
    for (const word of queryWords) {
      if (titleLower.includes(word)) score += 2; // title match scores higher
      else if (slugLower.includes(word)) score += 1;
    }
    
    // Bonus for exact title match
    if (titleLower === queryLower) score += 10;
    
    if (score >= Math.min(2, queryWords.length)) {
      matches.push({ ...entry, score });
    }
  }
  
  matches.sort((a, b) => b.score - a.score);
  return matches.slice(0, limit || 20);
}

/**
 * Get catalog as Stremio meta objects (for catalog endpoint).
 * Returns { metas: [...] } with Stremio-compatible meta objects.
 */
async function getCatalogMetas(opts) {
  const { type, page, pageSize, search } = opts || {};
  const pageNum = page || 1;
  const size = pageSize || 50;
  
  let entries;
  if (search) {
    entries = await searchCatalog(search, 100);
  } else {
    entries = await getCatalog();
  }
  
  // Filter by type if specified
  let filtered = entries;
  if (type && type !== 'all') {
    filtered = entries.filter(e => e.parsed.type === type);
  }
  
  // Paginate
  const start = (pageNum - 1) * size;
  const pageEntries = filtered.slice(start, start + size);
  
  // Convert to Stremio meta format
  const metas = pageEntries.map(entry => ({
    id: `xprimehub:${entry.slug}`,
    type: entry.parsed.type === 'tv' ? 'series' : 'movie',
    name: entry.parsed.title,
    poster: null, // XPrimeHub doesn't have posters in slugs
    description: `${entry.parsed.title}${entry.parsed.year ? ' (' + entry.parsed.year + ')' : ''} — ${entry.parsed.language} ${entry.parsed.qualities.join('/')}`,
    releaseInfo: entry.parsed.year ? String(entry.parsed.year) : '',
    genres: [entry.parsed.language],
    links: [{ name: 'XPrimeHub', category: 'stream', url: entry.url }],
    meta: {
      provider: 'XPrimeHub',
      slug: entry.slug,
      url: entry.url,
      qualities: entry.parsed.qualities,
      language: entry.parsed.language,
      season: entry.parsed.season,
      type: entry.parsed.type,
    },
  }));
  
  return {
    metas,
    total: filtered.length,
    page: pageNum,
    pageSize: size,
    hasMore: (start + size) < filtered.length,
  };
}

/**
 * Get streams for a catalog entry (by slug).
 * This bypasses the TMDB search and goes directly to the post URL.
 */
async function getStreamsBySlug(slug, season, episode) {
  const postUrl = `${XPRIME_BASE}/${slug}/`;
  console.log(`[XPrimeHub] Getting streams for slug: ${slug}`);
  
  // Fetch post page and extract nexdrive links
  const nexdriveLinks = await getNexdriveLink(postUrl);
  if (!nexdriveLinks || !nexdriveLinks.length) return [];
  
  // Resolve streams from each nexdrive link
  const streams = [];
  const seenUrls = new Set();
  const parsed = parsePostSlug(slug);
  
  for (const nexdriveUrl of nexdriveLinks.slice(0, 5)) {
    const streamData = await getStreamFromNexdrive(nexdriveUrl, postUrl);
    if (streamData && streamData.url && !seenUrls.has(streamData.url)) {
      seenUrls.add(streamData.url);
      
      const qualityText = `${slug} ${nexdriveUrl}`;
      const quality = parsed.qualities[0] || detectQuality(qualityText);
      const language = parsed.language || detectLanguage(qualityText);
      const format = detectFormat(streamData.url);
      const isGoogleCdn = streamData.url.includes('googleusercontent.com');
      
      streams.push({
        name: `XPrimeHub\n${quality} ${language}`,
        title: `${parsed.title}${parsed.year ? ' (' + parsed.year + ')' : ''}${parsed.season ? ' S' + parsed.season : ''}${episode ? ' E' + episode : ''} - ${quality}`,
        url: streamData.url,
        quality,
        behaviorHints: {
          notWebReady: true,
          headers: isGoogleCdn ? {} : { 'Referer': 'https://nexdrive.fit/' },
        },
        meta: {
          provider: 'XPrimeHub',
          source: streamData.source,
          server: isGoogleCdn ? 'googleusercontent.com' : streamData.source,
          type: format,
          quality,
          audio: language,
          language: [language.slice(0, 2)],
          title: parsed.title,
          year: parsed.year,
          mediaType: parsed.type,
          season: parsed.season || (season ? parseInt(season) : null),
          episode: episode ? parseInt(episode) : null,
          slug: slug,
          directStream: isGoogleCdn,
        },
      });
    }
  }
  
  // Sort by quality
  const qualityOrder = { '2160p': 0, '1080p': 1, '720p': 2, '480p': 3 };
  streams.sort((a, b) => (qualityOrder[a.quality] ?? 4) - (qualityOrder[b.quality] ?? 4));
  
  console.log(`[XPrimeHub] Returning ${streams.length} stream(s) for slug: ${slug}`);
  return streams;
}

// ─── Export ───────────────────────────────────────────────────────────────────
if (typeof module !== 'undefined' && module.exports) {
  module.exports = {
    getStreams,
    getCatalog,
    getCatalogMetas,
    searchCatalog,
    getStreamsBySlug,
    parsePostSlug,
    fetchAllPosts,
    PROVIDER_NAME: 'XPrimeHub',
  };
} else {
  global.getStreams = getStreams;
  global.getCatalog = getCatalog;
  global.getCatalogMetas = getCatalogMetas;
  global.searchCatalog = searchCatalog;
  global.getStreamsBySlug = getStreamsBySlug;
}
