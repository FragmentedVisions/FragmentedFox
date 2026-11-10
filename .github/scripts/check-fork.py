# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

import glob
import json
import os
import re
import sys

import yaml

ROOT = os.environ.get("FF_ROOT", os.getcwd())
APP = os.path.join(ROOT, "mobile/android/fenix/app")
SRC_DIRS = [os.path.join(APP, "src", d) for d in ("main", "test", "androidTest")]
AC = os.path.join(ROOT, "mobile/android/android-components")

failures = []


def fail(section, message):
    failures.append((section, message))


def read(path):
    with open(path, encoding="utf-8", errors="replace") as handle:
        return handle.read()


def source_files():
    for base in SRC_DIRS:
        for ext in ("kt", "java"):
            yield from glob.glob(os.path.join(base, "**", f"*.{ext}"), recursive=True)


def strip_comments(text):
    text = re.sub(r"/\*.*?\*/", "", text, flags=re.S)
    return re.sub(r"(?m)^\s*//.*$", "", text)


DECL = re.compile(
    r"(?:class|object|interface|typealias|fun|val|var|enum class)\s+(?:<[^>]*>\s*)?(?:[\w.<>?, ]+\.)?(\w+)"
)

FRAMEWORK_STRINGS = {"ok", "cancel", "yes", "no", "untitled", "copy", "cut", "paste", "selectAll"}

GENERATED_PREFIXES = (
    "org.mozilla.fenix.GleanMetrics",
    "org.mozilla.fenix.nimbus",
    "org.mozilla.fenix.databinding",
    "org.mozilla.fenix.R",
    "org.mozilla.fenix.BuildConfig",
)


def check_imports():
    packages = {}
    files = list(source_files())
    for path in files:
        text = strip_comments(read(path))
        match = re.search(r"(?m)^package\s+([\w.]+)", text)
        if not match:
            continue
        entry = packages.setdefault(match.group(1), {"names": set(), "words": set()})
        entry["names"].update(DECL.findall(text))
        entry["words"].update(re.findall(r"\b\w+\b", text))
        for enum_body in re.findall(r"enum class \w+[^{]*\{([^;}]*)", text):
            entry["names"].update(re.findall(r"\b([A-Z][A-Z0-9_]+)\b", enum_body))
    for path in files:
        text = strip_comments(read(path))
        for imp in re.findall(r"(?m)^import\s+(org\.mozilla\.fenix\.[\w.]+?)(?:\s+as\s+\w+)?\s*$", text):
            if imp.startswith(GENERATED_PREFIXES) or imp.endswith(("Directions", "Args")):
                continue
            segments = imp.split(".")
            if "Directions" in segments[-1] or segments[-1].endswith(("Args", "Directions")):
                continue
            ok = False
            for k in range(len(segments) - 1, 2, -1):
                package = ".".join(segments[:k])
                entry = packages.get(package)
                if entry and segments[k] in entry["names"]:
                    ok = all(seg in entry["words"] for seg in segments[k + 1:])
                    if ok:
                        break
            if not ok:
                fail("imports", f"{os.path.relpath(path, ROOT)}: unresolved import {imp}")


def resource_index():
    names = {"string": set(), "plurals": set(), "drawable": set(), "raw": set()}
    res_dirs = glob.glob(os.path.join(APP, "src/main/res"))
    res_dirs += glob.glob(os.path.join(AC, "**/src/main/res"), recursive=True)
    res_dirs += glob.glob(os.path.join(ROOT, "mobile/android/*/src/main/res"))
    for res in res_dirs:
        for path in glob.glob(os.path.join(res, "values*", "*.xml")):
            text = read(path)
            names["string"].update(re.findall(r'<string\s+name="(\w+)"', text))
            names["plurals"].update(re.findall(r'<plurals\s+name="(\w+)"', text))
        for kind in ("drawable", "raw"):
            for path in glob.glob(os.path.join(res, f"{kind}*", "*")):
                names[kind].add(os.path.splitext(os.path.basename(path))[0])
    return names


