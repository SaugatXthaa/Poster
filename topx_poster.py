"""TOPX / BLVCKTOP poster templates.

A port of the TOPX ranked-cover design onto the Posters Plus render
pipeline, drawn with PIL so every Posters Plus fact (trending rank, the
weighted score, genres, quality tokens, network logos) arrives through the
same request path the classic poster uses.

Two templates:

  topx      — the ranked cover on a canvas.  The canvas styles are the topx
              options:  fresh (full-bleed artwork behind the cover),
              provider (glass gradient in the streamer's colours), nuvio
              (solid black), stremio (Stremio navy) and transparent.
  blvcktop  — solid black canvas, monochrome artwork, the neon ranked
              cover.

The template draws:

  • the ranked cover card (rounded, shadowed artwork),
  • the rank number as a hollow gradient outline with the neon glow,
  • the genre + rating line, stood where topx puts it: bottom (under the
    card, the tuned default), top, or down the left / right edge,
  • the provider logo (the title's network mark, white ink),
  • the Posters Plus quality badges (4K, HDR10+, DV, 1080p, Remux, Web)
    drawn from the same assets and quality facts as the classic poster,
  • the brand tint — an ambient glow in the artwork's dominant colour.

Landscape runs the same design on the 16:9 canvas; the fresh canvas goes
full-bleed banner there, the way the topx landscape rows do.

Everything is positioned in fractions of the canvas, so any resolution the
pipeline hands over lays out identically.
"""

from __future__ import annotations

import os

from PIL import Image, ImageDraw, ImageFilter, ImageFont, ImageOps

# ---------------------------------------------------------------------------
# Geometry — fractions of the canvas, from the TOPX layouts.
# Portrait reference 1000×1500; landscape reference 1280×720.
# ---------------------------------------------------------------------------

_P = {   # portrait
    "card":       (0.210, 0.110, 0.730, 0.730),   # x, y, w, h
    "radius":     0.038,
    "num_size1":  0.405,  "num_x1":  0.014,
    "num_size2":  0.335,  "num_x2": -0.010,
    "stroke":     0.010,
    "meta_cx":    0.575,  "meta_bottom": 0.923, "meta_top": 0.075,
    "side_inset": 0.058,  "side_cy":    0.720,
    "g_size":     (0.048, 0.054, 0.060),        # long / mid / short genre text
    "star_size":  0.068,  "dot_size":   0.042,
    "logo_c":     (0.105, 0.920), "logo_max": (0.180, 0.075),
    "logo_land":  False,
}
_L = {   # landscape
    "card":       (0.2344, 0.1486, 0.7031, 0.7028),
    "radius":     0.0234,
    "num_size1":  0.3516, "num_x1":  0.0484,
    "num_size2":  0.2891, "num_x2":  0.0188,
    "stroke":     0.0086,
    "meta_cx":    0.586,  "meta_bottom": 0.933, "meta_top": 0.085,
    "side_inset": 0.042,  "side_cy":    0.700,
    "g_size":     (0.028, 0.031, 0.034),
    "star_size":  0.039,  "dot_size":   0.027,
    "logo_c":     (0.117, 0.861), "logo_max": (0.156, 0.100),
}

# Inks — the topx meta line's colours.
META_INK    = (241, 245, 249, 255)   # genre text
RATING_INK  = (255, 255, 255, 255)   # score text (or the score palette's)
STAR_INK    = (255, 184, 0, 255)     # the ★
DOT_INK     = (148, 163, 184, 255)   # the • separator
NEON        = (140, 117, 255, 255)   # blvcktop's ranked-cover neon

_STROKE_GRAD = ((255, 255, 255), (241, 242, 244), (201, 205, 211))
_CRYSTAL_GRAD = ((255, 255, 255), (226, 232, 240), (203, 213, 225))

_FONTS_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), "fonts")
_INTER_BOLD = os.path.join(_FONTS_DIR, "Inter-Bold.ttf")

# Canvas styles that exist for the topx template.
CANVAS_STYLES = ("fresh", "provider", "transparent", "nuvio", "stremio")
GENRE_POSITIONS = ("bottom", "top", "left", "right")


# ---------------------------------------------------------------------------
# Small helpers
# ---------------------------------------------------------------------------

def _clamp(v, lo, hi):
    return max(lo, min(hi, v))


