// MultiSourceSubtitles — Aggregates subtitles from multiple independent sources.
// Sources:
//   1. SubtitleCat.com — 11-67+ languages per title, direct .srt downloads
//   2. YTS-Subs.com — 50-200+ subtitle entries per movie, .zip downloads
//
// No PenguPlay, no auth. Pure Node.js + cheerio. Works on Render free tier.
// ============================================================================

'use strict';

const cheerio = require('cheerio-without-node-native');
const TMDB_API_KEY = '8476a7ab80ad76f0936744df0430e67c';
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';

const LANG_MAP = {
  af:'Afrikaans',ak:'Akan',sq:'Albanian',am:'Amharic',ar:'Arabic',hy:'Armenian',az:'Azerbaijani',
  eu:'Basque',be:'Belarusian',bn:'Bengali',bg:'Bulgarian',bs:'Bosnian',my:'Burmese',ca:'Catalan',
  ceb:'Cebuano',ny:'Chichewa','zh-CN':'Chinese (Simplified)','zh-TW':'Chinese (Traditional)',
  hr:'Croatian',cs:'Czech',da:'Danish',nl:'Dutch',en:'English',et:'Estonian',fi:'Finnish',
  fr:'French',fy:'Frisian',gl:'Galician',ka:'Georgian',de:'German',el:'Greek',gu:'Gujarati',
  ht:'Haitian',ha:'Hausa',he:'Hebrew',hi:'Hindi',iw:'Hebrew',hu:'Hungarian',is:'Icelandic',
  ig:'Igbo',id:'Indonesian',ga:'Irish',it:'Italian',ja:'Japanese',jw:'Javanese',kn:'Kannada',
  kk:'Kazakh',km:'Khmer',rw:'Kinyarwanda',ko:'Korean',ku:'Kurdish',ky:'Kyrgyz',lo:'Lao',
  la:'Latin',lv:'Latvian',lt:'Lithuanian',lb:'Luxembourgish',mk:'Macedonian',mg:'Malagasy',
  ms:'Malay',ml:'Malayalam',mt:'Maltese',mi:'Maori',mr:'Marathi',mn:'Mongolian',ne:'Nepali',
  no:'Norwegian',ps:'Pashto',fa:'Persian',pl:'Polish',pt:'Portuguese',pa:'Punjabi',ro:'Romanian',
  ru:'Russian',sm:'Samoan',gd:'Scots Gaelic',sr:'Serbian',st:'Sesotho',sn:'Shona',sd:'Sindhi',
  si:'Sinhala',sk:'Slovak',sl:'Slovenian',so:'Somali',es:'Spanish',su:'Sundanese',sw:'Swahili',
  sv:'Swedish',tg:'Tajik',ta:'Tamil',te:'Telugu',th:'Thai',tr:'Turkish',uk:'Ukrainian',
  ur:'Urdu',uz:'Uzbek',vi:'Vietnamese',cy:'Welsh',xh:'Xhosa',yi:'Yiddish',yo:'Yoruba',zu:'Zulu',
  br:'Breton',tl:'Tagalog',tw:'Twi',mk:'Macedonian',sh:'Serbo-Croatian',ckb:'Kurdish (Sorani)',
};

function fetchText(url, referer) {
  const headers = { 'User-Agent': UA, 'Accept': 'text/html,application/json,*/*' };
  if (referer) headers['Referer'] = referer;
  return fetch(url, { headers, redirect: 'follow' }).then(r => {
    if (!r.ok) throw new Error('HTTP ' + r.status);
    return r.text();
  });
}

async function getTMDBInfo(id, type) {
  const mediaType = type === 'tv' || type === 'series' ? 'tv' : 'movie';
  if (/^tt\d+$/.test(String(id))) {
    try {
      const d = JSON.parse(await fetchText(`https://api.themoviedb.org/3/find/${id}?api_key=${TMDB_API_KEY}&external_source=imdb_id`));
      const r = mediaType === 'tv' ? d.tv_results : d.movie_results;
      if (r && r[0]) return { title: mediaType==='tv'?r[0].name:r[0].title, year: ((r[0].first_air_date||r[0].release_date||'')+'').split('-')[0] };
    } catch(e){}
  }
  try {
    const d = JSON.parse(await fetchText(`https://api.themoviedb.org/3/${mediaType}/${id}?api_key=${TMDB_API_KEY}`));
    return { title: mediaType==='tv'?d.name:d.title, year: ((d.first_air_date||d.release_date||'')+'').split('-')[0] };
  } catch(e) { return null; }
}

