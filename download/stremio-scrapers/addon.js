/**
 * Adult Streaming Addon for Stremio/Nuvio
 * ========================================
 * 4 sources, each with its own named catalog:
 *   1. XPrimeHub  — xprimehub.bond  (movies, web series, short films)
 *   2. Damplips   — damplips.com    (adult short films / clips)
 *   3. Qorno      — qorno.com       (aggregator: 23+ source sites)
 *   4. FullPorn   — fullporn.to     (full-length adult videos, up to 4K)
 *
 * Each catalog is independently browsable and searchable.
 * Streams are resolved on-demand when the user clicks a video.
 *
 * RUN:
 *   node addon.js
 *
 * Then in Stremio: Add Addon → http://localhost:7000/manifest.json
 */
'use strict';

const express = require('express');
const cors = require('cors');

const xprime = require('./nuvio_xprimehub');
const damplips = require('./nuvio_damplips');
const qorno = require('./nuvio_qorno');
const fullporn = require('./nuvio_fullporn');

const app = express();
const PORT = process.env.PORT || 7000;

app.use(cors());
app.use(express.json());

// ─── Manifest ────────────────────────────────────────────────────────────────
const manifest = {
  id: 'com.adult.streaming.addon',
  version: '1.0.0',
  name: 'Adult Streaming Hub',
  description: '4 sources: XPrimeHub, Damplips, Qorno, FullPorn. Movies, clips, and full-length videos up to 4K.',
  logo: 'https://i.postimg.cc/8c5XGzsx/showbox.png',
  types: ['movie'],
  resources: ['catalog', 'meta', 'stream'],
  catalogs: [
    // XPrimeHub catalog
    {
      type: 'movie',
      id: 'xprimehub',
      name: 'XPrimeHub',
      extra: [
        { name: 'search', isRequired: false },
        { name: 'genre', isRequired: false, options: ['all', 'movie', 'tv'] },
        { name: 'skip', isRequired: false },
      ],
    },
    {
      type: 'movie',
      id: 'xprimehub_latest',
      name: 'XPrimeHub Latest',
      extra: [{ name: 'skip', isRequired: false }],
    },
    // Damplips catalog
    {
      type: 'movie',
      id: 'damplips',
      name: 'Damplips',
      extra: [
        { name: 'search', isRequired: false },
        { name: 'genre', isRequired: false, options: [
          'amateur-porn', 'anal-sex', 'asian-girls', 'babes', 'big-black-cocks',
          'big-tits', 'college-girls', 'creampie', 'foursome', 'girlfriends',
          'girls-masturbate', 'hardcore', 'hot-girls', 'hugecocks', 'latina-girls',
          'lesbians', 'momdaughter', 'naked-girls', 'naked-women', 'sexy-girls',
          'teen-sex', 'threesome', 'young-girls',
        ]},
        { name: 'skip', isRequired: false },
      ],
    },
    {
      type: 'movie',
      id: 'damplips_latest',
      name: 'Damplips Latest',
      extra: [{ name: 'skip', isRequired: false }],
    },
    // Qorno catalog
    {
      type: 'movie',
      id: 'qorno',
      name: 'Qorno',
      extra: [
        { name: 'search', isRequired: false },
        { name: 'genre', isRequired: false, options: [
          'japanese', 'blonde', 'teen', 'milf', 'anal', 'asian', 'ebony',
          'latina', 'lesbian', 'threesome', 'creampie', 'big-tits', 'cumshot',
          'blowjob', 'handjob', 'masturbation', 'squirt', 'rough', 'amateur',
          'public', 'voyeur', 'bondage', 'femdom', 'spanking',
        ]},
        { name: 'skip', isRequired: false },
      ],
    },
    {
      type: 'movie',
      id: 'qorno_latest',
      name: 'Qorno Latest',
      extra: [{ name: 'skip', isRequired: false }],
    },
    // FullPorn catalog
    {
      type: 'movie',
      id: 'fullporn',
      name: 'FullPorn',
      extra: [
        { name: 'search', isRequired: false },
        { name: 'genre', isRequired: false, options: ['latest-updates', 'most-popular', 'top-rated'] },
        { name: 'skip', isRequired: false },
      ],
    },
    {
      type: 'movie',
      id: 'fullporn_latest',
      name: 'FullPorn Latest',
      extra: [{ name: 'skip', isRequired: false }],
    },
  ],
  idPrefixes: ['xprimehub:', 'damplips:', 'qorno:', 'fullporn:'],
};

