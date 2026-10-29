#!/usr/bin/env python3
"""Remove the floating/expanded preview machinery from configurator.html,
add the sandbox-safe _API_ORIGIN fallback, and patch the fetches.
Every operation is anchored to a unique marker; the script aborts if any
anchor does not match exactly once, so it is safe to re-run / inspect."""
import sys

PATH = '/home/z/my-project/configurator.html'
src = open(PATH, encoding='utf-8').read()
lines = src.split('\n')
orig_len = len(lines)

def find_all(pred):
    return [i for i, l in enumerate(lines) if pred(l)]

def require_one(pred, name):
    hits = find_all(pred)
    if len(hits) != 1:
        print(f'ANCHOR FAIL: {name} matched {len(hits)} lines'); sys.exit(1)
    return hits[0]

def find_end(start, pred, limit=80):
    for j in range(start, min(start + limit, len(lines))):
        if pred(lines[j]):
            return j
    print(f'ANCHOR FAIL: end marker after line {start}'); sys.exit(1)

# ── 1. CSS: fixed floating thumbnail block → in-flow rules ──────────────────
# locate the mobile .preview-panel { position: fixed; ... } block start
a = None
for i, l in enumerate(lines):
    if l.strip() == '.preview-panel {' and i + 1 < len(lines) and 'position: fixed;' in lines[i + 1]:
        a = i; break
if a is None:
    print('ANCHOR FAIL: mobile fixed .preview-panel'); sys.exit(1)
b = find_end(a, lambda l: l.strip().startswith('.preview-placeholder-dim { font-size: 8px'))
replacement = [
    '    /* The preview sits at the top of the page in normal flow — never a',
    '       floating thumbnail, never an overlay: the poster and its changes stay',
    '       visible right above the configuration that draws them. */',
    '    .preview-panel { width: 100%; }',
    '    .preview-frame { height: auto; min-height: 240px; padding: 12px; }',
    '    .preview-poster { width: 100%; height: auto; max-height: 70vh; object-fit: contain; }',
]
lines[a:b+1] = replacement

# ── 2. CSS: drop the whole .preview-panel.expanded overlay block ─────────────
a = require_one(lambda l: l.strip() == '.preview-panel.expanded {', 'expanded block start')
b = find_end(a, lambda l: '/* Import modal — top-biased' in l)
del lines[a:b]  # keeps the import-modal comment line

# ── 3. CSS: drop the light-mode floating shadow rule ────────────────────────
a = require_one(lambda l: l.strip().startswith('html.light-mode .preview-panel {'), 'light-mode floating')
b = find_end(a, lambda l: l.strip() == '}')
del lines[a:b+1]

# ── 4. JS: remove expandPreview/collapsePreview ──────────────────────────────
a = require_one(lambda l: l.startswith('function expandPreview() {'), 'expandPreview')
b = find_end(a, lambda l: l.startswith('// A modal opened over the page'))
lines[a:b] = [
    '// The old expand-to-overlay preview is gone: the panel lives at the top of',
    '// the page in normal flow, so there is nothing to expand or collapse.',
]

# ── 5. JS: popstate handler — drop the previewExpanded tail ──────────────────
a = require_one(lambda l: 'A modal\'s own close pops its entry too' in l, 'popstate comment')
# The block is exactly 7 lines: 2 comments, the early return, the if + 3 lines.
tail = lines[a:a+7]
shape_ok = (
    'previewExpanded' in tail[2] and
    tail[3].strip().startswith('if (previewPanel.classList.contains') and
    '_unlockScroll();' in tail[5] and
    tail[6].strip() == '}'
)
if not shape_ok:
    print('ANCHOR FAIL: popstate tail shape'); sys.exit(1)
del lines[a:a+7]

# ── 6. JS: MQ-change listener + panel/frame click handlers ───────────────────
a = require_one(lambda l: l.startswith('// If the viewport crosses into desktop'), 'mq change comment')
b = find_end(a, lambda l: l.startswith('// At the bottom of the page'))
del lines[a:b]

