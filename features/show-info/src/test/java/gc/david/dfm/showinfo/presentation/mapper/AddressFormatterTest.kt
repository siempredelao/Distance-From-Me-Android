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

package gc.david.dfm.showinfo.presentation.mapper

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AddressFormatterTest {

    private val formatter = AddressFormatter()

    @Test
    fun `format with address combines address and coordinates`() {
        val address = "123 Main Street"
        val latitude = 40.7128
        val longitude = -74.006

        val result = formatter.format(address, latitude, longitude)

        assertEquals("123 Main Street\n\n(40.7128,-74.006)", result)
    }

    @Test
    fun `format with null address uses coordinates only`() {
        val latitude = 51.5074
        val longitude = -0.1278

        val result = formatter.format(null, latitude, longitude)

        assertEquals("null\n\n(51.5074,-0.1278)", result)
    }

    @Test
    fun `format with empty address`() {
        val address = ""
        val latitude = 35.6762
        val longitude = 139.6503

        val result = formatter.format(address, latitude, longitude)

        assertEquals("\n\n(35.6762,139.6503)", result)
    }

    @Test
    fun `format with negative coordinates`() {
        val address = "Test Location"
        val latitude = -33.8688
        val longitude = 151.2093

        val result = formatter.format(address, latitude, longitude)

        assertEquals("Test Location\n\n(-33.8688,151.2093)", result)
    }
}

