#!/usr/bin/env python3
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

import json
import os
import subprocess
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))


def matches(name, suffixes):
    return any(name == s or name.endswith("." + s) for s in suffixes)


def main():
    out_dir, pcap = sys.argv[1], sys.argv[2]
    if os.path.exists(os.path.join(out_dir, "failed.txt")):
        print("The emulator could not run the build; no report.")
        return 1
    phases = json.load(open(os.path.join(out_dir, "phases.json")))
    lists = json.load(open(os.path.join(ROOT, "fragmentedfox-docs/expected-hosts.json")))
    fields = ["frame.time_epoch", "ip.dst", "tcp.dstport", "udp.dstport", "tls.handshake.extensions_server_name", "dns.qry.name"]
    flt = "tls.handshake.type == 1 || (dns && dns.flags.response == 0) || (tcp.flags.syn == 1 && tcp.flags.ack == 0)"
    cmd = ["tshark", "-r", pcap, "-Y", flt, "-T", "fields"]
    for f in fields:
        cmd += ["-e", f]
    rows = subprocess.run(cmd, capture_output=True, text=True, check=True).stdout.splitlines()

    events = []
    sni_by_ip = {}
    for row in rows:
        cols = (row.split("\t") + [""] * len(fields))[: len(fields)]
        when, ip, tcp, udp, sni, dns = cols
        if not when:
            continue
        port = tcp or udp
        name = sni.split(",")[0] or dns.split(",")[0]
        if sni and ip:
            sni_by_ip[ip] = sni.split(",")[0]
        events.append((float(when), ip, port, name))

    baseline, app = set(), set()
    for when, ip, port, name in events:
        label = name or sni_by_ip.get(ip) or f"{ip}:{port}"
        if phases["baseline"] <= when < phases["app"]:
            baseline.add(label)
        elif when >= phases["app"]:
            app.add(label)
    new = sorted(app - baseline)

    allowed = lists["allowed"]
    forbidden = lists["forbidden"]
    bad = [n for n in new if not matches(n, allowed) and matches(n, forbidden)]
    expected = [n for n in new if matches(n, allowed)]
    other = [n for n in new if n not in bad and n not in expected]

    lines = ["# Traffic capture", ""]
    lines.append(f"Alive after first start: {open(os.path.join(out_dir, 'alive-after-start.txt')).read().strip()}, at the end: {open(os.path.join(out_dir, 'alive-at-end.txt')).read().strip()}")
    lines.append(f"Events in the capture: {len(events)}, hosts before the app: {len(baseline)}, hosts after: {len(app)}")
    lines += ["", "## Unexpected hosts (fail)", ""] + ([f"- {n}" for n in bad] or ["none"])
    lines += ["", "## Expected hosts", ""] + ([f"- {n}" for n in expected] or ["none"])
    lines += ["", "## Other hosts, to review", ""] + ([f"- {n}" for n in other] or ["none"])
    lines += ["", "## Seen before the app started (emulator itself)", ""] + ([f"- {n}" for n in sorted(baseline)] or ["none"])
    report = "\n".join(lines) + "\n"
    print(report)
    open(os.path.join(out_dir, "report.md"), "w").write(report)
    summary = os.environ.get("GITHUB_STEP_SUMMARY")
    if summary:
        open(summary, "a").write(report)
    if not events:
        print("The capture is empty.")
        return 1
    return 1 if bad else 0


sys.exit(main())
