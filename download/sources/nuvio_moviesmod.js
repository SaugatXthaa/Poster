/**
 * MoviesMod Provider — self-contained, returns direct playable 4K streams
 * =======================================================================
 * Site: https://moviesmod.ai.in (domain fetched dynamically from GitHub)
 *
 * Resolution chain:
 *   TMDB ID → search moviesmod → parse h4 quality headers → follow
 *   modrefer.in/links.modpro.blog → driveseed.org → window.location.replace
 *   → file page → Resume Cloud / Instant Download → workers.dev / Google Drive
 *   → direct .mkv/.mp4 URL
 *
 * Self-contained: uses only lib/http.js (native fetch), no external deps.
 */
'use strict';

const { fetchHtml, fetchJson, postForm, UA, decodeHtmlEntities } = require('../lib/http');

const TMDB_API_KEY = '439c478a771f35c05022f9feabcca01c';
const DOMAINS_JSON = 'https://raw.githubusercontent.com/phisher98/TVVVV/refs/heads/main/domains.json';
let cachedDomain = null;
let domainCacheTime = 0;

// ─── Domain resolution (fetches latest from GitHub, 4h cache) ─────────────
async function getDomain() {
  const now = Date.now();
  if (cachedDomain && (now - domainCacheTime) < 4 * 3600 * 1000) return cachedDomain;
  try {
    const data = await fetchJson(DOMAINS_JSON);
    if (data && data.moviesmod) {
      cachedDomain = data.moviesmod;
      domainCacheTime = now;
      return cachedDomain;
    }
  } catch (e) { /* fall through */ }
  cachedDomain = 'https://moviesmod.build';
  domainCacheTime = now;
  return cachedDomain;
}

// ─── TMDB lookup ──────────────────────────────────────────────────────────
async function getTmdbDetails(tmdbId, type) {
  const url = `https://api.themoviedb.org/3/${type === 'tv' ? 'tv' : 'movie'}/${tmdbId}?api_key=${TMDB_API_KEY}&language=en-US`;
  const data = await fetchJson(url);
  if (!data) return null;
  return {
    title: type === 'tv' ? data.name : data.title,
    year: ((type === 'tv' ? data.first_air_date : data.release_date) || '').substring(0, 4),
  };
}

// ─── String similarity (Levenshtein-based) ────────────────────────────────
function similarity(a, b) {
  a = (a || '').toLowerCase(); b = (b || '').toLowerCase();
  if (a === b) return 1;
  if (!a.length || !b.length) return 0;
  const matrix = Array(b.length + 1).fill(0).map(() => Array(a.length + 1).fill(0));
  for (let i = 0; i <= a.length; i++) matrix[0][i] = i;
  for (let j = 0; j <= b.length; j++) matrix[j][0] = j;
  for (let j = 1; j <= b.length; j++) {
    for (let i = 1; i <= a.length; i++) {
      const cost = a[i-1] === b[j-1] ? 0 : 1;
      matrix[j][i] = Math.min(matrix[j-1][i] + 1, matrix[j][i-1] + 1, matrix[j-1][i-1] + cost);
    }
  }
  return 1 - matrix[b.length][a.length] / Math.max(a.length, b.length);
}

// ─── Search moviesmod ─────────────────────────────────────────────────────
async function search(query) {
  const domain = await getDomain();
  const url = `${domain}/?s=${encodeURIComponent(query)}`;
  const html = await fetchHtml(url);
  if (!html) return [];
  const results = [];
  // Parse latestPost articles — extract first <a href> + title from each
  const articleRe = /<article[^>]*class="[^"]*latestPost[^"]*"[^>]*>([\s\S]*?)<\/article>/g;
  let m;
  while ((m = articleRe.exec(html)) !== null) {
    const block = m[1];
    // Try title attr first, then h2/h3 text, then img alt
    let href = (block.match(/href="([^"]+)"/) || [])[1];
    let title = (block.match(/title="([^"]+)"/) || [])[1] ||
                (block.match(/<h[23][^>]*>([^<]+)<\/h[23]>/) || [])[1] ||
                (block.match(/alt="([^"]+)"/) || [])[1] || '';
    if (href && title) {
      results.push({ url: decodeHtmlEntities(href), title: decodeHtmlEntities(title).trim() });
    }
  }
  return results;
}

