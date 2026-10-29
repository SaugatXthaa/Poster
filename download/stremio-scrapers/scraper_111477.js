/**
 * 111477 Scraper — 100% Pure Node.js (NO Playwright)
 * ===================================================
 * a.111477.xyz is a Cloudflare-hosted CDN directory listing with:
 *   - /movies/ — Movies (BluRay, REMUX, WebDL, 4K, HDR, DoVi)
 *   - /tvs/ — TV Shows
 *   - /kdrama/ — Korean Dramas
 *   - /asiandrama/ — Asian Dramas
 *   - /misc/anime/ — Anime (Dual Audio, HEVC, BD)
 *   - /misc/books/ — Books
 *   - /misc/edu/ — Educational
 *
 * Directory browsing works WITHOUT Cloudflare challenge.
 * File downloads redirect to p.111477.xyz (CF Managed Challenge) — requires browser.
 *
 * This scraper:
 *   1. Searches across ALL categories
 *   2. Lists all available files with full release metadata
 *   3. Returns proxy URLs that work when opened in a browser
 *   4. Extracts rich metadata: quality, source, codecs, audio, size
 *
 * Install: npm install axios
 * Usage:   node scraper_111477.js "dune part two"
 *          node scraper_111477.js "jujutsu kaisen" --category anime
 */

'use strict';

const axios = require('axios');

const BASE = 'https://a.111477.xyz';
const PROXY = 'https://p.111477.xyz/bulk?u=';
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 ' +
           '(KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';

const CATEGORIES = {
  movie: '/movies/',
  tv: '/tvs/',
  kdrama: '/kdrama/',
  asiandrama: '/asiandrama/',
  anime: '/misc/anime/',
};

class Scraper111477 {
  constructor() {
    this.client = axios.create({
      timeout: 20000,
      headers: { 'User-Agent': UA, 'Accept': 'text/html,*/*' },
      maxRedirects: 5,
    });
  }

  /**
   * Search across all categories (or a specific one).
   * The site uses client-side JS filtering, so we fetch the full listing
   * and filter server-side.
   */
  async search(query, category = null) {
    const categories = category ? [category] : Object.keys(CATEGORIES);
    const results = [];

    for (const cat of categories) {
      try {
        const path = CATEGORIES[cat];
        const r = await this.client.get(`${BASE}${path}`);
        const html = r.data;

        // Parse folder links from the directory listing
        const folderPattern = /href="([^"]*\/[^"]*\/)"/g;
        let m;
        while ((m = folderPattern.exec(html)) !== null) {
          const href = m[1];
          // Skip non-content links
          if (href === '/' || href.startsWith('https://') || href.startsWith('#')) continue;
          if (href.includes('cdn-cgi') || href.includes('discord')) continue;

          // Decode the folder name
          const folderName = decodeURIComponent(href.split('/').filter(s => s).pop() || '');

          // Check if it matches the query
          if (folderName.toLowerCase().includes(query.toLowerCase())) {
            const fullUrl = href.startsWith('http') ? href : `${BASE}${href}`;
            results.push({
              title: folderName,
              url: fullUrl,
              category: cat,
              path: href,
            });
          }
        }
      } catch (e) {
        // Skip failed categories
      }
    }

    return results;
  }

