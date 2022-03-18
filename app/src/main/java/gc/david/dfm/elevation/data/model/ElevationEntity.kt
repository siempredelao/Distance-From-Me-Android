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

package gc.david.dfm.elevation.data.model

class ElevationEntity(val results: List<Result> = emptyList(), val status: ElevationStatus)

enum class ElevationStatus {
    /**
     * The service request was successful.
     */
    OK,

    /**
     * The service request was malformed.
     */
    INVALID_REQUEST,

    /**
     * The requestor has exceeded quota.
     */
    OVER_QUERY_LIMIT,

    /**
     * The service did not complete the request, likely because on an invalid parameter.
     */
    REQUEST_DENIED,

    /**
     * Unknown error.
     */
    UNKNOWN_ERROR
}