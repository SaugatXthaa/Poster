// streamxtv.sbs — Multi-Provider Stream Extractor (Direct Playable HLS up to 4K)
// ============================================================================
// Resolves DIRECT PLAYABLE HLS streams from streamxtv.sbs via its backend API
// at api.framextv.tech. Supports 20 providers with up to 4K (2160p) quality.
//
// CHAIN
//   TMDB ID → api.framextv.tech/api/stream?type=<type>&id=<tmdbId>[&season=S&episode=E][&provider=<p>]
//          → JSON { success, provider, sources: [{url, quality, type, server, headers}], subtitles }
//
// The API returns direct HLS (.m3u8) URLs on peakstorm.top CDN with
// Referer: https://player.videasy.to/ header. No decryption needed.
//
// PROVIDERS (20 total — each tries different upstream backends)
//   barbarian, super_barbarian, miner, lavahound, electro_wizard, ice_golem,
//   bowler, headhunter, pekka, goblin, super_pekka, valkyrie, dragon, witch,
//   giant, golem, super_dragon, pekka_x, yeti, wizard
//
// USAGE
//   node streamxtv_all_in_one.js <tmdbId> <movie|tv> [season] [episode]
//   node streamxtv_all_in_one.js 299534 movie          # Avengers: Endgame (4K)
//   node streamxtv_all_in_one.js 1396 tv 1 1           # Breaking Bad S01E01 (4K)

'use strict';

const https = require('https');

const PROVIDER_NAME = 'StreamXTV';
const API_BASE = 'https://api.framextv.tech';
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';

// All 20 providers — ordered by typical quality (4K-capable first)
const ALL_PROVIDERS = [
  'barbarian',       // Videasy (Yoru) — 4K + 1080p + 720p
  'goblin',          // Videasy (Yoru) — 4K + 1080p + 720p
  'super_barbarian', // VidCore (CinePlay CDN) — 4K + 1080p + 720p
  'electro_wizard',  // VidCore (CinePlay CDN) — 4K + 1080p + 720p
  'lavahound',       // VidCore (CinePlay CDN) — 4K + 1080p + 720p
  'headhunter',      // VidCore (CinePlay CDN) — 4K + 1080p + 720p
  'pekka',           // VidCore (CinePlay CDN) — 4K + 1080p + 720p
  'super_pekka',     // VidCore (CinePlay CDN) — 4K + 1080p + 720p
  'valkyrie',        // VidCore (CinePlay CDN) — 4K + 1080p + 720p
  'dragon',          // VidCore (CinePlay CDN) — 4K + 1080p + 720p
  'witch',           // VidCore (CinePlay CDN) — 4K + 1080p + 720p
  'giant',           // VidCore (CinePlay CDN) — 4K + 1080p + 720p
  'golem',           // VidCore (CinePlay CDN) — 4K + 1080p + 720p
  'super_dragon',    // VidCore (CinePlay CDN) — 4K + 1080p + 720p
  'yeti',            // VidCore (CinePlay CDN) — 4K + 1080p + 720p
  'wizard',          // VidCore (CinePlay CDN) — 4K + 1080p + 720p
  'miner',           // Movy (Miami + Seattle) — 1080p + 720p + Auto HLS
  'bowler',          // RiveStream (Pulse + Apex + Citadel) — multi-lang
  'ice_golem',       // Vuflix (YesMovies) — 1080p
  'pekka_x',         // LookMovie — 480p
];

const sleep = (ms) => new Promise(r => setTimeout(r, ms));

function fetchBuf(url, { headers = {}, timeout = 20000 } = {}) {
  return new Promise((resolve, reject) => {
    const u = new URL(url);
    const req = https.request({
      hostname: u.hostname, path: u.pathname + u.search, method: 'GET',
      headers: { 'User-Agent': UA, 'Accept': '*/*', ...headers }, timeout,
    }, (res) => {
      const chunks = [];
      res.on('data', c => chunks.push(c));
      res.on('end', () => resolve({ status: res.statusCode, headers: res.headers, body: Buffer.concat(chunks) }));
    });
    req.on('error', reject);
    req.on('timeout', () => req.destroy(new Error('timeout')));
    req.end();
  });
}

