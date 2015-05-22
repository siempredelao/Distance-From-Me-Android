package gc.david.dfm.logger;

import android.util.Log;

import com.crashlytics.android.Crashlytics;

import gc.david.dfm.BuildConfig;
import io.fabric.sdk.android.Fabric;

public class DFMLogger {

    public static void logMessage(final String tag, final String message) {
        if (!BuildConfig.DEBUG && Fabric.isInitialized()) {
            Crashlytics.log(tag + ": " + message);
        } else {
            Log.d(tag, message);
        }
    }

    public static void logException(final Exception exception) {
        if (!BuildConfig.DEBUG && Fabric.isInitialized()) {
            Crashlytics.logException(exception);
        } else {
            Log.e("Exception", "Exception", exception);
        }
    }

    public static void logEvent(final String eventName) {
        if (!BuildConfig.DEBUG && Fabric.isInitialized()) {
            Crashlytics.setBool(eventName, true);
        } else {
            Log.i("New event", eventName);
        }
    }
}
