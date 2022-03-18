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

package gc.david.dfm.faq.presentation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gc.david.dfm.R
import gc.david.dfm.ResourceProvider
import gc.david.dfm.faq.data.model.Faq
import gc.david.dfm.faq.domain.GetFaqsUseCase
import kotlinx.coroutines.launch

class FaqViewModel(
    private val getFaqsUseCase: GetFaqsUseCase,
    private val resourceProvider: ResourceProvider
) : ViewModel() {

    val progressVisibility = MutableLiveData<Boolean>()
    val faqList = MutableLiveData<Set<Faq>>()
    val errorMessage = MutableLiveData<String>()

    fun onStart() {
        progressVisibility.value = true

        viewModelScope.launch {
            val result = getFaqsUseCase()
            progressVisibility.postValue(false)

            result.fold({
                faqList.postValue(it)
            }, {
                errorMessage.postValue(resourceProvider.get(R.string.faq_error_message))
            })
        }
    }
}