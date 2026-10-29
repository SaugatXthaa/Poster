#!/usr/bin/env python3
"""Round 8: elfhosted-style hero layout + Top X tab (charts & languages).
Replaces the topx strip (CSS + HTML) with the poster-row hero, inserts the
Top X tab before Core. All replacements are marker-anchored and asserted."""
import sys, re

PATH = "/home/z/my-project/download/topx-posters/public/index.html"

with open(PATH, "r", encoding="utf-8") as f:
    lines = f.readlines()

def find_one(marker):
    hits = [i for i, l in enumerate(lines) if marker in l]
    if len(hits) != 1:
        print(f"FAIL: marker {marker!r} found {len(hits)} times (need exactly 1)")
        sys.exit(1)
    return hits[0]

def read_block(path):
    with open(path, "r", encoding="utf-8") as f:
        return f.read().rstrip("\n").split("\n")

# ── 1. replace the strip style block ─────────────────────────────────────
s = find_one('<style id="topx-strip-style">')
e = next(i for i in range(s, len(lines)) if "</style>" in lines[i])
new_css = read_block("/home/z/my-project/scripts/round8_new_style.css")
lines[s:e + 1] = new_css
print(f"style block: lines {s+1}..{e+1} -> {len(new_css)} lines")

# ── 2. replace the strip HTML with the hero ──────────────────────────────
s = find_one("TOP: LOCKED LIVE PREVIEW STRIP")
# back up to include the comment opener line if the marker is mid-line
if not lines[s].lstrip().startswith("<!--"):
    s -= 1
e = find_one('<div class="tx-zoom"')
new_hero = read_block("/home/z/my-project/scripts/round8_hero.html")
lines[s:e] = new_hero + [""]  # keep one blank line before tx-zoom
print(f"strip html: lines {s+1}..{e} -> {len(new_hero)} lines")

# ── 3. Top X tab button before Core ──────────────────────────────────────
c = find_one('<div class="tab-btn" data-tab="core"')
tabbtn = read_block("/home/z/my-project/scripts/round8_tabbtn.html")
lines[c:c] = tabbtn
print(f"tab button inserted before line {c+1}")

# ── 4. Top X section before the Core section ─────────────────────────────
c = find_one("<!-- 1. CORE CONFIG -->")
section = read_block("/home/z/my-project/scripts/round8_section.html")
lines[c:c] = section
print(f"topx section inserted before line {c+1}")

out = "".join(lines)

# ── 5. assertions ─────────────────────────────────────────────────────────
must_have = ['topx-hero-style', 'id="topx-hero"', 'id="topx-posters"', 'id="tx-card-b"',
             'id="txCanvasRow"', 'data-tab="topx"', 'id="tx-lang-secondary"',
             'id="tx-movies-label"', 'id="tx-frame-b"', 'id="tx-poster-b"',
             'id="preview-frame"', 'id="tog-landscape"', 'id="countryGrid"',
             'id="platformsGrid"', 'id="tx-generate"', 'id="url-display-input"',
             'id="external-link"', 'id="import-url-btn"', 'id="preview-status"',
             'id="report-poster"', 'id="artwork-edit-link"', 'id="tx-status"']
must_not = ['topx-strip-style', 'class="topx-strip"', 'id="quickBg"', 'data-tab="topx" onclick="showTab(\'rating\')"']
for m in must_have:
    if m not in out:
        print(f"FAIL: missing {m!r} after edit"); sys.exit(1)
for m in must_not:
    if m in out:
        print(f"FAIL: stale {m!r} still present"); sys.exit(1)

with open(PATH, "w", encoding="utf-8") as f:
    f.write(out)
print(f"OK — wrote {len(out)} bytes")