// ─── Extract download links from movie page ──────────────────────────────
// Parses h4 headers (movie qualities) and finds modrefer.in / modpro.blog links
async function extractDownloadLinks(pageUrl) {
  const html = await fetchHtml(pageUrl);
  if (!html) return [];
  const links = [];
  // Find h4 headers and the links that follow them
  const h4Re = /<h4[^>]*>([\s\S]*?)<\/h4>[\s\S]*?(?=<h[234]|$)/g;
  let m;
  while ((m = h4Re.exec(html)) !== null) {
    const headerText = m[1].replace(/<[^>]+>/g, '').trim();
    const blockContent = m[0].substring(m[0].indexOf('</h4>') + 5);
    // Look for modrefer.in, links.modpro.blog, posts.modpro.blog links
    const linkMatch = blockContent.match(/href="(https?:\/\/(?:modrefer\.in|links\.modpro\.blog|posts\.modpro\.blog)[^"]+)"/);
    if (linkMatch) {
      const quality = extractQuality(headerText);
      links.push({ quality: headerText, cleanQuality: quality, url: linkMatch[1] });
    }
  }
  // Also try h3 (TV seasons)
  const h3Re = /<h3[^>]*>([\s\S]*?)<\/h3>[\s\S]*?(?=<h[234]|$)/g;
  while ((m = h3Re.exec(html)) !== null) {
    const headerText = m[1].replace(/<[^>]+>/g, '').trim();
    if (!headerText.toLowerCase().includes('season')) continue;
    const blockContent = m[0].substring(m[0].indexOf('</h3>') + 5);
    const epLinks = blockContent.match(/href="([^"]*maxbutton-episode-links[^"]*)"/g) || [];
    for (const el of epLinks) {
      const url = (el.match(/href="([^"]+)"/) || [])[1];
      if (url && !el.includes('batch')) {
        links.push({ quality: headerText, cleanQuality: extractQuality(headerText), url });
      }
    }
  }
  return links;
}

function extractQuality(text) {
  const m = (text || '').match(/(480p|720p|1080p|2160p|4k)/i);
  return m ? m[1].toLowerCase() : 'unknown';
}

function getTechDetails(text) {
  const lower = (text || '').toLowerCase();
  const d = [];
  if (lower.includes('10bit')) d.push('10-bit');
  if (lower.includes('hevc') || lower.includes('x265')) d.push('HEVC');
  if (lower.includes('hdr')) d.push('HDR');
  return d;
}

// ─── Resolve intermediate links (modrefer.in, modpro.blog, driveseed.org) ─
async function resolveIntermediate(url, referer, quality) {
  const html = await fetchHtml(url, { headers: { Referer: referer } });
  if (!html) return [];
  const finalLinks = [];
  // modrefer.in: decode base64 ?url= param
  if (url.includes('modrefer.in')) {
    const urlParam = new URL(url).searchParams.get('url');
    if (urlParam) {
      try {
        const decoded = Buffer.from(urlParam, 'base64').toString('utf8');
        return resolveIntermediate(decoded, url, quality);
      } catch (e) { /* fall through */ }
    }
    // Also look for timed-content links
    const re = /class="timed-content-client_show_0_5_0[^"]*"[^>]*>[\s\S]*?href="([^"]+)"/g;
    let m;
    while ((m = re.exec(html)) !== null) {
      finalLinks.push({ server: 'modrefer', url: m[1] });
    }
  }
  // episodes.modpro.blog / links.modpro.blog / posts.modpro.blog
  if (url.includes('modpro.blog')) {
    const re = /href="(https?:\/\/(?:driveseed\.org|tech\.unblockedgames\.world|tech\.creativeexpressionsblog\.com|tech\.examzculture\.in)[^"]+)"/g;
    let m;
    while ((m = re.exec(html)) !== null) {
      finalLinks.push({ server: 'modpro', url: m[1] });
    }
  }
  // dramadrip.com
  if (url.includes('dramadrip.com')) {
    const re = /href="(https?:\/\/(?:episodes\.modpro\.blog|cinematickit\.org)[^"]+)"/g;
    let m;
    while ((m = re.exec(html)) !== null) {
      finalLinks.push({ server: 'dramadrip', url: m[1] });
    }
  }
  // cinematickit.org
  if (url.includes('cinematickit.org')) {
    const re = /href="(https?:\/\/driveseed\.org[^"]+)"/g;
    let m;
    while ((m = re.exec(html)) !== null) {
      finalLinks.push({ server: 'cinematickit', url: m[1] });
    }
  }
  // If the URL itself is driveseed.org, add it directly
  if (url.includes('driveseed.org')) {
    finalLinks.push({ server: 'direct', url });
  }
  return finalLinks;
}

