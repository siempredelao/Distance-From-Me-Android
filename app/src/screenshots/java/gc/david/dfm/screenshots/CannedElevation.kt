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

import gc.david.dfm.elevation.domain.ElevationRepository
import gc.david.dfm.elevation.domain.model.ElevationResult

/**
 * The elevation profile of the Play Store picture, so that taking it doesn't use up the quota of Google's services.
 *
 * It is the terrain from Las Palmas de Gran Canaria to Santa María de Guía (SRTM, public domain). Any other path has no
 * profile, rather than this one.
 */
class CannedElevation : ElevationRepository {

    override suspend fun getElevation(coordinatesPath: String, maxSamples: Int): ElevationResult {
        val (latitude, longitude) = coordinatesPath.substringAfterLast('|').split(',').map(String::toDouble)
        val isToGuia = Place.SANTA_MARIA_DE_GUIA.isNear(latitude, longitude)
        return if (isToGuia) ElevationResult.Found(PROFILE) else ElevationResult.NoData
    }

    private companion object {

        // Metres, at 100 points of the line
        val PROFILE = listOf(
            74, 81, 76, 50, 66, 74, 27, 33, 63, 35, 41, 41, 84, 125, 68, 19, 9, 40, 64, 187,
            205, 200, 193, 193, 179, 109, 60, 103, 157, 246, 216, 175, 165, 140, 199, 178, 179, 191, 200, 216,
            231, 214, 224, 219, 213, 204, 180, 158, 152, 122, 158, 181, 194, 210, 209, 159, 143, 210, 217, 162,
            192, 197, 181, 86, 101, 183, 109, 183, 182, 172, 131, 103, 173, 191, 171, 194, 215, 217, 88, 205,
            205, 114, 166, 151, 102, 212, 309, 357, 377, 361, 286, 279, 253, 296, 296, 263, 230, 192, 203, 193,
        ).map(Int::toDouble)
    }
}
