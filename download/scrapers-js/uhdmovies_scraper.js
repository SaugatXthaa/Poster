/**
 * UHDMovies Scraper — 100% Pure Node.js (NO Playwright)
 * ======================================================
 * uhdmovies.autos is a WordPress site offering 4K/1080p movies and TV shows
 * with dual audio (Hindi-English), BluRay/Web-DL quality, and rich metadata.
 *
 * This scraper extracts ALL download links from movie pages with full metadata:
 *   - Quality: 2160p / 1080p / 720p / 480p
 *   - Source: BluRay Remux / BluRay / WebDL / WebRip / HDRip
 *   - Codecs: HEVC/x265, x264, HDR, Dolby Vision, TrueHD, Atmos, DDP, 10-bit
 *   - Audio: Hindi-English dual, English only
 *   - Size: GB/MB
 *   - Filename: full release name
 *
 * Download URLs point to cloud.unblockedgames.world (GDrive proxy).
 * These open in a browser and redirect to Google Drive.
 *
 * Install: npm install axios
 * Usage:   node uhdmovies_scraper.js "dune part two"
 *          node uhdmovies_scraper.js "dune" --tv 1 1
 */

'use strict';

const axios = require('axios');

const BASE = 'https://uhdmovies.autos';
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 ' +
           '(KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';

class UHDMoviesScraper {
  constructor() {
    this.client = axios.create({
      timeout: 15000,
      headers: { 'User-Agent': UA, 'Accept': 'text/html,*/*' },
      maxRedirects: 5,
    });
  }

  /**
   * Search for movies/TV shows by title.
   * Returns array of { title, url, slug } objects.
   */
  async search(query) {
    const r = await this.client.get(`${BASE}/?s=${encodeURIComponent(query)}`);
    const html = r.data;
    const results = [];
    const seen = new Set();

    // WordPress search results contain links like:
    // /download-<title>-<year>-<quality>-<audio>-<codec>-<source>-esubs/
    const matches = html.match(/href="(https:\/\/uhdmovies\.autos\/download-[^"]+)"/g) || [];
    for (const m of matches) {
      const urlMatch = m.match(/href="([^"]+)"/);
      if (!urlMatch) continue;
      const url = urlMatch[1];
      if (seen.has(url)) continue;
      seen.add(url);

