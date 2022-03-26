/*
 * Copyright (c) 2022 David Aguiar Gonzalez
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

package gc.david.dfm.distance.domain

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class ClearDistancesUseCaseTest {

    private val repository = mock<DistanceRepository>()

    private val useCase = ClearDistancesUseCase(repository)

    @Test
    fun `returns Unit on success`() = runTest {
        whenever(repository.clear()).thenReturn(Unit)

        val result = useCase.invoke()

        assertEquals(Result.success(Unit), result)
    }

    @Test
    fun `returns error message on failure`() = runTest {
        val throwable = Throwable()
        whenever(repository.clear()).thenAnswer { throw throwable }

        val result = useCase.invoke()

        assertEquals(Result.failure<Unit>(throwable), result)
    }
}