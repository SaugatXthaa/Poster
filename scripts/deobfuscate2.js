const fs = require('fs');

(async () => {
  const res = await fetch('https://cinejoy.to/_app/immutable/chunks/Drk1jBmW.js');
  const js = await res.text();
  
  // Find ALL variable assignments with string values
  const stringAssigns = [...js.matchAll(/[;,](\w+)=["']([^"']{3,80})["']/g)];
  const interesting = stringAssigns.filter(m => {
    const val = m[2];
    return val.includes('http') || val.includes('api') || val.includes('shegu') || 
           val.includes('earth') || val.includes('stream') || val.includes('video') ||
           val.includes('content') || val.includes('movie') || val.includes('playlist') ||
           val.includes('cdn') || val.includes('.st') || val.includes('.cc') ||
           val.match(/^[a-f0-9]{32}$/) || val.includes('key');
  });
  console.log('Interesting string assignments:');
  for (const m of interesting.slice(0, 20)) {
    console.log('  ' + m[1] + ' = ' + m[2]);
  }
  
  // Find ALL variable assignments with template literals (URLs with ${})
  const templateAssigns = [...js.matchAll(/[;,](\w+)=`([^`]{3,100})`/g)];
  const interestingTemplates = templateAssigns.filter(m => {
    const val = m[2];
    return val.includes('http') || val.includes('${') || val.includes('/api') || 
           val.includes('stream') || val.includes('video') || val.includes('content') ||
           val.includes('movie') || val.includes('playlist');
  });
  console.log('\nTemplate assignments:');
  for (const m of interestingTemplates.slice(0, 15)) {
    console.log('  ' + m[1] + ' = `' + m[2] + '`');
  }
  
  // Find the 'he' variable (TMDB API key)
  const heMatches = [...js.matchAll(/[;,]he=["']([^"']+)["']/g)];
  console.log('\nhe variable:', heMatches[0]?.[1]);
  
  // Find the 'fe' variable
  const feMatches = [...js.matchAll(/[;,]fe=["']([^"']+)["']/g)];
  console.log('fe variable:', feMatches[0]?.[1]);
  
  // Find ALL 32-char hex strings (API keys)
  const hexKeys = [...js.matchAll(/["']([a-f0-9]{32})["']/g)];
  console.log('\nHex keys:', [...new Set(hexKeys.map(m => m[1]))]);
  
  // Find the context around 'new URL' calls
  const urlCalls = [...js.matchAll(/new URL\(([^)]{5,100})\)/g)];
  console.log('\nnew URL() calls:');
  for (const m of urlCalls.slice(0, 10)) {
    console.log('  new URL(' + m[1] + ')');
  }
})();
