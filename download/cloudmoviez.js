// CloudMovieZ + MultiShows — Nuvio provider for the CloudMovieZ DooPlay WordPress site.
//
// ARCHITECTURE:
//   • multishows.top — fully locked behind anti-bot detection (webdriverCheck,
//     userAgentCheck, appVersionCheck). Returns "One moment, please..." JS
//     interstitial for ALL Node.js requests (403 on every endpoint).
//     CANNOT be bypassed from Node.js without a real browser + stealth plugin.
//   • cloudmoviez.top → 302 redirect → c1.cloudmoviez.shop (the actual site).
//   • c1.cloudmoviez.shop is a WordPress site using the DooPlay theme (v2.5.5).
//     Fully accessible from Node.js — no anti-bot.
//
// STREAM DISCOVERY FLOW:
//   For movies:
//     1. Search via `?s=<query>` → find /movies/{slug}/ URL
//     2. Load the movie page HTML
//     3. Parse the `<tr id='link-NUMBER'>` rows in `.links_table`
//        Each row has: post ID, link URL (https://c1.cloudmoviez.shop/links/{slug}/),
//        quality (e.g. "1080p x264"), language (e.g. "Dual Audio [Hindi-English]"),
//        size (e.g. "3.22 GB"), host (favicon domain).
//     4. Follow the /links/{slug}/ redirect → page reveals actual stream URL
//        (e.g. https://vcloud.zip/sw2dn62ugefuexw — Cloudflare-challenged file host)
//
//   For TV shows:
//     1. Search via `?s=<query>` → find /tvshows/{slug}/ URL
//     2. Load the TV page HTML
//     3. Parse the `<div class="cmz-row-card cmz-filter-item">` items
//        Each item has:
//          - title (filename, e.g. "A.Shop.for.Killers.S02E01...2160p.HDR.H265-Shubham.mkv")
//          - download URL: https://c1.cloudmoviez.shop/?file=<base64>
//          - The base64 decodes to {"url":"https://gdflix.dev/file/{id}","post":N,"title":"Download"}
//     4. Visit the gdflix.dev URL → page reveals actual download paths:
//          /cflare/{ts}/{id}  — Cloudflare-cached direct .mkv download
//          /cloud/{ts}/{id}   — Google Drive direct .mkv download
//          https://filesgram.xyz/?start=...&bot=gdflix29_bot — Telegram bot
//
// 4K SUPPORT:
//   • Movies: quality field shows "1080p x264" etc. 4K movies exist in /genre/4k/ archive.
//   • TV: filenames include "2160p.HDR.H265" or "2160p.DSNP.WEB-DL" — explicit 4K HDR streams.
//   • Provider labels streams "4K" when filename/quality field contains "2160p", "4K", "4k", or "UHD".
//
// ANIME SUB/DUB:
//   • cloudmoviez labels files "Dual Audio [Hindi-English]" — built-in multi-audio tracks.
//   • For anime, files often contain Japanese+English or Hindi+English audio pairs.
//   • Stremio's player auto-detects embedded audio tracks and shows the audio switcher.
//   • External subtitles: cloudmoviez doesn't have a subtitle endpoint. OpenSubtitles was
//     403-blocked in tests. The .mkv files typically have embedded subtitles too.
//
// ENRICHED METADATA:
//   • Parse from movie page HTML: title, year, IMDB rating, genres, runtime, TMDB poster.
//   • For full TMDB enrichment (cast, director, trailers, similar, recs), query TMDB directly
//     by resolving IMDB→TMDB ID via TMDB's /find endpoint.
//
// LIMITATIONS (honest):
//   • multishows.top unreachable from Node.js — anti-bot challenge requires real browser.
//   • The final stream URLs are on file hosts (vcloud.zip, gdflix.dev) that may be
//     Cloudflare-challenged. When challenged, we return the file-host page URL as an
//     iframe embed with foreignRefs — Stremio Desktop/Web can render it in an iframe.
//   • When the file host returns a direct .mkv/.mp4 URL (HTTP 200 with video content-type),
//     we return it as a direct playable stream.
//
// DEPENDENCIES:
//   • Node 18+ (for native fetch)
//   • No npm packages, no PenguPlay, no auth.
// ============================================================================

