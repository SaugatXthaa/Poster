# ShowBox TV Suite — 4 Providers, Movies/Shows/Anime/Dramas up to 4K

Pure Node.js — **no Playwright, no browser, no external tools**. Just `fetch()`.

## Files

| File | Provider | Token Env Var | Source API |
|---|---|---|---|
| `2peckle.js` | 2Peckle | `TWOPACKLE_TOKEN` | pengu.uk proxy → Febbox |
| `showbox.js` | ShowBox | `FEBBOX_TOKEN_1` | showbox.media → Febbox |
| `showbox2.js` | ShowBox 2 | `FEBBOX_TOKEN_2` | showbox.media → Febbox (2nd account) |
| `gramcinema.js` | GramCinema | `HASHHACKERS_TOKEN` | tga-hd.api.hashhackers.com |
| `test_showbox_sources.js` | Test runner | (all 4) | Verifies movies/shows/anime/dramas |
| `showbox_manifest.json` | Stremio/Nuvio manifest | — | Provider registry |

## Quick Start

```bash
# 1. Set tokens (any one enables its provider)
export TWOPACKLE_TOKEN=eyJhbGciOiJIUzI1NiJ9...      # pengu.uk auth token
export FEBBOX_TOKEN_1=eyJhbGciOiJIUzI1NiJ9...       # Febbox account #1
export FEBBOX_TOKEN_2=eyJhbGciOiJIUzI1NiJ9...       # Febbox account #2 (rotation)
export HASHHACKERS_TOKEN=your_bearer_token           # hashhackers API

# 2. Run the test suite (4 content types × 4 providers = 16 tests)
node test_showbox_sources.js
```

## Usage in Code

```javascript
const twoPeckle = require('./2peckle');
const showbox = require('./showbox');
const gramcinema = require('./gramcinema');

// Movies (TMDB ID)
const movieStreams = await twoPeckle.getStreams(693134, 'movie');        // Dune Part Two
const showStreams = await showbox.getStreams(82856, 'tv', 1, 1);         // Mandalorian S01E01
const animeStreams = await gramcinema.getStreams(95479, 'tv', 1, 1);     // Jujutsu Kaisen S01E01
const dramaStreams = await gramcinema.getStreams(86639, 'tv', 1, 1);     // Crash Landing on You S01E01 (K-drama)
```

## Content Type Support

All 4 providers accept these `mediaType` values:
- `'movie'` — Movies
- `'tv'` — TV shows
- `'series'` — Same as TV (alias)
- `'anime'` — Anime (treated as TV with season/episode)

For Asian dramas (Korean, Chinese, Thai, etc.), use `mediaType: 'tv'` with the appropriate TMDB ID — the providers will detect the language from the file name and tag it accordingly.

## Quality Support

All providers detect and return streams up to **2160p (4K)**:
- `2160p` / `4K` / `UHD`
- `1080p` / `FHD`
- `720p` / `HD`
- `480p` / `SD`

Streams are sorted with 4K first.

## Output Format (Stremio-compatible + enriched)

```javascript
{
  name: "2Peckle | 2160p | Hindi 🇮🇳",
  title: "🎬 Movie Name - (2024)\n💎 2160p | 🔊 Hindi | 💾 14.2 GB\n🎞️ MKV | ⛓️‍💥 2Peckle",
  url: "https://...",
  quality: "2160p",
  behaviorHints: {
    notWebReady: true,
    proxyHeaders: { request: { "Referer": "https://stremio-moviebox-1.onrender.com/" } }
  },
  meta: {
    provider: "2Peckle",
    source: "pengu.uk",
    quality: "2160p",
    language: ["hi"],            // ISO 639-1 codes array
    languageDisplay: "Hindi 🇮🇳", // Human-readable
    format: "MKV",
    size: "14.2 GB",
    audio: "Hindi",
    title: "Movie Name",
    year: "2024",
    mediaType: "movie",
    // For TV:
    season: 1,
    episode: 1,
  }
}
```

## Token Setup

### Febbox (for 2Peckle, ShowBox, ShowBox 2)
1. Register at `febbox.com` (Google OAuth — no email/password)
2. Open DevTools → Application → Cookies → `https://www.febbox.com`
3. Copy the `ui` cookie value (starts with `eyJ...`)
4. `export FEBBOX_TOKEN_1=eyJ...`
5. For rotation, register a 2nd account and `export FEBBOX_TOKEN_2=eyJ...`

### Hashhackers (for GramCinema)
1. Visit `bollywood.eu.org` and create an account
2. Get your Bearer token from the API
3. `export HASHHACKERS_TOKEN=your_token`

### 2Peckle (pengu.uk)
- Uses the same Febbox token: `export TWOPACKLE_TOKEN=eyJ...`

## Token Resolution Priority

Each provider resolves its token in this order:
1. **Environment variable** (recommended for server/PC)
2. **`global.SCRAPER_SETTINGS`** (Nuvio app integration)
3. **Local HTTP server** (Android Simple HTTP Server, for mobile)

## Differences from the Original Repo

These files are adapted from `https://github.com/D3adlyRocket/ShowboxTV.git`:

1. **Node.js compatible**: Uses `fetch()` directly (built into Node 18+)
2. **Env var token support**: Reads tokens from environment variables as the primary source
3. **AbortController timeouts**: All HTTP calls have proper timeouts
4. **Enriched metadata**: All streams include a `meta` object with provider/source/quality/language/format/size
5. **4K detection**: All providers detect and prioritize 2160p streams
6. **Better error handling**: Graceful `[]` return on any failure (no thrown errors)
7. **Multi-language detection**: Detects Hindi, Tamil, Telugu, Bengali, Korean, Japanese, Chinese, Thai, Spanish, French
8. **Sorted output**: Streams sorted with 4K first, then 1080p, 720p, 480p

## Test Runner

`test_showbox_sources.js` verifies all 4 providers against 4 content types:

| Test Case | TMDB ID | Type |
|---|---|---|
| Dune Part Two | 693134 | movie |
| Mandalorian S01E01 | 82856 | tv |
| Jujutsu Kaisen S01E01 | 95479 | tv (anime) |
| Crash Landing on You S01E01 | 86639 | tv (K-drama) |

Run with:
```bash
TWOPACKLE_TOKEN=... FEBBOX_TOKEN_1=... HASHHACKERS_TOKEN=... node test_showbox_sources.js
```

## License

For personal use only. Sources: https://github.com/D3adlyRocket/ShowboxTV.git
