/*
 * Copyright (c) 2022 David Aguiar Gonzalez
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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.android.gms.maps.model.LatLng
import gc.david.dfm.ConnectionManager
import gc.david.dfm.CoroutineDispatcherRule
import gc.david.dfm.PreferencesProvider
import gc.david.dfm.elevation.domain.GetElevationByCoordinatesUseCase
import gc.david.dfm.elevation.presentation.model.ElevationModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
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
    private val preferencesProvider = mock<PreferencesProvider>()

    private val viewModel =
        ElevationViewModel(getElevationByCoordinatesUseCase, connectionManager, preferencesProvider)

    @get:Rule var instantTaskRule: TestRule = InstantTaskExecutorRule()
    @get:Rule val coroutinesDispatcherRule = CoroutineDispatcherRule()

    @Test
    fun `hides chart when show elevation chart preference is false`() {
        val dummyList = emptyList<LatLng>()
        whenever(preferencesProvider.shouldShowElevationChart()).thenReturn(false)

        viewModel.onCoordinatesSelected(dummyList)

        assertTrue(viewModel.hideChartEvent.value!!.peekContent() == Unit)
    }

    @Test
    fun `hides chart when no connection available`() {
        val dummyList = emptyList<LatLng>()
        whenever(preferencesProvider.shouldShowElevationChart()).thenReturn(true)
        whenever(connectionManager.isOnline()).thenReturn(false)

        viewModel.onCoordinatesSelected(dummyList)

        assertTrue(viewModel.hideChartEvent.value!!.peekContent() == Unit)
    }

    @Test
    fun `executes use case when preference is activated and connection available`() = runTest {
        val coordinateList = emptyList<LatLng>()
        whenever(preferencesProvider.shouldShowElevationChart()).thenReturn(true)
        whenever(connectionManager.isOnline()).thenReturn(true)

        viewModel.onCoordinatesSelected(coordinateList)

        verify(getElevationByCoordinatesUseCase).invoke(coordinateList)
    }

    @Test
    fun `returns elevation samples when use case returns data`() = runTest {
        val coordinateList = emptyList<LatLng>()
        whenever(preferencesProvider.shouldShowElevationChart()).thenReturn(true)
        whenever(connectionManager.isOnline()).thenReturn(true)
        val elevation = gc.david.dfm.elevation.domain.model.Elevation(emptyList())
        whenever(getElevationByCoordinatesUseCase(any())).thenReturn(Result.success(elevation))

        viewModel.onCoordinatesSelected(coordinateList)

        val expectedElevationModel = ElevationModel(elevation.results, "m")
        assertEquals(expectedElevationModel, viewModel.elevationSamples.value)
    }
}