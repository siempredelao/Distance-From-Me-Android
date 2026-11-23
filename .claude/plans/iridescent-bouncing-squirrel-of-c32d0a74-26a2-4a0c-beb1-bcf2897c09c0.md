# Fix the second skill-audit findings: user-visible bugs and Compose UI

## Context

The first cleanup plan (`let-s-elaborate-a-plan-keen-stearns.md`) is done and committed on `develop`, up to 1896e11. You asked me to run all the skills again and plan the next fixes. Three read-only audits loaded every skill that applies to an Android, Kotlin and Compose app:
- **Compose UI:** state and effects, component design, compose-performance, animations, focus, UI testing, compose-agent, jetpack-compose-audit, android jetpack-compose-performance, adaptive, edge-to-edge, material-3.
- **Kotlin, architecture and tests:** concurrency and flow, control flow, API design, code review, simplification, interface design, observability, performance, TDD, testing setup.
- **Android platform and build:** permissions and intent security, ANR, R8, AGP 9, navigation-3 and navigation-event, play-policy, security hardening, CI, deprecation, gradle-run.

I skipped the skills that don't fit this app: Flutter and Dart, web, Wear, TV, cars, camera, billing, ML, XR and glasses. The audits found about 60 issues. I spot-checked the bugs against the code: the unit reset, the search guard, the URL building, the stale elevation, the FAQ `remember`, `configChanges`, the missing labels, the uncaught intents, the goo.gl link and the keystore NPE. All are real.

You chose two groups: **1, user-visible bugs** and **2, Compose performance and UI**. The chart-visibility and relay items stay deferred, along with the other groups (see the end).

Working rules, from your standing preferences:
- One step at a time, each sized as one commit.
- I leave each step uncommitted until you name it.
- Every fix gets before-and-after reproduction steps, with screenshots or a recording, or a note that nothing is visible.
- Steps marked **[long-press]** change the map state or how the map composes. They need your manual long-press check, read from logcat with the filter `MainViewModel\( *[0-9]+\): onMap(Long)?Click`.
- Each bug fix gets a regression test.

## Group 1: user-visible bugs

### 1. The unit setting resets on every cold start
- **Finding:** `app/.../initializers/DefaultUnitInitializer.kt:31` always calls `setUnitSystemPreference(locale default)` from `DFMApplication.onCreate`. If you pick miles in a non-US locale, it's back to km after the process restarts. Skills: code-review-and-quality (edge cases), anr-audit-and-fix (startup work).
- **Fix:**
  - Add `hasMeasureUnitPreference()` to `settings/.../data/SettingsDiskDataSource.kt`, using `contains(MEASURE_UNIT_KEY)`.
  - Add `hasUnitSystemPreference()` to `SettingsRepository` and `BaseSettingsRepository`.
  - The initializer then writes the default only when nothing is stored.
  - First check that the `ListPreference` at `settings/src/main/res/xml/settings.xml:43` has no `defaultValue` that would already be written.
- **Tests:** a new `DefaultUnitInitializerTest`, with a stored value and without one, plus a case in `BaseSettingsRepositoryTest`.
- **Repro:** use an emulator in the es locale. Pick miles, run `adb shell am force-stop gc.david.dfm`, then reopen and draw a distance. Before the fix it's in km; after, in miles.

### 2. Address search does nothing without a location fix
- **Finding:** `MainViewModel.onAddressSearch` (`:353`) drops the query when `currentLocation == null`. Since 2fb7bae, refusing location switches to any-position mode, so those users can't search at all. Skill: code-review-and-quality.
- **Fix:**
  - Remove the guard.
  - In `onPositionByNameResolved`, when the mode is FROM_CURRENT_POINT but there's no location yet, move the camera to the result (`CameraUpdate.MoveTo`) instead of silently doing nothing in `onMapLongClick`.
- **Tests (`MainViewModelTest`):** searching without a location sets `searchAddress`. Resolving a place in current-point mode without a location moves the camera.
- **Repro:** deny location (the mode becomes any-position), then search "Madrid". Before: nothing happens. After: the marker appears and the camera moves there.
- **[long-press]**, because it touches `cameraUpdate`.

