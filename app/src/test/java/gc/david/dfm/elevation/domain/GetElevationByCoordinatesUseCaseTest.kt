/*
 * Copyright (c) 2022 David Aguiar Gonzalez
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

import com.google.android.gms.maps.model.LatLng
import gc.david.dfm.elevation.data.mapper.ElevationEntityDataMapper
import gc.david.dfm.elevation.data.model.ElevationEntity
import gc.david.dfm.elevation.data.model.ElevationStatus
import gc.david.dfm.elevation.data.model.Result
import gc.david.dfm.elevation.domain.model.Elevation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class GetElevationByCoordinatesUseCaseTest {

    private val repository = mock<ElevationRepository>()
    private val mapper = mock<ElevationEntityDataMapper>()

    private val useCase = GetElevationByCoordinatesUseCase(repository, mapper)

    @Test
    fun `returns error when coordinate list is empty`() = runTest {
        val coordinateList = emptyList<LatLng>()

        val result = useCase.invoke(coordinateList)

        assertEquals("Empty coordinates list", result.exceptionOrNull()!!.message)
    }

    @Test
    fun `returns mapped elevation when repository call succeeds and status is OK`() = runTest {
        val coordinateList = mutableListOf(LatLng(0.0, 0.0))
        val elevation = 1.0
        val results = listOf(Result(elevation))
        val elevationEntity = ElevationEntity(results, ElevationStatus.OK)
        whenever(repository.getElevation(any(), any())).thenReturn(elevationEntity)
        val elevationResults = mutableListOf(elevation)
        val elevation1 = Elevation(elevationResults)
        whenever(mapper.transform(elevationEntity)).thenReturn(elevation1)

        val result = useCase.invoke(coordinateList)

        assertEquals(kotlin.Result.success(elevation1), result)
    }

    @Test
    fun `returns error when repository call succeeds but status is not OK`() = runTest {
        val coordinateList = mutableListOf(LatLng(0.0, 0.0))
        val elevation = 1.0
        val results = listOf(Result(elevation))
        val elevationEntity = ElevationEntity(results, ElevationStatus.INVALID_REQUEST)
        whenever(repository.getElevation(any(), any())).thenReturn(elevationEntity)

        val result = useCase.invoke(coordinateList)

        assertEquals(ElevationStatus.INVALID_REQUEST.toString(), result.exceptionOrNull()!!.message)
    }

    @Test
    fun `returns error when repository call fails`() = runTest {
        val coordinateList = mutableListOf(LatLng(0.0, 0.0))
        val throwable = Throwable()
        whenever(repository.getElevation(any(), any())).thenAnswer { throw throwable }

        val result = useCase.invoke(coordinateList)

        assertEquals(kotlin.Result.failure<Elevation>(throwable), result)
    }

    @Test
    fun `builds coordinates path for list with one coordinate`() = runTest {
        val coordinateList = mutableListOf(LatLng(0.0, 0.0))
        val coordinatesPath = "0.0,0.0"
        val maxSamples = 100

        useCase.invoke(coordinateList)

        verify(repository).getElevation(eq(coordinatesPath), eq(maxSamples))
    }

    @Test
    fun `builds coordinates path for list with more than one coordinate`() = runTest {
        val coordinateList = mutableListOf(LatLng(0.0, 0.0), LatLng(1.0, 1.0))
        val coordinatesPath = "0.0,0.0|1.0,1.0"
        val maxSamples = 100

        useCase.invoke(coordinateList)

        verify(repository).getElevation(eq(coordinatesPath), eq(maxSamples))
    }
}