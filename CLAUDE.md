# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

*Distance From Me* is an Android app (package `gc.david.dfm`) for calculating straight-line distances on a Google Map, with geocoding search, elevation profiles, and saved distances. Kotlin, Jetpack Compose, Coroutines/Flow, Koin, and Room. It is a multi-module Gradle project organised by feature and follows MVVM + Clean architecture.

## Commands

```bash
./gradlew assembleDebug                      # build debug APK (versionNameSuffix "-dev")
./gradlew installDebug                       # install on a connected device or emulator
./gradlew testDebugUnitTest                  # all unit tests, all modules (JVM modules alias it to `test`)
./gradlew :features:address:testDebugUnitTest # one Android module (JVM modules: ./gradlew :core:units:test)
./gradlew :app:testDebugUnitTest --tests "gc.david.dfm.main.presentation.MainViewModelTest"          # one class
./gradlew :app:testDebugUnitTest --tests "gc.david.dfm.main.presentation.MainViewModelTest.someTest*" # one method (pattern)
./gradlew :tools:konsist:test                # Konsist architecture and convention guards (JVM module, not part of testDebugUnitTest)
./gradlew lint                               # Android lint (app/lint.xml is empty, so defaults apply)
```

Build types are `debug` and `release`. `release` needs `keystore.properties` at the repo root. It is gitignored and holds `storeFile`, `storePassword`, `keyAlias`, and `keyPassword`. The `release` signing config reads it unconditionally.

API keys live in gitignored `other.xml` string resources: `maps_api_key` in `app/src/main/res/values/other.xml`, and `maps_geocode_api_key` in `core/geocoding/src/{main,debug,release}/res/values/other.xml`. A fresh clone won't build until these exist.

SDK levels, app `versionCode`/`versionName`, and all dependency versions are in `gradle/libs.versions.toml`. A release bump means editing `appVersionCode` and `appVersionName` there.

Shared build setup lives in the `build-logic/` included build. Every module applies these convention plugins instead of configuring them itself; `BuildConventionsKonsistTest` keeps it that way:
- `dfm.android.application`, `dfm.android.library`: AGP + Kotlin, SDK levels, Java 11 and the JVM target.
- `dfm.android.compose`: the Compose compiler, `buildFeatures.compose`, the Compose BOM and preview tooling.
- `dfm.jvm.library`: a plain Kotlin JVM module with the same targets and JUnit 5, plus a `testDebugUnitTest` task that runs `test`.
- `dfm.unit-test`: JUnit 5, Mockito, `kotlinx-coroutines-test`, and `:support:test` in Android modules.

## Architecture

### Module graph
Modules are grouped by role, and each group may only depend inwards. `ModuleGroupsKonsistTest` checks both imports and `project(...)` dependencies:

| Group | Holds | May depend on |
|---|---|---|
| `:features:*` | screens and their ViewModels | `:core`, `:support` (never another feature) |
| `:core:*` | domain and data shared by features | `:core`, `:support` |
| `:support:*` | technical helpers with no app domain | `:support` |
| `:tools:*` | build-time checks | nothing |

`:app` sits outside the groups and depends on all of them. It owns `MainActivity` (the Compose map screen), `OnboardActivity`, `AppNavigator`, the location and distance-mode code, `PermissionChecker` and `BuildConfigProvider`.

- Features: `:features:about`, `:features:address` (only the search `AddressViewModel`), `:features:connectivity`, `:features:elevation`, `:features:faq`, `:features:feedback`, `:features:opensource`, `:features:settings`, `:features:show-info`.
- `:core:address`: geocoding and reverse geocoding (remote data source, repository, use cases, `GeocodingResult`) and `GeocodingErrorMessageMapper` with its strings. Used by `:features:address`, `:features:show-info` and `:app`.
- `:core:coordinates` (JVM): `Coordinates`, `DistanceCalculator`, and the `CoordinatesRepository` backed by one in-memory list of points, which the map writes and Show info reads.
- `:core:design-system`: the Compose theme, shared UI pieces and the launcher icon (orange in `src/main`, overridden by the green one in `src/release`).
- `:core:distances`: the Room database (`DFMDatabase`) plus the distance and position use cases. Room schemas are exported to `core/distances/schemas/`. Commit the new schema JSON when you change entities.
- `:core:geocoding`: the client for the Google Maps web services (Geocoding and Elevation): the `GOOGLE_API_CLIENT` `OkHttpClient`, whose interceptor adds the API key, plus `getGoogleApiJson`, `googleApiJson`, `HttpException` and `toPlainString` for query values. Its key provider, `GeocodeApiKeyProvider`, is internal.
- `:core:settings`: the settings repository (`SettingsRepository`, backed by the default `SharedPreferences`) that the map, elevation and Show info screens read. `:features:settings` is only the screen that edits those preferences, through `res/xml/settings.xml`, so keys changed on one side must change on the other.
- `:core:units` (JVM): `UnitSystem`, `UnitConverter` and `UnitFormatter`.
- `:support:android`: thin wrappers over the Android framework: `ResourceProvider`, `ConnectionManager`, `tryStartActivity`, `collectOnStarted`, `UiUtils` and their toast strings.
- `:support:coroutines`: `launchCatching`, `suspendRunCatching`, `logUnexpectedFailure` and the IO `CoroutineDispatcher` binding.
- `:support:navigation`: `Navigator` and `Route` (see Navigation).
- `:support:test`: `CoroutineExtension`, the JUnit 5 extension that swaps `Dispatchers.Main`.
- `:tools:konsist`: a plain Kotlin JVM module whose tests use Konsist to parse every module's sources and enforce the conventions in this file: module groups, build conventions, layer dependencies, use case and ViewModel shape, repository naming, Koin registration and navigator scope, Activity `open` functions, accessibility labels, license headers, and test presence. When you add a convention, add a guard there too.

