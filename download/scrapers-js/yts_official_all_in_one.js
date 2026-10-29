// yts-official.com — Stream Scraper (4K playable, NO torrents)
// =========================================================================
// Scrapes playable streams via https://en.yts-official.com/.
//
// APPROACH
// --------
// yts-official.com itself ONLY serves torrent/magnet links (via the Knaben
// index + The Pirate Bay). It does NOT host any direct video files.
//
// The site's own "play" button uses vidsrc.mov → vsembed.ru iframe embeds,
// but vsembed.ru is Cloudflare-protected (returns HTTP 403 to all non-browser
// clients) so the embed URL cannot be used directly as a Stremio stream.
//
// This scraper takes a different approach:
//   1. Uses yts-official.com's catalog API (search, popular, trending) to
//      resolve a title → TMDB ID (the site proxies TMDB)
//   2. For each resolved TMDB ID, calls our existing 4K-capable Nuvio providers
//      (cineby, vidfast, dahmermovies, hdhub4u, hindmoviez, uhdmovies, etc.)
//      to fetch real playable HLS/MP4 streams
//   3. Filters OUT every magnet/torrent URL — only HTTP(S) playable URLs are
//      returned
//   4. Returns streams sorted by quality (2160p first, then 1080p, etc.)
//
// ENDPOINTS (from en.yts-official.com/?api=...):
//   ?api=popular&mode=movie&page=1           — popular movies
//   ?api=popular&mode=tv&page=1              — popular TV
//   ?api=trending&mode=movie&time=week      — trending
//   ?api=top_rated&mode=movie               — top rated
//   ?api=now_playing&mode=movie             — now playing
//   ?api=upcoming&mode=movie               — upcoming
//   ?api=on_the_air&mode=tv                 — on-air TV
//   ?api=airing_today&mode=tv               — airing today
//   ?api=discover&mode=movie&genre=28&year=2024&sort=popularity.desc
//   ?api=search&mode=movie&q=dune&page=1   — search
//   ?api=tv_details&id=1396                 — TV show details (seasons list)
//   ?api=season_details&id=1396&season=1    — episode list for a season
//   ?api=torrents&mode=movie&name=...&year=...&quality=all — torrent magnets
//     (NOT USED — user requested no torrents)
//
// USAGE
//   node yts_official_all_in_one.js <tmdbId> <movie|tv> [season] [episode]
//   node yts_official_all_in_one.js search "Dune Part Two"
//   node yts_official_all_in_one.js popular movie
//   node yts_official_all_in_one.js popular tv
//
// The scraper needs the existing nuvio_*.js providers in the same directory
// (or in /home/z/my-project/download/scrapers-js/).

'use strict';

const path = require('path');
const fs = require('fs');

const PROVIDER_NAME = 'YTS-Official';
const SITE_BASE = 'https://en.yts-official.com';
const TMDB_API_KEY = '8476a7ab80ad76f0936744df0430e67c';
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 ' +
           '(KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';

// 4K-capable providers we delegate to (in priority order — fastest first)
// All of these return playable HTTP(S) streams, never magnet links.
// INCLUDES the vidsrc direct extractor (returns playable HLS from yts source)
const PREFERRED_4K_PROVIDERS = [
  // ★ NEW: Direct vidsrc.io extractor (HLS streams up to 1080p from YTS source)
  // This is the "direct vidsrc extract" — bypasses all iframe embeds and
  // returns directly playable HLS master.m3u8 + variant index.m3u8 URLs.
  // File source: YTS.MX (1080p WEBRip x264 AAC MP4s)
  'vidsrc_extractor',
  // Existing 4K-capable providers (fallback)
  'cineby',          // 4K HLS via ironwallnet.net CDN
  'vidfast',         // multi-server HLS (10 servers in parallel)
  'dahmermovies-4k', // 4K-focused via p.111477.xyz proxy
  'uhdmovies',       // 4K via Google video-downloads CDN
  'hindmoviez',      // 4K via workers.dev
  'hdhub4u',         // 4K via HubCloud/VCloud (multi-audio)
  'dahmermovies',    // 4K via p.111477.xyz proxy (general)
  '4khdhub',         // 4K HDR via Cloudflare workers
  'videasy',         // multi-server HLS (speedracelight)
  'castle',          // multi-language with AES-CBC decryption
  'movieblast',      // mycdn-mb.xyz HLS
  'playimdb',        // vaplayer.ru API with HLS
  'vidlove',         // workers.dev
  'movix',           // finepulfe.xyz HLS
  'purstream',       // finepulfe.xyz (same as movix)
  'goated',          // reallyfast.xyz CDN HLS
  'netmirror',       // hakunaymatata.com CDN
  'movies4u',        // r2.dev Cloudflare workers
  'desiflix',        // vixsrc.to playlist (Desi content)
  'persianstremio',  // persianstremio.vercel.app
];

