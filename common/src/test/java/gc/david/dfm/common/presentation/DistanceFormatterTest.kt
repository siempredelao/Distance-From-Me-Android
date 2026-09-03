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

package gc.david.dfm.common.presentation

import gc.david.dfm.common.domain.UnitConverter
import gc.david.dfm.common.domain.model.UnitSystem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class DistanceFormatterTest {

    private val converter = mock<UnitConverter>()
    private val formatter = DistanceFormatter(converter)

    @Test
    fun `formatDistance formats metric kilometers correctly`() {
        whenever(converter.convertDistance(5000.0, UnitSystem.METRIC))
            .thenReturn(5.0 to "km")

        val result = formatter.formatDistance(5000.0, UnitSystem.METRIC)

        assertEquals("5.00 km", result)
    }

    @Test
    fun `formatDistance formats metric meters correctly`() {
        whenever(converter.convertDistance(500.0, UnitSystem.METRIC))
            .thenReturn(500.0 to "m")

        val result = formatter.formatDistance(500.0, UnitSystem.METRIC)

        assertEquals("500.00 m", result)
    }

    @Test
    fun `formatDistance formats imperial miles correctly`() {
        whenever(converter.convertDistance(10000.0, UnitSystem.IMPERIAL))
            .thenReturn(6.21 to "mi")

        val result = formatter.formatDistance(10000.0, UnitSystem.IMPERIAL)

        assertEquals("6.21 mi", result)
    }

    @Test
    fun `formatDistance formats imperial yards correctly`() {
        whenever(converter.convertDistance(500.0, UnitSystem.IMPERIAL))
            .thenReturn(546.81 to "yd")

        val result = formatter.formatDistance(500.0, UnitSystem.IMPERIAL)

        assertEquals("546.81 yd", result)
    }

    @Test
    fun `formatDistance formats with thousand separator`() {
        whenever(converter.convertDistance(2000000.0, UnitSystem.METRIC))
            .thenReturn(2000.0 to "km")

        val result = formatter.formatDistance(2000000.0, UnitSystem.METRIC)

        assertEquals("2,000.00 km", result)
    }

    @Test
    fun `formatAltitude rounds to 2 decimal places`() {
        whenever(converter.convertAltitude(100.0, UnitSystem.METRIC))
            .thenReturn(100.456)

        val result = formatter.formatAltitude(100.0, UnitSystem.METRIC)

        assertEquals(100.46, result, 0.001)
    }

    @Test
    fun `formatAltitude converts imperial correctly`() {
        whenever(converter.convertAltitude(100.0, UnitSystem.IMPERIAL))
            .thenReturn(328.084)

        val result = formatter.formatAltitude(100.0, UnitSystem.IMPERIAL)

        assertEquals(328.08, result, 0.001)
    }

    @Test
    fun `getAltitudeUnitLabel returns metric unit`() {
        whenever(converter.getAltitudeUnit(UnitSystem.METRIC))
            .thenReturn("m")

        val result = formatter.getAltitudeUnitLabel(UnitSystem.METRIC)

        assertEquals("m", result)
    }

    @Test
    fun `getAltitudeUnitLabel returns imperial unit`() {
        whenever(converter.getAltitudeUnit(UnitSystem.IMPERIAL))
            .thenReturn("ft")

        val result = formatter.getAltitudeUnitLabel(UnitSystem.IMPERIAL)

        assertEquals("ft", result)
    }
}

