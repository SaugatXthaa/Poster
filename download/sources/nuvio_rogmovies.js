/**
 * RogMovies Provider for Stremio/Nuvio Addon
 * ============================================
 * Site: https://rogmovies.one
 *
 * URL patterns:
 *   - Search API:  GET https://rogmovies.one/ts-search.php?query={query}
 *                  Returns JSON: { found, hits: [{ document: { id, imdb_id, permalink,
 *                  post_title, post_thumbnail, post_date, category[] } }] }
 *   - Post page:   https://rogmovies.one{permalink}  (e.g. /download-dhamaal-4-...)
 *   - Download:     nexdrive.fit/genxfm{id}/  → redirects to nexdrive.beer
 *                   nexdrive.beer page has multiple mirror links:
 *                   - megaup.net/.../...mkv (direct .mkv)
 *                   - fastdl.beer/embed.php?download=... (embed)
 *                   - 1fichier.com/?... (file host)
 *                   - gofile.io/d/... (file host)
 *                   - filebee.xyz/file/... (file host)
 *
 * Post page structure:
 *   <h5>Movie Title (Year) Hindi NF WEB-DL 2160p 4K HEVC [12.7GB]</h5>
 *   <a href="https://nexdrive.fit/genxfm{id}/"><button class="dwd-button">Download Now</button></a>
 *
 * Metadata extraction from h5 text:
 *   - Quality:  2160p | 1080p | 720p | 480p | 4K
 *   - Codec:    HEVC/x265 | H264/x264 | 10bit
 *   - Audio:    DDP5.1 | DD5.1 | AC3 | EAC3
 *   - Size:     [300MB] | [1.1GB] | [12.7GB]
 *   - Source:   WEB-DL | BluRay | NF | AMZN
 */
'use strict';

const { fetchHtml, fetchJson, UA } = require('../lib/http');
const cheerio = require('cheerio');

const ROGMOVIES_BASE = 'https://rogmovies.one';
const TMDB_API_KEY = '439c478a771f35c05022f9feabcca01c';

// ─── Quality / codec / size extraction from h5 text ─────────────────────
function extractMetadata(h5Text) {
  const text = (h5Text || '').replace(/<[^>]+>/g, '').trim();
  const lower = text.toLowerCase();

  // Quality: 2160p > 1080p > 720p > 480p > 4K
  let quality = 'Unknown';
  if (lower.includes('2160p') || lower.includes('4k')) quality = '2160p';
  else if (lower.includes('1080p')) quality = '1080p';
  else if (lower.includes('720p')) quality = '720p';
  else if (lower.includes('480p')) quality = '480p';

  // Codec
  const codec = [];
  if (lower.includes('hevc') || lower.includes('x265')) codec.push('HEVC');
  if (lower.includes('x264') || lower.includes('h.264') || lower.includes('h264')) codec.push('H264');
  if (lower.includes('10bit') || lower.includes('10-bit')) codec.push('10bit');

  // Audio
  let audio = '';
  const audioMatch = text.match(/(DD[PM]?5\.1|DD5\.1|AC3|EAC3|AAC5\.1)/i);
  if (audioMatch) audio = audioMatch[1];

  // Size: [300MB] or [1.1GB] or [12.7GB]
  const sizeMatch = text.match(/\[([0-9.,]+\s*[KMGT]?B)\]/i);
  const size = sizeMatch ? sizeMatch[1] : '';

  // Source: WEB-DL, BluRay, NF, AMZN, etc.
  let source = '';
  if (lower.includes('web-dl') || lower.includes('web dl')) source = 'WEB-DL';
  else if (lower.includes('bluray') || lower.includes('blu-ray')) source = 'BluRay';
  else if (lower.includes(' nf ') || lower.includes('nf ')) source = 'NF';
  else if (lower.includes('amzn')) source = 'AMZN';

  return { quality, codec, audio, size, source, rawText: text };
}

