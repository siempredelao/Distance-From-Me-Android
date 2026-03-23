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

package gc.david.dfm.opensource.presentation

import gc.david.dfm.common.presentation.ResourceProvider
import gc.david.dfm.opensource.R
import gc.david.dfm.opensource.domain.GetOpenSourceLibrariesUseCase
import gc.david.dfm.opensource.domain.model.License
import gc.david.dfm.opensource.domain.model.OpenSourceLibrary
import gc.david.dfm.opensource.presentation.mapper.OpenSourceLibraryUiMapper
import gc.david.dfm.opensource.presentation.model.OpenSourceLibraryUiModel
import gc.david.dfm.opensource.presentation.model.OpenSourceUiState
import gc.david.dfm.testsupport.CoroutineDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class OpenSourceViewModelTest {

    private val useCase = mock<GetOpenSourceLibrariesUseCase>()
    private val uiMapper = mock<OpenSourceLibraryUiMapper>()
    private val resourceProvider = mock<ResourceProvider>()

    private val viewModel = OpenSourceViewModel(useCase, uiMapper, resourceProvider)

    @get:Rule val coroutinesDispatcherRule = CoroutineDispatcherRule()

    @Test
    fun `onStart Given use case succeeds Then returns Content with mapped models`() = runTest {
        val libraryEntities = listOf(DUMMY_LIBRARY)
        whenever(useCase()).thenReturn(Result.success(libraryEntities))
        val libraryModel = DUMMY_LIBRARY_UI_MODEL
        whenever(uiMapper(libraryEntities)).thenReturn(listOf(libraryModel))

        viewModel.onStart()

        verify(useCase)()
        val state = viewModel.uiState.value
        assertEquals(OpenSourceUiState.Content(listOf(libraryModel)), state)
    }

    @Test
    fun `onStart Given use case fails Then returns Error with message`() = runTest {
        whenever(useCase()).thenReturn(Result.failure(Throwable()))
        val errorMessage = "error message"
        whenever(resourceProvider.get(R.string.opensourcelibrary_error_message)).thenReturn(errorMessage)

        viewModel.onStart()

        verify(useCase)()
        val state = viewModel.uiState.value
        assertEquals(OpenSourceUiState.Error(errorMessage), state)
    }

    companion object {

        private val DUMMY_LIBRARY = OpenSourceLibrary("", "", "", "", "", License.MIT, "")
        private val DUMMY_LIBRARY_UI_MODEL =
            OpenSourceLibraryUiModel("", "", "", "", "", "", "", "")
    }
}
