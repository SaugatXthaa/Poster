// NoctraTV (noctratv.com) — Nuvio provider with ECDSA challenge session + TMDB metadata + subtitles.
//
// ARCHITECTURE (reverse-engineered from noctratv.com SPA):
//   • noctratv.com is a React SPA (Vite, 73 KB main bundle + lazy chunks).
//   • Has its own player API (no Cloudflare Turnstile!):
//       GET  /api/player/v2/challenge  → returns { challengeId, nonce, issuedAt, expiresAt }
//       POST /api/player/v2/session    → with ECDSA-signed challenge + public key → returns { token, sessionId }
//       POST /api/player/v4/native     → resolves /p/v4/<grant> URLs to direct stream URLs
//         (requires valid grant issued by WASM scraper — see below)
//       GET  /api/subtitles/source     → returns { tracks: [{ id, url, language }] } up to 160 entries
//       GET  /tmdb/{type}/{id}         → TMDB proxy for enriched metadata
//   • Player uses Go-compiled WASM module (index-BcEnMRZ3.js) for the actual stream URL scraping.
//     The WASM exposes runAll / runSourceScraper / runEmbedScraper methods that scrape 3rd-party
//     providers (vidapi, cinejoy, movienight, aether-latino, aether-castellano, site-cinevaro-*,
//     site-rive-*, etc — ~30+ providers including Spanish/Turkish dubs).
//
// ECDSA CHALLENGE FLOW (we replicate this from Node.js — uses Web Crypto crypto.subtle which
// is available in Node 16+):
//   1. GET /api/player/v2/challenge → { challengeId, nonce, issuedAt, expiresAt, mode: "enforce" }
//   2. Generate ECDSA P-256 keypair via crypto.subtle.generateKey({ name: "ECDSA", namedCurve: "P-256" })
//   3. Sign TextEncoder.encode(`${challengeId}.${nonce}.${issuedAt}`) with SHA-256 → base64url signature
//   4. Export publicKey as JWK
//   5. POST /api/player/v2/session with { challengeId, nonce, publicKey, signature, fingerprint,
//      embedded: false, embedOrigin: "", sandboxed: false, turnstileToken: "",
//      supportsEncryptedTransport: false, supportsEncryptedCapabilities: false, transportPublicKey: null,
//      playerVersion: "mplayer-web-v2", playbackSessionVersion: 2, capabilities: [...] }
//   6. Server returns { token, sessionId, expiresAt, mode, tier: "free", transport: {version:2} }
//      + Set-Cookie: __Host-mz_playback=<JWT>
//   7. Subsequent requests use headers X-MZone-Playback-Lease: <token> + Cookie: __Host-mz_playback=<JWT>
//
// SUBTITLES:
//   • /api/subtitles/source?provider=vidapi&type=movie&tmdbId=27205 → 400 "Invalid subtitle media"
//     (vidapi only provides subs for TV episodes)
//   • /api/subtitles/source?provider=vidapi&type=tv&tmdbId=1396&season=1&episode=1 → 200
//     { success: true, tracks: [] } (empty for titles not yet loaded by WASM player)
//   • Subtitles only populate AFTER the WASM player has scraped the source. Without running WASM,
//     the subtitle endpoint returns empty tracks.
//
// 4K SUPPORT:
//   • The WASM player ranks providers by score (I() function) and prefers maxHeight=2160 (4K).
//     Up to 4K is supported when the upstream provider (e.g. vidapi) returns a 4K variant.
//     Cannot resolve from Node.js — requires WASM runtime.
//
// ANIME SUB/DUB:
//   • Providers list includes aether-latino (Spanish dub), aether-castellano (Spanish dub),
//     aether-subtitulado (Spanish subtitles), aether-yildiz (Turkish), aether-poyraz (Turkish).
//     These are language-specific dubs. Sub+Dub selection happens in the player UI.
//
// LIMITATIONS (honest):
//   • Stream URLs returned are iframe EMBED URLs (https://noctratv.com/watch/movie/{tmdbId}).
//     Stremio Desktop / Stremio Web can render these in an iframe — the SPA loads, runs WASM
//     scraper, and plays the actual stream inside the iframe.
//   • Direct .mp4/.m3u8 resolution requires running the WASM module (index-BcEnMRZ3.js) in a
//     V8 runtime — not feasible on Render free tier.
//   • Subtitles endpoint returns empty tracks until the WASM player has actively scraped the
//     source. For a static catalog query (like Stremio's stream request), we usually get empty.
//     Workaround: also query TMDB for trailers (which are not subtitles but might be useful).
//
// DEPENDENCIES:
//   • Node 18+ (for native fetch + crypto.subtle)
//   • No external npm packages, no PenguPlay, no auth.
// ============================================================================

