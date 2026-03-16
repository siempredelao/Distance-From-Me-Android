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

package gc.david.dfm.ui.activity

import android.content.Context
import android.graphics.Color
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import gc.david.dfm.R
import gc.david.dfm.common.Coordinates
import gc.david.dfm.distance.data.DistanceMode
import gc.david.dfm.main.presentation.model.DrawDistanceModel
import gc.david.dfm.settings.domain.model.CameraAnimation

/**
 * Class to take a [DrawDistanceModel] and render it into a GoogleMap canvas.
 */
class MapDrawer(private val context: Context) {

    fun drawDistance(googleMap: GoogleMap, model: DrawDistanceModel) {
        googleMap.clear()

        addMarkers(
            googleMap,
            model.positionList,
            model.formattedDistance,
            model.distanceName,
            model.source,
            model.distanceMode
        )

        addLines(googleMap, model.positionList, model.source)

        if (model.positionList.isNotEmpty() && model.cameraAnimation is CameraAnimation.Animate) {
            moveCameraZoom(googleMap, model.positionList, model.cameraAnimation)
        }
    }

    private fun addMarkers(
        googleMap: GoogleMap,
        coordinates: List<Coordinates>,
        distance: String,
        message: String,
        source: DrawDistanceModel.Source,
        selectedDistanceMode: DistanceMode
    ) {
        for (i in coordinates.indices) {
            if (i == 0
                && (source == DrawDistanceModel.Source.DATABASE || selectedDistanceMode == DistanceMode.FROM_ANY_POINT)
                || i == coordinates.size - 1
            ) {
                val coordinate = coordinates[i]
                val marker = addMarker(googleMap, coordinate.toLatLng())

                if (coordinates.isNotEmpty() && i == coordinates.size - 1) {
                    marker.title = message + distance
                    marker.showInfoWindow()
                }
            }
        }
    }

    private fun addMarker(googleMap: GoogleMap, position: LatLng): Marker {
        return googleMap.addMarker(MarkerOptions().position(position)) ?: error("Unable to add marker")
    }

    private fun addLines(
        googleMap: GoogleMap,
        positionList: List<Coordinates>,
        source: DrawDistanceModel.Source
    ) {
        for (i in 0 until positionList.size - 1) {
            addLine(googleMap, positionList[i].toLatLng(), positionList[i + 1].toLatLng(), source)
        }
    }

    private fun addLine(
        googleMap: GoogleMap,
        start: LatLng,
        end: LatLng,
        source: DrawDistanceModel.Source
    ) {
        val lineOptions = PolylineOptions().add(start).add(end)
        lineOptions.width(context.resources.getDimension(R.dimen.map_line_width))
        val color = when (source) {
            DrawDistanceModel.Source.MANUAL -> Color.GREEN
            DrawDistanceModel.Source.DATABASE -> Color.YELLOW
        }
        lineOptions.color(color)
        googleMap.addPolyline(lineOptions)
    }

    private fun moveCameraZoom(
        googleMap: GoogleMap,
        coordinatesList: List<Coordinates>,
        cameraAnimation: CameraAnimation.Animate
    ) {
        when (cameraAnimation) {
            is CameraAnimation.Animate.Centre -> {
                val latLngBoundsBuilder = LatLngBounds.Builder()
                coordinatesList.forEach { latLngBoundsBuilder.include(it.toLatLng()) }
                googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngBounds(
                        latLngBoundsBuilder.build(),
                        100
                    )
                )
            }
            is CameraAnimation.Animate.Destination -> {
                val lastCoordinate = coordinatesList.last()
                googleMap.animateCamera(CameraUpdateFactory.newLatLng(lastCoordinate.toLatLng()))
            }
        }
    }
}

private fun Coordinates.toLatLng() = LatLng(latitude, longitude)
