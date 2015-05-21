package gc.david.dfm;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import com.crashlytics.android.Crashlytics;

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

    private DaoSession daoSession;

    @Override
    public void onCreate() {
        super.onCreate();
//        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
//        }
        DFMLogger.logMessage(TAG, "onCreate");

        setupDatabase();
    }

    private void setupDatabase() {
        DFMLogger.logMessage(TAG, "setupDatabase");

        final UpgradeHelper helper = new UpgradeHelper(this, "DistanciasDB.db", null);
        final SQLiteDatabase db = helper.getWritableDatabase();
        final DaoMaster daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
    }

    public DaoSession getDaoSession() {
        DFMLogger.logMessage(TAG, "getDaoSession");

        return daoSession;
    }
}
