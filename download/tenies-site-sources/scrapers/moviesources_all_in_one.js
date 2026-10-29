// Multi-Source Scraper — movieshunt.casa + moviesdrive.christmas + hdhub4u.cl
// =========================================================================
// Returns direct playable download links (MKV/GDrive) for all 3 sites.
//
// All 3 sites use the HubCloud/gdflix ecosystem for file hosting:
//   - hubcloud.cx/drive/{id} → gamerxyt.com → GDrive (lh3.googleusercontent.com)
//   - gdflix.dev/file/{id} → indexserver.site (direct ZIP) or drivebot.sbs
//   - abhilinks.site/archives/{id} → hubcloud + gdflix links
//
// URL PATTERNS
// ------------
// movieshunt.casa:
//   Search: /?s={title}
//   Movie:  /{slug}/
//   Download: → abhilinks.site → hubcloud + gdflix
//
// moviesdrive.christmas:
//   Search: /wp-json/wp/v2/posts?search={title}
//   Movie:  /{slug}/
//   Download: → hubcloud.foo/drive/search-recover.php → hubcloud.cx/drive/{id}
//
// hdhub4u.cl:
//   Search: /?s={title}
//   Movie:  /{slug}/
//   Download: → hubcloud.cx/drive/{id} or hubcdn.sbs/file/{id}
//
// 4K SUPPORT
// ----------
// All 3 sites have 4K (2160p) content. Quality is extracted from:
//   - The search query parameter (base64 decoded)
//   - The download link text/label
//   - The file name in the HubCloud API response

'use strict';

const PROVIDER_NAME = 'MovieSources';
const TMDB_API_KEY = '8476a7ab80ad76f0936744df0430e67c';
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';

const SITES = {
  movieshunt: {
    name: 'MoviesHunt',
    origin: 'https://movieshunt.casa',
    searchPath: '/?s=',
  },
  moviesdrive: {
    name: 'MoviesDrive',
    origin: 'https://new3.moviesdrive.christmas',
    searchPath: '/wp-json/wp/v2/posts?search=',
  },
  hdhub4u: {
    name: 'HDHub4u',
    origin: 'https://new5.hdhub4u.cl',
    searchPath: '/?s=',
  },
};

// ---------------------------------------------------------------------------
// TMDB metadata
// ---------------------------------------------------------------------------
async function getTMDBInfo(tmdbId, type) {
  const url = `https://api.themoviedb.org/3/${type === 'tv' ? 'tv' : 'movie'}/${tmdbId}?api_key=${TMDB_API_KEY}&language=en-US`;
  const res = await fetch(url, {
    headers: { 'User-Agent': UA, Accept: 'application/json' },
    signal: AbortSignal.timeout(10000),
  });
  if (!res.ok) throw new Error(`TMDB HTTP ${res.status}`);
  const j = await res.json();
  return {
    title: j.name || j.title || 'Unknown',
    year: (j.first_air_date || j.release_date || '').slice(0, 4),
    type,
    tmdbId: String(tmdbId),
  };
}

// ---------------------------------------------------------------------------
// Fetch text with proper headers
// ---------------------------------------------------------------------------
async function fetchText(url, referer, timeout) {
  const headers = { 'User-Agent': UA, Accept: 'text/html,application/json,*/*' };
  if (referer) headers['Referer'] = referer;
  const res = await fetch(url, {
    headers,
    redirect: 'follow',
    signal: AbortSignal.timeout(timeout || 15000),
  });
  if (!res.ok) throw new Error(`HTTP ${res.status} for ${url.slice(0, 80)}`);
  return res.text();
}

