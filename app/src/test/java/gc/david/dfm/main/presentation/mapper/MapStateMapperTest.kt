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
import android.content.res.Resources
import gc.david.dfm.R
import gc.david.dfm.common.Coordinates
import gc.david.dfm.distance.data.model.DistanceMode
import gc.david.dfm.main.presentation.model.CameraUpdate
import gc.david.dfm.main.presentation.model.DrawDistanceUiModel
import gc.david.dfm.main.presentation.model.MapUiState
import gc.david.dfm.main.presentation.model.MarkerData
import gc.david.dfm.main.presentation.model.PolylineData
import gc.david.dfm.settings.domain.model.CameraAnimation
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class MapStateMapperTest {

    private val context = mock<Context>()
    private val resources = mock<Resources>()
    private val mapper = MapStateMapper(context)

    @BeforeEach
    fun setUp() {
        whenever(context.resources).thenReturn(resources)
        whenever(resources.getDimension(R.dimen.map_line_width)).thenReturn(LINE_WIDTH)
    }

    @Test
    fun `creates an empty state for empty coordinates`() {
        val model = createDrawDistanceModel(coordinates = emptyList())

        val actual = mapper.toMapUiState(model)

        val expected = MapUiState(
            markers = emptyList(),
            polylines = emptyList(),
            cameraUpdate = null,
            clearMap = true
        )
        assertEquals(expected, actual)
    }

    // TODO add previous behavior where only the end point had a marker?
    @Test
    fun `creates markers at start and end for FROM_CURRENT_POINT mode`() {
        val coords = listOf(
            Coordinates(0.0, 0.0),
            Coordinates(1.0, 1.0),
            Coordinates(2.0, 2.0)
        )
        val model = createDrawDistanceModel(
            coordinates = coords,
            source = DrawDistanceUiModel.Source.DATABASE,
            distanceMode = DistanceMode.FROM_CURRENT_POINT,
            cameraAnimation = CameraAnimation.None
        )

        val actual = mapper.toMapUiState(model)

        val expected = MapUiState(
            markers = listOf(
                MarkerData(
                    position = Coordinates(0.0, 0.0),
                    infoWindow = MarkerData.InfoWindow.None
                ),
                MarkerData(
                    position = Coordinates(2.0, 2.0),
                    infoWindow = MarkerData.InfoWindow.Visible(title = model.distanceName + model.formattedDistance)
                )
            ),
            polylines = listOf(
                PolylineData(
                    start = Coordinates(0.0, 0.0),
                    end = Coordinates(1.0, 1.0),
                    color = PolylineData.LineColor.YELLOW,
                    width = LINE_WIDTH
                ),
                PolylineData(
                    start = Coordinates(1.0, 1.0),
                    end = Coordinates(2.0, 2.0),
                    color = PolylineData.LineColor.YELLOW,
                    width = LINE_WIDTH
                )
            ),
            cameraUpdate = null,
            clearMap = true
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `creates markers at start and end for FROM_ANY_POINT mode`() {
        val coords = listOf(
            Coordinates(0.0, 0.0),
            Coordinates(1.0, 1.0),
            Coordinates(2.0, 2.0)
        )
        val model = createDrawDistanceModel(
            coordinates = coords,
            source = DrawDistanceUiModel.Source.MANUAL,
            distanceMode = DistanceMode.FROM_ANY_POINT,
            cameraAnimation = CameraAnimation.None
        )

        val actual = mapper.toMapUiState(model)

        val expected = MapUiState(
            markers = listOf(
                MarkerData(
                    position = Coordinates(0.0, 0.0),
                    infoWindow = MarkerData.InfoWindow.None
                ),
                MarkerData(
                    position = Coordinates(2.0, 2.0),
                    infoWindow = MarkerData.InfoWindow.Visible(title = model.distanceName + model.formattedDistance)
                )
            ),
            polylines = listOf(
                PolylineData(
                    start = Coordinates(0.0, 0.0),
                    end = Coordinates(1.0, 1.0),
                    color = PolylineData.LineColor.GREEN,
                    width = LINE_WIDTH
                ),
                PolylineData(
                    start = Coordinates(1.0, 1.0),
                    end = Coordinates(2.0, 2.0),
                    color = PolylineData.LineColor.GREEN,
                    width = LINE_WIDTH
                )
            ),
            cameraUpdate = null,
            clearMap = true
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `creates a FitBounds camera update for Centre animation`() {
        val coords = listOf(
            Coordinates(0.0, 0.0),
            Coordinates(1.0, 1.0)
        )
        val model = createDrawDistanceModel(
            coordinates = coords,
            cameraAnimation = CameraAnimation.Animate.Centre
        )

        val actual = mapper.toMapUiState(model)

        val expectedCameraUpdate = CameraUpdate.FitBounds(
            coordinates = coords,
            padding = 100
        )
        assertEquals(expectedCameraUpdate, actual.cameraUpdate)
    }

    @Test
    fun `creates a MoveTo camera update for Destination animation`() {
        val coords = listOf(
            Coordinates(0.0, 0.0),
            Coordinates(1.0, 1.0)
        )
        val model = createDrawDistanceModel(
            coordinates = coords,
            cameraAnimation = CameraAnimation.Animate.Destination
        )

        val actual = mapper.toMapUiState(model)

        val expectedCameraUpdate = CameraUpdate.MoveTo(position = coords.last())
        assertEquals(expectedCameraUpdate, actual.cameraUpdate)
    }

    @Test
    fun `does not create a camera update for None animation`() {
        val coords = listOf(
            Coordinates(0.0, 0.0),
            Coordinates(1.0, 1.0)
        )
        val model = createDrawDistanceModel(
            coordinates = coords,
            cameraAnimation = CameraAnimation.None
        )

        val result = mapper.toMapUiState(model)

        assertNull(result.cameraUpdate)
    }

    @Test
    fun `sets marker title with distance info`() {
        val coords = listOf(Coordinates(0.0, 0.0), Coordinates(1.0, 1.0), Coordinates(2.0, 2.0))
        val distanceName = "Distance: "
        val formattedDistance = "1.5 km"
        val model = createDrawDistanceModel(
            coordinates = coords,
            distanceName = distanceName,
            formattedDistance = formattedDistance
        )

        val result = mapper.toMapUiState(model)

        val expectedMarkers = listOf(
            MarkerData(
                position = Coordinates(2.0, 2.0),
                infoWindow = MarkerData.InfoWindow.Visible(title = distanceName + formattedDistance)
            )
        )
        assertEquals(expectedMarkers, result.markers)
    }

    private fun createDrawDistanceModel(
        coordinates: List<Coordinates> = emptyList(),
        source: DrawDistanceUiModel.Source = DrawDistanceUiModel.Source.MANUAL,
        distanceMode: DistanceMode = DistanceMode.FROM_CURRENT_POINT,
        cameraAnimation: CameraAnimation = CameraAnimation.Animate.Centre,
        distanceName: String = "Distance: ",
        formattedDistance: String = "1.0 km"
    ): DrawDistanceUiModel {
        return DrawDistanceUiModel(
            positionList = coordinates,
            distanceName = distanceName,
            formattedDistance = formattedDistance,
            source = source,
            distanceMode = distanceMode,
            cameraAnimation = cameraAnimation
        )
    }

    private companion object {

        const val LINE_WIDTH = 5f
    }
}

