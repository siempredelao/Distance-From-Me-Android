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

package gc.david.dfm.main.presentation

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gc.david.dfm.*
import gc.david.dfm.common.BuildConfigProvider
import gc.david.dfm.common.Coordinates
import gc.david.dfm.common.ResourceProvider
import gc.david.dfm.common.domain.DistanceCalculator
import gc.david.dfm.common.domain.model.UnitSystem
import gc.david.dfm.common.presentation.DistanceFormatter
import gc.david.dfm.toCoordinates
import gc.david.dfm.distance.data.CurrentLocationProvider
import gc.david.dfm.distance.data.DistanceMode
import gc.david.dfm.distance.data.DistanceModeProvider
import gc.david.dfm.distance.domain.CoordinatesRepository
import gc.david.dfm.core.distances.domain.GetDistancesUseCase
import gc.david.dfm.core.distances.domain.GetPositionListUseCase
import gc.david.dfm.core.distances.domain.model.Distance
import gc.david.dfm.main.presentation.model.DrawDistanceModel
import gc.david.dfm.main.presentation.model.MainUiState
import gc.david.dfm.map.mapper.MapStateMapper
import gc.david.dfm.map.model.CameraUpdate
import gc.david.dfm.map.model.MarkerData
import gc.david.dfm.settings.domain.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class MainViewModel(
    private val getDistancesUseCase: GetDistancesUseCase,
    private val getPositionListUseCase: GetPositionListUseCase,
    private val connectionManager: ConnectionManager,
    private val resourceProvider: ResourceProvider,
    private val settingsRepository: SettingsRepository,
    private val distanceModeProvider: DistanceModeProvider,
    private val currentLocationProvider: CurrentLocationProvider,
    private val permissionChecker: PermissionChecker,
    private val coordinatesRepository: CoordinatesRepository,
    private val distanceCalculator: DistanceCalculator,
    private val distanceFormatter: DistanceFormatter,
    private val buildConfigProvider: BuildConfigProvider,
    private val mapStateMapper: MapStateMapper
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val distances = getDistancesUseCase()
        .catch { Timber.tag(TAG).e(it) }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        distances
            .map { it.isNotEmpty() }
            .onEach { hasDistances -> _uiState.update { it.copy(showLoadDistancesItem = hasDistances) } }
            .launchIn(viewModelScope)
    }

    // Moves to current position if app has just started
    private var appHasJustStarted = true
    // Determines whether a multi-point distance is being marked on the map
    private var calculatingDistance: Boolean = false

    private val coordinates get() = coordinatesRepository.observeDistance().value

    private val unitSystem: UnitSystem
        get() = settingsRepository.getUnitSystemPreference()

    fun onStart() {
        if (!connectionManager.isOnline()) {
            _uiState.update { it.copy(showConnectionIssue = true) }
        }
    }

    /**
     * Triggered when the menu is already built and ready to be updated.
     */
    fun onMenuReady() {
        _uiState.update { it.copy(showForceCrashItem = !buildConfigProvider.isReleaseBuild()) }
    }

    /**
     * Triggered when the user taps on the "Show distances" menu item.
     */
    fun onLoadDistancesClick() {
        val current = distances.value
        if (current.isNotEmpty()) {
            _uiState.update { it.copy(selectFromDistancesLoaded = current) }
        }
    }

    /**
     * Triggered when the user selects a distance from the loaded distances dialog.
     */
    fun onDistanceToShowSelected(distance: Distance) {
        viewModelScope.launch {
            val result = getPositionListUseCase(distance.id)

            result.fold({
                val coordinates = it.toCoordinates()
                coordinatesRepository.setList(coordinates)

                val distanceInMetres = distanceCalculator.calculateTotalDistance(coordinates)
                val model = DrawDistanceModel(
                    coordinates,
                    distance.name + "\n",
                    distanceInMetres,
                    distanceFormatter.formatDistance(distanceInMetres, unitSystem),
                    DrawDistanceModel.Source.DATABASE,
                    distanceModeProvider.get(),
                    settingsRepository.getCameraAnimation()
                )
                val newMapState = mapStateMapper.toMapUiState(model)
                _uiState.update { state ->
                    state.copy(
                        mapState = newMapState,
                        triggerElevationUpdate = coordinates
                    )
                }
            },{
                Timber.tag(TAG).e(Exception("Unable to get position by id."))
            })
        }
    }

    fun onDistanceFromCurrentPositionSet() {
        distanceModeProvider.set(DistanceMode.FROM_CURRENT_POINT)
        resetMap()
        if (!permissionChecker.isLocationPermissionGranted()) {
            _uiState.update { it.copy(showLocationPermissionSnackbar = true) }
        }
    }

    fun onDistanceFromAnyPositionSet() {
        distanceModeProvider.set(DistanceMode.FROM_ANY_POINT)
        resetMap()
    }

    fun onMyLocationButtonClick() {
        val currentLocation = currentLocationProvider.get()
        if (currentLocation != CurrentLocationProvider.UNDEFINED) {
            val coordinates = Coordinates(currentLocation.lat, currentLocation.lon)
            _uiState.update { 
                it.copy(mapState = it.mapState.copy(cameraUpdate = CameraUpdate.MoveTo(coordinates))) 
            }
        }
    }

    // TODO this should be moved to a repooooo!!!
    fun onLocationChanged(location: Location) {
        currentLocationProvider.set(location)

        if (appHasJustStarted) {
            Timber.tag(TAG).d("onLocationChanged appHasJustStarted")

            val coordinates = Coordinates(location.latitude, location.longitude)
            _uiState.update { 
                it.copy(mapState = it.mapState.copy(cameraUpdate = CameraUpdate.ZoomTo(coordinates))) 
            }
            appHasJustStarted = false
        }
    }

    fun onMapClick(coordinates: Coordinates) {
        Timber.tag(TAG).d("onMapClick $coordinates")
        if (distanceModeProvider.get() == DistanceMode.FROM_ANY_POINT) {
            if (!calculatingDistance) {
                coordinatesRepository.clear()
                _uiState.update {
                    it.copy(
                        mapState = it.mapState.copy(
                            clearMap = true,
                            markers = emptyList(),
                            polylines = emptyList()
                        )
                    )
                }
            }

            calculatingDistance = true
        } else {
            val currentLocation = currentLocationProvider.get()
            if (currentLocation == CurrentLocationProvider.UNDEFINED) {
                calculatingDistance = false
                return // Without current location, we cannot calculate any distance
            }

            if (!calculatingDistance) {
                coordinatesRepository.clear()
                _uiState.update {
                    it.copy(
                        mapState = it.mapState.copy(
                            clearMap = true,
                            markers = emptyList(),
                            polylines = emptyList()
                        )
                    )
                }
            }
            calculatingDistance = true

            // To calculate the distance from the current position,
            // we effectively need the current position ;)
            if (this@MainViewModel.coordinates.isEmpty()) {
                coordinatesRepository.append(Coordinates(currentLocation.lat, currentLocation.lon))
            }
        }
        coordinatesRepository.append(coordinates)
        
        // Update map with markers for each coordinate
        val markers = this@MainViewModel.coordinates.map { MarkerData(position = it) }
        _uiState.update { 
            it.copy(mapState = it.mapState.copy(markers = markers)) 
        }
    }

    fun onPositionByNameResolved(coordinates: Coordinates) {
        if (distanceModeProvider.get() == DistanceMode.FROM_ANY_POINT) {
            if (this@MainViewModel.coordinates.isNotEmpty()) {
                onMapLongClick(coordinates)
            } else {
                coordinatesRepository.append(coordinates)
                val markers = this@MainViewModel.coordinates.map { MarkerData(position = it) }
                _uiState.update {
                    it.copy(
                        mapState = it.mapState.copy(
                            markers = markers,
                            cameraUpdate = CameraUpdate.MoveTo(coordinates)
                        )
                    )
                }
            }
        } else {
            onMapLongClick(coordinates)
        }
    }

    fun onMapLongClick(coordinates: Coordinates) {
        Timber.tag(TAG).d("onMapLongClick $coordinates")
        calculatingDistance = true

        if (distanceModeProvider.get() == DistanceMode.FROM_ANY_POINT) {
            if (this@MainViewModel.coordinates.isEmpty()) {
                _uiState.update { it.copy(errorMessage = resourceProvider.get(R.string.toast_first_point_needed)) }
                return
            }
        } else {
            val currentLocation = currentLocationProvider.get()
            if (currentLocation == CurrentLocationProvider.UNDEFINED) {
                calculatingDistance = false
                return // Without current location, we cannot calculate any distance
            }

            // To calculate the distance from the current position,
            // we effectively need the current position ;)
            if (this@MainViewModel.coordinates.isEmpty()) {
                coordinatesRepository.append(Coordinates(currentLocation.lat, currentLocation.lon))
            }
        }

        coordinatesRepository.append(coordinates)

        val distanceInMetres = distanceCalculator.calculateTotalDistance(this@MainViewModel.coordinates)
        val model = DrawDistanceModel(
            this@MainViewModel.coordinates,
            "",
            distanceInMetres,
            distanceFormatter.formatDistance(distanceInMetres, unitSystem),
            DrawDistanceModel.Source.MANUAL,
            distanceModeProvider.get(),
            settingsRepository.getCameraAnimation()
        )
        val newMapState = mapStateMapper.toMapUiState(model)
        _uiState.update { 
            it.copy(
                mapState = newMapState,
                triggerElevationUpdate = model.positionList
            )
        }

        calculatingDistance = false
    }

    fun handleSearchIntent(query: String) {
        Timber.tag(TAG).d("handleSearchIntent $query")
        val currentLocation = currentLocationProvider.get()
        if (currentLocation != CurrentLocationProvider.UNDEFINED) {
            _uiState.update { it.copy(searchAddress = query) }
        }
    }

    fun resetMap() {
        calculatingDistance = false
        coordinatesRepository.clear()
        _uiState.update { 
            it.copy(
                mapState = it.mapState.copy(
                    clearMap = true,
                    markers = emptyList(),
                    polylines = emptyList()
                ),
                hideChart = true
            ) 
        }
    }

    fun onForceCrashClick() {
        throw RuntimeException("User forced crash")
    }

    fun onConnectionIssueShown() {
        _uiState.update { it.copy(showConnectionIssue = false) }
    }

    fun onErrorMessageShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun onDistancesLoadedHandled() {
        _uiState.update { it.copy(selectFromDistancesLoaded = null) }
    }

    fun onCameraUpdateHandled() {
        _uiState.update { 
            it.copy(mapState = it.mapState.copy(cameraUpdate = null)) 
        }
    }

    fun onSearchAddressHandled() {
        _uiState.update { it.copy(searchAddress = null) }
    }

    fun onMapClearHandled() {
        _uiState.update { 
            it.copy(mapState = it.mapState.copy(clearMap = false)) 
        }
    }

    fun onHideChartHandled() {
        _uiState.update { it.copy(hideChart = false) }
    }

    fun onInfoWindowClick() {
        _uiState.update { it.copy(openShowInfo = true) }
    }

    fun onOpenShowInfoHandled() {
        _uiState.update { it.copy(openShowInfo = false) }
    }


    fun onElevationUpdateHandled() {
        _uiState.update { it.copy(triggerElevationUpdate = null) }
    }

    fun onLocationPermissionSnackbarShown() {
        _uiState.update { it.copy(showLocationPermissionSnackbar = false) }
    }

    private fun CoordinatesRepository.setList(list: List<Coordinates>) {
        clear()
        list.forEach(coordinatesRepository::append)
    }

    companion object {

        private const val TAG = "MainViewModel"
    }
}
