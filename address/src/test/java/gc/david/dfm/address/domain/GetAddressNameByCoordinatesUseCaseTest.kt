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

class GetAddressNameByCoordinatesUseCaseTest {

    private val repository = mock<AddressRepository>()

    private val useCase = GetAddressNameByCoordinatesUseCase(repository)

    @Test
    fun `returns the repository result`() = runTest {
        whenever(repository.getNameByCoordinates(COORDINATES)).thenReturn(GeocodingResult.NoResults)

        val result = useCase(COORDINATES)

        assertEquals(GeocodingResult.NoResults, result)
    }

    @Test
    fun `limits Found to one address`() = runTest {
        val addresses = listOf(
            Address("Address 1", Coordinates(1.0, 1.0)),
            Address("Address 2", Coordinates(2.0, 2.0)),
            Address("Address 3", Coordinates(3.0, 3.0))
        )
        whenever(repository.getNameByCoordinates(COORDINATES)).thenReturn(GeocodingResult.Found(addresses))

        val result = useCase(COORDINATES)

        assertEquals(GeocodingResult.Found(addresses.take(1)), result)
    }

    @Test
    fun `propagates repository failures`() = runTest {
        val exception = IOException("Network error")
        whenever(repository.getNameByCoordinates(COORDINATES)).thenAnswer { throw exception }

        val thrown = runCatching { useCase(COORDINATES) }.exceptionOrNull()

        assertSame(exception, thrown)
    }

    private companion object {

        val COORDINATES = Coordinates(40.7128, -74.0060)
    }
}
