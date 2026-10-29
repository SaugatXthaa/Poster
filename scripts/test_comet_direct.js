// Test Comet directly — does it work the same as PenguPlay's /direct/experimental/?
// This proves we can call comet.feels.legal/<config>/stream/movie/<imdb>.json
// and get HTTPS streams back (via TorBox CDN).
'use strict';

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';

// The exact same config PenguPlay uses (extracted from its 307 redirect)
const PENGUPLAY_COMET_B64 = 'eyJtYXhSZXN1bHRzUGVyUmVzb2x1dGlvbiI6MCwibWF4U2l6ZSI6MCwiY2FjaGVkT25seSI6dHJ1ZSwic29ydENhY2hlZFVuY2FjaGVkVG9nZXRoZXIiOmZhbHNlLCJyZW1vdmVUcmFzaCI6dHJ1ZSwicmVzdWx0Rm9ybWF0IjpbImFsbCJdLCJlbmFibGVUb3JyZW50IjpmYWxzZSwiZGVkdXBsaWNhdGVTdHJlYW1zIjp0cnVlLCJzY3JhcGVEZWJyaWRBY2NvdW50VG9ycmVudHMiOmZhbHNlLCJkZWJyaWRTdHJlYW1Qcm94eVBhc3N3b3JkIjoiIiwibGFuZ3VhZ2VzIjp7InJlcXVpcmVkIjpbXSwiYWxsb3dlZCI6W10sImV4Y2x1ZGUiOltdLCJwcmVmZXJyZWQiOltdfSwicmVzb2x1dGlvbnMiOnt9LCJvcHRpb25zIjp7InJlbW92ZV9yYW5rc191bmRlciI6LTEwMDAwMDAwMDAwLCJhbGxvd19lbmdsaXNoX2luX2xhbmd1YWdlcyI6ZmFsc2UsInJlbW92ZV91bmtub3duX2xhbmd1YWdlcyI6ZmFsc2V9LCJkZWJyaWRTZXJ2aWNlcyI6W3sic2VydmljZSI6InRvcmJveCIsImFwaUtleSI6IjcwZjNjNjg5LTgyMWYtNGE5Yy04Mjc3LTE5ODUxOGQ5OWJjOCJ9XX0=';

const COMET = 'https://comet.feels.legal/' + PENGUPLAY_COMET_B64;

async function testMovie(imdbId, title) {
  console.log('\n===== TEST: ' + title + ' (' + imdbId + ') =====');
  const url = COMET + '/stream/movie/' + imdbId + '.json';
  console.log('GET ' + url);
  try {
    const res = await fetch(url, {
      headers: { 'User-Agent': UA, 'Accept': 'application/json' },
      signal: AbortSignal.timeout(20000),
    });
    console.log('Status: ' + res.status + ' ' + res.statusText);
    if (!res.ok) {
      console.log('Body: ' + (await res.text()).slice(0, 500));
      return;
    }
    const data = await res.json();
    const streams = data.streams || [];
    console.log('Streams returned: ' + streams.length);
    streams.slice(0, 8).forEach((s, i) => {
      console.log('  [' + i + '] ' + (s.name || '(no name)'));
      console.log('      url: ' + (s.url || '').slice(0, 200));
      if (s.behaviorHints) console.log('      behaviorHints: ' + JSON.stringify(s.behaviorHints).slice(0, 150));
    });
    // For the FIRST stream, follow the URL to verify it 302s to a real HTTPS CDN URL.
    if (streams.length > 0) {
      const s0 = streams[0];
      console.log('\n  ----- following stream[0].url -----');
      let cur = s0.url;
      for (let i = 0; i < 5; i++) {
        const r = await fetch(cur, {
          method: 'HEAD',
          redirect: 'manual',
          headers: { 'User-Agent': UA },
          signal: AbortSignal.timeout(15000),
        });
        console.log('  [' + i + '] ' + r.status + ' ' + r.statusText);
        console.log('      content-type: ' + r.headers.get('content-type'));
        console.log('      content-length: ' + r.headers.get('content-length'));
        console.log('      accept-ranges: ' + r.headers.get('accept-ranges'));
        const loc = r.headers.get('location');
        if (loc) {
          console.log('      Location: ' + loc.slice(0, 200));
          cur = new URL(loc, cur).href;
        } else {
          console.log('      ✓ final URL (no more redirects)');
          break;
        }
      }
    }
  } catch (e) {
    console.log('Error: ' + e.message);
  }
}

(async () => {
  // 1. The exact movie the user asked about: Obsession 2025
  await testMovie('tt37287335', 'Obsession (2025)');
  
  // 2. Sanity check with a well-known movie
  await testMovie('tt1375666', 'Inception (2010)');
})();
