'use strict';

const { execSync } = require('child_process');
const TMDB_API_KEY = '8476a7ab80ad76f0936744df0430e67c';
const UA = 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';
const MOBILE_UA = 'Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36';

async function fetchText(url, ua = UA, timeout = 15000) {
  try {
    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), timeout);
    const r = await fetch(url, { headers: { 'User-Agent': ua }, signal: controller.signal });
    return { status: r.status, body: await r.text() };
  } catch { return { status: 0, body: '' }; }
}

function fetchCurl(url, ua = MOBILE_UA, timeout = 15000) {
  try {
    const result = execSync(
      `curl -s -L --max-time ${Math.floor(timeout/1000)} -A "${ua}" "${url}"`,
      { timeout: timeout + 2000, encoding: 'utf-8', maxBuffer: 5 * 1024 * 1024 }
    );
    return { status: 200, body: result };
  } catch { return { status: 0, body: '' }; }
}

async function getTmdbInfo(tmdbId, type) {
  try {
    const r = await fetch(`https://api.themoviedb.org/3/${type}/${tmdbId}?api_key=${TMDB_API_KEY}`);
    const d = await r.json();
    return { title: type === 'tv' ? d.name : d.title, year: (d.release_date || d.first_air_date || '').slice(0, 4) };
  } catch { return { title: '', year: '' }; }
}

async function getStreams(tmdbId, type, season, episode) {
  const isTV = type === 'tv' || type === 'series';
  const mediaType = isTV ? 'tv' : 'movie';
  const info = await getTmdbInfo(tmdbId, mediaType);
  const title = info.title || `TMDB ${tmdbId}`;
  const s = season || 1;
  const e = episode || 1;
  const streams = [];

  // 1. bcine.ru → 1embed.cc (curl for CF bypass)
  try {
    const r = await fetchText(`https://bcine.ru/${mediaType}/${tmdbId}`);
    if (r.status === 200) {
      const embedMatch = r.body.match(/https:\/\/1embed\.cc\/download\/[^"'\s]+/);
      if (embedMatch) {
        const embedR = fetchCurl(embedMatch[0], MOBILE_UA);
        if (embedR.status === 200 && !embedR.body.includes('Just a moment')) {
          streams.push({
            name: 'bCine | 1Embed Player',
            title, quality: 'HD', url: embedMatch[0], source: 'bcine',
            headers: { 'User-Agent': MOBILE_UA },
          });
        }
      }
    }
  } catch {}

  // 2. 2embed.cc (curl for CF bypass)
  try {
    const embedUrl = isTV
      ? `https://www.2embed.cc/embedtv/${tmdbId}&s=${s}&e=${e}`
      : `https://www.2embed.cc/embed/${tmdbId}`;
    const r = fetchCurl(embedUrl, MOBILE_UA);
    if (r.status === 200 && !r.body.includes('Just a moment')) {
      const dataSrc = r.body.match(/data-src="([^"]*)"/);
      if (dataSrc && dataSrc[1] !== 'about:blank' && dataSrc[1].startsWith('http')) {
        streams.push({
          name: '2Embed | Stream Source',
          title, quality: 'HD', url: dataSrc[1], source: '2embed',
          headers: { 'User-Agent': MOBILE_UA },
        });
      }
      streams.push({
        name: '2Embed | Player',
        title, quality: 'HD', url: embedUrl, source: '2embed',
        headers: { 'User-Agent': MOBILE_UA },
      });
    }
  } catch {}

  // 3. vidsrc.me (curl for reliability)
  try {
    const embedUrl = isTV
      ? `https://vidsrc.me/embed/tv/${tmdbId}/${s}/${e}/`
      : `https://vidsrc.me/embed/movie/${tmdbId}/`;
    const r = fetchCurl(embedUrl, MOBILE_UA);
    if (r.status === 200) {
      streams.push({
        name: 'VidSrc.me | Player',
        title, quality: 'HD', url: embedUrl, source: 'vidsrc',
        headers: { 'User-Agent': MOBILE_UA },
      });
    }
  } catch {}

  // 4. vidsrc.to (curl for CF bypass)
  try {
    const embedUrl = isTV
      ? `https://vidsrc.to/embed/tv/${tmdbId}/${s}/${e}`
      : `https://vidsrc.to/embed/movie/${tmdbId}`;
    const r = fetchCurl(embedUrl, MOBILE_UA);
    if (r.status === 200 && !r.body.includes('Just a moment')) {
      streams.push({
        name: 'VidSrc.to | Player',
        title, quality: 'HD', url: embedUrl, source: 'vidsrc',
        headers: { 'User-Agent': MOBILE_UA },
      });
    }
  } catch {}

  // 5. moviebite.org
  try {
    const embedUrl = isTV
      ? `https://moviebite.org/watch/tv/${tmdbId}/s${s}/e${e}`
      : `https://moviebite.org/watch/movie/${tmdbId}`;
    const r = await fetchText(embedUrl);
    if (r.status === 200) {
      streams.push({
        name: 'MovieBite | Player',
        title, quality: 'HD', url: embedUrl, source: 'moviebite',
        headers: {},
      });
    }
  } catch {}

  // 6. coreflix.tv
  try {
    const embedUrl = `https://coreflix.tv/${isTV ? 'tv' : 'movie'}/${tmdbId}`;
    const r = await fetchText(embedUrl);
    if (r.status === 200) {
      streams.push({
        name: 'Coreflix | Player',
        title, quality: 'HD', url: embedUrl, source: 'coreflix',
        headers: {},
      });
    }
  } catch {}

  // 7. aether.cx
  try {
    const embedUrl = isTV
      ? `https://aether.cx/media/tmdb-tv-${tmdbId}`
      : `https://aether.cx/media/tmdb-movie-${tmdbId}`;
    const r = await fetchText(embedUrl);
    if (r.status === 200) {
      streams.push({
        name: 'Aether | Player',
        title, quality: 'HD', url: embedUrl, source: 'aether',
        headers: {},
      });
    }
  } catch {}

  // 8. animex.one
  try {
    const embedUrl = `https://animex.one/watch/${tmdbId}`;
    const r = await fetchText(embedUrl);
    if (r.status === 200) {
      streams.push({
        name: 'AnimeX | Player',
        title, quality: 'HD', url: embedUrl, source: 'animex',
        headers: {},
      });
    }
  } catch {}

  return streams;
}

module.exports = { getStreams };