// ---------------------------------------------------------------------------
// Search movieshunt.casa
// ---------------------------------------------------------------------------
async function searchMoviesHunt(title) {
  try {
    const html = await fetchText(`${SITES.movieshunt.origin}/?s=${encodeURIComponent(title)}`);
    const links = [...html.matchAll(/href="(https:\/\/movieshunt\.casa\/[a-z0-9-]+\/)"/g)];
    const results = [];
    for (const m of links) {
      const slug = m[1];
      if (slug.includes('category') || slug.includes('tag') || slug.includes('page') || slug.includes('wp-') || slug.includes('feed') || slug.includes('search') || slug.includes('author')) continue;
      const slugLower = slug.toLowerCase();
      const titleLower = title.toLowerCase().split(' ')[0];
      if (slugLower.includes(titleLower)) {
        results.push({ url: slug, slug: slug.split('/').slice(-2)[0] });
      }
    }
    return results;
  } catch (e) {
    return [];
  }
}

// ---------------------------------------------------------------------------
// Search moviesdrive.christmas via WP REST API
// ---------------------------------------------------------------------------
async function searchMoviesDrive(title) {
  try {
    const body = await fetchText(`${SITES.moviesdrive.origin}/wp-json/wp/v2/posts?search=${encodeURIComponent(title)}&_fields=link,title,slug&per_page=10`);
    const posts = JSON.parse(body);
    return posts.map(p => ({
      url: p.link,
      slug: p.slug,
      title: p.title?.rendered || '',
    }));
  } catch (e) {
    return [];
  }
}

// ---------------------------------------------------------------------------
// Search hdhub4u.cl
// ---------------------------------------------------------------------------
async function searchHDHub4u(title) {
  try {
    const html = await fetchText(`${SITES.hdhub4u.origin}/?s=${encodeURIComponent(title)}`);
    const links = [...html.matchAll(/href="(https:\/\/[^\/]*hdhub4u[^\/]*\/[a-z0-9-]+\/)"/g)];
    const results = [];
    for (const m of links) {
      const slug = m[1];
      if (slug.includes('category') || slug.includes('tag') || slug.includes('page') || slug.includes('wp-') || slug.includes('feed') || slug.includes('search') || slug.includes('author')) continue;
      results.push({ url: slug, slug: slug.split('/').slice(-2)[0] });
    }
    return results;
  } catch (e) {
    return [];
  }
}

// ---------------------------------------------------------------------------
// Parse movie page for download links
// Returns: [{ quality, url, source }]
// ---------------------------------------------------------------------------
function parseDownloadLinks(html, siteName) {
  const links = [];

  // Find hubcloud links (hubcloud.cx/drive/{id} or hubcloud.foo/drive/search-recover.php)
  const hubcloudDirect = [...html.matchAll(/href="(https:\/\/hubcloud\.[a-z]+\/drive\/([a-z0-9]+))"/g)];
  for (const m of hubcloudDirect) {
    links.push({ url: m[1], fileId: m[2], source: 'hubcloud', type: 'direct' });
  }

  // Find hubcloud search-recover links (moviesdrive pattern)
  const hubcloudSearch = [...html.matchAll(/href="(https:\/\/hubcloud\.[a-z]+\/drive\/search-recover\.php\?from_ac=([^&]+)&amp;q=([^"\s]+))"/g)];
  for (const m of hubcloudSearch) {
    let quality = '?';
    try {
      const q = Buffer.from(m[3] + '==', 'base64').toString('utf8');
      quality = q.replace(/.*(\d{3,4}p|4K).*/, '$1') || '?';
    } catch (e) {}
    links.push({ url: m[1], fromAc: m[2], qB64: m[3], quality, source: 'hubcloud-search', type: 'search' });
  }

  // Find gdflix links
  const gdflix = [...html.matchAll(/href="(https:\/\/(?:new\d+\.)?gdflix\.[a-z]+\/file\/([A-Za-z0-9]+))"/g)];
  for (const m of gdflix) {
    links.push({ url: m[1], fileId: m[2], source: 'gdflix', type: 'gdflix' });
  }

  // Find abhilinks.site links (movieshunt pattern)
  const abhilinks = [...html.matchAll(/href="(https:\/\/abhilinks\.site\/archives\/(\d+)\/?)"/g)];
  for (const m of abhilinks) {
    links.push({ url: m[1], archiveId: m[2], source: 'abhilinks', type: 'abhilinks' });
  }

  // Find hubcdn.sbs links (hdhub4u pattern)
  const hubcdn = [...html.matchAll(/href="(https:\/\/hubcdn\.sbs\/file\/([A-Za-z0-9]+))"/g)];
  for (const m of hubcdn) {
    links.push({ url: m[1], fileId: m[2], source: 'hubcdn', type: 'hubcdn' });
  }

  // Extract quality from nearby text
  const qualityPattern = /(?:2160p|1080p|720p|480p|4K)/i;
  for (const link of links) {
    if (!link.quality) {
      // Try to find quality in the page near this link
      const idx = html.indexOf(link.url);
      if (idx >= 0) {
        const context = html.slice(Math.max(0, idx - 500), idx + 500);
        const qMatch = context.match(qualityPattern);
        if (qMatch) link.quality = qMatch[0].toLowerCase();
      }
      if (!link.quality) link.quality = '?';
    }
  }

  return links;
}

