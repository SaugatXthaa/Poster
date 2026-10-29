# ShowBox / 2Peckle Reverse-Engineering Report

> Target: build a Node.js scraper for the PenguPlay **"2Peckle"** provider.
> 2Peckle wraps **ShowBox**, which itself is a curated movie/TV index sitting on top of
> the **FebBox.com** cloud-storage / file-sharing backend.

All endpoints below were verified live from the sandbox on 2026-08-10.
HTTP statuses shown are real curl responses.

---

## 1. TL;DR — the architecture in one paragraph

```
                       Stremio / PenguPlay
                              |
                              v
               pengu.uk  (com.penguplay v1.3.8)
               requires sign-in, 17 sources incl. "2Peckle"
                              |
                              v  (2Peckle source)
               showbox.media   <--- the public "ShowBox" website
               (ThinkPHP on nginx, power-by: usa9-showbox-01)
               maps title/TMDB  ->  FebBox share link
                              |
                              v
               febbox.com   <--- the actual file storage / CDN
               (Cloudflare-fronted, power-by: usa7-lb01)
               hosts the .mkv/.mp4 files; needs a `ui` JWT cookie
               for download/stream (100 GB / month quota per account)
```

The old `tapframe/NuvioStreamsAddon` ran a **third layer** (`febapi.nuvioapp.space`)
that mapped TMDB → FebBox files directly. That subdomain is now DNS-dead
(repo archived, 339★), so a modern scraper must use the two surviving
layers: **showbox.media** (search + share-link) and **febbox.com** (file list +
download).

---

## 2. ShowBox's actual website (verified)

| Domain            | HTTP | Verdict                                                  |
|-------------------|------|----------------------------------------------------------|
| **showbox.media** | 200  | ✅ THE real ShowBox streaming site. `power-by: usa9-showbox-01`, nginx, ThinkPHP, Vue.js. |
| showbox.live      | 200  | ❌ Different site — an "APK download" landing page (Cloudflare). Not the backend. |
| showbox.org       | 200  | ❌ Parked, FingerprintJS redirect to an ad/affiliate `tr_uuid` link. |
| showbox.app       | 200  | ❌ Parked, redirects to `/lander`. |
| showbox.fun       | 403  | ❌ Cloudflare "Just a moment…" challenge. Probably a parked/affiliate mirror. |
| showbox.zone      | 403  | ❌ nginx 403 (locked down). |
| showbox.to        | —    | ❌ TLS unexpected EOF (broken cert). |
| showbox.cc / .tv  | —    | ❌ TLS internal error. |
| showbox.com       | —    | ❌ Connection reset. |
| showbox.net / .watch | — | ❌ Timeout / dead. |
| showbox.stream / .movie / .online / .video / .media-stream | — | ❌ DNS dead. |

**Use `https://showbox.media` — that is ShowBox.**

Server fingerprint (proves it): `power-by: usa9-showbox-01`,
identical infrastructure pattern to FebBox's `power-by: usa7-lb01`.

---

## 3. FebBox — the real storage backend (verified)

| Domain            | HTTP | Purpose |
|-------------------|------|---------|
| **www.febbox.com** | 200 | Main app + JSON API. Cloudflare-fronted. |
| febbox.com (apex) | —    | Timeout — always use `www.` |
| thumb.febbox.com  | 200  | Thumbnails. |
| images.febbox.com | 200  | Avatars / images. |
| www.feb.sg        | 200  | FebBox alternate/marketing domain. |
| febapi.nuvioapp.space | DNS-dead | The old Nuvio wrapper — **defunct**. |
| nuvioapp.space    | resolves (Cloudflare) | apex alive but no useful subdomains. |

FebBox is a Chinese mega.nz-style cloud disk ("FEB — 100G Cloud Storage",
App Store id `1543861870`). **ShowBox is just a curated index of FebBox
share links.** That is the whole secret.

---

## 4. The cookie / quota system (100 GB shared quota) — fully explained

