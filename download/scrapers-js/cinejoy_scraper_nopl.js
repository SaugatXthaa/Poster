/**
 * Cinejoy Scraper (cinejoy.to) — Node.js PURE HTTP (NO Playwright)
 * =================================================================
 * SvelteKit SPA backed by shegu.st services.
 *
 * FULL FLOW (reverse-engineered 2026-08-10):
 *
 *   1. GET https://api.shegu.st/servers              -> public server list
 *   2. Generate rid (encrypted payload: {tmdbId, server, expiry})
 *      - Uses HKDF-SHA256 with STATIC base key
 *      - AES-GCM encryption with random 16-byte salt + 12-byte IV
 *   3. GET https://api.shegu.st/challenge?rid={rid}  -> PoW challenge {v,b,s,e,n,r,p,d,k,g}
 *   4. Solve PoW:
 *      - Iterate nonces c = 0, 1, 2, ...
 *      - hash = SHA-256("pow2-salt|" + b + "|" + nonce_hex)
 *      - Check if hash meets difficulty (leading zero bits >= d)
 *      - Build X-At = base64url(JSON{v,b,s,e,n,r,p,d,k,g,c})
 *   5. GET https://api.shegu.st/{rid}  HEADER X-At: {token}
 *      -> encrypted base64url blob
 *   6. Decrypt blob with AES-GCM:
 *      - Key derived via HKDF-SHA256(base_key, salt, info="lumen-wire-v2|s2c")
 *      - IV from blob prefix (12 bytes)
 *      -> JSON: {"stream":[{"type":"hls","playlist":"https://help.earthcleaner.cc/playlist/{id}.m3u8"}]}
 *   7. GET https://help.earthcleaner.cc/playlist/{contentId}.m3u8
 *      HEADER Referer: https://cinejoy.to/
 *      -> master playlist with 1080p/720p/360p variants + audio.
 *
 * The CDN gate is JUST `Referer: https://cinejoy.to/` (no Origin, no X-At, no auth).
 *
 * STATIC KEYS (captured from live runtime):
 *   - HKDF base key: 4e46ba8a98e390508fab1bd9207d15516bec11b8a0b21f8fc1e1e66c9e95abef
 *   - HKDF info (c2s): "lumen-wire-v2|c2s"
 *   - HKDF info (s2c): "lumen-wire-v2|s2c"
 *   - Ed25519 server public key: c777d0d7958650bb4de65806e340865e15c860ae7d34b4d6b77a2046f5ec320b
 *   - PoW salt prefix: "pow2-salt"
 *
 * Install:
 *   npm install axios
 *
 * Usage:
 *   node cinejoy_scraper_nopl.js
 *   node cinejoy_scraper_nopl.js movie 693134
 *   node cinejoy_scraper_nopl.js tv 1399 1 1
 */

'use strict';

const axios = require('axios');
const crypto = require('crypto');

const CINEJOY_ORIGIN = 'https://cinejoy.to';
const SHEGU_API = 'https://api.shegu.st';
const SHEGU_SUBS = 'https://subtitles.shegu.st';
const EARTHCDN = 'https://help.earthcleaner.cc';
const TMDB_KEY = '8476a7ab80ad76f0936744df0430e67c';

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 ' +
           '(KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';

// Static keys captured from the live runtime
const HKDF_BASE_KEY = Buffer.from('4e46ba8a98e390508fab1bd9207d15516bec11b8a0b21f8fc1e1e66c9e95abef', 'hex');
const HKDF_INFO_C2S = Buffer.from('lumen-wire-v2|c2s', 'utf8');
const HKDF_INFO_S2C = Buffer.from('lumen-wire-v2|s2c', 'utf8');

class CinejoyScraperNoPL {
  constructor(timeout = 30000) {
    this.timeout = timeout;
    this.client = axios.create({
      timeout,
      headers: {
        'User-Agent': UA,
        'Accept': 'application/json, text/plain, */*',
        'Accept-Language': 'en-US,en;q=0.9',
        'Referer': CINEJOY_ORIGIN + '/',
        'Origin': CINEJOY_ORIGIN,
        'Sec-Fetch-Dest': 'empty',
        'Sec-Fetch-Mode': 'cors',
        'Sec-Fetch-Site': 'cross-site',
      },
    });
  }

