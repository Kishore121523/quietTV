#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

# FIRETV_* aliases preserve existing local setups. No device commands run here.
if [[ -n "${FIRETV_JAVA_HOME:-${JAVA_HOME:-}}" ]]; then
  export JAVA_HOME="${FIRETV_JAVA_HOME:-$JAVA_HOME}"
  export PATH="$JAVA_HOME/bin:$PATH"
fi
SDK="${FIRETV_SDK_ROOT:-${ANDROID_SDK_ROOT:-${ANDROID_HOME:-}}}"
if [[ -z "$SDK" ]]; then
  if [[ -d "$HOME/Library/Android/sdk" ]]; then SDK="$HOME/Library/Android/sdk";
  elif [[ -d "$HOME/Android/Sdk" ]]; then SDK="$HOME/Android/Sdk";
  else echo 'Set ANDROID_SDK_ROOT to your Android SDK directory.' >&2; exit 1; fi
fi
BT="$SDK/build-tools/34.0.0"
ANDROID="$SDK/platforms/android-34/android.jar"
for command in java javac jar keytool zip python3; do
  command -v "$command" >/dev/null || { echo "Missing command: $command" >&2; exit 1; }
done
for file in "$BT/aapt2" "$BT/d8" "$BT/zipalign" "$BT/apksigner" "$ANDROID"; do
  [[ -f "$file" ]] || { echo "Missing SDK component: $file. Install platforms;android-34 and build-tools;34.0.0." >&2; exit 1; }
done
# Clearing intermediates prevents removed classes/resources surviving a rebuild.
rm -rf build/classes build/dex build/gen build/res
mkdir -p build/classes build/dex build/gen build/res artifacts .local
cp -R app/res/. build/res/
# Fresh clones use original generated art. Private local photos take precedence.
python3 scripts/generate_wallpapers.py build/res/drawable-nodpi
"$BT/aapt2" compile --dir build/res -o build/resources.zip
"$BT/aapt2" link --auto-add-overlay -o build/base.apk -I "$ANDROID" --manifest app/AndroidManifest.xml -R build/resources.zip --java build/gen --min-sdk-version 26 --target-sdk-version 30
javac -encoding UTF-8 -source 8 -target 8 -classpath "$ANDROID" -d build/classes app/src/local/kishore/quiettv/*.java build/gen/local/kishore/quiettv/R.java
jar cf build/classes.jar -C build/classes .
"$BT/d8" --lib "$ANDROID" --min-api 26 --output build/dex build/classes.jar
cp build/base.apk build/unsigned.apk
(cd build/dex && zip -q ../unsigned.apk classes.dex)
"$BT/zipalign" -f 4 build/unsigned.apk build/aligned.apk
# Development signing only. Keep this identity private and backed up for updates.
if [[ ! -f .local/signing.jks ]]; then
  keytool -genkeypair -keystore .local/signing.jks -storepass android -keypass android -alias quiettv -dname 'CN=Quiet TV Local' -keyalg RSA -keysize 2048 -validity 10000
fi
"$BT/apksigner" sign --ks .local/signing.jks --ks-pass pass:android --out artifacts/quiet-tv.apk build/aligned.apk
"$BT/apksigner" verify artifacts/quiet-tv.apk
echo 'Built artifacts/quiet-tv.apk (locally signed; nothing installed on a device).'
