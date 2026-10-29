/**
 * Stream Proxy Server — resolves direct playable streams from protected sites
 * ===========================================================================
 * Deploy on a VPS with a residential IP (not a datacenter IP).
 * Cloudflare challenges residential IPs less aggressively, allowing the
 * server-side resolution chain to work.
 *
 * Sites supported:
 *   1. ToonFlix (toonflix.in) — anime/cartoon download portal
 *      Chain: toonflix.in → drive.toonflix.in → go.php → HubCloud/workers.dev → .mkv
 *   2. Re:ANIME (reanime.to) — anime streaming (HLS)
 *      Chain: reanime.to/__data.json → flixcloud.cc → enc-dec.app → .m3u8
 *   3. ToonWorld4All (toonworld4all.me) — anime/cartoon download
 *      Chain: toonworld4all.me → archive.toonworld4all.me → /redirect → HubCloud → .mkv
 *
 * Endpoints:
 *   GET /resolve/toonflix?tmdb={id}&type={movie|tv}&season={n}&episode={n}
 *   GET /resolve/reanime?slug={slug}&episode={n}
 *   GET /resolve/toonworld4all?tmdb={id}&type={movie|tv}&episode={n}
 *   GET /health
 *
 * Deploy:
 *   npm install express cors
 *   node proxy-server.js
 *   # Or with PMM: pm2 start proxy-server.js --name stream-proxy
 */
'use strict';

const express = require('express');
const cors = require('cors');
const { execFile } = require('child_process');
const { promisify } = require('util');
const execFileAsync = promisify(execFile);

const app = express();
const PORT = process.env.PORT || 7860;
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';
const TMDB_API_KEY = '439c478a771f35c05022f9feabcca01c';

// ─── HTTP helpers (all requests go through curl for Cloudflare compatibility) ─
async function httpGet(url, opts = {}) {
  const args = ['-sS', '-L', '--compressed', '--max-time', '15', '-A', UA];
  if (opts.referer) args.push('-H', `Referer: ${opts.referer}`);
  if (opts.cookie) args.push('-b', opts.cookie);
  if (opts.headers) for (const [k, v] of Object.entries(opts.headers)) args.push('-H', `${k}: ${v}`);
  if (opts.cookieJar) { args.push('-c', opts.cookieJar, '-b', opts.cookieJar); }
  if (opts.method === 'HEAD') args.unshift('-I');
  else args.push('-o', '-');
  args.push(url);
  try {
    const { stdout, stderr } = await execFileAsync('curl', args, { encoding: 'utf-8', maxBuffer: 30 * 1024 * 1024, timeout: 20000 });
    return stdout;
  } catch (e) {
    console.error(`[http] ${url} → ${e.message.slice(0, 80)}`);
    return '';
  }
}

async function httpGetRedirect(url, opts = {}) {
  const args = ['-sS', '-L', '--compressed', '--max-time', '12', '-A', UA, '-o', '/dev/null', '-w', '%{url_effective}'];
  if (opts.referer) args.push('-H', `Referer: ${opts.referer}`);
  if (opts.cookie) args.push('-b', opts.cookie);
  args.push(url);
  try {
    const { stdout } = await execFileAsync('curl', args, { encoding: 'utf-8', timeout: 15000 });
    return stdout.trim();
  } catch (e) { return null; }
}

async function httpGetJson(url, opts = {}) {
  const body = await httpGet(url, { ...opts, headers: { ...(opts.headers || {}), Accept: 'application/json' } });
  if (!body) return null;
  try { return JSON.parse(body); } catch (e) { return null; }
}

