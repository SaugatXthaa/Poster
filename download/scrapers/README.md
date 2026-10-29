# PenguPlay Source Directory — Live Status + Scrapers

## Live Domain Status (verified 2026-08-10)

### ✅ LIVE & SCRAPABLE

| # | Provider | Working URL | Notes |
|---|---|---|---|
| 1 | **MovieBox** | `moviebox.ph` | Live |
| 2 | **VegaMovies** | `vegamovies.center`, `vegamovies.top`, `vega-max.com`, `vegamovies.archi` (insecure SSL) | 4 working mirrors |
| 3 | **VAPlayer** | `vaplayer.ru`, `vaplayer.xyz` | Both live |
| 4 | **ZXCStream** | `zxcstream.icu`, `zxcstream.xyz` | `.pro` has TLS issues; use `.icu` or `.xyz` |
| 5 | **Miruro** | `miruro.com` | `.to` and `.tv` are Cloudflare-locked |
| 6 | **HDGharTV** | **`hdghartv.cc`** (NOT .com.pk) | Real app lives at `.cc`; `.com.pk` is just an APK landing page |
| 7 | **VidLink** | `vidlink.pro` | Live |
| 8 | **VidFast** | `vidfast.vc` | `.pro` redirects here |
| 9 | **CineFreak** | `cinefreak.net` | Live |
| 10 | **VidKing** | `vidking.net` | Live (ddos-guard) |
| 11 | **Cinejoy** | `cinejoy.to` | Live (SvelteKit SPA) |
| 12 | **Antova** | `anilibria.top/api/v1/` | AniLibria API rebranded to "AniLiberty" |

### ❌ DEAD / BLOCKED

| Provider | Dead URLs | Status |
|---|---|---|
| **4KHDHub** | `4khdhub.click`, `.dad`, `.link` | All redirect to `4khdhub.one` which returns 403 Cloudflare |
| **MKVBase** | `mkvbase.site` | Cloudflare challenge — needs JS |
| **MoviesDrives** | `moviesdrives.my`, `.cv`, `.design`, `.pics`, `.in`, `moviesdrive.forum` | ALL DNS_DEAD |
| **VegaMovies** (some mirrors) | `vegmoviez.mobi`, `vegmovie.me`, `vegamovies5.com` | DNS_DEAD |
| **VixSrc** | `vixsrc.to` | Cloudflare bot management — needs JS challenge solver |
| **Miruro** | `miruro.to`, `miruro.tv` | Cloudflare challenge |
| **Cinejoy** (old API) | `api.anilibria.top`, `api.anilibria.tv` | DNS_DEAD / 410 Gone |

### 📌 Special cases

- **2Peckle**: No public website — wraps ShowBox. Must reverse-engineer ShowBox (cookie-quota system).
- **Antova**: No website — it's the AniLibria app. Use `anilibria.top/api/v1/` API.
- **HDGharTV**: `.com.pk` is just an APK landing page. **The real web app is `hdghartv.cc`** with a public REST API at `/api/*`.

---

## How to Scrape Antova (AniLibria)

### Architecture
- App name: Antova (iOS id6742210155, package `com.anilibriasio.anilyme`)
- Backend: AniLibria API → **moved to `https://anilibria.top/api/v1/`** (rebranded "AniLiberty")
- Old `api.anilibria.tv/v3/` returns **410 Gone**
- Content: Russian-dubbed anime, multi-audio, sub+dub, HLS

### Endpoints (all return JSON, NO AUTH required)

| Action | Endpoint |
|---|---|
| Search releases | `GET /app/search/releases?query={query}&limit=20` |
| Get release by alias | `GET /anime/releases/{alias}` (e.g. `berserk-2016`) |
| Get release by ID | `GET /anime/releases/{id}` |
| Latest releases | `GET /anime/releases/latest?limit=20` |
| Weekly schedule | `GET /anime/schedule/week` |
| Health check | `GET /app/status` |

### Stream URL pattern
```
https://cache.libria.fun/videos/media/ts/{releaseId}/{episodeOrdinal}/{quality}/{md5hash}.m3u8
  ?countryIso={XX}&isAuthorized=0&isWithVideoAds=1&isWithVideoAdsAlways=1
```

- `countryIso` is auto-injected by server based on your IP
- `isAuthorized=0` → ads prepended (set to `1` only with auth)
- HLS URLs are **inline in `episodes[]`** as `hls_480` / `hls_720` / `hls_1080` fields

### ⚠️ CRITICAL: NO MAL/AniList mapping
The new AniLiberty API **dropped** the `metadata.myanimelist_id` / `anilist_id` fields. You must **fuzzy-match by title** (use `rapidfuzz`).

