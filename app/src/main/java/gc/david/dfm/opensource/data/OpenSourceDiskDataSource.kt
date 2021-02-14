/*
 * Copyright (c) 2021 David Aguiar Gonzalez
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

/**
 * Created by david on 25.01.17.
 */
class OpenSourceDiskDataSource : OpenSourceRepository {

    private val openSourceLibraryEntityList = listOf(
            OpenSourceLibraryEntity(
                    "Support Library",
                    "The Android Open Source Project",
                    "27.1.1",
                    "https://developer.android.com/topic/libraries/support-library/index.html",
                    "Apache-2.0",
                    "2007-2017",
                    "Serves a standard way to provide newer features on earlier versions of Android or gracefully fall back to equivalent functionality."),
            OpenSourceLibraryEntity(
                    "Google Play Services",
                    "The Android Open Source Project",
                    "17.0.0",
                    "https://developers.google.com/android/guides/overview",
                    "Apache-2.0",
                    "2007-2017",
                    "Take advantage of the latest, Google-powered features such as Maps, Google+, and more, with automatic platform updates distributed as an APK through the Google Play store."),
            OpenSourceLibraryEntity(
                    "GraphView",
                    "Jonas Gehring",
                    "4.2.2",
                    "http://www.android-graphview.org/",
                    "Apache-2.0",
                    "2016",
                    "Android Graph Library for creating zoomable and scrollable line and bar graphs."),
            OpenSourceLibraryEntity(
                    "Room",
                    "The Android Open Source Project",
                    "2.2.0",
                    "https://developer.android.com/topic/libraries/architecture/room",
                    "Apache-2.0",
                    "2016",
                    "The Room persistence library provides an abstraction layer over SQLite to allow for more robust database access while harnessing the full power of SQLite."),
            OpenSourceLibraryEntity(
                    "Firebase Crashlytics",
                    "Google",
                    "17.3.0",
                    "https://firebase.google.com/docs/crashlytics",
                    "Copyright",
                    "2017",
                    "The most powerful, yet lightest weight crash reporting solution."),
            OpenSourceLibraryEntity(
                    "Dagger",
                    "The Dagger Authors",
                    "2.16",
                    "https://google.github.io/dagger/",
                    "Apache-2.0",
                    "2012",
                    "A fast dependency injector for Android and Java."),
            OpenSourceLibraryEntity(
                    "OkHttp",
                    "Square, Inc.",
                    "3.11.0",
                    "http://square.github.io/okhttp/",
                    "Apache-2.0",
                    "2016",
                    "An HTTP+HTTP/2 client for Android and Java applications."),
            OpenSourceLibraryEntity(
                    "Gson",
                    "Google Inc.",
                    "2.8.6",
                    "https://github.com/google/gson",
                    "Apache-2.0",
                    "2008",
                    "A Java serialization/deserialization library that can convert Java Objects into JSON and back."),
            OpenSourceLibraryEntity(
                    "ConstraintLayout",
                    "The Android Open Source Project",
                    "1.1.3",
                    "https://developer.android.com/training/constraint-layout/index.html",
                    "Apache-2.0",
                    "2007-2017",
                    "ConstraintLayout allows you to create large and complex layouts with a flat view hierarchy (no nested view groups)."),
            OpenSourceLibraryEntity(
                    "LeakCanary",
                    "Square, Inc.",
                    "1.6.3",
                    "https://github.com/square/leakcanary",
                    "Apache-2.0",
                    "2015",
                    "A memory leak detection library for Android and Java."),
            OpenSourceLibraryEntity(
                    "JUnit",
                    "JUnit",
                    "4.12",
                    "http://junit.org/junit4/",
                    "EPL-1.0",
                    "2002-2017",
                    "JUnit is a simple framework to write repeatable tests. It is an instance of the xUnit architecture for unit testing frameworks."),
            OpenSourceLibraryEntity(
                    "Mockito",
                    "Mockito contributors",
                    "2.28.2",
                    "http://site.mockito.org/",
                    "MIT",
                    "2007",
                    "Most popular Mocking framework for unit tests written in Java.")
    )

    override fun getOpenSourceLibraries(callback: OpenSourceRepository.Callback) {
        waitToMakeThisFeatureMoreInteresting()
        callback.onSuccess(openSourceLibraryEntityList)
    }

    private fun waitToMakeThisFeatureMoreInteresting() {
        try {
            Thread.sleep(500L)
        } catch (e: InterruptedException) {
            // nothing
        }
    }
}
