import os
import re

ROOT = os.environ.get("FF_ROOT", os.getcwd())
SRC = ROOT + "/browser/branding/fragmentedfox/source/icon-small.svg"
RES = ROOT + "/mobile/android/fenix/app/src/main/res"
ICONS = ROOT + "/mobile/android/android-components/components/ui/icons/src/main/res/drawable"

LICENSE = """<!-- This Source Code Form is subject to the terms of the Mozilla Public
   - License, v. 2.0. If a copy of the MPL was not distributed with this
   - file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
"""

svg = open(SRC).read()
polys = [
    ([tuple(map(float, p.split(","))) for p in pts.split()], fill)
    for pts, fill in re.findall(r'<polygon points="([^"]+)" fill="(#[0-9A-Fa-f]{6})"', svg)
]
eyes = re.findall(r'<path d="([^"]+)"', svg)
assert len(polys) == 10 and len(eyes) == 2


def fmt(v):
    return ("%.2f" % v).rstrip("0").rstrip(".")


def poly_path(points, tx):
    pts = [tx(x, y) for x, y in points]
    return "M" + " L".join(f"{fmt(x)},{fmt(y)}" for x, y in pts) + " Z"


def eye_path(d, tx):
    nums = re.findall(r"[MQZ]|-?\d+(?:\.\d+)?", d)
    out = []
    i = 0
    while i < len(nums):
        t = nums[i]
        if t == "Z":
            out.append("Z")
            i += 1
            continue
        i += 1
        n = 2 if t == "M" else 4
        vals = list(map(float, nums[i:i + n]))
        i += n
        pairs = [tx(vals[k], vals[k + 1]) for k in range(0, n, 2)]
        out.append(t + " ".join(f"{fmt(x)},{fmt(y)}" for x, y in pairs))
    return " ".join(out)


def vector(width, height, vw, vh, body, extra=""):
    return (
        LICENSE
        + f'<vector xmlns:android="http://schemas.android.com/apk/res/android"{extra}\n'
        + f'    android:width="{width}dp"\n    android:height="{height}dp"\n'
        + f'    android:viewportWidth="{vw}"\n    android:viewportHeight="{vh}">\n'
        + body
        + "</vector>\n"
    )


def path(data, fill=None, stroke=None, stroke_w=None, alpha=None, extra=""):
    s = "    <path\n"
    if fill:
        s += f'        android:fillColor="{fill}"\n'
    if alpha is not None:
        s += f'        android:fillAlpha="{alpha}"\n'
    if stroke:
        s += f'        android:strokeColor="{stroke}"\n        android:strokeWidth="{stroke_w}"\n        android:strokeLineJoin="round"\n'
    s += extra
    s += f'        android:pathData="{data}" />\n'
    return s


def write(p, text):
    open(p, "w").write(text)


def foreground():
    s = 0.31
    tx = lambda x, y: ((x - 128) * s + 54, (y - 133) * s + 54)
    body = ""
    for pts, fill in polys:
        body += path(poly_path(pts, tx), fill=fill, stroke="#050505", stroke_w=fmt(7 * s))
    for d in eyes:
        body += path(eye_path(d, tx), fill="#00FF88")
    return vector(108, 108, 108, 108, body)


def monochrome():
    s = 0.31
    tx = lambda x, y: ((x - 128) * s + 54, (y - 133) * s + 54)
    alphas = {"#FF4757": "1", "#D4122A": "0.7", "#A80E21": "0.45", "#9E8FB2": "0.7"}
    body = ""
    for pts, fill in polys:
        body += path(poly_path(pts, tx), fill="#000000", alpha=alphas[fill.upper()])
    for d in eyes:
        body += path(eye_path(d, tx), fill="#000000")
    return vector(108, 108, 108, 108, body)


def mark(size, with_disc=True):
    tx = lambda x, y: (x, y)
    body = ""
    if with_disc:
        body += path("M128,4 A124,124 0 1 1 128,252 A124,124 0 1 1 128,4 Z", fill="#050505")
    for pts, fill in polys:
        body += path(poly_path(pts, tx), fill=fill, stroke="#050505", stroke_w="7")
    for d in eyes:
        body += path(eye_path(d, tx), fill="#00FF88")
    return vector(size, size, 256, 256, body)


