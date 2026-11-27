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

import android.content.Context
import gc.david.dfm.geocoding.R
import gc.david.dfm.geocoding.domain.GeocodeApiKeyProvider

internal class DefaultGeocodeApiKeyProvider(context: Context) : GeocodeApiKeyProvider {

    private val apiKey = context.getString(R.string.maps_geocode_api_key)

    override fun getApiKey(): String = apiKey
}
