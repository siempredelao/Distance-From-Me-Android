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

import gc.david.dfm.address.domain.model.Address
import gc.david.dfm.address.domain.model.Coordinates
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.IOException

/**
 * Created by david on 15.01.17.
 */
class GetAddressCoordinatesByNameUseCaseTest {

    private val repository = mock<AddressRepository>()

    private val useCase = GetAddressCoordinatesByNameUseCase(repository)

    @Test
    fun `returns the repository result`() = runTest {
        val rejected = GeocodingResult.Rejected(GeocodingError.INVALID_REQUEST)
        whenever(repository.getCoordinatesByName(LOCATION_NAME)).thenReturn(rejected)

        val result = useCase(LOCATION_NAME)

        assertEquals(rejected, result)
    }

    @Test
    fun `limits Found to five addresses`() = runTest {
        val addresses = (1..10).map { Address("Address $it", Coordinates(it.toDouble(), it.toDouble())) }
        whenever(repository.getCoordinatesByName(LOCATION_NAME)).thenReturn(GeocodingResult.Found(addresses))

        val result = useCase(LOCATION_NAME)

        assertEquals(GeocodingResult.Found(addresses.take(5)), result)
    }

    @Test
    fun `propagates repository failures`() = runTest {
        val exception = IOException("Network error")
        whenever(repository.getCoordinatesByName(LOCATION_NAME)).thenAnswer { throw exception }

        val thrown = runCatching { useCase(LOCATION_NAME) }.exceptionOrNull()

        assertSame(exception, thrown)
    }

    companion object {

        private const val LOCATION_NAME = "Berlin DE"
    }
}
