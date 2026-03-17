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

package gc.david.dfm.showinfo.presentation.mapper

import gc.david.dfm.common.ResourceProvider
import gc.david.dfm.showinfo.R

class ShareInfoMessageMapper(private val resourceProvider: ResourceProvider) {

    fun getSubject(): String = APP_HEADER

    fun mapMessage(
        originAddress: String,
        destinationAddress: String,
        distance: String,
    ): String {
        val fromLabel = resourceProvider.get(R.string.share_distance_from_message)
        val toLabel = resourceProvider.get(R.string.share_distance_to_message)
        val thereAreLabel = resourceProvider.get(R.string.share_distance_there_are_message)

        return """$APP_HEADER
$fromLabel
$originAddress

$toLabel
$destinationAddress

$thereAreLabel
$distance"""
    }

    private companion object {

        const val APP_HEADER = "Distance From Me (http://goo.gl/0IBHFN)"
    }
}
