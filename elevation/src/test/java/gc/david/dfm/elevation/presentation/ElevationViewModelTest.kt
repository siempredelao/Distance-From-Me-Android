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
import gc.david.dfm.elevation.domain.ElevationError
import gc.david.dfm.elevation.domain.ElevationResult
import gc.david.dfm.elevation.domain.GetElevationByCoordinatesUseCase
import gc.david.dfm.elevation.presentation.model.ElevationUiModel
import gc.david.dfm.settings.domain.SettingsRepository
import gc.david.dfm.testsupport.CoroutineExtension
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.io.IOException

/**
 * Created by david on 11.01.17.
 */
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
        whenever(settingsRepository.shouldShowElevationChart()).thenReturn(false)

        viewModel.onCoordinatesSelected(PATH)

        assertFalse(viewModel.uiState.value.showChart)
        verifyNoInteractions(getElevationByCoordinatesUseCase)
    }

    @Test
    fun `hides chart when no connection available`() {
        whenever(settingsRepository.shouldShowElevationChart()).thenReturn(true)
        whenever(connectionManager.isOnline()).thenReturn(false)

        viewModel.onCoordinatesSelected(PATH)

        assertFalse(viewModel.uiState.value.showChart)
        verifyNoInteractions(getElevationByCoordinatesUseCase)
    }

    @Test
    fun `hides chart without requesting elevation when there are no coordinates`() {
        givenChartEnabled()

        viewModel.onCoordinatesSelected(emptyList())

        assertFalse(viewModel.uiState.value.showChart)
        verifyNoInteractions(getElevationByCoordinatesUseCase)
    }

    @Test
    fun `shows chart with formatted samples when elevation is found`() = runTest {
        givenChartEnabled(UnitSystem.IMPERIAL, altitudeUnit = "ft")
        whenever(distanceFormatter.formatAltitude(100.5, UnitSystem.IMPERIAL)).thenReturn(329.72)
        whenever(distanceFormatter.formatAltitude(200.7, UnitSystem.IMPERIAL)).thenReturn(658.46)
        whenever(getElevationByCoordinatesUseCase(PATH)).thenReturn(ElevationResult.Found(listOf(100.5, 200.7)))

        viewModel.onCoordinatesSelected(PATH)

        assertEquals(ElevationUiModel(listOf(329.72, 658.46), "ft"), viewModel.uiState.value.elevation)
        assertTrue(viewModel.uiState.value.showChart)
    }

    @Test
    fun `onShowChartHandled clears show chart flag`() = runTest {
        givenChartEnabled()
        whenever(getElevationByCoordinatesUseCase(PATH)).thenReturn(ElevationResult.Found(listOf(1.0)))
        viewModel.onCoordinatesSelected(PATH)

        viewModel.onShowChartHandled()

        assertFalse(viewModel.uiState.value.showChart)
    }

    @Test
    fun `does not show chart when there is no elevation data`() = runTest {
        givenChartEnabled()
        whenever(getElevationByCoordinatesUseCase(PATH)).thenReturn(ElevationResult.NoData)

        viewModel.onCoordinatesSelected(PATH)

        assertNull(viewModel.uiState.value.elevation)
        assertFalse(viewModel.uiState.value.showChart)
    }

    @Test
    fun `does not show chart when the request is rejected`() = runTest {
        givenChartEnabled()
        whenever(getElevationByCoordinatesUseCase(PATH))
            .thenReturn(ElevationResult.Rejected(ElevationError.OVER_QUERY_LIMIT))

        viewModel.onCoordinatesSelected(PATH)

        assertNull(viewModel.uiState.value.elevation)
        assertFalse(viewModel.uiState.value.showChart)
    }

    @Test
    fun `does not show chart when the use case throws`() = runTest {
        givenChartEnabled()
        whenever(getElevationByCoordinatesUseCase(PATH)).thenAnswer { throw IOException("Network error") }

        viewModel.onCoordinatesSelected(PATH)

        assertNull(viewModel.uiState.value.elevation)
        assertFalse(viewModel.uiState.value.showChart)
    }

    @Test
    fun `shows the latest path when an earlier request finishes later`() = runTest {
        givenChartEnabled()
        val earlierResult = CompletableDeferred<ElevationResult>()
        whenever(getElevationByCoordinatesUseCase(PATH)).doSuspendableAnswer { earlierResult.await() }
        whenever(getElevationByCoordinatesUseCase(LATER_PATH)).thenReturn(ElevationResult.Found(listOf(2.0)))
        viewModel.onCoordinatesSelected(PATH)
        viewModel.onCoordinatesSelected(LATER_PATH)

        earlierResult.complete(ElevationResult.Found(listOf(1.0)))

        assertEquals(ElevationUiModel(listOf(2.0), "m"), viewModel.uiState.value.elevation)
    }

    @Test
    fun `does not show a pending request once the chart is turned off`() = runTest {
        givenChartEnabled()
        val pendingResult = CompletableDeferred<ElevationResult>()
        whenever(getElevationByCoordinatesUseCase(PATH)).doSuspendableAnswer { pendingResult.await() }
        viewModel.onCoordinatesSelected(PATH)
        whenever(settingsRepository.shouldShowElevationChart()).thenReturn(false)
        viewModel.onCoordinatesSelected(LATER_PATH)

        pendingResult.complete(ElevationResult.Found(listOf(1.0)))

        assertNull(viewModel.uiState.value.elevation)
        assertFalse(viewModel.uiState.value.showChart)
    }

    private fun givenChartEnabled(unitSystem: UnitSystem = UnitSystem.METRIC, altitudeUnit: String = "m") {
        whenever(settingsRepository.shouldShowElevationChart()).thenReturn(true)
        whenever(connectionManager.isOnline()).thenReturn(true)
        whenever(settingsRepository.getUnitSystemPreference()).thenReturn(unitSystem)
        whenever(distanceFormatter.getAltitudeUnitLabel(unitSystem)).thenReturn(altitudeUnit)
        whenever(distanceFormatter.formatAltitude(any(), any())).thenAnswer { it.arguments[0] as Double }
    }

    private companion object {

        val PATH = listOf(Coordinates(1.0, 2.0), Coordinates(3.0, 4.0))
        val LATER_PATH = listOf(Coordinates(5.0, 6.0), Coordinates(7.0, 8.0))
    }
}
