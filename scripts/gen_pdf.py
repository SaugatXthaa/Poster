#!/usr/bin/env python3
"""
Generate PDF: Stream Reverse Engineering — A Practical Guide
Simplified, beginner-friendly, step-by-step format.
"""
import sys, os, hashlib

PDF_SKILL_DIR = "/home/z/my-project/skills/pdf"
_scripts = os.path.join(PDF_SKILL_DIR, "scripts")
if _scripts not in sys.path:
    sys.path.insert(0, _scripts)

from reportlab.lib import colors
from reportlab.lib.pagesizes import A4
from reportlab.lib.units import mm
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.enums import TA_LEFT
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, PageBreak, Table, TableStyle,
    KeepTogether, HRFlowable
)
from reportlab.platypus.tableofcontents import TableOfContents
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont

# Fonts
pdfmetrics.registerFont(TTFont('BodyFont', '/usr/share/fonts/truetype/noto-serif-sc/NotoSerifSC-Regular.ttf'))
pdfmetrics.registerFont(TTFont('BodyFont-Bold', '/usr/share/fonts/truetype/noto-serif-sc/NotoSerifSC-Bold.ttf'))
pdfmetrics.registerFont(TTFont('HeadFont', '/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf'))
pdfmetrics.registerFont(TTFont('HeadFont-Normal', '/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf'))
pdfmetrics.registerFont(TTFont('Mono', '/usr/share/fonts/truetype/dejavu/DejaVuSansMono.ttf'))
pdfmetrics.registerFontFamily('BodyFont', normal='BodyFont', bold='BodyFont-Bold')
pdfmetrics.registerFontFamily('HeadFont', normal='HeadFont-Normal', bold='HeadFont')

# Palette (dark)
PAGE_BG = colors.HexColor('#0d0c0b')
CARD_BG = colors.HexColor('#1a1917')
TABLE_STRIPE = colors.HexColor('#1d1c18')
HEADER_FILL = colors.HexColor('#2d2a22')
BORDER = colors.HexColor('#4a4435')
ACCENT = colors.HexColor('#d1b257')
ACCENT2 = colors.HexColor('#6c53b9')
TEXT = colors.HexColor('#e8e7e5')
MUTED = colors.HexColor('#8a8780')
GREEN = colors.HexColor('#7abb90')
BLUE = colors.HexColor('#6590bb')
ORANGE = colors.HexColor('#c1aa7d')
RED = colors.HexColor('#c08984')

styles = getSampleStyleSheet()
S_TITLE = ParagraphStyle('T', fontName='HeadFont', fontSize=22, leading=28, textColor=TEXT, spaceAfter=4)
S_SUB = ParagraphStyle('ST', fontName='HeadFont-Normal', fontSize=14, textColor=ACCENT, leading=20)
S_H1 = ParagraphStyle('H1', fontName='HeadFont', fontSize=15, leading=21, textColor=ACCENT, spaceBefore=18, spaceAfter=8)
S_H2 = ParagraphStyle('H2', fontName='HeadFont', fontSize=12, leading=17, textColor=BLUE, spaceBefore=12, spaceAfter=5)
S_H3 = ParagraphStyle('H3', fontName='HeadFont', fontSize=10, leading=15, textColor=TEXT, spaceBefore=8, spaceAfter=3)
S_BODY = ParagraphStyle('B', fontName='BodyFont', fontSize=10, leading=15, textColor=TEXT, spaceAfter=5)
S_TIP = ParagraphStyle('TIP', fontName='BodyFont', fontSize=9.5, leading=14, textColor=GREEN, leftIndent=12, spaceAfter=4)
S_WARN = ParagraphStyle('WARN', fontName='BodyFont', fontSize=9.5, leading=14, textColor=ORANGE, leftIndent=12, spaceAfter=4)
S_CODE = ParagraphStyle('C', fontName='Mono', fontSize=8, leading=11, textColor=GREEN, backColor=CARD_BG, leftIndent=8, rightIndent=8, spaceBefore=3, spaceAfter=6, borderColor=BORDER, borderWidth=0.5, borderPadding=5)
S_MUTED = ParagraphStyle('M', fontName='BodyFont', fontSize=9, textColor=MUTED, leading=13, spaceAfter=3)
S_STEP = ParagraphStyle('STEP', fontName='HeadFont', fontSize=10, leading=15, textColor=ACCENT2, spaceBefore=8, spaceAfter=3)

class TocDoc(SimpleDocTemplate):
    def afterFlowable(self, f):
        if hasattr(f, 'bm'):
            self.notify('TOCEntry', (getattr(f, 'bml', 0), getattr(f, 'bmt', ''), self.page, getattr(f, 'bmk', '')))

def H(text, style, level=0):
    k = f'h_{hashlib.md5(text.encode()).hexdigest()[:8]}'
    p = Paragraph(f'<a name="{k}"/>{text}', style)
    p.bm = True; p.bml = level; p.bmt = text; p.bmk = k
    return p

def page_bg(c, d):
    c.saveState(); c.setFillColor(PAGE_BG); c.rect(0,0,A4[0],A4[1],fill=1,stroke=0)
    c.setFont('HeadFont-Normal',8); c.setFillColor(MUTED)
    c.drawRightString(A4[0]-15*mm, 10*mm, f'{d.page}')
    c.drawString(15*mm, 10*mm, 'Stream Reverse Engineering Guide')
    c.restoreState()