// ─── TMDB lookup ──────────────────────────────────────────────────────────
async function getTmdbDetails(tmdbId, type) {
  const url = `https://api.themoviedb.org/3/${type === 'tv' ? 'tv' : 'movie'}/${tmdbId}?api_key=${TMDB_API_KEY}&language=en-US`;
  const data = await httpGetJson(url);
  if (!data) return null;
  return {
    title: type === 'tv' ? data.name : data.title,
    year: ((type === 'tv' ? data.first_air_date : data.release_date) || '').substring(0, 4),
    poster: data.poster_path ? `https://image.tmdb.org/t/p/w500${data.poster_path}` : null,
    genres: (data.genres || []).map(g => g.name),
    overview: data.overview || '',
  };
}

// ═══════════════════════════════════════════════════════════════════════════
//  TOONFLIX RESOLVER
// ═══════════════════════════════════════════════════════════════════════════
async function resolveToonFlix(tmdbId, type, season, episode) {
  const tmdb = await getTmdbDetails(tmdbId, type);
  if (!tmdb) return [];

  // Step 1: Search toonflix.in
  const searchHtml = await httpGet(`https://toonflix.in/?s=${encodeURIComponent(tmdb.title)}`);
  if (!searchHtml) return [];

  // Parse post links
  const postLinks = [];
  const linkRe = /href="(https:\/\/toonflix\.in\/[^"]+\/)"/g;
  let m;
  while ((m = linkRe.exec(searchHtml)) !== null) {
    if (!m[1].includes('category') && !m[1].includes('tag') && !m[1].includes('page') &&
        !m[1].includes('feed') && !m[1].includes('wp-') && !m[1].includes('all-anime') &&
        !m[1].includes('about') && !m[1].includes('disclaimer') && !m[1].includes('contact')) {
      postLinks.push(m[1]);
    }
  }
  if (!postLinks.length) return [];

  // Find best match
  const bestPost = postLinks.find(l => l.includes(tmdb.title.toLowerCase().replace(/\s+/g, '-'))) || postLinks[0];

  // Step 2: Fetch post page → extract drive.toonflix.in link
  const postHtml = await httpGet(bestPost, { referer: 'https://toonflix.in/' });
  if (!postHtml) return [];

  const driveLinkMatch = postHtml.match(/href="(https:\/\/drive\.toonflix\.in\/index\.php\?[^"]+)"/);
  if (!driveLinkMatch) return [];
  const driveUrl = driveLinkMatch[1].replace(/&#038;/g, '&');

  // Step 3: Visit drive.toonflix.in → get quality cards + go.php links
  const cookieJar = `/tmp/tf_${Date.now()}.cookies`;
  const driveHtml = await httpGet(driveUrl, {
    referer: 'https://toonflix.in/',
    cookieJar,
    cookie: 'tf_sys_pref=12h; toon_short_count=3',
  });
  if (!driveHtml || driveHtml.includes('Access Denied')) return [];

  // Parse quality cards
  const qualities = [];
  const qRe = /<span class="quality-text">([^<]+)<\/span>\s*<span class="codec-tag">([^<]*)<\/span>[\s\S]*?<span class="file-size">([^<]+)<\/span>/g;
  while ((m = qRe.exec(driveHtml)) !== null) {
    qualities.push({ quality: m[1].trim(), codec: m[2].trim(), size: m[3].trim() });
  }

  // Parse server links (go.php)
  const serverRe = /server-name"><i[^>]*><\/i>\s*([^<]+)<\/span>\s*<a[^>]*onclick="handleLinkClick\('([^']+)'/g;
  const servers = [];
  while ((m = serverRe.exec(driveHtml)) !== null) {
    servers.push({ name: m[1].trim(), goLink: m[2] });
  }

  if (!servers.length) return [];

  // Step 4: Follow go.php links from same session → get file host URL
  const streams = [];
  let qIdx = 0;
  const serversPerQuality = 4; // 4 servers per quality card

  for (let i = 0; i < servers.length; i++) {
    const server = servers[i];
    const qIdxForServer = Math.floor(i / serversPerQuality);
    const q = qualities[qIdxForServer] || qualities[0] || { quality: 'Unknown', codec: '', size: '' };

    // Follow go.php
    const goUrl = `https://drive.toonflix.in/${server.goLink}&sys=12h`;
    const resolvedUrl = await httpGetRedirect(goUrl, {
      referer: 'https://drive.toonflix.in/',
      cookieJar,
      cookie: 'tf_sys_pref=12h; toon_short_count=3',
    });

    if (resolvedUrl && resolvedUrl !== goUrl && !resolvedUrl.includes('Access Denied')) {
      // Check if it's a direct file URL or a file host page
      const isDirect = /\.(mkv|mp4|webm|m3u8)/i.test(resolvedUrl);
      const isWorkers = /workers\.dev/i.test(resolvedUrl);
      const isGDrive = /drive\.google\.com|googleusercontent/i.test(resolvedUrl);
      const isHubCloud = /hubcloud/i.test(resolvedUrl);
      const isFilePress = /filepress/i.test(resolvedUrl);
      const isGDFlix = /gdflix/i.test(resolvedUrl);

      // For HubCloud: resolve further to workers.dev
      let finalUrl = resolvedUrl;
      if (isHubCloud) {
        finalUrl = await resolveHubCloud(resolvedUrl);
      }

      if (finalUrl && (isDirect || isWorkers || isGDrive || finalUrl !== resolvedUrl)) {
        streams.push({
          name: `ToonFlix\n${q.quality}`,
          title: `${tmdb.title} ${episode ? 'E' + episode : ''} · ${q.quality} · ${q.codec} · ${q.size} · ${server.name}`,
          url: finalUrl,
          quality: q.quality.toLowerCase(),
          behaviorHints: {
            notWebReady: false,
            headers: { 'Referer': 'https://drive.toonflix.in/', 'User-Agent': UA },
            proxyHeaders: { request: { 'Referer': 'https://drive.toonflix.in/', 'User-Agent': UA } },
          },
          poster: tmdb.poster,
          meta: {
            provider: 'ToonFlix',
            source: server.name,
            type: 'mp4',
            quality: q.quality.toLowerCase(),
            codec: q.codec,
            size: q.size,
            title: tmdb.title,
            episodeNumber: episode,
            description: tmdb.overview,
            poster: tmdb.poster,
            genres: tmdb.genres,
            directStream: true,
            mediaType: type,
          },
        });
      }
    }
  }

  // Sort by quality
  const order = { '2160p': 5, '1080p': 4, '1080p hq': 4, '720p': 3, '480p': 2 };
  streams.sort((a, b) => (order[b.quality] || 0) - (order[a.quality] || 0));
  return streams;
}

