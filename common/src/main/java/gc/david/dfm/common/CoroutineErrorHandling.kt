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
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch

/**
 * Launches [block] in [viewModelScope] and passes any exception it throws to [onError].
 *
 * Cancellation is never passed to [onError]: coroutines treat it as normal completion, so it
 * never reaches a [CoroutineExceptionHandler].
 */
fun ViewModel.launchCatching(
    onError: (Throwable) -> Unit,
    block: suspend CoroutineScope.() -> Unit,
): Job = viewModelScope.launch(CoroutineExceptionHandler { _, error -> onError(error) }, block = block)

/**
 * Like [runCatching], but rethrows when the calling coroutine has been cancelled.
 *
 * Use it only where a failure must become a value, such as one of several parallel calls that
 * should not cancel the others. Otherwise let the exception propagate to [launchCatching].
 */
suspend inline fun <T> suspendRunCatching(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (error: Throwable) {
        currentCoroutineContext().ensureActive()
        Result.failure(error)
    }
