/**
 * TOPX + PostersPlus + blvckTOP — Combined Stremio Addon
 * ========================================================
 * Multi-country Top 10 charts with numbered poster covers.
 * Supports 27 countries × 10 streaming platforms × multiple poster styles.
 *
 * Built by combining:
 *   - blvckTOP (Node.js/Sharp cover generator)
 *   - PostersPlus (poster style/format options)
 *   - TOPX (multi-country, multi-platform support)
 *
 * Config token encodes: country, platforms, poster shape, style, background
 */
import express from "express";
import path from "node:path";
import { fileURLToPath } from "node:url";
import {
  encryptConfig,
  decryptConfig,
  shortConfigId
} from "./config-token.js";
import {
  getTmdbImages,
  getTmdbDetails,
  chooseBackdrop,
  choosePoster,
  chooseBannerArtwork,
  chooseLogo,
  chooseLogoWithPriority,
  choosePosterPick,
  chooseTextlessPoster,
  chooseTextlessBackdrop,
  resolveTmdbId,
  resolveTmdbIdAndType,
  langForCountry,
  DEFAULT_TMDB_KEY
} from "./tmdb.js";
import {
  getCachedJson,
  setCachedJson,
  getCoverFilePath,
  saveCoverBuffer,
  getDbStats
} from "./db.js";
import { createTopCover, normalizeStyle, normalizeFontFamily } from "./cover-generator.js";
import { initScheduler, computeCoverKey, FRESH_VERSION } from "./preload.js";

const app = express();
const PORT = Number(process.env.PORT || 3000);
const TMDB_KEY = process.env.TMDB_KEY || DEFAULT_TMDB_KEY;
const APP_SECRET = process.env.APP_SECRET || "topx-posters-combined-secret-key-2026";

if (!APP_SECRET || APP_SECRET.length < 24) {
  throw new Error("APP_SECRET non configurato o troppo corto (minimo 24 caratteri).");
}

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

app.set("trust proxy", true);
app.use((_req, res, next) => {
  res.setHeader("Access-Control-Allow-Origin", "*");
  res.setHeader("Access-Control-Allow-Headers", "*");
  res.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
  next();
});
app.use(express.json({ limit: "32kb" }));
app.use(express.static(path.join(__dirname, "public"), {
  setHeaders: (res, filePath) => {
    if (filePath.endsWith(".html")) {
      res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
      res.setHeader("Pragma", "no-cache");
      res.setHeader("Expires", "0");
    }
  }
}));

const pendingCovers = new Map();
const JSON_TTL = 15 * 60 * 1000;
const coverMime = buffer =>
  buffer[0] === 0xff && buffer[1] === 0xd8 ? "image/jpeg" : "image/png";
const UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";

/* ─── Country data (27 countries from TOPX) ──────────────────────────────── */
const COUNTRIES = [
  { code: "world", iso: "WORLD", name: "World", nativeName: "Mondo (Global)", flag: "🌍", isWorld: true },
  { code: "it", iso: "IT", name: "Italy", nativeName: "Italia", flag: "🇮🇹" },
  { code: "dz", iso: "DZ", name: "Algeria", nativeName: "الجزائر", flag: "🇩🇿" },
  { code: "au", iso: "AU", name: "Australia", nativeName: "Australia", flag: "🇦🇺" },
  { code: "br", iso: "BR", name: "Brazil", nativeName: "Brasil", flag: "🇧🇷" },
  { code: "ca", iso: "CA", name: "Canada", nativeName: "Canada", flag: "🇨🇦" },
  { code: "hr", iso: "HR", name: "Croatia", nativeName: "Hrvatska", flag: "🇭🇷" },
  { code: "fr", iso: "FR", name: "France", nativeName: "France", flag: "🇫🇷" },
  { code: "de", iso: "DE", name: "Germany", nativeName: "Deutschland", flag: "🇩🇪" },
  { code: "gr", iso: "GR", name: "Greece", nativeName: "Ελλάδα", flag: "🇬🇷" },
  { code: "gt", iso: "GT", name: "Guatemala", nativeName: "Guatemala", flag: "🇬🇹" },
  { code: "hu", iso: "HU", name: "Hungary", nativeName: "Magyarország", flag: "🇭🇺" },
  { code: "in", iso: "IN", name: "India", nativeName: "भारत", flag: "🇮🇳" },
  { code: "id", iso: "ID", name: "Indonesia", nativeName: "Indonesia", flag: "🇮🇩" },
  { code: "ie", iso: "IE", name: "Ireland", nativeName: "Éire", flag: "🇮🇪" },
  { code: "lb", iso: "LB", name: "Lebanon", nativeName: "لبنان", flag: "🇱🇧" },
  { code: "my", iso: "MY", name: "Malaysia", nativeName: "Malaysia", flag: "🇲🇾" },
  { code: "mx", iso: "MX", name: "Mexico", nativeName: "México", flag: "🇲🇽" },
  { code: "ma", iso: "MA", name: "Morocco", nativeName: "المغرب", flag: "🇲🇦" },
  { code: "nl", iso: "NL", name: "Netherlands", nativeName: "Nederland", flag: "🇳🇱" },
  { code: "ph", iso: "PH", name: "Philippines", nativeName: "Pilipinas", flag: "🇵🇭" },
  { code: "pt", iso: "PT", name: "Portugal", nativeName: "Portugal", flag: "🇵🇹" },
  { code: "ro", iso: "RO", name: "Romania", nativeName: "România", flag: "🇷🇴" },
  { code: "es", iso: "ES", name: "Spain", nativeName: "España", flag: "🇪🇸" },
  { code: "tr", iso: "TR", name: "Turkey", nativeName: "Türkiye", flag: "🇹🇷" },
  { code: "gb", iso: "GB", name: "United Kingdom", nativeName: "United Kingdom", flag: "🇬🇧" },
  { code: "us", iso: "US", name: "United States", nativeName: "United States", flag: "🇺🇸" },
];

/* ─── Streaming platforms per country ────────────────────────────────────── */
const PLATFORMS = [
  { code: "netflix", name: "Netflix", accent: "#E50914" },
  { code: "amazon-prime", name: "Amazon Prime Video", accent: "#00A8E1" },
  { code: "disney", name: "Disney+", accent: "#2D7DFF" },
  { code: "apple-tv", name: "Apple TV+", accent: "#D8DFEA" },
  { code: "hbo-max", name: "HBO Max", accent: "#7D57FF" },
  { code: "paramount-plus", name: "Paramount+", accent: "#0064FF" },
  { code: "crunchyroll", name: "Crunchyroll", accent: "#F47521" },
  { code: "discovery-plus", name: "Discovery+", accent: "#1C6DFF" },
  { code: "raiplay", name: "RaiPlay", accent: "#1C6DFF", countryOnly: "it" },
  { code: "now", name: "NOW", accent: "#00CFFF" },
];

// Platforms available per country (subset — some are geo-restricted)
function getPlatformsForCountry(countryCode) {
  return PLATFORMS.filter(p => {
    if (p.countryOnly && p.countryOnly !== countryCode) return false;
    if (countryCode === "world" && p.countryOnly) return false;
    return true;
  });
}

/* ─── Poster styles (from PostersPlus + blvckTOP) ────────────────────────── */
const POSTER_SHAPES = ["landscape", "poster"];
const CANVAS_BACKGROUNDS = ["fresh", "provider", "stremio", "black", "transparent"];
const STYLES = ["classic", "banner"];

