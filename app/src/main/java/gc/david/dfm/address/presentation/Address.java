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

import java.util.List;

/**
 * Created by david on 13.01.17.
 */
public interface Address {

    interface Presenter {
        void searchPositionByName(String locationName);

        void selectAddressInDialog(gc.david.dfm.address.domain.model.Address address);

        void searchPositionByCoordinates(LatLng coordinates);
    }

    interface View {
        void setPresenter(Presenter presenter);

        void showConnectionProblemsDialog();

        void showProgressDialog();

        void hideProgressDialog();

        void showCallError(String errorMessage); // TODO: 13.01.17 find a better name

        void showNoMatchesMessage();

        void showAddressSelectionDialog(List<gc.david.dfm.address.domain.model.Address> addressList);

        void showPositionByName(gc.david.dfm.address.domain.model.Address address);

        void showPositionByCoordinates(gc.david.dfm.address.domain.model.Address address);
    }

}
