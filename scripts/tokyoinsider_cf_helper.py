#!/usr/bin/env python3
"""
TokyoInsider / Cloudflare bypass helper.

Usage:
    python3 tokyoinsider_cf_helper.py <url>

Attempts to load a Cloudflare-protected page through Playwright
(headless Chromium) and prints the final HTML to stdout.

Cloudflare Turnstile is hard to bypass automatically - it requires
real user interaction in most cases. This helper:
  1. Loads the page
  2. Waits up to 60s for the CF challenge to auto-clear
  3. Periodically tries to click the Turnstile checkbox
  4. Falls back to printing whatever HTML we have after 60s

Exits 0 with HTML on stdout (caller checks for "Just a moment" in
the output to detect CF failure). Exits non-zero only on hard errors
(Playwright not installed, URL unreachable, etc.).
"""

import sys
import time

try:
    from playwright.sync_api import sync_playwright
except ImportError:
    sys.stderr.write("playwright not installed: pip install playwright && playwright install chromium\n")
    sys.exit(2)


def try_click_turnstile(page):
    """Try to find and click the Cloudflare Turnstile checkbox."""
    try:
        for frame in page.frames:
            url = frame.url or ""
            if "challenges.cloudflare.com" not in url:
                continue
            try:
                # The checkbox is usually inside an iframe
                checkbox = frame.query_selector('input[type="checkbox"]')
                if checkbox:
                    checkbox.click(timeout=1500)
                    return True
            except Exception:
                pass
    except Exception:
        pass
    return False


def main():
    if len(sys.argv) < 2:
        sys.stderr.write("Usage: tokyoinsider_cf_helper.py <url>\n")
        sys.exit(2)
    url = sys.argv[1]
    max_wait_s = 60  # don't wait longer than this for CF to clear

    with sync_playwright() as p:
        browser = p.chromium.launch(
            headless=True,
            args=[
                "--disable-blink-features=AutomationControlled",
                "--no-sandbox",
                "--disable-dev-shm-usage",
            ],
        )
        context = browser.new_context(
            user_agent="Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 "
            "(KHTML, like Gecko) Chrome/151.0.0.0 Safari/537.36",
            viewport={"width": 1280, "height": 800},
            locale="en-US",
        )
        # Hide the webdriver flag
        context.add_init_script(
            "Object.defineProperty(navigator, 'webdriver', {get: () => undefined});"
        )
        page = context.new_page()

        try:
            page.goto(url, wait_until="domcontentloaded", timeout=30000)
        except Exception as e:
            sys.stderr.write(f"Initial navigation failed: {e}\n")
            browser.close()
            sys.exit(3)

        # Wait for CF to clear, periodically trying to click the Turnstile
        start = time.time()
        while time.time() - start < max_wait_s:
            try:
                title = page.title() or ""
            except Exception:
                title = ""
            if "Just a moment" not in title and "Performing security" not in title:
                # CF cleared! Give the page a moment to load real content
                time.sleep(2)
                break
            # Try clicking the Turnstile checkbox
            try_click_turnstile(page)
            time.sleep(3)

        # Always print whatever HTML we have
        try:
            html = page.content()
            sys.stdout.write(html)
            sys.stdout.flush()
        except Exception as e:
            sys.stderr.write(f"Failed to get page content: {e}\n")
            browser.close()
            sys.exit(4)

        browser.close()


if __name__ == "__main__":
    main()
