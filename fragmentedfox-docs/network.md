# Network contacts

What the Android app can contact, as read from the code and defaults. This is a code review, not a traffic capture. A capture on a fresh install is still worth doing.

## Off by default

| Feature | How |
| --- | --- |
| Telemetry (Glean) | `isTelemetryEnabled` is false |
| Remote experiments and rollouts (Nimbus) | the app is not given the Remote Settings service, so it never fetches them, and the experiments Mozilla bundled in the app are removed. See the correction below |
| Studies and Normandy | prefs off |
| Sponsored tiles (Contile) | `showContileFeature` false |
| Pocket stories and sponsored stories | home screen sections default to false in `nimbus.fml.yaml` |
| Mozilla Ads Client for top sites | `mozilla-ads-client` default false |
| Merino manifest and Merino client | defaults false |
| Firefox Suggest | default false on the release channel |
| Captive portal and connectivity checks | prefs off |
| Download reputation lookups | `browser.safebrowsing.downloads.remote.enabled` false |
| Link prefetch | `network.prefetch-next` false |
| Region lookup (Mozilla Location Service) | no token in this build, so the offline default is used |
| WebRTC | disabled at build time and by pref |

## Still contacted, on purpose

| Service | Why |
| --- | --- |
| Your search engine (DuckDuckGo by default) | search suggestions are on by default (`shouldShowSearchSuggestions`), so typed text is sent to the engine as you type. This is left on on purpose, and the user can turn it off in settings |
| Quad9 (`dns.quad9.net`) | encrypted DNS, the default resolver |
| Mozilla Remote Settings | tracker lists, certificate revocation filters, add-on blocklist, polled daily |
| Google Safe Browsing | phishing and malware list updates (hash prefixes only) |
| addons.mozilla.org | add-on search, install and update checks |
| Mozilla account servers | only if you sign in to sync |
| Mozilla crash-stats | only if you choose to send a crash report |

## Correction: experiments and rollouts

This page used to say remote experiments were off because `isExperimentationEnabled` follows telemetry. That was wrong. Mozilla split participation in two: experiments (studies) and rollouts ("Remote improvements"), and rollouts are decoupled from telemetry and on by default. On every foreground start the app fetched Mozilla's `nimbus-mobile-experiments` collection from Remote Settings without checking either setting, and it applied 11 experiments and rollouts bundled in `res/raw/initial_experiments.json` at first run: two tab groups rollouts, user surveys, a sports widget, the LongFox game, a marketing card change and a growth holdback group. It was found when an unfinished tab groups tab appeared in a build. Now `Components.kt` passes `null` as the Remote Settings service to Nimbus, so nothing is fetched, and the bundled file holds no experiments. Feature settings come from the defaults in `nimbus.fml.yaml` only. The Studies and Remote improvements screens still exist but have nothing to act on. `check-fork.py` fails if either change is undone, for example by an ESR merge.

## Telemetry

Telemetry is compiled out. See [telemetry](./telemetry.md) for how that is enforced and for the first-run ping that was found and fixed.

## Cleartext

Plain HTTP from the app's own Java code is refused except for localhost. Pages are covered by HTTPS-Only mode.

## Not contacted on Android

Several desktop prefs in `all.js` name Google or Mozilla URLs, such as the geolocation provider. They are not used by the Android app, which uses the system location service and asks the site permission first.

## Keeping this true

`fragmentedfox-docs/expected-nimbus.json` holds the defaults above and `check-fork.py` fails if one flips back. After an ESR upgrade, check whether new Nimbus features or prefs have been added that contact a server.
