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
