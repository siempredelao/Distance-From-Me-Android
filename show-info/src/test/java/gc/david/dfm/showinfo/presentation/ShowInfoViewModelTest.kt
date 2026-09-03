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

import gc.david.dfm.ConnectionManager
import gc.david.dfm.address.domain.GetAddressNameByCoordinatesUseCase
import gc.david.dfm.address.presentation.mapper.GeocodingErrorMessageMapper
import gc.david.dfm.common.Coordinates
import gc.david.dfm.common.presentation.ResourceProvider
import gc.david.dfm.common.domain.DistanceCalculator
import gc.david.dfm.common.domain.model.UnitSystem
import gc.david.dfm.common.presentation.DistanceFormatter
import gc.david.dfm.distance.domain.CoordinatesRepository
import gc.david.dfm.settings.domain.SettingsRepository
import gc.david.dfm.showinfo.R
import gc.david.dfm.showinfo.presentation.mapper.AddressFormatter
import gc.david.dfm.showinfo.presentation.mapper.ShareInfoMessageMapper
import gc.david.dfm.testsupport.CoroutineExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

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
    private val distanceFormatter = mock<DistanceFormatter>()
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
            distanceFormatter,
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
        whenever(resourceProvider.get(R.string.toast_network_problems)).thenReturn("network problems")
        whenever(distanceCalculator.calculateTotalDistance(coordinatesList)).thenReturn(100.0)
        whenever(settingsRepository.getUnitSystemPreference()).thenReturn(UnitSystem.METRIC)
        whenever(distanceFormatter.formatDistance(100.0, UnitSystem.METRIC)).thenReturn("100 m")

        viewModel.onStart()

        assertEquals("network problems", viewModel.uiState.value.userMessage)
    }

    @Test
    fun `onRefresh calls load with stored parameters`() = runTest {
        val coordinatesList = listOf(COORDS_1, COORDS_2)
        whenever(coordinatesRepository.observeDistance()).thenReturn(MutableStateFlow(coordinatesList))
        whenever(connectionManager.isOnline()).thenReturn(false)
        whenever(distanceCalculator.calculateTotalDistance(coordinatesList)).thenReturn(100.0)
        whenever(settingsRepository.getUnitSystemPreference()).thenReturn(UnitSystem.METRIC)
        whenever(distanceFormatter.formatDistance(100.0, UnitSystem.METRIC)).thenReturn("100 m")
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
            .thenReturn(Result.success(gc.david.dfm.address.domain.model.AddressCollection(listOf(address))))
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
    fun `onShareDialogShown clears share intent data`() = runTest {
        val coordinatesList = listOf(COORDS_1, COORDS_2)
        val address = createAddress("Test Address")
        setupSuccessfulStart(coordinatesList)
        whenever(getAddressNameByCoordinatesUseCase(any()))
            .thenReturn(Result.success(gc.david.dfm.address.domain.model.AddressCollection(listOf(address))))
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
            .thenReturn(Result.success(gc.david.dfm.address.domain.model.AddressCollection(listOf(address))))
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
            .thenReturn(Result.success(gc.david.dfm.address.domain.model.AddressCollection(listOf(address))))
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
        whenever(distanceCalculator.calculateTotalDistance(coordinatesList)).thenReturn(100.0)
        whenever(settingsRepository.getUnitSystemPreference()).thenReturn(UnitSystem.METRIC)
        whenever(distanceFormatter.formatDistance(100.0, UnitSystem.METRIC)).thenReturn("100 m")
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
        whenever(distanceFormatter.formatDistance(100.0, UnitSystem.METRIC)).thenReturn("100 m")
        whenever(resourceProvider.get(R.string.info_distance_title, "100 m")).thenReturn("Distance: 100 m")
        whenever(getAddressNameByCoordinatesUseCase(any()))
            .thenReturn(Result.success(gc.david.dfm.address.domain.model.AddressCollection(listOf(address1))))
            .thenReturn(Result.success(gc.david.dfm.address.domain.model.AddressCollection(listOf(address2))))

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
        whenever(distanceFormatter.formatDistance(100.0, UnitSystem.METRIC)).thenReturn("100 m")
        whenever(resourceProvider.get(R.string.info_distance_title, "100 m")).thenReturn("Distance: 100 m")
        whenever(resourceProvider.get(R.string.error_no_address_found_message)).thenReturn("No address found")
        whenever(getAddressNameByCoordinatesUseCase(any()))
            .thenReturn(Result.success(gc.david.dfm.address.domain.model.AddressCollection(emptyList())))

        viewModel.onStart()
        testScheduler.advanceUntilIdle()

        assertEquals("No address found", viewModel.uiState.value.originAddress)
        assertEquals("No address found", viewModel.uiState.value.destinationAddress)
    }

    @Test
    fun `resolveAddress with error maps error message`() = runTest {
        val coordinatesList = listOf(COORDS_1, COORDS_2)
        val exception = Exception("Geocoding error")
        whenever(coordinatesRepository.observeDistance()).thenReturn(MutableStateFlow(coordinatesList))
        whenever(connectionManager.isOnline()).thenReturn(true)
        whenever(distanceCalculator.calculateTotalDistance(coordinatesList)).thenReturn(100.0)
        whenever(settingsRepository.getUnitSystemPreference()).thenReturn(UnitSystem.METRIC)
        whenever(distanceFormatter.formatDistance(100.0, UnitSystem.METRIC)).thenReturn("100 m")
        whenever(resourceProvider.get(R.string.info_distance_title, "100 m")).thenReturn("Distance: 100 m")
        whenever(geocodingErrorMessageMapper.map(exception)).thenReturn("Geocoding failed")
        whenever(getAddressNameByCoordinatesUseCase(any())).thenReturn(Result.failure(exception))

        viewModel.onStart()
        testScheduler.advanceUntilIdle()

        assertEquals("Geocoding failed", viewModel.uiState.value.originAddress)
        assertEquals("Geocoding failed", viewModel.uiState.value.destinationAddress)
    }

    private fun setupSuccessfulStart(coordinatesList: List<Coordinates>) {
        whenever(coordinatesRepository.observeDistance()).thenReturn(MutableStateFlow(coordinatesList))
        whenever(connectionManager.isOnline()).thenReturn(true)
        whenever(distanceCalculator.calculateTotalDistance(coordinatesList)).thenReturn(100.0)
        whenever(settingsRepository.getUnitSystemPreference()).thenReturn(UnitSystem.METRIC)
        whenever(distanceFormatter.formatDistance(100.0, UnitSystem.METRIC)).thenReturn("100 m")
        whenever(resourceProvider.get(R.string.info_distance_title, "100 m")).thenReturn("Distance: 100 m")
    }

    private fun createAddress(formattedAddress: String) =
        gc.david.dfm.address.domain.model.Address(
            formattedAddress,
            gc.david.dfm.address.domain.model.Coordinates(1.0, 1.0)
        )

    private companion object {

        val COORDS_1 = Coordinates(1.0, 2.0)
        val COORDS_2 = Coordinates(3.0, 4.0)
    }
}

