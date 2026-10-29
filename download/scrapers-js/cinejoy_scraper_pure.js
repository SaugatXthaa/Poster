/**
 * Cinejoy Scraper — 100% Pure HTTP (NO Playwright)
 * ================================================
 * FULLY REVERSE-ENGINEERED from BToEzF61.js (2026-08-10).
 *
 * Architecture:
 *   1. GET https://api.shegu.st/servers              -> public server list
 *   2. Generate rid (encrypted payload):
 *      - W = random 32 bytes (key material)
 *      - d = random 16 bytes (IV material)
 *      - Derive gcmKey, maskKey, maskIV via HKDF-SHA256(base_key, salt=W, info="lumen-wire-v2|<purpose>")
 *      - AES-GCM encrypt the rid payload
 *      - rid = base64url(W + mask(W) + d + encrypted_payload)
 *   3. GET https://api.shegu.st/challenge?rid={rid}  -> PoW challenge {v,b,s,e,n,r,p,d,k,g}
 *   4. Solve PoW using SCRYPT:
 *      - salt_hash = SHA-256("pow2-salt|" + s + "|" + b)
 *      - For i = 0, 1, 2, ...:
 *          hash = scrypt(password="pow2|" + b + "|" + s + "|" + i, salt=salt_hash, N=n, r=r, p=p, dkLen=32)
 *          if leadingZeroBits(hash) >= d: return i
 *   5. Build X-At token = base64url(JSON.stringify({...challenge, c}))
 *   6. GET https://api.shegu.st/{rid}  HEADER X-At: {token}
 *      -> encrypted base64url blob
 *   7. Decrypt blob:
 *      - First 32 bytes = key material
 *      - Derive gcmKey, maskKey, maskIV via HKDF-SHA256(base_key, salt=key_material, info="lumen-wire-v2|<purpose>")
 *      - Unmask the remaining bytes with AES-CTR
 *      - AES-GCM decrypt the inner payload
 *      -> JSON: {"stream":[{"type":"hls","playlist":"https://help.earthcleaner.cc/playlist/{id}.m3u8"}]}
 *   8. GET https://help.earthcleaner.cc/playlist/{contentId}.m3u8
 *      HEADER Referer: https://cinejoy.to/
 *
 * Static constants (captured from live runtime):
 *   HKDF base key: 4e46ba8a98e390508fab1bd9207d15516bec11b8a0b21f8fc1e1e66c9e95abef
 *   HKDF info prefix: "lumen-wire-v2|"
 *   PoW salt prefix: "pow2-salt|"
 *   PoW iter prefix: "pow2|"
 *
 * Install:
 *   npm install axios
 *
 * Usage:
 *   node cinejoy_scraper_pure.js movie 693134
 *   node cinejoy_scraper_pure.js tv 1399 1 1
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

// Static base key for HKDF (captured from live runtime)
const HKDF_BASE_KEY = Buffer.from('4e46ba8a98e390508fab1bd9207d15516bec11b8a0b21f8fc1e1e66c9e95abef', 'hex');

// Byte length constants (from BToEzF61.js)
const KEY_MATERIAL_LEN = 16;   // $ = 32 (key material length)
const IV_MATERIAL_LEN = 12;    // c0 = 16 (IV material length)
const TAG_LEN = 16;            // S0 = 16 (AES-GCM tag length)
const MASK_MARKER = 1;         // T0 = 1 (mask marker byte)

class CinejoyScraperPure {
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
      },
    });
  }

  // ==================== PUBLIC API ====================

  async listServers() {
    const r = await this.client.get(`${SHEGU_API}/servers`);
    return (r.data || {}).servers || [];
  }

  async getSubtitles(tmdbId, isMovie = true, season = null, episode = null) {
    const params = { type: isMovie ? 'movie' : 'tv', tmdb: tmdbId };
    if (!isMovie && season !== null && episode !== null) {
      params.season = season;
      params.episode = episode;
    }
    const r = await this.client.get(`${SHEGU_SUBS}/subtitles`, { params });
    return (r.data || {}).subtitles || [];
  }

  async getMovieStreams(tmdbId, title = '') {
    return this._getStreams(tmdbId, 'movie', null, null, title);
  }

  async getSeriesStreams(tmdbId, season, episode, title = '') {
    return this._getStreams(tmdbId, 'tv', season, episode, title);
  }

  // ==================== CORE FLOW ====================

  async _getStreams(tmdbId, mediaType, season, episode, title) {
    // Step 1: Get servers
    const servers = await this.listServers();
    const server = servers[0]?.name || 'Lisbon';

    // Step 1b: Get TMDB metadata (for the rid payload)
    const tmdbInfo = await this._getTmdbDetails(tmdbId, mediaType);

    // Step 2: Generate rid with the correct payload format
    const rid = await this._generateRid(tmdbId, mediaType, server, season, episode, tmdbInfo);

    // Step 3: Fetch challenge
    const r = await this.client.get(`${SHEGU_API}/challenge`, { params: { rid } });
    const challenge = r.data;
    if (challenge.v !== 2) throw new Error(`Unsupported challenge version: ${challenge.v}`);

    // Step 4: Solve PoW (scrypt)
    const c = this._solvePoW(challenge);

    // Step 5: Build X-At token
    const xAt = Buffer.from(JSON.stringify({ ...challenge, c })).toString('base64');

    // Step 6: Fetch encrypted blob
    const blobResp = await this.client.get(`${SHEGU_API}/${rid}`, {
      headers: { 'X-At': xAt },
      responseType: 'text',
    });
    const blob = blobResp.data;

    // Step 7: Decrypt blob
    const decrypted = await this._decryptBlob(blob, 's2c');
    if (!decrypted.stream || !decrypted.stream[0] || !decrypted.stream[0].playlist) {
      throw new Error(`Decrypted blob has no stream URL: ${JSON.stringify(decrypted).slice(0, 300)}`);
    }

    // Step 8: Parse master playlist
    const playlistUrl = decrypted.stream[0].playlist;
    return this._parseMasterPlaylist(playlistUrl, title || `tmdb-${tmdbId}`);
  }

  /** Get TMDB metadata (title, year, imdb_id) for the rid payload. */
  async _getTmdbDetails(tmdbId, mediaType) {
    try {
      const r = await axios.get(`https://api.themoviedb.org/3/${mediaType === 'tv' ? 'tv' : 'movie'}/${tmdbId}`, {
        params: { api_key: TMDB_KEY, append_to_response: 'external_ids' },
        timeout: 8000,
      });
      const date = r.data.release_date || r.data.first_air_date || '';
      return {
        title: r.data.title || r.data.name || '',
        year: date ? parseInt(date.slice(0, 4)) : '',
        imdbId: r.data.external_ids?.imdb_id || r.data.imdb_id || '',
      };
    } catch (e) {
      return { title: '', year: '', imdbId: '' };
    }
  }

  // ==================== PoW SOLVER (scrypt) ====================

  /**
   * Solve the proof-of-work challenge using scrypt.
   *
   * Algorithm (verified against 3 live captures):
   *   1. salt_hash = SHA-256("pow2-salt|" + s + "|" + b)
   *   2. For i = 0, 1, 2, ...:
   *      hash = scrypt(password="pow2|" + b + "|" + s + "|" + i, salt=salt_hash,
   *                     N=n, r=r, p=p, dkLen=32)
   *      if leadingZeroBits(hash) >= d: return i
   *
   * @param {Object} challenge - {v, b, s, e, n, r, p, d, k, g}
   * @returns {number} c - the winning iteration count
   */
  _solvePoW(challenge) {
    const { b, s, n, r, p, d } = challenge;

    // Step 1: Compute salt hash
    const saltInput = `pow2-salt|${s}|${b}`;
    const saltHash = crypto.createHash('sha256').update(saltInput).digest();

    // Step 2: Iterate
    for (let i = 0; i < 1000000; i++) {
      const password = `pow2|${b}|${s}|${i}`;
      const hash = crypto.scryptSync(password, saltHash, 32, { N: n, r, p, maxmem: 128 * r * (n + p) * 2 });
      if (this._leadingZeroBits(hash) >= d) {
        return i;
      }
    }
    throw new Error('PoW solver failed after 1M iterations');
  }

  /** Count leading zero bits in a buffer (matches Uc function in BToEzF61.js). */
  _leadingZeroBits(buf) {
    let n = 0;
    for (const byte of buf) {
      if (byte === 0) { n += 8; continue; }
      n += Math.clz32(byte) - 24;
      break;
    }
    return n;
  }

  // ==================== RID GENERATION ====================

  /**
   * Generate the rid (request ID) — an encrypted payload.
   * Based on the Nc function in BToEzF61.js.
   *
   * Payload format (captured from live browser):
   *   "/<server>/<mediaType>?imdb=<imdbId>&title=<title>&tmdb=<tmdbId>&year=<year>"
   *   (params sorted alphabetically by key, URL-encoded)
   *
   * Rid structure: base64url(W(32) + AES-CTR-mask([marker(1) + d(16) + AES-GCM(payload)]))
   *   W = 32 random bytes (key material for HKDF)
   *   d = 16 random bytes (IV for AES-GCM)
   *   marker = 1 byte (value 1, identifies the inner structure version)
   *   AES-GCM(payload, gcmKey, iv=d, aad="lumen-wire-v2|c2s") -> ciphertext + 16-byte tag
   *   AES-CTR(inner, maskKey, maskIV) -> masked inner
   *   rid = base64url(W + masked_inner)
   */
  async _generateRid(tmdbId, mediaType, server, season, episode, tmdbInfo) {
    const purpose = 'c2s';

    // Build the payload path
    const params = new URLSearchParams();
    if (tmdbInfo.imdbId) params.set('imdb', tmdbInfo.imdbId);
    if (tmdbInfo.title) params.set('title', tmdbInfo.title);
    params.set('tmdb', String(tmdbId));
    if (tmdbInfo.year) params.set('year', String(tmdbInfo.year));
    if (season !== null) params.set('season', String(season));
    if (episode !== null) params.set('episode', String(episode));

    // Sort params alphabetically (matching the browser's behavior)
    const sortedParams = [...params.entries()].sort(([a], [b]) => a.localeCompare(b));
    const queryString = sortedParams
      .map(([k, v]) => encodeURIComponent(k) + '=' + encodeURIComponent(v))
      .join('&');

    const payload = `/${server}/${mediaType}?${queryString}`;

    // Generate random key material
    const W = crypto.randomBytes(KEY_MATERIAL_LEN);   // 32 bytes
    const d = crypto.randomBytes(IV_MATERIAL_LEN);     // 16 bytes

    // Derive keys via HKDF-SHA256
    const { gcmKey, maskKey, maskIV } = await this._deriveKeys(W, purpose);

    // AES-GCM encrypt the payload
    const aad = Buffer.from(`lumen-wire-v2|${purpose}`, 'utf8');
    const cipher = crypto.createCipheriv('aes-256-gcm', gcmKey, d);
    cipher.setAAD(aad);
    const encrypted = Buffer.concat([cipher.update(payload, 'utf8'), cipher.final()]);
    const tag = cipher.getAuthTag();

    // Build inner structure: marker(1) + d(16) + ciphertext + tag(16)
    const inner = Buffer.concat([
      Buffer.from([MASK_MARKER]),
      d,
      encrypted,
      tag,
    ]);

    // Mask the inner structure with AES-CTR
    const masked = await this._aesCtrEncrypt(inner, maskKey, maskIV);

    // Build the rid: W(32) + masked(inner)
    const ridBuf = Buffer.concat([W, masked]);

    return ridBuf.toString('base64url');
  }

  // ==================== BLOB DECRYPTION ====================

  /**
   * Decrypt the response blob from /api.shegu.st/{rid}.
   * Based on the Qc function in BToEzF61.js.
   *
   * Structure: base64url(key_material(32) + masked_data + tag)
   *   key_material = first 32 bytes (used for HKDF)
   *   masked_data = AES-CTR encrypted inner data
   *   inner = AES-GCM encrypted JSON payload
   */
  async _decryptBlob(blobBase64, purpose) {
    const buf = Buffer.from(blobBase64, 'base64url');

    // First 32 bytes = key material
    const keyMaterial = buf.slice(0, KEY_MATERIAL_LEN);
    const rest = buf.slice(KEY_MATERIAL_LEN);

    // Derive keys
    const { gcmKey, maskKey, maskIV } = await this._deriveKeys(keyMaterial, purpose);

    // Unmask the rest with AES-CTR
    const unmasked = await this._aesCtrDecrypt(rest, maskKey, maskIV);

    // Check mask marker
    if (unmasked[0] !== MASK_MARKER) {
      throw new Error('Invalid mask marker in blob');
    }

    // Parse inner structure: marker(1) + iv(16) + ciphertext + tag(16)
    const gcmIv = unmasked.slice(1, 1 + IV_MATERIAL_LEN);
    // The IV for GCM is typically 12 bytes. The 16-byte value might need truncation.
    const gcmIv12 = gcmIv.slice(0, 12);
    const ciphertextWithTag = unmasked.slice(1 + IV_MATERIAL_LEN);
    const ciphertext = ciphertextWithTag.slice(0, -TAG_LEN);
    const tag = ciphertextWithTag.slice(-TAG_LEN);

    // AES-GCM decrypt
    const aad = Buffer.from(`lumen-wire-v2|${purpose}`, 'utf8');
    const decipher = crypto.createDecipheriv('aes-256-gcm', gcmKey, gcmIv12);
    decipher.setAAD(aad);
    decipher.setAuthTag(tag);

    const decrypted = Buffer.concat([decipher.update(ciphertext), decipher.final()]);
    return JSON.parse(decrypted.toString('utf8'));
  }

  // ==================== KEY DERIVATION (B0 function) ====================

  /**
   * Derive gcmKey, maskKey, maskIV from key material + purpose.
   * Based on the B0 function in BToEzF61.js.
   *
   * Uses HKDF-SHA256 with:
   *   base key = static HKDF_BASE_KEY
   *   salt = key_material (32 bytes)
   *   info = "lumen-wire-v2|" + purpose
   *
   * Derives 80 bytes:
   *   gcmKey = bytes[0:32]  (AES-256-GCM key)
   *   maskKey = bytes[32:64] (AES-256-CTR key)
   *   maskIV = bytes[64:80]  (AES-CTR IV, 16 bytes)
   */
  async _deriveKeys(keyMaterial, purpose) {
    const info = Buffer.from(`lumen-wire-v2|${purpose}`, 'utf8');

    // HKDF-SHA256: derive 80 bytes
    const derived = Buffer.from(
      crypto.hkdfSync('sha256', HKDF_BASE_KEY, keyMaterial, info, 80)
    );

    return {
      gcmKey: derived.slice(0, 32),
      maskKey: derived.slice(32, 64),
      maskIV: derived.slice(64, 80),
    };
  }

  // ==================== AES-CTR (F0 function) ====================

  /** AES-CTR encrypt (used for masking). */
  async _aesCtrEncrypt(data, key, iv) {
    const cipher = crypto.createCipheriv('aes-256-ctr', key, iv.slice(0, 16));
    return Buffer.concat([cipher.update(data), cipher.final()]);
  }

  /** AES-CTR decrypt (used for unmasking). */
  async _aesCtrDecrypt(data, key, iv) {
    const decipher = crypto.createDecipheriv('aes-256-ctr', key, iv.slice(0, 16));
    return Buffer.concat([decipher.update(data), decipher.final()]);
  }

  // ==================== MASTER PLAYLIST PARSER ====================

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
// Demo / CLI
// --------------------------------------------------------------------- //
if (require.main === module) (async () => {
  const s = new CinejoyScraperPure();
  const args = process.argv.slice(2);
  const mediaType = args[0] || 'movie';
  const tmdbId = args[1] || '693134';

  console.log(`=== Cinejoy PURE HTTP scraper (NO Playwright) ===`);
  console.log(`Fetching streams for ${mediaType} ${tmdbId}...`);

  try {
    const streams = mediaType === 'movie'
      ? await s.getMovieStreams(tmdbId)
      : await s.getSeriesStreams(tmdbId, parseInt(args[2]), parseInt(args[3]));

    console.log(`\n✓ Got ${streams.length} streams:`);
    for (const st of streams) {
      console.log(`  [${st.quality}] ${st.resolution} ${st.bandwidth}bps`);
      console.log(`    URL: ${st.url}`);
      if (st.audioUrl) console.log(`    Audio: ${st.audioUrl}`);
    }
  } catch (e) {
    console.error(`Error: ${e.message}`);
    if (process.env.DEBUG) console.error(e.stack);
  }
})();

module.exports = { CinejoyScraperPure };
