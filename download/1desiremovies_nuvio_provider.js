// 1DesireMovies — PenguPlay-INDEPENDENT scraper for 1desiremovies.cymru.
// Returns direct playable HTTPS streams from video hosting services.
//
// ARCHITECTURE:
//   1. TMDB → title + year
//   2. Search 1desiremovies.cymru/search/<title> → find movie post URL
//   3. Extract hindifire.store/view/<code> links from post page
//   4. For each hindifire.store page: extract DIRECT video hosting links:
//      • StreamTape (streamtape.com) → change /v/ to /d/ for direct download
//      • DoodStream (doodstream.com) → direct stream URL
//      • MixDrop (mixdrop.co) → direct download URL
//      • VideoBin (videobin.co) → direct download URL
//      • PixelDrain (pixeldrain.dev) → direct API URL
//   5. Return direct playable HTTPS streams with enriched metadata
//
// No PenguPlay, no auth_token. Pure Node.js + cheerio.
// Works on Render free tier.
// ============================================================================

'use strict';

const path = require('path');
const cheerio = require('cheerio-without-node-native');

const PROVIDER_NAME = '1DesireMovies';
const BASE_URL = 'https://1desiremovies.cymru';
const TMDB_API_KEY = '1c29a5198ee1854bd5eb45dbe8d17d92';
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';
const MAX_STREAMS = 25;

// ─── HTTP helper ───────────────────────────────────────────────────────────
function fetchText(url, referer) {
  const headers = { 'User-Agent': UA, 'Accept': 'text/html,application/json,*/*' };
  if (referer) headers['Referer'] = referer;
  return fetch(url, { headers, redirect: 'follow' }).then(r => {
    if (!r.ok) throw new Error('HTTP ' + r.status + ' for ' + url);
    return r.text();
  });
}

// ─── TMDB ────────────────────────────────────────────────────────────────────
async function getTMDBInfo(tmdbId, mediaType) {
  const type = mediaType === 'tv' ? 'tv' : 'movie';
  const url = `https://api.themoviedb.org/3/${type}/${tmdbId}?api_key=${TMDB_API_KEY}`;
  try {
    const body = await fetchText(url);
    const d = JSON.parse(body);
    return {
      title: type === 'tv' ? d.name : d.title,
      year: ((d.first_air_date || d.release_date || '') + '').split('-')[0],
      type: type
    };
  } catch (e) { return null; }
}

// ─── Animation detection ──────────────────────────────────────────────────────
async function detectIsAnimation(tmdbId, mediaType) {
  const type = mediaType === 'tv' ? 'tv' : 'movie';
  try {
    const url = `https://api.themoviedb.org/3/${type}/${tmdbId}?api_key=${TMDB_API_KEY}`;
    const body = await fetchText(url);
    const d = JSON.parse(body);
    return (d.genres || []).some(g => g.id === 16);
  } catch (e) { return false; }
}

// ─── Search ────────────────────────────────────────────────────────────────
async function searchDesireMovies(title) {
  const searchUrl = `${BASE_URL}/search/${encodeURIComponent(title)}`;
  console.log('[1DesireMovies] Searching: ' + searchUrl);
  const html = await fetchText(searchUrl, BASE_URL + '/');
  const $ = cheerio.load(html);
  const results = [];
  $('main a[href], article a[href]').each((_, el) => {
    const href = $(el).attr('href') || '';
    const text = $(el).text().trim();
    if (href.includes(BASE_URL) && text.length > 10 && text.length < 300 &&
        !href.match(/\/(category|tag|page|feed|comments|wp-|about|contact|privacy|terms|dmca|author|how-to|request|join|disclaimer)/i) &&
        text.match(/(19|20)\d{2}|4k|1080p|720p|480p|bluray|web/i)) {
      if (!results.find(r => r.url === href)) results.push({ url: href, title: text });
    }
  });
  console.log('[1DesireMovies] Found ' + results.length + ' search results');
  return results;
}

function findBestMatch(results, title, year) {
  if (!results.length) return null;
  const normQuery = title.toLowerCase().replace(/[^a-z0-9\s]/g, ' ').replace(/\s+/g, ' ').trim();
  const y = parseInt(year, 10) || 0;
  let best = null, bestScore = 0;
  for (const r of results) {
    const raw = r.title || '';
    const yearMatch = raw.match(/\b(19|20)\d{2}\b/);
    const resultYear = yearMatch ? parseInt(yearMatch[0], 10) : 0;
    const bare = raw.replace(/\s*[\(\[]?(19|20)\d{2}[\)\]]?.*$/i, '').trim();
    const normBare = bare.toLowerCase().replace(/[^a-z0-9\s]/g, ' ').replace(/\s+/g, ' ').trim();
    let score = 0;
    if (normBare === normQuery) score += 100;
    else if (normBare.startsWith(normQuery) || normQuery.startsWith(normBare)) score += 70;
    else if (normBare.split(' ').slice(0, 3).join(' ') === normQuery.split(' ').slice(0, 3).join(' ')) score += 40;
    if (y && resultYear && y === resultYear) score += 30;
    if (score > bestScore) { bestScore = score; best = r; }
  }
  if (best && bestScore >= 30) {
    console.log('[1DesireMovies] Best match: ' + best.title.slice(0, 80) + ' (score=' + bestScore + ')');
    return best;
  }
  return null;
}

