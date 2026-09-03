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

import gc.david.dfm.common.domain.model.UnitSystem

/**
 * Pure domain logic for unit conversions between metric and imperial systems.
 * Contains conversion logic without any formatting or presentation concerns.
 */
class UnitConverter {

    /**
     * Converts distance in meters to the appropriate unit based on the unit system.
     * Returns raw values without formatting.
     *
     * @param metres Distance in meters.
     * @param system Unit system to convert to.
     * @return Pair of (converted value, unit symbol).
     */
    fun convertDistance(metres: Double, system: UnitSystem): Pair<Double, String> {
        return when (system) {
            UnitSystem.IMPERIAL -> if (metres >= MILE_IN_METRES) {
                (metres / MILE_IN_METRES) to "mi"
            } else {
                (metres * YARDS_PER_METRE) to "yd"
            }
            UnitSystem.METRIC -> if (metres >= KILOMETRE_IN_METRES) {
                (metres / KILOMETRE_IN_METRES) to "km"
            } else {
                metres to "m"
            }
        }
    }

    /**
     * Converts altitude in meters to the appropriate unit.
     * Returns raw value without formatting.
     *
     * @param metres Altitude in meters.
     * @param system Unit system to convert to.
     * @return Converted altitude value.
     */
    fun convertAltitude(metres: Double, system: UnitSystem): Double {
        return when (system) {
            UnitSystem.IMPERIAL -> metres / FEET_IN_METRES
            UnitSystem.METRIC -> metres
        }
    }

    /**
     * Gets the altitude unit symbol for the given unit system.
     *
     * @param system Unit system.
     * @return Unit symbol ("ft" or "m").
     */
    fun getAltitudeUnit(system: UnitSystem): String {
        return when (system) {
            UnitSystem.IMPERIAL -> "ft"
            UnitSystem.METRIC -> "m"
        }
    }

    private companion object {

        const val MILE_IN_METRES = 1609.344
        const val KILOMETRE_IN_METRES = 1000.0
        const val YARDS_PER_METRE = 1.093613298337708
        const val FEET_IN_METRES = 0.3048
    }
}
