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

import androidx.lifecycle.ViewModel
import gc.david.dfm.testsupport.CoroutineExtension
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

class CoroutineErrorHandlingTest {

    @JvmField
    @RegisterExtension
    val coroutineExtension = CoroutineExtension()

    private val viewModel = object : ViewModel() {}

    @Test
    fun `launchCatching passes a thrown exception to onError`() {
        val exception = IOException("Network error")
        var receivedError: Throwable? = null

        viewModel.launchCatching(onError = { receivedError = it }) { throw exception }

        assertSame(exception, receivedError)
    }

    @Test
    fun `launchCatching does not pass cancellation to onError`() = runTest {
        var receivedError: Throwable? = null
        val job = viewModel.launchCatching(onError = { receivedError = it }) { awaitCancellation() }

        job.cancelAndJoin()

        assertNull(receivedError)
    }

    @Test
    fun `suspendRunCatching returns success with the block result`() = runTest {
        val result = suspendRunCatching { "value" }

        assertEquals(Result.success("value"), result)
    }

    @Test
    fun `suspendRunCatching returns failure with the thrown exception`() = runTest {
        val exception = IOException("Network error")

        val result = suspendRunCatching { throw exception }

        assertSame(exception, result.exceptionOrNull())
    }

    @Test
    fun `suspendRunCatching returns failure for a cancellation exception while still active`() = runTest {
        val result = suspendRunCatching { throw CancellationException("Not ours") }

        assertTrue(result.exceptionOrNull() is CancellationException)
    }

    @Test
    fun `suspendRunCatching rethrows when the calling coroutine is cancelled`() = runTest {
        var continuedAfterCancellation = false
        val job = launch {
            suspendRunCatching { awaitCancellation() }
            continuedAfterCancellation = true
        }
        testScheduler.runCurrent()

        job.cancelAndJoin()

        assertFalse(continuedAfterCancellation)
    }
}
