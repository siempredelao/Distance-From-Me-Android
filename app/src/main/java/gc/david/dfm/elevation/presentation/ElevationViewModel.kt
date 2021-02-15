/*
 * Copyright (c) 2021 David Aguiar Gonzalez
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
import com.google.android.gms.maps.model.LatLng
import gc.david.dfm.ConnectionManager
import gc.david.dfm.Event
import gc.david.dfm.PreferencesProvider
import gc.david.dfm.elevation.domain.ElevationInteractor
import timber.log.Timber

class ElevationViewModel(
        private val elevationUseCase: ElevationInteractor,
        private val connectionManager: ConnectionManager,
        private val preferencesProvider: PreferencesProvider
) : ViewModel() {

    val elevationSamples = MutableLiveData<List<Double>>()
    val hideChartEvent = MutableLiveData<Event<Unit>>()

    fun onCoordinatesSelected(coordinates: List<LatLng>) {
        if (!preferencesProvider.shouldShowElevationChart() || !connectionManager.isOnline()) {
            hideChartEvent.value = Event(Unit)
            return
        }

        elevationUseCase.execute(coordinates, ELEVATION_SAMPLES, object : ElevationInteractor.Callback {
            override fun onElevationLoaded(elevation: gc.david.dfm.elevation.domain.model.Elevation) {
                elevationSamples.value = elevation.results
            }

            override fun onError(message: String) {
                Timber.tag(TAG).e(Exception(message))
            }
        })
    }

    companion object {

        private const val TAG = "ElevationViewModel"
        private const val ELEVATION_SAMPLES = 100
    }
}