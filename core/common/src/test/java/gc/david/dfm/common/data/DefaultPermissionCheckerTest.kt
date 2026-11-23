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

package gc.david.dfm.common.data

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.content.pm.PackageManager.PERMISSION_GRANTED
import androidx.core.content.ContextCompat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.mock

class DefaultPermissionCheckerTest {

    private val context = mock<Context>()
    private val checker = DefaultPermissionChecker(context)

    @ParameterizedTest(name = "fine={0}, coarse={1} -> granted={2}")
    @CsvSource(
        "true, true, true",
        "true, false, true",
        "false, true, true",
        "false, false, false",
    )
    fun `location counts as granted when precise or approximate location is`(
        fine: Boolean,
        coarse: Boolean,
        expected: Boolean,
    ) {
        mockStatic(ContextCompat::class.java).use { contextCompat ->
            contextCompat.`when`<Int> { ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) }
                .thenReturn(fine.toPermissionResult())
            contextCompat.`when`<Int> { ContextCompat.checkSelfPermission(context, ACCESS_COARSE_LOCATION) }
                .thenReturn(coarse.toPermissionResult())

            assertEquals(expected, checker.isLocationPermissionGranted())
        }
    }

    private fun Boolean.toPermissionResult() = if (this) PERMISSION_GRANTED else PERMISSION_DENIED
}
