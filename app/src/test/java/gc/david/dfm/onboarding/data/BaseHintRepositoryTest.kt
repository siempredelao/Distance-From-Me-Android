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

package gc.david.dfm.onboarding.data

import gc.david.dfm.onboarding.domain.model.MapHint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class BaseHintRepositoryTest {

    private val localDataSource = mock<HintDiskDataSource>()

    @Test
    fun `has no seen hints at first`() = runTest {
        assertEquals(emptySet<MapHint>(), repositoryWith().observeSeenHints().first())
    }

    @Test
    fun `reads the stored hints`() = runTest {
        assertEquals(
            setOf(MapHint.START_ANYWHERE, MapHint.OPEN_DETAILS),
            repositoryWith("START_ANYWHERE", "OPEN_DETAILS").observeSeenHints().first()
        )
    }

    @Test
    fun `ignores stored hints that no longer exist`() = runTest {
        assertEquals(
            setOf(MapHint.OPEN_DETAILS),
            repositoryWith("A_FORMER_HINT", "OPEN_DETAILS").observeSeenHints().first()
        )
    }

    @Test
    fun `observes and stores a marked hint along with the earlier ones`() = runTest {
        val repository = repositoryWith("START_ANYWHERE")

        repository.markSeen(MapHint.OPEN_DETAILS)

        assertEquals(
            setOf(MapHint.START_ANYWHERE, MapHint.OPEN_DETAILS),
            repository.observeSeenHints().first()
        )
        verify(localDataSource).setSeenHints(setOf("START_ANYWHERE", "OPEN_DETAILS"))
    }

    @Test
    fun `does not store a seen hint again`() {
        repositoryWith("START_ANYWHERE").markSeen(MapHint.START_ANYWHERE)

        verify(localDataSource, never()).setSeenHints(any())
    }

    private fun repositoryWith(vararg stored: String): BaseHintRepository {
        whenever(localDataSource.getSeenHints()).thenReturn(stored.toSet())
        return BaseHintRepository(localDataSource)
    }
}
