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

package gc.david.dfm.elevation.presentation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import gc.david.dfm.ConnectionManager
import gc.david.dfm.DFMPreferences
import gc.david.dfm.Event
import gc.david.dfm.PreferencesProvider
import gc.david.dfm.elevation.domain.GetElevationByCoordinatesUseCase
import gc.david.dfm.elevation.presentation.model.ElevationModel
import gc.david.dfm.map.Haversine
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

class ElevationViewModel(
    private val getElevationByCoordinatesUseCase: GetElevationByCoordinatesUseCase,
    private val connectionManager: ConnectionManager,
    private val preferencesProvider: PreferencesProvider
) : ViewModel() {

    val elevationSamples = MutableLiveData<ElevationModel>()
    val hideChartEvent = MutableLiveData<Event<Unit>>()

    private val locale: Locale
        get() {
            val defaultUnit = preferencesProvider.getMeasureUnitPreference()
            return if (DFMPreferences.MEASURE_AMERICAN_UNIT_VALUE == defaultUnit) Locale.US else Locale.FRANCE
        }

    fun onCoordinatesSelected(coordinates: List<LatLng>) {
        if (!preferencesProvider.shouldShowElevationChart() || !connectionManager.isOnline()) {
            hideChartEvent.value = Event(Unit)
            return
        }

        viewModelScope.launch {
            getElevationByCoordinatesUseCase(coordinates).fold({
                val normalizedElevationList =
                    it.results.map { Haversine.normalizeAltitudeByLocale(it, locale) }
                val altitudeUnit = Haversine.getAltitudeUnitByLocale(locale)
                elevationSamples.value = ElevationModel(normalizedElevationList, altitudeUnit)
            }, {
                Timber.tag(TAG).e(it)
            })
        }
    }

    companion object {

        private const val TAG = "ElevationViewModel"
    }
}