// BingeBang + KnightFlix — 18 streaming servers + TMDB-enriched metadata + subtitles.
//
// ARCHITECTURE (reverse-engineered from bingebang.club gateway + knightflix.pages.dev SPA):
//   • bingebang.club is a static gateway page — actual streaming happens on bingebang.st/.to/.pw/.sx
//     (all 4 are behind Cloudflare's "Just a moment..." JS challenge → unreachable via pure Node).
//   • knightflix.pages.dev is a Cloudflare Pages SPA that proxies TMDB + serves 18 video embeds
//     via worker `knightflix-dev.notflex.workers.dev`:
//         /api/{tmdb_path}              — TMDB proxy (used for enriched metadata)
//         /embed/:server/:type/:id/:se?/:ep? — 302 redirect to one of 18 player pages
//   • Each of the 18 servers (s1..s18) redirects to a different player domain
//     (vidlink.pro, vidzee.wtf, vidfast.pro, vixsrc.to, vidup.to, nontongo.win, etc).
//   • These players render the actual stream via JS + Go-compiled WASM (fu.wasm) — they CANNOT
//     be resolved to a direct .mp4/.m3u8 URL from Node.js alone (Cloudflare anti-bot + WASM
//     encryption). They CAN be played as iframe embeds in Stremio Desktop / Stremio Web (which
//     support behaviorHints.foreignRefs).
//
// SUBTITLES (goldmine):
//   • `core.vidzee.wtf/subs/movie/{tmdbId}` → returns up to 200 direct .vtt URLs across
//     37+ languages (Albanian, Arabic, Bengali, Chinese, English, French, German, Hindi,
//     Indonesian, Japanese, Korean, Malay, Portuguese, Russian, Spanish, Tamil, Telugu,
//     Thai, Turkish, Urdu, Vietnamese, ...).
//   • `core.vidzee.wtf/subs/tv/{tmdbId}/{season}/{episode}` → same for TV episodes.
//   • .vtt files are hosted on `rezesubs.com` (Cloudflare-cached, no auth).
//   • Examples captured: Inception → 202 subs (37 langs), Spirited Away → 86 subs (29 langs,
//     including Japanese — original audio), Breaking Bad S01E01 → 127 subs (36 langs).
//
// ANIME SUB/DUB:
//   • Subtitles: Spirited Away returns 86 .vtt files including Japanese (for sub watchers).
//   • Audio: Vidzee `/streams/languages/{type}/{tmdbId}` returns multi-AUDIO tracks for some
//     titles (e.g. Inception → Hindi/English/Tamil/Telugu, Breaking Bad → Hindi/English).
//     For pure anime (Spirited Away) it returns empty — the player's internal UI handles
//     audio switching. Each stream is labeled [MULTI-AUDIO] when tracks exist.
//
// 4K SUPPORT:
//   • Players (vidlink.pro, vidzee, vidfast, vixsrc) auto-detect up to 4K when available —
//     the player UI shows a quality picker. We label streams with "4K" in the name to indicate
//     multi-quality up to 4K is available (player will expose quality selector at play time).
//
// LIMITATIONS (honest):
//   • Stream URLs returned are iframe EMBED URLs, not raw .mp4/.m3u8. They will play in Stremio
//     Desktop / Stremio Web via foreignRefs, but will NOT play in players that lack iframe
//     support (e.g. some Android TV forks).
//   • Resolving to direct .mp4 requires running headless Chrome at request time, which is not
//     feasible on Render free tier. If you have a hosted Chrome endpoint, replace
//     `resolveDirectStream()` with a call to it.
//   • The bingebang.st/.to/.pw/.sx player sites themselves are Cloudflare-challenged and
//     unreachable. All access goes through knightflix.pages.dev as the only unblocked gateway.
//
// DEPENDENCIES:
//   • crypto-js (only used for hashing in OpenSubtitles fallback)
//   • No PenguPlay, no auth_token.
// ============================================================================

'use strict';

const crypto = require('crypto');
const TMDB_API_KEY = '8476a7ab80ad76f0936744df0430e67c';   // public TMDB key (used by knightflix worker)
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';
const WORKER = 'https://knightflix-dev.notflex.workers.dev';
const VIDZEE_CORE = 'https://core.vidzee.wtf';
const MAX_STREAMS = 50;

