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

package gc.david.dfm.address.presentation.mapper

import gc.david.dfm.address.R
import gc.david.dfm.address.domain.GeocodingException
import gc.david.dfm.common.presentation.ResourceProvider

class GeocodingErrorMessageMapper(private val resourceProvider: ResourceProvider) {
    fun map(error: Throwable): String =
        when (error) {
            is GeocodingException.InvalidRequest ->
                resourceProvider.get(R.string.geocoding_error_invalid_request)
            is GeocodingException.OverDailyLimit ->
                resourceProvider.get(R.string.geocoding_error_quota)
            is GeocodingException.OverQueryLimit ->
                resourceProvider.get(R.string.geocoding_error_rate_limited)
            is GeocodingException.RequestDenied ->
                resourceProvider.get(R.string.geocoding_error_request_denied)
            is GeocodingException.UnknownError ->
                resourceProvider.get(R.string.geocoding_error_unknown)
            else ->
                resourceProvider.get(R.string.geocoding_error_generic)
        }
}

