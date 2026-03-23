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

package gc.david.dfm.address.presentation.mapper

import gc.david.dfm.address.R
import gc.david.dfm.address.domain.GeocodingException
import gc.david.dfm.common.presentation.ResourceProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class GeocodingErrorMessageMapperTest {

    private val resourceProvider = mock<ResourceProvider>()
    private val mapper = GeocodingErrorMessageMapper(resourceProvider)

    @Test
    fun `maps InvalidRequest to invalid request string`() {
        whenever(resourceProvider.get(R.string.geocoding_error_invalid_request)).thenReturn("invalid")

        val result = mapper.map(GeocodingException.InvalidRequest())

        assertEquals("invalid", result)
    }

    @Test
    fun `maps OverDailyLimit to quota string`() {
        whenever(resourceProvider.get(R.string.geocoding_error_quota)).thenReturn("quota")

        val result = mapper.map(GeocodingException.OverDailyLimit())

        assertEquals("quota", result)
    }

    @Test
    fun `maps OverQueryLimit to rate limited string`() {
        whenever(resourceProvider.get(R.string.geocoding_error_rate_limited)).thenReturn("rate_limited")

        val result = mapper.map(GeocodingException.OverQueryLimit())

        assertEquals("rate_limited", result)
    }

    @Test
    fun `maps RequestDenied to request denied string`() {
        whenever(resourceProvider.get(R.string.geocoding_error_request_denied)).thenReturn("denied")

        val result = mapper.map(GeocodingException.RequestDenied())

        assertEquals("denied", result)
    }

    @Test
    fun `maps UnknownError to unknown string`() {
        whenever(resourceProvider.get(R.string.geocoding_error_unknown)).thenReturn("unknown")

        val result = mapper.map(GeocodingException.UnknownError())

        assertEquals("unknown", result)
    }

    @Test
    fun `maps any other throwable to generic string`() {
        whenever(resourceProvider.get(R.string.geocoding_error_generic)).thenReturn("generic")

        val result = mapper.map(IllegalStateException("boom"))

        assertEquals("generic", result)
    }
}
