package app.morphe.patches.googleapp.misc.gms

import app.morphe.patcher.Fingerprint
import app.morphe.patches.googleapp.misc.gms.Constants.GOOGLE_APP_MAIN_ACTIVITY_CLASS_TYPE

/**
 * Matches the onCreate(Bundle) method executed when the Google app's launcher
 * activity starts, triggering the GmsCore availability check as early as
 * possible.
 *
 * VERIFIED against Google 17.61.20.ve.arm64: the manifest's launchable entry
 * "com.google.android.googlequicksearchbox.SearchActivity" is an
 * activity-alias; its targetActivity
 * com.google.android.apps.search.googleapp.activity.GoogleAppActivity exists
 * in classes.dex and directly defines a public final
 * onCreate(Landroid/os/Bundle;)V, which is what this fingerprint targets.
 * GoogleAppActivity extends the obfuscated Lcdju; (which also defines an
 * on-Create, but the direct definition wins and keeps the hook on the exact
 * launcher class).
 *
 * Additional shared-fingerprint verification against the same APK:
 *   - ServiceCheckFingerprint       -> Ldaap;->d(Landroid/content/Context;I)V
 *     [public static], classes2.dex -- resolves.
 *   - GooglePlayUtilityFingerprint  -> Ldaap;->b(Landroid/content/Context;I)I
 *     [public static], classes2.dex -- resolves.
 *
 * NOTE: the app's Application class,
 * com.google.android.apps.gsa.binaries.velvet.app.VelvetMultiprocessRoot_Application,
 * runs before any activity and is a known candidate for pre-hook GMS binds
 * (same watch item as Gmail's Hub_Application). Only add extra fingerprints
 * if the app is observed crashing or hanging on a genuine
 * com.google.android.gms call after this patch is applied and GmsCore is
 * installed.
 */
internal object HomeActivityOnCreateFingerprint : Fingerprint(
    definingClass = GOOGLE_APP_MAIN_ACTIVITY_CLASS_TYPE,
    name = "onCreate",
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
)
