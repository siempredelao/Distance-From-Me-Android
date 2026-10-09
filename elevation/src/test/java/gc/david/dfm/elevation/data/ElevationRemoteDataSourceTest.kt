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

package gc.david.dfm.elevation.data

import gc.david.dfm.GeocodeApiKeyProvider
import gc.david.dfm.common.data.HttpException
import gc.david.dfm.elevation.data.model.ElevationStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.io.IOException

class ElevationRemoteDataSourceTest {

    private val geocodeApiKeyProvider = mock<GeocodeApiKeyProvider> {
        on { getApiKey() } doReturn API_KEY
    }

    // Answers every request in place of the network
    private var respond: (Request) -> Response = { throw IOException("No response configured") }
    private val requests = mutableListOf<Request>()
    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            requests += chain.request()
            respond(chain.request())
        }
        .build()

    private val dataSource = ElevationRemoteDataSource(geocodeApiKeyProvider, Dispatchers.Unconfined, client)

    @Test
    fun `requests the path and samples with the API key`() = runTest {
        respondWith(code = 200, body = OK_BODY)

        dataSource.getElevation("1.0,2.0|3.0,4.0", 100)

        val url = requests.single().url
        assertEquals("1.0,2.0|3.0,4.0", url.queryParameter("path"))
        assertEquals("100", url.queryParameter("samples"))
        assertEquals(API_KEY, url.queryParameter("key"))
    }

    @Test
    fun `parses a successful response`() = runTest {
        respondWith(code = 200, body = OK_BODY)

        val entity = dataSource.getElevation("1.0,2.0|3.0,4.0", 100)

        assertEquals(ElevationStatus.OK, entity.status)
        assertEquals(listOf(1608.6, 1610.2), entity.results.map { it.elevation })
    }

    @Test
    fun `throws HttpException with the status code for an unsuccessful response`() = runTest {
        respondWith(code = 503, body = "<html>Service unavailable</html>")

        val thrown = runCatching { dataSource.getElevation("1.0,2.0|3.0,4.0", 100) }.exceptionOrNull()

        assertEquals(503, (thrown as HttpException).code)
    }

    private fun respondWith(code: Int, body: String) {
        respond = { request ->
            Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message("Test")
                .body(body.toResponseBody("application/json".toMediaType()))
                .build()
        }
    }

    private companion object {

        const val API_KEY = "test-api-key"
        val OK_BODY = """
            {
              "results": [
                { "elevation": 1608.6, "location": { "lat": 39.74, "lng": -104.98 }, "resolution": 4.77 },
                { "elevation": 1610.2, "location": { "lat": 39.75, "lng": -104.99 }, "resolution": 4.77 }
              ],
              "status": "OK"
            }
        """.trimIndent()
    }
}
