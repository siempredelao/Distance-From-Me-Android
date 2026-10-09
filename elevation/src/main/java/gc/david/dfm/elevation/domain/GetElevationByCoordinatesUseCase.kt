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

package gc.david.dfm.elevation.domain

import gc.david.dfm.common.Coordinates

class GetElevationByCoordinatesUseCase(private val repository: ElevationRepository) {

    suspend operator fun invoke(coordinatesList: List<Coordinates>): ElevationResult {
        require(coordinatesList.isNotEmpty()) { "An elevation profile needs at least one coordinate" }
        return repository.getElevation(getCoordinatesPath(coordinatesList), ELEVATION_SAMPLES)
    }

    private fun getCoordinatesPath(coordinatesList: List<Coordinates>): String {
        return coordinatesList.joinToString("|") { "${it.latitude},${it.longitude}" }
    }

    companion object {

        private const val ELEVATION_SAMPLES = 100
    }
}