// ─── HubCloud resolver ────────────────────────────────────────────────────
async function resolveHubCloud(hubcloudUrl) {
  const html = await httpGet(hubcloudUrl, { referer: 'https://drive.toonflix.in/' });
  if (!html) return null;

  // Follow window.location.replace redirect
  const redirectMatch = html.match(/window\.location\.replace\("([^"]+)"\)/);
  let finalUrl = hubcloudUrl;
  let finalHtml = html;
  if (redirectMatch && redirectMatch[1]) {
    const base = new URL(hubcloudUrl).origin;
    finalUrl = new URL(redirectMatch[1], base).href;
    finalHtml = await httpGet(finalUrl, { referer: hubcloudUrl });
    if (!finalHtml) return null;
  }

  // Try Resume Cloud → workers.dev
  const rcMatch = finalHtml.match(/href="([^"]*)"[^>]*>\s*(?:Resume Cloud|Cloud Resume Download)/i);
  if (rcMatch) {
    let rcUrl = rcMatch[1];
    if (rcUrl.startsWith('/')) rcUrl = new URL(rcUrl, new URL(finalUrl).origin).href;
    const rcHtml = await httpGet(rcUrl, { referer: finalUrl });
    if (rcHtml) {
      const workerLink = rcHtml.match(/href="(https?:\/\/[a-z0-9-]+\.workers\.dev[^"]+)"/) ||
                         rcHtml.match(/class="btn-success"[^>]*href="([^"]+)"/);
      if (workerLink) return workerLink[1].replace(/ /g, '%20');
    }
  }

  // Try Instant Download
  const idMatch = finalHtml.match(/href="([^"]*)"[^>]*>\s*Instant Download/i);
  if (idMatch) {
    let idUrl = idMatch[1];
    if (idUrl.startsWith('/')) idUrl = new URL(idUrl, new URL(finalUrl).origin).href;
    try {
      const u = new URL(idUrl);
      const embedded = u.searchParams.get('url');
      if (embedded && /workers\.dev|\.r2\.dev|googlevideo|googleusercontent|\.(mp4|mkv)/i.test(embedded)) {
        return embedded.replace(/ /g, '%20');
      }
    } catch (e) {}
    if (/cdn\.video-leech\.pro|workers\.dev|\.r2\.dev/i.test(idUrl)) return idUrl.replace(/ /g, '%20');
  }

  // Scan for direct links
  const direct = finalHtml.match(/href="(https?:\/\/[a-z0-9-]+\.workers\.dev[^"]+)"/) ||
                 finalHtml.match(/href="(https?:\/\/[^"]*googleusercontent[^"]*)"/);
  if (direct) return direct[1].replace(/ /g, '%20');

  return null;
}

