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

package gc.david.dfm.initializers

import android.app.Application
import gc.david.dfm.DFMPreferences
import gc.david.dfm.map.Haversine
import java.util.*

class DefaultUnitInitializer : Initializer {

    private val isAmericanLocale: Boolean
        get() = Haversine.isAmericanLocale(Locale.getDefault())

    override fun init(application: Application) {
        val defaultUnit = DFMPreferences.getMeasureUnitPreference(application)
        if (defaultUnit == null) {
            DFMPreferences.setMeasureUnitPreference(
                    application,
                    if (isAmericanLocale) DFMPreferences.MEASURE_AMERICAN_UNIT_VALUE
                    else DFMPreferences.MEASURE_EUROPEAN_UNIT_VALUE
            )
        }
    }
}