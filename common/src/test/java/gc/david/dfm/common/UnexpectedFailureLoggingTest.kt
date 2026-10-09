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

import android.util.Log
import gc.david.dfm.common.data.HttpException
import kotlinx.serialization.SerializationException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import timber.log.Timber
import java.net.SocketTimeoutException

class UnexpectedFailureLoggingTest {

    private data class LogEntry(val priority: Int, val tag: String?, val throwable: Throwable?)

    private val entries = mutableListOf<LogEntry>()
    private val recordingTree = object : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            entries += LogEntry(priority, tag, t)
        }
    }

    @BeforeEach
    fun setUp() {
        Timber.plant(recordingTree)
    }

    @AfterEach
    fun tearDown() {
        Timber.uproot(recordingTree)
    }

    @Test
    fun `logs a connectivity failure as a warning without the exception`() {
        logUnexpectedFailure(TAG, SocketTimeoutException("timeout"))

        val entry = entries.single()
        assertEquals(Log.WARN, entry.priority)
        assertEquals(TAG, entry.tag)
        assertNull(entry.throwable)
    }

    @Test
    fun `logs an HTTP error as an error with the exception`() {
        val error = HttpException(403)

        logUnexpectedFailure(TAG, error)

        val entry = entries.single()
        assertEquals(Log.ERROR, entry.priority)
        assertSame(error, entry.throwable)
    }

    @Test
    fun `logs a parsing failure as an error with the exception`() {
        val error = SerializationException("Unexpected JSON token")

        logUnexpectedFailure(TAG, error)

        val entry = entries.single()
        assertEquals(Log.ERROR, entry.priority)
        assertSame(error, entry.throwable)
    }

    private companion object {

        const val TAG = "Test"
    }
}
