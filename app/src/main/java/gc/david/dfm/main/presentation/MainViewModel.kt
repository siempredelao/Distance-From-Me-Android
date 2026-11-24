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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gc.david.dfm.BuildConfigProvider
import gc.david.dfm.R
import gc.david.dfm.core.coordinates.domain.CoordinatesRepository
import gc.david.dfm.core.coordinates.domain.DistanceCalculator
import gc.david.dfm.core.coordinates.domain.model.Coordinates
import gc.david.dfm.core.distances.domain.GetDistancesUseCase
import gc.david.dfm.core.distances.domain.GetPositionListUseCase
import gc.david.dfm.core.distances.domain.model.PositionListResult
import gc.david.dfm.core.settings.domain.SettingsRepository
import gc.david.dfm.core.units.domain.model.UnitSystem
import gc.david.dfm.core.units.presentation.UnitFormatter
import gc.david.dfm.distance.domain.DistanceModeRepository
import gc.david.dfm.distance.domain.model.DistanceMode
import gc.david.dfm.location.domain.LocationRepository
import gc.david.dfm.location.domain.PermissionChecker
import gc.david.dfm.main.domain.GetStoredDistancesUseCase
import gc.david.dfm.main.presentation.mapper.LoadDistancesMapper
import gc.david.dfm.main.presentation.mapper.MapStateMapper
import gc.david.dfm.main.presentation.model.CameraUpdate
import gc.david.dfm.main.presentation.model.DrawDistanceUiModel
import gc.david.dfm.main.presentation.model.MainUiState
import gc.david.dfm.main.presentation.model.MarkerData
import gc.david.dfm.main.presentation.model.SideNavigationItemId
import gc.david.dfm.support.android.domain.ConnectionManager
import gc.david.dfm.support.android.presentation.ResourceProvider
import gc.david.dfm.support.coroutines.launchCatching
import gc.david.dfm.support.coroutines.logUnexpectedFailure
import gc.david.dfm.toCoordinates
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import timber.log.Timber

