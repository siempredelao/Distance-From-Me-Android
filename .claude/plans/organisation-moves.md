# Code organisation moves (follow-up to step 16 of iridescent-bouncing-squirrel)

Approved on 2026-11-22: moves 1 to 4 of the step 16 proposal, the Google API module (named `:geocoding`), and dropping Material Components from `:common`. One commit per move, in this order, on `develop`. Before each move: explain it, then change it. After each move: build, unit tests, Konsist guards, and an emulator check where behaviour could change. Commit only when the user says so.

Every move keeps the Konsist layer rules (`domain`, `data`, `presentation` and `di` package names, no Android framework in `domain`, packages match directories). Where a guard names a class, the move updates the guard in the same commit.

## 1. Remove dead and duplicate code, fix CLAUDE.md
- Delete `app/.../main/presentation/animation/AnimatorUtil.kt` (no callers).
- Delete `app/.../LifecycleExt.kt`; `MainActivity` imports `gc.david.dfm.common.collectOnStarted` instead.
- Remove `UiUtils.dumpBundleToString`; `MainActivity` logs `savedInstanceState` directly (a null bundle logs as `null` instead of `bundle is null`).
- CLAUDE.md: drop "the geofencing location service" from the `:app` description.
- Cost: about 5 files. No DI or Konsist changes.
- Check: build, tests, Konsist; launch the app once.

## 2. One `Coordinates` type
- Delete `address/.../domain/model/Coordinates.kt`; `:address` uses `gc.david.dfm.common.Coordinates` (it already depends on `:common`).
- Update `:address` main and test files (about 15), and the aliased imports and conversions in `MainActivity`, `AddressSuggestionsDialog` and `ShowInfoViewModel`.
- Cost: about 19 files. No DI or Konsist changes.
- Check: build, tests, Konsist; on the emulator, one address search (suggestions dialog or direct move) and Show info with its address. The Geocoding API rate-limits, so one run per build.

## 3. Tidy `:common` packages
- Interfaces to `gc.david.dfm.common.domain`: `ConnectionManager`, `PermissionChecker`, and `CoordinatesRepository` (now in `gc.david.dfm.distance.domain`, a package `:app` also uses).
- Android implementations to `gc.david.dfm.common.data`: `DefaultConnectionManager`, `DefaultPermissionChecker`. `SystemServiceExt` becomes internal to `:common`, next to its one caller.
- The API key provider is left for move 5, which moves and renames it in one go, so it isn't touched twice.
- No class remains in the root `gc.david.dfm` package of `:common`, and no package is split between `:common` and `:app`.
- Cost: about 13 files (imports in `:address`, `:app`, `:elevation`, `:show-info`, and tests). No Konsist change expected; the guards confirm the new layering.
- Check: build, tests, Konsist; launch the app once.

## 4. New `:core-settings` module
- New Android library `:core-settings`, packages `gc.david.dfm.core.settings.{data,domain,domain.model,di}`, following `:core-distances`.
- Moves from `:settings`: `SettingsRepository`, `CameraAnimation`, `BaseSettingsRepository`, `SettingsDiskDataSource` and `BaseSettingsRepositoryTest`. `:settings` keeps the screen (`SettingsActivity`, `SettingsFragment`, `SettingsViewModel`, `settings.xml`).
- Koin: the three repository bindings move from `settingsModule` to a new `coreSettingsModule`, registered in `DFMApplication`.
- Dependencies: `:elevation` and `:show-info` replace `:settings` with `:core-settings`; `:app` and `:settings` add `:core-settings`. `:core-settings` needs `androidx.preference` for `PreferenceManager`, `:common` for `UnitSystem`, and the usual test setup.
- The preference keys (`unit`, `elevation_chart`, `animation`) stay as literals in both `SettingsDiskDataSource` and `settings.xml`, as today; after the move they live in two modules. A comment on each side points to the other.
- Cost: a new build file and `settings.gradle.kts` entry, about 16 files with changed imports, CLAUDE.md module graph.
- Check: build, tests, Konsist; on the emulator, change the unit and the elevation chart setting, then check a distance label and the chart follow them.

## 5. New `:geocoding` module (Google Maps web services client)
- New Android library `:geocoding`, packages `gc.david.dfm.geocoding.{data,domain,di}`.
- Moves from `:common`: `GoogleApiKeyInterceptor`, `getGoogleApiJson`, `googleApiJson`, `HttpException`, the `GOOGLE_API_CLIENT` qualifier and its `OkHttpClient` binding (into a new `geocodingModule`, registered in `DFMApplication`), and their tests.
- `GeocodeApiKeyProvider` and `DefaultGeocodeApiKeyProvider` move here under their current names (the user's call on 2026-11-23), both `internal`. The Konsist guard for remote data sources keeps naming `GeocodeApiKeyProvider`.
- The key moves with its only reader (the user's call): `maps_geocode_api_key` is the only string in `:common`'s gitignored `common/src/{main,debug,release}/res/values/other.xml`, and `DefaultGeocodeApiKeyProvider` is its only reader. Move the three local files to `geocoding/src/{main,debug,release}/res/values/` with `mv` (they're untracked, so git sees nothing; never print their contents).
  - `.gitignore`: replace `/common/src/main/res/values/other.xml` with `/geocoding/src/main/res/values/other.xml`; the `*/src/debug/...` and `*/src/release/...` patterns already cover the new module. Check with `git check-ignore -v` on all three new paths and `git status` before committing, so no key file gets staged.
  - CLAUDE.md: the API key section names `geocoding/src/{main,debug}/res/values/other.xml` (and release). Other clones and machines must move their files the same way; the move 5 report says so.
  - `:geocoding` then doesn't need `:common` for the key; it depends on `:common` only if moved code still uses something there.
- `Double.toPlainString()` stays in `:common` (it isn't specific to the web services).
- Dependencies: `:address` and `:elevation` add `:geocoding`; `:common` drops OkHttp, okhttp-coroutines and kotlinx-serialization if nothing else in it uses them.
- Naming note: the Elevation API also goes through this client, so the module covers more than geocoding. The name was the user's choice.
- Cost: a new build file, about 15 files, a Koin module, one guard, `.gitignore`, CLAUDE.md, and three local key files moved.
- Check: build, tests, Konsist; on the emulator, one address search and one elevation chart (few API calls).

## 6. Drop Material Components from `:common`
- `:common` declares `material.components` only for its `Toolbar` style (`ThemeOverlay.AppCompat.Dark.ActionBar`), used only by `:settings`' `activity_settings.xml`. Move the style to `:settings` (which already declares Material Components), delete `:common`'s `styles.xml` if empty, and remove the dependency.
- Cost: 3 files.
- Check: build (resource linking proves nothing else needs it); screenshots of the Settings toolbar before and after.

## Final
- Update the README and CLAUDE.md module lists if they still differ, and the memory file.