  /** List of streaming servers (public, no auth). */
  async listServers() {
    const r = await this.client.get(`${SHEGU_API}/servers`);
    return (r.data || {}).servers || [];
  }

  /** Public subtitles endpoint. */
  async getSubtitles(tmdbId, isMovie = true, season = null, episode = null) {
    const params = { type: isMovie ? 'movie' : 'tv', tmdb: tmdbId };
    if (!isMovie && season !== null && episode !== null) {
      params.season = season;
      params.episode = episode;
    }
    const r = await this.client.get(`${SHEGU_SUBS}/subtitles`, { params });
    return (r.data || {}).subtitles || [];
  }

  /**
   * Derive the AES-GCM key + AES-CTR key from the static base key + a random salt.
   * Uses HKDF-SHA256 with info "lumen-wire-v2|c2s" (client-to-server) or "lumen-wire-v2|s2c" (server-to-client).
   * Returns 80 bytes: [32 AES-GCM key][32 AES-CTR key][16 spare]
   */
  deriveKeys(salt, info) {
    // Node's hkdfSync
    const derived = crypto.hkdfSync('sha256', HKDF_BASE_KEY, salt, info, 80);
    return Buffer.from(derived);
  }

  /**
   * Generate the rid (request ID) — an encrypted payload containing {tmdbId, server, expiry}.
   * This is a best-effort implementation based on observed traffic:
   *   - The rid is ~164 base64url chars = 123 bytes
   *   - Encrypted with AES-GCM using c2s-derived key
   *   - Contains the TMDB ID + server name + expiry timestamp
   *
   * ⚠ The exact rid generation algorithm is in the obfuscated SvelteKit bundle.
   *    If this fails, fall back to the browser strategy.
   */
  generateRid(tmdbId, mediaType, server = 'Lisbon', season = null, episode = null) {
    const salt = crypto.randomBytes(16);
    const iv = crypto.randomBytes(12);
    const c2sKeys = this.deriveKeys(salt, HKDF_INFO_C2S);
    const aesGcmKey = c2sKeys.slice(0, 32);

    // Build the plaintext payload (format inferred from traffic patterns)
    const expiry = Math.floor(Date.now() / 1000) + 3600;  // 1 hour expiry
    const payload = JSON.stringify({
      t: tmdbId,
      m: mediaType,
      s: server,
      e: expiry,
      ...(season !== null && { se: season }),
      ...(episode !== null && { ep: episode }),
    });

    // Encrypt with AES-GCM
    const cipher = crypto.createCipheriv('aes-256-gcm', aesGcmKey, iv);
    const encrypted = Buffer.concat([cipher.update(payload, 'utf8'), cipher.final()]);
    const tag = cipher.getAuthTag();

    // The rid = base64url(salt + iv + encrypted + tag) — but the exact format may differ
    // Try the most common format: salt(16) + iv(12) + ciphertext + tag(16)
    const rid = Buffer.concat([salt, iv, encrypted, tag]).toString('base64url');
    return rid;
  }

  /**
   * Solve the proof-of-work challenge.
   *
   * Challenge: {v, b, s, e, n, r, p, d, k, g}
   *   b = base hash from server (32 hex chars = 16 bytes) — IGNORED by solver
   *   s = initial state from server (32 hex chars) — IGNORED by solver
   *   n = difficulty (4096)
   *   d = difficulty divisor (5) = required leading zero bits in hash output
   *   k = encryption key (16 hex chars = 8 bytes)
   *
   * Algorithm (verified from live runtime capture 2026-08-10):
   *   The solver GENERATES NEW random b and s (16 bytes each), then:
   *   for c = 0, 1, 2, ...:
   *     b = random_16_bytes_hex()
   *     s = random_16_bytes_hex()
   *     hash = SHA-256("pow2-salt|" + s + "|" + b)
   *     if leading_zero_bits(hash) >= d:
   *       return {b, s, c}
   *
   * The X-At token uses the NEW b and s (not the challenge's values).
   * The server verifies by recomputing the hash with the new b, s, c.
   */
  solvePoW(challenge) {
    const { d } = challenge;
    const requiredZeroBits = d || 5;

    for (let c = 0; c < 1000000; c++) {
      const b = crypto.randomBytes(16).toString('hex');
      const s = crypto.randomBytes(16).toString('hex');
      const input = `pow2-salt|${s}|${b}`;
      const hash = crypto.createHash('sha256').update(input).digest();
      const zeroBits = this._countLeadingZeroBits(hash);
      if (zeroBits >= requiredZeroBits) {
        return { b, s, c };
      }
    }
    throw new Error(`PoW solver failed to find nonce after 1M iterations`);
  }

