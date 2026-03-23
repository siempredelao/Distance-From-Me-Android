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

package gc.david.dfm.opensource.data

import gc.david.dfm.opensource.data.mapper.OpenSourceLibraryMapper
import gc.david.dfm.opensource.data.model.OpenSourceLibraryEntity
import gc.david.dfm.opensource.domain.model.OpenSourceLibrary
import org.junit.jupiter.api.Assertions.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class BaseOpenSourceRepositoryTest {

    private val localDataSource = mock<OpenSourceDiskDataSource>()
    private val mapper = mock<OpenSourceLibraryMapper>()

    private val repository = BaseOpenSourceRepository(localDataSource, mapper)

    @Test
    fun `returns mapped libraries from local datasource`() = runTest {
        val libraryEntity = mock<OpenSourceLibraryEntity>()
        whenever(localDataSource.getOpenSourceLibraries()).thenReturn(listOf(libraryEntity))
        val library = mock<OpenSourceLibrary>()
        whenever(mapper.invoke(libraryEntity)).thenReturn(library)

        val actualOpenSourceLibraries = repository.getOpenSourceLibraries()

        assertEquals(listOf(library), actualOpenSourceLibraries)
    }
}