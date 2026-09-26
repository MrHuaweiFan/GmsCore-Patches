# GmsCore Patches — agent guide

> **Audience:** this document is the knowledge base for an AI agent (or a new maintainer)
> building, extending or debugging this repository. It contains everything verified about
> the six patched apps, the shared patch engine's design modes, the microG compatibility
> ground truth, the complete field-test failure history, and the exact build/release
> procedures. Human-facing overview: [README.md](README.md).
>
> **Ground rule:** every constant, fingerprint and claim below was extracted from real
> APKs, real microG builds or real device bug reports — nothing is a placeholder. When a
> new situation contradicts this document, trust the new evidence and update the doc.

## Project map

```
patches/src/main/kotlin/app/morphe/
  patches/{gmail,gdrive,gmaps,gdocs,gsheets,gslides,googleapp,gemini}/
      misc/gms/{Constants.kt, Fingerprints.kt, GmsCoreSupportPatch.kt}   per-app patch
      misc/extension/SharedExtensionPatch.kt                              activity hook
  patches/shared/misc/gms/GmsCoreSupportPatch.kt    the engine (bytecode + resource patch)
  patches/shared/misc/gms/Fingerprints.kt           shared fingerprints (ServiceCheck, …)
  patches/shared/compat/AppCompatibilities.kt       version pins + Morphe display metadata
  patches/all/misc/packagename/ChangePackageNamePatch.kt
  patches/shared/misc/extension/…                   extension wiring
extensions/shared/library/…/patches/GmsCoreSupportPatch.java   runtime microG checks (merged into apps)
archive/                                              preserved upstream sources (GPL)
patches-list.json / patches-bundle.json               GENERATED files (CI rewrites them)
```

Eight user-facing patches: Gmail, Drive, Maps, Docs, Sheets, Slides, Google app,
Gemini. Each app patch is
`gmsCoreSupportPatch(...)` (bytecode) plus a `gmsCoreSupportResourcePatch(...)` factory
(resource) plus an extension hook into the launcher activity's `onCreate`.

There is exactly ONE extension bundle: `extensions/shared.mpe`, built from
`extensions/shared/`. It carries the microG runtime (`GmsCoreSupportPatch.java` —
Gservices availability checks, vendor dialog helpers) that is dexed into every patched
app. v1.0.6 consolidated the legacy `shared-youtube` module into it: the module was an
upstream naming artifact (ReVanced shipped its GmsCore runtime in the YouTube extension),
and YouTube is not one of this repo's patchable apps — no patch in this repo targets it
and none ever should (the repo's fingerprints and version pins cover the six Google apps
listed above).

## The engine and its operating modes

The reusable engine is `patches/shared/misc/gms/GmsCoreSupportPatch.kt`. It rewrites
`com.google.android.gms` strings/URIs/permissions to the vendor id (`app.revanced`),
renames the app's own package, injects spoofing meta-data, retargets GMS checks and
installs the runtime microG guard (`extensions/.../GmsCoreSupportPatch.java`, dexed into
every patched app via the extension merge).

Per-app behavior is selected through these parameters:

