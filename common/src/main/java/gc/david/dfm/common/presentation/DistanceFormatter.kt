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
import java.text.DecimalFormat
import kotlin.math.roundToLong

/**
 * Formats distance and altitude values for UI display.
 * Uses domain converters but adds presentation logic (formatting).
 */
class DistanceFormatter(private val converter: UnitConverter) {

    private val decimalFormat = DecimalFormat("##,##0.00")

    /**
     * Formats distance with unit for display.
     * 
     * @param distanceInMetres Unformatted distance in metres.
     * @param unitSystem The unit system to use for formatting.
     * @return Formatted string like "1,234.56 km".
     */
    fun formatDistance(distanceInMetres: Double, unitSystem: UnitSystem): String {
        val (value, unit) = converter.convertDistance(distanceInMetres, unitSystem)
        return "${decimalFormat.format(value)} $unit"
    }

    /**
     * Formats altitude for display (normalized to 2 decimals).
     * 
     * @param altitudeInMetres Unformatted altitude in metres.
     * @param unitSystem The unit system to use for formatting.
     * @return Normalized altitude value with 2 decimal digits.
     */
    fun formatAltitude(altitudeInMetres: Double, unitSystem: UnitSystem): Double {
        val converted = converter.convertAltitude(altitudeInMetres, unitSystem)
        // Two decimal digits
        return (converted * 1e2).roundToLong() / 1e2
    }

    /**
     * Gets altitude unit label for display.
     * 
     * @param unitSystem The unit system.
     * @return Unit label ("ft" or "m").
     */
    fun getAltitudeUnitLabel(unitSystem: UnitSystem): String {
        return converter.getAltitudeUnit(unitSystem)
    }
}