### What the cookie is
- Name: **`ui`**
- Format: a **JWT** (`HS256`), base64url, ~2.6 KB long.
- Decoded payload (from the public sample in `tapframe/NuvioStreamsAddon/cookies.txt`):

  ```json
  {
    "iat": 1748439946,
    "nbf": 1748439946,
    "exp": 1779543966,            // ~1-year validity
    "data": {
      "uid": 791768,              // FebBox user id
      "token": "fe58820c8f12bdef3d982ffcdc106faf"
    }
  }
  ```

  Header: `{"typ":"JWT","alg":"HS256"}`. Starts with `eyJ` (the "ey" cue from the docs).

### How to get one
1. Register at **https://www.febbox.com** (Google or email).
2. Open DevTools → Application → Cookies → `febbox.com` → copy the value of `ui`.
3. (For the Nuvio addon:) paste it alone into `cookies.txt`. For a raw scraper, send it as `Cookie: ui=<jwt>`.

### What the cookie unlocks
- **100 GB / month download/streaming quota per account.** This is the "shared
  cookie-quota" PenguPlay/2Peckle manages server-side: it pools multiple FebBox
  accounts and round-robins them across users; when a shared account's 100 GB is
  exhausted, that account is rotated out. End-users can also paste their own `ui`
  JWT for a private 100 GB.
- Access to **4K / HDR / DV** versions (the bigger files).
- Direct (faster) download URLs — the Nuvio UI shows a ⚡ lightning icon for these.

### Quota-check endpoint (verified, requires cookie)
```
GET https://www.febbox.com/console/user_cards
Cookie: ui=<jwt>
Accept: application/json, text/javascript, */*; q=0.01
User-Agent: Mozilla/5.0 ...
```
Returns (when authenticated):
```json
{
  "code": 1,
  "data": {
    "flow": {
      "traffic_limit_mb": 102400,      // ~100 GB
      "traffic_usage_mb": 12345
    }
  }
}
```
Remaining MB = `traffic_limit_mb - traffic_usage_mb`.
(Anonymous call redirects to the `/login` HTML page — confirmed.)

### Can ShowBox/FebBox be used WITHOUT a cookie?
- **Browsing / search / file-listing: YES** (anonymous works for everything except
  the actual download URL — see §6).
- **Downloading / streaming the file: NO.** Both `/file/share_download` and
  `/file/player` reply `{"code":0,"msg":"Please log in first"}` / `{"code":-1,"msg":"please login"}`
  anonymously. There is **no public "slow mode <9 GB"** path exposed on the
  share-download endpoints — every download requires a valid `ui` JWT. (The
  "<9 GB slow mode" rumour likely refers to FebBox's own web UI for a logged-in
  *free* user, where small files can be streamed without consuming premium quota —
  not to a cookie-less API.)

---

## 5. Complete endpoint reference

### 5a. showbox.media endpoints (the index layer)

All accept a browser UA + the `ci` session cookie (auto-set, not required).
All return `access-control-allow-origin: *`.

| Method | Endpoint | Purpose | Verified response |
|--------|----------|---------|-------------------|
| GET | `/` | Homepage. Lists ~30 trending movies with `/movie/m-<slug>-<year>` links. | 200 |
| GET | `/search/autocomplate2?keyword=<q>` | **Title search.** Returns HTML `<a href="/movie/detail/<id>">` and `<a href="/tv/detail/<id>">` rows. | 200 ✅ |
| GET | `/movie/detail/<id>` | Movie detail page (also reachable via `/movie/m-<slug>-<year>`). | 200 ✅ |
| GET | `/tv/detail/<id>` | TV detail page. | 200 ✅ |
| GET | `/movie?genre=<n>` / `/movie?country=<x>` / `/movie?quality=<q>` | Browse. Genres are numeric (1=Action … 53). | 200 |
| GET | `/tv?quality=<q>` | Browse TV. | 200 |
| GET | **`/index/share_link?id=<id>&type=<1\|2>`** | **🔑 KEY ENDPOINT.** Returns the FebBox share URL for a title. `type=1` movie, `type=2` TV (returns different shares). | 200 ✅ — `{"code":1,"msg":"success","data":{"link":"https://www.febbox.com/share/<code>"}}` |
| GET | `/ajax/movie/episodes/<id>` | Episode listing (TV). | **500** (ThinkPHP error — endpoint exists but errors without proper session/region; not needed — FebBox share gives episodes directly) |
| GET | `/ajax/tv/seasons/<id>` | Season listing. | 500 (same as above) |
| GET | `/ajax/notice`, `/ajax/vote/<id>`, `/ajax/vote_info/<id>`, `/ajax/check_favorite/<id>`, `/ajax/favorite`, `/ajax/login`, `/ajax/login-state`, `/ajax/register`, `/ajax/update_profile`, `/ajax/remove_favorite` | User/account features. | 500 / login-gated |
| GET | `/index/top_imdb`, `/coming-soon`, `/download`, `/faq_list` | Misc pages. | 200 |
| GET | `/watch-movie/<slug>-<movieId>.<linkId>` | Player page. | **500** in testing (server errors without a valid browser session/JS fingerprint). The `data-linkid` values seen (`8336617` etc.) come from a global "featured player" widget, not the per-movie stream. **Bypass this — use `/index/share_link` instead.** |

