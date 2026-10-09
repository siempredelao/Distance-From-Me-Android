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

package gc.david.dfm.address.domain

import gc.david.dfm.address.domain.model.Address
import gc.david.dfm.address.domain.model.Coordinates
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GeocodingResultTest {

    @Test
    fun `Found does not accept an empty address list`() {
        assertThrows<IllegalArgumentException> { GeocodingResult.Found(emptyList()) }
    }

    @Test
    fun `limitedTo keeps the first addresses of Found`() {
        val result = GeocodingResult.Found(ADDRESSES).limitedTo(2)

        assertEquals(GeocodingResult.Found(ADDRESSES.take(2)), result)
    }

    @Test
    fun `limitedTo keeps Found unchanged when it has fewer addresses than the limit`() {
        val result = GeocodingResult.Found(ADDRESSES).limitedTo(5)

        assertEquals(GeocodingResult.Found(ADDRESSES), result)
    }

    @Test
    fun `limitedTo keeps NoResults unchanged`() {
        assertEquals(GeocodingResult.NoResults, GeocodingResult.NoResults.limitedTo(1))
    }

    @Test
    fun `limitedTo keeps Rejected unchanged`() {
        val rejected = GeocodingResult.Rejected(GeocodingError.REQUEST_DENIED)

        assertEquals(rejected, rejected.limitedTo(1))
    }

    private companion object {

        val ADDRESSES = (1..3).map { Address("Address $it", Coordinates(it.toDouble(), it.toDouble())) }
    }
}
