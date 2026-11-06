# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

"""Applies the Fragmented Visions colour palette to the Android app.

Safe to run again on an unchanged tree and on a fresh Mozilla tree after an upgrade.
It fails loudly when a file no longer looks the way it expects.
"""

import colorsys
import os
import re
import sys

ROOT = os.environ.get("FF_ROOT", os.getcwd())
AC = ROOT + "/mobile/android/android-components"
FENIX = ROOT + "/mobile/android/fenix/app/src/main"
COLORS_RES = AC + "/components/ui/colors/src/main/res/values"
COLORS_KT = AC + "/components/ui/colors/src/main/java/mozilla/components/ui/colors"
ACORN = AC + "/components/compose/base/src/main/java/mozilla/components/compose/base/theme"

LICENSE_XML = """<?xml version="1.0" encoding="utf-8"?>
<!-- This Source Code Form is subject to the terms of the Mozilla Public
   - License, v. 2.0. If a copy of the MPL was not distributed with this
   - file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
"""

# name -> #AARRGGBB. The first block is the Fragmented Visions palette, the rest are tints and
# shades derived from it for containers, light surfaces and contrast.
BRAND = {
    "Void": "FF000000",
    "Charcoal0": "FF0A0A0A",
    "Charcoal": "FF0E0E0E",
    "Charcoal2": "FF151515",
    "Charcoal3": "FF1D1D1D",
    "Line": "FF2B2B2B",
    "Ash": "FFA6A6A6",
    "SoftWhite": "FFF5F5F5",
    "SignalRed": "FFD4122A",
    "BrightRed": "FFFF4757",
    "TerminalGreen": "FF00FF88",
    "Lavender": "FF9E8FB2",
    "White": "FFFFFFFF",
    "Grey600": "FF6B6B6B",
    "Grey500": "FF8A8A8A",
    "AshDark": "FF5C5C5C",
    "Grey300": "FFD9D9D9",
    "Grey200": "FFE3E3E3",
    "Grey100": "FFECECEC",
    "Grey050": "FFFAFAFA",
    "RedDeep": "FFA80E21",
    "RedDark": "FF5A0A14",
    "RedTint": "FFFFD9DD",
    "GreenDeep": "FF007A45",
    "LavenderLight": "FFB9AECB",
    "LavenderDeep": "FF6B5E80",
    "LavenderDark": "FF3A3347",
    "LavenderInk": "FF2A2533",
    "LavenderTint": "FFE7E2EE",
    "LavenderA55": "8C9E8FB2",
    "AshA80": "CCA6A6A6",
    "PrivateBg": "FF0A080D",
    "PrivateSurface": "FF110F15",
    "PrivateVariant": "FF1B1820",
    "PrivateLine": "FF26222D",
}

# Roles per theme. Values are BRAND names.
DARK = {
    "primary": "BrightRed", "onPrimary": "Void", "primaryContainer": "SignalRed",
    "onPrimaryContainer": "SoftWhite", "inversePrimary": "SignalRed",
    "secondary": "Ash", "onSecondary": "Void", "secondaryContainer": "Line",
    "onSecondaryContainer": "SoftWhite",
    "tertiary": "Lavender", "onTertiary": "Void", "tertiaryContainer": "LavenderInk",
    "onTertiaryContainer": "SoftWhite",
    "background": "Void", "onBackground": "SoftWhite", "surface": "Charcoal", "onSurface": "SoftWhite",
    "surfaceVariant": "Charcoal2", "onSurfaceVariant": "Ash",
    "inverseSurface": "SoftWhite", "inverseOnSurface": "Void",
    "error": "BrightRed", "onError": "Void", "errorContainer": "RedDark", "onErrorContainer": "SoftWhite",
    "information": "Lavender", "onInformation": "Void", "informationContainer": "LavenderInk",
    "onInformationContainer": "SoftWhite",
    "success": "TerminalGreen", "onSuccess": "Void",
    "warning": "BrightRed", "onWarning": "Void", "warningContainer": "RedDark", "onWarningContainer": "SoftWhite",
    "outline": "Grey600", "outlineVariant": "Line",
    "surfaceBright": "Charcoal3", "surfaceDim": "Void", "surfaceContainer": "Charcoal",
    "surfaceContainerHigh": "Charcoal2", "surfaceContainerHighest": "Charcoal3",
    "surfaceContainerLow": "Charcoal0", "surfaceContainerLowest": "Void",
    "autofillText": "LavenderA55", "selectedText": "AshA80", "iconPrivate": "Lavender",
    "surfaceDimVariant": "Charcoal0",
    "layerGradientStart": "RedDeep", "layerGradientEnd": "SignalRed",
}

