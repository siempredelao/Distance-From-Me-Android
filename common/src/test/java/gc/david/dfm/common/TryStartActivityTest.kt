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
package gc.david.dfm.common

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class TryStartActivityTest {

    private val context = mock<Context>()
    private val intent = mock<Intent>()

    @Test
    fun `starts the activity and returns true when an app handles the intent`() {
        assertTrue(context.tryStartActivity(intent))

        verify(context).startActivity(intent)
    }

    @Test
    fun `returns false when no app handles the intent`() {
        whenever(context.startActivity(intent)).doThrow(ActivityNotFoundException())

        assertFalse(context.tryStartActivity(intent))
    }

    @Test
    fun `lets other failures propagate`() {
        whenever(context.startActivity(intent)).doThrow(SecurityException())

        assertThrows(SecurityException::class.java) { context.tryStartActivity(intent) }
    }
}
