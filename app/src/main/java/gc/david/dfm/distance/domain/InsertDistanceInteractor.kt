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

package gc.david.dfm.distance.domain

import gc.david.dfm.database.Distance
import gc.david.dfm.database.Position
import gc.david.dfm.distance.data.DistanceRepository
import gc.david.dfm.executor.Executor
import gc.david.dfm.executor.Interactor
import gc.david.dfm.executor.MainThread

/**
 * Created by david on 16.01.17.
 */
class InsertDistanceInteractor(
        private val executor: Executor,
        private val mainThread: MainThread,
        private val repository: DistanceRepository
) : Interactor, InsertDistanceUseCase {

    private lateinit var callback: InsertDistanceUseCase.Callback
    private lateinit var distance: Distance
    private lateinit var positionList: List<Position>

    override fun execute(distance: Distance, positionList: List<Position>, callback: InsertDistanceUseCase.Callback) {
        this.distance = distance
        this.positionList = positionList
        this.callback = callback
        this.executor.run(this)
    }

    override fun run() {
        repository.insert(distance, positionList, object : DistanceRepository.Callback {
            override fun onSuccess() {
                mainThread.post(Runnable { callback.onInsert() })
            }

            override fun onFailure() {
                mainThread.post(Runnable { callback.onError() })
            }
        })
    }
}