LIGHT = {
    "primary": "SignalRed", "onPrimary": "White", "primaryContainer": "RedTint",
    "onPrimaryContainer": "RedDark", "inversePrimary": "BrightRed",
    "secondary": "AshDark", "onSecondary": "White", "secondaryContainer": "Grey200",
    "onSecondaryContainer": "Charcoal",
    "tertiary": "LavenderDeep", "onTertiary": "White", "tertiaryContainer": "LavenderTint",
    "onTertiaryContainer": "LavenderInk",
    "background": "SoftWhite", "onBackground": "Charcoal", "surface": "SoftWhite", "onSurface": "Charcoal",
    "surfaceVariant": "Grey200", "onSurfaceVariant": "AshDark",
    "inverseSurface": "Charcoal2", "inverseOnSurface": "SoftWhite",
    "error": "RedDeep", "onError": "White", "errorContainer": "RedTint", "onErrorContainer": "RedDark",
    "information": "LavenderDeep", "onInformation": "White", "informationContainer": "LavenderTint",
    "onInformationContainer": "LavenderInk",
    "success": "GreenDeep", "onSuccess": "White",
    "warning": "SignalRed", "onWarning": "White", "warningContainer": "RedTint", "onWarningContainer": "RedDark",
    "outline": "Grey500", "outlineVariant": "Grey300",
    "surfaceBright": "White", "surfaceDim": "Grey200", "surfaceContainer": "SoftWhite",
    "surfaceContainerHigh": "Grey100", "surfaceContainerHighest": "Grey200",
    "surfaceContainerLow": "Grey050", "surfaceContainerLowest": "White",
    "autofillText": "Lavender", "selectedText": "Ash", "iconPrivate": "Lavender",
    "surfaceDimVariant": "Grey200",
    "layerGradientStart": "RedDeep", "layerGradientEnd": "SignalRed",
}

PRIVATE = {
    "primary": "Lavender", "onPrimary": "Void", "primaryContainer": "LavenderDark",
    "onPrimaryContainer": "SoftWhite", "inversePrimary": "LavenderDark",
    "secondary": "Ash", "onSecondary": "Void", "secondaryContainer": "PrivateLine",
    "onSecondaryContainer": "SoftWhite",
    "tertiary": "LavenderLight", "onTertiary": "Void", "tertiaryContainer": "LavenderInk",
    "onTertiaryContainer": "SoftWhite",
    "background": "PrivateBg", "onBackground": "SoftWhite", "surface": "PrivateSurface", "onSurface": "SoftWhite",
    "surfaceVariant": "PrivateVariant", "onSurfaceVariant": "Ash",
    "inverseSurface": "SoftWhite", "inverseOnSurface": "Void",
    "error": "BrightRed", "onError": "Void", "errorContainer": "RedDark", "onErrorContainer": "SoftWhite",
    "information": "Lavender", "onInformation": "Void", "informationContainer": "LavenderInk",
    "onInformationContainer": "SoftWhite",
    "success": "TerminalGreen", "onSuccess": "Void",
    "warning": "BrightRed", "onWarning": "Void", "warningContainer": "RedDark", "onWarningContainer": "SoftWhite",
    "outline": "Grey600", "outlineVariant": "PrivateLine",
    "surfaceBright": "PrivateVariant", "surfaceDim": "Void", "surfaceContainer": "PrivateSurface",
    "surfaceContainerHigh": "PrivateVariant", "surfaceContainerHighest": "PrivateLine",
    "surfaceContainerLow": "PrivateBg", "surfaceContainerLowest": "Void",
    "autofillText": "LavenderA55", "selectedText": "AshA80", "iconPrivate": "Lavender",
    "surfaceDimVariant": "PrivateBg",
}

