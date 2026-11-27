# Build status

What has been built, checked on a phone, and what is waiting. Update this page whenever a build runs or `Stable-Main` moves; the README and the other pages link here instead of repeating build numbers.

## Branches

| Branch | Purpose |
| --- | --- |
| `Stable-Main` | The default branch. Only moves forward after a `NonStable--test` build succeeds and the app has been checked on a phone, and only when the owner says so. Currently the state of build 43 (commit `f4eee4d61f`, Android build run 44507615003); later commits on it are documentation only. |
| `NonStable--test` | Where all work happens and builds are started from. |

The app stays on ESR 153.6.0. An upgrade to 153.7.0 was tried (build 38) and dropped; see [upgrading](./upgrading.md).

## Builds

| Build | Run | Commit | Result |
| --- | --- | --- | --- |
| 37 | 43492714687 | `cbd89e5d79` | Passed, installed and checked on a phone. Was `Stable-Main` until build 43. Known minor bug: see [known gaps](./known-gaps.md). |
| 38 | 43894514143 | `73fc64c766` | ESR 153.7.0 upgrade. Built, but dropped (pages got stuck after a Google Store tab change). |
| 40 | 44285769439 | `2c79351500` | ESR 153.6 with Safe Browsing off, no look-ahead connections, no DNS prefetch, no battery API and the Base64 fixes. Passed; waiting for a phone check. |
| 41 | 44304046692 | `9be010b72a` | Adds the Terms of Use prompt removal, a two-part backport of Mozilla's cache fix (bug 2052908), removal of the first-run "Manage settings" dialog, unused Glean metrics and baseline entries, and GitHub Actions pinned to commit SHAs. Passed. The Google Store repro on a phone is the check for the cache backport. |
| 42 | 44364545520 | `350c23a9bf` | Failed twice (two attempts) in "Build the app" after about 47 minutes: "The hosted runner lost communication with the server", with no logs kept. The engine step passed both times. The commit only deletes code, so it is likely memory starvation on the runner, not a compile error. The workflow now caps Gradle workers and the Kotlin daemon and prints memory every minute. |
| 43 | 44507615003 | `f4eee4d61f` | Passed, with Gradle capped at 3 workers and the Kotlin daemon at 3 GB. Contains everything listed under builds 40 and 41 plus the Lens and Labs removal, the Actions upgrades, the catalog cleanup, the IP Protection removal, the blank OHTTP URLs, Wi-Fi tickle off and the intermediate certificate preload off. This is `Stable-Main`. |

## On `NonStable--test` but not built yet

- JPEG XL decoder compiled out of the Android build (`--disable-jxl`, see [defaults](./defaults.md)). A configure failure points here first.
- Tracker counts: the all-time total now counts the same four categories as the weekly card (see [maintenance](./maintenance.md)).

## Still to do by hand

- Delete the remote branches `upgrade-153-7` and `upgrade-153-7-cachetest` on GitHub.
- Check the cache backport with the Google Store repro on a phone.
