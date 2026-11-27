# Module reorganisation and leak fix

## Context
The user listed six items: the LeakCanary leaks, feature modules that depend on other feature modules, a `:common` module that collects unrelated things, a flat module layout, the `:support`/`:test-support` names, and Gradle boilerplate repeated in 17 build files. This plan orders them so each step builds on the one before it:
- The convention plugins come first so that new modules are cheap to create.
- The directory moves come before the splits so that new modules go straight into `core/`, `support/` or `features/`.
- The leak fix comes last, at the user's request, on top of the final module layout.
- Added during step 5 (2026-11-23): the release build fails in R8. Fixing it is the last step of this plan, at the user's request.

Decisions made with the user:
- `:address` splits into `:core:address` and a small `:features:address` that keeps `AddressViewModel`.
- `:about` reaches the licences screen through a `Navigator` interface in `:support:navigation`.
- The plugins go in a `build-logic/` included build.
- The modules fall into four groups:

  | Group | Holds | Modules after the plan | May depend on |
  |---|---|---|---|
  | `:features` | screens and their ViewModels | about, address, connectivity, elevation, faq, feedback, opensource, settings, show-info | `:core`, `:support` |
  | `:core` | domain and data shared by features | distances, settings, geocoding, design-system, address, units, coordinates | `:core`, `:support` |
  | `:support` | technical helpers with no app domain | test, navigation, coroutines, android | `:support` only |
  | `:tools` | build-time checks | konsist | nothing |

  `:app` stays at the root and depends on everything. Packages follow the group: `gc.david.dfm.core.<name>` and `gc.david.dfm.support.<name>`.

Working rules (from memory):
- Do one step at a time and finish it before starting the next.
- Explain each step before the first edit.
- Back each change with before/after evidence and reproduction steps.
- Show the diff in the reply.
- Commit only when the user asks.

## What the exploration found
- **Feature→feature edges**: only two.
  - `about → opensource`: `AboutActivity.kt:72` calls `OpenSourceLicensesActivity.open`.
  - `show-info → address`: `ShowInfoViewModel` uses `GetAddressNameByCoordinatesUseCase`, `GeocodingResult` and `GeocodingErrorMessageMapper`.
- **Who uses `:common`**:

  | Piece | Users |
  |---|---|
  | `launchCatching`, `suspendRunCatching`, `logUnexpectedFailure`, IO dispatcher | 6 modules |
  | `Coordinates`, `CoordinatesRepository`, `DistanceCalculator` | address, app, elevation, show-info |
  | `UnitSystem`, `UnitConverter`, `DistanceFormatter` | app, core-settings, elevation, show-info |
  | `ResourceProvider`, `ConnectionManager`, `tryStartActivity` + toast strings, `UiUtils`, `collectOnStarted` | Android helpers used by several modules |
  | `BuildConfigProvider`, `PermissionChecker`, launcher mipmaps | `:app` only |
  | `toPlainString` | address and elevation only (Google API queries) |
  | `dimens.xml` | nothing (dead) |

- **Build files**: every Android module repeats the same blocks: `compileSdk`/`minSdk`, Java 11 + `jvmTarget`, `useJUnitPlatform`, the Jupiter/Mockito/coroutines-test dependencies, and the Compose setup. There is no `buildSrc` or `build-logic` yet. The build uses Gradle 8.13 and AGP 8.13.2.
- **Konsist**: the guards use `scopeFromProduction`/`scopeFromProject` and walk files from `projectRootPath`, with no hard-coded module paths, so the moves don't break them.
- **Trap**: `.gitignore` has `*/src/debug/res/values/other.xml` and `/geocoding/src/main/res/values/other.xml`. These patterns only match one directory level. After `geocoding/` moves to `core/geocoding/`, the API key files would no longer be ignored and could be committed. Fix `.gitignore` before the move, and move the ignored key files by hand, because `git mv` doesn't move them.
- **Leak**: `leaks.db` on the emulator holds one signature with 9 occurrences (3 heap analyses, hprofs in `/sdcard/Download/leakcanary-gc.david.dfm/`). It is an application leak, not a library leak. A destroyed `MainActivity` is still reachable through this chain:
  - `SnapshotKt.applyObservers` (static)
  - the `Recomposer.recompositionRunner` lambda
  - `Recomposer.runnerJob`
  - `WindowRecomposer…createLifecycleAwareWindowRecomposer…onStateChanged$1`, whose `$source` is `MainActivity` with `mDestroyed = true`

  The window recomposer was cancelled on ON_DESTROY but never finished. Usually that means a coroutine inside the composition ignores cancellation, for example an effect that swallows `CancellationException` or blocks.

