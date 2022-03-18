/*
 * Copyright (c) 2022 David Aguiar Gonzalez
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

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import gc.david.dfm.R
import gc.david.dfm.address.data.model.AddressCollectionEntity
import gc.david.dfm.await
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber

/**
 * Created by david on 12.01.17.
 */
class AddressRemoteDataSource(context: Context) {

    private val client = OkHttpClient()
    private val gson = Gson()
    private val geocodeApiKey = context.resources.getString(R.string.maps_geocode_api_key)

    suspend fun getNameByCoordinates(coordinates: LatLng): AddressCollectionEntity {
        return executeRequest(getNameByCoordinatesUrl(coordinates))
    }

    suspend fun getCoordinatesByName(name: String): AddressCollectionEntity {
        return executeRequest(getCoordinatesByNameUrl(name))
    }

    private suspend fun executeRequest(url: String): AddressCollectionEntity {
        val request = Request.Builder().url(url).header("content-type", "application/json").build()

        val response = client.newCall(request).await()
        val addressCollectionEntity =
                gson.fromJson(response.body!!.charStream(), AddressCollectionEntity::class.java)
        return addressCollectionEntity
    }

    private fun getNameByCoordinatesUrl(coordinates: LatLng): String {
        val parameter = "latlng=${coordinates.latitude},${coordinates.longitude}"
        Timber.tag(TAG).d(parameter)
        return getUrl(parameter)
    }

    private fun getCoordinatesByNameUrl(name: String): String {
        val parameterValue = name.replace(" ", "+")
        val parameter = "address=$parameterValue"
        Timber.tag(TAG).d(parameter)
        return getUrl(parameter)
    }

    private fun getUrl(parameter: String): String {
        return "https://maps.googleapis.com/maps/api/geocode/json?$parameter&key=$geocodeApiKey"
    }

    companion object {

        private const val TAG = "AddressRemoteDataSource"
    }
}