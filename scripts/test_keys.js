const reanime = require('/home/z/my-project/scripts/reanime_all_in_one.js');

(async () => {
  const streams = await reanime.getStreams('71014', 'tv', '1', '1');
  const s = streams[0];
  const masterProxyUrl = `http://localhost:7654/?url=${encodeURIComponent(s.url)}&key=${encodeURIComponent(s.reanime.xorKeyB64)}`;
  
  const resp = await fetch(masterProxyUrl);
  const master = await resp.text();
  console.log('=== Master.m3u8 ===');
  console.log(master);
  
  const variantMatch = master.match(/http:\/\/localhost:7654\/\?url=[^\n"]+/);
  if (variantMatch) {
    const vResp = await fetch(variantMatch[0]);
    const variant = await vResp.text();
    console.log('\n=== Variant playlist (first 1500 chars) ===');
    console.log(variant.slice(0, 1500));
    
    const keyLines = variant.split('\n').filter(l => l.startsWith('#EXT-X-KEY'));
    console.log('\n=== KEY lines (' + keyLines.length + ') ===');
    for (const k of keyLines) console.log(k);
  }
})();