      // Extract title from URL slug
      const slug = url.split('/').filter(s => s).pop() || '';
      const title = this._parseTitleFromSlug(slug);
      results.push({ title, url, slug });
    }

    return results;
  }

  /** Parse a human-readable title from the URL slug. */
  _parseTitleFromSlug(slug) {
    return slug
      .replace(/^download-/, '')
      .replace(/-esubs\/?$/, '')
      .replace(/-\d{4}.*$/, m => ' (' + m.match(/\d{4}/)[0] + ')')
      .replace(/-/g, ' ')
      .trim();
  }

  /**
   * Get all download streams from a movie/TV page.
   * Returns array of Stremio-compatible stream objects with rich metadata.
   */
  async getStreams(pageUrl) {
    const r = await this.client.get(pageUrl);
    const html = r.data;

    // Extract the page title
    const titleMatch = html.match(/<h1[^>]*>([^<]+)<\/h1>/i);
    const pageTitle = titleMatch ? titleMatch[1].trim() : 'Unknown';

    // Find all download sections
    // Each section has: filename (.mkv/.mp4), size (GB/MB), and one or more download buttons
    const sections = this._parseDownloadSections(html);

    // Also check for TV episode links
    const tvLinks = this._parseTvLinks(html);

    const streams = [];

    for (const section of sections) {
      for (const dlUrl of section.downloadUrls) {
        streams.push({
          name: `UHDMovies\n${section.quality} ${section.sourceType}`,
          title: section.filename,
          url: dlUrl,
          quality: section.quality,
          behaviorHints: { notWebReady: true },
          meta: {
            provider: 'UHDMovies',
            source: section.sourceType,
            server: 'GDrive (cloud.unblockedgames.world)',
            quality: section.quality,
            size: section.size,
            type: 'mp4',
            audio: section.audio,
            language: section.language,
            codecs: section.codecs,
            filename: section.filename,
            releaseGroup: section.releaseGroup,
            hdr: section.hdr,
            bitDepth: section.bitDepth,
            tmdbId: null,
            mediaType: tvLinks.length > 0 ? 'tv' : 'movie',
          },
        });
      }
    }

    // Add TV episode links if present
    for (const ep of tvLinks) {
      streams.push({
        name: `UHDMovies\nEpisode ${ep.episode}`,
        title: `${pageTitle} - Episode ${ep.episode}`,
        url: ep.url,
        quality: 'unknown',
        behaviorHints: { notWebReady: true },
        meta: {
          provider: 'UHDMovies',
          source: 'Episode Page',
          server: 'UHDMovies',
          quality: 'unknown',
          type: 'page',
          mediaType: 'tv',
          episode: ep.episode,
        },
      });
    }

    return streams;
  }

  /**
   * Parse download sections from the movie page HTML.
   * Handles two formats:
   *   1. Filename with .mkv extension (e.g., "Dune.Part.Two.2024.UHD.Blu-Ray.2160p.REMUX.HDR.DoVi...mkv")
   *   2. Plain text description (e.g., "Inception (2010) 2160p HDR 10bit Bluray HEVC [DD 5.1 Hindi + DD 7.1 English] x265")
   */
  _parseDownloadSections(html) {
    const sections = [];

    // Find all GDrive download buttons
    const buttonPattern = /class="maxbutton[^"]*maxbutton-download-g-drive"/gi;
    const buttons = [];
    let bm;
    while ((bm = buttonPattern.exec(html)) !== null) {
      buttons.push({ start: bm.index, end: bm.index + bm[0].length });
    }

    // Group buttons (3 per quality section: GDrive, Telegram, Stream)
    // We only take the GDrive button (first in each group)
    for (let i = 0; i < buttons.length; i++) {
      // Check if this is a GDrive button (every 3rd: 0, 3, 6, ...)
      if (i % 3 !== 0) continue;

      const btn = buttons[i];
      // Get text before this button for metadata
      const prevEnd = i > 0 ? buttons[i - 1].end : 0;
      const before = html.slice(Math.max(0, btn.start - 1500), btn.start);

      // Strip HTML tags
      const text = before.replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ').trim();

      // Extract the download URL
      const urlMatch = html.slice(btn.start - 200, btn.start + 500).match(/href="(https:\/\/cloud\.unblockedgames\.world\/\?sid=[^"]+)"/);
      if (!urlMatch) continue;
      const dlUrl = urlMatch[1];

      // Extract size (e.g., "17.5GB", "64.04 GB")
      const sizeMatch = text.match(/([\d.]+\s*(?:GB|MB))/i);
      const size = sizeMatch ? sizeMatch[1].replace(/\s+/g, '') : 'Unknown';

      // Extract filename or description (last 200 chars of text before size)
      let filename = text;
      if (sizeMatch) {
        filename = text.slice(0, text.lastIndexOf(sizeMatch[1])).trim();
      }
      // Clean up filename
      filename = filename.replace(/^.*?Download\s*(?:\(G-Drive\)\s*)?/i, '').trim();
      if (filename.length > 200) filename = filename.slice(-200);

      // Try to find .mkv/.mp4 in the text
      const extMatch = text.match(/([A-Za-z0-9][A-Za-z0-9._\-\[\]()~+]*\.(?:mkv|mp4))/i);
      if (extMatch) filename = extMatch[1];

      // Parse metadata from the text
      const meta = this._parseFilenameMetadata(filename + ' ' + text.slice(-200));

      sections.push({
        filename: filename || 'Unknown',
        size,
        downloadUrls: [dlUrl],
        ...meta,
      });
    }

    return sections;
  }

  /** Parse rich metadata from a release filename. */
  _parseFilenameMetadata(fn) {
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
    else if (upper.includes('BLU-RAY') || upper.includes('BLURAY') || upper.includes('BDRIP')) sourceType = 'BluRay';
    else if (upper.includes('WEB-DL') || upper.includes('WEBDL')) sourceType = 'WebDL';
    else if (upper.includes('WEBRIP') || upper.includes('WEB-RIP')) sourceType = 'WebRip';
    else if (upper.includes('HDRIP')) sourceType = 'HDRip';

    // Audio
    let audio = 'unknown';
    let language = ['en'];
    if (upper.includes('HINDI') && upper.includes('ENGLISH')) {
      audio = 'hindi-english';
      language = ['hi', 'en'];
    } else if (upper.includes('HINDI')) {
      audio = 'hindi';
      language = ['hi'];
    } else if (upper.includes('ENGLISH')) {
      audio = 'english';
      language = ['en'];
    }
    if (upper.includes('DUAL') && audio === 'unknown') {
      audio = 'dual';
      language = ['hi', 'en'];
    }

    // Codecs
    const codecs = [];
    if (upper.includes('HEVC') || upper.includes('X265')) codecs.push('HEVC');
    if (upper.includes('X264') || upper.includes('AVC')) codecs.push('x264');
    if (upper.includes('AV1')) codecs.push('AV1');
    if (upper.includes('TRUEHD')) codecs.push('TrueHD');
    if (upper.includes('ATMOS')) codecs.push('Atmos');
    if (upper.includes('DDP') || upper.includes('DD+') || upper.includes('DDP5')) codecs.push('DD+');
    if (upper.includes('DD5') || upper.includes('DDP7') || upper.includes('DDP2')) codecs.push('DD');
    if (upper.includes('DTS')) codecs.push('DTS');
    if (upper.includes('AAC')) codecs.push('AAC');
    if (upper.includes('AC3')) codecs.push('AC3');

    // HDR / Dolby Vision
    let hdr = null;
    if (upper.includes('DOVI') || upper.includes('DOLBY VISION')) hdr = 'Dolby Vision';
    else if (upper.includes('HDR10+')) hdr = 'HDR10+';
    else if (upper.includes('HDR')) hdr = 'HDR';
    if (hdr) codecs.push(hdr);

    // Bit depth
    let bitDepth = null;
    if (upper.includes('10BIT') || upper.includes('10-BIT') || upper.includes('10BIT')) bitDepth = '10-bit';
    else if (upper.includes('8BIT') || upper.includes('8-BIT')) bitDepth = '8-bit';

    // Release group (text after ~ or at the end in parentheses)
    let releaseGroup = null;
    const rgMatch = fn.match(/~\(([^)]+)\)/);
    if (rgMatch) releaseGroup = rgMatch[1];

    return { quality, sourceType, audio, language, codecs, hdr, bitDepth, releaseGroup };
  }

  /** Parse TV episode links from the page. */
  _parseTvLinks(html) {
    const episodes = [];
    const pattern = /href="(https:\/\/uhdmovies\.autos\/download-[^"]*s\d+e\d+[^"]*)"/gi;
    let m;
    while ((m = pattern.exec(html)) !== null) {
      const url = m[1];
      const epMatch = url.match(/s(\d+)e(\d+)/i);
      if (epMatch) {
        episodes.push({ episode: parseInt(epMatch[2]), url });
      }
    }
    return episodes;
  }

  /**
   * High-level: search by title and get streams.
   * @param {string} query - Movie/show title
   * @returns {Promise<Array>} Stream objects
   */
  async searchAndGetStreams(query) {
    const results = await this.search(query);
    if (!results.length) return [];

    // Pick the best match (first result)
    const best = results[0];
    return this.getStreams(best.url);
  }
}

