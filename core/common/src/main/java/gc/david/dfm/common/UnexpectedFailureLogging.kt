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

import timber.log.Timber
import java.io.IOException

/**
 * Logs a failure that is not a modelled outcome. Connectivity failures are expected and frequent,
 * so they are logged without the exception (a Crashlytics breadcrumb in release builds). Anything
 * else is logged with it (a Crashlytics non-fatal), because it points to a bug or a changed
 * server contract.
 */
fun logUnexpectedFailure(tag: String, error: Throwable) {
    if (error is IOException) {
        Timber.tag(tag).w("Connectivity failure: %s", error)
    } else {
        Timber.tag(tag).e(error)
    }
}
