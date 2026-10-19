# Troubleshooting

## Build fails

- **Missing SDK components:** install `platforms;android-34` and `build-tools;34.0.0` with SDK Manager. Confirm the environment points at that SDK, not its `platform-tools` subdirectory.
- **Java/tool errors:** use JDK 17, not only a Java runtime. `java`, `javac`, `jar`, and `keytool` must come from that JDK.
- **Missing Python/zip:** install Python 3 and the command-line zip utility. The generator needs no Python packages.
- **Duplicate resources:** use only one image extension per wallpaper resource name.
- **APK output:** `artifacts/quiet-tv.apk` is generated and ignored by Git. Do not commit `build/`.

## ADB cannot connect

Ensure the device runs Android-based Fire OS, ADB debugging is on, and both machines can reach each other. A guest Wi-Fi network may isolate clients. Check the TV's current IP address rather than copying another person's address. Approve the authorization dialog, then run `adb devices`. `unauthorized` means approval is still needed; `offline` may require disconnecting and reconnecting.

Do not expose ADB to the public Internet. No device connection is required after installation.

## INSTALL_FAILED_UPDATE_INCOMPATIBLE

The installed app and new APK have different signing keys. Rebuild with the original private key or uninstall the old package before installing the new one. Uninstallation resets Quiet TV preferences. A fresh CI APK normally has a different development key each run and is not a stable update channel.

## Home shows Amazon, flashes, or sometimes does not redirect

Quiet TV itself does not intercept Home. Check the separately installed helper, its accessibility status, its target package, and Replace Home setting. Follow upstream instructions for helper-specific permissions. Avoid enabling multiple competing Home remappers.

On the tested Fire OS the helper reacts after the Amazon screen appears. A flash can remain. The Live TV button uses a similar after-the-window-opens mechanism and is not inherently flash-free. A short Home press on an already-focused Amazon Home can produce no usable event. A long-press gesture may provide a return route depending on the helper's configuration; do not assume it is identical across devices.

This repository does not replace firmware, root the device, disable the Amazon launcher, or promise a seamless Home transition. Keep an alternative way to open Quiet TV from your app library.

## Sleep does nothing

Enable Quiet TV Sleep in Accessibility. If it stopped working after an update, check whether that service is still enabled. This service only performs a user-requested screen-lock action; it does not control the monitor's IR power or cut power to the stick.

## App missing or artwork looks different

Use Settings → Refresh apps after installing an app. Use Show all hidden apps for entries hidden through Quiet TV. OS utilities excluded by the source list do not appear through that action. Apps without a launchable launcher activity cannot appear. Banners are supplied by the installed apps and can differ by region/version. Quiet TV cannot improve the resolution of those source banners.

## Motion is choppy or app opening shows black

Use the motion toggle if needed, or select a static wallpaper. Avoid adding real-time blur, decoding images on the UI thread, or repeatedly clearing system/app caches. Crossfading two full-screen backgrounds costs more GPU work than a static wallpaper. Results from one device are not a 60fps guarantee.

External apps can show their own splash/black loading window after Quiet TV's animation. The launcher cannot remove that or accelerate content fetching. No automatic debloating, background-process limits, or system animation changes are part of setup.

## Remove or recover

Turn off helper redirection targeting Quiet TV before uninstalling it. Disable Quiet TV Sleep if enabled. Open Amazon Home through the helper's escape route or your device's app/settings UI. Uninstall only the packages you intended to remove. Streaming-app data is independent of Quiet TV. Never overwrite the entire enabled-accessibility-services setting with a copied command; doing so can disable other services you rely on.
