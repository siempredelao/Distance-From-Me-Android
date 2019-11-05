/*
 * Copyright (c) 2018 David Aguiar Gonzalez
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

import com.nhaarman.mockitokotlin2.whenever
import gc.david.dfm.distance.domain.ClearDistancesUseCase
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

/**
 * Created by david on 24.01.17.
 */
class SettingsPresenterTest {

    @Mock
    lateinit var settingsView: Settings.View
    @Mock
    lateinit var clearDistancesUseCase: ClearDistancesUseCase

    private lateinit var settingsPresenter: SettingsPresenter

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        settingsPresenter = SettingsPresenter(settingsView, clearDistancesUseCase)
    }

    @Test
    fun `shows success message when use case succeeds`() {
        doAnswer {
                (it.arguments[0] as ClearDistancesUseCase.Callback).onClear()
        }.whenever(clearDistancesUseCase).execute(any(ClearDistancesUseCase.Callback::class.java))

        settingsPresenter.onClearData()

        verify<Settings.View>(settingsView).showClearDataSuccessMessage()
    }

    @Test
    fun `shows error message when use case fails`() {
        doAnswer {
                (it.arguments[0] as ClearDistancesUseCase.Callback).onError()
        }.whenever(clearDistancesUseCase).execute(any(ClearDistancesUseCase.Callback::class.java))

        settingsPresenter.onClearData()

        verify(settingsView).showClearDataErrorMessage()
    }
}