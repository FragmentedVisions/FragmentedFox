# What FragmentedFox changes

Everything here is a default or a name change on top of Mozilla's ESR source. Prefs can still be changed by the user in `about:config` (desktop).

## Desktop

Defaults changed in `browser/app/profile/firefox.js` and `modules/libpref/init/all.js`:

- Data reporting: `datareporting.policy.dataSubmissionEnabled`, `datareporting.healthreport.uploadEnabled`, `datareporting.usage.uploadEnabled`.
- Telemetry pings: archive, shutdown, first shutdown, new profile and BHR pings, SERP categorization, Firefox account pings, and the newtab private ping.
- Crash report sending from tabs: `browser.tabs.crashReporting.sendReport`.
- Studies: `app.normandy.enabled`, `app.shield.optoutstudies.enabled`, `nimbus.rollouts.enabled`, `messaging-system.askForFeedback`, `browser.discovery.enabled`.
- Promos: `browser.vpn_promo.enabled`, `browser.promo.pin.enabled`, `browser.promo.focus.enabled`, `browser.topsites.contile.enabled`.
- Background connections: `network.captive-portal-service.enabled`, `network.connectivity-service.enabled`, `network.prefetch-next`, `browser.safebrowsing.downloads.remote.enabled`.
- HTTPS-Only mode is on (`dom.security.https_only_mode`), and encrypted DNS is on in fallback mode (`network.trr.mode` 2) with Quad9 (`https://dns.quad9.net/dns-query`) as the resolver. If Quad9 is unreachable, DNS falls back to the system resolver.
- WebRTC is off (`media.peerconnection.enabled`), so websites cannot use video calls, voice calls or peer-to-peer connections. Camera capture is also off (`media.navigator.video.enabled`) and, if WebRTC is switched back on, local network addresses stay hidden (`media.peerconnection.ice.default_address_only` and `ice.no_host`). The same four prefs are set for Android in `mobile/android/app/geckoview-prefs.js`. The CI builds also leave WebRTC out of the compile with `--disable-webrtc`. With that flag the engine compile finished (about 87 minutes), and GeckoView's `VideoCaptureTest.kt`, which needs the WebRTC classes, was deleted because it stopped the build.
- New tab page (`browser/extensions/newtab/lib/ActivityStream.sys.mjs`): sponsored content, sponsored top sites, top stories, weather and telemetry are off.

Not changed: Safe Browsing local list updates, search suggestions, and the add-on blocklist and certificate data that arrive through Remote Settings.

## Android

