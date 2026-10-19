# Quiet TV

A small, native Android TV launcher that puts your apps and wallpaper first. Built for a Fire TV Stick, with a six-column glass-style dock, remote-friendly navigation, and no recommendation feed or home-screen ads.

## Set it up with Claude or Codex

Give your coding assistant [SETUP_WITH_AI.md](SETUP_WITH_AI.md), or paste:

> Help me set up Quiet TV using https://github.com/Kishore121523/quietTV/blob/main/SETUP_WITH_AI.md. Ask about my device and preferences, then build, install, and customize it within the scope I authorize.

The guide walks the assistant through checking your computer, asking the right questions, building the app, optional Home/Sleep setup, customization, verification, and recovery. An assistant with local tools can perform the steps; a chat-only assistant can guide you through them.

## A personal setup you can make your own

I built Quiet TV for my own Fire TV, remote, favorite apps, and taste. The app order, wallpaper choices, clock time zone, spacing, and animations reflect that personal setup. I’m sharing it as a working starting point so you can build a home screen that suits you.

You can use the preset as it is, change everyday preferences from Settings, or fork the code to change the design and behavior. You do not need the same streaming apps as me: Quiet TV discovers the launchable apps installed on your device.

| Make it yours | Where to change it |
| --- | --- |
| Wallpaper rotation, fixed background, and motion | Quiet TV → Settings |
| Hide an app / restore hidden apps | Hold Select on the app / Settings |
| Which remote button opens your launcher | The optional Home on Fire helper, subject to your device’s supported buttons |
| Your own wallpaper collection | Add images to the wallpaper slots; see the customization guide |
| Favorite-app order, clock time zone, card sizes, spacing, and animation timings | Edit the documented values in `HomeActivity.java`, then rebuild |
| Header controls such as Sleep and Settings, or new actions of your own | Modify `build()` and the controls’ click handlers, then rebuild |
| App name, icon, and package identity | Manifest, drawable resources, and package declarations |

Some changes are settings; others require editing and rebuilding the app. There is no built-in visual layout editor or universal remote-button remapper. The [customization guide](docs/CUSTOMIZING.md) explains where to start and the limits to keep in mind.

Quiet TV is a launcher, not a replacement operating system. It leaves Fire OS and your streaming apps in place. Its visual direction is inspired by tvOS; it is not affiliated with Apple or Amazon.

## What you get

- Rounded app banners with a smooth 20% selection zoom, without labels or a drop shadow.
- A wallpaper-first home view; press Down to reveal the next app row.
- Twenty-two wallpaper slots, with a subtle slide, zoom, and crossfade approximately every 10 seconds.
- A 420 ms card-opening animation and a short handoff fade into the selected app.
- Sleep and Settings buttons, app hiding, and a motion toggle.
- Native Java and Android framework views: no web view, runtime libraries, Internet permission, analytics, or account requirement.

The public build includes original, procedurally generated abstract backgrounds. The photographs and character wallpapers shown in personal demos are **not included**. You can add your own images without changing the code. App logos come from installed apps, rather than bundled logo files.

## Compatibility and limits

Developed and manually tested on a **Fire TV Stick 4K, 2nd generation (AFTKM), Fire OS 8.1.8.2 / Android 11** at 1920 × 1080. The manifest allows Android 8.0/API 26 and newer; other Fire TV and Android TV devices are unverified. This is an early personal project, not a broadly tested launcher distribution. Non-Android Fire TV devices cannot run this APK.

Quiet TV removes advertising from **its own home screen**. It does not remove streaming-app ads, bypass subscriptions, disable Amazon services, or make third-party apps load instantly. App loading screens and system transitions remain outside its control. Performance varies: the wallpaper crossfade is heavier than simple navigation, and a constant 60 fps is not guaranteed.

## Build in a few steps

Use macOS or Linux with Bash, Python 3, `zip`, **JDK 17**, and the Android SDK. Windows users can try WSL with Linux SDK tools; that route has not been tested.

