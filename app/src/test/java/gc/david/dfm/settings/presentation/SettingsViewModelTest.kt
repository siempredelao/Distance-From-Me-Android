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

package gc.david.dfm.settings.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import gc.david.dfm.ResourceProvider
import gc.david.dfm.distance.domain.ClearDistancesInteractor
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.doAnswer
import org.mockito.junit.MockitoJUnitRunner

/**
 * Created by david on 24.01.17.
 */
@RunWith(MockitoJUnitRunner::class)
class SettingsViewModelTest {

    @Mock
    lateinit var clearDistancesUseCase: ClearDistancesInteractor
    @Mock
    lateinit var resourceProvider: ResourceProvider

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        viewModel = SettingsViewModel(clearDistancesUseCase, resourceProvider)
    }

    @Test
    fun `shows success message when use case succeeds`() {
        doAnswer {
                (it.arguments[0] as ClearDistancesInteractor.Callback).onClear()
        }.whenever(clearDistancesUseCase).execute(any())
        val successMessage = "success"
        whenever(resourceProvider.get(any())).thenReturn(successMessage)

        viewModel.onClearData()

        assertEquals(viewModel.resultMessage.value, successMessage)
    }
}