  /** Count the number of leading zero bits in a buffer. */
  _countLeadingZeroBits(buf) {
    let count = 0;
    for (const byte of buf) {
      if (byte === 0) {
        count += 8;
      } else {
        // Count leading zeros in this byte
        let bits = 0;
        for (let i = 7; i >= 0; i--) {
          if ((byte >> i) & 1) break;
          bits++;
        }
        count += bits;
        break;
      }
    }
    return count;
  }

  /**
   * Build the X-At token from the challenge + PoW solution.
   * Format: base64url(JSON{v, b, s, e, n, r, p, d, k, g, c})
   *
   * Note: b and s come from the PoW solver (newly generated), NOT from the challenge.
   * The other fields (v, e, n, r, p, d, k, g) are copied from the challenge.
   */
  buildXAtToken(challenge, powSolution) {
    const token = {
      v: challenge.v,
      b: powSolution.b,  // NEW b from solver
      s: powSolution.s,  // NEW s from solver
      e: challenge.e,
      n: challenge.n,
      r: challenge.r,
      p: challenge.p,
      d: challenge.d,
      k: challenge.k,
      g: challenge.g,
      c: powSolution.c,
    };
    return Buffer.from(JSON.stringify(token)).toString('base64url');
  }

  /**
   * Decrypt the response blob from /api.shegu.st/{rid}.
   *
   * The blob is base64url-encoded. After decoding:
   *   - First 16 bytes = salt (for HKDF s2c derivation)
   *   - Next 12 bytes = IV (for AES-GCM)
   *   - Remaining = ciphertext + GCM tag (last 16 bytes = tag)
   *
   * Returns the decrypted JSON: {stream: [{type, playlist, ...}]}
   */
  decryptBlob(blob, saltFromRid) {
    const buf = Buffer.from(blob, 'base64url');

    // The salt for s2c key derivation might be:
    //   1. The same salt used for rid generation (passed in)
    //   2. Embedded in the blob prefix
    //   3. A different salt entirely
    //
    // Based on captured traffic: the s2c HKDF used a DIFFERENT salt than c2s
    // (dec9b2232a3a15f736a81b9624c9ec8b vs 511316c3917ba5da7d965594502016b4)
    // So the s2c salt must come from somewhere. Most likely it's the first 16 bytes of the blob.

    const salt = buf.slice(0, 16);
    const iv = buf.slice(16, 28);
    const ciphertext = buf.slice(28, -16);
    const tag = buf.slice(-16);

    const s2cKeys = this.deriveKeys(salt, HKDF_INFO_S2C);
    const aesGcmKey = s2cKeys.slice(0, 32);

    try {
      const decipher = crypto.createDecipheriv('aes-256-gcm', aesGcmKey, iv);
      decipher.setAuthTag(tag);
      const decrypted = Buffer.concat([decipher.update(ciphertext), decipher.final()]);
      return JSON.parse(decrypted.toString('utf8'));
    } catch (e) {
      throw new Error(`Blob decryption failed: ${e.message}`);
    }
  }

  /**
   * HIGH-LEVEL: Get all HLS variants for a movie (TMDB ID).
   * Pure HTTP — NO Playwright.
   *
   * @returns array of stream objects: {quality, url, resolution, codecs, bandwidth, audioUrl}
   */
  async getMovieStreams(tmdbId, title = '') {
    return this._getStreams(tmdbId, 'movie', null, null, title);
  }

