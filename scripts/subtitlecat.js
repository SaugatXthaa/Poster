// SubtitleCat — Multi-language subtitle scraper.
// Returns direct .srt download URLs for movies and TV shows.
//
// ARCHITECTURE:
//   1. IMDB ID → TMDB → title + year
//   2. Search subtitlecat.com/index.php?search=<title> → find subtitle pages
//   3. Fetch subtitle detail page → parse available languages
//   4. Construct .srt download URLs: subtitlecat.com/subs/<id>/<title>-<lang>.srt
//   5. Return Stremio-compatible subtitle objects: { url, lang, name }
//
// SubtitleCat has 11+ languages already available per subtitle + 131 auto-
// translatable languages. We return only the already-available ones (instant
// download). Translation would require server-side processing.
//
// PURE NODE.JS — no PenguPlay, no auth, works on Render free tier.
// ============================================================================

'use strict';

const https = require('https');
const cheerio = require('cheerio-without-node-native');

const PROVIDER_NAME = 'SubtitleCat';
const BASE_URL = 'https://www.subtitlecat.com';
const TMDB_API_KEY = '8476a7ab80ad76f0936744df0430e67c';
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';

// SubtitleCat language code → Stremio ISO 639-1 + human-readable name
const LANG_MAP = {
  af: { code: 'af', name: 'Afrikaans' }, ak: { code: 'ak', name: 'Akan' },
  sq: { code: 'sq', name: 'Albanian' }, am: { code: 'am', name: 'Amharic' },
  ar: { code: 'ar', name: 'Arabic' }, hy: { code: 'hy', name: 'Armenian' },
  az: { code: 'az', name: 'Azerbaijani' }, eu: { code: 'eu', name: 'Basque' },
  be: { code: 'be', name: 'Belarusian' }, bn: { code: 'bn', name: 'Bengali' },
  bg: { code: 'bg', name: 'Bulgarian' }, bs: { code: 'bs', name: 'Bosnian' },
  my: { code: 'my', name: 'Burmese' }, ca: { code: 'ca', name: 'Catalan' },
 ceb: { code: 'ceb', name: 'Cebuano' }, ny: { code: 'ny', name: 'Chichewa' },
  'zh-CN': { code: 'zh-CN', name: 'Chinese (Simplified)' },
  'zh-TW': { code: 'zh-TW', name: 'Chinese (Traditional)' },
  hr: { code: 'hr', name: 'Croatian' }, cs: { code: 'cs', name: 'Czech' },
  da: { code: 'da', name: 'Danish' }, nl: { code: 'nl', name: 'Dutch' },
  en: { code: 'en', name: 'English' }, eo: { code: 'eo', name: 'Esperanto' },
  et: { code: 'et', name: 'Estonian' }, fi: { code: 'fi', name: 'Finnish' },
  fr: { code: 'fr', name: 'French' }, fy: { code: 'fy', name: 'Frisian' },
  gl: { code: 'gl', name: 'Galician' }, ka: { code: 'ka', name: 'Georgian' },
  de: { code: 'de', name: 'German' }, el: { code: 'el', name: 'Greek' },
  gu: { code: 'gu', name: 'Gujarati' }, ht: { code: 'ht', name: 'Haitian' },
  ha: { code: 'ha', name: 'Hausa' }, he: { code: 'he', name: 'Hebrew' },
  hi: { code: 'hi', name: 'Hindi' }, iw: { code: 'he', name: 'Hebrew' },
  hu: { code: 'hu', name: 'Hungarian' }, is: { code: 'is', name: 'Icelandic' },
  ig: { code: 'ig', name: 'Igbo' }, id: { code: 'id', name: 'Indonesian' },
  ga: { code: 'ga', name: 'Irish' }, it: { code: 'it', name: 'Italian' },
  ja: { code: 'ja', name: 'Japanese' }, jw: { code: 'jw', name: 'Javanese' },
  kn: { code: 'kn', name: 'Kannada' }, kk: { code: 'kk', name: 'Kazakh' },
  km: { code: 'km', name: 'Khmer' }, rw: { code: 'rw', name: 'Kinyarwanda' },
  ko: { code: 'ko', name: 'Korean' }, ku: { code: 'ku', name: 'Kurdish' },
  ky: { code: 'ky', name: 'Kyrgyz' }, lo: { code: 'lo', name: 'Lao' },
  la: { code: 'la', name: 'Latin' }, lv: { code: 'lv', name: 'Latvian' },
  lt: { code: 'lt', name: 'Lithuanian' }, lb: { code: 'lb', name: 'Luxembourgish' },
  mk: { code: 'mk', name: 'Macedonian' }, mg: { code: 'mg', name: 'Malagasy' },
  ms: { code: 'ms', name: 'Malay' }, ml: { code: 'ml', name: 'Malayalam' },
  mt: { code: 'mt', name: 'Maltese' }, mi: { code: 'mi', name: 'Maori' },
  mr: { code: 'mr', name: 'Marathi' }, mn: { code: 'mn', name: 'Mongolian' },
  ne: { code: 'ne', name: 'Nepali' }, no: { code: 'no', name: 'Norwegian' },
  ps: { code: 'ps', name: 'Pashto' }, fa: { code: 'fa', name: 'Persian' },
  pl: { code: 'pl', name: 'Polish' }, pt: { code: 'pt', name: 'Portuguese' },
  pa: { code: 'pa', name: 'Punjabi' }, ro: { code: 'ro', name: 'Romanian' },
  ru: { code: 'ru', name: 'Russian' }, sm: { code: 'sm', name: 'Samoan' },
  gd: { code: 'gd', name: 'Scots Gaelic' }, sr: { code: 'sr', name: 'Serbian' },
  st: { code: 'st', name: 'Sesotho' }, sn: { code: 'sn', name: 'Shona' },
  sd: { code: 'sd', name: 'Sindhi' }, si: { code: 'si', name: 'Sinhala' },
  sk: { code: 'sk', name: 'Slovak' }, sl: { code: 'sl', name: 'Slovenian' },
  so: { code: 'so', name: 'Somali' }, es: { code: 'es', name: 'Spanish' },
  su: { code: 'su', name: 'Sundanese' }, sw: { code: 'sw', name: 'Swahili' },
  sv: { code: 'sv', name: 'Swedish' }, tg: { code: 'tg', name: 'Tajik' },
  ta: { code: 'ta', name: 'Tamil' }, te: { code: 'te', name: 'Telugu' },
  th: { code: 'th', name: 'Thai' }, tr: { code: 'tr', name: 'Turkish' },
  uk: { code: 'uk', name: 'Ukrainian' }, ur: { code: 'ur', name: 'Urdu' },
  uz: { code: 'uz', name: 'Uzbek' }, vi: { code: 'vi', name: 'Vietnamese' },
  cy: { code: 'cy', name: 'Welsh' }, xh: { code: 'xh', name: 'Xhosa' },
  yi: { code: 'yi', name: 'Yiddish' }, yo: { code: 'yo', name: 'Yoruba' },
  zu: { code: 'zu', name: 'Zulu' },
};

