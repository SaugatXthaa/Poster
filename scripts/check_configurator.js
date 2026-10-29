// Extract every <script> block from index.html and syntax-check it.
const fs = require('fs');
const path = '/home/z/my-project/configurator.html';
const html = fs.readFileSync(path, 'utf8');
const re = /<script\b([^>]*)>/gi;
let m, i = 0, failures = 0;
while ((m = re.exec(html)) !== null) {
  i++;
  const start = m.index + m[0].length;
  const end = html.indexOf('</script>', start);
  if (end < 0) { console.log(`block ${i}: NO CLOSING TAG`); failures++; continue; }
  const attrs = m[1] || '';
  if (/\bsrc=/.test(attrs)) { console.log(`block ${i}: external (skipped)`); continue; }
  const code = html.slice(start, end);
  const lineOfStart = html.slice(0, start).split('\n').length;
  try {
    new Function(code);
    console.log(`block ${i}: OK  (~line ${lineOfStart}, ${code.length} chars)`);
  } catch (e) {
    failures++;
    console.log(`block ${i}: SYNTAX ERROR ~line ${lineOfStart}: ${e.message}`);
    // locate the error position within the block
    const pos = /position (\d+)/.exec(e.message);
    if (pos) {
      const p = parseInt(pos[1], 10);
      console.log('   near:', JSON.stringify(code.slice(Math.max(0, p - 80), p + 80)));
    }
  }
}
console.log(failures ? `\n${failures} broken block(s)` : '\nall blocks OK');