// ─── Catalog Handler ─────────────────────────────────────────────────────────
app.get('/catalog/:type/:id.json', async (req, res) => {
  try {
    const { type, id } = req.params;
    const search = req.query.search || '';
    const genre = req.query.genre || '';
    const skip = parseInt(req.query.skip || '0');
    const page = Math.floor(skip / 50) + 1;

    console.log(`[Catalog] ${id} | search="${search}" genre="${genre}" skip=${skip} page=${page}`);

    let result;

    if (id === 'xprimehub' || id === 'xprimehub_latest') {
      if (search) {
        const entries = await xprime.searchCatalog(search, 100);
        result = entriesToMetas(entries, 'xprimehub');
      } else {
        const catResult = await xprime.getCatalogMetas({ page, pageSize: 50 });
        result = catResult.metas.map(m => convertMeta(m, 'xprimehub'));
      }
    } else if (id === 'damplips' || id === 'damplips_latest') {
      if (search) {
        const entries = await damplips.searchCatalog(search, 100);
        result = entriesToMetasDamplips(entries);
      } else if (genre && genre !== 'all') {
        const streams = await damplips.getStreams(null, 'movie', null, null, { category: genre });
        result = streamsToMetas(streams, 'damplips');
      } else {
        const catResult = await damplips.getCatalogMetas({ page, pageSize: 50 });
        result = catResult.metas;
      }
    } else if (id === 'qorno' || id === 'qorno_latest') {
      if (search) {
        const cards = await qorno.searchCatalog(search, 100);
        result = cardsToMetasQorno(cards);
      } else if (genre) {
        const cards = await qorno.fetchByCategory(genre, page);
        result = cardsToMetasQorno(cards);
      } else {
        const catResult = await qorno.getCatalogMetas({ page, pageSize: 50 });
        result = catResult.metas;
      }
    } else if (id === 'fullporn' || id === 'fullporn_latest') {
      if (search) {
        const cards = await fullporn.searchCatalog(search, 100);
        result = cardsToMetasFullporn(cards);
      } else if (genre) {
        const cards = await fullporn.fetchByCategory(genre, page);
        result = cardsToMetasFullporn(cards);
      } else {
        const catResult = await fullporn.getCatalogMetas({ page, pageSize: 50 });
        result = catResult.metas;
      }
    } else {
      return res.json({ metas: [] });
    }

    res.json({ metas: result || [] });
  } catch (e) {
    console.error('[Catalog] Error:', e.message);
    res.json({ metas: [] });
  }
});

