package gc.david.dfm.db;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

/**
 * DistancesDataSource is a data access object (DAO) to manage the database 
 * data.
 * 
 * @author David
 * @see <a href="http://en.wikipedia.org/wiki/Data_access_object">Data Access Object</a>
 * @see <a href="http://en.wikipedia.org/wiki/Single_responsibility_principle">Single responsibility principle</a>
 */
public class DistancesDataSource {
	
	private SQLiteDatabase database				= null;
	private DistancesDatabaseHelper dbHelper	= null;
	
	public DistancesDataSource(Context context){
		this.dbHelper = new DistancesDatabaseHelper(context);
	}
	
	public void open() throws SQLException {
		// Esto hay que ponerlo en un hilo a parte
		try {
			this.database = new OpenDatabase().execute().get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}
	
	public void close() {
		database.close();
	}
	
	public void insert(String nombre, LatLng current, LatLng destination, String distancia){
		Object[] args = {	nombre,
							current.latitude, current.longitude,
							destination.latitude, destination.longitude,
							distancia
						};
		
		// La fecha, como el _ID, se crea automáticamente
		database.execSQL(DatabaseContract.SQL_INSERT_ROW, args);
	}
	
	public void delete(long id){
		Object[] args = { id };
		
		database.execSQL(DatabaseContract.SQL_DELETE_ROW, args);
	}
	
	/**
	 * Get all distances in the database in an arraylist. We can do this, or in
	 * other way, we also can return the cursor and use a CursorAdapter to show
	 * the information. 
	 * 
	 * @return An ArrayList with distances; null, if there is no distances.
	 */
	public ArrayList<Distance> getAllDistances(){
		ArrayList<Distance> distancias = null;
		
		Cursor c = database.rawQuery(DatabaseContract.SQL_SELECT_ALL, null);
		if (c.moveToFirst()){
			distancias = new ArrayList<Distance>();
			while (!c.isAfterLast()){
				Distance d = cursorToDistance(c);
				distancias.add(d);
				c.moveToNext();
			}
		}
		
		c.close();
		
		return distancias;
	}
	
	/**
	 * Converts the data cursor to a Distance object.
	 * 
	 * @param cursor
	 *            The cursor with the data.
	 * @return A new Distance object.
	 */
	private Distance cursorToDistance(Cursor cursor){
		Distance distance =
				new Distance(cursor.getLong(0),
						cursor.getString(1),
						cursor.getDouble(2),
						cursor.getDouble(3),
						cursor.getDouble(4),
						cursor.getDouble(5),
						cursor.getString(6),
						cursor.getString(7));
		
		// Convertimos el formato de la fecha
//		DateFormat dF = SimpleDateFormat.getDateInstance(SimpleDateFormat.DEFAULT, locale);
//		String aux = dF.format(distance.getFecha());
//		distance.setFecha(aux);
		
		return distance;
	}
	
	private class OpenDatabase extends AsyncTask<Void, Void, SQLiteDatabase>{
		@Override
		protected SQLiteDatabase doInBackground(Void... params) {
			return dbHelper.getWritableDatabase();
		}
	}
}
