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

package gc.david.dfm.main.presentation

import android.graphics.Color
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import gc.david.dfm.common.Coordinates
import gc.david.dfm.main.presentation.model.CameraUpdate
import gc.david.dfm.main.presentation.model.LineColor
import gc.david.dfm.main.presentation.model.MapUiState
import gc.david.dfm.main.presentation.model.MarkerData
import gc.david.dfm.main.presentation.model.PolylineData

/**
 * Pure renderer for GoogleMap.
 * Takes MapUiState and renders it into GoogleMap canvas.
 * Contains no business logic, only rendering commands.
 */
class MapRenderer {

    /**
     * Renders a MapUiState into the GoogleMap.
     * This is a pure function: same state always produces same rendering.
     */
    fun render(googleMap: GoogleMap, state: MapUiState) {
        if (state.clearMap) {
            googleMap.clear()
        }

        state.markers.forEach { markerData ->
            renderMarker(googleMap, markerData)
        }

        state.polylines.forEach { polylineData ->
            renderPolyline(googleMap, polylineData)
        }

        state.cameraUpdate?.let { cameraUpdate ->
            applyCameraUpdate(googleMap, cameraUpdate)
        }
    }

    private fun renderMarker(googleMap: GoogleMap, markerData: MarkerData) {
        val marker = googleMap.addMarker(
            MarkerOptions().position(markerData.position.toLatLng())
        ) ?: return

        markerData.title?.let { marker.title = it }
        if (markerData.showInfoWindow) {
            marker.showInfoWindow()
        }
    }

    private fun renderPolyline(googleMap: GoogleMap, polylineData: PolylineData) {
        val options = PolylineOptions()
            .add(polylineData.start.toLatLng())
            .add(polylineData.end.toLatLng())
            .width(polylineData.width)
            .color(polylineData.color.toAndroidColor())

        googleMap.addPolyline(options)
    }

    private fun applyCameraUpdate(googleMap: GoogleMap, cameraUpdate: CameraUpdate) {
        val update = when (cameraUpdate) {
            is CameraUpdate.FitBounds -> {
                val boundsBuilder = LatLngBounds.Builder()
                cameraUpdate.coordinates.forEach {
                    boundsBuilder.include(it.toLatLng())
                }
                CameraUpdateFactory.newLatLngBounds(
                    boundsBuilder.build(),
                    cameraUpdate.padding
                )
            }
            is CameraUpdate.MoveTo -> {
                CameraUpdateFactory.newLatLng(cameraUpdate.position.toLatLng())
            }
            is CameraUpdate.ZoomTo -> {
                CameraUpdateFactory.newLatLngZoom(
                    cameraUpdate.position.toLatLng(),
                    cameraUpdate.zoom
                )
            }
        }
        googleMap.animateCamera(update)
    }

    private fun LineColor.toAndroidColor(): Int {
        return when (this) {
            LineColor.GREEN -> Color.GREEN
            LineColor.YELLOW -> Color.YELLOW
        }
    }
}

private fun Coordinates.toLatLng() = LatLng(latitude, longitude)