  /**
   * Get all files from a folder.
   * Returns array of file objects with metadata.
   */
  async getFiles(folderUrl) {
    const r = await this.client.get(folderUrl);
    const html = r.data;
    const files = [];

    // Parse file links (.mkv, .mp4, .webm)
    const filePattern = /href="([^"]+\.(?:mkv|mp4|webm|m3u8))"/gi;
    let m;
    while ((m = filePattern.exec(html)) !== null) {
      const href = m[1];
      const fileName = decodeURIComponent(href.split('/').pop() || '');

      // Construct the full URL (on a.111477.xyz)
      const fullUrl = href.startsWith('http') ? href : `${BASE}${href.replace(/^\//, '')}`;

      // The download URL redirects to p.111477.xyz proxy
      const proxyUrl = `${PROXY}${encodeURIComponent(fullUrl)}`;

      // Parse metadata from filename
      const meta = this._parseMetadata(fileName);

      files.push({
        name: `111477\n${meta.quality} ${meta.sourceType}`,
        title: fileName,
        url: proxyUrl,           // Browser-openable URL (p.111477.xyz proxy)
        directUrl: fullUrl,      // Direct URL (307 redirects to proxy)
        quality: meta.quality,
        behaviorHints: { notWebReady: true },
        meta: {
          provider: '111477',
          source: meta.sourceType,
          server: 'p.111477.xyz (browser required)',
          quality: meta.quality,
          size: meta.size,
          type: 'mkv',
          audio: meta.audio,
          language: meta.language,
          codecs: meta.codecs,
          filename: fileName,
          releaseGroup: meta.releaseGroup,
          hdr: meta.hdr,
          bitDepth: meta.bitDepth,
          category: this._detectCategory(folderUrl),
          needsBrowser: true,    // Files require browser to download (CF challenge)
        },
      });
    }

    // Also check for subfolders (seasons, etc.)
    const subPattern = /href="([^"]+\/)"/g;
    while ((m = subPattern.exec(html)) !== null) {
      const href = m[1];
      if (href === '/' || href.startsWith('https://') || href.startsWith('#')) continue;
      if (href.includes('cdn-cgi') || href.includes('discord')) continue;
      const folderName = decodeURIComponent(href.split('/').filter(s => s).pop() || '');
      if (folderName && !folderName.includes('.')) {
        // It's a subfolder, not a file
        files.push({
          name: `111477\nFolder: ${folderName}`,
          title: folderName,
          url: href.startsWith('http') ? href : `${BASE}${href.replace(/^\//, '')}`,
          quality: 'unknown',
          behaviorHints: { notWebReady: true },
          meta: {
            provider: '111477',
            source: 'folder',
            type: 'folder',
            filename: folderName,
            category: this._detectCategory(folderUrl),
          },
        });
      }
    }

    return files;
  }

  /** Detect category from URL path. */
  _detectCategory(url) {
    if (url.includes('/movies/')) return 'movie';
    if (url.includes('/tvs/')) return 'tv';
    if (url.includes('/kdrama/')) return 'kdrama';
    if (url.includes('/asiandrama/')) return 'asiandrama';
    if (url.includes('/misc/anime/')) return 'anime';
    return 'unknown';
  }

  /** Parse rich metadata from filename. */
  _parseMetadata(fn) {
    const upper = fn.toUpperCase();

    // Quality
    let quality = 'unknown';
    if (upper.includes('2160P') || upper.includes('4K') || upper.includes('UHD')) quality = '2160p';
    else if (upper.includes('1080P')) quality = '1080p';
    else if (upper.includes('720P')) quality = '720p';
    else if (upper.includes('480P')) quality = '480p';

    // Source type
    let sourceType = 'unknown';
    if (upper.includes('REMUX')) sourceType = 'BluRay Remux';
    else if (upper.includes('BLURAY') || upper.includes('BLU-RAY') || upper.includes('BDRIP')) sourceType = 'BluRay';
    else if (upper.includes('WEB-DL') || upper.includes('WEBDL')) sourceType = 'WebDL';
    else if (upper.includes('WEBRIP') || upper.includes('WEB-RIP')) sourceType = 'WebRip';
    else if (upper.includes('HDRIP')) sourceType = 'HDRip';
    else if (upper.includes('BATCH')) sourceType = 'Batch';

    // Audio
    let audio = 'unknown';
    let language = ['en'];
    if (upper.includes('DUAL') || upper.includes('MULTI')) {
      audio = 'multi';
      language = ['en', 'ja']; // or other languages
    }
    if (upper.includes('HINDI')) { audio = 'hindi-english'; language = ['hi', 'en']; }
    if (upper.includes('KOREAN')) { language = ['ko']; }

    // Codecs
    const codecs = [];
    if (upper.includes('HEVC') || upper.includes('X265') || upper.includes('H265')) codecs.push('HEVC');
    if (upper.includes('X264') || upper.includes('H264') || upper.includes('AVC')) codecs.push('H264');
    if (upper.includes('AV1')) codecs.push('AV1');
    if (upper.includes('TRUEHD')) codecs.push('TrueHD');
    if (upper.includes('ATMOS')) codecs.push('Atmos');
    if (upper.includes('DDP') || upper.includes('DD+')) codecs.push('DD+');
    if (upper.includes('DDP5') || upper.includes('DD5')) codecs.push('DD');
    if (upper.includes('DTS')) codecs.push('DTS');
    if (upper.includes('AAC')) codecs.push('AAC');
    if (upper.includes('AC3')) codecs.push('AC3');
    if (upper.includes('EAC3') || upper.includes('E-AC-3')) codecs.push('EAC3');
    if (upper.includes('OPUS')) codecs.push('Opus');

    // HDR
    let hdr = null;
    if (upper.includes('DOVI') || upper.includes('DOLBY.VISION')) hdr = 'Dolby Vision';
    else if (upper.includes('HDR10+')) hdr = 'HDR10+';
    else if (upper.includes('HDR')) hdr = 'HDR';
    if (hdr) codecs.push(hdr);

    // Bit depth
    let bitDepth = null;
    if (upper.includes('10BIT') || upper.includes('10-BIT') || upper.includes('10BIT')) bitDepth = '10-bit';
    else if (upper.includes('8BIT') || upper.includes('8-BIT')) bitDepth = '8-bit';

    // Release group
    let releaseGroup = null;
    const rgMatch = fn.match(/[-~]\s*([A-Za-z0-9]+)$/);
    if (rgMatch) releaseGroup = rgMatch[1];

    // Size (sometimes in filename)
    let size = null;
    const sizeMatch = fn.match(/([\d.]+\s*(?:GB|MB))/i);
    if (sizeMatch) size = sizeMatch[1];

    return { quality, sourceType, audio, language, codecs, hdr, bitDepth, releaseGroup, size };
  }
}

// CLI
if (require.main === module) (async () => {
  const s = new Scraper111477();
  const query = process.argv[2] || 'dune';
  const catFlag = process.argv.indexOf('--category');
  const category = catFlag > -1 ? process.argv[catFlag + 1] : null;

  console.log(`111477 — searching for "${query}"${category ? ` in ${category}` : ' (all categories)'}...`);
  const results = await s.search(query, category);
  console.log(`\nFound ${results.length} result(s):\n`);

  for (const r of results.slice(0, 10)) {
    console.log(`  [${r.category}] ${r.title}`);
    console.log(`    ${r.url}`);
  }

  if (results.length) {
    console.log(`\nFetching files from: ${results[0].title}`);
    const files = await s.getFiles(results[0].url);
    console.log(`\n${files.length} file(s):\n`);
    for (const f of files.slice(0, 10)) {
      const m = f.meta;
      console.log(`  [${m.quality}] ${m.source} ${m.codecs?.join(',') || ''} audio=${m.audio || '?'}`);
      console.log(`    File: ${m.filename?.slice(0, 80)}`);
      console.log(`    URL:  ${f.url.slice(0, 80)}...`);
      console.log();
    }
  }
})();

module.exports = { Scraper111477 };