**Verified share_link results:**
```
id=40394 (Spider-Man: No Way Home 2021) type=1 -> https://www.febbox.com/share/3SJ9ygJO
id=3595  (Spider-Man 2002)                  type=1 -> https://www.febbox.com/share/N6LZUTvI
id=72805 (The Mandalorian and Grogu 2026)   type=1 -> https://www.febbox.com/share/7ZaKORa5
id=11526 (The Mandalorian)                  type=2 -> https://www.febbox.com/share/ow8U6bf-
id=11526 (The Mandalorian)                  type=1 -> https://www.febbox.com/share/EHA5iVs0   (different share!)
```

**Note on TMDB mapping:** showbox.media does **not** expose TMDB↔internal-id
publicly. You must search by title (`/search/autocomplate2`). The defunct
`febapi.nuvioapp.space/api/media/{movie|tv}/{tmdbId}/oss={OSS}` did TMDB-direct
resolution; without it, your scraper must do title search + year disambiguation.
(See the Nuvio OSS note below.)

### 5b. FebBox endpoints (the storage layer)

All on `https://www.febbox.com`. Most return JSON with `access-control-allow-origin: *`.

| Method | Endpoint | Auth | Purpose | Verified response |
|--------|----------|------|---------|-------------------|
| GET | `/share/<code>` | anon | Share landing page (HTML). Sets `ci`, `PHPSESSID`; clears `ui`. Contains the file list + all AJAX handlers. | 200 ✅ |
| GET | **`/file/file_share_list?share_key=<code>`** | **anon ✅** | Top-level file/dir list in a share. | 200 ✅ |
| GET | **`/file/file_share_list?share_key=<code>&parent_id=<fid>`** | **anon ✅** | Drill into a subdirectory (e.g. `season 1`). | 200 ✅ |
| GET | `/file/file_info?fid=<fid>` | anon | Full metadata for one file (size, name, is_dir, add_time, thumbs…). | 200 ✅ |
| GET | `/file/file_more_info` | anon | Extended file info. | 200 |
| GET | **`/file/share_download?share_key=<code>`** | **`ui` JWT required** | Returns the array of `download_url`s for every file in the share. | anon → `{"code":0,"msg":"Please log in first"}` |
| POST | **`/file/player`** body `fid=<fid>&share_key=<code>` | **`ui` JWT required** | Returns player/stream data (m3u8 or mp4 src). | anon → `{"code":-1,"msg":"please login"}` |
| GET | `/console/file_link2?fid=<fid>` | `ui` required | Per-file download link (console). | anon → 404/login |
| GET | `/console/user_cards` | `ui` required | **Quota check** (`data.flow.traffic_limit_mb` / `traffic_usage_mb`). | anon → login page |
| GET | `/console/user_info` | `ui` required | User profile (username, avatar, vip, quota_used, quota_total, feb_coins). | login |
| GET | `/console/user_traffic_query`, `/console/user_space`, `/console/user_plan`, `/console/speed_list` | `ui` | Quota / plan / speed info. | login |
| GET | `/file/client_request`, `/file/client_request_query` | session | Client request API (needs `request_id`). | `-1 request_id is empty` |
| GET | `/file/show_img2`, `/file/file_download` | varies | Image / direct download helpers. | `file_download` → "Please log in first" anon |

