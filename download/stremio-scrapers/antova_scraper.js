/**
 * Antova (AniLibria / AniLiberty) Scraper — Node.js
 * ================================================
 * Antova = the AniLibria anime dub group's app. Backend API moved from
 * api.anilibria.tv (now 410 Gone) -> anilibria.top/api/v1/ (rebranded "AniLiberty").
 *
 * NOTE: The new API NO LONGER exposes myanimelist_id / anilist_id fields, so MAL/AniList
 * must be mapped by fuzzy title match (we use a simple Levenshtein-style ratio).
 *
 * Tested live 2026-08-10. All endpoints return 200 without auth.
 *
 * Stream URL pattern:
 *   https://cache.libria.fun/videos/media/ts/{releaseId}/{episode}/{quality}/{md5hash}.m3u8
 *   ?countryIso={XX}&isAuthorized=0&isWithVideoAds=1&isWithVideoAdsAlways=1
 *
 * Install:
 *   npm install axios
 *
 * Usage:
 *   node antova_scraper.js
 */

const axios = require('axios');

const API_BASE = 'https://anilibria.top/api/v1';

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 ' +
           '(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36';

class AntovaScraper {
  constructor(timeout = 15000) {
    this.client = axios.create({
      baseURL: API_BASE,
      timeout,
      headers: {
        'User-Agent': UA,
        'Accept': 'application/json',
        'Accept-Language': 'en-US,en;q=0.9',
      },
    });
  }

  /**
   * Search releases by free-text query.
   * Returns array of release objects: {id, alias, name{main,english,alternative}, year, ...}
   */
  async search(query, limit = 20) {
    const r = await this.client.get('/app/search/releases', {
      params: { query, limit },
    });
    const data = r.data;
    if (Array.isArray(data)) return data;
    if (data && Array.isArray(data.data)) return data.data;
    return [];
  }

  /**
   * Fetch a single release by alias (e.g. "berserk-2016") or numeric ID.
   * Returns full release incl. episodes[] with hls_480/720/1080 URLs.
   */
  async getRelease(aliasOrId) {
    const r = await this.client.get(`/anime/releases/${aliasOrId}`);
    return r.data;
  }

  async getLatest(limit = 20) {
    const r = await this.client.get('/anime/releases/latest', { params: { limit } });
    return r.data;
  }

  async getSchedule() {
    const r = await this.client.get('/anime/schedule/week');
    return r.data;
  }

  /**
   * Get all episode streams for a series.
   * Works for any release regardless of type (movie / TV / OVA).
   */
  async getSeriesStreams(aliasOrId) {
    const rel = await this.getRelease(aliasOrId);
    const out = [];
    for (const ep of (rel.episodes || [])) {
      const ordinal = ep.ordinal || ep.sort_order || 0;
      const duration = ep.duration;
      for (const qKey of ['hls_1080', 'hls_720', 'hls_480']) {
        const url = ep[qKey];
        if (!url) continue;
        out.push({
          releaseAlias: rel.alias || String(aliasOrId),
          releaseId: rel.id || 0,
          episode: Number(ordinal),
          quality: qKey.split('_')[1],
          url,
          durationSec: duration ? Number(duration) : null,
        });
      }
    }
    return out;
  }

  async getMovieStreams(aliasOrId) {
    return this.getSeriesStreams(aliasOrId);
  }

  async getStreamsForEpisode(aliasOrId, episode) {
    const all = await this.getSeriesStreams(aliasOrId);
    return all.filter(s => s.episode === episode);
  }

