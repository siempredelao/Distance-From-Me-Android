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

import gc.david.dfm.address.data.model.AddressCollectionEntity
import gc.david.dfm.address.data.model.GeocodingStatus
import gc.david.dfm.address.domain.model.Coordinates
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class BaseAddressRepositoryTest {

    private val remoteDataSource = mock<AddressRemoteDataSource>()
    private val repository = BaseAddressRepository(remoteDataSource)

    @Test
    fun `getNameByCoordinates returns result from remote data source`() = runTest {
        val coordinates = Coordinates(40.7128, -74.0060)
        val expectedResult = AddressCollectionEntity(
            status = GeocodingStatus.OK,
            results = emptyList()
        )
        whenever(remoteDataSource.getNameByCoordinates(coordinates)).thenReturn(expectedResult)

        val result = repository.getNameByCoordinates(coordinates)

        assertEquals(expectedResult, result)
        verify(remoteDataSource).getNameByCoordinates(coordinates)
    }

    @Test
    fun `getCoordinatesByName returns result from remote data source`() = runTest {
        val name = "New York"
        val expectedResult = AddressCollectionEntity(
            status = GeocodingStatus.OK,
            results = emptyList()
        )
        whenever(remoteDataSource.getCoordinatesByName(name)).thenReturn(expectedResult)

        val result = repository.getCoordinatesByName(name)

        assertEquals(expectedResult, result)
        verify(remoteDataSource).getCoordinatesByName(name)
    }

    @Test
    fun `getNameByCoordinates handles zero results status`() = runTest {
        val coordinates = Coordinates(0.0, 0.0)
        val expectedResult = AddressCollectionEntity(
            status = GeocodingStatus.ZERO_RESULTS,
            results = emptyList()
        )
        whenever(remoteDataSource.getNameByCoordinates(coordinates)).thenReturn(expectedResult)

        val result = repository.getNameByCoordinates(coordinates)

        assertEquals(GeocodingStatus.ZERO_RESULTS, result.status)
    }

    @Test
    fun `getCoordinatesByName handles invalid request status`() = runTest {
        val name = ""
        val expectedResult = AddressCollectionEntity(
            status = GeocodingStatus.INVALID_REQUEST,
            results = emptyList()
        )
        whenever(remoteDataSource.getCoordinatesByName(name)).thenReturn(expectedResult)

        val result = repository.getCoordinatesByName(name)

        assertEquals(GeocodingStatus.INVALID_REQUEST, result.status)
    }
}

