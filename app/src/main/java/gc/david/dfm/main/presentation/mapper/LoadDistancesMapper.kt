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
import gc.david.dfm.main.domain.StoredDistances
import gc.david.dfm.main.presentation.model.DistanceSelectionUiModel
import java.text.DateFormat
import java.util.Locale

class LoadDistancesMapper(private val resourceProvider: ResourceProvider) {

    fun map(storedDistances: StoredDistances): List<DistanceSelectionUiModel> {
        val mappedDistances = storedDistances.distances
            .map { distance ->
                DistanceSelectionUiModel.Distance(
                    id = distance.id,
                    name = distance.name,
                    distance = distance.distance,
                    date = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())
                        .format(distance.date)
                )
            }
            .toMutableList<DistanceSelectionUiModel>()

        if (storedDistances.rateRequest is StoredDistances.RateRequest.Available) {
            mappedDistances.add(
                storedDistances.rateRequest.position,
                DistanceSelectionUiModel.RateApp(
                    resourceProvider.get(R.string.rate_app_prompt),
                    resourceProvider.get(R.string.rate_app_cta)
                )
            )
        }

        return mappedDistances
    }
}

