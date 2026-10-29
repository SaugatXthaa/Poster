// Wrapper that delegates to moviesdrive_all_in_one.js (v2 — 4K playable)
const real = require('./moviesdrive_all_in_one.js');
module.exports = {
  getStreams: real.getStreams,
  getTMDBInfo: real.getTMDBInfo,
  searchMoviesdrive: real.searchMoviesdrive,
  MAIN_URL: real.MAIN_URL,
};
