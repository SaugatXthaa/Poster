// 7Movies (7movies.in) All-In-One — Single-File Pure-Node.js Scraper
// =========================================================================
// Fetches stream URLs from 7movies.in via its embed player at embed.vidrift.in.
//
// ARCHITECTURE
// ------------
// 7movies.in is a Next.js frontend. When the user clicks "Play", it loads
// an iframe pointing to embed.vidrift.in, which serves an HTML player page.
// That page contains an inline `embedMeta` JSON object with:
//   - playbackToken (JWT-style, short-lived ~10 min)
//   - selfhostUrl  (the DIRECT HLS playlist URL on media.vidrift.in)
//   - provider     ('selfhost' if direct available, otherwise 'vaplayer')
//
// The media.vidrift.in CDN requires NO authentication at all — no Referer,
// no Origin, no User-Agent. The HLS playlist and all .ts segments are
// publicly fetchable. This means:
//
//   NO PROXY NEEDED. NO proxyHeaders NEEDED. NO penguplay/pengu.uk.
//
// The direct `selfhostUrl` stream is the highest-quality source (no quality
// cap — typically 1080p/2160p depending on the content). For backup, we
// also query /api/source?provider=vaplayer which returns ~720p capped
// "Earth" mirrors (proxied through embed.vidrift.in/api/proxy/hls).
//
// URL PATTERNS
// ------------
// Embed page (HTML):
//   Movie: https://embed.vidrift.in/embed/movie/{tmdbId}
//   TV:    https://embed.vidrift.in/embed/tv/{tmdbId}/{season}/{episode}
//
// Direct stream (HLS playlist, no auth):
//   Movie: https://media.vidrift.in/movie_{tmdbId}/vod.m3u8
//   TV:    https://media.vidrift.in/tv_{tmdbId}/Season {S}/S0{S}E0{E}/vod.m3u8
//          (note: spaces in the URL — must be URL-encoded as %20)
//
// Source API (returns stream list for a given provider):
//   GET https://embed.vidrift.in/api/source/{typePath}?token={playbackToken}&provider={provider}
//     typePath = movie/{tmdbId}  OR  tv/{tmdbId}/{season}/{episode}
//     provider = selfhost | vaplayer | vidgod | turbo
//   Returns: { success, streams: [{index, url, proxyUrl, type, provider, direct}], subtitles }
//
// Subtitles:
//   GET https://embed.vidrift.in/api/subtitles/{typePath}/{Language}
//   Languages available: English, Spanish, French, German, Italian, Portuguese,
//                        Czech, Slovak, Polish, Turkish
//
// USAGE
// -----
//   const sev = require('./7movies_all_in_one.js');
//   const streams = await sev.getStreams('27205', 'movie');
//   const streams = await sev.getStreams('1396', 'tv', 1, 1);  // Breaking Bad S01E01
//
// TEST
//   node 7movies_all_in_one.js 27205 movie
//   node 7movies_all_in_one.js 1396 tv 1 1

'use strict';

const { execFileSync } = require('child_process');

const PROVIDER_NAME = '7Movies';
const EMBED_ORIGIN = 'https://embed.vidrift.in';
const MEDIA_ORIGIN = 'https://media.vidrift.in';
const SEVENMOVIES_ORIGIN = 'https://7movies.in';
const TMDB_API_KEY = '8476a7ab80ad76f0936744df0430e67c';

const UA =
  'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 ' +
  '(KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';

// All known vidrift providers (in priority order — selfhost first because
// it's the only one that returns the uncapped direct stream).
const ALL_PROVIDERS = ['selfhost', 'vaplayer', 'vidgod', 'turbo'];

