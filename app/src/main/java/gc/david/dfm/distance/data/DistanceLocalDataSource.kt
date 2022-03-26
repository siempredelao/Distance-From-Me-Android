/*
 * Copyright (c) 2022 David Aguiar Gonzalez
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

package gc.david.dfm.distance.data

import gc.david.dfm.database.DFMDatabase
import gc.david.dfm.database.Distance
import gc.david.dfm.database.Position
import gc.david.dfm.distance.domain.DistanceRepository

/**
 * Created by david on 16.01.17.
 */
class DistanceLocalDataSource(private val database: DFMDatabase) {

    suspend fun insert(distance: Distance, positionList: List<Position>) {
        val rowID = database.distanceDao().insert(distance)
        if (rowID == -1L) {
            throw Exception() // TODO return a custom exception instead
        } else {
            val positionListWithDistanceId = positionList.map { it.apply { distanceId = rowID } }
            database.positionDao().insertMany(positionListWithDistanceId)
            return Unit
        }
    }

    fun loadDistances(callback: DistanceRepository.LoadDistancesCallback) {
        callback.onSuccess(database.distanceDao().loadAll())
    }

    suspend fun clear() {
        with(database) {
            distanceDao().deleteAll()
            positionDao().deleteAll()
        }
    }

    fun getPositionListById(distanceId: Long, callback: DistanceRepository.LoadPositionsByIdCallback) {
        callback.onSuccess(database.positionDao().loadAllById(distanceId))
    }
}