// ---------------------------------------------------------------------------
// Resolve HubCloud search-recover API → get file list
// ---------------------------------------------------------------------------
async function resolveHubcloudSearch(searchUrl, fromAc, qB64) {
  try {
    const query = Buffer.from(qB64 + '==', 'base64').toString('utf8');
    const apiUrl = `https://hubcloud.cx/drive/search-recover.php?api=search&q=${encodeURIComponent(query)}&page=1&from_ac=${fromAc}`;
    const res = await fetch(apiUrl, {
      headers: { 'User-Agent': UA, Accept: 'application/json' },
      signal: AbortSignal.timeout(10000),
    });
    if (!res.ok) return [];
    const data = await res.json();
    return (data.hits || []).map(h => ({
      fileName: h.file_name,
      size: h.size,
      url: h.url, // hubcloud.cx/drive/{id}
      fileId: h.url.split('/').pop(),
    }));
  } catch (e) {
    return [];
  }
}

// ---------------------------------------------------------------------------
// Resolve hubcloud.cx/drive/{id} → gamerxyt → GDrive URL
// ---------------------------------------------------------------------------
async function resolveHubcloudDrive(driveUrl) {
  try {
    const html = await fetchText(driveUrl, 'https://hubcloud.cx/');
    // Find gamerxyt URL
    const gxMatch = html.match(/https:\/\/gamerxyt\.com\/hubcloud\.php\?[^"'\s]+/);
    if (!gxMatch) return null;

    const gxHtml = await fetchText(gxMatch[0], driveUrl);
    // Find GDrive URL
    const gdMatch = gxHtml.match(/https:\/\/lh3\.googleusercontent\.com\/[^\s"'<>]+/);
    if (gdMatch) {
      // Clean the URL (remove =mXX#t=... suffix, add =d for download)
      let url = gdMatch[0].split('#')[0].split('=m')[0];
      return url + '=d';
    }

    // Find pixel.hubcloud.cx URL (alternative)
    const pixelMatch = gxHtml.match(/https:\/\/pixel\.hubcloud\.cx\/\?id=[^"'\s]+/);
    if (pixelMatch) return pixelMatch[0];

    // Find pixeldrain URL
    const pdMatch = gxHtml.match(/https:\/\/pixeldrain\.[a-z]+\/u\/[A-Za-z0-9]+/);
    if (pdMatch) return pdMatch[0].replace('/u/', '/api/file/') + '?download';

    return null;
  } catch (e) {
    return null;
  }
}

// ---------------------------------------------------------------------------
// Resolve gdflix file → direct download URL
// ---------------------------------------------------------------------------
async function resolveGdflix(gdflixUrl) {
  try {
    const html = await fetchText(gdflixUrl);
    // Find indexserver.site URL (direct ZIP download)
    const indexMatch = html.match(/https:\/\/max\.indexserver\.site\/[^\s"'<>]+/);
    if (indexMatch) return { url: indexMatch[0], type: 'zip' };

    // Find drivebot.sbs URL
    const drivebotMatch = html.match(/https:\/\/drivebot\.sbs\/download\?id=[^\s"'<>]+/);
    if (drivebotMatch) return { url: drivebotMatch[0], type: 'drivebot' };

    return null;
  } catch (e) {
    return null;
  }
}

// ---------------------------------------------------------------------------
// Resolve abhilinks.site → hubcloud + gdflix links
// ---------------------------------------------------------------------------
async function resolveAbhilinks(abhilinksUrl) {
  try {
    const html = await fetchText(abhilinksUrl);
    const links = [];

    // Find hubcloud links
    const hubcloud = [...html.matchAll(/href="(https:\/\/hubcloud\.[a-z]+\/drive\/([a-z0-9]+))"/g)];
    for (const m of hubcloud) {
      links.push({ url: m[1], fileId: m[2], source: 'hubcloud' });
    }

    // Find gdflix links
    const gdflix = [...html.matchAll(/href="(https:\/\/(?:new\d+\.)?gdflix\.[a-z]+\/file\/([A-Za-z0-9]+))"/g)];
    for (const m of gdflix) {
      links.push({ url: m[1], fileId: m[2], source: 'gdflix' });
    }

    return links;
  } catch (e) {
    return [];
  }
}

// ---------------------------------------------------------------------------
// Quick-check a URL — returns { playable, status }
// ---------------------------------------------------------------------------
async function quickCheckUrl(url) {
  try {
    const res = await fetch(url, {
      headers: { 'User-Agent': UA, Range: 'bytes=0-3' },
      signal: AbortSignal.timeout(5000),
    });
    return { playable: res.ok || res.status === 206, status: res.status };
  } catch (e) {
    return { playable: false, status: 0 };
  }
}

// ---------------------------------------------------------------------------
// Build a Stremio stream object
// ---------------------------------------------------------------------------
function buildStream(opts) {
  const s = {
    name: opts.name,
    title: opts.title,
    url: opts.url,
    quality: opts.quality || '1080p',
    type: opts.type || 'video/x-matroska',
    behaviorHints: { bingeGroup: opts.bingeGroup || 'moviesources' },
  };
  if (opts.filename) s.behaviorHints.filename = opts.filename;
  // GDrive URLs need browser-like headers
  if (opts.needsProxy) {
    s.behaviorHints.proxyHeaders = {
      request: { 'User-Agent': UA },
    };
  }
  return s;
}

// ---------------------------------------------------------------------------
// Process a single site: search → fetch movie page → parse → resolve → streams
// ---------------------------------------------------------------------------
async function processSite(siteKey, title, year, info) {
  const site = SITES[siteKey];
  const streams = [];
  console.log(`[${site.name}] Searching for "${title}"...`);

  let results = [];
  if (siteKey === 'movieshunt') results = await searchMoviesHunt(title);
  else if (siteKey === 'moviesdrive') results = await searchMoviesDrive(title);
  else if (siteKey === 'hdhub4u') results = await searchHDHub4u(title);

  if (results.length === 0) {
    console.log(`[${site.name}] No results found`);
    return streams;
  }

  // Pick the best match (first result that contains the title)
  let bestMatch = null;
  for (const r of results) {
    const slugLower = (r.slug || '').toLowerCase();
    const titleLower = title.toLowerCase();
    if (slugLower.includes(titleLower.split(' ')[0])) {
      bestMatch = r;
      break;
    }
  }
  if (!bestMatch) bestMatch = results[0];
  console.log(`[${site.name}] Match: ${bestMatch.slug || bestMatch.url}`);

  // Fetch the movie page
  let movieHtml;
  try {
    movieHtml = await fetchText(bestMatch.url);
  } catch (e) {
    console.log(`[${site.name}] Failed to fetch movie page: ${e.message}`);
    return streams;
  }

  // Parse download links
  const links = parseDownloadLinks(movieHtml, site.name);
  console.log(`[${site.name}] Found ${links.length} download links`);

  // Resolve each link
  for (const link of links) {
    try {
      if (link.type === 'search') {
        // HubCloud search-recover → get files → resolve each
        const files = await resolveHubcloudSearch(link.url, link.fromAc, link.qB64);
        for (const file of files) {
          const resolved = await resolveHubcloudDrive(file.url);
          if (resolved) {
            const quality = file.fileName.match(/(2160p|1080p|720p|480p|4K)/i)?.[0]?.toLowerCase() || link.quality || '?';
            streams.push(buildStream({
              name: `${site.name} - ${quality.toUpperCase()}`,
              title: `${info.title} [${site.name} ${quality.toUpperCase()}] ${file.size || ''}`.trim(),
              url: resolved,
              quality: quality === '4k' ? '2160p' : quality,
              type: 'video/x-matroska',
              filename: file.fileName,
              needsProxy: resolved.includes('googleusercontent'),
              bingeGroup: `${siteKey}-${quality}-${file.fileId}`,
            }));
            console.log(`[${site.name}] + ${quality} ${file.size || ''}: ${resolved.slice(0, 80)}`);
          }
        }
      } else if (link.type === 'direct') {
        // Direct hubcloud.cx/drive/{id} → resolve
        const resolved = await resolveHubcloudDrive(link.url);
        if (resolved) {
          streams.push(buildStream({
            name: `${site.name} - ${link.quality.toUpperCase()}`,
            title: `${info.title} [${site.name} ${link.quality.toUpperCase()}]`,
            url: resolved,
            quality: link.quality === '4k' ? '2160p' : link.quality,
            type: 'video/x-matroska',
            needsProxy: resolved.includes('googleusercontent'),
            bingeGroup: `${siteKey}-${link.quality}-${link.fileId}`,
          }));
          console.log(`[${site.name}] + ${link.quality}: ${resolved.slice(0, 80)}`);
        }
      } else if (link.type === 'gdflix') {
        // gdflix → resolve to indexserver or drivebot
        const resolved = await resolveGdflix(link.url);
        if (resolved) {
          streams.push(buildStream({
            name: `${site.name} - ${link.quality.toUpperCase()} (GDFlix)`,
            title: `${info.title} [${site.name} ${link.quality.toUpperCase()} GDFlix]`,
            url: resolved.url,
            quality: link.quality === '4k' ? '2160p' : link.quality,
            type: resolved.type === 'zip' ? 'application/zip' : 'video/x-matroska',
            bingeGroup: `${siteKey}-gdflix-${link.fileId}`,
          }));
          console.log(`[${site.name}] + GDFlix ${link.quality}: ${resolved.url.slice(0, 80)}`);
        }
      } else if (link.type === 'abhilinks') {
        // abhilinks → resolve to hubcloud + gdflix links
        const subLinks = await resolveAbhilinks(link.url);
        for (const sub of subLinks) {
          if (sub.source === 'hubcloud') {
            const resolved = await resolveHubcloudDrive(sub.url);
            if (resolved) {
              streams.push(buildStream({
                name: `${site.name} - ${link.quality.toUpperCase()}`,
                title: `${info.title} [${site.name} ${link.quality.toUpperCase()}]`,
                url: resolved,
                quality: link.quality === '4k' ? '2160p' : link.quality,
                type: 'video/x-matroska',
                needsProxy: resolved.includes('googleusercontent'),
                bingeGroup: `${siteKey}-${link.quality}-${sub.fileId}`,
              }));
              console.log(`[${site.name}] + ${link.quality} (abhi→hubcloud): ${resolved.slice(0, 80)}`);
            }
          } else if (sub.source === 'gdflix') {
            const resolved = await resolveGdflix(sub.url);
            if (resolved) {
              streams.push(buildStream({
                name: `${site.name} - ${link.quality.toUpperCase()} (GDFlix)`,
                title: `${info.title} [${site.name} ${link.quality.toUpperCase()} GDFlix]`,
                url: resolved.url,
                quality: link.quality === '4k' ? '2160p' : link.quality,
                type: resolved.type === 'zip' ? 'application/zip' : 'video/x-matroska',
                bingeGroup: `${siteKey}-gdflix-${sub.fileId}`,
              }));
              console.log(`[${site.name}] + GDFlix ${link.quality} (abhi): ${resolved.url.slice(0, 80)}`);
            }
          }
        }
      }
    } catch (e) {
      // Skip failed links
    }
  }

  return streams;
}

// ---------------------------------------------------------------------------
// Main entry: get streams from all 3 sites
// ---------------------------------------------------------------------------
async function getStreams(tmdbId, type, season, episode) {
  tmdbId = String(tmdbId);
  const isMovie = type !== 'tv';

  if (!isMovie && (season == null || episode == null)) {
    console.log('[MovieSources] TV request without season/episode — returning empty');
    return [];
  }

  console.log('[MovieSources] Request: tmdb=' + tmdbId + ' type=' + type);

  // 1. Fetch TMDB info
  let info;
  try {
    info = await Promise.race([
      getTMDBInfo(tmdbId, type),
      new Promise((_, reject) => setTimeout(() => reject(new Error('TMDB timeout')), 10000)),
    ]);
  } catch (e) {
    console.log('[MovieSources] TMDB error: ' + e.message);
    return [];
  }
  console.log('[MovieSources] TMDB: ' + info.title + (info.year ? ' (' + info.year + ')' : ''));

  const allStreams = [];

  // 2. Process all 3 sites in parallel
  const [huntStreams, driveStreams, hdhubStreams] = await Promise.all([
    processSite('movieshunt', info.title, info.year, info),
    processSite('moviesdrive', info.title, info.year, info),
    processSite('hdhub4u', info.title, info.year, info),
  ]);

  allStreams.push(...huntStreams);
  allStreams.push(...driveStreams);
  allStreams.push(...hdhubStreams);

  // 3. Sort by quality (2160p first, then 1080p, etc.)
  const qualityOrder = { '2160p': 0, '1080p': 1, '720p': 2, '480p': 3 };
  allStreams.sort((a, b) => {
    const qa = qualityOrder[a.quality] || 99;
    const qb = qualityOrder[b.quality] || 99;
    return qa - qb;
  });

  console.log('[MovieSources] ' + allStreams.length + ' streams total');
  return allStreams;
}

module.exports = {
  getStreams: getStreams,
  getTMDBInfo: getTMDBInfo,
  processSite: processSite,
  parseDownloadLinks: parseDownloadLinks,
  resolveHubcloudDrive: resolveHubcloudDrive,
  resolveGdflix: resolveGdflix,
  resolveAbhilinks: resolveAbhilinks,
  listSites: function () { return Object.keys(SITES); },
};

// CLI
if (require.main === module) {
  const args = process.argv.slice(2);
  if (args.length < 2) {
    console.log('Usage: node moviesources_all_in_one.js <tmdbId> <movie|tv> [season] [episode]');
    process.exit(1);
  }
  getStreams(args[0], args[1], args[2] ? parseInt(args[2]) : null, args[3] ? parseInt(args[3]) : null)
    .then(streams => {
      console.log('\n=== Final streams ===');
      streams.forEach((s, i) => {
        console.log((i + 1) + '. ' + s.name + ' | ' + s.quality + ' | ' + s.type);
        console.log('   ' + s.url.slice(0, 120));
      });
      console.log('\nTotal: ' + streams.length + ' stream(s)');
    })
    .catch(e => console.error('FATAL: ' + e.stack));
}
