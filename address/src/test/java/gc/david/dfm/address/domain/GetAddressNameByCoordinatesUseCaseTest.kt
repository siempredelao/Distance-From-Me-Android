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
import gc.david.dfm.address.data.model.AddressCollectionEntity
import gc.david.dfm.address.data.model.GeocodingStatus
import gc.david.dfm.address.domain.model.Address
import gc.david.dfm.address.domain.model.AddressCollection
import gc.david.dfm.address.domain.model.Coordinates
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GetAddressNameByCoordinatesUseCaseTest {

    private val repository = mock<AddressRepository>()
    private val mapper = mock<AddressCollectionEntityDataMapper>()

    private val useCase = GetAddressNameByCoordinatesUseCase(repository, mapper)

    @Test
    fun `returns success with limited addresses when status is OK`() = runTest {
        val coordinates = Coordinates(40.7128, -74.0060)
        val entity = AddressCollectionEntity(
            status = GeocodingStatus.OK,
            results = emptyList()
        )
        val addressCollection = AddressCollection(
            addressList = listOf(
                Address("Address 1", Coordinates(1.0, 1.0)),
                Address("Address 2", Coordinates(2.0, 2.0)),
                Address("Address 3", Coordinates(3.0, 3.0))
            )
        )
        whenever(repository.getNameByCoordinates(coordinates)).thenReturn(entity)
        whenever(mapper.transform(entity)).thenReturn(addressCollection)

        val result = useCase(coordinates)

        assertTrue(result.isSuccess)
        // Should limit to MAX_BY_COORD (1)
        assertEquals(1, result.getOrNull()?.addressList?.size)
        assertEquals("Address 1", result.getOrNull()?.addressList?.first()?.formattedAddress)
    }

    @Test
    fun `returns success when status is ZERO_RESULTS`() = runTest {
        val coordinates = Coordinates(40.7128, -74.0060)
        val entity = AddressCollectionEntity(
            status = GeocodingStatus.ZERO_RESULTS,
            results = emptyList()
        )
        val addressCollection = AddressCollection(addressList = emptyList())
        whenever(repository.getNameByCoordinates(coordinates)).thenReturn(entity)
        whenever(mapper.transform(entity)).thenReturn(addressCollection)

        val result = useCase(coordinates)

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.addressList?.size)
    }

    @Test
    fun `returns failure when status is OVER_QUERY_LIMIT`() = runTest {
        val coordinates = Coordinates(40.7128, -74.0060)
        val entity = AddressCollectionEntity(
            status = GeocodingStatus.OVER_QUERY_LIMIT,
            results = emptyList()
        )
        whenever(repository.getNameByCoordinates(coordinates)).thenReturn(entity)

        val result = useCase(coordinates)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is GeocodingException)
    }

    @Test
    fun `returns failure when status is REQUEST_DENIED`() = runTest {
        val coordinates = Coordinates(40.7128, -74.0060)
        val entity = AddressCollectionEntity(
            status = GeocodingStatus.REQUEST_DENIED,
            results = emptyList()
        )
        whenever(repository.getNameByCoordinates(coordinates)).thenReturn(entity)

        val result = useCase(coordinates)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is GeocodingException)
    }

    @Test
    fun `returns failure when status is INVALID_REQUEST`() = runTest {
        val coordinates = Coordinates(40.7128, -74.0060)
        val entity = AddressCollectionEntity(
            status = GeocodingStatus.INVALID_REQUEST,
            results = emptyList()
        )
        whenever(repository.getNameByCoordinates(coordinates)).thenReturn(entity)

        val result = useCase(coordinates)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is GeocodingException)
    }

    @Test
    fun `returns failure when repository throws exception`() = runTest {
        val coordinates = Coordinates(40.7128, -74.0060)
        val exception = Exception("Network error")
        whenever(repository.getNameByCoordinates(coordinates)).thenAnswer { throw exception }

        val result = useCase(coordinates)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `limits addresses to one when multiple addresses available`() = runTest {
        val coordinates = Coordinates(40.7128, -74.0060)
        val entity = AddressCollectionEntity(
            status = GeocodingStatus.OK,
            results = emptyList()
        )
        val addressCollection = AddressCollection(
            addressList = (1..10).map { 
                Address("Address $it", Coordinates(it.toDouble(), it.toDouble())) 
            }
        )
        whenever(repository.getNameByCoordinates(coordinates)).thenReturn(entity)
        whenever(mapper.transform(entity)).thenReturn(addressCollection)

        val result = useCase(coordinates)

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.addressList?.size)
        assertEquals("Address 1", result.getOrNull()?.addressList?.first()?.formattedAddress)
    }
}




