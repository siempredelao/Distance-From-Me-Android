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

package gc.david.dfm.address.domain

import gc.david.dfm.address.data.model.GeocodingStatus

sealed class GeocodingException(message: String) : Exception(message) {

    class InvalidRequest : GeocodingException("The geocoding query is missing or invalid")

    class OverDailyLimit : GeocodingException("The daily API quota has been exceeded or billing is not enabled")

    class OverQueryLimit : GeocodingException("Too many requests in a short time period")

    class RequestDenied : GeocodingException("The geocoding request was denied by the server")

    class UnknownError : GeocodingException("A server error occurred; retrying may help")

    companion object {

        fun from(status: GeocodingStatus): GeocodingException = when (status) {
            GeocodingStatus.INVALID_REQUEST -> InvalidRequest()
            GeocodingStatus.OVER_DAILY_LIMIT -> OverDailyLimit()
            GeocodingStatus.OVER_QUERY_LIMIT -> OverQueryLimit()
            GeocodingStatus.REQUEST_DENIED -> RequestDenied()
            GeocodingStatus.UNKNOWN_ERROR -> UnknownError()
            GeocodingStatus.OK, GeocodingStatus.ZERO_RESULTS ->
                throw IllegalArgumentException("$status is not an error status")
        }
    }
}
