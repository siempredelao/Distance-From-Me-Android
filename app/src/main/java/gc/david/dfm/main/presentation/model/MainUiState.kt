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

package gc.david.dfm.main.presentation.model

import gc.david.dfm.common.Coordinate
import gc.david.dfm.core.distances.data.database.Distance

data class MainUiState(
    val showLoadDistancesItem: Boolean = false,
    val showForceCrashItem: Boolean = false,
    val drawDistance: DrawDistanceModel? = null,
    val drawPoints: List<Coordinate>? = null,
    val showConnectionIssue: Boolean = false,
    val errorMessage: String? = null,
    val selectFromDistancesLoaded: List<Distance>? = null,
    val zoomMapInto: Coordinate? = null,
    val centerMapInto: Coordinate? = null,
    val searchAddress: String? = null,
    val resetMap: Boolean = false,
    val hideChart: Boolean = false,
    val showLocationPermissionSnackbar: Boolean = false,
    val openShowInfo: DrawDistanceModel? = null,
)
