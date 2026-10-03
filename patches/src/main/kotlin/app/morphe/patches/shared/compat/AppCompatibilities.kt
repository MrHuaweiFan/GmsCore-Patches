/*
 * Forked from:
 * https://gitlab.com/ReVanced/revanced-patches/-/blob/main/patches/src/main/kotlin/app/revanced/patches/shared/compat/AppCompatibilities.kt
 *
 * Central Morphe `Compatibility` metadata so Morphe Manager shows human-readable app names.
 */
package app.morphe.patches.shared.compat

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

@Suppress("MemberVisibilityCanBePrivate")
internal object AppCompatibilities {
    // Google apps with GmsCore (microG) support patches.
    //
    // ChatGPT (com.openai.chatgpt) is the first NON-Google app. It is an AAB
    // app re-signed by the Google Play app-signing key and bundles real GMS
    // dependencies (firebase-messaging push, play-services-auth, ML Kit,
    // play-services-base), so the same GmsCore patch model applies to it.
    //
    // Launcher activities, signature hashes and fingerprint targets were verified against
    // the EXACT versions pinned below (see each app's Constants.kt for provenance).
    // The fingerprints rely on obfuscated class names (e.g. Maps' superclass
    // `Lncu;`) that change on every Google release, so patching any other
    // version is expected to FAIL at fingerprint resolution.
    // To support a new version: download its APK, re-verify the values per
    // GMSCORE_GOOGLE_APPS_GUIDE.md, then add/replace the AppTarget here.
    //
    // Field status (2026-09-07, on-device with ReVanced GmsCore):
    //   - Gmail: launch, login and account WORKING (v1.0.3 legacy-identity
    //     fix, user-confirmed). v1.0.6 additionally hardens the
    //     hardware-keyboard shortcut strings whose empty resource values
    //     FATALed every launch on bug report DBY-W09NM-2026-09-07-03-11-49
    //     (see GMSCORE_GOOGLE_APPS_GUIDE.md field test 7).
    //   - Maps: launch + account + routing work (user-confirmed). The
    //     search-bar account avatar chip does not render (open item).
    //   - Drive/Docs/Sheets/Slides: launch + account detection WORKING
    //     (v1.0.4 self-package rewrite, user-confirmed 2026-09-01). v1.0.5
    //     adds a suite-wide cross-app identity rewrite so the four apps can
    //     keep resolving each other after all package names changed (file
    //     open via Drive's DocumentsProvider, editor hand-off, split view).
    //   - Google app / Gemini: added v1.1.0, statically verified against the
    //     pinned APKs (fingerprints, signature hashes, DEX string censuses);
    //     on-device testing under ReVanced GmsCore still pending. The Google
    //     app patch lowers its declared minSdkVersion (32) to 29 via
    //     forceMinSdkVersion so it installs on the EMUI reference device.
    //   - ChatGPT: added v1.2.0, statically verified against the pinned base
    //     APK (fingerprints, signature hash, full DEX census, three-site
    //     self-package analysis); on-device testing under ReVanced GmsCore
    //     pending. Push delivery additionally requires the microG side
    //     (Android Checkin + FCM registration) — see guide F11 for the
    //     honest expectations and known limitations.

    val GMAIL = Compatibility(
        name = "Gmail",
        packageName = "com.google.android.gm",
        appIconColor = 0xEA4335,
        targets = listOf(AppTarget("2026.08.24.971409176.Release")),
    )

    val GOOGLE_DRIVE = Compatibility(
        name = "Google Drive",
        packageName = "com.google.android.apps.docs",
        appIconColor = 0x2684FC,
        targets = listOf(AppTarget("2.26.347.3.all.alldpi")),
    )

    val GOOGLE_MAPS = Compatibility(
        name = "Google Maps",
        packageName = "com.google.android.apps.maps",
        appIconColor = 0x34A853,
        targets = listOf(AppTarget("26.34.04.965633971")),
    )

    val GOOGLE_DOCS = Compatibility(
        name = "Google Docs",
        packageName = "com.google.android.apps.docs.editors.docs",
        appIconColor = 0x4285F4,
        targets = listOf(AppTarget("1.26.341.02.90")),
    )

    val GOOGLE_SHEETS = Compatibility(
        name = "Google Sheets",
        packageName = "com.google.android.apps.docs.editors.sheets",
        appIconColor = 0x0F9D58,
        targets = listOf(AppTarget("1.26.341.01.90")),
    )

    val GOOGLE_SLIDES = Compatibility(
        name = "Google Slides",
        packageName = "com.google.android.apps.docs.editors.slides",
        appIconColor = 0xF4B400,
        targets = listOf(AppTarget("1.26.341.01.90")),
    )

    // Google app (Google Search), added v1.1.0. Repinned in v1.1.1 from the
    // 17.61.20.ve.arm64 beta to this stable release: 17.60.15.ve.arm64 is the
    // newest stable build distributed as a STANDALONE installable APK (nodpi,
    // arm64-v8a + arm-v7a fat build) — verified byte-parity between the
    // APKMirror and APKPure listings (same versionCode 301810370, same file
    // size) on 2026-09-26, so users can download the exact verified release
    // from either mirror. See the googleapp Constants.kt for the full
    // provenance, including the minSdk 30 -> 29 install-floor override this
    // patch applies.
    val GOOGLE_APP = Compatibility(
        name = "Google",
        packageName = "com.google.android.googlequicksearchbox",
        appIconColor = 0x4285F4,
        targets = listOf(AppTarget("17.60.15.ve.arm64")),
    )

    // Gemini, added v1.1.0. Pinned to the previous release-channel build
    // (APKCombo, 2026-09-26) because the Sep-8 build raised minSdk to 32
    // (Android 12L), which the reference EMUI device cannot install; this
    // build declares Android 10 (29). Both versions are also listed on
    // APKMirror, but Gemini is an app-bundle (AAB) app: every variant on
    // every mirror is a base-APK-plus-splits bundle, never a standalone
    // APK — patch the base APK inside (it is what the fingerprints were
    // verified against). See the gemini Constants.kt for the full
    // provenance, including the absent shared fingerprints and their
    // per-app overrides.
    val GEMINI = Compatibility(
        name = "Gemini",
        packageName = "com.google.android.apps.bard",
        appIconColor = 0x9B72CB,
        targets = listOf(AppTarget("1.0.970490183")),
    )

    // ChatGPT, added v1.2.0 — the first non-Google app. Pinned to the
    // ANDROID 10+ variant of 1.2026.265 (the 105 MB XAPK, versionCode
    // 2626527): APKCombo distributes four XAPK variants for this
    // versionName (the other three require Android 12+); this is the only
    // one whose minSdk 29 the reference EMUI device installs without an
    // override. ChatGPT is an app-bundle (AAB) app like Gemini: every
    // mirror ships base-APK-plus-splits bundles, so patch the base APK
    // inside (com.openai.chatgpt.apk — the exact verified base APK is
    // attached to the releases as an asset). The full pin provenance,
    // census and design rationale live in the guide (F11).
    val CHATGPT = Compatibility(
        name = "ChatGPT",
        packageName = "com.openai.chatgpt",
        appIconColor = 0x10A37F,
        targets = listOf(AppTarget("1.2026.265")),
    )
}
