package app.morphe.patches.chatgpt.misc.extension

import app.morphe.patches.chatgpt.misc.gms.HomeActivityOnCreateFingerprint
import app.morphe.patches.shared.misc.extension.ExtensionHook
import app.morphe.patches.shared.misc.extension.sharedExtensionPatch

/**
 * Passes the Activity context into the extension's GmsCoreSupportPatch as
 * soon as ChatGPT's main Activity is created, mirroring the Gemini and
 * Google Photos extensions.
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
