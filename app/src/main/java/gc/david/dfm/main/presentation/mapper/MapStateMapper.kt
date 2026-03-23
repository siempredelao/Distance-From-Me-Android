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

package gc.david.dfm.main.presentation.mapper

import android.content.Context
import gc.david.dfm.R
import gc.david.dfm.distance.data.model.DistanceMode
import gc.david.dfm.main.presentation.model.CameraUpdate
import gc.david.dfm.main.presentation.model.DrawDistanceUiModel
import gc.david.dfm.main.presentation.model.LineColor
import gc.david.dfm.main.presentation.model.MapUiState
import gc.david.dfm.main.presentation.model.MarkerData
import gc.david.dfm.main.presentation.model.PolylineData
import gc.david.dfm.settings.domain.model.CameraAnimation

/**
 * Maps DrawDistanceModel (presentation model) to MapUiState (rendering model).
 * Separates business logic from rendering logic.
 */
class MapStateMapper(private val context: Context) {

    /**
     * Transforms a DrawDistanceModel into a MapUiState ready for rendering.
     */
    fun toMapUiState(model: DrawDistanceUiModel): MapUiState {
        val markers = createMarkers(model)
        val polylines = createPolylines(model)
        val cameraUpdate = createCameraUpdate(model)

        return MapUiState(
            markers = markers,
            polylines = polylines,
            cameraUpdate = cameraUpdate,
            clearMap = true
        )
    }

    private fun createMarkers(model: DrawDistanceUiModel): List<MarkerData> {
        val coordinates = model.positionList
        if (coordinates.isEmpty()) return emptyList()

        return buildList {
            coordinates.forEachIndexed { index, coordinate ->
                // Show marker at start (for DATABASE or FROM_ANY_POINT) or at end
                val shouldShowMarker =
                    (index == 0 && (model.source == DrawDistanceUiModel.Source.DATABASE ||
                            model.distanceMode == DistanceMode.FROM_ANY_POINT)) ||
                            index == coordinates.size - 1

                if (shouldShowMarker) {
                    val isLastMarker = index == coordinates.size - 1
                    add(
                        MarkerData(
                            position = coordinate,
                            title = if (isLastMarker) model.distanceName + model.formattedDistance else null,
                            showInfoWindow = isLastMarker
                        )
                    )
                }
            }
        }
    }

    private fun createPolylines(model: DrawDistanceUiModel): List<PolylineData> {
        val coordinates = model.positionList
        if (coordinates.size < 2) return emptyList()

        val lineWidth = context.resources.getDimension(R.dimen.map_line_width)
        val color = when (model.source) {
            DrawDistanceUiModel.Source.MANUAL -> LineColor.GREEN
            DrawDistanceUiModel.Source.DATABASE -> LineColor.YELLOW
        }

        return buildList {
            for (i in 0 until coordinates.lastIndex) {
                add(
                    PolylineData(
                        start = coordinates[i],
                        end = coordinates[i + 1],
                        color = color,
                        width = lineWidth
                    )
                )
            }
        }
    }

    private fun createCameraUpdate(model: DrawDistanceUiModel): CameraUpdate? {
        if (model.positionList.isEmpty()) return null

        if (model.cameraAnimation !is CameraAnimation.Animate) return null

        return when (model.cameraAnimation) {
            is CameraAnimation.Animate.Centre -> {
                CameraUpdate.FitBounds(
                    coordinates = model.positionList,
                    padding = 100
                )
            }

            is CameraAnimation.Animate.Destination -> {
                CameraUpdate.MoveTo(position = model.positionList.last())
            }
        }
    }
}
