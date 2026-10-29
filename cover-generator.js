import sharp from "sharp";
import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";
import { getAssetBuffer, saveAssetBuffer } from "./db.js";

// Limit Sharp to 1 worker thread and constrain in-memory cache to prevent CPU/RAM saturation
sharp.concurrency(1);
sharp.cache({ memory: 32, items: 50 });

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const NETWORKS_DIR = path.join(__dirname, "public", "networks");
const ICONS_DIR = path.join(__dirname, "public", "icons");
const FONTS_DIR = path.join(__dirname, "public", "fonts");
const ICON_CACHE = new Map();

/* ─── PostersPlus-inspired font registry ───────────────────────────────────
 * Each entry maps a friendly name (used in the configurator's font picker)
 * to a TTF file in public/fonts/. The TTF is loaded once and embedded as a
 * base64 @font-face inside the rank-number SVG so sharp's librsvg renders
 * with the correct glyphs.
 */
const FONT_REGISTRY = {
  "Inter":             "Inter-Bold.ttf",
  "Space Grotesk":     "SpaceGrotesk-Bold.ttf",
  "Bebas Neue":        "BebasNeue-Bold.ttf",
  "Oswald":            "Oswald-Bold.ttf",
  "Montserrat":        "Montserrat-Bold.ttf",
  "Manrope":           "Manrope-Bold.ttf",
  "Fira Sans":         "FiraSans-Bold.ttf",
  "Roboto Condensed":  "RobotoCondensed-Bold.ttf",
  "Playfair Display":  "PlayfairDisplay-Bold.ttf",
};
const FONT_FACE_CACHE = new Map(); // fontFamily → base64-data-url string

/* PostersPlus label_font value → our registry key. Unknown values fall back
 * to Inter so a PostersPlus-format URL never breaks the renderer. */
const FONT_ALIASES = {
  "inter": "Inter",
  "rubik": "Montserrat",
  "almarai": "Manrope",
  "oswald": "Oswald",
  "oswaldlabel": "Oswald",
  "bebas": "Bebas Neue",
  "bebas neue": "Bebas Neue",
  "montserrat": "Montserrat",
  "manrope": "Manrope",
  "fira": "Fira Sans",
  "fira sans": "Fira Sans",
  "robotocondensed": "Roboto Condensed",
  "roboto condensed": "Roboto Condensed",
  "spacegrotesk": "Space Grotesk",
  "space grotesk": "Space Grotesk",
  "playfair": "Playfair Display",
  "playfair display": "Playfair Display",
  "plusjakarta": "Manrope",
  "exo2": "Fira Sans",
  "notoserif": "Playfair Display",
  "barlowcondensed": "Roboto Condensed",
  "opensans": "Inter",
  "pacifico": "Inter",
  "creepster": "Oswald",
  "ubuntu": "Inter",
};

/** Maps any font identifier (registry display name or PostersPlus label_font
 *  value) to a font in FONT_REGISTRY. Falls back to Inter. */
export function normalizeFontFamily(f) {
  if (!f) return "Inter";
  const raw = String(f).trim();
  if (FONT_REGISTRY[raw]) return raw;
  const key = raw.toLowerCase();
  if (FONT_ALIASES[key]) return FONT_ALIASES[key];
  return "Inter";
}

/**
 * Returns a CSS @font-face block (as a string) that the SVG can embed in a
 * <style> element. Falls back to "" if the font file is missing.
 */
function fontFaceXml(fontFamily) {
  if (!fontFamily || !FONT_REGISTRY[fontFamily]) return "";
  if (FONT_FACE_CACHE.has(fontFamily)) return FONT_FACE_CACHE.get(fontFamily);
  const filename = FONT_REGISTRY[fontFamily];
  const filePath = path.join(FONTS_DIR, filename);
  if (!fs.existsSync(filePath)) {
    FONT_FACE_CACHE.set(fontFamily, "");
    return "";
  }
  const buf = fs.readFileSync(filePath);
  const b64 = buf.toString("base64");
  const css = `@font-face { font-family: "${fontFamily}"; src: url(data:font/ttf;base64,${b64}) format("truetype"); }`;
  FONT_FACE_CACHE.set(fontFamily, css);
  return css;
}

/** Returns the font-family CSS string for an SVG text element, with the
 *  chosen PostersPlus font first and Inter as fallback. Uses single quotes
 *  around the family name so it can be safely embedded inside a double-quoted
 *  font-family="..." attribute. */
function svgFontStack(fontFamily) {
  if (fontFamily && FONT_REGISTRY[fontFamily]) {
    return `'${fontFamily}', Inter, Arial, Helvetica, sans-serif`;
  }
  return `Inter, Arial, Helvetica, sans-serif`;
}

/* ─── PostersPlus-style vignette ───────────────────────────────────────────
 * PostersPlus models the vignette as independent top/bottom gradient bands:
 *   top_gradient / bottom_gradient        — named level: off|low|medium|high
 *   top_gradient_opacity / bottom_…       — explicit 0..1 override
 *   top_gradient_height / bottom_…        — explicit 0..1 (fraction of canvas)
 * Named levels map to tuned opacity/height pairs; explicit numbers win.
 */
const GRADIENT_LEVELS  = { off: 0, none: 0, low: 0.35, medium: 0.6, high: 0.85 };
const GRADIENT_HEIGHTS = { off: 0, none: 0, low: 0.15, medium: 0.25, high: 0.35 };

function gradOpacity(v) {
  if (v == null || v === "") return null;
  const s = String(v).toLowerCase().trim();
  if (s in GRADIENT_LEVELS) return GRADIENT_LEVELS[s];
  const n = Number(s);
  return Number.isFinite(n) ? Math.max(0, Math.min(1, n)) : null;
}

function gradHeight(v) {
  if (v == null || v === "") return null;
  const s = String(v).toLowerCase().trim();
  if (s in GRADIENT_HEIGHTS) return GRADIENT_HEIGHTS[s];
  const n = Number(s);
  return Number.isFinite(n) ? Math.max(0, Math.min(1, n)) : null;
}

/** Builds a normalized vignette options object from loose inputs:
 *  - numeric (legacy blvckTOP-style intensity 0..3) → both sides
 *  - object {top, bottom, topOpacity, topHeight, bottomOpacity, bottomHeight,
 *            colorTop, colorBottom, colorSat, colorLight, colorBlur, colorStyle}
 *  Returns {topOp, topHt, botOp, botHt, color…} — all 0 when off.
 *  colorTop/colorBottom enable the PostersPlus "poster color" vignette: the
 *  band is tinted with the artwork's dominant colour instead of black. */
export function normalizeVignetteOpts(opts) {
  const off = { topOp: 0, topHt: 0, botOp: 0, botHt: 0, colorTop: false, colorBottom: false, colorSat: 2.5, colorLight: 1.3, colorBlur: 1, colorStyle: "shade" };
  if (opts == null || opts === "") return off;
  if (typeof opts === "number" || typeof opts === "string") {
    const n = Number(opts) || 0;
    if (n < 1) return off;
    const op = [0, 0.35, 0.55, 0.75][Math.min(n, 3)];
    const ht = [0, 0.18, 0.25, 0.32][Math.min(n, 3)];
    return { ...off, topOp: op, topHt: ht, botOp: op, botHt: ht };
  }
  // object form
  const topLevel = gradOpacity(opts.top);
  const botLevel = gradOpacity(opts.bottom);
  const topOp = opts.topOpacity != null ? gradOpacity(opts.topOpacity) : topLevel;
  const botOp = opts.bottomOpacity != null ? gradOpacity(opts.bottomOpacity) : botLevel;
  const topHtDef = gradHeight(opts.top);
  const botHtDef = gradHeight(opts.bottom);
  const topHt = opts.topHeight != null ? gradHeight(opts.topHeight) : (topHtDef ?? (topOp ? 0.25 : 0));
  const botHt = opts.bottomHeight != null ? gradHeight(opts.bottomHeight) : (botHtDef ?? (botOp ? 0.25 : 0));
  const truthy = v => v === true || v === "true" || v === 1 || v === "1";
  const numOr = (v, d) => { const n = Number(v); return Number.isFinite(n) && n > 0 ? n : d; };
  return {
    topOp: topOp || 0, topHt: topOp ? topHt : 0,
    botOp: botOp || 0, botHt: botOp ? botHt : 0,
    colorTop: truthy(opts.colorTop),
    colorBottom: truthy(opts.colorBottom),
    colorSat: numOr(opts.colorSat, 2.5),
    colorLight: numOr(opts.colorLight, 1.3),
    colorBlur: numOr(opts.colorBlur, 1),
    colorStyle: String(opts.colorStyle || "shade").toLowerCase().trim(),
  };
}

/** Dominant colour of an artwork buffer (sharp stats) as a #rrggbb string —
 *  used by the PostersPlus "poster colour" vignette. Falls back to black. */
async function dominantColor(artBuffer) {
  try {
    const stats = await sharp(artBuffer).stats();
    const [r, g, b] = stats.dominant ? stats.dominant : { r: 0, g: 0, b: 0 };
    const hex = n => Math.max(0, Math.min(255, Math.round(n))).toString(16).padStart(2, "0");
    return `#${hex(r)}${hex(g)}${hex(b)}`;
  } catch {
    return "#000000";
  }
}

/** Returns a sharp overlay buffer drawing independent top + bottom gradient
 *  bands. Accepts a legacy numeric intensity or the PostersPlus-style options
 *  object. When the colour vignette is on (vignette_poster_color_top/bottom),
 *  the bands are tinted with the artwork's dominant colour (passed as
 *  `artColor`) and softened by the colour blur factor. Returns null when both
 *  bands are off. Works with either a LAYOUTS entry (nested
 *  `canvas: {width,height}`) or a BANNER_LAYOUTS entry (flat width/height). */
async function vignetteOverlay(layout, opts = 0, artColor = "#000000") {
  const v = normalizeVignetteOpts(opts);
  if (!v.topOp && !v.botOp) return null;
  const canvas = layout?.canvas || layout;
  const W = Number(canvas?.width);
  const H = Number(canvas?.height);
  if (!W || !H) return null;
  const topHt = Math.round(H * (v.topHt || 0));
  const botHt = Math.round(H * (v.botHt || 0));
  // PostersPlus colour vignette: tint = dominant artwork colour, lightened by
  // vignette_color_lightness and saturated by vignette_color_saturation.
  const useColor = v.colorTop || v.colorBottom;
  let tint = "#000000";
  if (useColor) {
    const m = /^#([0-9a-fA-F]{6})$/.exec(artColor);
    if (m) {
      const int = parseInt(m[1], 16);
      let r = (int >> 16) & 255, g = (int >> 8) & 255, b = int & 255;
      // saturation around luminance, then lightness lift
      const lum = 0.2126 * r + 0.7152 * g + 0.0722 * b;
      r = Math.max(0, Math.min(255, lum + (r - lum) * v.colorSat));
      g = Math.max(0, Math.min(255, lum + (g - lum) * v.colorSat));
      b = Math.max(0, Math.min(255, lum + (b - lum) * v.colorSat));
      const lift = v.colorLight - 1;
      r += 255 * lift * 0.28; g += 255 * lift * 0.28; b += 255 * lift * 0.28;
      const hex = n => Math.max(0, Math.min(255, Math.round(n))).toString(16).padStart(2, "0");
      tint = `#${hex(r)}${hex(g)}${hex(b)}`;
    }
  }
  // colour blur softens the band edge (0..1 → extra gradient stop spread)
  const blur = Math.max(0, Math.min(1, Number(v.colorBlur) || 0));
  const topTint = v.colorTop ? tint : "#000000";
  const botTint = v.colorBottom ? tint : "#000000";
  const topStops = blur > 0
    ? `<stop offset="0%" stop-color="${topTint}" stop-opacity="${v.topOp}"/>
       <stop offset="${Math.round(40 + blur * 30)}%" stop-color="${topTint}" stop-opacity="${(v.topOp * 0.45).toFixed(3)}"/>
       <stop offset="100%" stop-color="${topTint}" stop-opacity="0"/>`
    : `<stop offset="0%" stop-color="${topTint}" stop-opacity="${v.topOp}"/>
       <stop offset="100%" stop-color="${topTint}" stop-opacity="0"/>`;
  const botStops = blur > 0
    ? `<stop offset="0%" stop-color="${botTint}" stop-opacity="0"/>
       <stop offset="${Math.round(60 - blur * 30)}%" stop-color="${botTint}" stop-opacity="${(v.botOp * 0.45).toFixed(3)}"/>
       <stop offset="100%" stop-color="${botTint}" stop-opacity="${v.botOp}"/>`
    : `<stop offset="0%" stop-color="${botTint}" stop-opacity="0"/>
       <stop offset="100%" stop-color="${botTint}" stop-opacity="${v.botOp}"/>`;
  const svg = `
    <svg width="${W}" height="${H}" xmlns="http://www.w3.org/2000/svg">
      <defs>
        <linearGradient id="vigTop" x1="0" y1="0" x2="0" y2="1">${topStops}</linearGradient>
        <linearGradient id="vigBot" x1="0" y1="0" x2="0" y2="1">${botStops}</linearGradient>
      </defs>
      ${topHt > 0 ? `<rect x="0" y="0" width="${W}" height="${topHt}" fill="url(#vigTop)"/>` : ""}
      ${botHt > 0 ? `<rect x="0" y="${H - botHt}" width="${W}" height="${botHt}" fill="url(#vigBot)"/>` : ""}
    </svg>
  `;
  return sharp(Buffer.from(svg)).png().toBuffer();
}

