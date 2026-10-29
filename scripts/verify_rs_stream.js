const https = require('https');
const http = require('http');
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';

function fetchBuf(url, { headers = {}, timeout = 20000 } = {}) {
  return new Promise((resolve, reject) => {
    const u = new URL(url);
    const lib = u.protocol === 'http:' ? http : https;
    const req = lib.request({
      hostname: u.hostname, path: u.pathname + u.search, method: 'GET',
      headers: { 'User-Agent': UA, 'Accept': '*/*', ...headers }, timeout,
    }, (res) => {
      const chunks = [];
      res.on('data', c => chunks.push(c));
      res.on('end', () => resolve({ status: res.statusCode, headers: res.headers, body: Buffer.concat(chunks) }));
    });
    req.on('error', reject);
    req.on('timeout', () => { req.destroy(new Error('timeout')); });
    req.end();
  });
}

async function main() {
  const rs = require('/home/z/my-project/scripts/rivestream_all_in_one.js');
  const streams = await rs.getStreams('693134', 'movie');
  console.log(`\nVerifying ${streams.length} streams...\n`);
  let ok = 0, fail = 0;
  for (const s of streams) {
    try {
      const r = await fetchBuf(s.url, { 
        headers: { 
          'Referer': 'https://rivestream.ru/',
          'Origin': 'https://rivestream.ru',
          'Accept': s.type === 'video/mp4' ? 'video/mp4, */*' : 'application/vnd.apple.mpegurl, */*',
          'Range': 'bytes=0-1023',
        },
        timeout: 12000
      });
      const ct = r.headers['content-type'] || '?';
      const body = r.body.toString('utf8').slice(0, 80);
      const ok_status = r.status === 200 || r.status === 206;
      const isHls = ok_status && (ct.includes('mpegurl') || body.startsWith('#EXTM3U'));
      const isMp4 = ok_status && (ct.includes('video/') || ct.includes('octet-stream'));
      if (isHls || isMp4) {
        console.log(`✅ ${s.name}`);
        console.log(`   HTTP ${r.status} (${ct}, ${r.body.length}B)`);
        ok++;
      } else {
        console.log(`❌ ${s.name}`);
        console.log(`   HTTP ${r.status} (${ct}): ${body}`);
        fail++;
      }
    } catch (e) {
      console.log(`❌ ${s.name}: ${e.message.slice(0, 80)}`);
      fail++;
    }
  }
  console.log(`\n=== RESULT: ${ok}/${streams.length} playable, ${fail} failed ===`);
}
main().catch(e => { console.error(e); process.exit(1); });
