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

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import gc.david.dfm.ConnectionManager
import gc.david.dfm.Event
import gc.david.dfm.R
import gc.david.dfm.ResourceProvider
import gc.david.dfm.address.domain.GetAddressCoordinatesByNameInteractor
import gc.david.dfm.address.domain.GetAddressNameByCoordinatesInteractor
import gc.david.dfm.address.domain.model.Address
import gc.david.dfm.address.domain.model.AddressCollection

class AddressViewModel(
        private val getAddressCoordinatesByNameUseCase: GetAddressCoordinatesByNameInteractor,
        private val getAddressNameByCoordinatesUseCase: GetAddressNameByCoordinatesInteractor,
        private val connectionManager: ConnectionManager,
        private val resourceProvider: ResourceProvider
) : ViewModel() {

    val progressVisibility = MutableLiveData<Boolean>()
    val connectionIssueEvent = MutableLiveData<Event<ConnectionIssuesData>>()
    val errorMessage = MutableLiveData<String>() // TODO transform this to Event
    val addressFoundEvent = MutableLiveData<Event<Address>>()
    val multipleAddressesFoundEvent = MutableLiveData<Event<List<Address>>>()

    fun onAddressSearch(query: String) {
        if (!connectionManager.isOnline()) {
            val connectionIssuesData = getConnectionIssuesData()
            connectionIssueEvent.value = Event(connectionIssuesData)
        } else {
            onSearchPositionByNameWithConnectionAvailable(query)
        }
    }

    private fun getConnectionIssuesData() = ConnectionIssuesData(
            resourceProvider.get(R.string.dialog_connection_problems_title),
            resourceProvider.get(R.string.dialog_connection_problems_message),
            resourceProvider.get(R.string.dialog_connection_problems_positive_button),
            resourceProvider.get(R.string.dialog_connection_problems_negative_button)
    )

    private fun onSearchPositionByNameWithConnectionAvailable(locationName: String) {
        progressVisibility.value = true

        getAddressCoordinatesByNameUseCase.execute(locationName, MAX_BY_NAME, object : GetAddressCoordinatesByNameInteractor.Callback {
            override fun onAddressLoaded(addressCollection: AddressCollection) {
                progressVisibility.value = false

                when {
                    addressCollection.addressList.isEmpty() ->
                        errorMessage.value = resourceProvider.get(R.string.toast_no_results)

                    addressCollection.addressList.size == 1 ->
                        addressFoundEvent.value = Event(addressCollection.addressList.first())

                    else ->
                        multipleAddressesFoundEvent.value = Event(addressCollection.addressList)
                }
            }

            override fun onError(message: String) {
                progressVisibility.value = false
                errorMessage.value = message
            }
        })
    }

    fun onAddressSelected(address: Address) {
        addressFoundEvent.value = Event(address)
    }

    fun onAddressSearch(coordinates: LatLng) { // FIXME consider using a Coordinates(x, y) custom data model
        if (!connectionManager.isOnline()) {
            val connectionIssuesData = getConnectionIssuesData()
            connectionIssueEvent.value = Event(connectionIssuesData)
        } else {
            onSearchPositionByCoordinatesWithConnectionAvailable(coordinates)
        }
    }

    private fun onSearchPositionByCoordinatesWithConnectionAvailable(coordinates: LatLng) {
        progressVisibility.value = true

        getAddressNameByCoordinatesUseCase.execute(coordinates, MAX_BY_COORD, object : GetAddressNameByCoordinatesInteractor.Callback {
            override fun onAddressLoaded(addressCollection: AddressCollection) {
                progressVisibility.value = false

                when {
                    addressCollection.addressList.isEmpty() ->
                        errorMessage.value = resourceProvider.get(R.string.toast_no_results)

                    else ->
                        addressFoundEvent.value = Event(addressCollection.addressList.first())
                }
            }

            override fun onError(message: String) {
                progressVisibility.value = false
                errorMessage.value = message
            }
        })
    }

    companion object {

        private const val MAX_BY_NAME = 5
        private const val MAX_BY_COORD = 1
    }
}

data class ConnectionIssuesData(val title: String, val description: String, val positiveMessage: String, val negativeMessage: String)