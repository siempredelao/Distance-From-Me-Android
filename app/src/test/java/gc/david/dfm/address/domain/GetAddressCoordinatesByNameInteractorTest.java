/*
 * Copyright (c) 2017 David Aguiar Gonzalez
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

package gc.david.dfm.address.domain;

import com.google.android.gms.maps.model.LatLng;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

import gc.david.dfm.address.data.AddressRepository;
import gc.david.dfm.address.data.mapper.AddressCollectionEntityDataMapper;
import gc.david.dfm.address.data.model.AddressCollectionEntity;
import gc.david.dfm.address.data.model.Geometry;
import gc.david.dfm.address.data.model.Location;
import gc.david.dfm.address.data.model.Result;
import gc.david.dfm.address.domain.model.Address;
import gc.david.dfm.address.domain.model.AddressCollection;
import gc.david.dfm.executor.Executor;
import gc.david.dfm.executor.Interactor;
import gc.david.dfm.executor.MainThread;

import static gc.david.dfm.address.domain.GetAddressAbstractInteractor.STATUS_INVALID_REQUEST;
import static gc.david.dfm.address.domain.GetAddressAbstractInteractor.STATUS_OK;
import static gc.david.dfm.address.domain.GetAddressAbstractInteractor.STATUS_ZERO_RESULTS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by david on 15.01.17.
 */
public class GetAddressCoordinatesByNameInteractorTest {

    @Mock
    Executor                          executor;
    @Mock
    MainThread                        mainThread;
    @Mock
    AddressCollectionEntityDataMapper dataMapper;
    @Mock
    AddressRepository                 repository;
    @Mock
    GetAddressUseCase.Callback        callback;

    private GetAddressCoordinatesByNameInteractor getAddressInteractor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        getAddressInteractor = new GetAddressCoordinatesByNameInteractor(executor, mainThread, dataMapper, repository);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((Interactor) invocation.getArguments()[0]).run();
                return null;
            }
        }).when(executor).run(any(Interactor.class));
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((Runnable) invocation.getArguments()[0]).run();
                return null;
            }
        }).when(mainThread).post(any(Runnable.class));
    }

    @Test
    public void loadsEmptyAddressWhenAddressCollectionModelIsCorrect() {
        // Given
        String fakeLocationName = "Berlin DE";
        ArrayList<Result> results = new ArrayList<>();
        final AddressCollectionEntity addressCollectionEntity = new AddressCollectionEntity.Builder().withStatus(
                STATUS_ZERO_RESULTS).withResults(results).build();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((AddressRepository.Callback) invocation.getArguments()[1]).onSuccess(addressCollectionEntity);
                return null;
            }
        }).when(repository).getCoordinatesByName(anyString(), any(AddressRepository.Callback.class));

        List<Address> addressList = new ArrayList<>();
        AddressCollection addressCollection = new AddressCollection(addressList);
        when(dataMapper.transform(addressCollectionEntity)).thenReturn(addressCollection);

        // When
        getAddressInteractor.execute(fakeLocationName, anyInt(), callback);

        // Then
        verify(callback).onAddressLoaded(addressCollection);
    }

    @Test
    public void loadsNonEmptyAddressWhenAddressCollectionModelIsCorrect() {
        // Given
        String fakeLocationName = "Berlin DE";
        ArrayList<Result> results = new ArrayList<>();
        double latitude = 1D;
        double longitude = 1D;
        Location location = new Location.Builder().withLatitude(latitude).withLongitude(longitude).build();
        Geometry geometry = new Geometry.Builder().withLocation(location).build();
        results.add(new Result.Builder().withFormattedAddress(fakeLocationName).withGeometry(geometry).build());
        final AddressCollectionEntity addressCollectionEntity = new AddressCollectionEntity.Builder().withStatus(
                STATUS_OK).withResults(results).build();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((AddressRepository.Callback) invocation.getArguments()[1]).onSuccess(addressCollectionEntity);
                return null;
            }
        }).when(repository).getCoordinatesByName(anyString(), any(AddressRepository.Callback.class));

        Address address = new Address(fakeLocationName, new LatLng(latitude, longitude));
        List<Address> addressList = new ArrayList<>();
        addressList.add(address);
        AddressCollection addressCollection = new AddressCollection(addressList);
        when(dataMapper.transform(addressCollectionEntity)).thenReturn(addressCollection);

        // When
        getAddressInteractor.execute(fakeLocationName, anyInt(), callback);

        // Then
        verify(callback).onAddressLoaded(addressCollection);
    }

    @Test
    public void returnsErrorCallbackWhenAddressCollectionModelIsNotCorrect() {
        // Given
        final AddressCollectionEntity addressCollectionEntity = new AddressCollectionEntity.Builder().withStatus(
                STATUS_INVALID_REQUEST).build();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((AddressRepository.Callback) invocation.getArguments()[1]).onSuccess(addressCollectionEntity);
                return null;
            }
        }).when(repository).getCoordinatesByName(anyString(), any(AddressRepository.Callback.class));
        String fakeLocationName = "Berlin DE";

        // When
        getAddressInteractor.execute(fakeLocationName, anyInt(), callback);

        // Then
        verify(callback).onError(STATUS_INVALID_REQUEST);
    }

    @Test
    public void returnsErrorCallbackWhenRepositoryErrorCallback() {
        // Given
        final String fakeErrorMessage = "fake error message";
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((AddressRepository.Callback) invocation.getArguments()[1]).onError(fakeErrorMessage);
                return null;
            }
        }).when(repository).getCoordinatesByName(anyString(), any(AddressRepository.Callback.class));
        String fakeLocationName = "Berlin DE";

        // When
        getAddressInteractor.execute(fakeLocationName, anyInt(), callback);

        // Then
        verify(callback).onError(fakeErrorMessage);
    }
}