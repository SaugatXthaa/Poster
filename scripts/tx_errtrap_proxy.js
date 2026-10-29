// Tiny proxy that injects an early error trap into the TOPX configurator page
// so the original (pre-trap) load-time exception can be captured and reported
// back through the DOM + sessionStorage for agent-browser to read.
const http = require('http');

const UPSTREAM = { host: '127.0.0.1', port: 3471 };
const TRAP = `<script>
window.__errs = [];
window.addEventListener('error', function (e) {
  var msg = (e && e.message) || String(e);
  var loc = e && e.filename ? (e.filename + ':' + e.lineno + ':' + e.colno) : '?';
  var st = e && e.error && e.error.stack ? String(e.error.stack).split('\\n').slice(0, 6).join(' ~ ') : '';
  window.__errs.push(msg + ' @ ' + loc + ' ||| ' + st);
  try { sessionStorage.setItem('__origErr', window.__errs.join('\\n')); } catch (x) {}
});
window.addEventListener('unhandledrejection', function (e) {
  var r = e && e.reason;
  window.__errs.push('UNHANDLED-REJECTION: ' + ((r && r.message) || String(r)));
  try { sessionStorage.setItem('__origErr', window.__errs.join('\\n')); } catch (x) {}
});
</script>`;

http.createServer((req, res) => {
  const opts = { ...UPSTREAM, path: req.url, method: req.method, headers: { ...req.headers, host: '127.0.0.1:3471' } };
  const up = http.request(opts, (ur) => {
    const isHtml = (ur.headers['content-type'] || '').includes('text/html');
    if (!isHtml) {
      res.writeHead(ur.statusCode, ur.headers);
      ur.pipe(res);
      return;
    }
    const chunks = [];
    ur.on('data', (c) => chunks.push(c));
    ur.on('end', () => {
      let body = Buffer.concat(chunks).toString('utf8');
      body = body.replace('<body>', '<body>' + TRAP);
      res.writeHead(ur.statusCode, { ...ur.headers, 'content-length': Buffer.byteLength(body) });
      res.end(body);
    });
    ur.on('error', () => { try { res.end(); } catch (_) {} });
  });
  up.on('error', (e) => { res.writeHead(502); res.end('proxy error: ' + e.message); });
  req.pipe(up);
}).listen(3499, () => console.log('err-trap proxy on :3499 -> :3471'));
