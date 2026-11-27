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

package gc.david.dfm.onboarding.presentation

import androidx.annotation.StringRes
import gc.david.dfm.R
import gc.david.dfm.onboarding.domain.model.MapHint

@get:StringRes
val MapHint.textRes: Int
    get() = when (this) {
        MapHint.WAITING_FOR_LOCATION -> R.string.map_hint_waiting_for_location
        MapHint.MEASURE_FROM_LOCATION -> R.string.map_hint_measure_from_location
        MapHint.START_ANYWHERE -> R.string.map_hint_start_anywhere
        MapHint.ADD_OR_FINISH -> R.string.map_hint_add_or_finish
        MapHint.OPEN_DETAILS -> R.string.map_hint_open_details
    }