# Other XML tokens that need an exact brand colour because re-hueing alone would give the wrong feel.
EXTRA_XML = {
    "values": {
        "accent_private_theme": "Lavender",
        "accent_high_contrast_private_theme": "LavenderLight",
        "pbm_notification_color": "LavenderDeep",
        "homepage_tab_edge_to_edge_private_toolbar_outline": "PrivateLine",
        "mozac_feature_qr_scan_success_color": "TerminalGreen",
        "fx_mobile_private_layer_color_2": "PrivateSurface",
        "fx_mobile_private_layer_color_3": "PrivateVariant",
        "fx_mobile_private_action_color_primary_disabled": "AshA80",
        "fx_mobile_icon_color_accent_blue": "Lavender",
        "search_suggestion_indicator_icon_bookmark_color_normal_theme": "Lavender",
    },
    "night": {
        "search_suggestion_indicator_icon_bookmark_color_normal_theme": "Lavender",
        "fx_mobile_icon_color_accent_blue": "Lavender",
        "protections_dashboard_chip_background": "LavenderDark",
        "protections_dashboard_chip_text": "LavenderTint",
        "homepage_edge_to_edge_gradient_start": "Charcoal0",
        "homepage_edge_to_edge_gradient_end": "Charcoal3",
        "homepage_tab_edge_to_edge_tab_strip_item_background_active": "Line",
        "homepage_tab_edge_to_edge_tab_strip_item_background_inactive": "Charcoal2",
    },
}

# Literal colours in the light and private sets that would stay violet or blue.
LITERAL_FIXES = {
    "values": {
        "device_type_mobile_background": "LavenderTint",
        "mozac_additional_note_background": "PrivateVariant",
        "mozac_grey_button_text_color": "Charcoal2",
        "fx_mobile_private_surface_dim": "Void",
        "fx_mobile_private_surface_container_high": "PrivateVariant",
        "fx_mobile_private_surface_container_low": "PrivateBg",
        "fx_mobile_private_surface_container_lowest": "Void",
    },
}

ROLE_TO_XML = {
    "inversePrimary": "primary_inverse", "inverseSurface": "surface_inverse",
    "inverseOnSurface": "on_surface_inverse", "layerGradientStart": "layer_color_gradient_start",
    "layerGradientEnd": "layer_color_gradient_end",
}


def snake(name):
    return ROLE_TO_XML.get(name) or re.sub(r"([A-Z])", lambda m: "_" + m.group(1).lower(), name)


def to_argb(value):
    value = value.lstrip("#")
    return value if len(value) == 8 else "FF" + value


def rehue(argb, hue, sat):
    a, r, g, b = (int(argb[i:i + 2], 16) for i in (0, 2, 4, 6))
    _, l, _ = colorsys.rgb_to_hls(r / 255, g / 255, b / 255)
    rr, gg, bb = colorsys.hls_to_rgb(hue / 360, l, sat)
    return "%02X%02X%02X%02X" % (a, round(rr * 255), round(gg * 255), round(bb * 255))


def family(name):
    base = re.sub(r"^(nova|photon)", "", name)
    base = re.sub(r"^(\w+?)(\d.*)$", r"\1", base)
    if base in ("VioletDesaturated", "Ink"):
        return (267, 0.19)
    if base == "Violet":
        return (353, 0.85)
    if base == "Gray" and name.startswith("nova"):
        return (0, 0.0)
    return None


