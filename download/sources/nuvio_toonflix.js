/**
 * ToonFlix Provider — direct playable streams via curl 4-step chain
 * ==========================================================================
 * Site: https://toonflix.in (WordPress)
 * Drive: https://drive.toonflix.in (PHP download portal)
 *
 * Resolution chain (NO proxy needed, uses curl):
 *   1. TMDB ID → search toonflix.in → parse post links
 *   2. Post page → extract drive.toonflix.in link (with fresh token)
 *   3. Drive page → parse quality cards + go.php server links (extract PHPSESSID)
 *   4. go.php → "Download Ready" page → extract action=start&token=XXX
 *   5. go.php?action=start&token=XXX → 302 redirect to file host URL
 *
 * Metadata: quality (480p/720p/1080p/1080pHQ), codec (x265), size, multi-audio
 * All streams returned are DIRECTLY PLAYABLE (302 redirect to workers.dev/GDrive)
 */
'use strict';

const { execFile } = require('child_process');
const { promisify } = require('util');
const execFileAsync = promisify(execFile);

const { fetchHtml, fetchJson, UA, decodeHtmlEntities } = require('../lib/http');
const cheerio = require('cheerio');

const TOONFLIX_BASE = 'https://toonflix.in';
const DRIVE_BASE = 'https://drive.toonflix.in';
const TMDB_API_KEY = '439c478a771f35c05022f9feabcca01c';
const FULL_HEADERS = [
  'Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8',
  'Accept-Language: en-US,en;q=0.9',
  'Sec-Ch-Ua: "Google Chrome";v="147", "Not?A_Brand";v="8", "Chromium";v="147"',
  'Sec-Ch-Ua-Mobile: ?0',
  'Sec-Ch-Ua-Platform: "Windows"',
  'Sec-Fetch-Dest: document',
  'Sec-Fetch-Mode: navigate',
  'Sec-Fetch-Site: none',
  'Sec-Fetch-User: ?1',
  'Upgrade-Insecure-Requests: 1',
];

// ─── Curl with full Chrome headers ─────────────────────────────────────────
async function curlGet(url, opts = {}) {
  const args = ['-sS', '-L', '--compressed', '--max-time', '12', '-A', UA];
  for (const h of FULL_HEADERS) args.push('-H', h);
  if (opts.referer) args.push('-H', `Referer: ${opts.referer}`);
  if (opts.cookie) args.push('-b', opts.cookie);
  if (opts.captureHeaders) args.push('-D', opts.captureHeaders);
  if (opts.method === 'HEAD') { args.unshift('-I'); args = args.filter(a => a !== '-o' && a !== '-'); }
  args.push(url);
  try {
    const { stdout } = await execFileAsync('curl', args, { encoding: 'utf-8', maxBuffer: 30 * 1024 * 1024, timeout: 15000 });
    return stdout;
  } catch (e) { return ''; }
}

async function curlHead(url, opts = {}) {
  const args = ['-sS', '-I', '--compressed', '--max-time', '10', '-A', UA];
  for (const h of FULL_HEADERS) args.push('-H', h);
  if (opts.cookie) args.push('-b', opts.cookie);
  if (opts.referer) args.push('-H', `Referer: ${opts.referer}`);
  args.push(url);
  try {
    const { stdout } = await execFileAsync('curl', args, { encoding: 'utf-8', timeout: 12000 });
    return stdout;
  } catch (e) { return ''; }
}

// ─── TMDB lookup ──────────────────────────────────────────────────────────
async function getTmdbDetails(tmdbId, type) {
  const url = `https://api.themoviedb.org/3/${type === 'tv' ? 'tv' : 'movie'}/${tmdbId}?api_key=${TMDB_API_KEY}&language=en-US`;
  const data = await fetchJson(url);
  if (!data) return null;
  return {
    title: type === 'tv' ? data.name : data.title,
    year: ((type === 'tv' ? data.first_air_date : data.release_date) || '').substring(0, 4),
    poster: data.poster_path ? `https://image.tmdb.org/t/p/w500${data.poster_path}` : null,
    genres: (data.genres || []).map(g => g.name),
    overview: data.overview || '',
  };
}

