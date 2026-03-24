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

package gc.david.dfm.distance.data

import gc.david.dfm.common.Coordinates
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class BaseCoordinatesRepositoryTest {

    private val memoryDataSource = mock<CoordinatesMemoryDataSource>()
    private val repository = BaseCoordinatesRepository(memoryDataSource)

    @Test
    fun `observeDistance returns flow from memory data source`() = runTest {
        val coordinates = listOf(
            Coordinates(40.7128, -74.0060),
            Coordinates(51.5074, -0.1278)
        )
        val flow = MutableStateFlow(coordinates)
        whenever(memoryDataSource.observe()).thenReturn(flow)

        val result = repository.observeDistance().first()

        assertEquals(coordinates, result)
        verify(memoryDataSource).observe()
    }

    @Test
    fun `observeDistance returns empty list initially`() = runTest {
        val flow = MutableStateFlow<List<Coordinates>>(emptyList())
        whenever(memoryDataSource.observe()).thenReturn(flow)

        val result = repository.observeDistance().first()

        assertEquals(emptyList<Coordinates>(), result)
    }

    @Test
    fun `append delegates to memory data source`() {
        val coordinates = Coordinates(40.7128, -74.0060)

        repository.append(coordinates)

        verify(memoryDataSource).append(coordinates)
    }

    @Test
    fun `clear delegates to memory data source`() {
        repository.clear()

        verify(memoryDataSource).clear()
    }

    @Test
    fun `multiple appends are delegated correctly`() {
        val coord1 = Coordinates(40.7128, -74.0060)
        val coord2 = Coordinates(51.5074, -0.1278)
        val coord3 = Coordinates(35.6762, 139.6503)

        repository.append(coord1)
        repository.append(coord2)
        repository.append(coord3)

        verify(memoryDataSource).append(coord1)
        verify(memoryDataSource).append(coord2)
        verify(memoryDataSource).append(coord3)
    }
}

