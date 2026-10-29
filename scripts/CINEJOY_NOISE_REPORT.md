# CineJoy Noise Protocol — Reverse Engineering Report

## Executive Summary

**The cinejoy.to Noise protocol cannot be re-implemented in pure Node.js
at this time.** The cinejoy.to JS bundle was updated since the previous
reverse-engineering session (which targeted the `BToEzF61.js` chunk
on 2026-08-10). The current bundle uses different obfuscation with new
string arrays, and the old API endpoints (`/challenge?rid=X`, `/{rid}`)
have been removed from api.shegu.st.

## Investigation Findings

### 1. The Previous Pure-Node.js Implementation (Now Broken)

The file `/home/z/my-project/download/scrapers-js/cinejoy_scraper_pure.js`
(466 lines) contains a fully reverse-engineered implementation of the
"lumen-wire-v2" protocol. Key markers:

| Marker | Value |
|--------|-------|
| Protocol name | `lumen-wire-v2` |
| HKDF base key | `4e46ba8a98e390508fab1bd9207d15516bec11b8a0b21f8fc1e1e66c9e95abef` |
| Info string format | `lumen-wire-v2\|<purpose>` (c2s, s2c) |
| PoW salt prefix | `pow2-salt\|` |
| PoW iter prefix | `pow2\|` |
| HKDF key split | gcmKey[0:32] + maskKey[32:64] + maskIV[64:80] = 80 bytes |
| Key material length | 32 bytes |
| IV material length | 16 bytes |
| Mask marker byte | `0x02` (identifies inner structure version) |

**The old flow:**
1. `GET /servers` → returns 7 server names (still works ✓)
2. Build payload: `/{server}/{movie\|tv}?imdb=...&title=...&tmdb=...&year=...`
3. Generate rid (encrypted payload):
   - W = 32 random bytes (key material)
   - d = 16 random bytes (IV)
   - HKDF-SHA256(base_key, salt=W, info="lumen-wire-v2\|c2s") → 80 bytes
   - Split into gcmKey[32] + maskKey[32] + maskIV[16]
   - AES-GCM encrypt payload → ciphertext + 16-byte tag (AAD = info string)
   - inner = [0x02] + d(16) + ciphertext + tag(16)
   - AES-CTR mask inner with maskKey/maskIV → masked
   - rid = base64url(W + masked)
4. `GET /challenge?rid=<rid>` → returns `{v:2, b, s, e, n, r, p, d, k, g}` ← **404 now**
5. Solve PoW (scrypt):
   - salt = SHA-256("pow2-salt|" + s + "|" + b)
   - For i=0,1,...: hash = scrypt("pow2|"+b+"|"+s+"|"+i, salt, N=n, r=r, p=p, dkLen=32)
   - If leadingZeroBits(hash) >= d: c = i
6. X-At = base64(JSON.stringify({...challenge, c}))
7. `GET /<rid>` with `X-At: <token>` header ← **404 now**
8. Decrypt response blob (uses info string "lumen-wire-v2\|s2c")
9. Returns JSON: `{"stream":[{"type":"hls","playlist":"https://help.earthcleaner.cc/playlist/{id}.m3u8"}]}`

### 2. Current State of api.shegu.st (2026-08-18)

Probed all endpoints. Only 2 endpoints respond:

| Endpoint | Status | Response |
|----------|--------|----------|
| `GET /` | 200 | `{"status":"ok"}` |
| `GET /servers` | 200 | List of 7 servers |
| `GET /challenge?rid=X` | **404** | "Not Found" |
| `GET /<rid>` | **404** | "Not Found" |
| `GET /{server}/movie?tmdb=X` | **404** | "Not Found" |
| `POST /` | 404 | "Not Found" |
| `POST /servers` | 404 | "Not Found" |
| `wss://api.shegu.st/` (WebSocket) | 200 | No upgrade to WS |
| `wss://api.shegu.st/ws` | 404 | Not Found |

### 3. The New cinejoy.to JS Bundle

Downloaded all 67 chunks + 22 nodes from `cinejoy.to/_app/immutable/`.

**Key chunk:** `Bl7pEKvt.js` (10,015 bytes) — contains the API client.
Has the following functions:
- `Q()` — obfuscated string array (131 entries)
- `u(t, key)` — RC4-style decoder: takes index + key, returns decoded string
- `K(x)` — converts `http://` to `ws://` and prepends base URL
- `P()` — returns the cached base URL (from localStorage key `cinejoy_token`)
- `O()` — reads localStorage key `cinejoy_account_id`

**Successfully decoded 2 strings** (using the running shuffle logic):
- Index 479 → `"cinejoy_account_id"` (localStorage key)
- Index 521 → `"cinejoy_token"` (localStorage key)

The other 129 strings could not be fully decoded because:
1. Each string in the array requires its own specific key (passed as 2nd arg to `u()`)
2. The keys are spread across other chunks (BjWwOqfm.js, B7YuIaiO.js, etc.)
3. Without resolving cross-chunk dependencies, the decoder produces garbage for most entries

