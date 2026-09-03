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
import gc.david.dfm.elevation.domain.model.Elevation
import gc.david.dfm.elevation.domain.model.ElevationStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class GetElevationByCoordinatesUseCaseTest {

    private val repository = mock<ElevationRepository>()

    private val useCase = GetElevationByCoordinatesUseCase(repository)

    @Test
    fun `returns error when coordinates list is empty`() = runTest {
        val coordinatesList = emptyList<Coordinates>()

        val result = useCase.invoke(coordinatesList)

        assertEquals("Empty coordinates list", result.exceptionOrNull()!!.message)
    }

    @Test
    fun `returns elevation when repository call succeeds and status is OK`() = runTest {
        val coordinatesList = mutableListOf(Coordinates(0.0, 0.0))
        val elevationResults = listOf(1.0)
        val elevation = Elevation(elevationResults, ElevationStatus.OK)
        whenever(repository.getElevation(any(), any())).thenReturn(elevation)

        val result = useCase.invoke(coordinatesList)

        assertEquals(kotlin.Result.success(elevation), result)
    }

    @Test
    fun `returns error when repository call succeeds but status is not OK`() = runTest {
        val coordinatesList = mutableListOf(Coordinates(0.0, 0.0))
        val elevationResults = listOf(1.0)
        val elevation = Elevation(elevationResults, ElevationStatus.INVALID_REQUEST)
        whenever(repository.getElevation(any(), any())).thenReturn(elevation)

        val result = useCase.invoke(coordinatesList)

        assertEquals(ElevationStatus.INVALID_REQUEST.toString(), result.exceptionOrNull()!!.message)
    }

    @Test
    fun `returns error when repository call fails`() = runTest {
        val coordinatesList = mutableListOf(Coordinates(0.0, 0.0))
        val throwable = Throwable()
        whenever(repository.getElevation(any(), any())).thenAnswer { throw throwable }

        val result = useCase.invoke(coordinatesList)

        assertEquals(kotlin.Result.failure<Elevation>(throwable), result)
    }

    @Test
    fun `builds coordinates path for list with one coordinate`() = runTest {
        val coordinatesList = mutableListOf(Coordinates(0.0, 0.0))
        val coordinatesPath = "0.0,0.0"
        val maxSamples = 100

        useCase.invoke(coordinatesList)

        verify(repository).getElevation(eq(coordinatesPath), eq(maxSamples))
    }

    @Test
    fun `builds coordinates path for list with more than one coordinate`() = runTest {
        val coordinatesLists = mutableListOf(Coordinates(0.0, 0.0), Coordinates(1.0, 1.0))
        val coordinatesPath = "0.0,0.0|1.0,1.0"
        val maxSamples = 100

        useCase.invoke(coordinatesLists)

        verify(repository).getElevation(eq(coordinatesPath), eq(maxSamples))
    }
}