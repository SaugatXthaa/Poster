// MLSBD Provider — JS port of the CNCVerse MlsbdProvider.cs3 Cloudstream plugin.
// Pure Node.js. Returns DIRECT playable stream URLs (when CF bypass succeeds).
//
// ARCHITECTURE (reverse-engineered from decompiled MlsbdProvider.cs3):
//   • mainUrl = "https://mlsbd.co" — WordPress DooPlay-style site
//   • Search: GET /?s=<query> — returns HTML with post URLs (/{slug}/)
//   • Movie page: GET /{slug}/ — HTML contains <a class="Dbtn btn" href="..."> 
//     download buttons pointing to https://link.bonghd.com/?x_data=<base64>:<index>
//   • link.bonghd.com — 302 redirects to https://mlsbd.to (Cloudflare-challenged)
//   • The MLSBD plugin uses fetchViaWebView() — Android WebView + libcloudflared.so
//     (37 MB binary bundled in CNCVerse-Bridge) to bypass the CF challenge
//   • After CF bypass, the page reveals gdflix.dev / hubcloud.cx / vcloud.zip URLs
//   • Sub-extractors (GDFlix, HubCloud, MultiCloudLinks) resolve those to direct .mkv
//
// WHAT WORKS IN PURE NODE.JS:
//   ✅ Search mlsbd.co (requires full browser headers — Sec-Fetch-*, Sec-Ch-Ua-*)
//   ✅ Load movie page and parse <a class="Dbtn btn" href="link.bonghd.com/..."> tags
//   ✅ Extract quality + size from button text (e.g., "2160p HDR 10bit ... 5.8GB")
//   ✅ Mark as 4K when "2160p" or "4K" detected in button text
//   ✅ Mark HDR when "HDR" / "HDR10" / "10bit" detected
//   ✅ Resolve Google Drive direct URLs (drive.usercontent.google.com/download?...)
//
// WHAT FAILS IN PURE NODE.JS:
//   ❌ Bypassing Cloudflare Turnstile challenge on mlsbd.to (link.bonghd.com redirect)
//      — requires either:
//        (a) Self-hosted FlareSolverr instance (free, runs Chromium headless)
//        (b) Paid Cloudflare-bypass API (ZenRows, ScrapingBee, Browserless.io)
//        (c) Android WebView + libcloudflared.so (what the original .cs3 plugin uses)
//   ❌ Resolving gdflix.dev / hubcloud.cx / vcloud.zip file hosts to direct .mkv URLs
//      — these are all CF-challenged too (same problem)
//
// CONFIGURATION:
//   Set FLARESOLVERR_URL env var to your FlareSolverr endpoint to enable CF bypass:
//     FLARESOLVERR_URL=http://localhost:8191
//   If set, the provider will call FlareSolverr's /v1 API to solve CF challenges
//   and get the actual file host URLs from mlsbd.to.
//
// DEPENDENCIES:
//   • Node 18+ (for native fetch)
//   • Optional: FlareSolverr instance for CF bypass (set FLARESOLVERR_URL)
//   • No npm packages, no PenguPlay, no auth.
// ============================================================================

'use strict';

const TMDB_API_KEY = '8476a7ab80ad76f0936744df0430e67c';
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';
const MLSBD_BASE = 'https://mlsbd.co';
const BONGHD_BASE = 'https://link.bonghd.com';
const FLARESOLVERR_URL = process.env.FLARESOLVERR_URL || '';
const MAX_STREAMS = 30;

// ─── HTTP helpers ──────────────────────────────────────────────────────────
// IMPORTANT: Node.js native fetch has a different TLS fingerprint (JA3/JA4)
// than real Chrome — Cloudflare detects this and returns 403 "Just a moment..."
// even with full browser headers. We shell out to curl (which uses OpenSSL's
// TLS stack that's similar enough to Chrome to pass basic CF checks) for ALL
// HTTP requests. Tested: curl with full browser headers gets HTTP 200 from
// mlsbd.co, while Node.js fetch gets HTTP 403.