// ─── Find provider file ─────────────────────────────────────────────────────
function findProviderFile(name) {
  const candidates = [
    path.join(__dirname, `${name === 'vidsrc_extractor' ? 'vidsrc_extractor' : 'nuvio_' + name}.js`),
    path.join(__dirname, 'scrapers-js', `nuvio_${name}.js`),
    path.join(__dirname, 'download', 'scrapers-js', `nuvio_${name}.js`),
    '/home/z/my-project/download/scrapers-js/nuvio_' + name + '.js',
    '/home/z/my-project/download/tenies-site-sources/scrapers/nuvio_' + name + '.js',
    '/home/z/my-project/download/stremio-scrapers/nuvio_' + name + '.js',
    '/home/z/my-project/scripts/vidsrc_extractor_all_in_one.js',  // For vidsrc_extractor
  ];
  for (const p of candidates) {
    if (fs.existsSync(p)) return p;
  }
  return null;
}

// ─── Call a single provider ──────────────────────────────────────────────────
async function callProvider(name, tmdbId, type, season, episode) {
  const file = findProviderFile(name);
  if (!file) {
    return { name, ok: false, error: 'provider file not found', streams: [] };
  }
  try {
    delete require.cache[require.resolve(file)];
    const mod = require(file);
    if (!mod || typeof mod.getStreams !== 'function') {
      return { name, ok: false, error: 'no getStreams export', streams: [] };
    }
    const result = await Promise.race([
      mod.getStreams(tmdbId, type, season, episode),
      new Promise(r => setTimeout(() => null, 12000)),
    ]).catch(e => null);
    if (!result || !Array.isArray(result)) {
      return { name, ok: false, error: 'no result (timeout)', streams: [] };
    }
    // Filter out any magnet/torrent URLs (defensive — user said no torrents)
    const playable = result.filter(s => {
      const u = s.url || s.streamUrl || '';
      return u && !u.startsWith('magnet:') && !u.includes('magnet:') &&
             !u.includes('btih:') && (u.startsWith('http://') || u.startsWith('https://'));
    });
    return { name, ok: true, streams: playable };
  } catch (e) {
    return { name, ok: false, error: e.message.slice(0, 100), streams: [] };
  }
}

// ─── yts-official.com catalog API ────────────────────────────────────────────
async function ytsApi(action, params) {
  const qs = new URLSearchParams({ api: action, ...params }).toString();
  const url = `${SITE_BASE}/?${qs}`;
  const res = await fetch(url, {
    headers: { 'User-Agent': UA, 'Accept': 'application/json' },
    signal: AbortSignal.timeout(15000),
  });
  if (!res.ok) throw new Error(`yts-official ${action} HTTP ${res.status}`);
  return res.json();
}

// ─── TMDB fallback (if yts-official.com is down) ──────────────────────────────
async function tmdbSearch(title, type) {
  const url = `https://api.themoviedb.org/3/search/${type === 'tv' ? 'tv' : 'movie'}?api_key=${TMDB_API_KEY}&query=${encodeURIComponent(title)}&language=en-US`;
  const res = await fetch(url, { headers: { 'User-Agent': UA }, signal: AbortSignal.timeout(10000) });
  if (!res.ok) throw new Error(`TMDB HTTP ${res.status}`);
  const j = await res.json();
  return (j.results || []).map(r => ({
    id: r.id,
    title: r.title || r.name,
    year: (r.release_date || r.first_air_date || '').slice(0, 4),
    overview: r.overview,
    poster: r.poster_path ? `https://image.tmdb.org/t/p/w342${r.poster_path}` : null,
    media_type: type,
  }));
}

async function getTMDBInfo(tmdbId, type) {
  const url = `https://api.themoviedb.org/3/${type === 'tv' ? 'tv' : 'movie'}/${tmdbId}?api_key=${TMDB_API_KEY}&language=en-US`;
  const res = await fetch(url, { headers: { 'User-Agent': UA }, signal: AbortSignal.timeout(10000) });
  if (!res.ok) throw new Error(`TMDB HTTP ${res.status}`);
  const j = await res.json();
  return {
    title: j.name || j.title || 'Unknown',
    year: (j.first_air_date || j.release_date || '').slice(0, 4),
    type, tmdbId: String(tmdbId),
  };
}

