"""
Antova (AniLibria / AniLiberty) Scraper
========================================
Antova = the AniLibria anime dub group's app. Backend API moved from
api.anilibria.tv (now 410 Gone) -> anilibria.top/api/v1/ (rebranded "AniLiberty").

NOTE: The new API NO LONGER exposes myanimelist_id / anilist_id fields, so MAL/AniList
must be mapped by fuzzy title match (we use RapidFuzz).

Tested live 2026-08-10. All endpoints return 200 without auth.

Stream URL pattern:
  https://cache.libria.fun/videos/media/ts/{releaseId}/{episode}/{quality}/{md5hash}.m3u8
  ?countryIso={XX}&isAuthorized=0&isWithVideoAds=1&isWithVideoAdsAlways=1

Install:
  pip install httpx rapidfuzz

Usage:
  from antova_scraper import AntovaScraper
  s = AntovaScraper()
  # Find by anime name
  results = s.search("berserk")
  # Get streams for a specific release alias
  streams = s.get_movie_streams("berserk-2016")              # movie/special
  streams = s.get_series_streams("berserk-2016")             # multi-episode
  # Map from a MAL ID (fuzzy match on title fetched from MAL — implement separately)
"""

from __future__ import annotations
import re
import httpx
from dataclasses import dataclass
from typing import Optional
from rapidfuzz import fuzz

API_BASE = "https://anilibria.top/api/v1"


@dataclass
class AntovaStream:
    """A single playable HLS stream."""
    release_alias: str        # e.g. "berserk-2016"
    release_id: int           # internal numeric ID
    episode: int              # 1-based episode ordinal
    quality: str              # "480" | "720" | "1080"
    url: str                  # full .m3u8 URL (already tokenized server-side)
    duration_sec: Optional[int]


