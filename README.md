<p align="center">
  <img src="./browser/branding/fragmentedfox/source/icon.svg" alt="FragmentedFox" width="160">
</p>

# FragmentedFox

FragmentedFox is a private, independent fork of the Firefox browser from [Fragmented Visions](https://github.com/FragmentedVisions). It removes telemetry, studies, sponsored content and unneeded background connections, and ships under its own name, icons and signing key.

> FragmentedFox is not affiliated with, endorsed by or sponsored by Mozilla. Firefox and the Firefox logo are trademarks of the Mozilla Foundation. This project is built from Mozilla's open source code, which is available under the [Mozilla Public License 2.0](https://www.mozilla.org/MPL/2.0/).

## Status

Early and experimental. The Android app builds on GitHub Actions into a signed APK that installs and runs on a phone. It has had only light testing. The latest build (build 30, commit `fb02ae860a`) passed and was installed and checked on a phone. It has one known minor bug, listed under known gaps in [changes](./fragmentedfox-docs/changes.md).

| Platform | Status |
| --- | --- |
| Android (arm64) | Builds on GitHub Actions into a signed APK (about 65 minutes with a warm compile cache). Installed and checked on a phone. |
| Linux desktop | Workflow written, not run yet. |
| Windows, macOS, iOS | Not supported yet. |

## Base version

FragmentedFox follows Mozilla's [Extended Support Release](https://support.mozilla.org/kb/choosing-firefox-update-channel) branch, currently ESR 153 (tag `FIREFOX_153_6_0esr_RELEASE`), so it picks up security fixes without the feature churn of the main development branch. See [upgrading](./fragmentedfox-docs/upgrading.md).

## What is different

- Telemetry, data reporting, studies (Normandy, Nimbus rollouts) and crash report sending are off by default.
- Sponsored content on the new tab page, Pocket recommendations on Android (removed), sponsored tiles and the VPN, pin and Firefox Focus promos are off.
- Captive portal and connectivity checks, link prefetching and remote download checks are off.
- HTTPS-Only mode is on, and DNS is encrypted by default (Quad9, falling back to normal DNS).
- TLS 1.2 connections only use ciphers with forward secrecy and authenticated encryption (ECDHE with AES-GCM or ChaCha20). 3DES, static RSA and CBC ciphers are off, so a few older sites that offer nothing else will not connect. TLS 1.3 early data (0-RTT) is also off, since it can be replayed, and servers that do not support secure renegotiation are refused. All of this is in build 30 (commit `fb02ae860a`), which was installed on a phone; sites were not tested systematically. See [changes](./fragmentedfox-docs/changes.md).
- Enhanced Tracking Protection is on Strict by default, and fingerprinting protection runs in every tab at that level. Some sites may need an exception from the shield in the toolbar. Compiled in build 31; not checked on a phone yet. See [changes](./fragmentedfox-docs/changes.md).
- Only the selected tab and the most recently used other tab stay running. Older tabs are shut down and reload when opened, so they run no scripts and make no connections in the background. Tabs playing media can be kept running with a setting. It is in build 30 (commit `fb02ae860a`); reloading on return, typed form input and media playback were checked on a phone. See [changes](./fragmentedfox-docs/changes.md).
- WebRTC is left out of the build and switched off in the prefs.
- On first run the Android app offers to install uBlock Origin and Privacy Badger, and only does so if you accept.
- Its own name, icons, wordmark, vendor (`FragmentedVisions`) and Android application ID (`com.fragmentedvisions.fragmentedfox`).

### Removed from the Android app

Mozilla's VPN, the Adjust, Firebase, Google Play Integrity and Sentry SDKs, the page summarizer and its LLM libraries, the Play Store review prompt, the marketing data consent flow, the growth conversion and install attribution code, and the partner distribution code are removed, not just switched off. All of these removals are in `Stable-Main` and were built and checked on a phone. Details are in [changes](./fragmentedfox-docs/changes.md), which also lists known gaps such as the missing network audit.

## Branches

There are only two branches. All changes are made on `NonStable--test`, and builds are run from it. `Stable-Main` only moves forward after a `NonStable--test` build succeeds and the app has been checked on a phone.

| Branch | Purpose |
| --- | --- |
| `Stable-Main` | The default branch. The last state that was built and installed on a phone (build 30, commit `fb02ae860a`, Android build run 42873290132). Anything after that commit is documentation only. |
| `NonStable--test` | Where all work happens. It includes everything in `Stable-Main` plus any newer changes. Right now it has code changes that `Stable-Main` lacks. Build 31 (commit `3902e41696`, run 43062300032) compiled them: about 26,000 lines of unused modules, strings and resources deleted, Strict tracking protection by default and the new fox artwork. It has not been checked on a phone yet. Build 34 (commit `bfbcf5db3c`, run 43262005292) compiled everything up to the memory docs: Ecosia in place of Bing, no partner codes in search URLs, no search results telemetry, fewer Nimbus features, the back/forward cache off, decoded images dropped after 15 seconds, autoplay blocked, the tab limit rework, about 30 unused library classes removed and Pocket and the Stories feature removed (see [changes](./fragmentedfox-docs/changes.md) and [memory](./fragmentedfox-docs/memory.md)). On a phone: no lag, Back and Forward keep a typed comment, autoplay is blocked, the homepage and Ecosia work. `Stable-Main` only moves up after a build of the new changes has been checked on a phone. |

## Building

Builds run on GitHub Actions and are started by hand from the Actions tab. See [building](./fragmentedfox-docs/building.md). What the app can and cannot send is in [memory](./fragmentedfox-docs/memory.md), [telemetry](./fragmentedfox-docs/telemetry.md) and [network](./fragmentedfox-docs/network.md). Android builds are signed with a stored release key, described in [signing](./fragmentedfox-docs/signing.md). A quick check (`.github/scripts/check-fork.py`) runs before every Android build and catches code that points at removed things, and prefs or defaults that changed.

## Security

See [SECURITY.md](./SECURITY.md).

## Upstream

Mozilla's source and documentation:

* [Firefox Source Docs](https://firefox-source-docs.mozilla.org/)
* [Mozilla's Firefox repository](https://github.com/mozilla-firefox/firefox)
