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

package gc.david.dfm.settings.presentation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import gc.david.dfm.R
import gc.david.dfm.ResourceProvider
import gc.david.dfm.distance.domain.ClearDistancesInteractor
import timber.log.Timber

class SettingsViewModel(
        private val clearDistancesUseCase: ClearDistancesInteractor,
        private val resourceProvider: ResourceProvider
) : ViewModel() {

    val resultMessage = MutableLiveData<String>()

    fun onClearData() {
        Timber.tag(TAG).d("onClearData")

        clearDistancesUseCase.execute(object : ClearDistancesInteractor.Callback {
            override fun onClear() {
                resultMessage.value = resourceProvider.get(R.string.toast_distances_deleted)
            }

            override fun onError() {
                Timber.tag(TAG).e(Exception("Unable to clear database."))
            }
        })
    }

    companion object {

        private const val TAG = "SettingsViewModel"
    }
}