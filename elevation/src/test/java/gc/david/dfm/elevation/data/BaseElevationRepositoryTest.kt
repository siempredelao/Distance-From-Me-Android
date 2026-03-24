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

package gc.david.dfm.elevation.data

import gc.david.dfm.elevation.data.mapper.ElevationEntityDataMapper
import gc.david.dfm.elevation.data.model.ElevationEntity
import gc.david.dfm.elevation.data.model.ElevationStatus
import gc.david.dfm.elevation.data.model.Result
import gc.david.dfm.elevation.domain.model.Elevation
import gc.david.dfm.elevation.domain.model.ElevationStatus as DomainElevationStatus
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class BaseElevationRepositoryTest {

    private val remoteDataSource = mock<ElevationRemoteDataSource>()
    private val mapper = ElevationEntityDataMapper()
    private val repository = BaseElevationRepository(remoteDataSource, mapper)

    @Test
    fun `getElevation returns mapped elevation from remote data source`() = runTest {
        val coordinatesPath = "40.7128,-74.0060|40.7589,-73.9851"
        val maxSamples = 100
        val elevationEntity = ElevationEntity(
            results = listOf(Result(10.5), Result(20.3)),
            status = ElevationStatus.OK
        )
        val expectedElevation = Elevation(
            results = listOf(10.5, 20.3),
            status = DomainElevationStatus.OK
        )
        whenever(remoteDataSource.getElevation(coordinatesPath, maxSamples))
            .thenReturn(elevationEntity)

        val result = repository.getElevation(coordinatesPath, maxSamples)

        assertEquals(expectedElevation.results, result.results)
        assertEquals(expectedElevation.status, result.status)
        verify(remoteDataSource).getElevation(coordinatesPath, maxSamples)
    }

    @Test
    fun `getElevation handles empty results`() = runTest {
        val coordinatesPath = "0.0,0.0"
        val maxSamples = 10
        val elevationEntity = ElevationEntity(
            results = emptyList(),
            status = ElevationStatus.OK
        )
        whenever(remoteDataSource.getElevation(coordinatesPath, maxSamples))
            .thenReturn(elevationEntity)

        val result = repository.getElevation(coordinatesPath, maxSamples)

        assertEquals(0, result.results.size)
        assertEquals(DomainElevationStatus.OK, result.status)
    }

    @Test
    fun `getElevation handles error status`() = runTest {
        val coordinatesPath = "invalid"
        val maxSamples = 10
        val elevationEntity = ElevationEntity(
            results = emptyList(),
            status = ElevationStatus.INVALID_REQUEST
        )
        whenever(remoteDataSource.getElevation(coordinatesPath, maxSamples))
            .thenReturn(elevationEntity)

        val result = repository.getElevation(coordinatesPath, maxSamples)

        assertEquals(DomainElevationStatus.INVALID_REQUEST, result.status)
    }

    @Test
    fun `getElevation maps multiple elevation points correctly`() = runTest {
        val coordinatesPath = "path"
        val maxSamples = 5
        val elevations = listOf(100.0, 150.0, 200.0, 175.0, 125.0)
        val elevationEntity = ElevationEntity(
            results = elevations.map { Result(it) },
            status = ElevationStatus.OK
        )
        whenever(remoteDataSource.getElevation(coordinatesPath, maxSamples))
            .thenReturn(elevationEntity)

        val result = repository.getElevation(coordinatesPath, maxSamples)

        assertEquals(5, result.results.size)
        assertEquals(elevations, result.results)
    }
}

