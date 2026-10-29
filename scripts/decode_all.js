const fs = require('fs');
const js = fs.readFileSync('/tmp/cinejoy_drk.js', 'utf-8');

// Setup
const ttStart = js.indexOf('function Tt()');
let depth = 0, ttEnd = ttStart;
for (let i = ttStart; i < js.length; i++) {
  if (js[i] === '{') depth++;
  if (js[i] === '}') { depth--; if (depth === 0) { ttEnd = i + 1; break; } }
}
const aStart = js.indexOf('function A(');
depth = 0;
let aEnd = aStart;
for (let i = aStart; i < js.length; i++) {
  if (js[i] === '{') depth++;
  if (js[i] === '}') { depth--; if (depth === 0) { aEnd = i + 1; break; } }
}
eval(js.substring(ttStart, ttEnd));
eval(js.substring(aStart, aEnd));

// Collect ALL unique keys
const allKeys = new Set();
const keyMatches = [...js.matchAll(/["']([a-zA-Z0-9!@#$%^&*()\-_=+]{2,8})["']/g)];
for (const m of keyMatches) {
  allKeys.add(m[1]);
}

// Try ALL offsets with ALL keys
// Print any result that's fully printable ASCII
console.log('=== Fully readable decoded strings ===');
const results = [];
for (let i = 194; i < 818; i++) {
  for (const key of allKeys) {
    try {
      const result = A(i, key);
      if (result && result.length > 2 && result.length < 200) {
        // Check if result is fully printable ASCII (no control chars)
        let isPrintable = true;
        for (let j = 0; j < result.length; j++) {
          const c = result.charCodeAt(j);
          if (c < 32 || c > 126) { isPrintable = false; break; }
        }
        if (isPrintable && /[a-zA-Z]/.test(result)) {
          results.push({ offset: i, key, result });
        }
      }
    } catch(e) {}
  }
}

// Sort by offset and deduplicate
const seen = new Set();
for (const r of results.sort((a, b) => a.offset - b.offset)) {
  const k = r.offset + '|' + r.result;
  if (!seen.has(k)) {
    seen.add(k);
    console.log('  [' + r.offset + '] key="' + r.key + '" => ' + r.result);
  }
}