def tbl(data, widths):
    t = Table(data, colWidths=widths)
    t.setStyle(TableStyle([
        ('BACKGROUND',(0,0),(-1,0),HEADER_FILL),('TEXTCOLOR',(0,0),(-1,0),TEXT),
        ('FONTNAME',(0,0),(-1,0),'HeadFont'),('FONTSIZE',(0,0),(-1,-1),8),
        ('FONTNAME',(0,1),(-1,-1),'BodyFont'),('TEXTCOLOR',(0,1),(-1,-1),TEXT),
        ('ROWBACKGROUNDS',(0,1),(-1,-1),[CARD_BG,TABLE_STRIPE]),
        ('GRID',(0,0),(-1,-1),0.5,BORDER),('VALIGN',(0,0),(-1,-1),'TOP'),
        ('LEFTPADDING',(0,0),(-1,-1),4),('RIGHTPADDING',(0,0),(-1,-1),4),
        ('TOPPADDING',(0,0),(-1,-1),2.5),('BOTTOMPADDING',(0,0),(-1,-1),2.5),
    ]))
    return t

story = []

# ═══════ COVER ═══════
story.append(Spacer(1, 70*mm))
story.append(Paragraph('Stream Reverse Engineering', S_TITLE))
story.append(Spacer(1, 3*mm))
story.append(Paragraph('A Practical Step-by-Step Guide', S_SUB))
story.append(Spacer(1, 12*mm))
story.append(HRFlowable(width='35%', thickness=2, color=ACCENT, spaceAfter=15))
story.append(Paragraph(
    'How to find direct playable streams from any web source using pure Node.js. '
    'No browser needed. Covers movies, TV shows, anime, and all content types. '
    'Includes Cloudflare bypass, WASM decryption, XOR segments, custom ciphers, '
    'and building decryption proxies — explained simply.', S_MUTED))
story.append(Spacer(1, 20*mm))
story.append(Paragraph('What You Will Learn:', S_H3))
for x in [
    'How to identify what framework a streaming site uses',
    'How to find the hidden API that serves stream URLs',
    'How to bypass Cloudflare protection without a browser',
    'How to decrypt WASM, AES, and XOR encrypted streams',
    'How to build a local proxy that makes encrypted streams playable',
    'How to verify your streams work with ffprobe/ffmpeg',
]:
    story.append(Paragraph(f'&bull; {x}', S_MUTED))
story.append(PageBreak())

# ═══════ TOC ═══════
toc = TableOfContents()
toc.levelStyles = [
    ParagraphStyle('T0', fontName='HeadFont', fontSize=11, leading=16, textColor=ACCENT, leftIndent=0),
    ParagraphStyle('T1', fontName='BodyFont', fontSize=10, leading=14, textColor=TEXT, leftIndent=15),
]
story.append(Paragraph('Table of Contents', S_H1))
story.append(Spacer(1, 5*mm))
story.append(toc)
story.append(PageBreak())

# ═══════ CH 1: THE BIG PICTURE ═══════
story.append(H('1. The Big Picture', S_H1, 0))
story.append(Paragraph(
    'Every streaming website shows you a video player in your browser. Behind that player, '
    'there is a chain of steps that turns a movie title into a playable video URL. '
    'This guide teaches you how to trace that chain and replicate it in Node.js.', S_BODY))

story.append(Paragraph(
    'Think of it like a treasure hunt. The movie title is the starting clue. '
    'Each step gives you the next clue. The treasure at the end is a direct URL '
    'to a .m3u8 (HLS) or .mp4 or .mkv file that any video player can play.', S_BODY))

story.append(H('1.1 The Universal Chain', S_H2, 1))
story.append(Paragraph('Every streaming site follows this pattern:', S_BODY))
story.append(Paragraph(
    'Step 1: Start with a movie title or ID (like a TMDB ID)<br/><br/>'
    'Step 2: Find the movie page on the streaming site<br/><br/>'
    'Step 3: Extract stream URLs from that page (or from a hidden API)<br/><br/>'
    'Step 4: Decrypt or decode the URLs if they are encrypted<br/><br/>'
    'Step 5: Return the final direct playable URL', S_CODE))

story.append(Paragraph(
    'TIP: Some sites have simple chains (just 2 steps). Others have complex chains '
    '(5+ steps with encryption). This guide covers both.', S_TIP))

story.append(H('1.2 What You Need', S_H2, 1))
story.append(Paragraph('Only 3 tools:', S_BODY))
story.append(Paragraph(
    '1. Node.js (v18+) — runs JavaScript code, has built-in HTTPS, crypto, WebAssembly<br/><br/>'
    '2. curl — a command-line HTTP client (already installed on most systems)<br/><br/>'
    '3. ffprobe/ffmpeg — for testing if streams actually play (optional but recommended)', S_CODE))
story.append(Paragraph('That is it. No browser. No Playwright. No npm install.', S_TIP))

# ═══════ CH 2: STEP 1 — RECONNAISSANCE ═══════
story.append(PageBreak())
story.append(H('2. Step 1: Reconnaissance (Scout the Site)', S_H1, 0))
story.append(Paragraph(
    'Before you can extract streams, you need to understand how the site works. '
    'This means fetching the homepage and a movie page, then looking for clues.', S_BODY))

story.append(H('2.1 Fetch the Homepage', S_H2, 1))
story.append(Paragraph('Use curl to download the homepage HTML:', S_BODY))
story.append(Paragraph(
    'curl -sL -H \'User-Agent: Mozilla/5.0\' \'https://streaming-site.com/\' > home.html', S_CODE))

story.append(H('2.2 Fetch a Movie Page', S_H2, 1))
story.append(Paragraph('Find any movie link on the homepage, then fetch that page too:', S_BODY))
story.append(Paragraph(
    'curl -sL -H \'User-Agent: Mozilla/5.0\' \'https://streaming-site.com/movie/299534\' > movie.html', S_CODE))

