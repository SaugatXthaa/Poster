/**
 * TENIES.SITE Addon — Stream Aggregator
 * =====================================
 * Aggregates streams from all working scrapers.
 * Each scraper returns Stremio-compatible stream objects:
 *   { name, title, url, quality, type, behaviorHints }
 *
 * USAGE:
 *   const { getStreams } = require('./addon');
 *   const streams = await getStreams(tmdbId, type, season, episode);
 *
 * Then expose as a Stremio addon:
 *   GET /:configKey/stream/:type/:id.json
 *
 * DEPLOY:
 *   Render / Railway / Vercel / Cloudflare Workers
 */
'use strict';

const path = require('path');
const SCRAPERS_DIR = path.join(__dirname, 'scrapers');

// ─── CORE SCRAPERS (matching deployed TENIES.SITE addon) ───────────────────
// These produce the URL patterns observed in the live addon:
//   - hubcloud.cx / hubdrive.tips  → nuvio_hdhub4u
//   - *.r2.cloudflarestorage.com   → nuvio_movies4u, nuvio_4khdhub
//   - pixeldrain.dev               → 1shows
//   - video-downloads.googleusercontent.com → nuvio_uhdmovies, nuvio_hdhub4u
//   - hdhub.thevolecitor.qzz.io    → cinejoy
//   - cdn.bunker/pongala/lenin     → nuvio_hdhub4u (direct CDN MP4s)
//   - *.workers.dev                → nuvio_movieshunt, nuvio_4khdhub
const CORE_SCRAPERS = [
  'nuvio_hdhub4u',       // Main source — HubCloud + Google + direct CDNs
  'nuvio_movies4u',      // Cloudflare R2 + Google + HLS
  'nuvio_uhdmovies',     // Google CDN (UHDMovies style)
  'nuvio_movieshunt',    // CF workers via abhilinks.site
  'nuvio_4khdhub',       // 4K HDR via CF workers
  'cinejoy',              // 4K/1080p/720p/480p HLS via addon resolver
                          //   (enhanced: returns 11 streams per title — 4 master
                          //    playlists + 4 video-only + 2 audio-only + 1 website)
  '1shows',              // Pixeldrain direct files
  'zstream',              // zstream.mov iframe (Turnstile solved in browser)
                          //   (returns 4 streams: page + watch + embed + IMDB)
];

// ─── EXTENDED SCRAPERS (bonus coverage) ────────────────────────────────────
const EXTENDED_SCRAPERS = [
  'nuvio_dahmermovies',     // 4K via p.111477.xyz proxy
  'nuvio_dahmermovies-4k',  // 4K-focused variant
  'nuvio_hindmoviez',       // 4K via workers.dev
  'nuvio_castle',           // Multi-language (Hindi/Tamil/Telugu)
  'nuvio_persianstremio',   // Persian + English content
  'nuvio_videasy',          // Multi-server HLS (10 servers)
  'nuvio_vidfast',          // ironwallnet HLS
  'nuvio_movix',            // finepulfe.xyz HLS
  'nuvio_playimdb',         // vaplayer.ru HLS
  'nuvio_netmirror',        // hakunaymatata CDN
  'nuvio_ctgmovies',        // CTG direct CDN
  'nuvio_goated',           // reallyfast.xyz HLS
  'nuvio_hdghartv',         // streamraiwind token-signed m3u8
  'nuvio_desiflix',         // vixsrc.to playlist
  'nuvio_vixsrc',           // komiknostalgia playlist
  'nuvio_purstream',        // finepulfe.xyz (purstream)
  'nuvio_vidlove',          // workers.dev
  'nuvio_torrentio',        // Magnet/torrent (4K available)
  'nuvio_zinkmovies',       // hanna427def stream
  'nuvio_movieblast',       // mycdn-mb.xyz HLS
  'animezey',               // Anime 4K wrapper
];

const ALL_SCRAPERS = [...CORE_SCRAPERS, ...EXTENDED_SCRAPERS];

// ─── Lazy-loaded module cache ─────────────────────────────────────────────
const _cache = {};
function loadScraper(name) {
  if (_cache[name]) return _cache[name];
  try {
    const mod = require(path.join(SCRAPERS_DIR, name + '.js'));
    if (typeof mod.getStreams !== 'function') {
      console.warn(`[Addon] ${name}.getStreams is not a function, skipping`);
      return null;
    }
    _cache[name] = mod;
    return mod;
  } catch (e) {
    console.warn(`[Addon] Failed to load ${name}: ${e.message}`);
    return null;
  }
}

