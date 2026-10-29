/**
 * HDGharTV Scraper — Node.js
 * ==========================
 * The real web app is at https://hdghartv.cc (NOT hdghartv.com.pk, which is a
 * static APK landing page that redirects desktop users to hdghartv.cc).
 *
 * Public REST API at https://hdghartv.cc/api/ — NO AUTH REQUIRED.
 *
 * Endpoints:
 *   GET /api/search?q={query}                       -> {movies[], series[], actors[], total, ...}
 *   GET /api/movies/public/{_id}                    -> movie detail w/ streamingLinks[]
 *   GET /api/series/public/{_id}                    -> series detail w/ seasons[].episodes[]
 *   GET /api/casts/public/{_id}                     -> actor/crew detail
 *   GET /api/featured/public/{_id}                  -> featured carousel item
 *
 * streamingLinks[] schema:
 *   {
 *     "quality":  "480p" | "720p" | "1080p",
 *     "url":      "https://cdn6.streamraiwind.stream/img/{hash}/nasty.m3u8?token=...&expires=...",
 *     "language": "",
 *     "headers":  "",            // JSON string of custom headers, usually empty
 *     "userAgent":"",
 *     "drm":      false,
 *     "embed":    false,
 *     "hlsPhpFormat": false,
 *     "type":     "hls",
 *     "isActive": true
 *   }
 *
 * The m3u8 URL has a signed `token` + `expires` — play it immediately after fetch
 * (default TTL ~1 hour). No special Referer/Origin needed.
 *
 * Install:
 *   npm install axios
 *
 * Usage:
 *   node hdghartv_scraper.js
 */

const axios = require('axios');

const API_BASE = 'https://hdghartv.cc/api';
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 ' +
           '(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36';

class HDGharTVScraper {
  constructor(timeout = 15000) {
    this.client = axios.create({
      baseURL: API_BASE,
      timeout,
      headers: {
        'User-Agent': UA,
        'Accept': 'application/json',
        'Accept-Language': 'en-US,en;q=0.9',
        'Referer': 'https://hdghartv.cc/',
      },
    });
  }

  /**
   * Free-text search across movies + series + actors.
   * Returns {movies[], series[], actors[], total, combined, page, totalPages}.
   */
  async search(query) {
    const r = await this.client.get('/search', { params: { q: query } });
    return r.data;
  }

  /**
   * Find a movie or series by title (and optional year for disambiguation).
   * Returns {movie, series} — each is a search-result item or null.
   */
  async findByTitle(title, year = null) {
    const data = await this.search(title);
    const movies = data.movies || [];
    const series = data.series || [];

    const pick = (items) => {
      if (!items.length) return null;
      if (year === null) return items[0];
      for (const it of items) {
        const rd = it.releaseDate || it.airDate || '';
        if (rd.startsWith(String(year))) return it;
      }
      return items[0];
    };

    return { movie: pick(movies), series: pick(series) };
  }

  /** GET /api/movies/public/{_id} — full movie detail with streamingLinks[]. */
  async getMovie(_id) {
    const r = await this.client.get(`/movies/public/${_id}`);
    return r.data;
  }

  /** GET /api/series/public/{_id} — full series detail with seasons[].episodes[]. */
  async getSeries(_id) {
    const r = await this.client.get(`/series/public/${_id}`);
    return r.data;
  }

  /** Resolve a movie by title (and optional year) -> list of streams. */
  async getMovieStreams(title, year = null) {
    const { movie } = await this.findByTitle(title, year);
    if (!movie) return [];
    const detail = await this.getMovie(movie._id);
    return (detail.streamingLinks || [])
      .filter(l => l.isActive !== false && l.url)
      .map(l => ({
        title: detail.title || title,
        quality: l.quality || 'unknown',
        url: l.url,
        language: l.language || '',
        type: l.type || 'hls',
        source: 'hdghartv-movie',
        season: null,
        episode: null,
      }));
  }

