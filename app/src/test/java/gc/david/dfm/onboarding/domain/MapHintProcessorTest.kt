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

package gc.david.dfm.onboarding.domain

import gc.david.dfm.distance.domain.model.DistanceMode
import gc.david.dfm.onboarding.domain.model.MapHint
import gc.david.dfm.onboarding.domain.model.MapSituation
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class MapHintProcessorTest {

    private val processor = MapHintProcessor()

    @Test
    fun `waits for the location in current position mode`() = runTest {
        assertEquals(
            listOf(MapHint.WAITING_FOR_LOCATION),
            hintsFor(situation(DistanceMode.FROM_CURRENT_POINT, hasLocation = false))
        )
    }

    @Test
    fun `measures from the location in current position mode`() = runTest {
        assertEquals(
            listOf(MapHint.MEASURE_FROM_LOCATION),
            hintsFor(situation(DistanceMode.FROM_CURRENT_POINT, hasLocation = true))
        )
    }

    @Test
    fun `has no hint in current position mode without permission`() = runTest {
        assertEquals(
            listOf(null),
            hintsFor(
                situation(DistanceMode.FROM_CURRENT_POINT, isLocationPermissionGranted = false, hasLocation = false)
            )
        )
    }

    @Test
    fun `starts anywhere in any position mode, also without permission or location`() = runTest {
        assertEquals(
            listOf(MapHint.START_ANYWHERE),
            hintsFor(situation(DistanceMode.FROM_ANY_POINT, isLocationPermissionGranted = false, hasLocation = false))
        )
    }

    @ParameterizedTest
    @EnumSource(DistanceMode::class)
    fun `adds or finishes while points are being selected`(distanceMode: DistanceMode) = runTest {
        assertEquals(listOf(MapHint.ADD_OR_FINISH), hintsFor(situation(distanceMode, hasPoints = true)))
    }

    @ParameterizedTest
    @EnumSource(DistanceMode::class)
    fun `opens the details once a distance is drawn`(distanceMode: DistanceMode) = runTest {
        assertEquals(
            listOf(MapHint.OPEN_DETAILS),
            hintsFor(situation(distanceMode, hasPoints = true, isDistanceDrawn = true))
        )
    }

    @Test
    fun `follows the situation without repeating a hint`() = runTest {
        val empty = situation(DistanceMode.FROM_ANY_POINT)
        val selecting = empty.copy(hasPoints = true)

        assertEquals(
            listOf(MapHint.START_ANYWHERE, MapHint.ADD_OR_FINISH, MapHint.START_ANYWHERE),
            hintsFor(empty, selecting, selecting.copy(hasLocation = false), empty)
        )
    }

    private suspend fun hintsFor(vararg situations: MapSituation): List<MapHint?> =
        processor.observeHint(flowOf(*situations)).toList()

    private fun situation(
        distanceMode: DistanceMode,
        isLocationPermissionGranted: Boolean = true,
        hasLocation: Boolean = true,
        hasPoints: Boolean = false,
        isDistanceDrawn: Boolean = false
    ) = MapSituation(distanceMode, isLocationPermissionGranted, hasLocation, hasPoints, isDistanceDrawn)
}
