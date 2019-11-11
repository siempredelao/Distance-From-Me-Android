/*
 * Copyright (c) 2019 David Aguiar Gonzalez
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

package gc.david.dfm

import android.app.Application
import com.crashlytics.android.Crashlytics
import com.squareup.leakcanary.LeakCanary
import gc.david.dfm.logger.DFMLogger
import gc.david.dfm.map.Haversine
import io.fabric.sdk.android.Fabric
import java.util.*

/**
 * Created by David on 28/10/2014.
 */
class DFMApplication : Application() {

    private val isAmericanLocale: Boolean
        get() = Haversine.isAmericanLocale(Locale.getDefault())

    override fun onCreate() {
        super.onCreate()

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        LeakCanary.install(this)

        if (!BuildConfig.DEBUG) {
            Fabric.with(this, Crashlytics())
        }
        DFMLogger.logMessage(TAG, "onCreate")

        setupDefaultUnit()
    }

    private fun setupDefaultUnit() {
        DFMLogger.logMessage(TAG, "setupDefaultUnit")

        val defaultUnit = DFMPreferences.getMeasureUnitPreference(baseContext)
        if (defaultUnit == null) {
            DFMPreferences.setMeasureUnitPreference(baseContext,
                    if (isAmericanLocale)
                        DFMPreferences.MEASURE_AMERICAN_UNIT_VALUE
                    else
                        DFMPreferences.MEASURE_EUROPEAN_UNIT_VALUE)
        }
    }

    companion object {

        private val TAG = DFMApplication::class.java.simpleName
    }
}