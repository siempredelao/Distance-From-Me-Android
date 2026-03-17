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
package gc.david.dfm.common.domain
import gc.david.dfm.common.Coordinates
import kotlin.math.*
/**
 * Pure domain logic for distance calculations using Haversine formula.
 * Contains no formatting or presentation logic.
 * 
 * @see [Haversine formula](https://en.wikipedia.org/wiki/Haversine_formula)
 */
class DistanceCalculator {
    /**
     * Calculates distance between two coordinates using Haversine formula.
     * 
     * @param latitudeA Current position latitude in degrees.
     * @param longitudeA Current position longitude in degrees.
     * @param latitudeB Destination position latitude in degrees.
     * @param longitudeB Destination position longitude in degrees.
     * @return Distance in meters (raw value without formatting).
     */
    fun calculateDistance(
        latitudeA: Double,
        longitudeA: Double,
        latitudeB: Double,
        longitudeB: Double
    ): Double {
        val latitudeAInRadians = Math.toRadians(latitudeA)
        val longitudeAInRadians = Math.toRadians(longitudeA)
        val latitudeBInRadians = Math.toRadians(latitudeB)
        val longitudeBInRadians = Math.toRadians(longitudeB)
        val distanceLatitudes = latitudeBInRadians - latitudeAInRadians
        val distanceLongitudes = longitudeBInRadians - longitudeAInRadians
        val sinLatitude = sin(distanceLatitudes / 2)
        val sinLongitude = sin(distanceLongitudes / 2)
        val a = sinLatitude * sinLatitude + 
                cos(latitudeAInRadians) * cos(latitudeBInRadians) * (sinLongitude * sinLongitude)
        val c = 2 * asin(min(1.0, sqrt(a)))
        return EARTH_RADIUS_IN_METRES * c
    }
    /**
     * Calculates total distance for a path of coordinates.
     * Sums the distance between each consecutive pair of coordinates.
     * 
     * @param coordinates List of coordinates representing a path.
     * @return Total distance in meters (raw value without formatting).
     */
    fun calculateTotalDistance(coordinates: List<Coordinates>): Double {
        if (coordinates.size < 2) return 0.0
        var totalDistance = 0.0
        for (i in 0 until coordinates.size - 1) {
            totalDistance += calculateDistance(
                coordinates[i].latitude,
                coordinates[i].longitude,
                coordinates[i + 1].latitude,
                coordinates[i + 1].longitude
            )
        }
        return totalDistance
    }
    private companion object {
        const val EARTH_RADIUS_IN_METRES = 6371000.0
    }
}
