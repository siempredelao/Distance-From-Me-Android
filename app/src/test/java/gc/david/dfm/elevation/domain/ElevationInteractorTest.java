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

package gc.david.dfm.elevation.domain;

import com.google.android.gms.maps.model.LatLng;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

import gc.david.dfm.elevation.data.ElevationRepository;
import gc.david.dfm.elevation.data.mapper.ElevationEntityDataMapper;
import gc.david.dfm.elevation.data.model.ElevationEntity;
import gc.david.dfm.elevation.data.model.Result;
import gc.david.dfm.elevation.domain.model.Elevation;
import gc.david.dfm.executor.Executor;
import gc.david.dfm.executor.Interactor;
import gc.david.dfm.executor.MainThread;

import static gc.david.dfm.elevation.domain.ElevationInteractor.STATUS_OK;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by david on 11.01.17.
 */
public class ElevationInteractorTest {

    @Mock
    Executor                  executor;
    @Mock
    MainThread                mainThread;
    @Mock
    ElevationEntityDataMapper elevationEntityDataMapper;
    @Mock
    ElevationRepository       repository;
    @Mock
    ElevationUseCase.Callback callback;

    private ElevationInteractor elevationInteractor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        elevationInteractor = new ElevationInteractor(executor, mainThread, elevationEntityDataMapper, repository);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                ((Interactor) invocation.getArguments()[0]).run();
                return null;
            }
        }).when(executor).run(any(Interactor.class));
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                ((Runnable) invocation.getArguments()[0]).run();
                return null;
            }
        }).when(mainThread).post(any(Runnable.class));
    }

    @Test
    public void showsErrorWhenCoordinateListIsEmpty() {
        // Given
        List<LatLng> coordinateList = new ArrayList<>();

        // When
        elevationInteractor.execute(coordinateList, anyInt(), callback);

        // Then
        verify(callback).onError("Empty coordinates list");
    }

    @Test
    public void returnsCallbackWhenElevationModelIsCorrect() {
        // Given
        List<LatLng> coordinateList = new ArrayList<>();
        coordinateList.add(new LatLng(0D, 0D));
        List<Result> results = new ArrayList<>();
        double elevation = 1D;
        results.add(new Result.Builder().withElevation(elevation).build());
        final ElevationEntity elevationEntity = new ElevationEntity.Builder().withStatus(STATUS_OK)
                                                                             .withResults(results)
                                                                             .build();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                ((ElevationRepository.Callback) invocation.getArguments()[2]).onSuccess(elevationEntity);
                return null;
            }
        }).when(repository).getElevation(anyString(), anyInt(), any(ElevationRepository.Callback.class));

        List<Double> elevationResults = new ArrayList<>();
        elevationResults.add(elevation);
        Elevation elevation1 = new Elevation(elevationResults);
        when(elevationEntityDataMapper.transform(elevationEntity)).thenReturn(elevation1);

        // When
        elevationInteractor.execute(coordinateList, anyInt(), callback);

        // Then
        verify(callback).onElevationLoaded(any(Elevation.class));
    }

    @Test
    public void showsErrorWhenElevationModelIsNotCorrect() {
        // Given
        List<LatLng> coordinateList = new ArrayList<>();
        coordinateList.add(new LatLng(0D, 0D));
        final String errorMessage = "fake error message";
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                ((ElevationRepository.Callback) invocation.getArguments()[2]).onError(errorMessage);
                return null;
            }
        }).when(repository).getElevation(anyString(), anyInt(), any(ElevationRepository.Callback.class));

        // When
        elevationInteractor.execute(coordinateList, anyInt(), callback);

        // Then
        verify(callback).onError(errorMessage);
    }

    @Test
    public void buildsCoordinatesPathForListWithOneCoordinate() {
        // Given
        List<LatLng> coordinateList = new ArrayList<>();
        coordinateList.add(new LatLng(0D, 0D));
        String coordinatesPath = "0.0,0.0";
        int maxSamples = 1;

        // When
        elevationInteractor.execute(coordinateList, maxSamples, callback);

        // Then
        verify(repository).getElevation(eq(coordinatesPath), eq(maxSamples), any(ElevationRepository.Callback.class));
    }

    @Test
    public void buildsCoordinatesPathForListWithMoreThanOneCoordinate() {
        // Given
        List<LatLng> coordinateList = new ArrayList<>();
        coordinateList.add(new LatLng(0D, 0D));
        coordinateList.add(new LatLng(1D, 1D));
        String coordinatesPath = "0.0,0.0|1.0,1.0";
        int maxSamples = 1;

        // When
        elevationInteractor.execute(coordinateList, maxSamples, callback);

        // Then
        verify(repository).getElevation(eq(coordinatesPath), eq(maxSamples), any(ElevationRepository.Callback.class));
    }
}