def silhouette(size, fill, tools=False):
    tx = lambda x, y: (x, y)
    extra = ""
    body = ""
    for pts, _ in polys:
        body += path(poly_path(pts, tx), fill=fill, stroke=fill, stroke_w="7")
    return vector(size, size, 256, 256, body)



BASE = {"#FF4757": "#FF4757", "#D4122A": "#D4122A", "#A80E21": "#A80E21", "#9E8FB2": "#9E8FB2"}
LIGHTER = {"#FF4757": "#9E8FB2", "#D4122A": "#6C5C85", "#A80E21": "#4B3F63", "#9E8FB2": "#D4CDE0"}
TERMINAL = {"#FF4757": "#00FF88", "#D4122A": "#00C26E", "#A80E21": "#008C4F", "#9E8FB2": "#00D97A"}
EMBER = {"#FF4757": "#FFD23F", "#D4122A": "#FF7A1A", "#A80E21": "#D9261C", "#9E8FB2": "#FFB347"}
NEON = {"#FF4757": "#00E5FF", "#D4122A": "#00A8C6", "#A80E21": "#6A3DFF", "#9E8FB2": "#FF2E93"}
RAINBOW = ["#E40303", "#FF8C00", "#FFED00", "#008026", "#004DFF", "#750787"]


def centroid_y(points):
    return sum(y for _, y in points) / len(points)


