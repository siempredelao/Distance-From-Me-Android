/*
 * Copyright (c) 2026 David Aguiar Gonzalez
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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gc.david.dfm.ConnectionManager
import gc.david.dfm.address.domain.GetAddressNameByCoordinatesUseCase
import gc.david.dfm.address.domain.model.AddressCollection
import gc.david.dfm.address.presentation.mapper.GeocodingErrorMessageMapper
import gc.david.dfm.address.domain.model.Coordinates as AddressCoordinate
import gc.david.dfm.common.Coordinates
import gc.david.dfm.common.ResourceProvider
import gc.david.dfm.distance.domain.CoordinatesRepository
import gc.david.dfm.showinfo.R
import gc.david.dfm.showinfo.presentation.mapper.ShareInfoMessageMapper
import gc.david.dfm.showinfo.presentation.model.ShareIntentData
import gc.david.dfm.showinfo.presentation.model.ShowInfoUiState
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class ShowInfoViewModel(
    private val getAddressNameByCoordinatesUseCase: GetAddressNameByCoordinatesUseCase,
    private val resourceProvider: ResourceProvider,
    private val connectionManager: ConnectionManager,
    private val addressFormatter: AddressFormatter,
    private val coordinatesRepository: CoordinatesRepository,
    private val geocodingErrorMessageMapper: GeocodingErrorMessageMapper,
    private val shareInfoMessageMapper: ShareInfoMessageMapper,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShowInfoUiState())
    val uiState: StateFlow<ShowInfoUiState> = _uiState.asStateFlow()

    private lateinit var inputParams: InputParams

    fun onStart(distance: String?) {
        val positionsList = coordinatesRepository.observeDistance().value
        if (positionsList.isEmpty() || distance.isNullOrBlank()) {
            _uiState.update { it.copy(shouldFinish = true) }
            return
        }

        this.inputParams = InputParams(positionsList, distance)

        load(distance, positionsList)
    }

    fun onRefresh() {
        load(inputParams.distance, inputParams.positionsList)
    }

    private fun load(distance: String, positionsList: List<Coordinates>) {
        if (!connectionManager.isOnline()) {
            _uiState.update {
                it.copy(userMessage = resourceProvider.get(R.string.toast_network_problems))
            }
            return
        }

        val distanceMessage = resourceProvider.get(R.string.info_distance_title, distance)
        _uiState.update { it.copy(isLoading = true, distanceMessage = distanceMessage) }

        viewModelScope.launch {
            val originPosition = positionsList.first()
            val destinationPosition = positionsList.last()
            val originAddressDeferred =
                async { getAddressNameByCoordinatesUseCase(originPosition.toAddressCoordinate()) }
            val destinationAddressDeferred =
                async { getAddressNameByCoordinatesUseCase(destinationPosition.toAddressCoordinate()) }
            val (originAddressResult, destinationAddressResult) =
                originAddressDeferred.await() to destinationAddressDeferred.await()

            _uiState.update { current ->
                current.copy(
                    isLoading = false,
                    originAddress = resolveAddress(originAddressResult, originPosition),
                    destinationAddress = resolveAddress(destinationAddressResult, destinationPosition),
                )
            }
        }
    }

    private fun resolveAddress(result: Result<AddressCollection>, coordinates: Coordinates) =
        result.fold({ collection ->
            val addressList = collection.addressList
            if (addressList.isEmpty()) {
                resourceProvider.get(R.string.error_no_address_found_message)
            } else {
                addressFormatter.format(
                    addressList.first().formattedAddress,
                    coordinates.latitude,
                    coordinates.longitude,
                )
            }
        }, { error ->
            Timber.tag(TAG).e(error)
            geocodingErrorMessageMapper.map(error)
        })

    fun onShare() {
        val state = _uiState.value
        val subject = shareInfoMessageMapper.getSubject()
        val message =
            shareInfoMessageMapper.mapMessage(
                originAddress = state.originAddress,
                destinationAddress = state.destinationAddress,
                distance = inputParams.distance,
            )

        _uiState.update {
            it.copy(
                shareIntentData = ShareIntentData(
                    title = resourceProvider.get(R.string.action_bar_item_social_share_title),
                    subject = subject,
                    message = message,
                )
            )
        }
    }

    fun onShareDialogShown() {
        _uiState.update { it.copy(shareIntentData = null) }
    }

    fun onSave() {
        _uiState.update { it.copy(showSaveDialog = true) }
    }

    fun onSaveDialogDismissed() {
        _uiState.update { it.copy(showSaveDialog = false) }
    }

    fun onUserMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }

    fun getSaveDistanceData(): SaveDistanceData =
        SaveDistanceData(inputParams.positionsList, inputParams.distance)

    companion object {

        private const val TAG = "ShowInfoViewModel"
    }

    data class InputParams(val positionsList: List<Coordinates>, val distance: String)

    data class SaveDistanceData(val positionsList: List<Coordinates>, val distance: String)
}

private fun Coordinates.toAddressCoordinate() = AddressCoordinate(latitude, longitude)