// ---------------------------------------------------------------------------
// curl-based fetch — bypasses Cloudflare's TLS fingerprint block on
// embed.vidrift.in's HTML pages (Node.js fetch gets 403, curl gets 200).
// Used for fetching the embed HTML page. JSON APIs (/api/source, /api/subtitles)
// are NOT blocked and use plain Node fetch.
// ---------------------------------------------------------------------------
function curlFetch(url, options) {
  options = options || {};
  const headers = options.headers || {};
  const args = ['-s', '--max-time', String(options.timeout ? Math.floor(options.timeout / 1000) : 15)];
  for (const [k, v] of Object.entries(headers)) {
    args.push('-H', k + ': ' + v);
  }
  if (options.method === 'HEAD') {
    args.push('-I');
  }
  args.push(url);
  try {
    const out = execFileSync('curl', args, { maxBuffer: 10 * 1024 * 1024 });
    if (options.method === 'HEAD') {
      // parse status line + headers from -I output
      const text = out.toString();
      const statusMatch = text.match(/^HTTP\/[\d.]+\s+(\d+)/);
      return { ok: statusMatch ? parseInt(statusMatch[1], 10) < 400 : false, status: statusMatch ? parseInt(statusMatch[1], 10) : 0, text: () => Promise.resolve(text) };
    }
    return {
      ok: true,
      status: 200,
      text: () => Promise.resolve(out.toString()),
    };
  } catch (e) {
    const err = new Error('curl failed: ' + (e.message || 'unknown'));
    err.curlExit = e.status;
    throw err;
  }
}

// ---------------------------------------------------------------------------
// Smart fetch: try plain Node fetch first (fast, works for JSON APIs),
// fall back to curl if Cloudflare blocks (403) — needed for HTML pages.
// ---------------------------------------------------------------------------
async function smartFetch(url, options) {
  options = options || {};
  try {
    const res = await fetch(url, options);
    // If Cloudflare blocks, retry with curl
    if (res.status === 403) {
      return curlFetch(url, options);
    }
    return res;
  } catch (e) {
    // Network error / TLS issue — try curl
    return curlFetch(url, options);
  }
}

// ---------------------------------------------------------------------------
// TMDB metadata helper (for stream titles)
// ---------------------------------------------------------------------------
async function getTMDBInfo(tmdbId, type) {
  const url = `https://api.themoviedb.org/3/${type === 'tv' ? 'tv' : 'movie'}/${tmdbId}` +
    `?api_key=${TMDB_API_KEY}&language=en-US`;
  const res = await fetch(url, {
    headers: { 'User-Agent': UA, Accept: 'application/json' },
    signal: AbortSignal.timeout(10000),
  });
  if (!res.ok) throw new Error(`TMDB HTTP ${res.status}`);
  const j = await res.json();
  return {
    title: j.title || j.name || 'Unknown',
    year: (j.release_date || j.first_air_date || '').slice(0, 4),
    type,
    tmdbId: String(tmdbId),
  };
}

// ---------------------------------------------------------------------------
// Build the embed URL for a given media item
// ---------------------------------------------------------------------------
function buildEmbedUrl(tmdbId, type, season, episode) {
  if (type === 'tv') {
    return `${EMBED_ORIGIN}/embed/tv/${tmdbId}/${season}/${episode}`;
  }
  return `${EMBED_ORIGIN}/embed/movie/${tmdbId}`;
}

// Build the typePath used by /api/source and /api/subtitles
function buildTypePath(tmdbId, type, season, episode) {
  if (type === 'tv') {
    return `tv/${tmdbId}/${season}/${episode}`;
  }
  return `movie/${tmdbId}`;
}

// ---------------------------------------------------------------------------
// Fetch the embed page HTML and extract the inline embedMeta JSON object
// ---------------------------------------------------------------------------
async function fetchEmbedMeta(tmdbId, type, season, episode) {
  const embedUrl = buildEmbedUrl(tmdbId, type, season, episode);
  // Cloudflare on embed.vidrift.in blocks Node.js fetch by TLS fingerprint.
  // Use smartFetch (curl fallback) for the HTML page.
  const res = await smartFetch(embedUrl, {
    headers: {
      'User-Agent': UA,
      'Referer': SEVENMOVIES_ORIGIN + '/',
      'Accept': 'text/html,application/xhtml+xml',
    },
    signal: AbortSignal.timeout(15000),
  });
  if (!res.ok) {
    throw new Error(`embed HTTP ${res.status}`);
  }
  const html = await res.text();

  // Extract: var embedMeta = {...};
  const m = html.match(/var\s+embedMeta\s*=\s*(\{[^;]+\});/);
  if (!m) {
    throw new Error('embedMeta not found in embed page');
  }
  let meta;
  try {
    meta = JSON.parse(m[1]);
  } catch (e) {
    throw new Error('embedMeta JSON parse failed: ' + e.message);
  }

  // Also extract the subtitleTracks array (declared before embedMeta)
  const subMatch = html.match(/var\s+subtitleTracks\s*=\s*(\[[^\]]+\]);/);
  let subtitles = [];
  if (subMatch) {
    try {
      subtitles = JSON.parse(subMatch[1]);
    } catch (e) {
      // ignore — subtitles are optional
    }
  }

  return { meta, subtitles, embedUrl };
}