'use strict';

const TMDB_API_KEY = '8476a7ab80ad76f0936744df0430e67c';   // public TMDB key
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';
const CMZ_BASE = 'https://c1.cloudmoviez.shop';
const MAX_STREAMS = 50;

// ─── HTTP helpers ────────────────────────────────────────────────────────────
async function fetchHtml(url, opts = {}) {
  const res = await fetch(url, {
    headers: { 'User-Agent': UA, 'Accept': 'text/html,application/xhtml+xml,*/*;q=0.8', 'Accept-Language': 'en-US,en;q=0.9', ...opts.headers },
    signal: AbortSignal.timeout(opts.timeout || 20000),
    ...opts,
  });
  return res;
}

async function fetchJson(url, opts = {}) {
  const res = await fetch(url, {
    headers: { 'User-Agent': UA, 'Accept': 'application/json', ...opts.headers },
    signal: AbortSignal.timeout(opts.timeout || 15000),
    ...opts,
  });
  return res.json();
}

// ─── TMDB metadata ──────────────────────────────────────────────────────────
async function getTMDBInfo(id, type) {
  const isMovie = type !== 'tv' && type !== 'series';
  const mediaType = isMovie ? 'movie' : 'tv';
  let tmdbId = id, imdbId = id, meta = null;

  if (/^tt\d+$/.test(String(id))) {
    try {
      const d = await fetchJson(`https://api.themoviedb.org/3/find/${id}?api_key=${TMDB_API_KEY}&external_source=imdb_id`);
      const r = isMovie ? d.movie_results : d.tv_results;
      if (r && r[0]) {
        tmdbId = String(r[0].id);
        meta = r[0];
      }
    } catch (e) {}
  } else if (/^\d+$/.test(String(id))) {
    tmdbId = String(id);
  }

  try {
    const full = await fetchJson(`https://api.themoviedb.org/3/${mediaType}/${tmdbId}?api_key=${TMDB_API_KEY}&append_to_response=credits,external_ids,videos,images,similar,recommendations,content_ratings,release_dates`);
    meta = { ...meta, ...full };
  } catch (e) {}

  if (!meta) return { title: id, year: '', tmdbId, imdbId, isMovie };

  if (meta.external_ids?.imdb_id) imdbId = meta.external_ids.imdb_id;
  const title = isMovie ? meta.title : meta.name;
  const year = ((meta.first_air_date || meta.release_date || '') + '').split('-')[0];
  return {
    title, year, tmdbId, imdbId, isMovie,
    genres: meta.genres?.map(g => g.name) || [],
    runtime: meta.runtime || meta.episode_run_time?.[0] || null,
    overview: meta.overview || '',
    posterPath: meta.poster_path || '',
    backdropPath: meta.backdrop_path || '',
    voteAverage: meta.vote_average || 0,
    cast: meta.credits?.cast?.slice(0, 10).map(c => c.name) || [],
    director: (meta.credits?.crew || []).find(c => c.job === 'Director')?.name || '',
    tagline: meta.tagline || '',
    status: meta.status || '',
    originalLanguage: meta.original_language || '',
    productionCountries: meta.production_countries?.map(c => c.name) || [],
    animeGenres: (meta.genres || []).some(g => g.id === 16),
    videos: meta.videos?.results?.filter(v => v.site === 'YouTube').slice(0, 3).map(v => v.key) || [],
    similar: meta.similar?.results?.slice(0, 5).map(s => ({ id: s.id, title: s.title || s.name, poster: s.poster_path })) || [],
    recommendations: meta.recommendations?.results?.slice(0, 5).map(s => ({ id: s.id, title: s.title || s.name, poster: s.poster_path })) || [],
  };
}

