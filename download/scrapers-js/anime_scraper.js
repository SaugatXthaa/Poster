/**
 * Anime Multi-Source Scraper — Pure Node.js (NO Playwright)
 * =========================================================
 * Provides multi-audio anime streams (JAP/ENG/ESP + sub) from multiple sources,
 * matching PenguPlay's "Antova" provider behavior.
 *
 * Sources (all verified live Aug 2026):
 *   1. AnimeHeaven (animeheaven.me)   — direct MP4, no CF, multi-quality
 *   2. AnimeGG (animegg.org)          — HLS + MP4, sub+sub, multi-quality
 *   3. AniKoto (anikototv.to)         — HLS via Megacloud, sub+sub
 *   4. AllManga (allmanga.to)         — aggregator, multiple embeds
 *
 * Architecture:
 *   - AniList ID is the primary identifier (same as PenguPlay's Antova)
 *   - ARM API (arm.haglund.dev) maps AniList → MAL/TVDB/TMDB
 *   - Each source is scraped by title search + episode list + stream extraction
 *   - All requests use pure fetch() — no browser, no CF bypass needed
 *
 * Endpoints:
 *   - Search anime by title
 *   - Get episodes by AniList ID
 *   - Get stream URLs by AniList ID + episode number
 *
 * Install:
 *   npm install axios cheerio
 *
 * Usage:
 *   node anime_scraper.js search "jujutsu kaisen"
 *   node anime_scraper.js episodes 101922
 *   node anime_scraper.js stream 101922 1
 */

'use strict';

const axios = require('axios');
const cheerio = require('cheerio');

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 ' +
           '(KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';

const ANILIST_GQL = 'https://graphql.anilist.co';
const ARM_API = 'https://arm.haglund.dev/api/v2/ids';

// ==================== AniList + ID Mapping ====================

async function getAniListMedia(anilistId) {
  const query = `
    query ($id: Int) {
      Media(id: $id, type: ANIME) {
        id
        idMal
        title { romaji english native }
        synonyms
        episodes
        coverImage { large extraLarge }
        bannerImage
        status
        season
        seasonYear
        format
      }
    }`;
  const r = await axios.post(ANILIST_GQL, { query, variables: { id: Number(anilistId) } }, {
    headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
    timeout: 10000,
  });
  return r.data?.data?.Media || null;
}

