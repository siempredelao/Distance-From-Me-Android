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
package gc.david.dfm.geocoding.data

import gc.david.dfm.geocoding.domain.GeocodeApiKeyProvider
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class GoogleApiKeyInterceptorTest {

    private val apiKeyProvider = mock<GeocodeApiKeyProvider> {
        on { getApiKey() } doReturn API_KEY
    }

    // Records the request after GoogleApiKeyInterceptor and answers in place of the network
    private val sentRequests = mutableListOf<Request>()
    private val client = OkHttpClient.Builder()
        .addInterceptor(GoogleApiKeyInterceptor(apiKeyProvider))
        .addInterceptor { chain ->
            sentRequests += chain.request()
            Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("Test")
                .body("{}".toResponseBody("application/json".toMediaType()))
                .build()
        }
        .build()

    @Test
    fun `adds the key to Google Maps web service requests`() {
        send("https://maps.googleapis.com/maps/api/geocode/json?address=Madrid")

        val url = sentRequests.single().url
        assertEquals(API_KEY, url.queryParameter("key"))
        assertEquals("Madrid", url.queryParameter("address"))
    }

    @Test
    fun `leaves requests to other hosts untouched`() {
        send("https://example.com/json?address=Madrid")

        assertNull(sentRequests.single().url.queryParameter("key"))
    }

    @Test
    fun `leaves plain http requests untouched`() {
        send("http://maps.googleapis.com/maps/api/geocode/json?address=Madrid")

        assertNull(sentRequests.single().url.queryParameter("key"))
    }

    @Test
    fun `keeps a key the request already has`() {
        send("https://maps.googleapis.com/maps/api/geocode/json?address=Madrid&key=other-key")

        assertEquals(listOf("other-key"), sentRequests.single().url.queryParameterValues("key"))
    }

    private fun send(url: String) {
        client.newCall(Request.Builder().url(url.toHttpUrl()).build()).execute().close()
    }

    private companion object {

        const val API_KEY = "test-api-key"
    }
}
