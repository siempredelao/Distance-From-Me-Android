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

package gc.david.dfm.onboarding.domain.model

/**
 * What the map can tell the user about its next step.
 */
enum class MapHint {

    /** The app measures from the current position, and doesn't know it yet. */
    WAITING_FOR_LOCATION,

    /** A long press measures from the current position. */
    MEASURE_FROM_LOCATION,

    /** A tap starts a path anywhere. */
    START_ANYWHERE,

    /** A tap adds a point to the path, and a long press finishes it. */
    ADD_OR_FINISH,

    /** The label of a drawn distance opens its details. */
    OPEN_DETAILS,
}