// ─── HTTP helper ───────────────────────────────────────────────────────────
function fetchText(url, referer) {
  const headers = { 'User-Agent': UA, 'Accept': 'text/html,application/json,*/*' };
  if (referer) headers['Referer'] = referer;
  return fetch(url, { headers, redirect: 'follow' }).then(r => {
    if (!r.ok) throw new Error('HTTP ' + r.status);
    return r.text();
  });
}

// ─── TMDB ────────────────────────────────────────────────────────────────────
async function getTMDBInfo(id, type) {
  const mediaType = type === 'tv' || type === 'series' ? 'tv' : 'movie';
  if (/^tt\d+$/.test(String(id))) {
    try {
      const findUrl = `https://api.themoviedb.org/3/find/${id}?api_key=${TMDB_API_KEY}&external_source=imdb_id`;
      const d = JSON.parse(await fetchText(findUrl));
      const results = mediaType === 'tv' ? d.tv_results : d.movie_results;
      if (results && results[0]) {
        return {
          title: mediaType === 'tv' ? results[0].name : results[0].title,
          year: ((results[0].first_air_date || results[0].release_date || '') + '').split('-')[0],
          type: mediaType,
        };
      }
    } catch (e) {}
  }
  try {
    const url = `https://api.themoviedb.org/3/${mediaType}/${id}?api_key=${TMDB_API_KEY}`;
    const d = JSON.parse(await fetchText(url));
    return {
      title: mediaType === 'tv' ? d.name : d.title,
      year: ((d.first_air_date || d.release_date || '') + '').split('-')[0],
      type: mediaType,
    };
  } catch (e) { return null; }
}

// ─── Search subtitlecat.com ─────────────────────────────────────────────────
async function searchSubtitles(title) {
  const searchUrl = `${BASE_URL}/index.php?search=${encodeURIComponent(title)}`;
  console.log('[SubtitleCat] Searching: ' + searchUrl);
  const html = await fetchText(searchUrl);
  const $ = cheerio.load(html);

  const results = [];
  // SubtitleCat lists results as relative links: subs/ID/title.html
  $('a[href*="subs/"]').each((_, el) => {
    const href = $(el).attr('href') || '';
    const text = $(el).text().trim();
    // Only keep .html links (detail pages), not .srt links
    if (href.endsWith('.html') && text.length > 5 && text.length < 200) {
      const fullUrl = href.startsWith('http') ? href : BASE_URL + '/' + href;
      if (!results.find(r => r.url === fullUrl)) {
        results.push({ url: fullUrl, title: text, href: href });
      }
    }
  });

  console.log('[SubtitleCat] Found ' + results.length + ' subtitle entries');
  return results;
}

