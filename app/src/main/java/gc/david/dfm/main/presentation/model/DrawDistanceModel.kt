/*
 * Copyright (c) 2022 David Aguiar Gonzalez
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

import com.google.android.gms.maps.model.LatLng
import gc.david.dfm.distance.data.DistanceMode

data class DrawDistanceModel(
    val positionList: MutableList<LatLng>, // TODO this is wip, should be List
    val distanceName: String,
    val distanceInMetres: Double,
    val formattedDistance: String,
    val source: Source,
    val distanceMode: DistanceMode
) {

    enum class Source { MANUAL, DATABASE }

    companion object {

        val EMPTY =
            DrawDistanceModel(
                mutableListOf(),
                "",
                0.0,
                "",
                Source.MANUAL,
                DistanceMode.FROM_CURRENT_POINT
            )
    }
}