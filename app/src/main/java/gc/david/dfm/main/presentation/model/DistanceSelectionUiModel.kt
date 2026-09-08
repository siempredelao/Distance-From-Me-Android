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

package gc.david.dfm.main.presentation.model

sealed interface DistanceSelectionUiModel {

    data class Distance(
        val id: Long,
        val name: String,
        val distance: String,
        val date: String
    ) : DistanceSelectionUiModel

    data class RateApp(
        val title: String,
        val ctaText: String
    ) : DistanceSelectionUiModel
}