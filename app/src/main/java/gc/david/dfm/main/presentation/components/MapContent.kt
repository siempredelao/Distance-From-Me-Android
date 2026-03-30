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

package gc.david.dfm.main.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import gc.david.dfm.common.Coordinates
import gc.david.dfm.main.presentation.model.CameraUpdate
import gc.david.dfm.main.presentation.model.MapUiState
import gc.david.dfm.main.presentation.model.MarkerData
import gc.david.dfm.main.presentation.model.PolylineData
import kotlinx.coroutines.delay

/**
 * Google Map content with declarative rendering of markers and polylines.
 * Uses maps-compose composables for reactive map rendering.
 */
@Composable
fun MapContent(
    mapState: MapUiState,
    onMapClick: (LatLng) -> Unit,
    onMapLongClick: (LatLng) -> Unit,
    onMarkerClick: (MarkerData) -> Unit,
    modifier: Modifier = Modifier,
    properties: MapProperties,
    uiSettings: MapUiSettings,
) {
    val cameraPositionState = rememberCameraPositionState()

    // Handle camera updates
    LaunchedEffect(mapState.cameraUpdate) {
        mapState.cameraUpdate?.let { cameraUpdate ->
            cameraPositionState.animate(cameraUpdate.toCameraUpdate())
        }
    }

    // Find the marker with visible info window that matches a polyline end coordinate
    val markerToShowInfoWindow = remember(mapState.markers, mapState.polylines) {
        mapState.markers.find { marker ->
            if (marker.infoWindow is MarkerData.InfoWindow.Visible) {
                // Check if this marker's position matches any polyline's end coordinate
                mapState.polylines.any { polyline ->
                    polyline.end == marker.position
                }
            } else {
                false
            }
        }
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = properties,
        uiSettings = uiSettings,
        onMapClick = onMapClick,
        onMapLongClick = onMapLongClick,
    ) {
        // Render polylines
        Polylines(mapState.polylines)

        // Render markers with info windows
        Markers(
            markers = mapState.markers,
            markerToShowInfoWindow = markerToShowInfoWindow,
            onMarkerClick = onMarkerClick
        )
    }
}

@Composable
private fun Polylines(list: List<PolylineData>) {
    list.forEach { polylineData ->
        Polyline(
            points = listOf(
                polylineData.start.toLatLng(),
                polylineData.end.toLatLng()
            ),
            color = polylineData.color.toComposeColor(),
            width = polylineData.width
        )
    }
}

@Composable
private fun Markers(
    markers: List<MarkerData>,
    markerToShowInfoWindow: MarkerData?,
    onMarkerClick: (MarkerData) -> Unit
) {
    markers.forEach { markerData ->
        // Use a unique key based on position for stable marker identity
        val markerKey = "${markerData.position.latitude},${markerData.position.longitude}"
        
        key(markerKey) {
            val markerState = rememberMarkerState(
                position = markerData.position.toLatLng()
            )

            // Extract title from InfoWindow
            val title = when (val infoWindow = markerData.infoWindow) {
                is MarkerData.InfoWindow.Visible -> infoWindow.title
                MarkerData.InfoWindow.None -> null
            }
            
            // Check if this marker should show info window
            val shouldShowInfoWindow = markerToShowInfoWindow?.position == markerData.position &&
                                      markerData.infoWindow is MarkerData.InfoWindow.Visible
            
            LaunchedEffect(shouldShowInfoWindow) {
                if (shouldShowInfoWindow) {
                    // Add delay to ensure marker is fully rendered
                    delay(300)
                    markerState.showInfoWindow()
                }
            }
            
            Marker(
                state = markerState,
                title = title,
                onInfoWindowClick = { onMarkerClick(markerData) }
            )
        }
    }
}

/**
 * Extension function to convert Coordinates to LatLng
 */
private fun Coordinates.toLatLng() = LatLng(latitude, longitude)

/**
 * Extension function to convert CameraUpdate to Google Maps CameraUpdate
 */
private fun CameraUpdate.toCameraUpdate(): com.google.android.gms.maps.CameraUpdate {
    return when (this) {
        is CameraUpdate.FitBounds -> {
            val boundsBuilder = LatLngBounds.Builder()
            coordinates.forEach { coordinate ->
                boundsBuilder.include(coordinate.toLatLng())
            }
            CameraUpdateFactory.newLatLngBounds(
                boundsBuilder.build(),
                padding
            )
        }
        is CameraUpdate.MoveTo -> {
            CameraUpdateFactory.newLatLng(position.toLatLng())
        }
        is CameraUpdate.ZoomTo -> {
            CameraUpdateFactory.newLatLngZoom(
                position.toLatLng(),
                zoom
            )
        }
    }
}

/**
 * Extension function to convert PolylineData.LineColor to Compose Color
 */
private fun PolylineData.LineColor.toComposeColor(): Color {
    return when (this) {
        PolylineData.LineColor.GREEN -> Color.Green
        PolylineData.LineColor.YELLOW -> Color.Yellow
    }
}

@Preview(showBackground = true)
@Composable
private fun MapContentPreview() {
    // Note: Run the preview on the emulator/device to see the actual preview
    val position = Coordinates(40.7128, -74.0060)

    MapContent(
        mapState = MapUiState(
            markers = listOf(
                MarkerData(
                    position = position,
                    infoWindow = MarkerData.InfoWindow.Visible(title = "New York City")
                )
            ),
            polylines = emptyList(),
            clearMap = false,
            cameraUpdate = CameraUpdate.ZoomTo(position = position)
        ),
        onMapClick = {},
        onMapLongClick = {},
        onMarkerClick = {},
        modifier = Modifier,
        properties = MapProperties(),
        uiSettings = MapUiSettings(zoomControlsEnabled = false)
    )
}

