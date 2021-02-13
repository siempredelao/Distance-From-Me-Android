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

package gc.david.dfm.distance.data

import gc.david.dfm.database.DFMDatabase
import gc.david.dfm.database.Distance
import gc.david.dfm.database.Position

/**
 * Created by david on 16.01.17.
 */
class DistanceLocalDataSource(private val database: DFMDatabase) : DistanceRepository {

    override fun insert(distance: Distance, positionList: List<Position>, callback: DistanceRepository.Callback) {
        val rowID = database.distanceDao().insert(distance)
        if (rowID == -1L) {
            callback.onFailure()
        } else {
            val positionListWithDistanceId = positionList.map { it.apply { distanceId = rowID } }
            database.positionDao().insertMany(positionListWithDistanceId)
            callback.onSuccess()
        }
    }

    override fun loadDistances(callback: DistanceRepository.LoadDistancesCallback) {
        callback.onSuccess(database.distanceDao().loadAll())
    }

    override fun clear(callback: DistanceRepository.Callback) {
        with(database) {
            distanceDao().deleteAll()
            positionDao().deleteAll()
        }
        callback.onSuccess()
    }

    override fun getPositionListById(distanceId: Long, callback: DistanceRepository.LoadPositionsByIdCallback) {
        callback.onSuccess(database.positionDao().loadAllById(distanceId))
    }
}
