// vidsrc_extractor_all_in_one.js — Direct vidsrc.io stream extractor
// =========================================================================
// Extracts DIRECT playable HLS streams from vidsrc.io's embed chain:
//
//   vidsrc.io → cloudorchestranova.com → data.vidsrcme.ru → welkinwanderer.space
//
// FULL CHAIN:
//   1. GET https://vidsrc.io/vs_src.php?type={movie|tv}&id={tmdbId}
//      → JSON {"src": "https://cloudorchestranova.com/embed/{type}/{tmdbId}?vs={vs_token}"}
//
//   2. GET https://cloudorchestranova.com/embed/{type}/{tmdbId}?vs={vs_token}
//      → HTML with window.CFG = { playerUrl: "/embed/player/{type}/{tmdbId}?vs={player_vs_token}" }
//
//   3. GET https://cloudorchestranova.com/embed/player/{type}/{tmdbId}?vs={player_vs_token}
//      → HTML with window.CONFIG = { api: "https://data.vidsrcme.ru/api.php?type=...&tmdb=...&stream_urls" }
//
//   4. GET https://data.vidsrcme.ru/api.php?type={type}&tmdb={tmdbId}[&season={s}&episode={e}]&stream_urls
//      → JSON {
//          "data": {
//            "title": "...", "imdb_id": "tt...", "file_name": "...",
//            "backdrop": "...",
//            "stream_urls": "<base64-encoded ChaCha20-encrypted string>"
//          },
//          "vs": { "w": <window>, "wasm_url": "https://data.vidsrcme.ru/wasm.php?w=...&_=..." }
//        }
//
//   5. GET https://data.vidsrcme.ru/wasm.php?w={w}&_={ts}
//      → WASM module (7.4KB) with exports: memory, alloc, decrypt, meta, sync, info
//
//   6. Decrypt stream_urls:
//      - base64-decode stream_urls → enc (binary, ~1100 bytes)
//      - ptr = wasm.alloc(enc.length)
//      - memory[ptr:ptr+len] = enc
//      - outLen = wasm.decrypt(ptr, enc.length)
//      - result = memory[ptr + 12 : ptr + 12 + outLen] as UTF-8 string
//      - Split by '\n' → array of master.m3u8 URLs
//
//   7. GET https://welkinwanderer.space/generate.php
//      Headers: { Origin: https://cloudorchestranova.com, Referer: https://cloudorchestranova.com/ }
//      → JWT token (plain text, base64-encoded, valid 4 hours, IP-bound to /24)
//
//   8. GET https://welkinwanderer.space/pl/{base64}/master.m3u8?token={JWT}
//      Headers: { Referer: https://cloudorchestranova.com/ }
//      → HLS master playlist with multiple quality variants (typically 640x268, 1280x534, 1920x802)
//
//   9. GET https://welkinwanderer.space/pl/{base64}/{hash}/index.m3u8?token={JWT}
//      → HLS media playlist with .ts segment URLs
//
// NOTES:
//   - The JWT token is IP-bound (allows /24 subnet) and valid for 4 hours.
//   - The stream_urls + WASM window rotates every ~5 minutes.
//   - Welkinwanderer.space has aggressive rate limiting (429) on /generate.php.
//   - The stream works in Stremio, mpv, VLC, etc. when Referer is set.
//   - Available qualities (from master.m3u8 RESOLUTION): 640x268 (~670kbps), 1280x534 (~2.5Mbps), 1920x802 (~4.5Mbps)
//     These are typically 360p/480p, 720p, 1080p respectively.
//   - Source files are YTS-encoded MP4s (file_name field reveals: Dune.Part.Two.2024.1080p.WEBRip.x264.AAC-[YTS.MX].mp4)
//     so the max quality is 1080p (no 4K from this source).
//   - The welkinwanderer.space CDN is fast and reliable (no Cloudflare in front).

'use strict';

const https = require('https');

const PROVIDER_NAME = 'VidSrc-Extract';
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';

