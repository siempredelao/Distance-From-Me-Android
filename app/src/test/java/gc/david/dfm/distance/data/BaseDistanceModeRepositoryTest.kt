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

import gc.david.dfm.distance.domain.model.DistanceMode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BaseDistanceModeRepositoryTest {

    private val repository = BaseDistanceModeRepository()

    @Test
    fun `starts in current position mode`() {
        assertEquals(DistanceMode.FROM_CURRENT_POINT, repository.getDistanceMode())
    }

    @Test
    fun `returns the mode that was set`() {
        repository.setDistanceMode(DistanceMode.FROM_ANY_POINT)

        assertEquals(DistanceMode.FROM_ANY_POINT, repository.getDistanceMode())
    }
}
