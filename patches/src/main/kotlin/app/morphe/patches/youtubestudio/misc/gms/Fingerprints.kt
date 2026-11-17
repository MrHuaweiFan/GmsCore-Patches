package app.morphe.patches.youtubestudio.misc.gms

import app.morphe.patcher.Fingerprint
import app.morphe.patches.youtubestudio.misc.gms.Constants.YOUTUBE_STUDIO_MAIN_ACTIVITY_CLASS_TYPE

/** Hooks MicroG availability handling into Studio's launcher activity. */
internal object HomeActivityOnCreateFingerprint : Fingerprint(
    definingClass = YOUTUBE_STUDIO_MAIN_ACTIVITY_CLASS_TYPE,
    name = "onCreate",
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
)
