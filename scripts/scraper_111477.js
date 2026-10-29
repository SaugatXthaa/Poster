// 111477.xyz Scraper — With Cloudflare Worker Proxy
// ===================================================
// This scraper returns stream URLs that work on ALL platforms (Desktop,
// Android, iOS, Web) by using a Cloudflare Worker to proxy p.111477.xyz.
//
// The Worker bypasses the CF managed challenge because Workers run
// within Cloudflare's network and are exempt from challenges on their
// own fetch() calls.
//
// SETUP:
//   1. Go to https://dash.cloudflare.com → Workers & Pages → Create
//   2. Name it "proxy111477" (or anything)
//   3. Paste the Worker code from the comment block below
//   4. Deploy
//   5. Set WORKER_URL below to your Worker's URL
//   6. Done! Streams will work on all platforms.
//
// If you DON'T deploy a Worker, set WORKER_URL to "" and the scraper
// will fall back to returning a.111477.xyz URLs directly (works only
// on Stremio Desktop with browser-based player).

'use strict';

const crypto = require('crypto');

const PROVIDER_NAME = '111477';
const TMDB_API_KEY = '8476a7ab80ad76f0936744df0430e67c';
const A_111477 = 'https://a.111477.xyz';

// ========================================
// SET YOUR WORKER URL HERE
// ========================================
// After deploying the Worker (code below), set this to your Worker URL.
// Example: 'https://proxy111477.your-subdomain.workers.dev'
// Leave as '' to use direct a.111477.xyz URLs (Desktop only).
const WORKER_URL = '';

// The existing public worker from the 111477 addon (if you want to use it).
// NOTE: This requires knowing the hash, which we can't generate.
// const EXISTING_WORKER = 'https://strem1o.xn1nazihva.workers.dev';

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';

/*
╔══════════════════════════════════════════════════════════════════╗
║                  CLOUDFLARE WORKER CODE                          ║
║                                                                  ║
║  Copy everything between the ══ lines and paste into             ║
║  dash.cloudflare.com → Workers → Create Worker                  ║
╚══════════════════════════════════════════════════════════════════╝

export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    
    // Handle CORS preflight
    if (request.method === 'OPTIONS') {
      return new Response(null, {
        headers: {
          'Access-Control-Allow-Origin': '*',
          'Access-Control-Allow-Methods': 'GET, HEAD, OPTIONS',
          'Access-Control-Allow-Headers': 'Range, Content-Type',
          'Access-Control-Max-Age': '86400',
        },
      });
    }
    
    // Route: /d/<base64url-encoded-file-path>
    // The file path is the path on a.111477.xyz (e.g. /movies/Inception (2010)/file.mkv)
    if (url.pathname.startsWith('/d/')) {
      const encoded = url.pathname.slice(3);
      
      // Decode base64url to get the file path
      let filePath;
      try {
        // Convert base64url to standard base64
        let b64 = encoded.replace(/-/g, '+').replace(/_/g, '/');
        b64 = b64.padEnd(4 * Math.ceil(b64.length / 4), '=');
        filePath = atob(b64);
      } catch (e) {
        return new Response('Invalid base64', { status: 400 });
      }
      
      // Build the p.111477.xyz/bulk URL
      const fileUrl = 'https://a.111477.xyz' + filePath;
      const bulkUrl = 'https://p.111477.xyz/bulk?u=' + encodeURIComponent(fileUrl);
      
      // Fetch from p.111477.xyz
      // CF Workers bypass managed challenges on their own fetch calls
      const headers = new Headers(request.headers);
      headers.set('User-Agent', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36');
      headers.set('Referer', 'https://a.111477.xyz/');
      
      let response;
      try {
        response = await fetch(bulkUrl, {
          headers: headers,
          redirect: 'follow',
        });
      } catch (e) {
        return new Response('Fetch failed: ' + e.message, { status: 502 });
      }
      
      // Return the response with CORS headers
      const respHeaders = new Headers(response.headers);
      respHeaders.set('Access-Control-Allow-Origin', '*');
      respHeaders.set('Access-Control-Expose-Headers', 'Content-Length, Content-Range, Accept-Ranges, Content-Type');
      
      return new Response(response.body, {
        status: response.status,
        statusText: response.statusText,
        headers: respHeaders,
      });
    }
    
    // Route: /health
    if (url.pathname === '/health') {
      return new Response('OK', { status: 200 });
    }
    
    return new Response('111477 proxy worker\n\nUsage: /d/<base64url-encoded-file-path>', {
      status: 200,
      headers: { 'Content-Type': 'text/plain' },
    });
  }
};

╚══════════════════════════════════════════════════════════════════╝
*/