// ---------------------------------------------------------------------------
// Fetch streams from /api/source for a given provider
// Returns an array of stream objects, or [] on failure.
// ---------------------------------------------------------------------------
async function fetchProviderStreams(typePath, playbackToken, provider) {
  const url = `${EMBED_ORIGIN}/api/source/${typePath}` +
    `?token=${encodeURIComponent(playbackToken)}` +
    `&provider=${encodeURIComponent(provider)}`;
  try {
    const res = await fetch(url, {
      headers: {
        'User-Agent': UA,
        'Referer': EMBED_ORIGIN + '/',
        'Accept': 'application/json',
      },
      signal: AbortSignal.timeout(8000),
    });
    if (!res.ok) {
      return { provider, error: `HTTP ${res.status}`, streams: [] };
    }
    const j = await res.json();
    if (!j || !j.success || !Array.isArray(j.streams)) {
      return { provider, error: 'no streams', streams: [] };
    }
    return { provider, streams: j.streams, maxRes: j.maxRes, quality: j.quality };
  } catch (e) {
    return { provider, error: e.message, streams: [] };
  }
}

// ---------------------------------------------------------------------------
// Validate a stream URL is actually reachable (HEAD or GET first 4 bytes)
// ---------------------------------------------------------------------------
async function validateStreamUrl(url) {
  try {
    const res = await fetch(url, {
      method: 'GET',
      headers: { 'User-Agent': UA, Range: 'bytes=0-3' },
      signal: AbortSignal.timeout(8000),
    });
    if (!res.ok && res.status !== 206) return false;
    const body = await res.text();
    // HLS playlists start with #EXTM3U; .ts segments return binary
    return body.trimStart().startsWith('#EXTM3U') || body.length > 0;
  } catch (e) {
    return false;
  }
}

// ---------------------------------------------------------------------------
// Probe a stream's actual video resolution using ffprobe.
// Downloads the first ~1.5 MB of the first .ts segment and reads its
// width/height/codec. This lets us label streams as 4K / 1080p / 720p
// accurately instead of hardcoding "1080p".
//
// Returns: { width, height, codec, quality } or null on failure.
//   quality is one of: '2160p' (4K), '1440p', '1080p', '720p', '576p', '480p', 'SD'
// ---------------------------------------------------------------------------
function mapResolutionToQuality(w, h) {
  // Use WIDTH as the canonical resolution axis. This correctly handles
  // scope/ultra-wide content (e.g. 1920x800 is 1080p scope, 3840x1600 is
  // 4K scope) — using height alone would mislabel them as 720p/1440p.
  //   4K UHD:    width >= 3840  (covers 3840x2160 AND 3840x1600 scope)
  //   1440p QHD: width >= 2560
  //   1080p:     width >= 1920  (covers 1920x1080 AND 1920x800 scope)
  //   720p:      width >= 1280
  //   576p:      width >= 1024
  //   480p:      width >= 854
  //   SD:        anything smaller
  const r = Math.max(w, h);
  if (r >= 3840) return '2160p';        // 4K UHD
  if (r >= 2560) return '1440p';        // 2K QHD
  if (r >= 1920) return '1080p';        // Full HD (also covers 1920x800 scope)
  if (r >= 1280) return '720p';         // HD
  if (r >= 1024) return '576p';         // PAL SD
  if (r >= 854)  return '480p';         // NTSC SD
  return 'SD';
}

