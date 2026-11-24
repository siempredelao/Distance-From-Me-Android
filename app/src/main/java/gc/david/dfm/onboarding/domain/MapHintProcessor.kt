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
import gc.david.dfm.onboarding.domain.model.MapHintEvent
import gc.david.dfm.onboarding.domain.model.MapSituation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Decides what the map tells the user about its next step, and when a hint has nothing more to teach.
 *
 * The hint follows from the situation rather than from the events that led to it, so it can't disagree with what is on
 * the map. The events only retire hints: one the user closed, or whose advice they followed, isn't shown again.
 */
class MapHintProcessor(private val hintRepository: HintRepository) {

    /** The hint for each [situation], or null when there is nothing to say. */
    fun observeHint(situation: Flow<MapSituation>): Flow<MapHint?> =
        combine(situation, hintRepository.observeSeenHints()) { current, seen ->
            current.toHint()?.takeUnless { it in seen }
        }.distinctUntilChanged()

    fun onEvent(event: MapHintEvent) {
        when (event) {
            is MapHintEvent.HintClosed -> retire(event.hint)
            is MapHintEvent.MapTapped -> retireHint(event.situation, TAP_HINTS)
            is MapHintEvent.MapLongPressed -> retireHint(event.situation, LONG_PRESS_HINTS)
            MapHintEvent.DetailsOpened -> retire(MapHint.OPEN_DETAILS)
        }
    }

    // Retires the hint for the situation if it's one of those that the gesture follows
    private fun retireHint(situation: MapSituation, followedHints: Set<MapHint>) {
        situation.toHint()?.takeIf { it in followedHints }?.let(::retire)
    }

    private fun retire(hint: MapHint) {
        // It tells what the app is doing, rather than teaching a gesture, so it's shown whenever it applies
        if (hint != MapHint.WAITING_FOR_LOCATION) {
            hintRepository.markSeen(hint)
        }
    }

    private fun MapSituation.toHint(): MapHint? =
        when {
            isDistanceDrawn -> MapHint.OPEN_DETAILS
            hasPoints -> MapHint.ADD_OR_FINISH
            else -> when (distanceMode) {
                DistanceMode.FROM_ANY_POINT -> MapHint.START_ANYWHERE
                // Without permission there is no location to wait for
                DistanceMode.FROM_CURRENT_POINT if !isLocationPermissionGranted -> null
                DistanceMode.FROM_CURRENT_POINT if !hasLocation -> MapHint.WAITING_FOR_LOCATION
                DistanceMode.FROM_CURRENT_POINT -> MapHint.MEASURE_FROM_LOCATION
            }
        }

    private companion object {

        // The hints whose advice a tap follows
        val TAP_HINTS = setOf(MapHint.START_ANYWHERE)

        // The hints whose advice a long press follows
        val LONG_PRESS_HINTS = setOf(MapHint.MEASURE_FROM_LOCATION, MapHint.ADD_OR_FINISH)
    }
}
