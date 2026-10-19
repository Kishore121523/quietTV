# Contributing

Start with the README build instructions, then read `docs/CUSTOMIZING.md` and `DESIGN.md`. Keep changes focused and explain the user-visible problem, the resulting behavior, and how you verified it.

## Development

1. Build the existing project with JDK 17 and Android SDK 34 tools.
2. Make a small change. Preserve the six-column dock, D-pad navigation, no-label focus, and reachable Settings/Sleep unless the change explicitly concerns them.
3. Run `./build.sh`. For build or resource changes, also test a clean checkout without private image overrides.
4. If you have a test device, verify the relevant remote interactions. Report device model, Fire OS/Android version, resolution, and whether Home on Fire was involved. Mark device testing as not performed if unavailable.

Do not commit signing keys, device IPs/serials, account details, raw log dumps, APKs, personal screenshots, or wallpaper assets without redistribution permission. Avoid new dependencies for features the framework already supports. Keep image decoding off the UI thread and avoid per-frame allocations.

The repository's CI compiles and verifies a fresh, development-signed APK. It does not test physical remote keys, Home redirection, sleep/wake, app loading, or frame pacing. Those need real-device verification.

Contributions are accepted under the project's MIT license. Include provenance for any asset or third-party code; do not copy GPL helper code into the MIT launcher without addressing licensing explicitly.
