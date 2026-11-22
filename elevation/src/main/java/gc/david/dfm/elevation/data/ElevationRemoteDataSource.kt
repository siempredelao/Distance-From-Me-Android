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

import gc.david.dfm.common.data.getGoogleApiJson
import gc.david.dfm.elevation.data.model.ElevationEntity
import kotlinx.coroutines.CoroutineDispatcher
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import timber.log.Timber

/**
 * Calls the Google Elevation API. [client] must be the shared [gc.david.dfm.common.GOOGLE_API_CLIENT], which adds the
 * API key.
 */
class ElevationRemoteDataSource(
    private val ioDispatcher: CoroutineDispatcher,
    private val client: OkHttpClient,
) {

    suspend fun getElevation(coordinatesPath: String, maxSamples: Int): ElevationEntity {
        val url = ELEVATION_URL.newBuilder()
            .addQueryParameter("path", coordinatesPath)
            .addQueryParameter("samples", maxSamples.toString())
            .build()
        Timber.tag(TAG).d(url.toString())

        return client.getGoogleApiJson(url, ioDispatcher)
    }

    companion object {

        private const val TAG = "ElevationRemoteDataSrc"
        private val ELEVATION_URL: HttpUrl = "https://maps.googleapis.com/maps/api/elevation/json".toHttpUrl()
    }
}