| Mode | Parameter | Used by | What it does |
| :--- | :--- | :--- | :--- |
| Default | all flags off | Maps | Rename package, authorities, c2dm strings to the vendor; DEX references follow the manifest. |
| Legacy identity | `keepOriginalPackageScopedNames = true` | **Gmail** | Keep every self-package-scoped name original (provider authorities, own C2D_MESSAGE permission), keep c2dm *actions* literal, retarget only sync-adapter account types, strip the receiver permission guard. See failure mode F3. |
| Self-rewrite | `rewriteSelfPackageNameStrings = true` | Drive/Docs/Sheets/Slides, **Google app, Gemini** | Also rewrite the app's own exact package string, self-targeted `content://` URIs and own C2D permission in DEX. See failure mode F4. |
| Cross-app | `crossAppPackageRenames` / `crossAppContentUriRenames` = the shared Drive-suite maps | Drive/Docs/Sheets/Slides | Exact-match renames of sibling suite package names and the Drive storage authorities so the four renamed apps keep resolving each other. Requires default `app.morphe.*` target names. |
| Cross-app (v1.1.0) | `GOOGLE_APP_CROSS_APP_RENAMES` / `GEMINI_CROSS_APP_RENAMES` | **Google app, Gemini** | The Drive-suite maps plus the Google-app↔Gemini pair, so the two renamed apps keep resolving each other (Gemini probes the Google app's version; the Google app hands Assistant traffic to bard). Requires default `app.morphe.*` target names. |
| Fingerprint overrides (v1.1.0) | `serviceCheckFingerprint` / `googlePlayUtilityFingerprint` | **Gemini** (serviceCheck = null, GPU = custom) | Per-app replacement of the two shared GMS-utility hooks for apps whose bundled GMS code has a different shape. See the section “The v1.1.0 additions” below. |
| Install floor (v1.1.0) | `forceMinSdkVersion = 29` | **Google app** | Rewrites the decoded manifest's `android:minSdkVersion` so the APK installs on devices below the declared minimum. Install gate only — not an API backport. |
| Shortcut hardening | `hardenShortcutCharStrings = true` | **Gmail** | Inline the hardware-keyboard shortcut trigger characters into the res menu XML files and re-assert their string values. See failure mode F7. |

**Bind actions stay LITERAL** (`Constants.ACTIONS` is empty, deliberately — see failure
mode F1). Accounts are retargeted by rewriting the account-type string `com.google` →
`app.revanced`, matching the account type the GmsCore build registers.

## Version pins and verified constants

All six patches pin their verified version in `shared/compat/AppCompatibilities.kt`
(`targets = listOf(AppTarget("<version>"))`). Pinning is mandatory, not cosmetic: the
fingerprints depend on obfuscated class names (`Lncu;`, `Labkz;`, …) that change with
essentially every Google release, so patching an unpinned version fails at fingerprint
resolution — confirmed on device.

| App | Verified version (vc) | Launcher manifest entry | REAL fingerprint target class | Signature SHA-1 |
|-----|----------------------|-------------------------|-------------------------------|-----------------|
| Gmail | 2026.08.24.971409176.Release (65987503) | `ConversationListActivityGmail` **(alias)** | `Lcom/google/android/gm/ui/MailActivityGmail;` (direct `onCreate`) | `38918a453d07199354f8b19af05ec6562ced5788` |
| Drive | 2.26.347.3.all.alldpi (214624049) | `NewMainProxyActivity` **(alias)** | `Lcom/google/android/apps/docs/drive/startup/StartupActivity;` (direct `onCreate`) | `38918a453d07199354f8b19af05ec6562ced5788` |
| Maps | 26.34.04.965633971 (1068739428) | `MapsActivity` (direct activity) | `Lncu;` — obfuscated superclass defines `onCreate` | `38918a453d07199354f8b19af05ec6562ced5788` |
| Docs | 1.26.341.02.90 (220701916) | `NewMainProxyActivity` **(alias)** | `Lcom/google/android/apps/docs/editors/homescreen/ProxyLaunchActivity;` (direct `onCreate`) | `24bb24c05e47e0aefa68a58a766179d9b613a600` |
| Sheets | 1.26.341.01.90 (220702133) | `NewMainProxyActivity` **(alias)** | `Lcom/google/android/apps/docs/editors/homescreen/ProxyLaunchActivity;` (direct `onCreate`) | `24bb24c05e47e0aefa68a58a766179d9b613a600` |
| Slides | 1.26.341.01.90 (220702177) | `NewMainProxyActivity` **(alias)** | `Lcom/google/android/apps/docs/editors/homescreen/ProxyLaunchActivity;` (direct `onCreate`) | `24bb24c05e47e0aefa68a58a766179d9b613a600` |
| Google app | 17.60.15.ve.arm64 (301810370) | `SearchActivity` **(alias)** | `Lcom/google/android/apps/search/googleapp/activity/GoogleAppActivity;` (direct `onCreate`) | `38918a453d07199354f8b19af05ec6562ced5788` |
| Gemini | 1.0.970490183 (338) | `BardEntryPointActivity` (direct activity) | `Lcom/google/android/apps/bard/shellapp/BardEntryPointActivity;` (direct `onCreate`) | `ec3549d92772531043b2dd2b85cd2469e75730af` |

Key structural facts (all verified from the APKs):

1. **Six of eight launchers are `activity-alias` entries.** The manifest's launchable
   name usually does not exist as a DEX class — the fingerprint must target the alias's
   `android:targetActivity` (or wherever that class's `onCreate` actually lives).
   Gemini's `BardEntryPointActivity` is one of the two direct launchers (Maps is the
   other, and Maps' `onCreate` lives on a superclass anyway).
2. **Maps needs an obfuscated-superclass hook.** `MapsActivity` is a 1-method shell
   whose `onCreate(Bundle)V` is defined on `Lncu;` — an R8 artifact that WILL change
   between versions; re-verify when bumping.
3. **THREE signing keys are in play.** Gmail/Drive/Maps/Google app use the classic
   Google-wide certificate (`38918a…`, CN=Android, O=Google Inc., valid 2008-2036).
   Docs/Sheets/Slides use the newer Google LLC certificate (`24bb24…`, same key as
   Google Photos). Gemini uses a THIRD key — same subject CN=Android, O=Google Inc.,
   but issued 2024-01-23 and valid to 2054 (`ec3549…`). Never copy a signature hash
   between app families.
4. **The shared engine's fingerprints resolve in seven of the eight APKs.** ServiceCheck
   was found in every APK except Gemini; the GooglePlayUtility string triple is present
   in Gmail/Docs/Sheets/Slides and the Google app, absent in Maps (handled gracefully —
   `methodOrNull`) and absent in the shared SHAPE in Gemini (instance method — handled
   by the v1.1.0 per-app fingerprint override).
5. **Gmail's launcher history**: `ConversationListActivityGmail` has been the public
   launcher name since the 2016 Material rewrite, `MailActivityGmail` its real target
   throughout — but the `onCreate` may move, so re-verify on version bumps.
6. **Gmail's runtime menu/shortcut consumption** (verified in the pinned APK's DEX):
   the options-menu inflater is R8 class `Lgp;` (an inlined androidx SupportMenuInflater
   clone extending `android.view.MenuInflater`, `inflate` kept, parser `b`) and the
   hardware-keyboard shortcut dispatcher is `Lzmq;->aA(...)`. Both read the
   `trigger_*_char` string resources through `charAt(0)` — this is why Gmail (and only
   Gmail so far) needs shortcut hardening; see failure mode F7.

## The v1.1.x additions: Google app and Gemini

Added 2026-09-26 (v1.1.0); the Google app pin was replaced in v1.1.1 the same day.
Both patches are statically verified against their pinned APKs. On-device field
testing under ReVanced GmsCore is still pending — expect the first
logs to surface pre-hook GMS binds in the Application classes
(`VelvetMultiprocessRoot_Application` for the Google app, `Bard_Application` for
Gemini), which would be handled via `primeMethodFingerprint` / `earlyReturnFingerprints`
the same way Gmail's were.

**Google app 17.60.15.ve.arm64 (301810370), minSdk 30 → forced to 29.**

- v1.1.1 repin rationale: 17.61.20.ve.arm64 is a Play **beta-track** build and
  ships only inside XAPK/APKM bundles; on-device patchers that only accept a
  plain .apk could not consume it. 17.60.15.ve.arm64 is the newest STABLE
  release distributed as a standalone installable APK — the "arm64-v8a +
  arm-v7a" fat nodpi build (236,766,877 bytes, carries both ARM ABIs' native
  libraries itself, no splits needed). Byte-parity between the APKMirror and
  APKPure listings was confirmed before pinning (same versionCode 301810370,
  same file size), so users can download the exact verified release from
  either mirror.
