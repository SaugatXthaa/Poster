const reanime = require('/home/z/my-project/scripts/reanime_all_in_one.js');

(async () => {
  console.log('=== Get stream URL + XOR key ===');
  const streams = await reanime.getStreams('71014', 'tv', '1', '1');
  if (streams.length === 0) { console.log('No streams'); process.exit(1); }
  
  const s = streams[0];
  console.log(`Stream: ${s.name}`);
  console.log(`Master URL: ${s.url}`);
  console.log(`XOR key: ${s.reanime.xorKeyB64}`);
  
  // Test 1: Fetch master.m3u8 via proxy
  const masterProxyUrl = `http://localhost:7654/?url=${encodeURIComponent(s.url)}&key=${encodeURIComponent(s.reanime.xorKeyB64)}`;
  console.log('\n=== Test 1: Fetch master.m3u8 via proxy ===');
  const mResp = await fetch(masterProxyUrl);
  const mBody = await mResp.text();
  console.log(`HTTP ${mResp.status}, Content-Type: ${mResp.headers.get('content-type')}, length ${mBody.length}`);
  console.log('Master m3u8 contents:');
  console.log(mBody);
  
  // Test 2: Fetch variant playlist via proxy
  const variantMatch = mBody.match(/http:\/\/localhost:7654\/\?url=[^\n"]+/);
  if (!variantMatch) { console.log('No variant URL in master'); process.exit(1); }
  const variantUrl = variantMatch[0];
  console.log('\n=== Test 2: Fetch variant playlist via proxy ===');
  const vResp = await fetch(variantUrl);
  const vBody = await vResp.text();
  console.log(`HTTP ${vResp.status}, Content-Type: ${vResp.headers.get('content-type')}, length ${vBody.length}`);
  console.log('Variant playlist (first 500 chars):');
  console.log(vBody.slice(0, 500));
  
  // Test 3: Fetch first segment via proxy + verify with ffprobe
  const segMatch = vBody.match(/http:\/\/localhost:7654\/\?url=[^\n"]+/);
  if (!segMatch) { console.log('No segment URL in variant'); process.exit(1); }
  const segUrl = segMatch[0];
  console.log('\n=== Test 3: Fetch first segment via proxy ===');
  const segResp = await fetch(segUrl);
  const segBuf = Buffer.from(await segResp.arrayBuffer());
  console.log(`HTTP ${segResp.status}, Content-Type: ${segResp.headers.get('content-type')}, length ${segBuf.length}`);
  console.log(`First 16 bytes hex: ${segBuf.slice(0, 16).toString('hex')}`);
  console.log(`Sync byte at 0: 0x${segBuf[0].toString(16)} (should be 0x47)`);
  
  // Save the segment for ffprobe
  const fs = require('fs');
  fs.writeFileSync('/tmp/proxy_seg.ts', segBuf);
  console.log('\nSaved segment to /tmp/proxy_seg.ts');
})();
