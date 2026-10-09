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

package gc.david.dfm.address.data

import gc.david.dfm.address.data.mapper.AddressCollectionEntityDataMapper
import gc.david.dfm.address.data.model.AddressCollectionEntity
import gc.david.dfm.address.domain.GeocodingError
import gc.david.dfm.address.domain.GeocodingResult
import gc.david.dfm.address.domain.model.Address
import gc.david.dfm.address.domain.model.Coordinates
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.IOException

class BaseAddressRepositoryTest {

    private val remoteDataSource = mock<AddressRemoteDataSource>()
    private val mapper = mock<AddressCollectionEntityDataMapper>()
    private val repository = BaseAddressRepository(remoteDataSource, mapper)

    @Test
    fun `getNameByCoordinates returns the mapped remote response`() = runTest {
        val entity = AddressCollectionEntity()
        val mapped = GeocodingResult.Found(listOf(Address("New York", COORDINATES)))
        whenever(remoteDataSource.getNameByCoordinates(COORDINATES)).thenReturn(entity)
        whenever(mapper.transform(entity)).thenReturn(mapped)

        val result = repository.getNameByCoordinates(COORDINATES)

        assertEquals(mapped, result)
    }

    @Test
    fun `getCoordinatesByName returns the mapped remote response`() = runTest {
        val entity = AddressCollectionEntity()
        val mapped = GeocodingResult.Rejected(GeocodingError.INVALID_REQUEST)
        whenever(remoteDataSource.getCoordinatesByName(NAME)).thenReturn(entity)
        whenever(mapper.transform(entity)).thenReturn(mapped)

        val result = repository.getCoordinatesByName(NAME)

        assertEquals(mapped, result)
    }

    @Test
    fun `propagates remote data source failures`() = runTest {
        val exception = IOException("Network error")
        whenever(remoteDataSource.getCoordinatesByName(NAME)).thenAnswer { throw exception }

        val thrown = runCatching { repository.getCoordinatesByName(NAME) }.exceptionOrNull()

        assertSame(exception, thrown)
    }

    private companion object {

        const val NAME = "New York"
        val COORDINATES = Coordinates(40.7128, -74.0060)
    }
}
