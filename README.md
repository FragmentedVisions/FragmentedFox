<p align="center">
  <img src="./browser/branding/fragmentedfox/source/icon.svg" alt="FragmentedFox" width="160">
</p>

# FragmentedFox

FragmentedFox is a private, independent fork of the Firefox browser from [Fragmented Visions](https://github.com/FragmentedVisions). It removes telemetry, studies, sponsored content and unneeded background connections, and ships under its own name, icons and signing key.

> FragmentedFox is not affiliated with, endorsed by or sponsored by Mozilla. Firefox and the Firefox logo are trademarks of the Mozilla Foundation. This project is built from Mozilla's open source code, which is available under the [Mozilla Public License 2.0](https://www.mozilla.org/MPL/2.0/).

## Status

Early and experimental. The Android app builds on GitHub Actions into a signed APK that installs and runs on a phone. It has had only light testing, and the newest changes (below) are not built yet.

| Platform | Status |
| --- | --- |
| Android (arm64) | Builds on GitHub Actions into a signed APK (about 47 minutes with a warm compile cache). Installed and run on a phone. |
| Linux desktop | Workflow written, not run yet. |
| Windows, macOS, iOS | Not supported yet. |

## Base version

FragmentedFox follows Mozilla's [Extended Support Release](https://support.mozilla.org/kb/choosing-firefox-update-channel) branch, currently ESR 153 (tag `FIREFOX_153_6_0esr_RELEASE`), so it picks up security fixes without the feature churn of the main development branch. See [upgrading](./fragmentedfox-docs/upgrading.md).

## What is different

- Telemetry, data reporting, studies (Normandy, Nimbus rollouts) and crash report sending are off by default.
- Sponsored content on the new tab page, Pocket recommendations on Android, sponsored tiles and the VPN, pin and Firefox Focus promos are off.
- Captive portal and connectivity checks, link prefetching and remote download checks are off.
- HTTPS-Only mode is on, and DNS is encrypted by default (Quad9, falling back to normal DNS).
- WebRTC is left out of the build and switched off in the prefs.
- On first run the Android app offers to install uBlock Origin and Privacy Badger, and only does so if you accept.
- Its own name, icons, wordmark, vendor (`FragmentedVisions`) and Android application ID (`com.fragmentedvisions.fragmentedfox`).

### Removed from the Android app

Mozilla's VPN, the Adjust, Firebase, Google Play Integrity and Sentry SDKs, the page summarizer and the Play Store review prompt are removed, not just switched off. The VPN removal is built and running. The rest are on the `remove-sdks` branch and are not built or merged yet. Details are in [changes](./fragmentedfox-docs/changes.md), which also lists known gaps such as the missing network audit.

## Branches

| Branch | Purpose |
| --- | --- |
| `esr153-fragmentedfox` | Default branch. |
| `remove-vpn` | The VPN removal, the WebRTC, HTTPS-Only and encrypted DNS defaults, and the build fixes. The last build from it was installed on a phone. Its newest commits (the FragmentedFox name fix and the size analysis) are not built yet. |
| `remove-sdks` | Builds on `remove-vpn` and removes the SDKs, summarizer and review prompt. Not built yet. |

## Building

Builds run on GitHub Actions and are started by hand from the Actions tab. See [building](./fragmentedfox-docs/building.md). Android builds are signed with a stored release key, described in [signing](./fragmentedfox-docs/signing.md).

## Security

See [SECURITY.md](./SECURITY.md).

## Upstream

Mozilla's source and documentation:

* [Firefox Source Docs](https://firefox-source-docs.mozilla.org/)
* [Mozilla's Firefox repository](https://github.com/mozilla-firefox/firefox)
