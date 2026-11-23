# Skill audit, fourth plan: bugs, stability, onboarding, clean-up

## Context

You asked for the skills to be run over the codebase to find bugs and improvements, with a plan. I read all the main Kotlin sources and manifests against: kotlin-concurrency-and-flow, compose-state-and-effects, compose-performance (stability), compose-component-design, jetpack-compose-audit, anr-audit-and-fix, android-intent-security, r8-analyzer and play-policy-insights.

What was **not** run:
- Nothing has been reproduced on a device yet. Findings marked **[library]** were confirmed in the library sources in the Gradle cache; the others come from reading our code.
- No tests, lint or Compose reports: a Gradle run I had started was stopped when plan mode came on. Step 1 runs them.
- jetpack-compose-audit's scored report, play-policy-insights' scripts and r8-analyzer's measured path (it edits `settings.gradle.kts`).

Your decisions so far:
- Reopening the map: you want a video first (step 2); step 7 waits for your choice.
- A tap after a search keeps the searched place as the start (step 6).
- Deleting saved distances stays as it is.
- Included: dead code and dependencies, missing unit tests, startup and lint, onboarding, unstable composable parameters. Backup rules are out.
- Onboarding: hints on the map, shown to everyone once, no replay.

## How each step is worked

- One step at a time, one commit per fix, and only when you ask for the commit.
- Each step starts with an explanation and ends with before/after reproduction steps, screenshots or a recording when visible, and the diff.
- A failing test first wherever the bug is in JVM-testable code.
- **[long-press]** marks steps touching the map state flow. They need your manual long-press check with logcat on `MainViewModel` `onMapClick`/`onMapLongClick` (the open stray-tap issue); scripts prove nothing there. `clearMap` stays.

## Group 0: evidence

### 1. Baseline (no commit)
- Run `testDebugUnitTest`, `:tools:konsist:test` and `lintDebug`, plus the Compose compiler reports for release through the jetpack-compose-audit init script, which changes no file in the repo.
- Keep the results for comparison with the final verification.

### 2. Video of the reopened map (no commit)
- **Expected bug:** Back finishes `MainActivity` (it isn't the task's launcher activity), but `CoordinatesMemoryDataSource` is a process-wide single. The new `MainViewModel` shows an empty map while the old points remain, so a long press draws a path from them.
- **Recording:** any-position mode; tap A, long press B; Back; reopen; long press C. Expected: a line A-B-C instead of the "Starting point needed" message.
- If it doesn't reproduce, step 7 is dropped.

## Group 1: bugs

### 3. Crash: rotate twice with a Settings list dialog open [library]
- **Finding:** `SettingsActivity.onCreate` replaces the fragment on every creation. After one rotation the dialog's target fragment is destroyed and its `mTargetWho` is null, so it isn't saved. The second rotation makes `PreferenceDialogFragmentCompat.onCreate` throw "Target fragment must implement TargetFragment interface".
- **Fix:** `features/settings/.../SettingsActivity.kt`: add the fragment only when `savedInstanceState == null`.
- **Repro:** Settings, Unit, rotate, rotate.

### 4. Rating the app: crash in the background, dead end without Play Store [library]
- **Finding:** `InAppReviewHandler` shows `RateAppFallbackDialogFragment` from an async callback. If the app went to the background meanwhile, `show` throws "Can not perform this action after onSaveInstanceState". Without Play Store both `market://` launches only log.
- **Fix:** skip the dialog when `supportFragmentManager.isStateSaved`. One shared function opens the store with `tryStartActivity` (`support/android/.../ContextExt.kt`), falls back to the https Play page, then to the existing `toast_no_browser_app`.

### 5. `MainViewModelTest` on the real in-memory repositories
- **Finding:** it mocks `CoordinatesRepository`, whose list never changes, so no path bug can fail a test.
- **Change:** use the real `BaseCoordinatesRepository`, `BaseDistanceModeRepository`, `DistanceCalculator` and `UnitFormatter`. Add tests that draw a distance in both modes. No production change.

### 6. A tap after a search deletes the searched place [long-press]
In any-position mode, `onPositionByNameResolved` adds the searched place but doesn't start a selection, so the next tap clears it. Set the flag.
- One commit, with a failing test first.

