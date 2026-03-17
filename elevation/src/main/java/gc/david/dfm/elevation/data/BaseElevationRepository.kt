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

package gc.david.dfm.elevation.data

import gc.david.dfm.elevation.data.mapper.ElevationEntityDataMapper
import gc.david.dfm.elevation.domain.ElevationRepository
import gc.david.dfm.elevation.domain.model.Elevation

class BaseElevationRepository(
    private val remoteDataSource: ElevationRemoteDataSource,
    private val mapper: ElevationEntityDataMapper
) : ElevationRepository {

    override suspend fun getElevation(coordinatesPath: String, maxSamples: Int): Elevation {
        val elevationEntity = remoteDataSource.getElevation(coordinatesPath, maxSamples)
        return mapper.transform(elevationEntity)
    }
}