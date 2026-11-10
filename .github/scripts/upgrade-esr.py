#!/usr/bin/env python3
"""Build a FragmentedFox tree on top of a new Mozilla ESR tag.

The app layer (OWNED) is FragmentedFox's own code and is copied over unchanged.
Every other file we changed is merged three ways between the old base tag, our
branch and the new tag. Everything else is Mozilla's new version.

Usage: upgrade-esr.py NEW_TAG [--ours REF] [--branch NAME] [--check]
  --check   build the tree only and print whether it equals the tree of --ours
            (use it with the current base tag as NEW_TAG to test the script)
"""
import argparse
import os
import subprocess
import sys
import tempfile

OWNED = [
    "mobile/android/fenix",
    "mobile/android/android-components",
    "mobile/android/branding/fragmentedfox",
    "browser/branding/fragmentedfox",
    "fragmentedfox-docs",
    ".github",
]
BASE_FILE = "fragmentedfox-docs/esr-base.txt"


def git(*args, env=None, input=None, check=True):
    r = subprocess.run(["git", *args], capture_output=True, env=env, input=input)
    if check and r.returncode != 0:
        sys.exit("git %s failed: %s" % (" ".join(args), r.stderr.decode(errors="replace")))
    return r


def out(*args, env=None):
    return git(*args, env=env).stdout.decode()


def is_owned(path):
    return any(path == p or path.startswith(p + "/") for p in OWNED)


def blob_at(ref, path):
    r = git("ls-tree", ref, "--", path, check=False)
    line = r.stdout.decode().strip()
    if not line:
        return None
    meta, _ = line.split("\t", 1)
    mode, _, sha = meta.split()
    return mode, sha


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("new_tag")
    ap.add_argument("--ours", default="HEAD")
    ap.add_argument("--branch")
    ap.add_argument("--check", action="store_true")
    a = ap.parse_args()

    base = out("show", "%s:%s" % (a.ours, BASE_FILE)).strip()
    new = out("rev-parse", a.new_tag + "^{commit}").strip()
    ours = out("rev-parse", a.ours + "^{commit}").strip()
    print("base tag %s, new %s, ours %s" % (base, new[:10], ours[:10]))

    changes = {}
    raw = out("diff", "--name-status", "--no-renames", "-z", base, ours).split("\0")
    i = 0
    while i + 1 < len(raw):
        status, path = raw[i], raw[i + 1]
        i += 2
        if status and not is_owned(path):
            changes[path] = status[0]

    idx = os.path.join(tempfile.mkdtemp(), "index")
    env = dict(os.environ, GIT_INDEX_FILE=idx)
    git("read-tree", new, env=env)

    stale = [p for p in out("ls-files", env=env).split("\n") if p and is_owned(p)]
    if stale:
        git("update-index", "--force-remove", "-z", "--stdin", env=env,
            input=("\0".join(stale) + "\0").encode())
    lines = []
    for p in OWNED:
        lines.append(out("ls-tree", "-r", ours, "--", p))
    info = "".join(lines)
    if info:
        entries = []
        for l in info.strip().split("\n"):
            meta, path = l.split("\t", 1)
            mode, _, sha = meta.split()
            entries.append("%s %s\t%s\n" % (mode, sha, path))
        git("update-index", "--add", "--index-info", env=env, input="".join(entries).encode())

    conflicts = []
    for path, st in sorted(changes.items()):
        mine = blob_at(ours, path)
        theirs = blob_at(new, path)
        if st == "D":
            if theirs:
                git("update-index", "--force-remove", path, env=env)
            continue
        if st == "A" or not theirs or not blob_at(base, path):
            git("update-index", "--add", "--cacheinfo", "%s,%s,%s" % (mine[0], mine[1], path), env=env)
            continue
        old = blob_at(base, path)
        if theirs[1] == old[1]:
            merged = mine[1]
        elif theirs[1] == mine[1]:
            merged = mine[1]
        else:
            with tempfile.TemporaryDirectory() as d:
                files = {}
                for name, sha in (("ours", mine[1]), ("base", old[1]), ("theirs", theirs[1])):
                    f = os.path.join(d, name)
                    with open(f, "wb") as fh:
                        fh.write(git("cat-file", "blob", sha).stdout)
                    files[name] = f
                r = git("merge-file", "-p", "-L", "FragmentedFox", "-L", "base", "-L", "Mozilla",
                        files["ours"], files["base"], files["theirs"], check=False)
                if r.returncode < 0 or r.returncode > 127:
                    sys.exit("merge-file failed on " + path)
                if r.returncode > 0:
                    conflicts.append(path)
                merged = git("hash-object", "-w", "--stdin", input=r.stdout).stdout.decode().strip()
        git("update-index", "--add", "--cacheinfo", "%s,%s,%s" % (mine[0], merged, path), env=env)

    marker = git("hash-object", "-w", "--stdin", input=(a.new_tag + "\n").encode()).stdout.decode().strip()
    git("update-index", "--add", "--cacheinfo", "100644,%s,%s" % (marker, BASE_FILE), env=env)

    tree = out("write-tree", env=env).strip()
    print("tree %s, %d three-way files, %d with conflicts" % (
        tree[:10], sum(1 for s in changes.values() if s == "M"), len(conflicts)))
    for c in conflicts:
        print("  conflict:", c)

    if a.check:
        same = tree == out("rev-parse", ours + "^{tree}").strip()
        print("tree equals ours:", same)
        sys.exit(0 if same and not conflicts else 1)

    if not a.branch:
        sys.exit("give --branch NAME to keep the result, or --check to test")
    msg = "Move FragmentedFox onto %s\n\nApp layer copied from %s, other changes merged three ways from %s.\n" % (
        a.new_tag, ours[:10], base)
    commit = git("commit-tree", tree, "-p", new, "-m", msg).stdout.decode().strip()
    git("branch", "-f", a.branch, commit)
    print("branch %s at %s" % (a.branch, commit[:10]))
    if conflicts:
        print("Conflict markers are committed in the files above. Fix them and commit.")


if __name__ == "__main__":
    main()
