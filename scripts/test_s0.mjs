import { gotScraping } from 'got-scraping';

const response = await gotScraping({
  url: 'https://s0-greate.net/go/1475978',
  headerGeneratorOptions: {
    browsers: ['chrome'],
    devices: ['desktop'],
    operatingSystems: ['linux'],
  },
  http2: true,
  timeout: { request: 10000 },
  headers: {
    'Referer': 'https://teknoasian.com/',
  },
});

console.log('Status:', response.statusCode);
console.log('Size:', response.body.length);
console.log('Content:', response.body.substring(0, 300));
