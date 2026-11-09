#!/usr/bin/env bash
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

set +e
PKG=com.fragmentedvisions.fragmentedfox
APK="${APK:?}"
OUT="${TRAFFIC_DIR:?}"
mkdir -p "$OUT"

ABIS=$(adb shell getprop ro.product.cpu.abilist | tr -d '\r')
echo "Emulator ABIs: $ABIS"
case "$ABIS" in
  *arm64-v8a*) ;;
  *) echo "This emulator cannot run the arm64 build, so there is nothing to capture."; echo "no-arm64" > "$OUT/failed.txt"; exit 1 ;;
esac

echo "== network check from inside the emulator"
{
  echo "-- ping an address"; adb shell ping -c 2 -W 3 8.8.8.8
  echo "-- ping a name"; adb shell ping -c 2 -W 3 example.org
  echo "-- fetch over https"; adb shell "toybox nc -w 5 example.org 443 </dev/null; echo nc exit \$?"
  echo "-- connectivity"; adb shell dumpsys connectivity | grep -i -E 'validated|state:' | head -6
  echo "-- dns"; adb shell getprop net.dns1; adb shell settings get global private_dns_mode
} 2>&1 | tee "$OUT/network-check.txt"

echo "== 1. baseline, emulator only"
BASELINE_START=$(date +%s)
sleep 60
APP_START=$(date +%s)

echo "== 2. install and first start"
adb install "$APK" 2>&1 | tail -2
adb logcat -c
adb shell monkey -p "$PKG" -c android.intent.category.LAUNCHER 1 2>&1 | tail -1
sleep 150
adb exec-out screencap -p > "$OUT/first-start.png"
if adb shell pidof "$PKG" | grep -q '[0-9]'; then echo yes > "$OUT/alive-after-start.txt"; else echo no > "$OUT/alive-after-start.txt"; fi

echo "== 3. open a page"
URL_START=$(date +%s)
adb shell am start -a android.intent.action.VIEW -d https://example.org/ -p "$PKG" 2>&1 | tail -1
sleep 60
adb exec-out screencap -p > "$OUT/page.png"

echo "== 4. in the background"
BACKGROUND_START=$(date +%s)
adb shell input keyevent KEYCODE_HOME
sleep 90
END=$(date +%s)

adb logcat -d > "$OUT/logcat.txt" 2>&1
{ echo "-- connectivity at the end"; adb shell dumpsys connectivity | grep -i -E 'validated|state:' | head -6; } 2>&1 | tee -a "$OUT/network-check.txt"
grep -i -E 'UnknownHost|ECONNREFUSED|ENETUNREACH|SocketTimeout|SSLHandshake|net::ERR|NS_ERROR_(UNKNOWN_HOST|NET|CONNECTION)' "$OUT/logcat.txt" | head -30 > "$OUT/app-network-errors.txt"
if adb shell pidof "$PKG" | grep -q '[0-9]'; then echo yes > "$OUT/alive-at-end.txt"; else echo no > "$OUT/alive-at-end.txt"; fi
printf '{"baseline":%s,"app":%s,"url":%s,"background":%s,"end":%s}\n' "$BASELINE_START" "$APP_START" "$URL_START" "$BACKGROUND_START" "$END" > "$OUT/phases.json"
