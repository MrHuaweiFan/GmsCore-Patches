# GmsCore Patches

**microG / GmsCore support patches for first-party Google apps — for use with [Morphe](https://morphe.software).**

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-7F52FF?style=flat-square&logo=kotlin)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android)](https://android.com)
[![Apps](https://img.shields.io/badge/Google%20Apps-8%20patched-success?style=flat-square)](#google-apps-patches)

This is a fork of [De-Vanced](https://github.com/RookieEnough/De-Vanced) focused on making various Google apps run against [microG](https://microg.org) instead of Google Play Services.

---

## Status

| App | State |
| :--- | :--- |
| Gmail | ✅ Launch and account working |
| Google Drive | ✅ Launch and account working |
| Google Docs / Sheets / Slides | ✅ Launch and account working; the suite keeps resolving its own apps after the rename |
| Google Maps | ⚠️ Launch and account working, account icon not showing so app settings are innacesible |
| Google app (Search) | ⚠️ Launch and account working, microphone not working |
| Gemini | ⚠️ Launch and account working, chat 3 dots button not working |

Patch support is **pinned to the exact app versions listed below** — other versions fail by
design, because the fingerprints depend on obfuscated class names that change with every
Google release. Version-specific notes, known-issue details, package-name guidance and the
analysis behind every pin live in
[GMSCORE_GOOGLE_APPS_GUIDE.md](GMSCORE_GOOGLE_APPS_GUIDE.md).

## Google apps patches

Each patch applies the same three-part transformation that the Google Photos patch uses:
the app package is renamed (e.g. `com.google.android.gm` → `app.morphe.android.gm`) so it can
be installed next to the Play Store original and binds to microG's GmsCore instead of Google's,
the original Google signing certificate hash is spoofed so signature checks inside the app pass,
and GmsCore vendor lookups are rewired to the microG package.

| App | Package | Pinned version (versionCode) | Signing key |
| :--- | :--- | :--- | :--- |
| Gmail | `com.google.android.gm` | 2026.08.24.971409176.Release (65987503) | Google Inc. classic |
| Google Drive | `com.google.android.apps.docs` | 2.26.347.3.all.alldpi (214624049) | Google Inc. classic |
| Google Maps | `com.google.android.apps.maps` | 26.34.04.965633971 (1068739428) | Google Inc. classic |
| Google Docs | `com.google.android.apps.docs.editors.docs` | 1.26.341.02.90 (220701916) | Google LLC (newer) |
| Google Sheets | `com.google.android.apps.docs.editors.sheets` | 1.26.341.01.90 (220702133) | Google LLC (newer) |
| Google Slides | `com.google.android.apps.docs.editors.slides` | 1.26.341.01.90 (220702177) | Google LLC (newer) |
| Google app | `com.google.android.googlequicksearchbox` | 17.60.15.ve.arm64 (301810370) | Google Inc. classic |
| Gemini | `com.google.android.apps.bard` | 1.0.970490183 (338) | Google Inc. 2024 key (third key) |

Each app's patch set in Morphe Manager consists of **GmsCore support** (the main patch), its
**Extension** dependency (injects the microG compatibility runtime), and the global
**Change package name** helper the engine relies on. Toggling *GmsCore support* pulls in the
other two automatically.

**Note** Gemini is an app-bundle (AAB) app, so mirrors only
offer base-APK-plus-splits bundles. The exact verified base APK
(`com.google.android.apps.bard`, versionCode 338, SHA-256
`a778f01e46d2beec58bba15cc928661deba078ac67069abd35e64729d6d95b26`) is attached to the
releases as an asset:
[Gemini-1.0.970490183-base.apk](https://github.com/MrHuaweiFan/GmsCore-Patches/releases/download/v1.1.2/Gemini-1.0.970490183-base.apk)

## Using the patches

1. Install [ReVanced GmsCore](https://github.com/ReVanced/GmsCore/releases).
2. Add the Google account inside microG and enable
   *Google device registration* in microG settings.
3. In Morphe Manager, add this repository as a patch source and patch one of the APK versions
   listed above; Morphe downloads new patch releases from this repo automatically.
4. If using Drive or Gmail, grant the app its permissions before the first launch, then open the patched app.

## Building

The build environment, release workflow and version re-verification procedures are documented
in [GMSCORE_GOOGLE_APPS_GUIDE.md](GMSCORE_GOOGLE_APPS_GUIDE.md).

## Credits

- **[De-Vanced](https://github.com/RookieEnough/De-Vanced)** — the fork this project builds on,
  and the Google Photos GmsCore patch these six patches are modeled on.
- **[ReVanced](https://github.com/ReVanced/revanced-patches)** — original patches (GPL v3);
  preserved source notices live in [`archive/`](archive/archive_contents.txt).
- **[Morphe](https://morphe.software)** — the patcher framework and ecosystem.

## License

GPL v3 — see [LICENSE](LICENSE). The [NOTICE](NOTICE) file adds conditions from GPLv3 §7: this
project is not affiliated with or endorsed by Morphe or Google, and derivatives must use a
distinct project name. Descriptive references such as "patches for use with Morphe" are fine.
