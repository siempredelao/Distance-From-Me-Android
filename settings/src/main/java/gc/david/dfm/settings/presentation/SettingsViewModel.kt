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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gc.david.dfm.common.presentation.ResourceProvider
import gc.david.dfm.core.distances.domain.ClearDistancesUseCase
import gc.david.dfm.settings.R
import gc.david.dfm.settings.presentation.model.SettingsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class SettingsViewModel(
    private val clearDistancesUseCase: ClearDistancesUseCase,
    private val resourceProvider: ResourceProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun onClearData() {
        Timber.tag(TAG).d("onClearData")

        viewModelScope.launch {
            clearDistancesUseCase().fold({
                _uiState.update { 
                    it.copy(successMessage = resourceProvider.get(R.string.toast_distances_deleted))
                }
            },{
                Timber.tag(TAG).e(Exception("Unable to clear database."))
            })
        }
    }

    fun onMessageShown() {
        _uiState.update { it.copy(successMessage = null) }
    }

    companion object {

        private const val TAG = "SettingsViewModel"
    }
}
