# Publishing the repository and releases

## First GitHub push

Create an empty GitHub repository when ready. No remote URL is assumed here. From this project directory:

```bash
git status --short
git add README.md LICENSE THIRD_PARTY_NOTICES.md CONTRIBUTING.md SECURITY.md DESIGN.md .gitignore build.sh app scripts docs .github
git diff --cached --stat
git diff --cached
```

Review the staged files before committing. Do not use `git add -f` on the ignored wallpaper collection, `.local`, `artifacts`, or private recovery script. Verify:

```bash
git ls-files '.local/*' 'artifacts/*' 'build/*' '*.jks' '*.keystore' 'app/res/drawable-nodpi/wall_*.jpg'
```

That should print nothing for this public source distribution. The README links to the optional helper upstream, not to an unpublished repository placeholder. Then commit, add your actual GitHub remote, and push your chosen branch. Enable Actions if you want CI, Issues for bug reports, and private vulnerability reporting under Security if available.

The initial public code includes original generated fallback wallpapers. It intentionally does not include the personal photo collection visible in demos. Make that distinction in the repository description or post when sharing screenshots.

## Build a public release from a clean checkout

Never publish the existing personal `artifacts/quiet-tv.apk`: it may contain private wallpapers. Clone the published source into a separate directory, set up SDK/JDK as in README, and run `./build.sh`. That clean build uses only original generated wallpapers.

CI artifacts are development builds with a newly generated key on each fresh runner. They are useful for build verification, not as a stable upgrade channel. Establish and back up a private release key for distributed releases. Do not commit it or its password. Sign `build/aligned.apk` with Android's `apksigner` using your release key and a private password source, then verify the signature. Refer to [Android app signing](https://developer.android.com/studio/publish/app-signing) for key management.

Use the same release key for future updates. Increase `android:versionCode` monotonically and update `android:versionName` in the manifest. Test upgrades with that key on a test device. A user with a development-signed build must uninstall it before installing an incompatibly signed public build; preferences are lost.

## Release validation

- Build from public source with no ignored/local files present.
- Confirm there are 22 original wallpaper resources, no private assets, and no device identifiers or logs in the release source.
- Verify the APK signature; record its SHA-256 (`shasum -a 256` on macOS, `sha256sum` on Linux).
- Test D-pad navigation, opening/returning from apps, fixed/rotating wallpaper, hide/restore, settings and optional Sleep on the devices you list as supported.
- Check the Home helper separately; disclose the possible flash and platform limitations.
- State tested hardware, known issues, release-key upgrade compatibility, and source tag in the release notes.

Publishing source does not require shipping an APK immediately. A source-only first release with build instructions is a valid starting point.
