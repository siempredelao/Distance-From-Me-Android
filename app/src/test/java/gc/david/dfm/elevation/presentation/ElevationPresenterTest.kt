/*
 * Copyright (c) 2019 David Aguiar Gonzalez
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

import com.google.android.gms.maps.model.LatLng
import com.nhaarman.mockitokotlin2.whenever
import gc.david.dfm.ConnectionManager
import gc.david.dfm.PreferencesProvider
import gc.david.dfm.elevation.domain.ElevationUseCase
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import java.util.*

/**
 * Created by david on 11.01.17.
 */
class ElevationPresenterTest {

    @Mock
    lateinit var elevationView: Elevation.View
    @Mock
    lateinit var elevationUseCase: ElevationUseCase
    @Mock
    lateinit var connectionManager: ConnectionManager
    @Mock
    lateinit var preferencesProvider: PreferencesProvider

    private lateinit var elevationPresenter: ElevationPresenter

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        elevationPresenter = ElevationPresenter(elevationView,
                elevationUseCase,
                connectionManager,
                preferencesProvider)
    }

    @Test
    fun `hides chart when show elevation chart preference is false`() {
        val dummyList = emptyList<LatLng>()
        whenever(preferencesProvider.shouldShowElevationChart()).thenReturn(false)

        elevationPresenter.buildChart(dummyList)

        verify(elevationView).hideChart()
    }

    @Test
    fun `hides chart when no connection available`() {
        val dummyList = emptyList<LatLng>()
        whenever(preferencesProvider.shouldShowElevationChart()).thenReturn(true)
        whenever(connectionManager.isOnline).thenReturn(false)

        elevationPresenter.buildChart(dummyList)

        verify(elevationView).hideChart()
    }

    @Test
    fun `executes use case when preference is activated and connection available`() {
        val coordinateList = emptyList<LatLng>()
        whenever(preferencesProvider.shouldShowElevationChart()).thenReturn(true)
        whenever(connectionManager.isOnline).thenReturn(true)

        elevationPresenter.buildChart(coordinateList)

        verify(elevationUseCase).execute(eq(coordinateList), anyInt(), any())
    }

    @Test
    fun `builds chart when use case returns data`() {
        val coordinateList = emptyList<LatLng>()
        whenever(preferencesProvider.shouldShowElevationChart()).thenReturn(true)
        whenever(connectionManager.isOnline).thenReturn(true)
        val elevation = gc.david.dfm.elevation.domain.model.Elevation(emptyList())
        doAnswer {
                (it.arguments[2] as ElevationUseCase.Callback).onElevationLoaded(elevation)
        }.whenever(elevationUseCase).execute(eq(coordinateList), anyInt(), any())

        elevationPresenter.buildChart(coordinateList)

        verify(elevationView).buildChart(elevation.results)
    }

    @Test
    fun `does not build chart when use case is stopped`() {
        val coordinateList = emptyList<LatLng>()
        whenever(preferencesProvider.shouldShowElevationChart()).thenReturn(true)
        whenever(connectionManager.isOnline).thenReturn(true)
        val elevation = gc.david.dfm.elevation.domain.model.Elevation(ArrayList())
        doAnswer {
                elevationPresenter.onReset() // reset called before thread finishes
                (it.arguments[2] as ElevationUseCase.Callback).onElevationLoaded(elevation)
        }.whenever(elevationUseCase).execute(eq(coordinateList), anyInt(), any())

        elevationPresenter.buildChart(coordinateList)

        verify(elevationView, never()).buildChart(elevation.results)
    }

    @Test
    fun `shows error when use case returns error`() {
        val coordinateList = emptyList<LatLng>()
        whenever(preferencesProvider.shouldShowElevationChart()).thenReturn(true)
        whenever(connectionManager.isOnline).thenReturn(true)
        val errorMessage = "fake error message"
        doAnswer {
                (it.arguments[2] as ElevationUseCase.Callback).onError(errorMessage)
        }.whenever(elevationUseCase).execute(eq(coordinateList), anyInt(), any())

        elevationPresenter.buildChart(coordinateList)

        verify(elevationView).logError(errorMessage)
    }

    @Test
    fun `does not show chart when minimise button is shown`() {
        whenever(elevationView.isMinimiseButtonShown).thenReturn(true)

        elevationPresenter.onChartBuilt()

        verify(elevationView, never()).showChart()
    }

    @Test
    fun `shows chart when minimise button is not shown`() {
        whenever(elevationView.isMinimiseButtonShown).thenReturn(false)

        elevationPresenter.onChartBuilt()

        verify(elevationView).showChart()
    }
}