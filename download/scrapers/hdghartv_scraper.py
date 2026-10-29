"""
HDGharTV Scraper
================
The real web app is at https://hdghartv.cc (NOT hdghartv.com.pk, which is a
static APK landing page that redirects desktop users to hdghartv.cc).

Public REST API at https://hdghartv.cc/api/ — NO AUTH REQUIRED.

Endpoints:
  GET /api/search?q={query}                       -> {movies[], series[], actors[], total, ...}
  GET /api/movies/public/{_id}                    -> movie detail w/ streamingLinks[]
  GET /api/series/public/{_id}                    -> series detail w/ seasons[].episodes[]
  GET /api/casts/public/{_id}                     -> actor/crew detail
  GET /api/featured/public/{_id}                  -> featured carousel item

streamingLinks[] schema:
  {
    "quality":  "480p" | "720p" | "1080p",
    "url":      "https://cdn6.streamraiwind.stream/img/{hash}/nasty.m3u8?token=...&expires=...",
    "language": "",
    "headers":  "",            # JSON string of custom headers, usually empty
    "userAgent":"",
    "drm":      false,
    "embed":    false,
    "hlsPhpFormat": false,
    "type":     "hls",
    "isActive": true
  }

The m3u8 URL has a signed `token` + `expires` — play it immediately after fetch
(default TTL appears to be ~1 hour). No special Referer/Origin needed.

Install:
  pip install httpx

Usage:
  from hdghartv_scraper import HDGharTVScraper
  s = HDGharTVScraper()
  streams = s.get_movie_streams(tmdb_id=693134)        # by TMDB ID
  streams = s.get_series_streams(tmdb_id=1399, s=1, e=1)  # Breaking Bad S01E01
"""

from __future__ import annotations
import httpx
from dataclasses import dataclass
from typing import Optional

API_BASE = "https://hdghartv.cc/api"


@dataclass
class HDGharStream:
    """A single playable stream from HDGharTV."""
    title: str
    quality: str            # "480p" | "720p" | "1080p"
    url: str                # m3u8 (signed)
    language: str
    type: str               # "hls" usually
    source: str             # "hdghartv-movie" | "hdghartv-series"
    season: Optional[int]   # for series
    episode: Optional[int]  # for series