/* ─── TMDB top-10 fetcher ────────────────────────────────────────────────── */
async function fetchTop10(countryCode, platform, type, page = 1) {
  const baseUrl = "https://api.themoviedb.org/3";
  const mediaType = type === "series" ? "tv" : "movie";
  const countryData = COUNTRIES.find(c => c.code === countryCode);
  const isoCode = countryData?.iso || "US";
  const lang = langForCountry(countryCode);

  let url;
  if (platform && platform !== "top10" && platform !== "topx") {
    // Fetch from TMDB discover with platform filter
    url = `${baseUrl}/discover/${mediaType}?api_key=${TMDB_KEY}` +
          `&language=${encodeURIComponent(lang)}&watch_region=${isoCode}` +
          `&with_watch_providers=${getProviderId(platform)}` +
          `&sort_by=popularity.desc&page=${page}`;
  } else if (countryCode === "world" || !countryData) {
    // Global trending
    url = `${baseUrl}/trending/${mediaType}/week?api_key=${TMDB_KEY}` +
          `&language=${encodeURIComponent(lang)}&page=${page}`;
  } else {
    // Country popular
    url = `${baseUrl}/${mediaType}/popular?api_key=${TMDB_KEY}` +
          `&language=${encodeURIComponent(lang)}&region=${isoCode}&page=${page}`;
  }

  try {
    const response = await fetch(url, {
      headers: { "Accept": "application/json" }
    });
    if (!response.ok) throw new Error(`TMDB HTTP ${response.status}`);
    const data = await response.json();
    return (data.results || []).slice(0, 10).map((r, i) => ({
      id: `tmdb:${r.id}`,
      tmdbId: String(r.id),
      name: r.title || r.name || `#${i + 1}`,
      poster: r.poster_path ? `https://image.tmdb.org/t/p/w500${r.poster_path}` : null,
      background: r.backdrop_path ? `https://image.tmdb.org/t/p/original${r.backdrop_path}` : null,
      year: (r.release_date || r.first_air_date || "").substring(0, 4),
      genre: r.genre_ids?.length ? getGenreName(r.genre_ids[0]) : "",
      imdbRating: r.vote_average ? r.vote_average.toFixed(1) : "",
      released: r.release_date || r.first_air_date || new Date().toISOString(),
      description: r.overview || "",
      type: mediaType
    }));
  } catch (err) {
    console.error(`[TMDB] Error fetching top10: ${err.message}`);
    return [];
  }
}

const GENRE_MAP = {
  28: "Action", 12: "Adventure", 16: "Animation", 35: "Comedy",
  80: "Crime", 99: "Documentary", 18: "Drama", 10751: "Family",
  14: "Fantasy", 36: "History", 27: "Horror", 10402: "Music",
  9648: "Mystery", 10749: "Romance", 878: "Sci-Fi", 53: "Thriller",
  10752: "War", 37: "Western", 10759: "Action & Adventure",
  10765: "Sci-Fi & Fantasy", 10768: "War & Politics"
};
function getGenreName(genreId) {
  return GENRE_MAP[genreId] || "";
}

const PROVIDER_IDS = {
  "netflix": 8,
  "amazon-prime": 9,
  "disney": 337,
  "apple-tv": 350,
  "hbo-max": 1899,
  "paramount-plus": 531,
  "crunchyroll": 283,
  "discovery-plus": 435,
  "raiplay": 1025,
  "now": 1,
};
function getProviderId(platformCode) {
  return PROVIDER_IDS[platformCode] || 8;
}

/* ─── TMDB fetch helpers ─────────────────────────────────────────────────── */
async function fetchJson(url, ttl = JSON_TTL) {
  const cached = getCachedJson(url);
  if (cached) return cached;
  const response = await fetch(url, {
    headers: {
      "User-Agent": UA,
      "Accept": "application/json, text/plain, */*"
    },
    // a wedged upstream call must not hold a render-queue slot for the full
    // 90s deadline — 15s is past every healthy TMDB / Cinemeta response
    signal: AbortSignal.timeout(15000)
  });
  if (!response.ok) throw new Error(`HTTP ${response.status} (${response.statusText})`);
  const text = await response.text();
  try {
    const data = JSON.parse(text);
    setCachedJson(url, data, ttl);
    return data;
  } catch {
    throw new Error(`Non valid JSON: ${text.slice(0, 80)}`);
  }
}

/* ─── Render queue: cap concurrent sharp pipelines.  Render's free tier  ──
 *  has 512 MB; a burst of parallel covers (warm-ahead + the two live cards
 *  + AIOMetadata traffic) OOM-kills the instance and every in-flight request
 *  502s.  FIFO, two at a time, shared by /poster and /top-cover.            */
const renderQueue = [];
let activeRenders = 0;
const MAX_CONCURRENT_RENDERS = 2;
// `priority` (unshift) — the live preview's newest settings change jumps the
// warm-ahead and stale probes still queued, so the poster the user is
// watching renders first instead of behind minutes of superseded work.
// `req` marks jobs whose browser already went away (a superseded preview
// probe aborted client-side): those are dropped before they can burn a
// render slot on art nobody is looking at any more.
function enqueueRender(job, opts = {}) {
  const item = { job, resolve: null, reject: null, closed: false };
  const promise = new Promise((resolve, reject) => {
    item.resolve = resolve; item.reject = reject;
  });
  if (opts.priority) renderQueue.unshift(item); else renderQueue.push(item);
  if (opts.req && typeof opts.req.once === "function") {
    const mark = () => { item.closed = true; };
    opts.req.once("close", mark);
    opts.req.once("aborted", mark);
  }
  pumpRenders();
  return promise;
}
const RENDER_DEADLINE_MS = 90000;
function pumpRenders() {
  while (activeRenders < MAX_CONCURRENT_RENDERS && renderQueue.length) {
    const item = renderQueue.shift();
    if (item.closed) {
      item.reject(new Error("request closed before its render started"));
      continue;
    }
    activeRenders++;
    // deadline: a wedged job (upstream stall the timeouts somehow missed)
    // must free its slot instead of starving the queue
    const deadline = new Promise((_, rej) =>
      setTimeout(() => rej(new Error("render deadline exceeded")), RENDER_DEADLINE_MS));
    Promise.race([item.job(), deadline]).then(item.resolve, item.reject)
      .finally(() => { activeRenders--; pumpRenders(); });
  }
}

/* ─── Catalog/cover helpers ──────────────────────────────────────────────── */
function publicBase(req) {
  const proto = req.headers["x-forwarded-proto"] || req.protocol;
  return `${proto}://${req.get("host")}`;
}

function catalogKey(type, id) { return `${type}::${id}`; }
function selectedCatalogSet(config) {
  return new Set((config.catalogs || []).map(c => catalogKey(c.type, c.id)));
}
function getCatalogConfig(config, type, id) {
  return (config.catalogs || []).find(c => c.type === type && c.id === id) || null;
}
function normalizeShape(shape) {
  return shape === "poster" || shape === "portrait" ? "poster" : "landscape";
}
export function normalizeCanvasBackground(bg) {
  const s = String(bg || "").toLowerCase().trim();
  if (s === "fresh") return "fresh";
  if (s === "transparent") return "transparent";
  if (s === "stremio" || s === "stremio-navy" || s === "rgb(26,23,62)" || s === "rgb(26, 23, 62)" || s === "#1a173e" || s === "1a173e") return "stremio";
  if (s === "black" || s === "nero") return "black";
  return "provider";
}
function catalogAccent(catalog = {}) {
  const key = `${catalog.id || ""} ${catalog.name || ""}`.toLowerCase();
  for (const p of PLATFORMS) {
    if (key.includes(p.code)) return p.accent;
  }
  if (key.includes("top10") || key.includes("world")) return "#8C75FF";
  return "#8C75FF";
}

