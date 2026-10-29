/**
 * ToonWorld4All Provider — direct playable streams via AES-CTR token decryption
 * ==========================================================================
 * Site: https://toonworld4all.me (WordPress Herald theme)
 * Archive: https://archive.toonworld4all.me (React SPA with __PROPS__)
 *
 * Resolution chain (NO proxy, NO residential IP needed):
 *   1. TMDB ID → search toonworld4all.me → parse Herald articles
 *   2. Post page → find archive.toonworld4all.me/episode/{slug} links
 *   3. Archive page → extract __PROPS__ JSON from HTML
 *      props.data.data.encodes[] = [{readable:{codec,size}, files:[{link:"/redirect/{token}"}]}]
 *   4. Decrypt redirect token with AES-CTR (hardcoded key, local computation)
 *      → JSON {url: "https://hubcloud.ist/drive/{hidden}"} or pixeldrain URL
 *   5. Resolve HubCloud → workers.dev direct .mkv URL
 *   6. Convert pixeldrain URLs → https://pixeldrain.com/api/file/{id}?download
 *
 * All streams returned are DIRECTLY PLAYABLE (notWebReady: false)
 */
'use strict';

const CryptoJS = require('crypto-js');
const cheerio = require('cheerio');
const { fetchHtml, fetchJson, UA, decodeHtmlEntities } = require('../lib/http');

const TW4A_BASE = 'https://toonworld4all.me';
const ARCV_BASE = 'https://archive.toonworld4all.me';
const TMDB_API_KEY = '439c478a771f35c05022f9feabcca01c';
const REDIRECT_KEY = '556151c9cefdee6ea51aec956e6b8682312bb609c0167d4adc71f365a928920f';

// ─── AES-CTR decryption for /redirect/{token} ─────────────────────────────
// The token is hex-encoded: first 16 bytes = counter/nonce, rest = ciphertext
// Decrypted payload is JSON {url: "https://hubcloud.ist/drive/{hidden}"}
function decryptRedirectToken(tokenHex) {
  try {
    if (!tokenHex || tokenHex.length < 34) return null;
    const rawBytes = CryptoJS.enc.Hex.parse(tokenHex);
    const allBytes = rawBytes.toString(CryptoJS.enc.Latin1);
    const bytes = [];
    for (let i = 0; i < allBytes.length; i++) bytes.push(allBytes.charCodeAt(i) & 0xff);

    const counter = bytes.slice(0, 16);
    const cipherBytes = bytes.slice(16);
    const blockCount = Math.ceil(cipherBytes.length / 16);
    const key = CryptoJS.enc.Hex.parse(REDIRECT_KEY);

    // Generate keystream using AES-ECB on counter blocks
    const counterBytes = [];
    for (let block = 0; block < blockCount; block++) {
      for (let i = 0; i < 16; i++) counterBytes.push(counter[i]);
      for (let i = 15; i >= 0; i--) {
        counter[i] = (counter[i] + 1) & 0xff;
        if (counter[i] !== 0) break;
      }
    }
    const counterWA = CryptoJS.lib.WordArray.create(
      counterBytes.reduce((w, b, i) => {
        const wi = Math.floor(i / 4);
        w[wi] = (w[wi] || 0) | (b << (24 - (i % 4) * 8));
        return w;
      }, new Array(Math.ceil(counterBytes.length / 4)).fill(0)),
      counterBytes.length
    );
    const keystream = CryptoJS.AES.encrypt(counterWA, key, {
      mode: CryptoJS.mode.ECB, padding: CryptoJS.pad.NoPadding
    }).ciphertext;

    // XOR ciphertext with keystream
    const ksBytes = [];
    for (let i = 0; i < keystream.sigBytes; i++) {
      ksBytes.push((keystream.words[i >>> 2] >>> (24 - (i % 4) * 8)) & 0xff);
    }
    const plainBytes = new Array(cipherBytes.length);
    for (let i = 0; i < cipherBytes.length; i++) {
      plainBytes[i] = cipherBytes[i] ^ ksBytes[i];
    }
    // Decode to UTF-8 string
    let encoded = '';
    for (let i = 0; i < plainBytes.length; i++) {
      encoded += '%' + (plainBytes[i] < 16 ? '0' : '') + plainBytes[i].toString(16);
    }
    const payload = JSON.parse(decodeURIComponent(encoded));
    return (payload && payload.url) || null;
  } catch (e) {
    return null;
  }
}