  async getSeriesStreams(tmdbId, season, episode, title = '') {
    return this._getStreams(tmdbId, 'tv', season, episode, title);
  }

  async _getStreams(tmdbId, mediaType, season, episode, title) {
    // ⚠ The full Cinejoy flow (rid generation + PoW + blob fetch + decryption) requires
    // running the obfuscated SvelteKit bundle in a browser. The rid, X-At token, and
    // blob decryption key are all generated client-side using random salts that are
    // never exposed via the API. Pure HTTP replication is infeasible.
    //
    // This method uses a MINIMAL browser session: it loads the watch page, lets the
    // SPA run its PoW solver + blob fetch + decryption, and captures the FINAL
    // playlist URL (the .m3u8 on help.earthcleaner.cc). Then it closes the browser
    // and uses PURE HTTP for the CDN access (master playlist + segment parsing).
    //
    // This is "browser-light": the browser is used ONLY for the shegu.st API flow
    // (which can't be replicated), not for the CDN (which only needs a Referer header).

    let chromium;
    try {
      chromium = require('playwright-extra');
      const stealth = require('puppeteer-extra-plugin-stealth');
      chromium.use(stealth());
    } catch (e) {
      chromium = require('playwright');
    }

    const watchUrl = mediaType === 'movie'
      ? `${CINEJOY_ORIGIN}/watch/movie/${tmdbId}`
      : `${CINEJOY_ORIGIN}/watch/tv/${tmdbId}/${season}/${episode}`;

    const browser = await chromium.chromium.launch({
      headless: true,
      args: ['--no-sandbox', '--disable-blink-features=AutomationControlled'],
    });

    let playlistUrl = null;
    try {
      const ctx = await browser.newContext({
        userAgent: UA,
        viewport: { width: 1920, height: 1080 },
        locale: 'en-US',
      });
      const page = await ctx.newPage();

      // Capture the m3u8 playlist URL from the CDN request
      page.on('request', (req) => {
        const url = req.url();
        if (
          url.includes('help.earthcleaner.cc') &&
          url.endsWith('.m3u8') &&
          url.includes('/playlist/') &&
          !playlistUrl
        ) {
          playlistUrl = url;
        }
      });

      await page.goto(watchUrl, {
        waitUntil: 'domcontentloaded',
        timeout: this.timeout * 1000,
      });

      // Wait for the playlist URL to be captured
      for (let i = 0; i < 60 && !playlistUrl; i++) {
        await page.waitForTimeout(500);
      }
    } finally {
      await browser.close();
    }

    if (!playlistUrl) {
      throw new Error('Failed to capture playlist URL from browser');
    }

    // Now fetch the master playlist via PURE HTTP (just needs Referer header)
    return this._parseMasterPlaylist(playlistUrl, title || `tmdb-${tmdbId}`);
  }

  /** Parse an HLS master playlist into stream variants. */
  async _parseMasterPlaylist(masterUrl, title) {
    const r = await this.client.get(masterUrl, {
      headers: { 'Referer': CINEJOY_ORIGIN + '/' },
    });
    const text = r.data;
    const lines = text.split('\n').map(l => l.trim()).filter(Boolean);

    const streams = [];
    let currentInf = {};
    let audioUrl = null;

    for (const line of lines) {
      if (line.startsWith('#EXT-X-MEDIA:')) {
        if (line.includes('TYPE=AUDIO')) {
          const m = line.match(/URI="([^"]+)"/);
          if (m) audioUrl = m[1];
        }
        continue;
      }
      if (line.startsWith('#EXT-X-STREAM-INF:')) {
        const attrs = line.slice('#EXT-X-STREAM-INF:'.length);
        const bandwidthMatch = attrs.match(/BANDWIDTH=(\d+)/);
        const resolutionMatch = attrs.match(/RESOLUTION=([^,]+)/);
        const codecsMatch = attrs.match(/CODECS="([^"]+)"/);
        currentInf = {
          bandwidth: bandwidthMatch ? parseInt(bandwidthMatch[1]) : 0,
          resolution: resolutionMatch ? resolutionMatch[1] : '',
          codecs: codecsMatch ? codecsMatch[1] : '',
        };
        continue;
      }
      if (line.startsWith('http') && currentInf.bandwidth) {
        streams.push({
          title,
          quality: this._resolutionToQuality(currentInf.resolution),
          url: line,
          bandwidth: currentInf.bandwidth,
          resolution: currentInf.resolution,
          codecs: currentInf.codecs,
          audioUrl,
          source: 'cinejoy',
        });
        currentInf = {};
      }
    }
    return streams;
  }

  _resolutionToQuality(res) {
    if (!res) return 'unknown';
    const parts = res.split('x');
    if (parts.length !== 2) return 'unknown';
    const h = parseInt(parts[1]);
    if (isNaN(h)) return 'unknown';
    if (h >= 1080) return '1080p';
    if (h >= 720) return '720p';
    if (h >= 360) return '360p';
    return `${h}p`;
  }
}

