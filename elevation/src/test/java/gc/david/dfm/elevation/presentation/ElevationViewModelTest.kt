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
import gc.david.dfm.common.Coordinate
import gc.david.dfm.testsupport.CoroutineDispatcherRule
import gc.david.dfm.elevation.domain.GetElevationByCoordinatesUseCase
import gc.david.dfm.elevation.presentation.model.ElevationModel
import gc.david.dfm.settings.domain.SettingsRepository
import gc.david.dfm.settings.domain.model.UnitSystem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Created by david on 11.01.17.
 */
@ExperimentalCoroutinesApi
class ElevationViewModelTest {

    private val getElevationByCoordinatesUseCase = mock<GetElevationByCoordinatesUseCase>()
    private val connectionManager = mock<ConnectionManager>()
    private val settingsRepository = mock<SettingsRepository>()

    private val viewModel =
        ElevationViewModel(getElevationByCoordinatesUseCase, connectionManager, settingsRepository)

    @get:Rule val coroutinesDispatcherRule = CoroutineDispatcherRule()

    @Test
    fun `hides chart when show elevation chart preference is false`() {
        val dummyList = emptyList<Coordinate>()
        whenever(settingsRepository.shouldShowElevationChart()).thenReturn(false)

        viewModel.onCoordinatesSelected(dummyList)

        assertTrue(viewModel.uiState.value.hideChart)
    }

    @Test
    fun `hides chart when no connection available`() {
        val dummyList = emptyList<Coordinate>()
        whenever(settingsRepository.shouldShowElevationChart()).thenReturn(true)
        whenever(connectionManager.isOnline()).thenReturn(false)

        viewModel.onCoordinatesSelected(dummyList)

        assertTrue(viewModel.uiState.value.hideChart)
    }

    @Test
    fun `executes use case when preference is activated and connection available`() = runTest {
        val coordinateList = emptyList<Coordinate>()
        whenever(settingsRepository.shouldShowElevationChart()).thenReturn(true)
        whenever(connectionManager.isOnline()).thenReturn(true)
        whenever(getElevationByCoordinatesUseCase(any())).thenReturn(Result.failure(Throwable()))

        viewModel.onCoordinatesSelected(coordinateList)

        verify(getElevationByCoordinatesUseCase).invoke(coordinateList)
    }

    @Test
    fun `returns elevation samples when use case returns data`() = runTest {
        val coordinateList = emptyList<Coordinate>()
        whenever(settingsRepository.shouldShowElevationChart()).thenReturn(true)
        whenever(settingsRepository.getUnitSystemPreference()).thenReturn(UnitSystem.METRIC)
        whenever(connectionManager.isOnline()).thenReturn(true)
        val elevation = gc.david.dfm.elevation.domain.model.Elevation(emptyList())
        whenever(getElevationByCoordinatesUseCase(any())).thenReturn(Result.success(elevation))

        viewModel.onCoordinatesSelected(coordinateList)

        val expectedElevationModel = ElevationModel(elevation.results, "m")
        assertEquals(expectedElevationModel, viewModel.uiState.value.elevationModel)
    }
}
