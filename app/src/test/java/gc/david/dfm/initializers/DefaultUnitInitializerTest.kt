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

package gc.david.dfm.initializers

import android.app.Application
import gc.david.dfm.common.domain.model.UnitSystem
import gc.david.dfm.settings.domain.SettingsRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Locale

class DefaultUnitInitializerTest {

    private val settingsRepository = mock<SettingsRepository>()
    private val application = mock<Application>()
    private val initializer = DefaultUnitInitializer(settingsRepository)
    private val originalLocale = Locale.getDefault()

    @AfterEach
    fun tearDown() {
        Locale.setDefault(originalLocale)
    }

    @Test
    fun `init keeps a stored unit`() {
        Locale.setDefault(Locale.US)
        whenever(settingsRepository.hasUnitSystemPreference()).thenReturn(true)

        initializer.init(application)

        verify(settingsRepository, never()).setUnitSystemPreference(any())
    }

    @Test
    fun `init stores imperial when nothing is stored and the locale is US`() {
        Locale.setDefault(Locale.US)
        whenever(settingsRepository.hasUnitSystemPreference()).thenReturn(false)

        initializer.init(application)

        verify(settingsRepository).setUnitSystemPreference(UnitSystem.IMPERIAL)
    }

    @Test
    fun `init stores metric when nothing is stored and the locale is not US`() {
        Locale.setDefault(Locale.forLanguageTag("es-ES"))
        whenever(settingsRepository.hasUnitSystemPreference()).thenReturn(false)

        initializer.init(application)

        verify(settingsRepository).setUnitSystemPreference(UnitSystem.METRIC)
    }
}
