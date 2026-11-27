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

import gc.david.dfm.core.address.domain.GetAddressNameByCoordinatesUseCase
import gc.david.dfm.core.address.domain.model.Address
import gc.david.dfm.core.address.domain.model.GeocodingError
import gc.david.dfm.core.address.domain.model.GeocodingResult
import gc.david.dfm.core.address.presentation.mapper.GeocodingErrorMessageMapper
import gc.david.dfm.core.coordinates.domain.CoordinatesRepository
import gc.david.dfm.core.coordinates.domain.DistanceCalculator
import gc.david.dfm.core.coordinates.domain.model.Coordinates
import gc.david.dfm.core.settings.domain.SettingsRepository
import gc.david.dfm.core.units.domain.model.UnitSystem
import gc.david.dfm.core.units.presentation.UnitFormatter
import gc.david.dfm.showinfo.R
import gc.david.dfm.showinfo.presentation.mapper.AddressFormatter
import gc.david.dfm.showinfo.presentation.mapper.ShareInfoMessageMapper
import gc.david.dfm.showinfo.presentation.model.ShowInfoUiState
import gc.david.dfm.support.android.domain.ConnectionManager
import gc.david.dfm.support.android.presentation.ResourceProvider
import gc.david.dfm.support.test.CoroutineExtension
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
internal class ShowInfoViewModelTest {

    @JvmField
    @RegisterExtension
    val coroutineExtension = CoroutineExtension()

    private val getAddressNameByCoordinatesUseCase = mock<GetAddressNameByCoordinatesUseCase>()
    private val resourceProvider = mock<ResourceProvider>()
    private val connectionManager = mock<ConnectionManager>()
    private val addressFormatter = AddressFormatter()
    private val coordinatesRepository = mock<CoordinatesRepository>()
    private val geocodingErrorMessageMapper = mock<GeocodingErrorMessageMapper>()
    private val shareInfoMessageMapper = mock<ShareInfoMessageMapper>()
    private val settingsRepository = mock<SettingsRepository>()
    private val unitFormatter = mock<UnitFormatter>()
    private val distanceCalculator = mock<DistanceCalculator>()

    private val viewModel =
        ShowInfoViewModel(
            getAddressNameByCoordinatesUseCase,
            resourceProvider,
            connectionManager,
            addressFormatter,
            coordinatesRepository,
            geocodingErrorMessageMapper,
            shareInfoMessageMapper,
            settingsRepository,
            unitFormatter,
            distanceCalculator
        )

    @Test
    fun `sets shouldFinish when coordinates repository is empty`() = runTest {
        whenever(coordinatesRepository.observeDistance()).thenReturn(MutableStateFlow(emptyList()))

        viewModel.onStart()

        assertTrue(viewModel.uiState.value.shouldFinish)
    }

    @Test
    fun `shows network problems message when offline`() = runTest {
        val coordinatesList = listOf(COORDS_1, COORDS_2)
        whenever(coordinatesRepository.observeDistance()).thenReturn(MutableStateFlow(coordinatesList))
        whenever(connectionManager.isOnline()).thenReturn(false)
        whenever(resourceProvider.get(R.string.info_distance_title, "100 m")).thenReturn("Distance: 100 m")
        whenever(resourceProvider.get(R.string.toast_network_problems)).thenReturn("network problems")
        whenever(distanceCalculator.calculateTotalDistance(coordinatesList)).thenReturn(100.0)
        whenever(settingsRepository.getUnitSystemPreference()).thenReturn(UnitSystem.METRIC)
        whenever(unitFormatter.formatDistance(100.0, UnitSystem.METRIC)).thenReturn("100 m")

        viewModel.onStart()

        assertEquals("network problems", viewModel.uiState.value.userMessage)
    }

    @Test
    fun `shows the distance when offline`() = runTest {
        val coordinatesList = listOf(COORDS_1, COORDS_2)
        whenever(coordinatesRepository.observeDistance()).thenReturn(MutableStateFlow(coordinatesList))
        whenever(connectionManager.isOnline()).thenReturn(false)
        whenever(resourceProvider.get(R.string.toast_network_problems)).thenReturn("network problems")
        whenever(resourceProvider.get(R.string.info_distance_title, "100 m")).thenReturn("Distance: 100 m")
        whenever(distanceCalculator.calculateTotalDistance(coordinatesList)).thenReturn(100.0)
        whenever(settingsRepository.getUnitSystemPreference()).thenReturn(UnitSystem.METRIC)
        whenever(unitFormatter.formatDistance(100.0, UnitSystem.METRIC)).thenReturn("100 m")

        viewModel.onStart()

        assertEquals("Distance: 100 m", viewModel.uiState.value.distanceMessage)
    }

