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
import com.google.android.gms.maps.model.LatLng
import gc.david.dfm.*
import gc.david.dfm.Utils.toLatLng
import gc.david.dfm.Utils.toPoint
import gc.david.dfm.common.ResourceProvider
import gc.david.dfm.core.distances.data.database.Distance
import gc.david.dfm.distance.data.CurrentLocationProvider
import gc.david.dfm.distance.data.DistanceMode
import gc.david.dfm.distance.data.DistanceModeProvider
import gc.david.dfm.core.distances.domain.GetDistancesUseCase
import gc.david.dfm.core.distances.domain.GetPositionListUseCase
import gc.david.dfm.main.presentation.model.DrawDistanceModel
import gc.david.dfm.main.presentation.model.MainUiState
import gc.david.dfm.map.Haversine
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
import java.util.*

class MainViewModel(
    private val getDistancesUseCase: GetDistancesUseCase,
    private val getPositionListUseCase: GetPositionListUseCase,
    private val connectionManager: ConnectionManager,
    private val resourceProvider: ResourceProvider,
    private val preferencesProvider: PreferencesProvider,
    private val distanceModeProvider: DistanceModeProvider,
    private val currentLocationProvider: CurrentLocationProvider,
    private val permissionChecker: PermissionChecker,
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
    private var positionList = mutableListOf<LatLng>()

    private val locale: Locale
        get() {
            val defaultUnit = preferencesProvider.getMeasureUnitPreference()
            return if (DFMPreferences.MEASURE_AMERICAN_UNIT_VALUE == defaultUnit) Locale.US else Locale.FRANCE
        }

    fun onStart() {
        if (!connectionManager.isOnline()) {
            _uiState.update { it.copy(showConnectionIssue = true) }
        }
    }

    /**
     * Triggered when the menu is already built and ready to be updated.
     */
    fun onMenuReady() {
        _uiState.update { it.copy(showForceCrashItem = !Utils.isReleaseBuild()) }
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
            val result = getPositionListUseCase(distance.id!!)

            result.fold({
                val distanceInMetres = Utils.calculateDistanceInMetres2(it)
                _uiState.update { state ->
                    state.copy(
                        drawDistance = DrawDistanceModel(
                            it.toLatLng().toMutableList(),
                            distance.name + "\n",
                            distanceInMetres,
                            Haversine.normalizeDistance(distanceInMetres, locale),
                            DrawDistanceModel.Source.DATABASE,
                            distanceModeProvider.get()
                        )
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
            val latLng = LatLng(currentLocation.lat, currentLocation.lon)
            _uiState.update { it.copy(centerMapInto = latLng) }
        }
    }

    // TODO this should be moved to a repooooo!!!
    fun onLocationChanged(location: Location) {
        Timber.tag(TAG).d("onLocationChanged ${location.toPoint()}")
        currentLocationProvider.set(location)

        if (appHasJustStarted) {
            Timber.tag(TAG).d("onLocationChanged appHasJustStarted")

            val latlng = LatLng(location.latitude, location.longitude)
            _uiState.update { it.copy(zoomMapInto = latlng) }
            appHasJustStarted = false
        }
    }

    fun onMapClick(point: LatLng) {
        Timber.tag(TAG).d("onMapClick ${point.toPoint()}")
        if (distanceModeProvider.get() == DistanceMode.FROM_ANY_POINT) {
            if (!calculatingDistance) {
                positionList.clear()
            }

            calculatingDistance = true
        } else {
            val currentLocation = currentLocationProvider.get()
            if (currentLocation == CurrentLocationProvider.UNDEFINED) {
                calculatingDistance = false
                return // Without current location, we cannot calculate any distance
            }

            if (!calculatingDistance) {
                positionList.clear()
            }
            calculatingDistance = true

            // To calculate the distance from the current position,
            // we effectively need the current position ;)
            if (positionList.isEmpty()) {
                positionList.add(LatLng(currentLocation.lat, currentLocation.lon))
            }
        }
        positionList.add(point)
        _uiState.update { it.copy(drawPoints = positionList.toList()) }
    }

    fun onPositionByNameResolved(point: LatLng) {
        if (distanceModeProvider.get() == DistanceMode.FROM_ANY_POINT) {
            if (positionList.isNotEmpty()) {
                onMapLongClick(point)
            } else {
                positionList.add(point)
                _uiState.update { it.copy(drawPoints = positionList.toList(), centerMapInto = point) }
            }
        } else {
            onMapLongClick(point)
        }
    }

    fun onMapLongClick(point: LatLng) {
        Timber.tag(TAG).d("onMapLongClick ${point.toPoint()}")
        calculatingDistance = true

        if (distanceModeProvider.get() == DistanceMode.FROM_ANY_POINT) {
            if (positionList.isEmpty()) {
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
            if (positionList.isEmpty()) {
                positionList.add(0, LatLng(currentLocation.lat, currentLocation.lon))
            }
        }

        positionList.add(point)

        val distanceInMetres = Utils.calculateDistanceInMetres(positionList)
        _uiState.update {
            it.copy(
                drawDistance = DrawDistanceModel(
                    positionList,
                    "",
                    distanceInMetres,
                    Haversine.normalizeDistance(distanceInMetres, locale),
                    DrawDistanceModel.Source.MANUAL,
                    distanceModeProvider.get()
                )
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
        positionList.clear()
        _uiState.update { it.copy(resetMap = true, hideChart = true) }
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

    fun onZoomHandled() {
        _uiState.update { it.copy(zoomMapInto = null) }
    }

    fun onCenterHandled() {
        _uiState.update { it.copy(centerMapInto = null) }
    }

    fun onSearchAddressHandled() {
        _uiState.update { it.copy(searchAddress = null) }
    }

    fun onResetMapHandled() {
        _uiState.update { it.copy(resetMap = false) }
    }

    fun onHideChartHandled() {
        _uiState.update { it.copy(hideChart = false) }
    }

    fun onDrawDistanceHandled() {
        _uiState.update { it.copy(drawDistance = null) }
    }

    fun onDrawPointsHandled() {
        _uiState.update { it.copy(drawPoints = null) }
    }

    fun onLocationPermissionSnackbarShown() {
        _uiState.update { it.copy(showLocationPermissionSnackbar = false) }
    }

    companion object {

        private const val TAG = "MainViewModel"
    }
}
