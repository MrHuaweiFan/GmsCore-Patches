package app.morphe.patches.youtubestudio.misc.gms

/** Values verified against YouTube Studio 26.45.002 (versionCode 126450027). */
internal object Constants {
    const val YOUTUBE_STUDIO_PACKAGE_NAME = "com.google.android.apps.youtube.creator"
    const val MORPHE_YOUTUBE_STUDIO_PACKAGE_NAME = "app.morphe.android.youtube.creator"

    const val YOUTUBE_STUDIO_MAIN_ACTIVITY_CLASS_TYPE =
        "Lcom/google/android/apps/youtube/creator/main/MainActivity;"

    // Preserve the historical Google app signing identity used by GmsCore for
    // package spoofing. Studio's APK signing lineage selects this certificate
    // on Android 12L and lower; Android 13+ APKs use the rotated signer.
    const val YOUTUBE_STUDIO_SPOOFED_PACKAGE_SIGNATURE =
        "24bb24c05e47e0aefa68a58a766179d9b613a600"
}
