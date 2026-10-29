// Probe PenguPlay /direct/experimental/ — see what it actually returns.
// Goal: confirm whether it's torrent-based or HTTPS-based, and find the real source.
'use strict';

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';

// The payload the user gave us (from /direct/experimental/<payload>)
const PAYLOAD_RAW = 'eyJ0IjoibW92aWUiLCJpIjoidHQzNzI4NzMzNSIsInMiOm51bGwsImUiOm51bGwsImgiOiIxOGQ4Mjk1YjNhMzk0YTUxZTZlN2JlZDZiM2ZiNDIxZDE1ZDBjNWZmIiwiZiI6MH0/Obsession.2025.2160p.UHD.BluRay.REMUX.DV.HDR.10bit...mkv';

// Decode base64url payload
function b64urlDecode(s) {
  const norm = s.replace(/-/g, '+').replace(/_/g, '/');
  const padded = norm + '='.repeat((4 - (norm.length % 4)) % 4);
  return Buffer.from(padded, 'base64').toString('utf8');
}

async function probe(url, label) {
  console.log('\n========== ' + label + ' ==========');
  console.log('URL: ' + url);
  try {
    const res = await fetch(url, {
      method: 'GET',
      redirect: 'manual', // do NOT follow redirects — see where pengu.uk points
      headers: {
        'User-Agent': UA,
        'Accept': '*/*',
      },
      signal: AbortSignal.timeout(15000),
    });
    console.log('Status: ' + res.status + ' ' + res.statusText);
    const headers = {};
    res.headers.forEach((v, k) => headers[k] = v);
    console.log('Headers: ' + JSON.stringify(headers, null, 2));
    const body = await res.text();
    console.log('Body length: ' + body.length);
    console.log('Body (first 2000 chars): ' + body.slice(0, 2000));
    return { status: res.status, headers, body };
  } catch (e) {
    console.log('Error: ' + e.message);
    return null;
  }
}

async function followWithHeaders(url, label) {
  // Follow but capture each hop
  console.log('\n========== ' + label + ' (follow) ==========');
  console.log('URL: ' + url);
  let currentUrl = url;
  let hop = 0;
  while (hop < 10) {
    hop++;
    try {
      const res = await fetch(currentUrl, {
        redirect: 'manual',
        headers: { 'User-Agent': UA, 'Accept': '*/*' },
        signal: AbortSignal.timeout(15000),
      });
      console.log('[hop ' + hop + '] ' + res.status + ' ' + res.statusText + ' → ' + currentUrl);
      const loc = res.headers.get('location');
      const ct = res.headers.get('content-type');
      const cl = res.headers.get('content-length');
      console.log('    content-type: ' + ct + ', content-length: ' + cl);
      if (loc) console.log('    Location: ' + loc);
      res.headers.forEach((v, k) => {
        if (k !== 'location' && k !== 'content-type' && k !== 'content-length' && k !== 'date' && k !== 'server') {
          console.log('    ' + k + ': ' + v);
        }
      });
      if (res.status >= 300 && res.status < 400 && loc) {
        // follow
        currentUrl = new URL(loc, currentUrl).href;
        continue;
      }
      // Not a redirect — print body if it's small/text
      if (ct && (ct.includes('text') || ct.includes('json') || ct.includes('xml') || ct.includes('m3u'))) {
        const body = await res.text();
        console.log('    body (first 1000): ' + body.slice(0, 1000));
      }
      return currentUrl;
    } catch (e) {
      console.log('    error: ' + e.message);
      return null;
    }
  }
  return currentUrl;
}

(async () => {
  console.log('===== PAYLOAD DECODE =====');
  // The payload is base64url of JSON + a literal filename (separated by '?'?)
  // The user said the payload decodes to: {"t":"movie","i":"tt37287335",...}?Obsession...mkv
  // So the base64 part stops at '?'
  const qMark = PAYLOAD_RAW.indexOf('?');
  const b64Part = qMark > 0 ? PAYLOAD_RAW.slice(0, qMark) : PAYLOAD_RAW;
  const suffix = qMark > 0 ? PAYLOAD_RAW.slice(qMark) : '';
  console.log('Base64 part: ' + b64Part);
  console.log('Suffix: ' + suffix);
  const decoded = b64urlDecode(b64Part);
  console.log('Decoded JSON: ' + decoded);
  console.log('Full decoded: ' + decoded + suffix);

  // Try with /direct/experimental/ endpoint
  const expUrl = 'https://pengu.uk/direct/experimental/' + PAYLOAD_RAW;
  console.log('\n===== PROBE PENGU /direct/experimental/ =====');
  // 1. First do no-redirect probe to see what it returns at top
  const top = await probe(expUrl, 'PenguPlay /direct/experimental/');
  
  // 2. Now do full follow — it might 307 to a real backend
  const finalUrl = await followWithHeaders(expUrl, 'PenguPlay /direct/experimental/ (follow)');
  console.log('\n===== FINAL URL =====');
  console.log('Final URL: ' + finalUrl);
})();
