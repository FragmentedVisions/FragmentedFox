# Network contacts

What the Android app can contact, as read from the code and defaults. This is a code review, not a traffic capture. The Check workflow can record the traffic of a build on an emulator (`traffic_run_id`, see [building](building.md)); it has been run, but the capture saw nothing from the app, so its results are not part of this page (see building).

## Off by default

| Feature | How |
| --- | --- |
| Telemetry (Glean) | `isTelemetryEnabled` is false |
| Remote experiments and rollouts (Nimbus) | the app is not given the Remote Settings service, so it never fetches them, and the experiments Mozilla bundled in the app are removed. See the correction below |
| Studies and Normandy | prefs off |
| Partner codes in search URLs | removed from result URLs by `PartnerParams.kt`, so Google, Bing, DuckDuckGo and others get a plain search. Suggestion URLs keep the parameters the engine needs to answer in the right format |
| Search results telemetry | the "ads" and "cookies" web extensions that watched search results pages are not installed in Fenix, and Mozilla's `search-telemetry-v2` list is not downloaded for them (earlier builds up to build 31 did download it) |
| Sponsored tiles (Contile) | `showContileFeature` false |
| Pocket stories and sponsored stories | removed, along with the `service-pocket` library that fetched them |
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
| Mozilla Remote Settings | tracker lists, certificate revocation filters, add-on blocklist and the list of tracking link parameters to strip (`query-stripping`) and the list of sites exempted from fingerprinting protection (`fingerprinting-protection-overrides`, `privacy.fingerprintingProtection.remoteOverrides.enabled` stays on so protected sites that would break are exempted), polled daily |
| Google Safe Browsing | phishing and malware list updates (hash prefixes only) |
| addons.mozilla.org | add-on search, install and update checks |
| Mozilla account servers | only if you sign in to sync |
| Mozilla crash-stats | only if you choose to send a crash report |
| Mozilla Remote Settings attachments | the language data for on-device translation, downloaded the first time you translate a page between a pair of languages (the `translations-models-v2` and `translations-wasm-v2` collections). Translation is on in the toolbar and menu by default |
| webcompat.com | only if you open Report broken site from the menu and submit a report |

## Link tracking parameters, referrers and ECH

- Tracking parameters (such as `fbclid` and `gclid`) are stripped from links in normal and private tabs (`GeckoProvider.kt`). The list is Mozilla's `query-stripping` collection from Remote Settings, which is not bundled, so nothing is stripped until the first sync. The release app has no setting for it.
- Cross-site referrers carry the site name only, even when a page asks to send the full address, and trackers get none (`network.http.referer.XOriginTrimmingPolicy` 2 and `defaultPolicy.trackers` 1, in `geckoview-prefs.js`). Sending referrers to the same site only was not chosen because it breaks logins.
- Encrypted Client Hello is on in the engine (`network.dns.echconfig.enabled` and `network.dns.http3_echconfig.enabled` are true and not overridden). It needs the encrypted DNS lookup to work and only helps for sites that publish an ECH key. DNS is "Quad9 first, then the phone's own DNS" (`network.trr.mode` 2), so during a fallback both are lost. To check on a phone, open `https://crypto.cloudflare.com/cdn-cgi/trace`: `sni=encrypted` means it works.

## Correction: experiments and rollouts

This page used to say remote experiments were off because `isExperimentationEnabled` follows telemetry. That was wrong. Mozilla split participation in two: experiments (studies) and rollouts ("Remote improvements"), and rollouts are decoupled from telemetry and on by default. On every foreground start the app fetched Mozilla's `nimbus-mobile-experiments` collection from Remote Settings without checking either setting, and it applied 11 experiments and rollouts bundled in `res/raw/initial_experiments.json` at first run: two tab groups rollouts, user surveys, a sports widget, the LongFox game, a marketing card change and a growth holdback group. It was found when an unfinished tab groups tab appeared in a build. Now `Components.kt` passes `null` as the Remote Settings service to Nimbus, so nothing is fetched, and the bundled file holds no experiments. Feature settings come from the defaults in `nimbus.fml.yaml` only. The Studies and Remote improvements screens had nothing left to act on and are hidden (see [changes](./changes.md)). `check-fork.py` fails if either change is undone, for example by an ESR merge.

## Telemetry

Telemetry is compiled out. See [telemetry](./telemetry.md) for how that is enforced and for the first-run ping that was found and fixed.

## Cleartext

Plain HTTP from the app's own Java code is refused except for localhost. Pages are covered by HTTPS-Only mode.

## Wallpapers

The app downloads wallpapers at start-up from `BuildConfig.WALLPAPER_URL`. That value comes from a `.wallpaper_url` file that our builds do not have, so it is empty and nothing is fetched.

## Not contacted on Android

Several desktop prefs in `all.js` name Google or Mozilla URLs, such as the geolocation provider. They are not used by the Android app, which uses the system location service and asks the site permission first.

## TLS versions and ciphers

TLS 1.2 is the oldest version accepted and TLS 1.3 is preferred, as in Mozilla. SSL and TLS 1.0 and 1.1 are not accepted. In TLS 1.2 the Android app only offers ECDHE key exchange with AES-GCM or ChaCha20: 3DES, static RSA and CBC ciphers are turned off in `geckoview-prefs.js` (the prefs are named `security.ssl3.*` for historical reasons). A site that offers nothing else fails with a connection error. TLS 1.3 early data (0-RTT) is also off (`security.tls.enable_0rtt_data`), so resumed connections cost one more round trip but nothing sent early can be replayed by an attacker. Safe renegotiation is required (`security.ssl.require_safe_negotiation`): a server that does not support RFC 5746 fails with a connection error, which protects against the 2009 renegotiation attack. Very few servers are left that lack it. This is stricter than Mozilla. The cipher change, 0-RTT off and safe renegotiation are in build 30 (commit `fb02ae860a`), installed on a phone without connection problems reported so far. `check-fork.py` checks these prefs; the reasons and how to allow a cipher again are in [changes](./changes.md).

## Keeping this true

`fragmentedfox-docs/expected-nimbus.json` holds the defaults above and `check-fork.py` fails if one flips back. After an ESR upgrade, check whether new Nimbus features or prefs have been added that contact a server.
