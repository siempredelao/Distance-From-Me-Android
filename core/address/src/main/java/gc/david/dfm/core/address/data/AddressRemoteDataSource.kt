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

package gc.david.dfm.core.address.data

import gc.david.dfm.core.address.data.model.AddressCollectionEntity
import gc.david.dfm.common.Coordinates
import gc.david.dfm.common.toPlainString
import gc.david.dfm.geocoding.data.getGoogleApiJson
import kotlinx.coroutines.CoroutineDispatcher
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import timber.log.Timber

/**
 * Calls the Google Geocoding API. [client] must be the shared [gc.david.dfm.geocoding.di.GOOGLE_API_CLIENT], which adds
 * the API key.
 */
class AddressRemoteDataSource(
    private val ioDispatcher: CoroutineDispatcher,
    private val client: OkHttpClient,
) {

    suspend fun getNameByCoordinates(coordinates: Coordinates): AddressCollectionEntity {
        val latLng = "${coordinates.latitude.toPlainString()},${coordinates.longitude.toPlainString()}"
        return executeRequest(name = "latlng", value = latLng)
    }

    suspend fun getCoordinatesByName(name: String): AddressCollectionEntity {
        return executeRequest(name = "address", value = name)
    }

    private suspend fun executeRequest(name: String, value: String): AddressCollectionEntity {
        val url = GEOCODE_URL.newBuilder().addQueryParameter(name, value).build()
        Timber.tag(TAG).d(url.toString())

        return client.getGoogleApiJson(url, ioDispatcher)
    }

    companion object {

        private const val TAG = "AddressRemoteDataSource"
        private val GEOCODE_URL: HttpUrl = "https://maps.googleapis.com/maps/api/geocode/json".toHttpUrl()
    }
}