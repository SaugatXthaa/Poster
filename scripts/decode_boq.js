const fs = require('fs');
const js = fs.readFileSync('/tmp/cinejoy_chunks/chunks_BOqDcafn.js', 'utf-8');

function extractFn(js, name) {
  const start = js.indexOf('function ' + name + '(');
  if (start === -1) return null;
  let depth = 0, end = start;
  let i = start;
  while (i < js.length && js[i] !== '{') i++;
  for (; i < js.length; i++) {
    if (js[i] === '{') depth++;
    if (js[i] === '}') { depth--; if (depth === 0) { end = i + 1; break; } }
  }
  return js.substring(start, end);
}

// Setup all array functions
const arrFns = ['Z', 'F', 'A', 'z'];
let setup = '';
for (const n of arrFns) {
  const fn = extractFn(js, n);
  if (fn) setup += fn + ';\n';
}
eval(setup);

// Setup all decoder functions
const decFns = ['O', 'G'];
for (const n of decFns) {
  const fn = extractFn(js, n);
  if (fn) eval(fn);
}

// Find and run rotation IIFEs
for (const arr of arrFns) {
  const rotRe = new RegExp('\\((' + arr + ')\\s*,\\s*(\\d+)\\)', 'g');
  const rotMatches = [...js.matchAll(rotRe)];
  for (const rm of rotMatches) {
    let start = rm.index;
    while (start > 0) {
      start--;
      if (js.substring(start, start + 10) === '(function(') break;
    }
    let end = rm.index + rm[0].length;
    while (end < js.length && js[end] !== ')') end++;
    end++;
    try { eval(js.substring(start, end)); console.log('Ran rotation for', arr, 'target', rm[2]); } catch(e) {}
  }
}

// Build property map
const keyMap = {};
const propMatches = [...js.matchAll(/(_0x[a-f0-9]{6,8}):\s*["']([^"']{1,10})["']/g)];
for (const m of propMatches) keyMap[m[1]] = m[2];
const numMatches = [...js.matchAll(/(_0x[a-f0-9]{6,8}):\s*([0-9]+)/g)];
for (const m of numMatches) keyMap[m[1]] = parseInt(m[2], 10);

// Brute-force decode
const keySet = new Set();
const keyMatches = [...js.matchAll(/["']([A-Za-z0-9_#%&*\[\]\^@!\-\.()\{}|\\/+=$]{2,8})["']/g)];
for (const m of keyMatches) keySet.add(m[1]);

const results = [];
const seen = new Set();
for (const dec of decFns) {
  const baseMatch = js.match(new RegExp('function\\s+' + dec + '\\([^)]+\\)\\{\\s*([A-Za-z_$])\\s*=\\s*([A-Za-z_$])\\s*-\\s*(\\d+)'));
  if (!baseMatch) continue;
  const base = parseInt(baseMatch[3], 10);
  
  for (let off = base; off < base + 500; off++) {
    for (const key of keySet) {
      if (key.length < 2 || key.length > 8) continue;
      try {
        const fn = eval(dec);
        const v = fn(off, key);
        if (typeof v === 'string' && v.length > 1 && v.length < 300) {
          let printable = true;
          for (let j = 0; j < v.length; j++) {
            const c = v.charCodeAt(j);
            if (c < 9 || (c > 13 && c < 32) || c > 126) { printable = false; break; }
          }
          if (printable && /[a-z]/i.test(v)) {
            const k = dec + '|' + off + '|' + key;
            if (!seen.has(k)) {
              seen.add(k);
              results.push({ dec, off, key, val: v });
            }
          }
        }
      } catch(e) {}
    }
  }
}

// Show interesting results
const interesting = results.filter(r =>
  /water|token|hash|salt|secret|sign|hmac|sha|md5|pow|challenge|atob|btoa|crypto|subtle|shegu|stream|source|video|play|m3u8|hls|playlist|mp4|cdn|http|fetch|url|src|auth|json|header|content|origin|referer|user-agent|embed|application|text|GET|POST|range|bytes|accept|lisbon|noise|lumen|wire|handshake|prologue|nonce|gcm|ctr|x25519|chacha|nacl|secretbox|curve|ed25519|ephemeral|static|identity|octet|importKey|deriveKey|deriveBits|generateKey|getRandomValues|encrypt|decrypt|exportKey|raw|spki|pkcs8|AES|CBC|GCM|CTR|base64|base64url|imdb|tmdb|season|episode|movie|type|server/i.test(r.val)
);
const unique = new Map();
for (const r of interesting) {
  if (!unique.has(r.val)) unique.set(r.val, r);
}
console.log('=== Interesting decoded strings from BOqDcafn.js ===');
for (const r of [...unique.values()].sort((a,b) => a.off - b.off)) {
  console.log('[' + r.dec + ' ' + r.off + '] ' + JSON.stringify(r.val));
}
console.log('---');
console.log('Total decoded:', results.length, '| Interesting:', unique.size);