  /** Resolve a specific episode of a series by title + season + episode. */
  async getSeriesStreams(title, season, episode, year = null) {
    const { series } = await this.findByTitle(title, year);
    if (!series) return [];
    const detail = await this.getSeries(series._id);
    const out = [];
    for (const s of (detail.seasons || [])) {
      if (s.seasonNumber !== season) continue;
      for (const ep of (s.episodes || [])) {
        if (ep.episodeNumber !== episode) continue;
        for (const l of (ep.streamingLinks || [])) {
          if (l.isActive === false || !l.url) continue;
          out.push({
            title: detail.title || title,
            quality: l.quality || 'unknown',
            url: l.url,
            language: l.language || '',
            type: l.type || 'hls',
            source: 'hdghartv-series',
            season,
            episode,
          });
        }
      }
    }
    return out;
  }

  /**
   * Stremio passes TMDB IDs. HDGharTV's /search returns tmdbId on each item,
   * so we can search by title and then verify the tmdbId matches.
   */
  async getMovieStreamsByTmdb(tmdbId, tmdbTitle, tmdbYear = null) {
    const data = await this.search(tmdbTitle);
    for (const m of (data.movies || [])) {
      if (m.tmdbId === tmdbId) {
        const detail = await this.getMovie(m._id);
        return (detail.streamingLinks || [])
          .filter(l => l.isActive !== false && l.url)
          .map(l => ({
            title: detail.title || tmdbTitle,
            quality: l.quality || 'unknown',
            url: l.url,
            language: l.language || '',
            type: l.type || 'hls',
            source: 'hdghartv-movie',
            season: null,
            episode: null,
          }));
      }
    }
    return this.getMovieStreams(tmdbTitle, tmdbYear);
  }

  async getSeriesStreamsByTmdb(tmdbId, tmdbTitle, season, episode) {
    const data = await this.search(tmdbTitle);
    for (const sItem of (data.series || [])) {
      if (sItem.tmdbId === tmdbId) {
        const detail = await this.getSeries(sItem._id);
        for (const s of (detail.seasons || [])) {
          if (s.seasonNumber !== season) continue;
          for (const ep of (s.episodes || [])) {
            if (ep.episodeNumber !== episode) continue;
            return (ep.streamingLinks || [])
              .filter(l => l.isActive !== false && l.url)
              .map(l => ({
                title: detail.title || tmdbTitle,
                quality: l.quality || 'unknown',
                url: l.url,
                language: l.language || '',
                type: l.type || 'hls',
                source: 'hdghartv-series',
                season,
                episode,
              }));
          }
        }
      }
    }
    return this.getSeriesStreams(tmdbTitle, season, episode);
  }
}

// --------------------------------------------------------------------- //
// Demo
// --------------------------------------------------------------------- //
if (require.main === module) (async () => {
  const s = new HDGharTVScraper();
  try {
    console.log('=== Search: dune ===');
    const data = await s.search('dune');
    console.log(`  total movies: ${(data.movies || []).length}`);
    console.log(`  total series: ${(data.series || []).length}`);

    console.log('\n=== Movie streams: "Dune Part Two" ===');
    const streams = await s.getMovieStreams('Dune Part Two', 2024);
    for (const st of streams) {
      console.log(`  [${st.quality}]  ${st.url.slice(0, 100)}...`);
    }

    console.log('\n=== Series streams: "Breaking Bad" S01E01 ===');
    const seriesStreams = await s.getSeriesStreams('Breaking Bad', 1, 1);
    for (const st of seriesStreams) {
      console.log(`  [${st.quality}]  ${st.url.slice(0, 100)}...`);
    }

    console.log('\n=== TMDB-aware lookup: tmdb 693134 (Dune Part Two) ===');
    const tmdbStreams = await s.getMovieStreamsByTmdb(693134, 'Dune Part Two', 2024);
    for (const st of tmdbStreams) {
      console.log(`  [${st.quality}]  ${st.url.slice(0, 100)}...`);
    }
  } catch (e) {
    console.error('Error:', e.message);
    if (e.response) {
      console.error('Status:', e.response.status);
      console.error('Body:', JSON.stringify(e.response.data).slice(0, 300));
    }
  }
})();

module.exports = { HDGharTVScraper };