- Launcher: `SearchActivity` alias → `GoogleAppActivity`
  (`Lcom/google/android/apps/search/googleapp/activity/GoogleAppActivity;`, direct
  public final `onCreate(Bundle)V`, classes.dex).
- Shared fingerprints both resolve: ServiceCheck `Lczey;->d(Context;I)V` and
  GooglePlayUtility `Lczey;->b(Context;I)I`, both public static in classes2.dex
  (the v1.1.0 beta pin had both on `Ldaap;` — the obfuscated name moves every
  release, the shape-based shared fingerprints match both).
- Self-identity: the exact package string is a string-table entry in 12 of 14 dex
  files → `rewriteSelfPackageNameStrings` (F4 pattern).
- Authorities: 17 package-scoped provider authorities are renamed by the manifest
  transform; of those, exactly ONE also appears as a DEX string —
  `com.google.android.googlequicksearchbox.contextmenu.utilities.fileprovider`
  (a FileProvider.getUriForFile authority argument; the FULL authority string in
  this build, the bare suffix in the 17.61.20 beta) — renamed via
  `GOOGLE_APP_CROSS_APP_RENAMES`. The other 16 are constructed at runtime from
  getPackageName() or resources and follow automatically. Three provider-looking
  strings in the DEX (`NetworkImageLoaderContentProvider`, `CommonContentProvider`,
  `PublicValueProvider`) are NOT declared in the base manifest (split-bound or dead)
  and are deliberately left alone.
- Cross-app: 8 exact DEX references to the bard package (Gemini handoff), 7 to
  `com.google.android.apps.docs` and 11 to `com.google.android.gm` (suite handoffs)
  — all renamed via the shared maps. Identical counts to the 17.61.20 beta.
- Collision check (F5): zero collisions for all rename targets across the
  157,824 type descriptors of the 14 dex files.
- minSdk 30 (Android 11) is above the Android-10 floor the install override
  targets → the resource patch lowers the install floor to 29. This is an
  install gate change only; if the app calls an API that genuinely needs 30+,
  that will surface as a runtime crash to be triaged from logs.

**Gemini 1.0.970490183 (338), minSdk 29 (Android 10).**

