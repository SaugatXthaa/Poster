'use strict';

const TMDB_API_KEY = '8476a7ab80ad76f0936744df0430e67c';
const HDHUB_BASE = 'https://hdhub.thevolecitor.qzz.io';
const UA = 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';
const SITE = 'https://vipstreammovies.com';

async function getTmdbInfo(tmdbId, type) {
  try {
    const r = await fetch(`https://api.themoviedb.org/3/${type}/${tmdbId}?api_key=${TMDB_API_KEY}&append_to_response=external_ids`);
    const d = await r.json();
    return { title: type === 'tv' ? d.name : d.title, imdbId: d.imdb_id || d.external_ids?.imdb_id };
  } catch { return { title: '', imdbId: null }; }
}

async function searchAndGetTmdbId(query) {
  try {
    const r = await fetch(`${SITE}/search?q=${encodeURIComponent(query)}`, { headers: { 'User-Agent': UA } });
    const html = await r.text();
    // Find movie page URLs
    const match = html.match(/href="(\/movie\/[^"]+)"/);
    if (match) {
      // Fetch the movie page to get the TMDB ID from the embed URL
      const r2 = await fetch(`${SITE}${match[1]}`, { headers: { 'User-Agent': UA } });
      const html2 = await r2.text();
      const embedMatch = html2.match(/\/embed\/movie\/(\d+)/);
      if (embedMatch) return parseInt(embedMatch[1]);
    }
  } catch {}
  return null;
}

async function fetchHdHub(imdbId, isTV, season, episode) {
  try {
    const url = isTV
      ? `${HDHUB_BASE}/stream/tv/${imdbId}:${season || 1}:${episode || 1}.json`
      : `${HDHUB_BASE}/stream/movie/${imdbId}.json`;
    const r = await fetch(url, { headers: { 'User-Agent': UA }, signal: AbortSignal.timeout(12000) });
    if (!r.ok) return [];
    const data = await r.json();
    return (data.streams || []).filter(s =>
      s.url && s.url.startsWith('http') &&
      !s.name.toLowerCase().includes('error') &&
      !s.name.toLowerCase().includes('donation') &&
      !s.name.toLowerCase().includes('discord')
    );
  } catch { return []; }
}

function parseStreamInfo(stream) {
  const text = (stream.name + ' ' + stream.description || '').replace(/\n/g, ' ');
  let quality = 'unknown';
  const qMatch = text.match(/(2160p|1080p|720p|480p|4K|UHD)/i);
  if (qMatch) quality = qMatch[1].toLowerCase();
  let size = '';
  const sMatch = text.match(/(\d+(?:\.\d+)?\s*(?:GB|MB))/i);
  if (sMatch) size = sMatch[1].toUpperCase();
  return { quality, size };
}

async function getStreams(tmdbId, type, season, episode) {
  const isTV = type === 'tv' || type === 'series';
  const info = await getTmdbInfo(tmdbId, isTV ? 'tv' : 'movie');
  if (!info.imdbId) return [];
  
  const title = info.title || `TMDB ${tmdbId}`;
  const streams = [];
  
  // Fetch from HdHub
  const hdhubStreams = await fetchHdHub(info.imdbId, isTV, season, episode);
  for (const s of hdhubStreams) {
    const info2 = parseStreamInfo(s);
    streams.push({
      name: `VipStream | HdHub | ${info2.quality} ${info2.size ? '[' + info2.size + ']' : ''}`.trim(),
      title: title + (isTV && season ? ` S${season}E${episode || 1}` : ''),
      size: info2.size,
      description: (s.description || s.name || '').replace(/\n/g, ' ').substring(0, 100),
      url: s.url,
      quality: info2.quality,
      source: 'vipstreammovies',
      headers: {},
    });
  }
  
  // Also add the vipstreammovies embed URL (works in browser)
  streams.push({
    name: 'VipStream | Player',
    title: title,
    quality: 'HLS',
    url: `${SITE}/embed/movie/${tmdbId}`,
    source: 'vipstreammovies',
    headers: {},
  });
  
  // Sort by URL reliability (PixelDrain first, then R2, then others)
  const urlPriority = (url) => {
    if (url.includes('pixeldrain')) return 0;
    if (url.includes('r2.dev') || url.includes('r2.cloudflarestorage')) return 1;
    if (url.includes('.m3u8')) return 2;
    if (url.includes('hubcloud')) return 3;
    return 4;
  };
  streams.sort((a, b) => urlPriority(a.url) - urlPriority(b.url));
  
  return streams;
}

module.exports = { getStreams };