## Step 1: Gradle convention plugins (`build-logic/`)
- Add `build-logic/settings.gradle.kts`, which reads `../gradle/libs.versions.toml`.
- Add `build-logic/convention/` (`kotlin-dsl`). The plugins reach the catalog through `VersionCatalogsExtension`.
- In the root `settings.gradle.kts`, add `includeBuild("build-logic")` inside `pluginManagement`.
- Plugins, each registered in `gradlePlugin { }` and aliased in the version catalog as `dfm-…`:

  | Plugin | What it sets up |
  |---|---|
  | `dfm.android.application` | AGP app + Kotlin, SDK levels, Java 11 + `jvmTarget` |
  | `dfm.android.library` | the same for libraries |
  | `dfm.android.compose` | Compose compiler plugin, `buildFeatures.compose`, Compose BOM, `ui-tooling-preview`, debug `ui-tooling` |
  | `dfm.unit-test` | `useJUnitPlatform`, the Jupiter api/params/engine, mockito, mockito-kotlin, coroutines-test, and the test-support project (skipped when applied to that module itself) |
  | `dfm.jvm.library` | Kotlin JVM + Java 11 + JUnit Platform (for Konsist) |

- Migrate the modules in two commits: first the plugins plus 2 pilot modules (`:faq` and `:core-distances`), then all the others.
- Konsist guard: no module's `build.gradle.kts` (outside `build-logic/`) sets `compileSdk`, `jvmTarget` or `useJUnitPlatform`, or applies `android.library`/`android.application` directly.
- Check:
  - Before migrating, save `dependencies --configuration debugRuntimeClasspath` and `debugUnitTestRuntimeClasspath` for every module to the scratchpad.
  - After migrating, diff the same output. The only changes should be test dependencies added on purpose.
  - Run `assembleDebug`, `testDebugUnitTest`, the Konsist tests and `lint`.
  - Install the app for a quick launch check.

## Step 2: Rename the support modules
- `support/konsist` becomes `tools/konsist` (`:tools:konsist`).
- `test-support/` becomes `support/test` (`:support:test`).
  - Its namespace and package change from `gc.david.dfm.testsupport` to `gc.david.dfm.support.test`, to match the other `:support` modules. That is an import change in 10 files.
- `:support` then stops being a holder only for Konsist and becomes the group for technical modules. `:support:navigation`, `:support:coroutines` and `:support:android` join it in steps 4 and 5.
- Update `settings.gradle.kts`, the `dfm.unit-test` plugin (`project(":support:test")`), the `CLAUDE.md` commands and the README.
- Check: `testDebugUnitTest` and `./gradlew :tools:konsist:test`.

## Step 3: `:core` and `:features` directories
- `.gitignore` first: switch to `**/src/*/res/values/other.xml`. Then move the ignored `other.xml` files by hand.
- The moves are `git mv` plus `settings.gradle.kts` and `project(...)` updates:
  - `:core:distances`, `:core:settings`, `:core:geocoding`, `:core:design-system`, `:core:common` (temporary, until step 5)
  - `:features:about`, `:features:address`, `:features:connectivity`, `:features:elevation`, `:features:faq`, `:features:feedback`, `:features:opensource`, `:features:settings`, `:features:show-info`
- `:app` stays at the root.
- Namespaces and packages don't change, so this step has no source edits.
- Room's `schemaDirectory` is relative to the project, so the schemas move with the module.
- Konsist guard for the dependency directions in the groups table, both on imports and on `project(...)` lines in each group's `build.gradle.kts` files:
  - Production code under `core/` imports nothing from a `features/` module.
  - Production code under `support/` imports nothing from a `core/` or `features/` module. Today this only covers `:support:test`; the guard then covers each new `:support` module as it is created.
- Check:
  - `git status --ignored` shows the key files as still ignored.
  - Full build and tests, plus Konsist.
  - Install the app and check that search works, as proof that the geocoding key resource still resolves.

## Step 4: No feature→feature dependencies
- **4a `:core:address`**:
  - Moves into it: `AddressRemoteDataSource`, `BaseAddressRepository`, the entities and their mapper, `AddressRepository`, both use cases, the `Address`/`GeocodingResult` models, `GeocodingErrorMessageMapper`, and the geocoding error strings (all 7 locales).
  - Tests move with the code, and the Koin module is split. Packages are renamed to `gc.david.dfm.core.address…` to match the `core.*` convention.
  - `:features:address` keeps only `AddressViewModel` (+ `AddressUiState`, `toast_no_results`).
  - `:features:show-info` now depends on `:core:address`.