// ─── Resolve driveseed.org → file page → direct download URL ─────────────
async function resolveDriveseed(driveseedUrl) {
  const html = await fetchHtml(driveseedUrl, { headers: { Referer: 'https://links.modpro.blog/' } });
  if (!html) return null;

  // Follow window.location.replace("...") redirect
  const redirectMatch = html.match(/window\.location\.replace\("([^"]+)"\)/);
  let finalUrl = driveseedUrl;
  let finalHtml = html;
  if (redirectMatch && redirectMatch[1]) {
    finalUrl = `https://driveseed.org${redirectMatch[1]}`;
    finalHtml = await fetchHtml(finalUrl, { headers: { Referer: driveseedUrl } });
    if (!finalHtml) return null;
  }

  // Extract size + filename
  let size = 'Unknown', fileName = null;
  const sizeMatch = finalHtml.match(/Size\s*:\s*([0-9.,]+\s*[KMGT]B)/i);
  if (sizeMatch) size = sizeMatch[1];
  const nameMatch = finalHtml.match(/Name\s*:\s*([^\n<]+)/i);
  if (nameMatch) fileName = nameMatch[1].trim();

  // Try Resume Cloud button → workers.dev direct link
  const resumeCloudMatch = finalHtml.match(/href="([^"]*)"[^>]*>\s*(?:Resume Cloud|Cloud Resume Download)/i);
  if (resumeCloudMatch) {
    let rcUrl = resumeCloudMatch[1];
    if (rcUrl.startsWith('/')) rcUrl = `https://driveseed.org${rcUrl}`;
    const rcHtml = await fetchHtml(rcUrl, { headers: { Referer: finalUrl } });
    if (rcHtml) {
      const workerLink = rcHtml.match(/class="btn-success"[^>]*href="([^"]+)"/) ||
                        rcHtml.match(/href="(https?:\/\/[a-z0-9-]+\.workers\.dev[^"]+)"/);
      if (workerLink) {
        return { url: workerLink[1].replace(/ /g, '%20'), size, fileName, method: 'Resume Cloud' };
      }
    }
  }

  // Try Instant Download → cdn.video-leech.pro / workers.dev
  const instantMatch = finalHtml.match(/href="([^"]*)"[^>]*>\s*Instant Download/i);
  if (instantMatch) {
    let idUrl = instantMatch[1];
    if (idUrl.startsWith('/')) idUrl = `https://driveseed.org${idUrl}`;
    // Check if it has ?url= with a direct file link
    try {
      const u = new URL(idUrl);
      const embedded = u.searchParams.get('url');
      if (embedded && /workers\.dev|\.r2\.dev|googlevideo|googleusercontent|\.(mp4|mkv)/i.test(embedded)) {
        return { url: embedded.replace(/ /g, '%20'), size, fileName, method: 'Instant Download' };
      }
    } catch (e) { /* fall through */ }
    // Check if it's a direct CDN link
    if (idUrl.includes('cdn.video-leech.pro') || idUrl.includes('workers.dev') || idUrl.includes('.r2.dev')) {
      return { url: idUrl.replace(/ /g, '%20'), size, fileName, method: 'Instant Download' };
    }
  }

  // Try Resume Worker Bot
  const workerMatch = finalHtml.match(/href="([^"]*)"[^>]*>\s*Resume Worker Bot/i);
  if (workerMatch) {
    let wUrl = workerMatch[1];
    if (wUrl.startsWith('/')) wUrl = `https://driveseed.org${wUrl}`;
    const wHtml = await fetchHtml(wUrl, { headers: { Referer: finalUrl } });
    if (wHtml) {
      const tokenMatch = wHtml.match(/formData\.append\('token',\s*'([^']+)'\)/);
      const idMatch = wHtml.match(/fetch\('\/download\?id=([^']+)',/);
      if (tokenMatch && idMatch) {
        const result = await postForm(`https://driveseed.org/download?id=${idMatch[1]}`,
          `token=${tokenMatch[1]}`,
          { headers: { 'x-requested-with': 'XMLHttpRequest', Referer: wUrl } });
        if (result) {
          try {
            const data = JSON.parse(result);
            if (data.url) return { url: data.url, size, fileName, method: 'Worker Bot' };
          } catch (e) { /* fall through */ }
        }
      }
    }
  }

  // Scan for any direct links on the file page
  const directLink = finalHtml.match(/href="(https?:\/\/[a-z0-9-]+\.workers\.dev[^"]+)"/) ||
                     finalHtml.match(/href="(https?:\/\/[a-z0-9-]+\.r2\.dev[^"]+)"/) ||
                     finalHtml.match(/href="(https?:\/\/[^"]*googleusercontent[^"]*)"/);
  if (directLink) {
    return { url: directLink[1].replace(/ /g, '%20'), size, fileName, method: 'Direct Scan' };
  }

  // If we found a cdn.video-leech.pro link, try to resolve it to Google Drive
  const videoLeech = finalHtml.match(/href="(https?:\/\/cdn\.video-leech\.pro[^"]+)"/);
  if (videoLeech) {
    const vlUrl = videoLeech[1];
    // Follow redirect to get ?url= parameter
    try {
      const r = await fetch(vlUrl, { method: 'HEAD', redirect: 'follow', signal: AbortSignal.timeout(8000) });
      const finalRedirect = r.url || r.headers.get('location');
      if (finalRedirect) {
        const urlParams = new URLSearchParams(new URL(finalRedirect).search);
        const gdriveUrl = urlParams.get('url');
        if (gdriveUrl && gdriveUrl.includes('video-downloads.googleusercontent.com')) {
          return { url: decodeURIComponent(gdriveUrl), size, fileName, method: 'VideoLeech→GDrive' };
        }
      }
    } catch (e) { /* fall through */ }
  }

  return null;
}

