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

package gc.david.dfm

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

/**
 * Created by david on 05.11.16.
 */
object DFMPreferences {

    private const val MEASURE_UNIT_KEY = "unit"
    const val MEASURE_EUROPEAN_UNIT_VALUE = "EU"
    const val MEASURE_AMERICAN_UNIT_VALUE = "US"
    private const val ELEVATION_CHART_KEY = "elevation_chart"
    private const val ANIMATION_KEY = "animation"
    const val ANIMATION_CENTRE_VALUE = "CEN"
    const val ANIMATION_DESTINATION_VALUE = "DES"
    const val NO_ANIMATION_DESTINATION_VALUE = "NO"
    const val CLEAR_DATABASE_KEY = "bbdd"


    fun getMeasureUnitPreference(context: Context): String? {
        return getPreferences(context).getString(MEASURE_UNIT_KEY, null)
    }

    fun setMeasureUnitPreference(context: Context, unit: String) {
        getPreferences(context).edit().putString(MEASURE_UNIT_KEY, unit).apply()
    }

    fun shouldShowElevationChart(context: Context): Boolean {
        return getPreferences(context).getBoolean(ELEVATION_CHART_KEY, true)
    }

    fun getAnimationPreference(context: Context): String? {
        return getPreferences(context).getString(ANIMATION_KEY, ANIMATION_CENTRE_VALUE)
    }

    private fun getPreferences(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }
}
