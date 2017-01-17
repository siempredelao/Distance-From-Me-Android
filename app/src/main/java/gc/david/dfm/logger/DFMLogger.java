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

package gc.david.dfm.logger;

import android.util.Log;

import com.crashlytics.android.Crashlytics;

import gc.david.dfm.BuildConfig;
import io.fabric.sdk.android.Fabric;

public class DFMLogger {

    public static void logMessage(final String tag, final String message) {
        if (isReleaseBuild()) {
            Crashlytics.log(tag + ": " + message);
        } else {
            Log.d(tag, message);
        }
    }

    public static void logException(final Exception exception) {
        if (isReleaseBuild()) {
            Crashlytics.logException(exception);
        } else {
            Log.e("Exception", "Exception", exception);
        }
    }

    public static void logEvent(final String eventName) {
        if (isReleaseBuild()) {
            Crashlytics.setBool(eventName, true);
        } else {
            Log.i("New event", eventName);
        }
    }

    private static boolean isReleaseBuild() {
        return !BuildConfig.DEBUG && Fabric.isInitialized();
    }
}