- **4b `:support:navigation`**:
  - The new module holds `interface Navigator { fun openOpenSourceLicenses(activity: Activity) }` in `gc.david.dfm.support.navigation`.
  - The interface names destinations, but it doesn't import any feature, so it respects the `:support` rule. Only `:app` sees the Activity classes.
  - `:app` implements it as `AppNavigator` and registers it in a Koin module.
  - `AboutActivity` injects it.
  - The `:features:about → :features:opensource` dependency is removed.
- Konsist guards:
  - Production code in a `features/` module doesn't import the packages of another `features/` module.
  - A file-based check that no `features/*/build.gradle.kts` contains `project(":features:`.
- Check:
  - Show info: reverse geocoding shows the address, and with the network off shows the error text.
  - About → Open source licences opens.
  - Search still works.

## Step 5: Split `:core:common`
5a is done (1c10654).

One commit per sub-step. Each new module uses the step 1 plugins, gets its own Koin module registered in `DFMApplication`, and takes its tests with it.
- **5a**: move single-consumer code to its consumer.
  - `BuildConfigProvider` and `PermissionChecker` (+ `DefaultPermissionChecker` and its test) go to `:app`.
  - The launcher mipmaps (main + release) go to `:app`. Done differently: they went to `:core:design-system`, because the About screen draws the icon too.
  - `toPlainString` (+ `DoubleExtTest`) goes to `:core:geocoding`.
  - The unused `dimens.xml` is deleted.
- **5b `:support:coroutines`**: `launchCatching`, `suspendRunCatching`, `logUnexpectedFailure`, the IO `CoroutineDispatcher` binding, and their tests.
- **5c `:core:units`**: `UnitSystem`, `UnitConverter`, `DistanceFormatter` (+ test).
- **5d `:core:coordinates`**: `Coordinates`, `CoordinatesRepository`, `DistanceCalculator`.
- **5e `:support:android`**: `ResourceProvider`, `ConnectionManager` + `DefaultConnectionManager` + `SystemServiceExt`, `tryStartActivity` + toast strings (7 locales) + test, `UiUtils`, `collectOnStarted`. These are thin wrappers over Android framework services and nothing else.
- **5f**: delete `:core:common` and `commonModule`.
- Packages are renamed to `gc.david.dfm.core.<module>` or `gc.david.dfm.support.<module>`, matching each module's group. About 91 files import `gc.david.dfm.common` today. Each sub-step only changes the imports for the code it moves.
- Order inside step 5: the `:support` modules (5b, 5e) don't depend on the `:core` ones (5c, 5d), so any order builds. Until 5c and 5d, `:core:address` and `:core:settings` still depend on `:core:common`.
- Check:
  - After each sub-step: build, tests, Konsist.
  - After 5f, an emulator smoke run covering distance, units switch, elevation chart, Show info share, About links and the no-connection toast.

## Step 6: Fix the MainActivity leak
**Outcome (2026-11-23):** not fixable inside this plan; documented instead, at the user's choice.
- **Cause:** maps-compose 6.2.0's `GoogleMap` (`launchSubcomposition`, `GoogleMap.kt:216`) waits for the map with maps-ktx 5.1.1's `MapView.awaitMap()`, a non-cancellable `suspendCoroutine` around `getMapAsync`. If `MainActivity` is destroyed before the map is ready, the callback never comes, that effect coroutine never ends, the window recomposer can't shut down, and the destroyed Activity stays reachable through `SnapshotKt.applyObservers`.
- **Reproduction:**
  - Recreate `MainActivity` within about a second of launch, with a size, density or dark-mode change (`wm size 1600x2560` + `wm density 320`, or `cmd uimode night yes` right after the map screen appears).
  - The same changes five seconds after launch don't leak, and neither does Back with or without the chart.
- **Fix path:** maps-compose 9.x uses `android-maps-utils-core` 6.0.0's cancellable `awaitMap` (`suspendCancellableCoroutine`). maps-compose 6.12.2 and 8.6.0 still use the old one, and maps-ktx never changed. A trial build with maps-compose 9.0.1 fails `checkDebugAarMetadata`: 9.0.1 and the Compose 1.12 and material3-adaptive 1.3 libraries it pulls in need compileSdk 37 and AGP 9.1.0.
- **Done here:** a comment at the `GoogleMap` call in `MapContent.kt`, a known-issues note in `CLAUDE.md` (docs step).
- **Follow-up:** a separate plan, after this one, for the toolchain upgrade (AGP 9.1, Gradle 9, compileSdk 37, Kotlin 2.4, KSP, Compose BOM, maps-compose 9.0.1), using the `agp-9-upgrade` skill. Then check this leak is gone with the same reproduction.

