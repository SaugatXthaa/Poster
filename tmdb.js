import {
  getTmdbMapping,
  setTmdbMapping,
  getTmdbImagesDb,
  setTmdbImagesDb
} from "./db.js";

const TMDB_API = "https://api.themoviedb.org/3";
const TMDB_IMAGE = "https://image.tmdb.org/t/p";
export const DEFAULT_TMDB_KEY = process.env.TMDB_API_KEY || "ad0f7351455041d8c9c0d4370a4b5fa5";

/* ─── Country → language mapping (PostersPlus-style i18n) ──────────────────
 * Maps an ISO 3166-1 alpha-2 country code to its primary TMDB language code.
 * Used both for /images?include_image_language= (artwork with burned-in text)
 * and for /details?language= (genre names, synopsis). Falls back to "en".
 * "world" or unknown → "en".
 */
const COUNTRY_LANG = {
  world: "en",
  it: "it",
  dz: "ar",
  au: "en",
  br: "pt-BR",
  ca: "en",
  hr: "hr",
  fr: "fr",
  de: "de",
  gr: "el",
  gt: "es",
  hu: "hu",
  in: "hi",
  id: "id",
  ie: "en",
  lb: "ar",
  my: "ms",
  mx: "es",
  ma: "ar",
  nl: "nl",
  ph: "en",
  pt: "pt",
  ro: "ro",
  es: "es",
  tr: "tr",
  gb: "en",
  us: "en",
};

export function langForCountry(country) {
  return COUNTRY_LANG[String(country || "").toLowerCase()] || "en";
}

/* Some TMDB language codes don't appear as image language tags — for image
 * searches we use the short 2-letter form (it, en, fr…). null = "no language"
 * (textless art). */
function imageLangsFor(country) {
  const full = langForCountry(country);
  const short = full.split("-")[0];
  // always include English + textless as fallback
  if (short === "en") return "en,null";
  return `${short},en,null`;
}

async function tmdbFetch(path, apiKey = DEFAULT_TMDB_KEY) {
  const key = apiKey || DEFAULT_TMDB_KEY;
  if (!key) throw new Error("TMDB API key mancante.");

  const sep = path.includes("?") ? "&" : "?";
  const url = `${TMDB_API}${path}${sep}api_key=${encodeURIComponent(key)}`;

  const response = await fetch(url, {
    headers: {
      "User-Agent": "topx-posters/1.0 (+https://github.com/SaugatXthaa)"
    }
  });

  if (!response.ok) {
    throw new Error(`TMDB error ${response.status}`);
  }

  return response.json();
}

export async function validateTmdbKey(apiKey = DEFAULT_TMDB_KEY) {
  await tmdbFetch("/configuration", apiKey);
  return true;
}

/* Country-aware image fetch — caches under (mediaType, tmdbId) because the
 * artwork set is mostly language-agnostic, but the sort order changes per
 * country. We re-sort at request time using the country code passed in. */
export async function getTmdbImages(type, tmdbId, apiKey = DEFAULT_TMDB_KEY, country = "world") {
  const mediaType = type === "series" || type === "tv" ? "tv" : "movie";

  // Check persistent DB first
  const dbData = getTmdbImagesDb(mediaType, tmdbId);
  if (dbData) return dbData;

  const includeLang = imageLangsFor(country);
  const data = await tmdbFetch(
    `/${mediaType}/${encodeURIComponent(tmdbId)}/images?include_image_language=${encodeURIComponent(includeLang)}`,
    apiKey
  );

  // Store in DB
  setTmdbImagesDb(mediaType, tmdbId, data);
  return data;
}

export async function getTmdbDetails(type, tmdbId, apiKey = DEFAULT_TMDB_KEY, country = "world") {
  if (!tmdbId) return null;
  const mediaType = type === "series" || type === "tv" ? "tv" : "movie";
  const lang = langForCountry(country);
  try {
    const data = await tmdbFetch(
      `/${mediaType}/${encodeURIComponent(tmdbId)}?language=${encodeURIComponent(lang)}`,
      apiKey
    );
    const genre = data.genres?.[0]?.name || "";
    const rating = data.vote_average && data.vote_average > 0 ? String(data.vote_average.toFixed(1)) : "";
    return { genre, rating, title: data.title || data.name };
  } catch (err) {
    console.warn(`TMDB details fallito per ${mediaType} ${tmdbId}:`, err.message);
    return null;
  }
}

