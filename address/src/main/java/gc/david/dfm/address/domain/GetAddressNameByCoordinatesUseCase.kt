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

package gc.david.dfm.address.domain

import gc.david.dfm.address.data.mapper.AddressCollectionEntityDataMapper
import gc.david.dfm.address.data.model.GeocodingStatus
import gc.david.dfm.address.domain.model.AddressCollection
import gc.david.dfm.address.domain.model.Coordinates

class GetAddressNameByCoordinatesUseCase(
    private val repository: AddressRepository,
    private val mapper: AddressCollectionEntityDataMapper
) {

    suspend operator fun invoke(coordinates: Coordinates): Result<AddressCollection> {
        return try {
            val addressCollectionEntity = repository.getNameByCoordinates(coordinates)
            if (addressCollectionEntity.status in setOf(
                    GeocodingStatus.OK,
                    GeocodingStatus.ZERO_RESULTS
                )
            ) {
                val addressCollection = mapper.transform(addressCollectionEntity)
                val limitedAddressCollection = limitAmountOfAddresses(addressCollection)
                Result.success(limitedAddressCollection)
            } else {
                Result.failure(GeocodingException.from(addressCollectionEntity.status))
            }
        } catch (exception: Throwable) {
            Result.failure(exception)
        }
    }

    private fun limitAmountOfAddresses(addressCollection: AddressCollection): AddressCollection {
        val addressList = addressCollection.addressList
        return addressCollection.copy(addressList = addressList.take(MAX_BY_COORD))
    }

    companion object {

        private const val MAX_BY_COORD = 1
    }
}