class HDGharTVScraper:
    """Scraper for HDGharTV — Indian/regional movies, web series, IPTV."""

    def __init__(self, timeout: int = 15):
        self.client = httpx.Client(
            base_url=API_BASE,
            timeout=timeout,
            headers={
                "User-Agent": (
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                    "AppleWebKit/537.36 (KHTML, like Gecko) "
                    "Chrome/120.0.0.0 Safari/537.36"
                ),
                "Accept": "application/json",
                "Referer": "https://hdghartv.cc/",
            },
        )

    # ----------------------------------------------------------------- #
    # Search
    # ----------------------------------------------------------------- #

    def search(self, query: str) -> dict:
        """
        Free-text search across movies + series + actors.
        Returns {movies[], series[], actors[], total, combined, page, totalPages}.
        """
        r = self.client.get("/search", params={"q": query})
        r.raise_for_status()
        return r.json()

    def _find_by_tmdb(
        self, tmdb_id: int, content_type: str
    ) -> Optional[dict]:
        """
        Find a movie or series by TMDB ID.
        HDGharTV's search doesn't expose /api/movies/public/{tmdbId} (500 error),
        so we search by title instead. For best results, fetch the title from
        TMDB first (caller's responsibility) and pass it to search().
        """
        # This is a fallback — caller should pass a title to search_by_title()
        return None

    def find_by_title(
        self, title: str, year: Optional[int] = None
    ) -> tuple[Optional[dict], Optional[dict]]:
        """
        Search by title; returns (movie_match, series_match).
        Each match is a search-result item with `_id` and `tmdbId`.
        Year is used to disambiguate if provided.
        """
        data = self.search(title)
        movies = data.get("movies", [])
        series = data.get("series", [])

        def pick(items):
            if not items:
                return None
            if year is None:
                return items[0]
            # Try to match release year if present
            for it in items:
                rd = it.get("releaseDate") or it.get("airDate") or ""
                if rd.startswith(str(year)):
                    return it
            return items[0]

        return pick(movies), pick(series)

    # ----------------------------------------------------------------- #
    # Detail endpoints
    # ----------------------------------------------------------------- #

    def get_movie(self, _id: str) -> dict:
        """GET /api/movies/public/{_id} — full movie detail with streamingLinks[]."""
        r = self.client.get(f"/movies/public/{_id}")
        r.raise_for_status()
        return r.json()

    def get_series(self, _id: str) -> dict:
        """GET /api/series/public/{_id} — full series detail with seasons[].episodes[]."""
        r = self.client.get(f"/series/public/{_id}")
        r.raise_for_status()
        return r.json()

    # ----------------------------------------------------------------- #
    # High-level stream getters
    # ----------------------------------------------------------------- #

    def get_movie_streams(
        self, title: str, year: Optional[int] = None
    ) -> list[HDGharStream]:
        """
        Resolve a movie by title (and optional year) -> list of streams.
        Returns [] if no match found.
        """
        movie_match, _ = self.find_by_title(title, year=year)
        if not movie_match:
            return []
        detail = self.get_movie(movie_match["_id"])
        out: list[HDGharStream] = []
        for link in detail.get("streamingLinks", []):
            if not link.get("isActive", True):
                continue
            if not link.get("url"):
                continue
            out.append(
                HDGharStream(
                    title=detail.get("title", title),
                    quality=link.get("quality", "unknown"),
                    url=link["url"],
                    language=link.get("language", "") or "",
                    type=link.get("type", "hls"),
                    source="hdghartv-movie",
                    season=None,
                    episode=None,
                )
            )
        return out

    def get_series_streams(
        self,
        title: str,
        season: int,
        episode: int,
        year: Optional[int] = None,
    ) -> list[HDGharStream]:
        """
        Resolve a specific episode of a series by title + season + episode.
        Returns [] if no match found.
        """
        _, series_match = self.find_by_title(title, year=year)
        if not series_match:
            return []
        detail = self.get_series(series_match["_id"])

        out: list[HDGharStream] = []
        for s in detail.get("seasons", []):
            if s.get("seasonNumber") != season:
                continue
            for ep in s.get("episodes", []):
                if ep.get("episodeNumber") != episode:
                    continue
                for link in ep.get("streamingLinks", []):
                    if not link.get("isActive", True):
                        continue
                    if not link.get("url"):
                        continue
                    out.append(
                        HDGharStream(
                            title=detail.get("title", title),
                            quality=link.get("quality", "unknown"),
                            url=link["url"],
                            language=link.get("language", "") or "",
                            type=link.get("type", "hls"),
                            source="hdghartv-series",
                            season=season,
                            episode=episode,
                        )
                    )
        return out

    # ----------------------------------------------------------------- #
    # TMDB-aware resolver (recommended for Stremio addons)
    # ----------------------------------------------------------------- #

    def get_movie_streams_by_tmdb(
        self, tmdb_id: int, tmdb_title: str, tmdb_year: Optional[int] = None
    ) -> list[HDGharStream]:
        """
        Stremio passes TMDB IDs. HDGharTV's /search returns tmdbId on each item,
        so we can search by title and then verify the tmdbId matches.
        """
        data = self.search(tmdb_title)
        for m in data.get("movies", []):
            if m.get("tmdbId") == tmdb_id:
                detail = self.get_movie(m["_id"])
                return [
                    HDGharStream(
                        title=detail.get("title", tmdb_title),
                        quality=link.get("quality", "unknown"),
                        url=link["url"],
                        language=link.get("language", "") or "",
                        type=link.get("type", "hls"),
                        source="hdghartv-movie",
                        season=None,
                        episode=None,
                    )
                    for link in detail.get("streamingLinks", [])
                    if link.get("isActive", True) and link.get("url")
                ]
        # Fallback to title-only match
        return self.get_movie_streams(tmdb_title, year=tmdb_year)

    def get_series_streams_by_tmdb(
        self,
        tmdb_id: int,
        tmdb_title: str,
        season: int,
        episode: int,
    ) -> list[HDGharStream]:
        """Resolve series episode by TMDB ID + season + episode."""
        data = self.search(tmdb_title)
        for s_item in data.get("series", []):
            if s_item.get("tmdbId") == tmdb_id:
                detail = self.get_series(s_item["_id"])
                for s in detail.get("seasons", []):
                    if s.get("seasonNumber") != season:
                        continue
                    for ep in s.get("episodes", []):
                        if ep.get("episodeNumber") != episode:
                            continue
                        return [
                            HDGharStream(
                                title=detail.get("title", tmdb_title),
                                quality=link.get("quality", "unknown"),
                                url=link["url"],
                                language=link.get("language", "") or "",
                                type=link.get("type", "hls"),
                                source="hdghartv-series",
                                season=season,
                                episode=episode,
                            )
                            for link in ep.get("streamingLinks", [])
                            if link.get("isActive", True) and link.get("url")
                        ]
        # Fallback to title-only
        return self.get_series_streams(tmdb_title, season, episode)

    # ----------------------------------------------------------------- #
    # Cleanup
    # ----------------------------------------------------------------- #

    def close(self):
        self.client.close()

    def __enter__(self):
        return self

    def __exit__(self, *args):
        self.close()


# --------------------------------------------------------------------- #
# Demo
# --------------------------------------------------------------------- #
if __name__ == "__main__":
    with HDGharTVScraper() as s:
        print("=== Search: dune ===")
        data = s.search("dune")
        print(f"  total movies: {len(data.get('movies', []))}")
        print(f"  total series: {len(data.get('series', []))}")

        print("\n=== Movie streams: 'Dune Part Two' ===")
        streams = s.get_movie_streams("Dune Part Two", year=2024)
        for st in streams:
            print(f"  [{st.quality}]  {st.url[:100]}...")

        print("\n=== Series streams: 'Breaking Bad' S01E01 ===")
        streams = s.get_series_streams("Breaking Bad", season=1, episode=1)
        for st in streams:
            print(f"  [{st.quality}]  {st.url[:100]}...")
