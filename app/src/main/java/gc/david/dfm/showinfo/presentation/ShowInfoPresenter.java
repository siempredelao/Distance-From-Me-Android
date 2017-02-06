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

package gc.david.dfm.showinfo.presentation;

import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import gc.david.dfm.ConnectionManager;
import gc.david.dfm.address.domain.GetAddressUseCase;
import gc.david.dfm.address.domain.model.Address;
import gc.david.dfm.address.domain.model.AddressCollection;
import gc.david.dfm.distance.domain.InsertDistanceUseCase;
import gc.david.dfm.model.Distance;
import gc.david.dfm.model.Position;

/**
 * Created by david on 15.01.17.
 */
public class ShowInfoPresenter implements ShowInfo.Presenter {

    private final ShowInfo.View         showInfoView;
    private final GetAddressUseCase     getOriginAddressNameByCoordinatesUseCase;
    private final GetAddressUseCase     getDestinationAddressNameByCoordinatesUseCase;
    private final InsertDistanceUseCase insertDistanceUseCase;
    private final ConnectionManager     connectionManager;

    public ShowInfoPresenter(final ShowInfo.View showInfoView,
                             final GetAddressUseCase getOriginAddressNameByCoordinatesUseCase,
                             final GetAddressUseCase getDestinationAddressNameByCoordinatesUseCase,
                             final InsertDistanceUseCase insertDistanceUseCase,
                             final ConnectionManager connectionManager) {
        this.showInfoView = showInfoView;
        this.getOriginAddressNameByCoordinatesUseCase = getOriginAddressNameByCoordinatesUseCase;
        this.getDestinationAddressNameByCoordinatesUseCase = getDestinationAddressNameByCoordinatesUseCase;
        this.insertDistanceUseCase = insertDistanceUseCase;
        this.connectionManager = connectionManager;
    }

    @Override
    public void searchPositionByCoordinates(final LatLng originLatLng, final LatLng destinationLatLng) {
        if (!connectionManager.isOnline()) {
            showInfoView.showNoInternetError();
        } else {
            showInfoView.showProgress();

            getOriginAddress(getOriginAddressNameByCoordinatesUseCase, originLatLng, true);
            getOriginAddress(getDestinationAddressNameByCoordinatesUseCase, destinationLatLng, false);
        }
    }

    private void getOriginAddress(final GetAddressUseCase getAddressUseCase,
                                  final LatLng latLng,
                                  final boolean isOrigin) {
        getAddressUseCase.execute(latLng, 1, new GetAddressUseCase.Callback() {
            @Override
            public void onAddressLoaded(final AddressCollection addressCollection) {
                showInfoView.hideProgress();

                final List<Address> addressList = addressCollection.getAddressList();
                if (addressList.isEmpty()) {
                    showInfoView.showNoMatchesMessage(isOrigin);
                } else {
                    showInfoView.setAddress(addressList.get(0).getFormattedAddress(), isOrigin);
                }
            }

            @Override
            public void onError(final String errorMessage) {
                showInfoView.hideProgress();

                showInfoView.showError(errorMessage, isOrigin);
            }
        });
    }

    @Override
    public void saveDistance(final String name, final String distance, final List<LatLng> latLngPositionList) {
        final Distance distanceAsDistance = new Distance();
        distanceAsDistance.setName(name);
        distanceAsDistance.setDistance(distance);
        distanceAsDistance.setDate(new Date());

        final List<Position> positionList = new ArrayList<>();
        for (final LatLng positionLatLng : latLngPositionList) {
            final Position position = new Position();
            position.setLatitude(positionLatLng.latitude);
            position.setLongitude(positionLatLng.longitude);
            positionList.add(position);
        }

        insertDistanceUseCase.execute(distanceAsDistance, positionList, new InsertDistanceUseCase.Callback() {
            @Override
            public void onInsert() {
                if (!TextUtils.isEmpty(name)) {
                    showInfoView.showSuccessfulSaveWithName(name);
                } else {
                    showInfoView.showSuccessfulSave();
                }
            }

            @Override
            public void onError() {
                showInfoView.showFailedSave();
            }
        });
    }
}
