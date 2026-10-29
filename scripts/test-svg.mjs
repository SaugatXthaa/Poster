import { _debugNumberSvg } from '/home/z/my-project/download/topx-posters/cover-generator.js';
import fs from 'fs';

// Need a LAYOUTS object - it's not exported but we can recreate the landscape one
const LAYOUTS = {
  landscape: {
    canvas: { width: 1280, height: 720 },
    card: { x: 300, y: 107, width: 900, height: 506, radius: 30 },
    number: { xSingle: 62, xDouble: 24, sizeSingle: 450, sizeDouble: 370, opticalDrop: 8 },
    logo: { maxWidth: 380, maxHeight: 155, left: 46, bottom: 40 }
  },
  poster: {
    canvas: { width: 1000, height: 1500 },
    card: { x: 210, y: 165, width: 730, height: 1095, radius: 38 },
    number: { xSingle: 14, xDouble: -10, sizeSingle: 405, sizeDouble: 335, opticalDrop: 22 },
    logo: { maxWidth: 380, maxHeight: 155, left: 46, bottom: 40 }
  }
};

const svg = _debugNumberSvg(1, LAYOUTS.landscape, '#8C75FF', 'Action', '8.5', 'provider', true, 'tx.it.topx.movie', 'Inter', 'star');
fs.writeFileSync('/tmp/num.svg', svg);
console.log('SVG length:', svg.length);
console.log('First 600 chars:', svg.slice(0, 600));
console.log('---');
console.log('Around line 46:');
const lines = svg.split('\n');
for (let i = 40; i < 55 && i < lines.length; i++) {
  console.log(`Line ${i+1}: ${lines[i].slice(0, 120)}`);
}
