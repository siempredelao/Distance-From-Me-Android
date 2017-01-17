/*
 * Copyright (c) 2017 David Aguiar Gonzalez
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

package gc.david.dfm;

import android.app.Application;

import com.crashlytics.android.Crashlytics;

import java.util.Locale;

import gc.david.dfm.logger.DFMLogger;
import gc.david.dfm.map.Haversine;
import io.fabric.sdk.android.Fabric;

/**
 * Created by David on 28/10/2014.
 */
public class DFMApplication extends Application {

    private static final String TAG = DFMApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }
        DFMLogger.logMessage(TAG, "onCreate");

        setupDefaultUnit();
    }

    private void setupDefaultUnit() {
        DFMLogger.logMessage(TAG, "setupDefaultUnit");

        final String defaultUnit = DFMPreferences.getMeasureUnitPreference(getBaseContext());
        if (defaultUnit == null) {
            DFMPreferences.setMeasureUnitPreference(getBaseContext(),
                                                    isAmericanLocale()
                                                    ? DFMPreferences.MEASURE_AMERICAN_UNIT_VALUE
                                                    : DFMPreferences.MEASURE_EUROPEAN_UNIT_VALUE);
        }
    }

    private boolean isAmericanLocale() {
        return Haversine.isAmericanLocale(Locale.getDefault());
    }

}