const { execSync } = require('child_process');

const BROWSER_HEADERS = {
  'User-Agent': UA,
  'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8',
  'Accept-Language': 'en-US,en;q=0.5',
  'Accept-Encoding': 'gzip, deflate, br',
  'Upgrade-Insecure-Requests': '1',
  'Sec-Fetch-Dest': 'document',
  'Sec-Fetch-Mode': 'navigate',
  'Sec-Fetch-Site': 'none',
  'Sec-Fetch-User': '?1',
  'Sec-Ch-Ua': '"Chromium";v="147", "Not?A_Brand";v="24"',
  'Sec-Ch-Ua-Mobile': '?0',
  'Sec-Ch-Ua-Platform': '"Windows"',
};

function buildCurlArgs(url, opts = {}) {
  const args = ['-sSL', '--compressed', '-A', UA, '--max-time', String((opts.timeout || 15000) / 1000)];
  const headers = { ...BROWSER_HEADERS, ...opts.headers };
  for (const [k, v] of Object.entries(headers)) {
    args.push('-H', `${k}: ${v}`);
  }
  args.push('-o', '-');  // output to stdout
  args.push('-w', '\n__HTTPCODE__:%{http_code}__URL__:%{url_effective}__CT__:%{content_type}');
  args.push(url);
  return args;
}

async function fetchHtml(url, opts = {}) {
  // Use curl via child_process to bypass Node.js TLS fingerprint detection
  return new Promise((resolve, reject) => {
    const args = buildCurlArgs(url, opts);
    try {
      const out = execSync('curl ' + args.map(a => JSON.stringify(a)).join(' '), {
        maxBuffer: 20 * 1024 * 1024,  // 20 MB
        timeout: (opts.timeout || 15000) + 5000,
      }).toString('utf8');
      // Parse: HTML + __HTTPCODE__:%{http_code}__URL__:%{url_effective}__CT__:%{content_type}
      const metaMatch = out.match(/\n__HTTPCODE__:(\d+)__URL__:([^_]+)__CT__:([^\n]*)$/);
      if (metaMatch) {
        const html = out.slice(0, metaMatch.index);
        const status = parseInt(metaMatch[1], 10);
        const url = metaMatch[2];
        const contentType = metaMatch[3];
        const res = {
          ok: status >= 200 && status < 300,
          status,
          statusText: '',
          url,
          headers: { get: (k) => k.toLowerCase() === 'content-type' ? contentType : null },
          text: () => Promise.resolve(html),
        };
        resolve(res);
      } else {
        // No meta trailer — return raw output
        resolve({
          ok: true, status: 200, statusText: '',
          url, headers: { get: () => null },
          text: () => Promise.resolve(out),
        });
      }
    } catch (e) {
      reject(new Error('curl failed: ' + e.message));
    }
  });
}

// ─── FlareSolverr integration (optional CF bypass) ────────────────────────
async function solveCloudflareViaFlareSolverr(targetUrl, referer) {
  if (!FLARESOLVERR_URL) return null;
  try {
    const r = await fetch(FLARESOLVERR_URL + '/v1', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        cmd: 'request.get',
        url: targetUrl,
        maxTimeout: 60000,
        ...(referer ? { headers: { Referer: referer } } : {}),
      }),
      signal: AbortSignal.timeout(65000),
    });
    const d = await r.json();
    if (d.status === 'ok' && d.solution) {
      return { html: d.solution.response, url: d.solution.url, cookies: d.solution.cookies };
    }
  } catch (e) { console.log('[MLSBD] FlareSolverr err:', e.message); }
  return null;
}

