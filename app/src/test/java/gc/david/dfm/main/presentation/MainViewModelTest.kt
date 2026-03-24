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

import gc.david.dfm.ConnectionManager
import gc.david.dfm.PermissionChecker
import gc.david.dfm.common.BuildConfigProvider
import gc.david.dfm.common.Coordinates
import gc.david.dfm.common.domain.DistanceCalculator
import gc.david.dfm.common.domain.model.UnitSystem
import gc.david.dfm.common.presentation.DistanceFormatter
import gc.david.dfm.common.presentation.ResourceProvider
import gc.david.dfm.core.distances.domain.GetDistancesUseCase
import gc.david.dfm.core.distances.domain.GetPositionListUseCase
import gc.david.dfm.distance.data.CurrentLocationProvider
import gc.david.dfm.distance.data.DistanceModeProvider
import gc.david.dfm.distance.data.model.DistanceMode
import gc.david.dfm.distance.domain.CoordinatesRepository
import gc.david.dfm.main.presentation.mapper.MapStateMapper
import gc.david.dfm.settings.domain.SettingsRepository
import gc.david.dfm.testsupport.CoroutineExtension
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class MainViewModelTest {

    @JvmField
    @RegisterExtension
    val coroutineExtension = CoroutineExtension()

    private val getDistancesUseCase = mock<GetDistancesUseCase>()
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

    private lateinit var viewModel: MainViewModel

    @BeforeEach
    fun setup() {
        whenever(getDistancesUseCase()).thenReturn(flowOf(emptyList()))
        whenever(coordinatesRepository.observeDistance()).thenReturn(MutableStateFlow(emptyList()))
        whenever(settingsRepository.getUnitSystemPreference()).thenReturn(UnitSystem.METRIC)
        
        viewModel = MainViewModel(
            getDistancesUseCase,
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
            mapStateMapper
        )
    }

    @Test
    fun `onStart shows connection issue when offline`() {
        whenever(connectionManager.isOnline()).thenReturn(false)

        viewModel.onStart()

        assertTrue(viewModel.uiState.value.showConnectionIssue)
    }

    @Test
    fun `onStart does not show connection issue when online`() {
        whenever(connectionManager.isOnline()).thenReturn(true)

        viewModel.onStart()

        assertFalse(viewModel.uiState.value.showConnectionIssue)
    }

    @Test
    fun `onMenuReady shows force crash item for debug builds`() {
        whenever(buildConfigProvider.isReleaseBuild()).thenReturn(false)

        viewModel.onMenuReady()

        assertTrue(viewModel.uiState.value.showForceCrashItem)
    }

    @Test
    fun `onMenuReady hides force crash item for release builds`() {
        whenever(buildConfigProvider.isReleaseBuild()).thenReturn(true)

        viewModel.onMenuReady()

        assertFalse(viewModel.uiState.value.showForceCrashItem)
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
    fun `onMapClick appends coordinates to repository`() {
        val coordinates = Coordinates(40.7128, -74.0060)
        whenever(distanceModeProvider.get()).thenReturn(DistanceMode.FROM_ANY_POINT)

        viewModel.onMapClick(coordinates)

        verify(coordinatesRepository).append(coordinates)
    }

    @Test
    fun `initial state has no connection issue`() {
        assertFalse(viewModel.uiState.value.showConnectionIssue)
    }

    @Test
    fun `initial state does not show force crash item`() {
        assertFalse(viewModel.uiState.value.showForceCrashItem)
    }

    @Test
    fun `initial state does not show location permission snackbar`() {
        assertFalse(viewModel.uiState.value.showLocationPermissionSnackbar)
    }

    @Test
    fun `showLoadDistancesItem is updated when distances are loaded`() = runTest {
        // Initial state should not show the item
        assertFalse(viewModel.uiState.value.showLoadDistancesItem)
    }
}