def fox(tx, k, palette=None, colors=None, stroke="#050505", eye="#00FF88", flat=None, outline=None):
    body = ""
    order = sorted(range(len(polys)), key=lambda i: centroid_y(polys[i][0]))
    rank = {i: r for r, i in enumerate(order)}
    for i, (pts, fill) in enumerate(polys):
        data = poly_path(pts, tx)
        if outline:
            body += path(data, stroke=outline, stroke_w=fmt(5 * k))
        elif flat:
            body += path(data, fill=flat, stroke=flat, stroke_w=fmt(7 * k))
        else:
            color = colors[rank[i] * len(colors) // len(polys)] if colors else palette[fill.upper()]
            body += path(data, fill=color, stroke=stroke, stroke_w=fmt(7 * k))
    for d in eyes:
        body += path(eye_path(d, tx), fill=eye)
    return body


def compose(mode, builder):
    if mode == "fg":
        s = 1.24
        return vector(432, 432, 432, 432, builder(lambda x, y: ((x - 128) * s + 216, (y - 133) * s + 216), s))
    return vector(432, 432, 256, 256, builder(lambda x, y: (x, y), 1))


def quad(p0, p1, p2, steps=6):
    return [
        ((1 - t) ** 2 * p0[0] + 2 * (1 - t) * t * p1[0] + t * t * p2[0],
         (1 - t) ** 2 * p0[1] + 2 * (1 - t) * t * p1[1] + t * t * p2[1])
        for t in [i / steps for i in range(steps)]
    ]


def pixel_fox(tx, k, cells=24):
    from PIL import Image, ImageDraw
    scale = 8
    size = 256 * scale
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    for pts, fill in polys:
        draw.polygon([(x * scale, y * scale) for x, y in pts], fill=fill)
    for d in eyes:
        n = [float(v) for v in re.findall(r"-?\d+(?:\.\d+)?", d)]
        a, b, c, e, f = (n[0], n[1]), (n[2], n[3]), (n[4], n[5]), (n[6], n[7]), (n[8], n[9])
        pts = quad(a, b, c) + quad(c, e, f) + [f]
        draw.polygon([(x * scale, y * scale) for x, y in pts], fill="#00FF88")
    step = 256 / cells
    grid = {}
    for row in range(cells):
        for col in range(cells):
            r, g, b, a = img.getpixel((int((col + 0.5) * step * scale), int((row + 0.5) * step * scale)))
            if a > 128:
                grid.setdefault("#%02X%02X%02X" % (r, g, b), []).append((col, row))
    body = ""
    for color, cell_list in grid.items():
        data = ""
        for row in sorted({r for _, r in cell_list}):
            cols = sorted(c for c, r in cell_list if r == row)
            start = prev = cols[0]
            for c in cols[1:] + [None]:
                if c is not None and c == prev + 1:
                    prev = c
                    continue
                x0, y0 = tx(start * step, row * step)
                x1, y1 = tx((prev + 1) * step, (row + 1) * step)
                data += f"M{fmt(x0)},{fmt(y0)}H{fmt(x1)}V{fmt(y1)}H{fmt(x0)}Z"
                if c is not None:
                    start = prev = c
        body += path(data, fill=color)
    return body


def duo(tx, k):
    scale = 0.64
    left = lambda x, y: tx((x - 128) * scale + 128 - 50, (y - 133) * scale + 133)
    right = lambda x, y: tx(128 - (x - 128) * scale + 50, (y - 133) * scale + 133)
    return fox(left, k * scale, BASE) + fox(right, k * scale, LIGHTER)


VARIANTS = {
    "retro_2004": lambda tx, k: fox(tx, k, TERMINAL, eye="#050505"),
    "pixelated": pixel_fox,
    "cuddling": duo,
    "pride": lambda tx, k: fox(tx, k, colors=RAINBOW, eye="#050505"),
    "flaming": lambda tx, k: fox(tx, k, EMBER, eye="#050505"),
    "minimal": lambda tx, k: fox(tx, k, flat="#FFFFFF", eye="#050505"),
    "momo": lambda tx, k: fox(tx, k, outline="#FFFFFF"),
    "cool": lambda tx, k: fox(tx, k, NEON, stroke="#0B0420", eye="#FFFFFF"),
}

AAPT = ' xmlns:aapt="http://schemas.android.com/aapt"'


def gradient_background(top, bottom):
    body = (
        '    <path android:pathData="M0,0h432v432h-432z">\n'
        '        <aapt:attr name="android:fillColor">\n'
        '            <gradient android:startX="216" android:startY="0" android:endX="216" android:endY="432" android:type="linear">\n'
        f'                <item android:offset="0" android:color="{top}"/>\n'
        f'                <item android:offset="1" android:color="{bottom}"/>\n'
        '            </gradient>\n'
        '        </aapt:attr>\n'
        '    </path>\n'
    )
    return vector(432, 432, 432, 432, body, AAPT)


for name, builder in VARIANTS.items():
    write(RES + f"/drawable/ic_launcher_foreground_{name}.xml", compose("fg", builder))
    write(RES + f"/drawable/ic_{name}.xml", compose("preview", builder))
write(RES + "/drawable/ic_launcher_background_cuddling.xml", gradient_background("#2A0A1E", "#050505"))
write(RES + "/drawable/ic_launcher_background_cool.xml", gradient_background("#1A0B3B", "#05010F"))

write(RES + "/drawable/ic_launcher_foreground.xml", foreground())
write(RES + "/drawable/ic_launcher_monochrome.xml", monochrome())
write(RES + "/drawable/ic_firefox.xml", mark(108))
write(RES + "/drawable/expressive_firefox.xml", mark(35))
write(RES + "/drawable/ic_status_logo.xml", silhouette(24, "@android:color/white"))
write(ICONS + "/mozac_ic_logo_firefox_24.xml", silhouette(24, "@color/mozac_ui_icons_fill"))


def illustration(width, height):
    # The fox without its disc, scaled to fit inside width x height dp without stretching.
    s = min(width, height) / 256 * 0.92
    ox = (width - 256 * s) / 2
    oy = (height - 256 * s) / 2
    tx = lambda x, y: (x * s + ox, y * s + oy)
    body = ""
    for pts, fill in polys:
        body += path(poly_path(pts, tx), fill=fill, stroke="#050505", stroke_w=fmt(7 * s))
    for d in eyes:
        body += path(eye_path(d, tx), fill="#00FF88")
    return vector(width, height, width, height, body)


# Illustrations that were Mozilla's fox artwork. The names stay the same so Mozilla's code keeps
# working and ESR merges stay small; only the pictures are ours.
for name, folder, w, h in [
    ("firefox_as_default_banner_illustration", "drawable-nodpi", 78, 53),
    ("illustration_fox_box_inside_light", "drawable", 176, 160),
    ("fox_exclamation_alert", "drawable", 200, 200),
    ("fox_alert_crash_light", "drawable", 156, 137),
    ("fox_alert_crash_dark", "drawable", 155, 137),
    ("fox_ai_on_state", "drawable", 62, 63),
]:
    write(RES + f"/{folder}/{name}.xml", illustration(w, h))

def fox_body(x0, y0, w, h):
    s = min(w, h) / 256 * 0.92
    ox = x0 + (w - 256 * s) / 2
    oy = y0 + (h - 256 * s) / 2
    tx = lambda x, y: (x * s + ox, y * s + oy)
    body = ""
    for pts, fill in polys:
        body += path(poly_path(pts, tx), fill=fill, stroke="#050505", stroke_w=fmt(7 * s))
    for d in eyes:
        body += path(eye_path(d, tx), fill="#00FF88")
    return body


def rounded(x, y, w, h, r):
    return (f"M{fmt(x + r)},{fmt(y)}h{fmt(w - 2 * r)}a{fmt(r)},{fmt(r)} 0 0 1 {fmt(r)},{fmt(r)}"
            f"v{fmt(h - 2 * r)}a{fmt(r)},{fmt(r)} 0 0 1 -{fmt(r)},{fmt(r)}h-{fmt(w - 2 * r)}"
            f"a{fmt(r)},{fmt(r)} 0 0 1 -{fmt(r)},-{fmt(r)}v-{fmt(h - 2 * r)}a{fmt(r)},{fmt(r)} 0 0 1 {fmt(r)},-{fmt(r)}z")


def tab_groups(width, height, fox_box, grid):
    gx, gy, tile, gap = grid
    body = path(rounded(gx - 6, gy - 6, 2 * tile + gap + 12, 2 * tile + gap + 12, 8), fill="#F2EBFF")
    for i, color in enumerate(["#FFB587", "#D9C2FF", "#BC8FFF", "#FFD5B2"]):
        x = gx + (i % 2) * (tile + gap)
        y = gy + (i // 2) * (tile + gap)
        body += path(rounded(x, y, tile, tile, 4), fill=color)
    body += fox_body(*fox_box)
    return vector(width, height, width, height, body)


# More of Mozilla's Kit artwork, replaced the same way under the same names.
for name, folder, w, h in [
    ("kit_head_protection_blocker_banner", "drawable", 78, 78),
    ("protection_panel_not_protected", "drawable", 80, 80),
    ("protection_panel_not_secure", "drawable", 100, 100),
    ("kit_expressive_full", "drawable", 66, 62),
    ("ic_kit_bookmarks_empty_state", "drawable", 200, 178),
    ("bookmarks_folder_illustration", "drawable", 200, 188),
    ("kit_sleeping_under_laptop", "drawable", 180, 103),
]:
    write(RES + f"/{folder}/{name}.xml", illustration(w, h))
write(RES + "/drawable/mozac_ic_kit_tab_groups.xml", tab_groups(144, 80, (4, 0, 80, 80), (86, 16, 22, 4)))
write(RES + "/drawable/mozac_ic_kit_tab_groups_list_view.xml", tab_groups(78, 68, (0, 0, 40, 40), (30, 22, 18, 4)))


def offline_svg():
    s = 118 / 256 * 0.92
    ox = (207 - 256 * s) / 2
    oy = 4
    tx = lambda x, y: (x * s + ox, y * s + oy)
    body = ""
    for pts, fill in polys:
        body += f'<path d="{poly_path(pts, tx)}" fill="{fill}" stroke="#050505" stroke-width="{fmt(7 * s)}" stroke-linejoin="round"/>'
    for d in eyes:
        body += f'<path d="{eye_path(d, tx)}" fill="#00FF88"/>'
    cable = "M34,122C52,146 88,148 108,128S156,118 174,132"
    plugs = ""
    for x, y, rot in ((22, 118, -25), (186, 134, 20)):
        plugs += (f'<g transform="translate({x} {y}) rotate({rot})"><rect x="-9" y="-7" width="18" height="14" rx="3" '
                  f'fill="#8FA8F0" stroke="#050505" stroke-width="3"/><rect x="-14" y="-4" width="5" height="3" fill="#050505"/>'
                  f'<rect x="-14" y="1" width="5" height="3" fill="#050505"/></g>')
    wire = (f'<path d="{cable}" fill="none" stroke="#050505" stroke-width="9" stroke-linecap="round"/>'
            f'<path d="{cable}" fill="none" stroke="#8FA8F0" stroke-width="5" stroke-linecap="round"/>')
    return ('<svg viewBox="0 0 207 169" fill="none" xmlns="http://www.w3.org/2000/svg">' + wire + plugs + body + "</svg>\n")


write(ROOT + "/mobile/android/android-components/components/ui/icons/src/main/assets/mozac_error_no_internet_connection.svg",
      offline_svg())

print("ok")
