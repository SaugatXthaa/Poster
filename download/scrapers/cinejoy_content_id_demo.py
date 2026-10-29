"""
Cinejoy Content ID Lookup Demo
================================
Demonstrates how to find the Cinejoy content ID for a given TMDB ID.

Since the TMDB ID -> Cinejoy content ID mapping requires solving a PoW
challenge, we use two practical approaches:

  1. BROWSER_STRATEGY — Use Playwright to load the watch page and intercept
     the .m3u8 URL. Slow but reliable.

  2. CACHED_LOOKUP — Maintain your own cache of TMDB -> content_id mappings
     by periodically running the browser strategy across your catalog.
     Once cached, lookups are instant.

  3. PROXY_MODE — Run a small reverse proxy (see nginx_cinejoy_proxy.conf)
     that handles the Referer injection. Your Stremio addon just returns
     proxy URLs.

Install for browser strategy:
  pip install playwright
  playwright install chromium

Run:
  python3 cinejoy_content_id_demo.py 693134
"""

import re
import sys
import json
import time
from typing import Optional

CINEJOY_ORIGIN = "https://cinejoy.to"
EARTHCDN = "https://help.earthcleaner.cc"


def find_content_id_via_browser(tmdb_id: int, is_movie: bool = True,
                                 season: Optional[int] = None,
                                 episode: Optional[int] = None,
                                 timeout_sec: int = 30) -> Optional[str]:
    """
    Launch Playwright, load the Cinejoy watch page, intercept the .m3u8
    request, and extract the content ID from the URL.

    Returns the numeric content ID (e.g. "9937486") or None on failure.
    """
    try:
        from playwright.sync_api import sync_playwright
    except ImportError:
        print("ERROR: pip install playwright && playwright install chromium", file=sys.stderr)
        return None

    if is_movie:
        url = f"{CINEJOY_ORIGIN}/watch/movie/{tmdb_id}"
    else:
        url = f"{CINEJOY_ORIGIN}/watch/tv/{tmdb_id}/{season}/{episode}"

    captured: Optional[str] = None

    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        ctx = browser.new_context(
            user_agent="Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                       "AppleWebKit/537.36 (KHTML, like Gecko) "
                       "Chrome/120.0.0.0 Safari/537.36",
            extra_http_headers={"Referer": CINEJOY_ORIGIN + "/"},
        )
        page = ctx.new_page()

        def on_request(req):
            nonlocal captured
            # Match: https://help.earthcleaner.cc/playlist/{id}.m3u8
            m = re.search(r"help\.earthcleaner\.cc/playlist/(\d+)\.m3u8", req.url)
            if m and not captured:
                captured = m.group(1)

        page.on("request", on_request)

        try:
            page.goto(url, wait_until="domcontentloaded", timeout=timeout_sec * 1000)
            # Wait up to 30s for the playlist request to fire
            for _ in range(60):
                if captured:
                    break
                page.wait_for_timeout(500)
        except Exception as e:
            print(f"WARN: page navigation error: {e}", file=sys.stderr)
        finally:
            browser.close()

    return captured


def build_stream_urls(content_id: str) -> dict:
    """
    Given a Cinejoy content ID, return all CDN URLs.
    The CDN requires only `Referer: https://cinejoy.to/` header.
    """
    # Split ID into the CDN's directory convention: /000/99/37/9937486/
    # (leading zeros / thousands / hundreds / full ID)
    cid = content_id.zfill(8)  # pad to 8 digits
    thousands = cid[-8:-5].lstrip("0") or "0"   # first 3 digits
    hundreds = cid[-5:-3].lstrip("0") or "0"    # next 2 digits
    path_prefix = f"/hls/000/{thousands.zfill(2)}/{hundreds.zfill(2)}/{content_id}"

    return {
        "master": f"{EARTHCDN}/playlist/{content_id}.m3u8",
        "variants": {
            "1080p": f"{EARTHCDN}/video/{content_id}/video_1080p.m3u8",
            "720p":  f"{EARTHCDN}/video/{content_id}/video_720p.m3u8",
            "360p":  f"{EARTHCDN}/video/{content_id}/video_360p.m3u8",
        },
        "audio": f"{EARTHCDN}{path_prefix}/audio_1.m3u8",
        "segments_pattern": f"{EARTHCDN}/video/{content_id}/video_{{quality}}_{{NNN}}.html",
        "required_headers": {"Referer": "https://cinejoy.to/"},
    }


def demo(tmdb_id: int):
    print(f"=== Looking up Cinejoy content ID for TMDB {tmdb_id} ===")
    t0 = time.time()
    cid = find_content_id_via_browser(tmdb_id, is_movie=True)
    elapsed = time.time() - t0

    if not cid:
        print(f"FAILED after {elapsed:.1f}s — could not capture content ID")
        return

    print(f"Found content ID: {cid}  (in {elapsed:.1f}s)")
    urls = build_stream_urls(cid)
    print(f"\nMaster playlist: {urls['master']}")
    print(f"Required header: {urls['required_headers']}")
    print(f"\nVariants:")
    for q, u in urls["variants"].items():
        print(f"  {q}: {u}")
    print(f"\nAudio: {urls['audio']}")

    # Quick verify: fetch the master playlist
    import httpx
    print(f"\n=== Verifying master playlist ===")
    r = httpx.get(urls["master"],
                  headers={"Referer": "https://cinejoy.to/",
                           "User-Agent": "Mozilla/5.0 ..."})
    print(f"HTTP {r.status_code}, {len(r.text)} bytes")
    if r.status_code == 200:
        # Show first 5 lines
        for line in r.text.splitlines()[:10]:
            print(f"  {line}")


if __name__ == "__main__":
    tmdb = int(sys.argv[1]) if len(sys.argv) > 1 else 693134  # default: Dune Part Two
    demo(tmdb)