### 3. Google API URLs: unescaped queries and coordinates in scientific notation
- **Findings:**
  - `address/.../data/AddressRemoteDataSource.kt:53` only replaces spaces. "Barnes & Noble" splits into a stray parameter, and "Flat #3" cuts off `&key=`. Skill: security-and-hardening, "Validate all external input at the system boundary".
  - `"${lat},${lng}"` turns |x| < 1e-3 into `5.0E-4`, both in `AddressRemoteDataSource.kt:47` and in `elevation/.../GetElevationByCoordinatesUseCase.kt:30`. That breaks points near the equator or the Greenwich meridian.
- **Fix:**
  - Build both URLs with `"https://maps.googleapis.com/...".toHttpUrl().newBuilder().addQueryParameter(...)`, for `address`, `latlng`, `path`, `samples` and `key`.
  - Format coordinates with `toBigDecimal().toPlainString()`.
  - Change `ElevationRemoteDataSource.getElevation` so the full URL is built in one place. The debug log keeps the URL without the key.
- **Tests:** extend `AddressRemoteDataSourceTest` (check first whether it captures the request URL) and `GetElevationByCoordinatesUseCaseTest` with a `&`, a `#` and a `0.0005` coordinate.
- **Repro:** search "Barnes & Noble New York". Before: the wrong place or no result. After: the right place. Also draw a short line across Greenwich in London with the elevation chart on.
- **Added during review (2026-11-22):** `toBigDecimal().toPlainString()` is extracted to `Double.toPlainString()` in `common/.../common/DoubleExt.kt`, with a parameterized `DoubleExtTest`. It lives outside `data/` because the elevation use case (domain) calls it.

### 3b. One Google API client that adds the key (intermediate step, added 2026-11-22 at your request)
- **Finding:**
  - `AddressRemoteDataSource` and `ElevationRemoteDataSource` each take `GeocodeApiKeyProvider` and append `key` themselves.
  - Each one builds its own `OkHttpClient()` through a default constructor argument, so the app runs two clients with separate connection pools and thread pools, both talking to `maps.googleapis.com`. OkHttp's guidance is to share one client.
  - Those two data sources are the only users of `GeocodeApiKeyProvider`.
- **Fix:**
  - `common/.../common/data/GoogleApiKeyInterceptor(apiKeyProvider)`: an OkHttp `Interceptor` that adds the `key` query parameter. To keep the key from reaching any other server, it only does so when the host is `maps.googleapis.com` and the request has no `key` yet.
  - `commonModule` provides one `OkHttpClient` under a qualifier (`named(GOOGLE_API_CLIENT)`, a constant in `:common`) with that interceptor. The qualifier states what the client is for and stops a future plain `OkHttpClient` injection from picking it up by accident.
  - Both data sources drop the `GeocodeApiKeyProvider` parameter and the default `OkHttpClient()`. They take the client as a required parameter, and `addressModule`/`elevationModule` pass `get(named(GOOGLE_API_CLIENT))`. They still log the URL before the interceptor runs, so the log stays key-free with no extra code.
  - Konsist guard (per CLAUDE.md, "when you add a convention, add a guard"): remote data sources don't depend on `GeocodeApiKeyProvider` and don't construct an `OkHttpClient`.
  - Out of scope: the deferred `callTimeout` (group 3) stays deferred. After this step, it's a one-line change in `commonModule`.
- **Tests:**
  - New `GoogleApiKeyInterceptorTest`: adds the key for `maps.googleapis.com`, leaves other hosts untouched, and doesn't duplicate an existing `key`.
  - `AddressRemoteDataSourceTest` and `ElevationRemoteDataSourceTest` stop mocking the key provider. Their parameter-set assertions drop `key`, which shows the data sources no longer add it.
- **Pros:**
  - The key is added in one place, so a new Google API call can't forget it or add it twice.
  - One shared client (one connection pool and thread pool) instead of two.
  - A single place for future client policy: the deferred `callTimeout`, retries, or headers.
  - Simpler data sources and tests, with one fewer dependency each.
