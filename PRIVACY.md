# FragmentedFox privacy

FragmentedFox is a browser. This page says what the Android app sends and where. It is written from a review of the code and the default settings, not from a traffic capture. A capture on a fresh install is planned. Details are in [network contacts](./fragmentedfox-docs/network.md) and [telemetry](./fragmentedfox-docs/telemetry.md).

## What FragmentedFox does not do

- It sends no telemetry, usage statistics or technical data to Fragmented Visions or to Mozilla. The telemetry code is switched off when the app is built, and the first run no longer sends anything.
- Fragmented Visions runs no servers for this app, and there are no accounts to create.
- Sponsored shortcuts, sponsored stories, Pocket stories, ads and Firefox Suggest are off by default.
- It does not download experiments, surveys or feature rollouts from Mozilla, and the ones Mozilla bundled in the app are removed, so Mozilla cannot switch features on in your copy.
- WebRTC is off.
- Tracking parameters are removed from links you open, and other sites only learn the name of the site you came from, never the page. Trackers get no referrer at all.

## What it does contact

| Service | Why | When |
| --- | --- | --- |
| The websites you visit | to load them | when you open a page |
| Your search engine (DuckDuckGo by default) | to search, and to suggest results as you type | when you search. Suggestions can be turned off in settings |
| Quad9 | encrypted DNS lookups, the default resolver | when you load pages. The resolver can be changed in settings |
| Mozilla Remote Settings | tracker lists, the list of tracking link parameters to remove, certificate revocation lists, the add-on blocklist | about once a day |
| Google Safe Browsing | phishing and malware list updates (hash prefixes only) | about once a day |
| addons.mozilla.org | to find, install and update add-ons | when you use add-ons |
| Mozilla account servers | to sync | only if you sign in |
| Mozilla crash reports | to send a crash report | only if you choose to send one |
| Mozilla language data | to translate a page on your device | the first time you translate between a pair of languages |
| webcompat.com (Mozilla) | to send a broken-site report | only if you open Report broken site and submit one |

Mozilla and Google run the services in that table. Their own privacy notices cover what they do with the requests they receive.

## Settings you can change

The data collection screen only has the crash report choice (ask, always send or never send). The telemetry, usage and studies switches were removed because telemetry is compiled out, so they would have done nothing.

## Changes

If the app starts contacting something new, this page and the network contacts page are updated in the same change.
