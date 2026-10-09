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

package gc.david.dfm.address.presentation

import androidx.lifecycle.ViewModel
import gc.david.dfm.ConnectionManager
import gc.david.dfm.address.R
import gc.david.dfm.address.domain.GeocodingResult
import gc.david.dfm.address.domain.GetAddressCoordinatesByNameUseCase
import gc.david.dfm.address.domain.GetAddressNameByCoordinatesUseCase
import gc.david.dfm.address.domain.model.Address
import gc.david.dfm.address.domain.model.Coordinates
import gc.david.dfm.address.presentation.mapper.GeocodingErrorMessageMapper
import gc.david.dfm.address.presentation.model.AddressUiState
import gc.david.dfm.common.launchCatching
import gc.david.dfm.common.logUnexpectedFailure
import gc.david.dfm.common.presentation.ResourceProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AddressViewModel(
    private val getAddressCoordinatesByNameUseCase: GetAddressCoordinatesByNameUseCase,
    private val getAddressNameByCoordinatesUseCase: GetAddressNameByCoordinatesUseCase,
    private val connectionManager: ConnectionManager,
    private val resourceProvider: ResourceProvider,
    private val geocodingErrorMessageMapper: GeocodingErrorMessageMapper,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddressUiState())
    val uiState: StateFlow<AddressUiState> = _uiState.asStateFlow()

    fun onAddressSearch(query: String) {
        if (!connectionManager.isOnline()) {
            _uiState.update { it.copy(showConnectionIssue = true) }
        } else {
            onSearchPositionByNameWithConnectionAvailable(query)
        }
    }

    private fun onSearchPositionByNameWithConnectionAvailable(locationName: String) {
        _uiState.update { it.copy(isLoading = true) }

        launchCatching(onError = ::onUnexpectedFailure) {
            val result = getAddressCoordinatesByNameUseCase(locationName)
            _uiState.update { current ->
                when (result) {
                    is GeocodingResult.Found if result.addresses.size == 1 ->
                        current.copy(
                            isLoading = false,
                            addressFound = result.addresses.first(),
                        )
                    is GeocodingResult.Found ->
                        current.copy(
                            isLoading = false,
                            multipleAddressesFound = result.addresses,
                        )
                    GeocodingResult.NoResults ->
                        current.copy(
                            isLoading = false,
                            errorMessage = resourceProvider.get(R.string.toast_no_results),
                        )
                    is GeocodingResult.Rejected ->
                        current.copy(
                            isLoading = false,
                            errorMessage = geocodingErrorMessageMapper.map(result.error),
                        )
                }
            }
        }
    }

    fun onAddressSelected(address: Address) {
        _uiState.update { it.copy(addressFound = address) }
    }

    fun onAddressSearch(coordinates: Coordinates) {
        if (!connectionManager.isOnline()) {
            _uiState.update { it.copy(showConnectionIssue = true) }
        } else {
            onSearchPositionByCoordinatesWithConnectionAvailable(coordinates)
        }
    }

    private fun onSearchPositionByCoordinatesWithConnectionAvailable(coordinates: Coordinates) {
        _uiState.update { it.copy(isLoading = true) }

        launchCatching(onError = ::onUnexpectedFailure) {
            val result = getAddressNameByCoordinatesUseCase(coordinates)
            _uiState.update { current ->
                when (result) {
                    is GeocodingResult.Found ->
                        current.copy(
                            isLoading = false,
                            addressFound = result.addresses.first(),
                        )
                    GeocodingResult.NoResults ->
                        current.copy(
                            isLoading = false,
                            errorMessage = resourceProvider.get(R.string.toast_no_results),
                        )
                    is GeocodingResult.Rejected ->
                        current.copy(
                            isLoading = false,
                            errorMessage = geocodingErrorMessageMapper.map(result.error),
                        )
                }
            }
        }
    }

    private fun onUnexpectedFailure(error: Throwable) {
        logUnexpectedFailure(TAG, error)
        _uiState.update {
            it.copy(
                isLoading = false,
                errorMessage = geocodingErrorMessageMapper.unexpectedFailureMessage(),
            )
        }
    }

    fun onConnectionIssueShown() {
        _uiState.update { it.copy(showConnectionIssue = false) }
    }

    fun onErrorMessageShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun onAddressHandled() {
        _uiState.update { it.copy(addressFound = null) }
    }

    fun onMultipleAddressesHandled() {
        _uiState.update { it.copy(multipleAddressesFound = null) }
    }

    private companion object {

        const val TAG = "AddressViewModel"
    }
}
