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

package gc.david.dfm.address.presentation

import com.google.android.gms.maps.model.LatLng

/**
 * Created by david on 13.01.17.
 */
interface Address {

    interface Presenter {
        fun searchPositionByName(locationName: String)

        fun selectAddressInDialog(address: gc.david.dfm.address.domain.model.Address)

        fun searchPositionByCoordinates(coordinates: LatLng)
    }

    interface View {
        fun setPresenter(presenter: Presenter)

        fun showConnectionProblemsDialog()

        fun showProgressDialog()

        fun hideProgressDialog()

        fun showCallError(errorMessage: String)  // TODO: 13.01.17 find a better name

        fun showNoMatchesMessage()

        fun showAddressSelectionDialog(addressList: List<gc.david.dfm.address.domain.model.Address>)

        fun showPositionByName(address: gc.david.dfm.address.domain.model.Address)

        fun showPositionByCoordinates(address: gc.david.dfm.address.domain.model.Address)
    }

}