def read(path):
    with open(path, encoding="utf-8") as handle:
        return handle.read()


def write(path, text):
    with open(path, "w", encoding="utf-8") as handle:
        handle.write(text)


def rehue_xml(path):
    text = read(path)
    count = 0

    def sub(match):
        nonlocal count
        info = family(match.group(1))
        if not info:
            return match.group(0)
        hue, sat = info
        new = rehue(to_argb(match.group(2)), hue, sat).lower()
        count += 1
        return f'{match.group(0).split(">")[0]}>#{new}</color>'

    text = re.sub(r'<color name="((?:nova|photon)\w+)">#([0-9A-Fa-f]{6,8})</color>', sub, text)
    write(path, text)
    return count


def rehue_kotlin(path):
    text = read(path)
    count = 0

    def sub(match):
        nonlocal count
        prefix = "nova" if "Nova" in os.path.basename(path) else "photon"
        info = family(prefix + match.group(1))
        if not info:
            return match.group(0)
        hue, sat = info
        new = rehue(to_argb(match.group(2)), hue, sat)
        count += 1
        return f"val {match.group(1)} = Color(0x{new})"

    text = re.sub(r"val (\w+) = Color\(0x([0-9A-Fa-f]{8})\)", sub, text)
    write(path, text)
    return count


def brand_files():
    xml = LICENSE_XML + "<resources>\n" + "".join(
        f'    <color name="ff{k}">#{v}</color>\n' for k, v in BRAND.items()
    ) + "</resources>\n"
    write(COLORS_RES + "/fragmentedfox_colors.xml", xml)
    kt = (
        "/* This Source Code Form is subject to the terms of the Mozilla Public\n"
        " * License, v. 2.0. If a copy of the MPL was not distributed with this\n"
        " * file, You can obtain one at http://mozilla.org/MPL/2.0/. */\n\n"
        "package mozilla.components.ui.colors\n\n"
        "import androidx.compose.ui.graphics.Color\n\n"
        "/**\n * The Fragmented Visions palette. Generated by `.github/scripts/make-android-colors.py`.\n */\n"
        "object FragmentedFoxColors {\n"
        + "".join(f"    val {k} = Color(0x{v})\n" for k, v in BRAND.items())
        + "}\n"
    )
    write(COLORS_KT + "/FragmentedFoxColors.kt", kt)


def xml_files(scope):
    folder = "values-night" if scope == "night" else "values"
    found = [FENIX + f"/res/{folder}/colors.xml"]
    for base, _, names in os.walk(AC + "/components"):
        if base.endswith(f"/src/main/res/{folder}") and "colors.xml" in names:
            found.append(base + "/colors.xml")
    return found


def resolve(path, value):
    # Some android-components modules do not depend on the colours module, so they get the hex value.
    if value.startswith("@color/ff") and path.startswith(AC):
        return "#" + BRAND[value[len("@color/ff"):]]
    return value


def set_xml_tokens(scope, tokens, required=True):
    files = {path: read(path) for path in xml_files(scope)}
    for name, value in tokens.items():
        pattern = re.compile(r'(<color name="%s"[^>]*>)[^<]*(</color>)' % re.escape(name))
        for path, text in files.items():
            if pattern.search(text):
                files[path] = pattern.sub(lambda m: m.group(1) + resolve(path, value) + m.group(2), text, count=1)
                break
        else:
            if required:
                raise SystemExit(f"colour token {name} not found in any {scope} colors.xml")
    for path, text in files.items():
        write(path, text)


