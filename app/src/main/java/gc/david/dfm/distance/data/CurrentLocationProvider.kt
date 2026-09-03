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

package gc.david.dfm.distance.data

import android.location.Location
import gc.david.dfm.distance.data.model.Point
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

// Temporal solution, this should be a repository instead
@Singleton
class CurrentLocationProvider @Inject constructor() {

    private var currentLocation: Point = UNDEFINED

    fun get() : Point {
        return currentLocation
    }

    fun set(location: Location) {
        Timber.tag(TAG).d("set ${location.toPoint()}")
        currentLocation = location.toPoint()
    }

    private fun Location.toPoint() = Point(latitude, longitude)

    companion object {

        private const val TAG = "CurrentLocationProvider"
        val UNDEFINED = Point(Double.MAX_VALUE, Double.MIN_VALUE)
    }
}
