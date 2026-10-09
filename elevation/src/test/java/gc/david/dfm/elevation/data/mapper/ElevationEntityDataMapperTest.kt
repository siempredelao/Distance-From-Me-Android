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
import gc.david.dfm.elevation.data.model.ElevationStatus
import gc.david.dfm.elevation.data.model.Result
import gc.david.dfm.elevation.domain.ElevationError
import gc.david.dfm.elevation.domain.ElevationResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class ElevationEntityDataMapperTest {

    private val mapper = ElevationEntityDataMapper()

    @Test
    fun `transforms OK with results to Found samples`() {
        val entity = ElevationEntity(
            results = listOf(Result(elevation = 100.5), Result(elevation = 200.7)),
            status = ElevationStatus.OK
        )

        val result = mapper.transform(entity)

        assertEquals(ElevationResult.Found(listOf(100.5, 200.7)), result)
    }

    @Test
    fun `transforms OK without results to NoData`() {
        val entity = ElevationEntity(results = emptyList(), status = ElevationStatus.OK)

        val result = mapper.transform(entity)

        assertEquals(ElevationResult.NoData, result)
    }

    @ParameterizedTest
    @CsvSource(
        "INVALID_REQUEST, INVALID_REQUEST",
        "OVER_QUERY_LIMIT, OVER_QUERY_LIMIT",
        "REQUEST_DENIED, REQUEST_DENIED",
        "UNKNOWN_ERROR, UNKNOWN_ERROR",
    )
    fun `transforms a rejecting status to Rejected with the matching error`(
        status: ElevationStatus,
        error: ElevationError,
    ) {
        val entity = ElevationEntity(results = emptyList(), status = status)

        val result = mapper.transform(entity)

        assertEquals(ElevationResult.Rejected(error), result)
    }
}
