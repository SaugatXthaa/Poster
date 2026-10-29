// Test got-scraping for CF bypass
const { gotScraping } = require('got-scraping');

(async () => {
  console.log('Testing got-scraping on mkvbase.site...');
  
  try {
    const response = await gotScraping({
      url: 'https://mkvbase.site/',
      headerGeneratorOptions: {
        browsers: ['chrome'],
        devices: ['desktop'],
        operatingSystems: ['linux'],
      },
      http2: true,
      timeout: { request: 15000 },
    });
    
    console.log('Status:', response.statusCode);
    console.log('Body length:', response.body.length);
    console.log('Is CF challenge:', response.body.includes('Just a moment'));
    console.log('Is blocked:', response.body.includes('blocked'));
    
    if (!response.body.includes('Just a moment') && !response.body.includes('blocked')) {
      console.log('\n=== SUCCESS! Got real content ===');
      console.log('Title:', response.body.match(/<title>([^<]*)<\/title>/)?.[1]);
      console.log('First 500 chars:', response.body.substring(0, 500));
    } else {
      console.log('\n=== Still CF challenge ===');
      console.log('First 200 chars:', response.body.substring(0, 200));
    }
  } catch(e) {
    console.log('Error:', e.message);
  }
})();