// ─── Aggregator ────────────────────────────────────────────────────────────
async function getStreams(tmdbId, type, season, episode, opts = {}) {
  const {
    includeExtended = true,  // toggle extended scrapers
    timeoutMs = 30000,        // per-scraper timeout
    concurrency = 8,          // parallel scrapers
  } = opts;

  const scraperList = includeExtended
    ? ALL_SCRAPERS
    : CORE_SCRAPERS;

  console.log(`[Addon] Aggregating streams for tmdb=${tmdbId} type=${type}` +
    (type === 'tv' ? ` S${season}E${episode}` : '') +
    ` (${scraperList.length} scrapers)`);

  // Run scrapers in batches of `concurrency`
  const results = [];
  for (let i = 0; i < scraperList.length; i += concurrency) {
    const batch = scraperList.slice(i, i + concurrency);
    const batchResults = await Promise.all(
      batch.map(async (name) => {
        const mod = loadScraper(name);
        if (!mod) return { name, streams: [], error: 'load_failed' };

        const timeoutPromise = new Promise((resolve) =>
          setTimeout(() => resolve({ name, streams: [], error: 'timeout' }), timeoutMs)
        );

        const scraperPromise = Promise.resolve()
          .then(() => mod.getStreams(tmdbId, type, season, episode))
          .then((streams) => ({
            name,
            streams: Array.isArray(streams) ? streams : [],
            error: null,
          }))
          .catch((e) => ({ name, streams: [], error: e.message }));

        return Promise.race([scraperPromise, timeoutPromise]);
      })
    );
    results.push(...batchResults);
  }

  // Merge all streams
  const allStreams = [];
  const errors = [];
  for (const r of results) {
    if (r.error) {
      errors.push(`${r.name}: ${r.error}`);
    } else if (r.streams.length > 0) {
      // Tag each stream with its source
      for (const s of r.streams) {
        allStreams.push({
          ...s,
          name: s.name || r.name,
          source: r.name,
        });
      }
    }
  }

  console.log(`[Addon] Got ${allStreams.length} streams from ${results.filter(r => !r.error && r.streams.length > 0).length} scrapers`);
  if (errors.length > 0) {
    console.log(`[Addon] Errors: ${errors.length} scrapers failed`);
  }

  return allStreams;
}

// ─── Optional: Stremio Addon HTTP server ───────────────────────────────────
// If you're running this on Render/Railway, you can use express to expose
// Stremio endpoints. Otherwise just export getStreams().
function startServer(port = process.env.PORT || 7000) {
  const express = require('express');
  const cors = require('cors');
  const app = express();
  app.use(cors());
  app.use(express.json());

  const CONFIG_KEY = process.env.CONFIG_KEY || 'abckdhfik-34585674';

  app.get(`/${CONFIG_KEY}/manifest.json`, (req, res) => {
    res.json({
      id: 'community.TENIES.SITE.main',
      version: '2.3.0',
      name: 'TENIES.SITE',
      description: 'Premium Stremio streams powered by TENIES.SITE.',
      logo: 'https://tenies-site-addon.vercel.app/main.png',
      background: 'https://tenies-site-addon.vercel.app/placeholder.jpg',
      resources: ['stream'],
      types: ['movie', 'series'],
      idPrefixes: ['tt', 'tmdb'],
      catalogs: [],
      behaviorHints: { configurable: true, configurationRequired: false },
    });
  });

  app.get(`/${CONFIG_KEY}/stream/:type/:id.json`, async (req, res) => {
    const { type, id } = req.params;
    // Parse id: tmdb:27205  |  tt1234567  |  tmdb:1396:1:1 (TV)
    const parts = id.split(':');
    let tmdbId, season, episode;
    if (parts[0] === 'tmdb') {
      tmdbId = parts[1];
      if (parts.length >= 4) {
        season = parseInt(parts[2], 10);
        episode = parseInt(parts[3], 10);
      }
    } else if (parts[0] === 'tt') {
      // IMDB id — would need to convert via TMDB API first
      tmdbId = parts[1]; // fallback
    } else {
      tmdbId = parts[0];
    }

    try {
      const streams = await getStreams(tmdbId, type === 'series' ? 'tv' : type, season, episode);
      res.json({ streams });
    } catch (e) {
      console.error(`[Addon] Fatal error: ${e.message}`);
      res.json({ streams: [] });
    }
  });

  app.get('/', (req, res) => {
    res.send(`<html><body><h1>TENIES.SITE Addon</h1><p>Configure in Stremio: <code>https://${req.headers.host}/${CONFIG_KEY}/manifest.json</code></p></body></html>`);
  });

  app.listen(port, () => {
    console.log(`[Addon] TENIES.SITE listening on port ${port}`);
    console.log(`[Addon] Manifest: http://localhost:${port}/${CONFIG_KEY}/manifest.json`);
  });
}

// ─── Exports ──────────────────────────────────────────────────────────────
module.exports = {
  getStreams,
  startServer,
  CORE_SCRAPERS,
  EXTENDED_SCRAPERS,
  ALL_SCRAPERS,
};

// Auto-start server if run directly
if (require.main === module) {
  startServer();
}
