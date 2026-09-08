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

package gc.david.dfm.main.presentation.mapper

import gc.david.dfm.R
import gc.david.dfm.common.presentation.ResourceProvider
import gc.david.dfm.core.distances.domain.model.Distance
import gc.david.dfm.main.domain.StoredDistances
import gc.david.dfm.main.presentation.model.DistanceSelectionUiModel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.text.DateFormat
import java.util.Date
import java.util.Locale

class LoadDistancesMapperTest {

    private val resourceProvider = mock<ResourceProvider>()
    private val mapper = LoadDistancesMapper(resourceProvider)

    @BeforeEach
    fun setup() {
        Locale.setDefault(Locale.US)
    }

    @Test
    fun `when rate request is not available returns only mapped distances`() {
        val date = Date(0)
        val storedDistances = StoredDistances(
            distances = listOf(Distance(1L, "Home", "3.2 km", date)),
            rateRequest = StoredDistances.RateRequest.NotAvailable
        )

        val result = mapper.map(storedDistances)

        assertEquals(
            listOf(
                DistanceSelectionUiModel.Distance(
                    id = 1L,
                    name = "Home",
                    distance = "3.2 km",
                    date = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.US).format(date)
                )
            ),
            result
        )
    }

    @Test
    fun `when rate request is available inserts the rate app at the requested position`() {
        val firstDate = Date(0)
        val secondDate = Date(1000)
        val thirdDate = Date(2000)
        val storedDistances = StoredDistances(
            distances = listOf(
                Distance(1L, "Home", "3.2 km", firstDate),
                Distance(2L, "Office", "8.4 km", secondDate),
                Distance(3L, "Park", "1.0 km", thirdDate)
            ),
            rateRequest = StoredDistances.RateRequest.Available(position = 2)
        )
        whenever(resourceProvider.get(R.string.rate_app_prompt)).thenReturn("Do you enjoy using this app?")
        whenever(resourceProvider.get(R.string.rate_app_cta)).thenReturn("Rate it now")

        val result = mapper.map(storedDistances)

        assertEquals(4, result.size)
        assertEquals(
            DistanceSelectionUiModel.Distance(
                id = 1L,
                name = "Home",
                distance = "3.2 km",
                date = DATE_FORMAT.format(firstDate)
            ),
            result[0]
        )
        assertEquals(
            DistanceSelectionUiModel.Distance(
                id = 2L,
                name = "Office",
                distance = "8.4 km",
                date = DATE_FORMAT.format(secondDate)
            ),
            result[1]
        )
        assertEquals(
            DistanceSelectionUiModel.RateApp(
                title = "Do you enjoy using this app?",
                ctaText = "Rate it now"
            ),
            result[2]
        )
        assertEquals(
            DistanceSelectionUiModel.Distance(
                id = 3L,
                name = "Park",
                distance = "1.0 km",
                date = DATE_FORMAT.format(thirdDate)
            ),
            result[3]
        )
    }

    private companion object {

        val DATE_FORMAT = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.US)
    }
}
