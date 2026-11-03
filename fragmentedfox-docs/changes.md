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
- New tab page (`browser/extensions/newtab/lib/ActivityStream.sys.mjs`): sponsored content, sponsored top sites, top stories, weather and telemetry are off.

Not changed: Safe Browsing local list updates, search suggestions, and the add-on blocklist and certificate data that arrive through Remote Settings.

## Android

- `mobile/android/fenix/app/build.gradle`: application ID `com.fragmentedvisions.fragmentedfox`, `TELEMETRY` and `CRASH_REPORTING` build flags off, Mozilla's shared user ID removed from the release build type, and a signing config that reads the release key from the environment.
- `Settings.kt`: the telemetry and Contile settings default to off.
- `ContentRecommendationsFeatureHelper.kt`: no supported locales, so Pocket recommendations and sponsored stories are disabled.

## Names and branding

- `browser/branding/fragmentedfox` and `mobile/android/branding/fragmentedfox` are the default branding directories. SVG sources for the logo are in `browser/branding/fragmentedfox/source`.
- The application vendor is `FragmentedVisions` (desktop and Android) in `browser/moz.configure` and `mobile/android/moz.configure`.
- Network error pages, the names of browser-owned features (View, Home, Labs, Screenshots, Translations, Suggest), the Windows startup dialog title and the distribution text say FragmentedFox.
- Names of real Mozilla services such as Relay, Monitor, Send and Profiler are left as they are.

## Known gaps

- Many strings, onboarding images and links in the Android app still mention Firefox or Mozilla.
- No update server. The desktop build is configured with the updater off, so new versions are shipped as downloads.
- The default search engines, Sync and add-on sources are Mozilla's.
- `Assets.car` (a compiled macOS asset catalog) still has the old icon.
