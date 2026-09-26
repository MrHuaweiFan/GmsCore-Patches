package app.morphe.patches.googleapp.misc.gms

/**
 * Verified against Google (the Google Search app) 17.60.15.ve.arm64
 * (versionCode 301810370), minSdk 30, targetSdk 37. Repinned in v1.1.1 from
 * the 17.61.20.ve.arm64 beta to this stable release, because 17.61.20 is a
 * Play beta track build and ships only inside XAPK/APKM bundles — this
 * 17.60.15 release exists as a STANDALONE installable APK ("arm64-v8a +
 * arm-v7a" fat build, nodpi, 236,766,877 bytes, no splits required), which
 * is what on-device patchers like Morphe Manager can consume directly.
 *
 * Provenance: downloaded from APKPure on 2026-09-26; the identical release
 * (same versionCode 301810370, same listed size 225.80 MB, same "nodpi"
 * variant) is downloadable from APKMirror as a plain .apk — that mirror
 * parity was checked before pinning, so users can fetch the file the patch
 * was verified against from either source. The APK carries both arm64-v8a
 * and armeabi-v7a native libraries itself, so the patched APK is
 * self-contained on all ARM devices.
 *
 * How these values were obtained (all from the real APK, 14 dex files):
 *   - The manifest launchable entry is the *activity-alias*
 *     "com.google.android.googlequicksearchbox.SearchActivity", whose
 *     android:targetActivity is the real class
 *     com.google.android.apps.search.googleapp.activity.GoogleAppActivity.
 *   - GoogleAppActivity exists in classes.dex and directly defines a public
 *     final onCreate(Landroid/os/Bundle;)V, so the
 *     HomeActivityOnCreateFingerprint resolves.
 *   - Signature: APK Signature Scheme v2 and v3, single signer, the classic
 *     Google-wide release certificate (same key as Gmail/Drive/Maps).
 *   - ServiceCheckFingerprint resolves: Lczey;->d(Landroid/content/Context;I)V
 *     [public static] in classes2.dex (same shape as the 17.61.20 build's
 *     Ldaap;->d; the obfuscated class name moved, the shared shape-based
 *     fingerprint still matches).
 *   - GooglePlayUtilityFingerprint resolves: Lczey;->b(Landroid/content/Context;I)I
 *     [public static] in classes2.dex, with the full string suite
 *     ("This should never happen.", "MetadataValueReader",
 *     "com.google.android.gms", "GooglePlayServicesUtil").
 *   - The app's own package name appears as an EXACT string-table entry in 12
 *     of the 14 dex files (Velvet/GSA self-identity probes), so
 *     rewriteSelfPackageNameStrings is required (Drive-suite failure mode F4).
 *   - Cross-app census: 8 exact DEX string entries for the bard package,
 *     7 for com.google.android.apps.docs and 11 for com.google.android.gm —
 *     identical counts to the 17.61.20 build, all covered by
 *     GOOGLE_APP_CROSS_APP_RENAMES.
 *   - Of the 17 package-scoped provider authorities the manifest declares,
 *     only one (com.google.android.googlequicksearchbox.contextmenu.
 *     utilities.fileprovider) also appears as a DEX string (the FULL
 *     authority string in this build — the rename map key matches it
 *     exactly); it is renamed through GOOGLE_APP_CROSS_APP_RENAMES.
 *   - Class-name collision check (F5 procedure): zero collisions for every
 *     rename target across all 157,824 type descriptors in the 14 dex files.
 *
 * minSdk note: this build declares android:minSdkVersion="30" (Android 11).
 * The patch keeps lowering the install floor to 29 via forceMinSdkVersion so
 * the APK also installs on Android-10-based EMUI devices; the gap is one API
 * level, and any runtime fallout would be a crash-log matter, not an install
 * matter.
 */
internal object Constants {
    const val GOOGLE_APP_PACKAGE_NAME = "com.google.android.googlequicksearchbox"
    const val MORPHE_GOOGLE_APP_PACKAGE_NAME = "app.morphe.android.googlequicksearchbox"

    // Real launcher activity class (target of the SearchActivity alias).
    // Exists in classes.dex with a direct public final onCreate(Bundle)V.
    const val GOOGLE_APP_MAIN_ACTIVITY_CLASS_TYPE =
        "Lcom/google/android/apps/search/googleapp/activity/GoogleAppActivity;"

    // SHA-1 of the original APK's v2 signer certificate (Google Inc. classic release key).
    // SHA-256 for cross-check: f0fd6c5b410f25cb25c3b53346c8972fae30f8ee7411df910480ad6b2d60db83
    const val GOOGLE_APP_SPOOFED_PACKAGE_SIGNATURE =
        "38918a453d07199354f8b19af05ec6562ced5788"

    // v1.1.0: lowered install floor for EMUI-class devices; v1.1.1 keeps it
    // (the new stable pin declares 30, one level above the floor).
    const val GOOGLE_APP_FORCE_MIN_SDK_VERSION = 29
}
