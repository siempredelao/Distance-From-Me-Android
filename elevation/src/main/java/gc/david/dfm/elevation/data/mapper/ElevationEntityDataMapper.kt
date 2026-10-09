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

package gc.david.dfm.elevation.data.mapper

import gc.david.dfm.elevation.data.model.ElevationEntity
import gc.david.dfm.elevation.data.model.ElevationStatus
import gc.david.dfm.elevation.domain.ElevationError
import gc.david.dfm.elevation.domain.ElevationResult

/**
 * Created by david on 13.01.17.
 *
 *
 * Mapper class used to transform [ElevationEntity] in the Data layer
 * to [ElevationResult] in the Domain layer.
 */
class ElevationEntityDataMapper {

    fun transform(elevationEntity: ElevationEntity): ElevationResult =
        when (elevationEntity.status) {
            ElevationStatus.OK -> {
                val samples = elevationEntity.results.map { it.elevation }
                if (samples.isEmpty()) ElevationResult.NoData else ElevationResult.Found(samples)
            }
            ElevationStatus.INVALID_REQUEST -> ElevationResult.Rejected(ElevationError.INVALID_REQUEST)
            ElevationStatus.OVER_QUERY_LIMIT -> ElevationResult.Rejected(ElevationError.OVER_QUERY_LIMIT)
            ElevationStatus.REQUEST_DENIED -> ElevationResult.Rejected(ElevationError.REQUEST_DENIED)
            ElevationStatus.UNKNOWN_ERROR -> ElevationResult.Rejected(ElevationError.UNKNOWN_ERROR)
        }
}
