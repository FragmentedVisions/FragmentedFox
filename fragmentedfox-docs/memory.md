# Memory and RAM

How the Android app uses memory and what FragmentedFox changes. Nothing here has been measured on a phone yet; the settings are chosen from reading the code.

## Processes

The app is several Android processes: the main one (Fenix UI and the Gecko parent), `:gpu`, `:socket`, `:rdd`, `:utility`, `:media`, `:gmplugin`, `:crashhelper`, and content processes `:tab0` to `:tab39` (`--num-content-services`, default 40, only declared slots; Android starts one when Gecko needs it). Web pages run in content processes. `dom.ipc.processCount` is 8 (Mozilla's default, unchanged), so pages share up to 8 content processes.

Fission (one process per site) follows the Nimbus `fission.isolationStrategy` value, which is 0 (isolate nothing) in release builds, so it is not a large cost here. The APK is the release variant.

## What happens under memory pressure

- Android calls `FenixApplication.onTrimMemory`. It clears the icon cache (memory cache up to 25 MB) at the background trim level and above.
- GeckoView's `MemoryController` turns trim levels into Gecko's `memory-pressure` notification, which makes the engine drop caches. GeckoView also sends one 10 seconds after a session goes inactive.
- Android 14 and later no longer send several trim levels, so on new phones the engine mostly relies on the 10 second signal and the settings below.
- Fenix used to have a middleware that suspended tabs on low memory. It was never installed and was removed (`TrimMemoryMiddleware`, `SystemAction.LowMemoryAction`).

## What the fork sets

| Setting | Value | Effect |
|---|---|---|
| `BackgroundTabsLimitMiddleware` | on by default | Only the selected tab and the most recent other tab stay active; the rest are suspended and reload when opened. Skips tabs with form data, loading tabs, and (by setting) tabs playing media. |
| `browser.sessionhistory.max_total_viewers` | 0 | Back/forward cache off. Back and Forward reload the page. |
| `image.mem.surfacecache.min_expiration_ms` | 15000 | Off-screen decoded images are freed after about 15 seconds (Mozilla: 60). Scrolling back re-decodes them. |
| `media.autoplay.blocking_policy` | 1 | With the Settings default of blocking all autoplay, a video only starts within 5 seconds of a tap. |

## Not done

A timed memory-pressure signal for a long-running tab, compressing page data over time (the engine has no such feature), and a lower `dom.ipc.processCount` were considered and not made. See [changes](./changes.md) for details of what was changed and what is untested.
