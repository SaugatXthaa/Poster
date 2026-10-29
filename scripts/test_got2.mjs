import { gotScraping } from 'got-scraping';

// Try with different header configurations
const configs = [
  {
    name: 'Chrome Desktop Linux',
    headerGeneratorOptions: { browsers: ['chrome'], devices: ['desktop'], operatingSystems: ['linux'] },
  },
  {
    name: 'Chrome Desktop Windows',
    headerGeneratorOptions: { browsers: ['chrome'], devices: ['desktop'], operatingSystems: ['windows'] },
  },
  {
    name: 'Chrome Mobile Android',
    headerGeneratorOptions: { browsers: ['chrome'], devices: ['mobile'], operatingSystems: ['android'] },
  },
  {
    name: 'Firefox Desktop Linux',
    headerGeneratorOptions: { browsers: ['firefox'], devices: ['desktop'], operatingSystems: ['linux'] },
  },
];

for (const config of configs) {
  try {
    const response = await gotScraping({
      url: 'https://mkvbase.site/',
      headerGeneratorOptions: config.headerGeneratorOptions,
      http2: true,
      timeout: { request: 10000 },
    });
    
    const isCF = response.body.includes('Just a moment');
    const isBlocked = response.body.includes('blocked');
    console.log(`${config.name}: Status ${response.statusCode} | CF=${isCF} | Blocked=${isBlocked} | Size=${response.body.length}`);
    
    if (!isCF && !isBlocked) {
      console.log('  SUCCESS! Got real content!');
      console.log('  Title:', response.body.match(/<title>([^<]*)<\/title>/)?.[1]);
    }
  } catch(e) {
    console.log(`${config.name}: Error - ${e.message.substring(0, 60)}`);
  }
}
