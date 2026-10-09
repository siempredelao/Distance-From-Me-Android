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

package gc.david.dfm.common.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.coroutines.executeAsync

/**
 * Sends a GET request to a Google Maps web service and decodes its JSON body as [T].
 *
 * The response is closed even when the calling coroutine is cancelled after it arrives.
 *
 * @throws HttpException when the server answers with a non-2xx status
 * @throws java.io.IOException when the request does not reach the server
 * @throws kotlinx.serialization.SerializationException when the body is not the expected JSON
 */
suspend inline fun <reified T> OkHttpClient.getGoogleApiJson(url: String, ioDispatcher: CoroutineDispatcher): T {
    val request = Request.Builder().url(url).header("content-type", "application/json").build()
    return newCall(request).executeAsync().use { response ->
        if (!response.isSuccessful) throw HttpException(response.code)
        withContext(ioDispatcher) { googleApiJson.decodeFromString<T>(response.body.string()) }
    }
}
