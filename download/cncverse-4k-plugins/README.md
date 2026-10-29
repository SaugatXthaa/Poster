# CNCVerse 4K-Supporting Plugin Sources

These are Cloudstream `.cs3` plugin files extracted from the CNCVerse-Bridge
default repo (https://github.com/NivinCNC/CNCVerse-Cloud-Stream-Extension).
Each has been verified to contain the `Quality.P2160` enum constant in its
compiled bytecode — meaning the plugin actively labels streams as 4K when
the upstream source has 4K content available.

## Files (6 total)

| File | Site | Quality ladder | Special | Status |
|---|---|---|---|---|
| `MovieBoxProvider.cs3` (92 KB) | movie-box.co | 240p → 360p → 480p → 720p → 1080p → 1440p → **2160p** | Multi-language | ✅ 200 OK |
| `MovieBoxProviderIN.cs3` (90 KB) | movie-box.co (India) | Same ladder incl. **2160p** | India-localized homepage | ✅ 200 OK |
| `DoFlixProvider.cs3` (83 KB) | molop.art | 360p → 480p → 720p → 1080p → **2160p** | Movies + TV | ✅ 200 OK |
| `PikashowProvider.cs3` (80 KB) | manoda.co / samui390dod.com | 360p → 480p → 720p → 1080p → **2160p** | Movies + TV | ✅ 200 OK |
| `MLSBDProvider.cs3` (112 KB) | new2.multicloudlinks.com | Up to **2160p** | **HDR10 + HDR10+ + HDRIP** — only HDR plugin | ✅ 200 OK |
| `YomoviesProvider.cs3` (15 KB) | Hexated fork | (no 4K — included in case "Rogmovies" was a typo) | Hindi movies | — |

## About "Rogmovies"

Rogmovies is **NOT publicly available** in any Cloudstream plugin repo I checked:

- ❌ Not in CNCVerse-Cloud-Stream-Extension default repo (35 plugins — confirmed)
- ❌ Not in Hexated/cloudstream-extensions-hexated (64 plugins)
- ❌ Not in Hexated/cloudstream-extensions (37 plugins)
- ❌ Not in CloudStream-Extensions-Archive (GitHub)
- ❌ No standalone "Rogmovies" GitHub repo exists
- ❌ No cutt.ly short code for "Rogmovies" or "Rog"
- ❌ No py.md short code for "Rogmovies"

It's possible "Rogmovies" is:
1. A typo for "YomoviesProvider" (included above — but it has no 4K support)
2. A typo for "Movierulzhd" (Hexated repo, no 4K)
3. A private/internal plugin not publicly released
4. A planned future plugin name

## How to install these plugins

### In CNCVerse-Bridge (Android/Desktop)
1. Add the default CNCVerse repo if not already added:
   `https://raw.githubusercontent.com/NivinCNC/CNCVerse-Cloud-Stream-Extension/refs/heads/builds/CNC.json`
2. The 5 plugins above should appear in the Extensions screen
3. Tap "Install" on each — they'll be cached locally as `.cs3` files

### In Cloudstream app directly
1. Settings → Add repository → paste the repo URL above
2. Install each plugin from the Extensions screen

### Manually
The `.cs3` files in this folder are the same files CNCVerse-Bridge downloads.
You can copy them to your device's plugin cache folder:
- Android: `/data/data/com.cncverse.stremiobridge.android/cache/plugins/<name>.cs3`
- Desktop: `<app cache dir>/plugins/<name>.cs3`

## File format

`.cs3` files are ZIP archives containing:
- `manifest.json` — plugin metadata (name, version, pluginClassName)
- `classes.dex` — compiled Kotlin/Java Dalvik bytecode

To inspect: `unzip -l PluginName.cs3`
To decompile: use jadx or dex2jar on the `classes.dex` file inside.

## Quality enum constants found in each plugin

Extracted via `strings classes.dex | grep -oE 'P[0-9]+' | sort -u`:

| Plugin | Qualities |
|---|---|
| MovieBoxProvider | P4, P7, P22, P240, P360, P480, P720, P1080, P1440, **P2160** |
| MovieBoxProviderIN | P0, P4, P7, P22, P240, P360, P480, P720, P1080, P1440, **P2160** |
| DoFlixProvider | P2, P5, P7, P360, P480, P720, P1080, **P2160** |
| PikashowProvider | P7, P360, P480, P720, P1080, **P2160** |
| MLSBDProvider | P7, **P2160** + HDR10/HDR10+/HDRIP strings |
| YomoviesProvider | (no P constants) |

## Source

All files originally from:
`https://raw.githubusercontent.com/NivinCNC/CNCVerse-Cloud-Stream-Extension/builds/<PluginName>.cs3`