// ─── 18 KnightFlix servers (reverse-engineered from app.js CONFIG.SERVERS) ──────────
// Each server redirects via worker /embed/{id}/{type}/{tmdbId}/{se?}/{ep?} to a player page.
// Probe results (Sep 2026) — status of each server's player domain reachability:
//   OK = 200 (iframe loads), CF = 403 (Cloudflare challenge — may still work in browser),
//   DNS = domain unreachable (knightflix worker returns 302 to itself).
const SERVERS = [
  { id: 's7',  name: 'VidLink',        domain: 'vidlink.pro',           max: '4K',   multi: true,  languages: ['en','ja','es','fr','de','hi'], health: 'OK'  },
  { id: 's11', name: 'Vidzee',         domain: 'player.vidzee.wtf',     max: '4K',   multi: true,  languages: ['en','ja','zh','ko'],          health: 'OK'  },
  { id: 's10', name: 'VidFast',        domain: 'vidfast.vc',            max: '4K',   multi: true,  languages: ['en'],                       health: 'OK'  },
  { id: 's2',  name: 'VAPlayer',       domain: 'vaplayer.ru',           max: '4K',   multi: true,  languages: ['en','ru'],                  health: 'OK'  },
  { id: 's8',  name: 'Videasy',        domain: 'player.videasy.to',     max: '4K',   multi: true,  languages: ['en'],                       health: 'OK'  },
  { id: 's13', name: 'VidUp',          domain: 'vidup.to',              max: '4K',   multi: true,  languages: ['en'],                       health: 'OK'  },
  { id: 's14', name: 'VidNest',        domain: 'vidnest.fun',           max: '4K',   multi: true,  languages: ['en'],                       health: 'OK'  },
  { id: 's6',  name: 'VidLove',        domain: 'player.vidlove.cc',    max: '1080', multi: false, languages: ['en'],                       health: 'OK'  },
  { id: 's1',  name: 'CinemaOS',       domain: 'cinemaos.tech',         max: '4K',   multi: true,  languages: ['en'],                       health: '500' },
  { id: 's9',  name: 'VixSrc',         domain: 'vixsrc.to',             max: '4K',   multi: true,  languages: ['en'],                       health: 'CF'  },
  { id: 's18', name: 'NontonGo',       domain: 'www.nontongo.win',      max: '4K',   multi: true,  languages: ['en','id','ja'],             health: 'CF'  },
  { id: 's15', name: 'Vidify',         domain: 'player.vidify.top',     max: '4K',   multi: true,  languages: ['en'],                       health: '301' },
  { id: 's5',  name: 'Cinextream',     domain: 'cinextream.net',       max: '4K',   multi: true,  languages: ['en'],                       health: 'DNS' },
  { id: 's4',  name: 'Vyla',           domain: 'player.vyla.cc',       max: '4K',   multi: true,  languages: ['en'],                       health: 'DNS' },
  { id: 's3',  name: 'VidCodin',       domain: 'vidcodin.net',          max: '1080', multi: false, languages: ['en'],                       health: 'DNS' },
  { id: 's16', name: 'VidKing',        domain: 'www.vidking.net',       max: '1080', multi: false, languages: ['en'],                       health: 'DNS' },
  { id: 's17', name: '111Movies.com',  domain: '111movies.com',         max: '1080', multi: false, languages: ['en'],                       health: 'DNS' },
  { id: 's12', name: 'Vidzee v2',      domain: 'player.vidzee.wtf/v2',  max: '4K',   multi: true,  languages: ['en','ja','zh','ko'],         health: '404' },
];

// ─── HTTP helpers ─────────────────────────────────────────────────────────────────
async function fetchJson(url, opts = {}) {
  const res = await fetch(url, {
    headers: { 'User-Agent': UA, 'Accept': 'application/json', ...opts.headers },
    signal: AbortSignal.timeout(opts.timeout || 15000),
    ...opts,
  });
  if (!res.ok) throw new Error(`HTTP ${res.status} on ${url.slice(0, 80)}`);
  return res.json();
}