// ─── Find best matching subtitle entry ──────────────────────────────────────
function findBestMatch(results, title, year) {
  if (!results.length) return null;
  const normQuery = title.toLowerCase().replace(/[^a-z0-9\s]/g, ' ').replace(/\s+/g, ' ').trim();
  const y = parseInt(year, 10) || 0;

  let best = null, bestScore = 0;
  for (const r of results) {
    const raw = r.title || '';
    const yearMatch = raw.match(/\b(19|20)\d{2}\b/);
    const resultYear = yearMatch ? parseInt(yearMatch[0], 10) : 0;
    const bare = raw.replace(/\s*[\(\[]?(19|20)\d{2}[\)\]]?.*$/i, '').trim();
    const normBare = bare.toLowerCase().replace(/[^a-z0-9\s]/g, ' ').replace(/\s+/g, ' ').trim();
    let score = 0;
    if (normBare === normQuery) score += 100;
    else if (normBare.startsWith(normQuery) || normQuery.startsWith(normBare)) score += 70;
    else if (normBare.split(' ').slice(0, 3).join(' ') === normQuery.split(' ').slice(0, 3).join(' ')) score += 40;
    if (y && resultYear && y === resultYear) score += 30;
    if (score > bestScore) { bestScore = score; best = r; }
  }
  if (best && bestScore >= 30) {
    console.log('[SubtitleCat] Best match: ' + best.title.slice(0, 80) + ' (score=' + bestScore + ')');
    return best;
  }
  // Return top 3 results if no strong match
  return results.slice(0, 3);
}

