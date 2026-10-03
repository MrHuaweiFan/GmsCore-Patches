package app.morphe.patches.chatgpt.misc.gms

import app.morphe.patches.shared.compat.AppCompatibilities
import app.morphe.patches.chatgpt.misc.gms.Constants.CHATGPT_PACKAGE_NAME
import app.morphe.patches.chatgpt.misc.gms.Constants.CHATGPT_SPOOFED_PACKAGE_SIGNATURE
import app.morphe.patches.chatgpt.misc.gms.Constants.MORPHE_CHATGPT_PACKAGE_NAME
import app.morphe.patches.chatgpt.misc.gms.HomeActivityOnCreateFingerprint
import app.morphe.patches.chatgpt.misc.extension.sharedExtensionPatch
import app.morphe.patches.shared.misc.gms.DRIVE_SUITE_CONTENT_URI_RENAMES
import app.morphe.patches.shared.misc.gms.DRIVE_SUITE_CROSS_APP_RENAMES
import app.morphe.patches.shared.misc.gms.gmsCoreSupportPatch
import app.morphe.patches.shared.misc.settings.preference.BasePreferenceScreen
import app.morphe.patches.shared.misc.settings.preference.PreferenceScreenPreference

/**
 * ChatGPT GmsCore support.
 *
 * The first NON-Google app in this repository. ChatGPT is an app-bundle
 * (AAB) app distributed by Google Play, re-signed by the Play app-signing
 * key, and it bundles real Google dependencies: firebase-messaging (FCM
 * push), play-services-auth, ML Kit, play-services-base (90 gms classes
 * plus 25 firebase classes in the 1.2026.265 census). On a GMS-free
 * device the patch redirects all of them to microG; push delivery is the
 * headline feature this buys.
 *
 * Modeled on the Google Photos implementation and the per-app lessons
 * F3/F4/F9; every constant is verified against the pinned APK (see
 * Constants.kt for provenance). The full census, the three-site
 * self-package analysis and the mode analysis live in guide F11.
 *
 * v1.2.0 design decisions, all field-derived from the 1.2026.265 census:
 *   - DEFAULT identity mode (keepOriginalPackageScopedNames = false). The
 *     app declares 13 package-scoped provider authorities
 *     (androidx-startup, firebaseinitprovider, mlkitinitprovider, stickers,
 *     files, glyphs, persona.provider, plaid, valdi.clipboard, datadog.rum,
 *     SentryNdkPreloadProvider, perfs-startup-listener and
 *     resources.AndroidContextProvider). NONE of them appears as a DEX const-string
 *     or a resource value (verified by full-census and raw resources.arsc
 *     scans), and androidx libraries resolve them from
 *     Context.getPackageName() at runtime -- so the manifest rename must
 *     be the default-mode one (authorities follow the package) or the
 *     androidx-startup initializer resolution desyncs after the rename.
 *   - keepLiteralC2dmIntentActions = true (new v1.2.0 engine parameter).
 *     ChatGPT is the repo's first default-mode app whose Firebase Cloud
 *     Messaging push must actually arrive. The stock default-mode manifest
 *     transform rewrites "com.google.android.c2dm" wholesale, which renames
 *     the FirebaseInstanceIdReceiver / FirebaseMessagingService intent
 *     filter actions to the vendor form; GmsCore's McsService broadcasts
 *     the literal actions only, so no delivery would ever match. The flag
 *     narrows the manifest c2dm rewrite to the permission subtree
 *     (receiver guard + uses-permission still follow the vendor rename,
 *     which GmsCore defines and holds), while the intent actions stay
 *     literal in both the manifest and the DEX (the DEX-side ACTIONS set is
 *     empty by design; the app's four c2dm DEX strings are the three
 *     literal intent actions plus com.google.android.c2dm.permission.SEND,
 *     which the PERMISSIONS set rewrites to the vendor form).
 *   - rewriteSelfPackageNameStrings is FALSE. The exact self-package string
 *     has exactly three call sites (telemetry data class, signature-
 *     integrity gate, finance-account-linking JSON identifier); every one
 *     of them is safer with the ORIGINAL value (guide F11 has the
 *     disassembly). The flag's other two rules (own C2D permission,
 *     content:// self URIs) have zero matches in the census, so false
 *     costs nothing.
 *   - rewriteProcessNameStrings / rewriteSelfComponentReferenceStrings are
 *     FALSE: the app is single-process (no android:process attribute in
 *     the manifest, no "<pkg>:" DEX strings) and contains zero flattened
 *     #Intent; URIs and zero slash-form self ComponentNames (census-
 *     verified), so both passes would be no-ops.
 *   - crossAppPackageRenames = the shared Drive-suite map. The only
 *     sibling-package strings in the census are the Gmail package (two
 *     email-integration sites: an installed-email-clients list and a
 *     domain-to-app mapping; both degrade gracefully when the target is
 *     absent), which the map retargets to the patched Gmail. No other map
 *     key matches the ChatGPT census.
 *   - No forceMinSdkVersion: the pinned build already declares minSdk 29,
 *     the reference device's floor, so the install gate passes unchanged.
 */
@Suppress("unused")
val gmsCoreSupportPatch = gmsCoreSupportPatch(
    fromPackageName = CHATGPT_PACKAGE_NAME,
    toPackageName = MORPHE_CHATGPT_PACKAGE_NAME,
    crossAppPackageRenames = DRIVE_SUITE_CROSS_APP_RENAMES,
    crossAppContentUriRenames = DRIVE_SUITE_CONTENT_URI_RENAMES,
    serviceCheckFingerprint = null,
    mainActivityOnCreateFingerprint = HomeActivityOnCreateFingerprint,
    extensionPatch = sharedExtensionPatch,
    gmsCoreSupportResourcePatchFactory = ::gmsCoreSupportResourcePatch,
) {
    compatibleWith(AppCompatibilities.CHATGPT)
}

/**
 * Minimal preference screen used only to satisfy the shared GmsCore support
 * resource patch API. Replace with a real settings screen if/when this repo
 * gets a dedicated Morphe settings UI for ChatGPT.
 */
private object DummyPreferenceScreen : BasePreferenceScreen() {
    val SCREEN = Screen(
        key = "morphe_settings_chatgpt_screen_1_misc",
        summaryKey = null,
    )

    override fun commit(screen: PreferenceScreenPreference) {
        // No-op: no dedicated settings screen for this app yet.
    }
}

private fun gmsCoreSupportResourcePatch() =
    app.morphe.patches.shared.misc.gms.gmsCoreSupportResourcePatch(
        fromPackageName = CHATGPT_PACKAGE_NAME,
        toPackageName = MORPHE_CHATGPT_PACKAGE_NAME,
        spoofedPackageSignature = CHATGPT_SPOOFED_PACKAGE_SIGNATURE,
        screen = DummyPreferenceScreen.SCREEN,
        keepLiteralC2dmIntentActions = true,
    )