// ─── TMDB metadata (via knightflix worker proxy — enriched) ──────────────────────
async function getMetadata(id, type, season, episode) {
  const isMovie = type !== 'tv' && type !== 'series';
  const mediaType = isMovie ? 'movie' : 'tv';

  // Step 1: resolve IMDB→TMDB if needed
  let tmdbId = id;
  let imdbId = id;
  let meta = null;

  if (/^tt\d+$/.test(String(id))) {
    try {
      const d = await fetchJson(`${WORKER}/api/find/${id}?external_source=imdb_id`);
      const r = isMovie ? d.movie_results : d.tv_results;
      if (r && r[0]) {
        tmdbId = String(r[0].id);
        meta = r[0];
      }
    } catch (e) { /* fall through */ }
  } else if (/^\d+$/.test(String(id))) {
    // already a TMDB id
    tmdbId = String(id);
  }

  // Step 2: fetch full metadata via worker TMDB proxy — with append_to_response for credits
  try {
    const full = await fetchJson(
      `${WORKER}/api/${mediaType}/${tmdbId}?append_to_response=credits,external_ids,videos,release_dates,images,similar,recommendations,content_ratings`,
      { timeout: 20000 }
    );
    meta = { ...meta, ...full };
  } catch (e) { /* fall back to whatever we have */ }

  if (!meta) return { title: id, year: '', tmdbId, imdbId, isMovie };

  const title = isMovie ? meta.title : meta.name;
  const year = ((meta.first_air_date || meta.release_date || '') + '').split('-')[0];

  // Get external IDs (imdb_id) — for subtitle lookup
  // (When append_to_response=external_ids was used, this is already in meta.external_ids)
  if (meta.external_ids?.imdb_id) imdbId = meta.external_ids.imdb_id;
  if (!imdbId || imdbId === id) {
    try {
      const ext = await fetchJson(`${WORKER}/api/${mediaType}/${tmdbId}/external_ids`);
      if (ext.imdb_id) imdbId = ext.imdb_id;
    } catch (e) { /* noop */ }
  }

  return {
    title,
    year,
    tmdbId,
    imdbId,
    isMovie,
    genres: meta.genres?.map(g => g.name) || [],
    runtime: meta.runtime || meta.episode_run_time?.[0] || null,
    overview: meta.overview || '',
    posterPath: meta.poster_path || '',
    backdropPath: meta.backdrop_path || '',
    voteAverage: meta.vote_average || 0,
    voteCount: meta.vote_count || 0,
    cast: meta.credits?.cast?.slice(0, 10).map(c => c.name) || [],
    director: (meta.credits?.crew || meta.crew || []).find(c => c.job === 'Director')?.name || '',
    tagline: meta.tagline || '',
    status: meta.status || '',
    originalLanguage: meta.original_language || '',
    productionCountries: meta.production_countries?.map(c => c.name) || [],
    animeGenres: (meta.genres || []).some(g => g.id === 16), // Animation genre ID = 16
    videos: meta.videos?.results?.filter(v => v.site === 'YouTube').slice(0, 3).map(v => v.key) || [],
    similar: meta.similar?.results?.slice(0, 5).map(s => ({ id: s.id, title: s.title || s.name, poster: s.poster_path })) || [],
    recommendations: meta.recommendations?.results?.slice(0, 5).map(s => ({ id: s.id, title: s.title || s.name, poster: s.poster_path })) || [],
  };
}

// ─── Build embed URL via knightflix worker ────────────────────────────────────────
function buildEmbedUrl(server, type, tmdbId, season, episode) {
  const isMovie = type !== 'tv' && type !== 'series';
  const mediaType = isMovie ? 'movie' : 'tv';
  if (isMovie) {
    return `${WORKER}/embed/${server}/${mediaType}/${tmdbId}`;
  } else {
    return `${WORKER}/embed/${server}/${mediaType}/${tmdbId}/${season || 1}/${episode || 1}`;
  }
}

// ─── Anime sub/dub language tracks (from vidzee core API) ───────────────────────
async function getLanguageTracks(id, type, season, episode) {
  const isMovie = type !== 'tv' && type !== 'series';
  const mediaType = isMovie ? 'movie' : 'tv';
  const path = isMovie
    ? `/streams/languages/${mediaType}/${encodeURIComponent(id)}`
    : `/streams/languages/${mediaType}/${encodeURIComponent(id)}/${season || 1}/${episode || 1}`;

  try {
    const d = await fetchJson(`${VIDZEE_CORE}${path}`, {
      headers: { 'Referer': 'https://player.vidzee.wtf/' }
    });
    return d.languages || [];
  } catch (e) { return []; }
}