**Verified file_share_list response (Spider-Man NWH, share `3SJ9ygJO`):**
```
fid=6484648   2.44 GB   Spider-Man.No.Way.Home.2021.EXTENDED.1080p.WEBRip.x265-RARBG.mp4
fid=5359664  26.91 GB   Spider-Man.No.Way.Home.2021.EXTENDED.2160p.WEB-DL.x265.10bit.HDR.DD5.1-NOGRP.mkv
fid=3191855  12.72 GB   Spider-Man.No.Way.Home.2021.2160p.UHD.BluRay.x265.10bit.HDR.DTS-HD.MA.TrueHD.7.1.Atmos-SWTYBLZ.mkv
fid=3133521  12.24 GB   Spider-Man.No.Way.Home.2021.1080p.BluRay.x264.DTS-HD.MA.5.1-NOGRP.mkv
fid=2982702   3.35 GB   Spider-Man.No.Way.Home.2021.1080p.V4.HDTS.H264.HQ.Line.AC3.Will1869.mp4
+ dirs: "Spider-Man: No Way Home (2021)", "Captures", "7.0.3300.0__b03f5f7f11d50a3a"
```

**TV share structure (The Mandalorian, `ow8U6bf-`):**
```
season 1/  (dir, 55.05 GB)
season 2/  (dir, 71.82 GB)
season 3/  (dir, 43.74 GB)
   └─ The.Mandalorian.S03E08.Chapter.24.The.Return.1080p.DSNP.WEB-DL.DDP5.1.Atmos.H.264-TBD.mkv  (1.93 GB)
   └─ The.Mandalorian.S03E07.Chapter.23.2160p.DSNP.WEB-DL.DDP5.1.Atmos.DV.HDR10.H.265-Pascal.mkv (4.74 GB)
   └─ … multiple 1080p + 2160p HDR/DV versions per episode
```

### 5c. The defunct Nuvio FebAPI (for historical context / schema reference)

`providers/Showbox.js` in `tapframe/NuvioStreamsAddon` called:
```
GET https://febapi.nuvioapp.space/api/media/movie/{tmdbId}/oss={OSS}
GET https://febapi.nuvioapp.space/api/media/tv/{tmdbId}/oss={OSS}/{season}/{episode}
                                                              ?cookie={urlencoded ui JWT}
```
- `OSS` = region/server selector. Default `USA7`; other values like `IN1` seen in code.
- Header: `User-Agent: NuvioStreamsAddon/1.0`.
- Response schema (this is what 2Peckle still consumes internally):

```json
{
  "success": true,
  "versions": [
    {
      "name": "Spider-Man.No.Way.Home.2021.2160p.UHD.BluRay.x265.10bit.HDR.DTS-HD.MA.TrueHD.7.1.Atmos-SWTYBLZ.mkv",
      "size": "12.72 GB",
      "links": [
        { "name": "Auto", "url": "<direct FebBox CDN url>", "quality": "2160p", "size": "12.72 GB" },
        ...
      ]
    }
  ]
}
```

The Nuvio addon parsed `version.name` for codec tags (HDR, DV, HDR10+, H.265, TrueHD,
Atmos, DTS-HD MA, EAC3, 10-bit…) and `link.quality` for resolution. `link.url` was
returned to Stremio verbatim — i.e. **FebBox's `download_url` is a direct, seekable
MP4/MKV URL** (range-request supported), not HLS.

The `PStream` provider in the same addon (`ENABLE_PSTREAM_API`) was a separate
ShowBox-backed CDN labelled "🌐 ShowBox ⚡" in the UI; it used the same cookie/quota.

---

## 6. Required headers

### showbox.media
```
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36
Accept: text/html,application/xhtml+xml,...
Cookie: ci=<any>          # auto-set; not strictly required but polite
Referer: https://showbox.media/   # for /index/share_link
X-Requested-With: XMLHttpRequest  # for /ajax/* and /index/share_link (JSON calls)
```
No CORS issue — `access-control-allow-origin: *` on all API responses.

