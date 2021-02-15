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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.android.gms.maps.model.LatLng
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.whenever
import gc.david.dfm.ConnectionManager
import gc.david.dfm.ResourceProvider
import gc.david.dfm.address.domain.GetAddressCoordinatesByNameInteractor
import gc.david.dfm.address.domain.GetAddressNameByCoordinatesInteractor
import gc.david.dfm.address.domain.model.AddressCollection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mock
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

/**
 * Created by david on 15.01.17.
 */
@RunWith(MockitoJUnitRunner::class)
class AddressViewModelTest {

    @Mock
    lateinit var getAddressCoordinatesByNameUseCase: GetAddressCoordinatesByNameInteractor
    @Mock
    lateinit var getAddressNameByCoordinatesUseCase: GetAddressNameByCoordinatesInteractor
    @Mock
    lateinit var connectionManager: ConnectionManager
    @Mock
    lateinit var resourceProvider: ResourceProvider

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var viewModel: AddressViewModel

    @Before
    fun setUp() {
        viewModel = AddressViewModel(
                getAddressCoordinatesByNameUseCase,
                getAddressNameByCoordinatesUseCase,
                connectionManager,
                resourceProvider)
    }

    @Test
    fun `shows connection problems dialog when no connection available in position by name`() {
        whenever(connectionManager.isOnline()).thenReturn(false)
        val locationName = LOCATION_NAME
        whenever(resourceProvider.get(any())).thenReturn("random string")

        viewModel.onAddressSearch(locationName)

        assertTrue(viewModel.connectionIssueEvent.value != null)
    }

    @Test
    fun `shows progress dialog when connection available in position by name`() {
        whenever(connectionManager.isOnline()).thenReturn(true)
        val locationName = LOCATION_NAME

        viewModel.onAddressSearch(locationName)

        assertEquals(true, viewModel.progressVisibility.value)
    }

    @Test
    fun `executes coordinates by name use case when connection available`() {
        whenever(connectionManager.isOnline()).thenReturn(true)
        val locationName = LOCATION_NAME

        viewModel.onAddressSearch(locationName)

        verify(getAddressCoordinatesByNameUseCase).execute(any(), anyInt(), any())
    }

    @Test
    fun `hides progress dialog when position by name use case succeeds`() {
        whenever(connectionManager.isOnline()).thenReturn(true)
        val locationName = LOCATION_NAME
        val addressCollection = EMPTY_ADDRESS_COLLECTION
        executeOnAddressLoadedAfterCoordinatesByNameUseCaseCallback(locationName, addressCollection)

        viewModel.onAddressSearch(locationName)

        assertEquals(false, viewModel.progressVisibility.value)
    }

    @Test
    fun `shows no matches when position by name use case return zero results`() {
        whenever(connectionManager.isOnline()).thenReturn(true)
        val locationName = LOCATION_NAME
        val addressCollection = EMPTY_ADDRESS_COLLECTION
        executeOnAddressLoadedAfterCoordinatesByNameUseCaseCallback(locationName, addressCollection)
        val message = "no matches"
        whenever(resourceProvider.get(any())).thenReturn(message)

        viewModel.onAddressSearch(locationName)

        assertEquals(message, viewModel.errorMessage.value)
    }

    @Test
    fun `shows position by name when use case return one result`() {
        whenever(connectionManager.isOnline()).thenReturn(true)
        val locationName = LOCATION_NAME
        val address = ADDRESS
        val addressList = mutableListOf(address)
        val addressCollection = AddressCollection(addressList)
        executeOnAddressLoadedAfterCoordinatesByNameUseCaseCallback(locationName, addressCollection)

        viewModel.onAddressSearch(locationName)

        assertEquals(address, viewModel.addressFoundEvent.value!!.peekContent())
    }

    @Test
    fun `shows address selection dialog when position by name use case return several results`() {
        whenever(connectionManager.isOnline()).thenReturn(true)
        val locationName = LOCATION_NAME
        val address = ADDRESS
        val addressList = mutableListOf(address, address)
        val addressCollection = AddressCollection(addressList)
        executeOnAddressLoadedAfterCoordinatesByNameUseCaseCallback(locationName, addressCollection)

        viewModel.onAddressSearch(locationName)

        assertEquals(addressCollection.addressList, viewModel.multipleAddressesFoundEvent.value!!.peekContent())
    }

    @Test
    fun `hides progress dialog when position by name use case fails`() {
        whenever(connectionManager.isOnline()).thenReturn(true)
        val locationName = LOCATION_NAME
        val errorMessage = ERROR_MESSAGE
        executeOnErrorAfterCoordinatesByNameUseCaseCallback(locationName, errorMessage)

        viewModel.onAddressSearch(locationName)

        assertEquals(false, viewModel.progressVisibility.value)
    }

    @Test
    fun `shows error when position by name use case fails`() {
        whenever(connectionManager.isOnline()).thenReturn(true)
        val locationName = LOCATION_NAME
        val errorMessage = ERROR_MESSAGE
        executeOnErrorAfterCoordinatesByNameUseCaseCallback(locationName, errorMessage)

        viewModel.onAddressSearch(locationName)

        assertEquals(errorMessage, viewModel.errorMessage.value)
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
        val coordinates = COORDINATES
        whenever(resourceProvider.get(any())).thenReturn("random string")

        viewModel.onAddressSearch(coordinates)

        assertTrue(viewModel.connectionIssueEvent.value != null)
    }

