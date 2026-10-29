# TENIES.SITE Addon — Working Sources Bundle

This bundle contains **31 working scraper files** including **2 new
streaming servers** (zstream.mov + enhanced cinejoy) that expand the
deployed TENIES.SITE addon.

## What's Inside

```
tenies-site-sources/
├── addon.js              ← Main aggregator + optional HTTP server
├── package.json          ← Dependencies
├── README.md             ← This file
├── test_sources.js       ← Test runner
├── scrapers/             ← 31 working scrapers
│   ├── nuvio_hdhub4u.js       ← MAIN: HubCloud + Google + direct CDNs
│   ├── nuvio_movies4u.js      ← Cloudflare R2 + Google + HLS
│   ├── nuvio_uhdmovies.js     ← Google CDN (4K available)
│   ├── nuvio_movieshunt.js    ← CF workers via abhilinks.site
│   ├── nuvio_4khdhub.js       ← 4K HDR via CF workers
│   ├── cinejoy.js              ← ENHANCED: 11 streams/title (master + video-only + audio + website)
│   ├── 1shows.js              ← Pixeldrain direct files
│   ├── zstream.js              ← NEW: zstream.mov iframe streams (4 URL variants)
│   └── ... 23 more bonus scrapers
└── lib/
    └── hub_extractor.js       ← HubCloud link resolver helper
```

## NEW: zstream.mov Support

**File:** `scrapers/zstream.js`

Returns 4 iframe streams per title pointing to zstream.mov's player:

| Stream | URL pattern |
|--------|-------------|
| Player Page | `https://zstream.mov/movie/{tmdbId}` |
| Watch Route | `https://zstream.mov/watch/movie/{tmdbId}` |
| Embed | `https://zstream.mov/embed/movie/{tmdbId}` |
| IMDB Route | `https://zstream.mov/movie/tt{imdbId}` |

### Why iframe (not direct video URL)?

zstream.mov uses **Cloudflare Turnstile** (sitekey `0x4AAAAAAB6ocCCpurfWRZyC`)
to gate access to its official backend at `api.theintrodb.org/v3`. The
Turnstile challenge **must be solved in a real browser** — pure Node.js
cannot solve it without Playwright/Selenium (which the user explicitly
excluded).

**Workflow when the user clicks the stream:**
1. Stremio opens the iframe → user's residential browser loads zstream.mov
2. The browser natively renders the Cloudflare Turnstile widget (no Playwright)
3. After the user solves the (often invisible) challenge, the page redirects
   to the configured backend URL (user must configure once during onboarding)
4. The stream loads — playback begins

For full source-list resolution, the user can install the **P-Stream**
Chrome extension (`geimjiobahmbkmaigaafjnoanebpmlmj`) or Firefox addon
(`p-stream-extension-v1`) — these intercept zstream.mov pages and resolve
streams natively.

## ENHANCED: CineJoy — All Playable Streams

**File:** `scrapers/cinejoy.js`

Now returns **11 streams per title** (up from 4):

