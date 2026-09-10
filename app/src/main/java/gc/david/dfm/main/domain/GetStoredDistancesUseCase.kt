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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetStoredDistancesUseCase(private val getDistancesUseCase: GetDistancesUseCase) {

    operator fun invoke(): Flow<StoredDistances> {
        return getDistancesUseCase()
            .map {
                val rateRequest = if (it.size > 1) {
                    StoredDistances.RateRequest.Available(position = 2)
                } else {
                    StoredDistances.RateRequest.NotAvailable
                }
                StoredDistances(it, rateRequest)
            }
    }
}