# ── 7. JS: placePreview function + listeners + its showTab call ──────────────
a = require_one(lambda l: l.startswith('// At the bottom of the page'), 'placePreview comment')
b = find_end(a, lambda l: l.startswith('window.addEventListener(\'resize\', placePreview);'))
del lines[a:b+1]
c = require_one(lambda l: l.strip() == 'placePreview();', 'placePreview call')
d = require_one(lambda l: 'A shorter tab can leave the page scrolled' in l, 'shorter tab comment')
del lines[d:c+1]

# ── 8. JS: stray collapsePreview() call in resetDefaults ─────────────────────
c = require_one(lambda l: l.strip() == 'collapsePreview();', 'collapsePreview call')
del lines[c]

# ── 9. JS: _API_ORIGIN sandbox fallback ──────────────────────────────────────
a = require_one(lambda l: l.startswith('const _REWRITE_DOMAIN ='), '_REWRITE_DOMAIN')
api_block = [
    '// ── API origin ────────────────────────────────────────────────────────────────',
    '// Same-origin ("") when this page is served by the addon itself — the',
    '// deployment or localhost. Inside embedded HTML previews and other sandboxed',
    '// frames the page origin is NOT the addon\'s server and relative URLs cannot',
    '// resolve at all ("… cannot be parsed as a URL"), so every API call — and the',
    '// instance domain the generated URLs carry — falls back to the deployment,',
    '// whose CORS is open to all origins. Images load cross-origin anyway.',
    "const _DEPLOY_ORIGIN  = 'https://topx-posters.onrender.com';",
    "const _OWN_API_HOSTS  = ['topx-posters.onrender.com', 'localhost', '127.0.0.1', '0.0.0.0'];",
    'const _API_ORIGIN = (() => {',
    '  try {',
    "    const p = window.location.protocol;",
    "    if (p !== 'http:' && p !== 'https:') return _DEPLOY_ORIGIN;   // sandboxed / opaque frame",
    "    return _OWN_API_HOSTS.includes(window.location.hostname) ? '' : _DEPLOY_ORIGIN;",
    '  } catch (_) { return _DEPLOY_ORIGIN; }',
    '})();',
]
lines[a+1:a+1] = api_block

# ── 10. JS: patch fetches / domain to carry _API_ORIGIN ──────────────────────
subs = [
    ("const resp = await fetch(url);",
     "const resp = await fetch(_API_ORIGIN + url);"),
    ("const resp = await fetch(`${window.location.origin}/resolve-tmdb?${params}`);",
     "const resp = await fetch(`${_API_ORIGIN || window.location.origin}/resolve-tmdb?${params}`);"),
    ("const resp = await fetch(`${window.location.origin}/resolve-imdb?${params}`);",
     "const resp = await fetch(`${_API_ORIGIN || window.location.origin}/resolve-imdb?${params}`);"),
    ("const resp = await fetch(`/trending-top10${ak ? '?access_key=' + encodeURIComponent(ak) : ''}`);",
     "const resp = await fetch(_API_ORIGIN + `/trending-top10${ak ? '?access_key=' + encodeURIComponent(ak) : ''}`);"),
    ("const resp = await fetch('/report' + (ak ? `?access_key=${encodeURIComponent(ak)}` : ''), {",
     "const resp = await fetch(_API_ORIGIN + '/report' + (ak ? `?access_key=${encodeURIComponent(ak)}` : ''), {"),
    ("const apiOrigin = window.location.origin;",
     "const apiOrigin = _API_ORIGIN || window.location.origin;"),
    (": window.location.origin;",
     ": (_API_ORIGIN || window.location.origin);"),
]
for old, new in subs:
    hits = [i for i, l in enumerate(lines) if l.strip() == old.strip()]
    if len(hits) != 1:
        print(f'SUB FAIL ({len(hits)} hits): {old[:60]}'); sys.exit(1)
    lines[hits[0]] = lines[hits[0]].replace(old.strip(), new)

out = '\n'.join(lines)
open(PATH, 'w', encoding='utf-8').write(out)
print(f'OK: {orig_len} → {len(lines)} lines')
