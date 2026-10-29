// Load decoder + perform array rotation, then decode all known offsets
const fs = require('fs');
const decoderSrc = fs.readFileSync('/tmp/cinejoy_decoder.js', 'utf-8');

// Strip module.exports so we have Tt and A in scope
const stripped = decoderSrc.replace(/module\.exports[^;]*;?/g, '');
eval(stripped);

// Re-run the rotation IIFE that runs once at file load (Tt, 459726)
(function (W, e) {
  const t = {
    _0x1bbcb4: "^gJh", _0x38cc3f: 380, _0x4701d2: 750, _0x2288c0: "JWv*",
    _0x3de60c: 782, _0x3eddfb: 548, _0x528f23: "0sDm", _0x5f0dae: 563,
    _0x127569: "]ekK", _0x11b9e7: 410, _0x4b1fc5: 353
  };
  const o = A, c = A, i = W();
  let count = 0;
  for (;;) {
    try {
      const v =
        (parseInt(o(215, t._0x1bbcb4)) / 1) * (parseInt(o(t._0x38cc3f, "QFq(")) / 2) +
        -parseInt(c(t._0x4701d2, t._0x2288c0)) / 3 * (parseInt(c(t._0x3de60c, "]3zF")) / 4) +
        parseInt(o(t._0x3eddfb, t._0x528f23)) / 5 +
        parseInt(c(t._0x5f0dae, t._0x127569)) / 6 +
        parseInt(c(598, "EYAt")) / 7 * (parseInt(o(t._0x11b9e7, "UvXv")) / 8) +
        parseInt(c(t._0x4b1fc5, "EYAt")) / 9 +
        -parseInt(c(197, "g%gf")) / 10;
      if (v === e) break;
      i.push(i.shift());
    } catch {
      i.push(i.shift());
    }
    if (++count > 5000) { console.error('Rotation did not stabilize after 5000 tries'); break; }
  }
  console.error('Rotation completed after', count, 'iterations');
})(Tt, 459726);

// Now decode the known offsets
const known = [
  [202, 'KMCq', 'TMDB api_key (he)'],
  [659, 'H5Gf', 'TMDB base URL (fe)'],
];

console.log('\n=== Known offsets ===');
for (const [off, key, label] of known) {
  console.log(label, '=>', JSON.stringify(A(off, key)));
}

// Collect all A(offset, "key") patterns from the 4 source files
const srcs = ['node0','node2','node9'].map(n => fs.readFileSync('/tmp/cinejoy_' + n + '.js','utf-8'));
srcs.push(fs.readFileSync('/tmp/cinejoy_drk.js','utf-8'));
const re = /(\w)\(([0-9]{2,3}),\s*["']([^"']{1,8})["']\)/g;
const seen = new Set();
const decoded = [];
for (const src of srcs) {
  let m;
  while ((m = re.exec(src))) {
    const fn = m[1];
    // We only care about A() calls (or aliases p, w, c, i, o = A)
    if (!/[Apwcio]/.test(fn)) continue;
    const off = parseInt(m[2], 10);
    const key = m[3];
    const k = off + '|' + key;
    if (seen.has(k)) continue;
    seen.add(k);
    let val;
    try { val = A(off, key); } catch (e) { continue; }
    if (typeof val === 'string' && val.length > 1 && val.length < 300) {
      // Filter printable ASCII
      let printable = true;
      for (let j = 0; j < val.length; j++) {
        const c = val.charCodeAt(j);
        if (c < 9 || (c > 13 && c < 32) || c > 126) { printable = false; break; }
      }
      if (printable) decoded.push({ off, key, val });
    }
  }
}

console.log('\n=== All decoded strings (printable) ===');
decoded.sort((a, b) => a.off - b.off);
for (const d of decoded) {
  console.log('[' + d.off + ' key=' + d.key + '] ' + JSON.stringify(d.val));
}
