package app.morphe.patches.chatgpt.misc.gms

import app.morphe.patcher.Fingerprint
import app.morphe.patches.chatgpt.misc.gms.Constants.CHATGPT_MAIN_ACTIVITY_CLASS_TYPE

/**
 * Matches the onCreate(Bundle) method of ChatGPT's direct launcher Activity.
 *
 * VERIFIED against ChatGPT 1.2026.265 (2626527): the manifest's launchable
 * entry com.openai.chatgpt.MainActivity is a direct activity (no alias) that
 * exists in classes.dex and defines a public onCreate(Landroid/os/Bundle;)V.
 * This fingerprint therefore resolves.
 *
 * Shared-fingerprint verification against the same APK produced two ABSENT
 * results (the reason for the patch's fingerprint choices):
 *   - ServiceCheckFingerprint       -> ABSENT. The string "Google Play
 *     Services not available" exists nowhere in the seven dex files, so the
 *     patch passes serviceCheckFingerprint = null; a non-null unresolved
 *     fingerprint would fail the patch.
 *   - GooglePlayUtilityFingerprint  -> ABSENT ("This should never happen."
 *     missing; the lone MetadataValueReader hit is a datadog metrics class,
 *     not an availability gate). The shared default is kept: the engine
 *     resolves it via methodOrNull, so an unresolved fingerprint is skipped
 *     gracefully, and the app has no GMS availability gate that needs
 *     neutering under microG.
 */
internal object HomeActivityOnCreateFingerprint : Fingerprint(
    definingClass = CHATGPT_MAIN_ACTIVITY_CLASS_TYPE,
    name = "onCreate",
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
)
