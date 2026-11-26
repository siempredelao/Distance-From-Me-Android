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

package gc.david.dfm.screenshots

import kotlin.math.abs

/**
 * A place of the Play Store pictures: what scripts/store_screenshots.py searches, and what the app shows for it.
 *
 * The addresses name no country, so they suit every language. Some of the data is © OpenStreetMap contributors.
 */
enum class Place(val query: String, val address: String, val latitude: Double, val longitude: Double) {

    START("Las Palmas de Gran Canaria", "Av. de Escaleritas, 35011 Las Palmas de Gran Canaria", 28.1235, -15.4363),
    PUERTO_DEL_ROSARIO("Puerto del Rosario", "Puerto del Rosario, 35600 Las Palmas", 28.5004, -13.8627),
    MASPALOMAS("Maspalomas", "Maspalomas, 35100 Las Palmas", 27.7606, -15.5860),
    AUDITORIO("Auditorio Alfredo Kraus", "Auditorio Alfredo Kraus, 35010 Las Palmas de Gran Canaria", 28.1302, -15.4490),
    LA_PUNTILLA("La Puntilla", "La Puntilla, 35009 Las Palmas de Gran Canaria", 28.1488, -15.4325),
    SANTA_MARIA_DE_GUIA("Santa Maria de Guia", "Santa María de Guía, 35450 Las Palmas", 28.1393, -15.6326);

    fun isNear(otherLatitude: Double, otherLongitude: Double): Boolean =
        abs(latitude - otherLatitude) < NEAR && abs(longitude - otherLongitude) < NEAR

    private companion object {

        // Degrees, less than between any two places
        const val NEAR = 0.005
    }
}