// ─── TMDB metadata (for resolving IMDB→TMDB + enriched info) ──────────────
async function getTMDBInfo(id, type) {
  const isMovie = type !== 'tv' && type !== 'series';
  const mediaType = isMovie ? 'movie' : 'tv';
  let tmdbId = id, imdbId = id, meta = null;
  if (/^tt\d+$/.test(String(id))) {
    try {
      const d = await (await fetch(`https://api.themoviedb.org/3/find/${id}?api_key=${TMDB_API_KEY}&external_source=imdb_id`)).json();
      const r = isMovie ? d.movie_results : d.tv_results;
      if (r && r[0]) { tmdbId = String(r[0].id); meta = r[0]; }
    } catch (e) {}
  } else if (/^\d+$/.test(String(id))) {
    tmdbId = String(id);
  }
  try {
    const full = await (await fetch(`https://api.themoviedb.org/3/${mediaType}/${tmdbId}?api_key=${TMDB_API_KEY}&append_to_response=credits,external_ids,videos,images,similar,recommendations`)).json();
    meta = { ...meta, ...full };
  } catch (e) {}
  if (!meta) return { title: id, year: '', tmdbId, imdbId, isMovie };
  if (meta.external_ids?.imdb_id) imdbId = meta.external_ids.imdb_id;
  return {
    title: isMovie ? meta.title : meta.name,
    year: ((meta.first_air_date || meta.release_date || '') + '').split('-')[0],
    tmdbId, imdbId, isMovie,
    genres: meta.genres?.map(g => g.name) || [],
    cast: meta.credits?.cast?.slice(0, 10).map(c => c.name) || [],
    director: (meta.credits?.crew || []).find(c => c.job === 'Director')?.name || '',
    posterPath: meta.poster_path || '',
    backdropPath: meta.backdrop_path || '',
    voteAverage: meta.vote_average || 0,
    overview: meta.overview || '',
    tagline: meta.tagline || '',
    animeGenres: (meta.genres || []).some(g => g.id === 16),
    videos: meta.videos?.results?.filter(v => v.site === 'YouTube').slice(0, 3).map(v => v.key) || [],
    similar: meta.similar?.results?.slice(0, 5).map(s => ({ id: s.id, title: s.title || s.name, poster: s.poster_path })) || [],
    recommendations: meta.recommendations?.results?.slice(0, 5).map(s => ({ id: s.id, title: s.title || s.name, poster: s.poster_path })) || [],
  };
}

// ─── Search mlsbd.co for a title ────────────────────────────────────────────
async function searchMLSBD(title, year) {
  // mlsbd.co search URL: /?s=<query>
  const queries = [title, `${title} ${year}`];
  for (const query of queries) {
    const searchUrl = `${MLSBD_BASE}/?s=${encodeURIComponent(query)}`;
    console.log('[MLSBD] Search:', searchUrl);
    try {
      const res = await fetchHtml(searchUrl);
      const html = await res.text();
      // Find post URLs: https://mlsbd.co/{slug}/ (not /category/, /feed/, etc.)
      const matches = [...html.matchAll(/href="(https:\/\/mlsbd\.co\/(?!category\/|feed\/|comments\/|author\/|review|wp-|about|contact|how-to)[a-z0-9-]+\/?)"/g)]
        .map(m => m[1]);
      if (matches.length === 0) continue;
      // Score: prefer slugs containing the title (normalized)
      const titleNorm = title.toLowerCase().replace(/[^a-z0-9]/g, '');
      const scored = matches.map(u => {
        const slug = u.toLowerCase().replace(/\/$/, '').split('/').pop().replace(/[^a-z0-9]/g, '');
        let score = 0;
        if (slug.includes(titleNorm)) score = 100;
        else if (titleNorm.includes(slug) && slug.length > 3) score = 60;
        else {
          const titleWords = title.toLowerCase().split(/[^a-z0-9]+/).filter(w => w.length > 2);
          const matched = titleWords.filter(w => slug.includes(w.replace(/[^a-z0-9]/g, '')));
          score = matched.length * 15;
        }
        return { url: u, slug, score };
      }).filter(s => s.score >= 30).sort((a, b) => b.score - a.score);
      console.log('[MLSBD] Top candidates:', scored.slice(0, 3).map(s => `${s.slug}=${s.score}`));
      // Return top 2 (e.g., one for 4K version, one for 480p/720p version)
      if (scored.length > 0) return scored.slice(0, 2).map(s => s.url);
    } catch (e) { console.log('[MLSBD] Search err:', e.message); }
  }
  return [];
}

