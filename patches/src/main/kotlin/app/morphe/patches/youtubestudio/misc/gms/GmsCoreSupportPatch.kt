package app.morphe.patches.youtubestudio.misc.gms

import app.morphe.patches.shared.compat.AppCompatibilities
import app.morphe.patches.shared.misc.gms.gmsCoreSupportPatch
import app.morphe.patches.shared.misc.settings.preference.BasePreferenceScreen
import app.morphe.patches.shared.misc.settings.preference.PreferenceScreenPreference
import app.morphe.patches.youtubestudio.misc.extension.sharedExtensionPatch
import app.morphe.patches.youtubestudio.misc.gms.Constants.MORPHE_YOUTUBE_STUDIO_PACKAGE_NAME
import app.morphe.patches.youtubestudio.misc.gms.Constants.YOUTUBE_STUDIO_PACKAGE_NAME
import app.morphe.patches.youtubestudio.misc.gms.Constants.YOUTUBE_STUDIO_SPOOFED_PACKAGE_SIGNATURE
import app.morphe.patches.youtubestudio.misc.gms.HomeActivityOnCreateFingerprint

/** Adds MicroG account and Google Play Services compatibility to YouTube Studio. */
@Suppress("unused")
val gmsCoreSupportPatch = gmsCoreSupportPatch(
    fromPackageName = YOUTUBE_STUDIO_PACKAGE_NAME,
    toPackageName = MORPHE_YOUTUBE_STUDIO_PACKAGE_NAME,
    rewriteSelfPackageNameStrings = true,
    mainActivityOnCreateFingerprint = HomeActivityOnCreateFingerprint,
    extensionPatch = sharedExtensionPatch,
    gmsCoreSupportResourcePatchFactory = ::gmsCoreSupportResourcePatch,
) {
    compatibleWith(AppCompatibilities.YOUTUBE_STUDIO)
}

private object DummyPreferenceScreen : BasePreferenceScreen() {
    val SCREEN = Screen(
        key = "morphe_settings_youtube_studio_screen_1_misc",
        summaryKey = null,
    )

    override fun commit(screen: PreferenceScreenPreference) = Unit
}

private fun gmsCoreSupportResourcePatch() =
    app.morphe.patches.shared.misc.gms.gmsCoreSupportResourcePatch(
        fromPackageName = YOUTUBE_STUDIO_PACKAGE_NAME,
        toPackageName = MORPHE_YOUTUBE_STUDIO_PACKAGE_NAME,
        spoofedPackageSignature = YOUTUBE_STUDIO_SPOOFED_PACKAGE_SIGNATURE,
        screen = DummyPreferenceScreen.SCREEN,
    )
