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

package gc.david.dfm.distance.domain

import gc.david.dfm.database.Distance
import gc.david.dfm.executor.Interactor
import gc.david.dfm.executor.NewMainThread
import gc.david.dfm.executor.NewThreadExecutor

/**
 * Created by david on 16.01.17.
 */
class LoadDistancesInteractor(
        private val executor: NewThreadExecutor,
        private val mainThread: NewMainThread,
        private val repository: DistanceRepository
) : Interactor {

    private lateinit var callback: Callback

    fun execute(callback: Callback) {
        this.callback = callback
        this.executor.run(this)
    }

    override fun run() {
        repository.loadDistances(object : DistanceRepository.LoadDistancesCallback {
            override fun onSuccess(distanceList: List<Distance>) {
                mainThread.post(Runnable { callback.onDistanceListLoaded(distanceList) })
            }

            override fun onFailure() {
                mainThread.post(Runnable { callback.onError() })
            }
        })
    }

    interface Callback {

        fun onDistanceListLoaded(distanceList: List<Distance>)

        fun onError()

    }
}
