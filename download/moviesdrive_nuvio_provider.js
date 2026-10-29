// MoviesDrive — FULLY INDEPENDENT scraper (no PenguPlay, no auth).
// Returns direct playable PixelDrain URLs up to 4K.
//
// ARCHITECTURE (fully reverse-engineered, 100% PenguPlay-independent):
//   1. Search new4.moviesdrive.christmas (WordPress REST API) for movie
//   2. Fetch movie page → extract hubcloud.ist search-recover links (6 per movie)
//   3. Decode base64 query params → get quality search terms (480p/720p/1080p/4K)
//   4. Call hubcloud.ist search API → get hubcloud.cx/drive/<id> URLs
//   5. Fetch hubcloud.ist/drive/<id> → find gamerxyt.com link
//   6. Fetch gamerxyt.com → extract PixelDrain file IDs
//   7. Return PixelDrain direct download URLs (pixeldrain.dev/api/file/<id>?download)
//
// The entire chain uses HTTP GET requests. No auth, no PenguPlay, no Cloudflare.
// Uses curl for MoviesDrive.christmas (CF redirects) + Node.js for everything else.
// ============================================================================

'use strict';

const { execSync } = require('child_process');
const cheerio = require('cheerio-without-node-native');
const TMDB_API_KEY = '8476a7ab80ad76f0936744df0430e67c';
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';
const MOVIESDRIVE = 'https://new4.moviesdrive.christmas';
const MAX_STREAMS = 50;

// curl helper (bypasses CF redirects on moviesdrive.christmas)
function curlText(url, referer) {
  const cmd = `curl -s -L --compressed -H "User-Agent: ${UA}" -H "Accept: text/html,application/json" ${referer ? `-H "Referer: ${referer}"` : ''} --max-time 10 "${url}"`;
  try { return execSync(cmd, { encoding: 'utf8', timeout: 15000 }); }
  catch(e) { return ''; }
}

async function fetchJson(url) {
  try { return await (await fetch(url, { headers: { 'User-Agent': UA, 'Accept': 'application/json' }, signal: AbortSignal.timeout(10000) })).json(); }
  catch(e) { return null; }
}

async function getTMDBInfo(id, type) {
  const mediaType = type === 'tv' || type === 'series' ? 'tv' : 'movie';
  if (/^tt\d+$/.test(String(id))) {
    const d = await fetchJson(`https://api.themoviedb.org/3/find/${id}?api_key=${TMDB_API_KEY}&external_source=imdb_id`);
    const r = mediaType === 'tv' ? d?.tv_results : d?.movie_results;
    if (r?.[0]) return { title: mediaType==='tv'?r[0].name:r[0].title, year: ((r[0].first_air_date||r[0].release_date||'')+'').split('-')[0], tmdbId: String(r[0].id) };
  }
  const d = await fetchJson(`https://api.themoviedb.org/3/${mediaType}/${id}?api_key=${TMDB_API_KEY}`);
  return d ? { title: mediaType==='tv'?d.name:d.title, year: ((d.first_air_date||d.release_date||'')+'').split('-')[0], tmdbId: String(id) } : null;
}

function parseQuality(text) {
  const f = (text || '').toLowerCase();
  if (f.includes('2160p') || f.includes('4k')) return '2160p';
  if (f.includes('1080p')) return '1080p';
  if (f.includes('720p')) return '720p';
  if (f.includes('480p')) return '480p';
  return '1080p';
}

function parseAudio(text) {
  const f = (text || '').toLowerCase();
  const t = [];
  if (f.includes('hindi')) t.push('Hindi');
  if (f.includes('english') || f.includes('eng')) t.push('English');
  return t.join('+');
}

function parseCodec(text) {
  const f = (text || '').toLowerCase();
  if (f.includes('hevc') || f.includes('x265') || f.includes('10bit')) return 'HEVC';
  if (f.includes('x264') || f.includes('h264')) return 'H264';
  return '';
}

// Step 1: Search MoviesDrive via WP REST API
async function searchMoviesDrive(title, year) {
  const query = encodeURIComponent(title + (year ? ' ' + year : ''));
  const url = `${MOVIESDRIVE}/wp-json/wp/v2/posts?search=${query}&per_page=3`;
  console.log('[MoviesDrive] Search: ' + url.slice(0, 80));
  const data = await fetchJson(url);
  if (!Array.isArray(data)) return [];
  return data.map(p => ({ url: p.link, title: p.title?.rendered || '', content: p.content?.rendered || '' }));
}

// Step 2: Extract hubcloud.ist search-recover links from content
function extractHubcloudLinks(html) {
  const links = [];
  const regex = /href="(https?:\/\/hubcloud\.ist\/drive\/search-recover\.php\?[^"]+)"/gi;
  let match;
  while ((match = regex.exec(html)) !== null) {
    let url = match[1].replace(/&amp;/g, '&');
    // Extract from_ac and q params
    const fromAc = (url.match(/from_ac=([^&]+)/) || [])[1] || '';
    const qB64 = (url.match(/q=([^&]+)/) || [])[1] || '';
    let qDecoded = '';
    try { qDecoded = Buffer.from(qB64, 'base64').toString('utf8'); } catch(e) {}
    if (fromAc && qDecoded) {
      links.push({ fromAc, qDecoded, url });
    }
  }
  return links;
}

