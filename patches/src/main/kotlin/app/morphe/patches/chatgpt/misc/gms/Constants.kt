package app.morphe.patches.chatgpt.misc.gms

/**
 * Verified against ChatGPT 1.2026.265 (versionCode 2626527), minSdk 29,
 * targetSdk 37, base APK extracted from the APKCombo XAPK on 2026-10-03.
 *
 * How these values were obtained (see the working environment's analysis
 * scripts; the full census and call-site disassembly are reproduced in
 * GMSCORE_GOOGLE_APPS_GUIDE.md, field note F11):
 *   - The XAPK's manifest.json pins package com.openai.chatgpt,
 *     version_code 2626527, min_sdk 29. Four XAPK variants exist for
 *     1.2026.265 (versionCodes 2626541 / 2626527 x2 / 2626526); this pin
 *     targets the ANDROID 10+ build (105 MB XAPK, the only variant whose
 *     minSdk the reference EMUI device satisfies without any install-floor
 *     override). The base APK inside is the patch target for every variant
 *     of one versionName.
 *   - The manifest launchable entry is the DIRECT activity
 *     com.openai.chatgpt.MainActivity (no alias). It exists in classes.dex
 *     and defines a public onCreate(Landroid/os/Bundle;)V, so the
 *     HomeActivityOnCreateFingerprint resolves.
 *   - Signature: APK Signature Scheme v2 and v3, single signer with subject
 *     CN=Android,OU=Android,O=Google Inc.,L=Mountain View,ST=California,C=US
 *     -- the Google Play app-signing certificate that re-signs every
 *     app-bundle (AAB) release. Distinct from the classic 2008 Google-wide
 *     release key (38918a..., Gmail/Drive/Maps/Google app), the Google LLC
 *     editors key (24bb24...) and Gemini's 2024 key (ec3549...). Do not copy
 *     the hash between app families.
 *   - The shared ServiceCheckFingerprint does NOT resolve: the string
 *     "Google Play Services not available" does not exist anywhere in the
 *     APK's seven dex files. The patch therefore passes
 *     serviceCheckFingerprint = null (the Gemini precedent).
 *   - The shared GooglePlayUtilityFingerprint does NOT resolve either
 *     ("This should never happen." is absent; the one MetadataValueReader
 *     hit, Ld5x;->b, is a datadog metrics class, not an availability gate).
 *     The default is kept anyway because the engine resolves it with
 *     methodOrNull: an unresolved fingerprint is skipped gracefully, and
 *     the app has no availability gate worth neutering (its bundled
 *     play-services-base code returns real values under microG).
 *   - rewriteSelfPackageNameStrings is deliberately FALSE for ChatGPT. The
 *     exact self-package string appears in only 3 of the 415k+ const-strings,
 *     all three disassembled and classified (guide F11): a telemetry/client
 *     data class (server-side androidPackageName identifier), a
 *     signature-integrity gate that short-circuits gracefully when the
 *     package does not equal the original, and a finance-account-linking
 *     JSON identifier whose value the OpenAI backend validates against the
 *     registered app identity. Rewriting would corrupt the last two;
 *     leaving the constants original costs nothing (nothing else in the
 *     app resolves the app by its own package string).
 */
internal object Constants {
    const val CHATGPT_PACKAGE_NAME = "com.openai.chatgpt"
    const val MORPHE_CHATGPT_PACKAGE_NAME = "app.morphe.android.chatgpt"

    // Direct launcher activity class. Exists in classes.dex with a direct
    // public onCreate(Bundle)V.
    const val CHATGPT_MAIN_ACTIVITY_CLASS_TYPE =
        "Lcom/openai/chatgpt/MainActivity;"

    // SHA-1 of the original APK's v2/v3 signer certificate (the Google Play
    // app-signing key; see header comment).
    // SHA-256 for cross-check: b24f4bfbb3cf293f938703b9d87027c1102cc36dc4fa206910e08927db40473c
    const val CHATGPT_SPOOFED_PACKAGE_SIGNATURE =
        "51a2f260766c9c1a83b7dd5b4572040ac23e4aea"
}
