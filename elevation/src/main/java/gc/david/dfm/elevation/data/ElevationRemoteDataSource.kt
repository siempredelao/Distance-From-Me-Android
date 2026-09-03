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

import com.google.gson.Gson
import gc.david.dfm.GeocodeApiKeyProvider
import gc.david.dfm.elevation.data.model.ElevationEntity
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.coroutines.executeAsync
import timber.log.Timber

class ElevationRemoteDataSource(geocodeApiKeyProvider: GeocodeApiKeyProvider) {

    private val client = OkHttpClient()
    private val gson = Gson()
    private val geocodeApiKey = geocodeApiKeyProvider.getApiKey()

    suspend fun getElevation(coordinatesPath: String, maxSamples: Int): ElevationEntity {
        val urlNoKey = "https://maps.googleapis.com/maps/api/elevation/json?path=$coordinatesPath&samples=$maxSamples"
        Timber.tag(TAG).d(urlNoKey)

        val url = "$urlNoKey&key=$geocodeApiKey"

        val request = Request.Builder().url(url)
                .header("content-type", "application/json")
                .build()
        val response = client.newCall(request).executeAsync()

        val elevationEntity =
                gson.fromJson(response.body.charStream(), ElevationEntity::class.java)
        return elevationEntity
    }

    companion object {

        private const val TAG = "ElevationRemoteDataSrc"
    }
}