def apply_xml():
    light = FENIX + "/res/values/colors.xml"
    night = FENIX + "/res/values-night/colors.xml"
    tokens = {}
    for role, color in LIGHT.items():
        tokens["fx_mobile_" + snake(role)] = "@color/ff" + color
    private = {}
    for role, color in PRIVATE.items():
        if role in ("layerGradientStart", "layerGradientEnd"):
            continue
        private["fx_mobile_private_" + snake(role)] = "@color/ff" + color
    extras = {k: "@color/ff" + v for k, v in EXTRA_XML["values"].items()}
    literals = {k: "@color/ff" + v for k, v in LITERAL_FIXES["values"].items()}
    set_xml_tokens("values", {**present(light, tokens), **present(light, private)}, required=False)
    set_xml_tokens("values", {**extras, **literals})
    dark = {"fx_mobile_" + snake(r): "@color/ff" + c for r, c in DARK.items()}
    extras_night = {k: "@color/ff" + v for k, v in EXTRA_XML["night"].items()}
    set_xml_tokens("night", {**present(night, dark), **extras_night})


def present(path, tokens):
    text = read(path)
    return {k: v for k, v in tokens.items() if re.search(r'<color name="%s"' % re.escape(k), text)}


def kotlin_ref(name):
    return "FragmentedFoxColors." + name


def set_kotlin_fields(text, start_marker, end_marker, roles):
    a = text.index(start_marker)
    b = text.index(end_marker, a)
    body = text[a:b]
    for role, color in roles.items():
        pattern = re.compile(r"(\b%s = )(?:NovaColors|PhotonColors|Color)[^\n]*,\n" % re.escape(role))
        body = pattern.sub(lambda m: m.group(1) + kotlin_ref(color) + ",\n", body, count=1)
    return text[:a] + body + text[b:]


def apply_compose():
    path = ACORN + "/AcornColors.kt"
    text = read(path)
    if "import mozilla.components.ui.colors.FragmentedFoxColors" not in text:
        text = text.replace(
            "import mozilla.components.ui.colors.NovaColors\n",
            "import mozilla.components.ui.colors.FragmentedFoxColors\nimport mozilla.components.ui.colors.NovaColors\n",
            1,
        )
    text = set_kotlin_fields(text, "val darkColorPalette = AcornColors(", "val lightColorPalette", DARK)
    text = set_kotlin_fields(text, "val lightColorPalette = AcornColors(", "val privateColorPalette", LIGHT)
    text = set_kotlin_fields(text, "fun acornDarkColorScheme()", "fun acornLightColorScheme()", DARK)
    text = set_kotlin_fields(text, "fun acornLightColorScheme()", "fun acornPrivateColorScheme()", LIGHT)
    marker = "fun acornPrivateColorScheme()"
    a = text.index(marker)
    b = text.index("\n)\n", a)
    body = text[a:b]
    for role, color in PRIVATE.items():
        pattern = re.compile(r"(\b%s = )(?:NovaColors|PhotonColors|Color)[^\n]*,\n" % re.escape(role))
        body = pattern.sub(lambda m: m.group(1) + kotlin_ref(color) + ",\n", body, count=1)
    text = text[:a] + body + text[b:]
    marker = "val privateColorPalette = darkColorPalette.copy(\n"
    a = text.index(marker) + len(marker)
    b = text.index("\n)\n", a)
    text = (
        text[:a]
        + "    surfaceDimVariant = FragmentedFoxColors.PrivateBg,\n    selectedText = FragmentedFoxColors.AshA80,"
        + text[b:]
    )
    write(path, text)


