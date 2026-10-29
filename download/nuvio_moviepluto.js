'use strict';

const TMDB_API_KEY = '8476a7ab80ad76f0936744df0430e67c';
const UA = 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';

// Stream backends used by moviepluto.cc
const HDHUB_BASE = 'https://hdhub.thevolecitor.qzz.io';
const YUKI_BASE = 'https://stremio.yukistreams.xyz/p.2jVe6a-WVvyK4J0a';
const STREAMVIX_BASE = 'https://streamvix.hayd.uk';

async function getTmdbInfo(tmdbId, type) {
  try {
    const r = await fetch(`https://api.themoviedb.org/3/${type}/${tmdbId}?api_key=${TMDB_API_KEY}&append_to_response=external_ids`);
    const d = await r.json();
    return {
      title: type === 'tv' ? d.name : d.title,
      year: (d.release_date || d.first_air_date || '').slice(0, 4),
      imdbId: d.imdb_id || (d.external_ids && d.external_ids.imdb_id),
    };
  } catch { return { title: '', year: '', imdbId: null }; }
}

/**
 * Fetch streams from a Stremio-compatible backend.
 * URL format: {base}/stream/{type}/{imdb}.json or {base}/stream/tv/{imdb}:{s}:{e}.json
 */
async function fetchFromBackend(base, imdbId, isTV, season, episode) {
  try {
    const url = isTV
      ? `${base}/stream/tv/${imdbId}:${season || 1}:${episode || 1}.json`
      : `${base}/stream/movie/${imdbId}.json`;
    
    const r = await fetch(url, { headers: { 'User-Agent': UA }, signal: AbortSignal.timeout(12000) });
    if (!r.ok) return [];
    const data = await r.json();
    
    // Filter to only real stream URLs (skip donation/discord/error entries)
    return (data.streams || []).filter(s =>
      s.url && s.url.startsWith('http') &&
      !s.name.toLowerCase().includes('error') &&
      !s.name.toLowerCase().includes('info') &&
      !s.name.toLowerCase().includes('donation') &&
      !s.name.toLowerCase().includes('discord') &&
      !s.name.toLowerCase().includes('no streams')
    );
  } catch { return []; }
}

/**
 * Parse stream name to extract quality and size.
 */
function parseStreamInfo(stream) {
  const name = (stream.name || '').replace(/\n/g, ' ');
  const desc = (stream.description || '').replace(/\n/g, ' ');
  const text = name + ' ' + desc;
  
  // Extract quality
  let quality = 'unknown';
  const qMatch = text.match(/(2160p|1080p|720p|480p|4K|UHD)/i);
  if (qMatch) quality = qMatch[1].toLowerCase();
  
  // Extract size
  let size = '';
  const sMatch = text.match(/(\d+(?:\.\d+)?\s*(?:GB|MB|gb|mb))/i);
  if (sMatch) size = sMatch[1].toUpperCase();
  
  // Extract codec
  let codec = '';
  const cMatch = text.match(/(x264|x265|HEVC|H\.264|H\.265|AVC)/i);
  if (cMatch) codec = cMatch[1].toUpperCase();
  
  // Check for HDR
  let hdr = '';
  if (/HDR10\+|HDR10|HDR|DV|Dolby/i.test(text)) {
    hdr = text.match(/(HDR10\+|HDR10|HDR|DV|Dolby Vision)/i)?.[1] || '';
  }
  
  return { quality, size, codec, hdr };
}

async function getStreams(tmdbId, type, season, episode) {
  const isTV = type === 'tv' || type === 'series';
  const mediaType = isTV ? 'tv' : 'movie';
  const info = await getTmdbInfo(tmdbId, mediaType);
  
  if (!info.imdbId) {
    console.error('[MoviePluto] No IMDB ID found');
    return [];
  }
  
  const title = info.title || `TMDB ${tmdbId}`;
  const streams = [];
  
  // 1. Fetch from HdHub (primary - returns direct file URLs)
  console.error('[MoviePluto] Fetching from HdHub...');
  const hdhubStreams = await fetchFromBackend(HDHUB_BASE, info.imdbId, isTV, season, episode);
  for (const s of hdhubStreams) {
    const info2 = parseStreamInfo(s);
    const displayQuality = [info2.quality, info2.hdr, info2.codec].filter(Boolean).join(' ');
    streams.push({
      name: `MoviePluto | HdHub | ${displayQuality} ${info2.size ? '[' + info2.size + ']' : ''}`.trim(),
      title: title + (isTV && season ? ` S${season}E${episode || 1}` : ''),
      size: info2.size,
      description: (s.description || s.name || '').replace(/\\n/g, ' ').substring(0, 100),
      url: s.url,
      quality: info2.quality,
      source: 'moviepluto',
      headers: {},
    });
  }
  
  // 2. Fetch from YukiStreams (HLS streams)
  console.error('[MoviePluto] Fetching from YukiStreams...');
  const yukiStreams = await fetchFromBackend(YUKI_BASE, info.imdbId, isTV, season, episode);
  for (const s of yukiStreams) {
    streams.push({
      name: `MoviePluto | Yuki | ${s.name?.replace(/\n/g, ' ').substring(0, 30) || 'Stream'}`,
      title: title,
      url: s.url,
      quality: 'HLS',
      source: 'moviepluto',
      headers: {},
    });
  }
  
  // 3. Fetch from StreamVix (MP4 streams)
  console.error('[MoviePluto] Fetching from StreamVix...');
  const vixStreams = await fetchFromBackend(STREAMVIX_BASE, info.imdbId, isTV, season, episode);
  for (const s of vixStreams) {
    const info2 = parseStreamInfo(s);
    streams.push({
      name: `MoviePluto | StreamVix | ${info2.quality}`,
      title: title,
      url: s.url,
      quality: info2.quality || 'MP4',
      source: 'moviepluto',
      headers: {},
    });
  }
  
  // Sort by quality (4K first, then 1080p, etc.)
  const qOrder = { '2160p': 5, '4k': 5, '1080p': 4, '720p': 3, '480p': 2, 'hls': 1, 'mp4': 1, 'unknown': 0 };
  streams.sort((a, b) => (qOrder[b.quality] || 0) - (qOrder[a.quality] || 0));
  
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