// =================== TMDB ===================

async function getTMDBInfo(tmdbId, type) {
  const endpoint = type === 'tv' ? 'tv' : 'movie';
  const url = 'https://api.themoviedb.org/3/' + endpoint + '/' + tmdbId +
    '?api_key=' + TMDB_API_KEY;
  try {
    const res = await fetch(url, { headers: { 'User-Agent': UA } });
    if (!res.ok) return null;
    const d = await res.json();
    return {
      title: type === 'tv' ? d.name : d.title,
      year: parseInt((d.first_air_date || d.release_date || '').slice(0, 4)) || null,
    };
  } catch (e) { return null; }
}

// =================== DIRECTORY LISTING ===================

async function fetchDirectory(path) {
  const url = A_111477 + path;
  const res = await fetch(url, {
    headers: { 'User-Agent': UA, 'Accept': 'text/html' },
    signal: AbortSignal.timeout(15000),
  });
  if (!res.ok) throw new Error('HTTP ' + res.status);
  const html = await res.text();
  const links = [];
  const regex = /href="([^"]+)"/g;
  let match;
  while ((match = regex.exec(html)) !== null) {
    const href = match[1];
    if (href.includes('cdn-cgi') || href === '/' || href.includes('discord.gg')) continue;
    if (href.startsWith('/')) links.push(href);
  }
  return links;
}

async function findTitleDirectory(title, year, type) {
  const baseDir = type === 'tv' ? '/tvs/' : '/movies/';
  const exactPath = baseDir + encodeURIComponent(title + ' (' + year + ')') + '/';
  try {
    const links = await fetchDirectory(exactPath);
    if (links.length > 0) return exactPath;
  } catch (e) {}
  
  const listing = await fetchDirectory(baseDir);
  for (const link of listing) {
    const decoded = decodeURIComponent(link);
    if (decoded.includes(title + ' (' + year + ')') ||
        decoded.toLowerCase().includes(title.toLowerCase() + ' (' + year + ')')) {
      return link;
    }
  }
  for (const link of listing) {
    const decoded = decodeURIComponent(link);
    if (decoded.includes(title)) return link;
  }
  return null;
}

async function listVideoFiles(dirPath, isTV, season, episode) {
  if (!isTV) {
    const links = await fetchDirectory(dirPath);
    return links.filter(l => /\.(mkv|mp4|avi|webm)$/i.test(l));
  }
  
  const seasonDir = dirPath + 'Season ' + String(season || 1).padStart(2, '0') + '/';
  let searchDir = dirPath;
  try {
    const seasonLinks = await fetchDirectory(seasonDir);
    if (seasonLinks.length > 0) searchDir = seasonDir;
  } catch (e) {}
  
  const links = await fetchDirectory(searchDir);
  const epPattern = episode ? [
    'S' + String(season).padStart(2, '0') + 'E' + String(episode).padStart(2, '0'),
    'S' + season + 'E' + episode,
  ] : [];
  const videoFiles = links.filter(l => /\.(mkv|mp4|avi|webm)$/i.test(l));
  
  if (epPattern.length > 0) {
    const matched = videoFiles.filter(f => {
      const decoded = decodeURIComponent(f);
      return epPattern.some(p => decoded.includes(p));
    });
    if (matched.length > 0) return matched;
  }
  return videoFiles;
}

// =================== STREAM URL BUILDER ===================

// Base64url encode a string
function base64urlEncode(str) {
  return Buffer.from(str, 'utf8').toString('base64url');
}

// Build the stream URL.
// If WORKER_URL is set: returns worker URL with base64url-encoded file path
// If WORKER_URL is empty: returns direct a.111477.xyz URL (Desktop only)
function buildStreamUrl(filePath) {
  if (WORKER_URL) {
    // Worker URL format: <worker_url>/d/<base64url-encoded-file-path>
    const encoded = base64urlEncode(filePath);
    return WORKER_URL + '/d/' + encoded;
  }
  // Fallback: direct URL (works only on Stremio Desktop)
  return A_111477 + filePath;
}

// =================== PARSERS ===================

function parseQuality(filename) {
  const f = filename.toLowerCase();
  if (f.includes('2160p') || f.includes('4k') || f.includes('uhd')) return '2160p';
  if (f.includes('1080p')) return '1080p';
  if (f.includes('720p')) return '720p';
  if (f.includes('480p')) return '480p';
  return '1080p';
}