  /**
   * Fuzzy-match a title to an AniLibria release.
   * Returns the best matching release object, or null.
   */
  async findByTitle(title, year = null) {
    const cleaned = title.replace(/\s*[\(\[]\d{4}[\)\]]\s*$/, '').trim();
    const results = await this.search(cleaned, 20);
    if (!results.length) return null;

    // Strip year suffixes from candidate names too
    const stripYear = (s) => (s || '').replace(/\s*[\(\[]\d{4}[\)\]]\s*$/, '').trim();
    const cleanTitle = cleaned.toLowerCase().replace(/\s*:\s*.*$/, '').trim();

    let best = null, bestScore = 0;
    for (const r of results) {
      const names = r.name || {};
      const candidates = [
        names.main, names.english,
        (names.alternative || '').split('/')[0],
      ].filter(Boolean).map(stripYear);
      if (!candidates.length) continue;
      let score = Math.max(...candidates.map(c =>
        this._fuzzRatio(cleanTitle, c.toLowerCase())
      ));
      if (year && r.year === year) score += 15;
      else if (year && r.year && r.year !== year) score -= 10;
      if (score > bestScore) { best = r; bestScore = score; }
    }
    return bestScore >= 60 ? best : null;
  }

  /**
   * Resolve MAL anime ID -> AniLibria streams via fuzzy title match.
   * Because the new AniLiberty API dropped myanimelist_id, we do fuzzy
   * title matching. Caller must pass the canonical MAL title (and year
   * if known) for accuracy.
   */
  async getStreamsByMalId(malId, malTitle, year = null) {
    const m = await this.findByTitle(malTitle, year);
    if (!m) return [];
    return this.getSeriesStreams(m.alias);
  }

  /**
   * Simple Levenshtein-based similarity ratio (0-100).
   * Equivalent to rapidfuzz.fuzz.ratio for plain ASCII.
   */
  _fuzzRatio(a, b) {
    if (a === b) return 100;
    if (!a.length || !b.length) return 0;
    const dist = this._levenshtein(a, b);
    const maxLen = Math.max(a.length, b.length);
    return Math.round((1 - dist / maxLen) * 100);
  }

  _levenshtein(a, b) {
    const m = a.length, n = b.length;
    const dp = Array.from({ length: m + 1 }, () => new Array(n + 1).fill(0));
    for (let i = 0; i <= m; i++) dp[i][0] = i;
    for (let j = 0; j <= n; j++) dp[0][j] = j;
    for (let i = 1; i <= m; i++) {
      for (let j = 1; j <= n; j++) {
        const cost = a[i - 1] === b[j - 1] ? 0 : 1;
        dp[i][j] = Math.min(
          dp[i - 1][j] + 1,
          dp[i][j - 1] + 1,
          dp[i - 1][j - 1] + cost
        );
      }
    }
    return dp[m][n];
  }
}

// --------------------------------------------------------------------- //
// Demo
// --------------------------------------------------------------------- //
if (require.main === module) (async () => {
  const s = new AntovaScraper();
  try {
    console.log('=== Search: berserk ===');
    const results = await s.search('berserk', 5);
    for (const r of results.slice(0, 5)) {
      console.log(`  - ${r.alias}  (${r.year})  ${(r.name || {}).main}`);
    }

    if (results.length) {
      const alias = results[0].alias;
      console.log(`\n=== Streams for ${alias} ===`);
      const streams = await s.getSeriesStreams(alias);
      for (const st of streams.slice(0, 6)) {
        console.log(`  EP${st.episode} ${st.quality}p  ${st.url.slice(0, 100)}...`);
      }
    }

    console.log('\n=== Title fuzzy match: Berserk (year=2016) ===');
    const m = await s.findByTitle('Berserk', 2016);
    if (m) console.log(`  matched: ${m.alias}  (${m.year})`);
    else console.log('  no match');

    console.log('\n=== Latest 3 releases ===');
    const latest = await s.getLatest(3);
    for (const r of latest) console.log(`  - ${r.alias}  (${r.year})`);
  } catch (e) {
    console.error('Error:', e.message);
    if (e.response) {
      console.error('Status:', e.response.status);
      console.error('Body:', JSON.stringify(e.response.data).slice(0, 300));
    }
  }
})();

module.exports = { AntovaScraper };