    @Test
    fun `shows the coordinates when offline`() = runTest {
        setupSuccessfulStart(listOf(COORDS_1, COORDS_2))
        whenever(connectionManager.isOnline()).thenReturn(false)
        whenever(resourceProvider.get(R.string.toast_network_problems)).thenReturn("network problems")

        viewModel.onStart()

        assertEquals("(1.0,2.0)", viewModel.uiState.value.originAddress)
        assertEquals("(3.0,4.0)", viewModel.uiState.value.destinationAddress)
    }

    @Test
    fun `shows the coordinates and the distance until the addresses are found`() = runTest {
        setupSuccessfulStart(listOf(COORDS_1, COORDS_2))
        val lookup = CompletableDeferred<GeocodingResult>()
        whenever(getAddressNameByCoordinatesUseCase(any())).doSuspendableAnswer { lookup.await() }

        viewModel.onStart()

        assertTrue(viewModel.uiState.value.isLoading)
        assertEquals("(1.0,2.0)", viewModel.uiState.value.originAddress)
        assertEquals("(3.0,4.0)", viewModel.uiState.value.destinationAddress)
        assertEquals("Distance: 100 m", viewModel.uiState.value.distanceMessage)

        lookup.complete(GeocodingResult.Found(listOf(createAddress("Address"))))

        assertEquals("Address\n\n(1.0,2.0)", viewModel.uiState.value.originAddress)
        assertEquals("Address\n\n(3.0,4.0)", viewModel.uiState.value.destinationAddress)
    }

    @Test
    fun `onRefresh calls load with stored parameters`() = runTest {
        val coordinatesList = listOf(COORDS_1, COORDS_2)
        whenever(coordinatesRepository.observeDistance()).thenReturn(MutableStateFlow(coordinatesList))
        whenever(connectionManager.isOnline()).thenReturn(false)
        whenever(resourceProvider.get(R.string.info_distance_title, "100 m")).thenReturn("Distance: 100 m")
        whenever(distanceCalculator.calculateTotalDistance(coordinatesList)).thenReturn(100.0)
        whenever(settingsRepository.getUnitSystemPreference()).thenReturn(UnitSystem.METRIC)
        whenever(unitFormatter.formatDistance(100.0, UnitSystem.METRIC)).thenReturn("100 m")
        whenever(resourceProvider.get(R.string.toast_network_problems)).thenReturn("network problems")
        viewModel.onStart()

        viewModel.onRefresh()

        assertEquals("network problems", viewModel.uiState.value.userMessage)
    }

    @Test
    fun `onShare creates share intent data with correct values`() = runTest {
        val coordinatesList = listOf(COORDS_1, COORDS_2)
        val address = createAddress("Test Address")
        setupSuccessfulStart(coordinatesList)
        whenever(getAddressNameByCoordinatesUseCase(any()))
            .thenReturn(GeocodingResult.Found(listOf(address)))
        whenever(shareInfoMessageMapper.getSubject()).thenReturn("Subject")
        whenever(shareInfoMessageMapper.mapMessage(any(), any(), any())).thenReturn("Message")
        whenever(resourceProvider.get(R.string.action_bar_item_social_share_title)).thenReturn("Share")
        viewModel.onStart()
        testScheduler.advanceUntilIdle()

        viewModel.onShare()

        val shareData = viewModel.uiState.value.shareIntentData
        assertEquals("Share", shareData?.title)
        assertEquals("Subject", shareData?.subject)
        assertEquals("Message", shareData?.message)
    }

    @Test
    fun `onShare sends the addresses that were found`() = runTest {
        setupSuccessfulStart(listOf(COORDS_1, COORDS_2))
        whenever(getAddressNameByCoordinatesUseCase(any()))
            .thenReturn(GeocodingResult.Found(listOf(createAddress("Address 1"))))
            .thenReturn(GeocodingResult.Found(listOf(createAddress("Address 2"))))
        setupShare()
        viewModel.onStart()
        testScheduler.advanceUntilIdle()

        viewModel.onShare()

        verify(shareInfoMessageMapper)
            .mapMessage("Address 1\n\n(1.0,2.0)", "Address 2\n\n(3.0,4.0)", "100 m")
    }