story.append(H('2.3 Look for Clues', S_H2, 1))
story.append(Paragraph('Search the downloaded HTML for these patterns:', S_BODY))

clues = [
    ['What you find', 'What it means', 'Next step'],
    ['.m3u8 or .mp4 URL', 'Direct stream URL found!', 'Done — play it directly'],
    ['/api/ endpoint', 'Site has an API', 'Call the API to get streams'],
    ['&lt;iframelt;iframe src="...">', 'Embed from another site', 'Fetch the iframe page'],
    ['download links', 'File host mirrors', 'Follow each mirror link'],
    ['base64 string (?id=...)', 'Encoded URL', 'Decode: Buffer.from(id, \'base64\')'],
    ['No stream URLs in HTML', 'JavaScript SPA', 'Download JS bundles (Ch 3)'],
    ['Encrypted data', 'API encrypts responses', 'Find decryption key (Ch 5)'],
]
story.append(tbl(clues, [45*mm, 50*mm, 60*mm]))

story.append(Spacer(1, 4*mm))
story.append(Paragraph(
    'TIP: If the HTML has no stream URLs, the site is a JavaScript SPA (Single Page App). '
    'The stream URLs are loaded dynamically by JavaScript. See Chapter 3.', S_TIP))

# ═══════ CH 3: STEP 2 — FIND THE API ═══════
story.append(PageBreak())
story.append(H('3. Step 2: Find the Hidden API', S_H1, 0))
story.append(Paragraph(
    'If the stream URLs are not in the HTML, they are loaded by JavaScript. '
    'You need to find which JavaScript file makes the API call that returns stream URLs.', S_BODY))

story.append(H('3.1 Download JavaScript Bundles', S_H2, 1))
story.append(Paragraph('Find all &lt;script src="..."&gt; tags in the HTML and download them:', S_BODY))
story.append(Paragraph(
    '# Find script tags<br/>'
    'grep -o \'src="[^"]*\\.js"\' movie.html<br/><br/>'
    '# Download each one<br/>'
    'curl -sL \'https://site.com/assets/index-ABC123.js\' > main.js', S_CODE))

story.append(H('3.2 Search for API URLs', S_H2, 1))
story.append(Paragraph('Search the JavaScript for patterns that look like API calls:', S_BODY))
story.append(Paragraph(
    '# Look for fetch() calls<br/>'
    'grep -o \'fetch(`[^`]*`\' main.js<br/><br/>'
    '# Look for URLs with template variables (${})<br/>'
    'grep -oE \'https?://[^"\\\' ]+\\$\\{[^}]+\\}[^"\\\' ]*\' main.js<br/><br/>'
    '# Look for /api/ paths<br/>'
    'grep -oE \'/api/[a-z0-9/_\\-]+\' main.js<br/><br/>'
    '# Look for baseURL<br/>'
    'grep -o \'baseURL["\\\' ]*[:=]["\\\' ]*[^"\\\' ]+\' main.js', S_CODE))

story.append(H('3.3 Understanding Webpack Chunks', S_H2, 1))
story.append(Paragraph(
    'Next.js and React sites split their JavaScript into multiple "chunks" (small files). '
    'The main bundle loads other chunks dynamically. You need to find which chunk has the '
    'stream code.', S_BODY))

story.append(Paragraph('The chunk ID to filename mapping is in the webpack runtime JS:', S_BODY))
story.append(Paragraph(
    '# The webpack runtime contains a function like:<br/>'
    '# p.u = function(e) { return 6476===e ? "chunks/6476.abc123.js" : ... }<br/><br/>'
    '# Extract chunk IDs and their hashes:<br/>'
    'grep -oE \'[0-9]+:"[a-f0-9]+"\' webpack.js<br/>'
    '# Output: 6476:"1364eecee369c803", 9230:"719542700d7b5fe3", ...<br/><br/>'
    '# Then download the chunk:<br/>'
    'curl -sL \'https://site.com/_next/static/chunks/9230.719542700d7b5fe3.js\' > player.js', S_CODE))

story.append(Paragraph(
    'TIP: Search each downloaded chunk for keywords like "m3u8", "mp4", "sources", '
    '"stream", "fetch", "url", "player" to find the one with stream logic.', S_TIP))

# ═══════ CH 4: STEP 3 — BYPASS CLOUDFLARE ═══════
story.append(PageBreak())
story.append(H('4. Step 3: Bypass Cloudflare', S_H1, 0))
story.append(Paragraph(
    'Many streaming sites use Cloudflare (CF) to protect against bots. '
    'CF has several protection layers. Here is how to bypass each one without a browser.', S_BODY))

story.append(H('4.1 Protection Type 1: TLS Fingerprinting (JA3)', S_H2, 1))
story.append(Paragraph(
    'Problem: CF checks the TLS handshake fingerprint. Node.js has a unique fingerprint '
    'that CF blocks with HTTP 403.', S_WARN))
story.append(Paragraph(
    'Solution: Use curl instead of Node.js HTTPS. Curl\'s TLS fingerprint looks like '
    'a real browser and is accepted by CF.', S_TIP))
story.append(Paragraph(
    'const { execFileSync } = require(\'child_process\');<br/><br/>'
    'function fetchWithCurl(url) {<br/>'
    '&nbsp;&nbsp;return execFileSync(\'curl\', [\'-sL\', \'--max-time\', \'30\', url],<br/>'
    '&nbsp;&nbsp;&nbsp;&nbsp;{ encoding: \'buffer\', maxBuffer: 50*1024*1024 });<br/>'
    '}', S_CODE))

story.append(H('4.2 Protection Type 2: User-Agent Detection', S_H2, 1))
story.append(Paragraph(
    'Problem: Some CF sites reject the full Chrome User-Agent string as a bot.', S_WARN))
