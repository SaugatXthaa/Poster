// multimovies.beer — Direct Stream Extractor (up to 1080p, dual-audio)
// =========================================================================
// Scrapes DIRECT PLAYABLE HLS streams from multimovies.beer (DooPlay theme).
//
// CHAIN (for movies):
//   1. GET /?s=<title>
//      → list of /movies/<slug>/ URLs
//   2. GET /movies/<slug>/
//      → extract data-id="<post_id>" (DooPlay post ID)
//   3. POST /wp-admin/admin-ajax.php
//      Body: action=doo_player_ajax&post=<post_id>&nume=2&type=movie
//      → JSON {"embed_url":"https://streams.iqsmartgames.com/embed/movie/<imdb_id>?key=<key>","type":"iframe"}
//   4. GET https://streams.iqsmartgames.com/mymovieapi?imdbid=<imdb_id>&key=<key>
//      Headers: Origin: https://streams.iqsmartgames.com
//      → JSON {"success":true,"data":[{"filename":"...","fileslug":"r1djjk9","fsize":"2.76 GB"}]}
//   5. GET https://pro.iqsmartgames.com/evid/<fileslug>
//      → HTML page that calls embedhelper2.php → check_st_strm.php
//      → fetches master.m3u8 from a stream host (hanerix.com, vibuxer.com, etc.)
//   6. The actual stream URL (captured via Playwright or guessed from fileslug):
//      https://<stream_host>/stream/<sig>/<token>/<expiry>/<id>/master.m3u8
//      Headers needed: Origin + Referer: https://pro.iqsmartgames.com
//
// CHAIN (for TV episodes):
//   1. GET /?s=<title>
//      → list of /tvshows/<slug>/ URLs (the parent show, not episodes)
//   2. POST /wp-admin/admin-ajax.php
//      Body: action=doo_player_ajax&post=<show_post_id>&nume=2&type=tv
//      → JSON {"embed_url":"https://streams.iqsmartgames.com/embed/tv/<tmdb_id>/<season>/<episode>?key=..."}
//   3. The iqsmartgames API doesn't support TV episodes directly, but the
//      streams.iqsmartgames.com embed URL contains TMDB ID + season/episode
//      → fetch from pro.iqsmartgames.com flow (same as movies, with TV fileslug)
//
// AVAILABLE PLAYERS (nume=1 through 5):
//   nume=1: filesforever.link → pro.iqsmartgames.com/evid/<fileslug>
//           (Dune Part Two: works, returns 480p/720p/1080p with Hindi+English audio)
//   nume=2: rozgarlelo.modiplay.xyz/embed/imdb/movie?id=<imdb_id>
//           → proxy.php returns TikTok ad-polluted segments (NOT usable)
//   nume=3: streams.iqsmartgames.com/embed/movie/<imdb_id>?key=<key>
//           → calls /mymovieapi → pro.iqsmartgames.com → direct stream
//           ✅ BEST — same flow as nume=1 but more reliable
//   nume=4: screenscape.me (Cloudflare-protected, 403)
//   nume=5: nxsha.space (uses Playwright-required flow)
//
// We use nume=3 (streams.iqsmartgames.com) as primary, nume=1 as fallback.
//
// STREAM QUALITY:
//   Movies return master.m3u8 with 3 variants:
//     - 480p (852x480, ~413 kbps)
//     - 720p (1280x720, ~848 kbps)
//     - 1080p (1920x1080, ~1.8 Mbps, 10-bit)
//   Each variant has dual audio: Hindi (default) + English
//   The source is typically YTS-style 1080p WEB-DL with dual audio.
//   NO 4K available from this provider (max 1080p as of current data).
//
// NOTE ON "PNG" SEGMENTS:
//   The .m3u8 playlists contain URLs like:
//     https://p16-ad-site-sign-sg.tiktokcdn.com/.../...~tplv-d5opwmad15-ttam-origin.image
//   These are TS video segments disguised as PNG images (Content-Type: image/png)
//   to bypass ad-blockers. Stremio + hls.js will play them transparently — the
//   .image extension and image/png Content-Type don't matter for HLS playback.
//
// USAGE:
//   node multimovies_all_in_one.js <tmdbId> <movie|tv> [season] [episode]
//   node multimovies_all_in_one.js search "dune"
//
// TESTED:
//   ✅ Dune Part Two (tt15239678, TMDB 693134) → 480p, 720p, 1080p (Hindi+English)
//   ✅ Avatar Way of Water (tt1630029) → 480p, 720p, 1080p
//   ✅ The Fable S01E01 (TMDB 238843) → 1080p Hindi-dubbed

'use strict';

const https = require('https');
const http = require('http');

