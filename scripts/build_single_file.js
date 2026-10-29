// Build script: inline all cinejoy_lib/*.mjs files into a single cinejoy.js
// Strategy: embed files as base64, extract to temp dir at runtime, load as native ES modules
const fs = require('fs');
const path = require('path');
const os = require('os');

const LIB_DIR = '/home/z/my-project/download/cinejoy_lib';
const OUT_FILE = '/home/z/my-project/download/cinejoy.js';

// Read all files
const FILES = [
  'DsnmJJEf.mjs',
  'DGey7X1U.mjs',
  'CHYmk9H1.mjs',
  'qdEHy3Nc.mjs',
  '_dCfw8rj.mjs',
  'B8RmfZZY.mjs',
  '6p5VnPzv.mjs',
  'BOqDcafn.mjs',
];

// Patch BOqDcafn.mjs to export C0 (only if not already patched)
function patchBOQ(code) {
  if (code.includes('C0 as _c0')) return code; // already patched
  return code.replace(
    'export{j as P,',
    'export{j as P,C0 as _c0,'
  );
}

// Read and encode all files
const fileMap = {};
for (const f of FILES) {
  let content = fs.readFileSync(path.join(LIB_DIR, f), 'utf-8');
  if (f === 'BOqDcafn.mjs') {
    content = patchBOQ(content);
  }
  fileMap[f] = Buffer.from(content).toString('base64');
}

// Read the template
const template = fs.readFileSync('/tmp/cinejoy_backup.js', 'utf-8');

// Replace the LIB_DIR/BOQ_MODULE section with embedded files
const embeddedData = JSON.stringify(fileMap);

let newCinejoy = template.replace(
  /\/\/ Path to the cinejoy JS bundle \(relative to this file\)\nconst LIB_DIR = path\.join\(__dirname, 'cinejoy_lib'\);\nconst BOQ_MODULE = path\.join\(LIB_DIR, 'BOqDcafn\.mjs'\);/,
  '// Cinejoy bundle files (base64-encoded, extracted to temp dir at runtime)\n' +
  'const CINEJOY_FILES = ' + embeddedData + ';\n' +
  'const CINEJOY_TMPDIR = path.join(os.tmpdir(), "cinejoy_bundle_" + require("crypto").createHash("md5").update(__filename).digest("hex").slice(0, 8));'
);

// Fix the require for os
newCinejoy = newCinejoy = newCinejoy.replace(
  "const path = require('path');",
  "const path = require('path');\nconst os = require('os');"
);

// Replace the _init method
newCinejoy = newCinejoy.replace(
  /async _init\(\) \{[\s\S]*?return this\._initPromise;\n\s*\}/,
  `async _init() {
    if (this._initPromise) return this._initPromise;
    this._initPromise = (async () => {
      polyfillBrowserAPIs();
      // Extract embedded bundle files to temp directory
      const fs = require('fs');
      if (!fs.existsSync(CINEJOY_TMPDIR)) {
        fs.mkdirSync(CINEJOY_TMPDIR, { recursive: true });
      }
      for (const [name, b64] of Object.entries(CINEJOY_FILES)) {
        const filePath = path.join(CINEJOY_TMPDIR, name);
        if (!fs.existsSync(filePath)) {
          fs.writeFileSync(filePath, Buffer.from(b64, 'base64'));
        }
      }
      // Load as native ES module (Node's native loader handles imports)
      const boqPath = path.join(CINEJOY_TMPDIR, 'BOqDcafn.mjs');
      const mod = await import('file://' + boqPath);
      if (!mod._c0) {
        throw new Error('Cinejoy bundle did not export _c0.');
      }
      this._rxClient = mod._c0(SHEGU_API);
      await this._rxClient.handshake();
    })();
    return this._initPromise;
  }`
);

fs.writeFileSync(OUT_FILE, newCinejoy);
console.log('Written:', OUT_FILE);
console.log('Size:', newCinejoy.length, 'bytes');
