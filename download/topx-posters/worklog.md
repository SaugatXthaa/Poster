# TOPX Posters+ · Worklog

## Round 5 (2026-10-29) — Single-preview UI restore + PostersPlus completeness

### Goal
- User reported the deployed configurator on `topx-posters.onrender.com` felt
  cramped on mobile — the bottom sticky preview was eating ~24vh and showed the
  exact same poster/rank/shape pills as the top pin, leaving only ~144px of
  scroll room for the options on a phone.
- Restore the previous single-preview layout, keep every working feature
  (sticky top preview, AIOMetadata URL template, /poster + /logo endpoints,
  anime id resolution, TMDB type priority), and add every PostersPlus URL
  parameter that was in `DEFAULTS` but had no UI control.

### Layout — single sticky preview
- Removed the entire `.preview-stage.bottom-stage` HTML block and its CSS rule.
- Removed every JS reference to `Bottom` IDs: `stageBottom`, `previewFrameBottom`,
  `previewImgBottom`, `prevRankBottom`, `nextRankBottom`, `rankLabelBottom`,
  `previewMetaBottom`, `quickBgBottom`, `qbLandscape`, `qbPoster`.
- `bindQuickBg` no longer mirrors to a second select; `setShape` only toggles
  the top quick pills; `setPreviewSrc`/`renderPreview`/`loadPreviewMetas`
  only touch the top frame. Zoom opens from the top frame only.
- Top sticky preview is unchanged: rank nav, title/genre/rating, shape pills,
  bg dropdown, "CLICK POSTER TO ZOOM · LIVE /POSTER RENDER" hint.
- Mobile measurement (iPhone 14 viewport, agent-browser): scroll area grew
  from ~144px → 329px (2.3× more content visible), zero console errors,
  AIOMetadata URL still shows raw `{tmdb_id?}` / `{imdb_id?}` / `{id}` /
  `{type}` / `{shape}` / `{rank}` placeholders.

### New PostersPlus controls (17 params previously in DEFAULTS without UI)
| Section | Params added |
|---|---|
| Sash / Notch | `sash_chip_x`, `sash_chip_y`, `sash_edge_y`, `sash_badge_frost_opacity`, `sash_badge_frost_saturation` |
| Quality Badges | `badge_legacy_style`, `badge_quality_style`, `badge_logo_style`, `badge_cinema_style`, `badge_cert_style` |
| Trending Rank Display | `trending_sash` (keep/replace/hide), `trending_frost_saturation` |
| Landscape Badges (new section) | `landscape_badge_style`, `landscape_badge_shape`, `landscape_badge_case`, `landscape_badge_scale`, `landscape_score_out_of_10` toggle |
| Release Status | `cinema_greyscale_skip_if_available` toggle |

Coverage check: every key in `DEFAULTS` now has a corresponding UI control
(only `rating_display_mode` uses the `ratingModePills` `data-rm` pattern
instead of a `pp-` prefix). All 135 keys accounted for.

### Functional unchanged
- `buildPosterUrl()` still emits the PostersPlus AIOMetadata template form
  with raw placeholders (URLSearchParams encoding undone for `{`, `}`, `?`).
- `/poster` endpoint unchanged (TMDB type priority, AIOMetadata row-id
  unwrapping, anime `kitsu:`/`anilist:`/`mal:` resolution).
- `/logo` endpoint unchanged.
- All PostersPlus params ride the URL non-default-only (E) or always (A) using
  the same emission rules — new UI controls wire into the existing
  `state.pp[<key>]` pipeline via the standard `id="pp-<key>"` / `"pp-r-<key>"`
  / `"pp-t-<key>"` convention; no JS handler changes needed.

### Test results
```
manifest:                          200
UI:                                200
/poster (Inception concrete):      200 · 179144 bytes
/poster (template {tmdb_id?}):     400  (matches PostersPlus — can't render literal placeholder)
/poster (BB as movie, type prio):  200 · 206039 bytes
/poster (kitsu:7442 Cowboy Bebop): 200 · 207709 bytes
/logo (Inception):                 200 · 44237 bytes PNG
/poster (all 17 new params):       200 · 174967 bytes
                                  ────
                                  7/7 PASS (100%)
```

### Files changed this round
- `public/index.html` — bottom-stage removed; 17 new PostersPlus controls added
  across Sash / Quality Badges / Trending / Landscape Badges / Release Status.

