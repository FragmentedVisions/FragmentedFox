# Building

Builds run on GitHub Actions. Each workflow is started by hand: open the repository's **Actions** tab, choose the workflow, tap **Run workflow** and pick the branch.

| Workflow | File | Output |
| --- | --- | --- |
| Build FragmentedFox (Android) | `.github/workflows/build-android.yml` | `fragmentedfox-android` artifact with the APK |
| Build FragmentedFox (Linux) | `.github/workflows/build-linux.yml` | `fragmentedfox-linux-x86_64` artifact with a tarball |
| Create Android signing key | `.github/workflows/create-signing-key.yml` | One-time job, see [signing](./signing.md) |

## Android

The job bootstraps the Android toolchain with `./mach bootstrap --application-choice=mobile_android`, builds GeckoView for arm64 with `./mach build`, then builds the app with `./mach gradle fenix:assembleRelease -PdisableOptimization`. The job adds 8 GB of swap and prints disk and memory details if a step fails.

To install the APK on a phone, download the artifact, unzip it, and open the APK. Android asks to allow installs from your browser or file manager the first time.

## Linux desktop

The job uses a `mozconfig` with the updater, tests and debug code off, then runs `./mach build` and `./mach package`. It prints the version, vendor and name from `application.ini` as a check.

## Limits

GitHub's free runners have 4 cores, 16 GB of memory and a 6 hour job limit. A full Firefox build uses most of that, so a run can fail from time or memory limits rather than a code error. Read the failing step's log first.
