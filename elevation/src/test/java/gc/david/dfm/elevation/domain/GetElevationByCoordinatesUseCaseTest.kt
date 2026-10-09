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

package gc.david.dfm.elevation.domain

import gc.david.dfm.common.Coordinates
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.io.IOException

class GetElevationByCoordinatesUseCaseTest {

    private val repository = mock<ElevationRepository>()

    private val useCase = GetElevationByCoordinatesUseCase(repository)

    @Test
    fun `rejects an empty coordinates list without calling the repository`() = runTest {
        val thrown = runCatching { useCase(emptyList()) }.exceptionOrNull()

        assertTrue(thrown is IllegalArgumentException)
        verifyNoInteractions(repository)
    }

    @Test
    fun `returns the repository result`() = runTest {
        val found = ElevationResult.Found(listOf(1.0))
        whenever(repository.getElevation(any(), any())).thenReturn(found)

        val result = useCase(listOf(Coordinates(0.0, 0.0)))

        assertEquals(found, result)
    }

    @Test
    fun `builds coordinates path for list with one coordinate`() = runTest {
        whenever(repository.getElevation(any(), any())).thenReturn(ElevationResult.NoData)

        useCase(listOf(Coordinates(0.0, 0.0)))

        verify(repository).getElevation(eq("0.0,0.0"), eq(100))
    }

    @Test
    fun `builds coordinates path for list with more than one coordinate`() = runTest {
        whenever(repository.getElevation(any(), any())).thenReturn(ElevationResult.NoData)

        useCase(listOf(Coordinates(0.0, 0.0), Coordinates(1.0, 1.0)))

        verify(repository).getElevation(eq("0.0,0.0|1.0,1.0"), eq(100))
    }

    @Test
    fun `propagates repository failures`() = runTest {
        val exception = IOException("Network error")
        whenever(repository.getElevation(any(), any())).thenAnswer { throw exception }

        val thrown = runCatching { useCase(listOf(Coordinates(0.0, 0.0))) }.exceptionOrNull()

        assertSame(exception, thrown)
    }
}
