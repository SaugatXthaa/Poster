import re

data = open('/tmp/mlsbd_inc.html').read()
print(f'Page size: {len(data)}')

print('\n--- data-* attributes ---')
for m in re.finditer(r'data-[a-z-]+="[^"]*"', data):
    print(' ', m.group(0)[:120])

print('\n--- fetch/api/nonce/app.mlsbd/stream patterns ---')
for m in re.finditer(r'fetch\([^)]+\)|/api/[a-z0-9/_-]+|admin-ajax|nonce[^a-zA-Z][^,;\s]{0,40}|app\.mlsbd[^"\'\s,;]*|stream\.[a-z]+', data, re.I):
    print(' ', m.group(0)[:120])

print('\n--- All non-trivial URLs in page ---')
for u in set(re.findall(r'https?://[a-z0-9.-]+\.[a-z]{2,}[a-z0-9/_?=&.-]*', data)):
    if any(x in u for x in ['mlsbd', 'gdflix', 'hubcloud', 'vcloud', 'google', 'pixeldrain', 'gofile', 'gdtot', 'streamtape', 'doodstream', 'mixdrop', 'filesgram', 'mega.nz']):
        if not any(x in u for x in ['wp-content', 'wp-includes', 'googletagmanager', 'gstatic', 'schema.org']):
            print(' ', u[:200])

print('\n--- Script sources + inline JS ---')
for m in re.finditer(r'<script[^>]*src="([^"]+)"', data):
    print(' script src:', m.group(1)[:120])

# Look for inline JS that might fetch streams
for m in re.finditer(r'<script[^>]*>([\s\S]*?)</script>', data):
    s = m.group(1).strip()
    if s and len(s) > 50:
        if any(kw in s.lower() for kw in ['fetch', 'ajax', 'stream', 'download', 'mlsbd', 'app.', 'api', 'gdflix', 'vcloud', 'hubcloud']):
            print('\n  inline JS (first 600):')
            print(' ', s[:600])

print('\n--- page text (stripped) ---')
text = re.sub(r'<script[^>]*>[\s\S]*?</script>', '', data)
text = re.sub(r'<style[^>]*>[\s\S]*?</style>', '', text)
text = re.sub(r'<[^>]+>', ' ', text)
text = re.sub(r'\s+', ' ', text).strip()
print(text[:2500])