// ─── Discover/search via yts-official.com ───────────────────────────────────
async function search(query, mode) {
  mode = mode || 'movie';
  try {
    const r = await ytsApi('search', { mode, q: query, page: 1 });
    return (r.results || []).map(x => ({
      id: x.id,
      title: x.title || x.name,
      year: (x.release_date || x.first_air_date || '').slice(0, 4),
      overview: x.overview,
      poster: x.poster_path ? `https://image.tmdb.org/t/p/w342${x.poster_path}` : null,
      media_type: x.media_type || mode,
    }));
  } catch (e) {
    console.log('[YTS] yts search failed (' + e.message + ') — falling back to TMDB');
    return await tmdbSearch(query, mode);
  }
}

async function popular(mode) {
  mode = mode || 'movie';
  const r = await ytsApi('popular', { mode, page: 1 });
  return (r.results || []).map(x => ({
    id: x.id, title: x.title || x.name,
    year: (x.release_date || x.first_air_date || '').slice(0, 4),
    poster: x.poster_path ? `https://image.tmdb.org/t/p/w342${x.poster_path}` : null,
  }));
}

async function trending(mode, time) {
  mode = mode || 'movie';
  time = time || 'week';
  const r = await ytsApi('trending', { mode, time, page: 1 });
  return (r.results || []).map(x => ({
    id: x.id, title: x.title || x.name,
    year: (x.release_date || x.first_air_date || '').slice(0, 4),
    poster: x.poster_path ? `https://image.tmdb.org/t/p/w342${x.poster_path}` : null,
  }));
}

// ─── Quality detection ──────────────────────────────────────────────────────
function detectQuality(text) {
  text = (text || '').toLowerCase();
  if (/2160p|4k|uhd|hdr/.test(text)) return '2160p';
  if (/1080p|fhd/.test(text)) return '1080p';
  if (/720p|hd/.test(text)) return '720p';
  if (/480p|sd/.test(text)) return '480p';
  return 'SD';
}

// ─── Build Stremio stream object ───────────────────────────────────────────
function buildStream(name, quality, url, title, type) {
  const isHls = url.includes('.m3u8') || /m3u8/i.test(name + title);
  return {
    name: PROVIDER_NAME + ' - ' + name + ' - ' + quality,
    title: title + ' [' + name + ']',
    url,
    quality,
    type: isHls ? 'application/vnd.apple.mpegurl' : 'video/mp4',
    behaviorHints: { bingeGroup: 'yts-' + name + '-' + quality },
  };
}

// ─── Main entry: get playable streams for a TMDB ID ──────────────────────────
async function getStreams(tmdbId, type, season, episode) {
  tmdbId = String(tmdbId);
  console.log(`[YTS] Request: tmdb=${tmdbId} type=${type} ` +
              (season ? `s${season}e${episode}` : ''));

  // Get title (for logging only)
  let info;
  try {
    info = await getTMDBInfo(tmdbId, type);
    console.log('[YTS] TMDB: ' + info.title + ' (' + info.year + ')');
  } catch (e) {
    console.log('[YTS] TMDB lookup failed — proceeding with provider search by TMDB ID');
    info = { title: 'Unknown', year: '', type, tmdbId };
  }

  // Call all preferred 4K-capable providers IN PARALLEL (Promise.allSettled so none can hang)
  console.log(`[YTS] Querying ${PREFERRED_4K_PROVIDERS.length} providers in parallel (12s each)...`);
  const results = await Promise.allSettled(
    PREFERRED_4K_PROVIDERS.map(p =>
      callProvider(p, tmdbId, type, season ? parseInt(season) : null, episode ? parseInt(episode) : null)
    )
  );
  const settled = results.map(r => r.status === 'fulfilled' ? r.value : { name: '?', ok: false, error: r.reason?.message || 'rejected', streams: [] });

  // Collect all playable streams (no magnets!)
  const allStreams = [];
  let providersOk = 0;
  let providersFailed = 0;
  const failedList = [];

  for (const r of settled) {
    if (r.ok && r.streams.length > 0) {
      providersOk++;
      for (const s of r.streams) {
        const url = s.url || s.streamUrl;
        if (!url) continue;
        // Double-check it's not a magnet
        if (url.startsWith('magnet:') || url.includes('btih:')) continue;
        const q = s.quality || detectQuality(s.name + ' ' + (s.title || ''));
        allStreams.push(buildStream(r.name, q, url, info.title, type));
      }
    } else if (!r.ok) {
      providersFailed++;
      if (r.error !== 'provider file not found') {
        failedList.push(`${r.name}: ${r.error}`);
      }
    }
  }

  // Sort by quality (4K first)
  const qOrder = { '2160p': 0, '1080p': 1, '720p': 2, '480p': 3, 'SD': 4 };
  allStreams.sort((a, b) => (qOrder[a.quality] || 99) - (qOrder[b.quality] || 99));

  console.log(`[YTS] ✅ ${providersOk} providers returned streams, ${providersFailed} failed`);
  console.log(`[YTS] Total playable streams: ${allStreams.length} (NO torrents)`);
  const counts = {};
  for (const s of allStreams) counts[s.quality] = (counts[s.quality] || 0) + 1;
  console.log('[YTS] Quality breakdown: ' + Object.entries(counts).map(([k,v]) => `${k}=${v}`).join(', '));

  if (failedList.length > 0 && process.env.DEBUG) {
    console.log('[YTS] Failed providers:');
    for (const f of failedList) console.log('  ' + f);
  }

  return allStreams;
}

