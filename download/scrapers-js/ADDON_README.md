# Adult Streaming Hub — Stremio/Nuvio Addon

4-source adult streaming addon for Stremio and Nuvio. Each source has its own
named catalog with real working streams.

## Sources

| # | Source | Catalog Name | Content | Max Quality |
|---|--------|-------------|---------|-------------|
| 1 | xprimehub.bond | XPrimeHub | Movies, Web Series, Short Films | 1080p |
| 2 | damplips.com | Damplips | Adult Short Films / Clips | 720p |
| 3 | qorno.com | Qorno | Aggregator (23+ source sites) | 720p |
| 4 | fullporn.to | FullPorn | Full-Length Videos | 4K (2160p) |

## Quick Start

```bash
# Install dependencies
npm install express cors

# Start the addon
node addon.js
# OR
./start_addon.sh
```

The addon runs on `http://localhost:7000`.

## Add to Stremio

1. Open Stremio
2. Go to **Settings** → **Addons**
3. Click **Add Addon**
4. Enter: `http://localhost:7000/manifest.json`
5. Click **Install**

## Add to Nuvio

1. Open Nuvio
2. Go to **Settings** → **Plugins/Scrapers**
3. Add the manifest URL: `http://localhost:7000/manifest.json`

## Catalogs

Each source has two catalogs:
- **Browse** (e.g., "FullPorn Latest") — paginated list of latest videos
- **Search** (e.g., "FullPorn") — searchable catalog with genre filters

### Supported genres per catalog

**XPrimeHub**: all, movie, tv

**Damplips**: amateur-porn, anal-sex, asian-girls, babes, big-black-cocks, big-tits,
college-girls, creampie, foursome, girlfriends, girls-masturbate, hardcore, hot-girls,
hugecocks, latina-girls, lesbians, momdaughter, naked-girls, naked-women, sexy-girls,
teen-sex, threesome, young-girls

**Qorno**: japanese, blonde, teen, milf, anal, asian, ebony, latina, lesbian, threesome,
creampie, big-tits, cumshot, blowjob, handjob, masturbation, squirt, rough, amateur,
public, voyeur, bondage, femdom, spanking

**FullPorn**: latest-updates, most-popular, top-rated

## API Endpoints

| Endpoint | Description |
|----------|-------------|
| `GET /manifest.json` | Addon manifest |
| `GET /catalog/movie/:id.json` | Browse catalog (with `?skip=N` for pagination) |
| `GET /catalog/movie/:id.json?search=QUERY` | Search within a catalog |
| `GET /catalog/movie/:id.json?genre=GENRE` | Filter by genre/category |
| `GET /meta/movie/:id.json` | Get video metadata |
| `GET /stream/movie/:id.json` | Get stream URLs for a video |

## ID Format

Each video ID is prefixed with the source name:
- `xprimehub:{slug}` — XPrimeHub post slug
- `damplips:{postId}` — Damplips post ID
- `qorno:{publicId}` — Qorno video public ID
- `fullporn:{videoId}` — FullPorn video ID

## Stream Format

All streams are direct MP4 (range-seekable, no HLS):
- **XPrimeHub**: Google CDN MKV (via nexdrive.fit → fastdl.zip chain)
- **Damplips**: video.damplips.com MP4 (direct CDN)
- **Qorno**: Varies by source (xtits, zbporn, drtuber, eporner, etc.)
- **FullPorn**: fullporn.to/get_file/ MP4 (302 redirect to CDN, up to 4K)

## How It Works

### XPrimeHub
1. Fetches sitemap (6300+ posts) → parses URL slugs for title/year/quality/language
2. Fetches post page → extracts nexdrive.fit download link
3. Fetches nexdrive.fit page → extracts fastdl.zip embed link
4. Fetches fastdl.zip embed → extracts Google CDN URL from `var reurl`
5. Returns direct MKV stream from Google CDN

### Damplips
1. Fetches pages via web.archive.org (main site is CF-protected)
2. Parses `<article>` blocks for video source URLs
3. Returns direct MP4 from video.damplips.com (no CF, no auth)

### Qorno
1. Fetches category/search pages using curl (bypasses CF TLS fingerprinting)
2. Parses `<div class="card sub" data-public-id="...">` blocks
3. Decodes `/out/?l={base64}` redirect links to get source URLs
4. Extracts direct MP4 from source sites (xtits, zbporn, drtuber, eporner)

### FullPorn
1. Fetches listing pages using curl (bypasses CF TLS fingerprinting)
2. Parses video cards for IDs, titles, thumbnails, durations
3. Fetches video page → extracts `/get_file/` URLs (2160p, 720p, 480p)
4. Returns direct MP4 (302 redirect to CDN, range-supported)

## Technical Notes

- **Pure Node.js** — no Playwright, no browser, no external tools (except `curl` which is pre-installed)
- **CF Bypass**: Qorno and FullPorn use `curl` via `child_process.execSync` to bypass
  Cloudflare TLS fingerprinting (Node.js `fetch()` gets 403, curl gets 200)
- **Damplips** uses web.archive.org as a proxy (main site is CF-protected)
- **XPrimeHub** sitemap is CF-free, only the search is CF-protected
- **Caching**: Catalogs are cached for 6 hours to avoid re-fetching

## Files

| File | Description |
|------|-------------|
| `addon.js` | Main addon server (Express) |
| `nuvio_xprimehub.js` | XPrimeHub scraper |
| `nuvio_damplips.js` | Damplips scraper |
| `nuvio_qorno.js` | Qorno scraper |
| `nuvio_fullporn.js` | FullPorn scraper |
| `start_addon.sh` | Startup script |
| `package.addon.json` | Package.json for the addon |

## License

For personal use only.
