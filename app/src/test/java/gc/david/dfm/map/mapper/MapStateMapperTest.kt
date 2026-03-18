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

package gc.david.dfm.map.mapper

import android.content.Context
import android.content.res.Resources
import gc.david.dfm.R
import gc.david.dfm.common.Coordinates
import gc.david.dfm.distance.data.DistanceMode
import gc.david.dfm.main.presentation.model.DrawDistanceModel
import gc.david.dfm.map.model.LineColor
import gc.david.dfm.settings.domain.model.CameraAnimation
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class MapStateMapperTest {

    private lateinit var context: Context
    private lateinit var resources: Resources
    private lateinit var mapper: MapStateMapper

    private val lineWidth = 5f

    @Before
    fun setUp() {
        resources = mock()
        whenever(resources.getDimension(R.dimen.map_line_width)).thenReturn(lineWidth)
        
        context = mock()
        whenever(context.resources).thenReturn(resources)
        
        mapper = MapStateMapper(context)
    }

    @Test
    fun `toMapUiState creates an empty state for empty coordinates`() {
        val model = createDrawDistanceModel(coordinates = emptyList())

        val result = mapper.toMapUiState(model)

        assertTrue(result.markers.isEmpty())
        assertTrue(result.polylines.isEmpty())
        assertNull(result.cameraUpdate)
        assertTrue(result.clearMap)
    }

    @Test
    fun `toMapUiState creates a marker at end for manual mode`() {
        val coords = listOf(
            Coordinates(0.0, 0.0),
            Coordinates(1.0, 1.0),
            Coordinates(2.0, 2.0)
        )
        val model = createDrawDistanceModel(
            coordinates = coords,
            source = DrawDistanceModel.Source.MANUAL,
            distanceMode = DistanceMode.FROM_CURRENT_POINT
        )

        val result = mapper.toMapUiState(model)

        assertEquals(1, result.markers.size)
        assertEquals(coords.last(), result.markers[0].position)
        assertTrue(result.markers[0].showInfoWindow)
    }

    @Test
    fun `toMapUiState creates markers at start and end for database mode`() {
        val coords = listOf(
            Coordinates(0.0, 0.0),
            Coordinates(1.0, 1.0),
            Coordinates(2.0, 2.0)
        )
        val model = createDrawDistanceModel(
            coordinates = coords,
            source = DrawDistanceModel.Source.DATABASE,
            distanceMode = DistanceMode.FROM_CURRENT_POINT
        )

        val result = mapper.toMapUiState(model)

        assertEquals(2, result.markers.size)
        assertEquals(coords.first(), result.markers[0].position)
        assertEquals(coords.last(), result.markers[1].position)
        assertTrue(result.markers[1].showInfoWindow)
    }

    @Test
    fun `toMapUiState creates markers at start and end for FROM_ANY_POINT mode`() {
        val coords = listOf(
            Coordinates(0.0, 0.0),
            Coordinates(1.0, 1.0)
        )
        val model = createDrawDistanceModel(
            coordinates = coords,
            source = DrawDistanceModel.Source.MANUAL,
            distanceMode = DistanceMode.FROM_ANY_POINT
        )

        val result = mapper.toMapUiState(model)

        assertEquals(2, result.markers.size)
        assertEquals(coords.first(), result.markers[0].position)
        assertEquals(coords.last(), result.markers[1].position)
    }

    @Test
    fun `toMapUiState creates green polylines for manual source`() {
        val coords = listOf(
            Coordinates(0.0, 0.0),
            Coordinates(1.0, 1.0),
            Coordinates(2.0, 2.0)
        )
        val model = createDrawDistanceModel(
            coordinates = coords,
            source = DrawDistanceModel.Source.MANUAL
        )

        val result = mapper.toMapUiState(model)

        assertEquals(2, result.polylines.size)
        result.polylines.forEach { polyline ->
            assertEquals(LineColor.GREEN, polyline.color)
            assertEquals(lineWidth, polyline.width)
        }
    }

    @Test
    fun `toMapUiState creates yellow polylines for database source`() {
        val coords = listOf(
            Coordinates(0.0, 0.0),
            Coordinates(1.0, 1.0)
        )
        val model = createDrawDistanceModel(
            coordinates = coords,
            source = DrawDistanceModel.Source.DATABASE
        )

        val result = mapper.toMapUiState(model)

        assertEquals(1, result.polylines.size)
        assertEquals(LineColor.YELLOW, result.polylines[0].color)
    }

    @Test
    fun `toMapUiState creates a FitBounds camera update for Centre animation`() {
        val coords = listOf(
            Coordinates(0.0, 0.0),
            Coordinates(1.0, 1.0)
        )
        val model = createDrawDistanceModel(
            coordinates = coords,
            cameraAnimation = CameraAnimation.Animate.Centre
        )

        val result = mapper.toMapUiState(model)

        assertNotNull(result.cameraUpdate)
        assertTrue(result.cameraUpdate is gc.david.dfm.map.model.CameraUpdate.FitBounds)
    }

    @Test
    fun `toMapUiState creates a MoveTo camera update for Destination animation`() {
        val coords = listOf(
            Coordinates(0.0, 0.0),
            Coordinates(1.0, 1.0)
        )
        val model = createDrawDistanceModel(
            coordinates = coords,
            cameraAnimation = CameraAnimation.Animate.Destination
        )

        val result = mapper.toMapUiState(model)

        assertNotNull(result.cameraUpdate)
        assertTrue(result.cameraUpdate is gc.david.dfm.map.model.CameraUpdate.MoveTo)
        val moveToUpdate = result.cameraUpdate as gc.david.dfm.map.model.CameraUpdate.MoveTo
        assertEquals(coords.last(), moveToUpdate.position)
    }

    @Test
    fun `toMapUiState does not create a camera update for None animation`() {
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
    fun `toMapUiState sets marker title with distance info`() {
        val coords = listOf(Coordinates(0.0, 0.0), Coordinates(1.0, 1.0))
        val distanceName = "Distance: "
        val formattedDistance = "1.5 km"
        val model = createDrawDistanceModel(
            coordinates = coords,
            distanceName = distanceName,
            formattedDistance = formattedDistance
        )

        val result = mapper.toMapUiState(model)

        val lastMarker = result.markers.last()
        assertEquals(distanceName + formattedDistance, lastMarker.title)
    }

    private fun createDrawDistanceModel(
        coordinates: List<Coordinates> = emptyList(),
        source: DrawDistanceModel.Source = DrawDistanceModel.Source.MANUAL,
        distanceMode: DistanceMode = DistanceMode.FROM_CURRENT_POINT,
        cameraAnimation: CameraAnimation = CameraAnimation.Animate.Centre,
        distanceName: String = "Distance: ",
        formattedDistance: String = "1.0 km",
        distanceInMetres: Double = 1000.0
    ): DrawDistanceModel {
        return DrawDistanceModel(
            positionList = coordinates,
            distanceName = distanceName,
            distanceInMetres = distanceInMetres,
            formattedDistance = formattedDistance,
            source = source,
            distanceMode = distanceMode,
            cameraAnimation = cameraAnimation
        )
    }
}