story.append(Paragraph(
    'Solution: Use a shorter, simpler User-Agent that passes CF bot detection:', S_TIP))
story.append(Paragraph(
    '// REJECTED by some CF sites (403 Forbidden):<br/>'
    '\'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36\'<br/><br/>'
    '// ACCEPTED (200 OK):<br/>'
    '\'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36\'', S_CODE))

story.append(H('4.3 Protection Type 3: Referer Gating', S_H2, 1))
story.append(Paragraph(
    'Problem: Some CDNs only serve streams if the Referer header matches their domain. '
    'Without the correct Referer, you get 403.', S_WARN))
story.append(Paragraph(
    'Solution: Include the Referer header in your request and pass it to the player:', S_TIP))
story.append(Paragraph(
    '// When fetching the stream, include Referer:<br/>'
    'const response = await fetch(streamUrl, {<br/>'
    '&nbsp;&nbsp;headers: { \'Referer\': \'https://player.videasy.to/\' }<br/>'
    '});<br/><br/>'
    '// When returning the stream, tell the player to use this Referer:<br/>'
    '{<br/>'
    '&nbsp;&nbsp;url: streamUrl,<br/>'
    '&nbsp;&nbsp;behaviorHints: {<br/>'
    '&nbsp;&nbsp;&nbsp;&nbsp;proxyHeaders: { request: { Referer: \'https://player.videasy.to/\' } }<br/>'
    '&nbsp;&nbsp;}<br/>'
    '}', S_CODE))

story.append(H('4.4 Quick Decision Tree', S_H2, 1))
story.append(Paragraph(
    'IF you get HTTP 403 from a site:<br/>'
    '&nbsp;&nbsp;TRY 1: Use curl instead of Node.js HTTPS (bypasses JA3)<br/>'
    '&nbsp;&nbsp;TRY 2: Use a shorter User-Agent string<br/>'
    '&nbsp;&nbsp;TRY 3: Add a Referer header matching the site\'s domain<br/>'
    '&nbsp;&nbsp;TRY 4: Check if the site has a separate API subdomain without CF<br/><br/>'
    'IF none of these work, the site uses a full CF challenge page.<br/>'
    'In that case, look for an alternative API endpoint that does NOT go through CF.', S_CODE))

# ═══════ CH 5: STEP 4 — DECRYPTION ═══════
story.append(PageBreak())
story.append(H('5. Step 4: Decrypt Encrypted Streams', S_H1, 0))
story.append(Paragraph(
    'Some sites encrypt their stream URLs or segment data. This chapter covers every '
    'encryption type I have encountered and how to decrypt each one in pure Node.js.', S_BODY))

# 5.1 Base64
story.append(H('5.1 Base64-Encoded URLs (Easy)', S_H2, 1))
story.append(Paragraph('Some sites hide URLs by encoding them as base64:', S_BODY))
story.append(Paragraph(
    '// URL: /generate.php?id=aHR0cHM6Ly9jZG4uZXhhbXBsZS5jb20vdmkgZG8ubTNO<br/><br/>'
    '// Decode it:<br/>'
    'const decoded = Buffer.from(base64String, \'base64\').toString(\'utf8\');<br/>'
    '// Result: "https://cdn.example.com/video.m3u8"', S_CODE))

# 5.2 XOR
story.append(H('5.2 XOR-Encrypted Segments (Medium)', S_H2, 1))
story.append(Paragraph(
    'Some CDNs disguise video segments as image files (.webp or .png). '
    'The file starts with a fake image header, then the rest is XOR-encrypted '
    'MPEG-TS data. The XOR key is hardcoded in the site\'s JavaScript.', S_BODY))

story.append(Paragraph('How to find the key:', S_STEP))
story.append(Paragraph(
    '1. Download the site\'s hls.js or player JavaScript file<br/>'
    '2. Search for a 16-byte array of numbers near the words "webp" or "png"<br/>'
    '3. It looks like: [157, 42, 241, 71, 179, 142, 92, 112, ...]<br/>'
    '4. That is your XOR key', S_CODE))

story.append(Paragraph('How to decrypt:', S_STEP))
story.append(Paragraph(
    'const KEY = Buffer.from([157, 42, 241, 71, 179, 142, 92, 112, 166, 25, 228, 59, 216, 98, 15, 197]);<br/><br/>'
    '// Check if it is a fake WebP (starts with "RIFF....WEBP")<br/>'
    'if (body[0] === 0x52 && body[8] === 0x57 && body[11] === 0x50) {<br/>'
    '&nbsp;&nbsp;const payload = body.slice(12); // Remove 12-byte fake header<br/>'
    '&nbsp;&nbsp;const decrypted = Buffer.alloc(payload.length);<br/>'
    '&nbsp;&nbsp;for (let i = 0; i < payload.length; i++) {<br/>'
    '&nbsp;&nbsp;&nbsp;&nbsp;decrypted[i] = payload[i] ^ KEY[i % 16]; // XOR each byte<br/>'
    '&nbsp;&nbsp;}<br/>'
    '&nbsp;&nbsp;// Result: valid MPEG-TS (first byte should be 0x47)<br/>'
    '}', S_CODE))

story.append(Paragraph(
    'TIP: To verify decryption worked, check that the first byte is 0x47 (MPEG-TS sync byte) '
    'and that every 188th byte is also 0x47.', S_TIP))

# 5.3 WASM
story.append(H('5.3 WASM Decryption (Hard)', S_H2, 1))
story.append(Paragraph(
    'Some sites embed a WebAssembly (WASM) module in the page as base64. '
    'This module contains functions that derive decryption keys. '
    'Node.js can run WASM natively — no browser needed.', S_BODY))

