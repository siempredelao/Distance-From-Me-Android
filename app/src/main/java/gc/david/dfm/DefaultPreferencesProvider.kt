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

package gc.david.dfm

import android.content.Context
import gc.david.dfm.map.CameraAnimation
import gc.david.dfm.map.UnitSystem

// TODO Consider moving to :common once DFMPreferences is decoupled from app-specific configuration
class DefaultPreferencesProvider(private val context: Context) : PreferencesProvider {

    override fun shouldShowElevationChart(): Boolean {
        return DFMPreferences.shouldShowElevationChart(context)
    }

    override fun getUnitSystemPreference(): UnitSystem {
        val value = DFMPreferences.getMeasureUnitPreference(context)
        return if (value == DFMPreferences.MEASURE_AMERICAN_UNIT_VALUE) UnitSystem.IMPERIAL else UnitSystem.METRIC
    }

    override fun getCameraAnimation(): CameraAnimation {
        return when (DFMPreferences.getAnimationPreference(context)) {
            DFMPreferences.ANIMATION_DESTINATION_VALUE -> CameraAnimation.Animate.Destination
            DFMPreferences.ANIMATION_CENTRE_VALUE -> CameraAnimation.Animate.Centre
            else -> CameraAnimation.None
        }
    }
}
