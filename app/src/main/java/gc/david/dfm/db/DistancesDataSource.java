package gc.david.dfm.db;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

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
	
	public void insert(String name, LatLng current, LatLng destination, String distance){
		Object[] args = {	name,
							current.latitude, current.longitude,
							destination.latitude, destination.longitude,
							distance
						};
		
		// La fecha, como el _ID, se crea automáticamente
		database.execSQL(DatabaseContract.SQL_INSERT_ROW, args);
	}
	
	public void delete(long id){
		Object[] args = { id };
		
		database.execSQL(DatabaseContract.SQL_DELETE_ROW, args);
	}
	
	public void deleteAll(){
		database.execSQL(DatabaseContract.SQL_DROP);
		database.execSQL(DatabaseContract.SQL_CREATE_TABLE);
	}
	
	/**
	 * Get all distances in the database in an arraylist. We can do this, or in
	 * other way, we also can return the cursor and use a CursorAdapter to show
	 * the information. 
	 * 
	 * @return An ArrayList with distances; null, if there is no distances.
	 */
	public ArrayList<Distance> getAllDistances(){
		ArrayList<Distance> distances = null;
		
		Cursor c = database.rawQuery(DatabaseContract.SQL_SELECT_ALL, null);
		if (c.moveToFirst()){
			distances = new ArrayList<Distance>();
			while (!c.isAfterLast()){
				Distance d = cursorToDistance(c);
				distances.add(d);
				c.moveToNext();
			}
		}
		c.close();
		
		return distances;
	}
	
	/**
	 * Converts the data cursor to a Distance object.
	 * 
	 * @param cursor
	 *            The cursor with the data.
	 * @return A new Distance object.
	 */
	private Distance cursorToDistance(Cursor cursor){
		return new Distance(cursor.getLong(0),
				cursor.getString(1),
				cursor.getDouble(2),
				cursor.getDouble(3),
				cursor.getDouble(4),
				cursor.getDouble(5),
				cursor.getString(6),
				cursor.getString(7));
	}
	
	private class OpenDatabase extends AsyncTask<Void, Void, SQLiteDatabase>{
		@Override
		protected SQLiteDatabase doInBackground(Void... params) {
			return dbHelper.getWritableDatabase();
		}
	}
}
