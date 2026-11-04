import collections
import glob
import io
import os
import re
import subprocess
import sys
import zipfile


def mb(n):
    return f"{n / 1048576:8.2f} MB"


def section(title):
    print()
    print("=" * 78)
    print(title)
    print("=" * 78)


def find_tool(name):
    pattern = os.path.expanduser(
        f"~/.mozbuild/android-ndk-*/toolchains/llvm/prebuilt/linux-x86_64/bin/{name}"
    )
    found = sorted(glob.glob(pattern))
    return found[-1] if found else None


def apk_overview(apk):
    section("APK contents by top-level folder (uncompressed / compressed)")
    groups = collections.defaultdict(lambda: [0, 0])
    entries = []
    for info in apk.infolist():
        top = info.filename.split("/")[0]
        groups[top][0] += info.file_size
        groups[top][1] += info.compress_size
        entries.append(info)
    for top, (raw, packed) in sorted(groups.items(), key=lambda kv: -kv[1][0]):
        print(f"{mb(raw)} {mb(packed)}  {top}")
    section("Largest 40 files in the APK (uncompressed / compressed)")
    for info in sorted(entries, key=lambda i: -i.file_size)[:40]:
        print(f"{mb(info.file_size)} {mb(info.compress_size)}  {info.filename}")


def omni_breakdown(apk):
    name = "assets/omni.ja"
    if name not in apk.namelist():
        print("assets/omni.ja not found")
        return
    inner = zipfile.ZipFile(io.BytesIO(apk.read(name)))
    section("assets/omni.ja (the engine's built-in JavaScript, CSS and HTML) by folder")
    groups = collections.defaultdict(int)
    for info in inner.infolist():
        parts = info.filename.split("/")
        key = "/".join(parts[:3]) if len(parts) > 3 else "/".join(parts[:-1]) or parts[0]
        groups[key] += info.file_size
    total = sum(groups.values())
    print(f"total uncompressed: {mb(total)}")
    for key, size in sorted(groups.items(), key=lambda kv: -kv[1])[:60]:
        print(f"{mb(size)}  {key}")


def symbol_key(name, depth):
    name = re.sub(r"^(non-virtual thunk to |virtual thunk to |vtable for |typeinfo for |typeinfo name for |guard variable for |construction vtable for )", "", name)
    name = name.lstrip("<")
    name = re.split(r"[(<\[ ]", name, maxsplit=1)[0] if depth else name
    parts = [p for p in name.split("::") if p]
    return "::".join(parts[:depth]) if parts else "(unnamed)"


def analyze_library(nm, size_tool, path, label):
    section(f"{label}: {path} ({mb(os.path.getsize(path))})")
    if size_tool:
        out = subprocess.run([size_tool, "-A", path], capture_output=True, text=True).stdout
        print("Sections:")
        for line in out.splitlines()[:30]:
            print("  " + line)
    result = subprocess.run(
        [nm, "-S", "-C", "--defined-only", "--size-sort", path],
        capture_output=True, text=True,
    )
    symbols = []
    for line in result.stdout.splitlines():
        match = re.match(r"^\s*[0-9a-fA-F]+\s+([0-9a-fA-F]+)\s+\S\s+(.*)$", line)
        if match:
            symbols.append((int(match.group(1), 16), match.group(2)))
    print(f"\nsymbols with a size: {len(symbols)}, total {mb(sum(s for s, _ in symbols))}")
    if len(symbols) < 2000:
        print("This copy has very few symbols, it is probably stripped, so the breakdown below is limited.")
    for depth in (1, 2):
        groups = collections.defaultdict(int)
        for size, name in symbols:
            groups[symbol_key(name, depth)] += size
        print(f"\nBiggest groups by the first {depth} name part(s):")
        for key, size in sorted(groups.items(), key=lambda kv: -kv[1])[:45]:
            print(f"{mb(size)}  {key[:100]}")
    print("\nBiggest 25 single symbols:")
    for size, name in sorted(symbols, reverse=True)[:25]:
        print(f"{mb(size)}  {name[:110]}")


def dex_breakdown(apk_path):
    section("App code (classes.dex) by package, from apkanalyzer")
    candidates = sorted(glob.glob(os.path.expanduser("~/.mozbuild/android-sdk-linux/cmdline-tools/*/bin/apkanalyzer")))
    if not candidates:
        print("apkanalyzer not found, skipping")
        return
    result = subprocess.run(
        [candidates[-1], "dex", "packages", "--defined-only", apk_path],
        capture_output=True, text=True,
    )
    rows = []
    for line in result.stdout.splitlines():
        parts = line.split()
        if len(parts) >= 5 and parts[0] == "P":
            rows.append((int(parts[-2]), parts[-1]))
    if not rows:
        print("no package data produced")
        print(result.stderr[:500])
        return
    groups = collections.defaultdict(int)
    for size, name in rows:
        groups[".".join(name.split(".")[:4])] += size
    for key, size in sorted(groups.items(), key=lambda kv: -kv[1])[:60]:
        print(f"{size / 1024:10.0f} KB  {key}")


def find_native(apk, apk_path, name):
    candidates = [p for p in glob.glob(f"obj-android/**/{name}", recursive=True) if "arm64" in p or "aarch64" in p or "/lib/" in p]
    candidates = candidates or glob.glob(f"obj-android/**/{name}", recursive=True)
    if candidates:
        return max(candidates, key=os.path.getsize)
    inside = f"lib/arm64-v8a/{name}"
    if inside in apk.namelist():
        target = f"/tmp/{name}"
        with open(target, "wb") as handle:
            handle.write(apk.read(inside))
        return target
    return None


def main():
    apk_path = sys.argv[1]
    nm = os.environ.get("NM") or find_tool("llvm-nm")
    size_tool = os.environ.get("SIZE_TOOL") or find_tool("llvm-size")
    apk = zipfile.ZipFile(apk_path)
    apk_overview(apk)
    omni_breakdown(apk)
    dex_breakdown(apk_path)
    if not nm:
        print("llvm-nm not found, skipping native library analysis")
        return
    for name, label in (("libxul.so", "The engine core"), ("libmegazord.so", "The Rust services library")):
        path = find_native(apk, apk_path, name)
        if path:
            analyze_library(nm, size_tool, path, label)
        else:
            print(f"{name} not found")


if __name__ == "__main__":
    main()
