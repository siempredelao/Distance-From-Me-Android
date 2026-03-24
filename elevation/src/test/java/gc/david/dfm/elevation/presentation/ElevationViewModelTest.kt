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

import gc.david.dfm.ConnectionManager
import gc.david.dfm.common.Coordinates
import gc.david.dfm.common.domain.model.UnitSystem
import gc.david.dfm.common.presentation.DistanceFormatter
import gc.david.dfm.testsupport.CoroutineExtension
import gc.david.dfm.elevation.domain.GetElevationByCoordinatesUseCase
import gc.david.dfm.elevation.domain.model.Elevation
import gc.david.dfm.elevation.domain.model.ElevationStatus
import gc.david.dfm.elevation.presentation.model.ElevationUiModel
import gc.david.dfm.settings.domain.SettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Created by david on 11.01.17.
 */
@ExperimentalCoroutinesApi
class ElevationViewModelTest {

    @JvmField
    @RegisterExtension
    val coroutineExtension = CoroutineExtension()

    private val getElevationByCoordinatesUseCase = mock<GetElevationByCoordinatesUseCase>()
    private val connectionManager = mock<ConnectionManager>()
    private val settingsRepository = mock<SettingsRepository>()
    private val distanceFormatter = mock<DistanceFormatter>()

    private val viewModel =
        ElevationViewModel(
            getElevationByCoordinatesUseCase,
            connectionManager,
            settingsRepository,
            distanceFormatter
        )


    @Test
    fun `hides chart when show elevation chart preference is false`() {
        val dummyList = emptyList<Coordinates>()
        whenever(settingsRepository.shouldShowElevationChart()).thenReturn(false)

        viewModel.onCoordinatesSelected(dummyList)

        assertTrue(viewModel.uiState.value.hideChart)
    }

    @Test
    fun `hides chart when no connection available`() {
        val dummyList = emptyList<Coordinates>()
        whenever(settingsRepository.shouldShowElevationChart()).thenReturn(true)
        whenever(connectionManager.isOnline()).thenReturn(false)

        viewModel.onCoordinatesSelected(dummyList)

        assertTrue(viewModel.uiState.value.hideChart)
    }

    @Test
    fun `returns elevation samples when use case returns data`() = runTest {
        val coordinatesList = emptyList<Coordinates>()
        val unitSystem = UnitSystem.METRIC
        whenever(settingsRepository.shouldShowElevationChart()).thenReturn(true)
        whenever(settingsRepository.getUnitSystemPreference()).thenReturn(unitSystem)
        whenever(connectionManager.isOnline()).thenReturn(true)
        whenever(distanceFormatter.getAltitudeUnitLabel(unitSystem)).thenReturn("m")
        val elevation = Elevation(emptyList(), ElevationStatus.OK)
        whenever(getElevationByCoordinatesUseCase(any())).thenReturn(Result.success(elevation))

        viewModel.onCoordinatesSelected(coordinatesList)

        val expectedElevationModel = ElevationUiModel(elevation.results, "m")
        assertEquals(expectedElevationModel, viewModel.uiState.value.elevation)
    }

    @Test
    fun `onHideChartHandled clears hide chart flag`() = runTest {
        val dummyList = emptyList<Coordinates>()
        whenever(settingsRepository.shouldShowElevationChart()).thenReturn(false)
        viewModel.onCoordinatesSelected(dummyList)
        assertTrue(viewModel.uiState.value.hideChart)

        viewModel.onHideChartHandled()

        assertEquals(false, viewModel.uiState.value.hideChart)
    }

    @Test
    fun `handles error when use case fails`() = runTest {
        val coordinatesList = listOf(Coordinates(1.0, 2.0))
        val unitSystem = UnitSystem.METRIC
        whenever(settingsRepository.shouldShowElevationChart()).thenReturn(true)
        whenever(settingsRepository.getUnitSystemPreference()).thenReturn(unitSystem)
        whenever(connectionManager.isOnline()).thenReturn(true)
        val exception = Exception("Elevation error")
        whenever(getElevationByCoordinatesUseCase(any())).thenReturn(Result.failure(exception))

        viewModel.onCoordinatesSelected(coordinatesList)
        testScheduler.advanceUntilIdle()

        // Should not crash, error is logged
        assertEquals(null, viewModel.uiState.value.elevation)
    }

    @Test
    fun `formats elevation results with correct altitude normalization`() = runTest {
        val coordinatesList = listOf(Coordinates(1.0, 2.0), Coordinates(3.0, 4.0))
        val unitSystem = UnitSystem.IMPERIAL
        val rawElevations = listOf(100.5, 200.7, 300.9)
        whenever(settingsRepository.shouldShowElevationChart()).thenReturn(true)
        whenever(settingsRepository.getUnitSystemPreference()).thenReturn(unitSystem)
        whenever(connectionManager.isOnline()).thenReturn(true)
        whenever(distanceFormatter.formatAltitude(100.5, unitSystem)).thenReturn(329.72)
        whenever(distanceFormatter.formatAltitude(200.7, unitSystem)).thenReturn(658.46)
        whenever(distanceFormatter.formatAltitude(300.9, unitSystem)).thenReturn(987.20)
        whenever(distanceFormatter.getAltitudeUnitLabel(unitSystem)).thenReturn("ft")
        val elevation = Elevation(rawElevations, ElevationStatus.OK)
        whenever(getElevationByCoordinatesUseCase(any())).thenReturn(Result.success(elevation))

        viewModel.onCoordinatesSelected(coordinatesList)
        testScheduler.advanceUntilIdle()

        val result = viewModel.uiState.value.elevation
        assertEquals(3, result?.elevationList?.size)
        assertEquals("ft", result?.altitudeUnit)
        assertEquals(329.72, result?.elevationList?.get(0))
    }
}