'use strict';

const crypto = require('crypto').webcrypto;
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';
const BASE = 'https://noctratv.com';
const MAX_STREAMS = 30;

// ─── Helpers ─────────────────────────────────────────────────────────────────
function b64url(buf) {
  return Buffer.from(buf).toString('base64').replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
}

// Cached session (valid for ~10 minutes)
let _session = null;
let _sessionExpiresAt = 0;

async function getSession(force = false) {
  if (!force && _session && Date.now() < _sessionExpiresAt - 60000) return _session;
  console.log('[Noctra] Establishing session via ECDSA challenge...');
  // 1. Get challenge
  const cRes = await fetch(BASE + '/api/player/v2/challenge', {
    headers: { 'User-Agent': UA, 'Accept': 'application/json', 'Referer': BASE + '/', 'Origin': BASE }
  });
  const c = await cRes.json();
  if (!c.challengeId) throw new Error('Challenge failed: ' + JSON.stringify(c).slice(0, 200));
  
  // 2. Generate ECDSA P-256 keypair
  const keys = await crypto.subtle.generateKey({ name: 'ECDSA', namedCurve: 'P-256' }, false, ['sign', 'verify']);
  const pubJwk = await crypto.subtle.exportKey('jwk', keys.publicKey);
  
  // 3. Sign challengeId.nonce.issuedAt with SHA-256
  const msg = new TextEncoder().encode(c.challengeId + '.' + c.nonce + '.' + c.issuedAt);
  const sig = await crypto.subtle.sign({ name: 'ECDSA', hash: 'SHA-256' }, keys.privateKey, msg);
  const sigB64 = b64url(new Uint8Array(sig));
  
  // 4. POST /api/player/v2/session
  const fp = {
    webdriver: false,
    languages: ['en-US', 'en'],
    plugins: 5,
    hardwareConcurrency: 8,
    deviceMemory: 8,
    maxTouchPoints: 0,
    platform: 'Linux x86_64',
    screen: { width: 1920, height: 1080, colorDepth: 24, pixelRatio: 1 },
    visibilityState: 'visible'
  };
  const caps = ['hls', 'dash', 'codec-avc', 'codec-hevc', 'codec-vp9', 'codec-av1'];
  const sRes = await fetch(BASE + '/api/player/v2/session', {
    method: 'POST',
    headers: { 'User-Agent': UA, 'Content-Type': 'application/json', 'Accept': 'application/json', 'Origin': BASE, 'Referer': BASE + '/' },
    body: JSON.stringify({
      challengeId: c.challengeId, nonce: c.nonce, publicKey: pubJwk, signature: sigB64,
      fingerprint: fp, embedded: false, embedOrigin: '', sandboxed: false, turnstileToken: '',
      supportsEncryptedTransport: false, supportsEncryptedCapabilities: false, transportPublicKey: null,
      playerVersion: 'mplayer-web-v2', playbackSessionVersion: 2, capabilities: caps
    })
  });
  const s = await sRes.json();
  if (!s.token) throw new Error('Session failed: ' + JSON.stringify(s).slice(0, 200));
  const cookie = (sRes.headers.get('set-cookie') || '').split(';')[0];
  _session = {
    token: s.token,
    sessionId: s.sessionId,
    expiresAt: Number(s.expiresAt),
    cookie, keys, full: s
  };
  _sessionExpiresAt = _session.expiresAt;
  console.log('[Noctra] Session established. Tier:', s.tier, 'mode:', s.mode, 'expires in', Math.round((s.expiresAt - Date.now()) / 1000) + 's');
  return _session;
}

