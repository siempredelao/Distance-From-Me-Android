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

package gc.david.dfm.core.distances.data

import gc.david.dfm.core.distances.data.database.DFMDatabase
import gc.david.dfm.core.distances.data.database.DistanceEntity
import gc.david.dfm.core.distances.data.database.PositionEntity
import gc.david.dfm.core.distances.data.mapper.DistanceEntityMapper
import gc.david.dfm.core.distances.data.mapper.PositionEntityMapper
import gc.david.dfm.core.distances.domain.InsertDistanceException
import gc.david.dfm.core.distances.domain.model.Distance
import gc.david.dfm.core.distances.domain.model.NewDistance
import gc.david.dfm.core.distances.domain.model.Position
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Created by david on 16.01.17.
 */
class DistanceLocalDataSource(
    private val database: DFMDatabase,
    private val distanceMapper: DistanceEntityMapper,
    private val positionMapper: PositionEntityMapper
) {

    suspend fun insert(distance: NewDistance) {
        val rowID =
            database.distanceDao().insert(
                DistanceEntity(
                    id = null,
                    name = distance.name,
                    distance = distance.distanceText,
                    date = distance.date
                )
            )

        if (rowID == -1L) {
            throw InsertDistanceException()
        } else {
            val positionListWithDistanceId =
                distance.positions
                    .map {
                        PositionEntity(
                            id = null,
                            latitude = it.latitude,
                            longitude = it.longitude,
                            distanceId = rowID,
                        )
                    }
            database.positionDao().insertMany(positionListWithDistanceId)
            return
        }
    }

    fun loadDistances(): Flow<List<Distance>> = 
        database.distanceDao().loadAll().map { entities ->
            distanceMapper.toDomainList(entities)
        }

    suspend fun clear() {
        with(database) {
            distanceDao().deleteAll()
            positionDao().deleteAll()
        }
    }

    suspend fun getPositionListById(distanceId: Long): List<Position> {
        val entities = database.positionDao().loadAllById(distanceId)
        return positionMapper.toDomainList(entities)
    }
}