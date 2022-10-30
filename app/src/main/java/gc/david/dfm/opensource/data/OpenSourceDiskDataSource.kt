/*
 * Copyright (c) 2022 David Aguiar Gonzalez
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gc.david.dfm.opensource.data

import gc.david.dfm.opensource.data.model.OpenSourceLibraryEntity
import kotlinx.coroutines.delay

/**
 * Created by david on 25.01.17.
 */
class OpenSourceDiskDataSource {

    companion object {

        private val SUPPORT_LIBRARY = OpenSourceLibraryEntity(
            "Support Library",
            "Serves a standard way to provide newer features on earlier versions of Android or gracefully fall back to equivalent functionality.",
            "The Android Open Source Project",
            "27.1.1",
            "https://developer.android.com/topic/libraries/support-library/index.html",
            "Apache-2.0",
            "2007-2017"
        )
        private val PLAY_SERVICES = OpenSourceLibraryEntity(
            "Google Play Services",
            "Take advantage of the latest, Google-powered features such as Maps, Google+, and more, with automatic platform updates distributed as an APK through the Google Play store.",
            "The Android Open Source Project",
            "17.0.0",
            "https://developers.google.com/android/guides/overview",
            "Apache-2.0",
            "2007-2017"
        )
        private val GRAPHVIEW = OpenSourceLibraryEntity(
            "GraphView",
            "Android Graph Library for creating zoomable and scrollable line and bar graphs.",
            "Jonas Gehring",
            "4.2.2",
            "http://www.android-graphview.org/",
            "Apache-2.0",
            "2016"
        )
        private val ROOM = OpenSourceLibraryEntity(
            "Room",
            "The Room persistence library provides an abstraction layer over SQLite to allow for more robust database access while harnessing the full power of SQLite.",
            "The Android Open Source Project",
            "2.2.0",
            "https://developer.android.com/topic/libraries/architecture/room",
            "Apache-2.0",
            "2016"
        )
        private val CRASHLYTICS = OpenSourceLibraryEntity(
            "Firebase Crashlytics",
            "The most powerful, yet lightest weight crash reporting solution.",
            "Google",
            "17.3.0",
            "https://firebase.google.com/docs/crashlytics",
            "Copyright",
            "2017"
        )
        private val KOIN = OpenSourceLibraryEntity(
            "Koin",
            "A pragmatic lightweight dependency injection framework for Kotlin developers.",
            "Arnaud GIULIANI, Laurent BARESSE",
            "2.2.2",
            "https://github.com/InsertKoinIO/koin",
            "Apache-2.0",
            "2017"
        )
        private val OKHTTP = OpenSourceLibraryEntity(
            "OkHttp",
            "An HTTP+HTTP/2 client for Android and Java applications.",
            "Square, Inc.",
            "3.11.0",
            "http://square.github.io/okhttp/",
            "Apache-2.0",
            "2016"
        )
        private val GSON = OpenSourceLibraryEntity(
            "Gson",
            "A Java serialization/deserialization library that can convert Java Objects into JSON and back.",
            "Google Inc.",
            "2.8.6",
            "https://github.com/google/gson",
            "Apache-2.0",
            "2008"
        )
        private val CONSTRAINT_LAYOUT = OpenSourceLibraryEntity(
            "ConstraintLayout",
            "ConstraintLayout allows you to create large and complex layouts with a flat view hierarchy (no nested view groups).",
            "The Android Open Source Project",
            "1.1.3",
            "https://developer.android.com/training/constraint-layout/index.html",
            "Apache-2.0",
            "2007-2017"
        )
        private val LEAK_CANARY = OpenSourceLibraryEntity(
            "LeakCanary",
            "A memory leak detection library for Android and Java.",
            "Square, Inc.",
            "1.6.3",
            "https://github.com/square/leakcanary",
            "Apache-2.0",
            "2015"
        )
        private val JUNIT = OpenSourceLibraryEntity(
            "JUnit",
            "JUnit is a simple framework to write repeatable tests. It is an instance of the xUnit architecture for unit testing frameworks.",
            "JUnit",
            "4.12",
            "http://junit.org/junit4/",
            "EPL-1.0",
            "2002-2017"
        )
        private val MOCKITO = OpenSourceLibraryEntity(
            "Mockito",
            "Most popular Mocking framework for unit tests written in Java.",
            "Mockito contributors",
            "2.28.2",
            "http://site.mockito.org/",
            "MIT",
            "2007"
        )

        private val OPEN_SOURCE_LIBRARIES = listOf(
            SUPPORT_LIBRARY,
            PLAY_SERVICES,
            GRAPHVIEW,
            ROOM,
            CRASHLYTICS,
            KOIN,
            OKHTTP,
            GSON,
            CONSTRAINT_LAYOUT,
            LEAK_CANARY,
            JUNIT,
            MOCKITO
        )
    }

    suspend fun getOpenSourceLibraries(): List<OpenSourceLibraryEntity> {
        waitToMakeThisFeatureMoreInteresting()
        return OPEN_SOURCE_LIBRARIES
    }

    private suspend fun waitToMakeThisFeatureMoreInteresting() = delay(1500L)
}