- **Cons:**
  - Authentication becomes implicit. Reading a data source no longer shows where the key comes from. The qualifier name and a KDoc on the interceptor offset this.
  - A shared client that adds a secret could leak it if it were ever used for another host. The host check prevents that.
  - Slightly more DI wiring (a qualifier) in a plan that is otherwise about user-visible bugs.
  - Nothing visible changes, so the evidence is the step 3 repros passing again, plus logcat showing key-free URLs and successful responses.

### 4. The chart button offers the previous distance's elevation
- **Finding:** `elevation/.../presentation/ElevationViewModel.kt:58-66` resets only `showChart` on the offline, NoData, Rejected and failure paths. `elevation` keeps the old profile, so `showChartFab = elevation != null && !showChart` shows a chart for a different line. Skill: kotlin-concurrency-and-flow, "Finish when cancellation, restart, replay, and failure behavior are all observable". This stays inside `ElevationViewModel`; the deferred three-owner chart cleanup is not touched.
- **Fix:** set `elevation = null` on every path that doesn't end in `Found`, including the `onError` of `launchCatching`.
- **Tests (`ElevationViewModelTest`):** offline, NoData, Rejected and an exception each clear `elevation`.
- **Repro:** draw a distance, close the chart, turn on airplane mode, then draw a new distance. Before: the chart button reappears with the old profile. After: no button.

### 5. FAQ answers collapse when scrolled away or when the Activity is recreated
- **Finding:** `faq/.../screen/FaqScreen.kt:96` uses `var expanded by remember`. `android:configChanges` in the faq, about and opensource manifests (line 23) hides the rotation case. Dark mode, locale, font scale and process death still recreate the Activity. Skills: adaptive 6.1, "Never add `android:configChanges`", and compose-agent, "Prefer `rememberSaveable`".
- **Fix:**
  - `rememberSaveable`.
  - Give the FAQ `items` a stable `key`, so the saved state follows the item.
  - Remove `configChanges` from the three feature manifests.
- **Repro:** expand an answer, scroll it off screen and back, and toggle dark mode with `adb shell cmd uimode night yes`. Before: it collapses. After: it stays open. Also check that the About and OpenSource screens look right after rotating.

### 6. `configChanges` on MainActivity (the riskiest step, can be dropped)
- **Finding:** `app/src/main/AndroidManifest.xml:59` (`orientation|keyboardHidden|screenSize`). Same adaptive rule.
- **Fix:** remove it. The `ViewModel`, `rememberSaveable` and `rememberCameraPositionState` should already hold the state.
- **Check:** rotate with markers, a distance, the open chart, the open rail and the search field. The map should keep its camera and markers, and nothing should re-prompt or zoom to the location again. If anything is lost, I'll report it and stop rather than patch around it.
- **[long-press]**

### 7. Accessibility labels
- **Findings:**
  - The ShowInfo back button has `contentDescription = null` (`show-info/.../ShowInfoScreen.kt:123`).
  - The rail's "Close Navigation" is hard-coded English (`app/.../components/AppNavigationRail.kt:225`).
  - The ShowInfo loading spinner sits in `IconButton(onClick = {})` (`:137`), so TalkBack announces a button that does nothing.
  - The elevation chart's close button is hard-coded English, `android:contentDescription="Close elevation chart"` (`elevation/src/main/res/layout/view_elevation_chart.xml:38`). Found while testing step 3; added on 2026-11-22 at your request.
  - Skills: compose-agent, "`null` is a valid value only if the icon is purely decorative"; material-3 accessibility; the CLAUDE.md rule on locales.
- **Fix:**
  - New strings `showinfo_navigate_back` (reusing the `faq_navigate_back` translations), `navigation_rail_close` and `elevation_chart_close` (in `:elevation`, used by the layout), each in en, ca, de, es, fr, it and pt.
  - Put the spinner in a `Box` sized like the icon buttons.
