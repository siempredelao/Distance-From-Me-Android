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

package gc.david.dfm.core.distances.data

import gc.david.dfm.core.distances.domain.model.Distance
import gc.david.dfm.core.distances.domain.model.NewDistance
import gc.david.dfm.core.distances.domain.model.NewPosition
import gc.david.dfm.core.distances.domain.model.Position
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

class BaseDistanceRepositoryTest {

    private val localDataSource = mock<DistanceLocalDataSource>()

    private val repository = BaseDistanceRepository(localDataSource)

    @Test
    fun `insert delegates to local data source`() = runTest {
        val newDistance = NewDistance(
            name = "Test",
            distanceText = "10 km",
            date = Date(),
            positions = listOf(NewPosition(1.0, 2.0))
        )

        repository.insert(newDistance)

        verify(localDataSource).insert(newDistance)
    }

    @Test
    fun `loadDistances returns flow from local data source`() = runTest {
        val distances = listOf(
            Distance(id = 1L, name = "Distance 1", distance = "10 km", date = Date())
        )
        whenever(localDataSource.loadDistances()).thenReturn(flowOf(distances))

        val result = repository.loadDistances().first()

        assertEquals(distances, result)
    }

    @Test
    fun `clear delegates to local data source`() = runTest {
        repository.clear()

        verify(localDataSource).clear()
    }

    @Test
    fun `getPositionListById returns positions from local data source`() = runTest {
        val distanceId = 1L
        val positions = listOf(
            Position(latitude = 1.0, longitude = 2.0),
            Position(latitude = 3.0, longitude = 4.0)
        )
        whenever(localDataSource.getPositionListById(distanceId)).thenReturn(positions)

        val result = repository.getPositionListById(distanceId)

        assertEquals(positions, result)
        verify(localDataSource).getPositionListById(distanceId)
    }
}