/* ─── AIOMetadata stremio_id unwrapping ──────────────────────────────────────
 * PostersPlus accepts the raw Stremio/Nuvio meta id in stremio_id and unwraps
 * its prefixed forms before resolving to a TMDB id. AIOMetadata's row ids
 * (Up Next / Resume / Unwatched) carry the show's IMDb id inside their own
 * scheme, so we extract it first. Anime ids (kitsu/anilist/mal) get their own
 * resolver path. Anything else that contains exactly one IMDb id segment is
 * rendered as that title — same as PostersPlus.
 */
function unwrapStremioId(rawId) {
  if (!rawId) return null;
  const id = String(rawId).trim();
  if (!id) return null;
  // Anime namespaces — keep the prefix so resolveTmdbId can dispatch.
  if (/^(kitsu|anilist|mal|anidb):/i.test(id)) return id;
  // tmdb: namespace — keep.
  if (/^tmdb:/i.test(id)) return id;
  // Bare IMDb id (Stremio movie id)
  if (/^tt\d{6,}$/i.test(id)) return id;
  // Stremio/Nuvio episode id "tt0121955:1:2" — truncate to the show's IMDb id.
  const episodeMatch = id.match(/^(tt\d{6,}):\d+:\d+/i);
  if (episodeMatch) return episodeMatch[1];
  // AIOMetadata row ids — extract the embedded tt or tmdb: token.
  //   upnext_tt0121955_trakt123   → tt0121955
  //   mdblist_upnext_tmdb:2190_S1E1 → tmdb:2190
  //   unwatched_tt0121955          → tt0121955
  //   had:episode:tt13491734:6:6   → tt13491734 (whole-segment IMDb id)
  const ttMatch = id.match(/(^|[_:])tt\d{6,}/i);
  if (ttMatch) return ttMatch[0].replace(/^[_:]/, "");
  const tmdbMatch = id.match(/tmdb:\d+/i);
  if (tmdbMatch) return tmdbMatch[0];
  return null;
}

/* Anime id → TMDB id resolution. Uses the provider's API to fetch the title,
 * then searches TMDB for an Animation show whose name matches exactly and
 * started within ±1 year. Result cached for 2 weeks; miss cached for 1 day.
 * (PostersPlus URL_REFERENCE.md § "Anime IDs" describes the same approach.)
 */
async function resolveAnimeId(provider, rawId, apiKey) {
  const id = String(rawId).trim();
  const cacheKey = `anime:${provider}:${id}`;
  const cached = getTmdbMapping(cacheKey, "tv");
  if (cached !== undefined) return cached;

  let title = "";
  let year = 0;
  try {
    if (provider === "kitsu") {
      const r = await fetch(`https://kitsu.app/api/edge/anime/${id}?fields[anime]=canonicalTitle,startDate`, {
        headers: { "Accept": "application/vnd.api+json", "User-Agent": "topx-posters/1.0" }
      });
      if (r.ok) {
        const d = await r.json();
        title = d?.data?.attributes?.canonicalTitle || "";
        const sd = d?.data?.attributes?.startDate || "";
        year = sd ? parseInt(String(sd).slice(0, 4), 10) || 0 : 0;
      }
    } else if (provider === "anilist") {
      const r = await fetch("https://graphql.anilist.co", {
        method: "POST",
        headers: { "Content-Type": "application/json", "Accept": "application/json" },
        body: JSON.stringify({ query: `{ Media(id: ${id}, type: ANIME) { title { romaji english } startDate { year } } }` })
      });
      if (r.ok) {
        const d = await r.json();
        const m = d?.data?.Media;
        if (m) {
          title = m.title?.romaji || m.title?.english || "";
          year = m.startDate?.year || 0;
        }
      }
    } else if (provider === "mal") {
      // MAL API requires auth — try the public page meta endpoint as a fallback.
      const r = await fetch(`https://myanimelist.net/anime/${id}/_/video`, {
        headers: { "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36" }
      });
      if (r.ok) {
        const html = await r.text();
        const m = html.match(/<title>([^<]+)<\/title>/i);
        if (m) title = m[1].replace(/\s*-\s*MyAnimeList.*$/i, "").trim();
      }
    }
  } catch (err) {
    console.warn(`[anime] ${provider}:${id} fetch failed:`, err.message);
  }
  if (!title) { setTmdbMapping(cacheKey, "tv", null); return null; }

  // Search TMDB for a TV show matching the title in Animation.
  try {
    const yearParam = year ? `&first_air_date_year=${year}` : "";
    const sr = await tmdbFetch(
      `/search/tv?query=${encodeURIComponent(title)}${yearParam}&include_adult=false`,
      apiKey
    );
    const hits = (sr?.results || []).filter(r => r.genre_ids?.includes(16));
    const tmdbId = hits.length ? String(hits[0].id) : null;
    setTmdbMapping(cacheKey, "tv", tmdbId);
    return tmdbId;
  } catch (err) {
    console.warn(`[anime] TMDB search for "${title}" failed:`, err.message);
    setTmdbMapping(cacheKey, "tv", null);
    return null;
  }
}

