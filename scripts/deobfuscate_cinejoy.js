const fs = require('fs');

(async () => {
  // Download Drk1jBmW.js
  const res = await fetch('https://cinejoy.to/_app/immutable/chunks/Drk1jBmW.js');
  const js = await res.text();
  console.log('=== Drk1jBmW.js (' + js.length + 'b) ===');
  
  // Find fetch calls
  const fetches = [...js.matchAll(/fetch\(`([^`]+)`/g)].map(m => m[1]);
  console.log('Fetch URLs:', fetches.slice(0, 10));
  
  // Find string URLs
  const urls = [...js.matchAll(/["'](https?:\/\/[^"']+)["']/g)].map(m => m[1]);
  console.log('URLs:', [...new Set(urls)].slice(0, 10));
  
  // Find template literal URLs
  const templates = [...js.matchAll(/`([^`]*\$\{[^`]*)`/g)].map(m => m[1]);
  const filtered = templates.filter(t => t.includes('http') || t.includes('/api') || t.includes('shegu') || t.includes('stream') || t.includes('video') || t.includes('content') || t.includes('movie') || t.includes('playlist'));
  console.log('Template URLs:', [...new Set(filtered)].slice(0, 15));
  
  // Find shegu
  const shegu = [...js.matchAll(/shegu/gi)];
  console.log('Shegu count:', shegu.length);
  
  // Find earthcleaner
  const earth = [...js.matchAll(/earthcleaner/gi)];
  console.log('Earth count:', earth.length);
  
  // Find ALL domain-like strings
  const domains = [...js.matchAll(/["']([a-z0-9-]+\.(st|cc|to|net|com|xyz|buzz|dev|io|cloud|workers))["']/gi)].map(m => m[1]);
  console.log('Domains:', [...new Set(domains)].slice(0, 15));
  
  // Find ALL paths that look like API endpoints
  const paths = [...js.matchAll(/["'](\/[a-z][a-z0-9/_-]{3,40})["']/g)].map(m => m[1]);
  const apiPaths = paths.filter(p => !p.startsWith('/app') && !p.startsWith('/static') && !p.includes('svg') && !p.includes('css') && !p.includes('chunk'));
  console.log('API-ish paths:', [...new Set(apiPaths)].slice(0, 15));
  
  // Also check 6p5VnPzv.js
  console.log('');
  const res2 = await fetch('https://cinejoy.to/_app/immutable/chunks/6p5VnPzv.js');
  const js2 = await res2.text();
  console.log('=== 6p5VnPzv.js (' + js2.length + 'b) ===');
  
  const fetches2 = [...js2.matchAll(/fetch\(`([^`]+)`/g)].map(m => m[1]);
  console.log('Fetch URLs:', fetches2.slice(0, 10));
  
  const templates2 = [...js2.matchAll(/`([^`]*\$\{[^`]*)`/g)].map(m => m[1]);
  const filtered2 = templates2.filter(t => t.includes('http') || t.includes('/api') || t.includes('shegu') || t.includes('stream') || t.includes('video') || t.includes('content') || t.includes('movie') || t.includes('playlist'));
  console.log('Template URLs:', [...new Set(filtered2)].slice(0, 15));
  
  const domains2 = [...js2.matchAll(/["']([a-z0-9-]+\.(st|cc|to|net|com|xyz|buzz|dev|io|cloud|workers))["']/gi)].map(m => m[1]);
  console.log('Domains:', [...new Set(domains2)].slice(0, 15));
  
  const shegu2 = [...js2.matchAll(/shegu/gi)];
  console.log('Shegu count:', shegu2.length);
})();