    @Test
    fun `onShare sends the coordinates of a rejected or failed lookup instead of its error`() = runTest {
        setupSuccessfulStart(listOf(COORDS_1, COORDS_2))
        whenever(geocodingErrorMessageMapper.map(GeocodingError.OVER_QUERY_LIMIT)).thenReturn("Too many requests")
        whenever(geocodingErrorMessageMapper.unexpectedFailureMessage()).thenReturn("Geocoding failed")
        whenever(getAddressNameByCoordinatesUseCase(any()))
            .thenReturn(GeocodingResult.Rejected(GeocodingError.OVER_QUERY_LIMIT))
            .thenAnswer { throw IOException("Network error") }
        setupShare()
        viewModel.onStart()
        testScheduler.advanceUntilIdle()

        viewModel.onShare()

        verify(shareInfoMessageMapper).mapMessage("(1.0,2.0)", "(3.0,4.0)", "100 m")
        assertEquals("Too many requests\n\n(1.0,2.0)", viewModel.uiState.value.originAddress)
        assertEquals("Geocoding failed\n\n(3.0,4.0)", viewModel.uiState.value.destinationAddress)
    }

    @Test
    fun `onShare sends the coordinates of a place without an address`() = runTest {
        setupSuccessfulStart(listOf(COORDS_1, COORDS_2))
        whenever(resourceProvider.get(R.string.error_no_address_found_message)).thenReturn("No address found")
        whenever(getAddressNameByCoordinatesUseCase(any())).thenReturn(GeocodingResult.NoResults)
        setupShare()
        viewModel.onStart()
        testScheduler.advanceUntilIdle()

        viewModel.onShare()

        verify(shareInfoMessageMapper).mapMessage("(1.0,2.0)", "(3.0,4.0)", "100 m")
    }

    @Test
    fun `onShare sends the coordinates when offline`() = runTest {
        setupSuccessfulStart(listOf(COORDS_1, COORDS_2))
        whenever(connectionManager.isOnline()).thenReturn(false)
        whenever(resourceProvider.get(R.string.toast_network_problems)).thenReturn("network problems")
        setupShare()
        viewModel.onStart()

        viewModel.onShare()

        verify(shareInfoMessageMapper).mapMessage("(1.0,2.0)", "(3.0,4.0)", "100 m")
    }

    @Test
    fun `onShareDialogShown clears share intent data`() = runTest {
        val coordinatesList = listOf(COORDS_1, COORDS_2)
        val address = createAddress("Test Address")
        setupSuccessfulStart(coordinatesList)
        whenever(getAddressNameByCoordinatesUseCase(any()))
            .thenReturn(GeocodingResult.Found(listOf(address)))
        whenever(shareInfoMessageMapper.getSubject()).thenReturn("Subject")
        whenever(shareInfoMessageMapper.mapMessage(any(), any(), any())).thenReturn("Message")
        whenever(resourceProvider.get(R.string.action_bar_item_social_share_title)).thenReturn("Share")
        viewModel.onStart()
        testScheduler.advanceUntilIdle()
        viewModel.onShare()

        viewModel.onShareDialogShown()

        assertEquals(null, viewModel.uiState.value.shareIntentData)
    }

    @Test
    fun `onSave shows save dialog with correct parameters`() = runTest {
        val coordinatesList = listOf(COORDS_1, COORDS_2)
        val address = createAddress("Test Address")
        setupSuccessfulStart(coordinatesList)
        whenever(getAddressNameByCoordinatesUseCase(any()))
            .thenReturn(GeocodingResult.Found(listOf(address)))
        viewModel.onStart()
        testScheduler.advanceUntilIdle()

        viewModel.onSave()

        val saveDialog = viewModel.uiState.value.showSaveDialog
        assertEquals(coordinatesList, saveDialog?.positionsList)
        assertEquals("100 m", saveDialog?.distance)
    }

    @Test
    fun `onSaveDialogDismissed clears save dialog`() = runTest {
        val coordinatesList = listOf(COORDS_1, COORDS_2)
        val address = createAddress("Test Address")
        setupSuccessfulStart(coordinatesList)
        whenever(getAddressNameByCoordinatesUseCase(any()))
            .thenReturn(GeocodingResult.Found(listOf(address)))
        viewModel.onStart()
        testScheduler.advanceUntilIdle()
        viewModel.onSave()

        viewModel.onSaveDialogDismissed()

        assertEquals(null, viewModel.uiState.value.showSaveDialog)
    }