def check_resources():
    names = resource_index()
    for path in source_files():
        text = strip_comments(read(path))
        framework = "import android.R\n" in text
        for kind, name in re.findall(r"\bR\.(string|plurals|drawable|raw)\.(\w+)", text):
            if framework or (kind == "string" and name in FRAMEWORK_STRINGS):
                continue
            if name not in names[kind]:
                fail("resources", f"{os.path.relpath(path, ROOT)}: R.{kind}.{name} does not exist")
    for path in glob.glob(os.path.join(APP, "src/main/res/**/*.xml"), recursive=True):
        if "/values-" in path:
            continue
        for kind, name in re.findall(r"@(string|plurals|drawable)/(\w+)", read(path)):
            if name not in names[kind]:
                fail("resources", f"{os.path.relpath(path, ROOT)}: @{kind}/{name} does not exist")


def check_pings():
    metrics = yaml.safe_load(read(os.path.join(APP, "metrics.yaml")))
    pings = yaml.safe_load(read(os.path.join(APP, "pings.yaml")))
    allowed = {"metrics", "events", "baseline", "deletion-request", "default", "nimbus-targeting-context"}
    for category, body in metrics.items():
        if not isinstance(body, dict):
            continue
        for name, metric in body.items():
            if isinstance(metric, dict):
                for ping in metric.get("send_in_pings") or []:
                    if ping not in pings and ping not in allowed:
                        fail("pings", f"metrics.yaml {category}.{name} sends to undefined ping {ping}")


def check_removed():
    forbidden = json.loads(read(os.path.join(ROOT, "fragmentedfox-docs/removed.json")))
    targets = [
        os.path.join(APP, "build.gradle"),
        os.path.join(APP, "src/main/AndroidManifest.xml"),
    ]
    targets += list(source_files())
    for path in targets:
        if "/src/test/" in path or "/src/androidTest/" in path:
            continue
        text = read(path)
        for pattern in forbidden["patterns"]:
            if re.search(pattern, text):
                fail("removed", f"{os.path.relpath(path, ROOT)}: matches removed pattern {pattern}")
    manifest = re.sub(r"<[^<>]*tools:node=\"remove\"[^<>]*>", "", read(os.path.join(APP, "src/main/AndroidManifest.xml")))
    for permission in forbidden["permissions"]:
        if permission in manifest:
            fail("removed", f"manifest still declares {permission}")


def check_prefs():
    expected = json.loads(read(os.path.join(ROOT, "fragmentedfox-docs/expected-prefs.json")))
    for relative, prefs in expected.items():
        text = read(os.path.join(ROOT, relative))
        for name, value in prefs.items():
            found = re.findall(r'pref\("%s",\s*(.*?)\);' % re.escape(name), text)
            if not found:
                fail("prefs", f"{relative}: {name} is not set")
            elif found[-1].strip() != value:
                fail("prefs", f"{relative}: {name} is {found[-1].strip()}, expected {value}")


def check_settings():
    expected = json.loads(read(os.path.join(ROOT, "fragmentedfox-docs/expected-settings.json")))
    text = read(os.path.join(APP, "src/main/java/org/mozilla/fenix/utils/Settings.kt"))
    for name, value in expected.items():
        match = re.search(r"\bvar %s(?::\s*[\w<>?]+)? by \w+\(.*?default\s*=\s*([^,\n]+),?" % re.escape(name), text, flags=re.S)
        if not match:
            fail("settings", f"Settings.kt: {name} not found")
        elif match.group(1).strip() != value:
            fail("settings", f"Settings.kt: {name} default is {match.group(1).strip()}, expected {value}")


def check_nimbus():
    expected = json.loads(read(os.path.join(ROOT, "fragmentedfox-docs/expected-nimbus.json")))
    features = yaml.safe_load(read(os.path.join(APP, "nimbus.fml.yaml")))["features"]
    for key, value in expected.items():
        feature, _, variable = key.partition(".")
        variable, _, entry = variable.partition(".")
        try:
            actual = features[feature]["variables"][variable]["default"]
            if entry:
                actual = actual[entry]
        except (KeyError, TypeError):
            fail("nimbus", f"nimbus.fml.yaml: {key} not found")
            continue
        if str(actual).lower() != value:
            fail("nimbus", f"nimbus.fml.yaml: {key} is {actual}, expected {value}")


def check_cleartext():
    manifest = read(os.path.join(APP, "src/main/AndroidManifest.xml"))
    if 'android:usesCleartextTraffic="false"' not in manifest:
        fail("cleartext", "AndroidManifest.xml: usesCleartextTraffic is not false")
    config = read(os.path.join(APP, "src/main/res/xml/network_security_config.xml"))
    base = re.search(r"<base-config[^>]*cleartextTrafficPermitted=\"(\w+)\"", config)
    if not base or base.group(1) != "false":
        fail("cleartext", "network_security_config.xml: base-config permits cleartext")


