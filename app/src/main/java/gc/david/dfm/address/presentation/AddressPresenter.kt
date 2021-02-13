/*
 * Copyright (c) 2021 David Aguiar Gonzalez
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

import gc.david.dfm.ConnectionManager
import gc.david.dfm.address.domain.GetAddressCoordinatesByNameUseCase
import gc.david.dfm.address.domain.GetAddressNameByCoordinatesUseCase
import gc.david.dfm.address.domain.model.AddressCollection

/**
 * Created by david on 13.01.17.
 */
class AddressPresenter(
        private val addressView: Address.View,
        private val getAddressCoordinatesByNameUseCase: GetAddressCoordinatesByNameUseCase,
        private val getAddressNameByCoordinatesUseCase: GetAddressNameByCoordinatesUseCase,
        private val connectionManager: ConnectionManager
) : Address.Presenter {

    init {
        this.addressView.setPresenter(this)
    }

    override fun searchPositionByName(locationName: String) {
        if (!connectionManager.isOnline()) {
            addressView.showConnectionProblemsDialog()
        } else {
            onSearchPositionByNameWithConnectionAvailable(locationName)
        }
    }

    private fun onSearchPositionByNameWithConnectionAvailable(locationName: String) {
        addressView.showProgressDialog()

        getAddressCoordinatesByNameUseCase.execute(locationName, MAX_BY_NAME, object : GetAddressCoordinatesByNameUseCase.Callback {
            override fun onAddressLoaded(addressCollection: AddressCollection) {
                addressView.hideProgressDialog()

                when {
                    addressCollection.addressList.isEmpty() -> addressView.showNoMatchesMessage()
                    addressCollection.addressList.size == 1 -> addressView.showPositionByName(addressCollection.addressList.first())
                    else -> addressView.showAddressSelectionDialog(addressCollection.addressList)
                }
            }

            override fun onError(errorMessage: String) {
                addressView.hideProgressDialog()

                addressView.showCallError(errorMessage)
            }
        })
    }

    override fun selectAddressInDialog(address: gc.david.dfm.address.domain.model.Address) {
        addressView.showPositionByName(address)
    }

    override fun searchPositionByCoordinates(coordinates: LatLng) {
        if (!connectionManager.isOnline()) {
            addressView.showConnectionProblemsDialog()
        } else {
            onSearchPositionByCoordinatesWithConnectionAvailable(coordinates)
        }
    }

    private fun onSearchPositionByCoordinatesWithConnectionAvailable(coordinates: LatLng) {
        addressView.showProgressDialog()

        getAddressNameByCoordinatesUseCase.execute(coordinates, MAX_BY_COORD, object : GetAddressNameByCoordinatesUseCase.Callback {
            override fun onAddressLoaded(addressCollection: AddressCollection) {
                addressView.hideProgressDialog()

                if (addressCollection.addressList.isEmpty()) {
                    addressView.showNoMatchesMessage()
                } else {
                    addressView.showPositionByCoordinates(addressCollection.addressList.first())
                }
            }

            override fun onError(errorMessage: String) {
                addressView.hideProgressDialog()

                addressView.showCallError(errorMessage)
            }
        })
    }

    companion object {

        private const val MAX_BY_NAME = 5
        private const val MAX_BY_COORD = 1
    }
}