- **Check:** `uiautomator dump` shows the `content-desc` values before and after.

### 8. Crashes when no app can open a link or an email
- **Finding:** `about/.../AboutActivity.kt:51,57` (`ACTION_VIEW`, `ACTION_SENDTO` mailto) and `opensource/.../OpenSourceLicensesActivity.kt:84` don't catch `ActivityNotFoundException`. `feedback/.../RateAppFallbackDialogFragment.kt:52` already does. Skill: android-intent-security, error handling.
- **Fix:**
  - Wrap each `startActivity` in `try/catch (ActivityNotFoundException)` and show a toast.
  - Reuse an existing "no app" string if `:feedback` or `:common` has one. Otherwise add one in all 7 locales.
- **Repro:** on the emulator, `adb shell pm disable-user` the browser and Gmail, then tap the About links. Before: a crash. After: a toast. Re-enable both afterwards.

### 9. The share text links through goo.gl over http
- **Finding:** `show-info/.../mapper/ShareInfoMessageMapper.kt:48` (`http://goo.gl/0IBHFN`, also in `README.md:106`). Google has wound down the goo.gl shortener. Skill: deprecation-and-migration.
- **Fix:** use `https://play.google.com/store/apps/details?id=gc.david.dfm` and update `ShareInfoMessageMapperTest`.
- **Before editing:** I'll ask before checking whether the short link still resolves (a single request to goo.gl), since that's an external call.

## Group 2: Compose performance and UI

### 10. The whole map recomposes on every frame of the rail animation
- **Finding:** `app/.../screen/MainScreen.kt:227` reads the animated `fabOffsetX` during composition (`.offset(x = -fabOffsetX)`). So the Scaffold content lambda recomposes every frame. Each time it builds new `MapProperties` and `MapUiSettings` (`:142-150`), which aren't stable, so `MapContent`, `GoogleMap`, the markers and the polylines all recompose. Skills: compose-performance, "Read frame-rate state in layout or draw", and android jetpack-compose-performance, "Wrap animated offsets in `Modifier.offset { IntOffset(...) }`".
- **Fix:**
  - `Modifier.offset { IntOffset(-fabOffsetX.roundToPx(), 0) }`, keeping `fabOffsetX` as `State` (`val fabOffsetX = animateDpAsState(...)`, read as `.value` inside the lambda).
  - `remember(isMyLocationEnabled) { MapProperties(...) }`.
  - A private file-level `val MainMapUiSettings = MapUiSettings(...)`.
- **Evidence:** Android Studio Layout Inspector recomposition counts for `MapContent` while opening the rail, before and after (or a temporary `SideEffect` log, not committed).
- **[long-press]**, since the timing of map recomposition is the main suspect in the stray-tap issue.

### 11. Rail animations and the dead `isExpanded` flag
- **Findings:**
  - The scrim fade, rail slide and FAB offset (`MainScreen.kt:111,174,195`) follow one boolean as three independent animations. Skill: compose-animations, "define them on one transition".
  - `AppNavigationRail.isExpanded` is always `true` (the only caller is `MainScreen.kt:204`), and the 256dp width is duplicated at `MainScreen.kt:112`. Skill: compose-component-design, Boolean shape flags.
- **Fix:**
  - `val railTransition = updateTransition(isRailVisible)`, then `railTransition.AnimatedVisibility` for the scrim and the rail, and `railTransition.animateDp` for the FAB offset, read in the lambda from step 10.
  - Drop `isExpanded` and its collapsed branch and `animateContentSize`.
  - Share one `RailWidth` constant.
- **Evidence:** a recording of opening and closing the rail.

### 12. The loading animation stops after 10 loops
- **Finding:** `app/.../components/LottieProgress.kt:53` uses `repeatCount = 10` in an `AndroidView`, while `lottie-compose` (`app/build.gradle.kts:144`) is unused.
- **Fix:**
  - Use `rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.hand_animation))` and `LottieAnimation(composition, iterations = LottieConstants.IterateForever, modifier = Modifier.size(300.dp))`.
  - Drop `implementation(libs.lottie)` and its catalog entry if nothing else uses `LottieAnimationView`.
