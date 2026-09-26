package app.morphe.patches.gemini.misc.gms

import app.morphe.patches.shared.compat.AppCompatibilities
import app.morphe.patches.gemini.misc.gms.Constants.GEMINI_PACKAGE_NAME
import app.morphe.patches.gemini.misc.gms.Constants.GEMINI_SPOOFED_PACKAGE_SIGNATURE
import app.morphe.patches.gemini.misc.gms.Constants.MORPHE_GEMINI_PACKAGE_NAME
import app.morphe.patches.gemini.misc.gms.GooglePlayServicesUtilAvailabilityFingerprint
import app.morphe.patches.gemini.misc.gms.HomeActivityOnCreateFingerprint
import app.morphe.patches.gemini.misc.extension.sharedExtensionPatch
import app.morphe.patches.shared.misc.gms.DRIVE_SUITE_CONTENT_URI_RENAMES
import app.morphe.patches.shared.misc.gms.GEMINI_CROSS_APP_RENAMES
import app.morphe.patches.shared.misc.gms.gmsCoreSupportPatch
import app.morphe.patches.shared.misc.settings.preference.BasePreferenceScreen
import app.morphe.patches.shared.misc.settings.preference.PreferenceScreenPreference

/**
 * Gemini GmsCore support.
 *
 * Modeled directly on the working Google Photos implementation and the
 * Drive-suite v1.0.4/v1.0.5 lessons (failure modes F4/F5).
 *
 * Status: every constant below is verified against the real APK (see
 * Constants.kt for provenance). Static verification is complete; runtime
 * behavior under microG still requires on-device testing.
 *
 * v1.1.0 design decisions, all field-derived from the pinned DEX census:
 *   - rewriteSelfPackageNameStrings is enabled because the shell activities
 *     resolve their own identity via PackageManager.getPackageInfo against the
 *     hardcoded original package string in their onCreate methods (3 exact DEX
 *     references); after the manifest rename those lookups throw
 *     NameNotFoundException unless the DEX constants follow (F4).
 *   - serviceCheckFingerprint = null / googlePlayUtilityFingerprint override:
 *     Gemini bundles GMS availability code in shapes the shared fingerprints
 *     cannot match (an Exception constructor and an instance-method util
 *     singleton). See Constants.kt and Fingerprints.kt for the full analysis.
 *   - GEMINI_CROSS_APP_RENAMES is enabled so the patched Gemini keeps
 *     resolving its patched siblings: the Google app (7 methods probe its
 *     exact package string for version checks and deep-link routing) and the
 *     already-patched Drive family + Gmail (one exact reference each).
 */
@Suppress("unused")
val gmsCoreSupportPatch = gmsCoreSupportPatch(
    fromPackageName = GEMINI_PACKAGE_NAME,
    toPackageName = MORPHE_GEMINI_PACKAGE_NAME,
    rewriteSelfPackageNameStrings = true,
    crossAppPackageRenames = GEMINI_CROSS_APP_RENAMES,
    crossAppContentUriRenames = DRIVE_SUITE_CONTENT_URI_RENAMES,
    serviceCheckFingerprint = null,
    googlePlayUtilityFingerprint = GooglePlayServicesUtilAvailabilityFingerprint,
    mainActivityOnCreateFingerprint = HomeActivityOnCreateFingerprint,
    extensionPatch = sharedExtensionPatch,
    gmsCoreSupportResourcePatchFactory = ::gmsCoreSupportResourcePatch,
) {
    compatibleWith(AppCompatibilities.GEMINI)
}

/**
 * Minimal preference screen used only to satisfy the shared GmsCore support
 * resource patch API. Replace with a real settings screen if/when this repo
 * gets a dedicated Morphe settings UI for Gemini.
 */
private object DummyPreferenceScreen : BasePreferenceScreen() {
    val SCREEN = Screen(
        key = "morphe_settings_gemini_screen_1_misc",
        summaryKey = null,
    )

    override fun commit(screen: PreferenceScreenPreference) {
        // No-op: no dedicated settings screen for this app yet.
    }
}

private fun gmsCoreSupportResourcePatch() =
    app.morphe.patches.shared.misc.gms.gmsCoreSupportResourcePatch(
        fromPackageName = GEMINI_PACKAGE_NAME,
        toPackageName = MORPHE_GEMINI_PACKAGE_NAME,
        spoofedPackageSignature = GEMINI_SPOOFED_PACKAGE_SIGNATURE,
        screen = DummyPreferenceScreen.SCREEN,
    )