function loadSvg(filename, fromNetworks = false) {
  const dir = fromNetworks ? NETWORKS_DIR : ICONS_DIR;
  const key = `${fromNetworks ? 'net:' : 'icon:'}${filename}`;
  if (ICON_CACHE.has(key)) return ICON_CACHE.get(key);
  try {
    const filePath = path.join(dir, filename);
    if (!fs.existsSync(filePath)) {
      const fallbackPath = path.join(fromNetworks ? ICONS_DIR : NETWORKS_DIR, filename);
      if (fs.existsSync(fallbackPath)) {
        const content = fs.readFileSync(fallbackPath, "utf8");
        ICON_CACHE.set(key, content);
        return content;
      }
      return null;
    }
    const content = fs.readFileSync(filePath, "utf8");
    ICON_CACHE.set(key, content);
    return content;
  } catch {
    return null;
  }
}

const pendingDownloads = new Map();

const LAYOUTS = {
  landscape: {
    canvas: { width: 1280, height: 720 },
    card: {
      x: 300,
      y: 107,
      width: 900,
      height: 506,
      radius: 30
    },
    number: {
      xSingle: 62,
      xDouble: 24,
      sizeSingle: 450,
      sizeDouble: 370,
      opticalDrop: 8
    },
    logo: {
      maxWidth: 380,
      maxHeight: 155,
      left: 46,
      bottom: 40
    }
  },

  poster: {
    canvas: { width: 1000, height: 1500 },
    card: {
      x: 210,
      y: 165,
      width: 730,
      height: 1095,
      radius: 38
    },
    number: {
      xSingle: 14,
      xDouble: -10,
      sizeSingle: 405,
      sizeDouble: 335,
      opticalDrop: 22
    },
    logo: {
      maxWidth: 350,
      maxHeight: 145,
      left: 40,
      bottom: 42
    }
  }
};

export function normalizedShape(shape) {
  return shape === "poster" || shape === "portrait"
    ? "poster"
    : "landscape";
}

async function fetchBuffer(url) {
  // 1. Check persistent disk cache first
  const diskCached = getAssetBuffer(url);
  if (diskCached) return diskCached;

  // 2. Check pending parallel downloads
  if (pendingDownloads.has(url)) {
    return pendingDownloads.get(url);
  }

  const promise = (async () => {
    const response = await fetch(url, {
      headers: {
        "User-Agent": "blvckTOP/7.2"
      }
    });

    if (!response.ok) {
      throw new Error(`Download immagine fallito (${response.status}) da ${url}`);
    }

    const buffer = Buffer.from(await response.arrayBuffer());
    saveAssetBuffer(url, buffer);
    return buffer;
  })();

  pendingDownloads.set(url, promise);

  try {
    return await promise;
  } finally {
    pendingDownloads.delete(url);
  }
}

function hexToRgb(hex) {
  const clean = String(hex || "#ffffff").replace("#", "");
  const value = clean.length === 3
    ? clean.split("").map(x => x + x).join("")
    : clean.padEnd(6, "f").slice(0, 6);

  return {
    r: parseInt(value.slice(0, 2), 16),
    g: parseInt(value.slice(2, 4), 16),
    b: parseInt(value.slice(4, 6), 16)
  };
}

function escapeXml(str) {
  if (!str) return "";
  return String(str)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&apos;");
}

function formatRating(raw) {
  if (!raw) return "";
  const num = parseFloat(String(raw).replace(",", "."));
  if (isNaN(num) || num <= 0) return "";
  return num.toFixed(1);
}

function cleanGenre(raw) {
  if (!raw) return "";
  let g = String(raw).trim();
  if (g.includes(",")) g = g.split(",")[0].trim();
  if (g.includes("/")) g = g.split("/")[0].trim();
  return g.toUpperCase();
}

function getBrandPalette(accent = "#8C75FF", catalogKey = "") {
  const key = String(catalogKey || "").toLowerCase();
  
  if (key.includes("netflix") || accent.toUpperCase() === "#E50914") {
    return {
      atmo: { c1: "#3d080c", c2: "#1f0305", c3: "#0a0102" },
      body: { c1: "#570a10", c2: "#38060a", c3: "#1a0204", c4: "#050001" },
      stroke: { s1: "#FFFFFF", s2: "#FECDD3", s3: "#F43F5E", s4: "#881337", s5: "#FDA4AF", s6: "#FFFFFF" },
      sheen: { c1: "#FFFFFF", c2: "#FECDD3" },
      bounce: { c1: "#E11D48", c2: "#FDA4AF" }
    };
  }

  if (key.includes("prime") || key.includes("amazon") || accent.toUpperCase() === "#00A8E1") {
    return {
      atmo: { c1: "#002847", c2: "#001324", c3: "#00050d" },
      body: { c1: "#004073", c2: "#002447", c3: "#001021", c4: "#00040a" },
      stroke: { s1: "#FFFFFF", s2: "#BAE6FD", s3: "#38BDF8", s4: "#0284C7", s5: "#7DD3FC", s6: "#FFFFFF" },
      sheen: { c1: "#FFFFFF", c2: "#BAE6FD" },
      bounce: { c1: "#0284C7", c2: "#7DD3FC" }
    };
  }

  if (key.includes("now") || key.includes("sky") || accent.toUpperCase() === "#00E575" || accent.toUpperCase() === "#00CFFF") {
    return {
      atmo: { c1: "#003319", c2: "#00170a", c3: "#000603" },
      body: { c1: "#005229", c2: "#003319", c3: "#00170b", c4: "#000502" },
      stroke: { s1: "#FFFFFF", s2: "#BBF7D0", s3: "#4ADE80", s4: "#15803D", s5: "#86EFAC", s6: "#FFFFFF" },
      sheen: { c1: "#FFFFFF", c2: "#BBF7D0" },
      bounce: { c1: "#16A34A", c2: "#86EFAC" }
    };
  }

  if (key.includes("disney") || accent.toUpperCase() === "#2D7DFF" || accent.toUpperCase() === "#155EEF") {
    return {
      atmo: { c1: "#0c1d42", c2: "#060e21", c3: "#02040a" },
      body: { c1: "#142d69", c2: "#0c1d42", c3: "#060d1f", c4: "#02040a" },
      stroke: { s1: "#FFFFFF", s2: "#BFDBFE", s3: "#60A5FA", s4: "#1D4ED8", s5: "#93C5FD", s6: "#FFFFFF" },
      sheen: { c1: "#FFFFFF", c2: "#BFDBFE" },
      bounce: { c1: "#2563EB", c2: "#93C5FD" }
    };
  }

  if (key.includes("apple") || accent.toUpperCase() === "#D8DFEA" || accent.toUpperCase() === "#F4F5F7") {
    return {
      atmo: { c1: "#26292e", c2: "#14161a", c3: "#08080a" },
      body: { c1: "#3c4048", c2: "#26292e", c3: "#14161a", c4: "#060708" },
      stroke: { s1: "#FFFFFF", s2: "#F1F5F9", s3: "#94A3B8", s4: "#475569", s5: "#CBD5E1", s6: "#FFFFFF" },
      sheen: { c1: "#FFFFFF", c2: "#F1F5F9" },
      bounce: { c1: "#64748B", c2: "#E2E8F0" }
    };
  }

  if (key.includes("hbo") || key.includes("max") || accent.toUpperCase() === "#7D57FF") {
    return {
      atmo: { c1: "#2c0b47", c2: "#160524", c3: "#07010d" },
      body: { c1: "#4a1478", c2: "#2c0b47", c3: "#130421", c4: "#040108" },
      stroke: { s1: "#FFFFFF", s2: "#DDD6FE", s3: "#A78BFA", s4: "#6D28D9", s5: "#C4B5FD", s6: "#FFFFFF" },
      sheen: { c1: "#FFFFFF", c2: "#DDD6FE" },
      bounce: { c1: "#7C3AED", c2: "#C4B5FD" }
    };
  }

  if (key.includes("paramount") || accent.toUpperCase() === "#0064FF") {
    return {
      atmo: { c1: "#001a44", c2: "#000d24", c3: "#00040d" },
      body: { c1: "#002a6e", c2: "#001a44", c3: "#000d24", c4: "#00030a" },
      stroke: { s1: "#FFFFFF", s2: "#BAE6FD", s3: "#38BDF8", s4: "#0284C7", s5: "#7DD3FC", s6: "#FFFFFF" },
      sheen: { c1: "#FFFFFF", c2: "#BAE6FD" },
      bounce: { c1: "#0284C7", c2: "#7DD3FC" }
    };
  }

  if (key.includes("rai") || accent.toUpperCase() === "#1C6DFF" || accent.toUpperCase() === "#0066CC") {
    return {
      atmo: { c1: "#002047", c2: "#001026", c3: "#00050f" },
      body: { c1: "#003575", c2: "#002047", c3: "#001026", c4: "#00040a" },
      stroke: { s1: "#FFFFFF", s2: "#BAE6FD", s3: "#38BDF8", s4: "#0369A1", s5: "#7DD3FC", s6: "#FFFFFF" },
      sheen: { c1: "#FFFFFF", c2: "#BAE6FD" },
      bounce: { c1: "#0284C7", c2: "#7DD3FC" }
    };
  }

  if (key.includes("infinity") || key.includes("mediaset") || accent.toUpperCase() === "#00A3E0") {
    return {
      atmo: { c1: "#002538", c2: "#00121d", c3: "#00060b" },
      body: { c1: "#003d5c", c2: "#002538", c3: "#00121d", c4: "#000508" },
      stroke: { s1: "#FFFFFF", s2: "#BAE6FD", s3: "#38BDF8", s4: "#0284C7", s5: "#7DD3FC", s6: "#FFFFFF" },
      sheen: { c1: "#FFFFFF", c2: "#BAE6FD" },
      bounce: { c1: "#0284C7", c2: "#7DD3FC" }
    };
  }

  // Generic calculated from accent RGB
  const rgb = hexToRgb(accent);
  const r1 = Math.round(rgb.r * 0.35), g1 = Math.round(rgb.g * 0.35), b1 = Math.round(rgb.b * 0.35);
  const r2 = Math.round(rgb.r * 0.18), g2 = Math.round(rgb.g * 0.18), b2 = Math.round(rgb.b * 0.18);
  const r3 = Math.round(rgb.r * 0.05), g3 = Math.round(rgb.g * 0.05), b3 = Math.round(rgb.b * 0.05);

  return {
    atmo: { c1: `rgb(${r1},${g1},${b1})`, c2: `rgb(${r2},${g2},${b2})`, c3: `rgb(${r3},${g3},${b3})` },
    body: { c1: `rgb(${Math.round(rgb.r * 0.5)},${Math.round(rgb.g * 0.5)},${Math.round(rgb.b * 0.5)})`, c2: `rgb(${r1},${g1},${b1})`, c3: `rgb(${r2},${g2},${b2})`, c4: `rgb(${r3},${g3},${b3})` },
    stroke: { s1: "#FFFFFF", s2: "#E0E7FF", s3: accent, s4: `rgb(${r1},${g1},${b1})`, s5: "#C7D2FE", s6: "#FFFFFF" },
    sheen: { c1: "#FFFFFF", c2: "#E0E7FF" },
    bounce: { c1: accent, c2: "#FFFFFF" }
  };
}

