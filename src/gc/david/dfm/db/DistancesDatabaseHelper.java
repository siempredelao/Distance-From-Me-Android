package gc.david.dfm.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DistancesDatabaseHelper extends SQLiteOpenHelper {
	
	// Con esta variable controlamos el número de versión, no desde fuera
	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "DistanciasDB.db";

	public DistancesDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DatabaseContract.SQL_CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// En principio esto, ya se verá si se necesita modificar para próximas versiones
//		db.execSQL(DatabaseContract.SQL_DROP);
//		onCreate(db);
	}

}
