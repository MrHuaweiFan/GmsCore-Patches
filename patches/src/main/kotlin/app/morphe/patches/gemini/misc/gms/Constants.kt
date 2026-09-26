package app.morphe.patches.gemini.misc.gms

/**
 * Verified against Gemini 1.0.970490183 (versionCode 338), minSdk 29,
 * targetSdk 37, base APK extracted from the XAPK bundle obtained from APKCombo
 * on 2026-09-26 (the previous release channel build that still declares
 * Android 10 as its minimum; the Sep-8 build 1.0.971139365 raised minSdk to
 * 32 and would not install on the reference EMUI device).
 *
 * How these values were obtained:
 *   - The manifest launchable entry is the *direct* activity
 *     com.google.android.apps.bard.shellapp.BardEntryPointActivity (NOT an
 *     alias). It exists in classes2.dex and directly defines a public final
 *     onCreate(Landroid/os/Bundle;)V, so the HomeActivityOnCreateFingerprint
 *     resolves.
 *   - Signature: APK Signature Scheme v2 and v3, single signer with subject
 *     CN=Android,OU=Android,O=Google Inc.,L=Mountain View,ST=California,C=US
 *     -- NOTE: a THIRD key, issued 2024-01-23 and valid to 2054, distinct from
 *     BOTH the classic 2008 Google-wide certificate (38918a..., Gmail/Drive/
 *     Maps/Google app) and the Google LLC editors certificate (24bb24...).
 *     Do not copy the hash between app families.
 *   - The shared ServiceCheckFingerprint does NOT resolve: the only method in
 *     the APK referencing "Google Play Services not available" is the
 *     constructor of an Exception subclass (Lbym;), which is not an
 *     availability check. The patch therefore passes serviceCheckFingerprint
 *     = null (v1.1.0 engine parameter).
 *   - The shared GooglePlayUtilityFingerprint does NOT resolve either: the
 *     same string triple lives in Lbxs;->d(Landroid/content/Context;I)I, an
 *     INSTANCE method of an R8-minified GooglePlayServicesUtil singleton
 *     (public final, not public static). The Gemini patch overrides
 *     googlePlayUtilityFingerprint with GooglePlayServicesUtilAvailabilityFingerprint
 *     so the engine's returnEarly(0) "GMS is fine" treatment applies to it.
 *   - The app's own package name appears as 3 exact DEX string references
 *     (PackageManager.getPackageInfo self-lookups in the shell activities'
 *     onCreate methods), so rewriteSelfPackageNameStrings is required
 *     (Drive-suite failure mode F4).
 *   - Gemini probes the Google app's exact package name from 7 methods; the
 *     GEMINI_CROSS_APP_RENAMES map retargets them so the patched Gemini finds
 *     the patched (renamed) Google app.
 */
internal object Constants {
    const val GEMINI_PACKAGE_NAME = "com.google.android.apps.bard"
    const val MORPHE_GEMINI_PACKAGE_NAME = "app.morphe.android.apps.bard"

    // Direct launcher activity class. Exists in classes2.dex with a direct
    // public final onCreate(Bundle)V.
    const val GEMINI_MAIN_ACTIVITY_CLASS_TYPE =
        "Lcom/google/android/apps/bard/shellapp/BardEntryPointActivity;"

    // SHA-1 of the original APK's v2/v3 signer certificate (the 2024-issued
    // Google Inc. release key; see header comment).
    // SHA-256 for cross-check: 997c9c5d63e84a5024f308a615c4e4a87773a52073b0e1547a4d3b8b1081efa7
    const val GEMINI_SPOOFED_PACKAGE_SIGNATURE =
        "ec3549d92772531043b2dd2b85cd2469e75730af"
}
