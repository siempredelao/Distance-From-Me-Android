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

import gc.david.dfm.core.distances.domain.model.NewDistance
import gc.david.dfm.core.distances.domain.model.NewPosition
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

class SaveDistanceUseCaseTest {

    private val repository = mock<DistanceRepository>()

    private val useCase = SaveDistanceUseCase(repository)

    @Test
    fun `returns Unit on success`() = runTest {
        val newDistance = NewDistance(
            name = "Test Distance",
            distanceText = "10 km",
            date = Date(),
            positions = listOf(
                NewPosition(latitude = 1.0, longitude = 2.0),
                NewPosition(latitude = 3.0, longitude = 4.0)
            )
        )
        whenever(repository.insert(newDistance)).thenReturn(Unit)

        val result = useCase(newDistance)

        assertTrue(result.isSuccess)
        assertEquals(Unit, result.getOrNull())
        verify(repository).insert(newDistance)
    }

    @Test
    fun `returns failure on error`() = runTest {
        val newDistance = NewDistance(
            name = "Test Distance",
            distanceText = "10 km",
            date = Date(),
            positions = emptyList()
        )
        val exception = Exception("Database error")
        whenever(repository.insert(newDistance)).thenAnswer { throw exception }

        val result = useCase(newDistance)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}