const PROVIDER_NAME = 'MultiMovies';
const TMDB_API_KEY = '8476a7ab80ad76f0936744df0430e67c';
const SITE_BASE = 'https://multimovies.beer';
const IQSMART_API = 'https://streams.iqsmartgames.com';
const IQSMART_PLAYER = 'https://pro.iqsmartgames.com';
const IQSMART_KEY = 'e11a7debaaa4f5d25b671706ffe4d2acb56efbd4';  // hardcoded in player 3 page
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';

// ─── HTTP fetch helpers ─────────────────────────────────────────────────────
// Cloudflare-protected endpoints need to use curl (Node's fetch is challenged)
// Also need to support cookie jars for session-based auth on hanerix.com
const { execFileSync } = require('child_process');
const fs = require('fs');
const os = require('os');
const path = require('path');

function fetchViaCurl(url, { headers = {}, timeout = 15000, method = 'GET', body = null, cookieJar = null, saveCookies = false } = {}) {
  // Always include User-Agent + default Accept (CF challenges Node's UA)
  const finalHeaders = {
    'User-Agent': UA,
    'Accept': '*/*',
    ...headers,
  };
  // Use --request GET explicitly — this works for hanerix.com hls4 URLs
  // (curl -X GET works but plain curl GET returns 403 for some CF-protected URLs)
  const args = ['-sL', '--max-time', String(Math.floor(timeout / 1000)), '--request', method];
  for (const [k, v] of Object.entries(finalHeaders)) {
    args.push('-H', `${k}: ${v}`);
  }
  if (body) {
    args.push('--data', body);
  }
  // Cookie jar support — needed for hanerix.com stream URL auth
  // The /e/<slug> page sets session cookies that are required for /stream/... requests
  if (cookieJar) {
    args.push('-b', cookieJar);  // send cookies
    if (saveCookies) args.push('-c', cookieJar);  // save received cookies
  } else if (saveCookies) {
    // Create a temp cookie jar file
    const jar = path.join(os.tmpdir(), `mm_cookies_${Date.now()}.txt`);
    args.push('-c', jar);
    args.push('-b', jar);
  }
  args.push(url);
  try {
    const stdout = execFileSync('curl', args, {
      maxBuffer: 50 * 1024 * 1024,
      timeout: timeout + 2000,
      encoding: 'utf8',
    });
    return stdout;
  } catch (e) {
    throw new Error(`curl failed for ${url.slice(0, 80)}: ${e.message.slice(0, 100)}`);
  }
}

function fetchText(url, opts = {}) {
  // Use curl by default (better CF compatibility)
  return Promise.resolve(fetchViaCurl(url, opts));
}

async function fetchJson(url, opts = {}) {
  const text = await fetchText(url, { ...opts, headers: { Accept: 'application/json', ...(opts.headers || {}) } });
  return JSON.parse(text);
}

// ─── Get TMDB info ──────────────────────────────────────────────────────────
async function getTMDBInfo(tmdbId, type) {
  const isTV = type === 'tv' || type === 'series';
  const url = `https://api.themoviedb.org/3/${isTV ? 'tv' : 'movie'}/${tmdbId}` +
              `?api_key=${TMDB_API_KEY}&append_to_response=external_ids`;
  try {
    const res = await fetch(url, {
      headers: { 'User-Agent': UA },
      signal: AbortSignal.timeout(10000),
    });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const j = await res.json();
    return {
      title: (isTV ? j.name : j.title) || 'Unknown',
      year: ((isTV ? j.first_air_date : j.release_date) || '').slice(0, 4),
      imdbId: j.imdb_id || (j.external_ids && j.external_ids.imdb_id) || null,
      type, tmdbId: String(tmdbId),
    };
  } catch (e) {
    console.log(`[MultiMovies] TMDB fetch failed: ${e.message}`);
    return null;
  }
}

// ─── Search multimovies.beer for a title → list of movie/show URLs ──────────
async function searchSite(query) {
  const url = `${SITE_BASE}/?s=${encodeURIComponent(query)}`;
  const html = await fetchText(url, { headers: { Referer: SITE_BASE + '/' } });
  // Extract movie and tvshow URLs
  const movieUrls = [...new Set(
    (html.match(/href="(https:\/\/multimovies\.beer\/movies\/[^"]+)"/g) || [])
      .map(m => m.replace(/href="|"/g, ''))
  )];
  const tvUrls = [...new Set(
    (html.match(/href="(https:\/\/multimovies\.beer\/tvshows\/[^"]+)"/g) || [])
      .map(m => m.replace(/href="|"/g, ''))
  )];
  return [...movieUrls, ...tvUrls];
}

// ─── Get post_id from a movie/show page ─────────────────────────────────────
async function getPostId(pageUrl) {
  const html = await fetchText(pageUrl, { headers: { Referer: SITE_BASE + '/' } });
  // Movie page: data-id="<post_id>"
  let m = html.match(/data-id="(\d+)"/);
  if (m) return m[1];
  // Episode page: body class postid-<post_id>
  m = html.match(/postid-(\d+)/);
  if (m) return m[1];
  return null;
}

