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
package gc.david.dfm.address.data.model

import gc.david.dfm.common.data.googleApiJson
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AddressCollectionEntityJsonTest {

    @Test
    fun `parses OK response ignoring unknown keys`() {
        val json = """
            {
              "results": [
                {
                  "address_components": [{ "long_name": "1600", "types": ["street_number"] }],
                  "formatted_address": "1600 Amphitheatre Pkwy, Mountain View, CA 94043, USA",
                  "geometry": {
                    "location": { "lat": 37.4224764, "lng": -122.0842499 },
                    "location_type": "ROOFTOP"
                  },
                  "place_id": "ChIJ2eUgeAK6j4ARbn5u_wAGqWA"
                }
              ],
              "status": "OK"
            }
        """.trimIndent()

        val entity = googleApiJson.decodeFromString<AddressCollectionEntity>(json)

        assertEquals(GeocodingStatus.OK, entity.status)
        assertEquals(1, entity.results.size)
        val result = entity.results[0]
        assertEquals("1600 Amphitheatre Pkwy, Mountain View, CA 94043, USA", result.formattedAddress)
        assertEquals(37.4224764, result.geometry.location.latitude, 0.0)
        assertEquals(-122.0842499, result.geometry.location.longitude, 0.0)
    }

    @Test
    fun `defaults results to empty list when missing`() {
        val json = """{ "error_message": "The provided API key is invalid.", "status": "REQUEST_DENIED" }"""

        val entity = googleApiJson.decodeFromString<AddressCollectionEntity>(json)

        assertEquals(GeocodingStatus.REQUEST_DENIED, entity.status)
        assertTrue(entity.results.isEmpty())
    }

    @Test
    fun `falls back to UNKNOWN_ERROR for an unknown status`() {
        val json = """{ "results": [], "status": "SOMETHING_NEW" }"""

        val entity = googleApiJson.decodeFromString<AddressCollectionEntity>(json)

        assertEquals(GeocodingStatus.UNKNOWN_ERROR, entity.status)
    }
}