### Release JSON schema (key fields)
```json
{
  "id": 2637,
  "alias": "berserk-2016",
  "name": {"main": "Берсерк", "english": "Berserk", "alternative": "..."},
  "year": 2016,
  "poster": {"src": "https://anilibria.top/storage/..."},
  "episodes_total": 12,
  "is_ongoing": false,
  "episodes": [
    {
      "id": "95b17860-...",
      "ordinal": 1,
      "sort_order": 1,
      "opening": {"start": 85, "stop": 171},
      "ending": {"start": null, "stop": null},
      "hls_480":  "https://cache.libria.fun/.../480/{hash}.m3u8?...",
      "hls_720":  "https://cache.libria.fun/.../720/{hash}.m3u8?...",
      "hls_1080": null,
      "duration": 1467
    }
  ],
  "torrents": [
    {"hash": "...", "magnet": "magnet:?xt=urn:btih:...", "size": ..., "quality": "1080p", "seeders": 12}
  ]
}
```

### Full working scraper → `antova_scraper.py` in this directory

---

## How to Scrape HDGharTV

### Architecture
- **Real web app: `https://hdghartv.cc`** (NOT `hdghartv.com.pk`)
- `hdghartv.com.pk` is just a static APK landing page that redirects desktop users to `.cc`
- Public REST API at `https://hdghartv.cc/api/*` — **NO AUTH REQUIRED**
- Video CDN: `cdn6.streamraiwind.stream` (token-signed URLs)
- Image CDN: `img1.streamraiwind.stream` (TMDB-style paths)

### Endpoints (all return JSON, NO AUTH)

| Action | Endpoint |
|---|---|
| Search movies + series + actors | `GET /api/search?q={query}` |
| Movie detail | `GET /api/movies/public/{_id}` (MongoDB ObjectId) |
| Series detail | `GET /api/series/public/{_id}` |
| Actor/cast detail | `GET /api/casts/public/{_id}` |
| Featured carousel | `GET /api/featured/public/{_id}` |

**⚠️ Note:** `GET /api/movies/public/{tmdbId}` returns 500. You MUST first call `/search?q={title}` to get the MongoDB `_id`, then call `/movies/public/{_id}`.

### Search response schema
```json
{
  "movies": [
    {
      "_id": "69ecd94ce651a6c8a546019a",   // MongoDB ObjectId — use this for detail
      "tmdbId": 693134,
      "title": "Dune: Part Two",
      "posterPath": "https://img1.streamraiwind.stream/t/p/w500/...",
      "type": "movie",
      "releaseDate": "2024-02-27"
    }
  ],
  "series": [...],
  "actors": [...],
  "total": 12,
  "combined": 15,
  "page": 1,
  "totalPages": 1
}
```

### Movie detail → `streamingLinks[]`
```json
{
  "title": "Dune: Part Two",
  "tmdbId": 693134,
  "streamingLinks": [
    {
      "quality": "1080p",
      "url": "https://cdn6.streamraiwind.stream/img/46d35c2d7120/nasty.m3u8?token=L6WqpublnRmpGkB_1Fyncg&expires=1786383178",
      "language": "",
      "type": "hls",
      "drm": false,
      "embed": false,
      "isActive": true
    },
    {"quality": "720p",  "url": "..."},
    {"quality": "480p",  "url": "..."}
  ]
}
```

### Series detail → `seasons[].episodes[].streamingLinks[]`
```json
{
  "title": "Breaking Bad",
  "seasons": [
    {
      "seasonNumber": 1,
      "episodes": [
        {
          "episodeNumber": 1,
          "streamingLinks": [
            {"quality": "1080p", "url": "https://cdn6.streamraiwind.stream/img/.../nasty.m3u8?token=...&expires=..."}
          ]
        }
      ]
    }
  ]
}
```

### Token expiry
- `expires` is a Unix timestamp (typically ~1 hour from issue)
- Token is bound to URL — play immediately after fetching, no special headers required
- No `Referer` or `Origin` needed on the m3u8

### Full working scraper → `hdghartv_scraper.py` in this directory

---

## How to Scrape Cinejoy

### Architecture
- Frontend: SvelteKit SPA at `cinejoy.to` (Cloudflare-protected, NO SSR data)
- Backend services at `*.shegu.st`:
  - `api.shegu.st` — main API (custom PoW-protected)
  - `subtitles.shegu.st` — subtitles (public)
  - `help.earthcleaner.cc` — HLS video CDN (Referer-gated)
  - `a.shegu.st` — Plausible analytics (NOT video)
