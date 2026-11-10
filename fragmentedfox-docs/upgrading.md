# Upgrading to a new Mozilla ESR release

FragmentedFox is built on a Mozilla ESR release tag. Upgrading means building the same tree on the new tag.

FragmentedFox splits the tree in two when upgrading:

- **App layer, ours:** `mobile/android/fenix`, `mobile/android/android-components`, the FragmentedFox branding folders, `fragmentedfox-docs` and `.github`. These are copied over from our branch unchanged. Mozilla's changes to them are not merged, so we can rewrite or delete anything here without making upgrades harder.
- **Everything else, Mozilla's:** this comes from the new tag. The few files outside the app layer that we changed (prefs, branding hooks, build configuration, about 20 files) are merged three ways between the old base tag, our branch and the new tag.

The old base tag is recorded in `fragmentedfox-docs/esr-base.txt`.

1. Add Mozilla's repository once:

   ```bash
   git remote add mozilla https://github.com/mozilla-firefox/firefox
   ```

2. List the newest ESR release tags:

   ```bash
   git ls-remote --tags mozilla '*esr*RELEASE'
   ```

3. Fetch the new tag, for example:

   ```bash
   git fetch --depth=1 mozilla tag FIREFOX_153_7_0esr_RELEASE --no-tags
   ```

4. Build the upgrade branch from it:

   ```bash
   python3 .github/scripts/upgrade-esr.py FIREFOX_153_7_0esr_RELEASE --branch upgrade-153-7
   ```

   The script prints how many files it merged and lists any with conflicts. Conflict markers are committed in those files: check out the branch, fix them and commit. It also writes the new tag into `esr-base.txt`. To test the script, run it against the current base tag with `--check`: it must report `tree equals ours: True`.
5. Fix what Mozilla's new engine broke in the app layer. The app is not merged, so GeckoView or android-components API changes show up as build errors. Pref files change between releases too, so check that every pref in `expected-prefs.json` still exists. If Mozilla's new release adds translations for strings we removed, run `python3 .github/scripts/check-fork.py`: it fails on them, and the fix is to delete those entries.
6. Look through Mozilla's changes to the app layer between the two tags (`git diff <old-tag> <new-tag> -- mobile/android/fenix mobile/android/android-components`) and bring over security fixes and other changes worth having by hand.
7. Re-apply the colours with `python3 .github/scripts/make-android-colors.py` if Mozilla changed its colour files (see [changes](./changes.md)), then run `python3 .github/scripts/check-fork.py`. Its telemetry check also fails if Mozilla added a ping that ignores the collection setting; see [telemetry](./telemetry.md) before adding it to the reviewed list. Then run the Android and Linux builds from the new branch and check the result.
8. Prune `mobile/android/fenix/app/lint-baseline.xml` of entries whose file no longer exists, so it does not hide new warnings in moved code.
9. Use the new branch as `NonStable--test`, and keep the old one until the new build is verified. Once a build from it succeeds and the app has been checked on a phone, move `Stable-Main` up to it.

Use only ESR release tags, not the moving `esr153` branch tip. Mozilla publishes security advisories for each release at https://www.mozilla.org/security/advisories/.

When Mozilla moves ESR to a new major version (for example ESR 153 to the next one), expect more build errors in the app layer where GeckoView and android-components changed, and more three-way conflicts in the branding and configuration files.