// ─── Search CloudMovieZ for a title (via WP ?s= query) ──────────────────────
async function searchCMZ(title, year, type) {
  const isMovie = type !== 'tv' && type !== 'series';
  const targetPath = isMovie ? '/movies/' : '/tvshows/';
  // Try multiple search variants: title only, then title + year if no exact match
  const queries = [title, `${title} ${year}`];
  for (const query of queries) {
    const searchUrl = `${CMZ_BASE}/?s=${encodeURIComponent(query)}`;
    console.log('[CMZ] Search:', searchUrl, '(targeting', targetPath + ')');
    try {
      const res = await fetchHtml(searchUrl);
      const html = await res.text();
      // Find all URLs of the appropriate type only
      const matches = [...html.matchAll(new RegExp(`href="(https://c1\\.cloudmoviez\\.shop${targetPath.replace('/', '\\/')}[^"\\/]+/?)"`, 'g'))].map(m => m[1]);
      if (matches.length === 0) continue;
      
      // Normalize the title for slug comparison
      const titleNorm = title.toLowerCase().replace(/[^a-z0-9]/g, '');
      // Score each match: prefer slug that contains the normalized title
      const scored = matches.map(u => {
        const slug = u.toLowerCase().replace(/\/$/, '').split('/').pop().replace(/[^a-z0-9]/g, '');
        let score = 0;
        if (slug === titleNorm) score = 100; // exact
        else if (slug.includes(titleNorm)) score = 80; // starts/contains
        else if (titleNorm.includes(slug) && slug.length > 3) score = 60;
        else {
          // word-by-word match
          const titleWords = title.toLowerCase().split(/[^a-z0-9]+/).filter(w => w.length > 2);
          const matched = titleWords.filter(w => slug.includes(w.replace(/[^a-z0-9]/g, '')));
          score = matched.length * 10;
        }
        if (year && !slug.includes(year)) score -= 5;
        return { url: u, slug, score };
      }).sort((a, b) => b.score - a.score);
      
      console.log('[CMZ] Top 3 candidates:', scored.slice(0, 3).map(s => `${s.slug}=${s.score}`));
      if (scored[0].score >= 60) return scored[0].url;
    } catch (e) { console.log('[CMZ] Search err:', e.message); }
  }
  return null;
}

