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
import gc.david.dfm.address.domain.model.AddressCollection
import gc.david.dfm.address.presentation.mapper.GeocodingErrorMessageMapper
import gc.david.dfm.common.Coordinates
import gc.david.dfm.common.ResourceProvider
import gc.david.dfm.distance.domain.CoordinatesRepository
import gc.david.dfm.showinfo.R
import gc.david.dfm.showinfo.presentation.mapper.ShareInfoMessageMapper
import gc.david.dfm.testsupport.CoroutineDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
internal class ShowInfoViewModelTest {

    @get:Rule val coroutineDispatcherRule = CoroutineDispatcherRule()

    private val getAddressNameByCoordinatesUseCase = mock<GetAddressNameByCoordinatesUseCase>()
    private val resourceProvider = mock<ResourceProvider>()
    private val connectionManager = mock<ConnectionManager>()
    private val addressFormatter = AddressFormatter()
    private val coordinatesRepository = mock<CoordinatesRepository>()
    private val geocodingErrorMessageMapper = mock<GeocodingErrorMessageMapper>()
    private val shareInfoMessageMapper = mock<ShareInfoMessageMapper>()

    private val viewModel =
        ShowInfoViewModel(
            getAddressNameByCoordinatesUseCase,
            resourceProvider,
            connectionManager,
            addressFormatter,
            coordinatesRepository,
            geocodingErrorMessageMapper,
            shareInfoMessageMapper,
        )

    @Test
    fun `sets shouldFinish when distance is blank`() = runTest {
        whenever(coordinatesRepository.observeDistance()).thenReturn(MutableStateFlow(listOf(COORDS_1)))

        viewModel.onStart("   ")

        assertTrue(viewModel.uiState.value.shouldFinish)
    }

    @Test
    fun `sets shouldFinish when coordinates repository is empty`() = runTest {
        whenever(coordinatesRepository.observeDistance()).thenReturn(MutableStateFlow(emptyList()))

        viewModel.onStart("100m")

        assertTrue(viewModel.uiState.value.shouldFinish)
    }

    @Test
    fun `shows network problems message when offline`() = runTest {
        whenever(coordinatesRepository.observeDistance()).thenReturn(MutableStateFlow(listOf(COORDS_1, COORDS_2)))
        whenever(connectionManager.isOnline()).thenReturn(false)
        whenever(resourceProvider.get(R.string.toast_network_problems)).thenReturn("network problems")

        viewModel.onStart("100m")

        assertEquals("network problems", viewModel.uiState.value.userMessage)
    }

    private companion object {

        val COORDS_1 = Coordinates(1.0, 2.0)
        val COORDS_2 = Coordinates(3.0, 4.0)
    }
}