// ─── Main: getMoviesModStreams ─────────────────────────────────────────────
async function getMoviesModStreams(tmdbId, type, season, episode) {
  try {
    const mediaType = type === 'tv' ? 'tv' : 'movie';
    const tmdb = await getTmdbDetails(tmdbId, mediaType);
    if (!tmdb || !tmdb.title) return [];

    const results = await search(tmdb.title + (tmdb.year ? ' ' + tmdb.year : ''));
    if (!results.length) return [];

    // Best match by string similarity
    let best = null, bestScore = 0;
    for (const r of results) {
      const score = similarity(tmdb.title, r.title);
      if (score > bestScore) { bestScore = score; best = r; }
    }
    if (!best || bestScore < 0.3) {
      // Fallback: strict word-boundary match
      best = results.find(r => {
        const t = r.title.toLowerCase();
        return t.includes(tmdb.title.toLowerCase()) && (!tmdb.year || t.includes(tmdb.year));
      });
    }
    if (!best) return [];

    const downloadLinks = await extractDownloadLinks(best.url);
    if (!downloadLinks.length) return [];

    // Filter 480p (low quality, skip)
    let relevant = downloadLinks.filter(l => !l.cleanQuality.includes('480p'));
    if (relevant.length === 0) relevant = downloadLinks;

    // For TV: filter by season
    if ((mediaType === 'tv' || mediaType === 'series') && season !== null) {
      relevant = relevant.filter(l => l.quality.toLowerCase().includes(`season ${season}`) || l.quality.toLowerCase().includes(`s${season}`));
    }

    const streams = [];
    for (const link of relevant.slice(0, 12)) {
      const intermediate = await resolveIntermediate(link.url, best.url, link.quality);
      for (const target of intermediate) {
        let currentUrl = target.url;
        if (currentUrl.includes('driveseed.org')) {
          const resolved = await resolveDriveseed(currentUrl);
          if (resolved && resolved.url) {
            const q = extractQuality(link.quality);
            const tech = getTechDetails(link.quality);
            streams.push({
              name: `MoviesMod\n${q}`,
              title: `${resolved.fileName || link.quality}\n${resolved.size}${tech.length ? ' • ' + tech.join(' • ') : ''}`,
              url: resolved.url,
              quality: q,
              behaviorHints: {
                notWebReady: false,
                headers: { 'Referer': 'https://driveseed.org/', 'User-Agent': UA },
                proxyHeaders: { request: { 'Referer': 'https://driveseed.org/', 'User-Agent': UA } },
              },
              meta: {
                provider: 'MoviesMod',
                source: resolved.method,
                type: 'mp4',
                quality: q,
                size: resolved.size,
                codec: tech.join(','),
                title: resolved.fileName || link.quality,
                directStream: true,
                mediaType,
              },
            });
            break; // one stream per quality
          }
        }
      }
    }

    // Sort by quality descending
    const order = { '2160p': 4, '1080p': 3, '720p': 2, '480p': 1 };
    streams.sort((a, b) => (order[b.quality] || 0) - (order[a.quality] || 0));
    return streams;
  } catch (e) {
    console.error(`[MoviesMod] Error: ${e.message}`);
    return [];
  }
}

module.exports = {
  getMoviesModStreams,
  search,
  extractDownloadLinks,
  resolveIntermediate,
  resolveDriveseed,
  getDomain,
  PROVIDER_NAME: 'MoviesMod',
};
