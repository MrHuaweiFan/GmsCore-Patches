package app.morphe.patches.gemini.misc.gms

import app.morphe.patcher.Fingerprint
import app.morphe.patches.gemini.misc.gms.Constants.GEMINI_MAIN_ACTIVITY_CLASS_TYPE
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Matches the onCreate(Bundle) method of Gemini's direct launcher Activity.
 *
 * VERIFIED against Gemini 1.0.970490183: the manifest's launchable entry
 * com.google.android.apps.bard.shellapp.BardEntryPointActivity is a direct
 * activity (no alias) that exists in classes2.dex and defines a public final
 * onCreate(Landroid/os/Bundle;)V. This fingerprint therefore resolves.
 *
 * Shared-fingerprint verification against the same APK produced two ABSENT
 * results (the reason for the engine's v1.1.0 per-app overrides):
 *   - ServiceCheckFingerprint       -> ABSENT. The string
 *     "Google Play Services not available" exists only in Lbym;-><init>()V,
 *     the constructor of an Exception subclass (the bundled
 *     GooglePlayServicesNotAvailableException stand-in). Early-returning a
 *     constructor would corrupt exception semantics without disabling any
 *     availability gate, so the Gemini patch skips the ServiceCheck hook
 *     entirely (serviceCheckFingerprint = null).
 *   - GooglePlayUtilityFingerprint  -> ABSENT as the shared shape
 *     (public static). The same string triple lives in Lbxs;->d, an INSTANCE
 *     method of the R8-minified GooglePlayServicesUtil singleton, which
 *     [GooglePlayServicesUtilAvailabilityFingerprint] below targets instead.
 */
internal object HomeActivityOnCreateFingerprint : Fingerprint(
    definingClass = GEMINI_MAIN_ACTIVITY_CLASS_TYPE,
    name = "onCreate",
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
)

/**
 * Gemini's bundled GooglePlayServicesUtil availability check.
 *
 * VERIFIED against Gemini 1.0.970490183: Lbxs; is an R8-minified singleton
 * (fields b:I, c:Lbxs;) whose public final d(Landroid/content/Context;I)I
 * (237 instructions, classes2.dex) carries the full GooglePlayServicesUtil
 * string suite -- "This should never happen.", "MetadataValueReader",
 * "com.google.android.gms", "Google Play services out of date for ",
 * " requires Google Play services, but they are missing.", etc. It differs
 * from the shared GooglePlayUtilityFingerprint ONLY in shape: public final
 * INSTANCE method instead of public static, which is exactly why the shared
 * fingerprint cannot resolve and this override exists.
 *
 * The engine applies the same returnEarly(0) treatment as for the shared
 * fingerprint: 0 == CONNECTION_OK, so every availability probe reports
 * "Google Play services available" under microG.
 *
 * Resolved gracefully (methodOrNull): if a future Gemini build moves the
 * util again, the patch still applies and the app surfaces GMS error
 * messages instead -- a visible, debuggable state rather than a failed patch.
 */
internal object GooglePlayServicesUtilAvailabilityFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "I",
    parameters = listOf("Landroid/content/Context;", "I"),
    strings = listOf(
        "This should never happen.",
        "MetadataValueReader",
        "com.google.android.gms",
    ),
)
