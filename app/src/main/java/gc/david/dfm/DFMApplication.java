package gc.david.dfm;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import com.crashlytics.android.Crashlytics;

import java.util.Locale;

import gc.david.dfm.dagger.DaggerRootComponent;
import gc.david.dfm.dagger.RootComponent;
import gc.david.dfm.dagger.RootModule;
import gc.david.dfm.logger.DFMLogger;
import gc.david.dfm.migration.UpgradeHelper;
import gc.david.dfm.model.DaoMaster;
import gc.david.dfm.model.DaoSession;
import io.fabric.sdk.android.Fabric;

/**
 * Created by David on 28/10/2014.
 */
public class DFMApplication extends Application {

    private static final String TAG = DFMApplication.class.getSimpleName();

    private DaoSession  daoSession;
    private RootComponent rootComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }

        DFMLogger.logMessage(TAG, "onCreate");

        setupDatabase();
        setupDefaultUnit();

        rootComponent = DaggerRootComponent.builder().rootModule(new RootModule(this)).build();
    }

    public DaoSession getDaoSession() {
        DFMLogger.logMessage(TAG, "getDaoSession");

        return daoSession;
    }

    private void setupDatabase() {
        DFMLogger.logMessage(TAG, "setupDatabase");

        final UpgradeHelper helper = new UpgradeHelper(this, "DistanciasDB.db", null);
        final SQLiteDatabase db = helper.getWritableDatabase();
        final DaoMaster daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
    }

    private void setupDefaultUnit() {
        DFMLogger.logMessage(TAG, "setupDefaultUnit");

        // Set default unit if not already set
        final String defaultUnit = DFMPreferences.getMeasureUnitPreference(getBaseContext());
        if (defaultUnit == null) {
            DFMPreferences.setMeasureUnitPreference(getBaseContext(),
                                                    isAmericanLocale() ?
                                                    DFMPreferences.MEASURE_AMERICAN_UNIT_VALUE :
                                                    DFMPreferences.MEASURE_EUROPEAN_UNIT_VALUE);
        }
    }

    private boolean isAmericanLocale() {
        final Locale locale = getResources().getConfiguration().locale;
        return locale.equals(Locale.CANADA)
               || locale.equals(Locale.CHINA)
               || locale.equals(Locale.JAPAN)
               || locale.equals(Locale.KOREA)
               || locale.equals(Locale.TAIWAN)
               || locale.equals(Locale.UK)
               || locale.equals(Locale.US);
    }

    public RootComponent getRootComponent() {
        return rootComponent;
    }
}
