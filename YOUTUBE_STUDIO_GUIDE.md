# YouTube Studio MicroG support

## Pinned input

| Field | Value |
| :--- | :--- |
| App | YouTube Studio |
| Package | `com.google.android.apps.youtube.creator` |
| Version | `26.45.002` (`126450027`) |
| Min SDK / target SDK | 30 / 37 |
| Launcher | `com.google.android.apps.youtube.creator.main.MainActivity` |
| Input | Base APK from the official-package XAPK downloaded from APKPure on 2026-11-17 |
| XAPK SHA-256 | `BF9456E16EC40A95F880B550AF40910DACDF4851CF09FA377967FFBCCD1FE200` |

The input's signer lineage has two API-specific certificates. `apksigner` reports
SHA-1 `24bb24c05e47e0aefa68a58a766179d9b613a600` for API 24–32 and
`916bdf1c32f7b10e7328680e4e051924f437aa40` for API 33+. The patch declares the
legacy Google signer used by existing GmsCore patches. This choice still needs an
account sign-in check against MicroG-RE on API 33+; switch the configured hash if
the login path rejects the rotated signer.

## Verified patch points

- The launcher class directly defines `onCreate(Landroid/os/Bundle;)V`.
- The APK bundles the Google Play Services utility methods matched by the shared
  Morphe fingerprints (`Google Play Services not available`, `MetadataValueReader`,
  and `com.google.android.gms`).
- The exact original app package string occurs in two DEX string tables, so the
  patch enables self-package string rewriting after changing the app ID.
- The manifest declares three package-scoped authorities and a dynamic receiver
  permission. The shared GmsCore resource patch rewrites those names with the
  patched package ID and adds MicroG package/signature metadata.

## Compatibility and status

This patch is pinned to `26.45.002`. The launcher/GMS fingerprints are version
specific, so another Studio version must be inspected and pinned before support is
claimed. Patch compilation, APK packaging, and applying the patch in Morphe
Manager were verified. The available API 34 AVD is x86_64, while this XAPK only
contains an `armeabi-v7a` native split, so Android rejected installation with
`INSTALL_FAILED_NO_MATCHING_ABIS`. Runtime launch and MicroG-RE account sign-in
still need verification on a compatible ARM device.
