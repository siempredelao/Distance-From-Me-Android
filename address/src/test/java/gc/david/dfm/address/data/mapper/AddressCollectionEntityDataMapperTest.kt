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

package gc.david.dfm.address.data.mapper

import gc.david.dfm.address.data.model.AddressCollectionEntity
import gc.david.dfm.address.data.model.Geometry
import gc.david.dfm.address.data.model.GeocodingStatus
import gc.david.dfm.address.data.model.Location
import gc.david.dfm.address.data.model.Result
import gc.david.dfm.address.domain.GeocodingError
import gc.david.dfm.address.domain.GeocodingResult
import gc.david.dfm.address.domain.model.Address
import gc.david.dfm.address.domain.model.Coordinates
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource

/**
 * Created by david on 15.01.17.
 */
class AddressCollectionEntityDataMapperTest {

    private val mapper = AddressCollectionEntityDataMapper()

    @Test
    fun `transforms results to Found addresses when status is OK`() {
        val entity = AddressCollectionEntity(
            results = listOf(Result("address", Geometry(Location(1.0, 2.0)))),
            status = GeocodingStatus.OK
        )

        val result = mapper.transform(entity)

        assertEquals(GeocodingResult.Found(listOf(Address("address", Coordinates(1.0, 2.0)))), result)
    }

    @ParameterizedTest
    @EnumSource(value = GeocodingStatus::class, names = ["OK", "ZERO_RESULTS"])
    fun `transforms a successful status without results to NoResults`(status: GeocodingStatus) {
        val entity = AddressCollectionEntity(results = emptyList(), status = status)

        val result = mapper.transform(entity)

        assertEquals(GeocodingResult.NoResults, result)
    }

    @ParameterizedTest
    @CsvSource(
        "INVALID_REQUEST, INVALID_REQUEST",
        "OVER_DAILY_LIMIT, OVER_DAILY_LIMIT",
        "OVER_QUERY_LIMIT, OVER_QUERY_LIMIT",
        "REQUEST_DENIED, REQUEST_DENIED",
        "UNKNOWN_ERROR, UNKNOWN_ERROR",
    )
    fun `transforms a rejecting status to Rejected with the matching error`(
        status: GeocodingStatus,
        error: GeocodingError,
    ) {
        val entity = AddressCollectionEntity(status = status)

        val result = mapper.transform(entity)

        assertEquals(GeocodingResult.Rejected(error), result)
    }
}