- **Repro:** throttle the network (`adb shell cmd connectivity` or emulator network delay) and search. Before: the hand stops moving after about 10 loops. After: it keeps going.

### 13. Effect keys and captured callbacks
- **Findings:**
  - `app/.../components/ElevationChart.kt:51` captures the first `onClose` in the `AndroidView` factory. Skill: compose-state-and-effects, "read a `rememberUpdatedState` value lazily".
  - The info-window `LaunchedEffect(shouldShowInfoWindow)` in `MapContent.kt:159` doesn't restart when the title of the same marker changes, so an open info window keeps the old text. Skill: "Key an effect by the semantic input that should restart or dispose it". Medium confidence: I'll first reproduce stale text by loading a saved distance whose end point matches the current one. If I can't reproduce it, I'll skip this half.
- **Fix:**
  - `val currentOnClose by rememberUpdatedState(onClose)`.
  - `LaunchedEffect(shouldShowInfoWindow, title)`.
- **[long-press]** for the `MapContent` half.

### 14. Insets: the ShowInfo top bar and the main FABs
- **Findings:**
  - `show-info/.../ShowInfoScreen.kt:117` overrides `TopAppBar(windowInsets = WindowInsets.statusBars)`, which drops the side insets. In landscape with 3-button navigation, Share, Refresh and Save sit under the navigation bar. Skill: edge-to-edge, "Material 3 Components manages safe areas for its own components".
  - The FAB column (`MainScreen.kt:228`) uses `navigationBarsPadding()`, which ignores the display cutout in landscape. Skill: edge-to-edge, `safeDrawingPadding`.
  - *Added on 2026-11-22 at your request, from the step 11 tablet screenshots:* the side menu header (`AppNavigationRail.kt`, `NavigationRailHeader`) looks wrong in landscape. The green header image starts about 56 px from the left edge, leaving a white strip, and the status bar area above the rail is plain gray instead of see-through. Likely cause: `NavigationRail` applies its default `windowInsets` (system bars and display cutout) as padding around its header and items, so the header image is inset while only the plain container colour reaches the edges. To confirm on the emulator first (landscape, both rotations, phone and tablet size).
- **Fix:**
  - Remove the override.
  - Use `windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.End))` on the FAB column.
  - Rail header: let the header image draw edge to edge, behind the status bar and the cutout, and pad only its content (logo, title, close button) and the items with the insets. For example `NavigationRail(windowInsets = WindowInsets(0))` plus `windowInsetsPadding` where it's needed.
- **Evidence:** screenshots in landscape on API 36 with 3-button navigation (`adb shell cmd overlay enable com.android.internal.systemui.navbar.threebutton`, restored afterwards) and a cutout device frame. For the rail header, before/after screenshots of the open side menu in both landscape rotations, also at tablet size (`adb shell wm size 1600x2560` and `wm density 320`, reset afterwards).

### 15. Material 3 component use
- **Findings:**
  - `Modifier.clickable` on cards (`FaqScreen.kt:98`, `OpenSourceListScreen.kt:141`, `AboutScreen.kt:176`): the ripple isn't clipped to the shape and there's no button role.
  - About link rows are about 32dp tall (`AboutScreen.kt:262`); the rule is 48dp.
  - `LinkAction` takes either `url` + `onOpenLink` or `onClick` (`AboutScreen.kt:249`).
  - `OutlinedTextField` gets `TextFieldDefaults.colors` (`TransparentSearchBar.kt:125`).
  - The OpenSource screens have no `modifier` parameter (`OpenSourceListScreen.kt:65`, `OpenSourceDetailScreen.kt:50`).
  - Lazy lists have no `key` or `contentType` (`DistanceSelectionDialog.kt:65` mixes two item types; `AddressSuggestionsDialog.kt:50` has an unused index; `OpenSourceListScreen.kt:111`).
  - Skills: material-3, compose-component-design, jetpack-compose-performance.
