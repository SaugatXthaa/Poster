'use strict';
const https = require('https');
const http = require('http');
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';

function probe(url) {
  return new Promise(resolve => {
    let cur = url, h = 0; const visited = [];
    const next = () => {
      if (h++ >= 6) return resolve({visited});
      const u = new URL(cur);
      const lib = u.protocol === 'http:' ? http : https;
      const req = lib.request({
        hostname: u.hostname, port: u.port, path: u.pathname + u.search, method: 'GET',
        headers: { 'User-Agent': UA, 'Range': 'bytes=0-1', 'Accept': '*/*', 'Referer': 'https://pengu.uk/' }
      }, (res) => {
        res.destroy();
        visited.push({ url: cur, status: res.statusCode, ct: res.headers['content-type'], cl: res.headers['content-length'], cr: res.headers['content-range'], loc: res.headers['location'] });
        if (res.statusCode >= 300 && res.statusCode < 400 && res.headers.location) {
          cur = new URL(res.headers.location, cur).href;
          next();
        } else {
          resolve({ visited });
        }
      });
      req.on('error', e => resolve({ error: e.message, visited }));
      req.on('timeout', () => { req.destroy(new Error('timeout')); resolve({ error: 'timeout', visited }); });
      req.setTimeout(20000);
      req.end();
    };
    next();
  });
}

(async () => {
  const samples = [
    { name: 'gdflix #1', b64: 'eyJraW5kIjoiZ2RmbGl4IiwiZmlsZVVybCI6Imh0dHBzOi8vZ2RmbGl4LmRldi9maWxlL2l0R3lMdkk1d0l5dnJOSSJ9' },
    { name: 'gdflix #2', b64: 'eyJraW5kIjoiZ2RmbGl4IiwiZmlsZVVybCI6Imh0dHBzOi8vZ2RmbGl4LmRldi9maWxlL053TlVsRVhJU2VCY1dJSCJ9' },
    { name: 'hubcloud', b64: 'eyJraW5kIjoiaHViY2xvdWQiLCJsYW5kaW5nVXJsIjoiaHR0cHM6Ly9odWJjbG91ZC5jeC9kcml2ZS9jZGNlNTNkZGFlbGJxamoifQ' },
  ];

  console.log('=== Decode PenguPlay base64 payloads and probe direct URLs (bypassing pengu.uk) ===\n');
  for (const s of samples) {
    const decoded = Buffer.from(s.b64, 'base64').toString('utf8');
    console.log('--- ' + s.name + ' ---');
    console.log('  decoded: ' + decoded);
    try {
      const j = JSON.parse(decoded);
      const directUrl = j.fileUrl || j.landingUrl;
      if (directUrl) {
        console.log('  direct URL: ' + directUrl);
        const r = await probe(directUrl);
        if (r.error) console.log('  ✗ error: ' + r.error);
        r.visited.forEach((v, i) => {
          console.log('  [' + i + '] ' + v.status + ' ' + (v.ct || '') + ' cl=' + v.cl + ' cr=' + (v.cr || '').slice(0, 60));
          console.log('      url: ' + v.url.slice(0, 200));
          if (v.loc) console.log('      → ' + v.loc.slice(0, 200));
        });
      }
    } catch (e) { console.log('  err: ' + e.message); }
    console.log('');
  }
})();
