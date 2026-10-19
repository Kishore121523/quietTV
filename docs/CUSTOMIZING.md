# Build your own Quiet TV

## Start with the working preset

Build and launch the unmodified project first. A missing preferred streaming app is normal: the grid contains apps actually installed on your device. Six columns are used, and the final tile opens Fire TV settings. Remote navigation and package exclusions were designed around the tested Fire OS device; verify on other platforms before distributing a fork.

## Wallpaper collection

The ordered `wallpaperResources` and `wallpaperNames` arrays in `HomeActivity.java` describe the collection. They must have the same length and order. The 22 resource IDs are `wall_00`–`wall_23`, except `wall_10` and `wall_14`, which were removed by preference.

The build stages `app/res` into `build/res`, then generates an original abstract PNG for every missing wallpaper slot. Add a JPG or PNG of the corresponding name in `app/res/drawable-nodpi/` to override a slot locally. Do not create two extensions with the same resource name. Rebuilding does not change an installed app until you explicitly install the resulting APK.

Photos remain on your machine by default because the wallpaper patterns are gitignored. To distribute images you own or have permission to redistribute, record their sources/license in THIRD_PARTY_NOTICES and deliberately revise the ignore rules. Do not force-add the personal demo wallpaper collection. The source photographs are not covered by the code license.

When adding/removing slots, update the resource array, name array, generator's `SLOTS`, and rotation menu count. `palette` stores 4 for rotation, 0–2 for built-in gradients, and 10 + collection index for a fixed wallpaper. Preserve or migrate existing indexes when changing order. Historical one-time migrations in `onCreate()` protect existing preferences; do not change or remove them casually.

Images are center-cropped to the screen and darkened for legibility. Frames are prepared on one worker thread; current and incoming frames are cached. Avoid decoding images or allocating paints/rectangles inside animation callbacks.

## App order and visibility

`priorities` is an ordered list of package names. Resolve a package on your own device with `adb shell pm list packages`, or inspect its app settings. Keep code queries to LAUNCHER and LEANBACK_LAUNCHER activities. App artwork is loaded from each installed package's banner/icon.

The `excluded` list removes OS utilities and helper apps; `hidden` is a reversible user preference. Missing preferred apps are not rendered as empty placeholders. Silk and Amazon Music are hidden once by the personal migration. To make a different first-run default, modify that migration for a new fork while preserving upgrade behavior for existing installs.

## Customize buttons and actions

There are two separate kinds of buttons:

**On-screen controls:** Sleep and Settings are built in `HomeActivity.build()`. Their `setOnClickListener` handlers define what they do. You can change their text/icon, replace an action, or add another focusable control. Give new controls unique view IDs, meaningful accessibility descriptions, and explicit D-pad links to the app grid. Keep a reachable settings/recovery route. Sleep needs the optional accessibility service; changing its label does not grant new system permissions.

**Physical remote buttons:** Quiet TV handles keys while its own Activity is on screen in `onKeyDown()` and `onBackPressed()`. Changing those handlers does not remap that key globally while another app is open. For a launcher shortcut outside Quiet TV, configure the separate Home on Fire helper for your own remote and target app. Only keys/events exposed by your device can be mapped. Home and branded buttons may open their original screen before redirection; a custom mapping does not guarantee a flash-free transition. The author's Home-button preference is an example, not a requirement for your setup.

For a simple first change, rename an on-screen control and update its accessibility description, rebuild, and check it with the D-pad. Then change its action. For a physical shortcut, test the helper configuration on your device before modifying launcher code.

## Layout and motion

- `build()`: six columns, dock, header, row height, inter-row margin, focus links.
- `tile()`: 5:3 banner shape, 12dp horizontal card inset, 20% focus enlargement, 2dp lift, no labels/shadows, 220ms focus/180ms release.
- `showRow()`: 220ms vertical movement and future-row visibility.
- `launch()`: 420ms expansion; crisp centered rounded logo over a colored surface.
- `Wallpaper.onDraw()`: crossfade and subtle horizontal drift/overscan; 1.4s transition after an 8.6s hold plus decode time.

Keep layout spacing and row stride consistent. Check first/last-column focus, quick reversals, and the first-row-only state after any size change. A source-card animation belongs to Quiet TV only; it cannot animate another app's private content.

## Clock, branding, and package name

The clock deliberately uses `Asia/Kolkata` in the unchanged personal preset. Replace it with your zone or remove the explicit setting to inherit device time. Change the app label in the manifest and original vector icon/banner in `app/res/drawable/` to brand your fork.

For a new package identity, update the manifest package, both Java `package` declarations and their folder, the build script's source/generated R paths, the package filter in `app/res/xml/sleep_service.xml`, and any documentation/install commands. Update helper targets on test devices. A renamed package installs separately and does not inherit the previous app's preferences.

## Validate before installing on a daily-use device

Build and verify the APK first. On a test device check all D-pad directions, quick focus changes, both rows, app opening/return, hide/restore, all wallpaper choices, the motion toggle, and sleep/wake when enabled. Keep the stock home screen available. Never add automatic package disabling, account changes, or ADB operations to application startup or the build script.