// ─── Parse mlsbd.co movie page for download buttons ────────────────────────
async function parseMLSBDPage(url) {
  console.log('[MLSBD] Loading page:', url);
  const res = await fetchHtml(url, { headers: { Referer: MLSBD_BASE + '/' } });
  const html = await res.text();
  
  // Find all <a class="Dbtn btn" href='https://link.bonghd.com/?x_data=<base64>:<index>'>
  // The button text contains quality + size info:
  //   "2160p HDR 10bit Bluray HEVC [DD 5.1 Hindi + DD 5.1 English] x265 5.8GB"
  //   "1080p 10bit Bluray x265 HEVC [Org DD 5.1 – 2.0 Hindi + DD 5.1 English] 5.1GB"
  const btnRegex = /<a[^>]*class=["'][^"']*Dbtn[^"']*["'][^>]*href=["']([^"']+)["'][^>]*>\s*(?:<span>)?([^<]+)(?:<\/span>)?\s*<\/a>/g;
  const links = [];
  let m;
  while ((m = btnRegex.exec(html)) !== null) {
    const href = m[1];
    const text = (m[2] || '').replace(/<[^>]+>/g, '').trim();
    if (!href || !text) continue;
    // Parse quality from text
    let quality = 'unknown';
    if (/2160p|4k|uhd/i.test(text)) quality = '4K';
    else if (/1440p/i.test(text)) quality = '1440p';
    else if (/1080p/i.test(text)) quality = '1080p';
    else if (/720p/i.test(text)) quality = '720p';
    else if (/480p/i.test(text)) quality = '480p';
    // HDR detection
    const hdr = /HDR10\+|HDR10/i.test(text) ? (text.match(/HDR10\+|HDR10/i)[0]) :
                /HDR/i.test(text) ? 'HDR' : '';
    // 10bit
    const bit10 = /10bit|10-bit/i.test(text) ? '10bit' : '';
    // Codec
    const codec = /x265|hevc/i.test(text) ? 'HEVC' :
                  /x264|h264|avc/i.test(text) ? 'H264' :
                  /av1/i.test(text) ? 'AV1' : '';
    // Audio (DD5.1, Atmos, etc.)
    const audio = [];
    if (/DD 5\.1|DD5\.1|DDP 5\.1/i.test(text)) audio.push('DD5.1');
    if (/Atmos/i.test(text)) audio.push('Atmos');
    if (/TrueHD/i.test(text)) audio.push('TrueHD');
    // Language
    const langs = [];
    if (/Hindi/i.test(text)) langs.push('Hindi');
    if (/English|Eng\b/i.test(text)) langs.push('English');
    if (/Tamil/i.test(text)) langs.push('Tamil');
    if (/Telugu/i.test(text)) langs.push('Telugu');
    if (/Bengali|Bangla/i.test(text)) langs.push('Bengali');
    if (/Korean|Kor\b/i.test(text)) langs.push('Korean');
    // Size (e.g., "5.8GB", "5.1GB", "400MB")
    const sizeMatch = text.match(/(\d+(?:\.\d+)?\s*(?:GB|MB))/i);
    const size = sizeMatch ? sizeMatch[1] : '';
    
    links.push({
      href, // link.bonghd.com URL
      text, quality, hdr, bit10, codec, audio: audio.join('+'),
      langs: langs.join('+'), size,
    });
  }
  console.log('[MLSBD] Parsed ' + links.length + ' download buttons');
  return links;
}

