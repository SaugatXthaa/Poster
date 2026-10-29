# CNCVerse 4K Plugins — Decompiled Java Sources

Reverse-engineered Java sources for the 5 confirmed 4K-supporting CNCVerse Cloudstream
plugins. Decompiled from `.cs3` bytecode using jadx v1.5.0.

## ⚠️ Why these can't be trivially ported to JavaScript

After full reverse-engineering, each plugin has a fundamental blocker that prevents
a working Node.js port that returns DIRECT playable streams:

### 1. DoFlixProvider (❌ upstream dead)
- **mainUrl**: `https://panel.watchkaroabhi.com`
- **apiKey**: `qNhKLJiZVyoKdi9NCQGz8CIGrpUijujE` (hardcoded, public)
- **API**: TMDB-style REST — `/api/3/movie/{id}/links`, `/api/3/tv/{id}/season/{s}/episode/{e}/links`
- **Headers required**: `User-Agent: dooflix`, `X-App-Version: 305`, `X-Package-Name: com.king.moja`
- **BLOCKER**: Domain is **PARKED by ParkLogic** (expired). Server returns "Redirecting..."
  HTML page with ad-blocker-detection JS, not JSON. Even with the magic headers, the
  upstream is dead.

### 2. MovieBoxProvider + MovieBoxProviderIN (❌ HMAC signatures)
- **mainUrl**: `https://api3.aoneroom.com` (404 on root)
- **API**: `https://apig.inmoviebox.com/wefeed-mobile-bff/...`
- **Secret keys** (decoded from base64):
  - secretKeyDefault: `76iRl07s0xSN9jqmEWAt79EBJZulIQIsV64FZr2O`
  - secretKeyAlt: `Xqn2nnO41/L92o1iuXhSLHTbXvY4Z5ZZ62m8mSLA`
- **Auth flow**:
  1. Generate deviceId (SecureRandom + brand model)
  2. Build canonical string from method + accept + contentType + url + body + timestamp
  3. HMAC-SHA256 with secretKey → base64 → `X-Tr-Signature`
  4. Fetch anonymous guest token from `/wefeed-mobile-bff/tab/ranking-list?...`
  5. Use guest token + signed headers for all subsequent API calls
- **BLOCKER**: jadx failed to fully decompile the loadLinks method (`Method not decompiled`).
  The HMAC signature logic involves `buildCanonicalString`, `generateXTrSignature`,
  `extractPolicyResource`, `generateXClientToken` — these have non-trivial implementations
  that are partially obfuscated.

### 3. PikashowProvider (❌ rotated secrets)
- **mainUrl**: `https://manoda.co`
- **API**: `/v1/api/videos` (with HMAC signature)
- **Secret keys**:
  - apiKey: `picashow-api-secret-key`
  - hmacSecret: `picashow-api-secret-2025`
- **Signature**: `HMAC-SHA256(hmacSecret, apiKey + ":" + timestamp)` → `X-Signature`
- **Headers**: `X-Timestamp`, `X-API-Key`, `X-Signature`
- **BLOCKER**: Tested live — server returns 401 "无效的API密钥或签名" (Invalid API key
  or signature). The hardcoded secrets are PLACEHOLDERS — the production server rotates
  them and the plugin fetches the current secret from a remote config at runtime (not
  visible in the bytecode).

### 4. MLSBDProvider (❌ Android WebView required)
- **mainUrl**: `https://mlsbd.co` (returns 403 to non-browser)
- **Multi-extractor architecture**:
  - GDFlix (gdflix.dev) — fetches latest URL from `https://raw.githubusercontent.com/SaurabhKaperwan/Utils/refs/heads/main/urls.json`
  - HubCloud (hubcloud.foo)
  - MultiCloudLinks (new2.multicloudlinks.com)
  - Gofile, Pixeldrain
- **Critical method**: `fetchViaWebView(url)` — uses Android WebView (main thread,
  Dispatchers.getMain()) to bypass Cloudflare challenges on the upstream file hosts.
