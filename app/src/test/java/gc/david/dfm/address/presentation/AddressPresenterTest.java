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

package gc.david.dfm.address.presentation;

import com.google.android.gms.maps.model.LatLng;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;

import gc.david.dfm.ConnectionManager;
import gc.david.dfm.address.domain.GetAddressUseCase;
import gc.david.dfm.address.domain.model.AddressCollection;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by david on 15.01.17.
 */
public class AddressPresenterTest {

    @Mock
    Address.View      addressView;
    @Mock
    GetAddressUseCase getAddressCoordinatesByNameUseCase;
    @Mock
    GetAddressUseCase getAddressNameByCoordinatesUseCase;
    @Mock
    ConnectionManager connectionManager;

    private AddressPresenter addressPresenter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        addressPresenter = new AddressPresenter(addressView,
                                                getAddressCoordinatesByNameUseCase,
                                                getAddressNameByCoordinatesUseCase,
                                                connectionManager);
    }

    @Test
    public void showsConnectionProblemsDialogWhenNoConnectionAvailableInPositionByName() {
        // Given
        when(connectionManager.isOnline()).thenReturn(false);
        String locationName = getFakeLocationName();

        // When
        addressPresenter.searchPositionByName(locationName);

        // Then
        verify(addressView).showConnectionProblemsDialog();
    }

    @Test
    public void showsProgressDialogWhenConnectionAvailableInPositionByName() {
        // Given
        when(connectionManager.isOnline()).thenReturn(true);
        String locationName = getFakeLocationName();

        // When
        addressPresenter.searchPositionByName(locationName);

        // Then
        verify(addressView).showProgressDialog();
    }

    @Test
    public void executesCoordinatesByNameUseCaseWhenConnectionAvailable() {
        // Given
        when(connectionManager.isOnline()).thenReturn(true);
        String locationName = getFakeLocationName();

        // When
        addressPresenter.searchPositionByName(locationName);

        // Then
        verify(getAddressCoordinatesByNameUseCase).execute(ArgumentMatchers.any(Object.class),
                                                           anyInt(),
                                                           any(GetAddressUseCase.Callback.class));
    }

    @Test
    public void hidesProgressDialogWhenPositionByNameUseCaseSucceeds() {
        // Given
        when(connectionManager.isOnline()).thenReturn(true);
        String locationName = getFakeLocationName();
        AddressCollection addressCollection = getEmptyAddressCollection();
        executeOnAddressLoadedAfterCoordinatesByNameUseCaseCallback(locationName, addressCollection);

        // When
        addressPresenter.searchPositionByName(locationName);

        // Then
        verify(addressView).hideProgressDialog();
    }

    @Test
    public void showsNoMatchesWhenPositionByNameUseCaseReturnZeroResults() {
        // Given
        when(connectionManager.isOnline()).thenReturn(true);
        String locationName = getFakeLocationName();
        AddressCollection addressCollection = getEmptyAddressCollection();
        executeOnAddressLoadedAfterCoordinatesByNameUseCaseCallback(locationName, addressCollection);

        // When
        addressPresenter.searchPositionByName(locationName);

        // Then
        verify(addressView).showNoMatchesMessage();
    }

    @Test
    public void showsPositionByNameWhenUseCaseReturnOneResult() {
        // Given
        when(connectionManager.isOnline()).thenReturn(true);
        String locationName = getFakeLocationName();
        gc.david.dfm.address.domain.model.Address address = getFakeAddress();
        ArrayList<gc.david.dfm.address.domain.model.Address> addressList = new ArrayList<>();
        addressList.add(address);
        AddressCollection addressCollection = new AddressCollection(addressList);
        executeOnAddressLoadedAfterCoordinatesByNameUseCaseCallback(locationName, addressCollection);

        // When
        addressPresenter.searchPositionByName(locationName);

        // Then
        verify(addressView).showPositionByName(address);
    }

    @Test
    public void showsAddressSelectionDialogWhenPositionByNameUseCaseReturnSeveralResults() {
        // Given
        when(connectionManager.isOnline()).thenReturn(true);
        String locationName = getFakeLocationName();
        gc.david.dfm.address.domain.model.Address address = getFakeAddress();
        ArrayList<gc.david.dfm.address.domain.model.Address> addressList = new ArrayList<>();
        addressList.add(address);
        addressList.add(address);
        AddressCollection addressCollection = new AddressCollection(addressList);
        executeOnAddressLoadedAfterCoordinatesByNameUseCaseCallback(locationName, addressCollection);

        // When
        addressPresenter.searchPositionByName(locationName);

        // Then
        verify(addressView).showAddressSelectionDialog(addressCollection.getAddressList());
    }

    @Test
    public void hidesProgressDialogWhenPositionByNameUseCaseFails() {
        // Given
        when(connectionManager.isOnline()).thenReturn(true);
        String locationName = getFakeLocationName();
        String errorMessage = getFakeErrorMessage();
        executeOnErrorAfterCoordinatesByNameUseCaseCallback(locationName, errorMessage);

        // When
        addressPresenter.searchPositionByName(locationName);

        // Then
        verify(addressView).hideProgressDialog();
    }

    @Test
    public void showsErrorWhenPositionByNameUseCaseFails() {
        // Given
        when(connectionManager.isOnline()).thenReturn(true);
        String locationName = getFakeLocationName();
        String errorMessage = getFakeErrorMessage();
        executeOnErrorAfterCoordinatesByNameUseCaseCallback(locationName, errorMessage);

        // When
        addressPresenter.searchPositionByName(locationName);

        // Then
        verify(addressView).showCallError(errorMessage);
    }

    @Test
    public void selectAddressInDialogShowsPositionByNameWithAddress() {
        // Given
        gc.david.dfm.address.domain.model.Address address = getFakeAddress();

        // When
        addressPresenter.selectAddressInDialog(address);

        // Then
        verify(addressView).showPositionByName(address);
    }

    @Test
    public void showsConnectionProblemsDialogWhenNoConnectionAvailableInPositionByCoordinates() {
        // Given
        when(connectionManager.isOnline()).thenReturn(false);
        LatLng coordinates = getFakeCoordinates();

        // When
        addressPresenter.searchPositionByCoordinates(coordinates);

        // Then
        verify(addressView).showConnectionProblemsDialog();
    }

    @Test
    public void showsProgressDialogWhenConnectionAvailableInPositionByCoordinates() {
        // Given
        when(connectionManager.isOnline()).thenReturn(true);
        LatLng coordinates = getFakeCoordinates();

        // When
        addressPresenter.searchPositionByCoordinates(coordinates);

        // Then
        verify(addressView).showProgressDialog();
    }

    @Test
    public void executesNameByCoordinatesUseCaseWhenConnectionAvailable() {
        // Given
        when(connectionManager.isOnline()).thenReturn(true);
        LatLng coordinates = getFakeCoordinates();

        // When
        addressPresenter.searchPositionByCoordinates(coordinates);

        // Then
        verify(getAddressNameByCoordinatesUseCase).execute(ArgumentMatchers.any(Object.class),
                                                           anyInt(),
                                                           any(GetAddressUseCase.Callback.class));
    }

    @Test
    public void hidesProgressDialogWhenUseCaseSucceedsInPositionByCoordinates() {
        // Given
        when(connectionManager.isOnline()).thenReturn(true);
        LatLng coordinates = getFakeCoordinates();
        AddressCollection addressCollection = getEmptyAddressCollection();
        executeOnAddressLoadedAfterNameByCoordinatesUseCaseCallback(coordinates, addressCollection);

        // When
        addressPresenter.searchPositionByCoordinates(coordinates);

        // Then
        verify(addressView).hideProgressDialog();
    }

    @Test
    public void showsNoMatchesWhenPositionByCoordinatesUseCaseReturnZeroResults() {
        // Given
        when(connectionManager.isOnline()).thenReturn(true);
        LatLng coordinates = getFakeCoordinates();
        AddressCollection addressCollection = getEmptyAddressCollection();
        executeOnAddressLoadedAfterNameByCoordinatesUseCaseCallback(coordinates, addressCollection);

        // When
        addressPresenter.searchPositionByCoordinates(coordinates);

        // Then
        verify(addressView).showNoMatchesMessage();
    }

    @Test
    public void showsPositionByCoordinatesWhenUseCaseReturnOneResult() {
        // Given
        when(connectionManager.isOnline()).thenReturn(true);
        LatLng coordinates = getFakeCoordinates();
        gc.david.dfm.address.domain.model.Address address = getFakeAddress();
        ArrayList<gc.david.dfm.address.domain.model.Address> addressList = new ArrayList<>();
        addressList.add(address);
        AddressCollection addressCollection = new AddressCollection(addressList);
        executeOnAddressLoadedAfterNameByCoordinatesUseCaseCallback(coordinates, addressCollection);

        // When
        addressPresenter.searchPositionByCoordinates(coordinates);

        // Then
        verify(addressView).showPositionByCoordinates(address);
    }

    @Test
    public void hidesProgressDialogWhenPositionByCoordinatesUseCaseFails() {
        // Given
        when(connectionManager.isOnline()).thenReturn(true);
        LatLng coordinates = getFakeCoordinates();
        String errorMessage = getFakeErrorMessage();
        executeOnErrorAfterNameByCoordinatesUseCaseCallback(coordinates, errorMessage);

        // When
        addressPresenter.searchPositionByCoordinates(coordinates);

        // Then
        verify(addressView).hideProgressDialog();
    }

    @Test
    public void showsErrorWhenPositionByCoordinatesUseCaseFails() {
        // Given
        when(connectionManager.isOnline()).thenReturn(true);
        LatLng coordinates = getFakeCoordinates();
        String errorMessage = getFakeErrorMessage();
        executeOnErrorAfterNameByCoordinatesUseCaseCallback(coordinates, errorMessage);

        // When
        addressPresenter.searchPositionByCoordinates(coordinates);

        // Then
        verify(addressView).showCallError(errorMessage);
    }

    private LatLng getFakeCoordinates() {
        return new LatLng(0D, 0D);
    }

    private String getFakeLocationName() {
        return "fake location name";
    }

    private gc.david.dfm.address.domain.model.Address getFakeAddress() {
        return new gc.david.dfm.address.domain.model.Address(getFakeLocationName(), getFakeCoordinates());
    }

    private String getFakeErrorMessage() {
        return "fake errorMessage";
    }

    private AddressCollection getEmptyAddressCollection() {
        return new AddressCollection(new ArrayList<gc.david.dfm.address.domain.model.Address>());
    }

    private void executeOnAddressLoadedAfterCoordinatesByNameUseCaseCallback(final String locationName,
                                                                             final AddressCollection addressCollection) {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((GetAddressUseCase.Callback) invocation.getArguments()[2]).onAddressLoaded(addressCollection);
                return null;
            }
        }).when(getAddressCoordinatesByNameUseCase)
          .execute(eq(locationName), anyInt(), any(GetAddressUseCase.Callback.class));
    }

    private void executeOnErrorAfterCoordinatesByNameUseCaseCallback(final String locationName,
                                                                     final String errorMessage) {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((GetAddressUseCase.Callback) invocation.getArguments()[2]).onError(errorMessage);
                return null;
            }
        }).when(getAddressCoordinatesByNameUseCase)
          .execute(eq(locationName), anyInt(), any(GetAddressUseCase.Callback.class));
    }

    private void executeOnAddressLoadedAfterNameByCoordinatesUseCaseCallback(final LatLng coordinates,
                                                                             final AddressCollection addressCollection) {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((GetAddressUseCase.Callback) invocation.getArguments()[2]).onAddressLoaded(addressCollection);
                return null;
            }
        }).when(getAddressNameByCoordinatesUseCase)
          .execute(eq(coordinates), anyInt(), any(GetAddressUseCase.Callback.class));
    }

    private void executeOnErrorAfterNameByCoordinatesUseCaseCallback(final LatLng coordinates,
                                                                     final String errorMessage) {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((GetAddressUseCase.Callback) invocation.getArguments()[2]).onError(errorMessage);
                return null;
            }
        }).when(getAddressNameByCoordinatesUseCase)
          .execute(eq(coordinates), anyInt(), any(GetAddressUseCase.Callback.class));
    }
}