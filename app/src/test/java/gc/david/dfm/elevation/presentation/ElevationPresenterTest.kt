/*
 * Copyright (c) 2018 David Aguiar Gonzalez
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

import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

import java.util.ArrayList

import gc.david.dfm.ConnectionManager
import gc.david.dfm.PreferencesProvider
import gc.david.dfm.elevation.domain.ElevationUseCase

import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

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
    fun hidesChartWhenShowElevationChartPreferenceIsFalse() {
        // Given
        val dummyList = ArrayList<LatLng>()
        whenever(preferencesProvider.shouldShowElevationChart()).thenReturn(false)

        // When
        elevationPresenter.buildChart(dummyList)

        // Then
        verify(elevationView).hideChart()
    }

    @Test
    fun hidesChartWhenNoConnectionAvailable() {
        // Given
        val dummyList = ArrayList<LatLng>()
        whenever(preferencesProvider.shouldShowElevationChart()).thenReturn(true)
        whenever(connectionManager.isOnline).thenReturn(false)

        // When
        elevationPresenter.buildChart(dummyList)

        // Then
        verify(elevationView).hideChart()
    }

    @Test
    fun executesUseCaseWhenPreferenceIsActivatedAndConnectionAvailable() {
        // Given
        val coordinateList = ArrayList<LatLng>()
        whenever(preferencesProvider.shouldShowElevationChart()).thenReturn(true)
        whenever(connectionManager.isOnline).thenReturn(true)

        // When
        elevationPresenter.buildChart(coordinateList)

        // Then
        verify<ElevationUseCase>(elevationUseCase).execute(eq<List<LatLng>>(coordinateList), anyInt(), any(ElevationUseCase.Callback::class.java))
    }

    @Test
    fun buildsChartWhenUseCaseReturnsData() {
        // Given
        val coordinateList = ArrayList<LatLng>()
        whenever(preferencesProvider.shouldShowElevationChart()).thenReturn(true)
        whenever(connectionManager.isOnline).thenReturn(true)
        val elevation = gc.david.dfm.elevation.domain.model.Elevation(ArrayList())
        doAnswer {
                (it.arguments[2] as ElevationUseCase.Callback).onElevationLoaded(elevation)
        }.whenever(elevationUseCase).execute(eq<List<LatLng>>(coordinateList), anyInt(), any(ElevationUseCase.Callback::class.java))

        // When
        elevationPresenter.buildChart(coordinateList)

        // Then
        verify(elevationView).buildChart(elevation.results)
    }

    @Test
    fun doesNotBuildChartWhenUseCaseIsStopped() {
        // Given
        val coordinateList = ArrayList<LatLng>()
        whenever(preferencesProvider.shouldShowElevationChart()).thenReturn(true)
        whenever(connectionManager.isOnline).thenReturn(true)
        val elevation = gc.david.dfm.elevation.domain.model.Elevation(ArrayList())
        doAnswer {
                elevationPresenter.onReset() // reset called before thread finishes
                (it.arguments[2] as ElevationUseCase.Callback).onElevationLoaded(elevation)
        }.whenever(elevationUseCase).execute(eq<List<LatLng>>(coordinateList), anyInt(), any(ElevationUseCase.Callback::class.java))

        // When
        elevationPresenter.buildChart(coordinateList)

        // Then
        verify(elevationView, never()).buildChart(elevation.results)
    }

    @Test
    fun showsErrorWhenUseCaseReturnsError() {
        // Given
        val coordinateList = ArrayList<LatLng>()
        whenever(preferencesProvider.shouldShowElevationChart()).thenReturn(true)
        whenever(connectionManager.isOnline).thenReturn(true)
        val errorMessage = "fake error message"
        doAnswer {
                (it.arguments[2] as ElevationUseCase.Callback).onError(errorMessage)
        }.whenever(elevationUseCase).execute(eq<List<LatLng>>(coordinateList), anyInt(), any(ElevationUseCase.Callback::class.java))

        // When
        elevationPresenter.buildChart(coordinateList)

        // Then
        verify(elevationView).logError(errorMessage)
    }

    @Test
    fun doesNotShowChartWhenMinimiseButtonIsShown() {
        // Given
        whenever(elevationView.isMinimiseButtonShown).thenReturn(true)

        // When
        elevationPresenter.onChartBuilt()

        // Then
        verify(elevationView, never()).showChart()
    }

    @Test
    fun showsChartWhenMinimiseButtonIsNotShown() {
        // Given
        whenever(elevationView.isMinimiseButtonShown).thenReturn(false)

        // When
        elevationPresenter.onChartBuilt()

        // Then
        verify(elevationView).showChart()
    }
}