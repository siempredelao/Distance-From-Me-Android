/*
 * Copyright (c) 2018 David Aguiar Gonzalez
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

import gc.david.dfm.model.DaoSession
import gc.david.dfm.model.Distance
import gc.david.dfm.model.Position

/**
 * Created by david on 16.01.17.
 */
class DistanceLocalDataSource(private val daoSession: DaoSession) : DistanceRepository {

    override fun insert(distance: Distance, positionList: List<Position>, callback: DistanceRepository.Callback) {
        val rowID = daoSession.insert(distance)
        if (rowID == -1L) {
            callback.onFailure()
        } else {
            positionList.forEach { position ->
                position.distanceId = rowID
                daoSession.insert(position)
            }
            callback.onSuccess()
        }
    }

    override fun loadDistances(callback: DistanceRepository.LoadDistancesCallback) {
        callback.onSuccess(daoSession.loadAll<Distance, Any>(Distance::class.java))
    }

    override fun clear(callback: DistanceRepository.Callback) {
        with(daoSession) {
            deleteAll(Distance::class.java)
            deleteAll(Position::class.java)
        }
        callback.onSuccess()
    }

    override fun getPositionListById(distanceId: Long, callback: DistanceRepository.LoadPositionsByIdCallback) {
        callback.onSuccess(daoSession.positionDao._queryDistance_PositionList(distanceId))
    }
}
