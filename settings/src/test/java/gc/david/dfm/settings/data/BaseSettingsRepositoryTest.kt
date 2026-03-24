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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class BaseSettingsRepositoryTest {

    private val localDataSource = mock<SettingsDiskDataSource>()
    private val repository = BaseSettingsRepository(localDataSource)

    @Test
    fun `shouldShowElevationChart returns true when enabled`() {
        whenever(localDataSource.shouldShowElevationChart()).thenReturn(true)

        val result = repository.shouldShowElevationChart()

        assertTrue(result)
        verify(localDataSource).shouldShowElevationChart()
    }

    @Test
    fun `shouldShowElevationChart returns false when disabled`() {
        whenever(localDataSource.shouldShowElevationChart()).thenReturn(false)

        val result = repository.shouldShowElevationChart()

        assertFalse(result)
    }

    @Test
    fun `getUnitSystemPreference returns IMPERIAL for American unit value`() {
        whenever(localDataSource.getMeasureUnitPreference())
            .thenReturn(SettingsDiskDataSource.MEASURE_AMERICAN_UNIT_VALUE)

        val result = repository.getUnitSystemPreference()

        assertEquals(UnitSystem.IMPERIAL, result)
    }

    @Test
    fun `getUnitSystemPreference returns METRIC for European unit value`() {
        whenever(localDataSource.getMeasureUnitPreference())
            .thenReturn(SettingsDiskDataSource.MEASURE_EUROPEAN_UNIT_VALUE)

        val result = repository.getUnitSystemPreference()

        assertEquals(UnitSystem.METRIC, result)
    }

    @Test
    fun `getUnitSystemPreference returns METRIC for unknown value`() {
        whenever(localDataSource.getMeasureUnitPreference()).thenReturn("unknown")

        val result = repository.getUnitSystemPreference()

        assertEquals(UnitSystem.METRIC, result)
    }

    @Test
    fun `setUnitSystemPreference sets American value for IMPERIAL`() {
        repository.setUnitSystemPreference(UnitSystem.IMPERIAL)

        verify(localDataSource).setMeasureUnitPreference(
            SettingsDiskDataSource.MEASURE_AMERICAN_UNIT_VALUE
        )
    }

    @Test
    fun `setUnitSystemPreference sets European value for METRIC`() {
        repository.setUnitSystemPreference(UnitSystem.METRIC)

        verify(localDataSource).setMeasureUnitPreference(
            SettingsDiskDataSource.MEASURE_EUROPEAN_UNIT_VALUE
        )
    }

    @Test
    fun `getCameraAnimation returns Destination animation`() {
        whenever(localDataSource.getAnimationPreference())
            .thenReturn(SettingsDiskDataSource.ANIMATION_DESTINATION_VALUE)

        val result = repository.getCameraAnimation()

        assertEquals(CameraAnimation.Animate.Destination, result)
    }

    @Test
    fun `getCameraAnimation returns Centre animation`() {
        whenever(localDataSource.getAnimationPreference())
            .thenReturn(SettingsDiskDataSource.ANIMATION_CENTRE_VALUE)

        val result = repository.getCameraAnimation()

        assertEquals(CameraAnimation.Animate.Centre, result)
    }

    @Test
    fun `getCameraAnimation returns None for unknown value`() {
        whenever(localDataSource.getAnimationPreference()).thenReturn("unknown")

        val result = repository.getCameraAnimation()

        assertEquals(CameraAnimation.None, result)
    }

    @Test
    fun `getCameraAnimation returns None for null value`() {
        whenever(localDataSource.getAnimationPreference()).thenReturn(null)

        val result = repository.getCameraAnimation()

        assertEquals(CameraAnimation.None, result)
    }
}

