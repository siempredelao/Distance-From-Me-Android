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

package gc.david.dfm.main.domain

import gc.david.dfm.core.distances.domain.GetDistancesUseCase
import gc.david.dfm.core.distances.domain.model.Distance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

class GetStoredDistancesUseCaseTest {

    private val getDistancesUseCase = mock<GetDistancesUseCase>()
    private val useCase = GetStoredDistancesUseCase(getDistancesUseCase)

    @Test
    fun `when there are no saved distances returns rate request not available`() = runTest {
        val distances = emptyList<Distance>()
        whenever(getDistancesUseCase()).thenReturn(flowOf(distances))

        val result = useCase().first()

        assertEquals(
            StoredDistances(
                distances = distances,
                rateRequest = StoredDistances.RateRequest.NotAvailable
            ),
            result
        )
    }

    @Test
    fun `when there is more than one saved distance returns rate request available`() = runTest {
        val distances = listOf(
            Distance(1L, "Home", "3.2 km", Date(0)),
            Distance(2L, "Office", "8.4 km", Date(1000)),
            Distance(3L, "Park", "1.0 km", Date(2000))
        )
        whenever(getDistancesUseCase()).thenReturn(flowOf(distances))

        val result = useCase().first()

        assertEquals(
            StoredDistances(
                distances = distances,
                rateRequest = StoredDistances.RateRequest.Available(position = 2)
            ),
            result
        )
    }
}
