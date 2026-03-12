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

package gc.david.dfm.showinfo.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import gc.david.dfm.common.ResourceProvider
import gc.david.dfm.core.distances.data.database.Distance
import gc.david.dfm.core.distances.data.database.Position
import gc.david.dfm.core.distances.domain.SaveDistanceUseCase
import gc.david.dfm.showinfo.R
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*

class SaveDistanceViewModel(
    private val saveDistanceUseCase: SaveDistanceUseCase,
    private val resourceProvider: ResourceProvider,
) : ViewModel() {

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private lateinit var inputParams: InputParams

    fun onStart(positionsList: List<LatLng>, distance: String) {
        this.inputParams = InputParams(positionsList, distance)
    }

    fun onSave(name: String) {
        val distanceAsDistance =
            Distance(id = null, name = name, distance = inputParams.distance, date = Date())

        val positionList = inputParams.positionsList.map {
            Position(
                id = null,
                latitude = it.latitude,
                longitude = it.longitude,
                distanceId = -1L // FIXME
            )
        }

        viewModelScope.launch {
            withContext(NonCancellable) {
                val result = saveDistanceUseCase(distanceAsDistance, positionList)

                result.fold({
                    val message = if (name.isNotEmpty()) {
                        resourceProvider.get(R.string.alias_dialog_with_name_toast, name)
                    } else {
                        resourceProvider.get(R.string.alias_dialog_no_name_toast)
                    }
                    _userMessage.update { message }
                }, {
                    Timber.tag(TAG).e(it, "Unable to insert distance into database.")
                    _userMessage.update { resourceProvider.get(R.string.save_distance_error) }
                })
            }
        }
    }

    fun onUserMessageShown() {
        _userMessage.update { null }
    }

    data class InputParams(val positionsList: List<LatLng>, val distance: String)

    companion object {

        private const val TAG = "SaveDistanceViewModel"
    }
}
