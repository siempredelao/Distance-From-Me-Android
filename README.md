# Distance From Me
![Image](/app/src/main/res/drawable-xxhdpi/ic_launcher.png)

## What is it?
*Distance From Me* is an Android application whose objective is to calculate straight-line distances.
Code is organised by features in a multi-module Gradle project, following MVVM + Clean architecture.

## Features
* Searches places by keyboard and by voice.
* Calculates multi-points distances.
* Calculates distances from current position or from any position.
* Allows to show elevation profile.
* Loads and saves distances.
* Shares distances with friends.
* Shortcut to Play Store to rate app.
* Some FAQ.
* Languages: English, Spanish, French, German, Italian, Portuguese and Catalan.

## Modules
| Module | Description |
|---|---|
| `:app` | Main application module |
| `:address` | Address / geocoding feature |
| `:common` | Shared resources and utilities |
| `:connectivity` | Connection issues UI |
| `:core-distances` | Room database and distance domain |
| `:design-system` | Compose theme and shared UI components |
| `:elevation` | Elevation profile feature |
| `:faq` | FAQ / Help & Feedback feature |
| `:feedback` | In-app review dialog |
| `:opensource` | Open source licenses screen |
| `:show-info` | Distance detail and sharing screen |
| `:test-support` | Shared test utilities |

## Main libraries used
* [Jetpack Compose](https://developer.android.com/jetpack/compose)
* [Kotlin Coroutines & Flow](https://github.com/Kotlin/kotlinx.coroutines)
* [Koin](https://github.com/InsertKoinIO/koin)
* [Room](https://developer.android.com/topic/libraries/architecture/room)
* [OkHttp](https://square.github.io/okhttp/)
* [Gson](https://github.com/google/gson)
* [Material Components](https://github.com/material-components/material-components-android)
* [Lottie](https://github.com/airbnb/lottie-android)
* [Timber](https://github.com/JakeWharton/timber)
* [GraphView](https://github.com/jjoe64/GraphView)
* [LeakCanary](https://github.com/square/leakcanary)
* [Firebase Crashlytics](https://firebase.google.com/docs/crashlytics)
* [Mockito](https://site.mockito.org/)

## Testing

The project has comprehensive test coverage for all critical components:
- **100% file coverage** 🎉 - All ViewModels, Mappers, Repositories, Formatters, and UseCases have unit tests
- **~80% branch coverage** 🟡 - Most code branches (if/when/else) are tested

For detailed branch coverage analysis, see [BRANCH_COVERAGE_REPORT.md](BRANCH_COVERAGE_REPORT.md).

### Run all unit tests

```bash
# Run all module tests with visual progress feedback
cd /home/david/projects/Distance-From-Me-Android && printf "=== RUNNING ALL UNIT TESTS ===\n\n" && for module in core-distances common show-info address faq opensource elevation settings app; do printf "📦 Testing: %s\n" "$module"; ./gradlew :"$module":testDebugUnitTest --quiet 2>&1 > /dev/null && printf "✅ PASSED\n\n" || printf "❌ FAILED\n\n"; done && printf "=== TESTS COMPLETED ===\n"

# Or run tests for all modules at once (debug variant)
./gradlew testDebugUnitTest

# Run tests for specific module
./gradlew :core-distances:testDebugUnitTest
./gradlew :common:testDebugUnitTest
./gradlew :show-info:testDebugUnitTest
./gradlew :address:testDebugUnitTest
./gradlew :faq:testDebugUnitTest
./gradlew :opensource:testDebugUnitTest
./gradlew :elevation:testDebugUnitTest
./gradlew :settings:testDebugUnitTest
./gradlew :app:testDebugUnitTest
```

### Test Coverage Summary

#### File Coverage (All classes have tests)
- ✅ **UseCases**: 100% (9/9)
- ✅ **Mappers**: 100% (12/12)
- ✅ **Formatters**: 100% (1/1)
- ✅ **ViewModels**: 100% (8/8)
- ✅ **Repositories**: 100% (7/7)

#### Branch Coverage (Code paths tested)
- ✅ **UseCases**: ~100% - All if/when branches covered
- ✅ **Repositories**: ~100% - All if/when branches covered
- ✅ **Formatters**: ~100% - All formatting paths covered
- ✅ **Mappers**: ~98% - Almost all branches covered
- ✅ **ViewModels**: ~78% - Most flows fully tested

**Overall**: 100% file coverage, ~86% branch coverage. See detailed analysis in [BRANCH_COVERAGE_REPORT.md](BRANCH_COVERAGE_REPORT.md).

## Contributions
Contributions are welcome. Please read the [contributions guide](CONTRIBUTING.md) for more information. 

## Download
[![Get it on Google Play](https://developer.android.com/images/brand/en_generic_rgb_wo_60.png)](https://goo.gl/0IBHFN)

## License
```
   Copyright (c) 2026 David Aguiar Gonzalez

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```