var gsHelper = require('./got_scraping_helper');

(async () => {
  // Step 1: Get the gemma page
  var html = await gsHelper.httpGet('https://gemma416okl.com/play/tt1375666', {
    headers: { 'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36' }
  });
  console.log('Gemma page size:', html.length);

  // Extract file and key
  var fileMatch = html.match(/"file":"([^"]+)"/);
  var keyMatch = html.match(/"key":"([^"]+)"/);
  if (!fileMatch || !keyMatch) { console.log('no match'); process.exit(0); }
  var file = fileMatch[1].replace(/\\\//g, '/');
  var key = keyMatch[1];
  console.log('File URL:', file.slice(0, 100));
  console.log('Key:', key.slice(0, 50));

  // Step 2: POST to the playlist URL
  var playlist = await gsHelper.httpGet(file, {
    method: 'POST',
    headers: {
      'X-CSRF-TOKEN': key,
      'Content-Type': 'application/x-www-form-urlencoded',
      'Origin': 'https://gemma416okl.com',
      'Referer': 'https://gemma416okl.com/play/tt1375666'
    }
  });
  console.log('');
  console.log('Playlist response length:', playlist.length);
  console.log('Playlist head:', playlist.slice(0, 800));

  // Parse the JSON
  try {
    var data = JSON.parse(playlist);
    console.log('');
    console.log('Parsed JSON. Top-level type:', Array.isArray(data) ? 'array' : typeof data);
    if (Array.isArray(data)) {
      console.log('Array length:', data.length);
      data.forEach(function(item, i) {
        console.log('  Item', i, ':', JSON.stringify(item).slice(0, 300));
      });
    } else {
      console.log('Keys:', Object.keys(data));
    }
  } catch(e) {
    console.log('JSON parse error:', e.message);
  }
  process.exit(0);
})();
