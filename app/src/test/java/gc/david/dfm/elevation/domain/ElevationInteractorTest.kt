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

package gc.david.dfm.elevation.domain

import com.google.android.gms.maps.model.LatLng
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.whenever
import gc.david.dfm.elevation.data.ElevationRepository
import gc.david.dfm.elevation.data.mapper.ElevationEntityDataMapper
import gc.david.dfm.elevation.data.model.ElevationEntity
import gc.david.dfm.elevation.data.model.ElevationStatus
import gc.david.dfm.elevation.data.model.Result
import gc.david.dfm.elevation.domain.model.Elevation
import gc.david.dfm.executor.Executor
import gc.david.dfm.executor.Interactor
import gc.david.dfm.executor.MainThread
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

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

        doAnswer { (it.arguments[0] as Interactor).run() }.whenever(executor).run(any())
        doAnswer { (it.arguments[0] as Runnable).run() }.whenever(mainThread).post(any())
    }

    @Test
    fun `shows error when coordinate list is empty`() {
        val coordinateList = emptyList<LatLng>()

        elevationInteractor.execute(coordinateList, anyInt(), callback)

        verify(callback).onError("Empty coordinates list")
    }

    @Test
    fun `returns callback when elevation model is correct`() {
        val coordinateList = mutableListOf(LatLng(0.0, 0.0))
        val results = ArrayList<Result>()
        val elevation = 1.0
        results.add(Result(elevation))
        val elevationEntity = ElevationEntity(results, ElevationStatus.OK)
        doAnswer { (it.arguments[2] as ElevationRepository.Callback).onSuccess(elevationEntity) }
                .whenever(repository).getElevation(anyString(), anyInt(), any())

        val elevationResults = mutableListOf(elevation)
        val elevation1 = Elevation(elevationResults)
        whenever(elevationEntityDataMapper.transform(elevationEntity)).thenReturn(elevation1)

        elevationInteractor.execute(coordinateList, anyInt(), callback)

        verify(callback).onElevationLoaded(any())
    }

    @Test
    fun `shows error when elevation model is not correct`() {
        val coordinateList = mutableListOf(LatLng(0.0, 0.0))
        val errorMessage = "fake error message"
        doAnswer {
                (it.arguments[2] as ElevationRepository.Callback).onError(errorMessage)
        }.whenever(repository).getElevation(anyString(), anyInt(), any())

        elevationInteractor.execute(coordinateList, anyInt(), callback)

        verify(callback).onError(errorMessage)
    }

    @Test
    fun `builds coordinates path for list with one coordinate`() {
        val coordinateList = mutableListOf(LatLng(0.0, 0.0))
        val coordinatesPath = "0.0,0.0"
        val maxSamples = 1

        elevationInteractor.execute(coordinateList, maxSamples, callback)

        verify(repository).getElevation(eq(coordinatesPath), eq(maxSamples), any())
    }

    @Test
    fun `builds coordinates path for list with more than one coordinate`() {
        val coordinateList = mutableListOf(LatLng(0.0, 0.0), LatLng(1.0, 1.0))
        val coordinatesPath = "0.0,0.0|1.0,1.0"
        val maxSamples = 1

        elevationInteractor.execute(coordinateList, maxSamples, callback)

        verify(repository).getElevation(eq(coordinatesPath), eq(maxSamples), any())
    }
}