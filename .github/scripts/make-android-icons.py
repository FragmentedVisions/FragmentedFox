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


write(RES + "/drawable/ic_launcher_foreground.xml", foreground())
write(RES + "/drawable/ic_launcher_monochrome.xml", monochrome())
write(RES + "/drawable/ic_firefox.xml", mark(108))
write(RES + "/drawable/expressive_firefox.xml", mark(35))
write(RES + "/drawable/ic_status_logo.xml", silhouette(24, "@android:color/white"))
write(ICONS + "/mozac_ic_logo_firefox_24.xml", silhouette(24, "@color/mozac_ui_icons_fill"))
print("ok")
