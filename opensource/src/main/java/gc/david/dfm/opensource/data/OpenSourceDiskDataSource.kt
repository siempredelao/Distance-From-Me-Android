/*
 * Copyright (c) 2026 David Aguiar Gonzalez
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

        private val GRAPHVIEW = OpenSourceLibraryEntity(
            "GraphView",
            "Android Graph Library for creating zoomable and scrollable line and bar graphs.",
            "Jonas Gehring",
            "4.2.2",
            "https://github.com/jjoe64/GraphView",
            "Apache-2.0",
            "2016"
        )
        private val ROOM = OpenSourceLibraryEntity(
            "Room",
            "The Room persistence library provides an abstraction layer over SQLite to allow for more robust database access while harnessing the full power of SQLite.",
            "The Android Open Source Project",
            "2.8.4",
            "https://developer.android.com/topic/libraries/architecture/room",
            "Apache-2.0",
            "2016"
        )
        private val CRASHLYTICS = OpenSourceLibraryEntity(
            "Firebase Crashlytics",
            "The most powerful, yet lightest weight crash reporting solution.",
            "Google",
            "19.4.4",
            "https://firebase.google.com/docs/crashlytics",
            "Copyright",
            "2017"
        )
        private val KOIN = OpenSourceLibraryEntity(
            "Koin",
            "A pragmatic lightweight dependency injection framework for Kotlin developers.",
            "Arnaud GIULIANI",
            "4.1.0",
            "https://github.com/InsertKoinIO/koin",
            "Apache-2.0",
            "2017-2025"
        )
        private val OKHTTP = OpenSourceLibraryEntity(
            "OkHttp",
            "An HTTP+HTTP/2 client for Android and Java applications.",
            "Square, Inc.",
            "5.3.2",
            "https://square.github.io/okhttp/",
            "Apache-2.0",
            "2019"
        )
        private val GSON = OpenSourceLibraryEntity(
            "Gson",
            "A Java serialization/deserialization library that can convert Java Objects into JSON and back.",
            "Google Inc.",
            "2.13.2",
            "https://github.com/google/gson",
            "Apache-2.0",
            "2008"
        )
        private val CONSTRAINT_LAYOUT = OpenSourceLibraryEntity(
            "ConstraintLayout",
            "ConstraintLayout allows you to create large and complex layouts with a flat view hierarchy (no nested view groups).",
            "The Android Open Source Project",
            "2.2.1",
            "https://developer.android.com/training/constraint-layout/index.html",
            "Apache-2.0",
            "2007-2017"
        )
        private val LEAK_CANARY = OpenSourceLibraryEntity(
            "LeakCanary",
            "A memory leak detection library for Android and Java.",
            "Square, Inc.",
            "2.14",
            "https://github.com/square/leakcanary",
            "Apache-2.0",
            "2015"
        )
        private val JUNIT = OpenSourceLibraryEntity(
            "JUnit",
            "JUnit is a simple framework to write repeatable tests. It is an instance of the xUnit architecture for unit testing frameworks.",
            "JUnit",
            "4.13.2",
            "https://junit.org/junit4/",
            "EPL-1.0",
            "2002-2021"
        )
        private val MOCKITO = OpenSourceLibraryEntity(
            "Mockito",
            "Most popular Mocking framework for unit tests written in Java.",
            "Mockito contributors",
            "5.22.0",
            "https://site.mockito.org/",
            "MIT",
            "2007"
        )

        private val OPEN_SOURCE_LIBRARIES = listOf(
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