### Pushed to GitHub
- Repo: https://github.com/SaugatXthaa/Poster (branch `main`)
- Commit `9cad20d` — fast-forward on top of `3119d2e` (Round 4)
- Verified via `https://raw.githubusercontent.com/SaugatXthaa/Poster/main/public/index.html`
  — byte-identical to local file (94735 bytes), contains the new controls,
  contains zero `bottom-stage` references.
- Token used one-time in push URL only; not persisted in `.git/config` and
  not present in the committed tree.

### How to use
1. Render auto-deploys on push to `main`; the live configurator at
   `topx-posters.onrender.com` now shows the single-preview layout.
2. Open the configurator — pick country + platforms + style. The top preview
   stays pinned while you scroll through Catalogs/Layout/Language/Style/Extras.
3. Switch to the **Extras** tab to find the new Sash chip/edge/frost controls,
  per-badge style selectors, Trending sash behaviour, and the Landscape Badges
  section. Switch to **Layout** for the new Landscape Badges block too.
4. The AIOMetadata URL box at the bottom is unchanged — paste it into
   AIOMetadata → Poster Providers as before.

---

## Round 4 (2026-10-29) — Render deployment + GitHub push

### Render readiness
- `render.yaml` Blueprint added: Docker runtime, `healthCheckPath: /manifest.json`,
  auto-generated stable `APP_SECRET`, `TMDB_API_KEY` prompt, commented persistent-disk block.
- `Dockerfile` verified: system fonts (fontconfig + TTF copy + fc-cache),
  python3/make/g++ for better-sqlite3 native build, `MALLOC_ARENA_MAX=2`, `DATA_DIR=/app/data`.
- `docker-compose.yml` healthcheck switched from wget (absent in bookworm-slim) to `node -e fetch(...)`.
- `.gitignore` extended: `data/`, `*.db*`, `.env`, `public/examples/` (unreferenced 5.5 MB sample renders) — lean clone for Render builds.
- README: new "Deploy to Render" section (Blueprint one-click + manual Docker runtime + free-tier notes: ephemeral cache, idle sleep, PORT binding).
- `preload.js` cron is NOT auto-activated (initScheduler imported but never invoked) — covers render lazily on first request; README updated to say so.

### Clean-clone verification (exact committed tree)
- `git archive HEAD` → fresh dir → `npm install` (115 pkgs) → boot on PORT=3472 with Render-style env →
  manifest 200 · UI 200 · /poster (Inception / rank+bebas / kitsu anime) 200 · /logo 200 ·
  token manifest + it/netflix catalog + top-cover chain 200.

### Pushed to GitHub
- Repo: https://github.com/SaugatXthaa/Poster (was empty; branch `main` created)
- Commit `3119d2e` — 106 files: source, public/{index.html,fonts,icons,networks},
  Dockerfile, render.yaml, docker-compose.yml, package-lock.json, README, .env.example
- NOT committed: node_modules/, data/, .env, public/examples/, worklog.md (kept local only)
- Token used one-time in push URL only; NOT persisted in .git/config and NOT present in the committed tree.

### Deploy on Render (user steps)
1. render.com dashboard → New → Blueprint → pick `SaugatXthaa/Poster`.
2. Paste a TMDB key for `TMDB_API_KEY` when prompted (free at themoviedb.org).
3. Deploy — configurator at `https://<service>.onrender.com/`, AIOMetadata poster URL ready from the UI.

---

## Round 3 (2026-10-29) — PostersPlus same-to-same integration

### Goal
- Sticky live preview above **and** below the 5 tabs (held in place while options scroll)
- Use **all** PostersPlus settings on top of topx/blvk; add extras not in either
- Keep the **PostersPlus poster URL format** so users can paste it directly into AIOMetadata poster providers
- End-to-end bug hunting, three-source same-to-same integration

### What was already in place (Rounds 1–2)
- 27 countries × 10 platforms matrix from TOPX/blvckTOP
- blvckTOP-style cover generator (Sharp + librsvg SVG composite)
- AES-256-GCM config token + dual Stremio/Nuvio manifest
- PostersPlus gold/black configurator with 5 tabs (Catalogs / Layout / Language / Style / Extras)
- 9-font registry, language-aware TMDB image selection, vignette pipeline
- `/poster` endpoint that accepted the PostersPlus URL surface

### What this round added / fixed
**1. UI — sticky preview verified**
- `.preview-stage` (top) + `.preview-stage.bottom-stage` (bottom) sit outside `.options-scroll`
- `body{overflow:hidden}` — only `.options-scroll` scrolls
- Verified by JS eval: scrolling options from y=0 → y=800 keeps `stageTop.y=38` and `stageBottom.y=566` unchanged
- Both stages refresh in place on every option change (live `/poster` re-render)

