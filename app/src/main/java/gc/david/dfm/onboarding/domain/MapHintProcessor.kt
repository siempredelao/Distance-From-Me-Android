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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Decides what the map tells the user about its next step.
 *
 * The hint follows from the situation alone rather than from the events that led to it, so it can't disagree with what
 * is on the map.
 */
class MapHintProcessor {

    /** The hint for each [situation], or null when there is nothing to say. */
    fun observeHint(situation: Flow<MapSituation>): Flow<MapHint?> =
        situation.map { it.toHint() }.distinctUntilChanged()

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
}