def check_launcher_aliases():
    manifest = read(os.path.join(APP, "src/main/AndroidManifest.xml"))
    if manifest.count("<activity-alias") < 26:
        fail("manifest", "AndroidManifest.xml: launcher aliases were removed; builds without them did not install (see changes.md)")
    if 'android.permission.REQUEST_INSTALL_PACKAGES" tools:node="remove"' in manifest:
        fail("manifest", "AndroidManifest.xml: REQUEST_INSTALL_PACKAGES removal is untested (see changes.md)")


def check_theme():
    prefs = read(os.path.join(APP, "src/main/res/xml/customization_preferences.xml"))
    for key, expected in (("pref_key_dark_theme", "true"), ("pref_key_light_theme", "false"), ("pref_key_follow_device_theme", "false")):
        match = re.search(r'android:defaultValue="([^"]*)"\s+android:key="@string/%s"' % key, prefs)
        if not match or match.group(1) != expected:
            fail("theme", f"customization_preferences.xml: {key} default is not {expected}")
    xml = read(os.path.join(AC, "components/ui/colors/src/main/res/values/fragmentedfox_colors.xml"))
    colors = read(os.path.join(APP, "src/main/res/values-night/colors.xml"))
    if "@color/ffBrightRed" not in colors or '<color name="ffSignalRed">#FFD4122A</color>' not in xml:
        fail("theme", "the Fragmented Visions colours are not applied, run .github/scripts/make-android-colors.py")


def check_palette_visibility():
    for path in glob.glob(os.path.join(AC, "**", "src", "main", "res", "**", "*.xml"), recursive=True):
        if "/ui/colors/" in path:
            continue
        if "@color/ff" not in read(path):
            continue
        module = path.split("/src/main/res/")[0]
        gradle = os.path.join(module, "build.gradle")
        if not os.path.exists(gradle) or ":components:ui-colors" not in read(gradle):
            fail("theme", f"{os.path.relpath(path, ROOT)} uses a palette colour but its module does not depend on ui-colors, use the hex value")


def check_remote_experiments():
    components = read(os.path.join(APP, "src/main/java/org/mozilla/fenix/components/Components.kt"))
    match = re.search(r"NimbusComponents\((.*?)\n\s{8}\)", components, flags=re.S)
    if not match or not re.search(r"remoteSettingsService\s*=\s*null\b", match.group(1)):
        fail("nimbus", "Components.kt: Nimbus must not get the Remote Settings service, it would fetch experiments and rollouts from Mozilla")
    bundled = json.loads(read(os.path.join(APP, "src/main/res/raw/initial_experiments.json")))
    if bundled.get("data"):
        fail("nimbus", "initial_experiments.json: bundled Mozilla experiments and rollouts must stay empty")


def check_query_stripping():
    provider = read(os.path.join(APP, "src/main/java/org/mozilla/fenix/gecko/GeckoProvider.kt"))
    for name in ("queryParameterStripping", "queryParameterStrippingPrivateBrowsing"):
        if not re.search(r"\b%s = true," % name, provider):
            fail("settings", f"GeckoProvider.kt: {name} is not true")


def check_licenses():
    gradle = read(os.path.join(APP, "build.gradle"))
    match = re.search(r"if \(([^\n]*)\) \{\s*apply plugin: 'com\.google\.android\.gms\.oss-licenses-plugin'", gradle)
    if not match:
        fail("licenses", "app/build.gradle: the oss-licenses plugin is no longer applied, so the libraries screen would be empty")
    elif "MOZILLA_OFFICIAL" in match.group(1):
        fail("licenses", "app/build.gradle: the oss-licenses plugin must not depend on MOZILLA_OFFICIAL, our builds never set it (see changes.md)")


def check_translations():
    res = os.path.join(APP, "src/main/res")
    pattern = r'<(?:string|plurals|string-array)\s+[^>]*?name="([^"]+)"'
    defined = set()
    for name in ("strings.xml", "static_strings.xml"):
        defined |= set(re.findall(pattern, read(os.path.join(res, "values", name))))
    for path in sorted(glob.glob(os.path.join(res, "values-*", "strings.xml"))):
        extra = set(re.findall(pattern, read(path))) - defined
        if extra:
            fail("translations", f"{os.path.relpath(path, ROOT)}: {len(extra)} translated strings have no default, e.g. {sorted(extra)[0]}")


