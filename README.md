# TOPX Posters — Combined Stremio / Nuvio Addon

**A unified Top 10 poster cover engine** that merges three sources:

| Source | Role |
|---|---|
| [**blvckTOP**](https://github.com/qwertyuiop8899/blvckTOP) | Node.js + Sharp cover generator (Fresh / Provider Glass / Stremio / Black / Transparent backgrounds; Landscape 1280×720 and Portrait 1000×1500 shapes; classic numbered overlay and banner styles). |
| [**PostersPlus**](https://github.com/UmbraProjects/PostersPlus) | Poster style/format vocabulary, country-aware language selection (22 languages mapped from 27 countries), title-logo overlay, genre+rating badges. |
| [**topx.stremio-italia.eu**](https://topx.stremio-italia.eu) | Source of the 27-country / 10-platform matrix (World + 26 countries × Netflix, Prime, Disney+, Apple TV+, HBO Max, Paramount+, Crunchyroll, Discovery+, RaiPlay, NOW). |

The result: **one addon that produces numbered Top 10 covers for any of 27 countries in the user's chosen language, in any of the 5 blvckTOP background styles, in either landscape or portrait shape** — and the two original "poster types" (blvckTOP's banner-vs-classic and PostersPlus's language/badge styling) coexist without conflict because they apply to orthogonal dimensions of the same render pipeline.

---

## Features

- **27 countries** — World, Italy, Algeria, Australia, Brazil, Canada, Croatia, France, Germany, Greece, Guatemala, Hungary, India, Indonesia, Ireland, Lebanon, Malaysia, Mexico, Morocco, Netherlands, Philippines, Portugal, Romania, Spain, Turkey, United Kingdom, United States.
- **10 streaming platforms** per country (RaiPlay is Italy-only; the other 9 are global).
- **5 background styles** (from blvckTOP) — `fresh` (full-bleed artwork with dark glass number + title logo), `provider` (Brand Glass tinted by platform accent), `stremio` (Stremio navy `#1a173e`), `black`, and `transparent`.
- **2 shapes** (from blvckTOP) — Landscape 1280×720 (for Continue Watching / 16:9 slots) and Portrait 1000×1500 (for vertical poster grids).
- **9 rank-number fonts** (PostersPlus-inspired) — Inter, Space Grotesk, Bebas Neue, Oswald, Montserrat, Manrope, Fira Sans, Roboto Condensed, Playfair Display. Installed system-wide so sharp's librsvg resolves them by name.
- **3 rating formats** (PostersPlus-inspired) — `★ star + number` (default), `number only` (drops the star), `hidden` (suppresses the rating entirely).
- **4 vignette intensities** (PostersPlus-inspired) — off / light / medium / strong. Darkens the top and bottom edges of the artwork card so logos and metadata read more clearly.
- **22 language mappings** (PostersPlus i18n approach) — each country is mapped to its primary TMDB language code so titles, genres, and posters come back in the user's language. e.g. Italy → `it`, Brazil → `pt-BR`, India → `hi`, Greece → `el`, Turkey → `tr`, Morocco/Algeria/Lebanon → `ar`.
- **Native language override** — the configurator's Language tab lets the user pick a different language than the country default (e.g. an Italian living in Germany can pick Italian titles while keeping the German Top 10 chart).
- **Logo priority selector** — Auto / Native first / English first / Textless only. Controls which TMDB logo language wins when multiple are available.
- **PostersPlus-style configurator UI** — gold-on-black theme with Cormorant Garamond serif headings + Inter mono labels, fine grain texture overlay, two-column grid (config | live preview), 5 tabs (Catalogs, Layout, Language, Style, Generate).
- **TMDB-driven Top 10** — uses `/trending/{media}/week` for World, `/discover/{media}?watch_region=…&with_watch_providers=…` for platform-specific charts, and `/{media}/popular?region=…` for country-only Top 10.
- **On-demand render + SQLite persistence** — IMDb→TMDB mappings, TMDB image sets, and rendered cover PNGs/JPEGs are all cached to disk under `data/` (0 ms on subsequent hits). An optional blvckTOP-style scheduler module (`preload.js`, daily 09:00 / 18:00 cron) ships in the repo and can be wired up for warm caches; on Render the lazy path is the right default.
- **Encrypted config tokens** — the user's selection is encrypted with AES-256-GCM (key derived from `APP_SECRET`) and embedded in the manifest URL, so no server-side session storage is required.

---

## Quick start

### 1. Install & run

```bash
cd topx-posters
npm install
# Required env (any of these methods):
export APP_SECRET="your-stable-secret-at-least-24-chars-long"
export TMDB_API_KEY="439c478a771f35c05022f9feabcca01c"   # or your own TMDB key
export PORT=3000
npm start
```

Or with Docker (recommended for production):

```bash
docker compose up -d --build
```

### 1b. Deploy to Render (free tier works)

The repo ships a [`render.yaml`](render.yaml) Blueprint and a Dockerfile tuned for Render (system fonts for librsvg, `sharp`/`better-sqlite3` native builds, `MALLOC_ARENA_MAX=2`).

**One-click (Blueprint):**
1. Push this repo to GitHub (it already is: `SaugatXthaa/Poster`).
2. In the Render dashboard: **New → Blueprint**, pick the `Poster` repo — Render reads `render.yaml` and creates the service.
3. When prompted, paste your TMDB key into the `TMDB_API_KEY` variable (a free key from [themoviedb.org](https://www.themoviedb.org/settings/api)). `APP_SECRET` is generated automatically and stays stable.

**Manual (Docker runtime):**
1. **New → Web Service**, connect the repo.
2. Runtime: **Docker** (auto-detected from the Dockerfile).
3. Health check path: `/manifest.json`.
4. Environment variables:
   | Key | Value |
   |---|---|
   | `APP_SECRET` | any stable secret ≥ 24 chars (**never change it later** — existing manifest tokens would stop decrypting) |
   | `TMDB_API_KEY` | your TMDB key |
   | `NODE_ENV` | `production` |

Notes:
- The app binds to Render's `PORT` automatically (`process.env.PORT || 3000`).
- Free plan has **no persistent disk** — the SQLite cache + rendered covers in `/app/data` are ephemeral and simply regenerate on demand after a restart. Attach a disk on `mountPath: /app/data` (paid plans) for instant cache hits; `render.yaml` shows the commented block.
- Free services sleep after ~15 min idle; the first poster after a wake-up takes a few seconds while sharp re-renders, then everything is disk-cached again.
- The configurator URL is your Render domain (e.g. `https://topx-posters.onrender.com/`) — the AIOMetadata poster URL it produces uses that same origin, so it works out of the box.

### 2. Open the configurator

Visit `http://localhost:3000/`. The layout is built for fast tuning: the **live preview is pinned below the 5 tabs and never moves** — only the options area scrolls, so you can sweep through settings and see the poster re-render in place, without scrolling up and down. The bottom bar (summary, Generate button, AIOMetadata URL) is fixed too.

Tabs:

1. **Catalogs tab** — click a country card (27 available, default = World 🌍), then toggle platforms (Netflix, Prime, Disney+, Apple TV+, HBO Max, Paramount+, Crunchyroll, Discovery+, RaiPlay [Italy only], NOW). The generic "Top X" chart is always included. Toggle **Show rank number** (blvckTOP) off for clean per-title posters.
2. **Layout tab** — pick **Shape** (Landscape 1280×720 / Portrait 1000×1500) and **Background** (Fresh / Provider Glass / Stremio Navy / Black / Transparent). Plus PostersPlus artwork toggles: **Use original art**, **Landscape poster crop**, **Greyscale**.
3. **Language tab** — **Native Language** + **Logo language** overrides, **Logo Priority** (8 PostersPlus orders), **Custom fallback language**, **Title's own language** (original_labels), and **Label font** (9 fonts, PostersPlus `label_font` values).
4. **Style tab** — **Rating display mode** (PostersPlus 1–4), **Text colour**, **Glow colour**, all six PostersPlus **font ratio** sliders, **Top/Bottom gradients** with opacity & height overrides, **Resolution**, **Fallback background style**.
5. **Extras tab** — PostersPlus passthrough settings encoded into the AIOMetadata URL: **Primary client**, **Sash/Notch** (mode, style, position), **Quality badges** (display mode, groups, min score, cert country), **Rating sources & weights** (IMDb/TMDB source, movie/TV weights), **Trending rank display**, **Logo sizing**, **Release status** toggles.

The preview renders through the exact same `/poster` endpoint AIOMetadata will call, so what you see is exactly what you get. Use `‹` `›` to walk through positions #1–10, click the poster to zoom.

### 3. Add to AIOMetadata (poster provider)

The **AIOMetadata** URL box in the bottom bar holds a PostersPlus-format template with `{tmdb_id}`, `{imdb_id}`, `{type}`, `{shape}`, `{rank}` placeholders. Paste it into AIOMetadata's poster provider field — AIOMetadata substitutes the placeholders per title. The URL works against this server *and* against any full PostersPlus instance (unsupported decoration params — sash, badges, weights — are simply ignored by this renderer and drawn by PostersPlus).

### 4. Add to Stremio / Nuvio

Click **Generate Manifest**, then the **Install in Stremio** or **Install in Nuvio** button (or copy the manifest URL). Catalogs appear with numbered Top 10 covers.

---

## Architecture

```
topx-posters/
├── server.js              ← Express app: 27-country × 10-platform matrix, /api/* endpoints, manifest + catalog + cover routes
├── cover-generator.js     ← (from blvckTOP, extended) Sharp renderer: 5 bgs × 2 shapes × 9 fonts × 3 rating formats × 4 vignette levels
├── preload.js             ← (from blvckTOP) optional daily 09:00 / 18:00 cron pre-renderer + cover key hashing (includes font/rating/vignette in key; not auto-activated — covers render lazily on first request)
├── db.js                  ← (from blvckTOP) better-sqlite3 cache (mappings / images / covers / JSON)
├── tmdb.js                ← (extended) country-aware image fetcher, 22 language mappings, country-aware artwork sorting
├── config-token.js        ← (from blvckTOP) AES-256-GCM config encryption
├── public/
│   ├── index.html         ← PostersPlus-styled configurator (5 tabs: Catalogs / Layout / Language / Style / Generate) + live preview
│   ├── fonts/             ← 9 TTF fonts (Inter, Space Grotesk, Bebas Neue, …) used by the rank number renderer
│   ├── icons/             ← 27 platform / brand SVGs (Netflix, Prime, Disney+, …)
│   └── networks/          ← 50+ studio/network SVGs (A24, Pixar, HBO, …) used by the classic style
└── data/                  ← SQLite DB + cached covers (auto-created, safe to delete)
```

### Request flow

```
User clicks "Install in Stremio"
  → stremio://host/c/<token>/manifest.json
    → server decrypts token, returns manifest with country-specific catalogs
      → Stremio fetches /c/<token>/catalog/movie/tx.it.topx.movie.json
        → server calls TMDB (Italian language) → returns 10 metas
          → for each meta, server builds /c/<token>/top-cover?rank=1&tmdbId=…&shape=…&canvasBackground=…
            → Stremio fetches the cover image
              → server checks SQLite cache → if hit, serve from disk (0 ms)
                → if miss, call createTopCover() → Sharp composites artwork + number + logo + badges
                  → save PNG/JPEG to disk, return image
```

### Country → language mapping (22 languages)

| Country | Lang | Country | Lang | Country | Lang |
|---|---|---|---|---|---|
| World | en | Italy | it | France | fr |
| Germany | de | Spain | es | Brazil | pt-BR |
| Portugal | pt | Greece | el | Turkey | tr |
| India | hi | Indonesia | id | Malaysia | ms |
| Hungary | hu | Romania | ro | Croatia | hr |
| Netherlands | nl | Morocco | ar | Algeria | ar |
| Lebanon | ar | Mexico | es | Guatemala | es |
| Philippines | en | Ireland | en | UK | en |
| US | en | Canada | en | Australia | en |

These map directly to TMDB `language=` and `include_image_language=` parameters, so:
- **Movie titles** come back in the user's language (`Odissea` instead of `Odyssey` for Italian users).
- **Genre names** are localized (e.g. `Sci-Fi` → `Fantascienza` for `it-IT`).
- **Posters/logos** are sorted with the user's language first, then English, then textless.

---

## API

| Method | Endpoint | Returns |
|---|---|---|
| `GET` | `/` | Configurator HTML |
| `GET` | `/manifest.json?country=it` | Default manifest (10 catalogs for the given country) |
| `GET` | `/c/:token/manifest.json` | User's encrypted manifest (catalogs shaped by their config) |
| `GET` | `/c/:token/catalog/:type/:catalogId.json` | Top 10 metas with numbered poster URLs |
| `GET` | `/c/:token/meta/:type/:id.json` | Single meta (TMDB details in the country's language) |
| `GET` | `/c/:token/top-cover?rank=1&tmdbId=…&shape=…&canvasBackground=…` | Rendered cover PNG/JPEG |
| `GET` | `/poster` | **PostersPlus-compatible poster render** (AIOMetadata poster provider). Accepts `tmdb_id`/`imdb_id`/`type`/`shape` + all PostersPlus params + TOPX extras (`rank`, `country`, `canvas_background`). |
| `GET` | `/api/countries` | List of 27 countries |
| `GET` | `/api/platforms/:country` | Platforms available in the country |
| `GET` | `/api/catalogs?country=it` | Catalog list (id/type/name) for the country |
| `POST` | `/api/generate` | Generate manifest URLs from a config payload |
| `GET` | `/api/stats` | Cache stats (covers, assets, mappings) |

### `POST /api/generate` body

Two modes are supported:

**Mode A — blvckTOP style (per-catalog options):**
```json
{
  "catalogs": [
    { "id": "tx.it.netflix.movie", "type": "movie", "name": "Netflix Top 10",
      "shape": "landscape", "canvasBackground": "fresh",
      "showMeta": true, "showLogo": true }
  ]
}
```

**Mode B — TOPX style (country + global options):**
```json
{
  "country": "it",
  "platforms": ["top10", "netflix", "disney"],
  "posterShape": "landscape",
  "canvasBackground": "fresh",
  "showMeta": true,
  "showLogo": true
}
```

Returns:
```json
{
  "ok": true,
  "manifestUrl": "http://host/c/<token>/manifest.json",
  "stremioManifestUrl": "http://host/c/<stremio-token>/manifest.json",
  "nuvioManifestUrl": "http://host/c/<nuvio-token>/manifest.json",
  "catalogCount": 6,
  "country": { "code": "it", "name": "Italy", "flag": "🇮🇹" },
  "posterShape": "landscape",
  "canvasBackground": "fresh"
}
```

---

## Why the two "poster types" don't conflict

The previous PostersPlus and blvckTOP plugins both produced "posters" but along **different axes**:

| Plugin | Axis | Options |
|---|---|---|
| PostersPlus | Language / region of artwork + ratings/awards badges | 33 languages, 4 rating styles, 12+ award sashes |
| blvckTOP | Visual layout / background / numbered overlay | 5 backgrounds × 2 shapes × 2 styles |

TOPX Posters combines them by:

1. **Taking blvckTOP's render pipeline as the visual layer** — `cover-generator.js` is unchanged. It still produces 5 backgrounds × 2 shapes × 2 styles exactly as in blvckTOP.
2. **Taking PostersPlus's i18n approach as the data layer** — `tmdb.js` is extended with the same country→language map that PostersPlus uses (mapped to 22 of PostersPlus's 33 languages — the other 11 are minor variants). The TMDB image fetcher now passes `include_image_language=<country-lang>,en,null` and the artwork sorter prefers the country's language first.
3. **Taking topx.stremio-italia.eu's country/platform matrix as the catalog layer** — `server.js` exposes 27 countries × 10 platforms and routes the country code through to both the catalog fetcher (so titles/genres come back in the right language) and the cover generator (so artwork/logos are sorted by the user's language).

So when an Italian user picks "Italy + Netflix + Fresh + Landscape", they get:
- The Italian-language TMDB chart for Netflix Italy (titles in Italian where available, e.g. `Odissea`)
- Italian-language posters/logos sorted first (then English, then textless)
- A blvckTOP Fresh cover with the rank number in dark glass + the Italian title logo

When a Brazilian user picks "Brazil + Disney+ + Provider Glass + Portrait", they get:
- The Brazilian Portuguese TMDB chart for Disney+ Brazil
- `pt-BR`-tagged posters sorted first
- A blvckTOP Provider Glass portrait cover with the rank number on a Disney+ purple-tinted glass background

No conflicts, no either/or — both "poster types" are simply different knobs on the same render pipeline.

---

## Configuration reference

| Env var | Default | Description |
|---|---|---|
| `PORT` | `3000` | HTTP port |
| `APP_SECRET` | (none, **required**) | Stable string ≥ 24 chars used to derive the AES-256-GCM key. **Never change this after users have generated manifests** — their tokens will become invalid. |
| `TMDB_API_KEY` | `ad0f7351455041d8c9c0d4370a4b5fa5` | TMDB API key. Override with your own to avoid rate limits. |
| `DATA_DIR` | `./data` | Where the SQLite DB + cached covers live. |
| `HTTPS_PROXY` / `HTTP_PROXY` | (none) | Outbound proxy (used by `undici`'s `ProxyAgent`). |

---

## License & credits

- Cover generator, scheduler, and DB layer: **blvckTOP** by [@qwertyuiop8899](https://github.com/qwertyuiop8899) — MIT.
- Country/language i18n approach: **PostersPlus** by [@UmbraProjects](https://github.com/UmbraProjects) — MIT.
- Country × platform matrix: inspired by **topx.stremio-italia.eu**.
- Combined plugin assembled by SaugatXthaa.
