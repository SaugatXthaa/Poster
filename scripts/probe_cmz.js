// Probe bingebang + cloudmoviez stream URLs for playability
const { SERVERS: BB_SERVERS, buildEmbedUrl: bbEmbed } = require('/home/z/my-project/allinone-nuvio-repo/providers/bingebang.js');
const cmz = require('/home/z/my-project/allinone-nuvio-repo/providers/cloudmoviez.js');
const https = require('https'); const http = require('http');
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';

function probe(url) {
  return new Promise(resolve => {
    let cur = url, h = 0; const visited = [];
    const next = () => {
      if (h++ >= 4) return resolve({ visited });
      const u = new URL(cur); const lib = u.protocol === 'http:' ? http : https;
      const req = lib.request({
        hostname: u.hostname, path: u.pathname + u.search, method: 'GET',
        headers: { 'User-Agent': UA, 'Range': 'bytes=0-1', 'Accept': '*/*', 'Referer': 'https://c1.cloudmoviez.shop/' }
      }, (res) => {
        res.destroy();
        visited.push({ url: cur, status: res.statusCode, ct: res.headers['content-type'] || '', cl: res.headers['content-length'] || '?', loc: res.headers['location'] || '' });
        if (res.statusCode >= 300 && res.statusCode < 400 && res.headers.location) {
          cur = new URL(res.headers.location, cur).href; next();
        } else resolve({ visited });
      });
      req.on('error', e => resolve({ error: e.message, visited }));
      req.on('timeout', () => { req.destroy(); resolve({ error: 'timeout', visited }); });
      req.setTimeout(8000); req.end();
    };
    next();
  });
}

(async () => {
  console.log('=== Probe CloudMovieZ streams for Inception ===');
  const incStreams = await cmz.getStreams('tt1375666', 'movie');
  for (const s of incStreams.slice(0, 3)) {
    const r = await probe(s.url);
    if (r.error) { console.log('  ' + s.quality + ' XX ' + r.error); continue; }
    const last = r.visited[r.visited.length - 1];
    const ok = (last.status === 200 || last.status === 206) && /video|octet-stream|mpegurl/i.test(last.ct);
    console.log('  ' + s.quality + ' ' + (ok ? 'OK' : 'XX') + ' HTTP:' + last.status + ' ' + last.ct.slice(0, 30) + ' ' + last.cl + 'B');
  }
  
  console.log('\n=== Probe CloudMovieZ streams for A Shop for Killers ===');
  const shopStreams = await cmz.getStreams('215072', 'tv', 1, 1);
  for (const s of shopStreams.slice(0, 4)) {
    const r = await probe(s.url);
    if (r.error) { console.log('  ' + s.quality + ' XX ' + r.error); continue; }
    const last = r.visited[r.visited.length - 1];
    const ok = (last.status === 200 || last.status === 206) && /video|octet-stream|mpegurl/i.test(last.ct);
    console.log('  ' + s.quality + ' ' + (ok ? 'OK' : 'XX') + ' HTTP:' + last.status + ' ' + last.ct.slice(0, 30) + ' ' + last.cl + 'B');
  }
})();