- Pinned to the Sep-7 build on purpose: the Sep-8 build (1.0.971139365) raised
  minSdk to 32 and cannot install on the reference device at all.
- Launcher: direct activity `BardEntryPointActivity`
  (`Lcom/google/android/apps/bard/shellapp/BardEntryPointActivity;`, direct public
  final `onCreate(Bundle)V`, classes2.dex).
- Signature: the third Google key (`ec3549…`, subject CN=Android, O=Google Inc.,
  issued 2024, valid to 2054) — NOT one of the two previously known keys.
- **Shared ServiceCheck fingerprint does NOT resolve** (first app where it fails):
  the only method referencing "Google Play Services not available" is
  `Lbym;-><init>()V` — the constructor of an Exception subclass (a
  GooglePlayServicesNotAvailableException stand-in). Early-returning a constructor
  would corrupt exception semantics without disabling any availability gate.
  → the patch passes `serviceCheckFingerprint = null` (v1.1.0 engine parameter).
- **Shared GooglePlayUtility fingerprint does NOT resolve either**: the same string
  triple lives in `Lbxs;->d(Landroid/content/Context;I)I` — a public FINAL INSTANCE
  method of an R8-minified GooglePlayServicesUtil singleton (237 instructions
  carrying the full "Google Play services out of date" / "requires Google Play
  Store" string suite). → the patch overrides `googlePlayUtilityFingerprint` with
  `GooglePlayServicesUtilAvailabilityFingerprint`, and the engine applies the same
  `returnEarly(0)` (CONNECTION_OK) treatment it uses for the shared fingerprint.
- Self-identity: 3 exact DEX references to the app's own package
  (`getPackageInfo` self-lookups in the shell activities' onCreate) →
  `rewriteSelfPackageNameStrings` (F4 pattern).
- Cross-app: 7 methods probe `com.google.android.googlequicksearchbox` exactly
  (version probe + Google-app deep-link routing in the launcher) → renamed via
  `GEMINI_CROSS_APP_RENAMES` so the patched Gemini finds the patched Google app.
  One exact reference each to docs and gm → covered by the reused Drive-suite map.

**Cross-app symmetry note:** the renames are one-directional per app. The Google
app resolves patched Gemini, patched Drive suite and patched Gmail; Gemini
resolves the patched Google app and the suite. What is NOT covered: the six
v1.0.x patches (Gmail, Drive, editors) do not yet rename the Google app's or
Gemini's package names — so a patched Gmail's "open in Google app" style probes
would miss the patched Google app. Adding those directions requires re-censusing
the six pinned APKs first (the F5 procedure); do it only if field testing shows
a concrete miss, not preemptively.

## How to re-verify and bump a version

1. **Launcher activity class**
   ```
   aapt dump badging yourapp.apk | grep launchable-activity
   ```
   Check the manifest: if the launchable name is an `<activity-alias>`, follow
   `android:targetActivity`. Confirm the target class directly defines
   `onCreate(Landroid/os/Bundle;)V` in the DEX (jadx-gui); if not, walk `extends` up
   the chain until the defining class is found and use that in the fingerprint.
   Convert `com.example.Foo` → smali form `Lcom/example/Foo;`.
2. **Signing certificate SHA-1**
   ```
   apksigner verify --print-certs yourapp.apk
   ```
   (or `keytool -printcert -jarfile` for v1-only APKs). Expect `38918a…` for
   Gmail/Drive/Maps and `24bb24…` for the editors.
3. **Whether more than the activity `onCreate` fingerprint is needed**: Google Photos
   needed only `mainActivityOnCreateFingerprint`. Some apps check for Play Services
   earlier (Application.onCreate, boot receivers, background services). Patch with just
   the activity hook first, test against microG, and watch logcat
   (`adb logcat | grep -iE "gms|microg|AndroidRuntime"`) for pre-hook crashes. If one
   appears, add a fingerprint via `primeMethodFingerprint` / `earlyReturnFingerprints`.
   Known candidates to watch: Gmail's `Hub_Application`, the suite's `DriveApplication`
   family, Maps' fused-location services.
4. **Gmail-specific on a version bump**: re-read the `trigger_*_char` values from the
   new APK's `res/values/strings.xml` (they have been stable single characters for
   years, but verify) and update the three tables in
   `shared/misc/gms/GmsCoreSupportPatch.kt` if any changed.
5. Update the per-app `Constants.kt` if anything changed, then add/replace the
   `AppTarget` entry with the new exact `versionName`.

App-specific precedent: Google Photos (upstream) needed one extra patch beyond GmsCore
support — `SelectedAccountPatch`, which stops the app from clearing the signed-in account
after a cold start under microG. Its fingerprint matched obfuscated names found by
decompiling that exact build. Expect to need something similar for some apps: decompile
the current APK, reproduce the symptom under microG, fingerprint the failing method.

## microG compatibility ground truth

| microG build | Use bundle | Why |
| :--- | :--- | :--- |
| ReVanced GmsCore, e.g. v0.3.13.3.250932 (package `app.revanced.android.gms`) | **v1.0.1+** | serves 251 intent actions, ALL literal (`com.google.android.gms.*`), zero renamed; real auth/signin/checkin services; providers + permissions + authenticator renamed |
| Morphe MicroG-RE 6.1.4 (morphe.software) | v1.0.0 | serves the auth family RENAMED plus a literal DummyService fallback |

Facts proven from the released ReVanced GmsCore APK and microG source (do not re-derive):

- A GMS bind is `Intent(action).setPackage("app.revanced.android.gms")` — the package
  must be renamed, the action string must stay literal (see F1).
- Accounts have two roads: **Road A** — the system `AccountManager` binds microG's
  authenticator via `android.accounts.AccountAuthenticator` (a system action, never
  renamed, works on any microG); **Road B** — direct GMS service binds
  (`auth.service.START`, `signin.service.START`, `auth.api.signin.service.START`, the
  `GOOGLE_SIGN_IN` activity, credentials, appcert…). Gmail is the only patched app whose
  startup hard-requires Road B; the other five tolerate Road A only.
- Spoofing works in our favor: microG's `AuthManager` applies
  `app.revanced.android.gms.SPOOFED_PACKAGE_NAME/SIGNATURE` from the calling app's
  meta-data (`PackageUtils.getAndCheckPackage` → `spoofPackageName` +
  `firstSignatureDigest`), so Google receives a byte-perfect genuine-Gmail identity
  (`com.google.android.gm` + `38918a45…`, which is in microG's `GOOGLE_PRIMARY_KEYS`).
- With "Trust Google for app permissions" enabled, tokens issue for any app without a
  per-app consent dialog (`AuthManager.java` 248/282).
- FCM/`cloudmessaging.service.START` is not implemented in the ReVanced build; push
  falls back to the legacy `c2dm.intent.REGISTER` path, which the patch renames and
  microG serves. Gmail falls back to periodic poll sync. Not a startup blocker.
- Actions served only by microG's DummyService (e.g. `common.service.START`,
  `accounts.ACCOUNT_SERVICE`) also resolve literally and fail gracefully (API_DISABLED).

## Failure modes and fixes (field-test history)

Each entry: symptom → root cause → fix → bundle version. Ordered chronologically; all
were reproduced and verified on-device (Huawei DBY-W09, Android 12 / EMUI, no Google
services, ReVanced GmsCore unless noted).

### F1 — Gmail stuck on loading, then crash (v1.0.0)

The engine renamed *every* GMS intent action to `app.revanced.*`, but the microG build
serves the needed services only under their literal `com.google.…` names. Gmail connects
CommonService telemetry and the accounts service **during startup** (verified in the
pinned DEX), so the renamed binds resolved to nothing and the startup chain hung until a
watchdog killed the process. Apps that tolerate a missing GMS connection
appeared fine.

**Fix (v1.0.1):** `Constants.ACTIONS` is now empty — bind actions stay literal. The
v1.0.0-era 223-entry rename list existed to match the Morphe MicroG-RE build, which
serves the auth family renamed — the mirror image of ReVanced GmsCore. That is why the
compatibility matrix above recommends v1.0.0 only for MicroG-RE.

### F2 — onboarding: "Gmail is having trouble with Google Play services" (v1.0.0/1.0.1)

Gmail's GMS error mapper (`Labqd;->b`) maps codes 3, 4, 5, 9, 20 to named string
resources that do not exist in Gmail's resources.arsc, so the fallback dialog
(`common_google_play_services_unknown_issue`) is shown for all of them. The actual code
was SIGN_IN_REQUIRED (4): microG's `SignInService`/`AuthSignInService` return it when
no usable account exists or the app's OAuth use is not permitted.

**Fix: on-device, not in the patch.** Add the Google account inside microG (not inside
the app), enable *Trust Google for app permissions* (`pref_auth_trust_google`), keep the
legacy "Google mail" permission granted in App Info before first launch, enable Google
device registration (Checkin), whitelist microG from battery optimization, then reboot.
Leaving onboarding half-finished poisons the app data — the subsequent launches hang on
loading again; clear data or reinstall after fixing the microG side.

### F3 — provider authority desync: "Failed to find provider com.google.android.gm.sapi" (v1.0.1) / "Unknown uri: content://com.google.android.gm.email.provider/uiaccts" and "Failed to find provider info for app.morphe.android.gm.email.provider" (v1.0.2)

Gmail references its own package-scoped names from DEX constants, res/xml (sync
adapters, notification URIs) and the manifest (provider authorities, including
multi-authority attributes that a prefix rename never touches) at the same time.
Renaming any one side desynchronizes the others.

**Fix (v1.0.3): legacy identity mode** — keep everything scoped by the app's own
original package name in its ORIGINAL form, and patch only what the GmsCore ecosystem
strictly requires: the package attribute, GMS vendor strings, c2dm permissions (not
actions), sync-adapter account types, and the receiver permission guard (GmsCore cannot
hold a signature permission declared by the patched app; without the attribute it
delivers via its documented package-scoped ordered-broadcast fallback). Cost: the
patched app cannot coexist with the genuine app (authority/permission collisions) —
acceptable, the genuine app is what is being replaced. Verified working: launch, login,
account.

### F4 — suite null-identity NPEs: `NullPointerException` at `abah.a`/`akzc.a`/`amvt.a` style null-check idioms, 39 crashes across Drive/Docs/Slides (v1.0.3)

The suite apps resolve their own identity at runtime by comparing
`Context.getPackageName()`/`PackageManager` lookups against hardcoded
`com.google.android.apps.docs…` strings (PackageInfoHelper feature gating,
OpenUrlAliasManager self-matching, editor allowlist validation that throws
`IllegalStateException: Invalid app package: <pkg>`). After the manifest rename none of
these can match, and the startup coroutines crash on the resulting null identity.

**Fix (v1.0.4):** `rewriteSelfPackageNameStrings` — EXACT package match + self
`content://` URIs + the app's own C2D permission only. Never dotted children: many
`<pkg>.xxx` strings are fully-qualified class names that stay valid after the rename.

### F5 — Suite cross-app breakage: editors degrade to "drive" identity, "Unable to open file because it is not a valid document", missing trash/remove actions (v1.0.4)

The Drive family resolves its siblings and its own role through DEX constants:
PackageInfoHelper's `startsWith("com.google.android.apps.docs.editors")`, Drive's
family registry (`Lqfg;`), URI security checks against the Drive storage authorities
(`Lsam;`, `com.google.android.libraries.security.content.c`, PromptBarFragment), the
provider-package prefix check (`Lpih;`), and the SAF open flow rooted at
`content://com.google.android.apps.docs.storage.documents/root` (`Luem;`). After the
manifest rename the Drive app serves the renamed authorities while these constants
stayed original, so every cross-app lookup missed.

**Fix (v1.0.5):** the shared `DRIVE_SUITE_CROSS_APP_RENAMES` /
`DRIVE_SUITE_CONTENT_URI_RENAMES` maps — EXACT whole-string matches only, never dotted
children, verified against the full DEX string census of the four pinned APKs (zero
class-name collisions). Deliberately separate maps: package-name keys are never used as
`content://` prefixes, because the editors' own providers (`…editors.kix|trix…`) are NOT
renamed in their manifests. Requires default `app.morphe.*` target names on all four.

### F6 — the patch bundle itself poisoned every app: Sheets `ExceptionInInitializerError` at `SavedViewportSerializer.<init>` → `ClassCastException: JavaTimeTypeAdapters cannot be cast to com.google.gson.internal.bind.bi` (v1.0.4)

`extensions/shared-youtube/library/build.gradle.kts` (the extension library as it was
named then — the module was consolidated into `extensions/shared/library` in v1.0.6)
declared `implementation(libs.gson)`,
and the patcher dexes `implementation` dependencies of an extension library into every
patched app. No extension source file imports gson — dead weight that shipped the entire
un-obfuscated gson 2.14.0. Google apps ship their own R8-minified gson whose static
initializer reflectively probes
`Class.forName("com.google.gson.internal.bind.JavaTimeTypeAdapters")`; with the injected
copy present the probe SUCCEEDS, the cast to the app's renamed factory interface throws
ClassCastException (not a ReflectiveOperationException — it escapes the catch), kills
the Gson class initializer and everything downstream. Also explained Drive's
server-driven action bar and Maps' missing account avatar without hard crashes.

**Fix (v1.0.5): one line** — remove `implementation(libs.gson)` from the extension
module. **DO NOT re-add runtime gson (or any runtime dependency) to an extension
module** — see the warning comment now living in `extensions/shared/library/build.gradle.kts`.
Patches-side gson is `compileOnly` + `patchListGeneratorClasspath` only (patcher-side,
correct). Existing installs carry the injected classes — every app must be re-patched.

### F7 — Gmail FATAL on every launch: `StringIndexOutOfBoundsException: length=0; index=0` at `String.charAt` ← `gp.b(PG:317)` ← `gp.inflate(PG:38)` ← `oil.eK(PG:137)` ← `oma.onCreateOptionsMenu` (v1.0.5, bug report DBY-W09NM-2026-09-07-03-11-49)

**Analysis** (all steps verified against the bug report and the pinned stock APK):

1. `oma` = `com.android.mail.ui.MailActivity` (the conversation-list activity),
   `oil.eK` = the ActionBar controller that picks the menu resource by view mode,
   `gp` = Gmail's R8-repackaged appcompat-style MenuInflater (extends
   `android.view.MenuInflater`; its parser method `b` inlines the MenuState logic).
