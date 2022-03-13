/*
 * Copyright (c) 2022 David Aguiar Gonzalez
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
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import gc.david.dfm.ConnectionManager
import gc.david.dfm.Event
import gc.david.dfm.R
import gc.david.dfm.ResourceProvider
import gc.david.dfm.address.domain.GetAddressCoordinatesByNameUseCase
import gc.david.dfm.address.domain.GetAddressNameByCoordinatesUseCase
import gc.david.dfm.address.domain.model.Address
import kotlinx.coroutines.launch

class AddressViewModel(
    private val getAddressCoordinatesByNameUseCase: GetAddressCoordinatesByNameUseCase,
    private val getAddressNameByCoordinatesUseCase: GetAddressNameByCoordinatesUseCase,
    private val connectionManager: ConnectionManager,
    private val resourceProvider: ResourceProvider
) : ViewModel() {

    val progressVisibility = MutableLiveData<Boolean>()
    val connectionIssueEvent = MutableLiveData<Event<ConnectionIssuesData>>()
    val errorMessage = MutableLiveData<Event<String>>()
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

        viewModelScope.launch {
            val result = getAddressCoordinatesByNameUseCase(locationName)
            progressVisibility.postValue(false)

            result.fold({
                when {
                    it.addressList.isEmpty() ->
                        errorMessage.postValue(Event(resourceProvider.get(R.string.toast_no_results)))
                    it.addressList.size == 1 ->
                        addressFoundEvent.postValue(Event(it.addressList.first()))
                    else ->
                        multipleAddressesFoundEvent.postValue(Event(it.addressList))
                }
            }, {
                errorMessage.postValue(Event(it.message.orEmpty()))
            })
        }
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

        viewModelScope.launch {
            val result = getAddressNameByCoordinatesUseCase(coordinates)
            progressVisibility.postValue(false)

            result.fold({
                when {
                    it.addressList.isEmpty() ->
                        errorMessage.postValue(Event(resourceProvider.get(R.string.toast_no_results)))
                    else ->
                        addressFoundEvent.postValue(Event(it.addressList.first()))
                }
            }, {
                errorMessage.postValue(Event(it.message.orEmpty()))
            })
        }
    }
}

data class ConnectionIssuesData(
    val title: String,
    val description: String,
    val positiveMessage: String,
    val negativeMessage: String
)