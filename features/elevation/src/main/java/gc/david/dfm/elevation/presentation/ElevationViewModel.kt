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
import gc.david.dfm.core.coordinates.domain.model.Coordinates
import gc.david.dfm.core.settings.domain.SettingsRepository
import gc.david.dfm.core.units.domain.model.UnitSystem
import gc.david.dfm.core.units.presentation.UnitFormatter
import gc.david.dfm.elevation.domain.GetElevationByCoordinatesUseCase
import gc.david.dfm.elevation.domain.model.ElevationResult
import gc.david.dfm.elevation.presentation.model.ElevationUiModel
import gc.david.dfm.elevation.presentation.model.ElevationUiState
import gc.david.dfm.support.android.domain.ConnectionManager
import gc.david.dfm.support.coroutines.launchCatching
import gc.david.dfm.support.coroutines.logUnexpectedFailure
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

class ElevationViewModel(
    private val getElevationByCoordinatesUseCase: GetElevationByCoordinatesUseCase,
    private val connectionManager: ConnectionManager,
    private val settingsRepository: SettingsRepository,
    private val unitFormatter: UnitFormatter
) : ViewModel() {

    private val _uiState = MutableStateFlow(ElevationUiState())
    val uiState: StateFlow<ElevationUiState> = _uiState.asStateFlow()

    private val unitSystem: UnitSystem
        get() = settingsRepository.getUnitSystemPreference()

    private var elevationJob: Job? = null

    fun onCoordinatesSelected(coordinates: List<Coordinates>) {
        // Only the latest coordinates matter, so a slower earlier request must not overwrite them
        elevationJob?.cancel()
        // The current profile belongs to the previous path, so drop it until this path's profile arrives
        _uiState.update { it.copy(elevation = null) }

        if (coordinates.isEmpty() || !settingsRepository.shouldShowElevationChart() || !connectionManager.isOnline()) {
            _uiState.update { it.copy(showChart = false) }
            return
        }

        elevationJob = launchCatching(onError = { logUnexpectedFailure(TAG, it) }) {
            when (val result = getElevationByCoordinatesUseCase(coordinates)) {
                is ElevationResult.Found -> showChart(result.samples)
                ElevationResult.NoData -> Unit
                is ElevationResult.Rejected -> Timber.tag(TAG).e("Elevation rejected: %s", result.error)
            }
        }
    }

    private fun showChart(samples: List<Double>) {
        val normalizedElevationList = samples.map { elevation ->
            unitFormatter.formatAltitude(elevation, unitSystem)
        }.toImmutableList()
        val altitudeUnit = unitFormatter.getAltitudeUnitLabel(unitSystem)
        _uiState.update { current ->
            current.copy(
                elevation = ElevationUiModel(normalizedElevationList, altitudeUnit),
                showChart = true
            )
        }
    }

    fun onShowChartHandled() {
        _uiState.update { it.copy(showChart = false) }
    }

    private companion object {

        const val TAG = "ElevationViewModel"
    }
}
