/*
 * Copyright (c) 2021 David Aguiar Gonzalez
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
package gc.david.dfm.executor

import android.os.Handler
import android.os.Looper

/**
 * MainThread implementation based on a Handler instantiated over the main looper obtained from
 * Looper class.
 *
 * @author Pedro Vicente Gómez Sánchez
 */
class NewMainThread {

    private val handler = Handler(Looper.getMainLooper())

    fun post(runnable: Runnable) {
        handler.post(runnable)
    }
}