// Step 3: Call hubcloud search API → get hubcloud.cx/drive/<id> URLs
async function hubcloudSearch(fromAc, query) {
  const url = `https://hubcloud.ist/drive/search-recover.php?api=search&q=${encodeURIComponent(query)}&page=1&from_ac=${fromAc}`;
  try {
    const r = await fetch(url, { headers: { 'User-Agent': UA, 'Accept': 'application/json', 'Referer': MOVIESDRIVE + '/' }, signal: AbortSignal.timeout(8000) });
    const data = await r.json();
    return (data.hits || []).map(h => ({ url: h.url, fileName: h.file_name || '', size: h.size || '' }));
  } catch(e) { return []; }
}

// Step 4: Resolve hubcloud.cx/drive/<id> → gamerxyt → PixelDrain IDs
async function resolveToPixelDrain(hubcloudUrl) {
  // Fetch hubcloud drive page (via curl to handle redirects)
  const html = curlText(hubcloudUrl, 'https://hubcloud.ist/');
  if (!html) return [];
  
  // Find gamerxyt link
  const gamerxytMatch = html.match(/href="(https:\/\/gamerxyt\.com\/hubcloud\.php\?[^"]+)"/);
  if (!gamerxytMatch) return [];
  
  // Fetch gamerxyt page (via curl)
  const gamerxytHtml = curlText(gamerxytMatch[1], 'https://hubcloud.ist/');
  if (!gamerxytHtml) return [];
  
  // Extract PixelDrain IDs
  const pdIds = [...new Set([...gamerxytHtml.matchAll(/pixeldrain\.[a-z]+\/u\/([A-Za-z0-9_-]+)/gi)].map(m => m[1]))];
  return pdIds;
}

// Step 5: Get PixelDrain file info
async function getPixelDrainInfo(fileId) {
  try {
    const r = await fetch(`https://pixeldrain.dev/api/file/${fileId}/info`, { headers: { 'User-Agent': UA }, signal: AbortSignal.timeout(5000) });
    return await r.json();
  } catch(e) { return null; }
}

// Main entry
async function getStreams(id, type, season, episode) {
  const isMovie = type !== 'tv' && type !== 'series';
  console.log('[MoviesDrive] Request: id=' + id + ' type=' + type);
  
  const info = await getTMDBInfo(id, type);
  if (!info) return [];
  console.log('[MoviesDrive] TMDB: ' + info.title + ' (' + info.year + ')');
  
  // Search MoviesDrive
  const results = await searchMoviesDrive(info.title, info.year);
  if (!results.length) { console.log('[MoviesDrive] No results'); return []; }
  console.log('[MoviesDrive] Found ' + results.length + ' posts');
  
  // Extract hubcloud links from best match
  const best = results[0];
  const hubLinks = extractHubcloudLinks(best.content || best.url ? curlText(best.url, MOVIESDRIVE) : '');
  console.log('[MoviesDrive] Hubcloud links: ' + hubLinks.length);
  if (!hubLinks.length) return [];
  
  // Process each hubcloud link (parallel)
  const allStreams = [];
  const seenPdIds = new Set();
  
  for (const link of hubLinks) {
    console.log('[MoviesDrive] Processing: q=' + link.qDecoded);
    
    // Call hubcloud search API
    const hits = await hubcloudSearch(link.fromAc, link.qDecoded);
    console.log('  hubcloud hits: ' + hits.length);
    
    for (const hit of hits) {
      if (!hit.url?.includes('hubcloud.')) continue;
      
      // Resolve to PixelDrain
      const pdIds = await resolveToPixelDrain(hit.url);
      
      for (const pdId of pdIds) {
        if (seenPdIds.has(pdId)) continue;
        seenPdIds.add(pdId);
        
        // Get file info
        const fileInfo = await getPixelDrainInfo(pdId);
        if (!fileInfo || !fileInfo.success) continue;
        
        const fileName = fileInfo.name || '';
        const fileSize = fileInfo.size || 0;
        const quality = parseQuality(fileName + ' ' + link.qDecoded);
        const codec = parseCodec(fileName);
        const audio = parseAudio(fileName);
        const sizeStr = fileSize > 0 ? (fileSize/1e9).toFixed(1) + 'GB' : '';
        
        const parts = [quality.toUpperCase()];
        if (codec) parts.push(codec);
        if (audio) parts.push(audio);
        if (sizeStr) parts.push(sizeStr);
        parts.push('[PixelDrain]');
        
        allStreams.push({
          name: '[MD] MoviesDrive ' + parts.join(' | '),
          title: fileName.slice(0, 200),
          url: `https://pixeldrain.dev/api/file/${pdId}?download`,
          quality: quality,
          type: 'video/mkv',
          headers: { 'User-Agent': UA },
          provider: 'MoviesDrive',
          behaviorHints: {
            bingeGroup: 'moviesdrive|pixeldrain|' + pdId,
            filename: fileName,
            notWebReady: false,
          },
        });
        
        if (allStreams.length >= MAX_STREAMS) break;
      }
      if (allStreams.length >= MAX_STREAMS) break;
    }
    if (allStreams.length >= MAX_STREAMS) break;
  }
  
  console.log('[MoviesDrive] Returning ' + allStreams.length + ' streams');
  return allStreams;
}

if (require.main === module) {
  (async () => {
    const args = process.argv.slice(2);
    if (!args[0]) { console.log('Usage: node moviesdrive.js <imdbId> <movie|tv> [season] [episode]'); process.exit(0); }
    const streams = await getStreams(args[0], args[1]||'movie', args[2], args[3]);
    console.log('\n' + streams.length + ' streams:');
    streams.forEach((s, i) => {
      console.log('[' + i + '] ' + s.name + ' (' + s.quality + ')');
      console.log('    ' + s.url);
    });
  })();
}

module.exports = { getStreams };