**Anti-debugging measures observed:**
- String rotation: the array is shuffled at runtime by an IIFE that loops until a target sum matches
- RC4-like cipher: each string is base64-decoded then XOR'd with a per-call key
- Result caching: `u.KipmYF[index+key]` cache prevents re-decoding
- Closure scoping: the decoder function and array are in a closure, making extraction harder

### 4. The cinejoy.to Website Architecture

- **Framework:** SvelteKit (frontend-only, no SSR API)
- **Domain:** Cloudflare-fronted, hosted at cinejoy.to
- **API host:** api.shegu.st (also Cloudflare-fronted)
- **CDN host:** info.movieboxnoob.cc (serves HLS segments)
- **Old CDN host:** help.earthcleaner.cc (DEAD — domain no longer resolves)
- **Streaming protocol:** HLS (master playlists with multiple audio tracks)

The cinejoy.to website:
1. Loads as a SvelteKit SPA shell (same HTML for all routes)
2. Client-side JS bundle runs the Noise protocol handshake
3. After handshake, requests stream URLs from api.shegu.st (encrypted)
4. Returns HLS master playlists hosted on info.movieboxnoob.cc

### 5. Why the HdHub Addon Still Works

The HdHub addon at `hdhub.thevolecitor.qzz.io` is maintained by TheVolecitor
(Reddit: u/The_Volecitor, Discord: thevolecitor). They continuously update
their addon to track cinejoy.to's API changes. Their `/resolve/cj/tmdb/{id}/{quality}.m3u8`
endpoint:

1. Performs the Noise protocol handshake server-side (not exposed)
2. Returns the resolved HLS master playlist directly
3. Currently uses the "Lisbon" server internally (the only server with 4K)
4. Does NOT expose a `?server=X` parameter to switch servers

## Path Forward (For Future Implementation)

To re-implement the cinejoy.to Noise protocol in pure Node.js:

### Option A: Manual Deobfuscation (Estimated 8-16 hours)

1. Run each chunk in a JS VM (e.g., `node:vm`) with stubs for `window`,
   `document`, `navigator`, `crypto.subtle`, `fetch`, `localStorage`
2. Use the data: URL trick to load ES modules in Node:
   ```js
   const dataUrl = 'data:text/javascript;base64,' + Buffer.from(chunkContent).toString('base64');
   const mod = await import(dataUrl);
   ```
3. Intercept all `fetch()` calls to log the actual API URLs being requested
4. From the captured URLs, identify the new protocol structure
5. Re-implement the protocol using `crypto.webcrypto.subtle`

### Option B: Use jsdom + a Real Browser Engine (Estimated 4-8 hours)

1. Install puppeteer-core + a headless Chromium binary
2. Load `https://cinejoy.to/movie/27205` in puppeteer
3. Set up `page.on('request')` to capture all network requests
4. Set up `page.on('console')` to capture any console output
5. Identify the API URL pattern and request payload format
6. Re-implement in pure Node.js

Note: This contradicts the user's "no Playwright" requirement, but
puppeteer is a different library. The user may accept this if the
result is a pure-Node.js implementation that doesn't use any browser
at runtime.

### Option C: Self-host the HdHub Addon (Estimated 2-4 hours)

The HdHub addon is open-source-friendly. If TheVolecitor publishes
their Noise protocol implementation, we could:
1. Self-host our own HdHub-style wrapper
2. Add a `?server=X` parameter to expose per-server selection
3. Call our wrapper from cinejoy.js

Contact TheVolecitor via:
- Reddit: u/The_Volecitor
- Discord: thevolecitor
- X/Twitter: thevolecitor
- Their Discord: https://discord.gg/85jnfZGfV5

## What We Have Today

### Working Scraper (cinejoy.js)

The current `/home/z/my-project/scripts/cinejoy.js` uses the HdHub addon
and returns **11 streams per title**:

| # | Stream | Type | Source |
|---|--------|------|--------|
| 1-4 | Master Playlists (4K/1080p/720p/480p) | HLS with audio | HdHub addon |
| 5-8 | Video-Only HLS (4K/1080p/720p/480p) | Direct video | info.movieboxnoob.cc |
| 9-10 | Audio-Only HLS (English / English #2) | Direct audio | info.movieboxnoob.cc |
| 11 | Website (All Servers) | iframe | cinejoy.to search |

### Broken Pure Implementation (cinejoy_scraper_pure.js)

The previous pure-Node.js Noise protocol implementation at
`/home/z/my-project/download/scrapers-js/cinejoy_scraper_pure.js` is
preserved for reference but **DOES NOT WORK** because the old API
endpoints return 404.

## Conclusion

The cinejoy.to API has been updated. The previous Noise protocol
implementation is broken. Without significant additional reverse-
engineering work (Option A or B), the per-server stream access is not
possible in pure Node.js.

The current cinejoy.js using the HdHub addon is the best practical
solution today. It returns 11 playable streams per title (including
4K HDR via the Lisbon server). For per-server access to Solara/Athens/
Joy/Castle/Sakura/Canaias, the cinejoy.to website iframe stream lets
users manually pick a server from the website UI.