story.append(Paragraph('Step-by-step:', S_STEP))
story.append(Paragraph(
    '// Step 1: Find the base64 WASM in the page HTML<br/>'
    'const wasmB64 = html.match(/w_payload:"([^"]+)"/)[1];<br/><br/>'
    '// Step 2: Compile and instantiate in Node.js<br/>'
    'const wasmBytes = Buffer.from(wasmB64, \'base64\');<br/>'
    'const mod = await WebAssembly.compile(wasmBytes);<br/>'
    'const inst = await WebAssembly.instantiate(mod, {});<br/><br/>'
    '// Step 3: Read the player JS to find which WASM functions to call<br/>'
    '// Look for: ex._s(), ex._r(), ex._c() in the player code<br/>'
    '// These are the WASM exports for: set_seed, run_derivation, get_key<br/><br/>'
    '// Step 4: Set up WASM memory (write inputs, call functions, read outputs)<br/>'
    'const mem = new Uint8Array(inst.exports.memory.buffer);<br/>'
    'mem.set(inputBuffer, 1000);  // Write input at offset 1000<br/>'
    'inst.exports._s(seedValue);  // Set the seed<br/>'
    'inst.exports._r(1000, ...);   // Run derivation<br/>'
    'const key = Buffer.from(mem.subarray(outputOffset, outputOffset + 32));<br/><br/>'
    '// Step 5: Use the derived key for AES decryption (see 5.4)', S_CODE))

story.append(Paragraph(
    'WARN: The exact memory layout and function arguments depend on the site. '
    'You must read the player JavaScript to find how it calls the WASM functions. '
    'Look for patterns like: ex._s(v), ex._r(I, P, U, nt, k).', S_WARN))

# 5.4 AES
story.append(H('5.4 AES-256-CBC Decryption (Hard)', S_H2, 1))
story.append(Paragraph(
    'After WASM derives a key fragment, the final URL is AES-256-CBC encrypted. '
    'The full key derivation chain: WASM output -> PBKDF2 -> XOR with seed -> SHA-256 -> AES key.', S_BODY))
story.append(Paragraph(
    'const crypto = require(\'crypto\');<br/><br/>'
    '// Step 1: PBKDF2 (derive 32 bytes from WASM output + seed)<br/>'
    'const W = crypto.pbkdf2Sync(wasmKey, seed, 1000, 32, \'sha256\');<br/><br/>'
    '// Step 2: XOR with seed bytes<br/>'
    'const tt = Buffer.from(W);<br/>'
    'for (let i = 0; i < 32; i++) tt[i] ^= seed.charCodeAt(i % seed.length);<br/><br/>'
    '// Step 3: SHA-256 to get final AES key<br/>'
    'const aesKey = crypto.createHash(\'sha256\').update(tt).digest();<br/><br/>'
    '// Step 4: AES-256-CBC decrypt<br/>'
    'const decipher = crypto.createDecipheriv(\'aes-256-cbc\', aesKey, iv);<br/>'
    'const decrypted = Buffer.concat([decipher.update(ct), decipher.final()]);<br/>'
    '// Result: the stream URL as a UTF-8 string', S_CODE))

# 5.5 SHA-256 field names
story.append(H('5.5 SHA-256 Obfuscated Field Names', S_H2, 1))
story.append(Paragraph(
    'Some sites do not use fixed JSON key names. Instead, the key names are derived '
    'from a per-request seed by chaining SHA-256 hashes. This makes it impossible to '
    'hardcode field names — you must compute them dynamically.', S_BODY))
story.append(Paragraph(
    'function sha256(s) {<br/>'
    '&nbsp;&nbsp;return crypto.createHash(\'sha256\').update(s, \'utf8\').digest(\'hex\');<br/>'
    '}<br/><br/>'
    '// Chain: hash the seed 3 times, then hash the result 3 more times<br/>'
    'let e = seed;<br/>'
    'for (let i = 0; i < 3; i++) e = sha256(e + i.toString());<br/>'
    'let a = e;<br/>'
    'for (let i = 0; i < 3; i++) a = sha256(a + i.toString());<br/><br/>'
    '// The hash determines field names:<br/>'
    '// containerName = "cd_" + e.substring(24, 32)<br/>'
    '// keyField = "kf_" + e.substring(8, 16)<br/>'
    '// tokenField = e.substring(48, 64) + "_" + e.substring(56, 64)<br/><br/>'
    '// Search the page HTML for these dynamic field names to extract values', S_CODE))

# 5.6 Custom cipher
story.append(H('5.6 Custom Stream Ciphers (Hardest)', S_H2, 1))
story.append(Paragraph(
    'Some sites build their own encryption from scratch, combining multiple algorithms: '
    'RC4 key schedule + FNV-1a hash + xmur3 finalizer + custom PRNG. '
    'These are harder to port because the PRNG often uses the output buffer itself as '
    'its internal state (reading from and writing to the same array).', S_BODY))

story.append(Paragraph('How to approach this:', S_STEP))
story.append(Paragraph(
    '1. Extract the crypto functions from the site\'s JavaScript chunk<br/><br/>'
    '2. Identify the constants (usually arrays of integers at the top)<br/><br/>'
    '3. Identify the helper functions: hash, rotate-left, RC4 key schedule<br/><br/>'
    '4. Identify the PRNG: does it read from the output buffer? If yes, you must<br/>'
    '&nbsp;&nbsp;&nbsp;use the SAME buffer for both PRNG state and keystream output<br/><br/>'
    '5. Look for a magic header (like "mvm1" = bytes [109, 118, 109, 49]) that<br/>'
    '&nbsp;&nbsp;&nbsp;verifies decryption was successful<br/><br/>'
    '6. Port each function to Node.js, then chain them together<br/><br/>'
    '7. Test by checking if the magic header appears at the start of the output', S_CODE))