// ─── Extract __PROPS__ JSON from archive page HTML ────────────────────────
function extractEmbeddedProps(html) {
  const markerIndex = html.indexOf('__PROPS__');
  if (markerIndex === -1) return null;
  const jsonStart = html.indexOf('{', markerIndex);
  if (jsonStart === -1) return null;
  const jsonEnd = html.indexOf('</script>', jsonStart);
  if (jsonEnd === -1) return null;
  try {
    return JSON.parse(html.slice(jsonStart, jsonEnd).trim().replace(/;\s*$/, ''));
  } catch (e) {
    return null;
  }
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

// ─── Search ToonWorld4All ──────────────────────────────────────────────────
async function searchTw4a(query) {
  const url = `${TW4A_BASE}/?s=${encodeURIComponent(query)}`;
  const html = await fetchHtml(url);
  if (!html) return [];
  const results = [];
  const articleRe = /<article[^>]*class="[^"]*herald-lay-[a-z][^"]*"[^>]*>[\s\S]*?<a[^>]*href="([^"]+)"[^>]*title="([^"]*)"/g;
  let m;
  while ((m = articleRe.exec(html)) !== null) {
    if (!m[1].includes('shows-list') && !m[1].includes('movies-list') &&
        !m[1].includes('category') && !m[1].includes('tag') && !m[1].includes('page')) {
      results.push({ url: decodeHtmlEntities(m[1]), title: decodeHtmlEntities(m[2]).trim() });
    }
  }
  return results;
}

// ─── Resolve HubCloud page to direct download URL ─────────────────────────
async function resolveHubCloud(pageUrl) {
  try {
    const html = await fetchHtml(pageUrl);
    if (!html) return [];
    const $ = cheerio.load(html);
    const streams = [];

    let fileName = ($('div.card-header').first().text() || '').trim();
    const sizeMatch = ($('i#size').first().text() || '').match(/(\d+(?:\.\d+)?\s*(?:GB|MB))/i);
    const fileSize = sizeMatch ? sizeMatch[1] : '';

    // Find generator URL (hubcloud.php)
    let generatorUrl = $('a#download').attr('href') ||
                       $('a.btn-primary[href*="hubcloud.php"]').attr('href') || '';
    if (!generatorUrl) {
      const fallback = html.match(/https?:\/\/[^"'\s>]+hubcloud\.php\?[^"'\s>]+/i);
      if (fallback) generatorUrl = fallback[0];
    }
    if (!generatorUrl) return streams;

    // Fetch generator page
    const genHtml = await fetchHtml(generatorUrl, { headers: { Referer: pageUrl } });
    if (!genHtml) return streams;
    const $gen = cheerio.load(genHtml);
    if (!fileName) fileName = ($gen('div.card-header').first().text() || '').trim();

    // Extract direct download links from generator page
    $gen('a[href]').each((_, anchor) => {
      const text = $gen(anchor).text().trim();
      const link = ($gen(anchor).attr('href') || '').trim();
      if (!link.startsWith('http')) return;

      if (/FSLv2/i.test(text)) {
        streams.push({
          url: link, server: 'FSLv2', fileName, fileSize,
          quality: detectQuality(fileName, text),
        });
      } else if (/FSL\s*Server/i.test(text)) {
        streams.push({
          url: link, server: 'FSL Server', fileName, fileSize,
          quality: detectQuality(fileName, text),
        });
      } else if (/pixeldrain|pixelserver/i.test(text) || link.includes('pixeldrain.')) {
        const pdMatch = link.match(/\/u\/([A-Za-z0-9_-]+)/);
        if (pdMatch) {
          streams.push({
            url: `https://pixeldrain.com/api/file/${pdMatch[1]}?download`,
            server: 'Pixeldrain', fileName, fileSize,
            quality: detectQuality(fileName, text),
          });
        }
      } else if (/workers\.dev/i.test(link) || /\.mkv$/i.test(link) || /\.mp4$/i.test(link)) {
        streams.push({
          url: link, server: 'Direct', fileName, fileSize,
          quality: detectQuality(fileName, text),
        });
      }
    });
    return streams;
  } catch (e) {
    return [];
  }
}

function detectQuality(fileName, fallback) {
  const m = (String(fileName || '') + ' ' + String(fallback || '')).match(/\b(2160p|4K|UHD|1080p|720p|480p)\b/i);
  if (!m) return '1080p';
  const q = m[1].toUpperCase();
  if (q === '2160P' || q === '4K' || q === 'UHD') return '4K';
  return q === '1080P' ? '1080p' : q.toLowerCase();
}

// ─── Resolve redirect token → file host URL → direct stream ──────────────
async function resolveRedirectToStream(redirectPath, label, size) {
  const streams = [];

  // Step 1: Try AES-CTR decryption (no network needed!)
  const tokenMatch = String(redirectPath).match(/\/redirect\/([0-9a-fA-F]{32,})/);
  if (tokenMatch) {
    const decryptedUrl = decryptRedirectToken(tokenMatch[1]);
    if (decryptedUrl && decryptedUrl.startsWith('http')) {
      // Step 2: Resolve based on the file host
      if (decryptedUrl.includes('hubcloud.')) {
        const hubStreams = await resolveHubCloud(decryptedUrl);
        for (const s of hubStreams) {
          streams.push({
            url: s.url, server: s.server, fileName: s.fileName || '',
            fileSize: s.fileSize || size || '',
            quality: s.quality,
          });
        }
      } else if (decryptedUrl.includes('pixeldrain.')) {
        const pdMatch = decryptedUrl.match(/\/u\/([A-Za-z0-9_-]+)/);
        if (pdMatch) {
          streams.push({
            url: `https://pixeldrain.com/api/file/${pdMatch[1]}?download`,
            server: 'Pixeldrain', fileName: '', fileSize: size || '',
            quality: detectQuality(label, ''),
          });
        }
      } else if (/workers\.dev|\.mkv$|\.mp4$/i.test(decryptedUrl)) {
        streams.push({
          url: decryptedUrl, server: 'Direct', fileName: '', fileSize: size || '',
          quality: detectQuality(label, ''),
        });
      }
      return streams;
    }
  }

  // Step 3: Fallback — fetch the redirect page and extract JSON
  const absoluteUrl = redirectPath.startsWith('http')
    ? redirectPath
    : ARCV_BASE + (redirectPath.startsWith('/') ? '' : '/') + redirectPath;
  const redirHtml = await fetchHtml(absoluteUrl, { headers: { Referer: ARCV_BASE + '/' } });
  if (redirHtml) {
    const props = extractEmbeddedProps(redirHtml);
    if (props && props.link) {
      const fileUrl = props.link.domain + props.link.hidden;
      if (fileUrl.includes('hubcloud.')) {
        const hubStreams = await resolveHubCloud(fileUrl);
        return hubStreams.map(s => ({
          url: s.url, server: s.server, fileName: s.fileName || '',
          fileSize: s.fileSize || size || '', quality: s.quality,
        }));
      }
    }
    // Try obfuscated URL
    const obfMatch = redirHtml.match(/<code>(https?:\/\/[^<]+)<span[^>]*>([^<]+)<\/span><\/code>/i);
    if (obfMatch) {
      streams.push({
        url: obfMatch[1] + obfMatch[2], server: 'Direct', fileName: '',
        fileSize: size || '', quality: detectQuality(label, ''),
      });
    }
  }
  return streams;
}

// ─── Extract streams from archive page ────────────────────────────────────
async function extractArchiveStreams(archiveUrl, postTitle) {
  const html = await fetchHtml(archiveUrl, { headers: { Referer: TW4A_BASE + '/' } });
  if (!html) return [];

  const allStreams = [];
  const props = extractEmbeddedProps(html);

  if (props && props.data && props.data.data && props.data.data.encodes) {
    // Structured __PROPS__ data — preferred path
    const encodes = props.data.data.encodes;
    const resolutions = await Promise.all(encodes.map(async (encode) => {
      const codec = (encode.readable && encode.readable.codec) || encode.resolution || '';
      const size = (encode.readable && encode.readable.size) || '';
      const label = [codec, postTitle].filter(Boolean).join(' ');
      const fileResolutions = (encode.files || []).map(file =>
        file && file.link ? resolveRedirectToStream(file.link, label, size) : []
      );
      return (await Promise.all(fileResolutions)).flat();
    }));
    allStreams.push(...resolutions.flat());
  }

  if (allStreams.length === 0) {
    // Fallback: parse redirect links from HTML directly
    const $ = cheerio.load(html);
    const redirectLinks = new Set();
    $("a[href*='/redirect/']").each((_, anchor) => {
      const href = ($(anchor).attr('href') || '').trim();
      if (href) redirectLinks.add(href);
    });
    const resolutions = await Promise.all(
      Array.from(redirectLinks).map(href => resolveRedirectToStream(href, postTitle, ''))
    );
    allStreams.push(...resolutions.flat());
  }

  return allStreams;
}

// ─── Build Stremio stream objects ──────────────────────────────────────────
function buildStremioStreams(resolvedStreams, tmdb, episodeNum) {
  const title = tmdb?.title || 'Unknown';
  const poster = tmdb?.poster || null;
  const description = tmdb?.overview || '';
  const genres = tmdb?.genres || [];
  const seen = new Set();

  const streams = [];
  for (const s of resolvedStreams) {
    if (!s.url || seen.has(s.url)) continue;
    seen.add(s.url);

    const isDirect = /\.(mkv|mp4|webm)/i.test(s.url) || /workers\.dev|pixeldrain/i.test(s.url);
    if (!isDirect) continue; // Only return directly playable streams

    streams.push({
      name: `ToonWorld4All\n${s.quality}`,
      title: `${title} - Episode ${episodeNum || 1} · ${s.quality} · ${s.server}${s.fileSize ? ' · ' + s.fileSize : ''}`,
      url: s.url,
      quality: s.quality,
      behaviorHints: {
        notWebReady: false,
        headers: { 'User-Agent': UA },
        proxyHeaders: { request: { 'User-Agent': UA } },
      },
      poster,
      meta: {
        provider: 'ToonWorld4All',
        source: s.server,
        type: 'mp4',
        quality: s.quality,
        title: `${title} - Episode ${episodeNum || 1}`,
        fileName: s.fileName || '',
        size: s.fileSize || '',
        episodeNumber: episodeNum,
        description,
        poster,
        genres,
        directStream: true,
        mediaType: 'series',
      },
    });
  }

  // Sort: 4K first, then 1080p, then by file size descending
  const order = { '4K': 5, '2160p': 5, '1080p': 4, '720p': 3, '480p': 2 };
  streams.sort((a, b) => {
    const qDiff = (order[b.quality] || 0) - (order[a.quality] || 0);
    if (qDiff !== 0) return qDiff;
    const aSize = parseFloat((a.meta.size || '').match(/(\d+(?:\.\d+)?)/)?.[1] || '0');
    const bSize = parseFloat((b.meta.size || '').match(/(\d+(?:\.\d+)?)/)?.[1] || '0');
    return bSize - aSize;
  });
  return streams;
}

// ─── Main entry ───────────────────────────────────────────────────────────
async function getToonWorld4AllStreams(tmdbId, type, season, episode) {
  try {
    const isSeries = type === 'tv' || type === 'series';
    const tmdb = await getTmdbDetails(tmdbId, isSeries ? 'tv' : 'movie');
    if (!tmdb || !tmdb.title) return [];

    // Search
    const results = await searchTw4a(tmdb.title);
    if (!results.length) return [];

    // Find best match
    let best = results.find(r => r.title.toLowerCase().includes(tmdb.title.toLowerCase())) || results[0];

    // Fetch post page
    const postHtml = await fetchHtml(best.url, { headers: { Referer: TW4A_BASE + '/' } });
    if (!postHtml) return [];

    const $ = cheerio.load(postHtml);
    const episodePattern = isSeries
      ? new RegExp(`(?:-${season}x0*${episode}|-${String(season).padStart(2,'0')}x0*${episode}|-ep0*${episode})(?:/|\\?|$)`, 'i')
      : null;

    const archiveUrls = new Set();
    $("a[href*='archive.toonworld4all']").each((_, anchor) => {
      const href = ($(anchor).attr('href') || '').trim();
      if (!href) return;
      if (isSeries) {
        if (href.includes('/episode/') && (!episodePattern || episodePattern.test(href))) archiveUrls.add(href);
      } else if (href.includes('/movie/')) {
        archiveUrls.add(href);
      }
    });

    if (!archiveUrls.size) return [];

    // Extract streams from archive pages
    const archiveResults = await Promise.all(
      Array.from(archiveUrls).map(url => extractArchiveStreams(url, best.title))
    );

    const resolved = archiveResults.flat();
    const epNum = episode ? parseInt(episode) : 1;
    const streams = buildStremioStreams(resolved, tmdb, epNum);
    console.log(`[ToonWorld4All] Returning ${streams.length} direct streams for ${tmdb.title} ep${epNum}`);
    return streams;
  } catch (e) {
    console.error(`[ToonWorld4All] Error: ${e.message}`);
    return [];
  }
}

module.exports = {
  getToonWorld4AllStreams,
  searchTw4a,
  extractArchiveStreams,
  resolveRedirectToStream,
  decryptRedirectToken,
  extractEmbeddedProps,
  resolveHubCloud,
  detectQuality,
  TW4A_BASE,
  PROVIDER_NAME: 'ToonWorld4All',
};