// Fetch JSON with retry on failure (API is rate-limited).
async function fetchJson(url, opts = {}, retries = 2) {
  let lastErr;
  for (let attempt = 0; attempt <= retries; attempt++) {
    try {
      const r = await fetchBuf(url, { ...opts, headers: { Accept: 'application/json', ...(opts.headers || {}) } });
      if (r.status !== 200) throw new Error(`HTTP ${r.status}`);
      return JSON.parse(r.body.toString('utf8'));
    } catch (e) {
      lastErr = e;
      if (attempt < retries) await sleep(2000 * (attempt + 1));
    }
  }
  throw lastErr;
}

// Normalize quality labels from the API into consistent values.
// API returns: "2160p", "1080p", "720p", "480p", "360p", "Auto HLS",
// "HLS", "Auto", "dcloud", "ipcloud", "tcloud", "480" (no 'p'), etc.
function normalizeQuality(rawQuality) {
  if (!rawQuality) return 'HLS';
  const q = String(rawQuality).trim();
  const ql = q.toLowerCase();

  // Standard resolution labels
  if (ql === '2160p' || ql === '4k') return '4K';
  if (ql === '1440p') return '1440p';
  if (ql === '1080p') return '1080p';
  if (ql === '720p') return '720p';
  if (ql === '480p' || ql === '480') return '480p';
  if (ql === '360p' || ql === '360') return '360p';

  // Multi-language qualities like "720p | English" → extract resolution
  const resMatch = q.match(/(\d{3,4})p/i);
  if (resMatch) {
    const h = parseInt(resMatch[1]);
    if (h >= 2160) return '4K';
    if (h >= 1080) return '1080p';
    if (h >= 720) return '720p';
    if (h >= 480) return '480p';
    if (h >= 360) return '360p';
  }

  // Non-standard labels from RiveStream/MeowTV providers — these are
  // typically 1080p or auto-quality HLS streams. Map them to "Auto" so
  // they sort after known resolutions but before unknown ones.
  // "dcloud" = PrimeVids dcloud stream (usually 1080p)
  // "ipcloud" = PrimeVids ipcloud stream (usually 1080p)
  // "tcloud" = PrimeVids tcloud stream (usually 1080p)
  // "auto" = MeowTV auto-quality (variable)
  // "Auto HLS" = Movy auto-quality
  // "HLS" = generic HLS (quality unknown)
  if (['dcloud', 'ipcloud', 'tcloud', 'auto', 'auto hls', 'hls'].includes(ql)) {
    return 'Auto';
  }

  return 'Auto';
}

// Sort order for normalized qualities
const Q_ORDER = { '4K': 0, '2160p': 0, '1440p': 1, '1080p': 2, '720p': 3, '480p': 4, '360p': 5, 'Auto': 6, 'HLS': 6 };

function qualitySortKey(quality) {
  return Q_ORDER[quality] ?? 99;
}