| # | Stream | URL | Description |
|---|--------|-----|-------------|
| 1-4 | Master Playlists (4K/1080p/720p/480p) | `hdhub.thevolecitor.qzz.io/resolve/cj/tmdb/{id}/{quality}.m3u8` | Full HLS with video + all audio tracks |
| 5-8 | Video-Only HLS (4K/1080p/720p/480p) | `info.movieboxnoob.cc/video/{hash}/video_{quality}.m3u8` | Direct video stream (no audio) |
| 9-10 | Audio-Only HLS (English / English #2) | `info.movieboxnoob.cc/hls/{hash}/audio_{1,2}.m3u8` | Audio tracks extracted from master |
| 11 | Website (All Servers) | `cinejoy.to/search?q={title}+{year}` | Iframe to cinejoy.to search — user picks server manually |

### CineJoy's 7 Internal Servers

Per `api.shegu.st/servers`, CineJoy has 7 internal servers:

| Server | Region | 4K? | Notes |
|--------|--------|-----|-------|
| Lisbon | US | ✓ | Has 4K HEVC + HDR; default for the HdHub addon |
| Solara | US | ✗ | 1080p/720p/480p only |
| Athens | US | ✗ | 1080p/720p/480p only |
| Joy | US | ✗ | 1080p/720p/480p only |
| Castle | US | ✗ | 1080p/720p/480p only |
| Sakura | JP | ✗ | Japan-region content |
| Canaias | BR | ✗ | Brazil-region content |

The HdHub addon (`hdhub.thevolecitor.qzz.io/resolve/cj/`) uses **Lisbon
internally** — passing `?server=Solara` (or any other) is silently
ignored and always returns Lisbon's stream.

To access per-server streams directly, the cinejoy.to Noise protocol
handshake (ECDH P-256 + AES-GCM + HKDF-SHA256 + custom block cipher)
must be implemented in pure Node.js. The cinejoy.to JS bundle is heavily
obfuscated, so this is left as a future enhancement. The cinejoy.to
website iframe stream (#11) lets the user manually pick any of the 7
servers from the website UI.

## URL Pattern → Scraper Mapping

The deployed addon returns streams from these sites. Each one is mapped to a
specific scraper file:

| Site / Domain                     | Streams (avg) | Scraper file             |
|-----------------------------------|---------------|--------------------------|
| `hubcloud.cx` (pixel/gpdl2/gpdl)  | 102           | `nuvio_hdhub4u.js`       |
| `*.r2.cloudflarestorage.com`      | 58            | `nuvio_movies4u.js`      |
| `pixeldrain.dev`                  | 49            | `1shows.js`              |
| `*.r2.dev`                        | 37            | `nuvio_movies4u.js`      |
| `video-downloads.googleusercontent.com` | 30    | `nuvio_uhdmovies.js`     |
| `hdhub.thevolecitor.qzz.io`       | 27            | `cinejoy.js`             |
| `hubdrive.tips`                   | 12            | `nuvio_hdhub4u.js`       |
| `cdn.bunker.monster`              | 1             | `nuvio_hdhub4u.js` (direct CDN) |
| `cdn.pongala.life`               | 2             | `nuvio_hdhub4u.js` (direct CDN) |
| `cdn.lenin.buzz`                 | 1             | `nuvio_hdhub4u.js` (direct CDN) |

## Usage

### As a Library

```js
const { getStreams } = require('./addon');

// Get all streams for a movie
const streams = await getStreams('27205', 'movie');

// Get all streams for a TV episode
const streams = await getStreams('1396', 'tv', 1, 1);

// Get only core streams (skip bonus scrapers)
const streams = await getStreams('27205', 'movie', null, null, {
  includeExtended: false,
});
```

### As a Stremio Addon Server

```bash
npm install
npm start
# → TENIES.SITE Addon listening on port 7000
# → Manifest: http://localhost:7000/abckdhfik-34585674/manifest.json
```

Then in Stremio: **Add Addon → http://localhost:7000/abckdhfik-34585674/manifest.json**

### Deploy to Render

1. Create a new Render Web Service
2. Connect this directory
3. Build command: `npm install`
4. Start command: `npm start`
5. Environment variables (optional):
   - `PORT` — defaults to 7000
   - `CONFIG_KEY` — defaults to `abckdhfik-34585674` (change for security)

## Quality Distribution

Aggregated across all 30 scrapers:

- **2160p (4K)** — available via cinejoy, nuvio_hdhub4u, nuvio_4khdhub,
  nuvio_uhdmovies, nuvio_dahmermovies-4k, nuvio_hindmoviez
- **1080p** — available from all main scrapers
- **720p / 480p** — available from all main scrapers
- **HDR / HEVC** — cinejoy 4K HEVC, nuvio_4khdhub HDR

## Content Types

| Type    | Coverage                                            |
|---------|-----------------------------------------------------|
| Movies  | All 30 scrapers                                     |
| TV      | cinejoy, nuvio_hdhub4u, nuvio_uhdmovies, and 20+ more |
| Anime   | cinejoy, animezey, nuvio_castle, nuvio_persianstremio |
| K-Drama | nuvio_castle, nuvio_persianstremio, nuvio_torrentio |
| Multi-language (Hindi/Tamil/Telugu/Kannada/Persian) | nuvio_castle, nuvio_persianstremio |

## CF Bypass Mechanisms Used

All scrapers use pure Node.js — **NO Playwright, NO browser, NO FlareSolverr**:

- Pure `fetch()` with custom User-Agent strings
- `AbortController`/`AbortSignal.timeout()` to avoid hanging
- AES-CBC local decryption (castle, cineby)
- Multi-server fallback (videasy tries 10 servers)
- Alternate domain routing (vixsrc, desiflix)
- Proxy CDNs (dahmermovies via p.111477.xyz, uhdmovies via googleusercontent)
- HubCloud/VCloud link aggregation (hdhub4u, movieshunt)
- Cloudflare R2 + workers.dev distribution (movies4u, 4khdhub, hindmoviez)
- Magnet/torrent (torrentio — requires Stremio's torrent support)

## Test

```bash
node test_sources.js
```

Runs each scraper against 3 test titles (Inception movie, Breaking Bad S01E01,
Jujutsu Kaisen S01E01) and reports the count + URL patterns.

## Dependencies

Only 2 third-party packages needed:

- `cheerio-without-node-native` — for HTML parsing (used by hdhub4u, etc.)
- `crypto-js` — for AES decryption (used by castle, cineby)

Express + cors are needed only if you run `addon.js` as a server.

## Notes

- The `cinejoy.js` scraper uses the addon's `/resolve/cj/` endpoint on
  `hdhub.thevolecitor.qzz.io`, which performs the Noise-protocol handshake
  with api.shegu.st and returns valid HLS m3u8 playlists.
- The `zxcstream.js` scraper returns the player page URL as an iframe
  (the backend's `/backend/token` endpoint is server-IP blocked at the
  application layer, but works from the user's residential browser IP).
- The `animezey.js` scraper uses Cloudflare Worker rotation — if the primary
  worker hits a 429, it rotates to a fallback worker automatically.
- The `nuvio_torrentio.js` scraper returns magnet links (Stremio handles
  these natively via its torrent client).
