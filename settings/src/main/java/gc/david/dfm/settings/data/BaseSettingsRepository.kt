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

package gc.david.dfm.settings.data

import gc.david.dfm.common.domain.model.UnitSystem
import gc.david.dfm.settings.domain.model.CameraAnimation
import gc.david.dfm.settings.domain.SettingsRepository

class BaseSettingsRepository(
    private val localDataSource: SettingsDiskDataSource
) : SettingsRepository {

    override fun shouldShowElevationChart(): Boolean =
        localDataSource.shouldShowElevationChart()

    override fun getUnitSystemPreference(): UnitSystem {
        val value = localDataSource.getMeasureUnitPreference()
        return if (value == SettingsDiskDataSource.MEASURE_AMERICAN_UNIT_VALUE)
            UnitSystem.IMPERIAL else UnitSystem.METRIC
    }

    override fun setUnitSystemPreference(unitSystem: UnitSystem) {
        val value = when (unitSystem) {
            UnitSystem.IMPERIAL -> SettingsDiskDataSource.MEASURE_AMERICAN_UNIT_VALUE
            UnitSystem.METRIC -> SettingsDiskDataSource.MEASURE_EUROPEAN_UNIT_VALUE
        }
        localDataSource.setMeasureUnitPreference(value)
    }

    override fun getCameraAnimation(): CameraAnimation =
        when (localDataSource.getAnimationPreference()) {
            SettingsDiskDataSource.ANIMATION_DESTINATION_VALUE -> CameraAnimation.Animate.Destination
            SettingsDiskDataSource.ANIMATION_CENTRE_VALUE -> CameraAnimation.Animate.Centre
            else -> CameraAnimation.None
        }
}