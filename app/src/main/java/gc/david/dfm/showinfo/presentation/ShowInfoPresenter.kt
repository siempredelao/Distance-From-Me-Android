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

package gc.david.dfm.showinfo.presentation

import com.google.android.gms.maps.model.LatLng
import gc.david.dfm.ConnectionManager
import gc.david.dfm.address.domain.GetAddressNameByCoordinatesUseCase
import gc.david.dfm.address.domain.model.AddressCollection
import gc.david.dfm.database.Distance
import gc.david.dfm.database.Position
import gc.david.dfm.distance.domain.InsertDistanceUseCase
import java.util.*

/**
 * Created by david on 15.01.17.
 */
class ShowInfoPresenter(
        private val showInfoView: ShowInfo.View,
        private val getOriginAddressNameByCoordinatesUseCase: GetAddressNameByCoordinatesUseCase,
        private val getDestinationAddressNameByCoordinatesUseCase: GetAddressNameByCoordinatesUseCase,
        private val insertDistanceUseCase: InsertDistanceUseCase,
        private val connectionManager: ConnectionManager
) : ShowInfo.Presenter {

    override fun searchPositionByCoordinates(originLatLng: LatLng, destinationLatLng: LatLng) {
        if (!connectionManager.isOnline()) {
            showInfoView.showNoInternetError()
        } else {
            showInfoView.showProgress()

            getOriginAddress(getOriginAddressNameByCoordinatesUseCase, originLatLng, true)
            getOriginAddress(getDestinationAddressNameByCoordinatesUseCase, destinationLatLng, false)
        }
    }

    private fun getOriginAddress(getAddressUseCase: GetAddressNameByCoordinatesUseCase,
                                 latLng: LatLng,
                                 isOrigin: Boolean) {
        getAddressUseCase.execute(latLng, 1, object : GetAddressNameByCoordinatesUseCase.Callback {
            override fun onAddressLoaded(addressCollection: AddressCollection) {
                showInfoView.hideProgress()

                val addressList = addressCollection.addressList
                if (addressList.isEmpty()) {
                    showInfoView.showNoMatchesMessage(isOrigin)
                } else {
                    showInfoView.setAddress(addressList.first().formattedAddress, isOrigin)
                }
            }

            override fun onError(errorMessage: String) {
                showInfoView.hideProgress()

                showInfoView.showError(errorMessage, isOrigin)
            }
        })
    }

    override fun saveDistance(name: String, distance: String, latLngPositionList: List<LatLng>) {
        val distanceAsDistance = Distance(id = null, name = name, distance = distance, date = Date())

        val positionList = latLngPositionList.map {
            Position(id = null, latitude = it.latitude, longitude = it.longitude, distanceId = -1L) // FIXME
        }

        insertDistanceUseCase.execute(distanceAsDistance, positionList, object : InsertDistanceUseCase.Callback {
            override fun onInsert() {
                if (name.isNotEmpty()) {
                    showInfoView.showSuccessfulSaveWithName(name)
                } else {
                    showInfoView.showSuccessfulSave()
                }
            }

            override fun onError() {
                showInfoView.showFailedSave()
            }
        })
    }
}