// ─── Extract hindifire.store links from post page ────────────────────────────
function extractHindifireLinks(html, targetSeason, targetEpisode) {
  const $ = cheerio.load(html);
  const links = [];
  $('a[href*="hindifire.store"]').each((_, el) => {
    const href = $(el).attr('href') || '';
    const text = $(el).text().trim();
    // Get quality context from parent/grandparent
    const $parent = $(el).parent();
    const $gparent = $parent.parent();
    let context = $gparent.text().trim().slice(0, 300);
    if (!context) context = $parent.text().trim().slice(0, 300);

    // For TV: check SxxExx match
    if (targetSeason && targetEpisode) {
      const sxxexx = context.match(/S(\d{2})E(\d{2})/i);
      if (sxxexx) {
        if (parseInt(sxxexx[1], 10) !== targetSeason || parseInt(sxxexx[2], 10) !== targetEpisode) return;
      }
    }
    if (!links.find(l => l.url === href)) {
      links.push({ url: href, text: text, context: context });
    }
  });
  console.log('[1DesireMovies] Found ' + links.length + ' hindifire.store links');
  return links;
}

// ─── Extract video hosting links from hindifire.store page ───────────────────
async function resolveHindifire(hindifireUrl) {
  try {
    const html = await fetchText(hindifireUrl, BASE_URL + '/');
    const $ = cheerio.load(html);
    const streams = [];

    // Look for direct video hosting links
    const videoHosts = [
      { pattern: /streamtape\.com\/v\/([^/]+)/i, build: (id) => `https://streamtape.com/d/${id}/video.mp4`, name: 'StreamTape', type: 'mp4' },
      { pattern: /doodstream\.com\/d\/([^/]+)/i, build: (id) => `https://doodstream.com/d/${id}`, name: 'DoodStream', type: 'mp4' },
      { pattern: /mixdrop\.co\/f\/([^/]+)/i, build: (id) => `https://mixdrop.co/f/${id}`, name: 'MixDrop', type: 'mp4' },
      { pattern: /videobin\.co\/([^/]+)/i, build: (id) => `https://videobin.co/${id}`, name: 'VideoBin', type: 'mp4' },
      { pattern: /pixeldrain\.(com|dev)\/u\/([^/"']+)/i, build: (id) => `https://pixeldrain.com/api/file/${id}?download`, name: 'PixelDrain', type: 'mp4' },
    ];

    $('a[href]').each((_, el) => {
      const href = $(el).attr('href') || '';
      for (const host of videoHosts) {
        const m = href.match(host.pattern);
        if (m) {
          const id = m[m.length - 1];
          const directUrl = host.build(id);
          const linkText = $(el).text().trim() || host.name;
          streams.push({ url: directUrl, host: host.name, text: linkText, type: host.type });
          break;
        }
      }
    });

    // Also look for nzarticles.xyz links (broken — skip them)
    // And hubcloud links (if any direct ones exist)
    $('a[href*="hubcloud"]').each((_, el) => {
      const href = $(el).attr('href') || '';
      streams.push({ url: href, host: 'HubCloud', text: $(el).text().trim(), type: 'mkv' });
    });

    return streams;
  } catch (e) {
    console.log('[1DesireMovies] hindifire resolve failed: ' + e.message);
    return [];
  }
}

// ─── Metadata parsers ──────────────────────────────────────────────────────
function parseQuality(text) {
  const t = String(text || '').toLowerCase();
  if (t.includes('2160p') || t.includes('4k') || t.includes('uhd')) return '2160p';
  if (t.includes('1080p')) return '1080p';
  if (t.includes('720p')) return '720p';
  if (t.includes('480p')) return '480p';
  return '1080p';
}
function parseCodec(text) {
  const t = String(text || '').toLowerCase();
  if (/10bit|10-bit|hevc|x265|h265/i.test(t)) return 'HEVC 10bit';
  if (/x264|h264/i.test(t)) return 'H264';
  return '';
}
function parseLanguage(text) {
  const t = String(text || '');
  const langs = [];
  if (/hindi/i.test(t)) langs.push('Hindi');
  if (/english/i.test(t)) langs.push('English');
  if (/tamil/i.test(t)) langs.push('Tamil');
  if (/telugu/i.test(t)) langs.push('Telugu');
  if (/chinese/i.test(t)) langs.push('Chinese');
  if (/bengali/i.test(t)) langs.push('Bengali');
  if (/gujarati/i.test(t)) langs.push('Gujarati');
  if (/korean/i.test(t)) langs.push('Korean');
  return langs.length ? langs.join('+') : '';
}
function parseSize(text) {
  const m = String(text || '').match(/([0-9.]+\s*(?:GB|MB))/i);
  return m ? m[1].replace(/\s+/g, '') : null;
}

// ─── Main entry ───────────────────────────────────────────────────────────────
async function getStreams(tmdbId, type, season, episode) {
  const isMovie = type !== 'tv' && type !== 'series';
  const targetSeason = season ? parseInt(season, 10) : null;
  const targetEpisode = episode ? parseInt(episode, 10) : null;

  console.log('[1DesireMovies] Request: tmdb=' + tmdbId + ' type=' + type +
    (isMovie ? '' : ' S' + season + 'E' + episode));

  const info = await getTMDBInfo(tmdbId, type);
  if (!info || !info.title) { console.log('[1DesireMovies] No TMDB info'); return []; }
  console.log('[1DesireMovies] TMDB: ' + info.title + ' (' + info.year + ')');

  const isAnimation = await detectIsAnimation(tmdbId, type);
  console.log('[1DesireMovies] isAnimation=' + isAnimation);

  // Search for movie
  const results = await searchDesireMovies(info.title);
  if (!results.length) return [];
  const match = findBestMatch(results, info.title, info.year);
  if (!match) { console.log('[1DesireMovies] No matching post'); return []; }

  // Fetch post page
  const postHtml = await fetchText(match.url, BASE_URL + '/');
  const hindifireLinks = extractHindifireLinks(postHtml, targetSeason, targetEpisode);
  if (!hindifireLinks.length) return [];

  // Resolve each hindifire link → extract video hosting streams
  const resolved = await Promise.all(
    hindifireLinks.slice(0, 10).map(l =>
      resolveHindifire(l.url).then(streams =>
        streams.map(s => ({ ...s, context: l.context, hindifireUrl: l.url }))
      ).catch(() => [])
    )
  );

  // Flatten + dedupe by URL
  const allStreams = [];
  const seen = new Set();
  for (const arr of resolved) {
    for (const s of arr) {
      if (!seen.has(s.url)) { seen.add(s.url); allStreams.push(s); }
    }
  }
  console.log('[1DesireMovies] Total resolved streams: ' + allStreams.length);

  // Build output
  const out = [];
  for (const s of allStreams) {
    if (out.length >= MAX_STREAMS) break;
    const quality = parseQuality(s.context);
    const codec = parseCodec(s.context);
    const lang = parseLanguage(s.context);
    const size = parseSize(s.context);

    let titleLine = info.title;
    if (!isMovie && targetSeason && targetEpisode) {
      titleLine += ' S' + String(targetSeason).padStart(2, '0') + 'E' + String(targetEpisode).padStart(2, '0');
    }
    titleLine += ' (' + info.year + ') ' + quality + (codec ? ' ' + codec : '');
    if (lang) titleLine += ' [' + lang + ']';
    if (size) titleLine += ' ' + size;
    titleLine += ' [' + s.host + ']';

    const parts = [quality.toUpperCase()];
    if (codec) parts.push(codec);
    if (lang) parts.push(lang);
    if (size) parts.push(size);
    parts.push('[' + s.host + ']');

    out.push({
      name: '[DM] ' + PROVIDER_NAME + ' ' + parts.join(' | '),
      title: titleLine,
      url: s.url,
      quality: quality,
      type: 'video/' + s.type,
      headers: { 'User-Agent': UA },
      provider: PROVIDER_NAME,
      behaviorHints: {
        bingeGroup: '1desiremovies-' + quality + '-' + s.host,
        notWebReady: false,
      },
    });
  }
  console.log('[1DesireMovies] Returning ' + out.length + ' streams');
  return out;
}

// ─── CLI ───────────────────────────────────────────────────────────────────────
if (require.main === module) {
  (async () => {
    const args = process.argv.slice(2);
    if (!args[0]) { console.log('Usage: node 1desiremovies.js <tmdbId> <movie|tv> [season] [episode]'); process.exit(0); }
    const streams = await getStreams(args[0], args[1] || 'movie', args[2], args[3]);
    console.log('\n' + streams.length + ' streams:');
    streams.forEach((s, i) => console.log('[' + i + '] ' + s.name + '\n    ' + s.url.slice(0, 200)));
  })();
}

module.exports = { getStreams };