// ─── Call doo_player_ajax to get embed_url ──────────────────────────────────
async function getPlayerEmbedUrl(postId, type, nume) {
  const body = `action=doo_player_ajax&post=${postId}&nume=${nume}&type=${type}`;
  const data = await fetchJson(`${SITE_BASE}/wp-admin/admin-ajax.php`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
      'Referer': SITE_BASE + '/',
      'Origin': SITE_BASE,
    },
    body,
  });
  // data.embed_url may contain &amp; instead of &
  if (data && data.embed_url) {
    data.embed_url = data.embed_url.replace(/&amp;/g, '&');
  }
  return data;
}

// ─── Extract TMDB ID and IMDB ID from embed URL ─────────────────────────────
function parseEmbedUrl(embedUrl) {
  const result = { url: embedUrl, imdbId: null, tmdbId: null, season: null, episode: null };
  // patterns:
  //   streams.iqsmartgames.com/embed/movie/tt15239678?key=...
  //   streams.iqsmartgames.com/embed/movie/<tmdb_id>?key=...
  //   streams.iqsmartgames.com/embed/tv/<tmdb_id>/<season>/<episode>?key=...
  //   rozgarlelo.modiplay.xyz/embed/imdb/movie?id=<imdb_id>
  //   rozgarlelo.modiplay.xyz/embed/tmdb/tv?id=<tmdb_id>&s=<s>&e=<e>
  let m = embedUrl.match(/\/movie\/(tt\d+)/);
  if (m) result.imdbId = m[1];
  m = embedUrl.match(/[?&]id=(tt\d+)/);
  if (m) result.imdbId = m[1];
  m = embedUrl.match(/\/movie\/(\d+)/);
  if (m) result.tmdbId = m[1];
  m = embedUrl.match(/\/tv\/(\d+)/);
  if (m) result.tmdbId = m[1];
  m = embedUrl.match(/[?&]id=(\d+)/);
  if (m && !result.tmdbId) result.tmdbId = m[1];
  m = embedUrl.match(/\/tv\/\d+\/(\d+)\/(\d+)/);
  if (m) { result.season = m[1]; result.episode = m[2]; }
  m = embedUrl.match(/[?&]s=(\d+)/);
  if (m) result.season = m[1];
  m = embedUrl.match(/[?&]e=(\d+)/);
  if (m) result.episode = m[1];
  return result;
}

// ─── Resolve direct stream URL via the iqsmartgames flow ────────────────────
// This calls the mymovieapi with imdbId (for movies) and returns a fileslug.
// The fileslug is the key to the actual stream.
async function resolveStreamViaIqsmart(imdbId, tmdbId, season, episode) {
  // Movies: use imdbId if available, fall back to tmdbId
  // TV: iqsmartgames API doesn't support TV via mymovieapi
  //     but the embed URL contains tmdb_id/s/e which we can use directly
  if (season) {
    // TV: use the streams.iqsmartgames.com embed URL directly
    // (the player.js on pro.iqsmartgames.com will resolve it)
    return null;  // TV handled separately
  }
  if (!imdbId && !tmdbId) return null;
  const idParam = imdbId ? `imdbid=${imdbId}` : `tmdbid=${tmdbId}`;
  const apiUrl = `${IQSMART_API}/mymovieapi?${idParam}&key=${IQSMART_KEY}`;
  try {
    // The API requires Origin: https://streams.iqsmartgames.com (else returns "Invalid dg")
    const data = await fetchJson(apiUrl, {
      headers: {
        'Origin': IQSMART_API,
        'Referer': `${IQSMART_API}/`,
        'Accept': 'application/json',
      },
    });
    if (data.success && data.data && data.data.length > 0) {
      return data.data.map(d => ({
        fileslug: d.fileslug,
        filename: d.filename,
        fsize: d.fsize,
      }));
    }
  } catch (e) {
    console.log(`[MultiMovies] iqsmart API failed: ${e.message}`);
  }
  return null;
}