### 7. Reopened map and stored points disagree [long-press], waits for step 2
- "Start empty": clear `CoordinatesRepository` when `MainViewModel` is created; or "Restore": plot the stored points. You choose after the video.

### 8. My location button stops working after an interrupted animation [library] [long-press]
- **Finding:** a gesture during `cameraPositionState.animate` makes it throw `CancellationException`, so `onCameraUpdateHandled()` never runs. The same update is then equal to the stored one and nothing happens. It also replays when the Activity is recreated.
- **Fix:** `MapContent.kt`: catch `CancellationException`, call `currentCoroutineContext().ensureActive()` (a restarted effect must not clear the newer update), then mark the update handled.
- **Repro:** My location, drag during the animation, My location again without moving.

### 9. Fixed 200 px bounds padding can crash small windows
- **Finding:** `MapStateMapper` passes 200 px to `newLatLngBounds`, which throws when the map is smaller than twice the padding.
- **Fix:** a dp dimension read with the line width. Check it in a small freeform or low-density window; if a crash remains, limit the padding by the map's size in `MapContent`.

### 10. Show info offline hides the distance, and errors are shared as addresses
- **a.** `ShowInfoViewModel.load` returns before setting `distanceMessage` when offline, although the distance is computed locally. Set it first.
- **b.** When a lookup fails, the error text is shared as the address. Share the coordinates instead, keeping the error on screen.
- Two commits.

### 11. Units and number format
- **a.** `DefaultUnitInitializer` compares with `Locale.US`, so US users in Spanish start in metric. Compare the country.
- **b.** `UnitFormatter` is a `single` that keeps one `DecimalFormat`, so separators stay in the old language until the process dies. Build it per call.