def check_hidden_settings():
    screen = read(os.path.join(APP, "src/main/java/org/mozilla/fenix/settings/datachoices/DataChoicesScreen.kt"))
    match = re.search(r"fun buildDataChoicesItems.*?\n}\n", screen, flags=re.S)
    if not match or re.findall(r"add\(DataChoicesSectionKey\.(\w+)\)", match.group(0)) != ["CRASH_REPORTS"]:
        fail("settings", "DataChoicesScreen.kt: only the crash report section may be shown, telemetry and studies do nothing here")
    dialog = read(os.path.join(APP, "src/main/java/org/mozilla/fenix/onboarding/ManagePrivacyPreferencesDialog.kt"))
    if "UsageDataPreference(" in dialog:
        fail("settings", "ManagePrivacyPreferencesDialog.kt: the usage data switch does nothing and must stay removed")


def check_search():
    path = os.path.join(AC, "components/feature/search/src/main/java/mozilla/components/feature/search/middleware/SearchMiddleware.kt")
    text = read(path)
    if 'PREFERRED_DEFAULT_SEARCH_ENGINE_ID = "ddg"' not in text or "it.id == PREFERRED_DEFAULT_SEARCH_ENGINE_ID" not in text:
        fail("search", "SearchMiddleware.kt: DuckDuckGo is no longer the preferred default search engine")


def glean_class(category):
    return "".join(part[:1].upper() + part[1:] for part in re.split(r"[._]", category))


def glean_member(name):
    parts = re.split(r"[._-]", name)
    return parts[0] + "".join(part[:1].upper() + part[1:] for part in parts[1:])


SCOPE_FUNCTIONS = {"apply", "let", "also", "run", "takeIf", "takeUnless", "with"}
USES = r"(?:set|add|record|start|stop|cancel|accumulate\w*|measure|testGetValue|get)"


def check_glean():
    metrics = yaml.safe_load(read(os.path.join(APP, "metrics.yaml")))
    pings = yaml.safe_load(read(os.path.join(APP, "pings.yaml")))
    members = {}
    for category, body in metrics.items():
        if isinstance(body, dict) and not category.startswith("$"):
            members[glean_class(category)] = {glean_member(n) for n, m in body.items() if isinstance(m, dict)}
    members["Pings"] = {glean_member(n) for n in pings if not n.startswith("$")}
    for path in source_files():
        text = strip_comments(read(path))
        imported = {}
        for name, alias in re.findall(r"(?m)^import org\.mozilla\.fenix\.GleanMetrics\.(\w+)(?: as (\w+))?\s*$", text):
            imported[alias or name] = name
        for local, name in imported.items():
            if name not in members:
                continue
            skip = SCOPE_FUNCTIONS | set(re.findall(r"\b(?:val|var)\s+(\w+)", text))
            skip |= set(re.findall(r"(?m)^import org\.mozilla\.fenix\.GleanMetrics\.\w+\.(\w+)", text))
            found = set(re.findall(r"(?<![\w.])%s\.([a-z]\w*)" % re.escape(local), text))
            for block in re.finditer(r"with\(%s\)\s*\{" % re.escape(local), text):
                depth, i = 1, block.end()
                while i < len(text) and depth:
                    depth += {"{": 1, "}": -1}.get(text[i], 0)
                    i += 1
                found |= set(re.findall(r"(?m)^\s*([a-z]\w*)\.%s\(" % USES, text[block.end():i]))
            for member in sorted(found):
                if member not in members[name] and member not in skip:
                    fail("glean", f"{os.path.relpath(path, ROOT)}: {name}.{member} is not defined in metrics.yaml or pings.yaml")


def check_features():
    names = set()
    for path in glob.glob(os.path.join(APP, "*.fml.yaml")):
        doc = yaml.safe_load(read(path)) or {}
        names |= {glean_member(n) for n in (doc.get("features") or {})}
    for path in source_files():
        text = strip_comments(read(path))
        for member in sorted(set(re.findall(r"FxNimbus\.features\.([a-z]\w*)", text))):
            if member not in names:
                fail("features", f"{os.path.relpath(path, ROOT)}: FxNimbus.features.{member} is not defined in the FML files")