- **Fix:**
  - The `Card(onClick = …)` overload.
  - `Modifier.minimumInteractiveComponentSize()` on the link rows.
  - `LinkAction(onClick)`.
  - `OutlinedTextFieldDefaults.colors`.
  - `modifier: Modifier = Modifier` on the root `Scaffold`.
  - `key`s and `contentType` on the lazy lists.
- **Evidence:** screenshots of FAQ, About and OpenSource with a pressed-state ripple. I might split this step into two commits (cards and touch targets; API cleanups) if the diff gets large.

### 15b. Large screens: an always-visible collapsed rail (added on 2026-11-22 at your request)
- **Context:** step 11 removed `AppNavigationRail.isExpanded` because its only caller always passed `true`, so the 72 dp collapsed width was never reachable. The app has no window-size handling (no window size classes, no `sw600dp` resources). On a tablet (checked at 800×1280 dp by resizing the emulator) the side menu opens as a phone-style overlay, and the location button slides 256 dp left into the middle of the screen.
- **Idea:** on medium and expanded widths, show the rail permanently in its collapsed form (72 dp, icons only), next to the map rather than over it. The menu button, or the rail's own toggle, expands it to 256 dp with labels. Compact widths keep today's overlay.
- **Work:**
  - Bring back the collapsed variant, designed for this layout: an `isExpanded` (or a two-value enum) state owned by `MainScreen`, labels and the header title only when expanded, and a width animated by the same rail transition as step 11.
  - Choose the layout from `currentWindowAdaptiveInfo().windowSizeClass` (material3-adaptive). Skill: android adaptive.
  - With a permanent rail, the map takes the remaining width, so the FABs no longer need the 256 dp offset there.
  - Keep `rememberSaveable` state for expanded or collapsed, so it survives rotation and resizing.
- **Check:** screenshots and a recording at phone size, at tablet size in both orientations (`adb shell wm size 1600x2560` and `wm density 320`, reset afterwards), and of resizing between them.
- **Note:** this is a new feature, not a cleanup. It touches the same files as steps 10, 11 and 14, so it goes after them.

### 16. Investigate code organisation: packages and modules (last step, added 2026-11-22 at your request)
- **Goal:** a read-only survey of whether code would be better placed in other packages or new modules. It ends with a written proposal for you to approve or trim, not with code changes. Any approved move becomes its own follow-up plan, one commit per move.
- **What I'll look at:**
  - **`:common`**, which has grown into a grab bag. It holds Android services (`ConnectionManager`, `PermissionChecker`), the Google API networking (`GoogleApiKeyInterceptor`, `getGoogleApiJson`, `googleApiJson`, `HttpException`), distance maths (`DistanceCalculator`, `UnitConverter`), presentation helpers (`DistanceFormatter`, `ResourceProvider`) and extensions. Some classes also sit in the root `gc.david.dfm` package instead of `gc.david.dfm.common` (`GeocodeApiKeyProvider`, `ConnectionManager`, `PermissionChecker`). Candidates: a `:network` or `:google-api` module, a `:location` or `:permissions` split, and package moves.
  - **`:app`**, which owns `MainActivity`, `MainViewModel` (15 dependencies), the location and distance-mode repositories, the coordinates repository and the onboarding. The question: should any of these be a feature module, for example `:location` or `:map`?
  - **Feature modules:** layering consistency (`data/`, `domain/`, `presentation/`, `di/`), where shared UI lives (`:design-system`), and naming that drifts. For example, `GeocodeApiKeyProvider` also serves the Elevation API.
  - **The module graph:** unnecessary dependencies (`implementation` vs `api`), and modules that could become plain JVM/Kotlin modules for faster builds and tests.
