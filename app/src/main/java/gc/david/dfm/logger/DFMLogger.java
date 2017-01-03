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