    @Test
    fun `shows progress dialog when connection available in position by coordinates`() {
        whenever(connectionManager.isOnline()).thenReturn(true)
        val coordinates = COORDINATES

        viewModel.onAddressSearch(coordinates)

        assertEquals(true, viewModel.progressVisibility.value)
    }

    @Test
    fun `executes name by coordinates use case when connection available`() {
        whenever(connectionManager.isOnline()).thenReturn(true)
        val coordinates = COORDINATES

        viewModel.onAddressSearch(coordinates)

        verify(getAddressNameByCoordinatesUseCase).execute(any(), anyInt(), any())
    }

    @Test
    fun `hides progress dialog when use case succeeds in position by coordinates`() {
        whenever(connectionManager.isOnline()).thenReturn(true)
        val coordinates = COORDINATES
        val addressCollection = EMPTY_ADDRESS_COLLECTION
        executeOnAddressLoadedAfterNameByCoordinatesUseCaseCallback(coordinates, addressCollection)

        viewModel.onAddressSearch(coordinates)

        assertEquals(false, viewModel.progressVisibility.value)
    }

    @Test
    fun `shows no matches when position by coordinates use case return zero results`() {
        whenever(connectionManager.isOnline()).thenReturn(true)
        val coordinates = COORDINATES
        val addressCollection = EMPTY_ADDRESS_COLLECTION
        executeOnAddressLoadedAfterNameByCoordinatesUseCaseCallback(coordinates, addressCollection)
        val message = "no matches"
        whenever(resourceProvider.get(any())).thenReturn(message)

        viewModel.onAddressSearch(coordinates)

        assertEquals(message, viewModel.errorMessage.value)
    }

    @Test
    fun `shows position by coordinates when use case return one result`() {
        whenever(connectionManager.isOnline()).thenReturn(true)
        val coordinates = COORDINATES
        val address = ADDRESS
        val addressList = mutableListOf(address)
        val addressCollection = AddressCollection(addressList)
        executeOnAddressLoadedAfterNameByCoordinatesUseCaseCallback(coordinates, addressCollection)

        viewModel.onAddressSearch(coordinates)

        assertEquals(address, viewModel.addressFoundEvent.value!!.peekContent())
    }

    @Test
    fun `hides progress dialog when position by coordinates use case fails`() {
        whenever(connectionManager.isOnline()).thenReturn(true)
        val coordinates = COORDINATES
        val errorMessage = ERROR_MESSAGE
        executeOnErrorAfterNameByCoordinatesUseCaseCallback(coordinates, errorMessage)

        viewModel.onAddressSearch(coordinates)

        assertEquals(false, viewModel.progressVisibility.value)
    }

    @Test
    fun `shows error when position by coordinates use case fails`() {
        whenever(connectionManager.isOnline()).thenReturn(true)
        val coordinates = COORDINATES
        val errorMessage = ERROR_MESSAGE
        executeOnErrorAfterNameByCoordinatesUseCaseCallback(coordinates, errorMessage)

        viewModel.onAddressSearch(coordinates)

        assertEquals(errorMessage, viewModel.errorMessage.value)
    }

    private fun executeOnAddressLoadedAfterCoordinatesByNameUseCaseCallback(locationName: String,
                                                                            addressCollection: AddressCollection) {
        doAnswer {
                (it.arguments[2] as GetAddressCoordinatesByNameInteractor.Callback).onAddressLoaded(addressCollection)
        }.whenever(getAddressCoordinatesByNameUseCase).execute(eq(locationName), anyInt(), any())
    }

    private fun executeOnErrorAfterCoordinatesByNameUseCaseCallback(locationName: String,
                                                                    errorMessage: String) {
        doAnswer {
                (it.arguments[2] as GetAddressCoordinatesByNameInteractor.Callback).onError(errorMessage)
        }.whenever(getAddressCoordinatesByNameUseCase).execute(eq(locationName), anyInt(), any())
    }

    private fun executeOnAddressLoadedAfterNameByCoordinatesUseCaseCallback(coordinates: LatLng,
                                                                            addressCollection: AddressCollection) {
        doAnswer {
                (it.arguments[2] as GetAddressNameByCoordinatesInteractor.Callback).onAddressLoaded(addressCollection)
        }.whenever(getAddressNameByCoordinatesUseCase).execute(eq(coordinates), anyInt(), any())
    }

    private fun executeOnErrorAfterNameByCoordinatesUseCaseCallback(coordinates: LatLng,
                                                                    errorMessage: String) {
        doAnswer {
                (it.arguments[2] as GetAddressNameByCoordinatesInteractor.Callback).onError(errorMessage)
        }.whenever(getAddressNameByCoordinatesUseCase).execute(eq(coordinates), anyInt(), any())
    }

    companion object {

        private const val LOCATION_NAME = "fake location name"
        private const val ERROR_MESSAGE = "fake errorMessage"

        private val COORDINATES = LatLng(0.0, 0.0)
        private val ADDRESS = gc.david.dfm.address.domain.model.Address(LOCATION_NAME, COORDINATES)
        private val EMPTY_ADDRESS_COLLECTION = AddressCollection(ArrayList())
    }
}