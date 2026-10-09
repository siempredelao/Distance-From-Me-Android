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

import gc.david.dfm.ConnectionManager
import gc.david.dfm.address.R
import gc.david.dfm.address.domain.GeocodingError
import gc.david.dfm.address.domain.GeocodingResult
import gc.david.dfm.address.domain.GetAddressCoordinatesByNameUseCase
import gc.david.dfm.address.domain.GetAddressNameByCoordinatesUseCase
import gc.david.dfm.address.domain.model.Address
import gc.david.dfm.address.domain.model.Coordinates
import gc.david.dfm.address.presentation.mapper.GeocodingErrorMessageMapper
import gc.david.dfm.common.presentation.ResourceProvider
import gc.david.dfm.testsupport.CoroutineExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.IOException

/**
 * Created by david on 15.01.17.
 */
class AddressViewModelTest {

    @JvmField
    @RegisterExtension
    val coroutineExtension = CoroutineExtension()

    private val getAddressCoordinatesByNameUseCase = mock<GetAddressCoordinatesByNameUseCase>()
    private val getAddressNameByCoordinatesUseCase = mock<GetAddressNameByCoordinatesUseCase>()
    private val connectionManager = mock<ConnectionManager>()
    private val resourceProvider = mock<ResourceProvider>()
    private val geocodingErrorMessageMapper = GeocodingErrorMessageMapper(resourceProvider)

    private val viewModel =
        AddressViewModel(
            getAddressCoordinatesByNameUseCase,
            getAddressNameByCoordinatesUseCase,
            connectionManager,
            resourceProvider,
            geocodingErrorMessageMapper
        )

    @Test
    fun `shows connection problems dialog when no connection available in position by name`() {
        whenever(connectionManager.isOnline()).thenReturn(false)

        viewModel.onAddressSearch(LOCATION_NAME)

        assertTrue(viewModel.uiState.value.showConnectionIssue)
    }

    @Test
    fun `executes coordinates by name use case when connection available`() = runTest {
        whenever(connectionManager.isOnline()).thenReturn(true)
        givenCoordinatesByNameReturns(GeocodingResult.NoResults)

        viewModel.onAddressSearch(LOCATION_NAME)

        verify(getAddressCoordinatesByNameUseCase)(LOCATION_NAME)
    }

