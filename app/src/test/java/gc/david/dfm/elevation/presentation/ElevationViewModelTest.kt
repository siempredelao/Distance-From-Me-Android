/*
 * Copyright (c) 2021 David Aguiar Gonzalez
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
import gc.david.dfm.PreferencesProvider
import gc.david.dfm.elevation.domain.ElevationInteractor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mock
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

/**
 * Created by david on 11.01.17.
 */
@RunWith(MockitoJUnitRunner::class)
class ElevationViewModelTest {

    @Mock
    lateinit var elevationUseCase: ElevationInteractor
    @Mock
    lateinit var connectionManager: ConnectionManager
    @Mock
    lateinit var preferencesProvider: PreferencesProvider

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var viewModel: ElevationViewModel

    @Before
    fun setUp() {
        viewModel = ElevationViewModel(elevationUseCase, connectionManager, preferencesProvider)
    }

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
    fun `executes use case when preference is activated and connection available`() {
        val coordinateList = emptyList<LatLng>()
        whenever(preferencesProvider.shouldShowElevationChart()).thenReturn(true)
        whenever(connectionManager.isOnline()).thenReturn(true)

        viewModel.onCoordinatesSelected(coordinateList)

        verify(elevationUseCase).execute(eq(coordinateList), anyInt(), any())
    }

    @Test
    fun `returns elevation samples when use case returns data`() {
        val coordinateList = emptyList<LatLng>()
        whenever(preferencesProvider.shouldShowElevationChart()).thenReturn(true)
        whenever(connectionManager.isOnline()).thenReturn(true)
        val elevation = gc.david.dfm.elevation.domain.model.Elevation(emptyList())
        doAnswer {
                (it.arguments[2] as ElevationInteractor.Callback).onElevationLoaded(elevation)
        }.whenever(elevationUseCase).execute(eq(coordinateList), anyInt(), any())

        viewModel.onCoordinatesSelected(coordinateList)

        assertEquals(elevation.results, viewModel.elevationSamples.value)
    }
}