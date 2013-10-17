package gc.david.dfm.db;

import android.provider.BaseColumns;

/**
 * DatabaseContract class is a container for constants that define names for
 * tables, columns and queries.
 * 
 * @author David
 * 
 */
public final class DatabaseContract {
	// To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
	public DatabaseContract(){}
	
	public static abstract class DatabaseEntry implements BaseColumns {
		public static final String TABLA				= "entry";
		public static final String COLUMNA_NOMBRE		= "nombre";
		public static final String COLUMNA_LAT_ORIGEN	= "lat_origen";
		public static final String COLUMNA_LON_ORIGEN	= "lon_origen";
		public static final String COLUMNA_LAT_DESTINO	= "lat_destino";
		public static final String COLUMNA_LON_DESTINO	= "lon_destino";
		public static final String COLUMNA_DISTANCIA	= "distancia";
		public static final String COLUMNA_FECHA		= "fecha";
	}
	
	public static final String SQL_CREATE_TABLE =
			"CREATE TABLE " + DatabaseEntry.TABLA + " (" +
					DatabaseEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
						DatabaseEntry.COLUMNA_NOMBRE + " TEXT, " +
							DatabaseEntry.COLUMNA_LAT_ORIGEN + " REAL NOT NULL, " +
								DatabaseEntry.COLUMNA_LON_ORIGEN + " REAL NOT NULL, " +
									DatabaseEntry.COLUMNA_LAT_DESTINO + " REAL NOT NULL, " +
										DatabaseEntry.COLUMNA_LON_DESTINO + " REAL NOT NULL, " +
											DatabaseEntry.COLUMNA_DISTANCIA + " TEXT NOT NULL, " +
												DatabaseEntry.COLUMNA_FECHA + " TEXT DEFAULT CURRENT_DATE" +
			")";
	
	public static final String SQL_DROP =
			"DROP TABLE IF EXISTS " + DatabaseEntry.TABLA;
	
	
	public static final String SQL_INSERT_ROW =
			"INSERT INTO " + DatabaseEntry.TABLA + " (" +
					DatabaseEntry.COLUMNA_NOMBRE + ", " +
						DatabaseEntry.COLUMNA_LAT_ORIGEN + ", " +
							DatabaseEntry.COLUMNA_LON_ORIGEN + ", " +
								DatabaseEntry.COLUMNA_LAT_DESTINO + ", " +
									DatabaseEntry.COLUMNA_LON_DESTINO + ", " +
										DatabaseEntry.COLUMNA_DISTANCIA +
			") VALUES(" +
					"?, " +
						"?, " +
							"?, " +
								"?, " +
									"?, " +
										"?" +
			")";
	
	public static final String SQL_DELETE_ROW =
			"DELETE FROM " + DatabaseEntry.TABLA + " WHERE " + DatabaseEntry._ID + " = ?";
	
	public static final String SQL_SELECT_ALL = 
			"SELECT * FROM " + DatabaseEntry.TABLA;
			
}
