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

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * UI state for map rendering.
 * Contains all information needed to render markers, polylines, and camera position.
 * This is a pure data model without any rendering logic.
 */
data class MapUiState(
    val markers: ImmutableList<MarkerData> = persistentListOf(),
    val polylines: ImmutableList<PolylineData> = persistentListOf(),
    val cameraUpdate: CameraUpdate? = null,
    val clearMap: Boolean = false
)

/**
 * Data for rendering a marker on the map.
 */
data class MarkerData(
    val position: CoordinatesUiModel,
    val infoWindow: InfoWindow = InfoWindow.None
) {
    
    /**
     * Info window state for a marker.
     */
    sealed interface InfoWindow {
        
        data object None : InfoWindow
        
        /** The [snippet] is a second, smaller line below the [title]. Each of them is cut off after one line. */
        data class Visible(val title: String, val snippet: String? = null) : InfoWindow
    }
}

/**
 * Data for rendering a polyline on the map.
 */
data class PolylineData(
    val start: CoordinatesUiModel,
    val end: CoordinatesUiModel,
    val color: LineColor,
    val width: Float
) {

    /**
     * Color options for polylines.
     */
    enum class LineColor {
        GREEN,   // For manual distances
        YELLOW   // For database distances
    }
}

/**
 * Camera update instructions.
 */
sealed interface CameraUpdate {

    data class ZoomTo(val position: CoordinatesUiModel, val zoom: Float = 17f) : CameraUpdate

    data class MoveTo(val position: CoordinatesUiModel) : CameraUpdate

    data class FitBounds(val coordinates: ImmutableList<CoordinatesUiModel>, val padding: Int) : CameraUpdate
}