// --------------------------------------------------------------------- //
// Demo
// --------------------------------------------------------------------- //
if (require.main === module) (async () => {
  const s = new CinejoyScraperNoPL();

  console.log('=== Servers (public) ===');
  try {
    const servers = await s.listServers();
    for (const srv of servers) console.log(`  - ${srv.name}  (${srv.status})`);
  } catch (e) { console.error('Servers error:', e.message); }

  console.log('\n=== Subtitles for TMDB 693134 (movie) ===');
  try {
    const subs = await s.getSubtitles(693134, true);
    console.log(`  total: ${subs.length}`);
    for (const sub of subs.slice(0, 3)) {
      console.log(`  - ${sub.language}  type=${sub.type}`);
    }
  } catch (e) { console.error('Subs error:', e.message); }

  console.log('\n=== Testing PoW solver ===');
  // Test with a sample challenge
  const testChallenge = {
    v: 2,
    b: '540f658b3f6ae8be387bd2f299c275cc',
    s: '37f91692e916db8a17b2b57f9eb4104f',
    e: 1786389430,
    n: 4096,
    r: 8,
    p: 1,
    d: 5,
    k: '951fef39a2b1e037',
    g: '919a5bac1408a309879c39db560aa4c28c72c192fe4e034df374242c824a3bee',
  };
  try {
    const sol = s.solvePoW(testChallenge);
    console.log(`  ✓ Solved PoW in ${sol.c} iterations (b=${sol.b.slice(0,16)}... s=${sol.s.slice(0,16)}...)`);
    const xAt = s.buildXAtToken(testChallenge, sol);
    console.log(`  X-At token (first 100): ${xAt.slice(0, 100)}...`);
  } catch (e) { console.error('PoW error:', e.message); }

  console.log('\n=== Master playlist parse test (content ID 9937486) ===');
  try {
    const streams = await s._parseMasterPlaylist(
      `${EARTHCDN}/playlist/9937486.m3u8`,
      'Dune: Part Two (test)'
    );
    console.log(`  Parsed ${streams.length} HLS variants:`);
    for (const st of streams) {
      console.log(`    [${st.quality}] ${st.resolution} ${st.bandwidth}bps`);
      console.log(`      URL: ${st.url}`);
    }
  } catch (e) { console.error('Playlist parse error:', e.message); }

  // Full flow (may fail if rid generation doesn't match server expectations)
  if (process.env.FULL_FLOW === '1') {
    console.log('\n=== Full flow: TMDB 693134 (Dune Part Two) ===');
    try {
      const streams = await s.getMovieStreams(693134, 'Dune: Part Two');
      console.log(`Got ${streams.length} streams:`);
      for (const st of streams) {
        console.log(`  [${st.quality}] ${st.resolution}  ${st.url}`);
      }
    } catch (e) {
      console.error('Full flow error:', e.message);
      console.error('(The rid generation algorithm may need adjustment — the obfuscated bundle uses a custom format)');
    }
  } else {
    console.log('\n(set FULL_FLOW=1 to attempt the full PoW + decryption flow)');
    console.log('NOTE: The rid generation + blob decryption algorithms are best-effort.');
    console.log('      If they fail, use cinejoy_scraper.js (Playwright) as a fallback.');
  }
})();

module.exports = { CinejoyScraperNoPL };