// CLI
if (require.main === module) (async () => {
  const s = new UHDMoviesScraper();
  const query = process.argv[2] || 'dune part two';

  console.log(`UHDMovies — searching for "${query}"...`);
  const results = await s.search(query);
  console.log(`Found ${results.length} result(s):`);
  for (const r of results.slice(0, 5)) {
    console.log(`  ${r.title}  →  ${r.url.slice(0, 80)}...`);
  }

  if (results.length) {
    console.log(`\nFetching streams from: ${results[0].title}`);
    const streams = await s.getStreams(results[0].url);
    console.log(`\n${streams.length} stream(s):\n`);
    for (const st of streams) {
      const m = st.meta;
      console.log(`  [${m.quality}] ${m.source} ${m.size} ${m.codecs.join(',')} audio=${m.audio}`);
      console.log(`    File: ${m.filename?.slice(0, 80) || '?'}`);
      console.log(`    URL:  ${st.url.slice(0, 80)}...`);
      console.log();
    }
  }
})();

module.exports = { UHDMoviesScraper };

// ============================================================================
// CLOUD.UNBLOCKEDGAMES.WORLD RESOLUTION NOTE
// ============================================================================
// The cloud.unblockedgames.world URLs use Cloudflare JS Detection mode which
// sets a cookie via JavaScript (document.cookie in a hidden iframe). This cookie
// is required for the POST form submission to work.
//
// Without JavaScript execution, the POST always returns the homepage instead of
// the download page. The sid parameter is AES-encrypted (192 bytes) and cannot
// be decrypted without the server-side key.
//
// APPROACHES TRIED (all failed):
// 1. Node.js axios with maxRedirects:0 → 403 on gdflix.io (TLS fingerprint)
// 2. System curl with full browser headers → POST returns homepage (no CF cookie)
// 3. Python urllib with cookie jar → same as curl (no CF cookie)
// 4. HTTP/1.1 forced → same result
// 5. CF challenge-platform endpoint → 404
// 6. Sid parameter decoding (base64 → AES-encrypted binary) → can't decrypt
//
// RESULT: The download URLs are returned as-is. They open in a browser and
// auto-redirect to Google Drive. For Stremio, these would be "external" links
// that need to be opened in a browser, not played directly.
// ============================================================================
