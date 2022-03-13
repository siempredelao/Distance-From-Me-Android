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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.android.gms.maps.model.LatLng
import gc.david.dfm.ConnectionManager
import gc.david.dfm.CoroutineDispatcherRule
import gc.david.dfm.ResourceProvider
import gc.david.dfm.address.domain.GetAddressCoordinatesByNameUseCase
import gc.david.dfm.address.domain.GetAddressNameByCoordinatesUseCase
import gc.david.dfm.address.domain.model.AddressCollection
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Created by david on 15.01.17.
 */
@ExperimentalCoroutinesApi
class AddressViewModelTest {

    private val getAddressCoordinatesByNameUseCase = mock<GetAddressCoordinatesByNameUseCase>()
    private val getAddressNameByCoordinatesUseCase = mock<GetAddressNameByCoordinatesUseCase>()
    private val connectionManager = mock<ConnectionManager>()
    private val resourceProvider = mock<ResourceProvider>()

    private val viewModel =
        AddressViewModel(
            getAddressCoordinatesByNameUseCase,
            getAddressNameByCoordinatesUseCase,
            connectionManager,
            resourceProvider
        )

    @get:Rule val instantTaskRule: TestRule = InstantTaskExecutorRule()
    @get:Rule val coroutinesDispatcherRule = CoroutineDispatcherRule()

    @Test
    fun `shows connection problems dialog when no connection available in position by name`() {
        whenever(connectionManager.isOnline()).thenReturn(false)
        whenever(resourceProvider.get(any())).thenReturn("random string")

        viewModel.onAddressSearch(LOCATION_NAME)

        assertTrue(viewModel.connectionIssueEvent.value != null)
    }

    @Test
    fun `executes coordinates by name use case when connection available`() = runTest {
        whenever(connectionManager.isOnline()).thenReturn(true)
        val locationName = LOCATION_NAME

        viewModel.onAddressSearch(locationName)

        verify(getAddressCoordinatesByNameUseCase)(locationName)
    }

    @Test
    fun `hides progress dialog when position by name use case succeeds`() = runTest {
        whenever(connectionManager.isOnline()).thenReturn(true)
        val addressCollection = EMPTY_ADDRESS_COLLECTION
        getAddressCoordinatesByNameSuccess(addressCollection)

        viewModel.onAddressSearch(LOCATION_NAME)

        assertEquals(false, viewModel.progressVisibility.value)
    }

    @Test
    fun `shows no matches when position by name use case return zero results`() = runTest {
        whenever(connectionManager.isOnline()).thenReturn(true)
        val addressCollection = EMPTY_ADDRESS_COLLECTION
        getAddressCoordinatesByNameSuccess(addressCollection)
        val message = "no matches"
        whenever(resourceProvider.get(any())).thenReturn(message)

        viewModel.onAddressSearch(LOCATION_NAME)

        assertEquals(message, viewModel.errorMessage.value!!.peekContent())
    }

    @Test
    fun `shows position by name when use case return one result`() = runTest {
        whenever(connectionManager.isOnline()).thenReturn(true)
        val address = ADDRESS
        val addressList = mutableListOf(address)
        val addressCollection = AddressCollection(addressList)
        getAddressCoordinatesByNameSuccess(addressCollection)

        viewModel.onAddressSearch(LOCATION_NAME)

        assertEquals(address, viewModel.addressFoundEvent.value!!.peekContent())
    }

    @Test
    fun `shows address selection dialog when position by name use case return several results`() =
        runTest {
            whenever(connectionManager.isOnline()).thenReturn(true)
            val address = ADDRESS
            val addressList = mutableListOf(address, address)
            val addressCollection = AddressCollection(addressList)
            getAddressCoordinatesByNameSuccess(addressCollection)

            viewModel.onAddressSearch(LOCATION_NAME)

            assertEquals(
                addressCollection.addressList,
                viewModel.multipleAddressesFoundEvent.value!!.peekContent()
            )
        }

    @Test
    fun `hides progress dialog when position by name use case fails`() = runTest {
        whenever(connectionManager.isOnline()).thenReturn(true)
        val errorMessage = ERROR_MESSAGE
        getAddressCoordinatesByNameFailure(errorMessage)

        viewModel.onAddressSearch(LOCATION_NAME)

        assertEquals(false, viewModel.progressVisibility.value)
    }

    @Test
    fun `shows error when position by name use case fails`() = runTest {
        whenever(connectionManager.isOnline()).thenReturn(true)
        val errorMessage = ERROR_MESSAGE
        getAddressCoordinatesByNameFailure(errorMessage)

        viewModel.onAddressSearch(LOCATION_NAME)

        assertEquals(errorMessage, viewModel.errorMessage.value!!.peekContent())
    }