// ─── Helper: fetch a URL as Buffer ──────────────────────────────────────────
function fetchBuf(url, referer, timeout = 15000) {
  return new Promise((resolve, reject) => {
    const headers = {
      'User-Agent': UA,
      'Accept': '*/*',
      'Accept-Language': 'en-US,en;q=0.5',
    };
    if (referer) headers['Referer'] = referer;
    const req = https.get(url, { headers, rejectUnauthorized: false }, (res) => {
      if (res.statusCode !== 200) {
        res.resume();
        return reject(new Error(`HTTP ${res.statusCode} for ${url.slice(0, 80)}`));
      }
      const chunks = [];
      res.on('data', c => chunks.push(c));
      res.on('end', () => resolve(Buffer.concat(chunks)));
    });
    req.on('error', reject);
    req.setTimeout(timeout, () => req.destroy(new Error('timeout')));
  });
}

async function fetchJson(url, referer) {
  const buf = await fetchBuf(url, referer);
  return JSON.parse(buf.toString('utf8'));
}

async function fetchText(url, referer) {
  const buf = await fetchBuf(url, referer);
  return buf.toString('utf8');
}

// ─── Step 1: Get cloudorchestranova embed URL ───────────────────────────────
async function getEmbedUrl(type, tmdbId, season, episode) {
  const vsSrcUrl = `https://vidsrc.io/vs_src.php?type=${type}&id=${tmdbId}` +
                    (season ? `&season=${season}&episode=${episode}` : '');
  const vsSrc = await fetchJson(vsSrcUrl, 'https://vidsrc.io/');
  if (!vsSrc.src) throw new Error('No src in /vs_src.php response');
  return vsSrc.src;
}

// ─── Step 2 & 3: Get the stream API URL from cloudorchestranova player ──────
async function getStreamApiUrl(embedUrl) {
  const embedHtml = await fetchText(embedUrl, 'https://vidsrc.io/');

  // First try window.CONFIG (direct player HTML)
  let m = embedHtml.match(/window\.CONFIG\s*=\s*({[^<]+});/);
  if (m) {
    const config = JSON.parse(m[1]);
    if (config.api) return config.api;
    // TV case: use streamBase + season/episode + stream_urls
    if (config.streamBase) {
      let url = config.streamBase;
      if (config.season) url += `&season=${config.season}`;
      if (config.episode) url += `&episode=${config.episode}`;
      url += '&stream_urls';
      return url;
    }
  }

  // Otherwise look for window.CFG → playerUrl → fetch player HTML → window.CONFIG
  m = embedHtml.match(/window\.CFG\s*=\s*({[^<]+});/);
  if (m) {
    const cfg = JSON.parse(m[1]);
    if (cfg.playerUrl) {
      const playerUrl = cfg.playerUrl.startsWith('http')
        ? cfg.playerUrl
        : `https://cloudorchestranova.com${cfg.playerUrl}`;
      const playerHtml = await fetchText(playerUrl, 'https://cloudorchestranova.com/');
      const m2 = playerHtml.match(/window\.CONFIG\s*=\s*({[^<]+});/);
      if (m2) {
        const config = JSON.parse(m2[1]);
        if (config.api) return config.api;
        if (config.streamBase) {
          let url = config.streamBase;
          if (config.season) url += `&season=${config.season}`;
          if (config.episode) url += `&episode=${config.episode}`;
          url += '&stream_urls';
          return url;
        }
      }
    }
  }
  throw new Error('Cannot find window.CONFIG.api in player HTML');
}

// ─── Step 4: Fetch stream_urls (encrypted) + WASM URL ──────────────────────
async function fetchStreamData(streamApiUrl) {
  const data = await fetchJson(streamApiUrl, 'https://cloudorchestranova.com/');
  if (!data.data || !data.data.stream_urls || !data.vs || !data.vs.wasm_url) {
    throw new Error('Missing stream_urls or wasm_url in API response');
  }
  return data;
}

// ─── Step 5 & 6: Decrypt stream_urls via WASM ───────────────────────────────
async function decryptStreamUrls(streamData) {
  const wasmBuf = await fetchBuf(streamData.vs.wasm_url, 'https://cloudorchestranova.com/');
  const wasmModule = await WebAssembly.compile(wasmBuf);
  const instance = await WebAssembly.instantiate(wasmModule, {});
  const ex = instance.exports;

  const encB64 = streamData.data.stream_urls;
  const enc = Buffer.from(encB64, 'base64');
  const ptr = ex.alloc(enc.length);
  new Uint8Array(ex.memory.buffer, ptr, enc.length).set(enc);
  const outLen = ex.decrypt(ptr, enc.length);
  const result = Buffer.from(ex.memory.buffer, ptr + 12, outLen).toString('utf8');
  return result.split('\n').filter(s => s.trim());
}

