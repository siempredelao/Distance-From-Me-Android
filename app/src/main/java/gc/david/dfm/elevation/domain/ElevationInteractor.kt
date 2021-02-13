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

package gc.david.dfm.elevation.domain

import com.google.android.gms.maps.model.LatLng

import gc.david.dfm.elevation.data.ElevationRepository
import gc.david.dfm.elevation.data.mapper.ElevationEntityDataMapper
import gc.david.dfm.elevation.data.model.ElevationEntity
import gc.david.dfm.elevation.data.model.ElevationStatus
import gc.david.dfm.executor.Executor
import gc.david.dfm.executor.Interactor
import gc.david.dfm.executor.MainThread

/**
 * Created by david on 05.01.17.
 */
class ElevationInteractor(
        private val executor: Executor,
        private val mainThread: MainThread,
        private val mapper: ElevationEntityDataMapper,
        private val repository: ElevationRepository
) : Interactor, ElevationUseCase {

    private lateinit var callback: ElevationUseCase.Callback
    private lateinit var coordinateList: List<LatLng>
    private var maxSamples: Int = 0

    override fun execute(coordinateList: List<LatLng>, maxSamples: Int, callback: ElevationUseCase.Callback) {
        this.coordinateList = coordinateList
        this.maxSamples = maxSamples
        this.callback = callback
        this.executor.run(this)
    }

    override fun run() {
        if (coordinateList.isEmpty()) {
            notifyError("Empty coordinates list")
        } else {
            val coordinatesPath = getCoordinatesPath(coordinateList)

            repository.getElevation(coordinatesPath, maxSamples, object : ElevationRepository.Callback {
                override fun onSuccess(elevationEntity: ElevationEntity) {
                    if (ElevationStatus.OK == elevationEntity.status) {
                        val elevation = mapper.transform(elevationEntity)

                        mainThread.post(Runnable { callback.onElevationLoaded(elevation) })
                    } else {
                        notifyError(elevationEntity.status.toString())
                    }
                }

                override fun onError(message: String) {
                    notifyError(message)
                }
            })
        }
    }

    private fun notifyError(errorMessage: String) {
        mainThread.post(Runnable { callback.onError(errorMessage) })
    }

    private fun getCoordinatesPath(coordinateList: List<LatLng>): String {
        return coordinateList.joinToString("|") { "${it.latitude},${it.longitude}" }
    }
}
