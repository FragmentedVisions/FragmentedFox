<p align="center">
  <img src="./browser/branding/fragmentedfox/source/icon.svg" alt="FragmentedFox" width="160">
</p>

# FragmentedFox

FragmentedFox is a private, independent fork of the Firefox browser from [Fragmented Visions](https://github.com/FragmentedVisions). It removes telemetry, studies, sponsored content and unneeded background connections, and ships under its own name, icons and signing key.

> FragmentedFox is not affiliated with, endorsed by or sponsored by Mozilla. Firefox and the Firefox logo are trademarks of the Mozilla Foundation. This project is built from Mozilla's open source code, which is available under the [Mozilla Public License 2.0](https://www.mozilla.org/MPL/2.0/).

## Status

Early and experimental. The Android app builds on GitHub Actions into a signed APK that installs and runs on a phone, and has had only light testing. Which build is which, and what is still unbuilt, is on the [status page](./fragmentedfox-docs/status.md).

| Platform | Status |
| --- | --- |
| Android (arm64) | Builds on GitHub Actions into a signed APK (about an hour with a warm compile cache). Installed and checked on a phone. |
| Linux desktop | Workflow written, not run yet. |
| Windows, macOS, iOS | Not supported yet. |

## Base version

FragmentedFox follows Mozilla's [Extended Support Release](https://support.mozilla.org/kb/choosing-firefox-update-channel) branch, currently ESR 153 (tag `FIREFOX_153_6_0esr_RELEASE`), so it picks up security fixes without the feature churn of the main development branch. See [upgrading](./fragmentedfox-docs/upgrading.md).

## What is different

- **No telemetry or remote control.** Telemetry, data reporting, studies and Mozilla's remote experiments are off or compiled out, and crash report sending is off by default.
- **No sponsored content or promos.** Sponsored tiles, Pocket stories, and the VPN, pin and Firefox Focus promos are gone or off.
- **Fewer background connections.** Captive portal and connectivity checks, link prefetching, look-ahead connections, DNS prefetch and Google Safe Browsing are off. WebRTC is left out of the build.
- **Safer connections.** HTTPS-Only mode is on, DNS is encrypted by default (Quad9, falling back to normal DNS), and TLS 1.2 only uses forward-secret authenticated ciphers.
- **Tracking protection.** Strict by default, with fingerprinting protection in every tab, and tracking parameters stripped from links.
- **Lean tabs.** Only the selected tab and the most recently used other tab keep running.
- **Removed from the Android app, not just switched off:** Mozilla's VPN, the Adjust, Firebase, Play Integrity and Sentry SDKs, the page summarizer, Google Lens, Firefox Labs, the Play Store review prompt, marketing consent and attribution code, and more. See [removed](./fragmentedfox-docs/removed.md).
- **Its own name and look:** name, icons, wordmark, vendor (`FragmentedVisions`) and application ID (`com.fragmentedvisions.fragmentedfox`). On first run the Android app offers to install uBlock Origin and Privacy Badger, and only does so if you accept.

The full list is in [changes](./fragmentedfox-docs/changes.md), with defaults in [defaults](./fragmentedfox-docs/defaults.md) and open problems in [known gaps](./fragmentedfox-docs/known-gaps.md).

## Branches

There are two branches. All changes are made on `NonStable--test`, and builds are run from it. `Stable-Main` is the default branch and only moves forward after a `NonStable--test` build succeeds and the app has been checked on a phone. Current commits and builds are on the [status page](./fragmentedfox-docs/status.md).

## Building

Builds run on GitHub Actions and are started by hand from the Actions tab. See [building](./fragmentedfox-docs/building.md). The full documentation index is [fragmentedfox-docs](./fragmentedfox-docs/README.md). What the app can and cannot send is in [memory](./fragmentedfox-docs/memory.md), [telemetry](./fragmentedfox-docs/telemetry.md) and [network](./fragmentedfox-docs/network.md). Android builds are signed with a stored release key, described in [signing](./fragmentedfox-docs/signing.md). A quick check (`.github/scripts/check-fork.py`) runs before every Android build and catches code that points at removed things, and prefs or defaults that changed.

## Security

See [SECURITY.md](./SECURITY.md).

## Upstream

Mozilla's source and documentation:

* [Firefox Source Docs](https://firefox-source-docs.mozilla.org/)
* [Mozilla's Firefox repository](https://github.com/mozilla-firefox/firefox)
