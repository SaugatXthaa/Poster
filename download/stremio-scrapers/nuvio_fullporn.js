/**
 * FullPorn Provider — www.fullporn.to
 * =====================================
 * Reverse-engineered flow:
 *   1. Fetch listing page (search/category/latest/popular) → parse video cards
 *   2. Each card has: video ID, title, preview MP4, thumbnail, duration
 *   3. Fetch video page → extract /get_file/ URLs (2160p, 720p, 480p)
 *   4. /get_file/ URLs redirect (302) to CDN → playable MP4 streams
 *
 * Stream sources:
 *   - Full video: https://www.fullporn.to/get_file/{aid}/{hash}/{folder}/{id}_{quality}m.mp4/
 *     (302 redirect to CDN, range-supported, up to 4K)
 *   - Preview: https://cast.fullporn.to/preview/{id}.mp4 (always available, small clip)
 *
 * Quality: 2160p (4K), 720p, 480p — varies per video
 * Content: Full-length adult videos
 *
 * NOTE: fullporn.to uses Cloudflare TLS fingerprinting — Node.js fetch()
 * gets 403/410, but curl (OpenSSL) gets 200/206. So all requests use curl.
 *
 * Pure Node.js (curl via child_process) — no Playwright, no browser.
 *
 * USAGE:
 *   const { getStreams, getCatalog, searchCatalog } = require('./nuvio_fullporn');
 *   const streams = await getStreams(null, 'movie', null, null, { search: 'angela' });
 *   const streams = await getStreams(null, 'movie', null, null, { category: 'latest-updates' });
 *   const catalog = await getCatalog();
 */
'use strict';

const FULLPORN_BASE = 'https://www.fullporn.to';
const PREVIEW_CDN = 'https://cast.fullporn.to';
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 ' +
           '(KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';

const { execSync } = require('child_process');

// ─── HTTP Helper (curl-based, bypasses CF TLS fingerprinting) ────────────────
function curlGetText(url, referer) {
  try {
    let cmd = `curl -sS -L --max-time 15 -A "${UA}"`;
    if (referer) cmd += ` -H "Referer: ${referer}"`;
    cmd += ` "${url}"`;
    return execSync(cmd, {
      encoding: 'utf-8',
      maxBuffer: 10 * 1024 * 1024,
      timeout: 20000,
    });
  } catch (e) {
    return '';
  }
}

async function httpGetText(url, referer) {
  return curlGetText(url, referer);
}

