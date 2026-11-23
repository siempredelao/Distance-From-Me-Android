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

package gc.david.dfm.opensource.domain

import gc.david.dfm.opensource.domain.model.License
import gc.david.dfm.opensource.domain.model.OpenSourceLibrary
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.IOException

/**
 * Created by david on 26.01.17.
 */
class GetOpenSourceLibrariesUseCaseTest {

    private val repository = mock<OpenSourceRepository>()

    private val useCase = GetOpenSourceLibrariesUseCase(repository)

    @Test
    fun `returns the libraries from the repository`() = runTest {
        val libraries = listOf(OpenSourceLibrary("Koin", "", "", "", "", License.APACHE_V2, ""))
        whenever(repository.getOpenSourceLibraries()).thenReturn(libraries)

        val result = useCase()

        assertEquals(libraries, result)
    }

    @Test
    fun `propagates repository failures`() = runTest {
        val exception = IOException("Read error")
        whenever(repository.getOpenSourceLibraries()).thenAnswer { throw exception }

        val thrown = runCatching { useCase() }.exceptionOrNull()

        assertSame(exception, thrown)
    }
}