async function buildCoverUrl(req, token, meta, rank, type, catalog) {
  const shape = normalizeShape(catalog.shape);
  const canvasBackground = normalizeCanvasBackground(catalog.canvasBackground);
  const style = canvasBackground === "fresh" ? "banner" : normalizeStyle(catalog.style);
  const showMeta = catalog.showMeta !== false;
  const showLogo = catalog.showLogo !== false;
  const fontFamily = String(catalog.fontFamily || "Inter");
  const ratingFormat = String(catalog.ratingFormat || "star");
  const vignette = Number(catalog.vignette || 0);
  const genre = showMeta ? meta?.genre || "" : "";
  const rating = showMeta ? meta?.imdbRating || "" : "";

  try {
    const tmdbId = await resolveTmdbId(type, meta.tmdbId, TMDB_KEY);
    if (tmdbId) {
      const qs = new URLSearchParams({
        rank: String(rank), type: type === "series" ? "tv" : type, tmdbId,
        shape, catalogId: catalog.id || "", canvasBackground,
        showMeta: String(showMeta), showLogo: String(showLogo),
        fontFamily, ratingFormat, vignette: String(vignette),
        genre: genre || "", rating: rating || "",
        v: style === "banner" ? FRESH_VERSION : "7.5.0"
      });
      if (style === "banner") qs.set("style", style);
      return `${publicBase(req)}/c/${token}/top-cover?${qs}`;
    }
  } catch (err) { console.warn("TMDB resolve failed:", err.message); }

  const fallbackArtwork = shape === "poster" ? (meta.poster || meta.background) : (meta.background || meta.poster);
  if (!fallbackArtwork) return meta.poster;

  const qs = new URLSearchParams({
    rank: String(rank), artwork: fallbackArtwork, shape,
    catalogId: catalog.id || "", canvasBackground,
    showMeta: String(showMeta), showLogo: String(showLogo),
    fontFamily, ratingFormat, vignette: String(vignette),
    genre: genre || "", rating: rating || "",
    v: style === "banner" ? FRESH_VERSION : "7.5.0"
  });
  if (style === "banner") qs.set("style", style);
  return `${publicBase(req)}/c/${token}/top-cover?${qs}`;
}

/* ─── API: Countries ──────────────────────────────────────────────────────── */
app.get("/api/countries", (_req, res) => {
  res.setHeader("Cache-Control", "public, max-age=31536000, immutable");
  res.json({ countries: COUNTRIES });
});

/* ─── API: Platforms per country ──────────────────────────────────────────── */
app.get("/api/platforms/:country", (req, res) => {
  const { country } = req.params;
  const platforms = getPlatformsForCountry(country);
  res.json({ platforms });
});

/* ─── API: Catalogs for country+platforms ────────────────────────────────── */
app.get("/api/catalogs/:country", (req, res) => {
  const { country } = req.params;
  const countryData = COUNTRIES.find(c => c.code === country);
  if (!countryData) return res.json({ catalogs: [] });

  const platforms = getPlatformsForCountry(country);
  const catalogs = [];
  // Add generic Top 10
  catalogs.push({ id: `tx.${country}.topx.movie`, type: "movie", name: `Top X ${countryData.name}` });
  catalogs.push({ id: `tx.${country}.topx.series`, type: "series", name: `Top X ${countryData.name}` });
  // Add platform-specific
  for (const p of platforms) {
    catalogs.push({ id: `tx.${country}.${p.code}.movie`, type: "movie", name: `${p.name} Top 10` });
    catalogs.push({ id: `tx.${country}.${p.code}.series`, type: "series", name: `${p.name} Top 10` });
  }
  res.json({ catalogs });
});

/* Default catalogs endpoint (world) — used by configurator on first load */
app.get("/api/catalogs", (req, res) => {
  const country = req.query.country || "world";
  const countryData = COUNTRIES.find(c => c.code === country) || COUNTRIES[0];
  const platforms = getPlatformsForCountry(country);
  const catalogs = [];
  catalogs.push({ id: `tx.${country}.topx.movie`, type: "movie", name: `Top X ${countryData.name}` });
  catalogs.push({ id: `tx.${country}.topx.series`, type: "series", name: `Top X ${countryData.name}` });
  for (const p of platforms) {
    catalogs.push({ id: `tx.${country}.${p.code}.movie`, type: "movie", name: `${p.name} Top 10` });
    catalogs.push({ id: `tx.${country}.${p.code}.series`, type: "series", name: `${p.name} Top 10` });
  }
  res.json({ catalogs, country: countryData, platforms });
});

/* ─── API: Generate manifest ─────────────────────────────────────────────── */
app.post("/api/generate", async (req, res) => {
  try {
    const body = req.body || {};
    let finalCatalogs = [];

    // Mode A: blvckTOP-style — client sends a fully-formed catalog list
    // (with shape/style/canvasBackground/showMeta/showLogo on each item)
    if (Array.isArray(body.catalogs) && body.catalogs.length > 0 && body.catalogs[0]?.id) {
      const seen = new Set();
      for (const c of body.catalogs) {
        if (!c?.id || !c?.type) continue;
        const key = `${c.type}::${c.id}`;
        if (seen.has(key)) continue;
        seen.add(key);
        const parts = String(c.id).split(".");
        const country = parts[1] || "world";
        finalCatalogs.push({
          id: c.id,
          type: c.type,
          name: (c.name && c.name.trim()) || c.id,
          shape: normalizeShape(c.shape || body.posterShape || "landscape"),
          style: (c.canvasBackground || body.canvasBackground || "fresh") === "fresh"
            ? "banner"
            : normalizeStyle(c.style || body.posterStyle || "classic"),
          canvasBackground: normalizeCanvasBackground(c.canvasBackground || body.canvasBackground || "fresh"),
          showMeta: c.showMeta !== false && body.showMeta !== false,
          showLogo: c.showLogo !== false && body.showLogo !== false,
          fontFamily: String(c.fontFamily || body.fontFamily || "Inter"),
          ratingFormat: String(c.ratingFormat || body.ratingFormat || "star"),
          vignette: Number(c.vignette ?? body.vignette ?? 0),
          country,
        });
      }
      if (!finalCatalogs.length) return res.status(400).json({ error: "No valid catalogs in payload." });
    } else {
      // Mode B: TOPX-style — client sends country + platforms + global poster opts
      const {
        country = "world",
        platforms = ["top10"],
        posterShape = "landscape",
        posterStyle = "classic",
        canvasBackground = "fresh",
        showMeta = true,
        showLogo = true,
        fontFamily = "Inter",
        ratingFormat = "star",
        vignette = 0,
      } = body;

      const countryData = COUNTRIES.find(c => c.code === country);
      if (!countryData) return res.status(400).json({ error: "Country not found" });

      const availablePlatforms = getPlatformsForCountry(country);
      // "top10" / "topx" are sentinels meaning "the generic Top 10 chart",
      // not real platforms — they always pass the filter.
      const selectedPlatforms = platforms.filter(p =>
        p === "top10" || p === "topx" || availablePlatforms.some(ap => ap.code === p)
      );

      const catalogs = [];
      if (selectedPlatforms.includes("top10") || selectedPlatforms.includes("topx")) {
        catalogs.push({ id: `tx.${country}.topx.movie`, type: "movie", name: `Top X ${countryData.name}` });
        catalogs.push({ id: `tx.${country}.topx.series`, type: "series", name: `Top X ${countryData.name}` });
      }
      for (const p of selectedPlatforms) {
        if (p === "top10" || p === "topx") continue;
        const platform = availablePlatforms.find(ap => ap.code === p);
        if (!platform) continue;
        catalogs.push({ id: `tx.${country}.${p}.movie`, type: "movie", name: `${platform.name} Top 10` });
        catalogs.push({ id: `tx.${country}.${p}.series`, type: "series", name: `${platform.name} Top 10` });
      }
      if (!catalogs.length) return res.status(400).json({ error: "Select at least one platform." });

      finalCatalogs = catalogs.map(c => ({
        ...c,
        shape: normalizeShape(posterShape),
        style: canvasBackground === "fresh" ? "banner" : normalizeStyle(posterStyle),
        canvasBackground: normalizeCanvasBackground(canvasBackground),
        showMeta: showMeta !== false,
        showLogo: showLogo !== false,
        fontFamily: String(fontFamily || "Inter"),
        ratingFormat: String(ratingFormat || "star"),
        vignette: Number(vignette || 0),
        country,
      }));
    }

    const token = encryptConfig({ v: 4, catalogs: finalCatalogs });
    const base = publicBase(req);

    // Stremio variant (canvasBackground "black" → "stremio" navy)
    const stremioCatalogs = finalCatalogs.map(c => ({
      ...c, canvasBackground: c.canvasBackground === "black" || c.canvasBackground === "stremio" ? "stremio" : c.canvasBackground
    }));
    const stremioToken = encryptConfig({ v: 4, catalogs: stremioCatalogs });

    // Nuvio variant (canvasBackground "stremio" → "black")
    const nuvioCatalogs = finalCatalogs.map(c => ({
      ...c, canvasBackground: c.canvasBackground === "black" || c.canvasBackground === "stremio" ? "black" : c.canvasBackground
    }));
    const nuvioToken = encryptConfig({ v: 4, catalogs: nuvioCatalogs });

    const firstCountry = finalCatalogs[0]?.country || "world";
    const countryData = COUNTRIES.find(c => c.code === firstCountry) || COUNTRIES[0];

    res.json({
      ok: true,
      manifestUrl: `${base}/c/${token}/manifest.json`,
      stremioManifestUrl: `${base}/c/${stremioToken}/manifest.json`,
      nuvioManifestUrl: `${base}/c/${nuvioToken}/manifest.json`,
      catalogCount: finalCatalogs.length,
      country: countryData,
      posterShape: finalCatalogs[0]?.shape || "landscape",
      posterStyle: finalCatalogs[0]?.style || "classic",
      canvasBackground: finalCatalogs[0]?.canvasBackground || "fresh",
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: err.message });
  }
});