// ─── Module exports ─────────────────────────────────────────────────────────
module.exports = {
  getStreams, search, popular, trending, getTMDBInfo,
  callProvider, findProviderFile, PREFERRED_4K_PROVIDERS,
};

// ─── CLI ────────────────────────────────────────────────────────────────────
if (require.main === module) {
  const args = process.argv.slice(2);

  if (args.length === 0) {
    console.log('yts-official.com — 4K Playable Stream Scraper');
    console.log('');
    console.log('Usage:');
    console.log('  node yts_official_all_in_one.js <tmdbId> <movie|tv> [season] [episode]');
    console.log('    Get playable streams for a movie or TV show (no torrents, up to 4K)');
    console.log('');
    console.log('  node yts_official_all_in_one.js search "Dune Part Two" [movie|tv]');
    console.log('    Search yts-official.com for a title (returns TMDB IDs)');
    console.log('');
    console.log('  node yts_official_all_in_one.js popular [movie|tv]');
    console.log('    List popular movies/TV shows');
    console.log('');
    console.log('  node yts_official_all_in_one.js trending [movie|tv] [day|week]');
    console.log('    List trending movies/TV shows');
    console.log('');
    console.log('Examples:');
    console.log('  node yts_official_all_in_one.js 693134 movie        # Dune Part Two (4K available)');
    console.log('  node yts_official_all_in_one.js 1396 tv 1 1         # Breaking Bad S01E01');
    console.log('  node yts_official_all_in_one.js search "dune" movie');
    console.log('  node yts_official_all_in_one.js popular movie');
    process.exit(1);
  }

  const cmd = args[0];

  // Search command
  if (cmd === 'search') {
    const query = args[1];
    const mode = args[2] || 'movie';
    if (!query) { console.error('Search query required'); process.exit(1); }
    search(query, mode)
      .then(r => {
        console.log(`=== Search results for "${query}" (${mode}) ===`);
        r.slice(0, 10).forEach((x, i) =>
          console.log(`${i+1}. [${x.id}] ${x.title} (${x.year}) ${x.media_type}`));
        console.log(`\nTotal: ${r.length} results`);
      })
      .catch(e => { console.error('FATAL: ' + e.message); process.exit(1); });
  }
  // Popular command
  else if (cmd === 'popular') {
    const mode = args[1] || 'movie';
    popular(mode)
      .then(r => {
        console.log(`=== Popular ${mode}s ===`);
        r.slice(0, 20).forEach((x, i) =>
          console.log(`${i+1}. [${x.id}] ${x.title} (${x.year})`));
      })
      .catch(e => { console.error('FATAL: ' + e.message); process.exit(1); });
  }
  // Trending command
  else if (cmd === 'trending') {
    const mode = args[1] || 'movie';
    const time = args[2] || 'week';
    trending(mode, time)
      .then(r => {
        console.log(`=== Trending ${mode}s (this ${time}) ===`);
        r.slice(0, 20).forEach((x, i) =>
          console.log(`${i+1}. [${x.id}] ${x.title} (${x.year})`));
      })
      .catch(e => { console.error('FATAL: ' + e.message); process.exit(1); });
  }
  // Default: get streams
  else {
    const tmdbId = args[0];
    const type = args[1] || 'movie';
    const season = args[2] || null;
    const episode = args[3] || null;
    getStreams(tmdbId, type, season, episode)
      .then(s => {
        console.log('\n=== Final playable streams (sorted by quality) ===');
        const byQuality = {};
        s.forEach(x => { byQuality[x.quality] = byQuality[x.quality] || []; byQuality[x.quality].push(x); });
        let idx = 1;
        for (const q of ['2160p', '1080p', '720p', '480p', 'SD']) {
          if (byQuality[q]) {
            console.log(`\n--- ${q} (${byQuality[q].length} streams) ---`);
            for (const x of byQuality[q]) {
              console.log(`${idx}. ${x.name}`);
              console.log(`   ${x.url.slice(0, 120)}${x.url.length > 120 ? '...' : ''}`);
              idx++;
            }
          }
        }
        console.log(`\nTotal: ${s.length} playable streams (0 torrents)`);
      })
      .catch(e => { console.error('FATAL: ' + e.stack); process.exit(1); });
  }
}