// Fetch streams from the StreamXTV API. Queries each provider to get ALL streams.
async function getStreams(tmdbId, type, season, episode) {
  tmdbId = String(tmdbId);
  const isTV = type === 'tv' || type === 'series';
  console.log(`[StreamXTV] Request: tmdb=${tmdbId} type=${type}` + (isTV ? ` S${season || '?'}E${episode || '?'}` : ''));

  // Build the base query params
  const params = new URLSearchParams({ type: isTV ? 'tv' : 'movie', id: tmdbId });
  if (isTV && season && episode) {
    params.set('season', String(season));
    params.set('episode', String(episode));
  }

  const allStreams = [];
  const seenUrls = new Set();

  // Query providers in batches of 5 with 500ms delay between batches
  // to avoid hitting the API rate limit.
  const batchSize = 5;
  for (let i = 0; i < ALL_PROVIDERS.length; i += batchSize) {
    const batch = ALL_PROVIDERS.slice(i, i + batchSize);
    if (i > 0) await sleep(500);

    const results = await Promise.allSettled(
      batch.map(provider =>
        fetchJson(`${API_BASE}/api/stream?${params}&provider=${provider}`, {
          headers: { Referer: 'https://embed.streamxtv.tech/' },
          timeout: 15000,
        }, 1).then(j => ({ provider, json: j }))
      )
    );

    for (const result of results) {
      if (result.status !== 'fulfilled') {
        console.log(`[StreamXTV]   ${batch[results.indexOf(result)]}: ${result.reason?.message?.slice(0, 60) || 'error'}`);
        continue;
      }
      const { provider, json } = result.value;

      // Validate API response
      if (!json || json.success === false) {
        console.log(`[StreamXTV]   ${provider}: API returned failure`);
        continue;
      }
      const sources = json.sources || [];
      if (sources.length === 0) {
        console.log(`[StreamXTV]   ${provider}: no sources`);
        continue;
      }
      console.log(`[StreamXTV]   ${provider}: ${sources.length} source(s)`);

      for (const src of sources) {
        // Skip sources without URLs
        if (!src.url || typeof src.url !== 'string') continue;
        // Deduplicate by URL
        if (seenUrls.has(src.url)) continue;
        seenUrls.add(src.url);

        const quality = normalizeQuality(src.quality);
        const server = src.server || provider;
        const streamType = src.type === 'dash' || src.url.includes('.mpd')
          ? 'application/dash+xml'
          : 'application/vnd.apple.mpegurl';
        const headers = src.headers || {};

        allStreams.push({
          name: `${PROVIDER_NAME} [${provider}] | ${quality} | ${server}`,
          title: `StreamXTV ${provider} ${quality} ${server}`,
          url: src.url,
          quality,
          type: streamType,
          behaviorHints: {
            bingeGroup: `streamxtv-${provider}-${quality}`,
            notWebReady: false,
            // Pass required headers (Referer) so HLS clients can request the stream
            proxyHeaders: headers.Referer ? {
              request: {
                'User-Agent': UA,
                'Referer': headers.Referer,
              },
            } : undefined,
          },
        });
      }
    }
  }

  // Sort by quality (4K first, then 1080p, etc.), then by provider name
  allStreams.sort((a, b) => {
    const qDiff = qualitySortKey(a.quality) - qualitySortKey(b.quality);
    if (qDiff !== 0) return qDiff;
    return a.name.localeCompare(b.name);
  });

  console.log(`[StreamXTV] ${allStreams.length} stream(s) total`);
  return allStreams;
}

// Quick check: which providers have sources for a given title (without fetching full streams)
async function checkProviders(tmdbId, type, season, episode) {
  tmdbId = String(tmdbId);
  const isTV = type === 'tv' || type === 'series';
  const params = new URLSearchParams({ id: tmdbId, type: isTV ? 'tv' : 'movie' });
  if (isTV && season && episode) {
    params.set('season', String(season));
    params.set('episode', String(episode));
  }

  const results = await Promise.allSettled(
    ALL_PROVIDERS.map(provider =>
      fetchJson(`${API_BASE}/api/stream/check?${params}&provider=${provider}`, {
        headers: { Referer: 'https://embed.streamxtv.tech/' },
      }, 1).then(j => ({ provider, ...j }))
    )
  );
  return results
    .filter(r => r.status === 'fulfilled' && r.value.available)
    .map(r => r.value);
}

module.exports = {
  getStreams, checkProviders, normalizeQuality,
  ALL_PROVIDERS, API_BASE, PROVIDER_NAME,
};

if (require.main === module) {
  const args = process.argv.slice(2);
  if (args.length < 2) {
    console.log('StreamXTV.sbs Direct Stream Extractor');
    console.log('  Movies + TV — direct playable HLS (up to 4K) via 20 providers');
    console.log('');
    console.log('Usage: node streamxtv_all_in_one.js <tmdbId> <movie|tv> [season] [episode]');
    console.log('Examples:');
    console.log('  node streamxtv_all_in_one.js 299534 movie        # Avengers: Endgame (4K)');
    console.log('  node streamxtv_all_in_one.js 1396 tv 1 1       # Breaking Bad S01E01 (4K)');
    process.exit(1);
  }
  getStreams(args[0], args[1], args[2], args[3])
    .then(s => {
      console.log('\n=== Final playable streams ===');
      if (s.length === 0) { console.log('No streams found.'); return; }
      s.forEach((x, i) => {
        console.log(`${i+1}. ${x.name}`);
        console.log(`   URL: ${x.url.slice(0, 180)}${x.url.length > 180 ? '...' : ''}`);
      });
      const byQuality = {};
      for (const x of s) byQuality[x.quality] = (byQuality[x.quality] || 0) + 1;
      console.log(`\nTotal: ${s.length} streams`);
      console.log('By quality:', Object.entries(byQuality).map(([q, c]) => `${q}=${c}`).join(', '));
    })
    .catch(e => { console.error('FATAL: ' + e.stack); process.exit(1); });
}
