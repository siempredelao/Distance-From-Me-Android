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
import gc.david.dfm.ConnectionManager
import gc.david.dfm.PermissionChecker
import gc.david.dfm.R
import gc.david.dfm.common.BuildConfigProvider
import gc.david.dfm.common.Coordinates
import gc.david.dfm.common.domain.DistanceCalculator
import gc.david.dfm.common.domain.model.UnitSystem
import gc.david.dfm.common.presentation.DistanceFormatter
import gc.david.dfm.common.presentation.ResourceProvider
import gc.david.dfm.core.distances.domain.GetDistancesUseCase
import gc.david.dfm.core.distances.domain.GetPositionListUseCase
import gc.david.dfm.core.distances.domain.model.Distance
import gc.david.dfm.distance.data.CurrentLocationProvider
import gc.david.dfm.distance.data.DistanceModeProvider
import gc.david.dfm.distance.data.model.DistanceMode
import gc.david.dfm.distance.data.model.Point
import gc.david.dfm.distance.domain.CoordinatesRepository
import gc.david.dfm.main.domain.GetStoredDistancesUseCase
import gc.david.dfm.main.domain.StoredDistances
import gc.david.dfm.main.presentation.mapper.LoadDistancesMapper
import gc.david.dfm.main.presentation.mapper.MapStateMapper
import gc.david.dfm.main.presentation.model.CameraUpdate
import gc.david.dfm.main.presentation.model.DistanceSelectionUiModel
import gc.david.dfm.settings.domain.SettingsRepository
import gc.david.dfm.testsupport.CoroutineExtension
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class MainViewModelTest {

    @JvmField
    @RegisterExtension
    val coroutineExtension = CoroutineExtension()

    private val getDistancesUseCase = mock<GetDistancesUseCase>()
    private val getStoredDistancesUseCase = mock<GetStoredDistancesUseCase>()
    private val getPositionListUseCase = mock<GetPositionListUseCase>()
    private val connectionManager = mock<ConnectionManager>()
    private val resourceProvider = mock<ResourceProvider>()
    private val settingsRepository = mock<SettingsRepository>()
    private val distanceModeProvider = mock<DistanceModeProvider>()
    private val currentLocationProvider = mock<CurrentLocationProvider>()
    private val permissionChecker = mock<PermissionChecker>()
    private val coordinatesRepository = mock<CoordinatesRepository>()
    private val distanceCalculator = mock<DistanceCalculator>()
    private val distanceFormatter = mock<DistanceFormatter>()
    private val buildConfigProvider = mock<BuildConfigProvider>()
    private val mapStateMapper = mock<MapStateMapper>()
    private val loadDistancesMapper = mock<LoadDistancesMapper>()

    private lateinit var viewModel: MainViewModel

    @BeforeEach
    fun setup() {
        whenever(getDistancesUseCase()).thenReturn(flowOf(emptyList()))
        whenever(coordinatesRepository.observeDistance()).thenReturn(MutableStateFlow(emptyList()))
        whenever(settingsRepository.getUnitSystemPreference()).thenReturn(UnitSystem.METRIC)
        whenever(distanceModeProvider.get()).thenReturn(DistanceMode.FROM_CURRENT_POINT)

        viewModel = MainViewModel(
            getDistancesUseCase,
            getStoredDistancesUseCase,
            getPositionListUseCase,
            connectionManager,
            resourceProvider,
            settingsRepository,
            distanceModeProvider,
            currentLocationProvider,
            permissionChecker,
            coordinatesRepository,
            distanceCalculator,
            distanceFormatter,
            buildConfigProvider,
            mapStateMapper,
            loadDistancesMapper
        )
    }

    @Test
    fun `initial state has no connection issue`() {
        assertFalse(viewModel.uiState.value.showConnectionIssue)
    }

    @Test
    fun `initial state does not show force crash item`() {
        assertFalse(viewModel.uiState.value.sideNavigationState.showCrashMenuItem)
    }

    @Test
    fun `initial state does not show location permission snackbar`() {
        assertFalse(viewModel.uiState.value.showLocationPermissionSnackbar)
    }

    @Test
    fun `onStart shows connection issue when offline`() {
        whenever(connectionManager.isOnline()).thenReturn(false)
        whenever(buildConfigProvider.isReleaseBuild()).thenReturn(false)

        viewModel.onStart()

        assertTrue(viewModel.uiState.value.showConnectionIssue)
        assertTrue(viewModel.uiState.value.sideNavigationState.showCrashMenuItem)
    }

    @Test
    fun `onStart does not show connection issue when online`() {
        whenever(connectionManager.isOnline()).thenReturn(true)
        whenever(buildConfigProvider.isReleaseBuild()).thenReturn(true)

        viewModel.onStart()

        assertFalse(viewModel.uiState.value.showConnectionIssue)
        assertFalse(viewModel.uiState.value.sideNavigationState.showCrashMenuItem)
    }

    @Test
    fun `onLoadDistancesClick updates selectFromDistancesLoaded with mapped distance list`() = runTest {
        val storedDistances = StoredDistances(
            distances = listOf(Distance(1L, "Home", "3.2 km", java.util.Date(0))),
            rateRequest = StoredDistances.RateRequest.NotAvailable
        )
        val expected = listOf(
            DistanceSelectionUiModel.Distance(
                id = 1L,
                name = "Home",
                distance = "3.2 km",
                date = "2026-01-01"
            )
        )
        whenever(getStoredDistancesUseCase()).thenReturn(flowOf(storedDistances))
        whenever(loadDistancesMapper.map(storedDistances)).thenReturn(expected)

        viewModel.onLoadDistancesClick()

        assertEquals(expected, viewModel.uiState.value.selectFromDistancesLoaded)
    }

    @Test
    fun `onDistanceFromCurrentPositionSet updates distance mode`() {
        viewModel.onDistanceFromCurrentPositionSet()

        verify(distanceModeProvider).set(DistanceMode.FROM_CURRENT_POINT)
        verify(coordinatesRepository).clear()
    }

    @Test
    fun `onDistanceFromCurrentPositionSet shows permission snackbar when permission not granted`() {
        whenever(permissionChecker.isLocationPermissionGranted()).thenReturn(false)

        viewModel.onDistanceFromCurrentPositionSet()

        assertTrue(viewModel.uiState.value.showLocationPermissionSnackbar)
    }

    @Test
    fun `onDistanceFromCurrentPositionSet does not show permission snackbar when permission granted`() {
        whenever(permissionChecker.isLocationPermissionGranted()).thenReturn(true)

        viewModel.onDistanceFromCurrentPositionSet()

        assertFalse(viewModel.uiState.value.showLocationPermissionSnackbar)
    }

    @Test
    fun `onDistanceFromAnyPositionSet updates distance mode`() {
        viewModel.onDistanceFromAnyPositionSet()

        verify(distanceModeProvider).set(DistanceMode.FROM_ANY_POINT)
        verify(coordinatesRepository).clear()
    }

    @Test
    fun `onMyLocationButtonClick does nothing when current location is undefined`() {
        whenever(currentLocationProvider.get()).thenReturn(CurrentLocationProvider.UNDEFINED)

        viewModel.onMyLocationButtonClick()

        assertNull(viewModel.uiState.value.mapState.cameraUpdate)
    }

    @Test
    fun `onLocationChanged on first update zooms to current location`() {
        val location = mock<Location>()
        whenever(location.latitude).thenReturn(51.5074)
        whenever(location.longitude).thenReturn(-0.1278)

        viewModel.onLocationChanged(location)

        assertEquals(
            Coordinates(51.5074, -0.1278),
            (viewModel.uiState.value.mapState.cameraUpdate as CameraUpdate.ZoomTo).position
        )
    }

    @Test
    fun `onMapClick appends coordinates to repository in any position mode`() {
        val coordinates = Coordinates(40.7128, -74.0060)
        whenever(distanceModeProvider.get()).thenReturn(DistanceMode.FROM_ANY_POINT)

        viewModel.onMapClick(coordinates)

        verify(coordinatesRepository).append(coordinates)
    }

    @Test
    fun `onMapClick does nothing without current location when mode is current position`() {
        val coordinates = Coordinates(40.7128, -74.0060)
        whenever(distanceModeProvider.get()).thenReturn(DistanceMode.FROM_CURRENT_POINT)
        whenever(currentLocationProvider.get()).thenReturn(CurrentLocationProvider.UNDEFINED)

        viewModel.onMapClick(coordinates)

        verify(coordinatesRepository, never()).append(any())
    }

    @Test
    fun `onPositionByNameResolved appends first point and moves camera in any position mode`() {
        val coordinates = Coordinates(40.0, -3.0)
        whenever(distanceModeProvider.get()).thenReturn(DistanceMode.FROM_ANY_POINT)

        viewModel.onPositionByNameResolved(coordinates)

        verify(coordinatesRepository).append(coordinates)
        assertEquals(CameraUpdate.MoveTo(coordinates), viewModel.uiState.value.mapState.cameraUpdate)
    }

    @Test
    fun `onMapLongClick with empty coordinates in any position mode shows missing start point error`() {
        whenever(distanceModeProvider.get()).thenReturn(DistanceMode.FROM_ANY_POINT)
        val message = "Start point needed"
        whenever(resourceProvider.get(R.string.toast_first_point_needed)).thenReturn(message)

        viewModel.onMapLongClick(Coordinates(40.0, -3.0))

        assertEquals(message, viewModel.uiState.value.errorMessage)
        verify(coordinatesRepository, never()).append(any())
    }

    @Test
    fun `onMapLongClick with undefined current location in current-position mode does nothing`() {
        whenever(distanceModeProvider.get()).thenReturn(DistanceMode.FROM_CURRENT_POINT)
        whenever(currentLocationProvider.get()).thenReturn(CurrentLocationProvider.UNDEFINED)

        viewModel.onMapLongClick(Coordinates(40.0, -3.0))

        verify(coordinatesRepository, never()).append(any())
    }

    @Test
    fun `onAddressSearch records query when current location is available`() {
        whenever(currentLocationProvider.get()).thenReturn(Point(51.5074, -0.1278))

        viewModel.onAddressSearch("London")

        assertEquals("London", viewModel.uiState.value.searchAddress)
    }

    @Test
    fun `resetMap clears map state and hides chart`() {
        viewModel.resetMap()

        assertTrue(viewModel.uiState.value.mapState.clearMap)
        assertTrue(viewModel.uiState.value.mapState.markers.isEmpty())
        assertTrue(viewModel.uiState.value.mapState.polylines.isEmpty())
        assertFalse(viewModel.uiState.value.showChart)
    }

    @Test
    fun `onConnectionIssueShown clears connection issue`() {
        viewModel.onConnectionIssueShown()

        assertFalse(viewModel.uiState.value.showConnectionIssue)
    }

    @Test
    fun `onErrorMessageShown clears error message`() {
        viewModel.onErrorMessageShown()

        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `onDistancesLoadedHandled clears selected distance list`() {
        viewModel.onDistancesLoadedHandled()

        assertNull(viewModel.uiState.value.selectFromDistancesLoaded)
    }

    @Test
    fun `onCameraUpdateHandled clears camera update`() {
        viewModel.onCameraUpdateHandled()

        assertNull(viewModel.uiState.value.mapState.cameraUpdate)
    }

    @Test
    fun `onSearchAddressHandled clears searchAddress`() {
        viewModel.onSearchAddressHandled()

        assertNull(viewModel.uiState.value.searchAddress)
    }

    @Test
    fun `onMapClearHandled clears map clear flag`() {
        viewModel.onMapClearHandled()

        assertFalse(viewModel.uiState.value.mapState.clearMap)
    }

    @Test
    fun `onShowChartHandled hides chart`() {
        viewModel.onShowChartHandled()

        assertFalse(viewModel.uiState.value.showChart)
    }

    @Test
    fun `onInfoWindowClick toggles info view and handler clears it`() {
        viewModel.onInfoWindowClick()

        assertTrue(viewModel.uiState.value.openShowInfo)
    }

    @Test
    fun `onElevationUpdateHandled clears triggerElevationUpdate`() = runTest {
        viewModel.onElevationUpdateHandled()

        assertNull(viewModel.uiState.value.triggerElevationUpdate)
    }

    @Test
    fun `onLocationPermissionSnackbarShown clears snackbar`() {
        viewModel.onLocationPermissionSnackbarShown()

        assertFalse(viewModel.uiState.value.showLocationPermissionSnackbar)
    }

    @Test
    fun `onLocationPermissionRequestHandled clears request flag`() {
        viewModel.onLocationPermissionRequestHandled()

        assertFalse(viewModel.uiState.value.requestLocationPermission)
    }
}
