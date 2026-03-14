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