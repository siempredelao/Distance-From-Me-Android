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

package gc.david.dfm.core.coordinates.domain

import gc.david.dfm.core.coordinates.domain.model.Coordinates
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class DistanceCalculatorTest {

    private val calculator = DistanceCalculator()

    @ParameterizedTest
    @CsvSource(
        "0.0, 0.0",
        "40.4168, -3.7038",
        "-33.8688, 151.2093",
        "90.0, 0.0",
        "0.0, 180.0",
    )
    fun `calculateDistance is zero between a point and itself`(latitude: Double, longitude: Double) {
        assertEquals(0.0, calculator.calculateDistance(latitude, longitude, latitude, longitude))
    }

    // The expected distances come from the atan2 (Vincenty) formula on a sphere of the same radius
    @ParameterizedTest
    @CsvSource(
        // Madrid to Barcelona
        "40.4168, -3.7038, 41.3874, 2.1686, 505095.664",
        // Nashville to Los Angeles, across the western longitudes
        "36.12, -86.67, 33.94, -118.40, 2886444.443",
        // One degree of latitude, and of longitude at the equator and at 60 degrees north
        "0.0, 0.0, 1.0, 0.0, 111194.927",
        "0.0, 0.0, 0.0, 1.0, 111194.927",
        "60.0, 0.0, 60.0, 1.0, 55596.934",
        // About a metre
        "0.0, 0.0, 0.0, 0.00001, 1.112",
    )
    fun `calculateDistance returns the known distance in both directions`(
        latitudeA: Double,
        longitudeA: Double,
        latitudeB: Double,
        longitudeB: Double,
        expected: Double
    ) {
        assertEquals(expected, calculator.calculateDistance(latitudeA, longitudeA, latitudeB, longitudeB), MILLIMETRE)
        assertEquals(expected, calculator.calculateDistance(latitudeB, longitudeB, latitudeA, longitudeA), MILLIMETRE)
    }

    @ParameterizedTest
    @CsvSource(
        "0.0, 179.5, 0.0, -179.5, 111194.927",
        "10.0, 179.5, 10.0, -179.5, 109505.584",
        // The same meridian, written both ways
        "45.0, 180.0, 45.0, -180.0, 0.0",
    )
    fun `calculateDistance takes the short way across the antimeridian`(
        latitudeA: Double,
        longitudeA: Double,
        latitudeB: Double,
        longitudeB: Double,
        expected: Double
    ) {
        assertEquals(expected, calculator.calculateDistance(latitudeA, longitudeA, latitudeB, longitudeB), MILLIMETRE)
        assertEquals(expected, calculator.calculateDistance(latitudeB, longitudeB, latitudeA, longitudeA), MILLIMETRE)
    }

    @ParameterizedTest
    @CsvSource(
        "0.0, 0.0, 0.0, 180.0",
        "90.0, 0.0, -90.0, 0.0",
        "40.4168, -3.7038, -40.4168, 176.2962",
        "-33.8688, 151.2093, 33.8688, -28.7907",
    )
    fun `calculateDistance is half the circumference between antipodes`(
        latitudeA: Double,
        longitudeA: Double,
        latitudeB: Double,
        longitudeB: Double
    ) {
        assertEquals(
            HALF_CIRCUMFERENCE_IN_METRES,
            calculator.calculateDistance(latitudeA, longitudeA, latitudeB, longitudeB),
            MILLIMETRE
        )
    }

    @Test
    fun `calculateDistance ignores the longitude at a pole`() {
        assertEquals(0.0, calculator.calculateDistance(90.0, 0.0, 90.0, 120.0), MILLIMETRE)
    }

    @Test
    fun `calculateTotalDistance is zero without points`() {
        assertEquals(0.0, calculator.calculateTotalDistance(emptyList()))
    }

    @Test
    fun `calculateTotalDistance is zero with one point`() {
        assertEquals(0.0, calculator.calculateTotalDistance(listOf(MADRID)))
    }

    @Test
    fun `calculateTotalDistance is the distance between two points`() {
        assertEquals(505095.664, calculator.calculateTotalDistance(listOf(MADRID, BARCELONA)), MILLIMETRE)
    }

    @Test
    fun `calculateTotalDistance adds up every leg instead of joining the ends`() {
        val thereAndBack = listOf(MADRID, BARCELONA, MADRID)

        assertEquals(2 * 505095.664, calculator.calculateTotalDistance(thereAndBack), MILLIMETRE)
    }

    private companion object {

        val MADRID = Coordinates(40.4168, -3.7038)
        val BARCELONA = Coordinates(41.3874, 2.1686)

        // Half of 2 * PI * 6371 km, the mean radius of the Earth
        const val HALF_CIRCUMFERENCE_IN_METRES = 20015086.796
        const val MILLIMETRE = 1e-3
    }
}
