/**
 * Qorno Provider — www.qorno.com
 * =====================================
 * Reverse-engineered flow:
 *   1. Fetch category/search page from qorno.com (NO Cloudflare — fully accessible)
 *   2. Parse <div class="card sub" data-public-id="..."> blocks to extract:
 *      - Video ID, title, duration, thumbnail, source site
 *      - /out/?l={base64} redirect link → decode to get actual source URL
 *   3. For each video, fetch the source site page and extract direct MP4 URL
 *   4. Return playable MP4 stream
 *
 * Qorno is an aggregator — it indexes videos from 23+ source sites:
 *   - xhamster, faphouse, eporner, javhdtube, drtuber, babestube, nuvid,
 *   - pornlib, zbporn, xtits, momvids, pumpmilf, etc.
 *
 * Stream extraction works for sources that expose direct MP4 URLs:
 *   - xtits.xxx: /get_file/{hash}/{folder}/{id}_{quality}.mp4 → HTTP 206 ✓
 *   - zbporn.com: /get_file/{hash}/{folder}/{id}_{quality}.mp4 → HTTP 206 ✓
 *   - drtuber.com: /media/videos/tmb/{id}/{id}.mp4 → HTTP 206 (preview clips)
 *   - eporner.com: gvideo.eporner.com/{id}/{id}.mp4 → HTTP 200 (with referer)
 *   - Others: returned as embed URLs (playable in browser/Stremio player)
 *
 * Pure Node.js fetch() — no Playwright, no browser.
 *
 * USAGE:
 *   const { getStreams, getCatalog, searchCatalog } = require('./nuvio_qorno');
 *   const streams = await getStreams(null, 'movie', null, null, { search: 'blonde' });
 *   const streams = await getStreams(null, 'movie', null, null, { category: 'japanese' });
 *   const catalog = await getCatalog();
 */
'use strict';

const QORNO_BASE = 'https://www.qorno.com';
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 ' +
           '(KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';

const { execSync } = require('child_process');

// ─── HTTP Helpers ─────────────────────────────────────────────────────────────
// Qorno's Cloudflare checks TLS fingerprints — Node.js fetch() gets blocked (403)
// but curl (OpenSSL) passes. So we use curl for all Qorno requests.
// For source sites (xtits, zbporn, etc.), we use fetch() since they don't have CF.

async function httpGetText(url, referer) {
  // Use curl for Qorno.com (CF-protected with TLS fingerprinting)
  // Use fetch() for other sites
  if (url.includes('qorno.com')) {
    return curlGetText(url, referer);
  }
  return fetchGetText(url, referer);
}

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

async function fetchGetText(url, referer) {
  const headers = {
    'User-Agent': UA,
    'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
    'Accept-Language': 'en-US,en;q=0.9',
  };
  if (referer) headers['Referer'] = referer;
  try {
    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), 15000);
    const res = await fetch(url, { headers, signal: controller.signal });
    clearTimeout(timer);
    if (!res.ok) return '';
    return await res.text();
  } catch (e) {
    return '';
  }
}

