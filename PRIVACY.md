# FragmentedFox privacy

FragmentedFox is a browser. This page says what the Android app sends and where. It is written from a review of the code and the default settings, not from a traffic capture. A capture on a fresh install is planned. Details are in [network contacts](./fragmentedfox-docs/network.md) and [telemetry](./fragmentedfox-docs/telemetry.md).

## What FragmentedFox does not do

- It sends no telemetry, usage statistics or technical data to Fragmented Visions or to Mozilla. The telemetry code is switched off when the app is built, and the first run no longer sends anything.
- Fragmented Visions runs no servers for this app, and there are no accounts to create.
- Sponsored shortcuts, sponsored stories, Pocket stories, ads and Firefox Suggest are off by default.
- It does not download experiments, surveys or feature rollouts from Mozilla, and the ones Mozilla bundled in the app are removed, so Mozilla cannot switch features on in your copy.
- It has no background jobs that talk to Mozilla's messaging or survey systems. The notification worker, the in-app surveys and Mozilla's built-in promotional messages (such as the default browser card and notification) are removed, not just switched off.
- Videos and audio do not start by themselves. Autoplay is blocked by default and a page may only start playback within a few seconds of a tap.
- WebRTC is off, so sites cannot find your local network address through it.
- Tracking parameters are removed from links you open, and other sites only learn the name of the site you came from, never the page. Trackers get no referrer at all.

## Protections that are on by default

- Enhanced Tracking Protection is on Strict, and fingerprinting protection runs in every tab, not only private ones.
- HTTPS-only mode is on in all tabs.
- DNS lookups use encrypted DNS in the "Increased" mode: the encrypted lookup is tried first, and if it fails the app falls back to your normal DNS for that lookup. The "Max" mode never falls back and can be picked in settings.
- Screenshots of private tabs are blocked.
- Backups of the app's data are off.
- Only TLS ciphers with forward secrecy and authentication are offered, TLS 1.3 early data is off, and servers without secure renegotiation are refused.

## What it does contact

| Service | Why | When |
| --- | --- | --- |
| The websites you visit | to load them | when you open a page |
| Your search engine (DuckDuckGo by default) | to search, and to suggest results as you type | when you search. Suggestions can be turned off in settings |
| Quad9 | encrypted DNS lookups, the default resolver | when you load pages. The resolver can be changed in settings |
| Mozilla Remote Settings | tracker lists, the list of tracking link parameters to remove, certificate revocation lists, the add-on blocklist | about every 2 hours |
| Google Safe Browsing | phishing and malware list updates (hash prefixes only) | about once a day |
| addons.mozilla.org | to find, install and update add-ons | when you use add-ons |
| Mozilla account servers | to sync | only if you sign in |
| Mozilla crash reports | to send a crash report | only if you choose to send one |
| Mozilla language data | to translate a page on your device | the first time you translate between a pair of languages |
| webcompat.com (Mozilla) | to send a broken-site report | only if you open Report broken site and submit one |

Mozilla and Google run the services in that table. Their own privacy notices cover what they do with the requests they receive.

## What is stored on your phone

Browsing data, saved logins and saved cards stay on the phone. Nothing is uploaded unless you sign in to a Mozilla account to sync.

- **Saved cards:** the card number is encrypted with AES-256-GCM, using a random 256-bit key made the first time you save a card. The name, expiry date, card type and last 4 digits are stored unencrypted in the same database. The CVV is never stored.
- **Where the key is kept:** on release builds, including ours, the key is stored in the app's private preferences file and is not wrapped by the Android Keystore. This is how Mozilla ships it too. Other apps cannot read the file, and the phone's own storage encryption covers it when the phone is off, but anyone who can read the app's private files (root access or a full device image) can decrypt the card numbers. Backups are off, which closes the usual route to those files.
- **Saved logins** use the same key storage and have the same limit.

Using the Keystore would make the key harder to extract, but on some phones Keystore keys are lost after system updates or restores, and the app then deletes the encrypted data it can no longer read. That is why it is not switched on. See [changes](./fragmentedfox-docs/changes.md).

## Settings you can change

The data collection screen only has the crash report choice (ask, always send or never send). The telemetry, usage and studies switches were removed because telemetry is compiled out, so they would have done nothing.

## Changes

If the app starts contacting something new, this page and the network contacts page are updated in the same change.
