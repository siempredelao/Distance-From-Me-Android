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

package gc.david.dfm

import gc.david.dfm.common.Coordinates
import gc.david.dfm.settings.domain.Haversine

object Utils {

    fun isReleaseBuild() = "release" == BuildConfig.BUILD_TYPE

    fun calculateDistanceInMetres(coordinates: List<Coordinates>): Double {
        var distanceInMetres = 0.0
        for (i in 0 until coordinates.size - 1) {
            distanceInMetres += Haversine.getDistance(
                    coordinates[i].latitude,
                    coordinates[i].longitude,
                    coordinates[i + 1].latitude,
                    coordinates[i + 1].longitude)
        }
        return distanceInMetres
    }
}
