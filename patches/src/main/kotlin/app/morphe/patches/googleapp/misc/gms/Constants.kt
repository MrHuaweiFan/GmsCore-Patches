package app.morphe.patches.googleapp.misc.gms

/**
 * Verified against Google (the Google Search app) 17.61.20.ve.arm64
 * (versionCode 301812194), minSdk 32, targetSdk 37, base APK extracted from
 * the arm64-v8a XAPK bundle obtained from APKCombo on 2026-09-26. The base
 * APK itself carries the arm64-v8a native libraries, so the patched base APK
 * is self-contained on ARM devices; the XAPK's extra splits (Lens on-device
 * ML module, xhdpi density config) are not required for patching.
 *
 * How these values were obtained:
 *   - The manifest launchable entry is the *activity-alias*
 *     "com.google.android.googlequicksearchbox.SearchActivity", whose
 *     android:targetActivity is the real class
 *     com.google.android.apps.search.googleapp.activity.GoogleAppActivity.
 *   - GoogleAppActivity exists in classes.dex and directly defines a public
 *     final onCreate(Landroid/os/Bundle;)V (52 instructions), so the
 *     HomeActivityOnCreateFingerprint resolves.
 *   - Signature: APK Signature Scheme v2 and v3, single signer, the classic
 *     Google-wide release certificate (same key as Gmail/Drive/Maps).
 *   - ServiceCheckFingerprint resolves: Ldaap;->d(Landroid/content/Context;I)V
 *     [public static] in classes2.dex.
 *   - GooglePlayUtilityFingerprint resolves: Ldaap;->b(Landroid/content/Context;I)I
 *     [public static] in classes2.dex.
 *   - The app's own package name appears as an EXACT string-table entry in 12
 *     of the 14 dex files (Velvet/GSA self-identity probes), so
 *     rewriteSelfPackageNameStrings is required (Drive-suite failure mode F4).
 *   - Of the 18 package-scoped provider authorities the manifest declares, only
 *     one (contextmenu.utilities.fileprovider) also appears as a bare DEX
 *     string; it is renamed through GOOGLE_APP_CROSS_APP_RENAMES.
 *
 * minSdk note: this build declares android:minSdkVersion="32" (Android 12L).
 * The reference field-test device (Huawei DBY-W09 / MatePad 11, EMUI) runs an
 * older Android base, so the resource patch lowers the install floor to 29 via
 * forceMinSdkVersion. Without it, install fails with INSTALL_FAILED_OLDER_SDK.
 * Any runtime fallout from the lowered floor is a crash-log matter, not an
 * install matter.
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

    // v1.1.0: lowered install floor for EMUI-class devices (see header comment).
    const val GOOGLE_APP_FORCE_MIN_SDK_VERSION = 29
}
