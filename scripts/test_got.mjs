import { gotScraping } from 'got-scraping';

const response = await gotScraping({
  url: 'https://1embed.cc/download/movie/155',
  headerGeneratorOptions: {
    browsers: ['chrome'],
    devices: ['mobile'],
    operatingSystems: ['android'],
  },
  http2: true,
  timeout: { request: 10000 },
});
console.log('Status:', response.statusCode);
console.log('Is CF:', response.body.includes('Just a moment'));
console.log('Size:', response.body.length);
