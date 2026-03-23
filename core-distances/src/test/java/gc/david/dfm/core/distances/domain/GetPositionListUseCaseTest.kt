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

import gc.david.dfm.core.distances.domain.model.Position
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class GetPositionListUseCaseTest {

    private val repository = mock<DistanceRepository>()

    private val useCase = GetPositionListUseCase(repository)

    @Test
    fun `returns position list on success`() = runTest {
        val distanceId = 1L
        val positions = listOf(
            Position(latitude = 1.0, longitude = 2.0),
            Position(latitude = 3.0, longitude = 4.0)
        )
        whenever(repository.getPositionListById(distanceId)).thenReturn(positions)

        val result = useCase(distanceId)

        assertTrue(result.isSuccess)
        assertEquals(positions, result.getOrNull())
        verify(repository).getPositionListById(distanceId)
    }

    @Test
    fun `returns empty list when no positions found`() = runTest {
        val distanceId = 1L
        whenever(repository.getPositionListById(distanceId)).thenReturn(emptyList())

        val result = useCase(distanceId)

        assertTrue(result.isSuccess)
        assertEquals(emptyList<Position>(), result.getOrNull())
    }

    @Test
    fun `returns failure on error`() = runTest {
        val distanceId = 1L
        val exception = Exception("Database error")
        whenever(repository.getPositionListById(distanceId)).thenAnswer { throw exception }

        val result = useCase(distanceId)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}

