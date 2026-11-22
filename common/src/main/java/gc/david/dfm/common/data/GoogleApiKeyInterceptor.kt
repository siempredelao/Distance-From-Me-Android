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

import gc.david.dfm.GeocodeApiKeyProvider
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Adds the API key to every HTTPS request to the Google Maps web services, so remote data sources build their URLs
 * without it.
 *
 * Requests to any other host, or over plain HTTP, pass through untouched so the key never reaches another server.
 * A request that already carries a `key` keeps it.
 */
class GoogleApiKeyInterceptor(private val apiKeyProvider: GeocodeApiKeyProvider) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url
        if (!url.isHttps || url.host != GOOGLE_API_HOST || url.queryParameter(KEY_PARAMETER) != null) {
            return chain.proceed(request)
        }

        val urlWithKey = url.newBuilder().addQueryParameter(KEY_PARAMETER, apiKeyProvider.getApiKey()).build()
        return chain.proceed(request.newBuilder().url(urlWithKey).build())
    }

    private companion object {

        const val GOOGLE_API_HOST = "maps.googleapis.com"
        const val KEY_PARAMETER = "key"
    }
}
