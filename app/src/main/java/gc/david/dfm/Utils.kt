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

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import gc.david.dfm.core.distances.data.database.Position
import gc.david.dfm.map.Haversine

object Utils {

    fun isReleaseBuild() = "release" == BuildConfig.BUILD_TYPE

    fun calculateDistanceInMetres(coordinates: List<LatLng>): Double {
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

    fun calculateDistanceInMetres2(coordinates: List<Position>): Double {
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

    fun convertPositionListToLatLngList(positionList: List<Position>): List<LatLng> {
        return positionList.map { LatLng(it.latitude, it.longitude) }
    }

    fun Position.toLatLng() = LatLng(latitude, longitude)

    fun List<Position>.toLatLng() = map { it.toLatLng() }

    data class Point(val lat: Double, val lon: Double) {
        override fun toString(): String {
            return "P($lat, $lon)"
        }
    }

    fun LatLng.toPoint() = Point(latitude, longitude)
    fun Position.toPoint() = Point(latitude, longitude)
    fun Location.toPoint() = Point(latitude, longitude)
}
