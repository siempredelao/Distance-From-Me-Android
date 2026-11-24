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

import android.content.Context
import android.content.res.Resources
import gc.david.dfm.BuildConfigProvider
import gc.david.dfm.R
import gc.david.dfm.core.coordinates.data.BaseCoordinatesRepository
import gc.david.dfm.core.coordinates.data.CoordinatesMemoryDataSource
import gc.david.dfm.core.coordinates.domain.DistanceCalculator
import gc.david.dfm.core.coordinates.domain.model.Coordinates
import gc.david.dfm.core.distances.domain.GetDistancesUseCase
import gc.david.dfm.core.distances.domain.GetPositionListUseCase
import gc.david.dfm.core.distances.domain.model.Distance
import gc.david.dfm.core.distances.domain.model.Position
import gc.david.dfm.core.distances.domain.model.PositionListResult
import gc.david.dfm.core.settings.domain.SettingsRepository
import gc.david.dfm.core.settings.domain.model.CameraAnimation
import gc.david.dfm.core.units.domain.UnitConverter
import gc.david.dfm.core.units.domain.model.UnitSystem
import gc.david.dfm.core.units.presentation.UnitFormatter
import gc.david.dfm.distance.data.BaseDistanceModeRepository
import gc.david.dfm.distance.domain.model.DistanceMode
import gc.david.dfm.location.domain.LocationRepository
import gc.david.dfm.location.domain.PermissionChecker
import gc.david.dfm.main.domain.GetStoredDistancesUseCase
import gc.david.dfm.main.domain.model.StoredDistances
import gc.david.dfm.main.presentation.mapper.LoadDistancesMapper
import gc.david.dfm.main.presentation.mapper.MapStateMapper
import gc.david.dfm.main.presentation.mapper.toUiModel
import gc.david.dfm.main.presentation.model.CameraUpdate
import gc.david.dfm.main.presentation.model.DistanceSelectionUiModel
import gc.david.dfm.main.presentation.model.MarkerData
import gc.david.dfm.main.presentation.model.PolylineData
import gc.david.dfm.support.android.domain.ConnectionManager
import gc.david.dfm.support.android.presentation.ResourceProvider
import gc.david.dfm.support.test.CoroutineExtension
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
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
    private val distanceModeRepository = BaseDistanceModeRepository()
    private val locationRepository = mock<LocationRepository>()
    private val locations = MutableSharedFlow<Coordinates>(extraBufferCapacity = 1)
    private val permissionChecker = mock<PermissionChecker>()
    private val coordinatesRepository = BaseCoordinatesRepository(CoordinatesMemoryDataSource())
    private val buildConfigProvider = mock<BuildConfigProvider>()
    private val context = mock<Context>()
    private val resources = mock<Resources>()
    private val loadDistancesMapper = mock<LoadDistancesMapper>()
    private val defaultLocale = Locale.getDefault()

    // The points of the path, which the repository keeps between calls
    private val storedPath get() = coordinatesRepository.observeDistance().value

    // The path as the map draws it
    private val drawnPath
        get() = viewModel.uiState.value.mapState.polylines.let { lines ->
            lines.take(1).map(PolylineData::start) + lines.map(PolylineData::end)
        }.map { Coordinates(it.latitude, it.longitude) }
    private val markers get() = viewModel.uiState.value.mapState.markers

    private lateinit var viewModel: MainViewModel

    @BeforeEach
    fun setup() {
        // The label's separators follow the locale
        Locale.setDefault(Locale.US)
        whenever(getDistancesUseCase()).thenReturn(flowOf(emptyList()))
        whenever(locationRepository.observeLocation()).thenReturn(locations)
        whenever(settingsRepository.getUnitSystemPreference()).thenReturn(UnitSystem.METRIC)
        whenever(settingsRepository.getCameraAnimation()).thenReturn(CameraAnimation.None)
        whenever(context.resources).thenReturn(resources)

        viewModel = createViewModel()
    }

    @AfterEach
    fun tearDown() {
        Locale.setDefault(defaultLocale)
    }

    @Test
    fun `a new view model drops the points left by the previous one`() {
        givenDistanceDrawn()

        viewModel = createViewModel()

        assertEquals(emptyList<Coordinates>(), storedPath)
        assertEquals(emptyList<MarkerData>(), markers)
    }

    @Test
    fun `a long press on a reopened map asks for a starting point in any position mode`() {
        val message = "Start point needed"
        whenever(resourceProvider.get(R.string.toast_first_point_needed)).thenReturn(message)
        givenDistanceDrawn()
        viewModel = createViewModel()

        viewModel.onMapLongClick(POINT_C)

        assertEquals(message, viewModel.uiState.value.errorMessage)
        assertEquals(emptyList<Coordinates>(), drawnPath)
    }

    @Test
    fun `does not read stored distances before onStart`() {
        verify(getDistancesUseCase, never()).invoke()
    }

    @Test
    fun `onStart shows the load menu item once distances are stored`() {
        val storedDistances = MutableStateFlow(emptyList<Distance>())
        whenever(getDistancesUseCase()).thenReturn(storedDistances)
        viewModel.onStart()

        storedDistances.value = listOf(Distance(1L, "Home", "3.2 km", java.util.Date(0)))

        assertTrue(viewModel.uiState.value.sideNavigationState.showLoadMenuItem)
    }

    @Test
    fun `onStart reads stored distances only once`() {
        viewModel.onStart()
        viewModel.onStart()

        verify(getDistancesUseCase).invoke()
    }

    @Test
    fun `initial state has no connection issue`() {
        assertFalse(viewModel.uiState.value.showConnectionIssue)
    }

    @Test
    fun `initial state does not show debug options`() {
        assertFalse(viewModel.uiState.value.sideNavigationState.showDebugOptions)
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
        assertTrue(viewModel.uiState.value.sideNavigationState.showDebugOptions)
    }

    @Test
    fun `onStart does not show connection issue when online`() {
        whenever(connectionManager.isOnline()).thenReturn(true)
        whenever(buildConfigProvider.isReleaseBuild()).thenReturn(true)

        viewModel.onStart()

        assertFalse(viewModel.uiState.value.showConnectionIssue)
        assertFalse(viewModel.uiState.value.sideNavigationState.showDebugOptions)
    }

    @Test
    fun `onLoadDistancesClick updates selectFromDistancesLoaded with mapped distance list`() = runTest {
        val storedDistances = StoredDistances(
            distances = listOf(Distance(1L, "Home", "3.2 km", Date(0))),
            rateRequest = StoredDistances.RateRequest.NotAvailable
        )
        val expected = persistentListOf<DistanceSelectionUiModel>(
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
    fun `onDistanceToShowSelected replaces the drawn distance with the stored one`() = runTest {
        givenDistanceDrawn()
        val positions = listOf(Position(0.0, 1.0), Position(0.0, 2.0))
        whenever(getPositionListUseCase(1L)).thenReturn(PositionListResult.Found(positions))

        viewModel.onDistanceToShowSelected(1L, "Home")

        assertEquals(listOf(POINT_B, POINT_C), storedPath)
        assertEquals(listOf(POINT_B, POINT_C), drawnPath)
        assertEquals(MarkerData.InfoWindow.Visible(title = "Home", snippet = "111.19 km"), markers.last().infoWindow)
        assertEquals(listOf(POINT_B, POINT_C), viewModel.uiState.value.triggerElevationUpdate)
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "  "])
    fun `onDistanceToShowSelected shows only the distance when the stored one has no name`(name: String) = runTest {
        val positions = listOf(Position(0.0, 0.0), Position(0.0, 1.0))
        whenever(getPositionListUseCase(1L)).thenReturn(PositionListResult.Found(positions))

        viewModel.onDistanceToShowSelected(1L, name)

        assertEquals(MarkerData.InfoWindow.Visible("111.19 km"), markers.last().infoWindow)
    }

    @Test
    fun `onDistanceToShowSelected keeps the map when the distance has no positions`() = runTest {
        givenDistanceDrawn()
        whenever(getPositionListUseCase(1L)).thenReturn(PositionListResult.NotFound)
        val mapState = viewModel.uiState.value.mapState

        viewModel.onDistanceToShowSelected(1L, "Home")

        assertEquals(listOf(POINT_A, POINT_B), storedPath)
        assertEquals(mapState, viewModel.uiState.value.mapState)
    }

    @Test
    fun `onDistanceToShowSelected keeps the map when loading the positions fails`() = runTest {
        givenDistanceDrawn()
        whenever(getPositionListUseCase(1L)).thenAnswer { throw IllegalStateException("Database error") }
        val mapState = viewModel.uiState.value.mapState

        viewModel.onDistanceToShowSelected(1L, "Home")

        assertEquals(listOf(POINT_A, POINT_B), storedPath)
        assertEquals(mapState, viewModel.uiState.value.mapState)
    }

    @Test
    fun `onDistanceFromCurrentPositionSet updates distance mode`() {
        givenDistanceDrawn()

        viewModel.onDistanceFromCurrentPositionSet()

        assertEquals(DistanceMode.FROM_CURRENT_POINT, distanceModeRepository.getDistanceMode())
        assertEquals(emptyList<Coordinates>(), storedPath)
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
    fun `onStart asks for location permission in current position mode without permission`() {
        whenever(permissionChecker.isLocationPermissionGranted()).thenReturn(false)

        viewModel.onStart()

        assertTrue(viewModel.uiState.value.requestLocationPermission)
    }

    @Test
    fun `onStart asks for location permission only once`() {
        whenever(permissionChecker.isLocationPermissionGranted()).thenReturn(false)
        viewModel.onStart()
        viewModel.onLocationPermissionRequestHandled()

        viewModel.onStart()

        assertFalse(viewModel.uiState.value.requestLocationPermission)
    }

    @Test
    fun `onLocationPermissionDenied switches to any position mode`() {
        viewModel.onLocationPermissionDenied(permanently = false)

        assertEquals(DistanceMode.FROM_ANY_POINT, distanceModeRepository.getDistanceMode())
        assertEquals(DistanceMode.FROM_ANY_POINT, viewModel.uiState.value.distanceMode)
        assertFalse(viewModel.uiState.value.showLocationPermissionSnackbar)
    }

    @Test
    fun `onLocationPermissionDenied permanently points the user to settings`() {
        viewModel.onLocationPermissionDenied(permanently = true)

        assertEquals(DistanceMode.FROM_ANY_POINT, distanceModeRepository.getDistanceMode())
        assertTrue(viewModel.uiState.value.showLocationPermissionSnackbar)
    }

    @Test
    fun `onLocationPermissionChanged exposes a grant`() {
        whenever(permissionChecker.isLocationPermissionGranted()).thenReturn(true)

        viewModel.onLocationPermissionChanged()

        assertTrue(viewModel.uiState.value.isLocationPermissionGranted)
    }

    @Test
    fun `onLocationPermissionChanged exposes a revocation`() {
        whenever(permissionChecker.isLocationPermissionGranted()).thenReturn(true, false)
        viewModel.onLocationPermissionChanged()

        viewModel.onLocationPermissionChanged()

        assertFalse(viewModel.uiState.value.isLocationPermissionGranted)
    }

    @Test
    fun `onDistanceFromAnyPositionSet updates distance mode`() {
        givenDistanceDrawn()

        viewModel.onDistanceFromAnyPositionSet()

        assertEquals(DistanceMode.FROM_ANY_POINT, distanceModeRepository.getDistanceMode())
        assertEquals(emptyList<Coordinates>(), storedPath)
    }

    @Test
    fun `onMyLocationButtonClick does nothing without a location`() {

        viewModel.onMyLocationButtonClick()

        assertNull(viewModel.uiState.value.mapState.cameraUpdate)
    }

    @Test
    fun `trackLocation does not observe location without permission`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.trackLocation() }

        verify(locationRepository, never()).observeLocation()
    }

    @Test
    fun `trackLocation zooms to the first location once location is granted`() = runTest {
        givenLocationTracked()

        locations.emit(LONDON)

        assertEquals(CameraUpdate.ZoomTo(LONDON.toUiModel()), viewModel.uiState.value.mapState.cameraUpdate)
    }

    @Test
    fun `onMyLocationButtonClick moves to the current location`() = runTest {
        givenLocationTracked()
        locations.emit(LONDON)
        viewModel.onCameraUpdateHandled()

        viewModel.onMyLocationButtonClick()

        assertEquals(CameraUpdate.MoveTo(LONDON.toUiModel()), viewModel.uiState.value.mapState.cameraUpdate)
    }

    @Test
    fun `onMapClick appends coordinates to repository in any position mode`() {
        val coordinates = Coordinates(40.7128, -74.0060)
        viewModel.onDistanceFromAnyPositionSet()

        viewModel.onMapClick(coordinates)

        assertEquals(listOf(coordinates), storedPath)
    }

    @Test
    fun `onMapClick does nothing without a location when mode is current position`() {
        val coordinates = Coordinates(40.7128, -74.0060)

        viewModel.onMapClick(coordinates)

        assertEquals(emptyList<Coordinates>(), storedPath)
    }

    @Test
    fun `onPositionByNameResolved appends first point and moves camera in any position mode`() {
        val coordinates = Coordinates(40.0, -3.0)
        viewModel.onDistanceFromAnyPositionSet()

        viewModel.onPositionByNameResolved(coordinates)

        assertEquals(listOf(coordinates), storedPath)
        assertEquals(CameraUpdate.MoveTo(coordinates.toUiModel()), viewModel.uiState.value.mapState.cameraUpdate)
    }

    @Test
    fun `a tap after a search keeps the searched place as the start in any position mode`() {
        viewModel.onDistanceFromAnyPositionSet()
        viewModel.onPositionByNameResolved(POINT_A)

        viewModel.onMapClick(POINT_B)

        assertEquals(listOf(POINT_A, POINT_B), storedPath)
        assertEquals(listOf(MarkerData(POINT_A.toUiModel()), MarkerData(POINT_B.toUiModel())), markers)
    }

    @Test
    fun `onMapLongClick with empty coordinates in any position mode shows missing start point error`() {
        viewModel.onDistanceFromAnyPositionSet()
        val message = "Start point needed"
        whenever(resourceProvider.get(R.string.toast_first_point_needed)).thenReturn(message)

        viewModel.onMapLongClick(Coordinates(40.0, -3.0))

        assertEquals(message, viewModel.uiState.value.errorMessage)
        assertEquals(emptyList<Coordinates>(), storedPath)
    }

    @Test
    fun `onMapLongClick without a location in current-position mode does nothing`() {
        viewModel.onMapLongClick(Coordinates(40.0, -3.0))

        assertEquals(emptyList<Coordinates>(), storedPath)
    }

    @Test
    fun `onAddressSearch records query when current location is available`() = runTest {
        givenLocationTracked()
        locations.emit(LONDON)

        viewModel.onAddressSearch("London")

        assertEquals("London", viewModel.uiState.value.searchAddress)
    }

    @Test
    fun `onAddressSearch records query without a current location`() {
        viewModel.onAddressSearch("Madrid")

        assertEquals("Madrid", viewModel.uiState.value.searchAddress)
    }

    @Test
    fun `onPositionByNameResolved without a location in current-position mode moves camera only`() {
        val coordinates = Coordinates(40.4168, -3.7038)

        viewModel.onPositionByNameResolved(coordinates)

        assertEquals(CameraUpdate.MoveTo(coordinates.toUiModel()), viewModel.uiState.value.mapState.cameraUpdate)
        assertEquals(emptyList<Coordinates>(), storedPath)
    }

    @Test
    fun `a long press draws the distance from the tapped point in any position mode`() {
        viewModel.onDistanceFromAnyPositionSet()
        viewModel.onMapClick(POINT_A)

        viewModel.onMapLongClick(POINT_B)

        assertEquals(listOf(POINT_A, POINT_B), drawnPath)
        assertEquals(
            listOf(
                MarkerData(POINT_A.toUiModel()),
                MarkerData(POINT_B.toUiModel(), MarkerData.InfoWindow.Visible("111.19 km")),
            ),
            markers
        )
    }

    @Test
    fun `a long press draws the distance from the current location in current position mode`() = runTest {
        givenLocationTracked()
        locations.emit(POINT_A)

        viewModel.onMapLongClick(POINT_B)

        assertEquals(listOf(POINT_A, POINT_B), drawnPath)
        assertEquals(listOf(MarkerData(POINT_B.toUiModel(), MarkerData.InfoWindow.Visible("111.19 km"))), markers)
    }

    @Test
    fun `taps add points that a long press joins into one path`() {
        viewModel.onDistanceFromAnyPositionSet()
        viewModel.onMapClick(POINT_A)
        viewModel.onMapClick(POINT_B)

        viewModel.onMapLongClick(POINT_C)

        assertEquals(listOf(POINT_A, POINT_B, POINT_C), drawnPath)
        assertEquals(MarkerData.InfoWindow.Visible("222.39 km"), markers.last().infoWindow)
    }

    @Test
    fun `a tap after a drawn distance starts a new path`() {
        givenDistanceDrawn()

        viewModel.onMapClick(POINT_C)

        assertEquals(listOf(POINT_C), storedPath)
        assertEquals(emptyList<Coordinates>(), drawnPath)
        assertEquals(listOf(MarkerData(POINT_C.toUiModel())), markers)
    }

    @Test
    fun `onDistanceFromAnyPositionSet clears map state and hides chart`() {
        givenDistanceDrawn()

        viewModel.onDistanceFromAnyPositionSet()

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

    private fun createViewModel() =
        MainViewModel(
            getDistancesUseCase,
            getStoredDistancesUseCase,
            getPositionListUseCase,
            connectionManager,
            resourceProvider,
            settingsRepository,
            distanceModeRepository,
            locationRepository,
            permissionChecker,
            coordinatesRepository,
            DistanceCalculator(),
            UnitFormatter(UnitConverter()),
            buildConfigProvider,
            MapStateMapper(context),
            loadDistancesMapper
        )

    // Grants location and tracks it, as MainActivity does while started; emit to locations to deliver one
    private fun TestScope.givenLocationTracked() {
        whenever(permissionChecker.isLocationPermissionGranted()).thenReturn(true)
        viewModel.onLocationPermissionChanged()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.trackLocation() }
    }

    // Draws POINT_A to POINT_B in any position mode
    private fun givenDistanceDrawn() {
        viewModel.onDistanceFromAnyPositionSet()
        viewModel.onMapClick(POINT_A)
        viewModel.onMapLongClick(POINT_B)
    }

    private companion object {

        // One degree apart on the equator, so 111.19 km from one to the next
        val POINT_A = Coordinates(0.0, 0.0)
        val POINT_B = Coordinates(0.0, 1.0)
        val POINT_C = Coordinates(0.0, 2.0)
        val LONDON = Coordinates(51.5074, -0.1278)
    }
}