// ─── Step 7: Get JWT token from generate.php ────────────────────────────────
async function getJwtToken(streamHostUrl) {
  // The stream URL might be on a different domain than welkinwanderer.space
  // Extract the host from the stream URL
  let host = 'https://welkinwanderer.space';
  if (streamHostUrl) {
    try {
      const u = new URL(streamHostUrl);
      host = `${u.protocol}//${u.host}`;
    } catch (e) { /* use default */ }
  }
  const generateUrl = `${host}/generate.php`;
  console.log(`[VidSrc-Extract] Fetching JWT from ${generateUrl}...`);

  // Note: this endpoint is rate-limited; allow retries with backoff
  for (let attempt = 0; attempt < 3; attempt++) {
    try {
      const buf = await fetchBuf(generateUrl, 'https://cloudorchestranova.com/', 10000);
      const token = buf.toString('utf8').trim();
      if (token && token.startsWith('eyJ')) return token;  // JWT starts with eyJ
      throw new Error('Invalid JWT response: ' + token.slice(0, 100));
    } catch (e) {
      if (attempt < 2) {
        const delay = (attempt + 1) * 3000;
        console.log(`[VidSrc-Extract] generate.php failed (${e.message.slice(0, 50)}), retrying in ${delay/1000}s...`);
        await new Promise(r => setTimeout(r, delay));
      } else throw e;
    }
  }
}

// ─── Step 8: Fetch master.m3u8 to verify quality ────────────────────────────
async function fetchMasterM3u8(streamUrl, jwt) {
  const url = `${streamUrl}?token=${jwt}`;
  const buf = await fetchBuf(url, 'https://cloudorchestranova.com/', 10000);
  const text = buf.toString('utf8');
  // Parse the master playlist for variants
  const variants = [];
  let currentRes = null, currentBw = null, currentCodecs = null;
  for (const line of text.split('\n')) {
    const trimmed = line.trim();
    if (trimmed.startsWith('#EXT-X-STREAM-INF:')) {
      const resMatch = trimmed.match(/RESOLUTION=(\d+)x(\d+)/);
      const bwMatch = trimmed.match(/BANDWIDTH=(\d+)/);
      const codecsMatch = trimmed.match(/CODECS="([^"]+)"/);
      if (resMatch) currentRes = { w: parseInt(resMatch[1]), h: parseInt(resMatch[2]) };
      if (bwMatch) currentBw = parseInt(bwMatch[1]);
      if (codecsMatch) currentCodecs = codecsMatch[1];
    } else if (trimmed && !trimmed.startsWith('#') && currentRes) {
      // This is a variant URL — could be absolute, root-relative (/path), or relative (path)
      const u = new URL(streamUrl);
      const host = `${u.protocol}//${u.host}`;
      let variantUrl;
      if (trimmed.startsWith('http')) {
        variantUrl = trimmed;
      } else if (trimmed.startsWith('/')) {
        variantUrl = host + trimmed;  // root-relative
      } else {
        // relative to current directory
        const base = streamUrl.replace(/\/[^/]*$/, '');
        variantUrl = `${base}/${trimmed}`;
      }
      // Make sure to append token to variant URL (master.m3u8 variants inherit the token)
      const variantUrlWithToken = variantUrl.includes('?') ? variantUrl : `${variantUrl}?token=${jwt}`;
      variants.push({
        resolution: currentRes,
        bandwidth: currentBw,
        codecs: currentCodecs,
        url: variantUrlWithToken,
        quality: currentRes.h >= 2160 ? '2160p'
              : currentRes.h >= 1080 ? '1080p'
              : currentRes.h >= 720 ? '720p'
              : currentRes.h >= 480 ? '480p'
              : '360p',
      });
      currentRes = null; currentBw = null; currentCodecs = null;
    }
  }
  return variants;
}