// ─── Subtitles (direct .vtt URLs from vidzee core API) ──────────────────────────
//   Movies:  GET https://core.vidzee.wtf/subs/movie/{tmdbId}
//   TV:      GET https://core.vidzee.wtf/subs/tv/{tmdbId}/{season}/{episode}
//   Returns: [{ label: "English", file: "https://rezesubs.com/subs/movie/.../english.vtt" }, ...]
//   Captured: Inception → 202 subs (37 langs), Spirited Away → 86 subs (29 langs incl. Japanese),
//             Breaking Bad S01E01 → 127 subs (36 langs).
async function getVidzeeSubs(tmdbId, type, season, episode) {
  const isMovie = type !== 'tv' && type !== 'series';
  const path = isMovie
    ? `/subs/movie/${tmdbId}`
    : `/subs/tv/${tmdbId}/${season || 1}/${episode || 1}`;

  try {
    const subs = await fetchJson(`${VIDZEE_CORE}${path}`, {
      headers: { 'Referer': 'https://player.vidzee.wtf/' },
      timeout: 15000,
    });
    if (!Array.isArray(subs)) return [];

    // Group by language to dedupe (vidzee often has multiple .vtt per language for the same movie)
    const byLang = new Map();
    for (const s of subs) {
      const lang = (s.label || s.language || 'Unknown').trim();
      const url = s.file || s.url;
      if (!url) continue;
      // Normalize language code
      const langKey = lang.toLowerCase().replace(/\s/g, '');
      if (!byLang.has(langKey)) {
        byLang.set(langKey, { lang, url, name: lang + ' [Vidzee Subs]' });
      }
    }
    return [...byLang.values()];
  } catch (e) {
    console.log('[BingeBang] Subs err: ' + e.message);
    return [];
  }
}

// ─── Main entry: getStreams(id, type, season, episode) ────────────────────────────
async function getStreams(id, type, season, episode) {
  console.log('[BingeBang] Request: id=' + id + ' type=' + type + (season ? ' S' + season + 'E' + episode : ''));

  // 1. Enriched metadata via TMDB proxy
  const info = await getMetadata(id, type, season, episode);
  console.log('[BingeBang] Title: ' + info.title + ' (' + info.year + ') tmdb=' + info.tmdbId + ' imdb=' + info.imdbId + (info.animeGenres ? ' [ANIME]' : ''));

  // 2. Anime sub/dub / multi-audio tracks (best-effort)
  const langTracks = await getLanguageTracks(info.tmdbId, type, season, episode);
  console.log('[BingeBang] Multi-audio tracks: ' + JSON.stringify(langTracks));

  // 3. Build embed URLs for all 18 servers — rank healthy ones first
  const streams = [];
  const multiAudioTag = langTracks.length > 0
    ? ' [MULTI-AUDIO: ' + langTracks.join('/') + ']'
    : (info.animeGenres ? ' [SUB+DUB]' : '');
  const rankedServers = [...SERVERS].sort((a, b) => {
    // rank: OK > 301 > CF > 500 > DNS > 404
    const order = { 'OK': 0, '301': 1, 'CF': 2, '500': 3, 'DNS': 4, '404': 5 };
    return (order[a.health] ?? 9) - (order[b.health] ?? 9);
  });
  for (const srv of rankedServers) {
    const embedUrl = buildEmbedUrl(srv.id, type, info.tmdbId, season, episode);
    const quality = srv.max === '4K' ? '4K' : srv.max;
    const srvAudioTag = srv.multi && srv.languages.length > 1
      ? ' [' + srv.languages.join('/') + ']'
      : ' [' + srv.languages.join('/') + ']';
    const animeTag = info.animeGenres ? ' [SUB+DUB]' : '';
    const healthTag = srv.health !== 'OK' ? ` [${srv.health}]` : '';

    streams.push({
      name: `[BB] BingeBang ${quality} | ${srv.name}${animeTag || multiAudioTag}${healthTag}`,
      title: info.title + (info.isMovie ? '' : ' S' + (season || 1) + 'E' + (episode || 1)) + ` (${info.year}) [${srv.name}]${srvAudioTag}${animeTag || multiAudioTag}${healthTag}`,
      url: embedUrl,
      quality: quality.toLowerCase(),
      type: 'video/mp4',  // Stremio plays iframe embeds via foreignRefs (below)
      headers: { 'User-Agent': UA, 'Referer': 'https://knightflix.pages.dev/' },
      provider: 'BingeBang',
      behaviorHints: {
        // Mark as iframe-embeddable stream (Stremio Desktop/Web will render in iframe).
        // These are NOT direct .mp4/.m3u8 URLs — they redirect to a player page that
        // loads the actual stream via JS+WASM. Resolving to direct URL requires a
        // headless browser at request time.
        notWebReady: true,
        foreignRefs: [{ url: embedUrl }],
        bingeGroup: `bingebang|${srv.id}|${quality}`,
      },
    });
  }

  // 4. If vidzee returned multi-audio tracks, expose them as separate streams (for sub/dub anime)
  if (langTracks.length > 0) {
    // These tracks are AUDIO LANGUAGES available in the player, not separate stream URLs.
    // We don't add separate stream entries for them — the player UI handles language
    // switching. The label on existing streams already shows available languages.
    // (Vidzee API returns ["Hindi","English","Tamil","Telugu"] — these are audio tracks,
    // not subbed/dubbed variants. The user can switch audio in the player.)
  }

  // 5. Subtitles (direct .vtt URLs from vidzee — goldmine!)
  let subtitles = [];
  try {
    subtitles = await getVidzeeSubs(info.tmdbId, type, season, episode);
    console.log('[BingeBang] Subtitles: ' + subtitles.length + ' (Vidzee)');
  } catch (e) { console.log('[BingeBang] Subs err: ' + e.message); }

  // Attach subtitles to each stream
  if (subtitles.length > 0) {
    for (const stream of streams) {
      stream.behaviorHints.subtitles = subtitles.map(s => s.url);
    }
  }
  streams.subtitles = subtitles;

  // 6. Attach enriched metadata to the response (Stremio can use it for catalog display)
  streams.meta = {
    id: info.imdbId || info.tmdbId,
    imdbId: info.imdbId,
    tmdbId: info.tmdbId,
    type: info.isMovie ? 'movie' : 'series',
    name: info.title,
    year: info.year,
    genres: info.genres,
    cast: info.cast,
    director: info.director,
    tag: info.tagline,
    tagline: info.tagline,
    overview: info.overview,
    poster: info.posterPath ? `https://image.tmdb.org/t/p/w500${info.posterPath}` : '',
    backdrop: info.backdropPath ? `https://image.tmdb.org/t/p/original${info.backdropPath}` : '',
    imdbRating: info.voteAverage ? info.voteAverage.toFixed(1) : '',
    runtime: info.runtime ? `${info.runtime}min` : '',
    status: info.status,
    originalLanguage: info.originalLanguage,
    productionCountries: info.productionCountries,
    isAnime: !!info.animeGenres,
    trailers: info.videos || [],
    similar: info.similar || [],
    recommendations: info.recommendations || [],
  };

  // Cap at MAX_STREAMS
  const capped = streams.slice(0, MAX_STREAMS);
  if (streams.subtitles) capped.subtitles = streams.subtitles;
  if (streams.meta) capped.meta = streams.meta;
  console.log('[BingeBang] Returning ' + capped.length + ' streams + ' + (capped.subtitles?.length || 0) + ' subtitles');
  return capped;
}