// ─── Meta Handler ────────────────────────────────────────────────────────────
app.get('/meta/:type/:id.json', async (req, res) => {
  try {
    const { type, id } = req.params;
    console.log(`[Meta] ${id}`);

    const [prefix, ...rest] = id.split(':');
    const realId = rest.join(':');

    let meta = null;

    if (prefix === 'xprimehub') {
      // For xprimehub, the id is the slug
      const parsed = xprime.parsePostSlug(realId);
      meta = {
        id: id,
        type: 'movie',
        name: parsed.title,
        poster: null,
        description: `${parsed.title}${parsed.year ? ' (' + parsed.year + ')' : ''} — ${parsed.language} ${parsed.qualities.join('/')}`,
        releaseInfo: parsed.year ? String(parsed.year) : '',
        genres: [parsed.language],
      };
    } else if (prefix === 'damplips') {
      // For damplips, the id is the post ID
      // Fetch the post to get metadata
      const streams = await damplips.getStreamsByPostId(realId);
      if (streams[0]) {
        meta = {
          id: id,
          type: 'movie',
          name: streams[0].meta.title,
          poster: streams[0].poster || streams[0].meta.poster,
          description: streams[0].title,
          releaseInfo: streams[0].meta.date || '',
          genres: streams[0].meta.category ? [streams[0].meta.category] : [],
        };
      }
    } else if (prefix === 'qorno') {
      // For qorno, the id is the video public-id
      // Search for it and return metadata from the first card found
      const cards = await qorno.searchCatalog(realId, 5);
      const card = cards.find(c => c.id === realId) || cards[0];
      if (card) {
        meta = {
          id: id,
          type: 'movie',
          name: card.title,
          poster: card.thumbnail,
          description: `${card.title}${card.duration ? ' [' + card.duration + ']' : ''}${card.source ? ' — ' + card.source : ''}`,
          releaseInfo: card.duration || '',
          genres: [card.source],
        };
      }
    } else if (prefix === 'fullporn') {
      // For fullporn, the id is the video ID
      // Fetch the video page to get title
      const streams = await fullporn.getStreamsByVideoId(realId);
      if (streams[0]) {
        meta = {
          id: id,
          type: 'movie',
          name: streams[0].meta.title,
          poster: streams[0].poster || streams[0].meta.thumbnail,
          description: streams[0].title,
          releaseInfo: streams[0].meta.duration || '',
          genres: streams[0].meta.isHd ? ['HD'] : [],
        };
      }
    }

    if (!meta) {
      return res.status(404).json({ error: 'Not found' });
    }

    res.json({ meta });
  } catch (e) {
    console.error('[Meta] Error:', e.message);
    res.status(500).json({ error: e.message });
  }
});

// ─── Stream Handler ──────────────────────────────────────────────────────────
app.get('/stream/:type/:id.json', async (req, res) => {
  try {
    const { type, id } = req.params;
    console.log(`[Stream] ${id}`);

    const [prefix, ...rest] = id.split(':');
    const realId = rest.join(':');

    let streams = [];

    if (prefix === 'xprimehub') {
      // realId is the slug
      streams = await xprime.getStreamsBySlug(realId);
    } else if (prefix === 'damplips') {
      // realId is the post ID
      streams = await damplips.getStreamsByPostId(realId);
    } else if (prefix === 'qorno') {
      // realId is the video public-id
      // Fetch the latest catalog or search to find the card with this ID
      const cards = await qorno.getCatalog();
      let card = cards.find(c => c.id === realId);
      if (!card) {
        // Try searching for it
        const searchCards = await qorno.searchCatalog(realId, 20);
        card = searchCards.find(c => c.id === realId);
      }
      if (card) {
        // Extract streams from the card's source URL
        const { execSync } = require('child_process');
        const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';
        streams = await qorno.getStreams(null, 'movie', null, null, { search: card.title });
        // Filter to match this video ID, or return all if no match
        const matched = streams.filter(s => s.meta && s.meta.videoId === realId);
        streams = matched.length > 0 ? matched : streams.slice(0, 3);
      }
    } else if (prefix === 'fullporn') {
      // realId is the video ID
      // First try to find the video with its slug from the catalog or search
      const cards = await fullporn.getCatalog();
      let card = cards.find(c => c.id === realId);
      if (!card) {
        // Search for it
        const searchCards = await fullporn.searchCatalog(realId, 20);
        card = searchCards.find(c => c.id === realId);
      }
      if (card) {
        // Use the full URL with slug
        streams = await fullporn.extractStreams(card);
      } else {
        // Fallback: try direct ID lookup (may only return preview)
        streams = await fullporn.getStreamsByVideoId(realId);
      }
    }

    console.log(`[Stream] Returning ${streams.length} stream(s) for ${id}`);
    res.json({ streams });
  } catch (e) {
    console.error('[Stream] Error:', e.message);
    res.json({ streams: [] });
  }
});

// ─── Manifest Endpoint ───────────────────────────────────────────────────────
app.get('/manifest.json', (req, res) => {
  res.json(manifest);
});

// ─── Helper: Convert functions ───────────────────────────────────────────────

