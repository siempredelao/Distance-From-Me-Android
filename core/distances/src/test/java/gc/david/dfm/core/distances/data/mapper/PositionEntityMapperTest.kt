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

package gc.david.dfm.core.distances.data.mapper

import gc.david.dfm.core.distances.data.database.model.PositionEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PositionEntityMapperTest {

    private val mapper = PositionEntityMapper()

    @Test
    fun `maps entity to domain model correctly`() {
        val entity = PositionEntity(
            id = 1L,
            distanceId = 1L,
            latitude = 40.7128,
            longitude = -74.0060
        )

        val result = mapper.toDomain(entity)

        assertEquals(40.7128, result.latitude)
        assertEquals(-74.0060, result.longitude)
    }

    @Test
    fun `maps entity list to domain list correctly`() {
        val entities = listOf(
            PositionEntity(id = 1L, distanceId = 1L, latitude = 40.7128, longitude = -74.0060),
            PositionEntity(id = 2L, distanceId = 1L, latitude = 51.5074, longitude = -0.1278)
        )

        val result = mapper.toDomainList(entities)

        assertEquals(2, result.size)
        assertEquals(40.7128, result[0].latitude)
        assertEquals(-74.0060, result[0].longitude)
        assertEquals(51.5074, result[1].latitude)
        assertEquals(-0.1278, result[1].longitude)
    }

    @Test
    fun `maps empty list correctly`() {
        val result = mapper.toDomainList(emptyList())

        assertEquals(0, result.size)
    }
}


