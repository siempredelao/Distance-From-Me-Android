/*
 * Copyright (c) 2019 David Aguiar Gonzalez
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

import gc.david.dfm.address.data.model.*
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Test

/**
 * Created by david on 15.01.17.
 */
class AddressCollectionEntityDataMapperTest {

    @Test
    fun `transforms address collection entity to address collection`() {
        val fakeAddress = "address"
        val latitude = 1.0
        val longitude = 2.0
        val location = Location(latitude, longitude)
        val geometry = Geometry(location)
        val result = Result(fakeAddress, geometry)
        val results = mutableListOf(result)
        val addressCollectionEntity = AddressCollectionEntity(results, GeocodingStatus.OK)

        val addressCollection = AddressCollectionEntityDataMapper().transform(addressCollectionEntity)

        assertEquals(1, addressCollection.addressList.size)
        val address = addressCollection.addressList[0]
        assertThat(address.coordinates.latitude, `is`(latitude))
        assertThat(address.coordinates.longitude, `is`(longitude))
        assertThat(address.formattedAddress, `is`(fakeAddress))
    }
}