class MainViewModel(
    private val getDistancesUseCase: GetDistancesUseCase,
    private val getStoredDistancesUseCase: GetStoredDistancesUseCase,
    private val getPositionListUseCase: GetPositionListUseCase,
    private val connectionManager: ConnectionManager,
    private val resourceProvider: ResourceProvider,
    private val settingsRepository: SettingsRepository,
    private val distanceModeRepository: DistanceModeRepository,
    private val locationRepository: LocationRepository,
    private val permissionChecker: PermissionChecker,
    private val coordinatesRepository: CoordinatesRepository,
    private val distanceCalculator: DistanceCalculator,
    private val unitFormatter: UnitFormatter,
    private val buildConfigProvider: BuildConfigProvider,
    private val mapStateMapper: MapStateMapper,
    private val loadDistancesMapper: LoadDistancesMapper
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val coordinates get() = coordinatesRepository.observeDistance().value
    private val unitSystem: UnitSystem
        get() = settingsRepository.getUnitSystemPreference()

    // Moves to current position if app has just started
    private var appHasJustStarted = true

    // The device's last known position, or null until the first location arrives
    private var currentLocation: Coordinates? = null

    // Determines whether a multi-point distance is being marked on the map
    private var isUserSelectingPoints: Boolean = false

    // Ask for location at most once per screen, so a refusal can never turn into a prompt loop
    private var hasPromptedForLocationPermission = false

    // onStart runs on every Activity creation, but one collection lasts the ViewModel's lifetime
    private var isObservingStoredDistances = false

    init {
        // The points outlive the screen, but its drawing doesn't: without this, a long press on a reopened map would
        // join the points of a path that is no longer drawn
        coordinatesRepository.clear()

        // Initialize with current distance mode and side navigation state
        val initialDistanceMode = distanceModeRepository.getDistanceMode()
        _uiState.update {
            it.copy(
                distanceMode = initialDistanceMode,
                sideNavigationState = it.sideNavigationState.copy(
                    selectedItemId = when (initialDistanceMode) {
                        DistanceMode.FROM_CURRENT_POINT -> SideNavigationItemId.CURRENT_POSITION
                        DistanceMode.FROM_ANY_POINT -> SideNavigationItemId.ANY_POSITION
                    }
                )
            )
        }
    }

    fun onStart() {
        if (!isObservingStoredDistances) {
            isObservingStoredDistances = true
            observeStoredDistances()
        }
        if (!connectionManager.isOnline()) {
            _uiState.update { it.copy(showConnectionIssue = true) }
        }
        _uiState.update {
            it.copy(
                sideNavigationState = it.sideNavigationState.copy(
                    showDebugOptions = !buildConfigProvider.isReleaseBuild()
                )
            )
        }

        // Request location permission on startup if mode is FROM_CURRENT_POINT
        if (!hasPromptedForLocationPermission &&
            distanceModeRepository.getDistanceMode() == DistanceMode.FROM_CURRENT_POINT &&
            !permissionChecker.isLocationPermissionGranted()
        ) {
            hasPromptedForLocationPermission = true
            _uiState.update { it.copy(requestLocationPermission = true) }
        }
    }

    // Shows the "Show distances" menu item only while there are distances to load
    private fun observeStoredDistances() {
        getDistancesUseCase()
            .map { it.isNotEmpty() }
            .onEach { hasDistances ->
                _uiState.update {
                    it.copy(
                        sideNavigationState = it.sideNavigationState.copy(
                            showLoadMenuItem = hasDistances
                        )
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Triggered when the user taps on the "Show distances" menu item.
     */
    fun onLoadDistancesClick() {
        getStoredDistancesUseCase()
            .map(loadDistancesMapper::map)
            .catch { Timber.tag(TAG).e(it) }
            .take(1)
            .onEach {
                _uiState.update { state -> state.copy(selectFromDistancesLoaded = it) }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Triggered when the user selects a distance from the loaded distances dialog.
     */
    fun onDistanceToShowSelected(id: Long, name: String) {
        launchCatching(onError = { logUnexpectedFailure(TAG, it) }) {
            when (val result = getPositionListUseCase(id)) {
                is PositionListResult.Found -> {
                    val coordinates = result.positions.toCoordinates()
                    coordinatesRepository.setList(coordinates)

                    plotDistances(coordinates, name, DrawDistanceUiModel.Source.DATABASE)
                }

                // Keep the current drawing instead of clearing the map for a distance with nothing to show
                PositionListResult.NotFound -> Timber.tag(TAG).w("Distance $id has no positions")
            }
        }
    }

    fun onDistanceFromCurrentPositionSet() {
        distanceModeRepository.setDistanceMode(DistanceMode.FROM_CURRENT_POINT)
        _uiState.update {
            it.copy(
                distanceMode = DistanceMode.FROM_CURRENT_POINT,
                sideNavigationState = it.sideNavigationState.copy(
                    selectedItemId = SideNavigationItemId.CURRENT_POSITION
                )
            )
        }
        resetMap()
        if (!permissionChecker.isLocationPermissionGranted()) {
            _uiState.update { it.copy(showLocationPermissionSnackbar = true) }
        }
    }

    fun onDistanceFromAnyPositionSet() {
        distanceModeRepository.setDistanceMode(DistanceMode.FROM_ANY_POINT)
        _uiState.update {
            it.copy(
                distanceMode = DistanceMode.FROM_ANY_POINT,
                sideNavigationState = it.sideNavigationState.copy(
                    selectedItemId = SideNavigationItemId.ANY_POSITION
                )
            )
        }
        resetMap()
    }

    fun onMyLocationButtonClick() {
        val coordinates = currentLocation ?: return
        _uiState.update {
            it.copy(mapState = it.mapState.copy(cameraUpdate = CameraUpdate.MoveTo(coordinates)))
        }
    }

    /**
     * Follows the device's location while location access is granted. The caller chooses how long: MainActivity runs
     * it while the screen is started, so location updates stop in the background.
     */
    suspend fun trackLocation() {
        uiState
            .map { it.isLocationPermissionGranted }
            .distinctUntilChanged()
            .collectLatest { granted ->
                if (granted) {
                    locationRepository.observeLocation().collect(::onLocationChanged)
                }
            }
    }

    private fun onLocationChanged(coordinates: Coordinates) {
        currentLocation = coordinates

        if (appHasJustStarted) {
            Timber.tag(TAG).d("onLocationChanged appHasJustStarted")

            _uiState.update {
                it.copy(mapState = it.mapState.copy(cameraUpdate = CameraUpdate.ZoomTo(coordinates)))
            }
            appHasJustStarted = false
        }
    }

    fun onMapClick(coordinates: Coordinates) {
        Timber.tag(TAG).d("onMapClick $coordinates")

        val origin = when (distanceModeRepository.getDistanceMode()) {
            DistanceMode.FROM_ANY_POINT -> null
            DistanceMode.FROM_CURRENT_POINT -> currentLocation ?: run {
                isUserSelectingPoints = false
                return // Without current location, we cannot calculate any distance
            }
        }

        if (!isUserSelectingPoints) {
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
        isUserSelectingPoints = true

        // To calculate the distance from the current position,
        // we effectively need the current position ;)
        if (origin != null && this@MainViewModel.coordinates.isEmpty()) {
            coordinatesRepository.append(origin)
        }
        coordinatesRepository.append(coordinates)

        // Update map with simple markers (no distance info yet)
        val markers = this@MainViewModel.coordinates.map { MarkerData(position = it) }
        _uiState.update {
            it.copy(mapState = it.mapState.copy(markers = markers))
        }
    }

    fun onPositionByNameResolved(coordinates: Coordinates) {
        when (distanceModeRepository.getDistanceMode()) {
            DistanceMode.FROM_ANY_POINT if this@MainViewModel.coordinates.isEmpty() -> {
                // The place starts a path, so the next tap adds to it instead of clearing it
                isUserSelectingPoints = true
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

            // Without a current location there is no distance to draw yet, so just show the place
            DistanceMode.FROM_CURRENT_POINT if currentLocation == null -> _uiState.update {
                it.copy(mapState = it.mapState.copy(cameraUpdate = CameraUpdate.MoveTo(coordinates)))
            }

            DistanceMode.FROM_ANY_POINT, DistanceMode.FROM_CURRENT_POINT -> onMapLongClick(
                coordinates
            )
        }
    }

    fun onMapLongClick(coordinates: Coordinates) {
        Timber.tag(TAG).d("onMapLongClick $coordinates")
        isUserSelectingPoints = true

        when (distanceModeRepository.getDistanceMode()) {
            DistanceMode.FROM_ANY_POINT -> {
                if (this@MainViewModel.coordinates.isEmpty()) {
                    _uiState.update { it.copy(errorMessage = resourceProvider.get(R.string.toast_first_point_needed)) }
                    return
                }
            }

            DistanceMode.FROM_CURRENT_POINT -> {
                val origin = currentLocation ?: run {
                    isUserSelectingPoints = false
                    return // Without current location, we cannot calculate any distance
                }

                // To calculate the distance from the current position,
                // we effectively need the current position ;)
                if (this@MainViewModel.coordinates.isEmpty()) {
                    coordinatesRepository.append(origin)
                }
            }
        }

        coordinatesRepository.append(coordinates)

        plotDistances(this@MainViewModel.coordinates, "", DrawDistanceUiModel.Source.MANUAL)

        isUserSelectingPoints = false
    }

    fun onAddressSearch(query: String) {
        Timber.tag(TAG).d("onAddressSearch $query")

        _uiState.update { it.copy(searchAddress = query) }
    }

    private fun resetMap() {
        isUserSelectingPoints = false
        coordinatesRepository.clear()
        _uiState.update {
            it.copy(
                mapState = it.mapState.copy(
                    clearMap = true,
                    markers = emptyList(),
                    polylines = emptyList()
                ),
                showChart = false
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
        _uiState.update { it.copy(mapState = it.mapState.copy(cameraUpdate = null)) }
    }

    fun onSearchAddressHandled() {
        _uiState.update { it.copy(searchAddress = null) }
    }

    fun onMapClearHandled() {
        _uiState.update { it.copy(mapState = it.mapState.copy(clearMap = false)) }
    }

    fun onShowChartHandled() {
        _uiState.update { it.copy(showChart = false) }
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

    /**
     * Triggered when the user refuses location access. [permanently] is true when Android will no longer show its
     * permission dialog, so only the app's system settings can grant it.
     */
    fun onLocationPermissionDenied(permanently: Boolean) {
        // Without location, fall back to measuring between any two points, as the rationale dialog's Cancel does
        onDistanceFromAnyPositionSet()
        if (permanently) {
            _uiState.update { it.copy(showLocationPermissionSnackbar = true) }
        }
    }

    /**
     * Triggered when location access may have changed: after the permission dialog, and on every resume, which
     * covers a grant made in the system settings.
     */
    fun onLocationPermissionChanged() {
        _uiState.update { it.copy(isLocationPermissionGranted = permissionChecker.isLocationPermissionGranted()) }
    }

    fun onLocationPermissionRequestHandled() {
        _uiState.update { it.copy(requestLocationPermission = false) }
    }

    private fun CoordinatesRepository.setList(list: List<Coordinates>) {
        clear()
        list.forEach(coordinatesRepository::append)
    }

    private fun plotDistances(
        coordinates: List<Coordinates>,
        distanceName: String,
        source: DrawDistanceUiModel.Source
    ) {
        val distanceInMetres = distanceCalculator.calculateTotalDistance(coordinates)
        val model = DrawDistanceUiModel(
            coordinates,
            distanceName,
            unitFormatter.formatDistance(distanceInMetres, unitSystem),
            source,
            distanceModeRepository.getDistanceMode(),
            settingsRepository.getCameraAnimation()
        )
        val newMapState = mapStateMapper.toMapUiState(model)
        _uiState.update { state ->
            state.copy(
                mapState = newMapState,
                triggerElevationUpdate = coordinates
            )
        }
    }

    companion object {

        private const val TAG = "MainViewModel"
    }
}