// ─── Search ToonFlix ────────────────────────────────────────────────────────
async function searchToonFlix(query) {
  const html = await curlGet(`${TOONFLIX_BASE}/?s=${encodeURIComponent(query)}`);
  if (!html) return [];
  const $ = cheerio.load(html);
  const results = [];
  $('article.entry-card').each((_, el) => {
    const anchor = $(el).find('a[href]').first();
    const url = (anchor.attr('href') || '').trim();
    const title = ($(el).find('h2, h3, .entry-title').first().text() || anchor.text() || '').trim();
    if (url && title && !/list|about|disclaimer|contact/i.test(url)) {
      results.push({ url, title: decodeHtmlEntities(title) });
    }
  });
  return results;
}

// ─── Extract drive link from post page ────────────────────────────────────
function extractDriveLink(html) {
  const match = html.match(/href="(https:\/\/drive\.toonflix\.in\/index\.php\?[^"]+)"/);
  if (!match) return null;
  return match[1].replace(/&#038;/g, '&');
}

// ─── Parse drive page for quality cards + server links ────────────────────
function parseDrivePage(html) {
  const $ = cheerio.load(html);
  const qualities = [];
  const servers = [];

  $('.quality-card').each((qi, card) => {
    const quality = $(card).find('.quality-text').text().trim();
    const codec = $(card).find('.codec-tag').text().trim();
    const size = $(card).find('.file-size').text().trim();
    qualities.push({ quality, codec, size });

    $(card).find('.server-item').each((_, server) => {
      const name = $(server).find('.server-name').text().trim();
      const onclick = $(server).find('a').attr('onclick') || '';
      const linkMatch = onclick.match(/handleLinkClick\('([^']+)'/);
      if (linkMatch) {
        servers.push({ name, goLink: linkMatch[1], qualityIndex: qi });
      }
    });
  });

  return { qualities, servers };
}

// ─── Extract start token from "Download Ready" page ───────────────────────
function extractStartToken(html) {
  const match = html.match(/action=start&token=([a-f0-9]+)/);
  return match ? match[1] : null;
}

// ─── Extract PHPSESSID from response headers ─────────────────────────────
function extractPHPSESSID(headers) {
  const match = headers.match(/PHPSESSID=([^;\s]+)/);
  return match ? match[1] : null;
}

// ─── Extract redirect URL from HEAD response ──────────────────────────────
function extractRedirectLocation(headers) {
  const match = headers.match(/^location:\s*(.+)$/im);
  return match ? match[1].trim() : null;
}

// ─── Build Stremio streams ─────────────────────────────────────────────────
function buildStreams(resolvedServers, qualities, tmdb, episodeNum) {
  const title = tmdb?.title || 'Unknown';
  const poster = tmdb?.poster || null;
  const streams = [];
  const seen = new Set();

  for (const server of resolvedServers) {
    if (!server.fileUrl || seen.has(server.fileUrl)) continue;
    seen.add(server.fileUrl);

    const q = qualities[server.qualityIndex] || { quality: '1080p', codec: 'x265', size: '' };
    const isDirect = /\.(mkv|mp4|webm)/i.test(server.fileUrl) ||
                     /workers\.dev|googleusercontent|drive\.google/i.test(server.fileUrl);
    if (!isDirect) continue;

    streams.push({
      name: `ToonFlix\n${q.quality}`,
      title: `${title} - Episode ${episodeNum || 1} · ${q.quality} · ${q.codec} · ${q.size} · ${server.name}`,
      url: server.fileUrl,
      quality: q.quality.toLowerCase().includes('4k') ? '4K' : q.quality.toLowerCase(),
      behaviorHints: {
        notWebReady: false,
        headers: { 'Referer': DRIVE_BASE + '/', 'User-Agent': UA },
        proxyHeaders: { request: { 'Referer': DRIVE_BASE + '/', 'User-Agent': UA } },
      },
      poster,
      meta: {
        provider: 'ToonFlix',
        source: server.name,
        type: 'mp4',
        quality: q.quality.toLowerCase(),
        codec: q.codec,
        size: q.size,
        title: `${title} - Episode ${episodeNum || 1}`,
        episodeNumber: episodeNum,
        description: tmdb?.overview || '',
        poster,
        genres: tmdb?.genres || [],
        directStream: true,
        mediaType: 'series',
      },
    });
  }

  const order = { '4k': 5, '2160p': 5, '1080p hq': 4.5, '1080p': 4, '720p': 3, '480p': 2 };
  streams.sort((a, b) => (order[b.quality] || 0) - (order[a.quality] || 0));
  return streams;
}