- `mobile/android/fenix/app/build.gradle`: application ID `com.fragmentedvisions.fragmentedfox`, `TELEMETRY` and `CRASH_REPORTING` build flags off, Mozilla's shared user ID removed from the release build type, and a signing config that reads the release key from the environment.
- `Settings.kt`: the telemetry and Contile settings default to off.
- `Settings.kt`: HTTPS-Only mode defaults to on for all tabs, and encrypted DNS defaults to Increased protection (fallback to normal DNS) with Quad9 as the provider. The settings screen shows Quad9 as a custom provider.
- `ContentRecommendationsFeatureHelper.kt`: no supported locales, so Pocket recommendations and sponsored stories are disabled.
- `DefaultExtensionsPrompt.kt`, `HomeFragment.kt`: the first time the home screen is shown, a dialog offers to install uBlock Origin and Privacy Badger from Mozilla's add-ons site (`addons.mozilla.org/firefox/downloads/latest/ublock-origin` and `privacy-badger17`). Nothing is installed unless the user taps Install both, and both are allowed in private tabs. The dialog shows once (`hasSeenDefaultExtensionsPrompt` in `Settings.kt`). Not tested on a device. If either extension is not available for Android, its install fails and is logged.
- `build.gradle`: only the languages in `fragmentedfox.locales` (default `en`) are packaged and offered in the language list. Override with `-Pfragmentedfox.locales=en,de`.
- `nimbus.fml.yaml`: the marketing onboarding card (sharing install data with Mozilla's marketing partners) defaults to off.
- `onboarding.fml.yaml` and `strings.xml`: the first-run Terms of Use card shows the Mozilla Public License and README links and a FragmentedFox privacy line instead of Mozilla's Terms of Use and data-sharing text.
- `static_strings.xml`: the fixed `firefox` and `app_name_firefox` text resources, which about 30 screens use as the browser's name (trackers panel, About, setup checklist, translation dialogs, link sharing, crash screen and others), now say FragmentedFox.
- `strings.xml` (default language only): strings that refer to this browser say FragmentedFox, and the About text says it is a fork built from Mozilla's source. Strings about Mozilla services (Sync, studies, crash reports, Terms of Use) and all other languages are unchanged.
- Third-party SDKs removed from the Android app (branch `remove-sdks`): Adjust marketing attribution, the Google Play install referrer and advertising ID reader (and the Mozilla Return to AMO handler that used the referrer), Firebase push (and with it web push and push-based Send Tab alerts, which were already off without a Firebase config), Google Play Integrity (the page summarizer now authenticates only through a Mozilla account), and Sentry crash reporting. Their Gradle dependencies, manifest entries, build tokens, debug screens and tests are gone. The settings, Glean metrics, the `adjust-attribution` ping and the onboarding code that only those SDKs fed were removed too. The library modules `lib-push-firebase`, `lib-integrity-googleplay` and `lib-crash-sentry` (still used by Focus) stay in the tree but are not compiled into the app, to keep the change small. The UTM install-referrer settings and Nimbus context fields are still present and now always empty.

## Checked and left as is

- Android Firefox Suggest (`fx-suggest` in `mobile/android/fenix/app/nimbus.fml.yaml`) defaults to off for release builds and is only on for developer and nightly channels. No remote experiment endpoint is configured, so nothing can turn it on.
- Android search suggestions and trending searches default to on (`Settings.kt`), so typed text is sent to the search engine as it is typed. Turning them off by default is an open decision.

- Mozilla's IP Protection (VPN) feature is removed from the Android app on the `remove-vpn` branch: the settings entry, menu item, toolbar pill, trust panel section, first-run prompt, snackbars, account sign-in hooks, navigation destinations, the `ip-protection` Nimbus feature and the Fenix dependency on `feature-ipprotection`, with the matching tests. Its strings (in every language) and the `vpn` Glean metric definitions are removed too. Left in place: the shared android-components and GeckoView VPN code, which nothing in the app uses, and entries for the deleted files in the lint and detekt baselines. This was not compiled here, so it needs a successful build before it is merged. `browser.ipProtection.enabled` is also false in `geckoview-prefs.js`.

## Names and branding

- `browser/branding/fragmentedfox` and `mobile/android/branding/fragmentedfox` are the default branding directories. SVG sources for the logo are in `browser/branding/fragmentedfox/source`.
- The application vendor is `FragmentedVisions` (desktop and Android) in `browser/moz.configure` and `mobile/android/moz.configure`.
- Network error pages, the names of browser-owned features (View, Home, Labs, Screenshots, Translations, Suggest), the Windows startup dialog title and the distribution text say FragmentedFox.
- Names of real Mozilla services such as Relay, Monitor, Send and Profiler are left as they are.

## Known gaps

- The Android home screen has a sports widget that fetches tournament data from Mozilla's servers. It has not been reviewed or disabled.
- uBlock Origin and Privacy Badger overlap in what they block. Running both costs some memory and gives little extra protection, so one may be dropped later.

- The first-run Terms page keeps its structure and buttons (accepting still records acceptance and starts the telemetry client, which stays disabled by the build flags), but its text now describes the licence and links to the README. The privacy line says the app is set up not to send diagnostic data to Mozilla. That statement has not been verified on a running build yet.
- Translations in other languages still say Firefox, and some Android strings, images and links still mention Firefox or Mozilla.
- No update server. The desktop build is configured with the updater off, so new versions are shipped as downloads.
- The default search engines, Sync and add-on sources are Mozilla's.
- `Assets.car` (a compiled macOS asset catalog) still has the old icon.