Original approach, kept for reference:
1. Find the trigger. Reproduce one action at a time with logcat filtered on `LeakCanary`:
   - rotation
   - Back out of the map
   - opening and closing the elevation chart, then rotating
   - opening Settings or About, then returning

   Clearing LeakCanary's history on the emulator first needs the user's OK.
2. Find the holder. Open the newest hprof (in Android Studio or with shark-cli) and look at the leaked `Recomposer`'s live child jobs, to see which effect or flow is still running after cancellation. Candidates to read alongside the hprof:
   - effects in `MainScreen`/`MapContent` (`LaunchedEffect` blocks, the camera-update queue held until `onMapLoaded`)
   - maps-compose callbacks
   - Lottie
   - any `catch (e: Exception)` around a suspend call inside composition
3. Fix it in place, rethrowing `CancellationException` as `CLAUDE.md` requires. If the cause is a swallowed cancellation in testable code, add a unit test. If a broad-catch pattern caused it, add a Konsist guard.
4. Evidence:
   - Before: the LeakCanary notification and the leak trace.
   - After: the same reproduction repeated 5 times with no retained `MainActivity`.

## Final step: docs and memory
- `CLAUDE.md`:
  - A known-issues note: the maps-compose 6 leak (step 6 outcome) and the release build until the last step.
  - The module graph, rewritten around the four groups and their allowed dependencies, naming the Konsist guards that enforce them.
  - The commands, the `:common` references and the test-support instructions.
  - The convention plugins, replacing the "new test module needs…" list.
- README module and Testing sections, and `BRANCH_COVERAGE_REPORT.md` paths.
- Memory: plan progress.

## Last step: make `assembleRelease` pass
**Outcome (2026-11-23):** `assembleRelease` passes with `-dontwarn androidx.window.extensions.**` and `-dontwarn androidx.window.sidecar.**`. Runtime check done with a release APK whose manifest had Crashlytics collection switched off (built in a temporary checkout, nothing committed), at the user's choice; the log showed Sessions with no subscribers and no Crashlytics upload (network traffic itself wasn't inspected). `assembleBeta` fails for an older, separate reason (no `matchingFallbacks`); the user first left it, then had the beta build type removed (0d0f85a).

Original step, kept for reference:
- **Problem:** `./gradlew :app:assembleRelease` fails in `minifyReleaseWithR8` with "Missing class `androidx.window.extensions.WindowExtensions`…". A clean checkout of 5df975d fails the same way, so it predates step 5.
  - **Cause:** material3-adaptive 1.2.0 (c86ccf4, 2026-11-22, the collapsed rail) brings in `androidx.window`. It references `androidx.window.extensions.*` and `androidx.window.sidecar.*` classes that the device supplies at runtime.
  - `app/proguard/proguard-androidx-window.pro` (2026-09-03) only suppresses four of them. R8's `missing_rules.txt` lists 12 more.
- **Fix:** in `proguard-androidx-window.pro`, replace the per-class lines with `-dontwarn androidx.window.extensions.**` and `-dontwarn androidx.window.sidecar.**`. Rebuild and check that R8 reports no other missing classes.
- **Build check:**
  - `./gradlew :app:assembleRelease :app:assembleBeta -x uploadCrashlyticsMappingFileRelease -x uploadCrashlyticsMappingFileBeta`. The upload tasks are excluded because they would send the mapping files to Firebase, and these are test builds, not releases.
  - Also run the Crashlytics plugin's other release tasks (`injectCrashlyticsMappingFileId…`). They only write into the build.
- **Runtime check:** install the release APK on `emulator-5554`.
  - Release and debug are signed with different keys and share `gc.david.dfm`, so the debug app has to be uninstalled first. That deletes its data, including LeakCanary's `leaks.db`. The hprofs in `/sdcard/Download/leakcanary-gc.david.dfm/` stay for step 6.
  - Flows: the map and rail, including the collapsed rail at tablet width (the `androidx.window` code path); search and Show info (kotlinx.serialization parsing after R8); saving and loading a distance (Room); the elevation chart; About and its links; Settings.
  - Reinstall the debug build afterwards.
- **Keep it passing:** after this step, the release build command above is part of every change's verification.

## Verification (every step)
- Unit tests: `./gradlew testDebugUnitTest`.
- Konsist: `./gradlew :support:konsist:test` in step 1, then `:tools:konsist:test` from step 2 on.
- `./gradlew lint`: it already fails on `MainScreen.kt:129` (`UnusedMaterial3ScaffoldPaddingParameter`). Only new failures count.
- `installDebug` on `emulator-5554` with the flows the step touches, recorded before and after.
- Until step 6, LeakCanary may still report the known `SnapshotKt.applyObservers` leak. Any other new leak signature during a step's emulator check counts as a regression of that step.
