# All-in-One-Nuvio — 27 Working Providers

Source: https://github.com/NuvioPlugin/All-in-One-Nuvio.git
Pure Node.js — **no Playwright, no browser, no external tools**.

## Files

| File | Provider | Max Quality | Content Focus |
|---|---|---|---|
| `nuvio_4khdhub.js` | 4KHDHub | 1080p | Movies, TV |
| `nuvio_animezey.js` | AnimeZeY | 1080p | Anime, Movies, TV |
| `nuvio_castle.js` | Castle | 1080p | Multi-language (Hindi/Tamil/Telugu/Kannada) |
| `nuvio_cineby.js` | Cineby | **4K** | Movies, TV |
| `nuvio_ctgmovies.js` | CTGMovies | 1080p | Movies, TV (Hindi/English) |
| `nuvio_dahmermovies.js` | Dahmermovies | **4K** | Movies, TV |
| `nuvio_dahmermovies-4k.js` | Dahmermovies 4K | **4K** | Movies, TV (4K focused) |
| `nuvio_desiflix.js` | DesiFlix | 1080p | Desi content |
| `nuvio_goated.js` | Goated | 1080p | Movies, TV |
| `nuvio_hdghartv.js` | HDGharTV | 1080p | Movies, TV |
| `nuvio_hdhub4u.js` | HDHub4u | **4K** | Movies, TV (Hindi/English, multi-audio) |
| `nuvio_hindmoviez.js` | HindMoviez | **4K** | Movies, TV (Hindi) |
| `nuvio_movieblast.js` | MovieBlast | 1080p | Movies (Hindi/English) |
| `nuvio_movies4u.js` | Movies4u | 1080p | Movies only |
| `nuvio_movieshunt.js` | MoviesHunt | 1080p | Movies only |
| `nuvio_movix.js` | Movix VF | 1080p | Movies, TV (French) |
| `nuvio_netmirror.js` | NetMirror | 1080p | Movies, TV |
| `nuvio_persianstremio.js` | PersianStremio | 1080p | Persian + English |
| `nuvio_playimdb.js` | PlayIMDb | 1080p | Movies, TV |
| `nuvio_purstream.js` | Purstream | 1080p | Movies, TV |
| `nuvio_torrentio.js` | Torrentio | **4K** | Torrent-based (magnet links) |
| `nuvio_uhdmovies.js` | UHDMovies | **4K** | Movies, TV (Google CDN) |
| `nuvio_videasy.js` | VidEasy | **4K** | Movies, TV (10-server fallback) |
| `nuvio_vidfast.js` | VidFast | 1080p | Movies, TV |
| `nuvio_vidlove.js` | VidLove | 1080p | Movies, TV |
| `nuvio_vixsrc.js` | VixSrc | 1080p | Movies, TV |
| `nuvio_zinkmovies.js` | ZinkMovies | 1080p | Movies, TV (Hindi) |

## Test Results

| Content Type | Test Case | Working Providers | Total Streams |
|---|---|---|---|
| Movie | Dune Part Two (TMDB 693134) | 21/27 | 116 |
| TV Show | Mandalorian S01E01 (TMDB 82856) | 21/27 | 105 |
| Anime | Jujutsu Kaisen S01E01 (TMDB 95479) | 16/27 | 77 |
| K-Drama | Crash Landing on You S01E01 (TMDB 86639) | 17/27 | (verified) |

## Usage

```javascript
const provider = require('./nuvio_cineby');

// Movie
const movieStreams = await provider.getStreams(693134, 'movie');

// TV Show episode
const tvStreams = await provider.getStreams(82856, 'tv', 1, 1);  // S01E01

// Anime
const animeStreams = await provider.getStreams(95479, 'tv', 1, 1);

// K-Drama
const kdramaStreams = await provider.getStreams(86639, 'tv', 1, 1);
```

## How They Bypass Cloudflare

These providers use **pure `fetch()`** with various CF bypass techniques:
- Custom User-Agent strings (mobile + desktop variants)
- AbortController timeouts (avoid hanging on CF challenges)
- AES-CBC local decryption of API responses (Castle, Cineby)
- Multi-server fallback in parallel (VidEasy tries 10 servers, VidFast tries 6)
- Alternate domain routing (VixSrc uses komiknostalgia.id, DesiFlix uses manifest.desitvhub.eu.org)
- Proxy CDN routing (Dahmermovies via p.111477.xyz, UHDMovies via googleusercontent.com)
- Cloudflare Workers frontends (HDHub4u, Movies4u, MoviesHunt, VidLove, HindMoviez)
- No Playwright, no browser, no puppeteer — all pure JS

## Run the Test Suite

```bash
node test_nuvio_working.js
```

Tests all 27 providers against 4 content types (Movie, TV, Anime, K-Drama) with HTTP HEAD/GET stream verification.

## Notes

- All files are obfuscated (variable-name mangling) but functionally pure JS
- Only 2 dependencies: `cheerio-without-node-native` and `crypto-js`
- Each provider exports `getStreams(tmdbId, mediaType, season, episode)` returning a Promise of stream objects
- Some providers are movie-only (movies4u, movieshunt, uhdmovies for non-movie test)
- torrentio returns magnet links (requires a torrent client like Stremio's built-in torrent support)
