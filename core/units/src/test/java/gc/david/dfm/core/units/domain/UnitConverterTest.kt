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

package gc.david.dfm.core.units.domain

import gc.david.dfm.core.units.domain.model.UnitSystem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class UnitConverterTest {

    private val converter = UnitConverter()

    @ParameterizedTest
    @CsvSource(
        "0.0, 0.0",
        "1.0, 1.0",
        "999.999, 999.999",
    )
    fun `convertDistance keeps metres below a kilometre`(metres: Double, expected: Double) {
        assertDistance(expected, "m", converter.convertDistance(metres, UnitSystem.METRIC))
    }

    @ParameterizedTest
    @CsvSource(
        "1000.0, 1.0",
        "1000.001, 1.000001",
        "1609.344, 1.609344",
        "20015086.796, 20015.086796",
    )
    fun `convertDistance uses kilometres from one kilometre on`(metres: Double, expected: Double) {
        assertDistance(expected, "km", converter.convertDistance(metres, UnitSystem.METRIC))
    }

    @ParameterizedTest
    @CsvSource(
        "0.0, 0.0",
        "0.9144, 1.0",
        "914.4, 1000.0",
        "1000.0, 1093.613298",
        "1609.343, 1759.998906",
    )
    fun `convertDistance uses yards below a mile`(metres: Double, expected: Double) {
        assertDistance(expected, "yd", converter.convertDistance(metres, UnitSystem.IMPERIAL))
    }

    @ParameterizedTest
    @CsvSource(
        "1609.344, 1.0",
        "1609.345, 1.000001",
        "42195.0, 26.218757",
        "160934.4, 100.0",
    )
    fun `convertDistance uses miles from one mile on`(metres: Double, expected: Double) {
        assertDistance(expected, "mi", converter.convertDistance(metres, UnitSystem.IMPERIAL))
    }

    @ParameterizedTest
    @CsvSource(
        "-430.5",
        "0.0",
        "8848.86",
    )
    fun `convertAltitude keeps metres in the metric system`(metres: Double) {
        assertEquals(metres, converter.convertAltitude(metres, UnitSystem.METRIC))
    }

    @ParameterizedTest
    @CsvSource(
        "-30.48, -100.0",
        "0.0, 0.0",
        "0.3048, 1.0",
        "8848.86, 29031.692913",
    )
    fun `convertAltitude uses feet in the imperial system`(metres: Double, expected: Double) {
        assertEquals(expected, converter.convertAltitude(metres, UnitSystem.IMPERIAL), DELTA)
    }

    @Test
    fun `getAltitudeUnit returns metres in the metric system`() {
        assertEquals("m", converter.getAltitudeUnit(UnitSystem.METRIC))
    }

    @Test
    fun `getAltitudeUnit returns feet in the imperial system`() {
        assertEquals("ft", converter.getAltitudeUnit(UnitSystem.IMPERIAL))
    }

    private fun assertDistance(expectedValue: Double, expectedUnit: String, actual: Pair<Double, String>) {
        val (value, unit) = actual
        assertEquals(expectedUnit, unit)
        assertEquals(expectedValue, value, DELTA)
    }

    private companion object {

        const val DELTA = 1e-6
    }
}