- TMDB key baked into bundle: `8476a7ab80ad76f0936744df0430e67c`

### The CDN gate — fully cracked
`help.earthcleaner.cc` requires ONLY the `Referer: https://cinejoy.to/` header. No `Origin`, no `X-At`, no auth. Without it, returns HTTP 418 (honeypot).

### CDN URL conventions (content ID = numeric, e.g. `9937486`)
Split as `/000/99/37/9937486/` (leading zeros / thousands / hundreds / full ID):

| Resource | URL |
|---|---|
| Master playlist | `/playlist/{id}.m3u8` |
| Video variant | `/video/{id}/video_{1080p\|720p\|360p}.m3u8` |
| Video init segment | `/video/{id}/video_{quality}_init.html` ⚠️ `.html` extension (evasion) |
| Video segment | `/video/{id}/video_{quality}_{NNN}.html` |
| Audio playlist | `/hls/000/99/37/{id}/audio_1.m3u8` |
| Audio segment | `/hls/000/99/37/{id}/audio_1_{N}.html` |

### Master playlist (example: Dune Part Two, content ID 9937486)
```m3u8
#EXTM3U
#EXT-X-VERSION:7
#EXT-X-INDEPENDENT-SEGMENTS
#EXT-X-MEDIA:TYPE=AUDIO,LANGUAGE="eng",GROUP-ID="audio",NAME="English",
  DEFAULT=YES,AUTOSELECT=YES,
  URI="https://help.earthcleaner.cc/hls/000/99/37/9937486/audio_1.m3u8"
#EXT-X-STREAM-INF:DEFAULT=YES,BANDWIDTH=6000000,RESOLUTION=1920x1080,
  CODECS="avc1.4D0032,mp4a.40.2",AUDIO="audio"
https://help.earthcleaner.cc/video/9937486/video_1080p.m3u8
#EXT-X-STREAM-INF:BANDWIDTH=3000000,RESOLUTION=1280x720,...
https://help.earthcleaner.cc/video/9937486/video_720p.m3u8
#EXT-X-STREAM-INF:BANDWIDTH=900000,RESOLUTION=640x360,...
https://help.earthcleaner.cc/video/9937486/video_360p.m3u8
```

### The hard part: TMDB ID → content ID

The content ID (e.g. `9937486`) is **NOT** derivable from the TMDB ID (`693134`). It is returned **inside an encrypted blob** from `api.shegu.st/{rid}`, which requires:

1. **Generate `rid`** — 164-char base64url token (encrypted payload encoding `{tmdbId, server, expiry}`)
2. **GET `/challenge?rid={rid}`** → returns PoW challenge:
   ```json
   {
     "v": 2,
     "b": "33e242ed87f88cc6fef857c892179c5a",   // base hash
     "s": "dd800343dec0eee1c9c6b6507bc834c7",   // initial state
     "e": 1786376601,                            // expiry
     "n": 4096,                                  // difficulty (leading zero bits)
     "r": 8, "p": 1, "d": 5,                     // memory/CPU params
     "k": "951fef39a2b1e037",                    // encryption key (8 bytes hex)
     "g": "0397c79cf9901d7c..."                  // additional state
   }
   ```
3. **Solve PoW** — iterate nonces `c` until hash function has `n` leading zero bits. Build X-At token = `base64url(JSON{v, b, s', e, n, r, p, d, k, g', c})` where `s'` and `g'` are final hash states.
4. **GET `/api.shegu.st/{rid}` with `X-At: {token}` header** → 3298-char encrypted base64url blob
5. **Decrypt blob** with key `k` → returns JSON containing the content ID

### Two strategies

**Strategy A: Headless browser (RECOMMENDED)**
- Use Playwright to navigate to `https://cinejoy.to/watch/movie/{tmdbId}`
- Intercept the first `.m3u8` request to `help.earthcleaner.cc`
- Capture the URL (it contains the content ID)
- ~3-5 seconds per title

**Strategy B: Direct PoW solver (fast but fragile)**
- Reverse-engineer the obfuscated `+page.js` for `/watch/movie/[id]`
- Reimplement the rid generator + PoW solver + blob decryption in pure Python
- ~50ms per title
- BREAKS if cinejoy rotates keys or changes the algorithm

### Public endpoints (no PoW needed)

