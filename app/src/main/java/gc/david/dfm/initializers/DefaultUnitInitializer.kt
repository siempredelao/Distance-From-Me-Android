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
import gc.david.dfm.core.settings.domain.SettingsRepository
import gc.david.dfm.core.units.domain.model.UnitSystem
import java.util.Locale

class DefaultUnitInitializer(
    private val settingsRepository: SettingsRepository
) : Initializer {

    private val isImperialCountry: Boolean
        get() = Locale.getDefault().country in IMPERIAL_COUNTRIES

    override fun init(application: Application) {
        if (settingsRepository.hasUnitSystemPreference()) {
            return
        }

        settingsRepository.setUnitSystemPreference(
            if (isImperialCountry) UnitSystem.IMPERIAL else UnitSystem.METRIC
        )
    }

    private companion object {

        // The countries that Unicode CLDR 46 doesn't count as metric. Android's LocaleData.getMeasurementSystem gives
        // the same ones, but needs API 28
        val IMPERIAL_COUNTRIES = setOf("US", "GB", "LR", "MM")
    }
}