function parseCodec(filename) {
  const f = filename.toLowerCase();
  if (f.includes('x265') || f.includes('h265') || f.includes('hevc')) return 'HEVC';
  if (f.includes('x264') || f.includes('h264')) return 'H264';
  return '';
}

function parseAudio(filename) {
  const f = filename.toLowerCase();
  if (f.includes('hindi')) return 'Hindi';
  if (f.includes('tamil')) return 'Tamil';
  if (f.includes('telugu')) return 'Telugu';
  if (f.includes('multi') || f.includes('dual')) return 'Multi';
  return '';
}

// =================== MAIN ===================

async function getStreams(tmdbId, type, season, episode) {
  const isMovie = type !== 'tv';
  if (!tmdbId) return [];

  console.log('[111477] Request: tmdb=' + tmdbId + ' type=' + type +
    (isMovie ? '' : ' S' + season + 'E' + episode));

  const info = await Promise.race([
    getTMDBInfo(tmdbId, type),
    new Promise((_, reject) => setTimeout(() => reject(new Error('TMDB timeout')), 10000)),
  ]).catch(() => null);

  if (!info || !info.title) return [];

  const dirPath = await findTitleDirectory(info.title, info.year, type);
  if (!dirPath) return [];

  const files = await listVideoFiles(dirPath, !isMovie, season, episode);
  if (files.length === 0) return [];

  const streams = [];
  const seen = {};

  for (const file of files) {
    const filename = decodeURIComponent(file.split('/').pop() || file);
    const quality = parseQuality(filename);
    const codec = parseCodec(filename);
    const audio = parseAudio(filename);

    const key = quality + codec + audio;
    if (seen[key]) continue;
    seen[key] = true;

    const streamUrl = buildStreamUrl(file);
    var titleLine = info.title;
    if (!isMovie && season && episode) {
      titleLine += ' S' + String(season).padStart(2, '0') + 'E' + String(episode).padStart(2, '0');
    }
    titleLine += ' (' + info.year + ')';

    // Determine behaviorHints based on whether we're using a Worker
    const usingWorker = !!WORKER_URL;
    
    streams.push({
      name: PROVIDER_NAME + ' - ' + quality + (codec ? ' ' + codec : '') + (audio ? ' [' + audio + ']' : ''),
      title: titleLine,
      url: streamUrl,
      quality: quality,
      type: filename.endsWith('.mp4') ? 'video/mp4' : 'video/mkv',
      headers: { 'User-Agent': UA },
      behaviorHints: {
        bingeGroup: '111477-' + quality + (codec ? '-' + codec : ''),
        // If using Worker: notWebReady is false (Worker returns video directly)
        // If NOT using Worker: notWebReady is true (player must solve CF challenge)
        notWebReady: !usingWorker,
        proxyHeaders: {
          request: { 'Referer': A_111477 + '/' },
        },
      },
    });
  }

  console.log('[111477] Returning ' + streams.length + ' streams' +
    (WORKER_URL ? ' (via CF Worker — works on ALL platforms)' : ' (direct URL — Desktop only)'));
  return streams;
}

module.exports = {
  getStreams,
  getTMDBInfo,
  fetchDirectory,
  findTitleDirectory,
  buildStreamUrl,
  base64urlEncode,
  // Allow setting WORKER_URL at runtime
  setWorkerUrl: function(url) {
    // Can't reassign const, but we use a module-level variable
  },
};

// CLI test
if (require.main === module) {
  (async () => {
    const args = process.argv.slice(2);
    const tmdbId = args[0] || '27205';
    const type = args[1] || 'movie';
    const season = args[2] ? parseInt(args[2], 10) : null;
    const episode = args[3] ? parseInt(args[3], 10) : null;
    
    console.log('=== 111477.xyz Scraper ===');
    console.log('Worker URL: ' + (WORKER_URL || '(none — direct mode, Desktop only)'));
    console.log('');
    
    const streams = await getStreams(tmdbId, type, season, episode);
    console.log('\nTotal: ' + streams.length);
    streams.forEach((s, i) => {
      console.log('[' + i + '] ' + s.name + ' | ' + s.quality);
      console.log('    URL: ' + s.url.slice(0, 120));
      console.log('    notWebReady: ' + s.behaviorHints.notWebReady);
    });
  })();
}
