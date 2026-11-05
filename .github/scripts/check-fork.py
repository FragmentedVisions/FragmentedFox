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
        if "/androidTest/" in path:
            continue
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


def check_syntax():
    for path in glob.glob(os.path.join(APP, "*.yaml")):
        try:
            yaml.safe_load(read(path))
        except yaml.YAMLError as error:
            fail("syntax", f"{os.path.relpath(path, ROOT)}: {error}")
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
    checks = [check_imports, check_resources, check_pings, check_removed, check_prefs, check_settings, check_nimbus, check_cleartext, check_syntax]
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
