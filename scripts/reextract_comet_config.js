// Re-probe pengu.uk/direct/experimental/ to extract the LATEST Comet config.
// Compare to what we have hardcoded.
'use strict';

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36';

(async () => {
  console.log('===== RE-EXTRACTING PENGUPLAY COMET CONFIG =====');
  // The same payload we always use for "Obsession 2025"
  const payload = 'eyJ0IjoibW92aWUiLCJpIjoidHQzNzI4NzMzNSIsInMiOm51bGwsImUiOm51bGwsImgiOiIxOGQ4Mjk1YjNhMzk0YTUxZTZlN2JlZDZiM2ZiNDIxZDE1ZDBjNWZmIiwiZiI6MH0/Obsession.2025.2160p.UHD.BluRay.REMUX.DV.HDR.10bit...mkv';
  const url = 'https://pengu.uk/direct/experimental/' + payload;
  console.log('GET ' + url.slice(0, 120) + '...');
  
  const res = await fetch(url, {
    redirect: 'manual',
    headers: { 'User-Agent': UA },
    signal: AbortSignal.timeout(20000),
  });
  console.log('Status: ' + res.status);
  const loc = res.headers.get('location');
  console.log('Location: ' + (loc || '(none)').slice(0, 400));
  
  if (loc) {
    // Extract base64 config from the comet.feels.legal URL
    const m = loc.match(/comet\.feels\.legal\/([A-Za-z0-9+/=_]+)/);
    if (m) {
      const newB64 = m[1];
      const decoded = Buffer.from(newB64, 'base64').toString('utf8');
      console.log('\n===== NEW COMET CONFIG (decoded) =====');
      console.log(decoded);
      try {
        const cfg = JSON.parse(decoded);
        console.log('\n===== PRETTY =====');
        console.log(JSON.stringify(cfg, null, 2));
        
        // Compare to what we have
        console.log('\n===== KEY COMPARISON =====');
        const newKey = cfg.debridServices && cfg.debridServices[0] && cfg.debridServices[0].apiKey;
        const ourKey = 'ef869d91-98b8-4d1c-81a5-0d53e259b179';
        const oldPenguKey = '70f3c689-821f-4a9c-8277-198518d99bc8';
        console.log('Old PenguPlay key: ' + oldPenguKey);
        console.log('Our user key:      ' + ourKey);
        console.log('New PenguPlay key: ' + newKey);
        if (newKey === oldPenguKey) console.log('  → PenguPlay is STILL using the old key (so the old key still works on Comet)');
        else console.log('  → PenguPlay has ROTATED the key! New key: ' + newKey);
      } catch (e) {
        console.log('Parse error: ' + e.message);
      }
    }
  }
})();
