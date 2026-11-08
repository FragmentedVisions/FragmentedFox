# Licensing

## What applies

FragmentedFox is built from Mozilla's source and is covered by the Mozilla Public License 2.0 (MPL 2.0). Mozilla's own notices stay in every file. Do not remove or change them.

## Where the licence texts are

| What | Where |
| --- | --- |
| The repository licence | `LICENSE` (unchanged from Mozilla). It points to `toolkit/content/license.html`, which holds the MPL 2.0 text and the licences of bundled third party code. |
| In the app | Settings, About, Licensing information opens `about:license`, which shows `toolkit/content/license.html` from inside the app. |
| Android library list | Settings, About, Open source libraries. In our builds this still shows Mozilla's placeholder text, see the open points below. |
| Source code | This repository is the source of every build. |

## Files added by FragmentedFox

Source files we added (the scripts in `.github/scripts`, the generated Android icons) carry the standard MPL header. Documentation, workflow files and JSON lists have none, as in Mozilla's tree. Add the header to any new script or source file.

## Names and logos

The FragmentedFox and Fragmented Visions names and the fox artwork are reserved, see `TRADEMARKS.md` in the repository root.

"Firefox" and the Firefox logo are Mozilla's trademarks and are not used for FragmentedFox's own branding. Names of real Mozilla services (Relay, Monitor, Send, Profiler) stay as they are. Mozilla's branding folders (`browser/branding/aurora`, `nightly`, `official` and `unofficial`, and `mobile/android/branding/beta`, `nightly`, `official` and `unofficial`) are left in the source tree and are not used by FragmentedFox builds. Remaining Mozilla artwork is listed under known gaps in `changes.md`.

## Open points

- The Android open source libraries screen was a placeholder because Google's licence plugin was only applied to official Mozilla builds. It is now applied to our builds, but this has not been built or checked on a phone yet (see `changes.md`).