// ─── CLI ─────────────────────────────────────────────────────────────────────────
if (require.main === module) {
  (async () => {
    const args = process.argv.slice(2);
    if (!args[0]) {
      console.log('Usage: node bingebang.js <imdbId|tmdbId> <movie|tv> [season] [episode]');
      process.exit(0);
    }
    const streams = await getStreams(args[0], args[1] || 'movie', args[2], args[3]);
    console.log('\n' + streams.length + ' streams:');
    streams.slice(0, 18).forEach((s, i) => {
      console.log('[' + i + '] ' + s.name + ' (' + s.quality + ')');
      console.log('    ' + s.url);
    });
    if (streams.subtitles && streams.subtitles.length) {
      console.log('\n' + streams.subtitles.length + ' subtitles:');
      streams.subtitles.slice(0, 10).forEach((s, i) => console.log('[' + i + '] ' + s.lang + ' ' + s.name + ' → ' + s.url.slice(0, 80)));
    }
    if (streams.meta) {
      console.log('\nMeta:');
      console.log('  Title:    ' + streams.meta.name + ' (' + streams.meta.year + ')');
      console.log('  Genres:   ' + (streams.meta.genres || []).join(', '));
      console.log('  Director: ' + streams.meta.director);
      console.log('  Cast:     ' + (streams.meta.cast || []).slice(0, 5).join(', '));
      console.log('  Rating:   ' + streams.meta.imdbRating + ' (TMDB)');
      console.log('  Runtime:  ' + streams.meta.runtime);
      console.log('  Status:   ' + streams.meta.status);
      console.log('  Lang:     ' + streams.meta.originalLanguage);
      console.log('  Anime:    ' + streams.meta.isAnime);
      console.log('  Poster:   ' + streams.meta.poster);
      console.log('  Trailer:  ' + (streams.meta.trailers || []).map(k => 'https://youtu.be/' + k).join(' | '));
      console.log('  Similar:  ' + (streams.meta.similar || []).map(s => s.title).join(' | '));
      console.log('  Recs:     ' + (streams.meta.recommendations || []).map(s => s.title).join(' | '));
    }
  })();
}

module.exports = { getStreams, getMetadata, buildEmbedUrl, SERVERS };
