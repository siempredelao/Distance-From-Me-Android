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

package gc.david.dfm.address.domain

import gc.david.dfm.address.data.AddressRepository
import gc.david.dfm.address.data.mapper.AddressCollectionEntityDataMapper
import gc.david.dfm.address.data.model.AddressCollectionEntity
import gc.david.dfm.address.data.model.GeocodingStatus
import gc.david.dfm.address.domain.model.AddressCollection
import gc.david.dfm.executor.Executor
import gc.david.dfm.executor.Interactor
import gc.david.dfm.executor.MainThread

/**
 * Created by david on 12.01.17.
 */
class GetAddressCoordinatesByNameInteractor(
        private val executor: Executor,
        private val mainThread: MainThread,
        private val mapper: AddressCollectionEntityDataMapper,
        private val repository: AddressRepository
) : Interactor, GetAddressCoordinatesByNameUseCase {

    private lateinit var locationName: String
    private var maxResults: Int = 0
    private lateinit var callback: GetAddressCoordinatesByNameUseCase.Callback

    override fun execute(locationName: String, maxResults: Int, callback: GetAddressCoordinatesByNameUseCase.Callback) {
        this.locationName = locationName
        this.maxResults = maxResults
        this.callback = callback
        this.executor.run(this)
    }

    override fun run() {
        repository.getCoordinatesByName(locationName, object : AddressRepository.Callback {
            override fun onSuccess(addressCollectionEntity: AddressCollectionEntity) {
                if (addressCollectionEntity.status in setOf(GeocodingStatus.OK, GeocodingStatus.ZERO_RESULTS)) {
                    val addressCollection = mapper.transform(addressCollectionEntity)
                    val limitedAddressCollection = limitAddress(addressCollection)
                    mainThread.post(Runnable { callback.onAddressLoaded(limitedAddressCollection) })
                } else {
                    notifyError(addressCollectionEntity.status.toString())
                }
            }

            override fun onError(message: String) {
                notifyError(message)
            }
        })
    }

    private fun notifyError(message: String) {
        mainThread.post(Runnable { callback.onError(message) })
    }

    private fun limitAddress(addressCollection: AddressCollection): AddressCollection {
        val addressList = addressCollection.addressList
        if (addressList.size > maxResults) {
            return addressCollection.copy(addressList = addressList.take(maxResults))
        }
        return addressCollection
    }
}