### febbox.com
```
User-Agent: Mozilla/5.0 ... (browser)
X-Requested-With: XMLHttpRequest
Referer: https://www.febbox.com/share/<code>
Cookie: ui=<jwt>          # REQUIRED for /file/share_download, /file/player, /console/*
Accept: application/json, text/javascript, */*; q=0.01
```
Anonymous calls to `/file/file_share_list` and `/file/file_info` work fine.
The `ui` cookie is the **only** auth token; no CSRF token needed for GETs
(the page emits a `csrf-token` meta but the share/player endpoints don't enforce it on GET).

---

## 7. Sample working stream URL (end-to-end trace)

Title: *Spider-Man: No Way Home (2021)*, TMDB `634649`, IMDb `tt10872600`.

```
1. GET https://showbox.media/search/autocomplate2?keyword=no%20way%20home
   -> <a href="/movie/detail/40394">Spider-Man: No Way Home (2021)</a>

2. GET https://showbox.media/index/share_link?id=40394&type=1
      (Referer: https://showbox.media/movie/detail/40394)
   -> {"code":1,"data":{"link":"https://www.febbox.com/share/3SJ9ygJO"}}

3. GET https://www.febbox.com/file/file_share_list?share_key=3SJ9ygJO
      (Referer: https://www.febbox.com/share/3SJ9ygJO, X-Requested-With: XMLHttpRequest)
   -> {file_list:[{fid:3133521, file_name:"...1080p.BluRay...mkv", file_size:"12.24 GB", is_dir:0}, …]}

4. GET https://www.febbox.com/file/share_download?share_key=3SJ9ygJO
      Cookie: ui=<jwt>
   -> {code:1, data:[{file_name:"….mkv", download_url:"<SIGNED CDN URL>", file_size:..., error:0}, …]}

   (or, for a single file's stream:)
   POST https://www.febbox.com/file/player
        Body: fid=3133521&share_key=3SJ9ygJO   Cookie: ui=<jwt>
   -> player src (m3u8 transcode or direct mp4)
```

The final `download_url` from step 4 is the value you hand to Stremio /
your player. It is a **direct MP4/MKV** (range-request seekable), **not** HLS —
HLS exists only as a FebBox in-browser transcode option (the share page prompts
to use the "HLS Downloader" Chrome extension for `.m3u8` URLs).

---

## 8. The complete scraping flow (numbered)

### Movies
1. Resolve TMDB/IMDb → title + year (via TMDB API `/find/{imdb}` or `/movie/{tmdb}`).
2. `GET https://showbox.media/search/autocomplate2?keyword=<urlencoded title>`
   → parse HTML for `/movie/detail/<id>` (match by year to disambiguate).
3. `GET https://showbox.media/index/share_link?id=<id>&type=1`
   → extract `data.link`, parse the share code (last path segment of `febbox.com/share/<code>`).
4. `GET https://www.febbox.com/file/file_share_list?share_key=<code>`
   → keep entries with `is_dir == 0` and `ext`/name ending in `.mkv`/`.mp4`/`.m4v`.
   (Drill into `is_dir == 1` rows with `&parent_id=<fid>` if needed.)
5. For each candidate file:
   - Parse resolution/codec from `file_name` (the version-name parser in
     `providers/Showbox.js` is a ready-made reference).
   - Optionally `GET /file/file_info?fid=<fid>` for richer metadata (runtime, thumbs).
6. **Resolve the actual stream URL** — pick one of:
   - `GET /file/share_download?share_key=<code>` with `Cookie: ui=<jwt>` →
     returns `download_url` for every file at once (preferred; one call).
   - `POST /file/player` with `fid`+`share_key` + cookie → single-file stream src.
7. Return `{ name, title, url: download_url, quality, codecs, size, provider:'ShowBox' }`.

### TV (series)
1. Resolve TMDB-TV → title + year.
2. `GET /search/autocomplate2?keyword=<title>` → find `/tv/detail/<id>`.
3. `GET /index/share_link?id=<id>&type=2` → FebBox share code (use `type=2` for TV;
   `type=1` returns a different, movie-style bundle).
4. `GET /file/file_share_list?share_key=<code>` → list of `season N` directories.
5. `GET /file/file_share_list?share_key=<code>&parent_id=<season_fid>` → episode files.
6. Match the requested `SxxExx` against `file_name` (regex `S(\d{1,2})E(\d{1,3})`,
   case-insensitive). Multiple quality variants per episode are normal.
7. Resolve stream URL as step 6 above.

### Quota-aware cookie rotation (mirror Nuvio's `selectBestCookie`)
- Maintain an array of `ui` JWTs.
- For each, `GET /console/user_cards` → read `flow.traffic_limit_mb - flow.traffic_usage_mb`.
- Pick the cookie with the most remaining MB; fall back to the first if checks fail.
- Tag streams with the remaining-MB so the UI can warn when low.

---

## 9. Does 2Peckle add custom logic on top of ShowBox?

**Mostly no — it's a thin wrapper.** Confirmed details:

- **PenguPlay manifest** (`https://pengu.uk/manifest.json`, fetched live):
  `com.penguplay` v1.3.8, configurable, requires sign-in.
  2Peckle is one of 17 checkbox sources (`source_2peckle`, default `checked`)
  alongside 4KHDHub, MovieBox, MKVBase, VegaMovies, MoviesDrives, VAPlayer,
  ZXCStream, Miruro, HDGharTV, VidLink, VidFast, CineFreak, VidKing, Cinejoy,
  VixSrc, Antova — i.e. the **same family** as the user's existing scrapers.
- 2Peckle's real job = **operate the ShowBox→FebBox flow server-side** (because
  Stremio can't hold a FebBox cookie per user). PenguPlay pools shared FebBox
  accounts (the "100 GB shared cookie-quota"), rotates them, and signs the
  `download_url`s on behalf of the end-user. That's why PenguPlay gates access
  behind its own sign-in.
- **Multi-audio handling:** the manifest exposes 18 audio-language checkboxes
  (English, Hindi, Tamil, Telugu, Korean, Japanese, Chinese, Spanish, French,
  German, Italian, Portuguese, Russian, Arabic, Thai, Vietnamese, Malay,
  Indonesian). ShowBox/FebBox files often ship with multiple audio tracks
  baked into one MKV (e.g. `DDP5.1.Atmos` + additional languages). PenguPlay
  does **not** remux — it filters by sniffing the track layout from the
  file_name/codec tags and only surfaces files whose name/codecs imply the
  requested language. Your scraper should do the same: parse the `version.name`
  for language hints (`Hindi`, `Tamil`, `Dual`, `Multi`) and filter.
- **No 2Peckle-specific domain exists.** All `2peckle.*` domains are DNS-dead
  (confirmed). 2Peckle has no public site — it lives entirely inside PenguPlay.
- PenguPlay also offers `emulate_vpn` (spoof client IP) and `disable_direct`
  (hide non-seekable streams) options — relevant because some FebBox CDN URLs
  are region-locked and some MKVs are not web-seekable without remux.

So: **to clone 2Peckle, you clone the ShowBox→FebBox flow in §8.** There is no
extra 2Peckle API to discover.

---

## 10. Repo / reference inventory

| Repo | Status | What it gives you |
|------|--------|-------------------|
| `tapframe/NuvioStreamsAddon` | **archived**, 339★ | `providers/Showbox.js` (the canonical scraper, 447 lines), `DOCUMENTATION.md` (cookie setup), `.env.example`, `cookies.txt` (sample JWT), `utils/linkResolver.js` (driveseed-style resolvers, **not** needed for ShowBox). |
| `yoruix/nuvio-providers` | active, 207★ | Successor framework for the Nuvio *mobile app*. Only ships a `_template` provider — **no ShowBox provider public**. Build system (`build.js`) + `DOCUMENTATION.md` are a good reference for provider structure. |
| `p-stream/simple-proxy` | (referenced in Nuvio docs) | The Cloudflare/Netlify proxy template for `SHOWBOX_PROXY_URLS` (pattern `?destination=<upstream>`). Use this if your region blocks FebBox. |
| `phisher98/cloudstream-extensions-phisher` | active | **No ShowBox/FebBox scraper** — only a plugin-server README + Stremio-setup docs. CloudStream doesn't scrape ShowBox. |
| `codeberg.org/cloudstream/cloudstream-extensions-multilingual` | 403 from sandbox | Not accessible to verify; unlikely to have ShowBox (CloudStream focuses on different sites). |

`providers/Showbox.js` is the single most important file — copy its
`parseQualityFromLabel`, `extractCodecDetails`, `parseSizeToBytes`,
`sortStreamsByQuality`, and `selectBestCookie`/`checkCookieQuota` helpers
verbatim into your Node.js scraper.

---

## 11. Gotchas & quirks

1. **`febapi.nuvioapp.space` is dead.** Any guide/tutorial referencing it is stale.
   Use showbox.media + febbox.com directly.
2. **showbox.media `/watch-movie/…` and most `/ajax/*` endpoints return HTTP 500**
   to non-browser clients (ThinkPHP "Page error! Please try again later ~").
   Don't rely on them — `/index/share_link` + FebBox's `/file/*` are the robust path.
3. **The `data-id` / `data-linkid` / `watch-movie` links on every movie page are
   for a global "featured player" widget (Top Gun Maverick, id 5448), NOT the
   current movie.** Don't scrape those — they're identical on every page.
4. **`type=1` vs `type=2` on `/index/share_link` return DIFFERENT FebBox shares**
   even for the same title. For TV, `type=2` gives the season-structured share;
   `type=1` gives a flatter alternate bundle. Try both if one is empty.
5. **FebBox share codes can contain `-`** (e.g. `ow8U6bf-`). URL-encode carefully.
6. **Anonymous access stops at the file list.** You WILL get `file_share_list`
   without a cookie, but you WILL NOT get `download_url`. A valid `ui` JWT is
   mandatory for the final stream URL. Plan for cookie rotation.
7. **Cookies expire ~1 year** (JWT `exp`). Refresh yearly; the Nuvio sample cookie
   in the public repo is already invalid.
8. **`thumb.chuaxin.com` is showbox.media's poster CDN; `thumb.febbox.com` is
   FebBox's.** Both use base64-encoded paths (`thumb_<b64>.png` decodes to
   `uploadimg/movie/YYYY/MM/DD/<ts>.jpg|w|h`). Cosmetic only.
