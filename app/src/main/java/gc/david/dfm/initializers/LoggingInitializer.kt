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

package gc.david.dfm.initializers

import android.app.Application
import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import gc.david.dfm.BuildConfig
import timber.log.Timber

class LoggingInitializer(private val context: Context) : Initializer {

    override fun init(application: Application) {
        // This shall be better done through different flavors
        Timber.plant(if (shouldLogToCrashlytics()) ReleaseTree(context) else Timber.DebugTree())
    }

    private fun shouldLogToCrashlytics(): Boolean {
        return !BuildConfig.DEBUG && isInitialized()
    }

    private fun isInitialized(): Boolean {
        return FirebaseApp.getApps(context).isNotEmpty()
    }
}

class ReleaseTree(private val context: Context) : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, throwable: Throwable?) {
        if (priority < MIN_LOG_LEVEL) return

        if (throwable != null) {
            exception(throwable)
        } else {
            val logTag = tag ?: DEFAULT_TAG
            log("$logTag: $message")
        }
    }

    private fun log(message: String) {
        if (!isInitialized()) return
        FirebaseCrashlytics.getInstance().log(message)
    }

    private fun exception(throwable: Throwable) {
        if (!isInitialized()) return
        FirebaseCrashlytics.getInstance().recordException(throwable)
    }

    private fun isInitialized(): Boolean {
        return FirebaseApp.getApps(context).isNotEmpty()
    }

    companion object {

        private const val DEFAULT_TAG = "DFM"
        private const val MIN_LOG_LEVEL = Log.INFO
    }
}