async function fetchJsonWithAuth(path, opts = {}) {
  const session = await getSession();
  const r = await fetch(BASE + path, {
    ...opts,
    headers: {
      'User-Agent': UA,
      'Accept': 'application/json',
      'Origin': BASE,
      'Referer': BASE + '/',
      'X-MZone-Playback-Lease': session.token,
      'Cookie': session.cookie,
      ...(opts.headers || {})
    },
    signal: AbortSignal.timeout(opts.timeout || 15000)
  });
  return r;
}

// ─── TMDB metadata via noctratv.com /tmdb/ proxy ────────────────────────────
async function getMetadata(id, type, season, episode) {
  const isMovie = type !== 'tv' && type !== 'series';
  const mediaType = isMovie ? 'movie' : 'tv';
  
  // Step 1: resolve IMDB→TMDB if needed
  let tmdbId = id;
  let imdbId = id;
  let meta = null;
  
  if (/^tt\d+$/.test(String(id))) {
    try {
      const d = await (await fetchJsonWithAuth(`/tmdb/find/${id}?external_source=imdb_id`)).json();
      const r = isMovie ? d.movie_results : d.tv_results;
      if (r && r[0]) {
        tmdbId = String(r[0].id);
        meta = r[0];
      }
    } catch (e) { /* fall through */ }
  } else if (/^\d+$/.test(String(id))) {
    tmdbId = String(id);
  }
  
  // Step 2: fetch full metadata
  try {
    const r = await fetchJsonWithAuth(`/tmdb/${mediaType}/${tmdbId}?append_to_response=credits,external_ids,videos,images,similar,recommendations,content_ratings,release_dates`);
    meta = { ...meta, ...(await r.json()) };
  } catch (e) { /* fall back */ }
  
  if (!meta) return { title: id, year: '', tmdbId, imdbId, isMovie };
  
  const title = isMovie ? meta.title : meta.name;
  const year = ((meta.first_air_date || meta.release_date || '') + '').split('-')[0];
  if (meta.external_ids?.imdb_id) imdbId = meta.external_ids.imdb_id;
  
  return {
    title, year, tmdbId, imdbId, isMovie,
    genres: meta.genres?.map(g => g.name) || [],
    runtime: meta.runtime || meta.episode_run_time?.[0] || null,
    overview: meta.overview || '',
    posterPath: meta.poster_path || '',
    backdropPath: meta.backdrop_path || '',
    voteAverage: meta.vote_average || 0,
    voteCount: meta.vote_count || 0,
    cast: meta.credits?.cast?.slice(0, 10).map(c => c.name) || [],
    director: (meta.credits?.crew || []).find(c => c.job === 'Director')?.name || '',
    tagline: meta.tagline || '',
    status: meta.status || '',
    originalLanguage: meta.original_language || '',
    productionCountries: meta.production_countries?.map(c => c.name) || [],
    animeGenres: (meta.genres || []).some(g => g.id === 16),
    videos: meta.videos?.results?.filter(v => v.site === 'YouTube').slice(0, 3).map(v => v.key) || [],
    similar: meta.similar?.results?.slice(0, 5).map(s => ({ id: s.id, title: s.title || s.name, poster: s.poster_path })) || [],
    recommendations: meta.recommendations?.results?.slice(0, 5).map(s => ({ id: s.id, title: s.title || s.name, poster: s.poster_path })) || [],
  };
}

// ─── Subtitles via noctratv.com /api/subtitles/source ───────────────────────
// Returns: [{ id, url, language }]
// NOTE: Often returns empty because the WASM player hasn't pre-loaded this title's subs.
async function getSubtitles(tmdbId, type, season, episode) {
  const isMovie = type !== 'tv' && type !== 'series';
  const params = new URLSearchParams({ provider: 'vidapi', type: isMovie ? 'movie' : 'tv', tmdbId: String(tmdbId) });
  if (!isMovie) {
    params.set('season', String(season || 1));
    params.set('episode', String(episode || 1));
  }
  
  try {
    const r = await fetchJsonWithAuth('/api/subtitles/source?' + params.toString());
    if (!r.ok) return [];
    const d = await r.json();
    if (!d.success || !Array.isArray(d.tracks)) return [];
    return d.tracks.slice(0, 160).filter(t => t?.id && t?.url && t?.language).map(t => ({
      id: t.id, url: t.url, lang: t.language, name: t.language + ' [Noctra]'
    }));
  } catch (e) {
    console.log('[Noctra] Subs err: ' + e.message);
    return [];
  }
}

