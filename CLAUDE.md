# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

*Distance From Me* is an Android app (package `gc.david.dfm`) for calculating straight-line distances on a Google Map, with geocoding search, elevation profiles, and saved distances. Kotlin, Jetpack Compose, Coroutines/Flow, Koin, and Room. It is a multi-module Gradle project organised by feature and follows MVVM + Clean architecture.

## Commands

```bash
./gradlew assembleDebug                      # build debug APK (versionNameSuffix "-dev")
./gradlew installDebug                       # install on a connected device or emulator
./gradlew testDebugUnitTest                  # all unit tests, all modules
./gradlew :address:testDebugUnitTest         # one module
./gradlew :app:testDebugUnitTest --tests "gc.david.dfm.main.presentation.MainViewModelTest"          # one class
./gradlew :app:testDebugUnitTest --tests "gc.david.dfm.main.presentation.MainViewModelTest.someTest*" # one method (pattern)
./gradlew :support:konsist:test              # Konsist architecture and convention guards (JVM module, not part of testDebugUnitTest)
./gradlew lint                               # Android lint (app/lint.xml is empty, so defaults apply)
```

Build types are `debug`, `release`, and `beta` (`beta` is initialised from `release` and adds the "-beta" suffix). `release` and `beta` need `keystore.properties` at the repo root. It is gitignored and holds `storeFile`, `storePassword`, `keyAlias`, and `keyPassword`. The `release` signing config reads it unconditionally.

API keys live in gitignored `other.xml` string resources: `maps_api_key` in `app/src/main/res/values/other.xml`, and `maps_geocode_api_key` in `common/src/{main,debug}/res/values/other.xml`. A fresh clone won't build until these exist.

SDK levels, app `versionCode`/`versionName`, and all dependency versions are in `gradle/libs.versions.toml`. A release bump means editing `appVersionCode` and `appVersionName` there.

## Architecture

### Module graph
- `:app` depends on every feature module. It owns `MainActivity` (a Compose screen with the map), `OnboardActivity`, the geofencing location service, and the in-memory `CoordinatesRepository` implementation.
- Feature modules (`:address`, `:elevation`, `:faq`, `:opensource`, `:settings`, `:show-info`, `:about`, `:feedback`, `:connectivity`) depend on `:common` and sometimes `:core-distances` or `:design-system`. They don't depend on `:app`.
- `:common` holds cross-cutting abstractions: `ConnectionManager`, `PermissionChecker`, `GeocodeApiKeyProvider`, `BuildConfigProvider`, `ResourceProvider`, `DistanceCalculator`/`UnitConverter`/`DistanceFormatter`, and the `CoordinatesRepository` interface. Feature modules share state through these interfaces, and `:app` supplies the implementations. For example, `ShowInfoViewModel` reads the points the user drew through `CoordinatesRepository`, which is backed by `CoordinatesMemoryDataSource` in `:app`.
- `:core-distances` is the Room database (`DFMDatabase`) plus the distance and position use cases. Room schemas are exported to `core-distances/schemas/`. Commit the new schema JSON when you change entities.
- `:test-support` provides `CoroutineExtension`, the JUnit 5 extension that swaps `Dispatchers.Main`.
- `:support:konsist` is a plain Kotlin JVM module whose tests use Konsist to parse every module's sources and enforce the conventions in this file: layer dependencies, use case and ViewModel shape, repository naming, Koin registration, Activity `open` functions, license headers, and test presence. When you add a convention, add a guard there too.

### Navigation
Each screen is its own Activity, launched through a `companion object { fun open(activity) }` (for example `SettingsActivity.open(this)`, called from `MainActivity`). There is no Navigation component.

### Layering inside a feature module
Packages are `data/` → `domain/` → `presentation/` → `di/`:
- `data/`: `*RemoteDataSource` (OkHttp calls to Google Geocoding/Elevation APIs, parsed with kotlinx.serialization using the shared `googleApiJson` from `:common`, on the injected IO `CoroutineDispatcher`) or `*DiskDataSource`. `Base*Repository` implements the domain `*Repository` interface. `data/mapper/*EntityDataMapper` turns entities into domain models.
- `domain/`: repository interface, domain models, and `*UseCase` classes with a single `operator fun invoke(...)`, either `suspend` and returning a value or non-suspending and returning a `Flow`. Model outcomes the caller is expected to handle as a domain sealed type (for example `GeocodingResult` in `:address`) rather than `kotlin.Result`. Let unexpected failures (network, parsing, bugs) propagate: ViewModels launch with `launchCatching` from `:common`, and `suspendRunCatching` is only for turning a failure into a value where it must not cancel sibling calls. Never swallow `CancellationException`: rethrow it from any broad catch. Suspending use cases outside `:address` (in `:core-distances`, `:elevation`, `:faq` and `:opensource`) still return `kotlin.Result` from an earlier convention, catching exceptions themselves and rethrowing `CancellationException`.
- `presentation/`: a `ViewModel` that exposes `StateFlow<*UiState>`, UI mappers, and Compose `screen/`. `:settings` still uses a preference `Fragment`, and `:elevation` uses a GraphView-based `ElevationChartView`.
- `di/`: a Koin `module { }` (for example `addressModule`). **Every new Koin module has to be added to `startKoin { modules(...) }` in `app/.../DFMApplication.kt`.** Constructor injection uses positional `get()` calls, so a changed constructor signature means updating the matching `get()` count in the module.

App startup work (Firebase, Timber, Maps SDK, default unit) goes through `Initializer`s, which are registered in `appModule` and run from `DFMApplication.onCreate`.

## Testing conventions
- JUnit 5 (Jupiter) on every module via `useJUnitPlatform()`, with Mockito + mockito-kotlin (`mock<>()`, `whenever`) and `kotlinx-coroutines-test` (`runTest`).
- ViewModel tests register `@JvmField @RegisterExtension val coroutineExtension = CoroutineExtension()`.
- A new test module needs `testImplementation(project(":test-support"))`, the Jupiter api/params deps, `testRuntimeOnly(libs.junit.jupiter.engine)`, and the `unitTests.all { it.useJUnitPlatform() }` block.
- The project aims for a test for every UseCase, Repository, Mapper, Formatter, and ViewModel. `BRANCH_COVERAGE_REPORT.md` and the Testing section of `README.md` track coverage by hand, so update them when coverage changes significantly.

## Conventions
- Every source file starts with the Apache 2.0 license header, "Copyright (c) 2026 David Aguiar Gonzalez".
- Code style follows `code-style/settings.jar` (Android Studio code style export).
- Commit messages follow the style at https://chris.beams.io/posts/git-commit/ (imperative subject line). Work happens on `develop` and is merged into `master` for releases.
- Supported locales are en, ca, de, es, fr, it, and pt (`localeFilters` in `app/build.gradle.kts`). New user-facing strings need translations for all of them.
