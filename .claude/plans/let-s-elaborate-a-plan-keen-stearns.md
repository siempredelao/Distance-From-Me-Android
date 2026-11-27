# Fix the remaining skill-audit findings

## Context

The earlier skill-guided cleanup (Konsist guards, sealed results, transactions) is committed on `develop`. This plan covers the findings still open after auditing the repo against the skills that apply to it. For each finding I checked the code against that skill's text.

Skills loaded and checked:
- `chrisbanes-skills:compose-state-and-effects`, including its side-effects and state-hoisting references
- `chrisbanes-skills:kotlin-concurrency-and-flow`, including its structured-concurrency and flow-state references
- `chrisbanes-skills:compose-component-design`, including its modifier-layout reference
- `android-skills:android-permissions-security`
- `android-skills:edge-to-edge`

You chose to cover groups 1–4 (bugs, Android platform, Compose cleanups, and the location architecture). Out of scope, and listed under "Deferred" at the end: the MainActivity ViewModel relays and the chart-visibility bug (you're investigating those), and the build/test group.

Per your standing preference, I'll leave every step uncommitted for you to review. The steps are sized as one commit each, but I'll commit only the ones you name.

## Findings and fixes, in execution order

### 1. ShowInfo after process death (bug)

**Finding:** `ShowInfoActivity` calls `showInfoViewModel.onStart()` only when `savedInstanceState == null`. After process death, the new ViewModel never gets its `lateinit var inputParams`, so refresh, share and save crash with `UninitializedPropertyAccessException`.
- Skill: `kotlin-concurrency-and-flow`, flow-state reference: "If absence … is real, model it explicitly … do not leak a fake" value.

**Related findings in the same Activity:**
- `finish()` is called directly in the composable body (`if (uiState.shouldFinish) finish()`).
  - Skill: `compose-state-and-effects`, side-effects core principle: "Composable bodies render; external work belongs to the smallest effect whose lifecycle matches."
- It uses `collectAsState()` on both ViewModels.
  - Skill: `compose-state-and-effects`, side-effects Exceptions: "Use lifecycle-aware state collection on Android."

**Fix:**
- `show-info/.../ShowInfoViewModel.kt`:
  - Make it `private var inputParams: InputParams? = null`.
  - `onStart()` returns early when it's already set.
  - `onRefresh`, `onShare` and `onSave` start with `val params = inputParams ?: return`.
- `ShowInfoActivity.kt`:
  - Call `onStart()` on every `onCreate`. After process death, `CoordinatesRepository` is empty, so the existing `shouldFinish` path closes the screen.
  - Wrap finishing in `LaunchedEffect(uiState.shouldFinish) { if (uiState.shouldFinish) finish() }`.
  - Switch to `collectAsStateWithLifecycle()`, and add `implementation(libs.lifecycle.runtime.compose)` to `show-info/build.gradle.kts` (it's currently missing).
- Tests, in `ShowInfoViewModelTest`:
  - `onShare`, `onRefresh` and `onSave` before `onStart` do nothing and don't throw.
  - A second `onStart` doesn't reload.

### 2. Android platform: manifest

**Finding:** `MainActivity` is `android:exported="true"` with no intent filter, so any app can launch it and skip `OnboardActivity`'s Play Services check.
- Skill: `android-permissions-security` §3: "Private components: Always set `android:exported="false"`."

**Fix:**
- In `app/src/main/AndroidManifest.xml`, set `exported="false"`. Only `OnboardActivity` launches it.
- While I'm in the manifests: `show-info/src/main/AndroidManifest.xml` has a `parentActivityName` pointing to a class that doesn't exist (`gc.david.dfm.onboarding.presentation.MainActivity`). Correct it to `gc.david.dfm.main.presentation.MainActivity`.
- Also in `app/src/main/AndroidManifest.xml`, add `android:windowSoftInputMode="adjustResize"` to `MainActivity`, which has the search field.
  - Skill: `edge-to-edge` IME section: "For each Activity with a soft keyboard, check that `adjustResize` is set."

### 3. Android platform: edge-to-edge

**Finding:** `AboutActivity`, `FaqActivity`, `OpenSourceLicensesActivity` and `ShowInfoActivity` never call `enableEdgeToEdge()`. With `targetSdk = 36` they're edge-to-edge on Android 15+ but not below, so the screens look different by API level.
- Skill: `edge-to-edge` Step 2 and its checklist: "Does every `Activity` call `enableEdgeToEdge()`?"

**Finding:** `AboutScreen`, `FaqScreen` and `OpenSourceListScreen` apply the Scaffold's `innerPadding` as `Modifier.padding(...)` on the `LazyColumn`, so the content is clipped instead of scrolling behind the system bars.
- Skill: `edge-to-edge` Lists section: "Apply inset padding … to the `contentPadding` parameter … DO NOT apply it as a `Modifier.padding()`."

**Finding:** `ShowInfoScreen` uses `contentWindowInsets = WindowInsets(0)`, so its scrolling content ends under the navigation bar on Android 15+.
- Skill: `edge-to-edge` Step 3, which says to apply system insets.

**Fix:**
- Add `enableEdgeToEdge()` before `setContent` in those four Activities.
- Give each list `contentPadding` = `innerPadding` plus the existing `Spacing` padding, and add `consumeWindowInsets(innerPadding)`. For the sum, add a small `PaddingValues.plus` helper in `:design-system`, next to `Spacing`.
- In `ShowInfoScreen`, drop `contentWindowInsets = WindowInsets(0)` and pad the content by the Scaffold's default `innerPadding`.
- `SettingsActivity` stays as it is. It's a Fragment with an XML layout, which this skill doesn't cover (it requires Compose), and its `fitsSystemWindows` already keeps it clear of the bars.

### 4. Compose cleanups

**`MapContent` parameter order**
- Finding: `properties` and `uiSettings` are required but declared after `modifier`.
  - Skill: `compose-component-design`, modifier-layout reference, step 1: "declare `modifier: Modifier = Modifier` after required parameters."
- Fix: in `app/.../components/MapContent.kt`, move both before `modifier`. Both callers, `MainScreen.kt:134` and the preview at `MapContent.kt:218`, use named arguments, so they compile unchanged.

**The dead `clearMap` flag** (dropped on 2026-11-21: not dead after all)
- Outcome: removing it made a long press in any-position mode add a stray point. The Maps SDK fired `onMapClick` about 0.57 s after `onMapLongClick`, as the finger lifted during the zoom animation. The user confirmed on the emulator that it doesn't happen with `clearMap` kept. The cause is unknown, so `clearMap` stays, and the rest of step 4 was committed without this item.
- Finding: `MapUiState.clearMap` is set by `MapStateMapper`, `MainViewModel.resetMap` and `onMapClick`. Its only consumer is a `LaunchedEffect` that immediately calls `onMapClearHandled()`; maps-compose already redraws from `markers` and `polylines`. That's an effect with no work, and a flag with no reader.
  - Skill: `compose-state-and-effects` step 9: "every effect has a justified lifecycle and key."
- Fix: remove `clearMap`, `onMapClearHandled` and the `onMapClearHandled` parameter of `MainScreen`/`MapContent`. That's 14 references, including assertions in `MainViewModelTest` and `MapStateMapperTest`.

**Stale address searches**
- Finding: `AddressViewModel.onAddressSearch(query)` and `onAddressSearch(coordinates)` launch without cancelling an earlier search, so a slow earlier response can overwrite a newer one. This is the race commit `847eba7` already fixed in `ElevationViewModel`.
  - Skill: `kotlin-concurrency-and-flow` core principle: "Give asynchronous work an explicit owner and lifetime."
- Fix: keep one `searchJob: Job?` and cancel it before each `launchCatching`, mirroring `ElevationViewModel.elevationJob`.
- Test: in `AddressViewModelTest`, a second search cancels the first. Use a `CompletableDeferred` stub, and check that only the second result reaches `uiState`.

**Screens without a `modifier` parameter**
- Finding (low priority): `AboutScreen`, `FaqScreen` and `ShowInfoScreen` emit layout but take no `modifier`.
  - Skill: `compose-component-design`, modifier-layout reference, step 1.
- Fix: add `modifier: Modifier = Modifier` after the required parameters, applied to the root `Scaffold`.

### 5. Location permissions (bugs)

**Coarse-only grants are rejected**
- Finding: `DefaultPermissionChecker.isLocationPermissionGranted()` requires FINE **and** COARSE, and `MainActivity`'s permission callback requires `permissions.values.all { it }`. On Android 12+, a user who picks "Approximate" only is treated as having denied, and gets prompted again.
  - Skill: `android-permissions-security` §7, whose sample treats `fineGranted || coarseGranted` as granted.
- Fix:
  - `common/.../DefaultPermissionChecker.kt`: FINE **or** COARSE.
  - `MainActivity` callback: `any { it }`.

**The rationale dialog can loop**
- Finding: `MainViewModel.onStart()`, called from `MainActivity.onCreate`, sets `requestLocationPermission` whenever the mode is FROM_CURRENT_POINT and permission is missing. Nothing checks `shouldShowRequestPermissionRationale`. After a permanent denial, `launch()` returns "denied" at once, `resetMap()` runs, and the next start prompts again.
  - Skill: `android-permissions-security` §6, the 3-state flow with "Handle Permanent Denial", and its checklist item "NEVER create infinite permission request loops."
- Fix:
  - `MainViewModel` only prompts once per ViewModel lifetime.
  - In the denial callback, `MainActivity` calls a new `mainViewModel.onLocationPermissionDenied(permanently = !shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION))`. That switches to FROM_ANY_POINT mode, the same as the existing rationale-dialog Dismiss button. When the denial is permanent, it also shows the existing settings snackbar (`showLocationPermissionSnackbar`).

**The permission state is read during composition**
- Finding: `isMyLocationEnabled = … && permissionChecker.isLocationPermissionGranted()` is read during composition. It's not observable state, so the my-location layer only updates when something else happens to recompose the screen.
  - Skill: `compose-state-and-effects` step 3, which gives each state value one owner. Here, the screen state holder.
- Fix: `MainViewModel` holds `locationPermissionGranted: MutableStateFlow<Boolean>` and exposes it in `MainUiState`. A new `onLocationPermissionChanged()` updates it from the permission callback and from `MainActivity.onResume`, which covers a grant made in Settings.

**`MainViewModel.init` launches a collection**
- Finding: `init` runs `getDistancesUseCase()…launchIn(viewModelScope)`.
  - Skill: `kotlin-concurrency-and-flow`, structured-concurrency reference, step 4: "Move construction-time and initializer launches to an explicit suspending bootstrap or named launch site."
- Fix: start that collection from the once-guarded `onStart()` described above.

**Tests**, in `MainViewModelTest`:
- Denial switches the mode.
- A permanent denial shows the snackbar.
- `onStart` prompts only once.
- The permission state flows into `MainUiState`.
- The distances flag updates after `onStart`.

### 6. Location stack: replace the Service with a Flow (architecture)

**Findings:**
- `GeofencingService` is a `START_STICKY` started Service that sends locations back by broadcast, only so they can be delivered while `MainActivity` is visible. After a process restart, the system can restart it with nobody receiving its broadcasts. Each `onStartCommand` also creates a new `LocationCallback` without removing the previous one.
  - Skill: `kotlin-concurrency-and-flow` core principle (explicit owner and lifetime). Its structured-concurrency table says to expose `suspend` and let the caller choose the scope.
- `requestLocationUpdates` and `lastLocation` run under `@SuppressLint("MissingPermission")` without catching `SecurityException`. A sticky restart after the permission is revoked would crash.
  - Skill: `android-permissions-security` §8: "MUST catch `SecurityException` around protected API calls."
- `CurrentLocationProvider.UNDEFINED = Point(Double.MAX_VALUE, Double.MIN_VALUE)` is a sentinel standing in for "no location yet".
  - Skill: `kotlin-concurrency-and-flow`, flow-state reference: "do not leak a fake … as domain data."
- `MainViewModel` imports `distance.data` types. This is the Konsist failure "presentation layer does not depend on the data layer", the only Konsist guard still failing.

**Fix**, all in `:app` and following the existing `CoordinatesRepository`/`Base*Repository` pattern:
- **New location repository:**
  - Interface `location/domain/LocationRepository` with `fun observeLocation(): Flow<Coordinates>`.
  - Implementation `location/data/BaseLocationRepository(FusedLocationProviderClient)`, built as a `callbackFlow`:
    1. Emit the last known location.
    2. `requestLocationUpdates(request, callback, Looper.getMainLooper())`.
    3. `awaitClose { removeLocationUpdates(callback) }`.
    4. On `SecurityException`, log it and close the flow normally.
    5. Finish with `distinctUntilChanged()`, which replaces the `previousLatLng` check.
  - Keep the current request settings: high accuracy, a 5 s interval and a 1 s minimum.
- **New distance-mode repository:**
  - Interface `distance/domain/DistanceModeRepository` with implementation `distance/data/BaseDistanceModeRepository`, replacing `DistanceModeProvider`.
  - Move `DistanceMode` to `distance/domain/model`.
  - It stays an app-wide singleton, so the chosen mode survives leaving and reopening the screen, as it does today.
- **`MainViewModel`:**
  - Add `suspend fun trackLocation()`: `locationPermissionGranted.collectLatest { granted -> if (granted) locationRepository.observeLocation().collect(::onLocationChanged) }`.
  - Keep `private var currentLocation: Coordinates?` and replace every `UNDEFINED` comparison with a null check.
  - `onLocationChanged` becomes private and takes `Coordinates`.
  - That makes 15 → 15 constructor dependencies: `LocationRepository` and `DistanceModeRepository` replace the two providers. Update the positional `get()` calls in `AppModule`.
- **`MainActivity`:**
  - In `onCreate`, run `lifecycleScope.launch { repeatOnLifecycle(STARTED) { mainViewModel.trackLocation() } }`. Updates then stop below STARTED, which is what `onStop` → `stopService` does today.
  - Remove `locationManager` and `lifecycle.addObserver`.
  - Keep the "loading position" toast on a grant.
- **Delete:**
  - `service/GeofencingService.kt` and its `<service>` manifest entry
  - `location/GeofencingLocationManager.kt`
  - `distance/data/CurrentLocationProvider.kt`
  - `distance/data/DistanceModeProvider.kt`
  - `distance/data/model/Point.kt`
  - their Koin bindings
- **DI:** provide `LocationServices.getFusedLocationProviderClient(androidContext())` in `appModule`.
- **Tests:**
  - `BaseLocationRepositoryTest`, with a mocked `FusedLocationProviderClient` (an interface in play-services-location 21.3.0). It checks that the last location is emitted, that updates arrive, that the callback is removed on cancel, that the flow completes on `SecurityException`, and that duplicates are dropped.
  - `BaseDistanceModeRepositoryTest`.
  - `MainViewModelTest`: switch to the new interfaces and use a fake `LocationRepository` flow. Check that `trackLocation` starts collecting on a grant and that a missing location blocks distance drawing, the existing `UNDEFINED` tests rewritten with null.

## Verification

After each step:
- `./gradlew :<module>:testDebugUnitTest` for each touched module.
- `./gradlew :support:konsist:test`. Every guard should pass after step 6. Until then, only the existing `:app` layer guard fails.

At the end:
- `./gradlew testDebugUnitTest :support:konsist:test assembleDebug lint`.
- On a device or emulator (the `run`/`android-cli` skills can drive this):
  1. Android 12+: grant "Approximate" only. The my-location dot and current-point distances should work, with no re-prompt.
  2. Deny twice (a permanent denial). There should be no loop: the mode switches to any-point and the settings snackbar appears. Grant in Settings and return; location should start without reopening the screen.
  3. Open Show info, background the app, run `adb shell am kill gc.david.dfm`, and return. The screen should close cleanly, with no crash.
  4. On an Android 15+ emulator, check About, FAQ, open source licences and Show info with gesture and 3-button navigation. The last list item and the Show info content should stay above the navigation bar.
  5. Background the map screen and check `adb shell dumpsys location` for no active request from `gc.david.dfm`. Bring it back and updates should resume.
  6. `adb shell am start -n gc.david.dfm/.main.presentation.MainActivity` from the shell should now be refused, because the Activity isn't exported.
- Update `BRANCH_COVERAGE_REPORT.md` and the README Testing section for the new and removed test classes, as CLAUDE.md asks.

## Deferred (not in this plan)

- **MainActivity relays and chart visibility**, pending your investigation. Chart visibility has three owners (`ElevationUiState.showChart`, `MainUiState.showChart`, and a local `rememberSaveable`). `MainUiState.showChart` is never set to `true`, so `resetMap()` can't close an open chart, and the chart probably closes itself right after opening. The `openShowInfo` click → flag → effect → navigation hop is the same pattern. (`compose-state-and-effects`)
- **Build/test group** (excluded by you):
  - Stale R8 rules in `app/proguard/`: `proguard-support.pro` keeps `android.support.v7` classes, `proguard-graphview.pro` and `proguard-playservices.pro` are empty, and the default optimize rules aren't included (`r8-analyzer`, not run).
  - There are no Compose UI tests or `androidTest` source sets (`compose-ui-testing-patterns` / `testing-setup`, not loaded).
  - There's no in-memory Room test for the save/clear transaction.
- **Legacy manifest entries**, not covered by any loaded skill: the `MAPS_RECEIVE` permission and the `org.apache.http.legacy` library.
- **Formatting:** the missing blank line after the license header in 12 files.
