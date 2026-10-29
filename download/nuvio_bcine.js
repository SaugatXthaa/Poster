'use strict';
const { execSync } = require('child_process');
const TMDB_API_KEY = '8476a7ab80ad76f0936744df0430e67c';
const UA = 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';
const MOBILE_UA = 'Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36';

function fetchCurl(url, ua = MOBILE_UA) {
  try {
    return execSync(`curl -s -L --max-time 15 -A "${ua}" "${url}"`, { timeout: 17000, encoding: 'utf-8', maxBuffer: 5*1024*1024 });
  } catch { return ''; }
}

async function getStreams(tmdbId, type, season, episode) {
  const isTV = type === 'tv' || type === 'series';
  const streams = [];
  try {
    const html = fetchCurl(`https://bcine.ru/${isTV ? 'tv' : 'movie'}/${tmdbId}`, UA);
    const embedMatch = html.match(/https:\/\/1embed\.cc\/download\/[^"'\s]+/);
    if (embedMatch) {
      const titleMatch = html.match(/<title>([^<]*)<\/title>/);
      const title = titleMatch ? titleMatch[1].replace(' - bCine', '') : `TMDB ${tmdbId}`;
      streams.push({
        name: 'bCine | 1Embed Player', title, quality: 'HD',
        url: embedMatch[0], source: 'bcine',
        headers: { 'User-Agent': MOBILE_UA },
      });
    }
  } catch {}
  return streams;
}
module.exports = { getStreams };
