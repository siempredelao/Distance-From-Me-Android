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

package gc.david.dfm.address.data

import gc.david.dfm.address.data.model.GeocodingStatus
import gc.david.dfm.common.Coordinates
import gc.david.dfm.geocoding.data.HttpException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.IOException

class AddressRemoteDataSourceTest {

    // Answers every request in place of the network
    private var respond: (Request) -> Response = { throw IOException("No response configured") }
    private val requests = mutableListOf<Request>()
    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            requests += chain.request()
            respond(chain.request())
        }
        .build()

    private val dataSource = AddressRemoteDataSource(Dispatchers.Unconfined, client)

    @Test
    fun `parses a successful response`() = runTest {
        respondWith(code = 200, body = OK_BODY)

        val entity = dataSource.getCoordinatesByName(LOCATION_NAME)

        assertEquals(GeocodingStatus.OK, entity.status)
        assertEquals("Berlin, Germany", entity.results.single().formattedAddress)
    }

    @Test
    fun `requests the name by coordinates without adding the API key`() = runTest {
        respondWith(code = 200, body = OK_BODY)

        dataSource.getNameByCoordinates(Coordinates(1.0, 2.0))

        val url = requests.single().url
        assertEquals("1.0,2.0", url.queryParameter("latlng"))
        assertEquals(setOf("latlng"), url.queryParameterNames)
    }

    @Test
    fun `requests coordinates near zero in plain decimal notation`() = runTest {
        respondWith(code = 200, body = OK_BODY)

        dataSource.getNameByCoordinates(Coordinates(0.0005, -0.0001))

        assertEquals("0.00050,-0.00010", requests.single().url.queryParameter("latlng"))
    }

    @Test
    fun `requests the name with reserved URL characters kept inside the address parameter`() = runTest {
        respondWith(code = 200, body = OK_BODY)

        dataSource.getCoordinatesByName("Barnes & Noble, Flat #3")

        val url = requests.single().url
        assertEquals("Barnes & Noble, Flat #3", url.queryParameter("address"))
        assertEquals(setOf("address"), url.queryParameterNames)
    }

    @Test
    fun `throws HttpException with the status code for an unsuccessful response`() = runTest {
        respondWith(code = 403, body = """{ "error": { "code": 403, "message": "Forbidden" } }""")

        val thrown = runCatching { dataSource.getCoordinatesByName(LOCATION_NAME) }.exceptionOrNull()

        assertEquals(403, (thrown as HttpException).code)
    }

    @Test
    fun `throws SerializationException for a malformed body`() = runTest {
        respondWith(code = 200, body = "<html>Bad gateway</html>")

        val thrown = runCatching { dataSource.getCoordinatesByName(LOCATION_NAME) }.exceptionOrNull()

        assertTrue(thrown is SerializationException)
    }

    @Test
    fun `propagates network failures`() = runTest {
        respond = { throw IOException("Connection refused") }

        val thrown = runCatching { dataSource.getCoordinatesByName(LOCATION_NAME) }.exceptionOrNull()

        assertTrue(thrown is IOException)
        assertEquals("Connection refused", thrown?.message)
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

        const val LOCATION_NAME = "Berlin"
        val OK_BODY = """
            {
              "results": [
                {
                  "formatted_address": "Berlin, Germany",
                  "geometry": { "location": { "lat": 52.52, "lng": 13.405 } }
                }
              ],
              "status": "OK"
            }
        """.trimIndent()
    }
}
