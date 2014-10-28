package gc.david.dfm.migration;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import gc.david.dfm.model.DaoMaster;

/**
 * A simple helper which determines which migration (if any) is required to be
 * applied when a database is opened.
 * <p/>
 * Created by David on 29/10/2014.
 */
public class UpgradeHelper extends DaoMaster.OpenHelper {

	public UpgradeHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
		super(context, name, factory);
	}

	/**
	 * Apply the appropriate migrations to update the database.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		switch (newVersion) {
			case 2:
				new MigrateV1ToV2().applyMigration(db, oldVersion);
				break;
			default:
				break;
		}
	}
}
