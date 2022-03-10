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

package gc.david.dfm.opensource.domain

import gc.david.dfm.opensource.data.model.OpenSourceLibraryEntity
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Created by david on 26.01.17.
 */
@ExperimentalCoroutinesApi
class GetOpenSourceLibrariesUseCaseTest {

    private val repository = mock<OpenSourceRepository>()

    private val useCase = GetOpenSourceLibrariesUseCase(repository)

    @Test
    fun `returns open source library list on success`() = runTest {
        val openSourceLibraryEntityList = emptyList<OpenSourceLibraryEntity>()
        whenever(repository.getOpenSourceLibraries()).thenReturn(openSourceLibraryEntityList)

        val result = useCase.invoke()

        assertEquals(Result.success(openSourceLibraryEntityList), result)
    }

    @Test
    fun `returns error message on failure`() = runTest {
        val throwable = Throwable()
        whenever(repository.getOpenSourceLibraries()).thenAnswer { throw throwable }

        val result = useCase.invoke()

        assertEquals(Result.failure<List<OpenSourceLibraryEntity>>(throwable), result)
    }
}