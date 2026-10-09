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

import androidx.lifecycle.ViewModel
import gc.david.dfm.common.launchCatching
import gc.david.dfm.common.logUnexpectedFailure
import gc.david.dfm.common.presentation.ResourceProvider
import gc.david.dfm.opensource.R
import gc.david.dfm.opensource.domain.GetOpenSourceLibrariesUseCase
import gc.david.dfm.opensource.presentation.mapper.OpenSourceLibraryUiMapper
import gc.david.dfm.opensource.presentation.model.OpenSourceUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class OpenSourceViewModel(
    private val getOpenSourceLibrariesUseCase: GetOpenSourceLibrariesUseCase,
    private val uiMapper: OpenSourceLibraryUiMapper,
    private val resourceProvider: ResourceProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow<OpenSourceUiState>(OpenSourceUiState.Loading)
    val uiState: StateFlow<OpenSourceUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    fun onStart() {
        // The Activity calls this on every creation, so a configuration change must not reload
        if (loadJob?.isActive == true || _uiState.value is OpenSourceUiState.Content) return

        _uiState.update { OpenSourceUiState.Loading }

        loadJob = launchCatching(onError = { error ->
            logUnexpectedFailure(TAG, error)
            _uiState.update {
                OpenSourceUiState.Error(resourceProvider.get(R.string.opensourcelibrary_error_message))
            }
        }) {
            val libraries = getOpenSourceLibrariesUseCase()
            _uiState.update { OpenSourceUiState.Content(uiMapper(libraries)) }
        }
    }

    private companion object {

        const val TAG = "OpenSourceViewModel"
    }
}
