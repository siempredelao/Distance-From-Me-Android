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

package gc.david.dfm.core.distances.domain

import gc.david.dfm.core.distances.domain.model.Distance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

class GetDistancesUseCaseTest {

    private val repository = mock<DistanceRepository>()

    private val useCase = GetDistancesUseCase(repository)

    @Test
    fun `returns flow of distances from repository`() = runTest {
        val distances = listOf(
            Distance(id = 1L, name = "Distance 1", distance = "10 km", date = Date()),
            Distance(id = 2L, name = "Distance 2", distance = "20 km", date = Date())
        )
        whenever(repository.loadDistances()).thenReturn(flowOf(distances))

        val result = useCase().first()

        assertEquals(distances, result)
    }

    @Test
    fun `returns empty flow when no distances`() = runTest {
        whenever(repository.loadDistances()).thenReturn(flowOf(emptyList()))

        val result = useCase().first()

        assertEquals(emptyList<Distance>(), result)
    }
}

