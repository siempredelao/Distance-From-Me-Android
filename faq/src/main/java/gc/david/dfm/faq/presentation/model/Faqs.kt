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

package gc.david.dfm.faq.presentation.model

import gc.david.dfm.faq.R

internal val faqs = listOf(
    Faq(R.string.faq_question_distance, R.string.faq_answer_distance),
    Faq(R.string.faq_question_distance_current_position, R.string.faq_answer_distance_current_position),
    Faq(R.string.faq_question_distance_any_position, R.string.faq_answer_distance_any_position),
    Faq(R.string.faq_question_permission_current_position, R.string.faq_answer_permission_current_position),
    Faq(R.string.faq_question_permission_any_position, R.string.faq_answer_permission_any_position),
    Faq(R.string.faq_question_multiple_points, R.string.faq_answer_multiple_points),
    Faq(R.string.faq_question_reset_map, R.string.faq_answer_reset_map),
    Faq(R.string.faq_question_gps_accuracy, R.string.faq_answer_gps_accuracy),
)
