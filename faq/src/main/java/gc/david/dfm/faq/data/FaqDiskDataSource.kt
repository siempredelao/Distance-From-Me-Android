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

package gc.david.dfm.faq.data

import gc.david.dfm.common.presentation.ResourceProvider
import gc.david.dfm.faq.R
import gc.david.dfm.faq.data.model.FaqEntity
import kotlinx.coroutines.delay

/**
 * Created by david on 19.12.16.
 */
class FaqDiskDataSource(private val resourceProvider: ResourceProvider) {

    private val questionsAndAnswers = mutableSetOf(
        FaqEntity(
            resourceProvider.get(R.string.faq_question_distance),
            resourceProvider.get(R.string.faq_answer_distance)
        ),
        FaqEntity(
            resourceProvider.get(R.string.faq_question_distance_current_position),
            resourceProvider.get(R.string.faq_answer_distance_current_position)
        ),
        FaqEntity(
            resourceProvider.get(R.string.faq_question_distance_any_position),
            resourceProvider.get(R.string.faq_answer_distance_any_position)
        ),
        FaqEntity(
            resourceProvider.get(R.string.faq_question_permission_current_position),
            resourceProvider.get(R.string.faq_answer_permission_current_position)
        ),
        FaqEntity(
            resourceProvider.get(R.string.faq_question_permission_any_position),
            resourceProvider.get(R.string.faq_answer_permission_any_position)
        ),
        FaqEntity(
            resourceProvider.get(R.string.faq_question_multiple_points),
            resourceProvider.get(R.string.faq_answer_multiple_points)
        ),
        FaqEntity(
            resourceProvider.get(R.string.faq_question_reset_map),
            resourceProvider.get(R.string.faq_answer_reset_map)
        ),
        FaqEntity(
            resourceProvider.get(R.string.faq_question_gps_accuracy),
            resourceProvider.get(R.string.faq_answer_gps_accuracy)
        )
    )

    suspend fun getFaqs(): Set<FaqEntity> {
        waitToMakeThisFeatureMoreInteresting()
        return questionsAndAnswers
    }

    private suspend fun waitToMakeThisFeatureMoreInteresting() = delay(1500L)
}