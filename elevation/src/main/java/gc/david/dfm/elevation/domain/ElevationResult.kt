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

package gc.david.dfm.elevation.domain

/**
 * The answer the Elevation API gave. Failures to get an answer at all (network, parsing) are
 * thrown instead.
 */
sealed interface ElevationResult {

    data class Found(val samples: List<Double>) : ElevationResult {
        init {
            require(samples.isNotEmpty()) { "Use NoData when no sample was returned" }
        }
    }

    data object NoData : ElevationResult

    data class Rejected(val error: ElevationError) : ElevationResult
}

/**
 * The Elevation API statuses that reject a request. Unlike the raw API status, it cannot hold a
 * successful status.
 */
enum class ElevationError {
    INVALID_REQUEST,
    OVER_QUERY_LIMIT,
    REQUEST_DENIED,
    UNKNOWN_ERROR
}
