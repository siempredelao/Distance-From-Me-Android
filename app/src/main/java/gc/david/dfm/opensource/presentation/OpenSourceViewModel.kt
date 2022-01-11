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

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import gc.david.dfm.R
import gc.david.dfm.ResourceProvider
import gc.david.dfm.opensource.data.model.OpenSourceLibraryEntity
import gc.david.dfm.opensource.domain.OpenSourceInteractor
import gc.david.dfm.opensource.presentation.mapper.OpenSourceLibraryMapper
import gc.david.dfm.opensource.presentation.model.OpenSourceLibraryModel

class OpenSourceViewModel(
        private val openSourceUseCase: OpenSourceInteractor,
        private val openSourceLibraryMapper: OpenSourceLibraryMapper,
        private val resourceProvider: ResourceProvider
) : ViewModel() {

    val progressVisibility = MutableLiveData<Boolean>()
    val openSourceList = MutableLiveData<List<OpenSourceLibraryModel>>()
    val errorMessage = MutableLiveData<String>()

    fun onStart() {
        progressVisibility.value = true

        openSourceUseCase.execute(object : OpenSourceInteractor.Callback {
            override fun onOpenSourceLibrariesLoaded(openSourceLibraryEntityList: List<OpenSourceLibraryEntity>) {
                progressVisibility.value = false
                openSourceList.value = openSourceLibraryMapper.transform(openSourceLibraryEntityList)
            }

            override fun onError(message: String) {
                progressVisibility.value = false
                errorMessage.value = resourceProvider.get(R.string.opensourcelibrary_error_message)
            }
        })
    }
}