```bash
# Server list
curl -sS "https://api.shegu.st/servers"
# -> {"servers":[{"name":"Lisbon","status":"ok",...}, ...]}

# Subtitles
curl -sS "https://subtitles.shegu.st/subtitles?type=movie&tmdb=693134"
# -> {"total":277,"subtitles":[{"id","language":"ar","url","type":"srt",...}]}

# TMDB metadata (key baked into cinejoy bundle)
curl -sS "https://api.themoviedb.org/3/movie/693134?api_key=8476a7ab80ad76f0936744df0430e67c&append_to_response=credits,videos,external_ids"
```

### Full working scraper → `cinejoy_scraper.py` in this directory

The scraper ships with **Strategy A** (Playwright) fully working out of the box. **Strategy B** is stubbed — to enable it, you must deobfuscate `/watch/movie/[id]/+page.js` from cinejoy's bundle and port the rid/PoW/decrypt algorithms into Python.

---

## Stremio Addon Integration Example

```javascript
// stremio-addon.js — minimal addon consuming all three scrapers
const { addonBuilder, getInterface } = require('stremio-addon-sdk');
const { spawnSync } = require('child_process');

const builder = new addonBuilder({
  id: 'com.my.customscraper',
  version: '1.0.0',
  name: 'MyScraper',
  resources: ['stream'],
  types: ['movie', 'series'],
  idPrefixes: ['tt', 'tmdb:'],
  catalogs: []
});

async function callPython(scraper, args) {
  const r = spawnSync('python3', [`./scrapers/${scraper}.py`, ...args], {
    encoding: 'utf-8',
    timeout: 30000,
  });
  return JSON.parse(r.stdout || '[]');
}

builder.defineStreamHandler(async ({ type, id }) => {
  // id can be "tt15239678" or "tmdb:693134"
  const tmdb = id.startsWith('tmdb:') ? id.slice(5) : null;
  if (!tmdb) return { streams: [] };

  const isSeries = type === 'series';
  const [_, season, episode] = id.match(/:(\d+):(\d+)$/) || [];

  const streams = [];

  // --- HDGharTV (no auth, fast) ---
  try {
    const hd = await callPython('hdghartv_scraper', [
      isSeries ? 'series' : 'movie', tmdb, season || '', episode || ''
    ]);
    for (const s of hd) {
      streams.push({
        name: `HDGharTV\n${s.quality}`,
        title: s.title,
        url: s.url,
        behaviorHints: { notWebReady: false }
      });
    }
  } catch (e) { /* skip */ }

  // --- Cinejoy (browser strategy, slower) ---
  try {
    const cj = await callPython('cinejoy_scraper', [
      isSeries ? 'series' : 'movie', tmdb, season || '', episode || ''
    ]);
    for (const s of cj) {
      streams.push({
        name: `Cinejoy\n${s.quality}`,
        title: s.title,
        url: s.url,                       // needs Referer header
        behaviorHints: {
          notWebReady: true,
          headers: { Referer: 'https://cinejoy.to/' }
        }
      });
    }
  } catch (e) { /* skip */ }

  // --- Antova (anime only — match by title via TMDB lookup) ---
  // Skip if not anime (use TMDB genres to detect)

  return { streams };
});

getInterface(builder).serveHTTP({ port: 7000 });
```

### ⚠️ Cinejoy CDN requires Referer header on EVERY request

Stremio's official player sends `behaviorHints.headers` on the master playlist request, but **segment requests may not include them**. Two workarounds:

1. **Self-host a reverse proxy** that injects `Referer: https://cinejoy.to/` on every request:
   ```nginx
   location /cinejoy/ {
     proxy_pass https://help.earthcleaner.cc/;
     proxy_set_header Referer https://cinejoy.to/;
     proxy_set_header User-Agent "Mozilla/5.0 ...";
   }
   ```
   Then rewrite the playlist URLs to point at your proxy.

2. **Use a server-side HLS rewriter** (like HDHub's `/resolve/cj/seg/{base64}.m4s` pattern) that fetches upstream bytes with the correct headers and streams them back with `Access-Control-Allow-Origin: *`.

---

## Summary Table

| Provider | Difficulty | Strategy | Code |
|---|---|---|---|
| **Antova** (AniLibria) | 🟢 Easy | Direct JSON API | `antova_scraper.py` ✅ Complete |
| **HDGharTV** | 🟢 Easy | Direct JSON API | `hdghartv_scraper.py` ✅ Complete |
| **Cinejoy** | 🔴 Hard | Browser (works) / Direct PoW (stub) | `cinejoy_scraper.py` ⚠️ Browser strategy ready |

**Recommendation:** Start with Antova + HDGharTV — both work out of the box. For Cinejoy, use the Playwright strategy initially, and only invest in reverse-engineering the PoW algorithm if you need the speed boost.