function generateGlassBackground(width, height, mode = "stremio", accent = "#8C75FF", catalogKey = "") {
  if (mode === "black" || mode === "nero") {
    return Buffer.from(`
      <svg width="${width}" height="${height}" xmlns="http://www.w3.org/2000/svg">
        <rect width="${width}" height="${height}" fill="#000000"/>
      </svg>
    `);
  }
  if (mode === "transparent") {
    return Buffer.from(`
      <svg width="${width}" height="${height}" xmlns="http://www.w3.org/2000/svg"/>
    `);
  }

  const isProvider = mode === "provider" || mode === "brand";
  const pal = isProvider ? getBrandPalette(accent, catalogKey) : {
    atmo: { c1: "#2e2569", c2: "#1b1642", c3: "#0a081c" },
    body: { c1: "#483896", c2: "#322673", c3: "#1d1647", c4: "#050410" },
    stroke: { s1: "#FFFFFF", s2: "#E0E7FF", s3: "#A5B4FC", s4: "#6366F1", s5: "#C7D2FE", s6: "#FFFFFF" },
    sheen: { c1: "#FFFFFF", c2: "#C7D2FE" },
    bounce: { c1: "#818CF8", c2: "#C7D2FE" }
  };

  const pad = 12;
  const rw = width - pad * 2;
  const rh = height - pad * 2;
  const rx = Math.round(width * 0.032);
  const strokeW = width > 1100 ? 4.0 : 3.5;

  return Buffer.from(`
    <svg width="${width}" height="${height}" xmlns="http://www.w3.org/2000/svg">
      <defs>
        <!-- Canvas Ambient Atmosphere -->
        <radialGradient id="glassAtmosphere" cx="50%" cy="25%" r="75%">
          <stop offset="0%" stop-color="${pal.atmo.c1}"/>
          <stop offset="42%" stop-color="${pal.atmo.c2}"/>
          <stop offset="100%" stop-color="${pal.atmo.c3}"/>
        </radialGradient>

        <!-- Liquid Glass Card Interior: Strong top gradient flowing into ultra-deep dark bottom -->
        <linearGradient id="glassBodyGrad" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stop-color="${pal.body.c1}" stop-opacity="0.95"/>
          <stop offset="20%" stop-color="${pal.body.c2}" stop-opacity="0.92"/>
          <stop offset="50%" stop-color="${pal.body.c3}" stop-opacity="0.95"/>
          <stop offset="80%" stop-color="${pal.atmo.c3}" stop-opacity="0.98"/>
          <stop offset="100%" stop-color="${pal.body.c4}" stop-opacity="1.0"/>
        </linearGradient>

        <!-- Top Internal Glass Light Sheen -->
        <linearGradient id="innerGlassSheen" x1="0" y1="0" x2="0.8" y2="0.6">
          <stop offset="0%" stop-color="${pal.sheen.c1}" stop-opacity="0.28"/>
          <stop offset="35%" stop-color="${pal.sheen.c2}" stop-opacity="0.10"/>
          <stop offset="100%" stop-color="${pal.bounce.c1}" stop-opacity="0"/>
        </linearGradient>

        <!-- 3D Liquid Glass Border Rim -->
        <linearGradient id="glassStrokeGrad" x1="0" y1="0" x2="0.75" y2="1">
          <stop offset="0%" stop-color="${pal.stroke.s1}" stop-opacity="1.0"/>
          <stop offset="16%" stop-color="${pal.stroke.s2}" stop-opacity="0.90"/>
          <stop offset="40%" stop-color="${pal.stroke.s3}" stop-opacity="0.55"/>
          <stop offset="70%" stop-color="${pal.stroke.s4}" stop-opacity="0.38"/>
          <stop offset="88%" stop-color="${pal.stroke.s5}" stop-opacity="0.70"/>
          <stop offset="100%" stop-color="${pal.stroke.s6}" stop-opacity="0.90"/>
        </linearGradient>

        <!-- Inner Bevel Stroke -->
        <linearGradient id="innerBevelGrad" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stop-color="#FFFFFF" stop-opacity="0.60"/>
          <stop offset="25%" stop-color="#FFFFFF" stop-opacity="0.12"/>
          <stop offset="65%" stop-color="#000000" stop-opacity="0"/>
          <stop offset="100%" stop-color="#000000" stop-opacity="0.65"/>
        </linearGradient>

        <!-- Top Edge Specular Flare -->
        <linearGradient id="topFlare" x1="0" y1="0" x2="1" y2="0">
          <stop offset="0%" stop-color="#FFFFFF" stop-opacity="0"/>
          <stop offset="20%" stop-color="#FFFFFF" stop-opacity="0.90"/>
          <stop offset="50%" stop-color="#FFFFFF" stop-opacity="1.0"/>
          <stop offset="80%" stop-color="#FFFFFF" stop-opacity="0.90"/>
          <stop offset="100%" stop-color="#FFFFFF" stop-opacity="0"/>
        </linearGradient>

        <!-- Bottom Rim Light Bounce -->
        <linearGradient id="bottomRimFlare" x1="0" y1="0" x2="1" y2="0">
          <stop offset="0%" stop-color="${pal.bounce.c1}" stop-opacity="0"/>
          <stop offset="30%" stop-color="${pal.bounce.c2}" stop-opacity="0.60"/>
          <stop offset="50%" stop-color="#FFFFFF" stop-opacity="0.85"/>
          <stop offset="70%" stop-color="${pal.bounce.c2}" stop-opacity="0.60"/>
          <stop offset="100%" stop-color="${pal.bounce.c1}" stop-opacity="0"/>
        </linearGradient>

        <!-- Outer Drop Shadow -->
        <filter id="glassShadow" x="-15%" y="-15%" width="130%" height="130%">
          <feDropShadow dx="0" dy="12" stdDeviation="22" flood-color="#020105" flood-opacity="0.94"/>
        </filter>
      </defs>

      <!-- Atmosphere Fill -->
      <rect width="${width}" height="${height}" fill="url(#glassAtmosphere)"/>

      <!-- Liquid Glass Container -->
      <g filter="url(#glassShadow)">
        <rect
          x="${pad}"
          y="${pad}"
          width="${rw}"
          height="${rh}"
          rx="${rx}"
          ry="${rx}"
          fill="url(#glassBodyGrad)"
          stroke="url(#glassStrokeGrad)"
          stroke-width="${strokeW}"
        />
        <rect
          x="${pad + 1.5}"
          y="${pad + 1.5}"
          width="${rw - 3}"
          height="${rh - 3}"
          rx="${rx - 1.5}"
          ry="${rx - 1.5}"
          fill="url(#innerGlassSheen)"
          stroke="url(#innerBevelGrad)"
          stroke-width="1.5"
        />
      </g>

      <!-- Top Lip -->
      <path
        d="M ${pad + rx + 8} ${pad + 2} Q ${width / 2} ${pad + 1.2} ${width - pad - rx - 8} ${pad + 2}"
        stroke="url(#topFlare)"
        stroke-width="${strokeW}"
        stroke-linecap="round"
        fill="none"
      />

      <!-- Bottom Lip -->
      <path
        d="M ${pad + rx + 14} ${height - pad - 2} Q ${width / 2} ${height - pad - 1.2} ${width - pad - rx - 14} ${height - pad - 2}"
        stroke="url(#bottomRimFlare)"
        stroke-width="2.5"
        stroke-linecap="round"
        fill="none"
      />
    </svg>
  `);
}

export function getProviderLogoSvg(catalogKey = "") {
  const key = String(catalogKey || "").toLowerCase().trim();

  // 1. Netflix (N ribbon from Pictorium)
  if (key.includes("netflix")) {
    const svg = loadSvg("Netflix_2016_N_logo.svg", true) || loadSvg("netflix.svg");
    return svg ? { width: 14, height: 26, svg } : null;
  }

  // 2. Prime Video (2024 logo from Pictorium)
  if (key.includes("prime") || key.includes("amazon")) {
    const svg = loadSvg("Prime_Video_logo_(2024).svg", true) || loadSvg("prime-logo.svg");
    return svg ? { width: 78, height: 24, svg } : null;
  }

  // 3. Disney+ (from Pictorium)
  if (key.includes("disney")) {
    const svg = loadSvg("Disney+_logo.svg", true) || loadSvg("disney.svg");
    return svg ? { width: 48, height: 26, svg } : null;
  }

  // 4. Apple TV+ (from Pictorium)
  if (key.includes("apple")) {
    const svg = loadSvg("Apple_TV_logo.svg", true) || loadSvg("apple.svg");
    return svg ? { width: 48, height: 24, svg } : null;
  }

  // 5. HBO / Max (from Pictorium)
  if (key.includes("hbo") || key.includes("max")) {
    const svg = loadSvg("HBO_logo.svg", true) || loadSvg("hbomax.svg");
    return svg ? { width: 58, height: 24, svg } : null;
  }

  // 6. Paramount+ (from Pictorium)
  if (key.includes("paramount")) {
    const svg = loadSvg("Paramount_Plus.svg", true) || loadSvg("paramount.svg");
    return svg ? { width: 42, height: 26, svg } : null;
  }

  // 7. NOW / Sky (from Pictorium)
  if (key.includes("now") || key.includes("sky")) {
    const svg = loadSvg("Now_logo.svg", true) || loadSvg("now.svg");
    return svg ? { width: 71, height: 22, svg } : null;
  }

  // 8. RaiPlay / RAI (from Pictorium)
  if (key.includes("rai")) {
    const svg = loadSvg("Logo_of_RAI_(2016).svg", true) || loadSvg("raiplay.svg");
    return svg ? { width: 44, height: 24, svg } : null;
  }

  // 9. Mediaset Infinity (from Pictorium)
  if (key.includes("infinity") || key.includes("mediaset")) {
    const svg = loadSvg("Mediaset_Infinity_logo.svg", true) || loadSvg("infinity.svg");
    return svg ? { width: 52, height: 25, svg } : null;
  }

  // 10. TIMVISION (from local /icons/tim.svg)
  if (key.includes("timvision") || key.includes("tim")) {
    const svg = loadSvg("tim.svg");
    return svg ? { width: 86, height: 20, svg } : null;
  }

  // 11. Discovery+ (from local /icons/discovery_plus.svg)
  if (key.includes("discovery")) {
    const svg = loadSvg("discovery_plus.svg");
    return svg ? { width: 30, height: 25, svg } : null;
  }

  // 12. Crunchyroll (from Pictorium)
  if (key.includes("crunchyroll")) {
    const svg = loadSvg("cr_logo_noTagline.svg", true) || loadSvg("crunchyroll.svg");
    return svg ? { width: 80, height: 16, svg } : null;
  }

  // 13. Hulu (from Pictorium)
  if (key.includes("hulu")) {
    const svg = loadSvg("Hulu_logo_(2018).svg", true);
    return svg ? { width: 60, height: 20, svg } : null;
  }

  // 14. Rakuten TV (from local /icons/rakuten.svg)
  if (key.includes("rakuten")) {
    const svg = loadSvg("rakuten.svg");
    return svg ? { width: 80, height: 18, svg } : null;
  }

  // 15. Top 10 Italia (Italian flag)
  if (key.includes("top10") || key.includes("italia") || key.includes("italy")) {
    const svg = loadSvg("italy.svg");
    return svg ? { width: 60, height: 40, svg } : null;
  }

  return null;
}