A new module that only needs Kotlin, the JDK, coroutines or Koin core is a JVM module (`dfm.jvm.library`, with `libs.coroutines.core` rather than the Android `libs.coroutines`). Anything that touches the Android framework, resources, ViewModels or Compose is an Android library (`dfm.android.library`). A JVM module can't depend on an Android library.

### Navigation
Each screen is its own Activity, launched through a `companion object { fun open(activity) }`. There is no Navigation component.

Screens in another module are opened through `:support:navigation`, so features never import each other:
- `Route` is a sealed interface of destinations (`About`, `Faq`, `OpenSourceLicenses`, `Settings`, `ShowInfo`); a destination that needs arguments becomes a data class.
- `navigator.navigate(Route.Settings)`; the caller gets its navigator with `private val navigator: Navigator by inject { parametersOf(this) }`.
- `:app`'s `AppNavigator(activity)` maps each route to its Activity's `open`, so a new route doesn't compile until it has a branch there.
- Koin creates a navigator per Activity (`factory`), because it holds that Activity. `DependencyInjectionKonsistTest` forbids binding it as a `single`.
- If the routes grow well beyond a handful, reconsider this setup (see the comment on `Route`).

### Layering inside a feature module
Packages are `data/` → `domain/` → `presentation/` → `di/`:
- `data/`: `*RemoteDataSource` (OkHttp calls to Google Geocoding/Elevation APIs, parsed with kotlinx.serialization using the shared `googleApiJson` from `:core:geocoding`, on the injected IO `CoroutineDispatcher`) or `*DiskDataSource`. `Base*Repository` implements the domain `*Repository` interface. `data/mapper/*EntityDataMapper` turns entities into domain models.
- `domain/`: repository interface, domain models, and `*UseCase` classes with a single `operator fun invoke(...)`, either `suspend` and returning a value or non-suspending and returning a `Flow`. Model outcomes the caller is expected to handle as a domain sealed type (for example `GeocodingResult` in `:core:address`) rather than `kotlin.Result`. Let unexpected failures (network, parsing, bugs) propagate: ViewModels launch with `launchCatching` from `:support:coroutines`, and `suspendRunCatching` is only for turning a failure into a value where it must not cancel sibling calls. Never swallow `CancellationException`: rethrow it from any broad catch.
- `presentation/`: a `ViewModel` that exposes `StateFlow<*UiState>`, UI mappers, and Compose `screen/`. `:features:settings` still uses a preference `Fragment`, and `:features:elevation` uses a GraphView-based `ElevationChartView`.
- `di/`: a Koin `module { }` (for example `addressModule`). **Every new Koin module has to be added to `startKoin { modules(...) }` in `app/.../DFMApplication.kt`.** Constructor injection uses positional `get()` calls, so a changed constructor signature means updating the matching `get()` count in the module.

App startup work (Firebase, Timber, Maps SDK, default unit) goes through `Initializer`s, which are registered in `appModule` and run from `DFMApplication.onCreate`.

## Testing conventions
- JUnit 5 (Jupiter) on every module via `useJUnitPlatform()`, with Mockito + mockito-kotlin (`mock<>()`, `whenever`) and `kotlinx-coroutines-test` (`runTest`).
- ViewModel tests register `@JvmField @RegisterExtension val coroutineExtension = CoroutineExtension()`.
- A module with unit tests applies the `dfm.unit-test` convention plugin, which adds JUnit 5, Mockito and `kotlinx-coroutines-test`, plus `:support:test` in Android modules. JVM modules can't use `:support:test`, an Android library, and have no Main dispatcher to swap.
- The project aims for a test for every UseCase, Repository, Mapper, Formatter, and ViewModel. `BRANCH_COVERAGE_REPORT.md` and the Testing section of `README.md` track coverage by hand, so update them when coverage changes significantly.

## Prepare for release
When the user asks to prepare a release, or to tag and merge one after its Play Store upload, read [RELEASING.md](RELEASING.md) first and follow its steps in order. Don't start either phase unasked.

## Known issues
- `MainActivity` leaks when it's destroyed before its map loads, for example a size, density or dark-mode change within about a second of launch. maps-compose 6.x waits for the map with a non-cancellable `awaitMap()`, so the window recomposer never shuts down (comment at the `GoogleMap` call in `MapContent.kt`). maps-compose 9 fixes it, but needs compileSdk 37 and AGP 9.1; that upgrade is planned separately.
- A release build uploads its mapping file to Firebase. Exclude `uploadCrashlyticsMappingFileRelease` for local builds that aren't a release, and remember that running such a build reports to the production Crashlytics project.

## Conventions
- Every source file starts with the Apache 2.0 license header, "Copyright (c) 2026 David Aguiar Gonzalez".
- Code style follows `code-style/settings.jar` (Android Studio code style export).
- Commit messages follow the style at https://chris.beams.io/posts/git-commit/ (imperative subject line). Work happens on `develop` and is merged into `master` for releases.
- Supported locales are en, ca, de, es, fr, it, and pt (`localeFilters` in `app/build.gradle.kts`). New user-facing strings need translations for all of them.
