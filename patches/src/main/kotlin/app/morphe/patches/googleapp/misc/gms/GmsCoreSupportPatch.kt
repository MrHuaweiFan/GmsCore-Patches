package app.morphe.patches.googleapp.misc.gms

import app.morphe.patches.shared.compat.AppCompatibilities
import app.morphe.patches.googleapp.misc.gms.Constants.GOOGLE_APP_FORCE_MIN_SDK_VERSION
import app.morphe.patches.googleapp.misc.gms.Constants.GOOGLE_APP_PACKAGE_NAME
import app.morphe.patches.googleapp.misc.gms.Constants.GOOGLE_APP_SPOOFED_PACKAGE_SIGNATURE
import app.morphe.patches.googleapp.misc.gms.Constants.MORPHE_GOOGLE_APP_PACKAGE_NAME
import app.morphe.patches.googleapp.misc.gms.HomeActivityOnCreateFingerprint
import app.morphe.patches.googleapp.misc.extension.sharedExtensionPatch
import app.morphe.patches.shared.misc.gms.DRIVE_SUITE_CONTENT_URI_RENAMES
import app.morphe.patches.shared.misc.gms.GOOGLE_APP_CROSS_APP_RENAMES
import app.morphe.patches.shared.misc.gms.gmsCoreSupportPatch
import app.morphe.patches.shared.misc.settings.preference.BasePreferenceScreen
import app.morphe.patches.shared.misc.settings.preference.PreferenceScreenPreference

/**
 * Google app (Google Search) GmsCore support.
 *
 * Modeled directly on the working Google Photos implementation and the
 * Drive-suite v1.0.4/v1.0.5 lessons (failure modes F4/F5).
 *
 * Status: every constant below is verified against the real APK (see
 * Constants.kt for provenance). Static verification is complete; runtime
 * behavior under microG still requires on-device testing.
 *
 * v1.1.0 design decisions, all field-derived from the pinned DEX census:
 *   - rewriteSelfPackageNameStrings is enabled because the Velvet/GSA code
 *     base probes its own package name as an exact DEX string across 12 of
 *     14 dex files; after the manifest rename none of those comparisons can
 *     ever match unless the DEX constants follow (F4).
 *   - GOOGLE_APP_CROSS_APP_RENAMES is enabled so the patched Google app keeps
 *     resolving its patched siblings: Gemini (8 exact DEX references to the
 *     bard package) and the already-patched Drive family + Gmail (7 respective
 *     11 exact references), plus the app's one self FileProvider authority
 *     that the manifest rename would otherwise desync (F5).
 *   - forceMinSdkVersion = 29 lowers the declared install floor (Android 11
 *     on the v1.1.1 pin; Android 12L on the v1.1.0 beta pin) so the APK
 *     installs on EMUI-class devices; see the resource patch parameter doc
 *     for the trade-off.
 *
 * v1.1.2 addition, field-derived from bug report DBY-W09NM-2026-09-26-18-09-54
 * (patched 17.60.15.ve.arm64, three FATAL "Missing entry point" crashes in the
 * :googleapp process):
 *   - rewriteProcessNameStrings is enabled because Velvet is a MULTIPROCESS app
 *     that builds a per-process Dagger/Hilt component by switching on literal
 *     "com.google.android.googlequicksearchbox:<process>" strings AND their
 *     hashCode int constants (Lgsxb;->gk()). After the package rename the
 *     runtime process name is app.morphe.android.googlequicksearchbox:<process>,
 *     the unpatched switch falls through to the fallback component, and
 *     GoogleAppActivity.onCreate dies with ClassCastException (wwx → ccwz;
 *     receiver path wrw → edkx). The flag rewrites both the strings and the
 *     paired hashCode constants; see the shared engine parameter doc for the
 *     full mechanism. All manifest android:process attributes are RELATIVE
 *     (":googleapp" etc.), so no manifest-side change is needed.
 *
 * v1.1.3 addition, field-derived from the v1.1.2 field report (both apps launch
 * and mostly work; mic dead/crashing, some settings/menu deep-links dead):
 *   - rewriteSelfComponentReferenceStrings is enabled because Velvet resolves
 *     self-launches through FLATTENED INTENT URIS and SLASH-FORM ComponentName
 *     strings that embed the ORIGINAL package name and therefore stop resolving
 *     after the manifest rename (Intent.parseUri keeps the stale package;
 *     ActivityNotFoundException is caught by some call sites and not others —
 *     hence "silently does nothing" and "sometimes crashes"). Field-verified
 *     matches in the 17.60.15 census: the flag-driven assistant/mic launch
 *     intent (Lgtis;->a → component=<old>/OpaActivity + i.requested_mic_state=3),
 *     the assistant settings deep-link
 *     (Latra;->a / Lekxw;->a → package=<old>, assistant_settings_feature=...
 *     — the settings screens the Gemini/Assistant UI's overflow menu items
 *     open), the GsaVoiceInteractionService component (Lenhg;/Lenho;) and the
 *     GsaNotificationListenerService component (Lafjw;). See the engine
 *     parameter doc for the verified-safe exclusions (resource prefixes,
 *     feature ids, dotted class names, actions, referrer labels).
 */
@Suppress("unused")
val gmsCoreSupportPatch = gmsCoreSupportPatch(
    fromPackageName = GOOGLE_APP_PACKAGE_NAME,
    toPackageName = MORPHE_GOOGLE_APP_PACKAGE_NAME,
    rewriteSelfPackageNameStrings = true,
    rewriteProcessNameStrings = true,
    rewriteSelfComponentReferenceStrings = true,
    crossAppPackageRenames = GOOGLE_APP_CROSS_APP_RENAMES,
    crossAppContentUriRenames = DRIVE_SUITE_CONTENT_URI_RENAMES,
    mainActivityOnCreateFingerprint = HomeActivityOnCreateFingerprint,
    extensionPatch = sharedExtensionPatch,
    gmsCoreSupportResourcePatchFactory = ::gmsCoreSupportResourcePatch,
) {
    compatibleWith(AppCompatibilities.GOOGLE_APP)
}

/**
 * Minimal preference screen used only to satisfy the shared GmsCore support
 * resource patch API. Replace with a real settings screen if/when this repo
 * gets a dedicated Morphe settings UI for the Google app.
 */
private object DummyPreferenceScreen : BasePreferenceScreen() {
    val SCREEN = Screen(
        key = "morphe_settings_googleapp_screen_1_misc",
        summaryKey = null,
    )

    override fun commit(screen: PreferenceScreenPreference) {
        // No-op: no dedicated settings screen for this app yet.
    }
}

private fun gmsCoreSupportResourcePatch() =
    app.morphe.patches.shared.misc.gms.gmsCoreSupportResourcePatch(
        fromPackageName = GOOGLE_APP_PACKAGE_NAME,
        toPackageName = MORPHE_GOOGLE_APP_PACKAGE_NAME,
        spoofedPackageSignature = GOOGLE_APP_SPOOFED_PACKAGE_SIGNATURE,
        screen = DummyPreferenceScreen.SCREEN,
        forceMinSdkVersion = GOOGLE_APP_FORCE_MIN_SDK_VERSION,
    )
