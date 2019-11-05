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

package gc.david.dfm.elevation.domain

import com.google.android.gms.maps.model.LatLng
import com.nhaarman.mockitokotlin2.whenever
import gc.david.dfm.elevation.data.ElevationRepository
import gc.david.dfm.elevation.data.mapper.ElevationEntityDataMapper
import gc.david.dfm.elevation.data.model.ElevationEntity
import gc.david.dfm.elevation.data.model.Result
import gc.david.dfm.elevation.domain.ElevationInteractor.STATUS_OK
import gc.david.dfm.elevation.domain.model.Elevation
import gc.david.dfm.executor.Executor
import gc.david.dfm.executor.Interactor
import gc.david.dfm.executor.MainThread
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.*
import org.mockito.Mock
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import java.util.*

/**
 * Created by david on 11.01.17.
 */
class ElevationInteractorTest {

    @Mock
    lateinit var executor: Executor
    @Mock
    lateinit var mainThread: MainThread
    @Mock
    lateinit var elevationEntityDataMapper: ElevationEntityDataMapper
    @Mock
    lateinit var repository: ElevationRepository
    @Mock
    lateinit var callback: ElevationUseCase.Callback

    private lateinit var elevationInteractor: ElevationInteractor

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        elevationInteractor = ElevationInteractor(executor, mainThread, elevationEntityDataMapper, repository)

        doAnswer { (it.arguments[0] as Interactor).run() }.whenever(executor).run(any(Interactor::class.java))
        doAnswer { (it.arguments[0] as Runnable).run() }.whenever(mainThread).post(any(Runnable::class.java))
    }

    @Test
    fun showsErrorWhenCoordinateListIsEmpty() {
        // Given
        val coordinateList = ArrayList<LatLng>()

        // When
        elevationInteractor.execute(coordinateList, anyInt(), callback)

        // Then
        verify(callback).onError("Empty coordinates list")
    }

    @Test
    fun returnsCallbackWhenElevationModelIsCorrect() {
        // Given
        val coordinateList = ArrayList<LatLng>()
        coordinateList.add(LatLng(0.0, 0.0))
        val results = ArrayList<Result>()
        val elevation = 1.0
        results.add(Result.Builder().withElevation(elevation).build())
        val elevationEntity = ElevationEntity.Builder().withStatus(STATUS_OK)
                .withResults(results)
                .build()
        doAnswer { (it.arguments[2] as ElevationRepository.Callback).onSuccess(elevationEntity) }
                .whenever(repository).getElevation(anyString(), anyInt(), any(ElevationRepository.Callback::class.java))

        val elevationResults = ArrayList<Double>()
        elevationResults.add(elevation)
        val elevation1 = Elevation(elevationResults)
        whenever(elevationEntityDataMapper.transform(elevationEntity)).thenReturn(elevation1)

        // When
        elevationInteractor.execute(coordinateList, anyInt(), callback)

        // Then
        verify(callback).onElevationLoaded(any(Elevation::class.java))
    }

    @Test
    fun showsErrorWhenElevationModelIsNotCorrect() {
        // Given
        val coordinateList = ArrayList<LatLng>()
        coordinateList.add(LatLng(0.0, 0.0))
        val errorMessage = "fake error message"
        doAnswer {
                (it.arguments[2] as ElevationRepository.Callback).onError(errorMessage)
        }.whenever(repository).getElevation(anyString(), anyInt(), any(ElevationRepository.Callback::class.java))

        // When
        elevationInteractor.execute(coordinateList, anyInt(), callback)

        // Then
        verify(callback).onError(errorMessage)
    }

    @Test
    fun buildsCoordinatesPathForListWithOneCoordinate() {
        // Given
        val coordinateList = ArrayList<LatLng>()
        coordinateList.add(LatLng(0.0, 0.0))
        val coordinatesPath = "0.0,0.0"
        val maxSamples = 1

        // When
        elevationInteractor.execute(coordinateList, maxSamples, callback)

        // Then
        verify(repository).getElevation(eq(coordinatesPath), eq(maxSamples), any(ElevationRepository.Callback::class.java))
    }

    @Test
    fun buildsCoordinatesPathForListWithMoreThanOneCoordinate() {
        // Given
        val coordinateList = ArrayList<LatLng>()
        coordinateList.add(LatLng(0.0, 0.0))
        coordinateList.add(LatLng(1.0, 1.0))
        val coordinatesPath = "0.0,0.0|1.0,1.0"
        val maxSamples = 1

        // When
        elevationInteractor.execute(coordinateList, maxSamples, callback)

        // Then
        verify(repository).getElevation(eq(coordinatesPath), eq(maxSamples), any(ElevationRepository.Callback::class.java))
    }
}