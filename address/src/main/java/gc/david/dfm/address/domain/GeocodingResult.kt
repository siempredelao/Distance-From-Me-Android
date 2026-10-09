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

import gc.david.dfm.address.domain.model.Address

/**
 * The answer the Geocoding API gave. Failures to get an answer at all (network, parsing) are
 * thrown instead.
 */
sealed interface GeocodingResult {

    data class Found(val addresses: List<Address>) : GeocodingResult {
        init {
            require(addresses.isNotEmpty()) { "Use NoResults when no address was found" }
        }
    }

    data object NoResults : GeocodingResult

    data class Rejected(val error: GeocodingError) : GeocodingResult
}

/**
 * The Geocoding API statuses that reject a request. Unlike the raw API status, it cannot hold a
 * successful status.
 */
enum class GeocodingError {
    INVALID_REQUEST,
    OVER_DAILY_LIMIT,
    OVER_QUERY_LIMIT,
    REQUEST_DENIED,
    UNKNOWN_ERROR
}

/** Keeps at most [maxAddresses] addresses of a [GeocodingResult.Found]; other results are unchanged. */
internal fun GeocodingResult.limitedTo(maxAddresses: Int): GeocodingResult =
    when (this) {
        is GeocodingResult.Found -> GeocodingResult.Found(addresses.take(maxAddresses))
        GeocodingResult.NoResults, is GeocodingResult.Rejected -> this
    }
