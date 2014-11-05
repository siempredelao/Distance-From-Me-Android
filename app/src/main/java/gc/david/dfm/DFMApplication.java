package gc.david.dfm;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import gc.david.dfm.migration.UpgradeHelper;
import gc.david.dfm.model.DaoMaster;
import gc.david.dfm.model.DaoSession;

/**
 * Created by David on 28/10/2014.
 */
public class DFMApplication extends Application {

    private DaoSession daoSession;

    @Override
    public void onCreate() {
        super.onCreate();

        setupDatabase();
    }

    private void setupDatabase() {
        final UpgradeHelper helper = new UpgradeHelper(this, "DistanciasDB.db", null);
        final SQLiteDatabase db = helper.getWritableDatabase();
        final DaoMaster daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }
}