export async function resolveTmdbId(type, rawId, apiKey = DEFAULT_TMDB_KEY) {
  const r = await resolveTmdbIdAndType(type, rawId, apiKey);
  return r ? r.tmdbId : null;
}

/* Like resolveTmdbId, but also returns the type TMDB reports for the title —
 * so a request for `?type=movie&stremio_id=tt0903747` (Breaking Bad, a TV
 * show) can render correctly with type=tv. PostersPlus rule: TMDB's type
 * wins. Returns { tmdbId, type } or null. */
export async function resolveTmdbIdAndType(typeHint, rawId, apiKey = DEFAULT_TMDB_KEY) {
  if (!rawId) return null;
  // Unwrap AIOMetadata row ids / Stremio episode ids first.
  const id = unwrapStremioId(rawId) || String(rawId);

  if (id.startsWith("tmdb:")) {
    const tmdbId = id.split(":")[1] || null;
    return tmdbId ? { tmdbId, type: typeHint || "movie" } : null;
  }

  // Anime namespaces — always TV.
  const anime = id.match(/^(kitsu|anilist|mal):(\d+)/i);
  if (anime) {
    const tmdbId = await resolveAnimeId(anime[1].toLowerCase(), anime[2], apiKey);
    return tmdbId ? { tmdbId, type: "tv" } : null;
  }

  if (/^\d+$/.test(id)) {
    return { tmdbId: id, type: typeHint || "movie" };
  }

  if (id.startsWith("tt")) {
    const wantsTv = typeHint === "series" || typeHint === "tv";
    const mediaType = wantsTv ? "tv" : "movie";

    // Cache check — both spellings, in case the IMDb id resolves under a
    // different type than the request asked for.
    const cachedRequested = getTmdbMapping(id, mediaType);
    const cachedOther = getTmdbMapping(id, wantsTv ? "movie" : "tv");
    if (cachedRequested !== undefined && cachedRequested) {
      return { tmdbId: cachedRequested, type: mediaType };
    }
    if (cachedOther !== undefined && cachedOther) {
      return { tmdbId: cachedOther, type: wantsTv ? "movie" : "tv" };
    }

    try {
      const data = await tmdbFetch(
        `/find/${encodeURIComponent(id)}?external_source=imdb_id`,
        apiKey
      );

      const wanted = wantsTv ? data.tv_results : data.movie_results;
      const other  = wantsTv ? data.movie_results : data.tv_results;
      const wantedId = wanted?.[0]?.id ? String(wanted[0].id) : null;
      const otherId  = other?.[0]?.id ? String(other[0].id) : null;

      setTmdbMapping(id, mediaType, wantedId);
      setTmdbMapping(id, wantsTv ? "movie" : "tv", otherId);

      if (wantedId) return { tmdbId: wantedId, type: mediaType };
      if (otherId)  return { tmdbId: otherId,  type: wantsTv ? "movie" : "tv" };
      return null;
    } catch (err) {
      console.warn(`TMDB find fallito per ${id}:`, err.message);
      return null;
    }
  }

  return null;
}

/* Country-aware language scoring — primary country lang first, then English,
 * then null (textless), then anything else. */
function scoreByLanguage(item, country) {
  const lang = langForCountry(country).split("-")[0];
  if (item.iso_639_1 === lang) return 0;
  if (item.iso_639_1 === "en") return 1;
  if (item.iso_639_1 == null) return 2;
  return 3;
}

function sortImages(items, country = "world") {
  return [...(items || [])]
    .filter(x => x.file_path)
    .sort((a, b) => {
      const lang = scoreByLanguage(a, country) - scoreByLanguage(b, country);
      if (lang !== 0) return lang;
      return (b.vote_average || 0) - (a.vote_average || 0);
    });
}

export function chooseBackdrop(images, country = "world") {
  const items = sortImages(images?.backdrops, country);
  return items.length ? `${TMDB_IMAGE}/w1280${items[0].file_path}` : null;
}

