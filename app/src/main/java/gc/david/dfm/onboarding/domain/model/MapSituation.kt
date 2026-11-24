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

package gc.david.dfm.onboarding.domain.model

import gc.david.dfm.distance.domain.model.DistanceMode

/**
 * What the user sees on the map, as far as the hints need to know.
 */
data class MapSituation(
    val distanceMode: DistanceMode,
    val isLocationPermissionGranted: Boolean,
    val hasLocation: Boolean,
    val hasPoints: Boolean,
    val isDistanceDrawn: Boolean,
)