    @Test
    fun `select address in dialog shows position by name with address`() {
        val address = ADDRESS

        viewModel.onAddressSelected(address)

        assertEquals(address, viewModel.addressFoundEvent.value!!.peekContent())
    }

    @Test
    fun `shows connection problems dialog when no connection available in position by coordinates`() {
        whenever(connectionManager.isOnline()).thenReturn(false)
        whenever(resourceProvider.get(any())).thenReturn("random string")

        viewModel.onAddressSearch(COORDINATES)

        assertTrue(viewModel.connectionIssueEvent.value != null)
    }

    @Test
    fun `executes name by coordinates use case when connection available`() = runTest {
        whenever(connectionManager.isOnline()).thenReturn(true)

        viewModel.onAddressSearch(COORDINATES)

        verify(getAddressNameByCoordinatesUseCase)(any())
    }

    @Test
    fun `hides progress dialog when use case succeeds in position by coordinates`() = runTest {
        whenever(connectionManager.isOnline()).thenReturn(true)
        val addressCollection = EMPTY_ADDRESS_COLLECTION
        getAddressNameByCoordinatesSuccess(addressCollection)

        viewModel.onAddressSearch(COORDINATES)

        assertEquals(false, viewModel.progressVisibility.value)
    }

    @Test
    fun `shows no matches when position by coordinates use case return zero results`() = runTest {
        whenever(connectionManager.isOnline()).thenReturn(true)
        val addressCollection = EMPTY_ADDRESS_COLLECTION
        getAddressNameByCoordinatesSuccess(addressCollection)
        val message = "no matches"
        whenever(resourceProvider.get(any())).thenReturn(message)

        viewModel.onAddressSearch(COORDINATES)

        assertEquals(message, viewModel.errorMessage.value!!.peekContent())
    }

    @Test
    fun `shows position by coordinates when use case return one result`() = runTest {
        whenever(connectionManager.isOnline()).thenReturn(true)
        val address = ADDRESS
        val addressList = mutableListOf(address)
        val addressCollection = AddressCollection(addressList)
        getAddressNameByCoordinatesSuccess(addressCollection)

        viewModel.onAddressSearch(COORDINATES)

        assertEquals(address, viewModel.addressFoundEvent.value!!.peekContent())
    }

    @Test
    fun `hides progress dialog when position by coordinates use case fails`() = runTest {
        whenever(connectionManager.isOnline()).thenReturn(true)
        val errorMessage = ERROR_MESSAGE
        getAddressNameByCoordinatesFailure(errorMessage)

        viewModel.onAddressSearch(COORDINATES)

        assertEquals(false, viewModel.progressVisibility.value)
    }

    @Test
    fun `shows error when position by coordinates use case fails`() = runTest {
        whenever(connectionManager.isOnline()).thenReturn(true)
        val errorMessage = ERROR_MESSAGE
        getAddressNameByCoordinatesFailure(errorMessage)

        viewModel.onAddressSearch(COORDINATES)

        assertEquals(errorMessage, viewModel.errorMessage.value!!.peekContent())
    }

    private suspend fun getAddressCoordinatesByNameSuccess(addressCollection: AddressCollection) {
        whenever(getAddressCoordinatesByNameUseCase(any()))
            .thenReturn(Result.success(addressCollection))
    }

    private suspend fun getAddressCoordinatesByNameFailure(errorMessage: String) {
        whenever(getAddressCoordinatesByNameUseCase(any()))
            .thenReturn(Result.failure(Exception(errorMessage)))
    }

    private suspend fun getAddressNameByCoordinatesSuccess(addressCollection: AddressCollection) {
        whenever(getAddressNameByCoordinatesUseCase(any()))
            .thenReturn(Result.success(addressCollection))
    }

    private suspend fun getAddressNameByCoordinatesFailure(errorMessage: String) {
        whenever(getAddressNameByCoordinatesUseCase(any()))
            .thenReturn(Result.failure(Exception(errorMessage)))
    }

    companion object {

        private const val LOCATION_NAME = "fake location name"
        private const val ERROR_MESSAGE = "fake errorMessage"

        private val COORDINATES = LatLng(0.0, 0.0)
        private val ADDRESS = gc.david.dfm.address.domain.model.Address(LOCATION_NAME, COORDINATES)
        private val EMPTY_ADDRESS_COLLECTION = AddressCollection(ArrayList())
    }
}