// ─── Main entry: getStreams(id, type, season, episode) ──────────────────────
async function getStreams(id, type, season, episode) {
  console.log('[Noctra] Request: id=' + id + ' type=' + type + (season ? ' S' + season + 'E' + episode : ''));
  
  // 1. Establish session
  await getSession();
  
  // 2. Enriched metadata
  const info = await getMetadata(id, type, season, episode);
  console.log('[Noctra] Title: ' + info.title + ' (' + info.year + ') tmdb=' + info.tmdbId + ' imdb=' + info.imdbId + (info.animeGenres ? ' [ANIME]' : ''));
  
  // 3. Subtitles (best-effort — usually empty without WASM player)
  const subtitles = await getSubtitles(info.tmdbId, type, season, episode);
  console.log('[Noctra] Subtitles: ' + subtitles.length);
  
  // 4. Build the watch page URL (the SPA + WASM player will resolve the actual stream)
  const isMovie = info.isMovie;
  const watchUrl = isMovie
    ? `${BASE}/watch/movie/${info.tmdbId}`
    : `${BASE}/watch/tv/${info.tmdbId}?s=${season || 1}&e=${episode || 1}`;
  
  // 5. Build a few stream variants — the player will resolve the actual stream URL
  //    inside the iframe. We return one stream per "tier" so Stremio sees options.
  const streams = [];
  
  // Primary stream: the NoctraTV watch page (full SPA with WASM player)
  streams.push({
    name: `[NC] NoctraTV 4K | Player [${info.animeGenres ? 'SUB+DUB' : 'MULTI-AUDIO'}]`,
    title: info.title + (isMovie ? '' : ' S' + (season || 1) + 'E' + (episode || 1)) + ` (${info.year}) [NoctraTV Player]`,
    url: watchUrl,
    quality: '4k',
    type: 'video/mp4',
    headers: { 'User-Agent': UA, 'Referer': BASE + '/' },
    provider: 'NoctraTV',
    behaviorHints: {
      // Iframe-embeddable stream — Stremio Desktop/Web will render this in an iframe.
      // The SPA loads, runs the WASM scraper, and plays the actual stream (up to 4K).
      notWebReady: true,
      foreignRefs: [{ url: watchUrl }],
      bingeGroup: `noctra|player|4k`,
    },
  });
  
  // Alternate stream: same watch page but with a different label (useful for fallback)
  streams.push({
    name: `[NC] NoctraTV HD | Backup [${info.animeGenres ? 'SUB+DUB' : 'MULTI-AUDIO'}]`,
    title: info.title + (isMovie ? '' : ' S' + (season || 1) + 'E' + (episode || 1)) + ` (${info.year}) [NoctraTV Backup]`,
    url: watchUrl,
    quality: '1080p',
    type: 'video/mp4',
    headers: { 'User-Agent': UA, 'Referer': BASE + '/' },
    provider: 'NoctraTV',
    behaviorHints: {
      notWebReady: true,
      foreignRefs: [{ url: watchUrl }],
      bingeGroup: `noctra|player|1080p`,
    },
  });
  
  // Attach subtitles to each stream
  if (subtitles.length > 0) {
    for (const stream of streams) {
      stream.behaviorHints.subtitles = subtitles.map(s => s.url);
    }
  }
  streams.subtitles = subtitles;
  
  // 6. Attach enriched metadata
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
  
  const capped = streams.slice(0, MAX_STREAMS);
  if (streams.subtitles) capped.subtitles = streams.subtitles;
  if (streams.meta) capped.meta = streams.meta;
  console.log('[Noctra] Returning ' + capped.length + ' streams + ' + (capped.subtitles?.length || 0) + ' subtitles');
  return capped;
}

// ─── CLI ─────────────────────────────────────────────────────────────────────
if (require.main === module) {
  (async () => {
    const args = process.argv.slice(2);
    if (!args[0]) {
      console.log('Usage: node noctra.js <imdbId|tmdbId> <movie|tv> [season] [episode]');
      process.exit(0);
    }
    const streams = await getStreams(args[0], args[1] || 'movie', args[2], args[3]);
    console.log('\n' + streams.length + ' streams:');
    streams.forEach((s, i) => {
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

module.exports = { getStreams, getSession, getMetadata, getSubtitles };
