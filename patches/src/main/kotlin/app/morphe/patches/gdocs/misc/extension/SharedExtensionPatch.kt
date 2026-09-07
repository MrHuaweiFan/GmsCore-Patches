package app.morphe.patches.gdocs.misc.extension

import app.morphe.patches.gdocs.misc.gms.HomeActivityOnCreateFingerprint
import app.morphe.patches.shared.misc.extension.ExtensionHook
import app.morphe.patches.shared.misc.extension.sharedExtensionPatch

/**
 * Passes the Activity context into the extension's GmsCoreSupportPatch as
 * soon as Google Docs's main Activity is created, mirroring Google Photos'
 * app.morphe.patches.googlephotos.misc.extension.SharedExtensionPatch.
 */
private class HomeActivityInitHook : ExtensionHook(
    fingerprint = HomeActivityOnCreateFingerprint,
    insertIndexResolver = { 0 },
    contextRegisterResolver = { "p0" },
)

internal val homeActivityInitHook: ExtensionHook = HomeActivityInitHook()

val sharedExtensionPatch = sharedExtensionPatch(
    homeActivityInitHook,
)
