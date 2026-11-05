# Network contacts

What the Android app can contact, as read from the code and defaults. This is a code review, not a traffic capture. A capture on a fresh install is still worth doing.

## Off by default

| Feature | How |
| --- | --- |
| Telemetry (Glean) | `isTelemetryEnabled` is false |
| Remote experiments (Nimbus) | `isExperimentationEnabled` follows telemetry, so off |
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
| Your search engine | search suggestions are on by default (`shouldShowSearchSuggestions`), so typed text is sent to the engine as you type. This is left on on purpose, and the user can turn it off in settings |
| Quad9 (`dns.quad9.net`) | encrypted DNS, the default resolver |
| Mozilla Remote Settings | tracker lists, certificate revocation filters, add-on blocklist, polled daily |
| Google Safe Browsing | phishing and malware list updates (hash prefixes only) |
| addons.mozilla.org | add-on search, install and update checks |
| Mozilla account servers | only if you sign in to sync |
| Mozilla crash-stats | only if you choose to send a crash report |

## Not contacted on Android

Several desktop prefs in `all.js` name Google or Mozilla URLs, such as the geolocation provider. They are not used by the Android app, which uses the system location service and asks the site permission first.

## Keeping this true

`fragmentedfox-docs/expected-nimbus.json` holds the defaults above and `check-fork.py` fails if one flips back. After an ESR upgrade, check whether new Nimbus features or prefs have been added that contact a server.
