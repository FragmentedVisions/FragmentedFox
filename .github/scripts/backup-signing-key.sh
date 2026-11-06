#!/usr/bin/env bash
set -euo pipefail

for name in KEYSTORE_B64 KEYSTORE_PASSWORD KEY_ALIAS EXPECTED_FINGERPRINT OUT_DIR; do
  if [ -z "${!name:-}" ]; then
    echo "Missing $name"
    exit 1
  fi
done
BACKUP_PASSPHRASE="${BACKUP_PASSPHRASE:-}"
if [ "${#BACKUP_PASSPHRASE}" -lt 20 ]; then
  echo "KEYSTORE_BACKUP_PASSPHRASE must be a repository secret of at least 20 characters"
  exit 1
fi

WORK=$(mktemp -d)
chmod 700 "$WORK"
cleanup() { rm -rf "$WORK"; }
trap cleanup EXIT

printf '%s' "$KEYSTORE_B64" | base64 -d > "$WORK/fragmentedfox.p12"
export KS_PASS="$KEYSTORE_PASSWORD"

FINGERPRINT=$(keytool -list -v -keystore "$WORK/fragmentedfox.p12" -storepass:env KS_PASS -alias "$KEY_ALIAS" \
  | grep 'SHA256:' | head -1 | sed 's/.*SHA256: *//' | tr 'a-f' 'A-F')
if [ "$FINGERPRINT" != "$EXPECTED_FINGERPRINT" ]; then
  echo "The key in the secrets does not match the documented certificate. Not making a backup."
  exit 1
fi
echo "Key verified, SHA-256 $FINGERPRINT"

printf 'alias=%s\npassword=%s\n' "$KEY_ALIAS" "$KEYSTORE_PASSWORD" > "$WORK/credentials.txt"
tar -C "$WORK" -cf "$WORK/bundle.tar" fragmentedfox.p12 credentials.txt

(umask 077; printf '%s' "$BACKUP_PASSPHRASE" > "$WORK/pass")
mkdir -p "$OUT_DIR"
OUT="$OUT_DIR/fragmentedfox-signing-key.tar.gpg"
rm -f "$OUT"
gpg --batch --yes --pinentry-mode loopback --passphrase-file "$WORK/pass" \
  --symmetric --cipher-algo AES256 --s2k-mode 3 --s2k-digest-algo SHA512 --s2k-count 65011712 \
  -o "$OUT" "$WORK/bundle.tar"

LISTING=$(gpg --batch --pinentry-mode loopback --passphrase-file "$WORK/pass" -d "$OUT" 2>/dev/null | tar -tf -)
case "$LISTING" in
  *fragmentedfox.p12*credentials.txt*|*credentials.txt*fragmentedfox.p12*) echo "Backup decrypts and contains the key and its credentials" ;;
  *) echo "The backup did not decrypt correctly"; rm -f "$OUT"; exit 1 ;;
esac
ls -l "$OUT"
sha256sum "$OUT"
