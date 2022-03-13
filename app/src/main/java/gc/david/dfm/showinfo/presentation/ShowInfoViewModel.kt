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

package gc.david.dfm.showinfo.presentation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import gc.david.dfm.ConnectionManager
import gc.david.dfm.Event
import gc.david.dfm.R
import gc.david.dfm.ResourceProvider
import gc.david.dfm.address.domain.GetAddressNameByCoordinatesUseCase
import gc.david.dfm.address.domain.model.AddressCollection
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import timber.log.Timber

class ShowInfoViewModel(
    private val getAddressNameByCoordinatesUseCase: GetAddressNameByCoordinatesUseCase,
    private val resourceProvider: ResourceProvider,
    private val connectionManager: ConnectionManager
) : ViewModel() {

    val originAddress = MutableLiveData<String>()
    val destinationAddress = MutableLiveData<String>()
    val distanceMessage = MutableLiveData<String>()
    val progressVisibility = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<Event<String>>()
    val showShareDialogEvent = MutableLiveData<Event<ShareDialogData>>()
    val saveDistanceEvent = MutableLiveData<Event<SaveDistanceData>>()

    private lateinit var inputParams: InputParams

    fun onStart(positionsList: List<LatLng>, distance: String) {
        this.inputParams = InputParams(positionsList, distance)

        if (!connectionManager.isOnline()) {
            errorMessage.value = Event(resourceProvider.get(R.string.toast_network_problems))
            return
        }

        progressVisibility.value = true
        viewModelScope.launch {
            val originPosition = positionsList.first()
            val destinationPosition = positionsList.last()
            val originAddressDeferred =
                async { getAddressNameByCoordinatesUseCase(originPosition) }
            val destinationAddressDeferred =
                async { getAddressNameByCoordinatesUseCase(destinationPosition) }
            val (originAddressResult, destinationAddressResult) =
                originAddressDeferred.await() to destinationAddressDeferred.await()
            progressVisibility.postValue(false)

            getAddressByLatLng(originAddressResult, positionsList.first(), true)
            getAddressByLatLng(destinationAddressResult, positionsList.last(), false)
        }

        val get = resourceProvider.get(R.string.info_distance_title)
        distanceMessage.value = String.format(get, distance)
    }

    private fun getAddressByLatLng(
        result: Result<AddressCollection>,
        latLng: LatLng,
        isOrigin: Boolean
    ) {
        result.fold({
            val addressList = it.addressList
            if (addressList.isEmpty()) {
                showNoMatchesMessage(isOrigin)
            } else {
                setAddress(addressList.first().formattedAddress, latLng, isOrigin)
            }
        }, {
            showError(it.message.orEmpty(), isOrigin)
        })
    }

    private fun showNoMatchesMessage(isOrigin: Boolean) {
        if (isOrigin) {
            originAddress.value = resourceProvider.get(R.string.error_no_address_found_message)
        } else {
            destinationAddress.value = resourceProvider.get(R.string.error_no_address_found_message)
        }
    }

    private fun setAddress(address: String, latLng: LatLng, isOrigin: Boolean) {
        if (isOrigin) {
            originAddress.value = formatAddress(address, latLng.latitude, latLng.longitude)
        } else {
            destinationAddress.value = formatAddress(address, latLng.latitude, latLng.longitude)
        }
    }

    // TODO: move to mapper class
    private fun formatAddress(address: String?, latitude: Double, longitude: Double): String {
        return "$address\n\n($latitude,$longitude)"
    }

    private fun showError(errorMessage: String, isOrigin: Boolean) {
        if (isOrigin) {
            originAddress.value = resourceProvider.get(R.string.toast_no_location_found)
        } else {
            destinationAddress.value = resourceProvider.get(R.string.toast_no_location_found)
        }
        Timber.tag(TAG).d(Exception(errorMessage))
    }

    fun onShare() {
        val title = resourceProvider.get(R.string.action_bar_item_social_share_title)
        val subject = "Distance From Me (http://goo.gl/0IBHFN)"
        val message = """Distance From Me (http://goo.gl/0IBHFN)
${resourceProvider.get(R.string.share_distance_from_message)}
${originAddress.value}

${resourceProvider.get(R.string.share_distance_to_message)}
${destinationAddress.value}

${resourceProvider.get(R.string.share_distance_there_are_message)}
${inputParams.distance}"""

        showShareDialogEvent.value = Event(ShareDialogData(title, subject, message))
    }

    fun onSave() {
        saveDistanceEvent.value =
            Event(SaveDistanceData(inputParams.positionsList, inputParams.distance))
    }

    companion object {

        private const val TAG = "ShowInfoViewModel"
    }

    data class InputParams(val positionsList: List<LatLng>, val distance: String)
}

data class ShareDialogData(val title: String, val subject: String, val description: String)
data class SaveDistanceData(val positionsList: List<LatLng>, val distance: String)


