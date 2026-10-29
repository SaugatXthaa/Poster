// Wrapper that delegates to desiflix_all_in_one.js (v3 - backend back online)
// The original nuvio_desiflix.js was obfuscated and pointed at the deprecated
// workers.dev URL. This wrapper uses the new manifest.desitvhub.eu.org endpoint.
const real = require('./desiflix_all_in_one.js');
module.exports = {
  getStreams: real.getStreams,
  getTMDBInfo: real.getTMDBInfo,
  fetchFromApi: real.fetchFromApi,
  BASE_URLS: real.BASE_URLS,
};
