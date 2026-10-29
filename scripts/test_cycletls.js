const { default: init, exit } = require('cycletls');

(async () => {
  const cycleTLS = await init();
  try {
    const result = await cycleTLS('https://new2.zinkmovies.mobi/', {
      ja3: '771,4865-4866-4867-49195-49199-49196-49200-52393-52392-49171-49172-156-157-47-53,0-23-65281-10-11-35-16-5-13-18-51-45-43-27-17513,29-23-24,0',
      userAgent: 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36',
      headers: {
        'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
        'Accept-Language': 'en-US,en;q=0.9',
      },
      timeout: 15,
    }, 'get');
    
    console.log('Status:', result.status);
    console.log('Body length:', result.body.length);
    console.log('Is CF challenge:', result.body.includes('Just a moment'));
    console.log('Body preview:', result.body.substring(0, 200));
  } catch(e) {
    console.log('Error:', e.message);
  } finally {
    exit();
  }
})();