story.append(Paragraph(
    'WARN: If porting fails after multiple attempts, you can still use Playwright as a fallback '
    'to run the decryption in the browser context. But try Node.js first — it is always faster.', S_WARN))

# ═══════ CH 6: BUILDING A PROXY ═══════
story.append(PageBreak())
story.append(H('6. Building a Decryption Proxy', S_H1, 0))
story.append(Paragraph(
    'When streams are encrypted, standard video players (VLC, mpv, Stremio) cannot play them '
    'directly. The solution is to build a small local HTTP server (proxy) that fetches the '
    'encrypted content, decrypts it, and serves it as plain HLS/MPEG-TS.', S_BODY))

story.append(H('6.1 How the Proxy Works', S_H2, 1))
story.append(Paragraph(
    'Player (VLC) --> Local Proxy (port 7654) --> CDN (encrypted)<br/>'
    '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|<br/>'
    '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;decrypt here<br/>'
    '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;serve as plain TS', S_CODE))

story.append(H('6.2 Proxy Code Template', S_H2, 1))
story.append(Paragraph(
    'const http = require(\'http\');<br/><br/>'
    'http.createServer(async (req, res) => {<br/>'
    '&nbsp;&nbsp;const url = new URL(req.url, \'http://localhost\');<br/>'
    '&nbsp;&nbsp;const targetUrl = url.searchParams.get(\'url\');<br/><br/>'
    '&nbsp;&nbsp;// 1. Fetch the encrypted content from the CDN<br/>'
    '&nbsp;&nbsp;const upstream = await fetch(targetUrl, {<br/>'
    '&nbsp;&nbsp;&nbsp;&nbsp;headers: { \'Referer\': \'https://target-cdn.com/\' }<br/>'
    '&nbsp;&nbsp;});<br/>'
    '&nbsp;&nbsp;const body = Buffer.from(await upstream.arrayBuffer());<br/><br/>'
    '&nbsp;&nbsp;// 2. Detect what type of content it is<br/>'
    '&nbsp;&nbsp;if (isWebP(body)) {<br/>'
    '&nbsp;&nbsp;&nbsp;&nbsp;// XOR decrypt the segment<br/>'
    '&nbsp;&nbsp;&nbsp;&nbsp;const decrypted = xorDecrypt(body.slice(12), KEY);<br/>'
    '&nbsp;&nbsp;&nbsp;&nbsp;res.writeHead(200, { \'Content-Type\': \'video/mp2t\' });<br/>'
    '&nbsp;&nbsp;&nbsp;&nbsp;res.end(decrypted);<br/>'
    '&nbsp;&nbsp;} else if (isBase64M3u8(body)) {<br/>'
    '&nbsp;&nbsp;&nbsp;&nbsp;// Base64 decode + XOR decrypt the playlist<br/>'
    '&nbsp;&nbsp;&nbsp;&nbsp;const decoded = Buffer.from(body.toString(), \'base64\');<br/>'
    '&nbsp;&nbsp;&nbsp;&nbsp;const decrypted = xorDecrypt(decoded, sessionKey);<br/>'
    '&nbsp;&nbsp;&nbsp;&nbsp;// Rewrite URLs to route through this proxy<br/>'
    '&nbsp;&nbsp;&nbsp;&nbsp;const rewritten = rewriteUrls(decrypted.toString(), port);<br/>'
    '&nbsp;&nbsp;&nbsp;&nbsp;res.writeHead(200, { \'Content-Type\': \'application/vnd.apple.mpegurl\' });<br/>'
    '&nbsp;&nbsp;&nbsp;&nbsp;res.end(rewritten);<br/>'
    '&nbsp;&nbsp;} else {<br/>'
    '&nbsp;&nbsp;&nbsp;&nbsp;// Plain content — pass through<br/>'
    '&nbsp;&nbsp;&nbsp;&nbsp;res.writeHead(200);<br/>'
    '&nbsp;&nbsp;&nbsp;&nbsp;res.end(body);<br/>'
    '&nbsp;&nbsp;}<br/>'
    '}).listen(7654);', S_CODE))

story.append(H('6.3 URL Rewriting (Critical)', S_H2, 1))
story.append(Paragraph(
    'The m3u8 playlist contains URLs for segments. If those URLs are encrypted or need '
    'special headers, you must rewrite them to go through your proxy.', S_BODY))
story.append(Paragraph(
    '// Original: https://cdn.example.com/seg-0-f1-v1-a0.webp<br/>'
    '// Rewritten: http://localhost:7654/seg.ts?url=<encoded>&e=.ts<br/><br/>'
    '// The "/seg.ts" path and "&e=.ts" suffix trick ffmpeg into accepting<br/>'
    '// the URL as a .ts file (otherwise it rejects .webp segment extensions)<br/><br/>'
    '// For sub-playlists (video.m3u8, audio.m3u8):<br/>'
    '// http://localhost:7654/playlist.m3u8?url=<encoded>&key=<key>', S_CODE))

story.append(H('6.4 Handling All Content Types', S_H2, 1))
story.append(Paragraph(
    'The proxy must handle: 1) WebP disguised segments, 2) PNG disguised segments, '
    '3) Base64-encoded m3u8 playlists, 4) Plain m3u8 playlists (already decoded), '
    '5) Plain segments (pass through). Detect by checking magic bytes at the start '
    'of the response body.', S_BODY))

