dependencies {
    implementation(project(":extensions:shared:library"))
}

extension {
    // v1.0.6: this is now the only extension bundle. The microG runtime
    // (GmsCoreSupportPatch.java) moved here from the legacy "shared-youtube"
    // module inherited from upstream — YouTube is not one of this repo's
    // patchable apps, so a YouTube-named bundle made no sense. All six app
    // patches extend with "extensions/shared.mpe".
    name = "extensions/shared.mpe"
}

android {
    // Unique per extension to avoid install-time package collisions.
    namespace = "app.morphe.extension.shared"

    buildTypes {
        release {
            // 'libj2v8.so' is already included in the patch.
            ndk {
                abiFilters.add("")
            }
        }
    }
}
