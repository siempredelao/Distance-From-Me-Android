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

package gc.david.dfm.address.data.mapper

import com.google.android.gms.maps.model.LatLng
import gc.david.dfm.address.data.model.AddressCollectionEntity
import gc.david.dfm.address.domain.model.Address
import gc.david.dfm.address.domain.model.AddressCollection

/**
 * Created by david on 13.01.17.
 *
 *
 * Mapper class used to transform [AddressCollectionEntity] in the Data layer
 * to [AddressCollection] in the Domain layer.
 */
class AddressCollectionEntityDataMapper {

    fun transform(addressCollectionEntity: AddressCollectionEntity): AddressCollection {
        val addressList = addressCollectionEntity.results.map {
            val location = it.geometry.location
            Address(it.formattedAddress, LatLng(location.latitude, location.longitude))
        }
        return AddressCollection(addressList)
    }
}