// ─── Parse movie page — extract links_table rows ────────────────────────────
async function parseMoviePage(url) {
  console.log('[CMZ] Loading movie page:', url);
  const res = await fetchHtml(url);
  const html = await res.text();
  
  // Extract post ID from data-id attribute
  const postIdMatch = html.match(/data-id="(\d+)"/);
  const postId = postIdMatch ? postIdMatch[1] : null;
  
  // Find links_table section
  const linksTableMatch = html.match(/<div class='links_table'>[\s\S]*?<\/table>/);
  if (!linksTableMatch) {
    console.log('[CMZ] No links_table found — page may be empty');
    return { postId, links: [] };
  }
  
  const tableHtml = linksTableMatch[0];
  // Parse each <tr id='link-NUMBER'>...</tr>
  const rowMatches = [...tableHtml.matchAll(/<tr id='link-(\d+)'>([\s\S]*?)<\/tr>/g)];
  const links = [];
  for (const rowMatch of rowMatches) {
    const linkId = rowMatch[1];
    const content = rowMatch[2];
    // Extract href (stops at first ')
    const hrefMatch = content.match(/href='([^']+)'/);
    // Extract quality from <strong class='quality'>...</strong>
    const qualMatch = content.match(/class='quality'>([^<]+)<\/strong>/);
    // Extract host from favicon domain= (stop at ' or & or ;)
    const hostMatch = content.match(/domain=([^'&;<>\s]+)/);
    // Parse all <td> contents
    const tds = [...content.matchAll(/<td[^>]*>([\s\S]*?)<\/td>/g)].map(m => m[1].replace(/<[^>]+>/g, '').trim());
    const lang = tds[2] || '';
    const size = tds[3] || '';
    if (hrefMatch && qualMatch) {
      links.push({
        linkId,
        href: hrefMatch[1],
        quality: qualMatch[1].trim(),
        language: lang,
        size,
        host: hostMatch ? hostMatch[1] : '',
      });
    }
  }
  console.log('[CMZ] Parsed', links.length, 'movie download links');
  return { postId, links };
}

// ─── Parse TV page — extract cmz-row-card items ─────────────────────────────
async function parseTVPage(url, season, episode) {
  console.log('[CMZ] Loading TV page:', url);
  const res = await fetchHtml(url);
  const html = await res.text();
  
  const postIdMatch = html.match(/data-id="(\d+)"/);
  const postId = postIdMatch ? postIdMatch[1] : null;
  
  // Find all cmz-row-card items
  const itemMatches = [...html.matchAll(/<div class="cmz-row-card cmz-filter-item"[^>]*>([\s\S]*?)(?=<div class="cmz-row-card|<div class="cmz-accordion|$)/g)];
  console.log('[CMZ] Found', itemMatches.length, 'TV items on page');
  
  const items = [];
  for (const itemMatch of itemMatches) {
    const content = itemMatch[1];
    // Extract title (filename)
    const titleMatch = content.match(/<div class="cmz-row-title">([^<]+)<\/div>/);
    // Extract download URL with ?file=base64
    const fileUrlMatch = content.match(/href="(https:\/\/c1\.cloudmoviez\.shop\/\?file=[^"]+)"/);
    if (!titleMatch || !fileUrlMatch) continue;
    
    const filename = titleMatch[1].trim();
    const fileUrl = fileUrlMatch[1];
    // Decode the base64 ?file= param
    const b64Match = fileUrl.match(/\?file=([^&]+)/);
    if (!b64Match) continue;
    let decoded = null;
    try {
      decoded = JSON.parse(Buffer.from(b64Match[1], 'base64').toString('utf-8'));
    } catch (e) { continue; }
    
    // Parse season/episode from filename
    const sMatch = filename.match(/S(\d{1,2})E(\d{1,2})/i);
    const sNumMatch = filename.match(/\.S(\d{1,2})\./i);
    const itemSeason = sMatch ? parseInt(sMatch[1], 10) : (sNumMatch ? parseInt(sNumMatch[1], 10) : null);
    const itemEpisode = sMatch ? parseInt(sMatch[2], 10) : null;
    
    // Parse quality from filename
    let quality = 'unknown';
    if (/2160p|4k|uhd/i.test(filename)) quality = '4K';
    else if (/1440p/i.test(filename)) quality = '1440p';
    else if (/1080p/i.test(filename)) quality = '1080p';
    else if (/720p/i.test(filename)) quality = '720p';
    else if (/480p/i.test(filename)) quality = '480p';
    
    // Detect HDR
    const hdr = /\.HDR\.|HDR10|HDR10\+|DV\b|Dolby.Vision/i.test(filename) ? ' HDR' : '';
    
    // Detect multi-audio
    const dualAudio = /dual.audio/i.test(filename);
    
    items.push({
      filename,
      fileUrl,
      streamUrl: decoded.url,
      postId: decoded.post,
      season: itemSeason,
      episode: itemEpisode,
      quality: quality + hdr,
      dualAudio,
      raw: decoded,
    });
  }
  
  // Filter by season/episode if provided
  let filtered = items;
  if (season && episode) {
    const sNum = parseInt(season, 10);
    const eNum = parseInt(episode, 10);
    filtered = items.filter(i => i.season === sNum && i.episode === eNum);
    if (filtered.length === 0) {
      // Fall back to season-only filter
      filtered = items.filter(i => i.season === sNum);
    }
  } else if (season) {
    const sNum = parseInt(season, 10);
    filtered = items.filter(i => i.season === sNum);
  }
  console.log('[CMZ] Filtered to', filtered.length, 'items for S' + season + 'E' + episode);
  return { postId, items: filtered };
}

// ─── Follow /links/{slug}/ redirect to find actual stream URL ──────────────
// The /links/ page embeds the actual URL in a "continue-btn" <a> tag.
// Pattern: <a class="continue-btn" href="https://drive.google.com/uc?...">Continue</a>
// OR for some movies: <a class="continue-btn" href="https://vcloud.zip/...">
// OR for some movies: <a class="continue-btn" href="https://c1.cloudmoviez.shop/file/{slug}">
//   (the /file/ URL is a fallback page when the original stream was removed)
async function resolveLinkRedirect(linkUrl) {
  try {
    const res = await fetchHtml(linkUrl);
    const html = await res.text();
    
    // Extract the continue-btn href
    const continueBtnMatch = html.match(/class=["']continue-btn["'][^>]*href=["']([^"']+)["']/) ||
                             html.match(/href=["']([^"']+)["'][^>]*class=["'][^"']*continue-btn/);
    let targetUrl = continueBtnMatch ? continueBtnMatch[1].replace(/&amp;/g, '&') : null;
    
    if (!targetUrl) {
      // Fallback: look for any href with a known stream host
      const streamUrlMatch = html.match(/href=["'](https:\/\/(?:vcloud\.zip|gdflix\.[a-z]{2,}|new\d+\.gdflix\.io|drive\.google\.com\/uc\?export=download|streamtape|doodstream|mixdrop|filesgram|hubcloud|gdtot|fastshare|filemoon|vizcloud|vaplayer|multiup|mega\.nz)[^"']*)["']/);
      if (streamUrlMatch) targetUrl = streamUrlMatch[1].replace(/&amp;/g, '&');
    }
    
    if (!targetUrl) return null;
    
    // If target is a Google Drive direct download URL, convert to direct playable URL
    if (targetUrl.includes('drive.google.com/uc?export=download')) {
      const idMatch = targetUrl.match(/[?&]id=([^&]+)/);
      if (idMatch) {
        return `https://drive.usercontent.google.com/download?id=${idMatch[1]}&export=download&confirm=t`;
      }
    }
    
    // If target is a /file/{slug}/ URL on cloudmoviez (fallback when stream was removed), follow it
    if (/^https:\/\/c1\.cloudmoviez\.shop\/file\//.test(targetUrl)) {
      // The /file/ page is usually a 404 fallback to homepage — no real stream URL
      return null;
    }
    
    return targetUrl;
  } catch (e) { return null; }
}

// ─── Probe URL for playability (HTTP 200/206 + video content-type) ─────────
async function probeUrl(url, headers = {}) {
  try {
    const u = new URL(url);
    const lib = u.protocol === 'http:' ? require('http') : require('https');
    return await new Promise(resolve => {
      let depth = 0;
      const follow = (curUrl) => {
        if (depth++ > 3) return resolve({ playable: false, status: 0, error: 'redirects exceeded' });
        const cu = new URL(curUrl);
        const lib2 = cu.protocol === 'http:' ? require('http') : require('https');
        const req = lib2.request({
          hostname: cu.hostname,
          path: cu.pathname + cu.search,
          method: 'GET',
          headers: {
            'User-Agent': UA,
            'Range': 'bytes=0-1',
            'Accept': '*/*',
            'Referer': headers.Referer || CMZ_BASE + '/',
            ...headers,
          }
        }, (res) => {
          res.destroy();
          const ct = res.headers['content-type'] || '';
          const cl = res.headers['content-length'];
          const loc = res.headers.location;
          if (res.statusCode >= 300 && res.statusCode < 400 && loc) {
            return follow(new URL(loc, curUrl).href);
          }
          const ok = (res.statusCode === 200 || res.statusCode === 206) &&
                    /video|octet-stream|mpegurl/i.test(ct);
          resolve({
            status: res.statusCode,
            contentType: ct,
            contentLength: cl,
            playable: ok,
          });
        });
        req.on('error', (e) => resolve({ playable: false, status: 0, error: e.message }));
        req.on('timeout', () => { req.destroy(); resolve({ playable: false, status: 0, timeout: true }); });
        req.setTimeout(8000); req.end();
      };
      follow(curUrl = u.href);
    });
  } catch (e) { return { playable: false, error: e.message }; }
}

// ─── Main entry: getStreams(id, type, season, episode) ─────────────────────
async function getStreams(id, type, season, episode) {
  console.log('[CMZ] Request: id=' + id + ' type=' + type + (season ? ' S' + season + 'E' + episode : ''));

  // 1. Enriched metadata via TMDB
  const info = await getTMDBInfo(id, type);
  console.log('[CMZ] Title:', info.title, '(' + info.year + ') tmdb=' + info.tmdbId + ' imdb=' + info.imdbId + (info.animeGenres ? ' [ANIME]' : ''));

  // 2. Search CloudMovieZ for the title
  const pageUrl = await searchCMZ(info.title, info.year, type);
  if (!pageUrl) {
    console.log('[CMZ] No matching title found on CloudMovieZ');
    return [];
  }
  console.log('[CMZ] Found page:', pageUrl);

  const isMovie = info.isMovie;
  const streams = [];

  if (isMovie) {
    // 3a. Parse movie page links
    const { postId, links } = await parseMoviePage(pageUrl);
    if (links.length === 0) {
      console.log('[CMZ] No download links on movie page');
      return [];
    }

    // 4a. For each link, resolve redirect and probe playability
    for (const link of links) {
      console.log('[CMZ] Resolving link ' + link.linkId + ' (' + link.quality + ', ' + link.language + ')');
      const streamUrl = await resolveLinkRedirect(link.href);
      if (!streamUrl) {
        console.log('[CMZ] Could not resolve redirect for ' + link.href);
        continue;
      }
      console.log('[CMZ] Resolved to:', streamUrl);
      // Probe the stream URL
      const probe = await probeUrl(streamUrl, { Referer: CMZ_BASE + '/' });
      // Parse quality label
      let qLabel = link.quality.split(/\s+/)[0]; // e.g. "1080p x264" → "1080p"
      if (/4k|2160|uhd/i.test(qLabel)) qLabel = '4K';
      // Language label
      const langLabel = link.language || 'Unknown';
      const audioTag = /dual.audio/i.test(langLabel) ? ' [DUAL-AUDIO]' : ` [${langLabel}]`;
      const animeTag = info.animeGenres ? ' [SUB+DUB]' : '';

      streams.push({
        name: `[CM] CloudMovieZ ${qLabel} | ${link.host}${audioTag}${animeTag}`,
        title: info.title + ` (${info.year}) [${link.host} ${link.quality}]${audioTag}${animeTag}` + (link.size ? ` ${link.size}` : ''),
        url: streamUrl,
        quality: qLabel.toLowerCase(),
        type: probe.playable ? 'video/mp4' : 'text/html',
        headers: { 'User-Agent': UA, 'Referer:': CMZ_BASE + '/' },
        provider: 'CloudMovieZ',
        behaviorHints: {
          notWebReady: !probe.playable,
          foreignRefs: probe.playable ? undefined : [{ url: streamUrl }],
          bingeGroup: `cmz|${postId || 'movie'}|${qLabel}|${link.host}`,
        },
        _probeStatus: probe.status,
        _probePlayable: probe.playable,
      });
    }
  } else {
    // 3b. Parse TV page items
    const { postId, items } = await parseTVPage(pageUrl, season, episode);
    if (items.length === 0) {
      console.log('[CMZ] No matching episodes on TV page');
      return [];
    }

    // 4b. For each item, use the gdflix.dev URL directly + probe
    for (const item of items) {
      console.log('[CMZ] Item:', item.filename, '|', item.quality, '| stream:', item.streamUrl);
      // The streamUrl from base64 is gdflix.dev/file/{id} — probe it
      const probe = await probeUrl(item.streamUrl, { Referer: CMZ_BASE + '/' });
      // Visit gdflix page to find direct download URL (cloud/cflare paths)
      let directStreamUrl = item.streamUrl;
      try {
        const gdRes = await fetchHtml(item.streamUrl, { headers: { Referer: CMZ_BASE + '/' } });
        const gdHtml = await gdRes.text();
        const dlMatch = gdHtml.match(/href="(https:\/\/[^"]*(?:cflare|cloud)\/[^"]+)"/) ||
                       gdHtml.match(/href="(\/(?:cflare|cloud)\/[^"]+)"/);
        if (dlMatch) {
          directStreamUrl = dlMatch[1].startsWith('/') ? `https://new4.gdflix.io${dlMatch[1]}` : dlMatch[1];
        }
      } catch (e) {}

      const qLabel = item.quality.replace(/\s+HDR$/, '') || '4K';
      const hdrTag = /HDR/.test(item.quality) ? ' [HDR]' : '';
      const audioTag = item.dualAudio ? ' [DUAL-AUDIO]' : '';
      const animeTag = info.animeGenres ? ' [SUB+DUB]' : '';

      streams.push({
        name: `[CM] CloudMovieZ ${qLabel}${hdrTag} | GDFlix${audioTag}${animeTag}`,
        title: info.title + ` S${item.season || season || 1}E${item.episode || episode || 1} (${info.year}) [GDFlix ${item.quality}]${audioTag}${animeTag}`,
        url: directStreamUrl,
        quality: qLabel.toLowerCase().replace('4k', '4k'),
        type: probe.playable ? 'video/mp4' : 'text/html',
        headers: { 'User-Agent': UA, 'Referer:': 'https://new4.gdflix.io/' },
        provider: 'CloudMovieZ',
        behaviorHints: {
          notWebReady: !probe.playable,
          foreignRefs: probe.playable ? undefined : [{ url: directStreamUrl }],
          bingeGroup: `cmz|${postId || 'tv'}|${qLabel}|gdflix|S${item.season || season}E${item.episode || episode}`,
        },
        _probeStatus: probe.status,
        _probePlayable: probe.playable,
      });
    }
  }

  // Sort: direct-playable first, then by quality desc
  streams.sort((a, b) => {
    if (a._probePlayable !== b._probePlayable) return a._probePlayable ? -1 : 1;
    const qRank = { '4k': 4, '2160p': 4, '1440p': 3, '1080p': 2, '720p': 1, '480p': 0 };
    return (qRank[b.quality] || -1) - (qRank[a.quality] || -1);
  });

  // Attach enriched metadata
  streams.meta = {
    id: info.imdbId || info.tmdbId,
    imdbId: info.imdbId,
    tmdbId: info.tmdbId,
    type: info.isMovie ? 'movie' : 'series',
    name: info.title,
    year: info.year,
    genres: info.genres,
    cast: info.cast,
    director: info.director,
    tag: info.tagline,
    tagline: info.tagline,
    overview: info.overview,
    poster: info.posterPath ? `https://image.tmdb.org/t/p/w500${info.posterPath}` : '',
    backdrop: info.backdropPath ? `https://image.tmdb.org/t/p/original${info.backdropPath}` : '',
    imdbRating: info.voteAverage ? info.voteAverage.toFixed(1) : '',
    runtime: info.runtime ? `${info.runtime}min` : '',
    status: info.status,
    originalLanguage: info.originalLanguage,
    productionCountries: info.productionCountries,
    isAnime: !!info.animeGenres,
    trailers: info.videos || [],
    similar: info.similar || [],
    recommendations: info.recommendations || [],
  };

  // Cap streams + cleanup internal fields
  const capped = streams.slice(0, MAX_STREAMS).map(s => {
    const { _probeStatus, _probePlayable, ...clean } = s;
    return clean;
  });
  if (streams.meta) capped.meta = streams.meta;
  console.log('[CMZ] Returning', capped.length, 'streams');
  return capped;
}

// ─── CLI ────────────────────────────────────────────────────────────────────
if (require.main === module) {
  (async () => {
    const args = process.argv.slice(2);
    if (!args[0]) {
      console.log('Usage: node cloudmoviez.js <imdbId|tmdbId> <movie|tv> [season] [episode]');
      process.exit(0);
    }
    const streams = await getStreams(args[0], args[1] || 'movie', args[2], args[3]);
    console.log('\n' + streams.length + ' streams:');
    streams.forEach((s, i) => {
      console.log('[' + i + '] ' + s.name + ' (' + s.quality + ') playable=' + (s.behaviorHints?.foreignRefs ? 'iframe' : 'direct'));
      console.log('    ' + s.url);
    });
    if (streams.meta) {
      console.log('\nMeta:');
      console.log('  Title:    ' + streams.meta.name + ' (' + streams.meta.year + ')');
      console.log('  Genres:   ' + (streams.meta.genres || []).join(', '));
      console.log('  Director: ' + streams.meta.director);
      console.log('  Cast:     ' + (streams.meta.cast || []).slice(0, 5).join(', '));
      console.log('  Rating:   ' + streams.meta.imdbRating + ' (TMDB)');
      console.log('  Anime:    ' + streams.meta.isAnime);
      console.log('  Poster:   ' + streams.meta.poster);
    }
  })();
}

module.exports = { getStreams, searchCMZ, parseMoviePage, parseTVPage, resolveLinkRedirect, getTMDBInfo };