// ─── Build Stremio stream object ────────────────────────────────────────────
function buildStream(masterUrl, jwt, title, qualityHint) {
  // The playable URL is master.m3u8?token=JWT — Stremio/HLS players can play it directly
  const streamUrl = `${masterUrl}?token=${jwt}`;
  return {
    name: PROVIDER_NAME + ' - ' + qualityHint,
    title: title + ' [vidsrc direct HLS]',
    url: streamUrl,
    quality: qualityHint,
    type: 'application/vnd.apple.mpegurl',
    behaviorHints: {
      bingeGroup: 'vidsrc-extract-' + qualityHint,
      // Stremio will pass these headers when fetching the stream
      proxyHeaders: {
        request: {
          'Referer': 'https://cloudorchestranova.com/',
          'Origin': 'https://cloudorchestranova.com',
          'User-Agent': UA,
        },
      },
    },
  };
}

// ─── Main entry: get playable streams for a TMDB ID ─────────────────────────
async function getStreams(tmdbId, type, season, episode) {
  tmdbId = String(tmdbId);
  type = type === 'tv' ? 'tv' : 'movie';
  console.log(`[VidSrc-Extract] ${type} ${tmdbId}` +
              (season ? ` S${season}E${episode}` : ''));

  try {
    // Step 1: Get embed URL
    const embedUrl = await getEmbedUrl(type, tmdbId, season, episode);
    console.log(`[VidSrc-Extract] embed URL: ${embedUrl.slice(0, 80)}...`);

    // Steps 2-3: Get stream API URL
    const streamApiUrl = await getStreamApiUrl(embedUrl);
    console.log(`[VidSrc-Extract] stream API: ${streamApiUrl.slice(0, 80)}...`);

    // Step 4: Fetch encrypted stream data
    const streamData = await fetchStreamData(streamApiUrl);
    const title = streamData.data.title || `${type}-${tmdbId}`;
    const fileName = streamData.data.file_name || '';
    console.log(`[VidSrc-Extract] Title: ${title}`);
    console.log(`[VidSrc-Extract] File: ${fileName}`);

    // Steps 5-6: Decrypt stream URLs
    const streamUrls = await decryptStreamUrls(streamData);
    console.log(`[VidSrc-Extract] Decrypted ${streamUrls.length} master.m3u8 URLs`);

    if (streamUrls.length === 0) {
      console.log('[VidSrc-Extract] No stream URLs available');
      return [];
    }

    // Step 7: Get JWT token (pass stream URL to detect the correct host)
    const jwt = await getJwtToken(streamUrls[0]);
    console.log(`[VidSrc-Extract] Got JWT token (${jwt.length} chars)`);

    // Step 8: Fetch master.m3u8 for each stream URL, deduplicate by quality set
    const allStreams = [];
    const seenVariants = new Set();

    for (let i = 0; i < streamUrls.length; i++) {
      const masterUrl = streamUrls[i];
      try {
        const variants = await fetchMasterM3u8(masterUrl, jwt);
        if (variants.length === 0) {
          // Maybe it's a direct media playlist (single quality) — just use it
          const stream = buildStream(masterUrl, jwt, title, '1080p');
          allStreams.push(stream);
          continue;
        }

        // Sort variants by height descending (1080p first)
        variants.sort((a, b) => b.resolution.h - a.resolution.h);

        // Use just the highest-quality variant URL directly (more reliable than master.m3u8)
        // The master.m3u8 also works, but using the variant index.m3u8 directly is more stable
        for (const v of variants) {
          // Make sure the variant URL includes the JWT token
          let finalUrl = v.url;
          if (!finalUrl.includes('token=')) {
            finalUrl += (finalUrl.includes('?') ? '&' : '?') + `token=${jwt}`;
          }
          // Deduplicate by quality (multiple URLs often have same quality set)
          const key = v.quality + '|' + v.resolution.w + 'x' + v.resolution.h;
          if (seenVariants.has(key)) continue;
          seenVariants.add(key);

          allStreams.push({
            name: PROVIDER_NAME + ' - ' + v.quality + ' #' + (i+1),
            title: `${title} [${v.resolution.w}x${v.resolution.h} ${v.quality} HLS]`,
            url: finalUrl,
            quality: v.quality,
            type: 'application/vnd.apple.mpegurl',
            behaviorHints: {
              bingeGroup: 'vidsrc-extract-' + v.quality,
              proxyHeaders: {
                request: {
                  'Referer': 'https://cloudorchestranova.com/',
                  'Origin': 'https://cloudorchestranova.com',
                  'User-Agent': UA,
                },
              },
            },
          });
        }
      } catch (e) {
        console.log(`[VidSrc-Extract] Failed to fetch master.m3u8 #${i+1}: ${e.message.slice(0, 80)}`);
      }
    }

    // Sort by quality (1080p first)
    const qOrder = { '2160p': 0, '1080p': 1, '720p': 2, '480p': 3, '360p': 4, 'SD': 5 };
    allStreams.sort((a, b) => (qOrder[a.quality] || 99) - (qOrder[b.quality] || 99));

    console.log(`[VidSrc-Extract] ✅ ${allStreams.length} playable HLS streams extracted`);
    const counts = {};
    for (const s of allStreams) counts[s.quality] = (counts[s.quality] || 0) + 1;
    console.log('[VidSrc-Extract] Quality breakdown: ' +
                Object.entries(counts).map(([k,v]) => `${k}=${v}`).join(', '));

    return allStreams;
  } catch (e) {
    console.log('[VidSrc-Extract] ❌ ' + e.message);
    return [];
  }
}