class AntovaScraper:
    """
    Scraper for AniLibria (AniLiberty) — the upstream source for the
    PenguPlay "Antova" provider.
    """

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
            },
        )

    # ----------------------------------------------------------------- #
    # Public API
    # ----------------------------------------------------------------- #

    def search(self, query: str, limit: int = 20) -> list[dict]:
        """
        Search releases by free-text query.
        Returns list of release dicts: {id, alias, name{main,english,alternative},
        year, poster{src}, ...}
        """
        r = self.client.get(
            "/app/search/releases",
            params={"query": query, "limit": limit},
        )
        r.raise_for_status()
        data = r.json()
        # API returns either a bare list or {"data":[...]}
        if isinstance(data, dict) and "data" in data:
            return data["data"]
        return data or []

    def get_release(self, alias_or_id: str | int) -> dict:
        """
        Fetch a single release by alias (e.g. "berserk-2016") or numeric ID.
        Returns full release incl. episodes[] with hls_480/720/1080 URLs.
        """
        r = self.client.get(f"/anime/releases/{alias_or_id}")
        r.raise_for_status()
        return r.json()

    def get_latest(self, limit: int = 20) -> list[dict]:
        """Get the latest releases (no episodes[] expanded — use get_release)."""
        r = self.client.get("/anime/releases/latest", params={"limit": limit})
        r.raise_for_status()
        return r.json()

    def get_schedule(self) -> list[dict]:
        """Weekly schedule. Each item: {release, full_season_is_released, ...}"""
        r = self.client.get("/anime/schedule/week")
        r.raise_for_status()
        return r.json()

    # ----------------------------------------------------------------- #
    # Stremio-style high-level helpers
    # ----------------------------------------------------------------- #

    def get_series_streams(self, alias_or_id: str | int) -> list[AntovaStream]:
        """
        Get all episode streams for a series.
        Works for any release regardless of type (movie / TV / OVA).
        """
        rel = self.get_release(alias_or_id)
        out: list[AntovaStream] = []
        for ep in rel.get("episodes", []):
            ordinal = ep.get("ordinal") or ep.get("sort_order") or 0
            duration = ep.get("duration")
            for quality_key in ("hls_1080", "hls_720", "hls_480"):
                url = ep.get(quality_key)
                if not url:
                    continue
                out.append(
                    AntovaStream(
                        release_alias=rel.get("alias", str(alias_or_id)),
                        release_id=rel.get("id", 0),
                        episode=int(ordinal),
                        quality=quality_key.split("_")[1],
                        url=url,
                        duration_sec=int(duration) if duration else None,
                    )
                )
        return out

    def get_movie_streams(self, alias_or_id: str | int) -> list[AntovaStream]:
        """Alias for get_series_streams — AniLibria treats movies as 1-episode releases."""
        return self.get_series_streams(alias_or_id)

    def get_streams_for_episode(
        self, alias_or_id: str | int, episode: int
    ) -> list[AntovaStream]:
        """Get streams for a specific episode of a series."""
        all_streams = self.get_series_streams(alias_or_id)
        return [s for s in all_streams if s.episode == episode]

    # ----------------------------------------------------------------- #
    # MAL / AniList -> AniLibria mapping
    # ----------------------------------------------------------------- #

    def find_by_title(self, title: str, year: Optional[int] = None) -> Optional[dict]:
        """
        Fuzzy-match a title to an AniLibria release.
        Returns the best matching release dict, or None.
        """
        # AniLibria search is keyword-based; use the cleaned title.
        cleaned = re.sub(r"\s*\(\d{4}\)\s*$", "", title).strip()
        results = self.search(cleaned, limit=20)
        if not results:
            return None

        # Strip year/parenthetical suffixes from the search title
        clean_title = re.sub(r"\s*[\(\[]\d{4}[\)\]]\s*$", "", title).strip()
        clean_title = re.sub(r"\s*:\s*.*$", "", clean_title).strip()  # drop subtitles

        best, best_score = None, 0.0
        for r in results:
            names = r.get("name", {}) or {}
            candidates = [
                names.get("main"),
                names.get("english"),
                (names.get("alternative") or "").split("/")[0],
            ]
            candidates = [c for c in candidates if c]
            if not candidates:
                continue
            # Strip year suffixes from candidate names too ("Berserk (2016)" -> "Berserk")
            stripped = [re.sub(r"\s*[\(\[]\d{4}[\)\]]\s*$", "", c).strip() for c in candidates]
            score = max(
                fuzz.ratio(clean_title.lower(), c.lower()) for c in stripped
            )
            # Year bonus: +15 if year matches (acts as a strong disambiguator)
            if year and r.get("year") == year:
                score += 15
            elif year and r.get("year") and r["year"] != year:
                score -= 10
            if score > best_score:
                best, best_score = r, score

        # Threshold — 60+ is a confident match (Berserk vs Berserk = 66.7)
        if best_score >= 60:
            return best
        return None

    def get_streams_by_mal_id(
        self, mal_id: int, mal_title: str, year: Optional[int] = None
    ) -> list[AntovaStream]:
        """
        Resolve MAL anime ID -> AniLibria streams.

        Because the new AniLiberty API dropped myanimelist_id, we do fuzzy
        title matching. Caller must pass the canonical MAL title (and year
        if known) for accuracy.
        """
        match = self.find_by_title(mal_title, year=year)
        if not match:
            return []
        return self.get_series_streams(match["alias"])

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
    import json

    with AntovaScraper() as s:
        print("=== Search: berserk ===")
        results = s.search("berserk", limit=5)
        for r in results[:5]:
            print(
                f"  - {r.get('alias')}  ({r.get('year')})  "
                f"{(r.get('name') or {}).get('main')}"
            )

        if results:
            alias = results[0]["alias"]
            print(f"\n=== Streams for {alias} ===")
            streams = s.get_series_streams(alias)
            for st in streams[:6]:
                print(f"  EP{st.episode} {st.quality}p  {st.url[:100]}...")

        print("\n=== Title fuzzy match: 'Berserk' (year=2016) ===")
        m = s.find_by_title("Berserk", year=2016)
        if m:
            print(f"  matched: {m.get('alias')}  ({m.get('year')})")
        else:
            print("  no match")

        print("\n=== Latest 3 releases ===")
        for r in s.get_latest(limit=3):
            print(f"  - {r.get('alias')}  ({r.get('year')})")
