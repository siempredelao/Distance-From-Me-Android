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

package gc.david.dfm.screenshots

import gc.david.dfm.core.address.domain.AddressRepository
import gc.david.dfm.core.address.domain.model.Address
import gc.david.dfm.core.address.domain.model.GeocodingResult
import gc.david.dfm.core.coordinates.domain.model.Coordinates

/** The places of the Play Store pictures, so that taking them doesn't use up the quota of Google's services. */
class CannedAddresses : AddressRepository {

    override suspend fun getNameByCoordinates(coordinates: Coordinates): GeocodingResult =
        Place.entries.find { it.isNear(coordinates.latitude, coordinates.longitude) }.toResult()

    override suspend fun getCoordinatesByName(name: String): GeocodingResult =
        Place.entries.find { it.query.equals(name.trim(), ignoreCase = true) }.toResult()

    private fun Place?.toResult(): GeocodingResult =
        this?.let { GeocodingResult.Found(listOf(Address(it.address, Coordinates(it.latitude, it.longitude)))) }
            ?: GeocodingResult.NoResults
}