// ─── Main entry ───────────────────────────────────────────────────────────
async function getToonFlixStreams(tmdbId, type, season, episode) {
  try {
    const tmdb = await getTmdbDetails(tmdbId, type === 'tv' || type === 'series' ? 'tv' : 'movie');
    if (!tmdb || !tmdb.title) return [];

    // Step 1: Search
    const results = await searchToonFlix(tmdb.title);
    if (!results.length) return [];
    const best = results.find(r => r.title.toLowerCase().includes(tmdb.title.toLowerCase())) || results[0];

    // Step 2: Fetch post page → extract drive link (fresh token)
    const postHtml = await curlGet(best.url, { referer: TOONFLIX_BASE + '/' });
    if (!postHtml) return [];
    const driveUrl = extractDriveLink(postHtml);
    if (!driveUrl) return [];

    // Step 3: Fetch drive page → get quality cards + PHPSESSID
    const headerFile = `/tmp/tf_${Date.now()}.hdr`;
    const driveHtml = await curlGet(driveUrl, {
      referer: TOONFLIX_BASE + '/',
      cookie: 'tf_sys_pref=12h; toon_short_count=3',
      captureHeaders: headerFile,
    });
    if (!driveHtml) return [];
    const headers = require('fs').readFileSync(headerFile, 'utf-8');
    const phpsessid = extractPHPSESSID(headers);
    const { qualities, servers } = parseDrivePage(driveHtml);
    if (servers.length === 0) return [];

    // Step 4: Follow go.php for each server → get "Download Ready" page → extract start token
    // Step 5: Follow action=start → get 302 redirect to file host URL
    const resolved = await Promise.all(servers.slice(0, 8).map(async (server) => {
      // go.php first call
      const goHtml = await curlGet(`${DRIVE_BASE}/${server.goLink}&sys=12h`, {
        referer: DRIVE_BASE + '/',
        cookie: `PHPSESSID=${phpsessid}; tf_sys_pref=12h; toon_short_count=3`,
      });
      if (!goHtml || goHtml.includes('Access Denied')) return null;

      const startToken = extractStartToken(goHtml);
      if (!startToken) return null;

      // go.php?action=start → HEAD to get 302 Location
      const headResp = await curlHead(`${DRIVE_BASE}/go.php?action=start&token=${startToken}`, {
        referer: DRIVE_BASE + '/',
        cookie: `PHPSESSID=${phpsessid}; tf_sys_pref=12h; toon_short_count=3`,
      });
      const fileUrl = extractRedirectLocation(headResp);
      if (!fileUrl || fileUrl.includes(DRIVE_BASE)) return null;

      return { ...server, fileUrl };
    }));

    const valid = resolved.filter(Boolean);
    const epNum = episode ? parseInt(episode) : 1;
    const streams = buildStreams(valid, qualities, tmdb, epNum);
    console.log(`[ToonFlix] Returning ${streams.length} direct streams for ${tmdb.title} ep${epNum}`);
    return streams;
  } catch (e) {
    console.error(`[ToonFlix] Error: ${e.message}`);
    return [];
  }
}

module.exports = {
  getToonFlixStreams,
  searchToonFlix,
  extractDriveLink,
  parseDrivePage,
  extractStartToken,
  extractPHPSESSID,
  extractRedirectLocation,
  curlGet,
  curlHead,
  TOONFLIX_BASE,
  PROVIDER_NAME: 'ToonFlix',
};
