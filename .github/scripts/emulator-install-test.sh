#!/usr/bin/env bash
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

set +e
PKG=com.fragmentedvisions.fragmentedfox
OLD=work/old.apk
NEW=work/new.apk

adb root >/dev/null 2>&1
sleep 3
adb shell getprop ro.build.version.release
adb shell getprop ro.build.version.sdk

echo "== 1. install the older build on a clean emulator"
adb install -r "$OLD" 2>&1
adb shell pm list packages | grep "$PKG"

echo "== 2. reproduce the state the app icon picker left (Dark chosen, main entry disabled)"
adb shell pm enable "$PKG/$PKG.AppSolidDark" 2>&1
adb shell pm disable "$PKG/$PKG.App" 2>&1
adb shell cmd package resolve-activity --brief -c android.intent.category.LAUNCHER "$PKG" 2>&1 | tail -3

echo "== 3. update to the newer build over it"
adb logcat -c
adb install -r "$NEW" 2>&1
echo "-- result of the update above; installer log follows"
adb logcat -d 2>&1 | grep -iE "PackageManager|PackageInstaller|INSTALL_|Parse|parsing" | tail -40
adb shell dumpsys package "$PKG" | grep -E "versionCode|versionName|AppSolidDark|PackageSetting" | head -10
echo "-- launcher entry after the update"
adb shell cmd package resolve-activity --brief -c android.intent.category.LAUNCHER "$PKG" 2>&1 | tail -3

echo "== 4. clean install of the newer build"
adb uninstall "$PKG" 2>&1
adb logcat -c
adb install "$NEW" 2>&1
adb logcat -d 2>&1 | grep -iE "PackageManager|PackageInstaller|INSTALL_|Parse|parsing" | tail -30
adb shell pm list packages | grep "$PKG"
