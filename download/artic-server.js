/**
 * Artic Standalone Server — multi-source stream resolver without PenguPlay
 * ===========================================================================
 * Queries 12+ WordPress movie sites in parallel, decrypts redirect tokens
 * with AES-CTR, resolves HubCloud → Pixeldrain/workers.dev, returns DIRECT
 * playable streams. No PenguPlay dependency, no proxy needed.
 *
 * Sites queried (from phisher98/TVVVV domains.json):
 *   HDHub4u, 4KHDHub, MoviesDrive, MoviesMod, UHDMovies, BollyFlix,
 *   VegaMovies, RogMovies, MoviesLeech, Cinevood, Movies4U, MultiMovies
 *
 * Deploy:
 *   npm install express cors crypto-js cheerio
 *   node artic-server.js
 */
'use strict';

const express = require('express');
const cors = require('cors');
const CryptoJS = require('crypto-js');
const cheerio = require('cheerio');
const { execFile } = require('child_process');
const { promisify } = require('util');
const execFileAsync = promisify(execFile);

const app = express();
const PORT = process.env.PORT || 7860;
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';
const TMDB_API_KEY = '439c478a771f35c05022f9feabcca01c';
const REDIRECT_KEY = '556151c9cefdee6ea51aec956e6b8682312bb609c0167d4adc71f365a928920f';
const DOMAINS_JSON = 'https://raw.githubusercontent.com/phisher98/TVVVV/refs/heads/main/domains.json';

// Artic sites to query
const ARTIC_SITES = [
  'HDHUB4u', '4khdhub', 'moviesdrive', 'moviesmod', 'UHDMovies',
  'bollyflix', 'vegamovies', 'rogmovies', 'topMovies', 'cinevood',
  'movies4u', 'MultiMovies',
];

let domainCache = null;
let domainCacheTime = 0;

// ─── HTTP (curl for Cloudflare compatibility) ──────────────────────────────
async function httpGet(url, opts = {}) {
  const args = ['-sS', '-L', '--compressed', '--max-time', '12', '-A', UA];
  if (opts.referer) args.push('-H', `Referer: ${opts.referer}`);
  if (opts.cookie) args.push('-b', opts.cookie);
  args.push(url);
  try {
    const { stdout } = await execFileAsync('curl', args, { encoding: 'utf-8', maxBuffer: 30 * 1024 * 1024, timeout: 15000 });
    return stdout;
  } catch (e) { return ''; }
}

async function httpGetJson(url) {
  const body = await httpGet(url, { headers: { Accept: 'application/json' } });
  if (!body) return null;
  try { return JSON.parse(body); } catch (e) { return null; }
}

async function httpPostJson(url, body) {
  try {
    const { stdout } = await execFileAsync('curl', [
      '-sS', '-L', '--compressed', '--max-time', '12',
      '-A', UA, '-H', 'Content-Type: application/json', '-H', 'Accept: application/json',
      '-X', 'POST', '-d', body, url,
    ], { encoding: 'utf-8', maxBuffer: 10 * 1024 * 1024, timeout: 15000 });
    return stdout ? JSON.parse(stdout) : null;
  } catch (e) { return null; }
}

// ─── Domain fetcher ────────────────────────────────────────────────────────
async function getDomains() {
  const now = Date.now();
  if (domainCache && (now - domainCacheTime) < 4 * 3600 * 1000) return domainCache;
  try {
    const data = await httpGetJson(DOMAINS_JSON);
    if (data) { domainCache = data; domainCacheTime = now; return data; }
  } catch (e) {}
  domainCache = {}; return domainCache;
}

// ─── AES-CTR token decryption (same as ToonWorld4All/Artic sites) ──────────
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
    const counterBytes = [];
    for (let block = 0; block < blockCount; block++) {
      for (let i = 0; i < 16; i++) counterBytes.push(counter[i]);
      for (let i = 15; i >= 0; i--) { counter[i] = (counter[i] + 1) & 0xff; if (counter[i] !== 0) break; }
    }
    const counterWA = CryptoJS.lib.WordArray.create(
      counterBytes.reduce((w, b, i) => { const wi = Math.floor(i / 4); w[wi] = (w[wi] || 0) | (b << (24 - (i % 4) * 8)); return w; }, new Array(Math.ceil(counterBytes.length / 4)).fill(0)),
      counterBytes.length
    );
    const keystream = CryptoJS.AES.encrypt(counterWA, key, { mode: CryptoJS.mode.ECB, padding: CryptoJS.pad.NoPadding }).ciphertext;
    const ksBytes = [];
    for (let i = 0; i < keystream.sigBytes; i++) ksBytes.push((keystream.words[i >>> 2] >>> (24 - (i % 4) * 8)) & 0xff);
    const plainBytes = new Array(cipherBytes.length);
    for (let i = 0; i < cipherBytes.length; i++) plainBytes[i] = cipherBytes[i] ^ ksBytes[i];
    let encoded = '';
    for (let i = 0; i < plainBytes.length; i++) encoded += '%' + (plainBytes[i] < 16 ? '0' : '') + plainBytes[i].toString(16);
    const payload = JSON.parse(decodeURIComponent(encoded));
    return (payload && payload.url) || null;
  } catch (e) { return null; }
}