// ═══════════════════════════════════════════════════════════════════════════
//  RE:ANIME RESOLVER
// ═══════════════════════════════════════════════════════════════════════════
async function resolveReAnime(slug, episode) {
  // Step 1: Fetch anime __data.json
  const data = await httpGetJson(`https://reanime.to/watch/${slug}/__data.json`);
  if (!data || !data.nodes) return [];

  // Find the node with anime data
  for (const node of data.nodes) {
    if (!node || !node.data || !node.data[0]) continue;
    const main = node.data[0];
    if (!main.episodeSources && !main.anime) continue;

    const resolveRef = (val) => typeof val === 'number' && val < node.data.length ? node.data[val] : val;

    // Get episode list
    const episodesRef = resolveRef(main.episodes) || [];
    const episodes = [];
    for (const epRef of episodesRef) {
      const ep = typeof epRef === 'number' ? resolveRef(epRef) : epRef;
      if (ep && ep.episodeId) {
        episodes.push({
          episodeId: typeof ep.episodeId === 'string' ? ep.episodeId : resolveRef(ep.episodeId),
          number: typeof ep.episode_number === 'number' ? ep.episode_number : 1,
          subbed: ep.subbed,
          dubbed: ep.dubbed,
        });
      }
    }

    // Get anime metadata
    const anime = resolveRef(main.anime);
    const titleObj = typeof anime?.title === 'number' ? resolveRef(anime.title) : anime?.title;
    const coverObj = typeof anime?.cover_image === 'number' ? resolveRef(anime.cover_image) : anime?.cover_image;

    const title = titleObj ? (resolveRef(titleObj.english) || resolveRef(titleObj.romaji)) : slug;
    const poster = coverObj ? (resolveRef(coverObj.extra_large) || resolveRef(coverObj.large)) : null;

    // Find the target episode
    const epNum = episode ? parseInt(episode) : 1;
    const targetEp = episodes.find(e => e.number === epNum) || episodes[0];
    if (!targetEp) continue;

    // Step 2: Get the flixcloud embed URL from Re:ANIME's stream API
    // The API at /api/v1/stream/{slug}/{episodeId} needs Cloudflare token.
    // Instead, we use the enc-dec.app API directly.
    // First, we need the flixcloud video ID — let's try fetching the episode page
    const epPageUrl = `https://reanime.to/watch/${slug}?ep=${targetEp.number}`;
    const epHtml = await httpGet(epPageUrl);

    // Look for flixcloud embed URL in the page
    const flixMatch = epHtml?.match(/https?:\/\/(?:fetch\d+\.flixcloud\.cc\/_v7\/[a-f0-9-]+\/master\.m3u8\?token=[^"'\s]+)/i);
    if (flixMatch) {
      // Direct flixcloud HLS — return it
      return [{
        name: `Re:ANIME\n1080p Sub`,
        title: `${title} - Episode ${targetEp.number} [Sub]`,
        url: flixMatch[0],
        quality: '1080p',
        behaviorHints: { notWebReady: false, headers: { 'Referer': 'https://flixcloud.cc/', 'User-Agent': UA } },
        poster,
        meta: { provider: 'Re:ANIME', source: 'flixcloud', type: 'hls', quality: '1080p', title, poster, subbed: true, directStream: true, mediaType: 'series' },
      }];
    }

    // Try the enc-dec.app flow: fetch flixcloud embed page
    // The flixcloud embed URL format: https://flixcloud.cc/e/{videoId}?v=2
    // We need to find the videoId — it's in the episodeSources data
    const epSrcRef = resolveRef(main.episodeSources);
    if (epSrcRef && typeof epSrcRef === 'object') {
      // Try to get the flixcloud video ID from the data
      const animeId = resolveRef(anime?.anime_id);
      // Try the enc-dec.app flow with the anime slug as videoId
      const flixEmbedUrl = `https://flixcloud.cc/e/${slug}?v=2`;
      const flixHtml = await httpGet(flixEmbedUrl, { referer: 'https://reanime.to/' });
      if (flixHtml) {
        // Extract SvelteKit data from the page
        const dataMatch = flixHtml.match(/type:\s*"data",\s*data:\s*(\{.*?\})\s*,\s*uses:/);
        if (dataMatch) {
          // Try to parse the data (might need json5)
          let flixData;
          try { flixData = JSON.parse(dataMatch[1].replace(/'/g, '"')); } catch (e) {
            // Try simple eval
            try { flixData = eval('(' + dataMatch[1] + ')'); } catch (e2) { continue; }
          }

          if (flixData) {
            // Step 3: Resolve token via enc-dec.app
            const tokenResp = await httpGetJson('https://enc-dec.app/api/dec-flixcloud?type=token', {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
            });

            // Try POST via curl
            const tokenBody = JSON.stringify({ data: flixData });
            const tokenResult = await httpPostJson('https://enc-dec.app/api/dec-flixcloud?type=token', tokenBody);
            if (tokenResult && tokenResult.result && tokenResult.result.token) {
              // Step 4: Fetch encrypted stream from flixcloud
              const streamUrl = `https://flixcloud.cc/api/m3u8/${tokenResult.result.token}`;
              const streamResp = await httpGetJson(streamUrl, { referer: 'https://flixcloud.cc/' });

              if (streamResp) {
                // Step 5: Decrypt stream via enc-dec.app
                const decBody = JSON.stringify({
                  data: { context: tokenResult.result.context, stream_response: streamResp }
                });
                const decResult = await httpPostJson('https://enc-dec.app/api/dec-flixcloud?type=stream', decBody);

                if (decResult && decResult.result && decResult.result.stream) {
                  return [{
                    name: `Re:ANIME\n1080p Sub`,
                    title: `${title} - Episode ${targetEp.number} [Sub]`,
                    url: decResult.result.stream,
                    quality: '1080p',
                    behaviorHints: { notWebReady: false, headers: { 'Referer': 'https://flixcloud.cc/', 'User-Agent': UA } },
                    poster,
                    meta: { provider: 'Re:ANIME', source: 'flixcloud', type: 'hls', quality: '1080p', title, poster, subbed: !!targetEp.subbed, directStream: true, mediaType: 'series' },
                  }];
                }
              }
            }
          }
        }
      }
    }
  }

  return [];
}

async function httpPostJson(url, body) {
  try {
    const { stdout } = await execFileAsync('curl', [
      '-sS', '-L', '--compressed', '--max-time', '15',
      '-A', UA, '-H', 'Content-Type: application/json', '-H', 'Accept: application/json',
      '-X', 'POST', '-d', body, url,
    ], { encoding: 'utf-8', maxBuffer: 10 * 1024 * 1024, timeout: 20000 });
    return JSON.parse(stdout);
  } catch (e) { return null; }
}

// ═══════════════════════════════════════════════════════════════════════════
//  TOONWORLD4ALL RESOLVER
// ═══════════════════════════════════════════════════════════════════════════
async function resolveToonWorld4All(tmdbId, type, season, episode) {
  const tmdb = await getTmdbDetails(tmdbId, type);
  if (!tmdb) return [];

  // Step 1: Search toonworld4all.me
  const searchHtml = await httpGet(`https://toonworld4all.me/?s=${encodeURIComponent(tmdb.title)}`);
  if (!searchHtml) return [];

  // Parse Herald articles
  const articleRe = /<article[^>]*class="[^"]*herald-lay-[a-z][^"]*"[^>]*>[\s\S]*?<a[^>]*href="([^"]+)"[^>]*title="([^"]*)"/g;
  let m;
  const results = [];
  while ((m = articleRe.exec(searchHtml)) !== null) {
    results.push({ url: m[1], title: m[2] });
  }
  if (!results.length) return [];

  const best = results.find(r => r.title.toLowerCase().includes(tmdb.title.toLowerCase())) || results[0];

  // Step 2: Parse post page for archive links
  const postHtml = await httpGet(best.url, { referer: 'https://toonworld4all.me/' });
  if (!postHtml) return [];

  const archiveRe = /href="(https:\/\/archive\.toonworld4all\.me\/episode\/[^"]+)"/g;
  const archiveLinks = [];
  while ((m = archiveRe.exec(postHtml)) !== null) {
    archiveLinks.push(m[1]);
  }
  if (!archiveLinks.length) return [];

  // Select episode
  const epIdx = episode ? Math.min(parseInt(episode) - 1, archiveLinks.length - 1) : 0;
  const archiveUrl = archiveLinks[Math.max(0, epIdx)];

  // Step 3: Parse archive page for /redirect/{hash} buttons
  const archHtml = await httpGet(archiveUrl, { referer: 'https://toonworld4all.me/' });
  if (!archHtml) return [];

  const redirectRe = /href="(\/redirect\/[a-f0-9]+)"/g;
  const buttons = [];
  while ((m = redirectRe.exec(archHtml)) !== null) {
    buttons.push(`https://archive.toonworld4all.me${m[1]}`);
  }
  if (!buttons.length) return [];

  // Step 4: Follow each redirect → extract JSON → resolve file host
  const streams = [];
  for (const btnUrl of buttons.slice(0, 8)) {
    const redirHtml = await httpGet(btnUrl, { referer: 'https://archive.toonworld4all.me/' });
    if (!redirHtml) continue;

    // Extract embedded JSON: {domain, hidden}
    const jsonMatch = redirHtml.match(/"domain":"([^"]+)".*?"hidden":"([^"]+)"/);
    if (!jsonMatch) continue;

    const domain = jsonMatch[1];
    const hidden = jsonMatch[2];
    const fileUrl = `${domain}?id=${hidden}`;

    // Resolve based on the file host
    let directUrl = null;
    if (domain.includes('hubcloud')) {
      directUrl = await resolveHubCloud(fileUrl);
    } else if (domain.includes('gdflix')) {
      directUrl = await resolveGDFlix(fileUrl);
    } else if (domain.includes('filepress')) {
      directUrl = await resolveFilePress(fileUrl);
    } else if (domain.includes('mega.nz')) {
      directUrl = `${domain}${hidden}`; // Mega URL — needs key, may not work directly
    }

    if (directUrl && /\.(mkv|mp4|webm|m3u8)/i.test(directUrl)) {
      streams.push({
        name: `ToonWorld4All\n1080p`,
        title: `${tmdb.title} - Episode ${episode || 1}`,
        url: directUrl,
        quality: '1080p',
        behaviorHints: {
          notWebReady: false,
          headers: { 'Referer': domain, 'User-Agent': UA },
          proxyHeaders: { request: { 'Referer': domain, 'User-Agent': UA } },
        },
        poster: tmdb.poster,
        meta: {
          provider: 'ToonWorld4All', source: new URL(domain).hostname,
          type: 'mp4', quality: '1080p', title: tmdb.title,
          episodeNumber: episode, description: tmdb.overview,
          poster: tmdb.poster, genres: tmdb.genres,
          directStream: true, mediaType: type,
        },
      });
    }
  }

  return streams;
}

async function resolveGDFlix(url) {
  const html = await httpGet(url, { referer: 'https://archive.toonworld4all.me/' });
  if (!html) return null;
  // GDFlix typically has a direct download button → Google Drive URL
  const dlMatch = html.match(/href="(https?:\/\/drive\.google\.com\/uc[^"]+)"/) ||
                 html.match(/href="(https?:\/\/[a-z0-9-]+\.workers\.dev[^"]+)"/);
  return dlMatch ? dlMatch[1] : null;
}

async function resolveFilePress(url) {
  const html = await httpGet(url, { referer: 'https://archive.toonworld4all.me/' });
  if (!html) return null;
  const dlMatch = html.match(/href="(https?:\/\/[a-z0-9-]+\.workers\.dev[^"]+)"/) ||
                 html.match(/href="(https?:\/\/[^"]*googleusercontent[^"]*)"/);
  return dlMatch ? dlMatch[1] : null;
}