def _hex(value, fallback):
    """A 6-digit hex string as an RGB tuple, else the fallback."""
    m = str(value or "").strip().lstrip("#")
    if len(m) == 6:
        try:
            return tuple(int(m[i:i + 2], 16) for i in (0, 2, 4))
        except ValueError:
            pass
    return fallback


def _cover(img: Image.Image, w: int, h: int) -> Image.Image:
    """*img* scaled to cover a w×h box, centre-cropped."""
    sw, sh = img.size
    scale = max(w / sw, h / sh)
    out = img.resize((max(1, round(sw * scale)), max(1, round(sh * scale))),
                     Image.Resampling.LANCZOS)
    left = (out.width - w) // 2
    top = (out.height - h) // 2
    return out.crop((left, top, left + w, top + h))


def _rounded_mask(w: int, h: int, radius: int) -> Image.Image:
    m = Image.new("L", (w, h), 0)
    ImageDraw.Draw(m).rounded_rectangle((0, 0, w - 1, h - 1), radius=radius, fill=255)
    return m


def _vertical_gradient(w: int, h: int, stops, alpha=255) -> Image.Image:
    """A vertical linear gradient through *stops* (RGB tuples)."""
    if len(stops) == 1:
        return Image.new("RGBA", (w, h), (*stops[0], alpha))
    col = Image.new("RGB", (1, h))
    pxv = col.load()
    n = len(stops) - 1
    for y in range(h):
        t = y / max(1, h - 1) * n
        i = min(n - 1, int(t))
        f = t - i
        c1, c2 = stops[i], stops[i + 1]
        pxv[0, y] = tuple(round(c1[k] + (c2[k] - c1[k]) * f) for k in range(3))
    return _set_alpha(col.resize((w, h)).convert("RGBA"), alpha)


def _set_alpha(img: Image.Image, alpha: int) -> Image.Image:
    img = img.copy()
    img.putalpha(Image.new("L", img.size, alpha))
    return img


def _dominant(img: Image.Image) -> tuple[int, int, int]:
    """The artwork's dominant colour, the brand tint's source."""
    small = img.convert("RGB").resize((24, 24))
    pxv = small.load()
    best, best_score = (28, 28, 34), -1.0
    for y in range(24):
        for x in range(24):
            r, g, b = pxv[x, y]
            mx, mn = max(r, g, b), min(r, g, b)
            sat = (mx - mn)
            score = sat * 2 + (255 - abs(mx - 160)) * 0.3
            if score > best_score:
                best_score, best = score, (r, g, b)
    return best