// ─── Extract __PROPS__ JSON from archive/page HTML ─────────────────────────
function extractProps(html) {
  const idx = html.indexOf('__PROPS__');
  if (idx === -1) return null;
  const start = html.indexOf('{', idx);
  if (start === -1) return null;
  const end = html.indexOf('</script>', start);
  if (end === -1) return null;
  try { return JSON.parse(html.slice(start, end).trim().replace(/;\s*$/, '')); } catch (e) { return null; }
}

// ─── HubCloud resolver ────────────────────────────────────────────────────
async function resolveHubCloud(pageUrl) {
  try {
    const html = await httpGet(pageUrl);
    if (!html) return [];
    const $ = cheerio.load(html);
    const streams = [];
    let fileName = ($('div.card-header').first().text() || '').trim();
    const sizeMatch = ($('i#size').first().text() || '').match(/(\d+(?:\.\d+)?\s*(?:GB|MB))/i);
    const fileSize = sizeMatch ? sizeMatch[1] : '';
    let genUrl = $('a#download').attr('href') || $('a.btn-primary[href*="hubcloud.php"]').attr('href') || '';
    if (!genUrl) { const m = html.match(/https?:\/\/[^"'\s>]+hubcloud\.php\?[^"'\s>]+/i); if (m) genUrl = m[0]; }
    if (!genUrl) return streams;
    const genHtml = await httpGet(genUrl, { referer: pageUrl });
    if (!genHtml) return streams;
    const $g = cheerio.load(genHtml);
    if (!fileName) fileName = ($g('div.card-header').first().text() || '').trim();
    $g('a[href]').each((_, a) => {
      const text = $g(a).text().trim();
      const link = ($g(a).attr('href') || '').trim();
      if (!link.startsWith('http')) return;
      if (/pixeldrain|pixelserver/i.test(text) || link.includes('pixeldrain.')) {
        const m = link.match(/\/u\/([A-Za-z0-9_-]+)/);
        if (m) streams.push({ url: `https://pixeldrain.com/api/file/${m[1]}?download`, server: 'Pixeldrain', fileName, fileSize });
      } else if (/FSL/i.test(text) || /workers\.dev/i.test(link) || /\.mkv$/i.test(link) || /\.mp4$/i.test(link)) {
        streams.push({ url: link, server: text || 'Direct', fileName, fileSize });
      }
    });
    return streams;
  } catch (e) { return []; }
}

// ─── Resolve a redirect link → file host URL → direct stream ──────────────
async function resolveRedirectLink(redirectPath, label) {
  const streams = [];
  // Try AES-CTR decryption first (no network)
  const tokenMatch = String(redirectPath).match(/\/redirect\/([0-9a-fA-F]{32,})/);
  if (tokenMatch) {
    const decrypted = decryptRedirectToken(tokenMatch[1]);
    if (decrypted && decrypted.startsWith('http')) {
      if (decrypted.includes('hubcloud.')) {
        return resolveHubCloud(decrypted);
      } else if (decrypted.includes('pixeldrain.')) {
        const m = decrypted.match(/\/u\/([A-Za-z0-9_-]+)/);
        if (m) return [{ url: `https://pixeldrain.com/api/file/${m[1]}?download`, server: 'Pixeldrain', fileName: '', fileSize: '' }];
      } else if (/workers\.dev|\.mkv$|\.mp4$/i.test(decrypted)) {
        return [{ url: decrypted, server: 'Direct', fileName: '', fileSize: '' }];
      }
    }
  }
  // Fallback: fetch redirect page and extract JSON
  const absUrl = redirectPath.startsWith('http') ? redirectPath : redirectPath.startsWith('/') ? 'https://archive.toonworld4all.me' + redirectPath : 'https://archive.toonworld4all.me/' + redirectPath;
  const html = await httpGet(absUrl);
  if (html) {
    const props = extractProps(html);
    if (props && props.link) {
      const fileUrl = props.link.domain + props.link.hidden;
      if (fileUrl.includes('hubcloud.')) return resolveHubCloud(fileUrl);
    }
    const obf = html.match(/<code>(https?:\/\/[^<]+)<span[^>]*>([^<]+)<\/span><\/code>/i);
    if (obf) streams.push({ url: obf[1] + obf[2], server: 'Direct', fileName: '', fileSize: '' });
  }
  return streams;
}

// ─── Per-site scraper: search → post page → download links → resolve ──────
async function scrapeSite(siteKey, domain, title, year) {
  try {
    // Step 1: Search
    const searchHtml = await httpGet(`${domain}/?s=${encodeURIComponent(title + (year ? ' ' + year : ''))}`);
    if (!searchHtml || searchHtml.length < 500) return [];

    // Parse search results — try MULTIPLE article patterns
    const $ = cheerio.load(searchHtml);
    const candidates = [];
    
    // Pattern 1: standard article tags
    $('article').each((_, art) => {
      const anchor = $(art).find('h2 a, .entry-title a, a[href]').first();
      const url = (anchor.attr('href') || '').trim();
      const postTitle = ($(art).find('h2, .entry-title, .title').first().text() || anchor.attr('title') || anchor.text() || '').trim();
      if (!url || !postTitle || /list|about|contact|disclaimer|category|tag/i.test(url)) return;
      const titleLower = title.toLowerCase();
      const postLower = postTitle.toLowerCase();
      let score = 0;
      title.toLowerCase().split(/\s+/).forEach(w => { if (w.length > 2 && postLower.includes(w)) score++; });
      if (year && postLower.includes(year)) score += 5;
      if (score > 0) candidates.push({ url, title: postTitle, score });
    });
    
    // Pattern 2: latestPost class (MoviesMod-style)
    $('.latestPost').each((_, art) => {
      const anchor = $(art).find('a[href]').first();
      const url = (anchor.attr('href') || '').trim();
      const postTitle = (anchor.attr('title') || $(art).find('h2, .title').first().text() || '').trim();
      if (!url || !postTitle) return;
      const postLower = postTitle.toLowerCase();
      let score = 0;
      title.toLowerCase().split(/\s+/).forEach(w => { if (w.length > 2 && postLower.includes(w)) score++; });
      if (year && postLower.includes(year)) score += 5;
      if (score > 0) candidates.push({ url, title: postTitle, score });
    });
    
    // Pattern 3: herald-lay articles (ToonWorld4All-style)
    $('.herald-lay-b, .herald-lay-f, .entry-card').each((_, art) => {
      const anchor = $(art).find('a[href]').first();
      const url = (anchor.attr('href') || '').trim();
      const postTitle = (anchor.attr('title') || $(art).find('h2, h3, .entry-title, .title').first().text() || '').trim();
      if (!url || !postTitle) return;
      const postLower = postTitle.toLowerCase();
      let score = 0;
      title.toLowerCase().split(/\s+/).forEach(w => { if (w.length > 2 && postLower.includes(w)) score++; });
      if (year && postLower.includes(year)) score += 5;
      if (score > 0) candidates.push({ url, title: postTitle, score });
    });
    
    // Pattern 4: WordPress REST API fallback (for sites where HTML search doesn't work)
    if (candidates.length === 0) {
      try {
        const apiHtml = await httpGet(`${domain}/wp-json/wp/v2/posts?search=${encodeURIComponent(title)}&per_page=5`);
        if (apiHtml) {
          const posts = JSON.parse(apiHtml);
          for (const post of (Array.isArray(posts) ? posts : [])) {
            const postTitle = (post.title && post.title.rendered) || '';
            const url = post.link || '';
            if (!url || !postTitle) continue;
            const postLower = postTitle.toLowerCase();
            let score = 0;
            title.toLowerCase().split(/\s+/).forEach(w => { if (w.length > 2 && postLower.includes(w)) score++; });
            if (year && postLower.includes(year)) score += 5;
            if (score > 0) candidates.push({ url, title: postTitle, score });
          }
        }
      } catch (e) {}
    }
    if (!candidates.length) return [];
    candidates.sort((a, b) => b.score - a.score);
    const bestPost = candidates[0];

    // Step 2: Fetch post page → find download links
    const postHtml = await httpGet(bestPost.url, { referer: domain + '/' });
    if (!postHtml) return [];

    // Look for:
    // a) archive.toonworld4all.me links (for ToonWorld4All-style sites)
    // b) HubCloud direct links
    // c) GDFlix links
    // d) /redirect/{token} links
    // e) Pixeldrain direct links
    const $$ = cheerio.load(postHtml);
    const allStreams = [];

    // Pattern 1: archive links with __PROPS__
    const archiveLinks = new Set();
    $$('a[href*="archive.toonworld4all"]').each((_, a) => {
      const href = ($$(a).attr('href') || '').trim();
      if (href) archiveLinks.add(href);
    });

    // Pattern 2: Direct HubCloud/GDFlix/Pixeldrain links in the post
    $$('a[href]').each((_, a) => {
      const href = ($$(a).attr('href') || '').trim();
      const text = $$(a).text().trim();
      if (href.includes('hubcloud.') || href.includes('gdflix.') || href.includes('pixeldrain.')) {
        allStreams.push(resolveRedirectLink(href, text));
      }
      if (href.includes('/redirect/')) {
        allStreams.push(resolveRedirectLink(href, text));
      }
    });

    // Pattern 3: Extract quality info from headers
    const qualities = {};
    $$('.thecontent h3, .thecontent h4, .entry-content h3, .entry-content h4, h5').each((_, h) => {
      const text = $$(h).text().trim();
      const qm = text.match(/(2160p|1080p|720p|480p|4K)/i);
      if (qm) {
        const q = qm[1].toLowerCase();
        const next = $$(h).nextUntil('h3, h4, h5').find('a[href*="hubcloud"], a[href*="gdflix"], a[href*="redirect/"], a[href*="pixeldrain"]');
        next.each((_, a) => {
          const href = $$(a).attr('href') || '';
          if (href) allStreams.push(resolveRedirectLink(href, q));
        });
      }
    });

    // Resolve archive pages (for ToonWorld4All-style sites)
    for (const archUrl of archiveLinks) {
      const archHtml = await httpGet(archUrl);
      if (!archHtml) continue;
      const props = extractProps(archHtml);
      if (props && props.data && props.data.data && props.data.data.encodes) {
        for (const encode of props.data.data.encodes) {
          const codec = (encode.readable && encode.readable.codec) || '';
          const size = (encode.readable && encode.readable.size) || '';
          for (const file of (encode.files || [])) {
            if (file && file.link) allStreams.push(resolveRedirectLink(file.link, codec + ' ' + size));
          }
        }
      }
      // Also parse redirect links directly from HTML
      const $a = cheerio.load(archHtml);
      $a("a[href*='/redirect/']").each((_, a) => {
        const href = ($a(a).attr('href') || '').trim();
        if (href) allStreams.push(resolveRedirectLink(href, bestPost.title));
      });
    }

    const resolved = (await Promise.all(allStreams)).flat();
    // Tag with site name
    return resolved.map(s => ({
      ...s,
      site: siteKey,
      quality: detectQuality(s.fileName || bestPost.title),
      postTitle: bestPost.title,
    }));
  } catch (e) {
    return [];
  }
}

function detectQuality(text) {
  const m = (text || '').match(/\b(2160p|4K|UHD|1080p|720p|480p|360p)\b/i);
  if (!m) return '1080p';
  const q = m[1].toUpperCase();
  if (q === '2160P' || q === '4K' || q === 'UHD') return '4K';
  return q === '1080P' ? '1080p' : q.toLowerCase();
}

// ─── TMDB lookup ──────────────────────────────────────────────────────────
async function getTmdb(tmdbId, type) {
  const url = `https://api.themoviedb.org/3/${type === 'tv' ? 'tv' : 'movie'}/${tmdbId}?api_key=${TMDB_API_KEY}&language=en-US`;
  const data = await httpGetJson(url);
  if (!data) return null;
  return {
    title: type === 'tv' ? data.name : data.title,
    year: ((type === 'tv' ? data.first_air_date : data.release_date) || '').substring(0, 4),
    poster: data.poster_path ? `https://image.tmdb.org/t/p/w500${data.poster_path}` : null,
    genres: (data.genres || []).map(g => g.name),
    overview: data.overview || '',
  };
}

// ─── Main stream resolver — queries ALL Artic sites in parallel ───────────
async function resolveStreams(tmdbId, type, season, episode) {
  const isSeries = type === 'tv' || type === 'series';
  const tmdbType = isSeries ? 'tv' : 'movie';
  const tmdb = await getTmdb(tmdbId, tmdbType);
  if (!tmdb || !tmdb.title) return [];

  const domains = await getDomains();
  const searchTitle = tmdb.title + (tmdb.year ? ' ' + tmdb.year : '');

  // Query ALL Artic sites in parallel
  console.log(`[Artic] Resolving "${searchTitle}" across ${ARTIC_SITES.length} sites...`);
  const siteResults = await Promise.all(
    ARTIC_SITES.map(async siteKey => {
      const domain = domains[siteKey];
      if (!domain) return [];
      const streams = await scrapeSite(siteKey, domain, tmdb.title, tmdb.year);
      console.log(`[Artic] ${siteKey}: ${streams.length} streams`);
      return streams;
    })
  );

  // Merge + deduplicate
  const allStreams = siteResults.flat();
  const seen = new Set();
  const unique = allStreams.filter(s => {
    if (!s.url || seen.has(s.url)) return false;
    seen.add(s.url);
    return true;
  });

  // Build Stremio stream objects
  const stremioStreams = unique.map(s => ({
    name: `Artic\n${s.quality} · ${s.site}`,
    title: `${tmdb.title}${episode ? ' S' + (season || 1) + 'E' + episode : ''} · ${s.quality} · ${s.server}${s.fileSize ? ' · ' + s.fileSize : ''}`,
    url: s.url,
    quality: s.quality,
    behaviorHints: {
      notWebReady: false,
      headers: { 'User-Agent': UA },
      proxyHeaders: { request: { 'User-Agent': UA } },
    },
    poster: tmdb.poster,
    meta: {
      provider: 'Artic',
      source: s.site,
      server: s.server,
      type: 'mp4',
      quality: s.quality,
      title: tmdb.title,
      size: s.fileSize || '',
      episodeNumber: episode,
      description: tmdb.overview,
      poster: tmdb.poster,
      genres: tmdb.genres,
      directStream: true,
      mediaType: tmdbType,
    },
  }));

  // Sort by quality descending, then by file size
  const order = { '4K': 5, '2160p': 5, '1080p': 4, '720p': 3, '480p': 2, '360p': 1 };
  stremioStreams.sort((a, b) => {
    const qDiff = (order[b.quality] || 0) - (order[a.quality] || 0);
    if (qDiff !== 0) return qDiff;
    const aSize = parseFloat((a.meta.size || '').match(/(\d+(?:\.\d+)?)/)?.[1] || '0');
    const bSize = parseFloat((b.meta.size || '').match(/(\d+(?:\.\d+)?)/)?.[1] || '0');
    return bSize - aSize;
  });

  console.log(`[Artic] Total: ${stremioStreams.length} direct streams from ${new Set(unique.map(s => s.site)).size} sites`);
  return stremioStreams;
}

// ─── Express server ────────────────────────────────────────────────────────
app.use(cors());
app.use(express.json());

app.get('/health', (req, res) => res.json({ status: 'ok' }));

app.get('/manifest.json', (req, res) => {
  res.json({
    id: 'com.artic.standalone',
    version: '1.0.0',
    name: 'Artic',
    description: 'Multi-source stream resolver — queries 12+ WordPress sites in parallel, returns direct playable streams',
    types: ['movie', 'series'],
    resources: ['stream'],
    idPrefixes: ['tt', 'tmdb:'],
    catalogs: [],
  });
});

app.get('/stream/:type/:id.json', async (req, res) => {
  const { type, id } = req.params;
  let tmdbId = id, season = null, episode = null;

  // Parse ID: tmdb:27205 or tmdb:27205:1:1 (series S1E1)
  const parts = id.split(':');
  if (parts[0] === 'tmdb') {
    tmdbId = parts[1];
    if (parts.length >= 4) { season = parts[2]; episode = parts[3]; }
  } else if (parts[0] === 'tt') {
    tmdbId = parts[1]; // IMDB ID — TMDB API can look it up
  }

  console.log(`[Artic] Stream request: ${type} ${id}`);
  try {
    const streams = await resolveStreams(tmdbId, type, season, episode);
    res.json({ streams });
  } catch (e) {
    console.error(`[Artic] Error: ${e.message}`);
    res.json({ streams: [] });
  }
});

app.listen(PORT, '0.0.0.0', () => {
  console.log(`\n  ╔══════════════════════════════════════════════╗`);
  console.log(`  ║  Artic Standalone Server                     ║`);
  console.log(`  ╚══════════════════════════════════════════════╝`);
  console.log(`  Port: ${PORT}`);
  console.log(`  Sites: ${ARTIC_SITES.length} Artic WordPress sites`);
  console.log(`  Manifest: http://localhost:${PORT}/manifest.json`);
  console.log(`  Stream: http://localhost:${PORT}/stream/movie/tmdb:27205.json\n`);
});