// ═══════════════════════════════════════════════════════════════════════════
//  EXPRESS SERVER
// ═══════════════════════════════════════════════════════════════════════════
app.use(cors());
app.use(express.json());

app.get('/health', (req, res) => res.json({ status: 'ok' }));

app.get('/resolve/toonflix', async (req, res) => {
  const { tmdb, type = 'tv', season, episode } = req.query;
  console.log(`[ToonFlix] Resolving TMDB ${tmdb} type=${type} ep=${episode}`);
  try {
    const streams = await resolveToonFlix(tmdb, type, season, episode);
    res.json({ streams, total: streams.length });
  } catch (e) {
    res.json({ streams: [], error: e.message });
  }
});

app.get('/resolve/reanime', async (req, res) => {
  const { slug, episode } = req.query;
  console.log(`[Re:ANIME] Resolving slug=${slug} ep=${episode}`);
  try {
    const streams = await resolveReAnime(slug, episode);
    res.json({ streams, total: streams.length });
  } catch (e) {
    res.json({ streams: [], error: e.message });
  }
});

app.get('/resolve/toonworld4all', async (req, res) => {
  const { tmdb, type = 'tv', season, episode } = req.query;
  console.log(`[ToonWorld4All] Resolving TMDB ${tmdb} type=${type} ep=${episode}`);
  try {
    const streams = await resolveToonWorld4All(tmdb, type, season, episode);
    res.json({ streams, total: streams.length });
  } catch (e) {
    res.json({ streams: [], error: e.message });
  }
});

app.listen(PORT, '0.0.0.0', () => {
  console.log(`\n  Stream Proxy Server running on port ${PORT}`);
  console.log(`  Endpoints:`);
  console.log(`    GET /resolve/toonflix?tmdb={id}&type={tv|movie}&episode={n}`);
  console.log(`    GET /resolve/reanime?slug={slug}&episode={n}`);
  console.log(`    GET /resolve/toonworld4all?tmdb={id}&type={tv|movie}&episode={n}`);
  console.log(`    GET /health\n`);
});