// ─── TMDB lookup ──────────────────────────────────────────────────────────
async function getTmdbDetails(tmdbId, type) {
  try {
    const url = `https://api.themoviedb.org/3/${type === 'tv' ? 'tv' : 'movie'}/${tmdbId}?api_key=${TMDB_API_KEY}&language=en-US`;
    const data = await fetchJson(url);
    if (!data) return null;
    const title = type === 'tv' ? data.name : data.title;
    const year = (type === 'tv' ? data.first_air_date : data.release_date || '').substring(0, 4);
    return { title, year };
  } catch (e) {
    return null;
  }
}

// ─── Search RogMovies via Typesense API ────────────────────────────────────
async function searchRogMovies(query) {
  try {
    // The search API uses the `q` parameter (NOT `query` — that returns all posts)
    const url = `${ROGMOVIES_BASE}/ts-search.php?q=${encodeURIComponent(query)}`;
    console.log(`[RogMovies] Search: ${url}`);
    const data = await fetchJson(url);
    if (!data || !data.hits) return [];
    return data.hits.map(h => ({
      id: h.document.id,
      imdb_id: h.document.imdb_id || '',
      permalink: h.document.permalink,
      title: h.document.post_title,
      thumbnail: h.document.post_thumbnail || '',
      categories: h.document.category || [],
      date: h.document.post_date || '',
    }));
  } catch (e) {
    console.error(`[RogMovies] Search error: ${e.message}`);
    return [];
  }
}

// ─── Match search results to TMDB title/year ──────────────────────────────
function findBestMatch(results, title, year, imdbId) {
  if (!results.length) return null;

  // 1. Try IMDB ID match (most reliable)
  if (imdbId) {
    const match = results.find(r => r.imdb_id && r.imdb_id === imdbId);
    if (match) {
      console.log(`[RogMovies] Matched by IMDB ID: ${imdbId}`);
      return match;
    }
  }

  // 2. String similarity on title + year
  const titleLower = title.toLowerCase();
  let bestScore = -1;
  let bestResult = null;

  for (const r of results) {
    const rTitle = r.title.toLowerCase();
    let score = 0;

    // Title contains all words from query
    const queryWords = titleLower.split(/\s+/).filter(w => w.length > 2);
    const matchingWords = queryWords.filter(w => rTitle.includes(w));
    score += matchingWords.length * 3;

    // Year match (high weight)
    if (year && rTitle.includes(year)) score += 20;

    // Penalize year mismatch
    const yearsInResult = r.title.match(/\b(19|20)\d{2}\b/g) || [];
    if (year && yearsInResult.length > 0 && !yearsInResult.includes(year)) {
      score -= 15;
    }

    // Penalize "collection" or "imdb top"
    if (rTitle.includes('collection') || rTitle.includes('imdb top')) {
      score -= 10;
    }

    if (score > bestScore) {
      bestScore = score;
      bestResult = r;
    }
  }

  if (bestResult && bestScore > 0) {
    console.log(`[RogMovies] Best match (score ${bestScore}): ${bestResult.title}`);
    return bestResult;
  }

  return null;
}

// ─── Parse post page for quality/size/download pairs ─────────────────────
async function parsePostPage(permalink) {
  const url = `${ROGMOVIES_BASE}${permalink}`;
  const html = await fetchHtml(url);
  if (!html) return [];

  const $ = cheerio.load(html);
  const streams = [];

  // Find all h5 tags that contain quality info (480p/720p/1080p/2160p/4K)
  $('h5').each((i, el) => {
    const h5Text = $(el).html() || '';
    const text = $(el).text().trim();

    // Must contain a quality marker
    if (!/480p|720p|1080p|2160p|4[kK]/i.test(text)) return;

    // Find the nexdrive link in the following sibling(s)
    let nexdriveLink = null;
    let nextEl = $(el).next();
    let attempts = 0;
    while (nextEl.length > 0 && attempts < 3) {
      const link = nextEl.find('a[href*="nexdrive"]').attr('href');
      if (link) {
        nexdriveLink = link;
        break;
      }
      nextEl = nextEl.next();
      attempts++;
    }

    if (!nexdriveLink) return;

    const meta = extractMetadata(h5Text);
    streams.push({
      quality: meta.quality,
      size: meta.size,
      codec: meta.codec.join(','),
      audio: meta.audio,
      source: meta.source,
      title: meta.rawText,
      nexdriveUrl: nexdriveLink,
    });
  });

  return streams;
}