# ═══════ CH 7: TROUBLESHOOTING ═══════
story.append(PageBreak())
story.append(H('7. Troubleshooting Guide', S_H1, 0))
story.append(Paragraph(
    'If something is not working, follow this decision tree to find and fix the problem.', S_BODY))

trouble = [
    ['Problem', 'Likely Cause', 'Fix'],
    ['HTTP 403 from site', 'Cloudflare blocking', 'Use curl instead of Node.js HTTPS'],
    ['HTTP 403 from CDN', 'Missing Referer header', 'Add Referer matching the player domain'],
    ['Empty response', 'Rate limited', 'Add delay between requests (500ms+)'],
    ['JSON parse error', 'Response is encrypted', 'Check if response is base64 or XOR'],
    ['WASM instantiation fails', 'Wrong WASM bytes', 'Check base64 decoding is correct'],
    ['AES decrypt throws error', 'Wrong key or IV', 'Verify PBKDF2 + XOR + SHA-256 chain'],
    ['Decrypted data is garbage', 'Wrong XOR key', 'Search JS for hardcoded byte array'],
    ['m3u8 is encrypted text', 'Base64 + XOR encryption', 'Base64 decode, then XOR with session key'],
    ['Segments are .webp files', 'Disguised MPEG-TS', 'Strip 12-byte header, XOR with 16-byte key'],
    ['ffmpeg rejects segment URLs', 'Extension mismatch', 'Use /seg.ts?url=...&e=.ts proxy URL'],
    ['No streams found', 'Site changed API', 'Re-download JS bundles, search for new URLs'],
    ['Stream URL returns 404', 'Token expired', 'Re-fetch fresh token from API'],
    ['VLC shows black screen', 'Missing CORS headers', 'Add Access-Control-Allow-Origin: * to proxy'],
    ['Proxy is slow', 'Synchronous XOR loop', 'Use Buffer.alloc + for loop (faster than spread)'],
]
story.append(tbl(trouble, [38*mm, 45*mm, 72*mm]))

# ═══════ CH 8: QUICK REFERENCE ═══════
story.append(PageBreak())
story.append(H('8. Quick Reference Card', S_H1, 0))

story.append(H('8.1 Decision Tree: What Type of Site Is This?', S_H2, 1))
story.append(Paragraph(
    'IF HTML contains .m3u8 or .mp4 URLs:<br/>'
    '&nbsp;&nbsp;-> Simple site. Extract URLs directly. Done.<br/><br/>'
    'IF HTML contains /api/ endpoints:<br/>'
    '&nbsp;&nbsp;-> API site. Call the API, parse JSON response. Done.<br/><br/>'
    'IF HTML has <iframelt;iframe> with embed URLs:<br/>'
    '&nbsp;&nbsp;-> Embed site. Fetch the iframe page, repeat from top.<br/><br/>'
    'IF HTML has download links (mirror sites):<br/>'
    '&nbsp;&nbsp;-> Download site. Follow each link to find direct file URL.<br/><br/>'
    'IF HTML has no stream URLs and loads JS bundles:<br/>'
    '&nbsp;&nbsp;-> SPA site. Download JS, search for API calls (Ch 3).<br/><br/>'
    'IF JS bundle contains encryption code (AES/XOR/WASM):<br/>'
    '&nbsp;&nbsp;-> Encrypted site. Find keys, decrypt responses (Ch 5).<br/><br/>'
    'IF decrypted response contains encrypted m3u8 + segments:<br/>'
    '&nbsp;&nbsp;-> Fully encrypted. Build a decryption proxy (Ch 6).', S_CODE))

story.append(H('8.2 Tool Selection Guide', S_H2, 1))
tools = [
    ['Task', 'Tool', 'Why'],
    ['Fetch HTML from CF site', 'curl', 'Bypasses JA3 fingerprint'],
    ['Fetch HTML from non-CF site', 'Node.js https', 'Faster, no subprocess'],
    ['Fetch TMDB info', 'Node.js https', 'TMDB has no CF'],
    ['Decode base64', 'Buffer.from(b64, \'base64\')', 'Built-in Node.js'],
    ['XOR decrypt', 'Buffer + for loop', 'Simple and fast'],
    ['AES decrypt', 'crypto.createDecipheriv', 'Built-in Node.js'],
    ['SHA-256 hash', 'crypto.createHash', 'Built-in Node.js'],
    ['PBKDF2 key derivation', 'crypto.pbkdf2Sync', 'Built-in Node.js'],
    ['WASM execution', 'WebAssembly.compile + instantiate', 'Built-in Node.js'],
    ['Local HTTP proxy', 'http.createServer', 'Built-in Node.js'],
    ['Verify stream plays', 'ffprobe / ffmpeg', 'External CLI tool'],
    ['Search JS for patterns', 'grep or regex', 'Find API URLs and keys'],
]
story.append(tbl(tools, [42*mm, 45*mm, 68*mm]))

story.append(H('8.3 Common Header Requirements', S_H2, 1))
headers = [
    ['CDN / Site', 'Required Header', 'Value'],
    ['peakstorm.top (StreamXTV)', 'Referer', 'https://player.videasy.to/'],
    ['img1.*.com (Citadel/Rivestream)', 'Referer', 'https://rivestream.ru/'],
    ['fetch8.flixcloud.cc (ReAnime)', 'Referer', 'https://flixcloud.cc/'],
    ['vault-95.atomic4cdn.top (ReAnime)', 'Referer', 'https://flixcloud.cc/'],
    ['proxy.valhallastream.dpdns.org', 'Referer', 'https://rivestream.ru/'],
    ['pixeldrain.com API', '(none)', 'CORS enabled, any UA works'],
    ['pub-*.r2.dev (KMMovies)', '(none)', 'No auth needed'],
    ['moon.peakstorm.top (Videasy)', 'Referer', 'https://player.videasy.to/'],
]
story.append(tbl(headers, [55*mm, 30*mm, 70*mm]))

