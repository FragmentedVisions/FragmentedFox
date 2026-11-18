# FragmentedFox documentation

## What the fork is and does

- [changes](./changes.md): map of everything that differs from Mozilla, with links to [defaults](./defaults.md), [removed](./removed.md), [maintenance](./maintenance.md), [branding](./branding.md) and [known gaps](./known-gaps.md)
- [network](./network.md): what the Android app can and cannot contact
- [telemetry](./telemetry.md): how telemetry is compiled out and kept out
- [memory](./memory.md): processes, tab limits and behaviour under memory pressure
- [../PRIVACY.md](../PRIVACY.md): the privacy statement for users

## Working on it

- [status](./status.md): branches, builds and what is unbuilt
- [building](./building.md): workflows, checks and size analysis
- [upgrading](./upgrading.md): moving to a new Mozilla ESR release, with the upgrade log
- [signing](./signing.md): the Android release key
- [licensing](./licensing.md): licences and trademarks

## Guard files used by `.github/scripts/check-fork.py`

`removed.json` (patterns that must not come back), `expected-prefs.json`, `expected-settings.json`, `expected-nimbus.json`, `expected-hosts.json`, `esr-base.txt` and `check-baseline.txt`. Update them whenever a change to a default or a removal is deliberate.
