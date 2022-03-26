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

import gc.david.dfm.database.Distance
import gc.david.dfm.database.Position
import gc.david.dfm.distance.domain.DistanceRepository

class BaseDistanceRepository(private val localDataSource: DistanceLocalDataSource)
    : DistanceRepository {

    override fun insert(distance: Distance, positionList: List<Position>, callback: DistanceRepository.Callback) {
        localDataSource.insert(distance, positionList, callback)
    }

    override fun loadDistances(callback: DistanceRepository.LoadDistancesCallback) {
        localDataSource.loadDistances(callback)
    }

    override suspend fun clear() {
        localDataSource.clear()
    }

    override fun getPositionListById(distanceId: Long, callback: DistanceRepository.LoadPositionsByIdCallback) {
        localDataSource.getPositionListById(distanceId, callback)
    }
}