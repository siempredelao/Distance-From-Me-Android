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

import gc.david.dfm.core.distances.data.database.model.DistanceEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Date

class DistanceEntityMapperTest {

    private val mapper = DistanceEntityMapper()

    @Test
    fun `maps entity to domain model correctly`() {
        val date = Date()
        val entity = DistanceEntity(
            id = 1L,
            name = "Test Distance",
            distance = "10.5 km",
            date = date
        )

        val result = mapper.toDomain(entity)

        assertEquals(1L, result.id)
        assertEquals("Test Distance", result.name)
        assertEquals("10.5 km", result.distance)
        assertEquals(date, result.date)
    }

    @Test
    fun `maps entity with null id correctly using default value`() {
        val date = Date()
        val entity = DistanceEntity(
            id = null,
            name = "Test Distance",
            distance = "5.0 km",
            date = date
        )

        val result = mapper.toDomain(entity)

        assertEquals(0L, result.id)
        assertEquals("Test Distance", result.name)
        assertEquals("5.0 km", result.distance)
        assertEquals(date, result.date)
    }

    @Test
    fun `maps entity list to domain list correctly`() {
        val date1 = Date()
        val date2 = Date()
        val entities = listOf(
            DistanceEntity(id = 1L, name = "Distance 1", distance = "10 km", date = date1),
            DistanceEntity(id = 2L, name = "Distance 2", distance = "20 km", date = date2)
        )

        val result = mapper.toDomainList(entities)

        assertEquals(2, result.size)
        assertEquals(1L, result[0].id)
        assertEquals("Distance 1", result[0].name)
        assertEquals(2L, result[1].id)
        assertEquals("Distance 2", result[1].name)
    }

    @Test
    fun `maps empty list correctly`() {
        val result = mapper.toDomainList(emptyList())

        assertEquals(0, result.size)
    }
}


