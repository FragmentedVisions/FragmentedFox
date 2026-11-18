# Build status

What has been built, checked on a phone, and what is waiting. Update this page whenever a build runs or `Stable-Main` moves; the README and the other pages link here instead of repeating build numbers.

## Branches

| Branch | Purpose |
| --- | --- |
| `Stable-Main` | The default branch. Only moves forward after a `NonStable--test` build succeeds and the app has been checked on a phone, and only when the owner says so. Currently the state of build 37 (commit `cbd89e5d79`, Android build run 43492714687); later commits on it are documentation only. |
| `NonStable--test` | Where all work happens and builds are started from. |

The app stays on ESR 153.6.0. An upgrade to 153.7.0 was tried (build 38) and dropped; see [upgrading](./upgrading.md).

## Builds

| Build | Run | Commit | Result |
| --- | --- | --- | --- |
| 37 | 43492714687 | `cbd89e5d79` | Passed, installed and checked on a phone. This is `Stable-Main`. Known minor bug: see [known gaps](./known-gaps.md). |
| 38 | 43894514143 | `73fc64c766` | ESR 153.7.0 upgrade. Built, but dropped (pages got stuck after a Google Store tab change). |
| 40 | 44285769439 | `2c79351500` | ESR 153.6 with Safe Browsing off, no look-ahead connections, no DNS prefetch, no battery API and the Base64 fixes. Passed; waiting for a phone check. |
| 41 | 44304046692 | `9be010b72a` | Adds the Terms of Use prompt removal, a two-part backport of Mozilla's cache fix (bug 2052908), removal of the first-run "Manage settings" dialog, unused Glean metrics and baseline entries, and GitHub Actions pinned to commit SHAs. Passed. The Google Store repro on a phone is the check for the cache backport. |

## On `NonStable--test` but not built yet

- Google Lens and Firefox Labs removal.
- GitHub Actions upgraded to current versions, and the version catalog cleaned.
- Target SDK guard (target stays at 36; see [maintenance](./maintenance.md)).
- IP Protection engine code removed for Android, OHTTP URLs blank, Wi-Fi tickle off, intermediate certificate preload off (see [defaults](./defaults.md) and [removed](./removed.md)). These touch `api.txt`, so a failure in the GeckoView API check points there first.
- This documentation reorganisation.

## Still to do by hand

- Delete the remote branches `upgrade-153-7` and `upgrade-153-7-cachetest` on GitHub.
- Check the cache backport with the Google Store repro on a phone.
