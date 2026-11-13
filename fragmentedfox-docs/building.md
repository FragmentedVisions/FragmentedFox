# Building

Builds run on GitHub Actions. Each workflow is started by hand: open the repository's **Actions** tab, choose the workflow, tap **Run workflow** and pick the branch.

| Workflow | File | Output |
| --- | --- | --- |
| Build FragmentedFox (Android) | `.github/workflows/build-android.yml` | `fragmentedfox-android` artifact with the APK |
| Build FragmentedFox (Linux) | `.github/workflows/build-linux.yml` | `fragmentedfox-linux-x86_64` artifact with a tarball |
| Check FragmentedFox | `.github/workflows/checks.yml` | Runs the quick guard on its own, see Checks below |
| Create Android signing key | `.github/workflows/create-signing-key.yml` | One-time job, see [signing](./signing.md) |

## Android

The job bootstraps the Android toolchain, compiles GeckoView from source on all 4 cores with debug symbols off (`--disable-debug-symbols`) WebRTC left out (`--disable-webrtc`) and only Fenix configured in Gradle (`--enable-android-subproject=fenix`), then builds the app with `./mach gradle fenix:assembleRelease`. The C and C++ compile output is cached between runs with ccache (saved even when a build fails), so a rerun after a small fix reuses most of it. Rust code is not cached. The toolchain download is not cached: caching the tools folder made bootstrap fail, because the Android NDK installer tried to unzip over the restored copy.

The `shrink` choice (default `on`) shrinks code and resources for a smaller app. Choose `off` to add `-PdisableOptimization` if shrinking fails. The job adds 8 GB of swap, uploads the build logs and a size breakdown of the APK, and prints disk and memory details if a step fails.

Build notes:

- Full builds pass. The latest (run 13, with a warm cache) took 65 minutes: about 29 for the engine and 27 for the app. Earlier figures: with a cold compile cache the engine compile took about 97 minutes. With the cache from the previous run it took about 20 minutes, with about half of all cached C and C++ compiles reused, and the whole job took 47 minutes. The app build took about 40 minutes, 25 of which were Android lint, which the workflow now skips (`-x :fenix:lintVitalRelease`), bringing it to about 19 minutes. The APK is signed with the release key: the `apksigner` check prints the certificate SHA-256 shown in [signing](./signing.md). Gradle output goes to `obj-android/gradle/build/mobile/android/fenix/app/outputs/apk/release/`, and only the `arm64-v8a` APK is a complete phone build, since only the arm64 engine is compiled.
- The earliest runs lost their runner without a log (about 13 minutes in with 4 jobs and debug symbols, twice, and about 26 minutes in with 2 jobs). Memory pressure during the heavy Rust compile is the likely cause. Turning debug symbols off and leaving WebRTC out fixed it, and the engine compile then finished (about 87 minutes cold). A build without WebRTC is a supported configuration, but Mozilla ships Android with WebRTC, so a new failure after an upgrade is possible.
- A cold full compile on 4 cores can approach GitHub's 6 hour job limit, so keep the compile cache.
- Mozilla's prebuilt engine (artifact builds) cannot be used: the lookup checked 150 upstream revisions and found no built Android artifacts for this base.

To install the APK on a phone, download the artifact. It always arrives as a zip, even if the file is renamed `.apk`, and Android then says "Can't install app". Rename it to `.zip`, decompress it, and open the APK inside. Android asks to allow installs from your browser or file manager the first time. Builds signed with the release key update over an installed copy.

The workflow runs on the branch you pick. Build from `NonStable--test` to test new changes. Build from `Stable-Main` only to rebuild the last known good version.

## Linux desktop

The job uses a `mozconfig` with the updater, tests and debug code off, then runs `./mach build` and `./mach package`. It prints the version, vendor and name from `application.ini` as a check.

## Limits

GitHub's free runners have 4 cores, 16 GB of memory and a 6 hour job limit. A full Firefox build uses most of that, so a run can fail from time or memory limits rather than a code error. Read the failing step's log first.

## Checks

`.github/scripts/check-fork.py` is a quick guard that runs in seconds. The Android build runs it before the long compile, and the `Check FragmentedFox` workflow runs it on its own. It checks that:

