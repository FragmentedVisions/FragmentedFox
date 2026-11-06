# Android signing

Android only installs an update over an installed app if both are signed with the same key. FragmentedFox release builds are signed with a key stored in GitHub repository secrets.

## Secrets

| Secret | Contents |
| --- | --- |
| `ANDROID_KEYSTORE_B64` | The PKCS12 keystore, base64 encoded |
| `ANDROID_KEYSTORE_PASSWORD` | Password for the keystore and the key |
| `ANDROID_KEY_ALIAS` | `fragmentedfox` |

The `Create Android signing key` workflow generated these. It needs a short-lived token stored as `SIGNING_ADMIN_TOKEN`, refuses to run if a key already exists, and the token should be deleted afterwards.

## Certificate fingerprint

SHA-256: `8A:8F:89:E8:D8:E4:CC:08:2C:AE:58:4D:11:D0:3B:44:FC:28:94:CC:C0:AA:86:04:2C:55:FD:A9:C7:61:EE:9F`

An APK signed with the release key shows this fingerprint when checked with `apksigner verify --print-certs`.

## How builds use the key

The Android workflow decodes the keystore to a temporary file and sets `FRAGMENTEDFOX_KEYSTORE`, `FRAGMENTEDFOX_KEYSTORE_PASSWORD` and `FRAGMENTEDFOX_KEY_ALIAS`. The `signingConfigs.debug` block in `mobile/android/fenix/app/build.gradle` reads them. Without the secrets, the build falls back to a temporary debug key.

## Backing up the key

GitHub secrets cannot be read back, so keep an encrypted copy outside GitHub.

1. Add a repository secret `KEYSTORE_BACKUP_PASSPHRASE`: a long random passphrase of at least 20 characters. Also store it in a password manager. Without it the backup cannot be opened.
2. Run the `Check FragmentedFox` workflow with `backup_key` ticked. It checks that the key in the secrets still has the certificate fingerprint above, packs the keystore with its password and alias, encrypts it with GPG (AES-256), checks that it decrypts, and uploads it as the artifact `fragmentedfox-signing-key-backup` kept for one day.
3. Download the artifact straight away. GitHub gives it to you inside a zip, so keep the zip or unzip it and keep `fragmentedfox-signing-key.tar.gpg`. Store it somewhere safe (not in this repository), then delete the run, which removes its artifact too. The repository is public, so anyone logged in to GitHub can download an artifact until it is deleted or expires. It stays useless without the passphrase, which is why the passphrase must be long and random.

To restore, unzip the download if needed, decrypt the file and put the three secrets back:

```bash
gpg --decrypt fragmentedfox-signing-key.tar.gpg | tar -x
base64 -w0 fragmentedfox.p12
```

`credentials.txt` holds the alias and the keystore password. Set `ANDROID_KEYSTORE_B64` to the base64 output, `ANDROID_KEYSTORE_PASSWORD` to the password and `ANDROID_KEY_ALIAS` to the alias. Check a new build with `apksigner verify --print-certs`: the fingerprint must match the one above.

## Warning

GitHub secrets cannot be read back. If they are deleted, the key is lost, and users have to uninstall and reinstall the app to move to a new key. Keep that in mind before deleting them.