2. `gp.b` reads `android:alphabeticShortcut` via `TypedArray.getString` and guards
   `E == null ? 0 : E.charAt(0)` — null-checked but not length-checked (the exact
   upstream androidx/framework `getShortcut` bug).
3. Gmail's menus reference single-character string resources:
   `android:alphabeticShortcut="@string/trigger_*_char"` (55 references across
   res/menu/*.xml; all verified to be shortcut attributes only). Stock values: `n`, `u`,
   `m`, `i`, `/`, `d`, `l`, `r`, `p`, backtick, `s`, comma, `\b` (delete), `\n` (send).
4. In the v1.0.5-patched APK those values resolve to EMPTY strings at runtime
   (`TypedArray.getString` cannot return "" for an absent attribute or an unresolved
   reference — `coerceToString` would produce "@<id>" — so the string table itself
   carried empty values). The same empty values also arm a second crash:
   `Lzmq;->aA` runs `resources.getString(R.string.trigger_..._char).charAt(0)` on every
   hardware key event.
5. Timeline from the bug report: the v1.0.4-patched install (08-31 15:31) never crashed
   in the menu; the v1.0.5 re-patch of the same APK (08-31 23:39) crashes deterministically
   from 09-04 onward (4x on 09-04, 3x on 09-07, minutes before the report was captured).
   The patch code for Gmail did not change between those builds — the corruption is
   introduced in the patch pipeline's resource decode/re-encode/merge round-trip, not in
   this repo's transforms (which touch only the manifest and res/xml for Gmail). The
   on-device patcher is part of Morphe Manager and its build demonstrably changed during
   the window (the package dump shows a Manager update on 09-04 16:09, after the crashes
   began; the Manager version used for the 23:39 patch job is not recorded). Retest after
   every Manager update — a patcher regression can silently re-poison resources that
   previous patch jobs produced correctly.

**Fix (v1.0.6): `hardenShortcutCharStrings`** in the resource patch —
(a) replace every `@string/trigger_*_char` reference in res/menu/*.xml with the verified
literal character, so menu inflation reads inline strings and cannot depend on the
string values; (b) strip the shortcut attribute for the two control-character shortcuts
(`\b`, `\n` — XML 1.0 cannot represent them as literal values); (c) re-assert the 14
values in res/values/strings.xml for the runtime shortcut dispatcher. Both steps are
defensive no-ops if the patcher did not decode that part of the resource tree.

**Known cost:** the delete (backspace) and send (enter) hardware-keyboard shortcuts are
lost in patched builds; all other shortcuts keep their verified characters.

**If a future build still crashes here:** the string re-assertion (b/c) can fail if the
corruption happens after the patch's edits — then the menu inlining still prevents the
menu crash, but hardware-key events could crash in `Lzmq;->aA`. Capture a bug report
with the key event, confirm the same stack, and consider a bytecode guard
(early-return on the dispatcher) as a last resort — it needs a per-version fingerprint
of the obfuscated `Lzmq;` class.

## On-device debugging playbook

1. **microG self-check first** (Self-Check screen: device registration OK, account
   listed). A red item there is never a patch bug.
2. **Which bundle is actually installed**: start patching in Morphe — the fixed builds
   pin the app version (a recommendation appears). No pin = old bundle. Local `.mpp`
   sources never update themselves; remote sources do (this repo:
   `github.com/MrHuaweiFan/GmsCore-Patches` — add it via Sources → + → Remote).
   Releases 1.0.0 existed twice (a pre-fix build was replaced under the same tag) —
   pull-to-refresh or delete + re-add the source to force re-download.
3. **Crash triage**: find the FATAL stack in the bug report's SYSTEM LOG
   (`E AndroidRuntime: FATAL EXCEPTION`), map the obfuscated frames by decompiling the
   pinned APK with jadx (`--single-class <name>` is enough), then decide which engine
   mode or hardening should own the fix. Obfuscated frames print as `(PG:<line>)` —
   R8-synthetic line numbers, not real source lines.
4. **After a bundle update, re-patch every app** — existing installs keep their old
   injected code (F6 proved this the hard way).
5. First launch after patching can legitimately take one to two minutes (flag sync,
   account setup). A spinner beyond ~5 minutes with a warm device means a bind is
   still failing — that is actionable.
6. Grant the app all permissions (especially the legacy "Google mail" one for Gmail)
   BEFORE the first launch; missing it raises an uncaught SecurityException on the
   startup path (expected, not a patch bug).

## Build and release procedure

Prerequisites (agent build environment):

- JDK 21 (Temurin works)
- Android SDK — set `sdk.dir` in `local.properties` at the repo root, or export `ANDROID_HOME`
- A GitHub personal access token with `read:packages` scope (the Morphe Gradle plugin is
  hosted on GitHub Packages, which requires authentication even for public packages)

Put the token in `~/.gradle/gradle.properties` (NOT the repo one — keep it out of git):

```properties
gpr.user=<your GitHub username>
gpr.key=<your token>
```

Build the Android patch bundle:

```bash
./gradlew :patches:buildAndroid
```

> **Repo hygiene — Kotlin block comments nest.** Writing a glob like `res/menu/*.xml`
> inside a `/** … */` KDoc opens a *nested* comment level that silently swallows code
> until a later `*/` closes it. The first v1.0.6 push broke CI exactly this way:
> "Unclosed comment" at EOF in two files plus cascading "Unresolved reference" errors
> everywhere else, because the runaway comment ate the `gmsCoreSupportResourcePatch`
> definition mid-file. Never write a `/*` sequence inside a comment — describe paths as
> "the res/menu XML files" instead. After editing Kotlin, re-read the touched comments
> before pushing; it is much cheaper than a red CI run.

The bundle lands in `patches/build/libs/patches-<version>.mpp`. `patches-list.json` and
`patches-bundle.json` in the repo root are GENERATED — rewritten by the build/CI, and
`patches-bundle.json` is what tells Morphe Manager where to download the bundle from.

Release flow:

1. Push to `main` with a [conventional commit](https://www.conventionalcommits.org)
   message (`feat:`, `fix:`, …) — the Release GitHub Action publishes only when it sees
   one, and it regenerates `patches-list.json` / `patches-bundle.json` / `CHANGELOG.md`.
2. Wait for the Release action to finish and create the tag.
3. On device: refresh the remote source in Morphe (or re-import), confirm the loaded
   bundle reports the new version, re-patch, reinstall.

## Known limitation: Maps can never fully work

This is an ecosystem limit, not a patch bug. The Google Maps app renders its map
through proprietary GMS modules and fetches tiles with device-bound credentials; microG
does not implement those modules, so the map view stays blank regardless of bytecode
patching. What the patch gives Maps is a non-crashing install that coexists with the
Play Store original, plus working account and (mostly) routing. The search-bar avatar
chip is a separate open item (account pipeline, likely the AANG auth client microG does
not implement). Treat Maps as permanently partial or drop it from the bundle.

## Provenance

The APKs used for verification were the current release-channel base APKs from APKCombo
(apkcombo.com), fetched and analyzed automatically on 2026-08-30: Gmail
2026.08.24.971409176.Release, Drive 2.26.347.3.all.alldpi, Maps 26.34.04.965633971,
Docs 1.26.341.02.90, Sheets 1.26.341.01.90, Slides 1.26.341.01.90 (the v1.0.6 shortcut
analysis re-fetched the same pinned Gmail APK on 2026-09-07). The v1.1.0 additions were
fetched and analyzed on 2026-09-26: Google app 17.61.20.ve.arm64 (base APK extracted
from the arm64 XAPK bundle; the x86_64 standalone APK offered the same day is a
different train AND lacks arm64-v8a native libraries, so it is NOT the pin) and Gemini
1.0.970490183 (base APK extracted from its XAPK). The v1.1.1 repin re-verified the
Google app against 17.60.15.ve.arm64 (301810370), the stable standalone APK build
downloaded from APKPure and byte-matched to APKMirror's identical nodpi variant listing
(same versionCode, same file size, same release); the Gemini pin was re-checked
unchanged. All are signed with APK
Signature Scheme v2 (v3 also present on the v1.1.x pair) by a single Google certificate
(three distinct keys as noted above).
Google distributes these apps as split bundles (with the exception of the Google app's
standalone fat-APK builds like this pin); the analysis used the universal/base
APK — the part a patcher consumes and the only part containing the manifest and signing
block. When patching a device-pulled copy, verify it is the same release channel and a
version at or near the ones listed — the obfuscated class names change with essentially
every release.

## License

De-Vanced (and therefore this fork) is GPL-3.0. See `NOTICE` for the attribution and
naming rules that apply if you publish a derivative (distinct project name required;
descriptive references such as "patches for use with Morphe" are fine).