### 12. The location snackbar covers the buttons until tapped
- **Finding:** with an action label Material 3 defaults to an indefinite duration, and there is no dismiss action (confirm in step 1's dependency sources).
- **Fix:** `SnackbarDuration.Long` with a dismiss action; open the settings with `tryStartActivity`.

### 13. Cancelling the Play Services dialog leaves a blank screen
- **Fix:** `OnboardActivity`: an `OnCancelListener` that finishes, and no second dialog on a later resume while one is showing.

### 14. Small corrections (one commit each)
- A blank search (`"   "`) calls the API: use `isNotBlank`.
- A saved distance without a name puts a blank line in the map label.
- `ElevationStatus` lacks `DATA_NOT_AVAILABLE`, so it's logged as an error: map it to `NoData`.

**Checkpoint:** all tests, Konsist, and a smoke run before Group 2.

## Group 2: unstable parameters on composables

**Expected effect:** with strong skipping (the default on Kotlin 2.3) composables with unstable parameters already skip when they get the *same instance*. Stable types also skip for *equal* instances. So the gain is fewer recompositions from equal copies; step 1's reports and recomposition counts decide whether each fix stays.

### 15. Domain models inside UI models
- **Affected:** `Coordinates` (`:core:coordinates`, JVM) inside `MarkerData`, `PolylineData`, `CameraUpdate`, `DrawDistanceUiModel` and `SaveDialogData`; `Address` (`:core:address`) inside `AddressUiState` and passed to `MainScreen` and `AddressSuggestionsDialog`. Their modules don't run the Compose compiler, so everything holding them is unstable.
- **Also affected, though it is already a UI model:** `ElevationUiModel`. A class is only inferred stable when its own module runs the Compose compiler, and `:features:elevation` and `:features:address` don't. So a UI model is enough only if its module applies `dfm.android.compose`.

| Option | Pros | Cons |
|---|---|---|
| **A. A UI model per module** (recommended, your direction): `CoordinatesUiModel` in `:app`, `AddressUiModel` in `:features:address`; both UI-model modules apply `dfm.android.compose` | Domain stays out of the UI; stability is inferred, so no promise to keep true by hand; follows the existing `*UiModel` + mapper pattern | Mapping at the ViewModel boundary; about 20 test lines change; two modules gain the Compose compiler |
| B. One shared `CoordinatesUiModel` in a `:core` module | One class for `:app` and Show info | A shared UI module for two doubles; couples the screens' UI models |
| C. Plain fields (`latitude`, `longitude`) in each UI model | No new class | Repeats pairs of doubles that can be swapped; a list of points still needs a class |
| D. `LatLng` in the map's UI models | What the map draws; no mapping in `MapContent` | Still unstable (a Java class); undoes e837c79, which took it out of the ViewModel |
| E. Stability configuration file | Smallest diff | Domain models stay in the UI; a hand-kept promise that a rename silently breaks |
| F. `@Immutable` on the domain models, with `runtime-annotation` | Small diff; works in JVM modules | A Compose dependency in the domain |

- **Fix (option A), one commit each:**
  - **a.** `:features:elevation` applies `dfm.android.compose`. It also moves ElevationChart within the elevation module. `ElevationUiModel` still needs step 16 for its list.
  - **b.** `CoordinatesUiModel(latitude, longitude)` in `app/.../main/presentation/model`, used by `MarkerData`, `PolylineData` and `CameraUpdate`. `MapStateMapper` maps from `Coordinates`, and so do the places in `MainViewModel` that build `MarkerData` and `CameraUpdate`. [long-press]
  - **c.** `AddressUiModel` in `:features:address`, which applies `dfm.android.compose`; `AddressViewModel` maps to it, and `MainActivity` converts the selected one to `Coordinates`, as it already does with `LatLng`.
- **Left as they are:** `DrawDistanceUiModel` (the input of `MapStateMapper`, not shown by any composable), `DistanceMode` in `MainUiState` (an enum, so stable), and `triggerElevationUpdate` (one of the MainActivity relays you deferred).
- **Guard:** a Konsist test that presentation models import no `..domain.model..` class, with those three as listed exceptions (CLAUDE.md asks for a guard with each new convention).

### 16. `List` in UI models and composable parameters
- **Affected:** `MapUiState.markers/polylines`, `CameraUpdate.FitBounds`, `MainUiState.selectFromDistancesLoaded`, `AddressUiState.multipleAddressesFound`, `ElevationUiModel`, `OpenSourceUiState.Content`, and the `List` parameters of `MainScreen`, `AddressSuggestionsDialog`, `DistanceSelectionDialog`, `MapContent`'s `Markers`/`Polylines` and `FaqScreen`.
- **Fix:** `ImmutableList` from `kotlinx-collections-immutable` (0.3.8 is already in the Gradle cache), converted once in the mapper or ViewModel. Domain models and repositories keep `List`.
- **Guard:** a Konsist test that composable parameters and `*UiState`/`*UiModel` properties don't use `List`, `Set` or `Map`, with step 15's `DrawDistanceUiModel` and `triggerElevationUpdate` as exceptions.
- One commit per module.

### 17. Sealed interfaces used as parameters
- `@Immutable` on `DistanceSelectionUiModel`, `MarkerData.InfoWindow`, `CameraUpdate` and `OpenSourceUiState`, whose implementations are all immutable once steps 15 and 16 are in. Only the ones the reports still list.

### 18. New instances on every recomposition [long-press]
- The second plan's skipped step 10: defer the FAB offset read to layout, remember `MapProperties`, a file-level `MapUiSettings`, and remember each polyline's `points` list.
- Kept apart because the timing of map recomposition is the main suspect in the stray-tap issue.

**Checkpoint:** compiler reports and `MapContent` recomposition counts during the rail animation, before and after.

## Group 3: onboarding hints on the map

Today `OnboardActivity` only checks Play Services, no gesture is visible, and taps or long presses without a location do nothing silently.

### 19. Choosing the hint
- `MapHint` (enum) and a pure `MapHintMapper` in `app/.../main/presentation`, with tests; `MainUiState.hint: MapHint?`.

| Situation | Hint |
|---|---|
| Current position, no location yet | Waiting for your position |
| Current position, located, nothing drawn | Long press a place to measure from where you are |
| Any position, no points | Tap a place to start |
| Points being selected | Tap to add more points. Long press to finish |
| Distance drawn | Tap the label for addresses, sharing and saving |

- "Waiting for your position" describes a state, so it shows whenever it applies; the other four are shown once.

### 20. Showing each hint once
- `HintRepository` in `:app`'s `onboarding/` package, with `BaseHintRepository` and `HintDiskDataSource` on the default `SharedPreferences`, bound in `appModule`, with tests. New keys, so existing users also see each hint once after updating.
- A hint is retired when the user does what it says or closes it. Nothing replays it.

### 21. The banner
- `MapHintBanner(text, onDismiss, modifier)` under the search bar and above the elevation chart, with a polite live region and a labelled close button. Hidden while the search field or the rationale dialog is open.
- Strings in en, ca, de, es, fr, it and pt. Checked with TalkBack labels, landscape, the permanent rail, and a large font.
- [long-press], since `MainUiState` changes with each tap.

### 22. Correct the FAQ
- "How can I get a distance?" and the any-position answer say a long press is enough, but any-position mode needs a tap first. Both answers in 7 languages.

## Group 4: startup and lint

### 23. Maps SDK initialiser on the startup path
- **Finding (ANR skill):** `MapInitializer` runs `MapsInitializer.initialize` on the main thread in `Application.onCreate`. Its stated reason is gone: `CameraUpdateFactory` is only used after `onMapLoaded` (2472bd5).
- Measure 10 cold starts (`am start -W`) with and without it. Remove it if the map, search and camera moves work; keep it if there is no measurable gain.

### 24. Lint error
- `MainScreen.kt`: use the Scaffold's `innerPadding` (zero here, `contentWindowInsets` is empty). `./gradlew lint` then passes.

## Group 5: dead code and dependencies

One commit each:
- **25.** Delete `:features:connectivity`, which nothing uses: `settings.gradle.kts`, `:app`, CLAUDE.md, README.
- **26.** Delete `AddressViewModel.onAddressSearch(coordinates)`, its use-case parameter (Koin `get()` count 5 to 4) and its tests; only tests call it. The use case stays, Show info uses it.
- **27.** Unused strings in all locales: `toast_no_find_address`, `toast_no_location_found`, `dialog_rate_app_negative_button`.
- **28.** Unused dependencies and plugins: `cardview`, `constraintlayout`, `collection-ktx`, `runtime-livedata`, `arch-core-testing`, `kotlin-parcelize`, and `:app`'s direct `okhttp`s if the build confirms it. `androidx.preference` stays (`PreferenceThemeOverlay`), and so does Jetifier (GraphView needs `support-v4`).
- **29.** Open source screen data: remove ConstraintLayout, JUnit 4.13.2 becomes 5.11.4.
- **30.** Legacy manifest entries: `MAPS_RECEIVE` and `org.apache.http.legacy`. Check the merged manifest and the map on the API 36 emulator.

## Group 6: missing tests and closing

- **31.** Tests for `UnitConverter` (thresholds in both systems) and `DistanceCalculator` (same point, known distance, antimeridian, antipodes), both in JVM modules.
- **32.** Docs: CLAUDE.md (modules, no domain models in UI models, `ImmutableList` rule, hints), README and `BRANCH_COVERAGE_REPORT.md`.

## Verification

- `./gradlew testDebugUnitTest :tools:konsist:test lint assembleDebug`, compared with step 1.
- `assembleRelease -x uploadCrashlyticsMappingFileRelease`, then a Crashlytics-off smoke run as in the third plan.
- Emulator smoke run: both modes, search, saved distances, Show info online and offline, Settings rotation, rail on phone and tablet sizes, each hint once. Few API searches, because of the rate limit.
- Your long-press pass over every [long-press] step.

## Left out

- **Yours or deferred:** chart visibility (including Back closing the screen with the chart open), MainActivity relays, Compose UI tests, Room transaction test, R8 rules, stashed step 6, AGP 9 and maps-compose 9.
- **Your choice now:** backup rules; confirmation or error message when deleting distances; the next tap joining a loaded distance when points were being selected; `ORDER BY` for stored positions.
- **Needs you outside the code:** the app has no privacy policy link although it uses location (Play policy); the geocoding key ships in the APK.
- **Checked, no finding:** intents and exported components, coroutine cancellation in `launchCatching`/`suspendRunCatching`, location collection lifetime, Room transactions, translations (no missing strings).
