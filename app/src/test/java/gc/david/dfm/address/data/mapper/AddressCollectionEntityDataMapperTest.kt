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

package gc.david.dfm.address.data.mapper

import org.junit.Test

import java.util.ArrayList

import gc.david.dfm.address.data.model.AddressCollectionEntity
import gc.david.dfm.address.data.model.Geometry
import gc.david.dfm.address.data.model.Location
import gc.david.dfm.address.data.model.Result
import gc.david.dfm.address.domain.model.Address
import gc.david.dfm.address.domain.model.AddressCollection

import org.hamcrest.core.Is.`is`
import org.junit.Assert.*

/**
 * Created by david on 15.01.17.
 */
class AddressCollectionEntityDataMapperTest {

    @Test
    fun transformsAddressCollectionEntityToAddressCollection() {
        // Given
        val fakeAddress = "address"
        val latitude = 1.0
        val longitude = 2.0
        val location = Location.Builder().withLatitude(latitude).withLongitude(longitude).build()
        val geometry = Geometry.Builder().withLocation(location).build()
        val result = Result.Builder().withFormattedAddress(fakeAddress).withGeometry(geometry).build()
        val results = ArrayList<Result>()
        results.add(result)
        val addressCollectionEntity = AddressCollectionEntity.Builder().withResults(results)
                .build()

        // When
        val addressCollection = AddressCollectionEntityDataMapper().transform(
                addressCollectionEntity)

        // Then
        assertEquals(1, addressCollection!!.addressList.size.toLong())
        val address = addressCollection.addressList[0]
        assertThat(address.coordinates.latitude, `is`(latitude))
        assertThat(address.coordinates.longitude, `is`(longitude))
        assertThat(address.formattedAddress, `is`(fakeAddress))
    }

}