REVIEWED_UNCONDITIONAL_PINGS = {"onboarding-opt-out", "usage-reporting", "usage-deletion-request"}


def check_telemetry():
    pings = yaml.safe_load(read(os.path.join(APP, "pings.yaml")))
    for name, spec in pings.items():
        if isinstance(spec, dict) and (spec.get("metadata") or {}).get("follows_collection_enabled", True) is False:
            if name not in REVIEWED_UNCONDITIONAL_PINGS:
                fail("telemetry", f"pings.yaml: ping {name} ignores the collection setting and has not been reviewed (see fragmentedfox-docs/telemetry.md)")
    controller = read(os.path.join(APP, "src/main/java/org/mozilla/fenix/components/metrics/MetricController.kt"))
    if not re.search(r"if \(BuildConfig\.TELEMETRY\) \{\s*ReleaseMetricController\(.*?\} else \{\s*DebugMetricController\(\)", controller, flags=re.S):
        fail("telemetry", "MetricController.kt: the metric controller no longer falls back to the logging-only one when TELEMETRY is off")
    glean = read(os.path.join(APP, "src/main/java/org/mozilla/fenix/components/GleanHelper.kt"))
    if "uploadEnabled = isTelemetryUploadEnabled && BuildConfig.TELEMETRY" not in glean:
        fail("telemetry", "GleanHelper.kt: Glean upload is no longer gated on BuildConfig.TELEMETRY")
    onboarding = read(os.path.join(APP, "src/main/java/org/mozilla/fenix/onboarding/OnboardingFragment.kt"))
    if "BuildConfig.TELEMETRY && !settings.isTelemetryEnabled" not in onboarding:
        fail("telemetry", "OnboardingFragment.kt: the onboarding opt-out ping is no longer gated on BuildConfig.TELEMETRY")


def check_syntax():
    for path in glob.glob(os.path.join(APP, "*.yaml")):
        try:
            yaml.safe_load(read(path))
        except yaml.YAMLError as error:
            fail("syntax", f"{os.path.relpath(path, ROOT)}: {error}")
    try:
        json.loads(read(os.path.join(ROOT, "fragmentedfox-docs/expected-hosts.json")))
    except ValueError as error:
        fail("syntax", f"expected-hosts.json: {error}")
    import xml.dom.minidom
    for path in glob.glob(os.path.join(APP, "src/main/res/values/*.xml")) + glob.glob(os.path.join(APP, "src/main/res/xml/*.xml")) + [os.path.join(APP, "src/main/AndroidManifest.xml")]:
        try:
            xml.dom.minidom.parse(path)
        except Exception as error:
            fail("syntax", f"{os.path.relpath(path, ROOT)}: {error}")


BASELINE = os.path.join(ROOT, "fragmentedfox-docs/check-baseline.txt")


def normalise(section, message):
    return f"[{section}] " + re.sub(r":\d+", "", message)


def main():
    checks = [check_imports, check_resources, check_pings, check_removed, check_prefs, check_settings, check_nimbus, check_cleartext, check_launcher_aliases, check_theme, check_palette_visibility, check_remote_experiments, check_query_stripping, check_licenses, check_translations, check_hidden_settings, check_search, check_glean, check_features, check_telemetry, check_syntax]
    selected_flags = [a for a in sys.argv[1:] if a.startswith("--")]
    selected = [a for a in sys.argv[1:] if not a.startswith("--")]
    for check in checks:
        if selected and check.__name__.replace("check_", "") not in selected:
            continue
        check()
    if "--write-baseline" in selected_flags:
        with open(BASELINE, "w") as handle:
            handle.write("".join(sorted({normalise(*f) + "\n" for f in failures})))
        print(f"Wrote {len(failures)} known findings to {BASELINE}")
        return 0
    known = set()
    if os.path.exists(BASELINE):
        known = {line.rstrip("\n") for line in read(BASELINE).splitlines()}
    failures[:] = [f for f in failures if normalise(*f) not in known]
    if failures:
        for section, message in failures:
            print(f"[{section}] {message}")
        print(f"\n{len(failures)} new problem(s) found")
        return 1
    print("All fork checks passed")
    return 0


if __name__ == "__main__":
    sys.exit(main())