- every `org.mozilla.fenix` import points at something that still exists, and every string and drawable used in code exists (this catches code that still points at something deleted, which would otherwise fail 90 minutes into a build);
- nothing from `fragmentedfox-docs/removed.json` (the removed SDKs and features) has come back;
- the prefs in `expected-prefs.json` and the Android defaults in `expected-settings.json` and the Nimbus defaults in `expected-nimbus.json` still have the values FragmentedFox sets (an ESR upgrade can silently change them);
- cleartext traffic is still refused, DuckDuckGo is still the preferred default search engine, and the Android defaults flipped by FragmentedFox (ads client, expanded toolbar and others) have not changed back;
- string, drawable and raw resources used by the unit tests exist as well, so removing a resource from the app cannot leave a test pointing at it (the instrumentation tests in `androidTest` were deleted);
- every Glean metric and ping the app code records is defined in `metrics.yaml` and `pings.yaml`, and every `FxNimbus.features` flag exists in the FML files (these are generated classes, so a metric or flag removed while code still uses it only fails deep in the compile);
- every top level `projectDir` in `settings.gradle` points at a folder that exists, and every `::function` reference in the app points at a function that still exists (both only failed minutes or an hour into a build before);
- the Glean pings, YAML and XML files are consistent.
- the 23 alternative launcher aliases are still in the manifest and the install permission removal has not come back (see [changes](./changes.md)); the dark theme is the default and the Fragmented Visions colours are applied; and no android-components module refers to a palette colour it cannot see.

The `Check FragmentedFox` workflow has options besides the guard, all started by hand:

- `run_id` (and `compare_run_id`) inspect the APK of a finished build without building anything: signature, alignment, manifest, permissions, launcher aliases, and every resource file the resource table points to. A second run id compares two builds.
- `emulator` installs both builds on an Android emulator to see the exact install error.
- `traffic_run_id` runs the build from that run on an Android emulator for about six minutes (one minute of the emulator alone, then a fresh start, a page load and the app in the background) and records every server it contacts. The report lists hosts the app contacted that the bare emulator did not, and fails if one of them is on the forbidden list in `expected-hosts.json` (Mozilla domains and known tracking services) and not on its allowed list. The capture shows server names and addresses only, not what was sent. It needs an emulator that can run arm64 apps and stops early if it cannot. It was run three times on build 28 and the capture was blind each time: it recorded only the emulator's own start-up traffic and nothing after the app started, not even the page the script opens, so it has not shown what the app contacts. The report fails in that case. The cause is not found; the recorder may stop early (its last packet came 130 seconds in, on a run of 364 seconds). The first run passed before that check existed, so disregard it.
- `backup_key` exports an encrypted backup of the signing key, see [signing](./signing.md).

The scripts in `.github/scripts` that rewrite Mozilla's files are meant to be run again after an upgrade: `make-android-icons.py` (logo and picker icons) and `make-android-colors.py` (the Fragmented Visions palette).

`check-baseline.txt` lists older findings that are false alarms (generated code and other modules), so only new problems fail. Run `python3 .github/scripts/check-fork.py` locally before pushing. After adding a deliberate change, add its entry to the matching JSON file.

## Size analysis

Every Android build also runs `.github/scripts/size-analysis.py` (it runs from both branches) and uploads `size-analysis.txt` as the `size-analysis` artifact. It lists the APK by folder and file, the engine's built-in JavaScript (`omni.ja`) by folder, app code by package, and the largest compiled symbols in `libxul.so` and `libmegazord.so` grouped by namespace. Use it to pick what to trim. The step never fails the build.

In the latest report (before the newest removals), the arm64 app's compressed parts add up to roughly 98 MB: native libraries about 73 MB, built-in assets about 12 MB, app code about 8 MB and resources about 4 MB. The engine library `libxul.so` is 143.6 MB uncompressed and the Rust services library `libmegazord.so` is 21 MB, both unchanged by the app removals. The built-in JavaScript is about 36 MB uncompressed, of which DevTools is 5.8 MB, the PDF viewer 4.8 MB and the remote automation agent 2 MB. App code was about 1.2 MB (6%) smaller after the SDK removals. The biggest size cuts would come from the engine, not the app.
