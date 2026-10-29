// Probe each of 18 bingebang embed URLs for: HTTP redirect target, player domain reachable?
// Usage: node /home/z/my-project/scripts/probe_bingebang.js <imdbId> <type> [season] [episode]

const { SERVERS, buildEmbedUrl } = require('/home/z/my-project/allinone-nuvio-repo/providers/bingebang.js');
const https = require('https'); const http = require('http');
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';

function fetch3(url, depth=3, headers={}) {
  return new Promise(resolve => {
    let cur = url, h = 0; const visited = [];
    const next = () => {
      if (h++ >= depth) return resolve({ visited });
      const u = new URL(cur); const lib = u.protocol === 'http:' ? http : https;
      const req = lib.request({
        hostname: u.hostname, path: u.pathname + u.search, method: 'GET',
        headers: { 'User-Agent': UA, 'Accept': 'text/html,*/*', 'Referer': 'https://knightflix.pages.dev/', ...headers }
      }, (res) => {
        res.destroy();
        visited.push({
          url: cur, status: res.statusCode, ct: res.headers['content-type'] || '',
          loc: res.headers['location'] || '',
        });
        if (res.statusCode >= 300 && res.statusCode < 400 && res.headers.location) {
          cur = new URL(res.headers.location, cur).href; next();
        } else { resolve({ visited }); }
      });
      req.on('error', e => resolve({ error: e.message, visited }));
      req.on('timeout', () => { req.destroy(); resolve({ error: 'timeout', visited }); });
      req.setTimeout(8000); req.end();
    };
    next();
  });
}

async function probeOne(tmdbId, type, season, episode) {
  const isMovie = type !== 'tv' && type !== 'series';
  console.log(`\n=== ${tmdbId} (${type}${isMovie ? '' : ' S'+season+'E'+episode}) ===`);
  const out = [];
  for (const srv of SERVERS) {
    const url = buildEmbedUrl(srv.id, type, tmdbId, season, episode);
    const r = await fetch3(url);
    let finalUrl = '', finalStatus = 0, finalCt = '', err = r.error;
    if (r.visited && r.visited.length > 0) {
      const last = r.visited[r.visited.length - 1];
      finalUrl = last.url;
      finalStatus = last.status;
      finalCt = last.ct;
    }
    const reachable = finalStatus === 200 || finalStatus === 403 /* cf-challenge */;
    const playerDomain = finalUrl ? new URL(finalUrl).hostname : '';
    out.push({
      srv: srv.id, name: srv.name,
      redirect: !!finalUrl, status: finalStatus,
      domain: playerDomain, ct: finalCt.slice(0, 30),
      err,
      playable: reachable && (/\bx?html|text\/html|application\/json/.test(finalCt) || finalCt === '' /* iframe page */),
    });
    console.log(`  ${srv.id} ${srv.name.padEnd(15)} → ${finalStatus || 'ERR'} ${playerDomain || err || ''} ${finalCt.slice(0,20)}`);
  }
  return out;
}

(async () => {
  const [,,idArg, typeArg, seasonArg, episodeArg] = process.argv;
  const targets = [];
  if (idArg) {
    targets.push({ id: idArg, type: typeArg || 'movie', season: seasonArg, episode: episodeArg });
  } else {
    // default: run all 3 tests
    targets.push({ id: '27205', type: 'movie' });           // Inception (TMDB)
    targets.push({ id: '129', type: 'movie' });              // Spirited Away (TMDB)
    targets.push({ id: '1396', type: 'tv', season: 1, episode: 1 });  // Breaking Bad S01E01 (TMDB)
  }
  const all = {};
  for (const t of targets) {
    all[`${t.id}.${t.type}${t.season ? '.s'+t.season+'e'+t.episode : ''}`] = await probeOne(t.id, t.type, t.season, t.episode);
  }
  console.log('\n=== SUMMARY ===');
  for (const [key, arr] of Object.entries(all)) {
    const ok = arr.filter(a => a.playable).length;
    const cf = arr.filter(a => a.status === 403).length;
    const fail = arr.filter(a => a.err).length;
    console.log(`${key}: ${ok}/18 playable, ${cf} cf-challenged, ${fail} failed`);
  }
})();
