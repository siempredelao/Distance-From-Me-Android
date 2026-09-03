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
import gc.david.dfm.address.data.model.*
import gc.david.dfm.address.domain.model.Address
import gc.david.dfm.address.domain.model.AddressCollection
import gc.david.dfm.address.domain.model.Coordinates
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Created by david on 15.01.17.
 */
@ExperimentalCoroutinesApi
class GetAddressCoordinatesByNameUseCaseTest {

    private val repository = mock<AddressRepository>()
    private val mapper = mock<AddressCollectionEntityDataMapper>()

    private val useCase = GetAddressCoordinatesByNameUseCase(repository, mapper)

    @Test
    fun `loads empty address when address collection model is correct`() = runTest {
        val addressCollectionEntity =
            AddressCollectionEntity(emptyList(), GeocodingStatus.ZERO_RESULTS)
        whenever(repository.getCoordinatesByName(anyString()))
            .thenReturn(addressCollectionEntity)
        val addressCollection = AddressCollection(mutableListOf())
        whenever(mapper.transform(addressCollectionEntity)).thenReturn(addressCollection)

        val result = useCase.invoke(LOCATION_NAME)

        assertEquals(kotlin.Result.success(addressCollection), result)
    }

    @Test
    fun `loads non empty address when address collection model is correct`() = runTest {
        val latitude = 1.0
        val longitude = 1.0
        val location = Location(latitude, longitude)
        val geometry = Geometry(location)
        val results = ArrayList<Result>()
        results.add(Result(LOCATION_NAME, geometry))
        val addressCollectionEntity = AddressCollectionEntity(results, GeocodingStatus.OK)
        whenever(repository.getCoordinatesByName(anyString()))
            .thenReturn(addressCollectionEntity)
        val address = Address(LOCATION_NAME, Coordinates(latitude, longitude))
        val addressList = mutableListOf(address)
        val addressCollection = AddressCollection(addressList)
        whenever(mapper.transform(addressCollectionEntity)).thenReturn(addressCollection)

        val result = useCase.invoke(LOCATION_NAME)

        assertEquals(kotlin.Result.success(addressCollection), result)
    }

    @Test
    fun `returns error when address collection model is not correct`() = runTest {
        val addressCollectionEntity =
            AddressCollectionEntity(status = GeocodingStatus.INVALID_REQUEST)
        whenever(repository.getCoordinatesByName(anyString())).thenReturn(addressCollectionEntity)

        val result = useCase.invoke(LOCATION_NAME)

        assertTrue(result.exceptionOrNull() is GeocodingException.InvalidRequest)
    }

    @Test
    fun `returns error when repository call fails`() = runTest {
        val throwable = Throwable()
        whenever(repository.getCoordinatesByName(anyString()))
            .thenAnswer { throw throwable }

        val result = useCase.invoke(LOCATION_NAME)

        assertEquals(kotlin.Result.failure<AddressCollectionEntity>(throwable), result)
    }

    companion object {

        private const val LOCATION_NAME = "Berlin DE"
    }
}