def _radial_glow(w: int, h: int, color, strength: float = 0.55) -> Image.Image:
    """A soft radial glow sheet, bright centre fading out, for the ambient."""
    gw = max(8, w // 4)
    gh = max(8, h // 4)
    glow = Image.new("L", (gw, gh), 0)
    pxv = glow.load()
    cx, cy = gw * 0.5, gh * 0.42
    maxd = (gw * gw + gh * gh) ** 0.5 * 0.62
    for y in range(gh):
        for x in range(gw):
            d = ((x - cx) ** 2 + (y - cy) ** 2) ** 0.5 / maxd
            v = max(0.0, 1.0 - d)
            pxv[x, y] = round(255 * strength * (v * v))
    glow = glow.resize((w, h), Image.Resampling.BILINEAR)
    glow = glow.filter(ImageFilter.GaussianBlur(w * 0.02))
    out = Image.new("RGBA", (w, h), (*color, 0))
    out.putalpha(glow)
    return out


def _shadow(w: int, h: int, radius: int, blur: int, alpha: int = 170) -> Image.Image:
    pad = blur * 2
    sh = Image.new("RGBA", (w + pad * 2, h + pad * 2), (0, 0, 0, 0))
    ImageDraw.Draw(sh).rounded_rectangle(
        (pad, pad, pad + w - 1, pad + h - 1), radius=radius, fill=(0, 0, 0, alpha))
    return sh.filter(ImageFilter.GaussianBlur(blur))


def _load_font(size: int) -> ImageFont.FreeTypeFont:
    """The render's label font — the same resolver the classic pipeline uses,
    so the configurator's Label Font setting reaches the topx meta line (and
    the language fallback / shaping rules apply identically). Falls back to
    the template's Inter when the fonts module isn't reachable (standalone
    use)."""
    try:
        import fonts as _fonts
        return _fonts.label_font(max(8, size))
    except Exception:
        return ImageFont.truetype(_INTER_BOLD, max(8, size))


def _text_w(draw: ImageDraw.ImageDraw, text: str, font) -> float:
    return draw.textlength(text, font=font)


def _star(draw: ImageDraw.ImageDraw, cx: float, cy: float, size: float,
          fill=STAR_INK) -> None:
    """A five-point star, the ★ drawn as a polygon (font-independent)."""
    import math
    pts = []
    for i in range(10):
        ang = -math.pi / 2 + i * math.pi / 5
        r = size if i % 2 == 0 else size * 0.42
        pts.append((cx + r * math.cos(ang), cy + r * math.sin(ang)))
    draw.polygon(pts, fill=fill)


# ---------------------------------------------------------------------------
# Canvas backgrounds
# ---------------------------------------------------------------------------

def _canvas_base(canvas_w: int, canvas_h: int, style: str, art: Image.Image,
                 cfg, accent, backdrop: Image.Image | None = None
                 ) -> tuple[Image.Image, tuple[int, int, int] | None]:
    """The topx canvas behind the ranked cover.

    Returns (canvas, glow_color) — glow_color None means "no ambient glow
    beyond what the canvas already carries".  *backdrop* stands in for *art*
    on the fresh canvas when the pipeline had one: the full-bleed reads the
    title's scene, not the poster doubled behind its own cover.
    """
    if style == "transparent":
        return Image.new("RGBA", (canvas_w, canvas_h), (0, 0, 0, 0)), None
    if style == "nuvio":
        return Image.new("RGBA", (canvas_w, canvas_h), (0, 0, 0, 255)), None
    if style == "provider" or style == "stremio":
        return _glass_canvas(canvas_w, canvas_h, style, art), None
    # fresh — the full-bleed artwork, darkened at the edges so the overlays
    # read, the way the topx fresh canvas sits behind its ranked cover.
    bleed = backdrop if backdrop is not None else art
    base = _cover(bleed.convert("RGB"), canvas_w, canvas_h).convert("RGBA")
    edge = Image.new("L", (1, canvas_h), 0)
    ep = edge.load()
    for y in range(canvas_h):
        t = abs(y - canvas_h / 2) / (canvas_h / 2)
        ep[0, y] = round(150 * (t ** 1.6))
    dark = Image.new("RGBA", (canvas_w, canvas_h), (0, 0, 0, 255))
    dark.putalpha(edge.resize((canvas_w, canvas_h)))
    base.alpha_composite(dark)
    dom = _dominant(backdrop if (backdrop is not None and style == "fresh") else art)
    return base, dom


def _glass_canvas(w: int, h: int, style: str, art: Image.Image) -> Image.Image:
    """The provider / stremio glass gradient, ported from the topx canvas."""
    if style == "stremio":
        atmo = ((54, 44, 122), (30, 24, 74), (12, 10, 34))
        body = ((88, 68, 172), (62, 48, 132), (36, 27, 86), (9, 7, 24))
    else:
        # The streamer's own colours, from the artwork's dominant hue.  A
        # dark or grey poster can't be allowed to make the glass black: the
        # hue keeps, the saturation and value lift to a floor first.
        import colorsys
        r, g, b = _dominant(art)
        hue, li, sat = colorsys.rgb_to_hls(r / 255, g / 255, b / 255)
        sat = max(sat, 0.45)
        li = max(min(li, 0.62), 0.30)
        rr, gg, bb = (round(c * 255) for c in colorsys.hls_to_rgb(hue, li, sat))
        atmo = ((round(rr * 0.42), round(gg * 0.42), round(bb * 0.42)),
                (round(rr * 0.22), round(gg * 0.22), round(bb * 0.22)),
                (round(rr * 0.07), round(gg * 0.07), round(bb * 0.07)))
        body = ((round(rr * 0.72), round(gg * 0.72), round(bb * 0.72)),
                (round(rr * 0.48), round(gg * 0.48), round(bb * 0.48)),
                (round(rr * 0.26), round(gg * 0.26), round(bb * 0.26)),
                (round(rr * 0.06), round(gg * 0.06), round(bb * 0.06)))

    canvas = Image.new("RGBA", (w, h))
    # Atmosphere: a radial wash, bright at a quarter height.
    small = Image.new("RGB", (48, 72))
    sp = small.load()
    for y in range(72):
        for x in range(48):
            dx, dy = (x - 24) / 34.0, (y - 18) / 40.0
            d = min(1.0, (dx * dx + dy * dy) ** 0.5)
            if d < 0.42:
                t, c = d / 0.42, atmo[0]
            elif d < 0.75:
                t, c = (d - 0.42) / 0.33, atmo[1]
            else:
                t, c = min(1.0, (d - 0.75) / 0.25), atmo[2]
            sp[x, y] = tuple(round(c[k] * (1 - t) + atmo[2][k] * t) for k in range(3))
    canvas.paste(small.resize((w, h), Image.Resampling.BILINEAR).convert("RGBA"),
                 (0, 0))

    # Body: an inset rounded sheet flowing from the body gradient into near
    # black, with a soft top sheen and a bright rim.
    pad = round(w * 0.012)
    rx = round(w * 0.032)
    bw, bh = w - pad * 2, h - pad * 2
    body_grad = _vertical_gradient(4, bh, body)
    sheet = Image.new("RGBA", (bw, bh), (0, 0, 0, 0))
    sheet.paste(body_grad, (0, 0))
    mask = _rounded_mask(bw, bh, rx)
    sheet.putalpha(mask.point(lambda a: round(a * 0.92)))
    canvas.alpha_composite(sheet, (pad, pad))

    # Sheen: a diagonal light across the top of the sheet.
    sheen = Image.new("RGBA", (bw, bh), (0, 0, 0, 0))
    sp2 = sheen.load()
    for y in range(0, bh, 2):
        for x in range(0, bw, 2):
            t = 1.0 - _clamp((x / bw + y / bh) / 1.1, 0.0, 1.0)
            a = round(60 * (t ** 2.2))
            if a:
                sp2[x, y] = (255, 255, 255, a)
                if x + 1 < bw:
                    sp2[x + 1, y] = (255, 255, 255, a)
                if y + 1 < bh:
                    sp2[x, y + 1] = (255, 255, 255, a)
                    if x + 1 < bw:
                        sp2[x + 1, y + 1] = (255, 255, 255, a)
    sheen.putalpha(sheen.getchannel("A"))
    sheen_masked = Image.new("RGBA", (bw, bh), (0, 0, 0, 0))
    sheen_masked.paste(sheen, (0, 0), mask)
    canvas.alpha_composite(sheen_masked, (pad, pad))

    # Rim: a bright rounded outline, lighter along the top.
    rim = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    rd = ImageDraw.Draw(rim)
    rd.rounded_rectangle((pad, pad, pad + bw - 1, pad + bh - 1), radius=rx,
                         outline=(255, 255, 255, 110), width=max(2, round(w * 0.004)))
    rd.rounded_rectangle((pad, pad, pad + bw - 1, pad + round(bh * 0.45)), radius=rx,
                         outline=(255, 255, 255, 66), width=max(1, round(w * 0.002)))
    return canvas


# ---------------------------------------------------------------------------
# The ranked cover card
# ---------------------------------------------------------------------------

def _ranked_card(art: Image.Image, L: dict, canvas_w: int, canvas_h: int,
                 greyscale: bool) -> Image.Image:
    """The cover card: rounded artwork with its drop shadow, composited on a
    full-canvas transparent sheet at the layout's card slot."""
    card = L["card"]
    cx, cy = round(card[0] * canvas_w), round(card[1] * canvas_h)
    cw, ch = round(card[2] * canvas_w), round(card[3] * canvas_h)
    radius = round(L["radius"] * canvas_w)

    sheet = Image.new("RGBA", (canvas_w, canvas_h), (0, 0, 0, 0))

    art_c = art.convert("RGB")
    if greyscale:
        art_c = ImageOps.grayscale(art_c).convert("RGB")
    art_c = _cover(art_c, cw, ch).convert("RGBA")

    sh = _shadow(cw, ch, radius, blur=max(10, round(canvas_w * 0.030)), alpha=185)
    sheet.alpha_composite(sh, (cx - sh.width // 2 + cw // 2,
                               cy - sh.height // 2 + ch // 2 + round(canvas_h * 0.010)))
    art_c.putalpha(_rounded_mask(cw, ch, radius))
    sheet.alpha_composite(art_c, (cx, cy))

    # A hairline inner rim, so the cover sits crisp on every canvas.
    rd = ImageDraw.Draw(sheet)
    rd.rounded_rectangle((cx, cy, cx + cw - 1, cy + ch - 1), radius=radius,
                         outline=(255, 255, 255, 28), width=max(1, round(canvas_w * 0.002)))
    return sheet


# ---------------------------------------------------------------------------
# The rank number — hollow gradient outline with the neon glow
# ---------------------------------------------------------------------------

def _rank_number(rank: int, L: dict, canvas_w: int, canvas_h: int,
                 accent, crystal: bool) -> Image.Image | None:
    """The rank as a hollow outlined number with a two-stage glow.

    None when the rank doesn't print (no trending rank, or rank <= 0).
    """
    if not rank or rank <= 0:
        return None
    text = str(rank)
    is_double = len(text) > 1
    size = round((L["num_size2"] if is_double else L["num_size1"]) * canvas_w)
    x = round((L["num_x2"] if is_double else L["num_x1"]) * canvas_w)
    stroke_w = max(2, round(L["stroke"] * canvas_w))

    font = _load_font(size)
    # Measure on a generous sheet; the number hangs left of the card.
    probe = Image.new("L", (canvas_w, canvas_h), 0)
    pd = ImageDraw.Draw(probe)
    bbox = pd.textbbox((0, 0), text, font=font, stroke_width=stroke_w)
    tw, th = bbox[2] - bbox[0], bbox[3] - bbox[1]

    pad = round(size * 0.9)
    mask = Image.new("L", (tw + pad * 2, th + pad * 2), 0)
    md = ImageDraw.Draw(mask)
    ox, oy = pad - bbox[0], pad - bbox[1]
    # Outline mask: glyph + stroke, minus the glyph → the hollow outline.
    md.text((ox, oy), text, font=font, fill=255, stroke_width=stroke_w, stroke_fill=255)
    outline = mask.copy()
    glyph = Image.new("L", mask.size, 0)
    ImageDraw.Draw(glyph).text((ox, oy), text, font=font, fill=255)
    from PIL import ImageChops
    stroke_mask = ImageChops.subtract(outline, glyph)

    sheet = Image.new("RGBA", (canvas_w, canvas_h), (0, 0, 0, 0))
    ny = _num_y(L, canvas_h, canvas_w, th, pad)

    # Glow: two blurred passes of the outline shape in the accent colour.
    for blur_f, alpha in ((0.030, 185), (0.011, 135)):
        gl = stroke_mask.filter(ImageFilter.GaussianBlur(max(2, round(canvas_w * blur_f))))
        tinted = Image.new("RGBA", stroke_mask.size, (*accent[:3], 0))
        tinted.putalpha(gl.point(lambda a: round(a * alpha / 255)))
        sheet.alpha_composite(tinted, (x - pad, ny))

    # The number itself: the vertical gradient through the outline mask.
    ny = _num_y(L, canvas_h, canvas_w, th, pad)
    grad = _vertical_gradient(stroke_mask.size[0], stroke_mask.size[1],
                              _CRYSTAL_GRAD if crystal else _STROKE_GRAD)
    sheet.paste(grad, (x - pad, ny), stroke_mask)
    return sheet


def _num_y(L: dict, canvas_h: int, canvas_w: int, th: int, pad: int) -> int:
    """The number sheet's top so the glyph centres on the card."""
    card = L["card"]
    card_mid = (card[1] + card[3] / 2) * canvas_h
    return round(card_mid - (pad + th / 2))


# ---------------------------------------------------------------------------
# The genre + rating line
# ---------------------------------------------------------------------------

def _score_text(score, cfg) -> str:
    """The weighted score as the topx line prints it.

    An empty weighted score (no key, no rating) prints nothing: the topx
    line drops the rating instead of carrying an empty stub.

    The weighted score travels on the 0-100 scale every normaliser lands on
    (SCORE_NORMALISERS), but the topx look prints it out of ten — "★ 7.6",
    the way the topx covers do — so a numeric score always divides by ten
    here, whatever the classic modes' score_out_of_10 says. That switch
    stays meaningful for the classic templates; the ranked cover has its
    own presentation.
    """
    if isinstance(score, str):
        s = score.strip()
        if not s or s in ("N/A", "—", "-"):
            return ""
        return s
    if isinstance(score, (int, float)):
        return f"{score / 10:.1f}"
    return str(score)


def _draw_meta(canvas: Image.Image, L: dict, genre: str, rating: str,
               position: str, star: bool, meta_ink, rating_ink,
               accent) -> None:
    """The genre + rating line, stood where topx puts it."""
    draw = ImageDraw.Draw(canvas)
    canvas_w, canvas_h = canvas.size

    # Compose the line once, whatever the position.
    segs = []
    if genre:
        n = len(genre)
        gs = L["g_size"][0] if n > 14 else (L["g_size"][1] if n > 10 else L["g_size"][2])
        segs.append((genre, gs, meta_ink))
    if genre and rating:
        segs.append(("\u00A0\u00A0\u2022\u00A0\u00A0", L["dot_size"], DOT_INK))
    if rating:
        segs.append((rating, L["star_size"], rating_ink))
    if not segs:
        return

    # A line too long for the canvas shortens: the side strips have only the
    # canvas height to run down, the horizontal ones the canvas width.
    fonts_sizes = [(t, _load_font(round(fs * canvas_w)), ink) for t, fs, ink in segs]
    widths = [_text_w(draw, t, f) for t, f, _ in fonts_sizes]
    star_w = L["star_size"] * canvas_w * 0.62   # room the vector star takes
    limit = (canvas_h if position in ("left", "right") else canvas_w) * 0.86
    run = sum(widths) + (star_w if star and rating else 0)
    if run > limit > 0:
        k = limit / run
        fonts_sizes = [(t, _load_font(max(8, round(f.size * k))), ink)
                       for t, f, ink in fonts_sizes]
        widths = [_text_w(draw, t, f) for t, f, _ in fonts_sizes]
        star_w *= k
    total = sum(widths)

    def _draw_row(cx: float, y: float, rotate: int = 0) -> None:
        """The line centred on (cx, y); rotate ±90 stands it up an edge."""
        if rotate:
            line_w = round(total + (star_w if star and rating else 0)) + 8
            line_h = round(max(f.size for _t, f, _i in fonts_sizes) * 1.4)
            strip = Image.new("RGBA", (line_w, line_h), (0, 0, 0, 0))
            _row_into(strip, 0, line_h * 0.30, fonts_sizes, widths, star, star_w)
            strip = strip.rotate(rotate, expand=True, resample=Image.Resampling.BICUBIC)
            # A soft shadow reads the strip on bright artwork.
            black = Image.new("RGBA", strip.size, (0, 0, 0, 0))
            black.putalpha(strip.getchannel("A").point(lambda a: round(a * 0.65)))
            shadow = black.filter(ImageFilter.GaussianBlur(max(1.5, canvas_w * 0.003)))
            canvas.alpha_composite(shadow, (round(cx - strip.width / 2 + canvas_w * 0.003),
                                            round(y - strip.height / 2 + canvas_w * 0.003)))
            canvas.alpha_composite(black, (round(cx - strip.width / 2 + canvas_w * 0.001),
                                           round(y - strip.height / 2 + canvas_w * 0.001)))
            canvas.alpha_composite(strip, (round(cx - strip.width / 2),
                                           round(y - strip.height / 2)))
            return
        # Horizontal: shadow drawn straight onto the canvas.
        x = cx - (total + (star_w if star and rating else 0)) / 2
        _row_into(canvas, x, y, fonts_sizes, widths, star, star_w, shadow=True,
                  shadow_dx=round(canvas_w * 0.002))

    def _row_into(target, x, y, rows, row_w, with_star, s_w, shadow=False, shadow_dx=0):
        td = ImageDraw.Draw(target)
        for (text, font, ink), w in zip(rows, row_w):
            if with_star and text == rating and star:
                # The star drawn first, then the score.  The star is a
                # polygon (font-independent), centred on the digits' optical
                # middle: PIL draws from the ascender top, whose centre sits
                # a little over half the font size down.
                cy = y + font.size * 0.56
                if shadow:
                    _star(td, x + s_w / 2 + shadow_dx, cy + canvas_w * 0.002,
                          s_w * 0.5, fill=(0, 0, 0, 190))
                _star(td, x + s_w / 2, cy, s_w * 0.5)
                x += s_w
                if shadow:
                    td.text((x + shadow_dx, y + canvas_w * 0.002), text, font=font,
                            fill=(0, 0, 0, 200))
                td.text((x, y), text, font=font, fill=ink)
            else:
                if shadow:
                    td.text((x + shadow_dx, y + canvas_w * 0.002), text, font=font,
                            fill=(0, 0, 0, 200))
                td.text((x, y), text, font=font, fill=ink)
            x += w

    # Baseline: PIL draws from the ascender top; nudge so the row's optical
    # centre sits on the anchor.
    asc = max(f.size for _t, f, _i in fonts_sizes) * 0.78

    if position == "top":
        _draw_row(L["meta_cx"] * canvas_w, L["meta_top"] * canvas_h)
    elif position == "bottom":
        _draw_row(L["meta_cx"] * canvas_w, L["meta_bottom"] * canvas_h - asc)
    elif position == "left":
        _draw_row(L["side_inset"] * canvas_w, L["side_cy"] * canvas_h, rotate=90)
    elif position == "right":
        _draw_row(canvas_w - L["side_inset"] * canvas_w, L["side_cy"] * canvas_h,
                  rotate=-90)


# ---------------------------------------------------------------------------
# Provider logo + quality badges
# ---------------------------------------------------------------------------

def _draw_provider_logo(canvas: Image.Image, L: dict, logo_mark: Image.Image | None,
                        glow: bool = True) -> None:
    """The network mark, white ink, at the topx logo slot, with a soft shadow."""
    if logo_mark is None:
        return
    canvas_w, canvas_h = canvas.size
    lw, lh = L["logo_max"][0] * canvas_w, L["logo_max"][1] * canvas_h
    scale = min(lw / logo_mark.width, lh / logo_mark.height)
    mark = logo_mark.resize((max(1, round(logo_mark.width * scale)),
                             max(1, round(logo_mark.height * scale))),
                            Image.Resampling.LANCZOS)
    lx = round(L["logo_c"][0] * canvas_w - mark.width / 2)
    ly = round(L["logo_c"][1] * canvas_h - mark.height / 2)
    if glow:
        sh = mark.getchannel("A").filter(ImageFilter.GaussianBlur(max(2, mark.width // 22)))
        shadow = Image.new("RGBA", mark.size, (0, 0, 0, 0))
        shadow.putalpha(sh.point(lambda a: round(a * 0.8)))
        canvas.alpha_composite(shadow, (lx + round(canvas_w * 0.002),
                                        ly + round(canvas_w * 0.003)))
    canvas.alpha_composite(mark, (lx, ly))


def _draw_quality_badges(canvas: Image.Image, tokens: list[str], cfg,
                         genre_position: str, badge_h_px: int) -> None:
    """The Posters Plus quality marks — 4K, HDR10+, DV, 1080p, Remux, Web —
    the same assets the classic poster draws, on the topx canvas."""
    if not tokens:
        return
    try:
        from quality import get_resized_badge
        from main import _score_points
    except Exception:
        return
    try:
        if _score_points(tokens) < (cfg.badge_min_score or 0):
            return
    except Exception:
        pass
    allowed = {"4K", "1080P", "REMUX", "WEBDL", "DV", "HDR10+", "HDR10"}
    marks = []
    for t in tokens:
        if t not in allowed:
            continue
        try:
            m = get_resized_badge(t, badge_h_px)
        except Exception:
            m = None
        if m is not None:
            marks.append(m)
    if not marks:
        return
    gap = max(4, round(badge_h_px * 0.25))
    total = sum(m.width for m in marks) + gap * (len(marks) - 1)
    canvas_w, canvas_h = canvas.size
    inset = round(canvas_w * 0.040)
    if genre_position == "top":
        x = round((canvas_w - total) / 2)
        y = canvas_h - badge_h_px - round(canvas_h * 0.035)
    else:
        x = canvas_w - total - inset
        y = round(canvas_h * 0.035)
    for m in marks:
        canvas.alpha_composite(m, (x, y))
        x += m.width + gap


# ---------------------------------------------------------------------------
# Entry point
# ---------------------------------------------------------------------------

def build_topx(image: Image.Image, score, genre: str, cfg, **bp) -> Image.Image:
    """The topx / blvcktop poster, portrait or landscape by the canvas aspect.

    Called from the request pipeline in build_poster's place when the URL
    asks for one of these templates, with the same argument shape.
    """
    image = image.convert("RGBA")
    canvas_w, canvas_h = image.size
    landscape = canvas_w > canvas_h
    L = _L if landscape else _P

    template = (getattr(cfg, "template", "") or "").lower()
    blvcktop = template == "blvcktop"
    style = "nuvio" if blvcktop else \
        (getattr(cfg, "topx_canvas", "") or "fresh").lower()
    if style not in CANVAS_STYLES:
        style = "fresh"
    position = (getattr(cfg, "topx_genre_position", "") or "bottom").lower()
    if position not in GENRE_POSITIONS:
        position = "bottom"

    # The genre line's facts, from the same switches the classic modes read.
    hide_genre = bool(getattr(cfg, "hide_genre", False))
    hide_rating = bool(getattr(cfg, "hide_rating", False))
    mode_hidden = getattr(cfg, "rating_display_mode", None) == 0
    genre_text = "" if hide_genre else (genre or "").strip()
    rating_text = "" if (hide_rating or mode_hidden) else _score_text(score, cfg)
    star = not (hide_rating or mode_hidden)

    # Rating ink: the score palette's first colour leads when the classic
    # score colour modes ask for it, the star stays gold.
    meta_ink, rating_ink = META_INK, RATING_INK
    try:
        if getattr(cfg, "score_color_mode", 0) in (1, 3) \
                and getattr(cfg, "score_custom_palette", ""):
            first = str(cfg.score_custom_palette).split(",")[0].strip().lstrip("#")
            if len(first) == 6:
                rating_ink = (*_hex(first, RATING_INK[:3]), 255)
    except Exception:
        pass

    # The accent: blvcktop's neon; otherwise the dominant colour drives the
    # glow on the dark canvases and the crystal rim leads on the glass.
    if blvcktop:
        accent = NEON
        crystal = False
    elif style == "provider":
        accent = (255, 255, 255, 255)
        crystal = True
    else:
        accent = (*_dominant(image), 255)
        crystal = False

    canvas, dom = _canvas_base(canvas_w, canvas_h, style, image, cfg, accent,
                               backdrop=bp.get("backdrop"))

    # Brand tint — the artwork's dominant colour as an ambient wash.  Fresh
    # always carries it (it is the design); the solid canvases opt in.
    if dom is not None and (style == "fresh" or getattr(cfg, "topx_brand_tint", False)):
        canvas.alpha_composite(_radial_glow(canvas_w, canvas_h, dom[:3],
                                            strength=0.5 if style == "fresh" else 0.42))

    # The ranked cover.  The fresh landscape goes banner: the art fills the
    # canvas already, so no card floats on it.
    if not (landscape and style == "fresh"):
        canvas.alpha_composite(
            _ranked_card(image, L, canvas_w, canvas_h, greyscale=blvcktop))

    # Rank number, from the same trending facts the classic rank draws.
    rank = None
    discovery_meta = bp.get("discovery_meta")
    if discovery_meta is not None:
        try:
            from main import shown_trending_rank
            rank = shown_trending_rank(discovery_meta, cfg.sash_priority)
        except Exception:
            rank = None
    number = _rank_number(rank, L, canvas_w, canvas_h, accent, crystal)
    if number is not None:
        canvas.alpha_composite(number)

    # Provider logo — the title's network mark (studio as the fallback).
    if getattr(cfg, "topx_provider_logo", True):
        logos = bp.get("badge_logos") or (None, None)
        mark = None
        for logo in logos[:2]:
            if logo is None:
                continue
            try:
                import graphic_badges
                mark = graphic_badges._logo_mark(
                    logo, max(10, round(L["logo_max"][1] * canvas_h * 0.72)))
            except Exception:
                mark = None
            if mark is not None:
                break
        _draw_provider_logo(canvas, L, mark)

    # Quality badges — the classic badge assets at the topx slot.
    tokens = bp.get("quality_tokens") or []
    if getattr(cfg, "topx_badges", True) and tokens:
        _badge_scale = getattr(cfg, "badge_height", 42) / 42.0
        _draw_quality_badges(
            canvas, tokens, cfg, position,
            badge_h_px=max(14, round(canvas_h * 0.084 * _clamp(_badge_scale, 0.4, 3.0))),
        )

    # The genre + rating line.
    _draw_meta(canvas, L, genre_text, rating_text, position, star, meta_ink,
               rating_ink, accent)

    return canvas
