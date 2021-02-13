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

package gc.david.dfm.map

import java.text.DecimalFormat
import java.util.*
import kotlin.math.*

/**
 * Haversine class implements static methods to calculate distances between two
 * points on a sphere, in this case, the Earth, from their longitudes and
 * latitudes. It also defines static methods to calculate elevation points.
 *
 * @author David
 * @see [Haversine](http://en.wikipedia.org/wiki/Haversine_formula)
 */
object Haversine {

    private const val EARTH_RADIUS_IN_METRES = 6371000.0
    private const val MILE_IN_METRES = 1609.344
    private const val KILOMETRE_IN_METRES = 1000.0
    private const val YARD_IN_METRES = 1.093613298337708
    private const val FEET_IN_METRES = 0.3048
    private val DISTANCE_FORMAT = DecimalFormat("##,##0.00")

    /**
     * Calculates distance between two positions in metres.
     *
     * @param latitudeA  Current position latitude in degrees.
     * @param longitudeA Current position longitude in degrees.
     * @param latitudeB  Destination position latitude in degrees.
     * @param longitudeB Destination position longitude in degrees.
     * @return Distance in meters.
     */
    fun getDistance(latitudeA: Double,
                    longitudeA: Double,
                    latitudeB: Double,
                    longitudeB: Double): Double {
        val latitudeAInRadians = Math.toRadians(latitudeA)
        val longitudeAInRadians = Math.toRadians(longitudeA)
        val latitudeBInRadians = Math.toRadians(latitudeB)
        val longitudeBInRadians = Math.toRadians(longitudeB)

        val distanceLatitudes = latitudeBInRadians - latitudeAInRadians
        val distanceLongitudes = longitudeBInRadians - longitudeAInRadians

        val sinLatitude = sin(distanceLatitudes / 2)
        val sinLongitude = sin(distanceLongitudes / 2)

        val a = sinLatitude * sinLatitude + cos(latitudeAInRadians) * cos(latitudeBInRadians) * (sinLongitude * sinLongitude)
        val c = 2 * asin(min(1.0, sqrt(a)))

        return EARTH_RADIUS_IN_METRES * c
    }

    /**
     * Normalizes distance corresponding to its unit and the device locale and with only two decimal
     * digits. This static function distinguish between metric and imperial and US customary units.
     *
     * @param distanceInMetres Unformatted distance in metres.
     * @param locale           The current locale of the device.
     * @return A String with the amount and the unit.
     */
    fun normalizeDistance(distanceInMetres: Double, locale: Locale): String {
        val (measureUnit, distanceByLocale) =
                if (isAmericanLocale(locale)) {
                    if (distanceInMetres >= MILE_IN_METRES) {
                        "mi" to distanceInMetres / MILE_IN_METRES
                    } else {
                        "yd" to distanceInMetres * YARD_IN_METRES
                    }
                } else {
                    if (distanceInMetres >= KILOMETRE_IN_METRES) {
                        "km" to distanceInMetres / KILOMETRE_IN_METRES
                    } else {
                        "m" to distanceInMetres
                    }
                }
        return "${DISTANCE_FORMAT.format(distanceByLocale)} $measureUnit"
    }

    /**
     * Normalizes altitude corresponding to its unit and the device locale and with only two decimal
     * digits. This static function distinguish between metric and imperial and US customary units.
     *
     * @param altitude Unformatted altitude in metres.
     * @param locale   The current locale of the device.
     * @return A double with only the normalized amount.
     */
    fun normalizeAltitudeByLocale(altitude: Double, locale: Locale): Double {
        val measure = if (isEnglishLocale(locale)) {
            altitude / FEET_IN_METRES
        } else {
            altitude
        }

        // Two decimal digits
        return (measure * 1e2).roundToLong() / 1e2
    }

    /**
     * Returns a string with the altitude unit, m or ft, based in the current locale.
     *
     * @param locale Current locale.
     * @return String with altitude unit.
     */
    fun getAltitudeUnitByLocale(locale: Locale): String {
        return if (isEnglishLocale(locale)) "ft" else "m"
    }

    private fun isEnglishLocale(locale: Locale): Boolean {
        return locale == Locale.CANADA || locale == Locale.UK || locale == Locale.US
    }

    fun isAmericanLocale(locale: Locale): Boolean {
        return (locale == Locale.CANADA
                || locale == Locale.CHINA
                || locale == Locale.JAPAN
                || locale == Locale.KOREA
                || locale == Locale.TAIWAN
                || locale == Locale.UK
                || locale == Locale.US)
    }
}