// ─── Decode /out/ Links ──────────────────────────────────────────────────────
function decodeOutLink(encoded) {
  try {
    const decoded = decodeURIComponent(encoded);
    const padding = 4 - (decoded.length % 4);
    const padded = padding !== 4 ? decoded + '='.repeat(padding) : decoded;
    const bytes = Buffer.from(padded, 'base64');
    const text = bytes.toString('utf-8');
    // Extract the actual URL from the decoded binary data
    const urls = text.match(/https?:\/\/[^\s\x00-\x1f<>"']+/g);
    if (urls && urls.length > 0) {
      return urls[0].replace(/[\x00-\x1f]/g, '').trim();
    }
    return null;
  } catch (e) {
    return null;
  }
}

// ─── Parse Cards from Qorno Page ─────────────────────────────────────────────
function parseCards(html) {
  const cards = [];
  // Pattern: <div class="card sub..." data-public-id="ID"> ... </div>
  const cardRegex = /<div class="card sub[^"]*"[^>]*data-public-id="([^"]+)"[^>]*>([\s\S]*?)(?=<div class="card sub|<div class="cards-container|$)/gi;
  let match;
  while ((match = cardRegex.exec(html)) !== null) {
    const publicId = match[1];
    const content = match[2];

    // Extract title
    const titleMatch = content.match(/title="([^"]+)"/);
    const title = titleMatch ? decodeHtmlEntities(titleMatch[1].trim()) : `Video ${publicId}`;

    // Extract /out/ link and decode it
    const outMatch = content.match(/href="\/out\/\?l=([^&"]+)/);
    let sourceUrl = null;
    if (outMatch) {
      sourceUrl = decodeOutLink(outMatch[1]);
    }

    // Extract source site
    const sourceMatch = content.match(/\/source\/([a-z0-9-]+)/);
    const source = sourceMatch ? sourceMatch[1] : 'unknown';

    // Extract thumbnail
    const thumbMatch = content.match(/(c\d\.ttcache\.com\/[^"\s]+)/);
    const thumbnail = thumbMatch ? `https://${thumbMatch[1]}` : null;

    // Extract duration
    const durationMatch = content.match(/(\d+:\d+(?::\d+)?)/);
    const duration = durationMatch ? durationMatch[1] : null;

    // Extract HD badge
    const isHd = /<span[^>]*>\s*HD\s*<\/span>/i.test(content) || /badge.*hd/i.test(content);

    cards.push({
      id: publicId,
      title,
      sourceUrl,
      source,
      thumbnail,
      duration,
      isHd,
    });
  }
  return cards;
}

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

// ─── Source-Specific Stream Extractors ──────────────────────────────────────

// XTits: /get_file/{hash}/{folder}/{id}_{quality}.mp4
async function extractXtits(url) {
  const html = await httpGetText(url);
  if (!html) return [];
  const mp4s = [...new Set(html.match(/https?:\/\/[^"'\s<>]+\.mp4[^"'\s<>]*/gi) || [])];
  // Filter to get_file URLs (actual videos, not preview screenshots)
  const videoUrls = mp4s.filter(u => u.includes('/get_file/') && !u.includes('preview'));
  return videoUrls.map(u => ({
    url: u,
    quality: /_720p/.test(u) ? '720p' : /_1080p/.test(u) ? '1080p' : /_360p/.test(u) ? '360p' : '480p',
    source: 'xtits',
  }));
}

// ZBPorn: /get_file/{hash}/{folder}/{id}_{quality}.mp4
async function extractZbporn(url) {
  const html = await httpGetText(url);
  if (!html) return [];
  const mp4s = [...new Set(html.match(/https?:\/\/[^"'\s<>]+\.mp4[^"'\s<>]*/gi) || [])];
  const videoUrls = mp4s.filter(u => u.includes('/get_file/') && !u.includes('preview'));
  return videoUrls.map(u => ({
    url: u,
    quality: /_720p/.test(u) ? '720p' : /_1080p/.test(u) ? '1080p' : /_360p/.test(u) ? '360p' : '480p',
    source: 'zbporn',
  }));
}

// DrTuber: direct /tmb/ MP4 (preview clips, but playable)
async function extractDrtuber(url) {
  // DrTuber URLs come as drtvid.com/v/{id} — need to get the video page
  const html = await httpGetText(url);
  if (!html) return [];
  // Find the video ID
  const idMatch = url.match(/\/v\/([a-zA-Z0-9]+)/);
  if (!idMatch) return [];
  // The /tmb/ MP4s are preview clips
  const mp4s = [...new Set(html.match(/https?:\/\/[^"'\s<>]+\.mp4[^"'\s<>]*/gi) || [])];
  const videoUrls = mp4s.filter(u => u.includes('/tmb/') && u.includes('/media/videos/'));
  return videoUrls.slice(0, 1).map(u => ({
    url: u,
    quality: '480p',
    source: 'drtuber',
  }));
}

// Eporner: gvideo.eporner.com/{id}/{id}.mp4
async function extractEporner(url) {
  const html = await httpGetText(url);
  if (!html) return [];
  const mp4s = [...new Set(html.match(/https?:\/\/[^"'\s<>]+\.mp4[^"'\s<>]*/gi) || [])];
  const videoUrls = mp4s.filter(u => u.includes('gvideo.eporner.com'));
  return videoUrls.map(u => ({
    url: u,
    quality: '720p',
    source: 'eporner',
    headers: { 'Referer': 'https://www.eporner.com/' },
  }));
}

// Generic: return embed URL as-is (playable in Stremio's browser-based player)
function extractGeneric(url, source) {
  return [{
    url: url,
    quality: '480p',
    source: source,
    isEmbed: true,
  }];
}

// ─── Extract Stream from Source URL ─────────────────────────────────────────
async function extractStream(card) {
  if (!card.sourceUrl) return [];

  const url = card.sourceUrl;
  const source = card.source;

  try {
    let streams = [];
    if (source === 'xtits' || /xtits\./.test(url)) {
      streams = await extractXtits(url);
    } else if (source === 'zbporn' || /zbporn\./.test(url)) {
      streams = await extractZbporn(url);
    } else if (source === 'drtuber' || /drtvid\.com|drtuber\.com/.test(url)) {
      streams = await extractDrtuber(url);
    } else if (source === 'eporner' || /eporner\.com/.test(url)) {
      streams = await extractEporner(url);
    } else {
      streams = extractGeneric(url, source);
    }

    return streams.map(s => ({
      name: `Qorno\n${s.source} ${s.quality}`,
      title: card.title,
      url: s.url,
      quality: s.quality,
      behaviorHints: {
        notWebReady: true,
        ...(s.headers && { headers: s.headers }),
        ...(s.isEmbed && { proxyHeaders: { request: { 'User-Agent': UA } } }),
      },
      poster: card.thumbnail,
      meta: {
        provider: 'Qorno',
        source: s.source,
        server: s.source,
        type: s.isEmbed ? 'embed' : 'mp4',
        quality: s.quality,
        audio: 'english',
        language: ['en'],
        title: card.title,
        videoId: card.id,
        sourceUrl: card.sourceUrl,
        duration: card.duration,
        thumbnail: card.thumbnail,
        isHd: card.isHd,
        directStream: !s.isEmbed,
        mediaType: 'movie',
      },
    }));
  } catch (e) {
    console.error(`[Qorno] Extract error for ${source}: ${e.message}`);
    return [];
  }
}

// ─── Search Qorno ────────────────────────────────────────────────────────────
async function searchQorno(query, page) {
  page = page || 1;
  // Qorno search uses /search/{query} (not /search/a/{query})
  const url = page > 1
    ? `${QORNO_BASE}/search/${encodeURIComponent(query)}/${page}`
    : `${QORNO_BASE}/search/${encodeURIComponent(query)}`;
  console.log(`[Qorno] Search: ${url}`);
  const html = await httpGetText(url);
  if (!html) return [];
  // Check for CF challenge
  if (html.includes('Just a moment...') || html.includes('challenge-platform')) {
    console.log('[Qorno] Search is CF-protected, trying category fallback...');
    // Try as a category instead
    return await fetchByCategory(query.toLowerCase().replace(/\s+/g, '-'), page);
  }
  return parseCards(html);
}

// ─── Fetch by Category ───────────────────────────────────────────────────────
async function fetchByCategory(category, page) {
  page = page || 1;
  const url = page > 1
    ? `${QORNO_BASE}/category/${category}/${page}`
    : `${QORNO_BASE}/category/${category}`;
  console.log(`[Qorno] Category: ${url}`);
  const html = await httpGetText(url);
  if (!html) return [];
  return parseCards(html);
}

// ─── Fetch Latest (homepage) ─────────────────────────────────────────────────
async function fetchLatest(page) {
  page = page || 1;
  const url = page > 1 ? `${QORNO_BASE}/new/${page}` : `${QORNO_BASE}/new`;
  console.log(`[Qorno] Latest: ${url}`);
  const html = await httpGetText(url);
  if (!html) return [];
  return parseCards(html);
}

// ─── Fetch Popular ───────────────────────────────────────────────────────────
async function fetchPopular(page) {
  page = page || 1;
  const url = page > 1 ? `${QORNO_BASE}/popular/${page}` : `${QORNO_BASE}/popular`;
  console.log(`[Qorno] Popular: ${url}`);
  const html = await httpGetText(url);
  if (!html) return [];
  return parseCards(html);
}

// ─── Get Available Categories ────────────────────────────────────────────────
async function getCategories() {
  const html = await httpGetText(QORNO_BASE + '/');
  if (!html) return [];
  const cats = new Set();
  const catRegex = /\/category\/([a-z0-9-]+)/gi;
  let match;
  while ((match = catRegex.exec(html)) !== null) {
    cats.add(match[1]);
  }
  return [...cats].sort();
}

// ─── Get Available Sources ───────────────────────────────────────────────────
async function getSources() {
  const html = await httpGetText(QORNO_BASE + '/');
  if (!html) return [];
  const sources = new Set();
  const srcRegex = /\/source\/([a-z0-9-]+)/gi;
  let match;
  while ((match = srcRegex.exec(html)) !== null) {
    sources.add(match[1]);
  }
  return [...sources].sort();
}

// ─── Main: getStreams ────────────────────────────────────────────────────────
async function getStreams(tmdbId, mediaType, season, episode, opts) {
  const { search, category, page, popular } = opts || {};
  console.log(`[Qorno] getStreams: search="${search || ''}" category="${category || ''}"`);

  try {
    let cards = [];
    if (search) {
      cards = await searchQorno(search, page);
    } else if (category) {
      cards = await fetchByCategory(category, page);
    } else if (popular) {
      cards = await fetchPopular(page);
    } else {
      cards = await fetchLatest(page);
    }

    if (!cards.length) {
      console.log('[Qorno] No cards found');
      return [];
    }

    console.log(`[Qorno] Found ${cards.length} videos, extracting streams...`);

    // Extract streams from top 10 cards (in parallel for speed)
    const topCards = cards.slice(0, 10);
    const streamPromises = topCards.map(card => extractStream(card));
    const results = await Promise.all(streamPromises);

    // Flatten and filter
    const allStreams = [];
    for (const streams of results) {
      allStreams.push(...streams);
    }

    // Sort: direct streams first, then by quality
    const qualityOrder = { '1080p': 0, '720p': 1, '480p': 2, '360p': 3 };
    allStreams.sort((a, b) => {
      // Direct streams before embeds
      if (a.meta.directStream && !b.meta.directStream) return -1;
      if (!a.meta.directStream && b.meta.directStream) return 1;
      return (qualityOrder[a.quality] ?? 4) - (qualityOrder[b.quality] ?? 4);
    });

    console.log(`[Qorno] Returning ${allStreams.length} stream(s)`);
    return allStreams;
  } catch (e) {
    console.error(`[Qorno] Error: ${e.message}`);
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
  // Fetch first 5 pages of latest
  for (let p = 1; p <= 5; p++) {
    const cards = await fetchLatest(p);
    allCards.push(...cards);
    if (cards.length === 0) break;
  }

  catalogCache = allCards;
  catalogFetchTime = now;
  console.log(`[Qorno] Catalog: ${allCards.length} items`);
  return allCards;
}

async function searchCatalog(query, limit) {
  const cards = await searchQorno(query);
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
  } else {
    entries = await getCatalog();
  }

  const start = (pageNum - 1) * size;
  const pageEntries = entries.slice(start, start + size);

  const metas = pageEntries.map(card => ({
    id: `qorno:${card.id}`,
    type: 'movie',
    name: card.title,
    poster: card.thumbnail,
    description: `${card.title}${card.duration ? ' [' + card.duration + ']' : ''}${card.source ? ' — ' + card.source : ''}`,
    genres: [card.source],
    releaseInfo: card.duration || '',
    links: card.sourceUrl ? [{ name: 'Qorno', category: 'stream', url: card.sourceUrl }] : [],
    meta: {
      provider: 'Qorno',
      videoId: card.id,
      source: card.source,
      sourceUrl: card.sourceUrl,
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
  // Search for the video by ID — Qorno doesn't have direct /p/{id} pages
  // Instead, we search for the ID in the search results
  const html = await httpGetText(`${QORNO_BASE}/search/a/${videoId}`);
  if (!html) return [];
  const cards = parseCards(html);
  const card = cards.find(c => c.id === videoId);
  if (!card) return [];
  return await extractStream(card);
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
    getSources,
    fetchByCategory,
    fetchLatest,
    fetchPopular,
    parseCards,
    decodeOutLink,
    PROVIDER_NAME: 'Qorno',
  };
} else {
  global.getStreams = getStreams;
  global.getCatalog = getCatalog;
  global.getCatalogMetas = getCatalogMetas;
  global.searchCatalog = searchCatalog;
}
