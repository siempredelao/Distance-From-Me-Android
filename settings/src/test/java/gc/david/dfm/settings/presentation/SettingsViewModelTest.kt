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

package gc.david.dfm.settings.presentation

import gc.david.dfm.common.presentation.ResourceProvider
import gc.david.dfm.core.distances.domain.ClearDistancesUseCase
import gc.david.dfm.settings.presentation.model.SettingsUiState
import gc.david.dfm.testsupport.CoroutineDispatcherRule
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

/**
 * Created by david on 24.01.17.
 */
@ExperimentalCoroutinesApi
class SettingsViewModelTest {

    private val clearDistancesUseCase = mock<ClearDistancesUseCase>()
    private val resourceProvider = mock<ResourceProvider>()

    private val viewModel = SettingsViewModel(clearDistancesUseCase, resourceProvider)

    @get:Rule val coroutinesDispatcherRule = CoroutineDispatcherRule()

    @Test
    fun `onClearData Given use case succeeds Then shows success message`() = runTest {
        whenever(clearDistancesUseCase()).thenReturn(Result.success(Unit))
        val successMessage = "success"
        whenever(resourceProvider.get(any())).thenReturn(successMessage)

        viewModel.onClearData()

        assertEquals(SettingsUiState(successMessage = successMessage), viewModel.uiState.value)
    }
}