// ─── Module exports ─────────────────────────────────────────────────────────
module.exports = {
  getStreams,
  getEmbedUrl,
  getStreamApiUrl,
  fetchStreamData,
  decryptStreamUrls,
  getJwtToken,
  fetchMasterM3u8,
  API_BASE: 'https://vidsrc.io',
  STREAM_HOST: 'https://welkinwanderer.space',
};

// ─── CLI ────────────────────────────────────────────────────────────────────
if (require.main === module) {
  const args = process.argv.slice(2);
  if (args.length < 2) {
    console.log('VidSrc.io Direct Stream Extractor');
    console.log('');
    console.log('Usage:');
    console.log('  node vidsrc_extractor_all_in_one.js <tmdbId> <movie|tv> [season] [episode]');
    console.log('');
    console.log('Examples:');
    console.log('  node vidsrc_extractor_all_in_one.js 693134 movie         # Dune Part Two');
    console.log('  node vidsrc_extractor_all_in_one.js 1396 tv 1 1          # Breaking Bad S01E01');
    console.log('  node vidsrc_extractor_all_in_one.js 95479 tv 1 1         # Jujutsu Kaisen S01E01');
    console.log('');
    console.log('Chain:');
    console.log('  vidsrc.io → cloudorchestranova.com → data.vidsrcme.ru → welkinwanderer.space');
    console.log('');
    console.log('Output: Direct playable HLS URLs (master.m3u8 + variant index.m3u8)');
    process.exit(1);
  }

  const tmdbId = args[0];
  const type = args[1];
  const season = args[2] || null;
  const episode = args[3] || null;

  getStreams(tmdbId, type, season, episode)
    .then(streams => {
      console.log('\n=== Final playable streams (sorted by quality) ===');
      const byQuality = {};
      streams.forEach(s => {
        byQuality[s.quality] = byQuality[s.quality] || [];
        byQuality[s.quality].push(s);
      });
      let idx = 1;
      for (const q of ['2160p', '1080p', '720p', '480p', '360p', 'SD']) {
        if (byQuality[q]) {
          console.log(`\n--- ${q} (${byQuality[q].length} streams) ---`);
          for (const s of byQuality[q]) {
            console.log(`${idx}. ${s.name}`);
            console.log(`   Title: ${s.title}`);
            console.log(`   URL:   ${s.url.slice(0, 150)}${s.url.length > 150 ? '...' : ''}`);
            idx++;
          }
        }
      }
      console.log(`\nTotal: ${streams.length} playable HLS streams`);
    })
    .catch(e => {
      console.error('FATAL: ' + e.message);
      console.error(e.stack);
      process.exit(1);
    });
}
