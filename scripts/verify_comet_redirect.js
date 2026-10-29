// Verify the Comet playback URL actually redirects to a TorBox HTTPS CDN URL via GET.
// HEAD returns 405 because the route only allows GET (player does GET, not HEAD).
'use strict';

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';

const PENGUPLAY_COMET_B64 = 'eyJtYXhSZXN1bHRzUGVyUmVzb2x1dGlvbiI6MCwibWF4U2l6ZSI6MCwiY2FjaGVkT25seSI6dHJ1ZSwic29ydENhY2hlZFVuY2FjaGVkVG9nZXRoZXIiOmZhbHNlLCJyZW1vdmVUcmFzaCI6dHJ1ZSwicmVzdWx0Rm9ybWF0IjpbImFsbCJdLCJlbmFibGVUb3JyZW50IjpmYWxzZSwiZGVkdXBsaWNhdGVTdHJlYW1zIjp0cnVlLCJzY3JhcGVEZWJyaWRBY2NvdW50VG9ycmVudHMiOmZhbHNlLCJkZWJyaWRTdHJlYW1Qcm94eVBhc3N3b3JkIjoiIiwibGFuZ3VhZ2VlcyI6eyJyZXF1aXJlZCI6W10sImFsbG93ZWQiOltdLCJleGNsdWRlIjpbXSwicHJlZmVycmVkIjpbXX0sInJlc29sdXRpb25zIjp7fSwib3B0aW9ucyI6eyJyZW1vdmVfcmFua3NfdW5kZXIiOi0xMDAwMDAwMDAwMCwiYWxsb3dfZW5nbGlzaF9pbl9sYW5ndWFnZXMiOmZhbHNlLCJyZW1vdmVfdW5rbm93bl9sYW5ndWFnZXMiOmZhbHNlfSwiZGVicmlkU2VydmljZXMiOlt7InNlcnZpY2UiOiJ0b3Jib3giLCJhcGlLZXkiOiI3MGYzYzY4OS04MjFmLTRhOWMtODI3Ny0xOTg1MThkOTliYzgifV19';

const COMET = 'https://comet.feels.legal/' + PENGUPLAY_COMET_B64;

async function followGet(url, hops = 5) {
  console.log('  -- following: ' + url.slice(0, 180) + (url.length > 180 ? '...' : ''));
  let cur = url;
  for (let i = 0; i < hops; i++) {
    const r = await fetch(cur, {
      method: 'GET',
      redirect: 'manual',
      headers: { 'User-Agent': UA, 'Range': 'bytes=0-1' }, // ask for 1 byte so video doesn't download
      signal: AbortSignal.timeout(15000),
    });
    const ct = r.headers.get('content-type');
    const cl = r.headers.get('content-length');
    const ar = r.headers.get('accept-ranges');
    const cr = r.headers.get('content-range');
    const loc = r.headers.get('location');
    const cd = r.headers.get('content-disposition');
    console.log('  [' + i + '] ' + r.status + ' ' + r.statusText +
      ' | ct=' + ct + ' | cl=' + cl + ' | accept-ranges=' + ar + ' | content-range=' + cr +
      ' | disposition=' + (cd || '').slice(0, 80));
    if (loc) {
      console.log('       Location: ' + loc.slice(0, 200));
      cur = new URL(loc, cur).href;
      continue;
    }
    return cur;
  }
  return cur;
}

async function testStreamsFor(imdbId, type, label, season, episode) {
  console.log('\n========== ' + label + ' ==========');
  let path;
  if (type === 'movie') {
    path = '/stream/movie/' + imdbId + '.json';
  } else {
    path = '/stream/series/' + imdbId + ':' + season + ':' + episode + '.json';
  }
  const url = COMET + path;
  console.log('GET ' + url.slice(0, 200) + (url.length > 200 ? '...' : ''));
  
  const res = await fetch(url, {
    headers: { 'User-Agent': UA, 'Accept': 'application/json' },
    signal: AbortSignal.timeout(25000),
  });
  console.log('Status: ' + res.status + ' ' + res.statusText);
  if (!res.ok) {
    console.log('Body: ' + (await res.text()).slice(0, 500));
    return;
  }
  const data = await res.json();
  const streams = data.streams || [];
  console.log('Total streams: ' + streams.length);
  
  // Count by resolution
  const byRes = {};
  streams.forEach(s => {
    const m = (s.name || '').match(/(\d{3,4}p)/);
    const r = m ? m[1] : '?';
    byRes[r] = (byRes[r] || 0) + 1;
  });
  console.log('By resolution: ' + JSON.stringify(byRes));
  
  // Pick the first 2160p stream and verify it 302s to a real CDN URL
  const top = streams.find(s => /2160p|4k/i.test(s.name || '')) || streams[0];
  if (!top) {
    console.log('No streams found');
    return;
  }
  console.log('\n  Picked stream: ' + top.name);
  console.log('  URL: ' + (top.url || '').slice(0, 250));
  if (top.behaviorHints && top.behaviorHints.filename) {
    console.log('  Filename: ' + top.behaviorHints.filename);
  }
  if (top.behaviorHints && top.behaviorHints.bingeGroup) {
    console.log('  BingeGroup: ' + top.behaviorHints.bingeGroup);
  }
  await followGet(top.url);
}

(async () => {
  // 1. Movie the user asked about
  await testStreamsFor('tt37287335', 'movie', 'Obsession 2025 (movie)');
  
  // 2. Classic movie
  await testStreamsFor('tt1375666', 'movie', 'Inception 2010 (movie)');
  
  // 3. TV show — Breaking Bad S01E01
  await testStreamsFor('tt0903747', 'series', 'Breaking Bad S01E01', 1, 1);
  
  // 4. TV show — Game of Thrones S01E01
  await testStreamsFor('tt0944947', 'series', 'Game of Thrones S01E01', 1, 1);
})();