1. Download or clone this repository, then open a terminal in its root.
2. Install JDK 17 and the [Android SDK command-line tools](https://developer.android.com/studio#command-tools), or install the SDK through Android Studio.
3. Set your SDK and Java paths. For example, on macOS:

   ```bash
   export JAVA_HOME="$(/usr/libexec/java_home -v 17)"
   export ANDROID_SDK_ROOT="$HOME/Library/Android/sdk"
   export PATH="$JAVA_HOME/bin:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/platform-tools:$PATH"
   ```

   On Linux, set `JAVA_HOME` to your JDK directory and `ANDROID_SDK_ROOT` to your SDK directory, commonly `$HOME/Android/Sdk`.

4. Install the pinned SDK components, reviewing and accepting the SDK licenses when prompted:

   ```bash
   sdkmanager 'platform-tools' 'platforms;android-34' 'build-tools;34.0.0'
   ./build.sh
   ```

The result is **`artifacts/quiet-tv.apk`**. Building never connects to or changes a TV. No Gradle or Android Studio project import is required; the build uses `aapt2`, `javac`, `d8`, `zipalign`, and `apksigner` directly.

The script accepts `ANDROID_SDK_ROOT` / `ANDROID_HOME`, `JAVA_HOME`, and the legacy aliases `FIRETV_SDK_ROOT` / `FIRETV_JAVA_HOME`. It also checks the usual macOS and Linux SDK locations. It generates missing wallpaper assets into `build/res`, never over your source images.

### Signing and updates

The first build creates a private development signing key at `.local/signing.jks`. Subsequent builds reuse it, so they can update an APK signed with that key. Keep a private backup; do not commit it. The development password is `android`, so this is not a production release-signing setup.

Different contributors and clean CI runs generate different signing identities. Their APKs cannot update each other without uninstalling the previous app, which removes Quiet TV preferences. For a public release, establish a separate stable release key and sign the aligned APK with it; see [releasing](docs/RELEASING.md).

## Install on your own Fire TV

1. Enable **ADB debugging** in the Fire TV's Developer Options. On devices where that menu is hidden, select the device name under About seven times. Menu names vary by Fire OS version.
2. Find the TV's IP address in its network settings. Connect the computer to the same trusted network.
3. Substitute your own address below, then approve the debugging prompt on the TV:

   ```bash
   adb connect YOUR_TV_IP:5555
   adb devices
   adb -s YOUR_TV_IP:5555 install --no-incremental -r artifacts/quiet-tv.apk
   adb -s YOUR_TV_IP:5555 shell am start -n local.kishore.quiettv/.HomeActivity
   ```

Only approve computers you trust. You can turn ADB debugging off after setup; Quiet TV does not need a computer or a persistent ADB connection to run.

### Home button: optional, separate setup

Installing Quiet TV alone does not change Fire OS's Home button. On the tested device, [Home on Fire](https://github.com/toolicious/home-on-fire) is used as a **separately installed** helper. Follow its upstream installation and permission instructions, select Quiet TV as the target, enable its accessibility service, and enable **Replace Home**. In Quiet TV, Settings → Home button setup opens the helper if installed.

Fire OS can show Amazon Home briefly before the helper redirects to Quiet TV. This is not a direct replacement of the firmware's button action. Live TV/app-button mappings can likewise flash the original app first. Disabling Live TV is not a solution for a shortcut that depends on detecting its window. A short Home press while Amazon Home is already focused can also fail to produce a detectable event. These are limitations of the helper/OS route.

The current personal setup uses Home → Amazon Home briefly → Quiet TV, leaves Live TV unmapped, and disables the helper's double-press escape. Helper settings are not bundled or changed by this project. Other Android TV devices may offer a native default-home choice; follow your device's settings instead.

### Sleep: optional accessibility service

Enable **Quiet TV Sleep** in the device's Accessibility settings. It only performs Android's lock-screen/sleep action when you select Sleep. It does not retrieve window content, filter remote keys, or monitor other apps. Without it, the launcher still works and the Sleep button explains that setup is needed.

Sleep is standby, not a power disconnect. A connected HDMI monitor may remain powered. The remote's power button may control a TV by infrared rather than putting the stick to sleep. Quiet TV does not remap that button.

## Controls

| Control | Action |
| --- | --- |
| D-pad | Move between apps and the top controls |
| Select | Open an app or activate a control |
| Hold Select on an app | Open, hide, or access app settings |
| Down from the dock | Show the next row |
| Back in Quiet TV | Return to the first app and dock view; cancel a pending launch |
| Menu / Settings | Background, motion, hidden apps, refresh, helper setup |
| Sleep | Put the device into standby when its accessibility service is enabled |

Hiding an app does not uninstall it. **Show all hidden apps** restores hidden entries. System packages excluded from discovery are a separate source-code list. Install apps using your device's official app store; Quiet TV does not download or install Spotify or any other app.

## Make it yours

Start with [CUSTOMIZING.md](docs/CUSTOMIZING.md). The main implementation is [`HomeActivity.java`](app/src/local/kishore/quiettv/HomeActivity.java); there is no hidden backend or hosted service.

- **Wallpapers:** put your own `wall_00.jpg` through `wall_23.jpg` in `app/res/drawable-nodpi/`, excluding slots `10` and `14`. Use one image per slot. Missing slots get original generated backgrounds at build time. Landscape 1920 × 1080 images work well.
- **Preferred apps:** edit `priorities` in `HomeActivity.java`. Missing apps are skipped. Other launchable user apps are discovered automatically.
- **Spacing and animation:** `build()`, `tile()`, `showRow()`, and `Wallpaper` contain the layout and motion values.
- **Clock:** the personal preset explicitly uses `Asia/Kolkata`. Set another IANA zone or remove `clock.setTimeZone(...)` to use device time.
- **Package identity:** keep it for a personal fork/update, or follow the customization guide to rename it for an independently installed app.

The existing default hides Silk Browser and Amazon Music on first launch; it does not disable or uninstall them. Both can be shown again from Settings. The app preference order and wallpaper names retain the personal preset so the existing installation's UI stays the same. Public fallback images use those same slots; rename the picker entries to match your own collection.

## Project map

```text
app/src/…/HomeActivity.java    Layout, app discovery, focus, launching, wallpapers
app/src/…/SleepService.java   Optional user-triggered sleep action
app/res/                     Theme, icons, transitions, accessibility metadata
app/AndroidManifest.xml      App identity, permissions, Android requirements
scripts/generate_wallpapers.py  Original fallback wallpaper generator
build.sh                     Local build and development signing
.github/workflows/build.yml  Clean-clone build on GitHub Actions
```

Build outputs, signing keys, personal photographs, device logs, screenshots, and session notes are ignored by Git. The repository does not include an automatic debloat script. Changes made to the author's own device are not requirements for using the launcher.

## Troubleshooting and removal

See [TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md) for ADB, signing, app discovery, sleep, and Home-button issues. To remove Quiet TV, first turn off any helper redirect targeting it and disable Quiet TV Sleep, then uninstall it through Settings or:

```bash
adb -s YOUR_TV_IP:5555 uninstall local.kishore.quiettv
```

This removes Quiet TV and its preferences, not streaming-app accounts. This project does not disable the stock launcher.

## Contributing and licensing

Bug reports and small, focused improvements are welcome. Read [CONTRIBUTING.md](CONTRIBUTING.md), [SECURITY.md](SECURITY.md), and [the release guide](docs/RELEASING.md).

Quiet TV's original code, documentation, vector assets, and generated fallback wallpapers are [MIT licensed](LICENSE). Third-party artwork, trademarks, and separately installed helpers are not covered by that grant; see [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md). Home on Fire is a separate GPL-3.0-or-later project, not bundled into this APK.
