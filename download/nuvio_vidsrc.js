'use strict';
const { execSync } = require('child_process');
const MOBILE_UA = 'Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36';

function fetchCurl(url) {
  try {
    return execSync(`curl -s -L --max-time 15 -A "" "${url}"`, { timeout: 17000, encoding: 'utf-8', maxBuffer: 5*1024*1024 });
  } catch { return ''; }
}

async function getStreams(tmdbId, type, season, episode) {
  const isTV = type === 'tv' || type === 'series';
  const s = season || 1, e = episode || 1;
  const streams = [];
  try {
    const embedUrl = isTV
      ? `https://www.2embed.cc/embedtv/${tmdbId}&s=${s}&e=${e}`
      : `https://www.2embed.cc/embed/${tmdbId}`;
    const html = fetchCurl(embedUrl);
    if (html && !html.includes('Just a moment')) {
      const dataSrc = html.match(/data-src="([^"]*)"/);
      if (dataSrc && dataSrc[1] !== 'about:blank' && dataSrc[1].startsWith('http')) {
        streams.push({ name: '2Embed | Stream', title: 'TMDB ' + tmdbId, quality: 'HD', url: dataSrc[1], source: '2embed', headers: { 'User-Agent': MOBILE_UA } });
      }
      streams.push({ name: '2Embed | Player', title: 'TMDB ' + tmdbId, quality: 'HD', url: embedUrl, source: '2embed', headers: { 'User-Agent': MOBILE_UA } });
    }
  } catch {}
  try {
    const vsUrl = isTV ? `https://vidsrc.me/embed/tv/${tmdbId}/${s}/${e}/` : `https://vidsrc.me/embed/movie/${tmdbId}/`;
    const vsHtml = fetchCurl(vsUrl);
    if (vsHtml) {
      streams.push({ name: 'VidSrc.me | Player', title: 'TMDB ' + tmdbId, quality: 'HD', url: vsUrl, source: 'vidsrc', headers: { 'User-Agent': MOBILE_UA } });
    }
  } catch {}
  return streams;
}
module.exports = { getStreams };