/* ─── Manifest endpoints ─────────────────────────────────────────────────── */
app.get("/manifest.json", async (_req, res) => {
  try {
    const country = _req.query.country || "world";
    const countryData = COUNTRIES.find(c => c.code === country) || COUNTRIES[0];
    const platforms = getPlatformsForCountry(country);
    const catalogs = [];
    catalogs.push({ id: `tx.${country}.topx.movie`, type: "movie", name: `Top X ${countryData.name}` });
    catalogs.push({ id: `tx.${country}.topx.series`, type: "series", name: `Top X ${countryData.name}` });
    for (const p of platforms.slice(0, 5)) {
      catalogs.push({ id: `tx.${country}.${p.code}.movie`, type: "movie", name: `${p.name} Top 10` });
      catalogs.push({ id: `tx.${country}.${p.code}.series`, type: "series", name: `${p.name} Top 10` });
    }
    const manifest = {
      id: "com.topx.combined",
      version: "1.0.0",
      name: "TOPX Posters",
      description: `Multi-country Top 10 charts with numbered poster covers. ${countryData.flag} ${countryData.name}`,
      logo: "https://raw.githubusercontent.com/blvckroby/MusicDB/refs/heads/main/loghi/Top10Badge.svg",
      types: ["movie", "series"],
      resources: ["catalog", "meta"],
      idPrefixes: ["tt", "tmdb"],
      catalogs,
      behaviorHints: { configurable: true, configurationRequired: false }
    };
    res.setHeader("Cache-Control", "public, max-age=300");
    res.json(manifest);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.get("/c/:token/manifest.json", async (req, res) => {
  try {
    const config = decryptConfig(req.params.token);
    const catalogs = config.catalogs || [];
    const idSuffix = shortConfigId(req.params.token);
    const country = catalogs[0]?.country || "world";
    const countryData = COUNTRIES.find(c => c.code === country) || COUNTRIES[0];
    const manifest = {
      id: `com.topx.${idSuffix}`,
      version: "1.0.0",
      name: "TOPX Posters",
      description: `Multi-country Top 10 charts with numbered poster covers. ${countryData.flag} ${countryData.name}`,
      logo: "https://raw.githubusercontent.com/blvckroby/MusicDB/refs/heads/main/loghi/Top10Badge.svg",
      types: ["movie", "series"],
      resources: ["catalog", "meta"],
      idPrefixes: ["tt", "tmdb"],
      catalogs,
      behaviorHints: { configurable: true, configurationRequired: false }
    };
    res.setHeader("Cache-Control", "public, max-age=300");
    res.json(manifest);
  } catch (err) {
    res.status(400).json({ error: err.message });
  }
});

/* ─── Catalog endpoint — fetches from TMDB and decorates with covers ─────── */
app.get("/c/:token/catalog/:type/:catalogId.json", async (req, res) => {
  try {
    const config = decryptConfig(req.params.token);
    const { type, catalogId } = req.params;
    const catalog = getCatalogConfig(config, type, catalogId);
    if (!catalog) return res.status(404).json({ metas: [] });

    // Parse catalogId: tx.{country}.{platform}.{type}
    const catParts = catalogId.split(".");
    const country = catParts[1] || "world";
    const platform = catParts[2] || "topx";

    // Fetch top 10 from TMDB
    const metas = await fetchTop10(country, platform, type);

    // Decorate with numbered covers
    const decorated = await Promise.all(
      metas.map(async (meta, index) => {
        const poster = await buildCoverUrl(req, req.params.token, meta, index + 1, type, catalog);
        const posterShape = normalizeShape(catalog.shape);
        let sanitizedReleased = meta.released;
        if (sanitizedReleased && new Date(sanitizedReleased) > new Date()) {
          sanitizedReleased = new Date().toISOString();
        }
        return {
          ...meta,
          ...(sanitizedReleased ? { released: sanitizedReleased } : {}),
          poster,
          ...(posterShape === "landscape" ? { landscapePoster: poster } : {}),
          posterShape
        };
      })
    );

    res.setHeader("Cache-Control", "public, max-age=300");
    res.json({ metas: decorated });
  } catch (err) {
    console.error(err);
    res.status(502).json({ error: err.message, metas: [] });
  }
});

app.get("/c/:token/meta/:type/:id.json", async (req, res) => {
  try {
    decryptConfig(req.params.token);
    const tmdbId = req.params.id.replace("tmdb:", "");
    const type = req.params.type === "series" ? "tv" : "movie";
    const url = `https://api.themoviedb.org/3/${type}/${tmdbId}?api_key=${TMDB_KEY}&language=en-US`;
    const data = await fetchJson(url);
    res.setHeader("Cache-Control", "public, max-age=1800");
    res.json({
      id: `tmdb:${tmdbId}`,
      type: req.params.type,
      name: data.title || data.name,
      poster: data.poster_path ? `https://image.tmdb.org/t/p/original${data.poster_path}` : null,
      background: data.backdrop_path ? `https://image.tmdb.org/t/p/original${data.backdrop_path}` : null,
      description: data.overview || "",
      releaseInfo: (data.release_date || data.first_air_date || "").substring(0, 4),
      imdbRating: data.vote_average ? data.vote_average.toFixed(1) : "",
      genres: (data.genres || []).map(g => g.name),
      runtime: data.runtime || data.episode_run_time?.[0] || null
    });
  } catch (err) {
    res.status(502).json({ error: err.message });
  }
});

/* ─── Cover generation endpoint (from blvckTOP) ─────────────────────────── */
app.get(["/c/:token/top-cover", "/top-cover"], async (req, res) => {
  try {
    let config = null;
    if (req.params.token) {
      try { config = decryptConfig(req.params.token); } catch {}
    }
    const requestedRank = Number(req.query.rank || 1);
    const rank = Number.isFinite(requestedRank) ? Math.max(1, Math.min(10, Math.trunc(requestedRank))) : 1;
    const type = String(req.query.type || "movie");
    const shape = normalizeShape(String(req.query.shape || "landscape"));
    const tmdbId = req.query.tmdbId ? String(req.query.tmdbId) : null;
    const catalogId = req.query.catalogId ? String(req.query.catalogId) : "";
    const catalog = config?.catalogs?.find(c => c.id === catalogId) || { id: catalogId };
    const canvasBackground = normalizeCanvasBackground(req.query.canvasBackground ?? catalog.canvasBackground);
    const style = canvasBackground === "fresh" ? "banner" : normalizeStyle(String(req.query.style ?? catalog.style));
    const showMeta = req.query.showMeta !== undefined ? req.query.showMeta !== "false" : (catalog.showMeta !== false);
    const showLogo = req.query.showLogo !== undefined ? req.query.showLogo !== "false" : (catalog.showLogo !== false);
    // PostersPlus-inspired style options (font / rating format / vignette)
    const fontFamily = String(req.query.fontFamily ?? catalog.fontFamily ?? "Inter");
    const ratingFormat = String(req.query.ratingFormat ?? catalog.ratingFormat ?? "star");
    const vignette = Number(req.query.vignette ?? catalog.vignette ?? 0);
    let genre = showMeta && req.query.genre ? String(req.query.genre) : "";
    let rating = showMeta && req.query.rating ? String(req.query.rating) : "";
    let artworkUrl = req.query.artwork ? String(req.query.artwork) : null;
    let logoUrl = "";
    let resolvedTmdbId = tmdbId;
    const accent = catalogAccent(catalog);
    const effectiveType = type === "series" || type === "tv" ? "tv" : "movie";
    // Country for language-aware artwork (parsed from catalogId: tx.<country>.<platform>.<type>)
    const catParts = String(catalogId || "").split(".");
    const country = catalog?.country || catParts[1] || "world";

    if (resolvedTmdbId && !artworkUrl) {
      try {
        const images = await getTmdbImages(effectiveType, resolvedTmdbId, TMDB_KEY, country);
        if (style === "banner") {
          ({ artworkUrl, logoUrl } = chooseBannerArtwork(images, shape, country));
        } else {
          artworkUrl = shape === "poster" ? choosePoster(images, country) : chooseBackdrop(images, country);
        }
      } catch (tmdbErr) {
        console.warn(`TMDB images error:`, tmdbErr.message);
      }
    }
    if (!artworkUrl) return res.status(404).send("No artwork available.");

    const coverKey = computeCoverKey({ rank, type: effectiveType, shape, tmdbId: resolvedTmdbId, catalogId, canvasBackground, accent, artworkUrl, genre, rating, showLogo, style, logoUrl, fontFamily, ratingFormat, vignette });
    const existingFilePath = getCoverFilePath(coverKey);
    if (existingFilePath) {
      res.setHeader("X-Cover-Cache", "HIT-DISK");
      res.setHeader("Cache-Control", "public, max-age=31536000, immutable");
      return res.sendFile(existingFilePath);
    }
    if (pendingCovers.has(coverKey)) {
      const png = await pendingCovers.get(coverKey);
      res.setHeader("Content-Type", coverMime(png));
      res.setHeader("Cache-Control", "public, max-age=31536000, immutable");
      res.setHeader("X-Cover-Cache", "SHARED");
      return res.send(png);
    }
    const generationPromise = enqueueRender(() => createTopCover({ rank, artworkUrl, logoUrl, style, shape, accent, canvasBackground, genre, rating, catalogId, showLogo, fontFamily, ratingFormat, vignette }).then(png => { saveCoverBuffer(coverKey, png); return png; }), { priority: true, req });
    pendingCovers.set(coverKey, generationPromise);
    try {
      const png = await generationPromise;
      res.setHeader("Content-Type", coverMime(png));
      res.setHeader("Cache-Control", "public, max-age=31536000, immutable");
      res.setHeader("X-Cover-Cache", "GENERATED");
      res.send(png);
    } finally {
      pendingCovers.delete(coverKey);
    }
  } catch (err) {
    console.error(`Cover error:`, err);
    res.status(500).json({ error: err.message });
  }
});

/* ─── PostersPlus-compatible /poster endpoint (AIOMetadata poster provider) ─
 * URL format matches PostersPlus exactly: /poster?tmdb_id=…&imdb_id=…&type=…&…
 * so the same URL works against this server OR a real PostersPlus instance.
 * blvckTOP/TOPX extras ride alongside: rank (numbered cover), country,
 * canvas_background, show_meta, show_logo.
 *
 * PostersPlus params honored by this renderer:
 *   type, tmdb_id, imdb_id, shape, language/logo_language,
 *   logo_priority + logo_language_secondary, label_font,
 *   rating_display_mode, rating_format (TOPX alias), rating_text_color,
 *   score_color_mode, score_custom_palette, score_glow_alpha,
 *   top_gradient(+opacity/height), bottom_gradient(+…),
 *   vignette_poster_color_top/bottom, vignette_color_saturation/lightness/blur,
 *   hide_genre, hide_rating, hide_year, meta_order, landscape_info_scale,
 *   logo_max_w_ratio, logo_max_h_ratio, logo_bottom_ratio, landscape_logo_scale,
 *   poster_pick, use_original_art, original_art_source, textless,
 *   landscape_poster_crop, landscape_greyscale, resolution,
 *   accent_bar_bottom_ratio (info-line lift)
 * Params accepted & passed through untouched (a real PostersPlus instance
 * renders them; this renderer draws what it supports):
 *   primary_client, sash_*, badge_*, bar_*, movie_weights, tv_weights,
 *   meta_order extras, release_status_*, airing_dates,
 *   hide_unreleased_rating, *_rating_source, fallback_to_imdb,
 *   fallback_bg_style, trending_*, score_glow_threshold/blur,
 *   minimalist_*, accent_bar_append_mode, landscape_art*, …
 */
const isTrue = v => v === "true" || v === "1" || v === 1;

app.get(["/poster", "/poster/"], async (req, res) => {
  try {
    const q = req.query;

    // ── Core identity ──────────────────────────────────────────────────────
    // Literal AIOMetadata placeholders ({tmdb_id}, {imdb_id?}, {type},
    // {shape}, {rank}) read as "absent" — same behaviour as PostersPlus's
    // _normalise_optional_id — so a URL pasted raw into a browser degrades
    // gracefully instead of 400ing on a malformed id.
    const cleanParam = v => {
      const s = v === undefined || v === null ? "" : String(v).trim();
      return /\{\w+\??\}$/.test(s) ? "" : s;
    };
    const rawType = cleanParam(q.type).toLowerCase() || "movie";
    const type = rawType === "tv" || rawType === "series" ? "tv" : "movie";

    let tmdbId = cleanParam(q.tmdb_id).replace(/^tmdb:/, "").replace(/\D/g, "");
    const rawImdb = cleanParam(q.imdb_id);
    const rawStremio = cleanParam(q.stremio_id);
    // AIOMetadata sends stremio_id={id} — which can be a bare IMDb id, a
    // tmdb:N id, an anime id (kitsu:/anilist:/mal:), or one of AIOMetadata's
    // own row ids (upnext_tt…, mdblist_upnext_tmdb:…, unwatched_tt…).
    // resolveTmdbIdAndType() unwraps all of these AND returns the actual TMDB
    // type — when the requested type doesn't match (e.g. Breaking Bad's IMDb
    // id requested as a movie), TMDB's type wins (PostersPlus rule).
    let resolvedType = type;
    if (!tmdbId) {
      const candidate = rawImdb || rawStremio;
      if (candidate) {
        const r = await resolveTmdbIdAndType(type, candidate, TMDB_KEY);
        if (r) { tmdbId = r.tmdbId; resolvedType = r.type; }
      }
    }
    // effectiveType follows the resolved TMDB type for the rest of the render.
    const effectiveType = resolvedType === "series" || resolvedType === "tv" ? "tv" : "movie";
    if (!tmdbId) {
      // Friendly hint when the URL still carries AIOMetadata placeholders
      const raw = `${q.tmdb_id || ""}${q.imdb_id || ""}${q.type || ""}`;
      if (/\{\w+\??\}/.test(raw)) {
        return res.status(400).send(
          "poster: this URL still contains {placeholders} — paste it into AIOMetadata (poster provider), which fills tmdb_id / imdb_id / type automatically. To test in a browser, replace them with real values."
        );
      }
      return res.status(400).send("poster: tmdb_id or imdb_id required (and resolvable).");
    }

    // ── Shape: PostersPlus uses shape={shape} → poster|landscape|square.
    // Square is refused by PostersPlus; we fall back to portrait gracefully.
    const rawShape = cleanParam(q.shape).toLowerCase() || "poster";
    const shape = rawShape === "landscape" ? "landscape" : "poster";

    // ── TOPX / blvckTOP extras ─────────────────────────────────────────
    const country = String(q.country || "world").toLowerCase().trim() || "world";
    const canvasBackground = normalizeCanvasBackground(q.canvas_background || q.canvasBackground || "fresh");
    const style = canvasBackground === "fresh" ? "banner" : "classic";
    const showMeta = q.show_meta !== undefined ? q.show_meta !== "false" : q.showMeta !== "false";
    const showLogo = q.show_logo !== undefined ? q.show_logo !== "false" : q.showLogo !== "false";
    // Provider logo + brand tint (the topx "Provider Logo" / "Brand Tint"
    // chips).  Both need a real streaming platform — generic Top X charts
    // have no brand, exactly like the topx engine restricts brandTint to
    // per-platform catalogs.  The platform code also drives the accent, so
    // the brand tint glows in the platform's own colour.
    const providerLogo = isTrue(q.provider_logo);
    const brandTint = isTrue(q.brand_tint);
    const platformRaw = cleanParam(q.platform || q.provider).toLowerCase().trim();
    const platform = ["top10", "topx", ""].includes(platformRaw) ? "" : platformRaw;
    const renderCatalogId = providerLogo && platform ? platform : "";
    const renderBrandTint = brandTint && !!platform;
    let rank = Number(cleanParam(q.rank));
    if (!Number.isFinite(rank) || rank <= 0 || rank > 10) rank = 0; // absent/invalid → plain poster

    // ── Language ────────────────────────────────────────────────────────
    const language = String(q.language || q.logo_language || langForCountry(country));
    const logoPriority = String(q.logo_priority || "native_original");
    const logoSecondary = cleanParam(q.logo_language_secondary);

    // ── Font (PostersPlus label_font values, aliased) ──────────────────
    const fontFamily = normalizeFontFamily(q.label_font || q.fontFamily || "Inter");

    // ── Rating format: PostersPlus rating_display_mode 1..4 + TOPX alias ───
    let ratingFormat = String(q.rating_format || "").toLowerCase().trim();
    if (!["star", "number", "hidden"].includes(ratingFormat)) {
      const mode = String(q.rating_display_mode ?? "1").trim();
      ratingFormat = ({ "0": "hidden", "1": "star", "2": "number", "3": "number", "4": "hidden" })[mode] || "star";
    }

    // ── Colors ─────────────────────────────────────────────────────────
    const metaColor = String(q.rating_text_color || "").replace(/^#/, "").trim();

    // ── Vignette (PostersPlus top/bottom gradients + colour vignette) ──
    const vignetteOpts = {
      top: q.top_gradient ?? null,
      bottom: q.bottom_gradient ?? null,
      topOpacity: q.top_gradient_opacity ?? null,
      topHeight: q.top_gradient_height ?? null,
      bottomOpacity: q.bottom_gradient_opacity ?? null,
      bottomHeight: q.bottom_gradient_height ?? null,
      colorTop: isTrue(q.vignette_poster_color_top),
      colorBottom: isTrue(q.vignette_poster_color_bottom),
      colorSat: q.vignette_color_saturation ?? null,
      colorLight: q.vignette_color_lightness ?? null,
      colorBlur: q.vignette_color_blur ?? null,
      colorStyle: q.vignette_color_style ?? null,
    };
    // Legacy numeric vignette (TOPX param) still wins when explicitly set
    const legacyVignette = q.vignette !== undefined ? Number(q.vignette) || 0 : 0;

    // ── Resolution (output width in px; 0 = native) ────────────────────
    const resolution = Number(cleanParam(q.resolution)) || 0;

    // ── PostersPlus render extras ──────────────────────────────────────
    const hideGenre = isTrue(q.hide_genre);
    const hideRating = isTrue(q.hide_rating);
    const hideYear = isTrue(q.hide_year); // no year data in this engine; kept for cache parity
    const textless = isTrue(q.textless);
    const metaOrder = cleanParam(q.meta_order) || (shape === "landscape" ? cleanParam(q.landscape_meta_order) : "");
    const scoreColorMode = String(q.score_color_mode ?? "").trim();
    const scoreCustomPalette = cleanParam(q.score_custom_palette);
    const glowAlpha = q.score_glow_alpha ?? "";
    const infoScale = shape === "landscape"
      ? (Number(q.landscape_info_scale) || 1)
      : 1;
    const logoMaxW = Number(q.logo_max_w_ratio) > 0 ? Number(q.logo_max_w_ratio) : 0;
    const logoMaxH = Number(q.logo_max_h_ratio) > 0 ? Number(q.logo_max_h_ratio) : 0;
    const landscapeLogoScale = Number(q.landscape_logo_scale) > 0 ? Number(q.landscape_logo_scale) : 1;
    const extra = {
      hideGenre, hideRating, hideYear,
      metaOrder,
      scoreColorMode,
      scoreCustomPalette,
      glowAlpha: glowAlpha === "" ? null : Number(glowAlpha),
      infoScale,
      logoMaxWRatio: landscapeLogoScale !== 1 ? logoMaxW * landscapeLogoScale : logoMaxW,
      logoMaxHRatio: landscapeLogoScale !== 1 ? logoMaxH * landscapeLogoScale : logoMaxH,
      logoBottomRatio: Number(q.logo_bottom_ratio) > 0 ? Number(q.logo_bottom_ratio) : 0,
      accentBarBottomRatio: Number(q.accent_bar_bottom_ratio) > 0 ? Number(q.accent_bar_bottom_ratio) : 0,
      // topx Provider Logo / Brand Tint — explicit booleans (decoupled from
      // show_logo, which stays the title-logo switch in banner mode)
      providerLogo,
      brandTint: renderBrandTint,
    };

    // ── Artwork: language-aware TMDB image selection ───────────────────
    const greyscale = String(q.landscape_greyscale || q.greyscale || "") === "true";
    const posterCrop = String(q.landscape_poster_crop || q.poster_crop || "") === "true";
    const posterPick = cleanParam(q.poster_pick) || cleanParam(q.landscape_poster_pick) || "top";
    const originalArtSource = cleanParam(q.original_art_source) || "primary";
    const useOriginalArt = isTrue(q.use_original_art);
    // Use the resolved TMDB type (effectiveType) — when an IMDb id resolves
    // to a TV show but the request asked for movie, TMDB's type wins.
    const images = await getTmdbImages(effectiveType, tmdbId, TMDB_KEY, country);
    let artworkUrl = null;
    let logoUrl = "";
    if (style === "banner") {
      ({ artworkUrl, logoUrl } = chooseBannerArtwork(images, shape, country));
    } else if (shape === "poster") {
      artworkUrl = posterPick !== "top"
        ? (choosePosterPick(images, country, posterPick) || choosePoster(images, country))
        : choosePoster(images, country);
    } else {
      // landscape: PostersPlus landscape_poster_crop=true crops the portrait
      // poster art to 16:9 instead of using the backdrop
      artworkUrl = posterCrop ? choosePoster(images, country) : chooseBackdrop(images, country);
    }
    // PostersPlus textless=true → force textless (no burned-in title) art
    if (textless) {
      const textlessUrl = shape === "poster" ? chooseTextlessPoster(images) : chooseTextlessBackdrop(images);
      if (textlessUrl) artworkUrl = textlessUrl;
      if (style === "banner") logoUrl = chooseLogo(images, country) || "";
    }
    // PostersPlus use_original_art=true → prefer art with the title baked in
    // (i.e. skip the textless+logo path and use the primary art as-is).
    if (useOriginalArt && style === "banner") {
      artworkUrl = originalArtSource === "backdrop" && shape === "poster"
        ? chooseBackdrop(images, country)
        : (shape === "poster" ? choosePoster(images, country) : chooseBackdrop(images, country));
      logoUrl = "";
    }
    // PostersPlus logo_priority — re-pick the banner's title logo language.
    if (style === "banner" && showLogo) {
      logoUrl = chooseLogoWithPriority(images, country, logoPriority, logoSecondary) || "";
      if (!logoUrl) {
        // priority excluded every available band → banner falls back to art
        const fallback = shape === "poster" ? choosePoster(images, country) : chooseBackdrop(images, country);
        if (fallback) artworkUrl = fallback;
      }
    }
    if (!artworkUrl) return res.status(404).send("No artwork available.");

    // ── Genre / rating (language-aware TMDB details) ───────────────────
    let genre = "";
    let rating = "";
    if (showMeta && !hideGenre && !hideRating) {
      const details = await getTmdbDetails(effectiveType, tmdbId, TMDB_KEY, country);
      genre = details?.genre || "";
      rating = details?.rating || "";
    } else if (showMeta && (hideGenre || hideRating)) {
      // One of the two is hidden — still need the other half
      const details = await getTmdbDetails(effectiveType, tmdbId, TMDB_KEY, country);
      genre = hideGenre ? "" : (details?.genre || "");
      rating = hideRating ? "" : (details?.rating || "");
    }

    // ── Render (disk-cached) ───────────────────────────────────────
    // Accent follows the platform when one is given (the brand colour the
    // provider logo and the brand tint glow in); generic charts keep the
    // topx violet.
    const accent = platform
      ? (PLATFORMS.find(p => p.code === platform)?.accent || catalogAccent({ id: platform }))
      : catalogAccent({ id: `poster.${country}` });
    // coverKey uses the resolved TMDB type so the cache is keyed by what was
    // actually rendered (a TV poster isn't a movie poster even if the URL
    // asked for one).
    const coverKey = computeCoverKey({
      rank, type: effectiveType, shape, tmdbId, catalogId: `poster.${renderCatalogId || "x"}`, canvasBackground, accent,
      artworkUrl, genre, rating, showLogo, style, logoUrl, fontFamily,
      ratingFormat, vignette: legacyVignette,
      vignetteOptsJson: legacyVignette ? "" : JSON.stringify(vignetteOpts),
      metaColor, resolution: String(resolution), greyscale,
      extrasJson: JSON.stringify(extra),
    });
    const existingFilePath = getCoverFilePath(coverKey);
    if (existingFilePath) {
      res.setHeader("X-Cover-Cache", "HIT-DISK");
      res.setHeader("Cache-Control", "public, max-age=31536000, immutable");
      return res.sendFile(existingFilePath);
    }
    if (pendingCovers.has(coverKey)) {
      const png = await pendingCovers.get(coverKey);
      res.setHeader("Content-Type", coverMime(png));
      res.setHeader("Cache-Control", "public, max-age=31536000, immutable");
      return res.send(png);
    }
    const generationPromise = enqueueRender(() => createTopCover({
        rank, artworkUrl, logoUrl, style, shape, accent, canvasBackground,
        genre, rating, catalogId: renderCatalogId, showLogo, fontFamily, ratingFormat,
        vignette: legacyVignette,
        vignetteOpts: legacyVignette ? null : vignetteOpts,
        metaColor, resolution, greyscale,
        extra,
      }).then(png => { saveCoverBuffer(coverKey, png); return png; }), { priority: true, req });
    pendingCovers.set(coverKey, generationPromise);
    try {
      const png = await generationPromise;
      res.setHeader("Content-Type", coverMime(png));
      res.setHeader("Cache-Control", "public, max-age=31536000, immutable");
      res.setHeader("X-Cover-Cache", "GENERATED");
      res.send(png);
    } finally {
      pendingCovers.delete(coverKey);
    }
  } catch (err) {
    console.error(`[poster] error:`, err);
    res.status(500).send(`poster error: ${err.message}`);
  }
});

/* ─── PostersPlus-compatible /logo endpoint ────────────────────────────────
 * URL format matches PostersPlus exactly:
 *   /logo?tmdb_id=…&type=…&lang=en
 * Returns the best available title logo as its original PNG. Uses the same
 * cached TMDB selection chain as poster rendering (chooseLogoWithPriority).
 * AIOMetadata's "logo providers" feature can consume this URL directly.
 */
app.get(["/logo", "/logo/"], async (req, res) => {
  try {
    const q = req.query;
    const cleanParam = v => {
      const s = v === undefined || v === null ? "" : String(v).trim();
      return /\{\w+\??\}$/.test(s) ? "" : s;
    };
    const rawType = cleanParam(q.type).toLowerCase() || "movie";
    const reqType = rawType === "tv" || rawType === "series" ? "tv" : "movie";
    let tmdbId = cleanParam(q.tmdb_id).replace(/^tmdb:/, "").replace(/\D/g, "");
    let resolvedType = reqType;
    const candidate = cleanParam(q.imdb_id) || cleanParam(q.stremio_id);
    if (!tmdbId && candidate) {
      const r = await resolveTmdbIdAndType(reqType, candidate, TMDB_KEY);
      if (r) { tmdbId = r.tmdbId; resolvedType = r.type; }
    }
    const type = resolvedType === "series" || resolvedType === "tv" ? "tv" : "movie";
    if (!tmdbId) {
      const raw = `${q.tmdb_id || ""}${q.imdb_id || ""}${q.type || ""}`;
      if (/\{\w+\??\}/.test(raw)) {
        return res.status(400).send("logo: this URL still contains {placeholders} — paste it into AIOMetadata (logo provider), which fills tmdb_id / imdb_id / type automatically.");
      }
      return res.status(400).send("logo: tmdb_id or imdb_id required (and resolvable).");
    }
    const country = String(q.country || "world").toLowerCase().trim() || "world";
    const logoLang = cleanParam(q.lang) || cleanParam(q.logo_language) || langForCountry(country);
    const logoPriority = String(q.logo_priority || "native_original");
    const logoSecondary = cleanParam(q.logo_language_secondary);
    const images = await getTmdbImages(type, tmdbId, TMDB_KEY, country);
    // chooseLogoWithPriority honours the same priority/secondary scheme used
    // by /poster, so a single logo URL stays consistent across endpoints.
    let logoUrl = chooseLogoWithPriority(images, country, logoPriority, logoSecondary) || "";
    if (!logoUrl) logoUrl = chooseLogo(images, country) || "";
    if (!logoUrl) return res.status(404).send("logo: no logo available for this title.");

    // Pass-through fetch — the underlying PNG comes from TMDB's CDN.
    const r = await fetch(logoUrl, { headers: { "User-Agent": UA }, signal: AbortSignal.timeout(20000) });
    if (!r.ok) return res.status(502).send(`logo: upstream HTTP ${r.status}`);
    const buf = Buffer.from(await r.arrayBuffer());
    res.setHeader("Content-Type", "image/png");
    res.setHeader("Cache-Control", "public, max-age=86400");
    res.setHeader("X-Logo-Source", "tmdb");
    res.send(buf);
  } catch (err) {
    console.error(`[logo] error:`, err);
    res.status(500).send(`logo error: ${err.message}`);
  }
});

/* ─── API: Server caps ─────────────────────────────────────────────────────
   The configurator probes this on load (PostersPlus upstream contract);
   returning it kills the console 404 and lets the page enable the fields
   this instance actually supports. */
app.get("/server-caps", (_req, res) => {
  res.setHeader("Cache-Control", "no-cache");
  res.json({
    access_key_required: false,
    admin_link: false,
    tmdb_key_set: !!TMDB_KEY,
    mdblist_key_set: false,
    mdblist_key_count: 0,
    aiostreams_configured: false,
    quality_configured: false,
    imdb_dataset_enabled: false,
    imdb_dataset_titles: 0,
    max_poster_resolution: 2000,
    preview_at_resolution: true,
    random_posters: false,
    anime_sources: false,
    tvdb_posters: false,
    fanart_posters: false,
    custom_fonts: [],
    operator_presets: [],
    trending_lists: { enabled: false,
      lists: { movie: { default: "imdb" }, tv: { default: "imdb" }, anime: { default: "kitsu" }, anime_movie: { default: "kitsu" } },
      languages: [], max_picks: 8, top: "", home_default: false },
    trending_fetch_count: 0,
    trending_fetch_time: null,
    param_defaults: {},
    param_defaults_landscape: {},
    param_aliases: {},
    weight_source_aliases: {},
    sash_priority_default: null,
    sash_priority_diff_seed: null,
  });
});

/* ─── API: Configurator title search ───────────────────────────────────────
   TMDB multi-search with the user's key or the server's own; without any
   key it falls back to Cinemeta (IMDb-keyed, key-less) exactly like
   PostersPlus upstream, so Search always returns something. */
app.get("/search", async (req, res) => {
  try {
    const q = String(req.query.q || "").trim();
    if (!q) return res.status(400).json({ detail: "Empty query" });
    if (q.length > 200) return res.status(400).json({ detail: "Query too long" });
    const userKey = String(req.query.tmdb_key || "").trim();
    const effKey = userKey || TMDB_KEY;
    if (effKey) {
      try {
        const d = await fetchJson(
          `https://api.themoviedb.org/3/search/multi?api_key=${encodeURIComponent(effKey)}` +
          `&query=${encodeURIComponent(q)}&include_adult=false&page=1`,
          10 * 60 * 1000
        );
        const results = (d.results || []).filter(r => r.media_type === "movie" || r.media_type === "tv");
        return res.json({ results, source: "tmdb" });
      } catch (e) {
        console.error(`[search] TMDB failed, falling back to Cinemeta: ${e.message}`);
      }
    }
    // Key-less fallback — Cinemeta top-search per type, shaped like TMDB's
    // response so the configurator reads it unchanged (id is null; renders
    // resolve through the IMDb id).
    const perType = await Promise.all(["movie", "series"].map(async (t) => {
      try {
        const d = await fetchJson(`https://v3-cinemeta.strem.io/catalog/${t}/top/search=${encodeURIComponent(q)}.json`, 30 * 60 * 1000);
        const year = m => (m.releaseInfo || "").split(/\D/)[0] || "";
        return (d.metas || []).slice(0, 10).map(m => ({
          id: null,
          imdb_id: m.imdb_id || m.id || "",
          media_type: t === "series" ? "tv" : "movie",
          title: m.name,
          name: m.name,
          release_date: year(m) ? `${year(m)}-01-01` : "",
          first_air_date: year(m) ? `${year(m)}-01-01` : "",
          poster_url: m.poster || null,
        }));
      } catch (e) { return []; }
    }));
    res.json({ results: [...perType[0], ...perType[1]], source: "cinemeta" });
  } catch (err) {
    console.error("[search] error:", err);
    res.status(500).json({ detail: "search failed" });
  }
});

/* ─── API: Stats ──────────────────────────────────────────────────────────── */
app.get("/api/stats", (_req, res) => {
  res.json({ ...getDbStats(), countries: COUNTRIES.length, platforms: PLATFORMS.length });
});

/* ─── Start server ────────────────────────────────────────────────────────── */
app.listen(PORT, () => {
  console.log(`\n  ╔══════════════════════════════════════════════════╗`);
  console.log(`  ║  TOPX Posters — Combined Addon                   ║`);
  console.log(`  ╚══════════════════════════════════════════════════╝`);
  console.log(`  Port: ${PORT}`);
  console.log(`  Countries: ${COUNTRIES.length}`);
  console.log(`  Platforms: ${PLATFORMS.length}`);
  console.log(`  Poster shapes: ${POSTER_SHAPES.join(", ")}`);
  console.log(`  Canvas backgrounds: ${CANVAS_BACKGROUNDS.join(", ")}`);
  console.log(`  Styles: ${STYLES.join(", ")}`);
  console.log(`  Manifest: http://localhost:${PORT}/manifest.json\n`);
});

/* ─── Boot warm-up: pull the default world chart artwork (top 3 of both
       types) into getTmdbImages' memory cache so the first configurator
       visit renders composite-only instead of paying the TMDB round trips.
       Fire-and-forget — failures are harmless. ─── */
(async () => {
  for (const t of ["movie", "series"]) {
    try {
      const metas = await fetchTop10("world", "topx", t);
      for (const m of metas.slice(0, 3)) {
        try { await getTmdbImages(t === "series" ? "tv" : "movie", m.tmdbId, TMDB_KEY, "world"); } catch (e) { /* skip */ }
      }
    } catch (e) { /* warm-up best effort */ }
  }
  console.log("  [warmup] default chart artwork cached");
})();
