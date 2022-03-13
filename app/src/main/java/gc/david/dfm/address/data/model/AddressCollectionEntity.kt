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

package gc.david.dfm.address.data.model

class AddressCollectionEntity(val results: List<Result> = emptyList(), val status: GeocodingStatus)

enum class GeocodingStatus {
    /**
     * No errors occurred; the address was successfully parsed and at least one geocode was returned.
     */
    OK,

    /**
     * The geocode was successful but returned no results. This may occur if the geocoder was passed a non-existent address.
     */
    ZERO_RESULTS,

    /**
     * The query (address, components or latlng) is missing.
     */
    INVALID_REQUEST,

    /**
     * We are over our quota.
     */
    OVER_QUERY_LIMIT,

    /**
     * The request was denied. The web page is not allowed to use the geocoder.
     */
    REQUEST_DENIED,

    /**
     * The request could not be processed due to a server error. The request may succeed if you try again.
     */
    UNKNOWN_ERROR,

    /**
     * The request timed out or there was a problem contacting the Google servers. The request may succeed if you try again.
     */
    ERROR
}
