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
package gc.david.dfm.elevation.data.model

import gc.david.dfm.common.data.googleApiJson
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ElevationEntityJsonTest {

    @Test
    fun `parses OK response ignoring unknown keys`() {
        val json = """
            {
              "results": [
                {
                  "elevation": 1608.637939453125,
                  "location": { "lat": 39.7391536, "lng": -104.9847034 },
                  "resolution": 4.771975994110107
                },
                {
                  "elevation": -50.78903579711914,
                  "location": { "lat": 36.455556, "lng": -116.866667 },
                  "resolution": 19.08790397644043
                }
              ],
              "status": "OK"
            }
        """.trimIndent()

        val entity = googleApiJson.decodeFromString<ElevationEntity>(json)

        assertEquals(ElevationStatus.OK, entity.status)
        assertEquals(listOf(1608.637939453125, -50.78903579711914), entity.results.map { it.elevation })
    }

    @Test
    fun `defaults results to empty list when missing`() {
        val json = """{ "error_message": "Invalid request.", "status": "INVALID_REQUEST" }"""

        val entity = googleApiJson.decodeFromString<ElevationEntity>(json)

        assertEquals(ElevationStatus.INVALID_REQUEST, entity.status)
        assertTrue(entity.results.isEmpty())
    }

    @Test
    fun `falls back to UNKNOWN_ERROR for a status not modelled`() {
        val json = """{ "results": [], "status": "DATA_NOT_AVAILABLE" }"""

        val entity = googleApiJson.decodeFromString<ElevationEntity>(json)

        assertEquals(ElevationStatus.UNKNOWN_ERROR, entity.status)
    }
}