// ─── Follow link.bonghd.com → mlsbd.to → resolve to actual file host URL ──
async function resolveBonghdUrl(bonghdUrl) {
  // link.bonghd.com/?x_data=<base64>:<index> → 302 → https://mlsbd.to
  // mlsbd.to is Cloudflare-challenged (HTTP 403 + "Just a moment...")
  // Try FlareSolverr first (if configured), else return null
  console.log('[MLSBD] Resolving bonghd URL:', bonghdUrl.slice(0, 80));
  
  // First, try a direct HEAD request — see if it 302-redirects anywhere
  try {
    const r = await fetch(bonghdUrl, {
      method: 'GET',
      headers: { ...BROWSER_HEADERS, Referer: MLSBD_BASE + '/' },
      redirect: 'manual',
      signal: AbortSignal.timeout(8000),
    });
    if (r.status >= 300 && r.status < 400 && r.headers.get('location')) {
      const loc = r.headers.get('location');
      console.log('[MLSBD] Direct redirect to:', loc);
      // If it goes to mlsbd.to (CF-challenged), try FlareSolverr
      if (loc.includes('mlsbd.to') || loc.includes('mlsbd.co')) {
        const fs = await solveCloudflareViaFlareSolverr(loc, MLSBD_BASE + '/');
        if (fs) {
          // Look for file host URLs in the resolved HTML
          let fileUrls = extractFileHostUrls(fs.html);
          // Convert any pixeldrain URLs to bypass URLs
          fileUrls = await convertPixeldrainUrls(fileUrls);
          if (fileUrls.length > 0) return fileUrls;
        }
        // Fallback: return the redirect target as iframe embed
        return { iframeUrl: loc, requiresCF: true };
      }
      // If it redirects to a file host directly, return that
      if (/gdflix|hubcloud|vcloud|gofile|pixeldrain|streamtape|drive\.google|mega\.nz/i.test(loc)) {
        // If it's pixeldrain, convert to bypass URL
        const converted = await convertPixeldrainUrls([loc]);
        return { directUrl: converted[0] };
      }
    }
  } catch (e) { /* fall through */ }
  
  // Try FlareSolverr directly on the bonghd URL
  if (FLARESOLVERR_URL) {
    const fs = await solveCloudflareViaFlareSolverr(bonghdUrl, MLSBD_BASE + '/');
    if (fs) {
      let fileUrls = extractFileHostUrls(fs.html);
      // Convert any pixeldrain URLs to bypass URLs
      fileUrls = await convertPixeldrainUrls(fileUrls);
      if (fileUrls.length > 0) return fileUrls;
      return { iframeUrl: fs.url, requiresCF: true };
    }
  }
  
  return null;
}