// ─── Decode HTML Entities ────────────────────────────────────────────────────
function decodeHtmlEntities(text) {
  return text
    .replace(/&amp;/g, '&')
    .replace(/&#038;/g, '&')
    .replace(/&#039;/g, "'")
    .replace(/&#8217;/g, "'")
    .replace(/&#8220;/g, '"')
    .replace(/&#8221;/g, '"')
    .replace(/&quot;/g, '"')
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/&nbsp;/g, ' ')
    .trim();
}

// ─── Parse Video Cards from Listing Pages ────────────────────────────────────
function parseCards(html) {
  const cards = [];
  // Pattern: <a href=".../videos/{id}/{slug}/" ... title="..." class="thumb_img">
  //          ... data-preview="...preview/{id}.mp4" ... <img src="..." ... />
  const cardRegex = /<a[^>]*href="https:\/\/www\.fullporn\.to\/videos\/(\d+)\/([^"]+)\/"[^>]*title="([^"]*)"[^>]*>([\s\S]*?)<\/a>/gi;
  let match;
  const seen = new Set();
  while ((match = cardRegex.exec(html)) !== null) {
    const videoId = match[1];
    if (seen.has(videoId)) continue;
    seen.add(videoId);

    const slug = match[2];
    const title = decodeHtmlEntities(match[3]);
    const content = match[4];

    // Extract preview MP4
    const previewMatch = content.match(/data-preview="(https:\/\/cast\.fullporn\.to\/preview\/[^"]+)"/);
    const preview = previewMatch ? previewMatch[1] : `${PREVIEW_CDN}/preview/${videoId}.mp4`;

    // Extract thumbnail
    const thumbMatch = content.match(/src="(https:\/\/img\.fullporn\.to\/[^"]+)"/);
    const thumbnail = thumbMatch ? thumbMatch[1] : null;

    // Extract duration
    const durationMatch = content.match(/(\d+:\d+:\d+|\d+:\d+)/);
    const duration = durationMatch ? durationMatch[1] : null;

    // Extract HD badge
    const isHd = /class="[^"]*hd[^"]*"/i.test(content) || /4k|2160/i.test(content);

    cards.push({
      id: videoId,
      slug,
      title,
      url: `${FULLPORN_BASE}/videos/${videoId}/${slug}/`,
      previewUrl: preview,
      thumbnail,
      duration,
      isHd,
    });
  }
  return cards;
}

// ─── Extract Streams from Video Page ─────────────────────────────────────────
async function extractStreams(videoCard) {
  console.log(`[FullPorn] Fetching video page: ${videoCard.url}`);
  const html = await httpGetText(videoCard.url, FULLPORN_BASE + '/');
  if (!html) return [];

  const streams = [];

  // Find all get_file URLs (full video streams)
  const getFileUrls = html.match(/https:\/\/www\.fullporn\.to\/get_file\/[^"'\s<>]+\.mp4\//gi) || [];
  const uniqueUrls = [...new Set(getFileUrls)].filter(u => !u.includes('download=true'));

  // Map quality from URL pattern: _{quality}m.mp4
  const qualityMap = {
    '2160': '2160p',
    '1080': '1080p',
    '720': '720p',
    '480': '480p',
    '360': '360p',
  };

  for (const url of uniqueUrls) {
    const qMatch = url.match(/_(\d+)m\.mp4/);
    const qualityNum = qMatch ? qMatch[1] : '';
    const quality = qualityMap[qualityNum] || 'unknown';

    streams.push({
      name: `FullPorn\n${quality}`,
      title: videoCard.title,
      url: url,
      quality,
      behaviorHints: {
        notWebReady: true,
        headers: {
          'Referer': FULLPORN_BASE + '/',
          'User-Agent': UA,
        },
      },
      poster: videoCard.thumbnail,
      meta: {
        provider: 'FullPorn',
        source: 'fullporn.to',
        server: 'fullporn.to',
        type: 'mp4',
        quality,
        audio: 'english',
        language: ['en'],
        title: videoCard.title,
        videoId: videoCard.id,
        videoUrl: videoCard.url,
        thumbnail: videoCard.thumbnail,
        duration: videoCard.duration,
        isHd: videoCard.isHd,
        directStream: true,
        mediaType: 'movie',
      },
    });
  }

  // Add preview MP4 as fallback (always available, small clip)
  streams.push({
    name: `FullPorn\nPreview`,
    title: `${videoCard.title} (Preview)`,
    url: videoCard.previewUrl,
    quality: '480p',
    behaviorHints: {
      notWebReady: true,
    },
    poster: videoCard.thumbnail,
    meta: {
      provider: 'FullPorn',
      source: 'cast.fullporn.to',
      server: 'cast.fullporn.to',
      type: 'mp4',
      quality: '480p',
      audio: 'english',
      language: ['en'],
      title: videoCard.title,
      videoId: videoCard.id,
      videoUrl: videoCard.url,
      thumbnail: videoCard.thumbnail,
      duration: videoCard.duration,
      isPreview: true,
      directStream: true,
      mediaType: 'movie',
    },
  });

  // Sort by quality (4K first)
  const qualityOrder = { '2160p': 0, '1080p': 1, '720p': 2, '480p': 3, '360p': 4 };
  streams.sort((a, b) => (qualityOrder[a.quality] ?? 5) - (qualityOrder[b.quality] ?? 5));

  console.log(`[FullPorn] Found ${streams.length} stream(s) for: ${videoCard.title.slice(0, 50)}`);
  return streams;
}

// ─── Search ──────────────────────────────────────────────────────────────────
async function searchFullporn(query, page) {
  page = page || 1;
  const url = page > 1
    ? `${FULLPORN_BASE}/search/${encodeURIComponent(query)}/${page}/`
    : `${FULLPORN_BASE}/search/${encodeURIComponent(query)}/`;
  console.log(`[FullPorn] Search: ${url}`);
  const html = await httpGetText(url);
  if (!html) return [];
  return parseCards(html);
}

// ─── Fetch by Category ───────────────────────────────────────────────────────
async function fetchByCategory(category, page) {
  page = page || 1;
  const url = page > 1
    ? `${FULLPORN_BASE}/${category}/${page}/`
    : `${FULLPORN_BASE}/${category}/`;
  console.log(`[FullPorn] Category: ${url}`);
  const html = await httpGetText(url);
  if (!html) return [];
  return parseCards(html);
}

// ─── Fetch Latest ────────────────────────────────────────────────────────────
async function fetchLatest(page) {
  return await fetchByCategory('latest-updates', page);
}

// ─── Fetch Popular ───────────────────────────────────────────────────────────
async function fetchPopular(page) {
  return await fetchByCategory('most-popular', page);
}

// ─── Fetch Top Rated ─────────────────────────────────────────────────────────
async function fetchTopRated(page) {
  return await fetchByCategory('top-rated', page);
}

// ─── Get Available Categories ────────────────────────────────────────────────
async function getCategories() {
  const html = await httpGetText(FULLPORN_BASE + '/');
  if (!html) return [];
  // The site has these main listing pages
  return [
    { slug: 'latest-updates', name: 'Latest Updates' },
    { slug: 'most-popular', name: 'Most Popular' },
    { slug: 'top-rated', name: 'Top Rated' },
  ];
}

// ─── Main: getStreams ────────────────────────────────────────────────────────
async function getStreams(tmdbId, mediaType, season, episode, opts) {
  const { search, category, page, videoId } = opts || {};
  console.log(`[FullPorn] getStreams: search="${search || ''}" category="${category || ''}" videoId="${videoId || ''}"`);

  try {
    let cards = [];

    if (videoId) {
      // Direct video ID lookup — construct the URL
      // First try to find the slug by searching for the video ID
      let videoUrl = `${FULLPORN_BASE}/videos/${videoId}/`;
      
      // Try to fetch the page directly (the slug is optional, the ID is what matters)
      const html = await httpGetText(videoUrl, FULLPORN_BASE + '/');
      if (html) {
        // If the page loaded, extract streams directly
        const titleMatch = html.match(/<h1[^>]*>([^<]+)<\/h1>/);
        const title = titleMatch ? decodeHtmlEntities(titleMatch[1]) : `Video ${videoId}`;
        const card = {
          id: videoId,
          slug: '',
          title,
          url: videoUrl,
          previewUrl: `${PREVIEW_CDN}/preview/${videoId}.mp4`,
          thumbnail: null,
          duration: null,
          isHd: false,
        };
        // Extract streams from this page directly
        return await extractStreams(card);
      }
      
      // If the page didn't load (404), try searching for the video ID
      const searchHtml = await httpGetText(`${FULLPORN_BASE}/search/${videoId}/`);
      if (searchHtml) {
        const cards = parseCards(searchHtml);
        const card = cards.find(c => c.id === videoId);
        if (card) {
          return await extractStreams(card);
        }
      }
      
      // Fallback: return just the preview
      return [{
        name: `FullPorn\nPreview`,
        title: `Video ${videoId} (Preview)`,
        url: `${PREVIEW_CDN}/preview/${videoId}.mp4`,
        quality: '480p',
        behaviorHints: { notWebReady: true },
        meta: {
          provider: 'FullPorn',
          source: 'cast.fullporn.to',
          type: 'mp4',
          quality: '480p',
          title: `Video ${videoId}`,
          videoId: videoId,
          isPreview: true,
          directStream: true,
          mediaType: 'movie',
        },
      }];
    } else if (search) {
      cards = await searchFullporn(search, page);
    } else if (category) {
      cards = await fetchByCategory(category, page);
    } else {
      cards = await fetchLatest(page);
    }

    if (!cards.length) {
      console.log('[FullPorn] No cards found');
      return [];
    }

    console.log(`[FullPorn] Found ${cards.length} videos, extracting streams from top 5...`);

    // Extract streams from top 5 cards
    const topCards = cards.slice(0, 5);
    const streamPromises = topCards.map(card => extractStreams(card));
    const results = await Promise.all(streamPromises);

    const allStreams = [];
    for (const streams of results) {
      allStreams.push(...streams);
    }

    // Sort: 4K first, then 1080p, 720p, etc.
    const qualityOrder = { '2160p': 0, '1080p': 1, '720p': 2, '480p': 3, '360p': 4 };
    allStreams.sort((a, b) => (qualityOrder[a.quality] ?? 5) - (qualityOrder[b.quality] ?? 5));

    console.log(`[FullPorn] Returning ${allStreams.length} stream(s)`);
    return allStreams;
  } catch (e) {
    console.error(`[FullPorn] Error: ${e.message}`);
    return [];
  }
}

// ─── Catalog Functions ───────────────────────────────────────────────────────
let catalogCache = null;
let catalogFetchTime = 0;
const CATALOG_CACHE_TTL = 6 * 60 * 60 * 1000;

async function getCatalog(forceRefresh) {
  const now = Date.now();
  if (!forceRefresh && catalogCache && (now - catalogFetchTime) < CATALOG_CACHE_TTL) {
    return catalogCache;
  }

  const allCards = [];
  // Fetch first 2 pages of latest updates (faster than 5)
  for (let p = 1; p <= 2; p++) {
    const cards = await fetchLatest(p);
    allCards.push(...cards);
    if (cards.length === 0) break;
  }

  catalogCache = allCards;
  catalogFetchTime = now;
  console.log(`[FullPorn] Catalog: ${allCards.length} items`);
  return allCards;
}

async function searchCatalog(query, limit) {
  const cards = await searchFullporn(query);
  return cards.slice(0, limit || 20);
}

async function getCatalogMetas(opts) {
  const { page, pageSize, search, category, sort } = opts || {};
  const pageNum = page || 1;
  const size = pageSize || 50;

  let entries;
  if (search) {
    entries = await searchCatalog(search, 100);
  } else if (category) {
    entries = await fetchByCategory(category, pageNum);
  } else if (sort === 'popular') {
    entries = await fetchPopular(pageNum);
  } else if (sort === 'top-rated') {
    entries = await fetchTopRated(pageNum);
  } else {
    entries = await getCatalog();
  }

  const start = (pageNum - 1) * size;
  const pageEntries = entries.slice(start, start + size);

  const metas = pageEntries.map(card => ({
    id: `fullporn:${card.id}`,
    type: 'movie',
    name: card.title,
    poster: card.thumbnail,
    description: `${card.title}${card.duration ? ' [' + card.duration + ']' : ''}${card.isHd ? ' HD' : ''}`,
    releaseInfo: card.duration || '',
    genres: card.isHd ? ['HD'] : [],
    links: [{ name: 'FullPorn', category: 'stream', url: card.url }],
    meta: {
      provider: 'FullPorn',
      videoId: card.id,
      videoUrl: card.url,
      previewUrl: card.previewUrl,
      thumbnail: card.thumbnail,
      duration: card.duration,
      isHd: card.isHd,
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

async function getStreamsByVideoId(videoId) {
  return await getStreams(null, 'movie', null, null, { videoId });
}

// ─── Export ───────────────────────────────────────────────────────────────────
if (typeof module !== 'undefined' && module.exports) {
  module.exports = {
    getStreams,
    getCatalog,
    getCatalogMetas,
    searchCatalog,
    getStreamsByVideoId,
    getCategories,
    fetchByCategory,
    fetchLatest,
    fetchPopular,
    fetchTopRated,
    parseCards,
    extractStreams,
    PROVIDER_NAME: 'FullPorn',
  };
} else {
  global.getStreams = getStreams;
  global.getCatalog = getCatalog;
  global.getCatalogMetas = getCatalogMetas;
  global.searchCatalog = searchCatalog;
}
