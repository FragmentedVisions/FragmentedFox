# FragmentedFox privacy

FragmentedFox is a browser. This page says what the Android app sends and where. It is written from a review of the code and the default settings, not from a traffic capture. A capture on a fresh install is planned. Details are in [network contacts](./fragmentedfox-docs/network.md) and [telemetry](./fragmentedfox-docs/telemetry.md).

## What FragmentedFox does not do

- It sends no telemetry, usage statistics or technical data to Fragmented Visions or to Mozilla. The telemetry code is switched off when the app is built, and the first run no longer sends anything.
- Fragmented Visions runs no servers for this app, and there are no accounts to create.
- Sponsored shortcuts, sponsored stories, Pocket stories, ads and Firefox Suggest are off by default.
- It does not download experiments, surveys or feature rollouts from Mozilla, and the ones Mozilla bundled in the app are removed, so Mozilla cannot switch features on in your copy.
- WebRTC is off.

## What it does contact

| Service | Why | When |
| --- | --- | --- |
| The websites you visit | to load them | when you open a page |
| Your search engine (DuckDuckGo by default) | to search, and to suggest results as you type | when you search. Suggestions can be turned off in settings |
| Quad9 | encrypted DNS lookups, the default resolver | when you load pages. The resolver can be changed in settings |
| Mozilla Remote Settings | tracker lists, certificate revocation lists, the add-on blocklist | about once a day |
| Google Safe Browsing | phishing and malware list updates (hash prefixes only) | about once a day |
| addons.mozilla.org | to find, install and update add-ons | when you use add-ons |
| Mozilla account servers | to sync | only if you sign in |
| Mozilla crash reports | to send a crash report | only if you choose to send one |

Mozilla and Google run the services in that table. Their own privacy notices cover what they do with the requests they receive.

## Settings you can change

Settings has a data collection section. Its switches are off by default and, because telemetry is compiled out, switching them on does not make the app send telemetry.

## Changes

If the app starts contacting something new, this page and the network contacts page are updated in the same change.