function numberSvg(rank, layout, accent, genre = "", rating = "", canvasBackground = "transparent", showLogo = true, catalogId = "", fontFamily = "Inter", ratingFormat = "star", metaColor = "", extra = {}) {
  const { canvas, card, number } = layout;
  const isDouble = String(rank).length > 1;
  const fontSize = isDouble ? number.sizeDouble : number.sizeSingle;
  const x = isDouble ? number.xDouble : number.xSingle;

  const centerY = card.y + card.height / 2;
  const y = centerY + fontSize * 0.34 + number.opticalDrop;

  const strokeWidth = layout === LAYOUTS.poster ? 10 : 11;
  const isProvider = canvasBackground === "provider" || canvasBackground === "brand";
  // rank <= 0 → no rank number (plain poster mode for AIOMetadata /poster)
  const showNumber = Number(rank) > 0;

  // Apply ratingFormat: "hidden" → empty, "number" → just the number, "star" → ★ prefix
  // PostersPlus hide_genre / hide_rating strip their label from the info line.
  const escapedGenre = extra.hideGenre ? "" : escapeXml(cleanGenre(genre));
  const rawRating = formatRating(rating);
  const ratingVal = ratingFormat === "hidden" || extra.hideRating ? "" : rawRating;
  const starPrefix = (ratingFormat === "star" || ratingFormat === "auto") && ratingVal ? "★ " : "";
  const isLandscape = layout === LAYOUTS.landscape;
  const fontStack = svgFontStack(fontFamily);
  const fontFace = fontFaceXml(fontFamily);
  // PostersPlus rating_text_color — applies to genre + rating text (star stays gold)
  // score_color_mode 3 + score_custom_palette → rating ink from the palette.
  let metaInk = /^[0-9a-fA-F]{6}$/.test(String(metaColor || "")) ? `#${metaColor}` : "#F1F5F9";
  let ratingInk = /^[0-9a-fA-F]{6}$/.test(String(metaColor || "")) ? `#${metaColor}` : "#FFFFFF";
  const scoreMode = Number(extra.scoreColorMode) || 0;
  if ((scoreMode === 1 || scoreMode === 3) && typeof extra.scoreCustomPalette === "string" && extra.scoreCustomPalette) {
    const first = extra.scoreCustomPalette.split(",")[0]?.trim().replace(/^#/, "");
    if (/^[0-9a-fA-F]{6}$/.test(first)) { ratingInk = `#${first}`; metaInk = `#${first}`; }
  }
  // score_glow_alpha (0..100, default 40) scales the neon glow strength.
  const glowScale = Number.isFinite(Number(extra.glowAlpha)) && Number(extra.glowAlpha) >= 0
    ? Math.max(0, Math.min(100, Number(extra.glowAlpha))) / 40 : 1;
  // PostersPlus logo_max_w_ratio / logo_max_h_ratio (fraction of canvas) size
  // the standalone provider logo in the classic layouts.
  const logoWMax = Number(extra.logoMaxWRatio) > 0
    ? Math.round(canvas.width * Number(extra.logoMaxWRatio)) : (isLandscape ? 200 : 180);
  const logoHMax = Number(extra.logoMaxHRatio) > 0
    ? Math.round(canvas.height * Number(extra.logoMaxHRatio)) : (isLandscape ? 72 : 85);

  // 1. Standalone Large Provider Logo (Left Column, Under Number)
  // extra.providerLogo (the /poster provider_logo param) decouples the
  // provider logo from show_logo (the title-logo switch) when explicitly
  // set; the manifest /top-cover path keeps the legacy showLogo gate.
  const wantProviderLogo = extra.providerLogo !== undefined
    ? (!!extra.providerLogo && !!catalogId)
    : !!(showLogo && catalogId);
  let logoXml = "";
  const rawLogo = wantProviderLogo ? getProviderLogoSvg(catalogId) : null;

  if (rawLogo && rawLogo.svg) {
    const maxLogoW = logoWMax;
    const maxLogoH = logoHMax;
    const scale = Math.min(maxLogoW / rawLogo.width, maxLogoH / rawLogo.height);
    const logoW = Math.round(rawLogo.width * scale);
    const logoH = Math.round(rawLogo.height * scale);

    const leftCenterX = isLandscape ? 150 : 105;
    const leftCenterY = isLandscape ? 620 : 1380;
    const logoX = Math.round(leftCenterX - logoW / 2);
    const logoY = Math.round(leftCenterY - logoH / 2);

    const logoHref = `data:image/svg+xml;base64,${Buffer.from(rawLogo.svg).toString("base64")}`;
    logoXml = `
      <g filter="url(#logoShadow)">
        <image href="${logoHref}" x="${logoX}" y="${logoY}" width="${logoW}" height="${logoH}" />
      </g>
    `;
  }

  // 2. Metadata (Genre & Rating Centered Under Artwork Card)
  const metaX = isLandscape ? 750 : 575;
  const metaY = isLandscape ? 672 : 1385;
  let metaXml = "";

  if (isLandscape) {
    const gSize = escapedGenre.length > 14 ? 36 : (escapedGenre.length > 10 ? 40 : 44);
    const starSize = 50;
    const dotSize = 34;

    if (escapedGenre && ratingVal) {
      metaXml = `
        <text x="${metaX}" y="${metaY}" text-anchor="middle" font-family="${fontStack}" filter="url(#metaShadow)">
          <tspan fill="${metaInk}" font-size="${gSize}" font-weight="900" letter-spacing="2.5">${escapedGenre}</tspan>
          <tspan fill="#94A3B8" font-size="${dotSize}" font-weight="800">   •   </tspan>
          <tspan fill="#FFB800" font-size="${starSize}" font-weight="900">${starPrefix ? "★ " : ""}</tspan>
          <tspan fill="${ratingInk}" font-size="${starSize}" font-weight="900">${ratingVal}</tspan>
        </text>
      `;
    } else if (escapedGenre) {
      metaXml = `<text x="${metaX}" y="${metaY}" text-anchor="middle" font-family="${fontStack}" font-size="44" font-weight="900" letter-spacing="2.5" fill="${metaInk}" filter="url(#metaShadow)">${escapedGenre}</text>`;
    } else if (ratingVal) {
      metaXml = `<text x="${metaX}" y="${metaY}" text-anchor="middle" font-family="${fontStack}" font-size="50" font-weight="900" filter="url(#metaShadow)">${starPrefix ? '<tspan fill="#FFB800">★ </tspan>' : ''}<tspan fill="${ratingInk}">${ratingVal}</tspan></text>`;
    }
  } else {
    // Poster / Portrait
    const gSize = escapedGenre.length > 14 ? 48 : (escapedGenre.length > 10 ? 54 : 60);
    const starSize = 68;
    const dotSize = 42;

    if (escapedGenre && ratingVal) {
      metaXml = `
        <text x="${metaX}" y="${metaY}" text-anchor="middle" font-family="${fontStack}" filter="url(#metaShadow)">
          <tspan fill="${metaInk}" font-size="${gSize}" font-weight="900" letter-spacing="3">${escapedGenre}</tspan>
          <tspan fill="#94A3B8" font-size="${dotSize}" font-weight="800">   •   </tspan>
          <tspan fill="#FFB800" font-size="${starSize}" font-weight="900">${starPrefix ? "★ " : ""}</tspan>
          <tspan fill="${ratingInk}" font-size="${starSize}" font-weight="900">${ratingVal}</tspan>
        </text>
      `;
    } else if (escapedGenre) {
      metaXml = `<text x="${metaX}" y="${metaY}" text-anchor="middle" font-family="${fontStack}" font-size="60" font-weight="900" letter-spacing="3" fill="${metaInk}" filter="url(#metaShadow)">${escapedGenre}</text>`;
    } else if (ratingVal) {
      metaXml = `<text x="${metaX}" y="${metaY}" text-anchor="middle" font-family="${fontStack}" font-size="68" font-weight="900" filter="url(#metaShadow)">${starPrefix ? '<tspan fill="#FFB800">★ </tspan>' : ''}<tspan fill="${ratingInk}">${ratingVal}</tspan></text>`;
    }
  }

  let defsFilterAndStroke;
  if (isProvider) {
    // Pure Crystal Glass Number (transparent, no colored neon glow)
    defsFilterAndStroke = `
      <linearGradient id="crystalGlassStroke" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0%" stop-color="#FFFFFF" stop-opacity="0.95"/>
        <stop offset="30%" stop-color="#FFFFFF" stop-opacity="0.80"/>
        <stop offset="65%" stop-color="#E2E8F0" stop-opacity="0.45"/>
        <stop offset="100%" stop-color="#CBD5E1" stop-opacity="0.85"/>
      </linearGradient>
      <filter id="numFilter" x="-60%" y="-60%" width="220%" height="220%">
        <feDropShadow dx="0" dy="6" stdDeviation="12" flood-color="#000000" flood-opacity="0.95"/>
        <feGaussianBlur in="SourceAlpha" stdDeviation="4" result="whiteGlow"/>
        <feFlood flood-color="#FFFFFF" flood-opacity="0.40" result="whiteColor"/>
        <feComposite in="whiteColor" in2="whiteGlow" operator="in" result="glassRim"/>
        <feMerge>
          <feMergeNode in="glassRim"/>
          <feMergeNode in="SourceGraphic"/>
        </feMerge>
      </filter>
    `;
  } else {
    // Standard Neon Glow with brand accent
    defsFilterAndStroke = `
      <linearGradient id="strokeGrad" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0%" stop-color="#FFFFFF" stop-opacity=".98"/>
        <stop offset="48%" stop-color="#F1F2F4" stop-opacity=".94"/>
        <stop offset="100%" stop-color="#C9CDD3" stop-opacity=".86"/>
      </linearGradient>

      <filter id="numFilter" x="-120%" y="-120%" width="340%" height="340%">
        <feGaussianBlur stdDeviation="30" result="bigBlur"/>
        <feFlood flood-color="${accent}" flood-opacity="${Math.min(0.95, 0.72 * glowScale).toFixed(3)}" result="brandColor"/>
        <feComposite in="brandColor" in2="bigBlur" operator="in" result="bigGlow"/>

        <feGaussianBlur in="SourceAlpha" stdDeviation="11" result="midBlur"/>
        <feFlood flood-color="${accent}" flood-opacity="${Math.min(0.9, 0.52 * glowScale).toFixed(3)}" result="midColor"/>
        <feComposite in="midColor" in2="midBlur" operator="in" result="midGlow"/>

        <feMerge>
          <feMergeNode in="bigGlow"/>
          <feMergeNode in="midGlow"/>
          <feMergeNode in="SourceGraphic"/>
        </feMerge>
      </filter>
    `;
  }

  const strokeUrl = isProvider ? "url(#crystalGlassStroke)" : "url(#strokeGrad)";

  // rank <= 0 → plain poster (no rank number): AIOMetadata /poster mode
  const numberXml = showNumber ? `
      <text
        x="${x}"
        y="${y}"
        font-family="${fontStack}"
        font-size="${fontSize}"
        font-weight="800"
        letter-spacing="-16"
        fill="none"
        stroke="${strokeUrl}"
        stroke-width="${strokeWidth}"
        stroke-linejoin="round"
        paint-order="stroke"
        filter="url(#numFilter)"
      >${rank}</text>` : "";

  return Buffer.from(`
    <svg width="${canvas.width}" height="${canvas.height}" xmlns="http://www.w3.org/2000/svg">
      <defs>
        <style>${fontFace}</style>
        ${defsFilterAndStroke}

        <filter id="logoShadow" x="-30%" y="-30%" width="160%" height="160%">
          <feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000000" flood-opacity="0.92"/>
        </filter>

        <filter id="metaShadow" x="-30%" y="-30%" width="160%" height="160%">
          <feDropShadow dx="0" dy="3" stdDeviation="4" flood-color="#000000" flood-opacity="0.95"/>
        </filter>
      </defs>
${numberXml}
      ${logoXml}
      ${metaXml}
    </svg>
  `);
}

async function roundedArtwork(buffer, layout) {
  const { card } = layout;

  const resized = await sharp(buffer)
    .resize(card.width, card.height, {
      fit: "cover",
      position: "centre"
    })
    .png()
    .toBuffer();

  const mask = Buffer.from(`
    <svg width="${card.width}" height="${card.height}" xmlns="http://www.w3.org/2000/svg">
      <rect
        width="100%"
        height="100%"
        rx="${card.radius}"
        ry="${card.radius}"
        fill="#fff"
      />
    </svg>
  `);

  return sharp(resized)
    .composite([{ input: mask, blend: "dest-in" }])
    .png()
    .toBuffer();
}

async function cardShadow(layout) {
  const { card } = layout;

  const svg = Buffer.from(`
    <svg width="${card.width + 120}" height="${card.height + 120}" xmlns="http://www.w3.org/2000/svg">
      <rect
        x="60"
        y="46"
        width="${card.width}"
        height="${card.height}"
        rx="${card.radius}"
        ry="${card.radius}"
        fill="#000"
        fill-opacity=".52"
      />
    </svg>
  `);

  return sharp(svg)
    .blur(24)
    .png()
    .toBuffer();
}

async function prepareLogo(buffer, layout) {
  const { logo: conf } = layout;

  const meta = await sharp(buffer).metadata();

  let width = meta.width || conf.maxWidth;
  let height = meta.height || conf.maxHeight;

  const scale = Math.min(
    conf.maxWidth / width,
    conf.maxHeight / height,
    1
  );

  width = Math.max(1, Math.round(width * scale));
  height = Math.max(1, Math.round(height * scale));

  const logo = await sharp(buffer)
    .resize({
      width,
      height,
      fit: "inside",
      withoutEnlargement: true
    })
    .png()
    .toBuffer();

  const pad = 32;
  const outWidth = width + pad * 2;
  const outHeight = height + pad * 2;

  const alpha = await sharp(logo)
    .ensureAlpha()
    .extractChannel("alpha")
    .extend({
      top: pad,
      bottom: pad,
      left: pad,
      right: pad,
      background: { r: 0, g: 0, b: 0, alpha: 0 }
    })
    .blur(10)
    .toBuffer();

  const shadow = await sharp({
    create: {
      width: outWidth,
      height: outHeight,
      channels: 4,
      background: { r: 0, g: 0, b: 0, alpha: 0.58 }
    }
  })
    .composite([{ input: alpha, blend: "dest-in" }])
    .png()
    .toBuffer();

  const composed = await sharp({
    create: {
      width: outWidth,
      height: outHeight,
      channels: 4,
      background: { r: 0, g: 0, b: 0, alpha: 0 }
    }
  })
    .composite([
      { input: shadow, left: 0, top: 6 },
      { input: logo, left: pad, top: pad }
    ])
    .png()
    .toBuffer();

  return {
    buffer: composed,
    visibleWidth: width,
    visibleHeight: height,
    pad
  };
}

async function brandAmbientGlow(layout, accent) {
  const { canvas, card } = layout;
  const rgb = hexToRgb(accent);

  const glowWidth = Math.min(canvas.width, card.x + 140);
  const glowHeight = Math.min(canvas.height, card.height + 260);

  const svg = Buffer.from(`
    <svg width="${canvas.width}" height="${canvas.height}" xmlns="http://www.w3.org/2000/svg">
      <defs>
        <radialGradient id="ambient" cx="34%" cy="52%" r="45%">
          <stop offset="0%" stop-color="rgb(${rgb.r},${rgb.g},${rgb.b})" stop-opacity=".20"/>
          <stop offset="45%" stop-color="rgb(${rgb.r},${rgb.g},${rgb.b})" stop-opacity=".07"/>
          <stop offset="100%" stop-color="rgb(${rgb.r},${rgb.g},${rgb.b})" stop-opacity="0"/>
        </radialGradient>
      </defs>
      <rect width="${glowWidth}" height="${glowHeight}" fill="url(#ambient)"/>
    </svg>
  `);

  return sharp(svg)
    .blur(10)
    .png()
    .toBuffer();
}

export function parseBackgroundColor(canvasBackground) {
  const bg = String(canvasBackground || "").toLowerCase().trim();
  if (!bg || bg === "transparent") {
    return { r: 0, g: 0, b: 0, alpha: 0 };
  }
  if (bg === "stremio" || bg === "stremio-navy" || bg === "rgb(26,23,62)" || bg === "rgb(26, 23, 62)" || bg === "#1a173e" || bg === "1a173e") {
    return { r: 26, g: 23, b: 62, alpha: 1 };
  }
  if (bg === "black" || bg === "nero") {
    return { r: 0, g: 0, b: 0, alpha: 1 };
  }
  if (bg.startsWith("#")) {
    const rgb = hexToRgb(bg);
    return { r: rgb.r, g: rgb.g, b: rgb.b, alpha: 1 };
  }
  return { r: 0, g: 0, b: 0, alpha: 0 };
}

/* ---------------- Banner style: full-bleed artwork, glass rank on the left ---------------- */

const BANNER_FONT = "Inter Display, Inter, Arial, sans-serif";

const BANNER_LAYOUTS = {
  landscape: {
    width: 1280,
    height: 720,
    zone: 0.30,
    backdrop: { zoom: 1.03, shiftX: 0.06, blur: 14 },
    number: { padX: 30, padY: 68, gap: 26, maxHeight: 0.54, rim: 2.8 },
    meta: { genre: 25, rating: 46, gap: 14 },
    provider: { maxWidth: 236, maxHeight: 64 },
    logo: { maxWidth: 440, maxHeight: 170, area: 52000, right: 46, bottom: 40 }
  },
  poster: {
    width: 1000,
    height: 1500,
    zone: 0.33,
    backdrop: { zoom: 1.03, shiftX: 0.06, blur: 12 },
    number: { padX: 22, padY: 280, gap: 34, maxHeight: 0.33, rim: 3.5 },
    meta: { genre: 30, rating: 54, gap: 16 },
    provider: { maxWidth: 280, maxHeight: 78 },
    logo: { maxWidth: 560, maxHeight: 260, area: 90000, right: 50, bottom: 64 }
  }
};

// Intermediate buffers only: skip zlib work.
const FAST_PNG = { compressionLevel: 0, adaptiveFiltering: false };

export function normalizeStyle(style) {
  return style === "banner" ? "banner" : "classic";
}

const GLYPH_CACHE = new Map();

// Tight box of the rendered digits at font-size 1000, relative to anchor x / baseline y.
async function measureGlyphs(text, letterSpacing) {
  const key = `${text}|${letterSpacing}`;
  if (GLYPH_CACHE.has(key)) return GLYPH_CACHE.get(key);

  const x = 200;
  const y = 1200;
  const svg = Buffer.from(`
    <svg width="${400 + 800 * text.length}" height="1500" xmlns="http://www.w3.org/2000/svg">
      <rect width="100%" height="100%" fill="#000"/>
      <text x="${x}" y="${y}" font-family="${BANNER_FONT}" font-size="1000" font-weight="800" letter-spacing="${letterSpacing * 1000}" fill="#fff">${text}</text>
    </svg>
  `);

  const { info } = await sharp(svg).trim({ threshold: 40 }).toBuffer({ resolveWithObject: true });
  const box = {
    left: -info.trimOffsetLeft - x,
    top: -info.trimOffsetTop - y,
    width: info.width,
    height: info.height
  };

  GLYPH_CACHE.set(key, box);
  return box;
}

function starPath(cx, cy, outer, inner) {
  const points = [];
  for (let i = 0; i < 10; i++) {
    const r = i % 2 === 0 ? outer : inner;
    const a = -Math.PI / 2 + (i * Math.PI) / 5;
    points.push(`${(cx + r * Math.cos(a)).toFixed(1)},${(cy + r * Math.sin(a)).toFixed(1)}`);
  }
  return `M${points.join("L")}Z`;
}

async function bannerBackdrop(artworkBuffer, L, zoneW) {
  const { width: W, height: H } = L;
  const scaledW = Math.round(W * L.backdrop.zoom);
  const scaledH = Math.round(H * L.backdrop.zoom);
  const offsetX = Math.round(W * L.backdrop.shiftX) - Math.floor((scaledW - W) / 2);
  const fillW = Math.max(0, offsetX);
  const scaled = await sharp(artworkBuffer)
    .resize(scaledW, scaledH, { fit: "cover", position: sharp.strategy.attention })
    .removeAlpha()
    .png(FAST_PNG)
    .toBuffer();
  const cropped = await sharp(scaled)
    .extract({ left: Math.max(0, -offsetX), top: Math.floor((scaledH - H) / 2), width: W - fillW, height: H })
    .toColourspace("srgb")
    .removeAlpha()
    .raw()
    .toBuffer();
  const stripWidth = Math.max(1, Math.round(W * 0.02));
  const stripLeft = Math.max(0, Math.min(W - fillW - stripWidth, Math.round(zoneW * 0.70) - fillW));
  const edge = await sharp(cropped, { raw: { width: W - fillW, height: H, channels: 3 } })
    .extract({ left: stripLeft, top: 0, width: stripWidth, height: H })
    .resize(1, H)
    .blur(H * 0.075)
    .raw()
    .toBuffer();
  const pixels = Buffer.alloc(W * H * 3);
  const fadeStart = Math.max(fillW, zoneW * 0.55);
  const fadeEnd = zoneW * 0.9;
  for (let row = 0; row < H; row++) {
    for (let column = 0; column < W; column++) {
      const fraction = Math.max(0, Math.min(1, (column - fadeStart) / (fadeEnd - fadeStart)));
      const blend = 1 - fraction * fraction * (3 - 2 * fraction);
      const original = (row * (W - fillW) + Math.max(0, column - fillW)) * 3;
      const destination = (row * W + column) * 3;
      for (let channel = 0; channel < 3; channel++) {
        pixels[destination + channel] = Math.round(edge[row * 3 + channel] * blend + cropped[original + channel] * (1 - blend));
      }
    }
  }
  const blended = await sharp(pixels, { raw: { width: W, height: H, channels: 3 } })
    .png(FAST_PNG)
    .toBuffer();
  const blurMask = Buffer.from(`
    <svg width="${W}" height="${H}" xmlns="http://www.w3.org/2000/svg">
      <defs>
        <linearGradient id="soft" gradientUnits="userSpaceOnUse" x1="${fadeStart}" y1="0" x2="${zoneW * 1.1}" y2="0">
          <stop offset="0" stop-color="#FFFFFF" stop-opacity="1"/>
          <stop offset="1" stop-color="#FFFFFF" stop-opacity="0"/>
        </linearGradient>
      </defs>
      <rect width="${W}" height="${H}" fill="url(#soft)"/>
    </svg>
  `);
  const softened = await sharp(blended)
    .blur(L.backdrop.blur)
    .ensureAlpha()
    .composite([{ input: blurMask, blend: "dest-in" }])
    .png(FAST_PNG)
    .toBuffer();
  const { channels } = await sharp(blended).extract({ left: 0, top: 0, width: zoneW, height: H }).stats();
  const brightness = 0.2126 * channels[0].mean + 0.7152 * channels[1].mean + 0.0722 * channels[2].mean;
  const sideOpacity = Math.min(0.65, Math.max(0.28, (brightness - 60) / 200));
  const shade = Buffer.from(`
    <svg width="${W}" height="${H}" xmlns="http://www.w3.org/2000/svg">
      <defs>
        <linearGradient id="side" gradientUnits="userSpaceOnUse" x1="0" y1="0" x2="${zoneW * 1.5}" y2="0">
          <stop offset="0" stop-color="#000" stop-opacity="${sideOpacity.toFixed(2)}"/>
          <stop offset=".5" stop-color="#000" stop-opacity="${(sideOpacity * 0.7).toFixed(2)}"/>
          <stop offset="1" stop-color="#000" stop-opacity="0"/>
        </linearGradient>
        <linearGradient id="bottom" x1="0" y1="0" x2="0" y2="1">
          <stop offset=".5" stop-color="#000" stop-opacity="0"/>
          <stop offset="1" stop-color="#000" stop-opacity=".5"/>
        </linearGradient>
      </defs>
      <rect width="${W}" height="${H}" fill="url(#side)"/>
      <rect width="${W}" height="${H}" fill="url(#bottom)"/>
    </svg>
  `);

  return sharp(blended)
    .composite([
      { input: softened, left: 0, top: 0 },
      { input: shade, left: 0, top: 0 }
    ])
    .png(FAST_PNG)
    .toBuffer();
}

function bannerMetaLayout(L, zoneW, { genre, rating, provider, metaColor = "", extra = {} }) {
  const { meta } = L;
  const maxW = zoneW - L.number.padX * 2;
  let rows = [];
  // PostersPlus rating_text_color → genre + rating ink (star keeps its violet)
  // score_color_mode 3 + score_custom_palette → ink from the palette.
  let ink = /^[0-9a-fA-F]{6}$/.test(String(metaColor || "")) ? `#${metaColor}` : "#FFFFFF";
  const scoreMode = Number(extra.scoreColorMode) || 0;
  if ((scoreMode === 1 || scoreMode === 3) && typeof extra.scoreCustomPalette === "string" && extra.scoreCustomPalette) {
    const first = extra.scoreCustomPalette.split(",")[0]?.trim().replace(/^#/, "");
    if (/^[0-9a-fA-F]{6}$/.test(first)) ink = `#${first}`;
  }
  // landscape_info_scale (PostersPlus) scales the whole info strip.
  const infoScale = Number(extra.infoScale) > 0 ? Math.max(0.6, Math.min(2, Number(extra.infoScale))) : 1;

  if (genre && !extra.hideGenre) {
    let size = meta.genre * infoScale;
    while (size > 14 && genre.length * size * 0.84 > maxW) size--;
    const capH = size * 0.73;
    rows.push({
      _tag: "genre",
      h: capH,
      draw: (top, cx) => `<text x="${cx.toFixed(1)}" y="${(top + capH).toFixed(1)}" text-anchor="middle" font-family="Inter, Arial, sans-serif" font-size="${size}" font-weight="700" letter-spacing="${(size * 0.12).toFixed(1)}" fill="${ink}" fill-opacity=".88">${escapeXml(genre)}</text>`
    });
  }

  if (rating && !extra.hideRating) {
    const size = meta.rating * infoScale;
    const capH = size * 0.73;
    const starR = size * 0.42;
    const gap = size * 0.2;
    const textW = [...rating].reduce((w, ch) => w + (ch === "." ? 0.3 : 0.63), 0) * size;
    const h = Math.max(capH, starR * 2);
    rows.push({
      _tag: "rating",
      h,
      draw: (top, cx) => {
        const x0 = cx - (starR * 2 + gap + textW) / 2;
        const midY = top + h / 2;
        return `<path d="${starPath(x0 + starR, midY, starR, starR * 0.46)}" fill="#C9C7FF"/>` +
          `<text x="${(x0 + starR * 2 + gap).toFixed(1)}" y="${(midY + capH / 2).toFixed(1)}" font-family="Inter, Arial, sans-serif" font-size="${size}" font-weight="800" fill="${ink}">${rating}</text>`;
      }
    });
  }

  if (provider?.svg) {
    const scale = Math.min(L.provider.maxWidth / provider.width, L.provider.maxHeight / provider.height) * infoScale;
    const w = Math.round(provider.width * scale);
    const h = Math.round(provider.height * scale);
    const href = `data:image/svg+xml;base64,${Buffer.from(provider.svg).toString("base64")}`;
    rows.push({
      _tag: "provider",
      h,
      gapBefore: meta.gap * 1.8,
      draw: (top, cx) => `<image href="${href}" x="${Math.round(cx - w / 2)}" y="${Math.round(top)}" width="${w}" height="${h}"/>`
    });
  }

  // PostersPlus meta_order — reorder the info rows. A comma list like
  // "rating,genre,provider" puts the rating line first; names absent from the
  // list keep their default slot after the ordered ones.
  const orderNames = String(extra.metaOrder || "").split(",").map(s => s.trim().toLowerCase()).filter(Boolean);
  if (orderNames.length && rows.length > 1) {
    rows.sort((a, b) => {
      const ia = orderNames.indexOf(a._tag), ib = orderNames.indexOf(b._tag);
      return (ia === -1 ? 99 : ia) - (ib === -1 ? 99 : ib);
    });
  }

  const height = rows.reduce((sum, r, i) => sum + r.h + (i ? (r.gapBefore || meta.gap) : 0), 0);

  return {
    height,
    render(top, cx) {
      let y = top;
      const body = rows.map((r, i) => {
        if (i) y += r.gapBefore || meta.gap;
        const out = r.draw(y, cx);
        y += r.h;
        return out;
      }).join("");

      return Buffer.from(`
        <svg width="${L.width}" height="${L.height}" xmlns="http://www.w3.org/2000/svg">
          <defs>
            <filter id="metaShadow" x="-20%" y="-40%" width="140%" height="180%">
              <feDropShadow dx="0" dy="2" stdDeviation="3" flood-color="#000000" flood-opacity=".8"/>
            </filter>
          </defs>
          <g filter="url(#metaShadow)">${body}</g>
        </svg>
      `);
    }
  };
}

async function bannerNumberGeometry(rank, L, zoneW, { hasProvider = true, hasMeta = true } = {}) {
  const text = String(rank);
  const isDouble = text.length > 1;
  const spacing = isDouble ? -0.05 : 0;
  const m = await measureGlyphs(text, spacing);

  const { padX, padY, gap, maxHeight } = L.number;
  const H = L.height;
  const boxW = zoneW - padX * 2;
  const numberOnly = !hasProvider && !hasMeta;
  const glyphH = H * maxHeight * (numberOnly ? 1.08 : 1);
  const s = glyphH / m.height;
  const scaleX = Math.min(1, boxW / (m.width * s));

  const glyphW = m.width * scaleX * s;
  const glyphLeft = padX + (boxW - glyphW) / 2;
  const glyphTop = numberOnly
    ? Math.round((H - glyphH) / 2)
    : padY + (hasProvider ? 0 : Math.round(H * 0.035));

  const geometry = {
    text,
    scaleX,
    fontSize: 1000 * s,
    letterSpacing: spacing * 1000 * s,
    x: glyphLeft - m.left * scaleX * s,
    y: glyphTop - m.top * s,
    centerX: zoneW / 2,
    metaTop: glyphTop + glyphH + gap
  };
  if (isDouble) {
    const digits = await Promise.all([...text].map(digit => measureGlyphs(digit, 0)));
    const digitGap = 20;
    const available = zoneW - padX;
    const horizontal = Math.min(1, (available - digitGap) / (digits.reduce((sum, digit) => sum + digit.width, 0) * s));
    const total = digits.reduce((sum, digit) => sum + digit.width, 0) * s * horizontal + digitGap;
    const left = (zoneW - total) / 2;
    geometry.runs = digits.map((digit, index) => ({
      text: text[index],
      x: left + (index ? digits[0].width * s * horizontal + digitGap : 0) - digit.left * s * horizontal,
      scaleX: horizontal
    }));
    geometry.letterSpacing = 0;
  }
  return geometry;
}

function bannerNumberText(g, attrs, fontFamily = "Inter") {
  const fontStack = svgFontStack(fontFamily);
  if (g.runs) {
    return g.runs.map(run => `<text x="0" y="0" transform="translate(${run.x.toFixed(1)} ${g.y.toFixed(1)}) scale(${run.scaleX} 1)" font-family="${fontStack}" font-size="${g.fontSize.toFixed(1)}" font-weight="800" ${attrs}>${run.text}</text>`).join("");
  }
  return `<text x="0" y="0" transform="translate(${g.x.toFixed(1)} ${g.y.toFixed(1)}) scale(${g.scaleX} 1)" font-family="${fontStack}" font-size="${g.fontSize.toFixed(1)}" font-weight="800" letter-spacing="${g.letterSpacing.toFixed(1)}" ${attrs}>${g.text}</text>`;
}

function euclideanDistanceSquared(coverage, width, height) {
  const INF = 1e20;
  const grid = new Float64Array(width * height);
  for (let pixel = 0; pixel < grid.length; pixel++) grid[pixel] = coverage[pixel] >= 128 ? INF : 0;
  const size = Math.max(width, height);
  const f = new Float64Array(size);
  const d = new Float64Array(size);
  const z = new Float64Array(size + 1);
  const v = new Int32Array(size);
  const transform = length => {
    let k = 0;
    v[0] = 0;
    z[0] = -INF;
    z[1] = INF;
    for (let q = 1; q < length; q++) {
      let s = ((f[q] + q * q) - (f[v[k]] + v[k] * v[k])) / (2 * q - 2 * v[k]);
      while (s <= z[k]) {
        k--;
        s = ((f[q] + q * q) - (f[v[k]] + v[k] * v[k])) / (2 * q - 2 * v[k]);
      }
      k++;
      v[k] = q;
      z[k] = s;
      z[k + 1] = INF;
    }
    k = 0;
    for (let q = 0; q < length; q++) {
      while (z[k + 1] < q) k++;
      d[q] = (q - v[k]) * (q - v[k]) + f[v[k]];
    }
  };
  for (let x = 0; x < width; x++) {
    for (let y = 0; y < height; y++) f[y] = grid[y * width + x];
    transform(height);
    for (let y = 0; y < height; y++) grid[y * width + x] = d[y];
  }
  for (let y = 0; y < height; y++) {
    for (let x = 0; x < width; x++) f[x] = grid[y * width + x];
    transform(width);
    for (let x = 0; x < width; x++) grid[y * width + x] = d[x];
  }
  return grid;
}

function floatBoxBlur(source, width, height, radius) {
  const span = radius * 2 + 1;
  const temp = new Float32Array(source.length);
  const output = new Float32Array(source.length);
  for (let y = 0; y < height; y++) {
    const row = y * width;
    let sum = 0;
    for (let x = -radius; x <= radius; x++) sum += source[row + Math.min(width - 1, Math.max(0, x))];
    for (let x = 0; x < width; x++) {
      temp[row + x] = sum / span;
      sum += source[row + Math.min(width - 1, x + radius + 1)] - source[row + Math.max(0, x - radius)];
    }
  }
  for (let x = 0; x < width; x++) {
    let sum = 0;
    for (let y = -radius; y <= radius; y++) sum += temp[Math.min(height - 1, Math.max(0, y)) * width + x];
    for (let y = 0; y < height; y++) {
      output[y * width + x] = sum / span;
      sum += temp[Math.min(height - 1, y + radius + 1) * width + x] - temp[Math.max(0, y - radius) * width + x];
    }
  }
  return output;
}

async function bannerGlassNumber(base, L, g, fontFamily = "Inter") {
  const { width: W, height: H } = L;
  const material = {
    refractiveIndex: 1.33,
    blurRadius: 6.5,
    distortionStrength: 0.051,
    curvature: 1.46,
    edgeSharpness: 0.05,
    glowIntensity: 0.78,
    shadowStrength: 0.29,
    borderRadius: 50,
    opacity: 0.86
  };
  const sampleScale = 2;
  const cornerBlur = Math.max(1, Math.min(material.borderRadius / 4, g.fontSize * 0.024));
  const maskContrast = Math.max(6, cornerBlur * 2);
  const fontFace = fontFaceXml(fontFamily);
  const svg = body => Buffer.from(
    `<svg width="${W}" height="${H}" xmlns="http://www.w3.org/2000/svg"><defs><style>${fontFace}</style></defs>${body}</svg>`
  );
  const sharpMask = await sharp(svg(bannerNumberText(g, 'fill="#FFFFFF"', fontFamily)), { density: 72 * sampleScale })
    .extractChannel("alpha")
    .blur(cornerBlur * sampleScale)
    .png(FAST_PNG)
    .toBuffer();
  const maskImage = await sharp(sharpMask)
    .linear(maskContrast, 128 * (1 - maskContrast))
    .toColourspace("b-w")
    .raw()
    .toBuffer({ resolveWithObject: true });
  const maskStride = maskImage.info.channels;
  const maskWidth = maskImage.info.width;
  const maskHeight = maskImage.info.height;
  let minimumX = maskWidth;
  let minimumY = maskHeight;
  let maximumX = -1;
  let maximumY = -1;
  for (let row = 0; row < maskHeight; row++) {
    for (let column = 0; column < maskWidth; column++) {
      if (!maskImage.data[(row * maskWidth + column) * maskStride]) continue;
      minimumX = Math.min(minimumX, column);
      minimumY = Math.min(minimumY, row);
      maximumX = Math.max(maximumX, column);
      maximumY = Math.max(maximumY, row);
    }
  }
  if (maximumX < minimumX) throw new Error("Maschera del numero vuota");

  const left = Math.max(0, Math.floor(minimumX / sampleScale) - 12);
  const top = Math.max(0, Math.floor(minimumY / sampleScale) - 12);
  const width = Math.min(W, Math.ceil((maximumX + 1) / sampleScale) + 12) - left;
  const height = Math.min(H, Math.ceil((maximumY + 1) / sampleScale) + 12) - top;
  const sampleWidth = width * sampleScale;
  const sampleHeight = height * sampleScale;
  const coverage = new Uint8Array(sampleWidth * sampleHeight);
  for (let row = 0; row < sampleHeight; row++) {
    for (let column = 0; column < sampleWidth; column++) {
      const original = ((top * sampleScale + row) * maskWidth + left * sampleScale + column) * maskStride;
      coverage[row * sampleWidth + column] = maskImage.data[original];
    }
  }

  const inside = euclideanDistanceSquared(coverage, sampleWidth, sampleHeight);
  const outside = euclideanDistanceSquared(Uint8Array.from(coverage, alpha => 255 - alpha), sampleWidth, sampleHeight);
  const rawDistance = new Float32Array(coverage.length);
  for (let pixel = 0; pixel < rawDistance.length; pixel++) {
    const alpha = coverage[pixel] / 255;
    rawDistance[pixel] = coverage[pixel] >= 128
      ? Math.sqrt(inside[pixel]) - 1.5 + alpha
      : 0.5 - Math.sqrt(outside[pixel]) + alpha;
  }
  const distance = floatBoxBlur(floatBoxBlur(rawDistance, sampleWidth, sampleHeight, 2), sampleWidth, sampleHeight, 2);
  const texture = await sharp(base)
    .resize(W, H)
    .toColourspace("srgb")
    .modulate({ saturation: 1 + (material.refractiveIndex - 1) * 0.5 })
    .linear(1 + material.edgeSharpness * 0.3, -128 * material.edgeSharpness * 0.3)
    .removeAlpha()
    .png(FAST_PNG)
    .toBuffer();
  const photo = await sharp(texture).raw().toBuffer();
  const blurredPhoto = await sharp(texture).blur(material.blurRadius).raw().toBuffer();
  const pixels = Buffer.alloc(sampleWidth * sampleHeight * 4);
  const centerX = (minimumX + maximumX + 1) / (2 * sampleScale);
  const centerY = (minimumY + maximumY + 1) / (2 * sampleScale);
  const halfWidth = (maximumX - minimumX + 1) / (2 * sampleScale);
  const halfHeight = (maximumY - minimumY + 1) / (2 * sampleScale);
  const eta = 1 / material.refractiveIndex;
  const etaSquared = eta * eta;
  const contourRefraction = Math.sqrt(1 - etaSquared);
  const smoothstep = (lower, upper, value) => {
    const fraction = Math.max(0, Math.min(1, (value - lower) / (upper - lower)));
    return fraction * fraction * (3 - 2 * fraction);
  };

  for (let row = 1; row < sampleHeight - 1; row++) {
    for (let column = 1; column < sampleWidth - 1; column++) {
      const pixel = row * sampleWidth + column;
      if (!coverage[pixel]) continue;
      const positionX = left + (column + 0.5) / sampleScale;
      const positionY = top + (row + 0.5) / sampleScale;
      const localX = (positionX - centerX) / halfWidth;
      const localY = (positionY - centerY) / halfHeight * W / H;
      const radialLength = Math.hypot(localX, localY);
      const radialDistance = Math.min(1, radialLength);
      const curvature = Math.pow(radialDistance, material.curvature);
      const radialFactor = eta * (1 - curvature * curvature)
        + Math.sqrt(1 - etaSquared * (1 - Math.pow(curvature, 4)));
      const radialOffset = curvature * radialFactor * material.distortionStrength;

      const gradientX = distance[pixel - 1] - distance[pixel + 1];
      const gradientY = distance[pixel - sampleWidth] - distance[pixel + sampleWidth];
      const gradientLength = Math.hypot(gradientX, gradientY);
      const normalX = gradientLength ? gradientX / gradientLength : 0;
      const normalY = gradientLength ? gradientY / gradientLength : 0;
      const edgeDistance = Math.abs(distance[pixel]) / sampleScale;
      const contourFalloff = Math.exp(-edgeDistance * material.edgeSharpness);
      const contourOffset = contourRefraction * 0.35 * Math.pow(contourFalloff, 2.5);
      const contourWeight = Math.max(0, Math.min(1,
        smoothstep(0, 1, edgeDistance) - smoothstep(0.5, 1, radialDistance) * 0.5));
      const radialX = radialLength ? localX / radialLength * radialOffset : 0;
      const radialY = radialLength ? localY / radialLength * radialOffset : 0;
      const sampleX = Math.max(0, Math.min(W - 1.001,
        positionX - (radialX * (1 - contourWeight) + normalX * contourOffset * contourWeight) * W));
      const sampleY = Math.max(0, Math.min(H - 1.001,
        positionY - (radialY * (1 - contourWeight) + normalY * contourOffset * contourWeight) * H));
      const sampleLeft = Math.floor(sampleX);
      const sampleTop = Math.floor(sampleY);
      const fractionX = sampleX - sampleLeft;
      const fractionY = sampleY - sampleTop;
      const topLeft = (sampleTop * W + sampleLeft) * 3;
      const bottomLeft = topLeft + W * 3;
      const topShadow = (1 - smoothstep(-1.5, -0.2, localY)) * material.shadowStrength;
      const glow = Math.exp(-edgeDistance * 0.18) * material.glowIntensity
        * (0.35 + 0.65 * Math.max(0, -0.6 * normalX - 0.8 * normalY));
      const border = (1 - smoothstep(0, 1, edgeDistance)) * 0.28;
      const inset = (1 - smoothstep(0, 2, edgeDistance)) * Math.max(0, -normalY) * material.glowIntensity * 0.6;
      const rim = Math.min(1, border + inset);

      for (let channel = 0; channel < 3; channel++) {
        const upper = photo[topLeft + channel] * (1 - fractionX) + photo[topLeft + 3 + channel] * fractionX;
        const lower = photo[bottomLeft + channel] * (1 - fractionX) + photo[bottomLeft + 3 + channel] * fractionX;
        const blurredUpper = blurredPhoto[topLeft + channel] * (1 - fractionX) + blurredPhoto[topLeft + 3 + channel] * fractionX;
        const blurredLower = blurredPhoto[bottomLeft + channel] * (1 - fractionX) + blurredPhoto[bottomLeft + 3 + channel] * fractionX;
        const transmitted = (upper * 0.9 + blurredUpper * 0.1) * (1 - fractionY)
          + (lower * 0.9 + blurredLower * 0.1) * fractionY;
        const body = (transmitted * 0.93 + 255 * 0.07) * (1 - topShadow);
        const color = body * (1 - glow) + 255 * 0.90 * glow;
        pixels[pixel * 4 + channel] = Math.round(color * (1 - rim) + 255 * rim);
      }
      pixels[pixel * 4 + 3] = Math.round(coverage[pixel] * material.opacity);
    }
  }

  const glass = await sharp(pixels, { raw: { width: sampleWidth, height: sampleHeight, channels: 4 } })
    .resize(width, height, { kernel: sharp.kernel.lanczos3 })
    .png(FAST_PNG)
    .toBuffer();
  const roundedAlpha = await sharp(glass).extractChannel("alpha").raw().toBuffer();
  const shadowPixels = Buffer.alloc(W * H * 4);
  for (let row = 0; row < height; row++) {
    const shadowRow = top + row + 3;
    if (shadowRow >= H) continue;
    for (let column = 0; column < width; column++) {
      const sourceAlpha = roundedAlpha[row * width + column];
      shadowPixels[(shadowRow * W + left + column) * 4 + 3] =
        Math.round(sourceAlpha * material.shadowStrength / material.opacity);
    }
  }
  const shadow = await sharp(shadowPixels, { raw: { width: W, height: H, channels: 4 } })
    .blur(4)
    .png(FAST_PNG)
    .toBuffer();

  return [
    { input: shadow, left: 0, top: 0 },
    { input: glass, left, top }
  ];
}

async function bannerTitleLogo(logoUrl, L) {
  if (!logoUrl) return null;

  try {
    const raw = await fetchBuffer(logoUrl);
    let src = raw;
    try {
      src = await sharp(raw).trim().png(FAST_PNG).toBuffer();
    } catch {}

    // Size by area so wide and stacked logos get a similar visual weight.
    const { width: sw = 1, height: sh = 1 } = await sharp(src).metadata();
    const ratio = sw / sh;
    const { maxWidth, maxHeight, area, right, bottom } = L.logo;
    const fit = Math.min(1, maxWidth / Math.sqrt(area * ratio), maxHeight / Math.sqrt(area / ratio));
    const w = Math.max(1, Math.round(Math.sqrt(area * ratio) * fit));
    const h = Math.max(1, Math.round(Math.sqrt(area / ratio) * fit));

    const { data, info } = await sharp(src)
      .resize(w, h, { fit: "fill" })
      .ensureAlpha()
      .raw()
      .toBuffer({ resolveWithObject: true });

    // Average brightness of the visible pixels (raw output may be premultiplied).
    let value = 0;
    let weight = 0;
    for (let i = 0; i < data.length; i += 4) {
      const a = data[i + 3];
      const v = Math.max(data[i], data[i + 1], data[i + 2]);
      value += info.premultiplied ? v * 255 : v * a;
      weight += a;
    }
    const darkLogo = weight > 0 && value / weight < 80;

    const rawInfo = { width: info.width, height: info.height, channels: info.channels, premultiplied: info.premultiplied };
    const logo = await sharp(data, { raw: rawInfo }).png(FAST_PNG).toBuffer();
    // Separate pipeline: sharp extracts channels after extend/blur.
    const mask = await sharp(data, { raw: rawInfo }).extractChannel("alpha").png(FAST_PNG).toBuffer();
    const pad = 28;
    const alpha = await sharp(mask)
      .extend({ top: pad, bottom: pad, left: pad, right: pad, background: { r: 0, g: 0, b: 0, alpha: 0 } })
      .blur(darkLogo ? 12 : 9)
      .linear(darkLogo ? 0.55 : 0.8, 0)
      .png(FAST_PNG)
      .toBuffer();
    const halo = await sharp({
      create: {
        width: w + pad * 2,
        height: h + pad * 2,
        channels: 3,
        background: darkLogo ? { r: 255, g: 255, b: 255 } : { r: 0, g: 0, b: 0 }
      }
    })
      .joinChannel(alpha)
      .png(FAST_PNG)
      .toBuffer();

    const left = L.width - right - w;
    const top = L.height - bottom - h;
    return {
      dark: darkLogo,
      box: { left, top, width: w, height: h },
      layers: [
        { input: halo, left: left - pad, top: top - pad + (darkLogo ? 0 : 4) },
        { input: logo, left, top }
      ]
    };
  } catch (err) {
    console.warn(`Logo titolo non disponibile (${logoUrl}):`, err.message);
    return null;
  }
}

// Light logo on a bright area: darken just around it, more the brighter the area.
async function bannerLogoShade(base, L, titleLogo) {
  if (!titleLogo || titleLogo.dark) return null;

  const { left, top, width, height } = titleLogo.box;
  const { channels } = await sharp(base).extract({ left, top, width, height }).stats();
  const brightness = 0.2126 * channels[0].mean + 0.7152 * channels[1].mean + 0.0722 * channels[2].mean;
  if (brightness < 95) return null;

  const opacity = Math.min(0.7, 0.25 + ((brightness - 95) / 160) * 0.45).toFixed(2);
  return {
    input: Buffer.from(`
      <svg width="${L.width}" height="${L.height}" xmlns="http://www.w3.org/2000/svg">
        <defs>
          <radialGradient id="s">
            <stop offset="0" stop-color="#000000" stop-opacity="${opacity}"/>
            <stop offset=".55" stop-color="#000000" stop-opacity="${(opacity * 0.6).toFixed(2)}"/>
            <stop offset="1" stop-color="#000000" stop-opacity="0"/>
          </radialGradient>
        </defs>
        <ellipse cx="${left + width / 2}" cy="${top + height / 2}" rx="${width * 0.85}" ry="${height * 1.25}" fill="url(#s)"/>
      </svg>
    `),
    left: 0,
    top: 0
  };
}

/* ── Brand Tint (topx chip): soft radial glow of the platform's accent
 *  behind the number zone of the banner (fresh) canvas.  Pure SVG → sharp,
 *  no extra fetches, so it costs ~1ms on top of the base render. ── */
function brandTintOverlay(L, accent) {
  const { width: W, height: H } = L;
  const zoneW = Math.round(W * L.zone);
  const safeAccent = /^#[0-9a-fA-F]{6}$/.test(String(accent)) ? accent : "#8C75FF";
  const svg = Buffer.from(`
    <svg width="${W}" height="${H}" xmlns="http://www.w3.org/2000/svg">
      <defs>
        <radialGradient id="bt" gradientUnits="userSpaceOnUse"
          cx="${Math.round(zoneW * 0.36)}" cy="${Math.round(H * 0.44)}" r="${Math.round(zoneW * 0.98)}">
          <stop offset="0" stop-color="${safeAccent}" stop-opacity="0.50"/>
          <stop offset="0.55" stop-color="${safeAccent}" stop-opacity="0.18"/>
          <stop offset="1" stop-color="${safeAccent}" stop-opacity="0"/>
        </radialGradient>
        <linearGradient id="btl" x1="0" y1="0" x2="1" y2="0">
          <stop offset="0" stop-color="${safeAccent}" stop-opacity="0.20"/>
          <stop offset="1" stop-color="${safeAccent}" stop-opacity="0"/>
        </linearGradient>
      </defs>
      <rect width="${zoneW}" height="${H}" fill="url(#btl)"/>
      <rect width="${W}" height="${H}" fill="url(#bt)"/>
    </svg>`);
  return sharp(svg).png(FAST_PNG).toBuffer();
}

async function createBannerCover({ rank, artworkUrl, logoUrl, shape, genre, rating, catalogId, showLogo, fontFamily = "Inter", ratingFormat = "star", vignette = 0, vignetteOpts = null, metaColor = "", greyscale = false, resolution = 0, extra = {}, accent = "#8C75FF", brandTint = false }) {
  const L = BANNER_LAYOUTS[normalizedShape(shape)];
  const zoneW = Math.round(L.width * L.zone);
  // rank <= 0 → plain poster (no glass number): AIOMetadata /poster mode
  const showNumber = Number(rank) > 0;

  let artworkBuffer = await fetchBuffer(artworkUrl);
  if (greyscale) artworkBuffer = await sharp(artworkBuffer).greyscale().png(FAST_PNG).toBuffer();
  const titleLogo = await bannerTitleLogo(logoUrl, L);

  const base = await bannerBackdrop(artworkBuffer, L, zoneW);
  // Apply ratingFormat: hidden removes rating, number drops the star prefix
  const displayGenre = cleanGenre(genre);
  const rawRating = formatRating(rating);
  const displayRating = ratingFormat === "hidden" ? "" : rawRating;
  // Provider Logo chip: extra.providerLogo (the /poster provider_logo param)
  // decouples the provider logo from show_logo (the title-logo switch).
  const wantProvider = extra.providerLogo !== undefined
    ? (!!extra.providerLogo && !!catalogId)
    : !!(showLogo && catalogId);
  const provider = wantProvider ? getProviderLogoSvg(catalogId) : null;
  const meta = bannerMetaLayout(L, zoneW, {
    genre: displayGenre,
    rating: displayRating,
    provider,
    metaColor,
    extra
  });

  const layers = [];
  // Brand Tint chip (topx): a soft glow of the platform's own accent behind
  // the number zone, before the glass number and the meta line.
  if (brandTint && accent) {
    layers.push({ input: await brandTintOverlay(L, accent), left: 0, top: 0 });
  }
  if (showNumber) {
    const g = await bannerNumberGeometry(rank, L, zoneW, {
      hasProvider: Boolean(provider?.svg),
      hasMeta: Boolean(displayGenre || displayRating)
    });
    const glass = await bannerGlassNumber(base, L, g, fontFamily);
    layers.push(...glass);
    if (meta.height) layers.push({ input: meta.render(g.metaTop, g.centerX), left: 0, top: 0 });
  } else if (meta.height) {
    // No number: centre the meta line along the bottom zone
    layers.push({ input: meta.render(L.height * 0.86, L.width * 0.5), left: 0, top: 0 });
  }
  const logoShade = await bannerLogoShade(base, L, titleLogo);
  if (logoShade) layers.push(logoShade);
  if (titleLogo) layers.push(...titleLogo.layers);

  // Vignette overlay on top of everything (PostersPlus top/bottom gradients;
  // colour vignette tints with the artwork's dominant colour)
  const vigInput = vignetteOpts ?? vignette;
  if (vigInput) {
    const vCheck = normalizeVignetteOpts(vigInput);
    const artColor = (vCheck.colorTop || vCheck.colorBottom) ? await dominantColor(artworkBuffer) : "#000000";
    const vigBuf = await vignetteOverlay(L, vigInput, artColor);
    if (vigBuf) layers.push({ input: vigBuf, left: 0, top: 0 });
  }

  // Composite first, then scale: sharp always resizes before compositing in a
  // single pipeline, which would shrink the base while overlays stay full-size.
  const composited = await sharp(base).composite(layers)
    .jpeg({ quality: 88, chromaSubsampling: "4:4:4", mozjpeg: true })
    .toBuffer();

  const res = Number(resolution) || 0;
  if (res > 0 && res !== L.width) {
    const targetH = Math.round(L.height * (res / L.width));
    return sharp(composited).resize(Math.round(res), targetH)
      .jpeg({ quality: 88, chromaSubsampling: "4:4:4", mozjpeg: true }).toBuffer();
  }
  return composited;
}

export async function createTopCover({
  rank,
  artworkUrl,
  logoUrl = "",
  style = "classic",
  shape = "landscape",
  accent = "#FFFFFF",
  canvasBackground = "provider",
  genre = "",
  rating = "",
  catalogId = "",
  showLogo = true,
  fontFamily = "Inter",
  ratingFormat = "star",
  vignette = 0,
  vignetteOpts = null,
  metaColor = "",
  resolution = 0,
  greyscale = false,
  extra = {},
  brandTint = false
}) {
  if (!artworkUrl) throw new Error("artworkUrl mancante.");
  const requestedRank = Number(rank);
  // rank <= 0 / NaN → 0 → no rank number (plain poster, AIOMetadata mode)
  rank = Number.isFinite(requestedRank) ? Math.max(0, Math.min(10, Math.trunc(requestedRank))) : 0;

  if (canvasBackground === "fresh" || normalizeStyle(style) === "banner") {
    return createBannerCover({ rank, artworkUrl, logoUrl, shape, genre, rating, catalogId, showLogo, fontFamily, ratingFormat, vignette, vignetteOpts, metaColor, greyscale, resolution, extra, accent, brandTint });
  }

  const normalized = normalizedShape(shape);
  const layout = LAYOUTS[normalized];

  let artworkBuffer = await fetchBuffer(artworkUrl);
  if (greyscale) artworkBuffer = await sharp(artworkBuffer).greyscale().png(FAST_PNG).toBuffer();

  const vigInput = vignetteOpts ?? vignette;
  const [art, shadow, ambient, vig] = await Promise.all([
    roundedArtwork(artworkBuffer, layout),
    cardShadow(layout),
    brandAmbientGlow(layout, accent),
    (async () => {
      const vCheck = normalizeVignetteOpts(vigInput);
      const artColor = (vCheck.colorTop || vCheck.colorBottom) ? await dominantColor(artworkBuffer) : "#000000";
      return vignetteOverlay(layout, vigInput, artColor);
    })()
  ]);

  const { canvas, card } = layout;
  const bgLower = String(canvasBackground || "").toLowerCase().trim();
  const isProvider = bgLower === "provider" || bgLower === "brand";
  const isStremio = bgLower === "stremio" || bgLower === "stremio-navy" || bgLower === "rgb(26,23,62)" || bgLower === "rgb(26, 23, 62)" || bgLower === "#1a173e" || bgLower === "1a173e";

  const composites = [];

  // Soft brand tint around the number area (only for standard/neon modes)
  if (!isProvider) {
    composites.push({ input: ambient, left: 0, top: 0 });
  }

  // Number and metadata go behind the card.
  composites.push(
    { input: numberSvg(rank, layout, accent, genre, rating, canvasBackground, showLogo, catalogId, fontFamily, ratingFormat, metaColor, extra), left: 0, top: 0 },
    // Card shadow.
    {
      input: shadow,
      left: card.x - 60,
      top: card.y - 46 + 16
    },
    // Backdrop/poster.
    {
      input: art,
      left: card.x,
      top: card.y
    }
  );

  // Vignette goes on top of the artwork card to darken its edges.
  if (vig) {
    composites.push({ input: vig, left: 0, top: 0 });
  }

  let baseSharp;
  if (isProvider) {
    baseSharp = sharp(generateGlassBackground(canvas.width, canvas.height, "provider", accent, catalogId));
  } else if (isStremio) {
    baseSharp = sharp(generateGlassBackground(canvas.width, canvas.height, "stremio", accent, catalogId));
  } else {
    baseSharp = sharp({
      create: {
        width: canvas.width,
        height: canvas.height,
        channels: 4,
        background: parseBackgroundColor(canvasBackground)
      }
    });
  }

  // Composite first (sharp's internal pipeline always resizes BEFORE it
  // composites, so a single-pipe resize would shrink the base while the
  // overlays stay full-size → "Image to composite must have same dimensions").
  const composited = await baseSharp.composite(composites).png().toBuffer();

  // PostersPlus resolution param — scale output to the requested width,
  // height proportional. 0 / absent = native size.
  const res = Number(resolution) || 0;
  if (res > 0 && res !== canvas.width) {
    const targetH = Math.round(canvas.height * (res / canvas.width));
    return sharp(composited).resize(Math.round(res), targetH)
      .png({ compressionLevel: 8, adaptiveFiltering: true }).toBuffer();
  }

  return sharp(composited).png({ compressionLevel: 8, adaptiveFiltering: true }).toBuffer();
}
