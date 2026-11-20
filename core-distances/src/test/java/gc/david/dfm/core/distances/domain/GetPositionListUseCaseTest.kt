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
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GetPositionListUseCaseTest {

    private val repository = mock<DistanceRepository>()

    private val useCase = GetPositionListUseCase(repository)

    @Test
    fun `returns the positions found by the repository`() = runTest {
        val found = PositionListResult.Found(
            listOf(
                Position(latitude = 1.0, longitude = 2.0),
                Position(latitude = 3.0, longitude = 4.0)
            )
        )
        whenever(repository.getPositionListById(1L)).thenReturn(found)

        assertEquals(found, useCase(1L))
    }

    @Test
    fun `returns not found when the repository finds no positions`() = runTest {
        whenever(repository.getPositionListById(1L)).thenReturn(PositionListResult.NotFound)

        assertEquals(PositionListResult.NotFound, useCase(1L))
    }

    @Test
    fun `propagates repository failures`() = runTest {
        val exception = IllegalStateException("Database error")
        whenever(repository.getPositionListById(1L)).thenAnswer { throw exception }

        val thrown = assertThrows<IllegalStateException> { useCase(1L) }

        assertSame(exception, thrown)
    }
}
