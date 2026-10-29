/**
 * Nuvio Curl Wrapper — Patches Nuvio providers to use system curl
 * Bypasses Cloudflare JS Detection on anime sites.
 * 
 * Usage:
 *   require('./nuvio_curl_wrapper'); // patches global fetch
 *   const s = require('./nuvio_animesalt.js');
 *   const streams = await s.getStreams(95479, 'tv', 1, 1);
 */
'use strict';
const { execSync } = require('child_process');

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 ' +
           '(KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';

// Patch global fetch to use system curl for CF-protected sites
const originalFetch = global.fetch;

global.fetch = async function(url, options = {}) {
  const urlStr = typeof url === 'string' ? url : url.url || url.toString();
  
  // Sites that need curl (CF JS Detection)
  const CF_SITES = [
    'animesalt.link',
    'animesalt.ac',
    'watchanimeworld.top',
    'watchanimeworld.net',
    'all-wish.me',
    'as-cdn21.top',
    'as-cdn22.top',
    'as-cdn23.top',
    'play.zephyrix.top',
    'play.zephyrflick.top',
  ];
  
  const needsCurl = CF_SITES.some(s => urlStr.includes(s));
  
  if (!needsCurl) {
    return originalFetch(url, options);
  }
  
  // Use system curl
  const method = options.method || 'GET';
  const headers = options.headers || {};
  const body = options.body;
  
  // Build curl command
  let cmd = `curl -sS -L --max-time 15 -A "${UA}"`;
  
  // Add headers
  for (const [k, v] of Object.entries(headers)) {
    cmd += ` -H "${k}: ${v}"`;
  }
  
  // Add body for POST
  if (body && method === 'POST') {
    cmd += ` -X POST -d "${body}"`;
  }
  
  cmd += ` "${urlStr}"`;
  
  try {
    const output = execSync(cmd, {
      encoding: 'utf-8',
      maxBuffer: 20 * 1024 * 1024,
      timeout: 20000,
    });
    
    return {
      ok: true,
      status: 200,
      statusText: 'OK',
      headers: new Map(),
      text: () => Promise.resolve(output),
      json: () => Promise.resolve(JSON.parse(output)),
      arrayBuffer: () => Promise.resolve(Buffer.from(output)),
      clone: function() { return this; },
    };
  } catch (e) {
    return {
      ok: false,
      status: 0,
      statusText: e.message,
      headers: new Map(),
      text: () => Promise.resolve(''),
      json: () => Promise.resolve({}),
      arrayBuffer: () => Promise.resolve(Buffer.alloc(0)),
      clone: function() { return this; },
    };
  }
};

// Also patch the domain changes
// animesalt.ac → animesalt.link
// watchanimeworld.net → watchanimeworld.top

console.log('[Nuvio Curl Wrapper] fetch patched for CF sites');
