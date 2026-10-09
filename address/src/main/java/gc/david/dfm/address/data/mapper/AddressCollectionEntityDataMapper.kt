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

package gc.david.dfm.address.data.mapper

import gc.david.dfm.address.data.model.AddressCollectionEntity
import gc.david.dfm.address.data.model.GeocodingStatus
import gc.david.dfm.address.domain.GeocodingError
import gc.david.dfm.address.domain.GeocodingResult
import gc.david.dfm.address.domain.model.Address
import gc.david.dfm.address.domain.model.Coordinates

/**
 * Created by david on 13.01.17.
 *
 *
 * Mapper class used to transform [AddressCollectionEntity] in the Data layer
 * to [GeocodingResult] in the Domain layer.
 */
class AddressCollectionEntityDataMapper {

    fun transform(addressCollectionEntity: AddressCollectionEntity): GeocodingResult =
        when (addressCollectionEntity.status) {
            GeocodingStatus.OK, GeocodingStatus.ZERO_RESULTS -> {
                val addresses = addressCollectionEntity.results.map {
                    val location = it.geometry.location
                    Address(it.formattedAddress, Coordinates(location.latitude, location.longitude))
                }
                if (addresses.isEmpty()) GeocodingResult.NoResults else GeocodingResult.Found(addresses)
            }
            GeocodingStatus.INVALID_REQUEST -> GeocodingResult.Rejected(GeocodingError.INVALID_REQUEST)
            GeocodingStatus.OVER_DAILY_LIMIT -> GeocodingResult.Rejected(GeocodingError.OVER_DAILY_LIMIT)
            GeocodingStatus.OVER_QUERY_LIMIT -> GeocodingResult.Rejected(GeocodingError.OVER_QUERY_LIMIT)
            GeocodingStatus.REQUEST_DENIED -> GeocodingResult.Rejected(GeocodingError.REQUEST_DENIED)
            GeocodingStatus.UNKNOWN_ERROR -> GeocodingResult.Rejected(GeocodingError.UNKNOWN_ERROR)
        }
}
