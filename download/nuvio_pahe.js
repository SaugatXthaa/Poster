'use strict';

const { execSync } = require('child_process');
const TMDB_API_KEY = '8476a7ab80ad76f0936744df0430e67c';
const UA = 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';

function fetchCurl(url, headers = {}, timeout = 15) {
  try {
    let cmd = `curl -s -L --max-time ${timeout} -A "${UA}"`;
    for (const [k, v] of Object.entries(headers)) cmd += ` -H "${k}: ${v}"`;
    cmd += ` "${url}"`;
    return execSync(cmd, { timeout: (timeout + 2) * 1000, encoding: 'utf-8', maxBuffer: 5*1024*1024 });
  } catch { return ''; }
}

async function getTmdbInfo(tmdbId, type) {
  try {
    const r = await fetch(`https://api.themoviedb.org/3/${type}/${tmdbId}?api_key=${TMDB_API_KEY}`);
    const d = await r.json();
    return { title: type === 'tv' ? d.name : d.title, year: (d.release_date || d.first_air_date || '').slice(0, 4) };
  } catch { return { title: '', year: '' }; }
}

function buildSlug(title, year) {
  let slug = title.toLowerCase().replace(/[^a-z0-9\s-]/g, '').replace(/\s+/g, '-').replace(/-+/g, '-');
  if (year) slug += '-' + year;
  return slug;
}

function extractDownloadLinks(html) {
  const streams = [];
  const boxStart = html.indexOf('class="box download');
  if (boxStart < 0) return streams;
  const boxHtml = html.substring(boxStart, boxStart + 30000);
  const lines = boxHtml.split(/<br\s*\/?>/i);
  
  let currentQuality = '';
  let currentSize = '';
  
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    const cleanText = line.split('>').pop().replace(/<[^>]+>/g, '').trim();
    
    // Split by | to get quality and size
    const pipeIdx = cleanText.indexOf('|');
    let qText = cleanText, qSize = '';
    if (pipeIdx > 0) {
      qText = cleanText.substring(0, pipeIdx).trim();
      qSize = cleanText.substring(pipeIdx + 1).trim();
    }
    
    const qMatch = qText.match(/^(\d{3,4}p.*)/);
    if (qMatch && qText.match(/^\d{3,4}p/)) {
      currentQuality = qMatch[1].trim();
      currentSize = qSize ? (qSize.match(/([0-9.]+\s*(?:GB|MB|gb|mb))/i) || [''])[0].toUpperCase() : '';
      continue;
    }
    
    if (currentQuality) {
      const bRegex = /<a[^>]*href="(https:\/\/teknoasian\.com\/[^"]+)"[^>]*>\s*(GD|MG|1D|PD|1F|GDF|SL)\s*<\/a>/gi;
      let bMatch;
      while ((bMatch = bRegex.exec(line)) !== null) {
        const hostNames = { GD: 'GDrive', MG: 'MegaGo', '1D': '1Fichier', PD: 'PixelDrain', '1F': '1Fichier', GDF: 'GDFlix', SL: 'SolidFiles' };
        streams.push({
          name: `Pahe | ${currentQuality} | ${hostNames[bMatch[2]] || bMatch[2]} ${currentSize ? '[' + currentSize + ']' : ''}`,
          qualityText: currentQuality,
          quality: currentQuality.match(/\d+/)[0] + 'p',
          size: currentSize,
          teknoUrl: bMatch[1],
          host: hostNames[bMatch[2]] || bMatch[2],
        });
      }
    }
  }
  return streams;
}

async function getStreams(tmdbId, type, season, episode) {
  const isTV = type === 'tv' || type === 'series';
  const mediaType = isTV ? 'tv' : 'movie';
  const info = await getTmdbInfo(tmdbId, mediaType);
  if (!info.title) return [];
  
  const slug = buildSlug(info.title, info.year);
  const html = fetchCurl(`https://pahe.ink/${slug}/`);
  
  let links = [];
  if (html && html.includes('box download')) {
    links = extractDownloadLinks(html);
  }
  
  if (links.length === 0 && info.year) {
    const html2 = fetchCurl(`https://pahe.ink/${buildSlug(info.title, '')}/`);
    if (html2 && html2.includes('box download')) links = extractDownloadLinks(html2);
  }
  
  if (links.length === 0) {
    try {
      const searchHtml = fetchCurl(`https://pahe.ink/wp-json/wp/v2/posts?search=${encodeURIComponent(info.title)}&per_page=5`);
      const posts = JSON.parse(searchHtml);
      for (const post of posts) {
        if (post.title.rendered.toLowerCase().includes(info.title.toLowerCase())) {
          const postHtml = fetchCurl(post.link);
          if (postHtml && postHtml.includes('box download')) {
            links = extractDownloadLinks(postHtml);
            if (links.length > 0) break;
          }
        }
      }
    } catch {}
  }
  
  if (links.length === 0) return [];
  
  // Return streams with teknoasian URLs as externalUrl
  // The Nuvio/Stremio app will open these in a WebView/browser
  // which will execute the JS redirect chain to get the file
  const result = [];
  const seen = new Set();
  
  for (const link of links) {
    const key = link.quality + '-' + link.host;
    if (seen.has(key)) continue;
    seen.add(key);
    
    result.push({
      name: link.name,
      title: info.title + (isTV && season ? ` S${season}E${episode || 1}` : ''),
      size: link.size,
      description: `${link.qualityText} ${link.size} | ${link.host}`,
      url: link.teknoUrl,
      externalUrl: link.teknoUrl,  // Mark as external (browser-openable)
      quality: link.quality,
      source: 'pahe',
      headers: { 'Referer': 'https://pahe.ink/' },
    });
    
    if (result.length >= 8) break;
  }
  
  return result;
}

module.exports = { getStreams };