async function probeStreamResolution(streamUrl) {
  try {
    // 1. Fetch the m3u8 playlist to find the first .ts segment
    const m3u8Res = await fetch(streamUrl, {
      headers: { 'User-Agent': UA },
      signal: AbortSignal.timeout(8000),
    });
    if (!m3u8Res.ok) return null;
    const m3u8Text = await m3u8Res.text();

    // If this is a master playlist (has #EXT-X-STREAM-INF), recurse into
    // the highest-bandwidth variant to find the actual media playlist.
    if (m3u8Text.includes('#EXT-X-STREAM-INF')) {
      const variants = [];
      const lines = m3u8Text.split('\n');
      for (let i = 0; i < lines.length; i++) {
        if (lines[i].startsWith('#EXT-X-STREAM-INF')) {
          const bwMatch = lines[i].match(/BANDWIDTH=(\d+)/);
          const bandwidth = bwMatch ? parseInt(bwMatch[1], 10) : 0;
          const variantUrl = lines[i + 1] ? lines[i + 1].trim() : '';
          if (variantUrl) {
            // Resolve relative URL
            const absUrl = variantUrl.startsWith('http')
              ? variantUrl
              : new URL(variantUrl, streamUrl).href;
            variants.push({ bandwidth, url: absUrl });
          }
        }
      }
      if (variants.length > 0) {
        // Pick highest bandwidth variant (most likely 4K if available)
        variants.sort((a, b) => b.bandwidth - a.bandwidth);
        return probeStreamResolution(variants[0].url);
      }
    }

    // Find first .ts segment
    const segMatch = m3u8Text.match(/^[^\s#][^\s]*\.ts$/m);
    if (!segMatch) return null;
    const segUrl = segMatch[0].startsWith('http')
      ? segMatch[0]
      : new URL(segMatch[0], streamUrl).href;

    // 2. Download first ~1.5 MB of the segment (enough for ffprobe to read
    //    codec parameters from the SPS/PPS NAL units)
    const segRes = await fetch(segUrl, {
      headers: { 'User-Agent': UA, Range: 'bytes=0-1500000' },
      signal: AbortSignal.timeout(12000),
    });
    if (!segRes.ok && segRes.status !== 206) return null;
    const buf = Buffer.from(await segRes.arrayBuffer());
    if (buf.length < 10000) return null; // too small to probe

    // 3. Write to temp file and run ffprobe
    const { writeFileSync, unlinkSync } = require('fs');
    const { join } = require('path');
    const { tmpdir } = require('os');
    const tmpFile = join(tmpdir(), '7movies_probe_' + process.pid + '_' + Date.now() + '.ts');
    writeFileSync(tmpFile, buf);

    try {
      const { execFileSync } = require('child_process');
      const out = execFileSync('ffprobe', [
        '-v', 'error',
        '-select_streams', 'v:0',
        '-show_entries', 'stream=width,height,codec_name,bit_rate',
        '-of', 'default=noprint_wrappers=1:nokey=0',
        tmpFile,
      ], { timeout: 8000, maxBuffer: 1024 * 1024 }).toString();

      const widthMatch  = out.match(/^width=(\d+)/m);
      const heightMatch = out.match(/^height=(\d+)/m);
      const codecMatch  = out.match(/^codec_name=(\w+)/m);
      if (!widthMatch || !heightMatch) return null;

      const w = parseInt(widthMatch[1], 10);
      const h = parseInt(heightMatch[1], 10);
      const codec = codecMatch ? codecMatch[1] : 'h264';
      const quality = mapResolutionToQuality(w, h);
      return { width: w, height: h, codec, quality };
    } finally {
      try { unlinkSync(tmpFile); } catch (e) { /* ignore */ }
    }
  } catch (e) {
    return null;
  }
}

// ---------------------------------------------------------------------------
// Build a Stremio stream object for a direct HLS URL
// ---------------------------------------------------------------------------
function buildStream(opts) {
  const s = {
    name: PROVIDER_NAME + (opts.serverLabel ? ' - ' + opts.serverLabel : ''),
    title: opts.title,
    url: opts.url,
    quality: opts.quality || '1080p',
    type: opts.isMp4 ? 'video/mp4' : 'application/vnd.apple.mpegurl',
    behaviorHints: {
      bingeGroup: opts.bingeGroup || ('7movies-' + (opts.serverLabel || 'default').toLowerCase()),
    },
  };
  // 7movies / vidrift CDNs need NO auth at all — but for safety on vaplayer
  // proxy URLs (which go through embed.vidrift.in), we set Referer.
  if (opts.needsReferer) {
    s.behaviorHints.proxyHeaders = {
      request: {
        'User-Agent': UA,
        'Referer': EMBED_ORIGIN + '/',
        'Origin': EMBED_ORIGIN,
      },
    };
  }
  if (opts.subtitles && opts.subtitles.length > 0) {
    s.subtitles = opts.subtitles.map(function (sub) {
      return { id: sub.code, url: sub.url, lang: sub.label };
    });
  }
  return s;
}

// ---------------------------------------------------------------------------
// Main entry: get streams for a TMDB item
// ---------------------------------------------------------------------------
async function getStreams(tmdbId, type, season, episode) {
  const isMovie = type !== 'tv';
  tmdbId = String(tmdbId);

  // Input validation
  if (!isMovie && (season == null || episode == null)) {
    console.log('[7Movies] TV request without season/episode — returning empty');
    return [];
  }
  if (!tmdbId) {
    console.log('[7Movies] Empty TMDB ID — returning empty');
    return [];
  }

  console.log('[7Movies] Request: tmdb=' + tmdbId + ' type=' + type +
    (isMovie ? '' : ' S' + season + 'E' + episode));

  // Get TMDB info (with 10s timeout)
  let info;
  try {
    info = await Promise.race([
      getTMDBInfo(tmdbId, type),
      new Promise(function (_, reject) {
        setTimeout(function () { reject(new Error('TMDB timeout')); }, 10000);
      }),
    ]);
  } catch (e) {
    console.log('[7Movies] TMDB fetch error: ' + e.message);
    info = { title: 'TMDB ' + tmdbId, year: '', type, tmdbId };
  }
  console.log('[7Movies] TMDB: ' + info.title + (info.year ? ' (' + info.year + ')' : ''));

  // Fetch embed page → parse embedMeta
  let embedData;
  try {
    embedData = await fetchEmbedMeta(tmdbId, type, season, episode);
  } catch (e) {
    console.log('[7Movies] Embed fetch failed: ' + e.message);
    return [];
  }
  const meta = embedData.meta;
  const subtitleTracks = embedData.subtitles;
  console.log('[7Movies] Embed OK. provider=' + meta.provider +
    ', selfhostUrl=' + (meta.selfhostUrl || 'NULL') +
    ', subtitles=' + subtitleTracks.length);

  // If selfhostUrl is null, vidrift doesn't have the content directly.
  if (!meta.selfhostUrl) {
    console.log('[7Movies] No selfhostUrl — vidrift does not have this content directly');
    // Still try /api/source in case vaplayer has it
  }

  // Build Stremio subtitles list (deduplicated)
  const stremioSubs = subtitleTracks.slice(0, 8).map(function (s) {
    return { code: s.code, label: s.label, url: s.url };
  });

  // --- Primary stream: the direct selfhostUrl from embedMeta (no auth needed)
  // Probe the actual resolution so we can label it 4K / 1080p / 720p correctly.
  // Run the validation + resolution probe in parallel with the backup provider
  // fetches to hide latency.
  const allStreams = [];
  const directProbePromise = (async function () {
    if (!meta.selfhostUrl) return null;
    const ok = await validateStreamUrl(meta.selfhostUrl);
    if (!ok) {
      console.log('[7Movies] Direct URL not reachable, skipping: ' + meta.selfhostUrl);
      return null;
    }
    console.log('[7Movies] Probing direct stream resolution...');
    const probe = await probeStreamResolution(meta.selfhostUrl);
    if (probe) {
      console.log('[7Movies] Direct stream: ' + probe.width + 'x' + probe.height +
        ' (' + probe.codec + ') -> ' + probe.quality);
    } else {
      console.log('[7Movies] Resolution probe failed — defaulting to 1080p');
    }
    return probe;
  })();

  // --- Backup: query /api/source for each provider in parallel
  // Skip selfhost if we already have it (it returns the same URL)
  const providersToQuery = ALL_PROVIDERS.filter(function (p) {
    if (p === 'selfhost' && meta.selfhostUrl) return false;
    return true;
  });
  const typePath = buildTypePath(tmdbId, type, season, episode);

  console.log('[7Movies] Querying backup providers in parallel with direct probe: ' + providersToQuery.join(', '));
  const [directProbe, providerResults] = await Promise.all([
    directProbePromise,
    Promise.all(providersToQuery.map(function (p) {
      return fetchProviderStreams(typePath, meta.playbackToken, p);
    })),
  ]);

  // Add the direct stream first (it's the highest quality — uncapped)
  if (meta.selfhostUrl && directProbe !== null) {
    const probe = directProbe || { quality: '1080p', width: 0, height: 0, codec: 'h264' };
    const is4K = probe.quality === '2160p';
    allStreams.push(buildStream({
      title: info.title + ' [7Movies ' + (is4K ? '4K' : probe.quality.toUpperCase()) + ' Direct' +
        (probe.width ? ' ' + probe.width + 'x' + probe.height : '') + ']',
      url: meta.selfhostUrl,
      quality: probe.quality,
      serverLabel: (is4K ? '4K' : 'Direct') + ' Stream',
      bingeGroup: '7movies-direct-' + tmdbId,
      subtitles: stremioSubs,
    }));
    console.log('[7Movies] + Direct stream (' + probe.quality + '): ' + meta.selfhostUrl);
  } else if (meta.selfhostUrl && directProbe === null) {
    // Validation failed — skip
  }

  for (const r of providerResults) {
    if (r.error) {
      console.log('[7Movies]   ' + r.provider + ': ' + r.error);
      continue;
    }
    let added = 0;
    for (const s of r.streams) {
      if (!s.url && !s.proxyUrl) continue;
      // vaplayer returns url='' and proxyUrl='/api/proxy/hls?url=...'. Build full URL.
      let streamUrl = s.url;
      let needsReferer = false;
      if (!streamUrl && s.proxyUrl) {
        streamUrl = EMBED_ORIGIN + s.proxyUrl;
        needsReferer = true;
      }
      if (!streamUrl || !streamUrl.startsWith('http')) continue;

      // Quality label
      let qLabel = r.quality || (s.provider ? s.provider + ' ' : '') + 'HD';
      if (r.maxRes) qLabel = r.maxRes + 'p';

      allStreams.push(buildStream({
        title: info.title + ' [7Movies ' + (s.provider || r.provider) + ']',
        url: streamUrl,
        quality: String(qLabel),
        serverLabel: s.provider || r.provider,
        bingeGroup: '7movies-' + (s.provider || r.provider).toLowerCase() + '-' + tmdbId,
        needsReferer: needsReferer,
        subtitles: stremioSubs,
      }));
      added++;
    }
    console.log('[7Movies]   ' + r.provider + ': ' + added + ' stream(s) added' +
      (r.maxRes ? ' (max ' + r.maxRes + 'p)' : ''));
  }

  // If no streams found at all, add an iframe fallback pointing to the embed page
  if (allStreams.length === 0) {
    allStreams.push({
      name: PROVIDER_NAME + ' - Player',
      title: info.title + ' [7Movies Player]',
      description: 'Open the 7Movies player in your browser',
      url: embedData.embedUrl,
      quality: '1080p',
      type: 'iframe',
      behaviorHints: {
        bingeGroup: '7movies-iframe-' + tmdbId,
        notWebVideo: true,
      },
    });
    console.log('[7Movies] Added iframe fallback to embed page');
  }

  console.log('[7Movies] ' + allStreams.length + ' streams total');
  return allStreams;
}

// ---------------------------------------------------------------------------
// Exports
// ---------------------------------------------------------------------------
module.exports = {
  getStreams: getStreams,
  getTMDBInfo: getTMDBInfo,
  fetchEmbedMeta: fetchEmbedMeta,
  fetchProviderStreams: fetchProviderStreams,
  validateStreamUrl: validateStreamUrl,
  probeStreamResolution: probeStreamResolution,
  mapResolutionToQuality: mapResolutionToQuality,
  listProviders: function () { return ALL_PROVIDERS.slice(); },
};

// ---------------------------------------------------------------------------
// CLI entry point: node 7movies_all_in_one.js <tmdbId> <movie|tv> [season] [episode]
// ---------------------------------------------------------------------------
if (require.main === module) {
  const args = process.argv.slice(2);
  if (args.length < 2) {
    console.log('Usage: node 7movies_all_in_one.js <tmdbId> <movie|tv> [season] [episode]');
    console.log('Examples:');
    console.log('  node 7movies_all_in_one.js 27205 movie        # Inception');
    console.log('  node 7movies_all_in_one.js 1396 tv 1 1        # Breaking Bad S01E01');
    process.exit(1);
  }
  const tmdbId = args[0];
  const type = args[1];
  const season = type === 'tv' ? parseInt(args[2] || '1', 10) : null;
  const episode = type === 'tv' ? parseInt(args[3] || '1', 10) : null;

  getStreams(tmdbId, type, season, episode).then(function (streams) {
    console.log('\n=== Final streams ===');
    streams.forEach(function (s, i) {
      console.log((i + 1) + '. ' + s.name + ' | ' + s.quality + ' | ' + s.type);
      console.log('   ' + s.url);
      if (s.subtitles) console.log('   subtitles: ' + s.subtitles.length);
    });
    console.log('\nTotal: ' + streams.length + ' stream(s)');
  }).catch(function (e) {
    console.error('FATAL: ' + e.stack);
    process.exit(1);
  });
}
