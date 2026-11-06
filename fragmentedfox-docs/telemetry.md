# Telemetry and what the app can send

FragmentedFox is built without Mozilla's telemetry (`TELEMETRY` is off in `build.gradle`). The Settings switches for it still exist, but they cannot make the app upload anything. This page records how that is enforced and one leak that was found and fixed.

## What is blocked, and where

| Control | Where | Effect |
| --- | --- | --- |
| Metric controller | `MetricController.create` | With `TELEMETRY` off it returns a controller that only logs. No metrics service is ever started. |
| Glean upload | `GleanHelper.kt` | `uploadEnabled` is the Settings switch and `BuildConfig.TELEMETRY`, so the switch alone cannot turn uploads on. |
| Onboarding opt-out ping | `OnboardingFragment.kt` | Sent only when `BuildConfig.TELEMETRY` is on. |
| Prefs | `all.js`, `geckoview-prefs.js` | The engine's own data reporting prefs are off. |

## The onboarding opt-out ping (fixed)

Mozilla's code sends an `onboarding-opt-out` ping when someone finishes the first-run terms card with telemetry off, so that Mozilla can count people who declined. Unlike every other ping, it is sent even when collection is off (`follows_collection_enabled: false` in `pings.yaml`). The ping has no client ID and no device details, but it is still a request to Mozilla's telemetry server, which contradicts the first-run text.

- Present in Mozilla's ESR 153.6.0 and in every FragmentedFox build up to and including the one at `a82cf4a57c` (build 14).
- It fired once per fresh install, when Continue was tapped on the terms card. Updating an existing install or clearing nothing did not send it.
- Whether a given ping reached the server was not observed. This is from reading the code, not from a network capture.
- Fixed in `8e5af27eec`: the ping and the Glean upload switch are tied to `BuildConfig.TELEMETRY`.

## Keeping it fixed

`python3 .github/scripts/check-fork.py` (the `telemetry` check) fails if:

- Glean's upload is no longer tied to `BuildConfig.TELEMETRY`;
- the onboarding opt-out ping is no longer tied to it;
- the metric controller no longer falls back to the logging-only controller when `TELEMETRY` is off;
- `pings.yaml` contains a ping that ignores the collection setting and is not on the reviewed list below. Mozilla can add one in any release, so this check runs on every upgrade.

Reviewed pings that ignore the collection setting: `onboarding-opt-out` (gated, see above), `usage-reporting` and `usage-deletion-request` (both sent only by the usage reporting service, which never starts when `TELEMETRY` is off).

If the check reports a new ping, find out where it is submitted, make sure that code only runs when `BuildConfig.TELEMETRY` is on, and then add the ping to `REVIEWED_UNCONDITIONAL_PINGS` in `check-fork.py` and to the list above.

## Still possible by user choice

Turning on "Usage and technical data" in Settings starts Mozilla's experiments (Nimbus) over Remote Settings. It does not upload telemetry. See [changes](./changes.md) for the open question about hiding these switches.