    @Test
    fun `onUserMessageShown clears user message`() = runTest {
        val coordinatesList = listOf(COORDS_1, COORDS_2)
        whenever(coordinatesRepository.observeDistance()).thenReturn(MutableStateFlow(coordinatesList))
        whenever(connectionManager.isOnline()).thenReturn(false)
        whenever(resourceProvider.get(R.string.info_distance_title, "100 m")).thenReturn("Distance: 100 m")
        whenever(distanceCalculator.calculateTotalDistance(coordinatesList)).thenReturn(100.0)
        whenever(settingsRepository.getUnitSystemPreference()).thenReturn(UnitSystem.METRIC)
        whenever(unitFormatter.formatDistance(100.0, UnitSystem.METRIC)).thenReturn("100 m")
        whenever(resourceProvider.get(R.string.toast_network_problems)).thenReturn("network problems")
        viewModel.onStart()

        viewModel.onUserMessageShown()

        assertEquals(null, viewModel.uiState.value.userMessage)
    }

    @Test
    fun `load with success resolves addresses correctly`() = runTest {
        val coordinatesList = listOf(COORDS_1, COORDS_2)
        val address1 = createAddress("Address 1")
        val address2 = createAddress("Address 2")
        whenever(coordinatesRepository.observeDistance()).thenReturn(MutableStateFlow(coordinatesList))
        whenever(connectionManager.isOnline()).thenReturn(true)
        whenever(distanceCalculator.calculateTotalDistance(coordinatesList)).thenReturn(100.0)
        whenever(settingsRepository.getUnitSystemPreference()).thenReturn(UnitSystem.METRIC)
        whenever(unitFormatter.formatDistance(100.0, UnitSystem.METRIC)).thenReturn("100 m")
        whenever(resourceProvider.get(R.string.info_distance_title, "100 m")).thenReturn("Distance: 100 m")
        whenever(getAddressNameByCoordinatesUseCase(any()))
            .thenReturn(GeocodingResult.Found(listOf(address1)))
            .thenReturn(GeocodingResult.Found(listOf(address2)))

        viewModel.onStart()
        testScheduler.advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.originAddress.contains("Address 1"))
        assertTrue(viewModel.uiState.value.destinationAddress.contains("Address 2"))
    }

    @Test
    fun `resolveAddress with empty address list shows error message`() = runTest {
        val coordinatesList = listOf(COORDS_1, COORDS_2)
        whenever(coordinatesRepository.observeDistance()).thenReturn(MutableStateFlow(coordinatesList))
        whenever(connectionManager.isOnline()).thenReturn(true)
        whenever(distanceCalculator.calculateTotalDistance(coordinatesList)).thenReturn(100.0)
        whenever(settingsRepository.getUnitSystemPreference()).thenReturn(UnitSystem.METRIC)
        whenever(unitFormatter.formatDistance(100.0, UnitSystem.METRIC)).thenReturn("100 m")
        whenever(resourceProvider.get(R.string.info_distance_title, "100 m")).thenReturn("Distance: 100 m")
        whenever(resourceProvider.get(R.string.error_no_address_found_message)).thenReturn("No address found")
        whenever(getAddressNameByCoordinatesUseCase(any()))
            .thenReturn(GeocodingResult.NoResults)

        viewModel.onStart()
        testScheduler.advanceUntilIdle()

        assertEquals("No address found\n\n(1.0,2.0)", viewModel.uiState.value.originAddress)
        assertEquals("No address found\n\n(3.0,4.0)", viewModel.uiState.value.destinationAddress)
    }

    @Test
    fun `resolveAddress with unexpected failure shows generic message`() = runTest {
        val coordinatesList = listOf(COORDS_1, COORDS_2)
        whenever(coordinatesRepository.observeDistance()).thenReturn(MutableStateFlow(coordinatesList))
        whenever(connectionManager.isOnline()).thenReturn(true)
        whenever(distanceCalculator.calculateTotalDistance(coordinatesList)).thenReturn(100.0)
        whenever(settingsRepository.getUnitSystemPreference()).thenReturn(UnitSystem.METRIC)
        whenever(unitFormatter.formatDistance(100.0, UnitSystem.METRIC)).thenReturn("100 m")
        whenever(resourceProvider.get(R.string.info_distance_title, "100 m")).thenReturn("Distance: 100 m")
        whenever(geocodingErrorMessageMapper.unexpectedFailureMessage()).thenReturn("Geocoding failed")
        whenever(getAddressNameByCoordinatesUseCase(any())).thenAnswer { throw IOException("Network error") }

        viewModel.onStart()
        testScheduler.advanceUntilIdle()

        assertEquals("Geocoding failed\n\n(1.0,2.0)", viewModel.uiState.value.originAddress)
        assertEquals("Geocoding failed\n\n(3.0,4.0)", viewModel.uiState.value.destinationAddress)
    }

    @Test
    fun `resolveAddress with rejected request maps rejection message`() = runTest {
        val coordinatesList = listOf(COORDS_1, COORDS_2)
        setupSuccessfulStart(coordinatesList)
        whenever(geocodingErrorMessageMapper.map(GeocodingError.REQUEST_DENIED)).thenReturn("Request denied")
        whenever(getAddressNameByCoordinatesUseCase(any()))
            .thenReturn(GeocodingResult.Rejected(GeocodingError.REQUEST_DENIED))

        viewModel.onStart()
        testScheduler.advanceUntilIdle()

        assertEquals("Request denied\n\n(1.0,2.0)", viewModel.uiState.value.originAddress)
        assertEquals("Request denied\n\n(3.0,4.0)", viewModel.uiState.value.destinationAddress)
    }

    @Test
    fun `one failing lookup does not prevent the other address from resolving`() = runTest {
        val coordinatesList = listOf(COORDS_1, COORDS_2)
        setupSuccessfulStart(coordinatesList)
        whenever(geocodingErrorMessageMapper.unexpectedFailureMessage()).thenReturn("Geocoding failed")
        whenever(getAddressNameByCoordinatesUseCase(any()))
            .thenAnswer { throw IOException("Network error") }
            .thenReturn(GeocodingResult.Found(listOf(createAddress("Address 2"))))

        viewModel.onStart()
        testScheduler.advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.isLoading)
        assertEquals("Geocoding failed\n\n(1.0,2.0)", viewModel.uiState.value.originAddress)
        assertTrue(viewModel.uiState.value.destinationAddress.contains("Address 2"))
    }

    @Test
    fun `onRefresh, onShare and onSave do nothing before onStart loads a distance`() = runTest {
        // As after process death: a new ViewModel whose onStart has not run yet
        viewModel.onRefresh()
        viewModel.onShare()
        viewModel.onSave()

        assertEquals(ShowInfoUiState(), viewModel.uiState.value)
        verifyNoInteractions(getAddressNameByCoordinatesUseCase, shareInfoMessageMapper)
    }

    @Test
    fun `onStart does not reload once a distance is loaded`() = runTest {
        val coordinatesList = listOf(COORDS_1, COORDS_2)
        setupSuccessfulStart(coordinatesList)
        whenever(getAddressNameByCoordinatesUseCase(any()))
            .thenReturn(GeocodingResult.Found(listOf(createAddress("Address"))))
        viewModel.onStart()
        testScheduler.advanceUntilIdle()

        viewModel.onStart()
        testScheduler.advanceUntilIdle()

        // One lookup for the origin and one for the destination, from the first onStart only
        verify(getAddressNameByCoordinatesUseCase, times(2)).invoke(any())
    }

    private fun setupSuccessfulStart(coordinatesList: List<Coordinates>) {
        whenever(coordinatesRepository.observeDistance()).thenReturn(MutableStateFlow(coordinatesList))
        whenever(connectionManager.isOnline()).thenReturn(true)
        whenever(distanceCalculator.calculateTotalDistance(coordinatesList)).thenReturn(100.0)
        whenever(settingsRepository.getUnitSystemPreference()).thenReturn(UnitSystem.METRIC)
        whenever(unitFormatter.formatDistance(100.0, UnitSystem.METRIC)).thenReturn("100 m")
        whenever(resourceProvider.get(R.string.info_distance_title, "100 m")).thenReturn("Distance: 100 m")
    }

    private fun setupShare() {
        whenever(shareInfoMessageMapper.getSubject()).thenReturn("Subject")
        whenever(shareInfoMessageMapper.mapMessage(any(), any(), any())).thenReturn("Message")
        whenever(resourceProvider.get(R.string.action_bar_item_social_share_title)).thenReturn("Share")
    }

    private fun createAddress(formattedAddress: String) =
        Address(formattedAddress, Coordinates(1.0, 1.0))

    private companion object {

        val COORDS_1 = Coordinates(1.0, 2.0)
        val COORDS_2 = Coordinates(3.0, 4.0)
    }
}

