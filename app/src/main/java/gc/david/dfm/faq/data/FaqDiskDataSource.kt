/*
 * Copyright (c) 2022 David Aguiar Gonzalez
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

import gc.david.dfm.faq.data.model.Faq
import kotlinx.coroutines.delay

/**
 * Created by david on 19.12.16.
 */
class FaqDiskDataSource {

    private val questionsAndAnswers = mutableSetOf(
            Faq("How can I get a distance?",
                    "Just do a long press in the map :)"),
            Faq("How can I get a distance from my current position to any position in the map?",
                    "In the side bar, select \"Current position\" item and then perform a long press in the desired place in the map."),
            Faq("How can I get a distance from any position to another position in the map?",
                    "In the side bar, select \"Any position\" item ant then perform a long press in the desired place in the map."),
            Faq("Do I need to grant \"Location\" permission to get distances from current point?",
                    "Yes."),
            Faq("Do I need to grant \"Location\" permission to get distances from any point?",
                    "No."),
            Faq("How can I create a multiple points distance?",
                    "Perform single clicks in the desired points and a long click in the last one."),
            Faq("How can I reset the status of the map?",
                    "Select any position mode in the side bar."),
            Faq("Why my GPS position is not accurate?",
                    "This issue could be related to your device GPS sensor."))

    suspend fun getFaqs(): Set<Faq> {
        waitToMakeThisFeatureMoreInteresting()
        return questionsAndAnswers
    }

    private suspend fun waitToMakeThisFeatureMoreInteresting() = delay(1500L)
}