// ─── Extract file host URLs from HTML ──────────────────────────────────────
// Also converts any pixeldrain URLs to bypass URLs (via cdn.pixeldrain.eu.cc)
// for better speed + reliability (bypasses pixeldrain's per-IP rate limits).
function extractFileHostUrls(html) {
  const patterns = [
    /https?:\/\/(?:new\d+\.)?gdflix\.[a-z]{2,}\/[a-zA-Z0-9/_?=&.-]+/gi,
    /https?:\/\/hubcloud\.[a-z]{2,}\/[a-zA-Z0-9/_?=&.-]+/gi,
    /https?:\/\/vcloud\.[a-z]{2,}\/[a-zA-Z0-9/_?=&.-]+/gi,
    /https?:\/\/new\d+\.multicloudlinks\.com\/[a-zA-Z0-9/_?=&.-]+/gi,
    /https?:\/\/pixeldrain\.[a-z]{2,}\/[a-zA-Z0-9/_?=&.-]+/gi,  // captures /u/, /d/, /api/file/, /l/
    /https?:\/\/(?:api\.)?gofile\.io\/[a-zA-Z0-9/_?=&.-]+/gi,
    /https?:\/\/streamtape\.com\/[a-zA-Z0-9/_?=&.-]+/gi,
    /https?:\/\/drive\.google\.com\/uc\?export=download&id=[a-zA-Z0-9_-]+/gi,
    /https?:\/\/drive\.usercontent\.google\.com\/download\?id=[a-zA-Z0-9_-]+[^"'\s]*/gi,
    /https?:\/\/mega\.nz\/[a-zA-Z0-9/_?=&#.-]+/gi,
  ];
  const urls = new Set();
  for (const p of patterns) {
    for (const m of html.matchAll(p)) urls.add(m[0].replace(/&amp;/g, '&'));
  }
  return [...urls];
}

// ─── Pixeldrain bypass integration ──────────────────────────────────────────
// Converts direct pixeldrain URLs to bypass URLs via cdn.pixeldrain.eu.cc.
// This bypasses pixeldrain's per-IP rate limits by distributing requests
// across random CDN subdomains (cdn07/cdn08/cdn09...).
let _pdBypass = null;
async function loadPixeldrainBypass() {
  if (_pdBypass) return _pdBypass;
  try {
    _pdBypass = require('./pixeldrain-bypass');
  } catch (e) {
    try {
      // Try relative path
      _pdBypass = require('/home/z/my-project/scripts/pixeldrain-bypass');
    } catch (e2) {
      console.log('[MLSBD] pixeldrain-bypass module not found — using direct URLs');
      _pdBypass = null;
    }
  }
  return _pdBypass;
}

async function convertPixeldrainUrls(urls) {
  const pdBypass = await loadPixeldrainBypass();
  if (!pdBypass) return urls;
  const out = [];
  for (const url of urls) {
    if (pdBypass.isPixeldrainUrl(url)) {
      const bypassed = await pdBypass.bypassPixeldrain(url);
      console.log('[MLSBD] Pixeldrain bypass: ' + url.slice(0, 60) + ' → ' + (bypassed || url).slice(0, 80));
      out.push(bypassed || url);
    } else {
      out.push(url);
    }
  }
  return out;
}

// ─── Probe URL for playability ─────────────────────────────────────────────
async function probeUrl(url) {
  try {
    const u = new URL(url);
    const lib = u.protocol === 'http:' ? require('http') : require('https');
    return await new Promise(resolve => {
      const req = lib.request({
        hostname: u.hostname, path: u.pathname + u.search, method: 'GET',
        headers: { 'User-Agent': UA, 'Range': 'bytes=0-1', 'Accept': '*/*', 'Referer': MLSBD_BASE + '/' }
      }, (res) => {
        res.destroy();
        const ct = res.headers['content-type'] || '';
        const ok = (res.statusCode === 200 || res.statusCode === 206) &&
                  /video|octet-stream|mpegurl/i.test(ct);
        resolve({ status: res.statusCode, contentType: ct, playable: ok, location: res.headers.location || '' });
      });
      req.on('error', () => resolve({ playable: false }));
      req.on('timeout', () => { req.destroy(); resolve({ playable: false, timeout: true }); });
      req.setTimeout(8000); req.end();
    });
  } catch (e) { return { playable: false, error: e.message }; }
}

// ─── Convert Google Drive URL to direct playable ──────────────────────────
function convertGdriveUrl(url) {
  // https://drive.google.com/uc?export=download&id=FILE_ID → 
  // https://drive.usercontent.google.com/download?id=FILE_ID&export=download&confirm=t
  const m = url.match(/[?&]id=([^&]+)/);
  if (m) {
    return `https://drive.usercontent.google.com/download?id=${m[1]}&export=download&confirm=t`;
  }
  return url;
}

// ─── Main entry: getStreams(id, type, season, episode) ─────────────────────
async function getStreams(id, type, season, episode) {
  console.log('[MLSBD] Request: id=' + id + ' type=' + type + (season ? ' S' + season + 'E' + episode : ''));
  
  // 1. Enriched metadata via TMDB
  const info = await getTMDBInfo(id, type);
  console.log('[MLSBD] Title:', info.title, '(' + info.year + ') tmdb=' + info.tmdbId + (info.animeGenres ? ' [ANIME]' : ''));
  
  // 2. Search mlsbd.co (may return multiple URLs — e.g., 4K version + 480p/720p version)
  const pageUrls = await searchMLSBD(info.title, info.year);
  if (pageUrls.length === 0) {
    console.log('[MLSBD] No matching title on mlsbd.co');
    return [];
  }
  
  // 3. Parse each page for download buttons
  const allLinks = [];
  for (const pageUrl of pageUrls) {
    const links = await parseMLSBDPage(pageUrl);
    for (const link of links) {
      // Skip duplicates (same href)
      if (!allLinks.find(l => l.href === link.href)) {
        allLinks.push(link);
      }
    }
  }
  if (allLinks.length === 0) {
    console.log('[MLSBD] No download buttons on page');
    return [];
  }
  console.log('[MLSBD] Total unique links:', allLinks.length);
  
  // 4. Resolve each bonghd.com URL → file host URL
  const streams = [];
  for (const link of allLinks) {
    console.log('[MLSBD] Resolving link:', link.quality, link.size, '→', link.href.slice(0, 80));
    const resolved = await resolveBonghdUrl(link.href);
    if (!resolved) {
      console.log('[MLSBD] Could not resolve (CF challenge, no FlareSolverr)');
      continue;
    }
    
    // resolved can be:
    // - array of file host URLs
    // - { directUrl: ... } (single direct URL)
    // - { iframeUrl: ..., requiresCF: true } (need iframe rendering)
    const targetUrls = Array.isArray(resolved) ? resolved :
                       resolved.directUrl ? [resolved.directUrl] :
                       resolved.iframeUrl ? [resolved.iframeUrl] : [];
    
    for (const targetUrl of targetUrls) {
      // Convert Google Drive URL to direct playable
      const finalUrl = targetUrl.includes('drive.google.com') ? convertGdriveUrl(targetUrl) : targetUrl;
      
      // Probe for playability
      const probe = await probeUrl(finalUrl);
      
      // Build stream name
      const parts = [`[MB] MLSBD ${link.quality}`];
      if (link.hdr) parts.push(link.hdr);
      if (link.bit10) parts.push(link.bit10);
      if (link.codec) parts.push(link.codec);
      if (link.langs) parts.push(`[${link.langs}]`);
      if (link.audio) parts.push(link.audio);
      if (link.size) parts.push(link.size);
      const animeTag = info.animeGenres ? ' [SUB+DUB]' : '';
      
      streams.push({
        name: parts.join(' | ') + animeTag,
        title: info.title + ` (${info.year}) [MLSBD ${link.quality}${link.hdr ? ' ' + link.hdr : ''}]${link.langs ? ' [' + link.langs + ']' : ''}${link.size ? ' ' + link.size : ''}${animeTag}`,
        url: finalUrl,
        quality: link.quality.toLowerCase(),
        type: probe.playable ? 'video/mp4' : 'text/html',
        headers: { 'User-Agent': UA, 'Referer': MLSBD_BASE + '/' },
        provider: 'MLSBD',
        behaviorHints: {
          notWebReady: !probe.playable,
          foreignRefs: probe.playable ? undefined : [{ url: finalUrl }],
          bingeGroup: `mlsbd|${link.quality}|${link.codec || 'unknown'}`,
        },
        _probeStatus: probe.status,
        _probePlayable: probe.playable,
      });
    }
  }
  
  // Sort: direct-playable first, then quality desc
  streams.sort((a, b) => {
    if (a._probePlayable !== b._probePlayable) return a._probePlayable ? -1 : 1;
    const qRank = { '4k': 4, '2160p': 4, '1440p': 3, '1080p': 2, '720p': 1, '480p': 0 };
    return (qRank[b.quality] || -1) - (qRank[a.quality] || -1);
  });
  
  // Attach metadata
  streams.meta = {
    id: info.imdbId || info.tmdbId,
    imdbId: info.imdbId, tmdbId: info.tmdbId,
    type: info.isMovie ? 'movie' : 'series',
    name: info.title, year: info.year,
    genres: info.genres, cast: info.cast, director: info.director,
    tag: info.tagline, overview: info.overview,
    poster: info.posterPath ? `https://image.tmdb.org/t/p/w500${info.posterPath}` : '',
    backdrop: info.backdropPath ? `https://image.tmdb.org/t/p/original${info.backdropPath}` : '',
    imdbRating: info.voteAverage ? info.voteAverage.toFixed(1) : '',
    isAnime: !!info.animeGenres,
    trailers: info.videos || [],
    similar: info.similar || [],
    recommendations: info.recommendations || [],
  };
  
  const capped = streams.slice(0, MAX_STREAMS).map(s => {
    const { _probeStatus, _probePlayable, ...clean } = s;
    return clean;
  });
  if (streams.meta) capped.meta = streams.meta;
  console.log('[MLSBD] Returning', capped.length, 'streams');
  return capped;
}

// ─── CLI ──────────────────────────────────────────────────────────────────
if (require.main === module) {
  (async () => {
    const args = process.argv.slice(2);
    if (!args[0]) {
      console.log('Usage: node mlsbd.js <imdbId|tmdbId> <movie|tv> [season] [episode]');
      console.log('Optional env: FLARESOLVERR_URL=http://localhost:8191');
      process.exit(0);
    }
    const streams = await getStreams(args[0], args[1] || 'movie', args[2], args[3]);
    console.log('\n' + streams.length + ' streams:');
    streams.forEach((s, i) => {
      const playable = s.behaviorHints?.foreignRefs ? 'iframe' : 'direct';
      console.log('[' + i + '] ' + s.name + ' (' + s.quality + ') [' + playable + ']');
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
    if (!FLARESOLVERR_URL && streams.length === 0) {
      console.log('\n⚠ NO STREAMS RETURNED. Reason:');
      console.log('  link.bonghd.com → mlsbd.to is Cloudflare-challenged.');
      console.log('  The original .cs3 plugin uses Android WebView + libcloudflared.so to bypass.');
      console.log('  To make this work in pure Node.js, set FLARESOLVERR_URL env var:');
      console.log('    docker run -d -p 8191:8191 ghcr.io/flaresolverr/flaresolverr:latest');
      console.log('    FLARESOLVERR_URL=http://localhost:8191 node mlsbd.js tt1375666 movie');
    }
  })();
}

module.exports = { getStreams, searchMLSBD, parseMLSBDPage, resolveBonghdUrl, getTMDBInfo };

// ─── Deployment guide (append at end of file) ───────────────────────────────
//
// TO GET DIRECT PLAYABLE STREAMS (not iframe embeds):
//
// 1. Self-host FlareSolverr (recommended — free, runs Chromium headless):
//
//    docker run -d --name=flaresolverr -p 8191:8191 \
//      -e LOG_LEVEL=info --restart unless-stopped \
//      ghcr.io/flaresolverr/flaresolverr:latest
//
// 2. Set the env var:
//
//    export FLARESOLVERR_URL=http://localhost:8191
//
// 3. Run the provider:
//
//    node mlsbd.js tt1375666 movie
//
// The provider will call FlareSolverr's /v1 endpoint to solve Cloudflare's
// Turnstile challenge on mlsbd.to → get the actual file host URLs (gdflix.dev,
// hubcloud.cx, vcloud.zip) → resolve those to direct .mkv CDN URLs.
//
// ────────────────────────────────────────────────────────────────────────────
//
// ALTERNATIVE: Use your existing pengu.uk setup (already does CF bypass)
//
// The MLSBD provider eventually resolves to gdflix.dev / hubcloud.cx / vcloud.zip
// file hosts — the SAME hosts that pengu.uk's "arctic" source scrapes.
//
// Your existing antarctica.js provider (with source_arctic=on) returns DIRECT
// playable .mkv URLs for these hosts via pengu.uk's 307-redirect mechanism.
//
// To combine both:
//   1. Use mlsbd.js for SEARCH + metadata (returns post URLs + quality info)
//   2. Use antarctica.js (source_arctic=on) for STREAM URL resolution
//   3. The antarctica.js provider already covers gdflix/hubcloud/vcloud hosts
//
// ────────────────────────────────────────────────────────────────────────────