    @Test
    fun `shows no matches when position by name use case returns no results`() = runTest {
        whenever(connectionManager.isOnline()).thenReturn(true)
        givenCoordinatesByNameReturns(GeocodingResult.NoResults)
        whenever(resourceProvider.get(R.string.toast_no_results)).thenReturn(NO_RESULTS_MESSAGE)

        viewModel.onAddressSearch(LOCATION_NAME)

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(NO_RESULTS_MESSAGE, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `shows position by name when use case returns one result`() = runTest {
        whenever(connectionManager.isOnline()).thenReturn(true)
        givenCoordinatesByNameReturns(GeocodingResult.Found(listOf(ADDRESS)))

        viewModel.onAddressSearch(LOCATION_NAME)

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(ADDRESS, viewModel.uiState.value.addressFound)
    }

    @Test
    fun `shows address selection dialog when position by name use case returns several results`() =
        runTest {
            whenever(connectionManager.isOnline()).thenReturn(true)
            val addresses = listOf(ADDRESS, ADDRESS)
            givenCoordinatesByNameReturns(GeocodingResult.Found(addresses))

            viewModel.onAddressSearch(LOCATION_NAME)

            assertFalse(viewModel.uiState.value.isLoading)
            assertEquals(addresses, viewModel.uiState.value.multipleAddressesFound)
        }

    @Test
    fun `shows rejection message when position by name request is rejected`() = runTest {
        whenever(connectionManager.isOnline()).thenReturn(true)
        givenCoordinatesByNameReturns(GeocodingResult.Rejected(GeocodingError.OVER_QUERY_LIMIT))
        whenever(resourceProvider.get(R.string.geocoding_error_rate_limited)).thenReturn(REJECTED_MESSAGE)

        viewModel.onAddressSearch(LOCATION_NAME)

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(REJECTED_MESSAGE, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `shows generic error when position by name use case throws`() = runTest {
        whenever(connectionManager.isOnline()).thenReturn(true)
        whenever(getAddressCoordinatesByNameUseCase(any())).thenAnswer { throw IOException("Network error") }
        whenever(resourceProvider.get(R.string.geocoding_error_generic)).thenReturn(GENERIC_MESSAGE)

        viewModel.onAddressSearch(LOCATION_NAME)

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(GENERIC_MESSAGE, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `select address in dialog shows position by name with address`() {
        viewModel.onAddressSelected(ADDRESS)

        assertEquals(ADDRESS, viewModel.uiState.value.addressFound)
    }

    @Test
    fun `shows connection problems dialog when no connection available in position by coordinates`() {
        whenever(connectionManager.isOnline()).thenReturn(false)

        viewModel.onAddressSearch(COORDINATES)

        assertTrue(viewModel.uiState.value.showConnectionIssue)
    }

    @Test
    fun `executes name by coordinates use case when connection available`() = runTest {
        whenever(connectionManager.isOnline()).thenReturn(true)
        givenNameByCoordinatesReturns(GeocodingResult.NoResults)

        viewModel.onAddressSearch(COORDINATES)

        verify(getAddressNameByCoordinatesUseCase)(COORDINATES)
    }

    @Test
    fun `shows no matches when position by coordinates use case returns no results`() = runTest {
        whenever(connectionManager.isOnline()).thenReturn(true)
        givenNameByCoordinatesReturns(GeocodingResult.NoResults)
        whenever(resourceProvider.get(R.string.toast_no_results)).thenReturn(NO_RESULTS_MESSAGE)

        viewModel.onAddressSearch(COORDINATES)

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(NO_RESULTS_MESSAGE, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `shows position by coordinates when use case returns one result`() = runTest {
        whenever(connectionManager.isOnline()).thenReturn(true)
        givenNameByCoordinatesReturns(GeocodingResult.Found(listOf(ADDRESS)))

        viewModel.onAddressSearch(COORDINATES)

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(ADDRESS, viewModel.uiState.value.addressFound)
    }

    @Test
    fun `shows rejection message when position by coordinates request is rejected`() = runTest {
        whenever(connectionManager.isOnline()).thenReturn(true)
        givenNameByCoordinatesReturns(GeocodingResult.Rejected(GeocodingError.REQUEST_DENIED))
        whenever(resourceProvider.get(R.string.geocoding_error_request_denied)).thenReturn(REJECTED_MESSAGE)

        viewModel.onAddressSearch(COORDINATES)

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(REJECTED_MESSAGE, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `shows generic error when position by coordinates use case throws`() = runTest {
        whenever(connectionManager.isOnline()).thenReturn(true)
        whenever(getAddressNameByCoordinatesUseCase(any())).thenAnswer { throw IOException("Network error") }
        whenever(resourceProvider.get(R.string.geocoding_error_generic)).thenReturn(GENERIC_MESSAGE)

        viewModel.onAddressSearch(COORDINATES)

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(GENERIC_MESSAGE, viewModel.uiState.value.errorMessage)
    }

    private suspend fun givenCoordinatesByNameReturns(result: GeocodingResult) {
        whenever(getAddressCoordinatesByNameUseCase(any())).thenReturn(result)
    }

    private suspend fun givenNameByCoordinatesReturns(result: GeocodingResult) {
        whenever(getAddressNameByCoordinatesUseCase(any())).thenReturn(result)
    }

    companion object {

        private const val LOCATION_NAME = "fake location name"
        private const val NO_RESULTS_MESSAGE = "no matches"
        private const val REJECTED_MESSAGE = "rejected"
        private const val GENERIC_MESSAGE = "generic"

        private val COORDINATES = Coordinates(0.0, 0.0)
        private val ADDRESS = Address(LOCATION_NAME, COORDINATES)
    }
}