// XPrimeHub: convert catalog entries to Stremio metas
function entriesToMetas(entries, prefix) {
  return entries.map(entry => ({
    id: `${prefix}:${entry.slug}`,
    type: 'movie',
    name: entry.parsed.title,
    poster: null,
    description: `${entry.parsed.title}${entry.parsed.year ? ' (' + entry.parsed.year + ')' : ''} — ${entry.parsed.language} ${entry.parsed.qualities.join('/')}`,
    releaseInfo: entry.parsed.year ? String(entry.parsed.year) : '',
    genres: [entry.parsed.language],
  }));
}

function convertMeta(m, prefix) {
  return {
    id: m.id || `${prefix}:${Date.now()}`,
    type: m.type || 'movie',
    name: m.name,
    poster: m.poster,
    description: m.description,
    releaseInfo: m.releaseInfo,
    genres: m.genres || [],
  };
}

// Damplips: convert search results to metas
function entriesToMetasDamplips(entries) {
  return entries.map(entry => ({
    id: `damplips:${entry.id}`,
    type: 'movie',
    name: entry.title,
    poster: entry.poster,
    description: `${entry.title}${entry.date ? ' — ' + entry.date : ''}${entry.category ? ' [' + entry.category + ']' : ''}`,
    releaseInfo: entry.date || '',
    genres: entry.category ? [entry.category] : [],
  }));
}

// Damplips: convert streams to metas (for category browse)
function streamsToMetas(streams, prefix) {
  const seen = new Set();
  return streams.filter(s => {
    if (seen.has(s.meta.postId)) return false;
    seen.add(s.meta.postId);
    return true;
  }).map(s => ({
    id: `${prefix}:${s.meta.postId}`,
    type: 'movie',
    name: s.meta.title,
    poster: s.poster || s.meta.poster,
    description: s.title,
    releaseInfo: s.meta.date || '',
    genres: s.meta.category ? [s.meta.category] : [],
  }));
}

// Qorno: convert cards to metas
function cardsToMetasQorno(cards) {
  return cards.map(card => ({
    id: `qorno:${card.id}`,
    type: 'movie',
    name: card.title,
    poster: card.thumbnail,
    description: `${card.title}${card.duration ? ' [' + card.duration + ']' : ''}${card.source ? ' — ' + card.source : ''}`,
    releaseInfo: card.duration || '',
    genres: [card.source],
  }));
}

// FullPorn: convert cards to metas
function cardsToMetasFullporn(cards) {
  return cards.map(card => ({
    id: `fullporn:${card.id}`,
    type: 'movie',
    name: card.title,
    poster: card.thumbnail,
    description: `${card.title}${card.duration ? ' [' + card.duration + ']' : ''}${card.isHd ? ' HD' : ''}`,
    releaseInfo: card.duration || '',
    genres: card.isHd ? ['HD'] : [],
  }));
}

// ─── Start Server ────────────────────────────────────────────────────────────
app.listen(PORT, () => {
  console.log('');
  console.log('╔══════════════════════════════════════════════════════════════╗');
  console.log('║  Adult Streaming Hub — Stremio/Nuvio Addon                  ║');
  console.log('╚══════════════════════════════════════════════════════════════╝');
  console.log('');
  console.log(`  Server: http://localhost:${PORT}`);
  console.log(`  Manifest: http://localhost:${PORT}/manifest.json`);
  console.log('');
  console.log('  Catalogs:');
  console.log('    1. XPrimeHub  — Movies, Web Series, Short Films');
  console.log('    2. Damplips   — Adult Short Films / Clips');
  console.log('    3. Qorno      — Aggregator (23+ sources)');
  console.log('    4. FullPorn   — Full-Length Videos (up to 4K)');
  console.log('');
  console.log('  To add in Stremio:');
  console.log(`    http://localhost:${PORT}/manifest.json`);
  console.log('');
  console.log('  Endpoints:');
  console.log('    GET /manifest.json                    — Addon manifest');
  console.log('    GET /catalog/movie/:id.json           — Browse catalog');
  console.log('    GET /catalog/movie/:id.json?search=X  — Search catalog');
  console.log('    GET /meta/movie/:id.json              — Video metadata');
  console.log('    GET /stream/movie/:id.json            — Stream URLs');
  console.log('');
});
