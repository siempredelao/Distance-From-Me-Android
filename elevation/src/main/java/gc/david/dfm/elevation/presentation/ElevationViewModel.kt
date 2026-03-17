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

package gc.david.dfm.elevation.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gc.david.dfm.ConnectionManager
import gc.david.dfm.common.Coordinates
import gc.david.dfm.common.domain.model.UnitSystem
import gc.david.dfm.common.presentation.DistanceFormatter
import gc.david.dfm.elevation.domain.GetElevationByCoordinatesUseCase
import gc.david.dfm.elevation.presentation.model.ElevationModel
import gc.david.dfm.elevation.presentation.model.ElevationUiState
import gc.david.dfm.settings.domain.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class ElevationViewModel(
    private val getElevationByCoordinatesUseCase: GetElevationByCoordinatesUseCase,
    private val connectionManager: ConnectionManager,
    private val settingsRepository: SettingsRepository,
    private val distanceFormatter: DistanceFormatter
) : ViewModel() {

    private val _uiState = MutableStateFlow(ElevationUiState())
    val uiState: StateFlow<ElevationUiState> = _uiState.asStateFlow()

    private val unitSystem: UnitSystem
        get() = settingsRepository.getUnitSystemPreference()

    fun onCoordinatesSelected(coordinates: List<Coordinates>) {
        if (!settingsRepository.shouldShowElevationChart() || !connectionManager.isOnline()) {
            _uiState.update { it.copy(hideChart = true) }
            return
        }

        viewModelScope.launch {
            getElevationByCoordinatesUseCase(coordinates).fold({
                val normalizedElevationList =
                    it.results.map { elevation -> 
                        distanceFormatter.formatAltitude(elevation, unitSystem)
                    }
                val altitudeUnit = distanceFormatter.getAltitudeUnitLabel(unitSystem)
                _uiState.update { current ->
                    current.copy(elevationModel = ElevationModel(normalizedElevationList, altitudeUnit))
                }
            }, {
                Timber.tag(TAG).e(it)
            })
        }
    }

    fun onHideChartHandled() {
        _uiState.update { it.copy(hideChart = false) }
    }

    private companion object {

        const val TAG = "ElevationViewModel"
    }
}