export function choosePoster(images, country = "world") {
  const items = sortImages(images?.posters, country);
  return items.length ? `${TMDB_IMAGE}/w780${items[0].file_path}` : null;
}

export function chooseLogo(images, country = "world") {
  const items = sortImages(images?.logos, country);
  return items.length ? `${TMDB_IMAGE}/w500${items[0].file_path}` : null;
}

/* PostersPlus logo_priority — orders logo languages:
 *   native    = the country's language
 *   english   = en
 *   custom    = logo_language_secondary
 *   original  = textless (iso_639_1 null)
 * Priority values mirror the configurator's cfg-logo-priority list. */
export function chooseLogoWithPriority(images, country = "world", priority = "native_original", secondary = "") {
  const logos = [...(images?.logos || [])].filter(x => x.file_path);
  if (!logos.length) return null;
  const lang = langForCountry(country).split("-")[0];
  const sec = String(secondary || "").split("-")[0];
  const band = item => {
    const code = item.iso_639_1;
    if (code == null) return "original";
    if (sec && code === sec) return "custom";
    if (code === lang) return "native";
    if (code === "en") return "english";
    return "other";
  };
  const ORDERS = {
    native_original:            { native: 0, original: 1, english: 2, custom: 3, other: 4 },
    native_english_original:    { native: 0, english: 1, original: 2, custom: 3, other: 4 },
    native_custom_original:     { native: 0, custom: 1, original: 2, english: 3, other: 4 },
    english_native_original:    { english: 0, native: 1, original: 2, custom: 3, other: 4 },
    original_native_english:    { original: 0, native: 1, english: 2, custom: 3, other: 4 },
    native_original_english:    { native: 0, original: 1, english: 2, custom: 3, other: 4 },
    native_only:                { native: 0, english: 1, custom: 2, original: 3, other: 4 },
    english_only:               { english: 0, native: 1, custom: 2, original: 3, other: 4 },
  };
  const order = ORDERS[String(priority || "native_original")] || ORDERS.native_original;
  const sorted = logos.sort((a, b) => {
    const d = (order[band(a)] ?? 9) - (order[band(b)] ?? 9);
    if (d !== 0) return d;
    return (b.vote_average || 0) - (a.vote_average || 0);
  });
  return `${TMDB_IMAGE}/w500${sorted[0].file_path}`;
}

/* PostersPlus poster_pick — "top" (default), "random", or a 1-based index
 * into the language-sorted poster list. Random is seeded from the list's
 * first path so the same title always renders the same pick (cache-stable). */
export function choosePosterPick(images, country = "world", pick = "top") {
  const items = sortImages(images?.posters, country);
  if (!items.length) return null;
  const s = String(pick ?? "top").toLowerCase().trim();
  if (s === "random") {
    let h = 0;
    for (const ch of items[0].file_path || "") h = (h * 31 + ch.charCodeAt(0)) >>> 0;
    const idx = items.length > 1 ? h % items.length : 0;
    return `${TMDB_IMAGE}/w780${items[idx].file_path}`;
  }
  const n = Number(s);
  if (Number.isFinite(n) && n > 0) {
    const idx = Math.min(items.length - 1, Math.trunc(n) - 1);
    return `${TMDB_IMAGE}/w780${items[idx].file_path}`;
  }
  return `${TMDB_IMAGE}/w780${items[0].file_path}`;
}

function chooseTextless(items, size) {
  const textless = [...(items || [])]
    .filter(x => x.file_path && !x.iso_639_1)
    .sort((a, b) => (b.vote_average || 0) - (a.vote_average || 0));

  return textless.length ? `${TMDB_IMAGE}/${size}${textless[0].file_path}` : null;
}

export function chooseTextlessPoster(images) {
  return chooseTextless(images?.posters, "w780");
}

export function chooseTextlessBackdrop(images) {
  return chooseTextless(images?.backdrops, "w1280");
}

// Banner: textless art + title logo when both exist, otherwise the regular art.
export function chooseBannerArtwork(images, shape, country = "world") {
  const logoUrl = chooseLogo(images, country);
  const textless = logoUrl
    ? (shape === "poster" ? chooseTextless(images?.posters, "w780") : chooseTextless(images?.backdrops, "w1280"))
    : null;

  return textless
    ? { artworkUrl: textless, logoUrl }
    : {
        artworkUrl: shape === "poster" ? choosePoster(images, country) : chooseBackdrop(images, country),
        logoUrl: ""
      };
}