**2. AIOMetadata URL template — PostersPlus format match**
- `buildPosterUrl()` now emits `tmdb_id={tmdb_id?}&imdb_id={imdb_id?}&stremio_id={id}&type={type}&shape={shape}&rank={rank}` — exactly the form documented in PostersPlus `URL_REFERENCE.md § "Client templates"`
- `URLSearchParams` was URL-encoding the `{`, `}`, `?` chars; a final replace step decodes them back so the URL string matches PostersPlus verbatim

**3. `/poster` endpoint — full PostersPlus URL surface**
- AIOMetadata row-id unwrapping in `unwrapStremioId()`: `upnext_tt…`, `mdblist_upnext_tmdb:…`, `unwatched_tt…`, Stremio episode ids (`tt0903747:1:2`), `had:episode:tt…:6:6`
- Anime ID resolution (`kitsu:`, `anilist:`, `mal:`) via provider API → title → TMDB Animation search, cached per (provider, id)
- `resolveTmdbIdAndType()` returns both id and TMDB's actual type — when an IMDb id resolves to a TV show but the request said `type=movie`, **TMDB's type wins** (PostersPlus rule)
- Cache is now keyed per (id, type) with cross-type fallback so a wrong-type request finds the right title without re-query

**4. `/logo` endpoint — PostersPlus-compatible**
- `GET /logo?tmdb_id=…&type=…&lang=en` returns the best available title logo as PNG
- Uses `chooseLogoWithPriority()` (same priority/secondary scheme as `/poster`)
- Same anime ID + AIOMetadata row-id unwrapping as `/poster`
- Same type-mismatch handling (TMDB's type wins)

**5. Three-source integration (topx + blvk + PostersPlus)**
- TOPX: 27-country catalog matrix, multi-platform Top 10, country→language map
- blvckTOP: numbered cover generator, banner-style fresh background, language-aware artwork, provider palettes
- PostersPlus: URL format, parameter set (label_font, rating_display_mode, vignette_color_*, gradients, hide_*, sash_*, badge_*, trending_*, etc.), `/poster` + `/logo` endpoints, AIOMetadata placeholder semantics

### Test results (final verification)
```
1. Catalog pipeline (manifest → catalog → meta → top-cover): 13/13 PASS
2. /poster matrix (5 ids × 2 types × 2 shapes):               16/16 PASS
3. /logo endpoint:                                             4/4  PASS
4. AIOMetadata row id unwrapping:                              4/4  PASS
5. Stremio + Nuvio install URLs (canvas variants):             2/2  PASS
                                                            ──────
                                                            37/37 PASS (100%)

Extended /poster matrix (PostersPlus params + canvas × shape):
                                                            46/47 PASS
(The single 400 is the literal {tmdb_id?} placeholder URL — same
behavior as PostersPlus, which can't render a verbatim placeholder.)
```

### Files changed this round
- `tmdb.js` — `unwrapStremioId()`, `resolveAnimeId()`, `resolveTmdbIdAndType()`
- `server.js` — `/logo` endpoint; `/poster` uses `resolveTmdbIdAndType` + `effectiveType` for image fetch, details, coverKey
- `public/index.html` — `buildPosterUrl()` emits raw `{tmdb_id?}`/`{imdb_id?}`/`{id}`/`{type}`/`{shape}`/`{rank}` placeholders (PostersPlus verbatim form)

### How to use
1. `bash /home/z/my-project/scripts/topx-start.sh` (port 3471)
2. Open `http://localhost:3471/` — pick country + platforms + style
3. Click **Generate Manifest** → install Stremio or Nuvio from the deep links
4. The **AIOMetadata** URL box at the bottom is the PostersPlus-format poster URL — paste it into AIOMetadata → Poster Providers. AIOMetadata substitutes `{tmdb_id?}`, `{imdb_id?}`, `{id}`, `{type}`, `{shape}` per title, and your server renders each one.
5. Optional: also paste the matching `/logo?tmdb_id={tmdb_id?}&imdb_id={imdb_id?}&stremio_id={id}&type={type}` URL into AIOMetadata → Logo Providers.

## Round 7 — Locked topx preview strip in the ad area (commit b26ab7c, pushed)
User correction: not a floating preview — replace the announcement/ad area
(circle in IMG_5322.jpeg) with the topx live preview: options LEFT, locked
preview RIGHT, fast local rendering, nothing else changed.

- public/index.html: added .topx-strip (sticky, z-40) after <header> —
  left panel = Preview Options (Movies/Series catalog pills, rank nav
  prevRank/nextRank, 16:9/2:3 shape pills synced to tog-landscape, canvas
  bg fresh/provider/stremio/black/transparent, language select mirroring
  cfg-language 1:1, overlay toggles rank/meta/logo/original-art/crop/
  greyscale, 27-country grid, per-country platform chips, Generate
  Manifest + COPY + stremio:// nuvio:// deep links); right panel = the
  verbatim preview markup (same ids: preview-frame, preview-error,
  preview-actions, url-display-row) + topx meta caption + zoom overlay.
- Removed the floating right-column preview (.right-col); null-guarded
  previewPanel wiring (expand/collapse/placePreview/popstate/MQ) — the
  unguarded addEventListener killed the main script before COPY_TEMPLATES
  init (TDZ) and took the whole page down.
- New graft script: /api/countries + /api/platforms + /api/generate +
  /c/<token>/catalog/<type>/tx.<c>.<p>.<t>.json feed previewMetas (cached
  per country|platform|type); ranked meta sets resolvedTmdbId/mediaType +
  selected card + external links, then buildPreviewUrl() (full PP param
  set) + topx extras (country, canvas_background, rank, show_meta,
  show_logo, use_original_art, landscape_poster_crop, landscape_greyscale,
  pp_ts cache-buster) renders via /poster (sharp local, preview speed).
  queuePreviewReload overridden (all tab controls drive the new preview);
  loadPreview no-op'd; buildTemplateUrl wrapper adds the same extras with
  raw {rank} + decodePlaceholders idiom (%7B/%7D/%3F}) for AIOMetadata.
- Verified: zero console errors, movies+series, landscape 1280x720 /
  portrait 1000x1500, rank #1-#10 nav, Italy switch + platform chips,
  language strip<->Core sync (it), rating-mode change -> preview params,
  zoom + Escape, Copy config "Copied", manifest URL + install links,
  template placeholders all raw, mobile 390px (preview-first stack), light
  mode, sticky lock while scrolling. Remote diffed byte-for-byte.

## Round 8 — Elfhosted-style hero + Top X tab (commit 8707e62, pushed)
User: separate tab BEFORE Core for languages + charts; preview layout same-to-same
as the IMG_5325 example (two ranked posters top, MOVIES shape/canvas block, config below).

- public/index.html: .topx-strip (2-col sticky) replaced by .topx-hero —
  .topx-posters grid with card A (#preview-frame verbatim wiring, rank N)
  + card B (#tx-poster-b, rank N+1, own tmdb_id/type from the next meta so
  the row shows two DIFFERENT titles like the example); MOVIES block
  (label syncs MOVIES/SERIES with catalog; .tx-shape pills keep
  toggleShape() path; .topx-canvas-chips replace quickBg select, same
  TX.bg → build() + renderTxPreview()); verbatim output row (Copy config /
  Load preset / Reset config / url-display) stays under the hero; verbatim
  hidden controls preserved (tog-landscape, report-poster, artwork-edit-
  link, external-link, import-url-btn, preview-status, preview-error).
- New tab: data-tab="topx" first in the tab bar, .section[data-tab="topx"]
  first in #tab-host — Charts (tx-cat, rank nav, countryGrid 27,
  platformsGrid), Languages (tx-lang mirror + NEW tx-lang-secondary mirror
  of cfg-language-secondary via extended fillLangSelect), Overlays
  (tx-tog x6), Install (tx-generate + manifest box + deep links). All ids
  unchanged → handlers untouched; default tab still Core.
- txSwapSecondary() rides txSwapImage (debounced path included) and the
  renderTxPreview early-return; idempotent src compare; card B zoom.
- Verified in browser (fresh session, 0 console errors): both cards render
  rank1/rank2 different titles; shape pills flip BOTH cards
  1000x1500<->1280x720 + is-landscape container class; canvas chip provider
  -> both URLs canvas_background=provider; tx-lang it -> logo_language=it +
  cfg-language synced; rank nav A=#3/B=#4; Italy switch -> country=it,
  rank reset 1; manifest resolves (2 catalogs) + raw {rank}/{tmdb_id?}
  template with country/canvas, no %7B; desktop 1440 + mobile 390 two-col
  no h-scroll; remote diffed byte-for-byte.
