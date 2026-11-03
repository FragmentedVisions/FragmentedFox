# Building

Builds run on GitHub Actions. Each workflow is started by hand: open the repository's **Actions** tab, choose the workflow, tap **Run workflow** and pick the branch.

| Workflow | File | Output |
| --- | --- | --- |
| Build FragmentedFox (Android) | `.github/workflows/build-android.yml` | `fragmentedfox-android` artifact with the APK |
| Build FragmentedFox (Linux) | `.github/workflows/build-linux.yml` | `fragmentedfox-linux-x86_64` artifact with a tarball |
| Create Android signing key | `.github/workflows/create-signing-key.yml` | One-time job, see [signing](./signing.md) |

## Android

The workflow has a `mode` choice when started:

- `full` (default) compiles GeckoView from source with 2 parallel jobs.
- `artifact` is meant to download Mozilla's prebuilt GeckoView engine. It does not work for this base: the lookup checked 150 upstream revisions and found no built Android artifacts, so it fails in under a minute.  Two earlier full runs with 4 parallel jobs were killed about 13 minutes in, during the heavy Rust compile, with "The runner has received a shutdown signal". Memory pressure is the likely cause but is not confirmed.

Either way the job then builds the app with `./mach gradle fenix:assembleRelease`. The `shrink` choice (default `on`) shrinks code and resources for a smaller app. Choose `off` to add `-PdisableOptimization` if shrinking fails. The job adds 8 GB of swap and prints disk and memory details if a step fails.

To install the APK on a phone, download the artifact, unzip it, and open the APK. Android asks to allow installs from your browser or file manager the first time.

## Linux desktop

The job uses a `mozconfig` with the updater, tests and debug code off, then runs `./mach build` and `./mach package`. It prints the version, vendor and name from `application.ini` as a check.

## Limits

GitHub's free runners have 4 cores, 16 GB of memory and a 6 hour job limit. A full Firefox build uses most of that, so a run can fail from time or memory limits rather than a code error. Read the failing step's log first.
