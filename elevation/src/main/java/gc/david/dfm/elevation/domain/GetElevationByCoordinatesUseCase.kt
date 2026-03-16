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
import gc.david.dfm.elevation.data.mapper.ElevationEntityDataMapper
import gc.david.dfm.elevation.data.model.ElevationStatus
import gc.david.dfm.elevation.domain.model.Elevation

class GetElevationByCoordinatesUseCase(
    private val repository: ElevationRepository,
    private val mapper: ElevationEntityDataMapper
) {

    suspend operator fun invoke(coordinatesList: List<Coordinates>): Result<Elevation> {
        return if (coordinatesList.isEmpty()) {
            Result.failure(Exception("Empty coordinates list"))
        } else {
            try {
                val coordinatesPath = getCoordinatesPath(coordinatesList)
                val elevationEntity = repository.getElevation(coordinatesPath, ELEVATION_SAMPLES)
                if (elevationEntity.status == ElevationStatus.OK) {
                    val elevation = mapper.transform(elevationEntity)
                    Result.success(elevation)
                } else {
                    Result.failure(Exception(elevationEntity.status.toString()))
                }
            } catch (exception: Throwable) {
                Result.failure(exception)
            }
        }
    }

    private fun getCoordinatesPath(coordinatesList: List<Coordinates>): String {
        return coordinatesList.joinToString("|") { "${it.latitude},${it.longitude}" }
    }

    companion object {

        private const val ELEVATION_SAMPLES = 100
    }
}