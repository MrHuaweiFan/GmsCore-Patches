package app.morphe.patches.youtubestudio.misc.extension

import app.morphe.patches.shared.misc.extension.ExtensionHook
import app.morphe.patches.shared.misc.extension.sharedExtensionPatch
import app.morphe.patches.youtubestudio.misc.gms.HomeActivityOnCreateFingerprint

/** Provides the YouTube Studio Activity context to the shared MicroG runtime. */
private class HomeActivityInitHook : ExtensionHook(
    fingerprint = HomeActivityOnCreateFingerprint,
    insertIndexResolver = { 0 },
    contextRegisterResolver = { "p0" },
)

private val homeActivityInitHook: ExtensionHook = HomeActivityInitHook()

val sharedExtensionPatch = sharedExtensionPatch(homeActivityInitHook)