// ─── Try to fetch the actual stream URL by inspecting pro.iqsmartgames.com ──
// The actual stream URL is hidden inside the obfuscated eval-packed JS on
// the stream host's /e/<file_slug> page. The packed JS contains a `links`
// object with `hls2` and `hls4` URLs that point to the actual master.m3u8.
//
// We iterate over ALL available sources from embedhelper2.php:
//   - smwh (streamhg → hanerix.com → premilkyway.com CDN)
//   - flls (earnvids → smoothpre.com → acek-cdn.com CDN)
//   - rpmshre, strmp2, upnshr (return 404 on slug URLs — skipped)
//
// Both working sources return the same 1080p stream via different CDNs,
// giving us redundancy if one CDN goes down.
async function fetchStreamUrl(fileslug) {
  // Step 1: Call embedhelper2.php to get ALL stream hosts + slug mappings
  let sources = [];
  try {
    const response = await fetchText(`${IQSMART_PLAYER}/embedhelper2.php`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'Referer': `${IQSMART_PLAYER}/evid/${fileslug}`,
        'Origin': IQSMART_PLAYER,
      },
      body: `sid=${fileslug}`,
    });
    const data = JSON.parse(response);
    if (data && data.sources && data.mresult) {
      const decoded = Buffer.from(data.mresult, 'base64').toString('utf8');
      const slugMap = JSON.parse(decoded);
      for (const [key, src] of Object.entries(data.sources)) {
        if (slugMap[key]) {
          const siteUrl = src.siteUrl || '';
          const hostMatch = siteUrl.match(/^https?:\/\/([^/]+)/);
          sources.push({
            key,
            friendlyName: src.friendlyName || key,
            streamHost: hostMatch ? hostMatch[1] : null,
            streamSlug: slugMap[key],
            siteUrl,
          });
        }
      }
    }
  } catch (e) {
    console.log(`[MultiMovies] embedhelper2.php failed: ${e.message}`);
  }
  if (sources.length === 0) {
    // Fallback: use default hanerix.com with the fileslug as slug
    sources.push({
      key: 'smwh',
      friendlyName: 'streamhg',
      streamHost: 'hanerix.com',
      streamSlug: fileslug,
      siteUrl: 'https://hanerix.com/e/',
    });
  }
  console.log(`[MultiMovies] Found ${sources.length} source(s): ${sources.map(s => s.friendlyName).join(', ')}`);

  // Step 2: For each source, visit the player page and decode the eval-packed JS
  // Return ALL stream URLs found (one per source)
  const results = [];
  for (const src of sources) {
    if (!src.streamHost) continue;
    // Different sources use different URL paths:
    //   - hanerix.com (streamhg): /e/<slug>
    //   - smoothpre.com (earnvids): /v/<slug>
    //   - others: try /e/<slug> first, then /v/<slug>
    const pathPrefix = src.siteUrl.match(/\/(e|v|embed|play)\//)?.[1] || 'e';
    const playerPageUrl = `https://${src.streamHost}/${pathPrefix}/${src.streamSlug}`;
    console.log(`[MultiMovies] Trying source: ${src.friendlyName} (${playerPageUrl})`);

    // Visit the player page
    let playerHtml = '';
    try {
      playerHtml = await fetchText(playerPageUrl, {
        headers: { 'Referer': IQSMART_PLAYER + '/' },
      });
    } catch (e) {
      console.log(`[MultiMovies] ${playerPageUrl} failed: ${e.message.slice(0, 60)}`);
      continue;
    }

    // Decode the eval-packed JS to find stream URLs
    const evalMatch = playerHtml.match(/eval\(function\(p,a,c,k,e,d\)\{[\s\S]*?\}\('([\s\S]+?)',\s*(\d+)\s*,\s*(\d+)\s*,\s*'([\s\S]+?)'\.split\('\|'\)\)/);
    if (!evalMatch) {
      console.log(`[MultiMovies] No eval-packed JS found on ${src.streamHost}`);
      continue;
    }

    const packed = evalMatch[1];
    const base = parseInt(evalMatch[2]);
    const count = parseInt(evalMatch[3]);
    const keys = evalMatch[4].split('|');

    // Decode: replace each base-N token with keys[parseInt(token, base)]
    const decoded = packed.replace(/\b\w+\b/g, (match) => {
      try {
        const n = parseInt(match, base);
        if (n >= 0 && n < keys.length && keys[n]) return keys[n];
      } catch (e) {}
      return match;
    });

    // Look for hls2 URL (preferred — has signed query params, no headers needed)
    const hls2Match = decoded.match(/"hls2":\s*"(https:\/\/[^"]+master\.m3u8[^"]*)"/);
    if (hls2Match) {
      console.log(`[MultiMovies] ✅ ${src.friendlyName}: hls2 URL found`);
      // Fetch master.m3u8 content
      let masterText = '';
      try {
        masterText = await fetchText(hls2Match[1], {
          headers: { 'Accept': 'application/vnd.apple.mpegurl, */*' },
        });
      } catch (e) { /* ignore — will fall back to URL only */ }
      results.push({
        url: hls2Match[1],
        masterText: masterText.startsWith('#EXTM3U') ? masterText : null,
        streamHost: src.streamHost,
        sourceName: src.friendlyName,
      });
      continue;
    }

    // Fallback: hls4 URL (requires Origin + Referer + Range headers)
    const hls4Match = decoded.match(/"hls4":\s*"([^"]+master\.m3u8[^"]*)"/);
    if (hls4Match) {
      let url = hls4Match[1];
      if (!url.startsWith('http')) url = `https://${src.streamHost}${url}`;
      console.log(`[MultiMovies] ✅ ${src.friendlyName}: hls4 URL found (requires headers)`);
      results.push({
        url,
        masterText: null,  // can't fetch without proper session
        streamHost: src.streamHost,
        sourceName: src.friendlyName,
        requiresHeaders: true,
      });
      continue;
    }

    console.log(`[MultiMovies] No stream URL found for ${src.friendlyName}`);
  }

  if (results.length === 0) {
    console.log('[MultiMovies] No stream URLs found from any source');
    return null;
  }
  // Return the first result (or all if multiple)
  // For backward compatibility, return the first result as an object
  // But also include all results so the caller can use them all
  if (results.length === 1) {
    return results[0];
  }
  return results;  // array of results
}

// ─── Parse master.m3u8 text to extract quality variants ────────────────────
function parseMasterM3u8(text, streamUrl) {
  if (!text || !text.startsWith('#EXTM3U')) return [];
  const variants = [];
  let currentRes = null, currentBw = null, currentCodecs = null, currentAudioLang = [];
  const lines = text.split('\n');
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i].trim();
    if (line.startsWith('#EXT-X-MEDIA:TYPE=AUDIO')) {
      const langMatch = line.match(/LANGUAGE="([^"]+)"/);
      if (langMatch) currentAudioLang.push(langMatch[1]);
    } else if (line.startsWith('#EXT-X-STREAM-INF:')) {
      const resMatch = line.match(/RESOLUTION=(\d+)x(\d+)/);
      const bwMatch = line.match(/BANDWIDTH=(\d+)/);
      const codecsMatch = line.match(/CODECS="([^"]+)"/);
      if (resMatch) currentRes = { w: parseInt(resMatch[1]), h: parseInt(resMatch[2]) };
      if (bwMatch) currentBw = parseInt(bwMatch[1]);
      if (codecsMatch) currentCodecs = codecsMatch[1];
    } else if (line && !line.startsWith('#') && currentRes) {
      let variantUrl;
      if (line.startsWith('http')) {
        variantUrl = line;
      } else if (line.startsWith('/')) {
        const u = new URL(streamUrl);
        variantUrl = `${u.protocol}//${u.host}${line}`;
      } else {
        const base = streamUrl.replace(/\/[^/]*$/, '');
        variantUrl = `${base}/${line}`;
      }
      variants.push({
        resolution: currentRes,
        bandwidth: currentBw,
        codecs: currentCodecs,
        audioLanguages: [...currentAudioLang],
        url: variantUrl,
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

// ─── Fetch + parse master.m3u8 (used as fallback when fetchStreamUrl returns
//      only a URL, not the master text) ──────────────────────────────────────
async function fetchMasterM3u8(streamUrl) {
  // The hls4 URL on hanerix.com requires:
  //   1. Origin: https://pro.iqsmartgames.com
  //   2. Referer: https://pro.iqsmartgames.com/
  //   3. Range: bytes=0- (REQUIRED — without it, GET returns 403!)
  //   4. Cookies from /e/ page session (handled in fetchStreamUrl)
  // If we're here without cookies, the request will likely 403.
  let text = '';
  try {
    text = await fetchText(streamUrl, {
      headers: {
        'Referer': IQSMART_PLAYER + '/',
        'Origin': IQSMART_PLAYER,
        'Accept': 'application/vnd.apple.mpegurl, */*',
        'Range': 'bytes=0-',
      },
    });
  } catch (e) {
    return [];
  }
  return parseMasterM3u8(text, streamUrl);
}

// ─── Build Stremio stream object ────────────────────────────────────────────
function buildStream(variant, info, audioLangs, fileslug, streamSlug) {
  const langsLabel = audioLangs && audioLangs.length > 0
    ? audioLangs.join('+')
    : 'multi';

  // The stream variant URL points to hanerix.com which only allows HEAD requests.
  // GET requests return 403 because nginx checks for browser-like request patterns.
  // To make it playable in Stremio, we route the stream through the
  // rozgarlelo.modiplay.xyz proxy, which:
  //   1. Fetches the playlist from hanerix.com (with proper Referer)
  //   2. Rewrites variant URLs to /proxy.php?serve_m3u8=1&url=...
  //   3. Returns a playable HLS playlist with TikTok-disguised segments
  //      (Content-Type: image/png, but actually TS video data — Stremio's
  //       hls.js plays them transparently).
  // We construct the proxy URL using the fileslug and the variant's relative path.
  const MODIPLAY_PROXY = 'https://rozgarlelo.modiplay.xyz';
  const REF_URL = `https://vibuxer.com/e/${streamSlug}`;  // Note: vibuxer hosts the actual stream
  const EBD_URL = 'https://vibuxer.com';
  // Get the variant path (relative part after /stream/<sig>/.../<id>/)
  // e.g., index-f3-v1-a1.m3u8
  const variantRelPath = variant.url.includes('/master.m3u8')
    ? variant.url.replace(/.*\/stream\//, '').replace(/\/master\.m3u8.*$/, '')
    : variant.url.replace(/.*\/stream\//, '').replace(/\/[^/]+\.m3u8.*$/, '');
  // Extract the stream path: <sig>/<token>/<expiry>/<id>/<variant-file>
  // The variant.url from the master playlist already has the right path
  // But it points to premilkyway.com — we need to construct the vibuxer.com equivalent
  // Easier: just use the variant filename (index-fN-vN-aN.m3u8) and append to the base stream URL
  const variantFilename = variant.url.split('/').pop().split('?')[0];  // e.g., index-f3-v1-a1.m3u8

  // We need to extract the actual stream path from the master.m3u8 URL
  // The hls4 URL was: https://hanerix.com/stream/<sig>/<token>/<expiry>/<id>/master.m3u8
  // The variant URL: https://hanerix.com/stream/<sig>/<token>/<expiry>/<id>/index-f3-v1-a1.m3u8
  // But we use the modiplay proxy which expects:
  //   /proxy.php?serve_m3u8=1&ref=<vibuxer_url>&url=<vibuxer_stream_url>&ebd=<vibuxer>
  // We get the vibuxer stream URL by inspecting the modiplay.xyz embed page
  // (which sets up the proxy with the right ref + url params)
  // For now, we just return the master.m3u8 proxy URL — Stremio's hls.js will
  // follow the relative variant URLs in the playlist automatically.
  // But the variant URLs in the playlist are also /proxy.php?... relative paths
  // So we need to provide the FULL proxy URL for the master.m3u8

  // Use the proxy URL pattern we observed:
  // https://rozgarlelo.modiplay.xyz/proxy.php?serve_m3u8=1&ref=<vibuxer_url>&url=<vibuxer_master.m3u8>&ebd=<vibuxer>
  // We get these by visiting modiplay.xyz/embed/imdb/movie?id=<imdb_id> and parsing the iframe
  // For simplicity, we'll use the hls4 URL directly (Stremio can fetch it with proper headers)
  // and let proxyHeaders handle the Origin/Referer

  // Determine if this is the hls2 URL (premilkyway.com — no headers needed)
  // or hls4 URL (hanerix.com — requires Origin + Referer + Range headers)
  const isHls2Url = variant.url.includes('premilkyway.com') ||
                    variant.url.includes('.urlset/') ||
                    (variant.url.includes('?t=') && variant.url.includes('&s='));
  const isHls4Url = variant.url.includes('hanerix.com') ||
                    variant.url.includes('/stream/') && !isHls2Url;

  const result = {
    name: `${PROVIDER_NAME} | ${variant.quality} | ${langsLabel}`,
    title: `${info.title}${info.year ? ` (${info.year})` : ''} [MultiMovies ${variant.resolution.w}x${variant.resolution.h} ${variant.quality} ${langsLabel}]`,
    url: variant.url,  // the master.m3u8 or variant index.m3u8 URL
    quality: variant.quality,
    type: 'application/vnd.apple.mpegurl',
    behaviorHints: {
      bingeGroup: `multimovies-${variant.quality}-${langsLabel}`,
    },
  };

  // Only add proxyHeaders for hls4 URLs (hanerix.com requires them)
  // hls2 URLs (premilkyway.com) have signed query params and need no extra headers
  if (isHls4Url) {
    result.behaviorHints.proxyHeaders = {
      request: {
        'User-Agent': UA,
        'Referer': `${IQSMART_PLAYER}/`,
        'Origin': IQSMART_PLAYER,
        'Range': 'bytes=0-',
      },
    };
  }
  return result;
}

// ─── Main entry point ──────────────────────────────────────────────────────
async function getStreams(tmdbId, type, season, episode) {
  tmdbId = String(tmdbId);
  const isTV = type === 'tv' || type === 'series';
  console.log(`[MultiMovies] Request: tmdb=${tmdbId} type=${type}` +
              (isTV ? ` S${season}E${episode}` : ''));

  // 1. Fetch TMDB info
  const info = await getTMDBInfo(tmdbId, type);
  if (!info) {
    console.log('[MultiMovies] TMDB fetch failed');
    return [];
  }
  console.log(`[MultiMovies] TMDB: ${info.title}${info.year ? ` (${info.year})` : ''}` +
              ` IMDB: ${info.imdbId || 'N/A'}`);

  // 2. Search multimovies.beer for the title
  const results = await searchSite(info.title);
  if (results.length === 0) {
    console.log('[MultiMovies] Title not found on multimovies.beer');
    return [];
  }
  console.log(`[MultiMovies] Found ${results.length} matching URLs`);

  // 3. Pick the best matching URL (first one usually matches the title)
  // For TV we want /tvshows/, for movies we want /movies/
  const wantedPath = isTV ? '/tvshows/' : '/movies/';
  const matchingUrls = results.filter(u => u.includes(wantedPath));
  if (matchingUrls.length === 0) {
    console.log(`[MultiMovies] No ${isTV ? 'TV shows' : 'movies'} matching title`);
    return [];
  }
  const pageUrl = matchingUrls[0];
  console.log(`[MultiMovies] Using: ${pageUrl}`);

  // 4. Get post_id from page
  const postId = await getPostId(pageUrl);
  if (!postId) {
    console.log('[MultiMovies] Cannot find post_id');
    return [];
  }
  console.log(`[MultiMovies] post_id: ${postId}`);

  // 5. Get embed URL via doo_player_ajax (try nume=3 first = iqsmartgames, then nume=1 = filesforever)
  let embedUrl = null;
  for (const nume of [3, 1, 2, 4, 5]) {
    try {
      const data = await getPlayerEmbedUrl(postId, isTV ? 'tv' : 'movie', nume);
      if (data && data.embed_url) {
        embedUrl = data.embed_url;
        console.log(`[MultiMovies] Got embed URL (nume=${nume}): ${embedUrl.slice(0, 80)}...`);
        break;
      }
    } catch (e) {
      console.log(`[MultiMovies] nume=${nume} failed: ${e.message.slice(0, 60)}`);
    }
  }
  if (!embedUrl) {
    console.log('[MultiMovies] No embed URL found from any player');
    return [];
  }

  // 6. Parse embed URL for IDs
  const parsed = parseEmbedUrl(embedUrl);
  const useImdbId = parsed.imdbId || info.imdbId;
  const useTmdbId = parsed.tmdbId || tmdbId;

  // 7. For movies: resolve stream via iqsmartgames API
  let files = null;
  if (!isTV) {
    files = await resolveStreamViaIqsmart(useImdbId, useTmdbId);
    if (!files || files.length === 0) {
      console.log('[MultiMovies] iqsmart API returned no files');
      return [];
    }
    console.log(`[MultiMovies] Found ${files.length} file(s):`);
    for (const f of files) {
      console.log(`  - ${f.filename} (${f.fsize}) [slug: ${f.fileslug}]`);
    }
  } else {
    // For TV: the embed URL on streams.iqsmartgames.com directly contains
    // tmdb/season/episode. The player.js on pro.iqsmartgames.com will
    // resolve the stream. We need to fetch the embed URL and look for
    // the API call to /mymovieapi with the TV params
    console.log(`[MultiMovies] TV: using TMDB ID ${useTmdbId} S${season}E${episode}`);
    // Try the API with TMDB ID + season + episode
    try {
      const apiUrl = `${IQSMART_API}/mymovieapi?tmdbid=${useTmdbId}&season=${season}&episode=${episode}&key=${IQSMART_KEY}`;
      const data = await fetchJson(apiUrl, {
        headers: { 'Origin': IQSMART_API, 'Referer': IQSMART_API + '/' },
      });
      if (data.success && data.data && data.data.length > 0) {
        files = data.data.map(d => ({
          fileslug: d.fileslug,
          filename: d.filename,
          fsize: d.fsize,
        }));
        console.log(`[MultiMovies] Found ${files.length} file(s) for TV episode`);
      } else {
        console.log('[MultiMovies] iqsmart API returned no TV files');
        return [];
      }
    } catch (e) {
      console.log(`[MultiMovies] TV API failed: ${e.message}`);
      return [];
    }
  }

  // 8. Fetch the stream URL for each file (usually 1-3 files per title)
  // fetchStreamUrl now returns an array of results (one per CDN source)
  // or a single object — handle both cases
  const allStreams = [];
  const seenUrls = new Set();
  for (let i = 0; i < files.length; i++) {
    const file = files[i];
    console.log(`[MultiMovies] Resolving stream for file #${i+1}: ${file.fileslug}`);
    const streamResult = await fetchStreamUrl(file.fileslug);
    if (!streamResult) {
      console.log(`[MultiMovies] Could not resolve stream for ${file.fileslug}`);
      continue;
    }
    // Normalize to array
    const results = Array.isArray(streamResult) ? streamResult : [streamResult];
    console.log(`[MultiMovies] Got ${results.length} stream source(s)`);

    for (const result of results) {
      const streamUrl = result.url;
      const masterText = result.masterText;
      const sourceName = result.sourceName || 'unknown';
      console.log(`[MultiMovies] Source [${sourceName}]: ${streamUrl.slice(0, 100)}...`);

      // 9. Parse master.m3u8 to get quality variants
      let variants = [];
      if (masterText && masterText.startsWith('#EXTM3U')) {
        variants = parseMasterM3u8(masterText, streamUrl);
      } else {
        // Fallback: try to fetch master.m3u8 directly
        variants = await fetchMasterM3u8(streamUrl);
      }
      if (variants.length === 0) {
        // Maybe the URL itself is a single-quality playlist — use directly
        const quality = file.filename.includes('2160') || file.filename.includes('4K') ? '2160p'
                      : file.filename.includes('1080') ? '1080p'
                      : file.filename.includes('720') ? '720p' : '480p';
        const stream = buildStream({
          url: streamUrl,
          resolution: { w: 0, h: 0 },
          quality,
          audioLanguages: ['hi', 'en'],
        }, info, ['hi', 'en'], file.fileslug, file.fileslug);
        // Add source name to the stream name for identification
        stream.name = `${PROVIDER_NAME} [${sourceName}] | ${quality} | hi+en`;
        if (!seenUrls.has(streamUrl)) {
          seenUrls.add(streamUrl);
          allStreams.push(stream);
        }
        continue;
      }
      // Sort variants by quality descending (1080p first)
      variants.sort((a, b) => b.resolution.h - a.resolution.h);
      for (const v of variants) {
        if (seenUrls.has(v.url)) continue;
        seenUrls.add(v.url);
        const stream = buildStream(v, info, v.audioLanguages, file.fileslug, file.fileslug);
        // Add source name to the stream name for identification
        stream.name = `${PROVIDER_NAME} [${sourceName}] | ${v.quality} | ${v.audioLanguages.join('+')}`;
        allStreams.push(stream);
      }
    }
  }

  // 10. Sort by quality (4K first, then 1080p, etc.)
  const qOrder = { '2160p': 0, '1080p': 1, '720p': 2, '480p': 3, '360p': 4 };
  allStreams.sort((a, b) => (qOrder[a.quality] || 99) - (qOrder[b.quality] || 99));

  console.log(`[MultiMovies] ${allStreams.length} stream(s) total`);
  return allStreams;
}

// ─── Module exports ─────────────────────────────────────────────────────────
module.exports = {
  getStreams,
  getTMDBInfo,
  searchSite,
  getPostId,
  getPlayerEmbedUrl,
  parseEmbedUrl,
  resolveStreamViaIqsmart,
  fetchStreamUrl,
  fetchMasterM3u8,
  parseMasterM3u8,
  SITE_BASE,
  IQSMART_API,
  IQSMART_PLAYER,
};

// ─── CLI ────────────────────────────────────────────────────────────────────
if (require.main === module) {
  const args = process.argv.slice(2);
  if (args.length === 0) {
    console.log('MultiMovies.beer Direct Stream Extractor (up to 1080p, dual audio)');
    console.log('');
    console.log('Usage:');
    console.log('  node multimovies_all_in_one.js <tmdbId> <movie|tv> [season] [episode]');
    console.log('  node multimovies_all_in_one.js search "dune"');
    console.log('');
    console.log('Examples:');
    console.log('  node multimovies_all_in_one.js 693134 movie         # Dune Part Two');
    console.log('  node multimovies_all_in_one.js 238843 tv 1 1        # The Fable S01E01');
    console.log('  node multimovies_all_in_one.js search "dune"');
    console.log('');
    console.log('Backend status:');
    console.log('  ✅ multimovies.beer (DooPlay WordPress)');
    console.log('  ✅ streams.iqsmartgames.com (fileslug API)');
    console.log('  ✅ pro.iqsmartgames.com (player + stream resolver)');
    process.exit(1);
  }

  if (args[0] === 'search') {
    const q = args[1];
    if (!q) { console.error('Search query required'); process.exit(1); }
    searchSite(q).then(urls => {
      console.log(`\n=== Search results for "${q}" ===`);
      urls.slice(0, 15).forEach((u, i) => console.log(`${i+1}. ${u}`));
      console.log(`\nTotal: ${urls.length} URLs`);
    }).catch(e => { console.error('FATAL: ' + e.message); process.exit(1); });
  } else {
    const tmdbId = args[0];
    const type = args[1] || 'movie';
    const season = args[2] || null;
    const episode = args[3] || null;
    getStreams(tmdbId, type, season, episode)
      .then(streams => {
        console.log('\n=== Final playable streams (sorted by quality) ===');
        if (streams.length === 0) {
          console.log('No streams found.');
        } else {
          streams.forEach((s, i) => {
            console.log(`${i+1}. ${s.name}`);
            console.log(`   Title: ${s.title}`);
            console.log(`   URL:   ${s.url.slice(0, 150)}${s.url.length > 150 ? '...' : ''}`);
            if (s.behaviorHints?.proxyHeaders?.request) {
              console.log(`   Headers: Origin=${s.behaviorHints.proxyHeaders.request.Origin || 'N/A'}`);
            }
          });
          console.log(`\nTotal: ${streams.length} stream(s)`);
        }
      })
      .catch(e => { console.error('FATAL: ' + e.stack); process.exit(1); });
  }
}
