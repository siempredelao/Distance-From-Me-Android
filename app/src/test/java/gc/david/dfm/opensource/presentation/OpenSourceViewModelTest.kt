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

package gc.david.dfm.opensource.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import gc.david.dfm.CoroutineDispatcherRule
import gc.david.dfm.R
import gc.david.dfm.ResourceProvider
import gc.david.dfm.opensource.data.model.OpenSourceLibraryEntity
import gc.david.dfm.opensource.domain.OpenSourceUseCase
import gc.david.dfm.opensource.presentation.mapper.OpenSourceLibraryMapper
import gc.david.dfm.opensource.presentation.model.OpenSourceLibraryModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class OpenSourceViewModelTest {

    private val useCase = mock<OpenSourceUseCase>()
    private val mapper = mock<OpenSourceLibraryMapper>()
    private val resourceProvider = mock<ResourceProvider>()

    private val viewModel = OpenSourceViewModel(useCase, mapper, resourceProvider)

    @get:Rule var instantTaskRule: TestRule = InstantTaskExecutorRule()
    @get:Rule val coroutinesDispatcherRule = CoroutineDispatcherRule()

    @Test
    fun `onStart Given use case succeeds Then returns result mapped as presentation models`() = runTest {
        val libraryEntities = listOf(DUMMY_LIBRARY_ENTITY)
        whenever(useCase()).thenReturn(Result.success(libraryEntities))
        val libraryModel = DUMMY_LIBRARY_MODEL
        whenever(mapper.transform(DUMMY_LIBRARY_ENTITY)).thenReturn(libraryModel)

        viewModel.onStart()

        verify(useCase)()
        assertEquals(listOf(libraryModel), viewModel.openSourceList.value)
    }

    @Test
    fun `onStart Given use case fails Then returns error message`() = runTest {
        whenever(useCase()).thenReturn(Result.failure(Throwable()))
        val errorMessage = "error message"
        whenever(resourceProvider.get(R.string.opensourcelibrary_error_message)).thenReturn(errorMessage)

        viewModel.onStart()

        verify(useCase)()
        assertEquals(errorMessage, viewModel.errorMessage.value)
    }

    companion object {

        private val DUMMY_LIBRARY_ENTITY = OpenSourceLibraryEntity("", "", "", "", "", "", "")
        private val DUMMY_LIBRARY_MODEL = OpenSourceLibraryModel("", "", "", "", "", "", "")
    }
}