# ═══════ CH 9: VERIFICATION ═══════
story.append(PageBreak())
story.append(H('9. How to Verify Your Streams Work', S_H1, 0))
story.append(Paragraph(
    'After extracting stream URLs, always verify they actually play. '
    'This saves you from shipping broken providers.', S_BODY))

story.append(H('9.1 Quick Check with curl', S_H2, 1))
story.append(Paragraph(
    '# Check if the URL returns valid HLS:<br/>'
    'curl -sL -H \'Referer: https://player.videasy.to/\' \'<stream_url>\' | head -1<br/><br/>'
    '# If output starts with "#EXTM3U" -> valid HLS playlist<br/>'
    '# If output starts with binary -> encrypted segment (needs decryption)', S_CODE))

story.append(H('9.2 Full Check with ffprobe', S_H2, 1))
story.append(Paragraph(
    'ffprobe -v error -show_streams \\<br/>'
    '&nbsp;&nbsp;-headers $\'Referer: https://player.videasy.to/\\r\\n\' \\<br/>'
    '&nbsp;&nbsp;\'<stream_url>\'<br/><br/>'
    '# Look for:<br/>'
    '# codec_name=h264 (video codec found)<br/>'
    '# width=1920, height=1080 (resolution matches)<br/>'
    '# codec_name=aac (audio codec found)', S_CODE))

story.append(H('9.3 Play Test with ffmpeg', S_H2, 1))
story.append(Paragraph(
    '# Extract 5 seconds of video to verify end-to-end playback:<br/>'
    'ffmpeg -y \\<br/>'
    '&nbsp;&nbsp;-protocol_whitelist "file,http,https,tcp,tls,crypto,data" \\<br/>'
    '&nbsp;&nbsp;-allowed_extensions ALL \\<br/>'
    '&nbsp;&nbsp;-i "<stream_url>" -t 5 -c copy output.mp4<br/><br/>'
    '# If output.mp4 exists and is > 0 bytes -> stream is playable!', S_CODE))

story.append(H('9.4 Segment Verification (for encrypted streams)', S_H2, 1))
story.append(Paragraph(
    'After decrypting a segment, verify it is valid MPEG-TS by checking sync bytes:', S_BODY))
story.append(Paragraph(
    '// MPEG-TS packets are 188 bytes, each starting with 0x47<br/>'
    'let valid = true;<br/>'
    'for (let i = 0; i < decrypted.length; i += 188) {<br/>'
    '&nbsp;&nbsp;if (decrypted[i] !== 0x47) { valid = false; break; }<br/>'
    '}<br/>'
    'console.log(valid ? \'Valid MPEG-TS\' : \'Decryption failed\');', S_CODE))

# ═══════ CH 10: FULL WORKFLOW ═══════
story.append(PageBreak())
story.append(H('10. Complete Workflow Example', S_H1, 0))
story.append(Paragraph(
    'Here is the complete workflow from start to finish, using a hypothetical encrypted '
    'streaming site as an example.', S_BODY))

steps = [
    ['#', 'Action', 'Tool'],
    ['1', 'Fetch the homepage HTML', 'curl'],
    ['2', 'Search for stream URLs in HTML', 'grep / regex'],
    ['3', 'If no URLs found, download JS bundles', 'curl'],
    ['4', 'Search JS for API endpoints and encryption', 'grep / regex'],
    ['5', 'If encrypted, find keys in JS (XOR array, WASM base64)', 'grep'],
    ['6', 'Call the API to get stream data', 'Node.js https or curl'],
    ['7', 'If response is base64, decode it', 'Buffer.from(b64)'],
    ['8', 'If response is XOR encrypted, decrypt it', 'Buffer XOR loop'],
    ['9', 'If WASM is needed, compile and run it', 'WebAssembly.compile'],
    ['10', 'If AES is needed, derive key and decrypt', 'crypto module'],
    ['11', 'Extract final stream URL from decrypted data', 'JSON.parse'],
    ['12', 'If segments are encrypted, build a proxy', 'http.createServer'],
    ['13', 'Verify the stream plays with ffprobe', 'ffprobe CLI'],
    ['14', 'Package as a provider module', 'module.exports'],
]
story.append(tbl(steps, [10*mm, 65*mm, 80*mm]))

story.append(Spacer(1, 6*mm))
story.append(HRFlowable(width='100%', thickness=1, color=BORDER))
story.append(Spacer(1, 4*mm))
story.append(Paragraph(
    'This guide covers the complete methodology for extracting direct playable streams '
    'from any web source. The techniques are universal — they work on movies, TV shows, '
    'anime, and any content type. The only tools needed are Node.js and curl.', S_MUTED))
story.append(Paragraph(
    'Key principle: The browser is just a JavaScript runtime that makes HTTP requests. '
    'Replicate what the browser does, in pure Node.js, and you never need a browser.', S_MUTED))

# Build
out = '/home/z/my-project/download/Reverse_Engineering_Streaming_Sites.pdf'
doc = TocDoc(out, pagesize=A4, leftMargin=18*mm, rightMargin=18*mm, topMargin=18*mm, bottomMargin=18*mm,
    title='Stream Reverse Engineering - A Practical Guide', author='Z.ai', subject='How to extract direct playable streams', creator='Z.ai')
doc.multiBuild(story, onFirstPage=page_bg, onLaterPages=page_bg)
print(f'PDF: {out} ({doc.page} pages)')