- **Method:** read the module graph from the `build.gradle.kts` files and run `./gradlew :app:dependencies` where useful. List package contents, and check the Konsist guards so any proposal stays compatible with them, or comes with guard changes. Skills: `agent-skills:api-and-interface-design` (module boundaries), `chrisbanes-skills:kotlin-api-design` (ownership), `agent-skills:code-simplification`.
- **Output:** a short report with each candidate move: what moves where, why, the cost (files touched, DI and Konsist changes, risk), and a recommendation (do, maybe, don't). It also gives a suggested order.

## Verification

After each step:
- `./gradlew :<module>:testDebugUnitTest` for the touched modules, plus `./gradlew :support:konsist:test`.
- Run the step's repro on the Pixel_4a_API_36 emulator, before (on the parent commit) and after, and send the evidence.
- For **[long-press]** steps: you do 10 long presses in any-position mode while I read `adb logcat -d`. No `onMapClick` should follow within 2 s.

At the end:
- `./gradlew testDebugUnitTest :support:konsist:test assembleDebug :app:lintDebug`. Lint is expected to fail only on the old `MainScreen.kt:129` error.
- Update `BRANCH_COVERAGE_REPORT.md` and the README Testing section for the new tests.
- Record the progress in the `konsist-cleanup-series` memory, committed separately when you ask.

## Deferred (not in this plan)

- **Your investigation:** chart visibility with three owners, and the MainActivity click → flag → navigation relays. Step 4 only fixes the stale `elevation` field inside `ElevationViewModel`.
- **Group 3, Kotlin and architecture:**
  - `observeStoredDistances` has no `catch`, so a Room error crashes the app (`MainViewModel.kt:145`).
  - `ShowInfoViewModel.load` has no owner job, so a refresh can run twice.
  - `CoordinatesRepository.setList` should become an atomic `replaceAll`.
  - `SaveDistanceViewModel` should take its inputs in `onSave` and use an injected clock.
  - One shared `OkHttpClient` with a `callTimeout`.
  - Crashlytics non-fatals for Rejected API statuses; `ReleaseTree` drops the message when there's a throwable.
  - Slim `MainViewModel` from 15 to about 11 dependencies with a `DistancePlotMapper`.
  - `DistanceEntityMapper` should use `requireNotNull(entity.id)`.
  - Smaller items: the `UnitConverter` Pair, the stuck `isUserSelectingPoints` flag.
- **Tests:**
  - `UnitConverter` and `DistanceCalculator` have no tests.
  - `MainViewModelTest` never covers drawing a distance; use the real in-memory repositories there.
  - Two `SaveDistanceViewModelTest` tests have weak assertions.
  - Kover or JaCoCo would replace the hand-kept coverage numbers.
  - Compose UI tests and `androidTest`; the in-memory Room transaction test.
- **Group 4, platform, security and build:**
  - Predictive back (`enableOnBackInvokedCallback`).
  - Backup `dataExtractionRules` for the distances DB.
  - R8 cleanup: the stale `android.support` keeps, the broad Exception keep, `-dontwarn okhttp3`, `optimizedResourceShrinking`.
  - Deprecations: `Icons.Default.ShowChart`, the LiveData dependencies, `firebase-crashlytics-ktx`, `kotlin-parcelize`, Jetifier.
  - `MapsInitializer` on the startup path.
  - Explicit `exported` in the feature manifests.
  - `:app` failing to configure without `keystore.properties`.
  - A GitHub Actions CI workflow.
  - The lint error at `MainScreen.kt:129`.
  - The AGP 9 upgrade, which needs the AGP Upgrade Assistant run first.
  - `MAPS_RECEIVE` and `org.apache.http.legacy`.
- **Your checks (consoles):**
  - Restrict the geocoding and elevation key, which ships in the APK, with quotas.
  - Confirm the 2013 Maps keys in the git history are dead.
  - Play Data Safety: location and crash logs; a privacy policy link.
- **Compose items left out:**
  - Dark theme and dynamic color: hard-coded White and Black in `TransparentSearchBar` and `DistanceSelectionDialog`.
  - Type-safe Navigation routes in OpenSource.
  - Moving the share and toast effects out of `ShowInfoScreen` for testability.
  - The Android 12+ splash icon, which is a bitmap.
  - A maximum list width on tablets.
- **The stray-tap issue** after long presses stays open. The [long-press] checks above will add data points, and step 10 is the most likely to change it.
