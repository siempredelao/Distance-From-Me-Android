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
import androidx.lifecycle.viewModelScope
import gc.david.dfm.ConnectionManager
import gc.david.dfm.address.R
import gc.david.dfm.address.domain.GetAddressCoordinatesByNameUseCase
import gc.david.dfm.address.domain.GetAddressNameByCoordinatesUseCase
import gc.david.dfm.address.domain.model.Address
import gc.david.dfm.address.domain.model.Coordinates
import gc.david.dfm.address.presentation.mapper.GeocodingErrorMessageMapper
import gc.david.dfm.address.presentation.model.AddressUiState
import gc.david.dfm.common.presentation.ResourceProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

        viewModelScope.launch {
            val result = getAddressCoordinatesByNameUseCase(locationName)
            _uiState.update { current ->
                result.fold({ collection ->
                    when {
                        collection.addressList.isEmpty() ->
                            current.copy(
                                isLoading = false,
                                errorMessage = resourceProvider.get(R.string.toast_no_results),
                            )
                        collection.addressList.size == 1 ->
                            current.copy(
                                isLoading = false,
                                addressFound = collection.addressList.first(),
                            )
                        else ->
                            current.copy(
                                isLoading = false,
                                multipleAddressesFound = collection.addressList,
                            )
                    }
                }, { error ->
                    current.copy(
                        isLoading = false,
                        errorMessage = geocodingErrorMessageMapper.map(error),
                    )
                })
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

        viewModelScope.launch {
            val result = getAddressNameByCoordinatesUseCase(coordinates)
            _uiState.update { current ->
                result.fold({ collection ->
                    when {
                        collection.addressList.isEmpty() ->
                            current.copy(
                                isLoading = false,
                                errorMessage = resourceProvider.get(R.string.toast_no_results),
                            )
                        else ->
                            current.copy(
                                isLoading = false,
                                addressFound = collection.addressList.first(),
                            )
                    }
                }, { error ->
                    current.copy(
                        isLoading = false,
                        errorMessage = geocodingErrorMessageMapper.map(error),
                    )
                })
            }
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
}