// ─── Resolve nexdrive page to get mirror links ───────────────────────────
async function resolveNexdriveMirrors(nexdriveUrl) {
  try {
    const html = await fetchHtml(nexdriveUrl, {
      headers: { 'Referer': ROGMOVIES_BASE + '/' },
    });
    if (!html) return { directUrl: null, fastdlUrl: null, pageUrl: nexdriveUrl };

    const $ = cheerio.load(html);
    const mirrors = [];

    // Look for all external links (file hosts)
    $('a[href]').each((i, el) => {
      const href = $(el).attr('href') || '';
      const text = $(el).text().trim();

      if (href.includes('megaup.net') && href.includes('.mkv')) {
        mirrors.push({ type: 'direct', url: href, label: text || 'MegaUp' });
      }
      else if (href.includes('1fichier.com')) {
        mirrors.push({ type: 'filehost', url: href, label: '1Fichier' });
      }
      else if (href.includes('gofile.io')) {
        mirrors.push({ type: 'filehost', url: href, label: 'GoFile' });
      }
      else if (href.includes('filebee.xyz')) {
        mirrors.push({ type: 'filehost', url: href, label: 'FileBee' });
      }
      else if (href.includes('fastdl.beer')) {
        mirrors.push({ type: 'fastdl', url: href, label: 'FastDL' });
      }
    });

    const direct = mirrors.find(m => m.type === 'direct');
    const fastdl = mirrors.find(m => m.type === 'fastdl');

    // Try to resolve fastdl.beer embed → direct .mp4/.mkv
    let fastdlDirect = null;
    if (fastdl) {
      const fdHtml = await fetchHtml(fastdl.url, { headers: { Referer: nexdriveUrl } });
      if (fdHtml) {
        const mp4Match = fdHtml.match(/https?:\/\/[^"'<>\s]+\.(?:mp4|mkv)[^"'<>\s]*/i);
        if (mp4Match) fastdlDirect = mp4Match[0];
        // Also check for <source> or <video> tags
        const sourceMatch = fdHtml.match(/<source[^>]*src="([^"]+)"/);
        if (sourceMatch) fastdlDirect = sourceMatch[1];
        // Check for file URL in JS
        const jsFileMatch = fdHtml.match(/file\s*:\s*["']([^"']+\.(?:mp4|mkv|m3u8))["']/i);
        if (jsFileMatch) fastdlDirect = jsFileMatch[1];
      }
    }

    return {
      directUrl: direct ? direct.url : null,
      fastdlUrl: fastdl ? fastdl.url : null,
      fastdlDirect,
      pageUrl: nexdriveUrl,
      allMirrors: mirrors,
    };
  } catch (e) {
    return { directUrl: null, fastdlUrl: null, pageUrl: nexdriveUrl };
  }
}

// ─── Build Stremio stream objects ──────────────────────────────────────────
async function buildStreams(parsedStreams) {
  const stremioStreams = [];

  for (const s of parsedStreams) {
    const mirrors = await resolveNexdriveMirrors(s.nexdriveUrl);
    const parts = [s.quality];
    if (s.size) parts.push(s.size);
    if (s.codec) parts.push(s.codec);
    if (s.audio) parts.push(s.audio);
    if (s.source) parts.push(s.source);
    const streamTitle = parts.join(' · ');

    // 1. Direct .mkv from megaup.net (best — direct playable)
    if (mirrors.directUrl) {
      stremioStreams.push({
        name: `RogMovies\n${s.quality}`,
        title: streamTitle,
        url: mirrors.directUrl,
        quality: s.quality,
        behaviorHints: {
          notWebReady: false,
          headers: { 'Referer': 'https://megaup.net/', 'User-Agent': UA },
          proxyHeaders: { request: { 'Referer': 'https://megaup.net/', 'User-Agent': UA } },
        },
        meta: {
          provider: 'RogMovies', source: 'megaup.net', type: 'mp4',
          quality: s.quality, size: s.size, codec: s.codec, audio: s.audio,
          directStream: true, mediaType: 'movie',
        },
      });
    }

    // 2. Direct stream from fastdl.beer embed (if resolved to .mp4/.mkv)
    if (mirrors.fastdlDirect) {
      stremioStreams.push({
        name: `RogMovies\n${s.quality}`,
        title: streamTitle + ' (FastDL)',
        url: mirrors.fastdlDirect,
        quality: s.quality,
        behaviorHints: {
          notWebReady: false,
          headers: { 'Referer': 'https://fastdl.beer/', 'User-Agent': UA },
          proxyHeaders: { request: { 'Referer': 'https://fastdl.beer/', 'User-Agent': UA } },
        },
        meta: {
          provider: 'RogMovies', source: 'fastdl.beer', type: 'mp4',
          quality: s.quality, size: s.size, codec: s.codec, audio: s.audio,
          directStream: true, mediaType: 'movie',
        },
      });
    }

    // 3. Embed stream (nexdrive page — user clicks Download in webview)
    stremioStreams.push({
      name: `RogMovies\n${s.quality}`,
      title: streamTitle + ' (Web)',
      url: mirrors.pageUrl,
      quality: s.quality,
      behaviorHints: {
        notWebReady: true,
        proxyHeaders: { request: { 'User-Agent': UA } },
      },
      meta: {
        provider: 'RogMovies', source: 'nexdrive.beer', type: 'embed',
        quality: s.quality, size: s.size, codec: s.codec, audio: s.audio,
        directStream: false, mediaType: 'movie',
      },
    });
  }

  const qualityOrder = { '2160p': 4, '1080p': 3, '720p': 2, '480p': 1 };
  stremioStreams.sort((a, b) => (qualityOrder[b.quality] || 0) - (qualityOrder[a.quality] || 0));
  return stremioStreams;
}

// ─── Main entry: getRogMoviesStreams ───────────────────────────────────────
async function getRogMoviesStreams(tmdbId, type, season, episode) {
  try {
    const mediaType = type === 'tv' ? 'tv' : 'movie';
    console.log(`[RogMovies] getStreams: tmdb=${tmdbId} type=${mediaType} S${season || '?'}E${episode || '?'}`);

    // 1. TMDB lookup
    const tmdb = await getTmdbDetails(tmdbId, mediaType);
    if (!tmdb || !tmdb.title) {
      console.error('[RogMovies] No TMDB title found');
      return [];
    }

    // 2. Search
    const results = await searchRogMovies(tmdb.title);
    if (!results.length) {
      console.log('[RogMovies] No search results');
      return [];
    }

    // 3. Match
    const match = findBestMatch(results, tmdb.title, tmdb.year, null);
    if (!match) {
      console.log('[RogMovies] No matching result found');
      return [];
    }

    // 4. Parse post page
    const parsedStreams = await parsePostPage(match.permalink);
    if (!parsedStreams.length) {
      console.log('[RogMovies] No download links found on post page');
      return [];
    }

    // 5. Build streams (resolves nexdrive mirrors)
    const streams = await buildStreams(parsedStreams);
    console.log(`[RogMovies] Returning ${streams.length} streams (best: ${streams[0]?.quality || '?'})`);
    return streams;
  } catch (e) {
    console.error(`[RogMovies] Error: ${e.message}`);
    return [];
  }
}

module.exports = {
  getRogMoviesStreams,
  searchRogMovies,
  parsePostPage,
  resolveNexdriveMirrors,
  extractMetadata,
  ROGMOVIES_BASE,
  PROVIDER_NAME: 'RogMovies',
};