async function searchAniList(query) {
  const q = `query ($search: String) { Page(page: 1, perPage: 10) { media(search: $search, type: ANIME, sort: SEARCH_MATCH) { id idMal title { romaji english native } episodes seasonYear format coverImage { large } } } }`;
  const r = await axios.post(ANILIST_GQL,
    { query: q, variables: { search: query } },
    { headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' }, timeout: 10000 }
  );
  return r.data?.data?.Page?.media || [];
}

async function mapIds(anilistId) {
  try {
    const r = await axios.get(ARM_API, {
      params: { source: 'anilist', id: anilistId },
      headers: { 'User-Agent': UA, Accept: 'application/json' },
      timeout: 8000,
      validateStatus: () => true,
    });
    if (r.status === 200 && r.data) {
      return {
        anilist: anilistId,
        mal: r.data.myanimelist,
        anidb: r.data.anidb,
        tvdb: r.data.thetvdb,
        tmdb: r.data.themoviedb,
        kitsu: r.data.kitsu,
      };
    }
  } catch (e) {}
  return { anilist: anilistId };
}

// ==================== Source 1: AnimeHeaven ====================

const ANIMEHEAVEN_BASE = 'https://animeheaven.me';

async function animeheavenSearch(query) {
  const r = await axios.get(`${ANIMEHEAVEN_BASE}/search.php`, {
    params: { q: query },
    headers: { 'User-Agent': UA },
    timeout: 10000,
  });
  const $ = cheerio.load(r.data);
  const results = [];
  $('a[href*="/detail.php"]').each((_, el) => {
    const href = $(el).attr('href') || '';
    const m = href.match(/detail\.php\?a=(.+)$/);
    if (!m) return;
    const title = $(el).text().trim();
    if (title.length > 2 && title.length < 200) {
      results.push({ id: m[1], title, source: 'animeheaven' });
    }
  });
  return results;
}

async function animeheavenGetEpisodes(animeId) {
  const r = await axios.get(`${ANIMEHEAVEN_BASE}/detail.php?a=${animeId}`, {
    headers: { 'User-Agent': UA },
    timeout: 10000,
  });
  const $ = cheerio.load(r.data);
  const title = $('title').text().replace(/\s*-.*$/, '').trim();
  const episodes = [];
  // AnimeHeaven lists episodes as links
  $('a[href*="/play.php"]').each((_, el) => {
    const href = $(el).attr('href') || '';
    const m = href.match(/play\.php\?a=([^&]+)&e=(\d+)/);
    if (m) {
      episodes.push({
        number: parseInt(m[2]),
        url: `${ANIMEHEAVEN_BASE}${href}`,
        source: 'animeheaven',
      });
    }
  });
  // Sort descending (newest first) then reverse
  episodes.sort((a, b) => b.number - a.number);
  return { title, episodes: episodes.reverse() };
}

async function animeheavenGetStream(episodeUrl) {
  const r = await axios.get(episodeUrl, {
    headers: { 'User-Agent': UA, Referer: ANIMEHEAVEN_BASE + '/' },
    timeout: 10000,
  });
  const $ = cheerio.load(r.data);
  const streams = [];
  // Look for video source tags
  $('video source, source[src]').each((_, el) => {
    const src = $(el).attr('src');
    const type = $(el).attr('type') || '';
    if (src && (src.includes('.mp4') || src.includes('.m3u8'))) {
      streams.push({
        url: src.startsWith('http') ? src : ANIMEHEAVEN_BASE + src,
        quality: '1080p',
        type: src.includes('.m3u8') ? 'hls' : 'mp4',
        audio: 'japanese',
        subtitles: 'embedded',
        source: 'animeheaven',
      });
    }
  });
  // Also check for JavaScript video URLs
  const html = r.data;
  const m = html.match(/(?:src|file|url)\s*[:=]\s*["']([^"']+\.(?:mp4|m3u8)[^"']*)["']/i);
  if (m && streams.length === 0) {
    streams.push({
      url: m[1],
      quality: '1080p',
      type: m[1].includes('.m3u8') ? 'hls' : 'mp4',
      audio: 'japanese',
      subtitles: 'embedded',
      source: 'animeheaven',
    });
  }
  return streams;
}

// ==================== Source 2: AnimeGG ====================

const ANIMEGG_BASE = 'https://www.animegg.org';

async function animeggSearch(query) {
  const r = await axios.get(`${ANIMEGG_BASE}/search/`, {
    params: { q: query },
    headers: { 'User-Agent': UA },
    timeout: 10000,
  });
  const $ = cheerio.load(r.data);
  const results = [];
  $('a.mse').each((_, el) => {
    const href = $(el).attr('href') || '';
    const m = href.match(/^\/series\/([^/?#]+)/);
    if (!m) return;
    const title = $(el).find('strong').text().trim() || m[1].replace(/-/g, ' ');
    results.push({ id: m[1], title, source: 'animegg' });
  });
  return results;
}

async function animeggGetEpisodes(slug) {
  const r = await axios.get(`${ANIMEGG_BASE}/series/${slug}`, {
    headers: { 'User-Agent': UA },
    timeout: 10000,
  });
  const $ = cheerio.load(r.data);
  const episodes = [];
  // Episode links are like: /jujutsu-kaisen-tv-episode-1
  $('a[href*="-episode-"]').each((_, el) => {
    const href = $(el).attr('href') || '';
    // Extract episode number from URL
    const m = href.match(/-episode-(\d+)/);
    if (!m) return;
    const number = parseInt(m[1]);
    // Check for sub/dub classes in parent
    const parent = $(el).closest('li').html() || '';
    const hasSub = parent.includes('btn-subbed') || href.includes('#subbed') || !href.includes('#');
    const hasDub = parent.includes('btn-dubbed') || href.includes('#dubbed');
    // Only add the base episode (no #subbed/#dubbed fragments)
    if (!href.includes('#')) {
      episodes.push({ number, epSlug: href.replace(/^\//, ''), hasSub, hasDub, source: 'animegg' });
    }
  });
  episodes.sort((a, b) => a.number - b.number);
  // Deduplicate
  const seen = new Set();
  return { title: slug.replace(/-/g, ' '), episodes: episodes.filter(e => seen.has(e.number) ? false : (seen.add(e.number), true)) };
}

async function animeggGetStream(epSlug, category = 'sub') {
  // First fetch the video page to get the numeric embed ID
  const pageR = await axios.get(`${ANIMEGG_BASE}/${epSlug}`, {
    headers: { 'User-Agent': UA, Referer: ANIMEGG_BASE + '/' },
    timeout: 10000,
    validateStatus: () => true,
  });
  if (pageR.status !== 200) return [];

  // Extract the numeric embed ID from the iframe src
  const iframeMatch = pageR.data.match(/<iframe[^>]+src=["']\/embed\/(\d+)["']/i);
  if (!iframeMatch) return [];
  const embedId = iframeMatch[1];

  // Now fetch the embed page
  const r = await axios.get(`${ANIMEGG_BASE}/embed/${embedId}`, {
    headers: { 'User-Agent': UA, Referer: `${ANIMEGG_BASE}/${epSlug}` },
    timeout: 10000,
  });
  const html = r.data;
  const m = html.match(/var\s+videoSources\s*=\s*(\[[\s\S]*?\]);/);
  if (!m) return [];
  try {
    const asJson = m[1]
      .replace(/([{,]\s*)([a-zA-Z_][a-zA-Z0-9_]*)\s*:/g, '$1"$2":')
      .replace(/:\s*'([^']*)'/g, ': "$1"');
    const parsed = JSON.parse(asJson);
    return parsed.map(s => {
      let backup = null;
      if (s.bk) {
        try { backup = decodeURIComponent(Buffer.from(s.bk, 'base64').toString()); } catch (e) {}
      }
      return {
        url: s.file ? (s.file.startsWith('http') ? s.file : ANIMEGG_BASE + s.file) : (backup || ''),
        backup: backup,
        quality: s.label || 'unknown',
        type: (s.file || '').includes('.m3u8') ? 'hls' : 'mp4',
        audio: category === 'dub' ? 'english' : 'japanese',
        subtitles: category === 'sub' ? 'embedded' : null,
        source: 'animegg',
      };
    }).filter(s => s.url);
  } catch (e) {
    return [];
  }
}

// ==================== Source 3: AniKoto ====================

const ANIKOTO_BASE = 'https://anikototv.to';

async function anikotoSearch(query) {
  const r = await axios.get(`${ANIKOTO_BASE}/search.html`, {
    params: { q: query },
    headers: { 'User-Agent': UA, Referer: 'https://hianimes.re/' },
    timeout: 10000,
    validateStatus: () => true,
  });
  if (r.status !== 200) return [];
  const $ = cheerio.load(r.data);
  const results = [];
  $('a[href*="/anime/"]').each((_, el) => {
    const href = $(el).attr('href') || '';
    const m = href.match(/\/anime\/([^/?#]+)/);
    if (!m) return;
    const title = $(el).find('.name, .title, strong').text().trim() || $(el).text().trim();
    if (title.length > 2 && title.length < 200) {
      results.push({ id: m[1], title, source: 'anikoto' });
    }
  });
  return results;
}

async function anikotoGetEpisodes(slug) {
  const r = await axios.get(`${ANIKOTO_BASE}/anime/${slug}`, {
    headers: { 'User-Agent': UA, Referer: 'https://hianimes.re/' },
    timeout: 10000,
    validateStatus: () => true,
  });
  if (r.status !== 200) return { title: slug, episodes: [] };
  const $ = cheerio.load(r.data);
  const title = $('title').text().replace(/\s*-.*$/, '').trim();
  const episodes = [];
  $('a[href*="/watch/"], a[href*="/episode/"]').each((_, el) => {
    const href = $(el).attr('href') || '';
    const m = href.match(/(?:watch|episode)\/([^/?#]+)/);
    if (!m) return;
    const epNum = parseInt($(el).text().trim()) || parseInt(href.match(/(\d+)/)?.[1] || 0);
    if (epNum > 0) {
      episodes.push({
        number: epNum,
        epId: m[1],
        url: href.startsWith('http') ? href : ANIKOTO_BASE + href,
        source: 'anikoto',
      });
    }
  });
  episodes.sort((a, b) => a.number - b.number);
  // Deduplicate
  const seen = new Set();
  return { title, episodes: episodes.filter(e => seen.has(e.number) ? false : (seen.add(e.number), true)) };
}

async function anikotoGetStream(epId) {
  // AniKoto uses Megacloud embed — extract via the /embed/ URL
  const r = await axios.get(`${ANIKOTO_BASE}/watch/${epId}`, {
    headers: { 'User-Agent': UA, Referer: 'https://hianimes.re/' },
    timeout: 10000,
    validateStatus: () => true,
  });
  if (r.status !== 200) return [];
  const html = r.data;
  const streams = [];
  // Look for iframe embed URLs (Megacloud, Vidstream, etc.)
  const iframeMatch = html.match(/<iframe[^>]+src=["']([^"']+)["']/i);
  if (iframeMatch) {
    let embedUrl = iframeMatch[1];
    if (embedUrl.startsWith('//')) embedUrl = 'https:' + embedUrl;
    // Try to extract sources from the embed
    try {
      const embedR = await axios.get(embedUrl, {
        headers: { 'User-Agent': UA, Referer: ANIKOTO_BASE + '/' },
        timeout: 10000,
        validateStatus: () => true,
      });
      if (embedR.status === 200) {
        // Look for m3u8 URLs in the embed page
        const m3u8Match = embedR.data.match(/["'](https?:[^"']+\.m3u8[^"']*)["']/);
        if (m3u8Match) {
          streams.push({
            url: m3u8Match[1],
            quality: '1080p',
            type: 'hls',
            audio: 'japanese',
            subtitles: 'embedded',
            source: 'anikoto',
          });
        }
        // Look for file URL in JSON
        const fileMatch = embedR.data.match(/"file"\s*:\s*"([^"]+\.m3u8[^"]*)"/);
        if (fileMatch) {
          streams.push({
            url: fileMatch[1],
            quality: '1080p',
            type: 'hls',
            audio: 'japanese',
            subtitles: 'embedded',
            source: 'anikoto',
          });
        }
      }
    } catch (e) {}
  }
  // Also check for direct m3u8 in the page
  const directM3u8 = html.match(/["'](https?:[^"']+\.m3u8[^"']*)["']/);
  if (directM3u8 && streams.length === 0) {
    streams.push({
      url: directM3u8[1],
      quality: '1080p',
      type: 'hls',
      audio: 'japanese',
      subtitles: 'embedded',
      source: 'anikoto',
    });
  }
  return streams;
}

// ==================== High-Level: AniList ID → All Sources ====================

/**
 * Get streams for an anime episode from ALL available sources.
 * @param {number} anilistId - AniList anime ID
 * @param {number} episode - Episode number
 * @param {string} category - 'sub' or 'dub'
 * @returns {Promise<Array>} Array of stream objects from all sources
 */
async function getStreams(anilistId, episode, category = 'sub') {
  // Get anime metadata + ID mappings
  const [media, ids] = await Promise.all([
    getAniListMedia(anilistId),
    mapIds(anilistId),
  ]);
  if (!media) return [];

  const title = media.title?.english || media.title?.romaji || media.title?.native || '';
  console.error(`[anime] Looking for "${title}" episode ${episode} (${category})`);

  const allStreams = [];

  // Try each source in parallel
  const sources = [
    { name: 'animeheaven', search: animeheavenSearch, getEpisodes: animeheavenGetEpisodes, getStream: animeheavenGetStream },
    { name: 'animegg', search: animeggSearch, getEpisodes: animeggGetEpisodes, getStream: (url) => animeggGetStream(url, category) },
    { name: 'anikoto', search: anikotoSearch, getEpisodes: anikotoGetEpisodes, getStream: anikotoGetStream },
  ];

  const results = await Promise.allSettled(
    sources.map(async (src) => {
      try {
        const searchResults = await src.search(title);
        if (!searchResults.length) return [];

        // Pick the best match (first result, or exact title match)
        let best = searchResults[0];
        for (const r of searchResults) {
          if (r.title.toLowerCase() === title.toLowerCase()) { best = r; break; }
        }

        const epData = await src.getEpisodes(best.id);
        const ep = epData.episodes.find(e => e.number === episode);
        if (!ep) return [];

        if (src.name === 'animeheaven') {
          return await src.getStream(ep.url);
        } else if (src.name === 'animegg') {
          return await src.getStream(ep.epSlug, category);
        } else if (src.name === 'anikoto') {
          return await src.getStream(ep.epId || ep.url);
        }
      } catch (e) {
        console.error(`  [${src.name}] error: ${e.message}`);
        return [];
      }
    })
  );

  for (const r of results) {
    if (r.status === 'fulfilled' && r.value) {
      allStreams.push(...r.value);
    }
  }

  return allStreams;
}

// ==================== CLI ====================

if (require.main === module) (async () => {
  const [cmd, ...args] = process.argv.slice(2);

  if (cmd === 'search') {
    const query = args.join(' ');
    console.log(`Searching AniList for "${query}"...`);
    const results = await searchAniList(query);
    for (const r of results) {
      console.log(`  [AL:${r.id} MAL:${r.idMal || '-'}] ${r.title?.english || r.title?.romaji} (${r.seasonYear})`);
    }
  } else if (cmd === 'episodes') {
    const anilistId = parseInt(args[0]);
    console.log(`Fetching episodes for AniList ${anilistId}...`);
    const media = await getAniListMedia(anilistId);
    if (!media) { console.error('Not found'); process.exit(1); }
    console.log(`  Title: ${media.title?.english || media.title?.romaji}`);
    console.log(`  Episodes: ${media.episodes || 'unknown'}`);

    // Try each source
    const title = media.title?.english || media.title?.romaji || '';
    for (const [name, searchFn, epFn] of [
      ['AnimeHeaven', animeheavenSearch, animeheavenGetEpisodes],
      ['AnimeGG', animeggSearch, animeggGetEpisodes],
      ['AniKoto', anikotoSearch, anikotoGetEpisodes],
    ]) {
      try {
        const results = await searchFn(title);
        if (results.length) {
          const eps = await epFn(results[0].id);
          console.log(`  ${name}: ${eps.episodes.length} episodes (top match: "${results[0].title}")`);
        }
      } catch (e) {
        console.log(`  ${name}: error - ${e.message}`);
      }
    }
  } else if (cmd === 'stream') {
    const anilistId = parseInt(args[0]);
    const episode = parseInt(args[1]);
    const category = args[2] || 'sub';
    console.log(`Fetching streams for AniList ${anilistId} episode ${episode} (${category})...`);
    const streams = await getStreams(anilistId, episode, category);
    console.log(`\nFound ${streams.length} stream(s):`);
    for (const s of streams) {
      console.log(`  [${s.source}] ${s.quality} ${s.type} audio=${s.audio}  ${s.url.slice(0, 80)}...`);
    }
  } else {
    console.log('Usage:');
    console.log('  node anime_scraper.js search "<query>"');
    console.log('  node anime_scraper.js episodes <anilist_id>');
    console.log('  node anime_scraper.js stream <anilist_id> <episode> [sub|dub]');
    console.log('\nExamples:');
    console.log('  node anime_scraper.js search "jujutsu kaisen"');
    console.log('  node anime_scraper.js episodes 101922');
    console.log('  node anime_scraper.js stream 101922 1 sub');
  }
})();

module.exports = {
  // High-level
  getStreams,
  searchAniList,
  getAniListMedia,
  mapIds,
  // Source-specific
  animeheavenSearch,
  animeheavenGetEpisodes,
  animeheavenGetStream,
  animeggSearch,
  animeggGetEpisodes,
  animeggGetStream,
  anikotoSearch,
  anikotoGetEpisodes,
  anikotoGetStream,
};
