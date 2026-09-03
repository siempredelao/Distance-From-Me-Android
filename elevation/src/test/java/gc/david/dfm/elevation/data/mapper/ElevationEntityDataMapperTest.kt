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

package gc.david.dfm.elevation.data.mapper

import gc.david.dfm.elevation.data.model.ElevationEntity
import gc.david.dfm.elevation.data.model.Result
import gc.david.dfm.elevation.data.model.ElevationStatus as DataElevationStatus
import gc.david.dfm.elevation.domain.model.ElevationStatus as DomainElevationStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ElevationEntityDataMapperTest {

    private val mapper = ElevationEntityDataMapper()

    @Test
    fun `transform maps OK status correctly`() {
        val entity = ElevationEntity(
            results = listOf(
                Result(elevation = 100.5),
                Result(elevation = 200.7)
            ),
            status = DataElevationStatus.OK
        )

        val result = mapper.transform(entity)

        assertEquals(2, result.results.size)
        assertEquals(100.5, result.results[0])
        assertEquals(200.7, result.results[1])
        assertEquals(DomainElevationStatus.OK, result.status)
    }

    @Test
    fun `transform maps INVALID_REQUEST status correctly`() {
        val entity = ElevationEntity(
            results = emptyList(),
            status = DataElevationStatus.INVALID_REQUEST
        )

        val result = mapper.transform(entity)

        assertEquals(0, result.results.size)
        assertEquals(DomainElevationStatus.INVALID_REQUEST, result.status)
    }

    @Test
    fun `transform maps OVER_QUERY_LIMIT status correctly`() {
        val entity = ElevationEntity(
            results = emptyList(),
            status = DataElevationStatus.OVER_QUERY_LIMIT
        )

        val result = mapper.transform(entity)

        assertEquals(DomainElevationStatus.OVER_QUERY_LIMIT, result.status)
    }

    @Test
    fun `transform maps REQUEST_DENIED status correctly`() {
        val entity = ElevationEntity(
            results = emptyList(),
            status = DataElevationStatus.REQUEST_DENIED
        )

        val result = mapper.transform(entity)

        assertEquals(DomainElevationStatus.REQUEST_DENIED, result.status)
    }

    @Test
    fun `transform maps UNKNOWN_ERROR status correctly`() {
        val entity = ElevationEntity(
            results = emptyList(),
            status = DataElevationStatus.UNKNOWN_ERROR
        )

        val result = mapper.transform(entity)

        assertEquals(DomainElevationStatus.UNKNOWN_ERROR, result.status)
    }

    @Test
    fun `transform handles single elevation result`() {
        val entity = ElevationEntity(
            results = listOf(Result(elevation = 543.21)),
            status = DataElevationStatus.OK
        )

        val result = mapper.transform(entity)

        assertEquals(1, result.results.size)
        assertEquals(543.21, result.results[0])
    }

    @Test
    fun `transform handles multiple elevation results`() {
        val elevations = listOf(10.0, 20.0, 30.0, 40.0, 50.0)
        val entity = ElevationEntity(
            results = elevations.map { Result(elevation = it) },
            status = DataElevationStatus.OK
        )

        val result = mapper.transform(entity)

        assertEquals(5, result.results.size)
        assertEquals(elevations, result.results)
    }
}


