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
import gc.david.dfm.onboarding.data.BaseHintRepository
import gc.david.dfm.onboarding.data.HintDiskDataSource
import gc.david.dfm.onboarding.domain.model.MapHint
import gc.david.dfm.onboarding.domain.model.MapHintEvent
import gc.david.dfm.onboarding.domain.model.MapSituation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.kotlin.mock

@OptIn(ExperimentalCoroutinesApi::class)
class MapHintProcessorTest {

    private val hintRepository = BaseHintRepository(mock<HintDiskDataSource>())
    private val processor = MapHintProcessor(hintRepository)

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

    @ParameterizedTest
    @EnumSource(MapHint::class, names = ["WAITING_FOR_LOCATION"], mode = EnumSource.Mode.EXCLUDE)
    fun `a closed hint is not shown again`(hint: MapHint) = runTest {
        processor.onEvent(MapHintEvent.HintClosed(hint))

        assertEquals(setOf(hint), seenHints())
        assertEquals(listOf(null), hintsFor(situationOf(hint)))
    }

    @Test
    fun `waiting for the location is shown whenever it applies`() = runTest {
        processor.onEvent(MapHintEvent.HintClosed(MapHint.WAITING_FOR_LOCATION))

        assertEquals(emptySet<MapHint>(), seenHints())
        assertEquals(listOf(MapHint.WAITING_FOR_LOCATION), hintsFor(situationOf(MapHint.WAITING_FOR_LOCATION)))
    }

    @Test
    fun `a hint goes away as soon as it is closed`() = runTest {
        val hints = mutableListOf<MapHint?>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            processor.observeHint(MutableStateFlow(situationOf(MapHint.START_ANYWHERE))).toList(hints)
        }

        processor.onEvent(MapHintEvent.HintClosed(MapHint.START_ANYWHERE))

        assertEquals(listOf(MapHint.START_ANYWHERE, null), hints)
    }

    @ParameterizedTest
    @EnumSource(MapHint::class)
    fun `a tap only does what the starting hint says`(hint: MapHint) = runTest {
        processor.onEvent(MapHintEvent.MapTapped(situationOf(hint)))

        assertEquals(setOf(MapHint.START_ANYWHERE).intersect(setOf(hint)), seenHints())
    }

    @ParameterizedTest
    @EnumSource(MapHint::class)
    fun `a long press does what the measuring and finishing hints say`(hint: MapHint) = runTest {
        processor.onEvent(MapHintEvent.MapLongPressed(situationOf(hint)))

        assertEquals(
            setOf(MapHint.MEASURE_FROM_LOCATION, MapHint.ADD_OR_FINISH).intersect(setOf(hint)),
            seenHints()
        )
    }

    @Test
    fun `presses without any hint mark nothing`() = runTest {
        val noHint =
            situation(DistanceMode.FROM_CURRENT_POINT, isLocationPermissionGranted = false, hasLocation = false)

        processor.onEvent(MapHintEvent.MapTapped(noHint))
        processor.onEvent(MapHintEvent.MapLongPressed(noHint))

        assertEquals(emptySet<MapHint>(), seenHints())
    }

    @Test
    fun `opening the details does what its hint says`() = runTest {
        processor.onEvent(MapHintEvent.DetailsOpened)

        assertEquals(setOf(MapHint.OPEN_DETAILS), seenHints())
    }

    private suspend fun seenHints() = hintRepository.observeSeenHints().first()

    private fun situationOf(hint: MapHint) =
        when (hint) {
            MapHint.WAITING_FOR_LOCATION -> situation(DistanceMode.FROM_CURRENT_POINT, hasLocation = false)
            MapHint.MEASURE_FROM_LOCATION -> situation(DistanceMode.FROM_CURRENT_POINT)
            MapHint.START_ANYWHERE -> situation(DistanceMode.FROM_ANY_POINT)
            MapHint.ADD_OR_FINISH -> situation(DistanceMode.FROM_ANY_POINT, hasPoints = true)
            MapHint.OPEN_DETAILS ->
                situation(DistanceMode.FROM_ANY_POINT, hasPoints = true, isDistanceDrawn = true)
        }

    // The seen hints can always change, so the hints never complete: collect them in the background
    private suspend fun TestScope.hintsFor(vararg situations: MapSituation): List<MapHint?> {
        val hints = mutableListOf<MapHint?>()
        val situation = MutableSharedFlow<MapSituation>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            processor.observeHint(situation).toList(hints)
        }
        situations.forEach {
            situation.emit(it)
            runCurrent()
        }
        return hints
    }

    private fun situation(
        distanceMode: DistanceMode,
        isLocationPermissionGranted: Boolean = true,
        hasLocation: Boolean = true,
        hasPoints: Boolean = false,
        isDistanceDrawn: Boolean = false
    ) = MapSituation(distanceMode, isLocationPermissionGranted, hasLocation, hasPoints, isDistanceDrawn)
}
