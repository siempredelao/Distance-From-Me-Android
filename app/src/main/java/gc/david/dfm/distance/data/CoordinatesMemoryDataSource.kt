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

package gc.david.dfm.distance.data

import gc.david.dfm.common.Coordinate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CoordinatesMemoryDataSource {

    private val _coordinates = MutableStateFlow<List<Coordinate>>(emptyList())

    fun observe(): StateFlow<List<Coordinate>> = _coordinates.asStateFlow()

    fun append(coordinate: Coordinate) {
        _coordinates.update { it + coordinate }
    }

    fun clear() {
        _coordinates.update { emptyList() }
    }
}