// ═════════════════════════════════════════════════════════════════════════
// SOURCE 1: SubtitleCat.com
// ═════════════════════════════════════════════════════════════════════════
async function searchSubtitleCat(title, year, isMovie, season, episode) {
  let query = title + (year ? ' ' + year : '');
  if (!isMovie && season && episode) {
    query = title + ' S' + String(season).padStart(2,'0') + 'E' + String(episode).padStart(2,'0');
  }
  const searchUrl = 'https://www.subtitlecat.com/index.php?search=' + encodeURIComponent(query);
  let html;
  try { html = await fetchText(searchUrl); } catch(e) { return []; }
  const $ = cheerio.load(html);
  const results = [];
  $('a[href*="subs/"]').each((_, el) => {
    const href = $(el).attr('href') || '';
    const text = $(el).text().trim();
    if (href.endsWith('.html') && text.length > 5 && text.length < 200) {
      const fullUrl = href.startsWith('http') ? href : 'https://www.subtitlecat.com/' + href;
      if (!results.find(r => r.url === fullUrl)) results.push({ url: fullUrl, title: text });
    }
  });
  // If TV SxxExx search found nothing, try title only
  if (!isMovie && !results.length) {
    try {
      html = await fetchText('https://www.subtitlecat.com/index.php?search=' + encodeURIComponent(title + (year ? ' ' + year : '')));
      const $2 = cheerio.load(html);
      $2('a[href*="subs/"]').each((_, el) => {
        const href = $2(el).attr('href') || '';
        const text = $2(el).text().trim();
        if (href.endsWith('.html') && text.length > 5 && text.length < 200) {
          const sxxexx = text.match(/S(\d{1,2})E(\d{1,2})/i);
          if (sxxexx && parseInt(sxxexx[1])===parseInt(season) && parseInt(sxxexx[2])===parseInt(episode)) {
            const fullUrl = href.startsWith('http') ? href : 'https://www.subtitlecat.com/' + href;
            if (!results.find(r => r.url === fullUrl)) results.push({ url: fullUrl, title: text });
          }
        }
      });
    } catch(e) {}
  }
  return results;
}