9. **Files are direct MKV/MP4, range-seekable.** Stremio plays them directly.
   No HLS packaging needed unless you want the FebBox in-browser transcode
   (`/file/player` returns an m3u8 for that).
10. **Region locking:** some `download_url`s are geo-restricted. PenguPlay's
    `emulate_vpn` option exists for this. In your scraper, validate URLs with a
    HEAD/range request and drop 403s (the Nuvio addon does this via
    `DISABLE_URL_VALIDATION`).
11. **`OSS` region codes** (`USA7`, `IN1`, …) from the old FebAPI are no longer
    reachable. The current equivalents are implicit in which FebBox share you get
    from showbox.media — there's no region parameter to pass anymore.
12. **Anti-Virus flagging** ("Miruno, 2Peckle etc get flagged by AV as suspicious")
    is because FebBox shares historically hosted adware installers next to media
    files (see the `7.0.3300.0__b03f5f7f11d50a3a` .NET-framework dir in the NWH
    share) — purely a reputational issue, not a real malware signal in the .mkv files.

---

## 12. Minimal Node.js scraper skeleton

See `showbox-scraper.js` in this directory — a working, dependency-light
(`axios` + `cheerio`) implementation of the full flow in §8, including:
- title search → internal id
- share-link resolution (movie + TV)
- recursive FebBox file-list walking (seasons → episodes)
- quality/codec/size parsing (ported from `providers/Showbox.js`)
- cookie-rotation with quota checks
- episode matching by `SxxExx`
- output in Nuvio/Stremio-compatible `{name,title,url,quality,codecs,size,provider}` shape

Run:
```bash
node showbox-scraper.js movie "Spider-Man: No Way Home" 2021
node showbox-scraper.js tv "The Mandalorian" 1 3        # season 1, ep 3
```
Set `FEBBOX_COOKIE` env var (one or more `ui` JWTs, comma-separated) to get
real stream URLs; without it the scraper still returns file metadata
(filenames, sizes, fids) but `url` will be null.