def apply_gradients():
    path = ACORN + "/AcornGradientScheme.kt"
    text = read(path)
    if "FragmentedFoxColors" not in text:
        text = text.replace(
            "import mozilla.components.ui.colors.NovaColors\n",
            "import mozilla.components.ui.colors.FragmentedFoxColors\nimport mozilla.components.ui.colors.NovaColors\n",
            1,
        )
    repl = {
        "NovaColors.Violet60": "FragmentedFoxColors.RedDeep",
        "NovaColors.Violet50": "FragmentedFoxColors.SignalRed",
        "NovaColors.Violet30": "FragmentedFoxColors.BrightRed",
        "NovaColors.Orange30": "FragmentedFoxColors.SignalRed",
        "NovaColors.Violet10A50": "FragmentedFoxColors.RedTint.copy(alpha = 0.5f)",
        "NovaColors.Orange10A50": "FragmentedFoxColors.RedTint.copy(alpha = 0.25f)",
        "NovaColors.VioletDesaturated90A50": "FragmentedFoxColors.Charcoal.copy(alpha = 0.5f)",
        "NovaColors.Orange70A50": "FragmentedFoxColors.RedDark.copy(alpha = 0.5f)",
        "NovaColors.VioletDesaturated90": "FragmentedFoxColors.PrivateBg",
    }
    for old, new in repl.items():
        text = re.sub(r"%s(?![\w])" % re.escape(old), new, text)
    write(path, text)


def apply_styles():
    path = FENIX + "/res/values/styles.xml"
    text = read(path)
    text = text.replace('<item name="accentBright">@color/photonViolet70</item>', '<item name="accentBright">@color/ffSignalRed</item>')
    text = text.replace('<item name="awesomeBarIndicatorBookmarkColor">@color/photonBlue40</item>', '<item name="awesomeBarIndicatorBookmarkColor">@color/ffLavender</item>')
    write(path, text)


def luminance(argb):
    out = []
    for i in (2, 4, 6):
        c = int(argb[i:i + 2], 16) / 255
        out.append(c / 12.92 if c <= 0.03928 else ((c + 0.055) / 1.055) ** 2.4)
    return 0.2126 * out[0] + 0.7152 * out[1] + 0.0722 * out[2]


def contrast(a, b):
    la, lb = sorted((luminance(BRAND[a]), luminance(BRAND[b])), reverse=True)
    return (la + 0.05) / (lb + 0.05)


PAIRS = [
    ("onPrimary", "primary"), ("onPrimaryContainer", "primaryContainer"),
    ("onSecondary", "secondary"), ("onSecondaryContainer", "secondaryContainer"),
    ("onTertiary", "tertiary"), ("onTertiaryContainer", "tertiaryContainer"),
    ("onBackground", "background"), ("onSurface", "surface"), ("onSurfaceVariant", "surfaceVariant"),
    ("onError", "error"), ("onErrorContainer", "errorContainer"),
    ("onInformation", "information"), ("onSuccess", "success"), ("onWarning", "warning"),
    ("onWarningContainer", "warningContainer"), ("onInformationContainer", "informationContainer"),
    ("inverseOnSurface", "inverseSurface"),
]


def report():
    bad = 0
    for name, roles in (("dark", DARK), ("light", LIGHT), ("private", PRIVATE)):
        for fg, bg in PAIRS:
            if fg in roles and bg in roles:
                ratio = contrast(roles[fg], roles[bg])
                if ratio < 4.5:
                    bad += 1
                    print(f"low contrast {name} {fg} on {bg}: {ratio:.2f}")
        for fg, bg in (("primary", "background"), ("tertiary", "background"), ("onSurface", "surfaceContainerHighest")):
            ratio = contrast(roles[fg], roles[bg])
            if ratio < 3:
                bad += 1
                print(f"low contrast {name} {fg} against {bg}: {ratio:.2f}")
    print("contrast pairs below 4.5 (text) or 3 (accents):", bad)
    return bad


def main():
    nova_xml = rehue_xml(COLORS_RES + "/nova_colors.xml")
    photon_xml = rehue_xml(COLORS_RES + "/photon_colors.xml")
    nova_kt = rehue_kotlin(COLORS_KT + "/NovaColors.kt")
    photon_kt = rehue_kotlin(COLORS_KT + "/PhotonColors.kt")
    print("re-hued", nova_xml, photon_xml, nova_kt, photon_kt)
    brand_files()
    apply_xml()
    apply_compose()
    apply_gradients()
    apply_styles()
    return report()


if __name__ == "__main__":
    sys.exit(0 if main() == 0 else 1)