// ─── Parse subtitle detail page for available languages ────────────────────
async function parseSubtitlePage(pageUrl) {
  const html = await fetchText(pageUrl, BASE_URL + '/');
  const $ = cheerio.load(html);

  // Find the "Download" languages (already available) from onclick handlers
  const downloadLangs = new Set();
  $('[onclick*="log_download"]').each((_, el) => {
    const onclick = $(el).attr('onclick') || '';
    const m = onclick.match(/show_voting\(['"]([a-z]+)['"]\)/);
    if (m) downloadLangs.add(m[1]);
  });

  // Find ALL actual .srt URLs in the page HTML
  // Pattern: subs/<id>/<filename>-<lang>.srt
  const srtPattern = /\/subs\/(\d+)\/([^"'\s]+)-([a-z]+)\.srt/gi;
  const srtMatches = [];
  let match;
  while ((match = srtPattern.exec(html)) !== null) {
    const fullUrl = BASE_URL + match[0];
    const langCode = match[3];
    // Only include if this language is in the "Download" list, OR it's 'orig'
    if (downloadLangs.has(langCode) || langCode === 'orig') {
      if (!srtMatches.find(s => s.url === fullUrl)) {
        srtMatches.push({ url: fullUrl, langCode });
      }
    }
  }

  // Also check for the orig.srt file
  const origPattern = /\/subs\/(\d+)\/([^"'\s]+)-orig\.srt/gi;
  while ((match = origPattern.exec(html)) !== null) {
    const fullUrl = BASE_URL + match[0];
    if (!srtMatches.find(s => s.url === fullUrl)) {
      srtMatches.push({ url: fullUrl, langCode: 'orig' });
    }
  }

  // Map to Stremio subtitle format
  const subtitles = srtMatches.map(s => {
    const langInfo = LANG_MAP[s.langCode] || { code: s.langCode, name: s.langCode };
    return {
      url: s.url,
      lang: langInfo.code,
      name: (s.langCode === 'orig' ? 'English (Original)' : langInfo.name),
      langCode: s.langCode,
    };
  });

  // Also collect "Translate" languages — try their .srt URLs
  // Some translations might already exist (done by other users via the web UI)
  const translateLangs = new Set();
  $('[onclick*="translate_from_server_folder"]').each((_, el) => {
    const onclick = $(el).attr('onclick') || '';
    const m = onclick.match(/translate_from_server_folder\(['"]([a-z]+)['"]/);
    if (m) translateLangs.add(m[1]);
  });

  // For translate languages, try to find their .srt URL in the page HTML
  // Pattern: subs/<id>/<filename>-<lang>.srt (even for translate ones — if already generated)
  for (const langCode of translateLangs) {
    // Check if the .srt URL already exists in the page HTML
    const srtRegex = new RegExp('(/subs/\\d+/[^"\'\\s]+-' + langCode + '\\.srt)', 'i');
    const srtMatch = html.match(srtRegex);
    if (srtMatch) {
      const fullUrl = BASE_URL + srtMatch[1];
      if (!subtitles.find(s => s.url === fullUrl)) {
        const langInfo = LANG_MAP[langCode] || { code: langCode, name: langCode };
        subtitles.push({
          url: fullUrl,
          lang: langInfo.code,
          name: langInfo.name,
          langCode: langCode,
        });
      }
    }
  }

  console.log('[SubtitleCat] Available: ' + subtitles.length + ' languages (' +
    subtitles.map(s => s.langCode).join(', ') + ')');
  return subtitles;
}

// ─── Main entry: getSubtitles(id, type, season, episode) ────────────────────
async function getSubtitles(id, type, season, episode) {
  console.log('[SubtitleCat] Request: id=' + id + ' type=' + type +
    (season ? ' S' + season + 'E' + episode : ''));

  // 1. Get title + year from TMDB
  const info = await getTMDBInfo(id, type);
  if (!info || !info.title) {
    console.log('[SubtitleCat] Could not resolve TMDB info');
    return [];
  }
  console.log('[SubtitleCat] TMDB: ' + info.title + ' (' + info.year + ')');

  const isMovie = type !== 'tv' && type !== 'series';

  // 2. Search subtitlecat.com — for TV, include season/episode in query
  let searchQuery = info.title + (info.year ? ' ' + info.year : '');
  if (!isMovie && season && episode) {
    // For TV: try "Title S01E01" format first (more specific)
    searchQuery = info.title + ' S' + String(season).padStart(2, '0') + 'E' + String(episode).padStart(2, '0');
  }
  const results = await searchSubtitles(searchQuery);
  // If TV search with SxxExx found nothing, try without it
  if (!isMovie && !results.length) {
    console.log('[SubtitleCat] No results with SxxExx, trying title only');
    const results2 = await searchSubtitles(info.title + (info.year ? ' ' + info.year : ''));
    // Filter for matching season/episode
    const filtered = results2.filter(r => {
      const sxxexx = r.title.match(/S(\d{1,2})E(\d{1,2})/i);
      if (!sxxexx) return false; // skip non-episode entries
      return parseInt(sxxexx[1], 10) === parseInt(season, 10) &&
             parseInt(sxxexx[2], 10) === parseInt(episode, 10);
    });
    if (filtered.length > 0) {
      console.log('[SubtitleCat] Found ' + filtered.length + ' matching S' + season + 'E' + episode + ' episodes');
      results.push(...filtered);
    } else {
      // Fall back to all results (might have episode-specific subtitles with different naming)
      results.push(...results2);
    }
  }
  if (!results.length) return [];

  // 3. Find best matches — always try top 5 entries for max language coverage
  const matches = findBestMatch(results, info.title, info.year);
  if (!matches) return [];

  // If findBestMatch returned a single entry, still try ALL results for max language coverage
  const entries = Array.isArray(matches) ? matches : [matches];
  // Add ALL other results to collect maximum languages across different subtitle entries
  for (const r of results) {
    if (!entries.find(e => e.url === r.url)) entries.push(r);
  }
  // Cap at 10 entries to avoid excessive requests (each entry = 1 HTTP request)
  const maxEntries = entries.slice(0, 10);

  // 4. Parse each entry for available languages
  const allSubs = [];
  const seenUrls = new Set();
  for (const entry of maxEntries) {
    try {
      const langs = await parseSubtitlePage(entry.url);
      for (const sub of langs) {
        if (!seenUrls.has(sub.url)) {
          seenUrls.add(sub.url);
          allSubs.push({
            url: sub.url,
            lang: sub.lang,
            name: sub.name + ' [' + PROVIDER_NAME + ']',
            origin: PROVIDER_NAME,
          });
        }
      }
    } catch (e) {
      console.log('[SubtitleCat] Failed to parse ' + entry.url + ': ' + e.message);
    }
  }

  console.log('[SubtitleCat] Returning ' + allSubs.length + ' subtitles');
  return allSubs;
}

// ─── CLI ───────────────────────────────────────────────────────────────────────
if (require.main === module) {
  (async () => {
    const args = process.argv.slice(2);
    if (!args[0]) {
      console.log('Usage: node subtitlecat.js <imdbId> <movie|tv> [season] [episode]');
      console.log('Returns direct .srt download URLs for multiple languages');
      process.exit(0);
    }
    const subs = await getSubtitles(args[0], args[1] || 'movie', args[2], args[3]);
    console.log('\n' + subs.length + ' subtitles:');
    subs.forEach((s, i) => console.log('[' + i + '] ' + s.lang + ' - ' + s.name + '\n    ' + s.url));
  })();
}

module.exports = { getSubtitles };