async function parseSubtitleCatPage(pageUrl) {
  let html;
  try { html = await fetchText(pageUrl, 'https://www.subtitlecat.com/'); } catch(e) { return []; }
  const $ = cheerio.load(html);
  const downloadLangs = new Set();
  $('[onclick*="log_download"]').each((_, el) => {
    const m = ($(el).attr('onclick')||'').match(/show_voting\(['"]([a-z]+)['"]\)/);
    if (m) downloadLangs.add(m[1]);
  });
  // Also collect translate langs
  $('[onclick*="translate_from_server_folder"]').each((_, el) => {
    const m = ($(el).attr('onclick')||'').match(/translate_from_server_folder\(['"]([a-z]+)['"]/);
    if (m) downloadLangs.add(m[1]);
  });
  // Find ALL .srt URLs in the HTML
  const subs = [];
  const srtRegex = /\/subs\/\d+\/[^"'\s]+-([a-z]+)\.srt/gi;
  let match;
  while ((match = srtRegex.exec(html)) !== null) {
    const fullUrl = 'https://www.subtitlecat.com' + match[0];
    const langCode = match[1];
    if (downloadLangs.has(langCode) || langCode === 'orig') {
      if (!subs.find(s => s.url === fullUrl)) {
        const name = LANG_MAP[langCode] || langCode;
        subs.push({ url: fullUrl, lang: langCode === 'orig' ? 'en' : langCode, name: (langCode==='orig'?'English (Original)':name) + ' [SubtitleCat]' });
      }
    }
  }
  return subs;
}

async function getSubtitleCatSubs(info, type, season, episode) {
  const isMovie = type !== 'tv' && type !== 'series';
  const results = await searchSubtitleCat(info.title, info.year, isMovie, season, episode);
  if (!results.length) return [];
  const allSubs = [];
  const seen = new Set();
  for (const entry of results.slice(0, 10)) {
    try {
      const subs = await parseSubtitleCatPage(entry.url);
      for (const s of subs) {
        if (!seen.has(s.url)) { seen.add(s.url); allSubs.push(s); }
      }
    } catch(e) {}
  }
  return allSubs;
}

// ═════════════════════════════════════════════════════════════════════════
// SOURCE 2: YTS-Subs.com
// ═════════════════════════════════════════════════════════════════════════
async function getYTSSubs(imdbId) {
  const movieUrl = 'https://yts-subs.com/movie-imdb/' + imdbId;
  let html;
  try { html = await fetchText(movieUrl); } catch(e) { return []; }
  const $ = cheerio.load(html);
  const subs = [];
  // Parse table rows for language + subtitle entry URL
  $('tr').each((_, el) => {
    const text = $(el).text().trim().replace(/\s+/g, ' ');
    const link = $(el).find('a[href*="/subtitles/"]').first();
    if (link.length) {
      const href = link.attr('href') || '';
      // Extract language from text (format: "rating N Language subtitle ... download")
      const langMatch = text.match(/(?:\d+\s+)?([A-Za-z][A-Za-z\s]+?)\s+subtitle/i);
      const langName = langMatch ? langMatch[1].trim() : 'Unknown';
      // Map language name to code
      let langCode = 'en';
      for (const [code, name] of Object.entries(LANG_MAP)) {
        if (name.toLowerCase() === langName.toLowerCase()) { langCode = code; break; }
      }
      // Construct .zip URL: https://subtitles.yts-subs.com + href + .zip
      const entryPath = href.replace(/^https?:\/\/[^/]+/, '');
      const zipUrl = 'https://subtitles.yts-subs.com' + entryPath + '.zip';
      if (!subs.find(s => s.url === zipUrl)) {
        subs.push({ url: zipUrl, lang: langCode, name: langName + ' [YTS-Subs]' });
      }
    }
  });
  return subs;
}

// ═════════════════════════════════════════════════════════════════════════
// MAIN: getSubtitles(id, type, season, episode)
// ═════════════════════════════════════════════════════════════════════════
async function getSubtitles(id, type, season, episode) {
  console.log('[Subs] Request: id=' + id + ' type=' + type + (season ? ' S'+season+'E'+episode : ''));
  const info = await getTMDBInfo(id, type);
  if (!info || !info.title) { console.log('[Subs] No TMDB info'); return []; }
  console.log('[Subs] TMDB: ' + info.title + ' (' + info.year + ')');

  // Call both sources in parallel
  const [catSubs, ytsSubs] = await Promise.allSettled([
    getSubtitleCatSubs(info, type, season, episode),
    getYTSSubs(String(id).startsWith('tt') ? String(id) : ''),
  ]);

  const catResults = catSubs.status === 'fulfilled' ? catSubs.value : [];
  const ytsResults = ytsSubs.status === 'fulfilled' ? ytsSubs.value : [];
  console.log('[Subs] SubtitleCat: ' + catResults.length + ' | YTS-Subs: ' + ytsResults.length);

  // Combine + dedupe by URL
  const seen = new Set();
  const all = [];
  for (const s of [...catResults, ...ytsResults]) {
    if (!seen.has(s.url)) { seen.add(s.url); all.push(s); }
  }
  console.log('[Subs] Total: ' + all.length + ' subtitles');
  return all;
}

if (require.main === module) {
  (async () => {
    const args = process.argv.slice(2);
    if (!args[0]) { console.log('Usage: node multisource_subtitles.js <imdbId> <movie|tv> [season] [episode]'); process.exit(0); }
    const subs = await getSubtitles(args[0], args[1]||'movie', args[2], args[3]);
    console.log('\n' + subs.length + ' subtitles:');
    const langs = [...new Set(subs.map(s => s.lang))];
    console.log('Unique languages: ' + langs.length + ' (' + langs.join(', ') + ')');
    subs.slice(0, 10).forEach((s, i) => console.log('[' + i + '] ' + s.lang + ' ' + s.name + '\n    ' + s.url.slice(0, 150)));
  })();
}

module.exports = { getSubtitles };
