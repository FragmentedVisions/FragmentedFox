# Building

Builds run on GitHub Actions. Each workflow is started by hand: open the repository's **Actions** tab, choose the workflow, tap **Run workflow** and pick the branch.

| Workflow | File | Output |
| --- | --- | --- |
| Build FragmentedFox (Android) | `.github/workflows/build-android.yml` | `fragmentedfox-android` artifact with the APK |
| Build FragmentedFox (Linux) | `.github/workflows/build-linux.yml` | `fragmentedfox-linux-x86_64` artifact with a tarball |
| Create Android signing key | `.github/workflows/create-signing-key.yml` | One-time job, see [signing](./signing.md) |

## Android

The job bootstraps the Android toolchain, compiles GeckoView from source on all 4 cores with debug symbols off (`--disable-debug-symbols`), then builds the app with `./mach gradle fenix:assembleRelease`. The toolchain download is cached between runs, even when a build fails.

The `shrink` choice (default `on`) shrinks code and resources for a smaller app. Choose `off` to add `-PdisableOptimization` if shrinking fails. The job adds 8 GB of swap, uploads the build logs and a size breakdown of the APK, and prints disk and memory details if a step fails.

Known problems:

- No full compile has finished yet. Earlier runs lost their runner without a log: with 4 jobs and debug symbols about 13 minutes in (twice), and with 2 jobs about 26 minutes in. Memory pressure during the heavy Rust compile is the likely cause but is not confirmed. Debug symbols were turned off to reduce it, and that change has not been tested.
- A full compile on 4 cores may approach GitHub's 6 hour job limit.
- Mozilla's prebuilt engine (artifact builds) cannot be used: the lookup checked 150 upstream revisions and found no built Android artifacts for this base.

To install the APK on a phone, download the artifact, unzip it, and open the APK. Android asks to allow installs from your browser or file manager the first time.

## Linux desktop

The job uses a `mozconfig` with the updater, tests and debug code off, then runs `./mach build` and `./mach package`. It prints the version, vendor and name from `application.ini` as a check.

## Limits

GitHub's free runners have 4 cores, 16 GB of memory and a 6 hour job limit. A full Firefox build uses most of that, so a run can fail from time or memory limits rather than a code error. Read the failing step's log first.