- **BLOCKER**: Not reproducible in Node.js — requires actual Android WebView component
  + the bundled `libcloudflared.so` (37 MB ARM64 / 39 MB x86_64) in CNCVerse-Bridge.

## The actual upstream URL config

The MLSBD plugin's GDFlix sub-extractor fetches this config at runtime:
https://raw.githubusercontent.com/SaurabhKaperwan/Utils/refs/heads/main/urls.json

Contents (saved as `upstream-urls.json` here):

```json
{
  "4khdhub": "https://4khdhub.one",
  "bollyflix": "https://new.bollyflix.vote",
  "rtally": "https://rtally.lol",
  "hindmoviez": "https://hindmovie.dev",
  "moviesdrive": "https://new5.moviesdrive.christmas",
  "movies4u": "https://www.google.com",
  "multimovies": "https://multimovies.casa",
  "nfmirror": "https://tv.imgcdn.kim/newtv",
  "skymovies": "https://skymovieshd.band",
  "uhdmovies": "https://uhdmovies.my",
  "moviesmod": "https://moviesmod.ai.in",
  "topmovies": "https://moviesleech.club",
  "vegamovies": "https://vegamovies.gallery",
  "rogmovies": "https://rogmovies.best",     ← YES, Rogmovies IS here!
  "gdflix": "https://new4.gdflix.io",
  "hubcloud": "https://hubcloud.ist",
  "toonstream": "https://toonstream.us",
  "zinkmovies": "https://new4.zinkmovies.cc",
  "vcloud": "https://vcloud.fit",
  "dudefilms": "https://dudefilms.date",
  "m4ufree": "https://ww4.m4ufree.lat",
  "animedao": "https://anidao.to",
  "mlsbd": "https://mlsbd.co",
  "fibwatch": "https://fibwatch.art",
  "hdhub4u": "https://new1.hdhub4u.free"
}
```

## Why no working JS port exists

The fundamental architecture of all 5 plugins relies on EITHER:
1. **Android WebView + CloudflareKiller** — bypasses Cloudflare Turnstile on upstream file
   hosts (gdflix.dev, hubcloud.cx, vcloud.zip, mlsbd.co). Pure Node.js has no WebView.
2. **Rotated HMAC secret keys** — fetched at runtime from remote config (not in bytecode).
3. **Parked upstream domains** — DoFlixProvider's panel.watchkaroabhi.com is dead.

The Cloudstream `.cs3` plugin format is **fundamentally tied to the Android runtime**:
- It uses `OkHttpClient` with custom interceptors
- It uses `Dispatchers.getMain()` for WebView operations
- It uses `com.lagradost.cloudstream3.network.CloudflareKiller` (Android WebView-based)
- It uses the CNCVerse-Bridge's bundled `libcloudflared.so` (37 MB binary)

A pure Node.js port would require either:
1. **Self-hosting a FlareSolverr instance** to bypass Cloudflare on the file hosts
2. **Running the actual Android .cs3 plugin in a JVM** with cloudstream-api.jar (5.3 MB)
   + Android WebView emulation (not feasible)
3. **Using the user's existing pengu.uk/antarctica setup** — which already does this
   server-side via pengu.uk's CF-bypass infrastructure

## What's in this folder

- `MovieBoxProvider-java/` — full Kotlin-decompiled Java sources for MovieBoxProvider
- `MovieBoxProviderIN-java/` — same for the India variant
- `DoFlixProvider-java/` — same (upstream dead)
- `PikashowProvider-java/` — same (HMAC secrets are placeholders)
- `MLSBDProvider-java/` — same (requires Android WebView)
- `upstream-urls.json` — the remote URL config used by MLSBD's sub-extractors

## Reference: original .cs3 files

The original compiled .cs3 plugin files are at:
`/home/z/my-project/download/cncverse-4k-plugins/`

To re-decompile or modify:
```bash
# Decompile
/tmp/jadx/bin/jadx -d output_dir PluginName.cs3

# Inspect manifest.json
unzip -p PluginName.cs3 manifest.json | jq .
```
