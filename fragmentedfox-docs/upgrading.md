# Upgrading to a new Mozilla ESR release

FragmentedFox is a small set of commits on top of a Mozilla ESR release tag. Upgrading means moving those commits onto the new tag.

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

4. Create a branch from it and cherry-pick the FragmentedFox commits from the current branch:

   ```bash
   git checkout -b upgrade-153-7 FETCH_HEAD
   git cherry-pick <first-fragmentedfox-commit>^..NonStable--test
   ```

   The feature removals (VPN, SDKs, summarizer, review prompt) are commits too and come along with the range. Upstream changes to the files they delete will show up as modify/delete conflicts. Delete the file again and check nothing new references it.
5. Fix any conflicts. Pref files change between releases, so when `firefox.js`, `all.js` or `brandings.ftl` conflict, take Mozilla's version and re-apply each value change by exact match. Check that every changed pref still exists.
6. Run `python3 .github/scripts/check-fork.py` first. After an upgrade it reports any pref or default that Mozilla changed under us, and any code that now points at something that moved. Then run the Android and Linux builds from the new branch and check the result.
7. Use the new branch as `NonStable--test`, and keep the old one until the new build is verified. Once a build from it succeeds and the app has been checked on a phone, move `Stable-Main` up to it.

Use only ESR release tags, not the moving `esr153` branch tip. Mozilla publishes security advisories for each release at https://www.mozilla.org/security/advisories/.

When Mozilla moves ESR to a new major version (for example ESR 153 to the next one), expect larger conflicts in the Android app and the branding directories.
