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

package gc.david.dfm.faq.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gc.david.dfm.common.ResourceProvider
import gc.david.dfm.faq.R
import gc.david.dfm.faq.domain.GetFaqsUseCase
import gc.david.dfm.faq.presentation.model.FaqUiState
import kotlinx.coroutines.launch

internal class FaqViewModel(
    private val getFaqsUseCase: GetFaqsUseCase,
    private val resourceProvider: ResourceProvider
) : ViewModel() {

    private val _uiState = MutableLiveData<FaqUiState>(FaqUiState.Loading)
    val uiState: LiveData<FaqUiState> = _uiState

    fun onStart() {
        _uiState.value = FaqUiState.Loading

        viewModelScope.launch {
            val result = getFaqsUseCase()

            result.fold(
                onSuccess = { faqs ->
                    _uiState.postValue(FaqUiState.Content(faqs.toList()))
                },
                onFailure = {
                    _uiState.postValue(FaqUiState.Error(resourceProvider.get(R.string.faq_error_message)))
                }
            )
        }
    }
}
