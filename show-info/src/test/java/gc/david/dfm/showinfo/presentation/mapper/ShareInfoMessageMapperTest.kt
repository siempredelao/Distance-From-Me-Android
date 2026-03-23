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

import gc.david.dfm.common.presentation.ResourceProvider
import gc.david.dfm.showinfo.R
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ShareInfoMessageMapperTest {

    private val resourceProvider = mock<ResourceProvider>()
    private val mapper = ShareInfoMessageMapper(resourceProvider)

    @Test
    fun `getSubject returns app header`() {
        val result = mapper.getSubject()

        assertEquals("Distance From Me (http://goo.gl/0IBHFN)", result)
    }

    @Test
    fun `mapMessage creates formatted message`() {
        val originAddress = "123 Main St"
        val destinationAddress = "456 Oak Ave"
        val distance = "10 km"

        whenever(resourceProvider.get(R.string.share_distance_from_message))
            .thenReturn("From:")
        whenever(resourceProvider.get(R.string.share_distance_to_message))
            .thenReturn("To:")
        whenever(resourceProvider.get(R.string.share_distance_there_are_message))
            .thenReturn("Distance:")

        val result = mapper.mapMessage(originAddress, destinationAddress, distance)

        val expected = """Distance From Me (http://goo.gl/0IBHFN)
From:
123 Main St

To:
456 Oak Ave

Distance:
10 km"""

        assertEquals(expected, result)
    }

    @Test
    fun `mapMessage handles empty strings`() {
        whenever(resourceProvider.get(R.string.share_distance_from_message))
            .thenReturn("From:")
        whenever(resourceProvider.get(R.string.share_distance_to_message))
            .thenReturn("To:")
        whenever(resourceProvider.get(R.string.share_distance_there_are_message))
            .thenReturn("Distance:")

        val result = mapper.mapMessage("", "", "")

        val expected = """Distance From Me (http://goo.gl/0IBHFN)
From:


To:


Distance:
"""

        assertEquals(expected, result)
    }
}

