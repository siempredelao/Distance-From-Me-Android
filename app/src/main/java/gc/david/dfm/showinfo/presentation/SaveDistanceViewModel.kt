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

package gc.david.dfm.showinfo.presentation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import gc.david.dfm.R
import gc.david.dfm.ResourceProvider
import gc.david.dfm.database.Distance
import gc.david.dfm.database.Position
import gc.david.dfm.distance.domain.SaveDistanceUseCase
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

class SaveDistanceViewModel(
    private val saveDistanceUseCase: SaveDistanceUseCase,
    private val resourceProvider: ResourceProvider
) : ViewModel() {

    val errorMessage = MutableLiveData<String>()

    private lateinit var inputParams: InputParams

    fun onStart(positionsList: List<LatLng>, distance: String) {
        this.inputParams = InputParams(positionsList, distance)
    }

    fun onSave(name: String) {
        val distanceAsDistance = Distance(id = null, name = name, distance = inputParams.distance, date = Date())

        val positionList = inputParams.positionsList.map {
            Position(id = null, latitude = it.latitude, longitude = it.longitude, distanceId = -1L) // FIXME
        }

        viewModelScope.launch {
            val result = saveDistanceUseCase(distanceAsDistance, positionList)

            result.fold({
                if (name.isNotEmpty()) {
                    val message = resourceProvider.get(R.string.alias_dialog_with_name_toast)
                    errorMessage.value = String.format(message, name)
                } else {
                    errorMessage.value = resourceProvider.get(R.string.alias_dialog_no_name_toast)
                }
            },{
                Timber.tag(TAG).d(Exception("Unable to insert distance into database."))
                errorMessage.value = "Unable to save distance. Try again later." // TODO translate
            })
        }
    }

    data class InputParams(val positionsList: List<LatLng>, val distance: String)

    companion object {

        private const val TAG = "SaveDistanceViewModel"
    }
}