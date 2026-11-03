# Building

Builds run on GitHub Actions. Each workflow is started by hand: open the repository's **Actions** tab, choose the workflow, tap **Run workflow** and pick the branch.

| Workflow | File | Output |
| --- | --- | --- |
| Build FragmentedFox (Android) | `.github/workflows/build-android.yml` | `fragmentedfox-android` artifact with the APK |
| Build FragmentedFox (Linux) | `.github/workflows/build-linux.yml` | `fragmentedfox-linux-x86_64` artifact with a tarball |
| Create Android signing key | `.github/workflows/create-signing-key.yml` | One-time job, see [signing](./signing.md) |

## Android

The workflow has a `mode` choice when started:

- `artifact` (default) downloads Mozilla's prebuilt GeckoView engine and only builds the app on top. This is fast and avoids compiling the engine. It only works if Mozilla published engine artifacts for the base revision, and it is only valid while FragmentedFox changes no compiled engine code.
- `full` compiles GeckoView from source with 2 parallel jobs. Two full runs on GitHub's free runners were killed about 13 minutes in, during the heavy Rust compile, with "The runner has received a shutdown signal". Memory pressure is the likely cause but is not confirmed.

Either way the job then builds the app with `./mach gradle fenix:assembleRelease -PdisableOptimization`. It adds 8 GB of swap and prints disk and memory details if a step fails.

To install the APK on a phone, download the artifact, unzip it, and open the APK. Android asks to allow installs from your browser or file manager the first time.

## Linux desktop

The job uses a `mozconfig` with the updater, tests and debug code off, then runs `./mach build` and `./mach package`. It prints the version, vendor and name from `application.ini` as a check.

## Limits

GitHub's free runners have 4 cores, 16 GB of memory and a 6 hour job limit. A full Firefox build uses most of that, so a run can fail from time or memory limits rather than a code error. Read the failing step's log first.
